/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package assets;

import save_load.Dictionary;
import save_load.SaveManager;
import save_load.Saveable;

public class Association extends Payment implements Saveable {

	protected String name;
	
	public String getName(){
		return name;
	}
	
	@Override
	public void addTransaction(Transaction newT) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Transaction getTransaction(int identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeTransaction(Transaction T) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean replaceTransaction(Transaction oldT, Transaction newT) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T getProperty(String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dictionary constructDictionary(SaveManager sm) {
		
		return new Dictionary();
	}

	@Override
	/** 
	 * Returns static String A-0 for the main association.
	 */
	public String getSLC() {
		return "A-0";
	}

	@Override
	public void setSLC(String slc) {}

	@Override
	public String toString(){
		return name;
	}

	@Override
	public <T> void setProperty(String fieldName, T val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanup() {}
	
}
