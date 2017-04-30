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
		
		System.out.println("\n------- START PRINTING HIR -------");
		printHIR(hir,"");	
		System.out.println("\n------- START PRINTING SYMBOL TABLES -------");
		printSymbolTable();
	}

	private void analyzeBody(JsonObject jobject, Node node) 
	{
		System.out.println("Current Node : "+node.getType());

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
					// special case where PARAMS originaly belongs to START node but we need them at FUNCTION node
					if(key.equals("params"))
					{
						Node param = new Node(JSONType.PARAM);
						analyzeBody(elem.getAsJsonObject(),param);
						newNode.addAdj(param);
					}
					else
					{
						if(newNode == null)
							newNode = node;

						System.out.println(" --- ANTES --- ");
						analyzeBody(elem.getAsJsonObject(),newNode);
						System.out.println(" --- DEPOIS --- ");
					}
				}

				break;
			case "JsonPrimitive":
				System.out.println("\nPRIMITIVE: \n" +  key + " = " + value);

				//function : type(FUNCTION), specification(function name), reference(NULL)
				if(value.equals("\"FunctionDeclaration\""))
				{
					//create new Node
					newNode = new Node(JSONType.FUNCTION);
					node.addAdj(newNode);

					//create one symbol table per function
					tables.add(new SymbolTable());
				}
				else if(key.equals("name") && (node.getType() == JSONType.FUNCTION))
				{
					//set function name at node and at the symbolTable
					node.setSpecification(value);
					addNameToLastST(value);
				}
				//return : type(RETURN), specification(NULL), reference(var name and type)
				else if(value.equals("\"ReturnStatement\"") && (node.getType() == JSONType.FUNCTION))
				{
					newNode = new Node(JSONType.RETURN);
					node.addAdj(newNode);
				}
				else if(key.equals("name") && (node.getType() == JSONType.RETURN))
				{
					Descriptor d = findDescriptorAtLastST(value); 
					
					if(d == null)
						System.out.println("rip");
					
					node.setReference(d);
					addReturnToLastST(d);
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

					System.out.println("Adicionou variable");
					node.addAdj(newNode);
				}
				else if(key.equals("name") && node.getType() == JSONType.VARIABLEDECLARATION)
				{
					//create descriptor, add to node and to SymbolTable
					Descriptor d = new Descriptor(value);
					node.setReference(d);
					addLocalToLastST(d);
				}
				//BinaryExpression : type(OPERATION), specification(operator), reference(NULL)
				else if(value.equals("\"BinaryExpression\""))
				{
					newNode = new Node(JSONType.OPERATION);

					System.out.println("Adicionou operator");
					node.addAdj(newNode);
				}
				//add specification to OPERATION
				else if(key.equals("operator") && newNode.getType() == JSONType.OPERATION)
				{
					newNode.setSpecification(value);
				}
				else if(value.equals("\"Literal\""))
				{
					newNode = new Node();

					System.out.println("Adicionou literal");
					node.addAdj(newNode);
				}
				else if(key.equals("raw"))
				{
					//ATENCAO : falta verificar o tipo!!
					newNode.setType(JSONType.INT);
					newNode.setSpecification(value);
				}

				break;
			case "JsonObject":
				System.out.println("\nOBJECT: \n" +  key + " = " + value);

				if(newNode == null)
					newNode = node;

				System.out.println(" --- ANTES --- ");
				analyzeBody(entry.getValue().getAsJsonObject(),newNode);
				System.out.println(" --- DEPOIS --- ");

				break;
			default:
				System.out.println("OUTRO");
				break;
			}	
		}
	}

	private void printHIR(Node n,String spacement)
	{
		System.out.println(spacement + "Type  : " + n.getType().toString());
		System.out.println(spacement + "Specification : "+n.getSpecification());

		Descriptor d = n.getReference();
		if(d != null)
			System.out.println(spacement + "Descriptor ( Name : "+d.name + " | Type : "+d.type+" )");

		for(Node n1 : n.getAdj())
		{
			printHIR(n1, spacement+"- ");
		}
	}
	
	private void printSymbolTable()
	{
		System.out.println();
		
		for(SymbolTable st : tables)
		{
			System.out.println("Function \n   Name : " + st.functionName + "\n   Params : ");
			
			for(Descriptor d : st.params)
				System.out.println("      Name : " + d.name + "   AND   Type : " + d.type);
			
			System.out.println("   Locals : ");
			for(Descriptor d : st.locals)
				System.out.println("      Name : " + d.name + "   AND   Type : " + d.type);
			
			if(st.functionReturn != null)
				System.out.println("   Return : \n      Name : "+st.functionReturn.name + "   AND   Type : "+st.functionReturn.type+"\n");	
			else
				System.out.println("   Return : void\n"); 
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
	
	private void addLocalToLastST(Descriptor d){
		SymbolTable st = tables.get(tables.size()-1);
		st.addLocal(d);
	}

	private void addReturnToLastST(Descriptor d) {
		SymbolTable st = tables.get(tables.size()-1);
		st.addReturn(d);
	}

	private Descriptor findDescriptorAtLastST(String value){
		SymbolTable st = tables.get(tables.size()-1);

		Descriptor d = st.findParam(value);

		if(d == null)
			d = st.findLocal(value);

		return d;
	}
}
