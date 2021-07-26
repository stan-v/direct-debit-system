/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package system;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import save_load.Dictionary;
import save_load.Saveable;
import save_load.SaveableSystem;
import tools.DebugTool;
import assets.Association;
import assets.Couple;
import assets.TheAssociation;
import assets.Level;
import assets.Payment;
import assets.Person;
import assets.Transaction;
import global.Global;
import global.GlobalConfig;
import rules.Paylist;

public class AssociationSystem implements MouseListener, Runnable, KeyListener, SaveableSystem{
	
	static{
		GlobalConfig.load(Dictionary.loadDictionaries("resources/config.txt")[0]);
	}
	
	public static final String NAME = GlobalConfig.get("name");
	
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 750;
	
	private Thread thread = null;
	private boolean running = false;
	
	private static AssociationSystem system;
	private AssociationSaveManager asm;
	private AssociationGUIManager gui;
	protected Association theAssociation = new TheAssociation();
	private File directory;
	private File mainFile;
	
	public boolean halfYearSubscriptions = false;
	
	private Manipulator manipulator = new Manipulator();
	private Person[] leden = new Person[0];
	private Couple[] koppels = new Couple[0];
	
	private BufferedReader in;
	
	protected class Manipulator{
		private Saveable selectedObject = null;
		
		void printSaveable(){printSaveable(selectedObject);}
		private void printSaveable(Saveable S){
			Global.println(S != null? S.print() : "[null]");
		}
		void setSelected(Saveable S){
			selectedObject = S;
			printSaveable();
		}
		
		Saveable getSelected(){ return selectedObject;}
		
	}
	
	public static void main(String[] args){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AssociationSystem AS;
		if(args.length>0){
			 AS = new AssociationSystem(args[0]);
		}
		else {
			AS = new AssociationSystem(null);
		}
		AS.init();
	}
	
	public AssociationSystem(String filename){

		system = this;
		
		if(filename == null){
			JFileChooser jfc;
			jfc = new JFileChooser();
			jfc.showOpenDialog(null);
			mainFile = jfc.getSelectedFile();	
		}
		else {
			mainFile = new File(filename);
		}
		directory = mainFile.getParentFile();
		
	}
	
	private void init(){

		asm = new AssociationSaveManager(this);
		if(mainFile.getName().endsWith(".ron")){
			asm.load(mainFile);
		}
		else{
			oldLoad(mainFile);
		}
		start();
	}
	
	public static AssociationSystem getSystem(){
		return system;
	}
	
	public AssociationSaveManager getSaveManager(){
		return asm;
	}
	
	public Manipulator getManipulator() {
		return manipulator;
	}
	
	public Association getAssociation(){
		return theAssociation;
	}
	/**
	 * Laadt een bestand in in de vorm van het ledenbestand.
	 */
	private void oldLoad(File file){
		Global.println("Attempt to load: " + file.getName());
		try(BufferedReader tIn = new BufferedReader(new FileReader(file));){
			
			String line = "";
			while((line = tIn.readLine())!=null && !line.equals("")){
				if(line.startsWith(";")) break; //Dit betekent dat de eerste cel van de rij leeg is. 
				Person p = Person.newPerson(line);
				if(p!= null){
					asm.processLoad(p);
					leden = DebugTool.extend(leden, 1, Person.class);
					leden[leden.length-1] = p;
					Couple[] nieuweKoppels = p.getCouples();
					for(Couple C: nieuweKoppels){
						asm.processLoad(C);
					}
					asm.saveSchedule(p);
				}
			}
			fixCouples();
			
		}catch(IOException e){e.printStackTrace();}
	}
	
	
	@SuppressWarnings("unused")
	private void load(File file){
		asm.load(file);
	}
	
	public File getDirectory(){
		return directory;
	}
	
	public Person getPerson(String name,boolean tryToFind){
		Person p = getPerson(name);
		if(p != null) return p;
		else if(tryToFind){
			p = findPerson(name);
		}
		return p;
	}
	
	//Actual useful functions:
	public Person getPerson(String name){
		for(Person P: leden){
			if(P != null){
				//Bewerken van trial
				name = name.replace("  ", " ").trim();
				if((P.getProperty("name")+" "+P.getProperty("surname")).equalsIgnoreCase(name)){
					return P;
				}
			}
		}
		
		if(DebugTool.DEBUG_MODE)System.out.println("No person found by the name: "+ name);
		return null;
	}
	
	public Person findPerson(String name){
		if(name == null || name.length() == 0) return null;
		String firstLetter = name.substring(0, 1);
		double maxCorr = 0;
		String bestName = null;
		Person bestPerson = null;
		for(Person P : leden){
			if(P != null){
				if(P.<String>getProperty("name").substring(0, 1).equalsIgnoreCase(firstLetter)){
					double c = correlate(P.getProperty("name")+" "+P.getProperty("surname"),name);
					if(c>maxCorr && c>= 0.81){maxCorr = c; bestName = P.getProperty("name")+" "+P.getProperty("surname");bestPerson = P;}
					//System.out.println("Correlation: "+P.getProperty("name")+" "+P.getProperty("surname")+" + " +name + " = " + c);
				}
			}
		}
		if(bestPerson == null){
			Global.println("No Match found: " + name, 6);
		}
		else{
			int confirmation = JOptionPane.showConfirmDialog(gui==null?null : gui.jFrame, "Is "+ name + " this person: " + bestName + "? (" + (int)Math.round(maxCorr*100) + "%)");
			if(confirmation == JOptionPane.YES_OPTION) {
				return bestPerson;
			}
			else {
				return null;
			}
		}
		return bestPerson;
	}
	
	public void showMessage(String caption, String message) {
		JOptionPane.showMessageDialog(gui==null?null : gui.jFrame, message, caption, JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	public static double correlate(String s1, String s2){
		int correlation = 0;
		if(s1.length() == s2.length()){
			for(int i = 0; i < s1.length(); i++){
				if(s1.substring(i, i+1).equalsIgnoreCase(s2.substring(i, i+1))){
					correlation++;
				}
			}
			return ((double)correlation)/s1.length();
		}
		else{
			boolean s1Largest = s1.length()>s2.length();
			int sizeDiff = Math.abs(s1.length()-s2.length())+1;
			int[] xcorr = new int[sizeDiff];
			for(int lag = 0; lag < sizeDiff; lag++){
				correlation =0;
				for(int i = 0; i < (s1Largest? s2.length():s1.length()); i++){		//Goes through the shortest array.
					if(s1Largest){
						if(s1.substring(i+lag, i+1+lag).equalsIgnoreCase(s2.substring(i, i+1))) correlation++;
					}
					else{
						if(s2.substring(i+lag, i+1+lag).equalsIgnoreCase(s1.substring(i,i+1))) correlation++;
					}
				}
				xcorr[lag] = correlation;
			}
			correlation = 0;
			for(int i = 0; i < sizeDiff; i++){
				if(xcorr[i]>=3){
					correlation += xcorr[i];
				}
			}
			return ((double)correlation)/(s1Largest? s2.length():s1.length());
		}
	}
	
	private void fixCouples(){
		System.out.println("\r\nFIXING COUPLES--------------------------------------- \r\n");
		for(int i = 0; i < koppels.length; i++){
			Couple k = koppels[i];
			if(k != null){
				Person p1 = k.<Person>getProperty("person1");
				Person p2 = k.<Person>getProperty("person2");
				if(p1.isSubstitute()){
					k.setPerson1(AssociationSystem.getSystem().getPerson(p1.get("name"), true));
				}
				if(p2.isSubstitute()){
					k.setPerson2(AssociationSystem.getSystem().getPerson(p2.get("name"),true));
				}
			}
		}
	}
	
	public void add(Saveable S){
		if(S instanceof Person){
			leden = (Person[])addTo(leden, (Person)S);
		}
		else if(S instanceof Couple){
			koppels = (Couple[])addTo(koppels, (Couple)S);
		}
	}
	
	public Saveable[] addTo(Saveable[] array, Saveable S){
		array = DebugTool.extend(array, 1, S.getClass());
		array[array.length-1] = S;
		return array;
	}
	
	/**
	 * Makes an Excel (.csv) file that can be used for mail merging.
	 */
	private void createBatch(){
		JFileChooser jfc = new JFileChooser(directory);
		jfc.setFileFilter(new FileNameExtensionFilter("Comma Separated File (.csv)", "csv"));
		jfc.showSaveDialog(gui==null?null:gui.jFrame);
		String file = jfc.getSelectedFile().getAbsolutePath();
		if(file == null) return;
		if(!file.endsWith(".csv")) file += ".csv";
		try(BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(new File(file)),StandardCharsets.UTF_8
				)
		))
		{
			DecimalFormat df = new DecimalFormat("\u20AC #.00");
			out.write("Lidnr;Naam;Achternaam;Emailadres;Rekeningnummer;totaal;"
					+ "omschrijving1;post1;"
					+ "omschrijving2;post2;"
					+ "omschrijving3;post3;"
					+ "omschrijving4;post4;"
					+ "omschrijving5;post5;"
					+ "omschrijving6;post6;"
					+ "omschrijving7;post7;"
					+ "omschrijving8;post8;"
					+ "omschrijving9;post9;"
					+ "omschrijving10;post10\r\n");
			for(Person p : leden){
				if(Double.parseDouble(p.get("balance")) == 0) {Global.println(p.getName()+" has no balance.");continue;}
				out.write(Integer.parseInt(p.get("ID")) + ";"
							+ p.get("name") + ";"
							+ p.get("surname")+ ";"
							+ p.get("mail") + ";"
							+ p.get("iban") + ";"
							+ df.format(Double.parseDouble(p.get("balance")))+";"  );
				Transaction[] ts = p.getTransactions();
				if(ts.length>10){
					System.err.print("More than 10 transactions for: "+p.getName());
				}
				for(int i = 0; i < 10;i++){
					if(i<ts.length){
						out.write(ts[i].getDescription()+";");
						out.write(df.format((ts[i].getDebitor()==p?ts[i].getAmount():-ts[i].getAmount())));
					}
					else out.write(";");
					if(i < 9) out.write(";");
					else out.write("\r\n");
					
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This function prints the content of an ING Incasso Template.
	 * The content of the written file can be copy-pasted into the Template.
	 * @param description
	 */
	private void createIncassoTemplate(String description){
		JFileChooser jfc = new JFileChooser(directory);
		jfc.setFileFilter(new FileNameExtensionFilter("Comma Separated File (.csv)", "csv"));
		jfc.showSaveDialog(gui==null?null:gui.jFrame);
		String file = jfc.getSelectedFile().getAbsolutePath();
		if(file == null) return;
		if(!file.endsWith(".csv")) file += ".csv";
		try(BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(new File(file)),StandardCharsets.UTF_8
				)
		)){
			DecimalFormat df = new DecimalFormat("\u20AC #.00");
			
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

			for(Person p : leden){
				if(Double.parseDouble(p.get("balance")) == 0) {Global.println(p.getName()+" has no balance.");continue;}
				//Remove all accents from the name
				String name = Normalizer
						.normalize(p.getName().replace("\u00DF", "ss"), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
				
				Global.println(name);
				out.write(	  name + ";"
							+ p.get("iban") + ";"
							+ (Integer.parseInt(p.get("ID"))) + ";"
							+ df.format(Double.parseDouble(p.get("balance")))+";"
							+ description + ";"
							+ dateFormat.format(p.<Date>getProperty("membershipStarted")) + "\r\n");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	public void execute(String code){
		System.out.println("Execute: " + code);
		String prefix = "";
		if(!code.startsWith(prefix)) return;				//Required prefix
		code = code.substring(prefix.length());
		String func = code.split(" ")[0].toLowerCase();
		String[] args = new String[0];
		if(code.length() > func.length() + 1){
			code = code.substring(func.length()+1);
			args = new String[code.split(" ").length];
			boolean inString = false;
			int argNumber = 0;
			args[0] = "";
			for(int i = 0; i < code.length(); i++){
				char c = code.charAt(i);
				if(c=='"'){inString = !inString;}
				else if(c == ' '){
					if(!inString) {args[++argNumber] = "";}
					else args[argNumber]+=c;
				}
				else{
					args[argNumber]+=c;
				}
			}
		}
		else args = new String[]{};
		
		switch(func){
		case "t": 
			Transaction.createTransaction(Double.parseDouble(args[1].replace(",", ".")), getPerson(args[0],true), theAssociation, args[3].trim(), args[2].trim(), new Date());
			break;
		case "ct": //Clear all transactions
			for(Person p : leden) if(p!=null) p.clearAllTransactions();
			File failedTransactions = new File("resources/failed_transactions.txt");
			if(failedTransactions.exists()) failedTransactions.delete();
			break;
		case "cb":
			createBatch();
			break;
		case "cit":
			createIncassoTemplate(args[0]);
			break;
		case "s":
		case "save":
			asm.saveAll();
			break;
		case "p":
			Dictionary d = new Dictionary();
			d.setName("M-"+ asm.getCounter("M"));
			try{
				for(int i = 0; i < Person.VARIABLE_NAMES.length; i++){
					System.out.print(Person.VARIABLE_NAMES[i] + ": ");
					while(!in.ready()){}
					d.add(Person.VARIABLE_NAMES[i], in.readLine());
					
				}
				Person p = Person.constructPerson(d, asm);
				asm.processLoad(p);
			}catch(IOException e){
				e.printStackTrace();
			}
			break;
		case "list":
			JFileChooser listSelecter = new JFileChooser();
			listSelecter.setFileFilter(new FileNameExtensionFilter("Payment List (.csv)", "csv"));
			listSelecter.showOpenDialog(gui==null?null:gui.jFrame);
			File list = listSelecter.getSelectedFile();
			Paylist.processPaylist(leden, this, list);
			break;
		case "levellist":
			for(byte b : Level.ALL_LEVELS){
				try(BufferedWriter out = new BufferedWriter(new FileWriter(new File("LedenCheck " + Level.levelName(b) + ".csv")))){
					out.write("Naam;Niveau \r\n");
					for(int i = 0; i < leden.length; i++){
						Person lid = leden[i];
						
						Couple[] niveaus = lid.getCouples();
						for(int j = 0; j < niveaus.length; j++){
							if(niveaus[j].<Byte>getProperty("level") == b){
								out.write(lid.getName() + ";" + niveaus[j].print()+"\r\n");
							}
						}
					}
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			break;
		case "sort":
			Arrays.sort(leden, new Comparator<Person>(){

				@Override
				public int compare(Person o1, Person o2) {
					String s1 = o1.getName();
					String s2 = o2.getName();
					return (int)Math.signum(s1.compareTo(s2));
				}});
			for(int i = 0; i < leden.length; i++){
				Global.println(leden[i].getName());
			}
			break;
		case "select":
			switch(args[0].toLowerCase()){
			case "transaction":
				Saveable selected = manipulator.getSelected();
				if(selected instanceof Payment){
					for(Transaction T : ((Payment)selected).getTransactions()){
						if(T.getDescription().toLowerCase().contains(args[1].toLowerCase())){
							manipulator.setSelected(T);
						}
					}
				}
				break;
			default:
					manipulator.setSelected(getPerson(args[0], true));
			}
			break;
		case "delete":
			manipulator.getSelected().destroy(asm);
			break;
		case "load":
			JFileChooser jfc = new JFileChooser();
			jfc.showOpenDialog(gui==null?null:gui.jFrame);
			oldLoad(jfc.getSelectedFile());
			break;
		case "break":

			break;
		}
	}
	
	//End actual useful functions
	
	public void start(){
		running = true;
		if(thread == null){
			thread = new Thread(this);
		}
		thread.start();
		//Open the window
		AssociationSystem AS = this;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					gui = new AssociationGUIManager(AS);
					gui.jFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
	@Override
	public void run() {
		
		String line;
		in = new BufferedReader(new InputStreamReader(System.in));
		
		try (BufferedWriter out = new BufferedWriter(new FileWriter(new File("log.txt"),true));){
			while(running){
				if((line = in.readLine()) != null){
					if(line.equalsIgnoreCase("exit")){running = false;}
					out.write(line + "\r\n");
					execute(line);
				}	

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void terminate(){
		if(thread != null){
			running = false;
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void keyPressed(KeyEvent arg0) {}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}

	@Override
	public Saveable[] getAllSaveables() {
		// TODO Auto-generated method stub
		return leden;
	}
	
}
