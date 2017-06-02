package parser;

import cli.Resources;
import cli.Resources.JSONType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    public Parser(String filename)
    {
        File json = new File(filename);

        String jsonToString = read(json);
        JsonElement jelement = new JsonParser().parse(jsonToString);

        root = jelement.getAsJsonObject();
        setHir(new Node(JSONType.START));
    }

    public void run()
    {
        analyzeBody(root, getHir());
        typeInference(getHir());
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
                        // special case where PARAMS originaly belongs to START node but we need them at FUNCTION node
                        if(key.equals("params"))
                        {
                            Node param = new Node(JSONType.PARAM);
                            analyzeBody(elem.getAsJsonObject(),param);
                            newNode.addAdj(param);
                        }
                        else
                        {
                            analyzeBody(elem.getAsJsonObject(),currentNode);
                        }
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

                    //return : type(RETURN), specification(NULL), reference(var name and type)
                    else if(value.equals("ReturnStatement") && nodeType == JSONType.FUNCTION)
                    {
                        //create new Node
                        newNode = createNewNode(currentNode, JSONType.RETURN, null, null);
                    }
                    else if(key.equals("name") && nodeType == JSONType.RETURN && nodeReference == null)
                    {
                        Descriptor d = setReference(currentNode,value);
                        addReturnToLastST(d);
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

                    //local variable : type(VARIABLEDECLARATION), specification(NULL), reference(var name and DataType)
                    else if(value.equals("VariableDeclaration"))
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
                    else if(key.equals("operator") && nodeType == JSONType.OPERATION)
                    {
                        currentNode.setSpecification(value);
                    }

                    //literal : type(dataType), specification(data), specification(NULL)
                    else if(value.equals("Literal"))
                    {
                        newNode = createNewNode(currentNode, null, null, null);
                    }
                    else if(key.equals("value") && newNode != null)
                    {
                        currentNode.setSpecification(value);
                        setType(currentNode,value);
                    }
                    else if(key.equals("raw") && newNode != null)
                    {
                        confirmType(currentNode,value);
                    }

                    //when we need to load some descriptor, and don't want to store it ==> create IDENTIFIER node
                    //special cases that have identifiers but we'll use them on a different way : FUNCTION, PARAM and RETURN
                    //identifier : type(IDENTIFIER), specification(NULL), reference(variable name and type)
                    else if(value.equals("Identifier") &&
                              !(nodeType == JSONType.RETURN || nodeType == JSONType.PARAM || nodeType == JSONType.FUNCTION))
                    {

                        //
                        if(!((nodeSpecification.equals("store") || nodeSpecification.equals("load"))
                               && nodeReference == null))
                        {
                            newNode = createNewNode(currentNode, JSONType.IDENTIFIER, "load", null);
                        }
                    }

                    //adding a descriptor to some load/store node
                    else if(key.equals("name") &&
                              (nodeSpecification.equals("store") || nodeSpecification.equals("load"))
                              && nodeReference == null)
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
                    else
                    {
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

    private Descriptor setReference(Node node, String value)
    {
        Descriptor d = findDescriptorAtLastST(value);
        node.setReference(d);

        return d;
    }

    private void setDescriptorType(Node node, JSONType descriptorType)
    {
        if(descriptorType == JSONType.INT){
            if(node.getSpecification().equals("storearray")){
                node.setDescriptorType(Resources.DataType.ARRAYINT);
            }else{
                node.setDescriptorType(Resources.DataType.INT);
            }
        }else if(descriptorType == JSONType.STRING){
            if(node.getSpecification().equals("storearray")){
                node.setDescriptorType(Resources.DataType.ARRAYSTRING);
            }else{
                node.setDescriptorType(Resources.DataType.STRING);
            }
        }else if(descriptorType == JSONType.BOOLEAN){
            if(node.getSpecification().equals("storearray")){
                node.setDescriptorType(Resources.DataType.ARRAYBOOLEAN);
            }else{
                node.setDescriptorType(Resources.DataType.BOOLEAN);
            }
        }else if(descriptorType == JSONType.DOUBLE){
            if(node.getSpecification().equals("storearray")){
                node.setDescriptorType(Resources.DataType.ARRAYDOUBLE);
            }else{
                node.setDescriptorType(Resources.DataType.DOUBLE);
            }
        }
    }

    private void setType(Node currentNode, String key)
    {
        if(isInteger(key)){
            currentNode.setType(JSONType.INT);
        }
        else if(key.equals("true") || key.equals("false")){
            currentNode.setType(JSONType.BOOLEAN);
        }
        else{
            currentNode.setType(JSONType.STRING);
        }
    }

    private void confirmType(Node currentNode, String key)
    {
        if(currentNode.getType() == JSONType.INT) {
            if (key.contains(".")) {
                currentNode.setType(JSONType.DOUBLE);
            }
        }
    }

    public static boolean isInteger(String s)
    {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }

    private void addNameToLastST(String name){
        SymbolTable st = getTables().get(getTables().size()-1);
        st.addName(name);
    }

    private void addParamToLastST(Descriptor d){
        SymbolTable st = getTables().get(getTables().size()-1);
        st.addParam(d);
    }

    private void addLocalToLastST(Descriptor d){
        SymbolTable st = getTables().get(getTables().size()-1);
        st.addLocal(d);
    }

    private void addReturnToLastST(Descriptor d) {
        SymbolTable st = getTables().get(getTables().size()-1);
        st.addReturn(d);
    }

    private Descriptor findDescriptorAtLastST(String value){
        SymbolTable st = getTables().get(getTables().size()-1);

        Descriptor d = st.findParam(value);

        if(d == null)
            d = st.findLocal(value);

        return d;
    }

    private void typeInference(Node node)
    {
        //if not null reference, must interpret its childs
        if (node.getReference() != null && node.getSpecification().contains("store"))
        {
            ArrayList<Node> nodes = node.getAdj();
            Node firstNode = nodes.get(0);

            if(node.getSpecification().equals("storearray") ){
                nodes = firstNode.getAdj();
                firstNode = nodes.get(0);
            }

            if(nodes.get(0).getType() == JSONType.OPERATION){
                System.out.println("2");
                Resources.DataType dt = typeInferenceOp(firstNode);
                node.setDescriptorType(dt);
            }
            else{
                System.out.println("1");
                setDescriptorType(node, firstNode.getType());
            }

            return;
        }

        ArrayList<Node> nodes = node.getAdj();
        for (Node n : nodes)
        {
            typeInference(n);
        }
    }

    private Resources.DataType typeInferenceOp(Node node)
    {
        ArrayList<Node> nodes = node.getAdj();
        ArrayList<Resources.DataType> dataTypes = new ArrayList<>();

        for(Node n : nodes){
            if(n.getSpecification().equals("load"))
                dataTypes.add(n.getReference().getType());
            else
            {
                Resources.DataType dt = typeInferenceOp(n);
                dataTypes.add(dt);
            }
        }

        if(node.getSpecification().equals("*") || node.getSpecification().equals("+") || node.getSpecification().equals("-")){
            return getDescriptionTypeOp(nodes);
        }
        else if(node.getSpecification().equals("/")){
           return Resources.DataType.DOUBLE;
        }

        return Resources.DataType.NOTASSIGNED;
    }

    private Resources.DataType getDescriptionTypeOp(ArrayList<Node> nodes)
    {
        Resources.DataType dtRight = nodes.get(0).getReference().getType();
        Resources.DataType dtLeft = nodes.get(1).getReference().getType();

        if(dtLeft != dtRight){
            if(dtLeft == Resources.DataType.DOUBLE || dtRight == Resources.DataType.DOUBLE){
                return Resources.DataType.DOUBLE;
            }
            else{
                return Resources.DataType.NOTASSIGNED;
            }
        }
        else
            return dtRight;
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
}
