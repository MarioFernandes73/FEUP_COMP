package semantic;

import cli.Resources.DataType;
import cli.Resources.JSONType;
import parser.Descriptor;
import parser.Node;
import parser.SymbolTable;

import java.util.ArrayList;

public class TypeInference
{
    private ArrayList<SymbolTable> tables = new ArrayList<>();
    private Node hir;
    private SymbolTable currentTable;
    private String errorMessage;

    public TypeInference(ArrayList<SymbolTable> tables, Node hir){
        this.tables = tables;
        this.hir = hir;
        this.currentTable = tables.get(0);
        this.errorMessage = null;
    }

    public void run(){
        try
        {
            SemanticTypeInference(null,hir);
            verifyCalleeArgsRetType(hir);
        }
        catch (Exceptions.TypeMismatchException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        catch (Exceptions.InitializationException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        catch (Exceptions.InvalidOperationException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        catch (Exceptions.InvalidReturnTypeException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        catch (Exceptions.FunctionNameException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        catch (Exceptions.InvalidNumArgsException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
    }

    private void SemanticTypeInference(Node parent, Node node)
      throws
      Exceptions.TypeMismatchException,
        Exceptions.InvalidOperationException,
        Exceptions.InitializationException,
        Exceptions.InvalidReturnTypeException,
        Exceptions.FunctionNameException, Exceptions.InvalidNumArgsException
    {

        if(node.getType() == JSONType.FUNCTION){
            changeCurrentTable(node.getSpecification());
        }
        //callers
        else if(node.getType() == JSONType.CALLEE) {
            SymbolTable st = getSymbolTable(node.getSpecification());

            //callee does not exist
            if(st == null){
                throw new Exceptions.FunctionNameException(node.getSpecification());
            }

            //args
            ArrayList<Node> nodes = node.getAdj();
            if(nodes.size() != st.getNumArgs())
                throw new Exceptions.InvalidNumArgsException(node.getSpecification());

            if(parent != null){
                DataType dt = st.getFunctionReturn();
                node.setDescriptorType(dt);
                return;
            }
        }
        //return type
        else if(node.getType() == JSONType.RETURN)
        {
            DataType dt1 = node.getDescriptorType();
            DataType dt2 = currentTable.getFunctionReturn();
            //different return type
            if (dt1 != dt2) {
                if(dt2 == DataType.NOTASSIGNED) currentTable.setFunctionReturn(dt1);
                else throw new Exceptions.InvalidReturnTypeException(currentTable.getFunctionName());
            }
        }
        //operations
        else if(node.getType() == JSONType.OPERATION) {
            System.out.println("OPERADOR");
            DataType dt = typeInferenceOp(node);

            if(parent != null){
                parent.setDescriptorType(dt);
                return;
            }
        }
       //others
        else if(node.getSpecification() != null)
        {
            //assignments or variable declarations --> must analyse childs
            if(node.getSpecification() == "store")
            {
                System.out.println("e store");
                //childs
                ArrayList<Node> nodes = node.getAdj();

                //no childs -> end
                if(nodes.size() == 0)
                    return;

                //analyse first child
                Node firstNode = nodes.get(0);

                System.out.println("vai para o backtracking");
                //semantic inference to sons
                SemanticTypeInference(node,firstNode);
                return;
            }
            //loadarrays -> right or left side
            else if(node.getSpecification().equals("loadarray")){
                DataType dt = typeInferenceArray(node);

                //right side ? //TODO COMPOR
                if(parent != null){
                    parent.setDescriptorType(dt);
                    return;
                }
            }
            //store arrays
            else if(node.getSpecification().equals("storearray")){
                DataType dt = typeInferenceArray(node);

                if(parent != null){
                    parent.setDescriptorType(getDescriptionTypeArrays(dt));
                    return;
                }
            }
            //identifiers and literals assignments to parents
            else if(parent != null) {
                parent.setDescriptorType(node.getDescriptorType());
            }
        }
        else{
            System.out.println("-->parou uma coisa aqui<--");
        }

        //recursive call
        ArrayList<Node> nodes = node.getAdj();
        for (Node n : nodes) {
            SemanticTypeInference(null,n);
        }
    }

    /**
     * Verifies if arguments and return types of calees functions are correct.
     * Executed after type inference.
     * @param node
     */
    private void verifyCalleeArgsRetType(final Node node) throws Exceptions.TypeMismatchException
    {
        if(node.getType() == JSONType.CALLEE)
        {
            SymbolTable st = getSymbolTable(node.getSpecification());
            //childs
            ArrayList<Node> nodes = node.getAdj();
            ArrayList<Descriptor> args = st.getParams();

            //two cases :
            //    args are NOTASSIGNED -> assign now
            //    types assigned and different -> error
            for (int i = 0; i < nodes.size(); i++)
            {
                if(nodes.get(i).getDescriptorType() != args.get(i).getType())
                {
                    if(args.get(i).getType() == DataType.NOTASSIGNED){
                        args.get(i).setType(nodes.get(i).getDescriptorType());
                    }else{
                        throw new Exceptions.TypeMismatchException(args.get(i).getName());
                    }
                }
            }
        }

        ArrayList<Node> nodes = node.getAdj();
        for (Node n : nodes) {
            verifyCalleeArgsRetType(n);
        }
    }

    private void changeCurrentTable(final String specification) {
        for (SymbolTable st : tables){
            if(st.getFunctionName().equals(specification))
                currentTable = st;
        }
    }

    private SymbolTable getSymbolTable(final String specification) {
        for (SymbolTable st : tables)
            if(st.getFunctionName().equals(specification))
                return st;
        return null;
    }

    private DataType typeInferenceArray(Node node) throws Exceptions.TypeMismatchException
    {
        ArrayList<Node> nodes = node.getAdj();
        Node firstNode = nodes.get(0);

        //special case for stores --> all childs must have the same type
        if(node.getType() == JSONType.ARRAYDECLARATION)
        {
            DataType dt1 = DataType.NOTASSIGNED;

            if(firstNode.getReference() != null)
                dt1 = firstNode.getDescriptorType();

            //multi-dimensional array
            for(Node n_dim : nodes)
            {
                if(n_dim.getType() == JSONType.ARRAYDECLARATION)
                {
                    DataType dt2 = typeInferenceArray(n_dim);
                    if(dt1 == DataType.NOTASSIGNED){
                        dt1 = dt2;
                    }
                    else if(dt1 != dt2){
                        if((dt1 == DataType.INT && dt2 == DataType.DOUBLE) || (dt2 == DataType.INT && dt1 == DataType.DOUBLE)) {
                            dt1 = DataType.DOUBLE;
                        }
                        else{
                            throw new Exceptions.TypeMismatchException("resolver 2");
                        }
                    }
                }
                else{
                    if(n_dim.getDescriptorType() != dt1)
                        throw new Exceptions.TypeMismatchException("resolver 1");
                }
            }
            return dt1;
        }
        else if(node.getType() == JSONType.ARRAYLOAD) {
            if(firstNode.getType() == JSONType.ARRAYLOAD) {
                return typeInferenceArray(firstNode);
            }
            return getDescriptionTypeArrays(firstNode.getDescriptorType());
        }
        return DataType.NOTASSIGNED;
    }

    private DataType typeInferenceOp(Node node) throws Exceptions.TypeMismatchException, Exceptions.InvalidOperationException, Exceptions.InitializationException
    {
        ArrayList<Node> nodes = node.getAdj();
        ArrayList<DataType> dataTypes = new ArrayList<>();

        for(Node n : nodes){
            if(n.getType() == cli.Resources.JSONType.OPERATION){
                DataType dt = typeInferenceOp(n);
                dataTypes.add(dt);
            }
            else if(n.getType() == cli.Resources.JSONType.IDENTIFIER || n.getType() == cli.Resources.JSONType.LITERAL){
                dataTypes.add(n.getReference().getType());
            }
            else if(n.getType() == cli.Resources.JSONType.ARRAYLOAD)
            {
                DataType dt = typeInferenceArray(n);
                System.out.println("2 - "+dt);
                dataTypes.add(dt);
            }
        }

        return getDescriptionTypeOp(dataTypes,node.getSpecification());
    }

    private DataType getDescriptionTypeArrays(DataType descriptorType)
    {
        if(descriptorType == DataType.INT){
            return DataType.ARRAYINT;
        }else if(descriptorType == DataType.STRING){
            return DataType.ARRAYSTRING;
        }else if(descriptorType == DataType.BOOLEAN){
            return (DataType.ARRAYBOOLEAN);
        }else if(descriptorType == DataType.DOUBLE){
            return (DataType.ARRAYDOUBLE);
        }else if(descriptorType == DataType.ARRAYINT){
            return (DataType.INT);
        }else if(descriptorType == DataType.ARRAYSTRING){
            return (DataType.STRING);
        }else if(descriptorType == DataType.ARRAYBOOLEAN){
            return (DataType.BOOLEAN);
        }else if(descriptorType == DataType.ARRAYDOUBLE){
            return (DataType.DOUBLE);
        }

        return DataType.NOTASSIGNED;
    }

    private DataType getDescriptionTypeOp(ArrayList<DataType> dataTypes, String op) throws Exceptions.InitializationException, Exceptions.InvalidOperationException
    {
        DataType dtLeft = dataTypes.get(0);

        //if some variables are NOT ASSIGNED -> problems with initialization
        if(dtLeft == DataType.NOTASSIGNED){
            throw new Exceptions.InitializationException("resolver");
        }
        //- / * ++ -- only allowed for numbers
        System.out.println("aqui : "+op);
        if((op.equals("/") || op.equals("-") || op.equals("*") || op.equals("++") || op.equals("--")) && !(dtLeft == DataType.INT || dtLeft == DataType.DOUBLE)) {
            throw new Exceptions.InvalidOperationException();
        }
        // + not allowed for booleans
        if(op.equals("+") && (dtLeft == DataType.BOOLEAN )) {
            throw new Exceptions.InvalidOperationException();
        }

        //operadores unários
        if(op.equals("++") || op.equals("--"))
            return dtLeft;

        DataType dtRight = dataTypes.get(1);

        //if some variables are NOT ASSIGNED -> problems with initialization
        if(dtRight == DataType.NOTASSIGNED){
            throw new Exceptions.InitializationException("resolver");
        }
        // - / * only allowed for numbers
        if((op.equals("/") || op.equals("-") || op.equals("*")) && !(dtRight == DataType.INT || dtRight == DataType.DOUBLE)) {
            throw new Exceptions.InvalidOperationException();
        }
        // + not allowed for booleans
        if(op.equals("+") && dtRight == DataType.BOOLEAN) {
            throw new Exceptions.InvalidOperationException();
        }

        // division --> always double
        if(op.equals("/"))
            return DataType.DOUBLE;

        //if different -> must apply type inference
        if((dtLeft != dtRight))
        {
            //string + (int|double)
            if(dtLeft == DataType.STRING || dtRight == DataType.STRING) {
                return DataType.STRING;
            }
            //all the other options : (double|int) (+ - *) (double|int)
            else{
                return DataType.DOUBLE;
            }
        }
        else
            return dtRight;
    }

    public String getErrorMessage(){
        return errorMessage;
    }

}
