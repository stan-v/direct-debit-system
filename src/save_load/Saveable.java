/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package save_load;

public interface Saveable {
	
	public static final int FULL_SAVE = 0;
	public static final int SHORT_SAVE = 0;
	
	/**
	 * Instructs the SaveManager how to save this object. Can be done in multiple ways (shortform/long)
	 * @param The SaveManager that is used to delegate the saving administration.
	 */
	//public void save(SaveManager sm);
	//public void save(SaveManager sm, int identifier);
	//public void save(SaveManager sm, int identifier, String preferredFile);
	/**
	 * Gets the value or reference to a field of the object.
	 * @param fieldName
	 * @return
	 */
	public <T> T getProperty(String fieldName);
	public <T> void setProperty(String fieldName, T val);
	/**
	 * Creates a full dictionary for all variables.
	 * @return
	 */
	public Dictionary constructDictionary(SaveManager sm);
	/* 
	 * CHECK IF VALID OBJECT
	 * for(String S : VARIABLE_NAMES){
	 * 		d.add(S, getProperty(S));
	 * }
	 * d.add("varname", Saveable.arrayDictionarize(varname, fhsm));
	 * ...
	 * 
	 */
	
	/**
	 * Creates a one-line description for recreating this object.
	 * @return
	 */
	//public String getShort();
	
	public String getSLC();
	public void setSLC(String slc);
	
	public default void hookup(String slcString, String variableName, SaveManager sm){
		if(slcString == null || slcString.equals("")) return;
		if(slcString.startsWith("{")){
			slcString = slcString.substring(1,slcString.length()-1);		//Cut off the accolades.
			String[] SLCs = slcString.split(",");							//Get distinct SLCs with prefix
			for(int i = 0; i < SLCs.length; i++){							//Cycle through the slcs.
				if(SLCs[i].length()==0) continue;							//If I get a "" value for the slc for example an empty array skip.
				String slc = SLCs[i].trim().substring(1); 					//First trim then remove the #	
				final int j = i;
				sm.addLoadHook(new LoadHook(slc) {
					
					@Override
					protected void loadRef(Saveable S) {
						setProperty(variableName+"["+j+"]", S);
					}
				});
			}
		}
		else{
			sm.addLoadHook(new LoadHook(slcString.substring(1).trim()) {
				
				@Override
				protected void loadRef(Saveable S) {
					setProperty(variableName, S);
				}
			});
		}
			
	}
	
	public default void destroy(SaveManager sm){
		cleanup();
		sm.release(this);
	}
	public void cleanup();
	
	
	/**
	 * Creates the value string for a dictionary for an array of Saveables and immediately calls them to be saved too.
	 * @param savs
	 * @param sm
	 * @return
	 */
	public static String arrayDictionarize(Saveable[] savs, SaveManager sm){
		if(savs == null || savs.length == 0){return "{}";}
		String valString = "{";
		for(Saveable S: savs){
			if(S != null) valString += sm.insert(S)+ ", ";
		}
		if(valString.contains(String.valueOf(SaveManager.SLC_PREFIX))){
			return valString.substring(0, valString.length()-2) + "}";
		}
		else return "{}";
	}
	public default String print(){
		return this.toString();
	}
	
	
	
}
