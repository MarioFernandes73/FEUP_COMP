package parser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Parser {

	private String JSONFile;
	//hashmap to store the global variables of the json file
	private HashMap<String,String> globalVariables = new HashMap<String,String>();
	
	public Parser(String JSONFile)
	{
		this.JSONFile = JSONFile;
		JSONParser parser = new JSONParser();
		
		try {

            Object obj = parser.parse(new FileReader(JSONFile));

            JSONArray variables = (JSONArray)obj.get("variables");

            

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

}
