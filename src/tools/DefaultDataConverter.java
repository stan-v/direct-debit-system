/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package tools;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;

public class DefaultDataConverter extends DataConverter{

	{
		dateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		date = new SimpleDateFormat("dd-MM-yyyy");
		currency = new DecimalFormat("\u20AC #.00");
	}
	
	
	public Object fromString(String string) {
		//TODO implement
		return null;
	}
	
	public String toString(Object object) {
		//TODO implement
		return null;
	}
	
	@SuppressWarnings("unused")
	private String normalize(String nonASCII) {
		nonASCII = nonASCII.replace("\u00DF", "ss"); //Replaces Sharp s (�) by "ss"
		return Normalizer
		.normalize(nonASCII, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}
}
