/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package save_load;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tools.DebugTool;
import tools.Queue;
import global.Global;

public abstract class SaveManager {

	public static final char SLC_PREFIX = '#';
	
	/*
	 * Problematic cases: 
	 *  1. An object has not been saved, has no slc, but the object with reference to it must be saved. 
	 *  	The referenced object must be assigned an SLC and will be queued for saving.
	 *  2. Two objects refer to each other and eternally schedule each other for saving.
	 *  	When scheduled the state must change so that it will not be scheduled again. All objects in the savequeue must have the 
	 *  	ACTIVE_SCHEDULED state.
	 *  3. An object that is loaded has a loadhook that is not fired after all loading is done:
	 *   	Cause 1: The object that is referenced does not exist in the saves (therefore shouldn't be in the register). Clean up ref.
	 *   	Cause 2: The object was loaded before this one and the loadhook doesn't fire because the reference is never 'newly loaded'. 
	 *   		-> Check when throwing the hook if the object has been loaded already.
	 *   	Cause 3: The object has not been scheduled for loading, because it is in none of the loading files. -> A new queue must be made
	 *   			for loading scheduling.
	 *   
	 */
	
	/** 
	 * If it has no slc at all -> NO_SLC
	 * If it has an slc but is not yet in the register -> NOT_REGISTERED
	 * If it has an slc, is in the register, but is not loaded in the application -> INACTIVE
	 * If it has an slc, is in the register, is in the application and is to be saved -> ACTIVE_NOT_SAVED
	 * If it has an slc, is in the register, is in the application and is scheduled but not yet saved -> ACTIVE_SCHEDULED
	 * If it has an slc, is in the register, is in the application and has already been saved in this cycle -> ACTIVE_SAVED
	 * @author Stan
	 *
	 */
	public static enum SaveState {NO_SLC, NOT_REGISTERED, INACTIVE, ACTIVE_NOT_SAVED, ACTIVE_SCHEDULED, ACTIVE_SAVED};
	
	protected boolean ready = false;
	protected Queue<Saveable> savingQueue = new Queue<Saveable>();
	protected Queue<String> loadingQueue = null;
	protected LoadHook[] hooks = new LoadHook[0];
	
	protected BufferedWriter out;
	protected BufferedReader in;
	protected Saveable[] entities;
	protected File directory;
	protected String[] files;
	
	protected SaveableSystem system;
	protected int dataType;
	protected String[] dataTypes;
	protected String[] dataIdentifiers;
	protected int[] dataCounters;
	//Data identifiers will be a literal, a dash and a numerical code before the opening <. 
	
	protected String[] register = new String[128];
	protected Saveable[] objectRegister = new Saveable[128];
	protected SaveState[] stateRegister = new SaveState[128];
	
	public static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	
	public String getFileName(int saveID){
		return null;
	}
	
	/*
	 * De manier waarop het saven gebeurt is:
	 *  - Alle dataobjecten die gesaved moeten worden, gaan naar de state ACTIVE_NOT_SAVED of ACTIVE_NO_SLC;
	 *  - Ze worden allemaal in de Queue gezet. 
	 *  - Als er een object tegengekomen wordt dat nog niet gesaved wordt, gaat deze ook in de Queue en krijgt de ACTIVE_NOT_SAVED state
	 *  - Nadat een object opgeslagen is, krijgt hij de state ACTIVE_SAVED.
	 * De manier waarop het loaden gebeurt:
	 *  - List van files die geladen moeten worden
	 *  - Als er een object gevonden wordt tijdens het laden wordt er een loadhook aangemaakt, die gefired wordt als het object geladen wordt.
	 *  	Als het object al geladen is, moet deze direct gefired worden.
	 *  - Anders moet dit object op een of andere manier in de list komen van objecten die geladen moeten worden. 
	 *  
	 *  
	 */
	
	public static String writeDate(Date date){
		return SDF.format(date);
	}
	
	public static Date parseDate(String date){
		try {
			return SDF.parse(date);
		} catch (ParseException e) {
			Global.println("Invalid Date string: " + date);
			e.printStackTrace();
		}
		return null;
	}
	
	protected void initWriter(String filename){
		try{
			out = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(new File(directory + "/" + filename),true),StandardCharsets.UTF_8
					)
			);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public abstract void save();
	protected abstract void save(Saveable S);
	public void handleSave(Saveable S){
		setRegister(S.getSLC(), SaveState.ACTIVE_SAVED);
		save(S);
	}
	public abstract void load(File startfile);
	
	/* Suggestion for load(File)
	@Override
	public void load(File file){
		if(file.getName().equals("init.4hf")){
			try{
				in = new BufferedReader(new FileReader(file));
				String line;
				while((line = in.readLine()) != null){
					File loadFile = new File(file.getParentFile().getAbsolutePath()+"/"+line);
					if(loadFile.exists()) load(loadFile);
				}
				
			}catch(IOException e){e.printStackTrace();}
		}
		else{
			Dictionary[] ds = Dictionary.loadDictionaries(file);
			for(Dictionary d: ds){
				Saveable s = load(d);
				processLoad(s);
			}
		}
	}
	 */
	
	public abstract void load(Dictionary d);
	public void processLoad(Saveable S){
		addToRegister(S.getSLC(), S, SaveState.ACTIVE_SAVED);
		fireHooks(S, S.getSLC());
		system.add(S);
	}
	
	protected abstract int determineType(Saveable S);
	
	/**
	 * This function should be called on newly generated objects that do not yet have an slc. 
	 * Assigning an SLC automatically calls the addToRegister with the state NOT_REGISTERED.
	 * @param S
	 * @return
	 */
	protected String assignSLC(Saveable S){
		int type = determineType(S);
		String newSLC = dataIdentifiers[type]+"-"+dataCounters[type]++;
		S.setSLC(newSLC);
		addToRegister(newSLC, S, SaveState.NOT_REGISTERED);
		return newSLC;
	}
	/**
	 * Schedules an object for saving. When this object does not have an slc, it will be assigned one.
	 * Prequisite for scheduling is that the object has the state ACTIVE_NOT_SAVED.
	 * This function also makes sure that the object will be put in ACTIVE_SCHEDULED state and that already saved or scheduled objects will
	 * be ignored.
	 * @param S
	 */
	public void saveSchedule(Saveable S){
		if(S == null){return;}
		String slc = S.getSLC();
		SaveState ss = getSaveState(slc);
		switch(ss){
		case NO_SLC:
			slc = assignSLC(S);
//			addToRegister(slc, S, SaveState.NOT_REGISTERED); <- Automatically called.
		case NOT_REGISTERED:
			addToRegister(slc, S, SaveState.NOT_REGISTERED);
// 			continue here to the ACTIVE_NOT_SAVED case because that's what it is in now.
		case ACTIVE_NOT_SAVED:
			addToSaveQueue(S);
			break;
		case ACTIVE_SCHEDULED:
		case ACTIVE_SAVED:
		case INACTIVE:
			//The object is not in the scope of the application, or already saved or scheduled. Nothing has to be done anymore.
			return;	
		}
	}
	
	public void loadSchedule(String slc){
		if(slc == null) return;
		
	}
	
	/**
	 * When object 1 is saved and wants its references to be saved, the reference object is 'inserted' and must return an SLC 
	 * of the reference and schedule the reference to be saved too.
	 * @param S Saveable that needs to be queued for saving
	 * @return Placeholder that can be used as a reference to the saved data when loaded back in, <b>including preceding char.</b>
	 */
	public String insert(Saveable S){
		if(S == null) return "";
		String slc = S.getSLC();
		assert getSaveState(slc) != SaveState.INACTIVE;
		SaveState ss = getSaveState(slc);
		//Option 1 slc is null
		switch(ss){
		case NO_SLC:
			slc = assignSLC(S);				//Assigns standard SLC to the object 
			saveSchedule(S);					//Schedules it for saving
			return SLC_PREFIX+slc;			//Returns the SLC for insertion.
		case NOT_REGISTERED:				//The object has an SLC but is for some reason not registered yet.
			addToRegister(slc, S, SaveState.NOT_REGISTERED);
		default:
			saveSchedule(S);	
			return SLC_PREFIX+slc;	
		}
	}
	
	protected boolean inRegister(String slc){
		if(slc == null) return false;
		for(int i = 0; i < register.length; i++){
			if(register[i] != null && register[i].equals(slc)){
				return true;
			}
		}
		return false;
	}
	
	protected SaveState getSaveState(String slc){
		if(slc == null || slc.equals("")) return SaveState.NO_SLC;
		for(int i = 0; i < register.length; i++){
			if(register[i] != null && register[i].equals(slc)){
				return stateRegister[i];
			}
		}
		return SaveState.NOT_REGISTERED;
	}

	/**
	 * Adds a new object to the register. If it is null, then return immediately. If it is a new object without slc, assign it an slc
	 * @param slc
	 * @param S
	 * @param state
	 */
	protected void addToRegister(String slc, Saveable S, SaveState state){
		if(S == null) return; 
		if(slc == null) slc = assignSLC(S);
		adjustDataCounters(slc);
		if(inRegister(slc)){
			setRegister(slc, state);
			return;
		}
		for(int i = 0; i < register.length; i++){
			if(register[i] == null && objectRegister[i] == null){
				register[i] = slc;
				objectRegister[i] = S;
				if(state == SaveState.NOT_REGISTERED) state = SaveState.ACTIVE_NOT_SAVED;
				stateRegister[i] = state;
				return;
			}
		}
		register = DebugTool.extend(register, 32, String.class);
		objectRegister = DebugTool.extend(objectRegister, 32, Saveable.class);
		SaveState[] temp = new SaveState[stateRegister.length+32];
		System.arraycopy(stateRegister, 0, temp, 0, stateRegister.length);
		stateRegister = temp;
		register[register.length-32] = slc;
		objectRegister[objectRegister.length-32] = S;
		stateRegister[stateRegister.length-32] = state;
	}
	
	/*
	 * Uncontrolled change of state for a non-null slc. Does not check for the object to be null.
	 */
	protected void setRegister(String slc, SaveState state){
		if(slc == null || slc.equals("")) return;
		for(int i = 0; i < register.length;i++){
			if(register[i] != null && register[i].equals(slc)){
				stateRegister[i] = state;
				return;
			}
		}
	}
	
	public void release(String slc){
		for(int i = 0; i < register.length;i++){
			if(register[i]!= null && register[i].equals(slc)){
				register[i] = null;
				objectRegister[i].cleanup(); 
				objectRegister[i] = null;
				stateRegister[i] = SaveState.NO_SLC;
			}
		}
	}
	
	public void release(Saveable S){
		release(S.getSLC());
	}
	
	
	public int getCounter(){
		return dataCounters[dataType]++;
	}
	
	public int getCounter(String dataIdentifier){
		for(int i = 0; i < dataIdentifiers.length; i++){
			if(dataIdentifier.equalsIgnoreCase(dataIdentifiers[i])){
				return dataCounters[i];
			}
		}
		return 0;
	}
	
	private void adjustDataCounters(String slc){
		if(slc == null || slc.equals("")) return;
		int dividingChar = slc.indexOf("-");
		String dIdentifier = slc.substring(slc.indexOf("#")+1, dividingChar);
		int slcNumber = Integer.parseInt(slc.substring(dividingChar+1));
		for(int i = 0; i < dataIdentifiers.length; i++){
			if(dIdentifier.equalsIgnoreCase(dataIdentifiers[i])){
				dataCounters[i] = Math.max(dataCounters[i], slcNumber+1);
				return;
			}
		}
		
	}
	
	public SaveManager(SaveableSystem sys){
		system = sys;
	}
	
	public void addLoadHook(LoadHook lh){
		if(inRegister(lh.getSLC())){
			for(int i = 0; i < register.length; i++){
				if(register[i].equals(lh.getSLC())){
					lh.loadRef(objectRegister[i]);
					return;
				}
			}
		}
		
		hooks = DebugTool.extend(hooks, 1, LoadHook.class);
		hooks[hooks.length-1] = lh;
	}
	
	public void fireHooks(Saveable S, String SLC){
		for(int i = 0; i < hooks.length; i++){				//Go through all hooks
			if(hooks[i] != null){							//Check if the position is filled
				if(hooks[i].getSLC().equalsIgnoreCase(SLC)){//If the SLC's match...
					hooks[i].loadRef(S);					//Fire the hook...
					hooks[i] = null;						//and remove it from the list. 
				}
			}
		}
	}
	
	/** 
	 * Checks one last time, whether the saveable is marked to be scheduled.
	 * @param S
	 */
	protected void addToSaveQueue(Saveable S){
		assert getSaveState(S.getSLC()) == SaveState.ACTIVE_SCHEDULED : "Illegal state for adding to save queue.";
		if(!savingQueue.contains(S)) {
			savingQueue.add(S);			
		}
		setRegister(S.getSLC(), SaveState.ACTIVE_SCHEDULED);
		ready = true;
	}
	
	public boolean queueEmpty(){
		return savingQueue.isEmpty();
	}
	
	public boolean hooksEmpty(){
		for(int i = 0; i < hooks.length; i++){
			if(hooks[i] != null) return false;
		}
		return true;
	}
	
	protected void clearAll(){
		clearHooks();
		clearSaveQueue();
	}
	
	private void clearHooks(){
		hooks = new LoadHook[0];
	}
	
	private void clearSaveQueue(){
		savingQueue = new Queue<Saveable>();
	}
}
