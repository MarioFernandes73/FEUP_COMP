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
		VARIABLEDECLARATION,	//variableDeclaration
		PARAM,					//function parameter
		OPERATION,				// + - * / ==
		INT, DOUBLE, FLOAT, STRING, BOOLEAN,	//data types for literals
		RETURN,					//function return
	}

    final public static String DEFAULT_CHAR_SET = "UTF-8";
}
