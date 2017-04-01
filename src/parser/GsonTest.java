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
        JsonObject  jobject = jelement.getAsJsonObject();
        JsonArray jarray = jobject.getAsJsonArray("body");
        jobject = jarray.get(0).getAsJsonObject();
        analyzeBody(jobject);
        
        
        String result = jobject.get("type").toString();

        
    }

    private static void analyzeBody(JsonObject jobject) {
    	Set<Map.Entry<String,JsonElement>> ola = jobject.entrySet();
        Iterator<Entry<String, JsonElement>> it = ola.iterator();
        while(it.hasNext()){
        	Entry<String, JsonElement> entry = it.next();
        	System.out.println(entry.getKey().toString());
        	System.out.println(entry.getValue().toString());
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
