/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package assets;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import save_load.Dictionary;
import save_load.SaveManager;
import save_load.Saveable;
import tools.DebugTool;
import global.Global;
import system.AssociationSaveManager;
import system.AssociationSystem;

public class Person extends Payment implements Saveable{

	public static String[] VARIABLE_NAMES = { 
		"ID",
		"name", 
		"surname",  
		"unioncard", 
		"mail", 
		"phonenumber", 
		"address", 
		"house_number",
		"postalcode", 
		"city", 
		"country", 
		"iban", 
		"bic",
		"membership_period",
		"prescribed",
		"membershipstarted", 
		"experience", 
		"yearsMember", 
		"tournaments", 
		"dms_subscribe", 
		"dms_paid"
	};
	private String SLC = null;
	private int ID;
	private String name;
	private String surname;

	private String unioncard;
	
	private String mail;
	private String phonenumber;
	
	private String address;
	private String house_number;
	private String postalcode;
	private String city;
	private String country;
	private String iban;
	private String bic;
	
	DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	private String membership_period;
	private boolean prescribed;
	private Date membershipStarted;
	private String experience;
	private int yearsMember;
	
	private boolean tournaments = false;
	
	private boolean dms_subscribe = false;
	private boolean dms_paid = false;
	
	private Couple[] levels = new Couple[0];
	
	public static Person shallowPerson(String serialized) {
		Dictionary d = DebugTool.readFormatted("[ID];[name];[surname]"
				+ ";[unioncard];[mail];[phonenumber];[address];[postalcode];"
				+ "[city];[country];"
				+ "[iban];[bic];[experience];[yearsMember];[tournaments];"
				+ "[level1];[partner1];"
				+ "[level2];[partner2];"
				+ "[level3];[partner3];"
				+ "[level4];[partner4];"
				+ "[salsachat];[dchat];[membershipstarted];[dms_subscribe];[dms_paid](;.*)*", serialized);
		Person p;
		if(d.get("id")!= null && !d.get("id").equals("")){
			p = new Person();			
		}
		else return null;
		for(String S : VARIABLE_NAMES){
			if(d.contains(S)){
				if(S.equalsIgnoreCase("name")){
					String processedName = 
						(d.get(S)
							.substring(0, 1)
							.toUpperCase()
						+d.get(S)
							.substring(1)
						)
						.trim()
						.replace("\u00DF", "ss");

					p.setProperty(S, processedName);
				}else if(S.equalsIgnoreCase("surname")){
					String lastname = d.get("surname").trim();
					lastname = lastname.replace("\u00DF", "ss");
					int lastSpace = lastname.lastIndexOf(" ");
					if(lastSpace == -1){
						p.setProperty(S, (lastname.substring(0, 1).toUpperCase()+lastname.substring(1)).trim());
					}
					else{
						lastname = lastname.substring(0,lastSpace+1) + lastname.substring(lastSpace+1,lastSpace+2).toUpperCase() + 
								lastname.substring(lastSpace+2);
						p.setProperty(S, lastname.trim());
					}
				}
				else{
					p.setProperty(S, d.get(S));
				}
			}
		}
		
		return p;
		
	}
	
	public static Person newPerson(String serialized){
		Dictionary d = DebugTool.readFormatted(
				"[ID];[name];[surname];[unioncard];[mail];[phonenumber];[address];[house_number];[postalcode];"
				+ "[city];[country];[iban];[bic];[membership_period];[prescribed];"
				+ "[level1];[period1];[partner1];"
				+ "[level2];[period2];[partner2];"
				+ "[level3];[period3];[partner3];"
				+ "[level4];[period4];[partner4];"
				+ "[membershipstarted](;.*)*"
				,
				serialized);
		Person p;
		if(d.get("id")!= null && !d.get("id").equals("")){
			p = new Person();			
		}
		else return null;
		for(String S : VARIABLE_NAMES){
			if(d.contains(S)){
				if(S.equalsIgnoreCase("name")){
					String processedName = 
						(d.get(S)
							.substring(0, 1)
							.toUpperCase()
						+d.get(S)
							.substring(1)
						)
						.trim()
						.replace("\u00DF", "ss");
					p.setProperty(S, processedName);
				}else if(S.equalsIgnoreCase("surname")){
					String lastname = d.get("surname").trim();
					lastname = lastname.replace("\u00DF", "ss");
					int lastSpace = lastname.lastIndexOf(" ");
					if(lastSpace == -1){
						p.setProperty(S, (lastname.substring(0, 1).toUpperCase()+lastname.substring(1)).trim());
					}
					else{
						lastname = lastname.substring(0,lastSpace+1) + lastname.substring(lastSpace+1,lastSpace+2).toUpperCase() + 
								lastname.substring(lastSpace+2);
						p.setProperty(S, lastname.trim());
					}
				}
				else{
					p.setProperty(S, d.get(S));
				}
			}
		}
		for(int i = 0; i < 4; i++){
			p.addLevel(	d.get("level"+String.valueOf(i+1)), 
						d.get("period")+String.valueOf(i), 
						d.get("partner"+ String.valueOf(i+1))
					  );
		}
		return p;
	}
	
	private Person(){
		
	}
	
	private Person(int ID,String name, String surname){
		this.ID = ID;
		this.name = name.trim();
		this.surname = surname.trim();
	}
	
	public static Person constructPerson(Dictionary construction, SaveManager sm){
		Person p = new Person();
		p.setSLC(construction.getName());
		for(String varname: VARIABLE_NAMES){
			p.setProperty(varname, construction.get(varname));
		}
		p.hookup(construction.get("levels"), "levels", sm);
		return p;
	}
	
	//Normal methods
	/**
	 * Method checks whether the level is already assigned to this person. 
	 * If not, it does so.
	 * @param c
	 */
	protected void addLevel(Couple c){
		for(int i = 0; i < levels.length; i++){
			if(c.sameLevel(levels[i])|| c == levels[i]) return;
		}
		levels = DebugTool.extend(levels, 1, Couple.class);
		levels[levels.length-1] = c;
	}
	/**
	 * Always checked whether the level already exists
	 * @param partner
	 * @param levelcode
	 * @param halfyear
	 * @param startAtHalf
	 */
	protected void addLevel(Person partner, byte levelcode, boolean halfyear, boolean startAtHalf){
		addLevel(new Couple(this,partner,levelcode,halfyear,startAtHalf));
	}

	protected void addLevel(String levelString, String personString){
		byte levelcode = Level.extract(levelString);
		if(levelcode == Level.NONE) return;
		Person partner = AssociationSystem.getSystem().getPerson(personString,true);
		if(partner == null){
			partner = new Person();
			partner.setProperty("name", personString);
		}
		else{
			for(Couple c : partner.getCouples()){
				if(c.<Byte>getProperty("level")!=levelcode) continue;
				if(AssociationSystem.correlate(c.<Person>getProperty("person2").getName(),(this.getName()))>0.81){
					c.setPerson2(this);
					this.addLevel(c);
					return;
				}
			}
			Global.println("Failed to find appropriate level @"+ partner.getName()+ " with " + this.getName());
		}
		
		boolean halfYear = levelString.contains("half-a-year");
		boolean startAtHalf = AssociationSystem.getSystem().halfYearSubscriptions;
		if(levelcode != Level.NONE){
			addLevel(partner, levelcode,halfYear,startAtHalf);
		}
	}
	
	protected void addLevel(String levelString, String periodString, String personString){
		byte levelcode = Level.extract(levelString);
		if(levelcode == Level.NONE) return;
		Person partner = AssociationSystem.getSystem().getPerson(personString,true);
		if(partner == null){
			partner = new Person();
			partner.setProperty("name", personString);
		}
		else{
			for(Couple c : partner.getCouples()){
				if(c.<Byte>getProperty("level")!=levelcode) continue;
				if(AssociationSystem.correlate(c.<Person>getProperty("person2").getName(),(this.getName()))>0.81){
					c.setPerson2(this);
					this.addLevel(c);
					return;
				}
			}
			Global.println("Failed to find appropriate level @"+ partner.getName()+ " with " + this.getName());
		}
		
		boolean halfYear = !periodString.equalsIgnoreCase("Full year");
		boolean startAtHalf = AssociationSystem.getSystem().halfYearSubscriptions;
		if(levelcode != Level.NONE){
			addLevel(partner, levelcode,halfYear,startAtHalf);
		}
	}
	
	
	//End normal methods
	
	//Saveable method implementations
	
	public <T> void setProperty(String varname, T val){ 
		switch(varname.toLowerCase()){ 
		case "name":
			name = (String) val;
			break;
		case "surname":
			surname = (String) val;
			break;
		case "id":
			ID = Integer.parseInt(((String)val).trim());
			break;
		case "unioncard":
		case "student_id":
			unioncard = (String) val;
			break;
		case "mail":
			mail = (String) val;
			break;
		case "phonenumber":
			phonenumber = (String) val;
			break;
		case "country":
			country = (String) val;
			break;
		case "city":
			city = (String) val;
			break;
		case "address":
			address = (String) val;
			break;
		case "house_number":
			house_number = (String) val;
			break;
		case "postalcode":
			postalcode = (String) val;
			break;
		case "iban":
			iban = (String) val;
			break;
		case "bic":
			bic = (String) val;
			break;
		case "membership_period":
			membership_period = (String) val;
			break;
		case "prescribed":
			prescribed = ((String) val).equalsIgnoreCase("true");
			break;
		case "membershipstarted":
			try {
				membershipStarted = df.parse((String)val);
				
			} catch (ParseException e) {}
			break;
		case "experience":
			experience = (String) val;
			break;
		case "yearsmember":
			try{
				yearsMember = Integer.parseInt((String)val);
			}catch(NumberFormatException e){
				if(DebugTool.DEBUG_MODE)System.out.println("Filled in value: " + (String)val);
				yearsMember = 0;
			}
			break;
		case "tournaments":
			tournaments = ((String)val).equalsIgnoreCase("true")||((String)val).toLowerCase().contains("yes");
			break;
		case "dms_subscribe":
			dms_subscribe = ((String) val).equals("true");
			break;
		case "dms_paid":
			dms_paid = ((String)val).equals("true");
			break;
		default:
			if(varname.startsWith("transaction")){
				int index = Integer.parseInt(varname.split("\\[")[1].split("\\]")[0]);
				if(index>=transactions.length){transactions = DebugTool.extend(transactions,index+1-transactions.length,Transaction.class);}
				transactions[index] = (Transaction) val;
			}
			else if(varname.startsWith("level")){
				int index = Integer.parseInt(varname.split("\\[")[1].split("\\]")[0]);
				if(index>=levels.length){levels = DebugTool.extend(levels, index+1-levels.length, Couple.class);}
				levels[index] = (Couple) val;
			}
			else System.out.println("No variable found by the name: " + varname); 
		} 

	}
	
	
	public String get(String varname){
		if(varname.equalsIgnoreCase("membershipstarted")){
			if(membershipStarted == null){
				System.out.println("Membership started not registered for " + getName());
			}
			return df.format(membershipStarted);
		}
		else {
			return getProperty(varname)==null? "[null]":getProperty(varname).toString();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String varname){ 
		Object returnObj = null;
		switch(varname.toLowerCase()){ 
		case "name":
			returnObj = name; 
			break;
		case "surname":
			returnObj = surname; 
			break;
		case "id":
			returnObj = ID; 
			break;
		case "unioncard":
		case "student_id":
			returnObj = unioncard; 
			break;
		case "mail":
			returnObj = mail; 
			break;
		case "phonenumber":
			returnObj = phonenumber; 
			break;
		case "country":
			returnObj = country; 
			break;
		case "city":
			returnObj = city; 
			break;
		case "address":
			returnObj = address; 
			break;
		case "house_number":
			returnObj = house_number;
			break;
		case "postalcode":
			returnObj = postalcode; 
			break;
		case "iban":
			returnObj = iban; 
			break;
		case "bic":
			returnObj = bic;
			break;
		case "membership_period":
			returnObj = membership_period;
			break;
		case "prescribed":
			returnObj = prescribed;
			break;
		case "membershipstarted":
			returnObj = membershipStarted; 
			break;
		case "experience":
			returnObj = experience; 
			break;
		case "yearsmember":
			returnObj = yearsMember; 
			break;
		case "tournaments":
			returnObj = tournaments; 
			break;
		case "dms_subscribe":
			returnObj = dms_subscribe; 
			break;
		case "dms_paid":
			returnObj = dms_paid; 
			break;
		case "balance":
			returnObj = balance;
			break;
		default:
			System.out.println("Property not found: \"" + varname+"\"");
			throw new IllegalArgumentException();
		} 
		
		return (T) returnObj;
	} 

	public String getName(){
		return (name + (isSubstitute()? "":(" " + surname))).trim();
	}
	
	
	public Couple[] getCouples(){
		return levels;
	}
		
	public Transaction[] getTransactions(){
		return transactions;
	}
	
	public Dictionary constructDictionary(AssociationSaveManager fhsm){
		return constructDictionary(fhsm);
	}
	
	public boolean isSubstitute(){
		return surname == null;
	}
	
	@Override
	public String getSLC() {
		return SLC;
	}
	public void setSLC(String slc){
		SLC = slc;
	}
	
	@Override
	/**
	 * Parses the object to standard output format.
	 */
	public String toString(){
		if(isSubstitute()){
			return name+"*";
		}
		else return name + " " + surname; 
	}

	public String print(){
		String output =  		"ID: " + ID + "\r\n"
				+	"Name: " +getName() + "\r\n"
				+ 	"Transactions: \r\n";
		for(int i = 0; i < transactions.length;i++){
			if(transactions[i] != null) output += transactions[i].toString() + "\r\n";
		}
		Dictionary d = new Dictionary();
		
		for(String S : VARIABLE_NAMES){
			d.add(S, get(S));
		}
		d.print();
		return output;
	}
	
	@Override
	public Dictionary constructDictionary(SaveManager sm) {
		if(isSubstitute()) return null;
		Dictionary d = new Dictionary();
		
		for(String S : VARIABLE_NAMES){
			d.add(S, get(S));
		}
		d.add("transactions", Saveable.arrayDictionarize(transactions, sm));
		d.add("levels", Saveable.arrayDictionarize(levels, sm));
		
		return d;
	}
	
	public void cleanup(){
		
	}
	
}
