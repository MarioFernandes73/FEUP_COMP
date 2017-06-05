package parser;

import cli.Resources;
import cli.Resources.JSONType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import semantic.Exceptions;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

public class Parser
{
    private ArrayList<SymbolTable> tables = new ArrayList<>();
    private Node hir;
    private JsonObject root;
    private String errorMessage;

    public Parser(String jsonCode)
    {
        //File json = new File(jsonCode);
        //jsonCode = read(json);
        System.out.println(jsonCode);

        //Gson gson = new Gson();
        Gson gson = new GsonBuilder()
                      .disableHtmlEscaping()
                      .setLenient()
                      .create();
        //gson.setCharacterEncoding("UTF8");
        JsonElement jelement = gson.fromJson (jsonCode, JsonElement.class);

        root = jelement.getAsJsonObject();
        setHir(new Node(JSONType.START));
        errorMessage = null;
    }

    public void run()
    {
        try {
            analyzeBody(root, getHir());
        }
        catch (Exceptions.AssignmentException e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        catch (Exceptions.TypeMismatchException e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        catch (Exceptions.FunctionNameException e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
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

    private void analyzeBody(JsonObject jobject, Node node)
      throws Exceptions.AssignmentException, Exceptions.TypeMismatchException, Exceptions.FunctionNameException
    {
        System.out.println(" --- begin --- \n\nCurrent Node : "+node.getType());

        String key;
        String classType;
        String value;
        Node newNode = null;
        Node currentNode = node;

        Set<Map.Entry<String,JsonElement>> obj = jobject.entrySet();
        Iterator<Entry<String, JsonElement>> it = obj.iterator();

        while(it.hasNext())
        {
            Entry<String, JsonElement> entry = it.next();

            classType = entry.getValue().getClass().getSimpleName();
            key = entry.getKey().toString();
            value = entry.getValue().toString().replaceAll("\"", "");

            if(newNode != null)
                currentNode = newNode;

            JSONType nodeType = currentNode.getType();
            String nodeSpecification = currentNode.getSpecification();
            Descriptor nodeReference = currentNode.getReference();

            switch (classType)
            {
                case "JsonArray":
                    System.out.println("\nARRAY: \n" + key + " = " + value);

                    for(JsonElement elem : entry.getValue().getAsJsonArray())
                    {
                        // special case where PARAM originaly belongs to START node but we need them at FUNCTION
                        if(key.equals("params"))
                        {
                            Node param = new Node(JSONType.PARAM);
                            analyzeBody(elem.getAsJsonObject(),param);
                            newNode.addAdj(param);
                        }
                        // special case where ARG originaly belongs to FUNCTION node but we need them at CALLEE
                        else if(key.equals("arguments"))
                        {
                            Node arg = new Node(JSONType.ARG);
                            analyzeBody(elem.getAsJsonObject(),arg);
                            newNode.addAdj(arg);
                        }
                        else analyzeBody(elem.getAsJsonObject(),currentNode);
                    }

                    break;
                case "JsonPrimitive":
                    System.out.println("\nPRIMITIVE: \n" +  key + " = " + value);

                    //function : type(FUNCTION), specification(function name), reference(NULL)
                    if(value.equals("FunctionDeclaration"))
                    {
                        //create new Node
                        newNode = createNewNode(currentNode, JSONType.FUNCTION, null, null);

                        //create one symbol table per function
                        getTables().add(new SymbolTable());
                    }
                    else if(key.equals("name") && nodeType == JSONType.FUNCTION)
                    {
                        //set function name at node and at the symbolTable
                        currentNode.setSpecification(value);
                        addNameToLastST(value);
                    }
                    else if(value.equals("CallExpression"))
                    {
                        //create new Node
                        newNode = createNewNode(currentNode, JSONType.CALLEE, null, null);
                    }
                    else if(key.equals("name") && nodeType == JSONType.CALLEE)
                    {
                        //set function name at node and at the symbolTable
                        currentNode.setSpecification(value);
                    }

                    //return : type(RETURN), specification(NULL), reference(var name and type)
                    else if(value.equals("ReturnStatement"))
                    {
                        //create new Node
                        newNode = createNewNode(currentNode, JSONType.RETURN, null, null);
                    }
                    else if(key.equals("name") && nodeType == JSONType.RETURN && nodeReference == null)
                    {
                        Descriptor d = setReference(currentNode,value);
                        addReturnToLastST(d.getType());
                    }

                    //parameter : type(PARAM), specification(NULL), reference(var name and DataType)
                    else if(key.equals("name") && nodeType == JSONType.PARAM && nodeReference == null)
                    {
                        //param descriptor starts with unknown dataType
                        Descriptor d = new Descriptor(value);
                        //add parameter to SymbolTable and set a reference to the descriptor at the HIR
                        addParamToLastST(d);
                        currentNode.setReference(d);
                    }

                    /*//argument : type(ARG), specification(NULL), reference(var name and DataType)
                    else if(key.equals("name") && nodeType == JSONType.ARG && nodeReference == null)
                    {
                        //arg descriptor starts with unknown dataType
                        Descriptor d = new Descriptor(value);
                        currentNode.setReference(d);
                    }*/

                    //local variable : type(VARIABLEDECLARATION), specification(NULL), reference(var name and DataType)
                    else if(value.equals("VariableDeclaration") || value.equals("VariableDeclarator"))
                    {
                        //create new Node
                        newNode = createNewNode(currentNode, JSONType.VARIABLEDECLARATION, "store", null);
                    }

                    else if(key.equals("name") && nodeType == JSONType.VARIABLEDECLARATION && nodeReference == null)
                    {
                        //create descriptor, add to node and to SymbolTable
                        Descriptor d = new Descriptor(value);
                        currentNode.setReference(d);
                        addLocalToLastST(d);
                    }

                    else if(value.equals("ArrayExpression") && nodeType == JSONType.VARIABLEDECLARATION )
                    {
                        currentNode.setSpecification("storearray");
                        newNode = createNewNode(currentNode, JSONType.ARRAYDECLARATION, "storearray", null);
                    }
                    //fill the array content
                    else if(value.equals("ArrayExpression") && nodeType == JSONType.ARRAYDECLARATION)
                    {
                        newNode = createNewNode(currentNode, JSONType.ARRAYDECLARATION, "storearray", null);
                    }

                    //assignment : type(ASSIGNMENT), specification(=), reference(variable name and type)
                    else if(value.equals("AssignmentExpression"))
                    {
                        newNode = createNewNode(currentNode, JSONType.ASSIGNMENT, "store", null);
                    }
                    //assignment of arrays (left side)
                    else if(value.equals("MemberExpression") )
                    {
                        if(nodeType == JSONType.ASSIGNMENT)
                        {
                            currentNode.setSpecification("storearray");
                        }
                        //load of arrays
                        newNode = createNewNode(currentNode, JSONType.ARRAYLOAD, "loadarray", null);
                    }
                    else if(value.equals("MemberExpression") && nodeType == JSONType.ARRAYLOAD)
                    {
                        newNode = createNewNode(currentNode, JSONType.ARRAYLOAD, "loadarray", null);
                    }

                    //if statement : type(IFSTATEMENT), specification(null), reference(null)
                    else if(value.equals("IfStatement"))
                    {
                        newNode = createNewNode(currentNode, JSONType.IFSTATEMENT, null, null);
                    }

                    //while statement : type(WHILESTATEMENT), specification(null), reference(null)
                    else if(value.equals("WhileStatement"))
                    {
                        newNode = createNewNode(currentNode, JSONType.WHILESTATEMENT, null, null);
                    }

                    //do while statement : type(DOWHILESTATEMENT), specification(null), reference(null)
                    else if(value.equals("DoWhileStatement"))
                    {
                        newNode = createNewNode(currentNode, JSONType.DOWHILESTATEMENT, null, null);
                    }

                    //for statement : type(FORSTATEMENT), specification(null), reference(null)
                    else if(value.equals("ForStatement"))
                    {
                        newNode = createNewNode(currentNode, JSONType.FORSTATEMENT, null, null);
                    }

                    //BinaryExpression : type(OPERATION), specification(operator), reference(NULL)
                    else if(value.equals("BinaryExpression") || value.equals("LogicalExpression") || value.equals("UnaryExpression") || value.equals("UpdateExpression"))
                    {
                        newNode = createNewNode(currentNode, JSONType.OPERATION, null, null);
                    }
                    //add specification to OPERATION
                    else if(key.equals("operator") && (nodeType == JSONType.OPERATION || nodeType == JSONType.ASSIGNMENT))
                    {
                        currentNode.setSpecification(value);
                    }

                    //literal : type(dataType), specification(data), specification(NULL)
                    else if(value.equals("Literal"))
                    {
                        newNode = createNewNode(currentNode, JSONType.LITERAL, null, null);
                    }
                    else if(key.equals("value") && (nodeType == JSONType.LITERAL || nodeType == JSONType.RETURN || nodeType == JSONType.ARG))
                    {
                        Descriptor d = new Descriptor(null, Resources.DataType.NOTASSIGNED);
                        currentNode.setReference(d);
                        currentNode.setSpecification(value);
                        setType(currentNode,value);
                    }
                    else if(key.equals("raw") && (nodeType == JSONType.LITERAL || nodeType == JSONType.RETURN || nodeType == JSONType.ARG))
                    {
                        confirmType(currentNode,value);
                    }

                    //when we need to load some descriptor, and don't want to store it ==> create IDENTIFIER node
                    //special cases that have identifiers but we'll use them on a different way : FUNCTION, PARAM and RETURN
                    //identifier : type(IDENTIFIER), specification(NULL), reference(variable name and type)
                    else if(value.equals("Identifier") &&
                              !(nodeType == JSONType.PARAM || nodeType == JSONType.FUNCTION ||
                                  nodeType == JSONType.CALLEE))
                    {

                        if (!(nodeSpecification != null && ((nodeSpecification.equals("store") || nodeSpecification.equals("load")))
                                && nodeReference == null) || nodeType == JSONType.WHILESTATEMENT || nodeType == JSONType.IFSTATEMENT ||
                                nodeType == JSONType.RETURN || nodeType == JSONType.ARG) {
                            newNode = createNewNode(currentNode, JSONType.IDENTIFIER, "load", null);
                        }
                    }

                    //adding a descriptor to some load/store or arg node
                    else if(key.equals("name") &&
                              (nodeType == JSONType.ARG || nodeType == JSONType.ASSIGNMENT || (nodeSpecification != null && (nodeSpecification.equals("store") ||
                                                                                            nodeSpecification.equals("load")))
                              && nodeReference == null))
                    {
                        setReference(currentNode, value);
                    }

                    break;
                case "JsonObject":
                    System.out.println("\nOBJECT: \n" +  key + " = " + value);

                    //special case where alternate object needs a new node to handle the else possibility
                    if(key.equals("alternate"))
                    {
                        Node elseNode = createNewNode(currentNode, JSONType.ELSE, null, null);
                        analyzeBody(entry.getValue().getAsJsonObject(),elseNode);
                    }
                    else {
                        analyzeBody(entry.getValue().getAsJsonObject(),currentNode);
                    }

                    break;
                default:
                    System.out.println("OTHER");
                    break;
            }
        }
        System.out.println(" --- end --- ");
    }

    private Node createNewNode(Node node, JSONType type, String specification, Descriptor reference)
    {
        Node newNode = new Node();
        if(type != null)
            newNode = new Node(type);

        newNode.setSpecification(specification);
        newNode.setReference(reference);
        node.addAdj(newNode);

        return newNode;
    }

    private Descriptor setReference(Node node, String value) throws Exceptions.AssignmentException
    {
        Descriptor d = null;
        d = findDescriptorAtLastST(value);
        node.setReference(d);

        return d;
    }

    private void setType(Node node, String key) throws Exceptions.TypeMismatchException
    {
        if(isInteger(key))
        {
            node.setDescriptorType(Resources.DataType.INT);
        }
        else if(key.equals("true") || key.equals("false"))
        {
            node.setDescriptorType(Resources.DataType.BOOLEAN);
        }
        else{
            node.setDescriptorType(Resources.DataType.STRING);
        }
    }

    private void confirmType(Node currentNode, String key) throws Exceptions.TypeMismatchException
    {
        if(currentNode.getReference().getType() == Resources.DataType.INT) {
            if (key.contains(".")) {
                currentNode.getReference().setType(Resources.DataType.NOTASSIGNED);
                currentNode.setDescriptorType(Resources.DataType.DOUBLE);
            }
        }
    }

    private boolean isInteger(String s)
    {
        try {
            Double.parseDouble(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }

    private void addNameToLastST(String name) throws Exceptions.FunctionNameException
    {
        for(int i = 0; i < tables.size(); i++){
            if(i == tables.size()-1){
                tables.get(i).addName(name);
            }
            else if(tables.get(i).getFunctionName().equals(name)){
                throw new Exceptions.FunctionNameException(name);
            }
        }
    }

    private void addParamToLastST(Descriptor d){
        SymbolTable st = getTables().get(getTables().size()-1);
        st.addParam(d);
    }

    private void addLocalToLastST(Descriptor d){
        SymbolTable st = getTables().get(getTables().size()-1);
        st.addLocal(d);
    }

    private void addReturnToLastST(Resources.DataType dataType) {
        SymbolTable st = getTables().get(getTables().size()-1);
        st.addReturn(dataType);
    }

    private Descriptor findDescriptorAtLastST(String value) throws Exceptions.AssignmentException
    {
        SymbolTable st = getTables().get(getTables().size()-1);

        Descriptor d = st.findParam(value);

        if(d == null){
            d = st.findLocal(value);
            if(d == null){
                throw new Exceptions.AssignmentException(value);
            }
        }

        return d;
    }

    public ArrayList<SymbolTable> getTables()
    {
        return tables;
    }

    public void setTables(ArrayList<SymbolTable> tables)
    {
        this.tables = tables;
    }

    public Node getHir()
    {
        return hir;
    }

    public void setHir(Node hir)
    {
        this.hir = hir;
    }

    public String getErrorMessage(){
        return errorMessage;
    }
}
