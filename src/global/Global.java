/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package global;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.Normalizer;

public class Global {

	public static final String NL = System.lineSeparator();
	
	public static final boolean ON = true;
	public static final boolean OFF = false;
	
	public static boolean silent = false;
	
	public static final int STANDARD = 5;
	
	protected static boolean[] errorReporting = new boolean[]{false,false,false,true,true,true,true,true,true,true};
	
	protected static BufferedWriter outputChannel = new BufferedWriter(new OutputStreamWriter(System.out));
	
	
	public static void println(String message){
		print(message+NL);
	}
	
	public static void println(Object obj){
		if(obj == null) print("[null]"+NL);
		else print(obj.toString()+NL);
	}
	
	public static void println(String message, int errorChannel){
		print(message+NL, errorChannel);
	}
	
	public static void println(Object obj, int errorChannel) {
		if(obj == null) print("[null]"+NL, errorChannel);
		print(obj.toString()+NL, errorChannel);
	}
	
	public static void print(String message){
		print(message, STANDARD);
	}
	
	public static void print(String message, int errorChannel){
		if(errorChannel< errorReporting.length){
			if(errorReporting[errorChannel] && !silent){
				try {
					outputChannel.write(message);
					outputChannel.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void forcePrintln(String message){forcePrint(message+NL);}
	public static void forcePrint(String message){
		try{
			outputChannel.write(message);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	public static String normalize(String text) {
		return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[\\p{M}]", "");
	}
	
}
