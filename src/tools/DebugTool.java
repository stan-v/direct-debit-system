/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package tools;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import save_load.Dictionary;

public class DebugTool {
	
	public static boolean DEBUG_MODE = false;
	
	public static OutputStream out = System.out;
	public static BufferedReader in;
	public static String encoding = "UTF-8";
	public static final String newline = System.lineSeparator();
	
	public static void printArray(String[] array){
		for(int i = 0; i < array.length; i++){
			try {
				if(array[i] != null) out.write(array[i].getBytes(encoding));
				else out.write("null".getBytes(encoding));
				out.write('\r');
				out.write('\n');
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void printNumberedArray(String[] array, boolean startWithZero){
		for(int i = 0; i < array.length; i++){
			try {
				out.write((i+(startWithZero?0:1)+". "+array[i]).getBytes(encoding));
				out.write('\r');
				out.write('\n');
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void printNumberedArray(String[] array){
		printNumberedArray(array,true);
	}
	public static String readFile(String pathname){
		String line = null,output = null;
		try{
			in = new BufferedReader(new FileReader(new File(pathname)));
			while((line = in.readLine()) != null){
				output += line + newline;
			}
			
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return output;
	}
	
	public static BufferedImage loadImage(String pathname){
		BufferedImage bi = null;
		try{
			bi = ImageIO.read(new File(pathname));
		}catch(IOException e){
			e.printStackTrace();
		}
		return bi;
	}
	
	public static Dictionary readFormatted(String format){
		String input = null;
		try{
			if(in == null || !in.ready()){
				in = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("Voer gegevens in: >> ");
			}
			input = in.readLine();
		}catch(IOException e){}
		return readFormatted(format, input);
	}
	
	/**
	 * Allows for reading one formatted line into a Dictionary
	 * The keys should be enclosed by square brackets and the separators cannot be space-only.
	 */
	public static Dictionary readFormatted(String format, String input) {
		Dictionary d = new Dictionary();
		String target = "\\[\\w+\\]";
		String replacement =  "([^;]*)";  //"([\\\\w ./ ^[;]]*)";
		Pattern p1 = Pattern.compile("\\[(\\w+)\\]");
		Matcher getKeys = p1.matcher(format);
		String[] keys = new String[0];
		String[] values = new String[0];
		while(getKeys.find()){
			keys = addStringToArray(keys, getKeys.group(1));
		}
		String newFormat = format.replaceAll(target,replacement);
		Pattern p2 = Pattern.compile(newFormat);
	
		String line = input;
		Matcher m = p2.matcher(line);
		if(m.matches()){
			for(int i = 0; i < Math.min(m.groupCount(), keys.length); i++){
				values = addStringToArray(values, m.group(i+1));
			}				
		}
		else{
			System.out.println("No match found:");
			System.out.println(format);
			System.out.println(line);
			return null;
		}
		
		if(keys.length != values.length){
			System.out.println("Key amount doensn't match value amount");
			System.out.println("KEYS:");
			printArray(keys);
			System.out.println("VALUES");
			printArray(values);
			return null;
		}
		d = new Dictionary(keys,values);
		if(DEBUG_MODE){d.print();}
		return d;
	}
	
	public static String[] extendStringArray(String[] array, int length){
		String[] newArray = new String[array.length+1];
		System.arraycopy(array,0,newArray,0, array.length);
		return newArray;
	}
	
	public static String[] addStringToArray(String[] array, String s){
		array = extendStringArray(array, 1);
		array[array.length-1] = s;
		return array;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T[] extend(T[] array, int extension, Class<?> clazz){
		T[] newArray = null;
		newArray = (T[])Array.newInstance(clazz, array.length+extension);
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}
	
}
