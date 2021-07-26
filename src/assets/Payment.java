/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package assets;

import save_load.Saveable;
import tools.DebugTool;

public abstract class Payment implements Saveable{

	protected Transaction[] transactions = new Transaction[0];
	protected double balance;
	
	/**
	 * Adds one transaction to this person. New transactions can only be done by calling 
	 * new Transaction().
	 */
	
	public abstract String getName();
	
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
	
	public Transaction getTransaction(int identifier) {
		for(Transaction T : transactions){
			if(T.identifier == identifier){
				return T;
			}
		}
		return null;
	}
	
	public Transaction[] getTransactions(){
		return transactions;
	}

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
	
	public void clearAllTransactions(){
		for(Transaction T : transactions) T.destroy();
		transactions = new Transaction[0];
		balance = 0;
	}
	

	@Deprecated
	/** 
	 * Not yet implemented
	 */
	public boolean replaceTransaction(Transaction oldT, Transaction newT) {
		// TODO Auto-generated method stub
		return false;
	}

	
}
