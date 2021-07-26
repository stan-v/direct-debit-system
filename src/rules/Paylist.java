/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;

import assets.Person;
import assets.Transaction;
import global.Global;
import system.AssociationSystem;

public class Paylist {
	
	public static BufferedReader in;
	
	public static void processPaylist(Person[] people, AssociationSystem savSys, File paylist){
		try(BufferedWriter out = new BufferedWriter(new FileWriter(new File("resources/failed_transactions.txt"),true))){
			in = new BufferedReader(new FileReader(paylist));
			
			String post = in.readLine().split(";")[0];
			String defDesc = in.readLine().split(";")[0];
			NumberFormat nf = NumberFormat.getInstance();
			NumberFormat cf = NumberFormat.getCurrencyInstance();
			double defPrice = 0;
			try {
				defPrice = nf.parse(in.readLine().split(";")[0]).doubleValue();
				String line = "";
				while((line = in.readLine()) != null){
					String[] entry = line.split(";");
					if(entry.length==0) continue; //Skips empty entries with ;; constructions
					String name = entry[0];
					String desc = defDesc; double price = defPrice;
					if(entry.length>=2) desc = entry[1].equals("")? defDesc : entry[1];
					if(entry.length>=3) {
						try{
							price = entry[2].equals("") ? defPrice : nf.parse(entry[2]).doubleValue();
						}catch(ParseException e) {
							price = entry[2].equals("") ? defPrice : cf.parse(entry[2]).doubleValue();
						}
					}
					assert price != 0;
					Person p = savSys.getPerson(name, true);
					if(p != null) Transaction.createTransaction(price, p, savSys.getAssociation(), desc, post, new Date());
					else {
						savSys.showMessage("Failed payment", name + " is not in the administration. \r\n"
								+ "Description: "+ desc + Global.NL
								+ "Price: " + price + Global.NL
								+ "Please contact this person separately");
						out.write(name + ", " + price + ", " + post + ", " + desc + "\r\n");
					}
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
}
