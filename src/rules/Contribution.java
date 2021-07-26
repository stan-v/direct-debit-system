/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package rules;

import java.util.Date;

import system.AssociationSystem;
import assets.Couple;
import assets.Person;
import assets.Transaction;

public interface Contribution {

	public static final double[] YEAR_CONTRIBUTIONS = {0,35,60,80,80};
	public static final double[] HALF_YEAR_CONTRIBUTIONS = {0,25,40,50,55};
	
	public static void assign(Person p){
		int yearcount = 0, halfyearcount = 0;
		Couple[] cps = p.getCouples();
		for(int i = 0; i < cps.length; i++){
			if(cps[i].isHalfYear()){
				halfyearcount++;
			}
			else yearcount++;
		}
		if(yearcount > 0) {
			Transaction.createTransaction(YEAR_CONTRIBUTIONS[yearcount], p, AssociationSystem.getSystem().getAssociation(),
				String.valueOf(yearcount) + " year level" + ((yearcount > 1)?"s":""), "Contribution",p.<Date>getProperty("membershipStarted"));
		}
		if(halfyearcount > 0) {
			Transaction.createTransaction(HALF_YEAR_CONTRIBUTIONS[halfyearcount], p, AssociationSystem.getSystem().getAssociation(),
					String.valueOf(halfyearcount) + " half-a-year level" + ((halfyearcount > 1)?"s":""), "Contribution",p.<Date>getProperty("membershipStarted"));
		}
		if(YEAR_CONTRIBUTIONS[yearcount]+HALF_YEAR_CONTRIBUTIONS[halfyearcount] > 80){
			Transaction.createTransaction(80-YEAR_CONTRIBUTIONS[yearcount]-HALF_YEAR_CONTRIBUTIONS[halfyearcount],
					p, AssociationSystem.getSystem().getAssociation(),
					"Reduction due to contribution cap (\u20AC 80)", "Contribution", p.<Date>getProperty("membershipStarted"));
		}
		
	}
	
}
