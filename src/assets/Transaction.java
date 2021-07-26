/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package assets;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import save_load.Dictionary;
import save_load.LoadHook;
import save_load.SaveManager;
import save_load.Saveable;
import global.Global;
import system.AssociationSystem;

/**
 * This Transaction class creates a transaction between two parties.
 * The creditor is the person who should receive money and the debitor is the person who owes the creditor money.
 * E.g. if a member pays upfront for something for the association, the member is the creditor and the association is the debitor.
 * This means the association effectively owes the member the money.
 * If the member participates in an event, he is the debitor and the association is the creditor.
 * The amount should therefore always be positive in every normal case.
 * 
 * @author Stan
 *
 */
public class Transaction implements Saveable {
	
	public static int transactionIndex = 0;
	
	private String SLC = null;
	
	public static final String[] VARIABLE_NAMES = { 
		"identifier",
		"amount",
		"description", 
		"post", 
		"date"
	};
	
	protected final int identifier;
	
	private double amount;
	private String description = "[No description available]";
	private Payment creditor;	//Receiver
	private Payment debitor;	//Payer
	private String post = "[No post specified]";
	private Date date;
	
	/**
	 * Creates a transaction and automatically assigns itself to the parties.
	 * @param money
	 * @param payer
	 * @param receiver
	 * @param description
	 * @param date
	 * @return
	 */
	public static Transaction createTransaction(double money, Payment payer, Payment receiver, String description, String post,Date date){
		Transaction T = new Transaction(money, payer,receiver,description, post,date);
		if(payer != null) payer.addTransaction(T);
		if(receiver != null) receiver.addTransaction(T);
		return T;
	}
	
	public static Transaction createTransaction(Dictionary construction, SaveManager sm){
		double money = Double.parseDouble(construction.get("amount"));
		String description = construction.get("description");
		String post = construction.get("post");
		Date date = null;
		date = SaveManager.parseDate(construction.get("date"));
		Transaction T = new Transaction(money, null, null, description, post, date);
		sm.addLoadHook(new LoadHook(construction.get("creditor").substring(1)) {
			
			@Override
			protected void loadRef(Saveable S) {
				T.creditor = (Payment)S;
				((Payment)S).addTransaction(T);
			}
		});
		sm.addLoadHook(new LoadHook(construction.get("debitor").substring(1)) {
			
			@Override
			protected void loadRef(Saveable S) {
				T.debitor = (Payment)S;
				((Payment)S).addTransaction(T);
			}
		});
		T.setSLC(construction.getName());
		return T;
	}
	
	private Transaction(double money, Payment d, Payment c, String description, String post,Date date){
		if(amount < 0 ){
			Global.println("Negative payment issued");
		}
		if(c == null || d == null){
			Global.println("Creditor and/or Debitor are null. Loading?",1);
		}
		
		//Assume the transaction is legal from this point
		identifier = transactionIndex;
		SLC = "T-"+transactionIndex;
		transactionIndex++;
		
		amount = money;
		this.description = description;
		this.post = post;
		debitor = d;
		creditor = c;
		this.date = date;
	}	
	
	public Payment getCreditor(){
		return creditor;
	}
	
	public Payment getDebitor(){
		return debitor;
	}
	
	public double getAmount(){
		return amount;
	}
	
	public String getDescription(){
		return description == null ? "[No description available]" : description;
	}
	
	public Date getDate(){
		return date;
	}
	
	/**
	 * Removes a faulty transaction
	 */
	public void destroy(){
		AssociationSystem.getSystem().getSaveManager().release(this);
		cleanup();
	}

	public void cleanup(){
		creditor.removeTransaction(this);
		debitor.removeTransaction(this);
		amount = 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String varname){ 
		Object returnObj = null; 
		switch(varname.toLowerCase()){ 
		case "identifier":
			returnObj = identifier; 
			break;
		case "amount":
			returnObj = amount; 
			break;
		case "description":
			returnObj = description; 
			break;
		case "creditor":
			returnObj = creditor; 
			break;
		case "debitor":
			returnObj = debitor; 
			break;
		case "post":
			returnObj = post;
			break;
		case "date":
			returnObj = date; 
			break;
		default:
			System.out.println("Property not found: " + varname); 
		} 
		return (T) returnObj; 
	}

	@Override
	public Dictionary constructDictionary(SaveManager sm) {
		Dictionary d = new Dictionary();
		d.add("identifier", String.valueOf(identifier));
		d.add("amount", String.valueOf(amount));
		d.add("description", description);
		d.add("post", post);
		d.add("date", SaveManager.writeDate(date));
		d.add("debitor", sm.insert(debitor));
		d.add("creditor", sm.insert(creditor));
		return d;
	}

	
	@Override
	public String getSLC() {
		return SLC;
	}

	@Override
	public void setSLC(String slc) {
		
	}
	
	public String toString(){
		//Change toString of Transaction
		return toText();
	}
	
	public String toText(){
		new DecimalFormat();
		return debitor.getName()+ " -> "+ creditor.getName() + ": "+ NumberFormat.getCurrencyInstance().format(amount) + " ("+description+")";
	}

	@Override
	public <T> void setProperty(String fieldName, T val) {
		// TODO Auto-generated method stub
		
	}
	
}
