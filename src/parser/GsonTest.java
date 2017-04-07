package parser;

/**
 * Copyright 2017 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gson.*;

public class GsonTest {

    final public static String DEFAULT_CHAR_SET = "UTF-8";

    public static void main(String[] args) {

        File json = new File("file.json");

        Map<String, Object> jsonJavaRootObject = new Gson().fromJson(read(json), Map.class);
        
    /*   
        System.out.println("JSON:\n" + jsonJavaRootObject);
        
        for (Map.Entry<String, Object> entry : jsonJavaRootObject.entrySet())
        {
            System.out.println(entry.getKey() + "/" + entry.getValue());
        }
        */
        
        JsonElement jelement = new JsonParser().parse(jsonJavaRootObject.toString());
        JsonObject  body = jelement.getAsJsonObject();
        
        HashMap<String,Info> test = new HashMap<String,Info>();
        analyzeBody(body,test);
        
        printInfo(test);
    }

    private static void analyzeBody(JsonObject jobject, HashMap<String,Info> map) {
    	Set<Map.Entry<String,JsonElement>> ola = jobject.entrySet();
        Iterator<Entry<String, JsonElement>> it = ola.iterator();
        
        while(it.hasNext()){
        	Entry<String, JsonElement> entry = it.next();
        	
        	String classType = entry.getValue().getClass().getSimpleName();
        	
        	switch (classType) {
			case "JsonArray":
			{
				//System.out.println("\nARRAY: \n" + entry.getKey().toString() + " = " + entry.getValue().toString());
				ArrayList<Info> tmp_array = new ArrayList<Info>();
				
				for(JsonElement elem : entry.getValue().getAsJsonArray()){
					HashMap<String,Info> tmp = new HashMap<String,Info>();
	        		analyzeBody(elem.getAsJsonObject(),tmp);
	        		tmp_array.add(new Info(tmp));
				}
				map.put(entry.getKey().toString(),new Info(tmp_array));
				break;
			}
			case "JsonPrimitive":
			{	
				//System.out.println("\nPRIMITIVE: \n" + entry.getKey().toString() + " = " + entry.getValue().toString());
				map.put(entry.getKey().toString(), new Info(entry.getValue().toString()));
				break;
			}
			case "JsonObject":
			{
				//System.out.println("\nOBJECT: \n" + entry.getKey().toString() + " = " + entry.getValue().toString());
				Info tmp = new Info(new HashMap<String,Info>());
        		analyzeBody(entry.getValue().getAsJsonObject(),tmp.getScope());
        		map.put(entry.getKey().toString(),tmp);
				break;
			}
			default:
			{
				System.out.println("OUTRO");
				break;
			}
			}
        	
        }
	}
    
    private static void printInfo(HashMap<String,Info> hm){
    	for(Entry<String, Info> entry : hm.entrySet()){
    		
    		System.out.println("\nKey   : " + entry.getKey());
    		System.out.print("Value : ");
    		
    		if(entry.getValue().isValue())
    			System.out.println(entry.getValue().getValue());
    		
    		else if(entry.getValue().isScope()){
    			
    			System.out.println("Scope\n{");
    			printInfo(entry.getValue().getScope());
    			System.out.println("\n}ENDScope");
    			
    		}
    		else if(entry.getValue().isArray()){
    			System.out.println("Array\n{");
    			
    			for(Info elem : entry.getValue().getArray())
    				{
    				System.out.println("Elem\n{");
    				printInfo(elem.getScope());
    				System.out.println("\n}ENDElem");
    				}
    			
    			System.out.println("\n}ENDArray");
    		}
    	}
    }

	/**
     * Given a File object, returns a String with the contents of the file.
     * 
     * <p>
     * If an error occurs (ex.: the File argument does not represent a file) returns null and logs the cause.
     * 
     * @param file
     *            a File object representing a file.
     * @return a String with the contents of the file.
     */
    public static String read(File file) {
        // Check null argument. If null, it would raise and exception and stop
        // the program when used to create the File object.
        if (file == null) {
            Logger.getLogger("info").info("Input 'file' is null.");
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();

        // Try to read the contents of the file into the StringBuilder

        try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                DEFAULT_CHAR_SET))) {

            // Read first character. It can't be cast to "char", otherwise the
            // -1 will be converted in a character.
            // First test for -1, then cast.
            int intChar = bufferedReader.read();
            while (intChar != -1) {
                char character = (char) intChar;
                stringBuilder.append(character);
                intChar = bufferedReader.read();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger("info").info("FileNotFoundException: " + ex.getMessage());
            return null;

        } catch (IOException ex) {
            Logger.getLogger("info").info("IOException: " + ex.getMessage());
            return null;
        }

        return stringBuilder.toString();
    }
}
