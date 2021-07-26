/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package save_load;

import global.Global;
import tools.DebugTool;

import java.io.*;

public class Dictionary {
	
	public static final String PREFIX = "#";
	
	private String name = "";
	
	private String keyName = "Key";
	private String valueName = "Value";
	
	private String[] keys = new String[0];
	private String[] values = new String[0];
	
	
	public static void main(String[] args){
		Dictionary[] test = loadTables("prescriptions.csv", null);
		for( Dictionary d : test) {
			Global.println(d.toString());
		}
	}
	
	
	
	public Dictionary() {
		//Do absolutely nothing
	}
	
	public static String loadTemplate(String pathname){
		return loadTemplate(new File(pathname));
	}
	
	public static String loadTemplate(File file){
		try {
			return loadTemplate(new BufferedReader(new FileReader(file)));
		} catch (FileNotFoundException e) {
			System.out.println("File: \""+file.getPath() + "\" not found.");
			e.printStackTrace();
		}
		return null;
	}
	
	public static String loadTemplate(BufferedReader in){
		String output = null;
		try{
			char character;
			while((character = (char)(in.read())) != '<'){
				if(character == -1){
					return null;
				}
			}
			output = "";
			do {
				output += character;
			} while (((character = (char) (in.read())) != '>') );
			output += ">";
			in.close();
		} catch(IOException e){
			e.printStackTrace();
		}
		return output;
	}
	
	public static Dictionary[] loadDictionaries(String filename){
		return loadDictionaries(new File(filename));
	}
	
	public static Dictionary[] loadDictionaries(File file){
		Dictionary[] dictionaries = new Dictionary[0];
		try(BufferedReader in = new BufferedReader(new FileReader(file))){
			while(true){
				String output = null;
				char character;
				while ((character = (char) (in.read())) != '<') {
					if(character == (char)-1){
						return dictionaries;
					}
				}
				output = "";
				do {
					output += character;
				} while (((character = (char) (in.read())) != '>') );
				output += ">";
				
				Dictionary[] temp = new Dictionary[dictionaries.length+1];
				System.arraycopy(dictionaries, 0, temp, 0, dictionaries.length);
				dictionaries = temp;
				dictionaries[dictionaries.length-1] = new Dictionary(output);
			}
		} catch (IOException e) {
			if(e instanceof FileNotFoundException){
				System.out.println("File : \""+file.getPath()+"\" not found.");
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dictionaries;
	}
	
	/**
	 * Constructs an array of Dictionaries from a csv file. The format is the way the records are read from the file. 
	 * If format is <code> null </code>, then the first line of the file will be taken as format.
	 * @param fileName
	 * @param format
	 * @return
	 */
	public static Dictionary[] loadTables(String fileName, String format) {
		return loadTables(new File(fileName), format);
	}
	
	public static Dictionary[] loadTables(File file, String format) {
		try(BufferedReader in = new BufferedReader(new FileReader(file))){
			String[] globalKeys;
			if(format == null) {
				format = in.readLine();
				globalKeys = format.split(";");
				format = "";
				for(int i = 0; i < globalKeys.length; i++) {
					if(!globalKeys[i].startsWith("[")) globalKeys[i] = "[" + globalKeys[i];
					if(!globalKeys[i].endsWith("]")) globalKeys[i] = globalKeys[i] + "]";
					format += globalKeys[i] + ";";
				}
				
				format = format.substring(0, format.length()-1); //Strip last ;
				Global.println("format: " + format);
			}
			
			Dictionary[] output = new Dictionary[100];
			String line = null; int i = 0;
			while((line = in.readLine()) != null) {
				if(i >= output.length) {
					Dictionary[] temp = new Dictionary[output.length+100];
					System.arraycopy(output, 0, temp, 0, output.length);
					output = temp;
				}
				output[i] = DebugTool.readFormatted(format, line); 
				i++;
			}
			
			Dictionary[] temp = new Dictionary[i];
			System.arraycopy(output, 0, temp, 0, i);
			output = temp;
			return output;
			
		}catch(IOException e) {e.printStackTrace();}
		return null;
	}
	
	public Dictionary(String[] keys, String[] values){
		if(keys == null || values == null){
			System.out.println("Keys and values must be non-null");
			return;
		}
		if(keys.length != values.length){
			System.out.println("Keys and values must have the same length");
			return;
		}
		this.keys = keys;
		this.values = values;
	}
	
	/**
	 * Zet een String beginnend met < en eindigend met > om in een dictionary
	 * Voorbeeld (komma's zijn optioneel)
	 * <
	 * 	a = 1;
	 *  b = 2;
	 *  c = 3;
	 * >
	 */
	public Dictionary(String template) {
		int index = 0;
		if(template == null || template.length() == 0){
			System.out.println("Template cannot be null or empty");
		}
		if(!template.substring(0, 1).equals("<")){
			System.out.println("Template doesn't start with '<'.");
			return;
		}
		index++;
		int entry = 0;
		boolean end = false;
		boolean inString = false;
		String word = "";
		while(!template.substring(index, index+1).equals("\n")){		//Gooi de eerste regel weg.
			word += template.substring(index, index+1);
			index++;
		}
		name = word.trim();
		word = "";
		index++;
		while(!end){	
			String currentSymbol = template.substring(index, index+1);
			if(!inString){
				if(currentSymbol.equals(">")){
					end = true;
					break;
				}
				if(currentSymbol.equals("\t")||currentSymbol.equals("\r")){
					index++;
					continue;
				}
				if(currentSymbol.equals("=")){
					keys = extend(keys,1);
					keys[entry] = word.trim();
					word = "";
				}
				else if(currentSymbol.equals(";")||currentSymbol.equals("\n")){
					if(word != null && word.length()>0){
						values = extend(values,1);
						values[entry] = word.trim();
						word = "";
						entry++;
					}				
				}
				else if(currentSymbol.equals("\"")){
					inString = true;
				}
				else {
					word = word + currentSymbol;
				}
			}
			else{	//inString = true
				if(currentSymbol.equals("\\")){
					word = word + template.substring(index+1,index+2);
					index+=2;
					continue;
				}
				if(currentSymbol.equals("\"")){
					inString =  false;
					index++;
					continue;
				}
				if(currentSymbol.equals("\t")){
					index++; 
					continue;
				}
				word = word + currentSymbol;
			}
			
			index++;
		}
		
		return;
	}

	public void setName(String newName){
		name = newName;
	}
	
	public String getName(){
		return name;
	}
	
	public String getKeyName() {
		return keyName;
	}
	
	public String getValueName() {
		return valueName;
	}
	public boolean contains(String key){
		for(int i = 0; i < keys.length; i++){
			if(keys[i].equalsIgnoreCase(key)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isEmpty(){
		return !(keys.length>0);
	}
	
	/**
	 * Merges the dictionaries together. New values overwrite old values.
	 * @param d
	 */
	public void add(Dictionary d) {
		for(String key : d.keys) {
			if(!contains(key)) {add(key, d.get(key));}
			else {values[getIndexOfKey(key)] = d.get(key);}	//The value of the duplicate key is overriden by the new value from d.
		}
	}
	
	/**
	 * Adds one key-value pair without checking doubles.
	 * @param s1
	 * @param s2
	 */
	public void add(String s1, String s2){
		keys = extend(keys,1);
		values = extend(values,1);
		keys[keys.length-1] = s1;
		values[keys.length-1] = s2;
	}
	
	/**
	 * Adds keys without checking doubles.
	 * @param s1
	 * @param s2
	 */
	public void add(String[] s1, String[] s2){
		if(s1.length != s2.length){
			System.out.println("Length of arrays must be equal");
			return;
		}
		int length = s1.length;
		int start = keys.length;
		keys = extend(keys, length);
		values = extend(values, length);
		System.arraycopy(s1, 0, keys, start, length);
		System.arraycopy(s2, 0, values, start, length);
	}
	
	public void remove(String key) {
		if(!contains(key)) return;
		int index = getIndexOfKey(key);
		keys[index] = null;
		values[index] = null;
		
		
	}

	public String get(String key){
		return translate(key);
	}

	public String translate(String key){
		for(int i = 0; i < keys.length; i++){
			if(keys[i].equalsIgnoreCase(key)){
				return values[i];
			}
		}
		return null;
	}
	
	public String[] revert(String value){
		String[] foundKeys = new String[0];
		int ii = 0;
		for(int i = 0; i < values.length; i++){
			if(values[i].equalsIgnoreCase(value)){
				foundKeys = extend(foundKeys,1);
				foundKeys[ii++] = keys[i];
			}
		}
		return foundKeys;
	}
	
	public static String[] extend(String[] array, int extension){
		if(extension <= 0){
			System.out.println("Extension must be positive");
			return null;
		}
		String[] newArray = new String[array.length+extension];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	public String toString(){
		String output = "<\t"+ name +"\r\n";
		for(int i = 0; i < keys.length; i++){
			String printedValue = values[i];
			char[] escapeChars = new char[]{'\\', ';', '<', '>','=', '"'};
			for(char c : escapeChars){
				printedValue = printedValue.replace(String.valueOf(c), "\\"+c);
			}
			if(printedValue.contains("\r")||printedValue.contains("\n")){
				printedValue = '"'+ printedValue + '"';
			}
			output += "\t"+keys[i]+" = " + printedValue + "\r\n";
		}
		output += ">\r\n";
		return output;
	}
	
	public void print(){
		System.out.println(name);
		System.out.println(new String(new char[20+valueName.length()]).replace("\0", "="));
		if(keys == null||values==null){System.out.println("*No completed entries found*");return;}
		for(int i = 0; i < keys.length; i++){
			System.out.printf("%-20s%s%n", keys[i], 
					values[i] == null? "" : values[i].replaceAll("\n", "\n" + new String(new char[20]).replace("\0", " ")));
		}
		System.out.println();
	}
	
	/**
	 * Returns the index of the key in the list or -1 if it is not contained.
	 * @param key
	 * @return
	 */
	private int getIndexOfKey(String key) {
		for(int i = 0; i < keys.length; i++) {
			if(keys[i].equalsIgnoreCase(key)) return i;
		}
		return -1;
	}
	
	/**
	 * Moves all pairs one place down.
	 * @param startIndex First empty spot that must be filled.
	 * @return
	 */
	@SuppressWarnings("unused")
	private void collapse(int startIndex) {
		while(startIndex +1<keys.length) {
			keys[startIndex] = keys[startIndex+1];
			values[startIndex] = values[startIndex+1];
			startIndex++;
		}
		String[] newKeys = new String[keys.length-1];
		String[] newValues = new String[keys.length-1];
		System.arraycopy(keys, 0, newKeys, 0, keys.length-1);
		System.arraycopy(values, 0, newValues, 0, keys.length-1);
		keys = newKeys;
		values = newValues;
	}
	
}
