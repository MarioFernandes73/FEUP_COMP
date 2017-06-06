package cli;

public class Resources
{
	public enum DataType{
		BOOLEAN, ARRAYBOOLEAN, 
		INT, ARRAYINT, 
		DOUBLE, ARRAYDOUBLE,
		STRING, ARRAYSTRING,
		NOTASSIGNED
	}
	
	public enum JSONType{
		START,					//first node
		FUNCTION,				//function node
        CALLEE,                 //function callee
        ARG,                    //function caller arguments
		PARAM,					//function parameter
		RETURN,					//function return
        LITERAL,
		IDENTIFIER,				//descriptor	(load or loadarray)
		VARIABLEDECLARATION,	//var a = b 	(store)
								//var a = [0,2] (storearray)
		ASSIGNMENT,				//a = b			(store)
								//a[i] = 2 		(storearray)
        ARRAYLOAD,
        ARRAYDECLARATION,       //[0,1] array content with 2 literals
                                //type: storearray if declaration and loadarray if x[0] for example
		OPERATION,				//+ - * /
		IFSTATEMENT,			//first child node : test condition; others : consequence (except else node)
		ELSE,					//ifstatement child; all child's are consequences
		WHILESTATEMENT,			//first child node : test condition; others : consequence
		DOWHILESTATEMENT,		//last child node : test condition; others : consequence
		FORSTATEMENT		//last child node : test condition; others : consequence
	}

	final public static String DEF_SPC = new String("    ");
    final public static String DEFAULT_CHAR_SET = "UTF-8";

    public static String DataTypeToString(DataType type){
    	switch (type){
			case INT: return "int";
			case ARRAYINT: return "int[]";
			case DOUBLE: return "double";
			case ARRAYDOUBLE: return "double[]";
			case STRING: return "String";
			case ARRAYSTRING: return "String[]";
			case ARRAYBOOLEAN: return "boolean[]";
			case BOOLEAN: return "boolean";
			default: return "Integer";
		}
	}
}
