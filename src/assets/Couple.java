/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package assets;

import save_load.*;

public class Couple implements Saveable{
	
	public static final boolean YEAR = false;
	public static final boolean HALF_YEAR = true;
	
	private String SLC;
	
	private Person person1;
	private Person person2;
	private byte level;
	
	private boolean halfYear = false;
	private boolean halfYearStart = false;	
	
	
	/** 
	 * Constructs a couple between two people. Does not add the couple to the two people.
	 * @param lead
	 * @param follow
	 * @param level
	 * @param halfYear
	 * @param halfYearStart
	 */
	protected Couple(Person lead, Person follow, byte level, boolean halfYear, boolean halfYearStart){
		if(lead == null){
			System.out.println("Please specify lead.");
			return;
		}
		if(follow == null){
			System.out.println("Please specify follow.");
			return;
		}
		
		this.person1 = lead;
		this.person2 = follow;
		this.level = level;
		this.halfYear = halfYear;
		this.halfYearStart = halfYearStart;
	}
	
	protected Couple(){
		
	}
	
	public static Couple constructCouple(Dictionary construction, SaveManager sm){
		Couple c = new Couple();
		c.setSLC(construction.getName());
		c.level = Level.extract(construction.get("level"));
		c.halfYear = construction.get("halfYear").equals("true");
		c.halfYearStart = construction.get("halfYearStart").equals("true");
		c.hookup(construction.get("person1"), "person1", sm);
		c.hookup(construction.get("person2"), "person2", sm);
		return c;
	}
	
	/**
	 * Adds itself to its two participants.
	 */
	protected void bind(){
		person1.addLevel(this);
		person2.addLevel(this);
		
	}
	
	/**
	 * Prints the datatype in a clear way
	 */
	public String toString(){
		return "< \r\n"
				+ "\t" + "lead = " + (person1==null? "": person1.toString())+"\r\n"
				+ "\t" + "follow = " + (person2==null? "": person2.toString()) + "\r\n"
				+ "\t" + "level = " + Level.levelName(level) +"\r\n"
				+ "\t" + "halfYear = " + String.valueOf(halfYear) + "\r\n"
				+ "\t" + "halfYearStart = " + String.valueOf(halfYearStart) + "\r\n"
				+ "> \r\n";
				
	}

	public String print(){
		return Level.fancyName(level, halfYear);
	}
	

	public boolean isHalfYear(){
		return halfYear;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String varname) {
		Object returnObj = null; 
		switch(varname){ 
		case "person1":
			returnObj = person1; 
			break;
		case "person2":
			returnObj = person2; 
			break;
		case "level":
			returnObj = level; 
			break;
		case "halfYear":
			returnObj = halfYear; 
			break;
		case "halfYearStart":
			returnObj = halfYearStart; 
			break;
		default:
			System.out.println("No variable found by the name: " + varname); 
		} 
		return (T) returnObj; 
	}

	public <T> void setProperty(String varname, T val){
		switch(varname.toLowerCase()){
		case "person1":
			person1 = (Person)val;
			break;
			
		case "person2":
			person2 = (Person)val;
			break;
		}
	}
	

	public void setPerson1(Person newP1){
		person1 = newP1;
	}
	
	public void setPerson2(Person newP2){
		person2 = newP2;
	}

	/**
	 * returns whether comparison has the same level characteristics as the current level, e.g. same levelcode, start and duration.
	 * @param comparison
	 * @return
	 */
	public boolean sameLevel(Couple comparison){
		return comparison.level == level&& comparison.halfYear == halfYear && comparison.halfYearStart == halfYearStart;
	}
	
	@Override
	public Dictionary constructDictionary(SaveManager sm) {
		Dictionary d = new Dictionary();
		d.add("person1", sm.insert(person1));
		d.add("person2", sm.insert(person2));
		d.add("level", Level.levelName(level));
		d.add("halfYear", String.valueOf(halfYear));
		d.add("halfYearStart", String.valueOf(halfYearStart));
		
		return d;
	}


	@Override
	public String getSLC() {
		return SLC;
	}


	@Override
	public void setSLC(String slc) {
		SLC = slc;
	}

	@Override
	public void cleanup() {
		
	}
	
}
