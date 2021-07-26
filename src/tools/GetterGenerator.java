/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class GetterGenerator {

	public static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public static void main(String[] args){
		createGetter();
	}
	
	public static void createGetter(){
		try (BufferedWriter out = new BufferedWriter(new FileWriter(new File("script.txt")))){
			out.write("\tpublic <T> T getProperty(String varname){ \r\n");
			out.write("\t\tObject returnObj = null; \r\n");
			out.write("\t\tswitch(varname.toLowerCase()){ \r\n");
			String line = "";
			while(!(line = in.readLine()).equals(".")){
				line = line.trim();
				if(line.isEmpty()) continue;
				String[] words = line.split("=");
				words = words[0].split("//");
				words = words[0].split(" ");
				String var = words[words.length-1].replace(";", "").trim();
				
				out.write("\t\tcase \"" + var.toLowerCase() + "\":\r\n");
				out.write("\t\t\treturnObj = "+ var + "; \r\n");
				out.write("\t\t\tbreak;\r\n");
			}
			out.write("\t\tdefault:\r\n");
			out.write("\t\t\tSystem.out.println(\"Property not found: \" + varname); \r\n");
			out.write("\t\t} \r\n");
			out.write("\t\treturn (T) returnObj; \r\n");
			out.write("\t}\r\n");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void createSetter(){
		try (BufferedWriter out = new BufferedWriter(new FileWriter(new File("script.txt")))){
			out.write("\tpublic <T> void setProperty(String varname, T val){ \r\n");
			//out.write("\t\t \r\n");
			out.write("\t\tswitch(varname.toLowerCase()){ \r\n");
			String line = "";
			while(!(line = in.readLine()).equals(".")){
				line = line.trim();
				if(line.isEmpty()) continue;
				String[] words = line.split("=");
				words = words[0].split(" ");
				String var = words[words.length-1].replace(";", "");
				
				out.write("\t\tcase \"" + var.toLowerCase() + "\":\r\n");
				//out.write("\t\t\treturnObj = "+ var + "; \r\n");
				
				out.write("\t\t\t"+var+" = val;\r\n" );
				out.write("\t\t\tbreak;\r\n");
			}
			out.write("\t\tdefault:\r\n");
			out.write("\t\t\tSystem.out.println(\"No variable found by the name: \" + varname); \r\n");
			out.write("\t\t} \r\n");
			out.write("\t}\r\n");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void createVarNames(){
		try (BufferedWriter out = new BufferedWriter(new FileWriter(new File("script.txt")))){
			out.write("\tpublic static final String[] VARIABLE_NAMES = { \r\n");
			String line = "";
			while(!(line = in.readLine()).equals(".")){
				line = line.trim();
				if(line.isEmpty()) continue;
				String[] words = line.split("=");
				words = words[0].split(" ");
				String var = words[words.length-1].replace(";", "");
				
				out.write("\t\t\"" + var + "\", \r\n");
			}
			out.write("\t}; \r\n");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
