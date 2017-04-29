package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cli.Resources;
import cli.Resources.JSONType;

public class Parser 
{
	public ArrayList<SymbolTable> tables = new ArrayList<>();
	public Node hir;

	public Parser(String filename)
	{
		File json = new File(filename);

		@SuppressWarnings("unchecked")
		Map<String, Object> jsonJavaRootObject = new Gson().fromJson(read(json), Map.class);

		/*System.out.println("JSON:\n" + jsonJavaRootObject);
	    for (Map.Entry<String, Object> entry : jsonJavaRootObject.entrySet())
	        System.out.println(entry.getKey() + "/" + entry.getValue());*/

		JsonElement jelement = new JsonParser().parse(jsonJavaRootObject.toString());
		JsonObject  body = jelement.getAsJsonObject();

		hir = new Node(JSONType.START);
		analyzeBody(body,hir);

		System.out.println("\n------- START PRINTING -------");
		
		printInfo(hir);	
	}

	private void analyzeBody(JsonObject jobject, Node node) 
	{
		System.out.println("+++++");

		String key;
		String classType;
		String value;
		Node newNode = null;

		Set<Map.Entry<String,JsonElement>> obj = jobject.entrySet();
		Iterator<Entry<String, JsonElement>> it = obj.iterator();

		while(it.hasNext())
		{
			Entry<String, JsonElement> entry = it.next();

			classType = entry.getValue().getClass().getSimpleName();
			key = entry.getKey().toString();
			value = entry.getValue().toString();

			switch (classType) 
			{
			case "JsonArray":
				System.out.println("\nARRAY: \n" + key + " = " + value);
			
				for(JsonElement elem : entry.getValue().getAsJsonArray())
				{
					if(key.equals("params"))
					{
						newNode = new Node(JSONType.PARAM);
						analyzeBody(elem.getAsJsonObject(),newNode);
						node.addAdj(newNode);
						newNode = null;
					}
					else if(newNode != null){
						analyzeBody(elem.getAsJsonObject(),newNode);
						node.addAdj(newNode);
					}
					else
						analyzeBody(elem.getAsJsonObject(),node);
				}

				break;
			case "JsonPrimitive":
				System.out.println("\nPRIMITIVE: \n" +  key + " = " + value);

				//function : type(FUNCTION), specification(function name), reference(NULL)
				if(value.equals("\"FunctionDeclaration\""))
				{
					//create new Node
					newNode = new Node(JSONType.FUNCTION);
					//create one symbol table per function
					tables.add(new SymbolTable());
				}
				else if(key.equals("name") && (node.getType() == JSONType.FUNCTION))
				{
					//set function name at node and at the symbolTable
					node.setSpecification(value);
					addNameToLastST(value);
				}
				//parameter : type(PARAM), specification(NULL), reference(var name and DataType)
				else if(key.equals("name") && node.getType() == JSONType.PARAM)
				{
					//param descriptor starts with unknown dataType
					Descriptor d = new Descriptor(value);
					//add parameter to SymbolTable and set a reference to the descriptor at the HIR
					addParamToLastST(d);
					node.setReference(d);
				}
				//local variable : type(VARIABLEDECLARATION), specification(NULL), reference(var name and DataType)
				else if(value.equals("\"VariableDeclaration\""))
				{
					//create new Node
					newNode = new Node(JSONType.VARIABLEDECLARATION);
				}
				else if(key.equals("name") && node.getType() == JSONType.VARIABLEDECLARATION)
				{
					//create descriptor, add to node and to SymbolTable
					Descriptor d = new Descriptor(value);
					node.setReference(d);
				}

				break;
			case "JsonObject":
				System.out.println("\nOBJECT: \n" +  key + " = " + value);
				
				//init to create new Nodes for VARIABLEDECLARATION

				if(newNode != null)
				{
					analyzeBody(entry.getValue().getAsJsonObject(),newNode);
					node.addAdj(newNode);
				}
				else
					analyzeBody(entry.getValue().getAsJsonObject(),node);

				break;
			default:
				System.out.println("OUTRO");
				break;
			}

		}
	}

	private void printInfo(Node n)
	{
		System.out.println("\nType  : " + n.getType().toString());
		System.out.println("Specification : "+n.getSpecification());

		Descriptor d = n.getReference();
		if(d != null)
			System.out.println("Descriptor : \n   Name : "+d.name + "\n   Type : "+d.type);

		for(Node n1 : n.getAdj()){
			printInfo(n1);
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
	private String read(File file) {
		// Check null argument. If null, it would raise and exception and stop
		// the program when used to create the File object.
		if (file == null) {
			Logger.getLogger("info").info("Input 'file' is null.");
			return null;
		}

		StringBuilder stringBuilder = new StringBuilder();

		// Try to read the contents of the file into the StringBuilder

		try (final BufferedReader bufferedReader = 
				new BufferedReader(new InputStreamReader(new FileInputStream(file), Resources.DEFAULT_CHAR_SET))) {

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

	private void addNameToLastST(String name){
		SymbolTable st = tables.get(tables.size()-1);
		st.addName(name);
	}

	private void addParamToLastST(Descriptor d){
		SymbolTable st = tables.get(tables.size()-1);
		st.addParam(d);
	}
}
