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
		START,
		FUNCTION,
		VARIABLEDECLARATION,
		PARAM
	}

    final public static String DEFAULT_CHAR_SET = "UTF-8";
}
