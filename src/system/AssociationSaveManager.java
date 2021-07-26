/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import save_load.Dictionary;
import save_load.SaveManager;
import save_load.Saveable;
import save_load.SaveableSystem;
import assets.Committee;
import assets.Couple;
import assets.Level;
import assets.Person;
import assets.Post;
import assets.Transaction;
import global.Global;

public class AssociationSaveManager extends SaveManager{
	
	public static final int PERSON = 0;
	public static final int COMMITTEE = 1;
	public static final int LEVEL= 2;
	public static final int COUPLE = 3;
	public static final int TRANSACTION = 4;
	public static final int POST = 5;
	
	{
		dataTypes = new String[]{	
				"Personen",
				"Commissies",
				"Lessen",
				"Koppels",
				"Transacties",
				"Posten"};
		
		dataIdentifiers = new String[]{
					"M", //Member
					"C", //Committee
					"L", //Level
					"K", //Couple
					"T", //Transaction
					"P" //Post
					};
		dataCounters = new int[dataTypes.length];
	}
	
	{
		//DEBUGGING PURPOSE
		register[0] = "A-0";
		objectRegister[0] = ((AssociationSystem)system).getAssociation();
		stateRegister[0] = SaveState.INACTIVE;
	}
	
	public AssociationSaveManager(SaveableSystem sys) {
		super(sys);
		directory = sys.getDirectory();
	}
	
	@Override
	public void load(File file){
		if(file.getName().equals("init.ron")){ 		//Loading bootstrap, calls load on subfiles
			try{
				in = new BufferedReader(new FileReader(file));
				String line;
				while((line = in.readLine()) != null){
					File loadFile = new File(file.getParentFile().getAbsolutePath()+"/"+line);
					if(loadFile.exists()) load(loadFile); //Nice.
				}
				
			}catch(IOException e){e.printStackTrace();}
		}
		else{										//Loading of subfiles
			Dictionary[] ds = Dictionary.loadDictionaries(file);
			for(Dictionary d: ds){
				if(d.getName().startsWith("M")){
					Person p = Person.constructPerson(d, this);
					processLoad(p);
				}
				if(d.getName().startsWith("K")){
					Couple c = Couple.constructCouple(d, this);
					processLoad(c);
				}
				if(d.getName().startsWith("T")){
					Transaction t = Transaction.createTransaction(d, this);
					processLoad(t);
				}
			}
		}
	}
	
	@Override
	public void load(Dictionary d) {
		Global.println("AssociationSaveManager.load(Dictionary) [Not implemented]");
		
	}
	
	protected int determineType(Saveable S){
		if(S instanceof Person){
			return PERSON;
		}
		else if(S instanceof Committee){
			return COMMITTEE;
		}
		else if(S instanceof Level){
			return LEVEL;
		}
		else if(S instanceof Couple){
			return COUPLE;
		}
		else if(S instanceof Transaction){
			return TRANSACTION;
		}
		else if(S instanceof Post){
			return POST;
		}
		else
			return -1;
	}
	
	public void saveAll(){
		File[] files = directory.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".bck");
			}
		});
		for(File f: files) f.delete();
		files = directory.listFiles(new FileFilter(){@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".ron") && !pathname.getName().contains("init.ron");
			}
		});
		for (int i = 0; i < files.length; i++) {
			String newPath = directory.getAbsolutePath()+"/"+files[i].getName().replace(".ron", ".bck");
			String.valueOf(files[i].renameTo(new File(newPath)));

		}
		
		for(Saveable S: objectRegister){
			if(S == null) continue;
			if(getSaveState(S.getSLC()) == SaveState.ACTIVE_SAVED){
				setRegister(S.getSLC(), SaveState.ACTIVE_NOT_SAVED);
			}
			saveSchedule(S);
		}
		save();
	}
	
	public void save(){
		Saveable S;
		if(ready){
			while((S = savingQueue.accept())!= null){
				//Save one instance
				if(S instanceof Person){
					dataType = PERSON;
					//save((Person)S);
					initWriter("Personen.ron");
				}
				else if(S instanceof Committee){
					dataType = COMMITTEE;
					//save((Committee)S);
				}
				else if(S instanceof Level){
					dataType = LEVEL;
					//save((Level)S);
				}
				else if(S instanceof Couple){
					dataType = COUPLE;
					//save((Couple)S);
					initWriter("Koppels.ron");
				}
				else if(S instanceof Transaction){
					dataType = TRANSACTION;
					//save((Transaction)S);
					initWriter("Transacties.ron");
				}
				else if(S instanceof Post){
					dataType = POST;
					//save((Post)S);
				}
				handleSave(S);
				
				//End of saving one entry
			}
			ready = false;
		}
	}
	
	
	protected void save(Saveable S){
		Global.println("Save: " + S.toString());
		Dictionary d = S.constructDictionary(this);
		d.setName(S.getSLC());
		try{
			out.write(d.toString());
			out.close();
		}catch(IOException e){e.printStackTrace();}
	}
	
	public void save(Person M){
		//For debugging
		if(M.isSubstitute()) return;
		
		initWriter("Personen.ron");
		Dictionary d = M.constructDictionary(this);
		try{
			out.write(M.getSLC());
			out.write(d.toString());
			out.close();
		}catch(IOException e){e.printStackTrace();}
	}
	
	public void save(Committee C){
		initWriter("Commissies.ron");
	}
	public void save(Level L){
		initWriter("Lessen.ron");
	}
	public void save(Couple K){
		initWriter("Koppels.ron");
		Dictionary d = K.constructDictionary(this);
		try{
			out.write(K.getSLC());
			out.write(d.toString());
			out.close();
		}catch(IOException e){e.printStackTrace();}
	}
	public void save(Transaction T){
		initWriter("Transacties.ron");
	}
	public void save(Post P){
		initWriter("Posten.ron");
	}
}
