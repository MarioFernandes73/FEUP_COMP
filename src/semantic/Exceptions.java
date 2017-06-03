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
            return "Variable \""+symbol+"\" might not have been initialized";
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

}