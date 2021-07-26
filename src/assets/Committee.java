/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package assets;

import save_load.Dictionary;
import save_load.SaveManager;
import tools.DebugTool;

public class Committee extends Payment {

	
	private String name;
	private String description;
	
	private Person[] members;
	
	private Transaction[] transactions;
	private double balance = 0;
	
	private double startBudget;
	private double adjustedBudget;
	
	
	protected Committee(String name, String description){
		this.name = name;
		this.description = description;
	}
	
	
	public String get(String varname){ 
		switch(varname){ 
		case "name":
			return name; 
		case "description":
			return description; 
		case "members":
			System.out.println("Members can be accessed by reference by getMembers()");
			return members.toString(); 
		case "transactions":
			System.out.println("Transactions can be accessed by reference by getTransactions()");
			return transactions.toString(); 
		case "balance":
			return String.valueOf(balance); 
		case "startBudget":
			return String.valueOf(startBudget); 
		case "adjustedBudget":
			return String.valueOf(adjustedBudget); 
		} 
		System.out.println("No variable found by the name: " + varname);
		return null;
	} 

	protected Person[] getMembers(){
		return members;
	}
	
	public Transaction[] getTransactions(){
		return transactions;
	}
	
	@Override
	public void addTransaction(Transaction newT) {
		transactions = DebugTool.extend(transactions, 1, Transaction.class);
		transactions[transactions.length-1] = newT;
		if(newT.getCreditor() == this){
			balance -= newT.getAmount();
		}
		else if(newT.getDebitor() == this){
			balance += newT.getAmount();
		}
	}

	@Override
	public Transaction getTransaction(int identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeTransaction(Transaction T) {
		int index = -1;
		for(int i = 0; i < transactions.length; i++){
			if(transactions[i] == T){
				index = i;
				break;
			}
		}
		if(T.getCreditor() == this){
			balance += T.getAmount();
		}
		else if(T.getDebitor() == this){
			balance -= T.getAmount();
		}
		if(index != -1){
			transactions[index] = null;
		}
	}

	@Override
	@Deprecated
	/**
	 * Not yet implemented
	 */
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
	public <T> void setProperty(String fieldName, T val) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Dictionary constructDictionary(SaveManager sm) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getSLC() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setSLC(String slc) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
