/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package assets;

public class Level {

	public static final byte NONE = 0; 	//0o00
	
	public static final byte DE = 1; 	//0o01
	public static final byte D = 2;		//0o02
	public static final byte C = 3;		//0o03
	public static final byte B = 4;		//0o04
	public static final byte A = 5;		//0o05
	
	public static final byte S1 = 9;	//0o11
	public static final byte S2 = 10;	//0o12
	
	public static final byte[] ALL_LEVELS = new byte[]{DE,D,C,B,A,S1,S2};
	
	public String teacher;
	
	@SuppressWarnings("unused")
	private Couple[] participants;
	
	public static byte extract(String levelname){
		if(levelname.startsWith("English D")|| levelname.startsWith("DE")){
			return DE;
		}
		else if(levelname.startsWith("Dutch D")){
			return D;
		}
		else if(levelname.startsWith("C")){
			return C;
		}
		else if(levelname.startsWith("B")){
			return B;
		}
		else if(levelname.startsWith("A")){
			return A;
		}
		else if(levelname.startsWith("Salsa 1")||levelname.startsWith("S1")){
			return S1;
		}
		else if(levelname.startsWith("Salsa 2")||levelname.startsWith("S2")){
			return S2;
		}
		else{
			return NONE;
		}
	}
	
	public static String levelName(byte levelcode){
		switch(levelcode){
		case 1:
			return "DE";
		case 2:
			return "D";
		case 3:
			return "C";
		case 4: 
			return "B";
		case 5: 
			return "A";
		case 9:
			return "S1";
		case 10: 
			return "S2";
		}
		return "[No valid level]";
	}
	
	public static String fancyName(byte levelcode, boolean halfYear){
		String output = "";
		switch(levelcode){
		case 1:
			output += "D (English, ";
			break;
		case 2:
			output += "D (Dutch, ";
			break;
		case 3:
			output += "C (";
			break;
		case 4: 
			output += "B (";
			break;
		case 5: 
			output += "A (";
			break;
		case 9:
			output += "Salsa 1 (";
			break;
		case 10: 
			output += "Salsa 2 (";
			break;
		default:
			return "[No valid level]";
		}
		if(halfYear){
			output += "half-a-year)";
		}
		else output += "year)";
		return output;
	}
	
	
}
