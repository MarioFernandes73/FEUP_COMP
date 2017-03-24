package parser;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.google.gson.*;

/*
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;*/

public class Parser {

	private String JSONFile;
	//hashmap to store the global variables of the json file
	private HashMap<String,String> globalVariables = new HashMap<String,String>();

	public Parser(String JSONFile)
	{
		
		
		  String json = "{buyer:'Happy Camper',creditCard:'4111-1111-1111-1111',"
			      + "lineItems:[{name:'nails',priceInMicros:100000,quantity:100,currencyCode:'USD'}]}";
		
		Map<String, Object> javaRootMapObject = new Gson().fromJson(json, Map.class);

		 System.out.println(
			        (
			            (Map)
			            (
			                (List)
			                (
			                    (Map)
			                    (
			                        javaRootMapObject.get("data")
			                    )
			                 ).get("buyer")
			            ).get(0)
			        ).get("translatedText")
			    );
		
	}
	
	/*	
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
        }*/

}
