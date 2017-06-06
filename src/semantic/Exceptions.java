package semantic;

public class Exceptions {

    public static class AssignmentException extends Exception
    {
        private String symbol;
        public AssignmentException(String symbol){
            super("Assignment to undefined variable");
            this.symbol = symbol;
        }

        public String getMessage(){
            return "Cannot find symbol \""+symbol+"\"";
        }
    }

    public static class InitializationException extends Exception
    {
        private String symbol;
        public InitializationException(String symbol){
            super("Initialization");
            this.symbol = symbol;
        }

        public String getMessage(){
            return "A variable might not have been initialized";
        }
    }

    public static class TypeMismatchException extends Exception
    {
        private String symbol;
        public TypeMismatchException(String symbol){
            super("Type Mismatch");
            this.symbol = symbol;
        }

        public String getMessage(){
            return "Mismatch types for \""+symbol+"\" variable";
        }
    }

    public static class InvalidOperationException extends Exception
    {
        public InvalidOperationException(){
            super("Invalid Operation");
        }

        public String getMessage(){
            return "Invalid operation";
        }
    }

    public static class InvalidReturnTypeException extends Exception
    {
        private String name;
        public InvalidReturnTypeException(String function){
            super("Invalid Operation");
            this.name = function;
        }
        public String getMessage(){
            return "Invalid type of return for "+name;
        }
    }

    public static class FunctionNameException extends Exception
    {
        private String name;
        public FunctionNameException(String name){
            super("Invalid Function Name");
            this.name = name;
        }
        public String getMessage(){
            return "Invalid function name "+name;
        }
    }

    public static class InvalidNumArgsException extends Exception
    {
        private String name;
        public InvalidNumArgsException(String name){
            super("Invalid Function Name");
            this.name = name;
        }
        public String getMessage(){
            return "Invalid number of arguments for function "+name;
        }
    }
}
