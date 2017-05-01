package cli;

public class Resources 
{
	public enum DataType{
		BOOLEAN, ARRAYBOOLEAN, 
		INT, ARRAYINT, 
		DOUBLE, ARRAYDOUBLE, 
		FLOAT, ARRAYFLOAT,
		CHAR, ARRAYCHAR,
		STRING, ARRAYSTRING,
		INTEGER, ARRAYINTEGER,
		NOTASSIGNED
	}
	
	public enum JSONType{
		START,					//first node
		FUNCTION,				//function node
		PARAM,					//function parameter
		RETURN,					//function return
		INT, DOUBLE, FLOAT, STRING, BOOLEAN,	//data types for literals
		IDENTIFIER,				//descriptor	(load or loadarray)
		VARIABLEDECLARATION,	//var a = b 	(store)  	
								//var a = [0,2] (storearray)
		ASSIGNMENT,				//a = b			(store)
								//a[i] = 2 		(storearray)
		OPERATION				//+ - * /
	}

    final public static String DEFAULT_CHAR_SET = "UTF-8";
}