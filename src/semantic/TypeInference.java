package semantic;

import cli.Resources;
import parser.Node;
import parser.SymbolTable;

import java.util.ArrayList;

public class TypeInference
{
    private ArrayList<SymbolTable> tables = new ArrayList<>();
    private Node hir;

    public TypeInference(ArrayList<SymbolTable> tables, Node hir){
        this.tables = tables;
        this.hir = hir;
    }

    public void run(){
        try
        {
            typeInference(hir);
        }
        catch (Exceptions.TypeMismatchException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (Exceptions.InitializationException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (Exceptions.InvalidOperationException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void typeInference(Node node) throws Exceptions.TypeMismatchException, Exceptions.InvalidOperationException, Exceptions.InitializationException
    {
        //if not null reference, must interpret its childs
        if (node.getReference() != null && node.getSpecification().contains("store"))
        {
            //childs
            ArrayList<Node> nodes = node.getAdj();

            //no childs -> end
            if(nodes.size() == 0)
                return;

            //analyse first child
            Node firstNode = nodes.get(0);

            //my child is an operation
            if(nodes.get(0).getType() == Resources.JSONType.OPERATION) {
                Resources.DataType dt = typeInferenceOp(firstNode);
                node.setDescriptorType(dt);
            }
            //if arrays
            else if(node.getSpecification().equals("storearray") || firstNode.getSpecification().equals("loadarray")){
                Resources.DataType dt = typeInferenceArray(firstNode);
                node.setDescriptorType(dt);
            }
            //identifiers and literals
            else {
                node.setDescriptorType(firstNode.getDescriptorType());
            }
            return;
        }

        ArrayList<Node> nodes = node.getAdj();
        for (Node n : nodes) {
            typeInference(n);
        }
    }

    private Resources.DataType typeInferenceArray(Node node) throws Exceptions.TypeMismatchException
    {
        ArrayList<Node> nodes = node.getAdj();
        Node firstNode = nodes.get(0);

        //special case for stores --> all childs must have the same type
        if(node.getSpecification().equals("storearray"))
        {
            Resources.DataType dt = firstNode.getDescriptorType();
            for (Node n : nodes){
                if(n.getDescriptorType() != dt)
                    throw new Exceptions.TypeMismatchException("resolver");
            }
        }
        System.out.println("first -- "+firstNode.getDescriptorType());
        return getDescriptionTypeArrays(firstNode.getDescriptorType());
    }

    private Resources.DataType typeInferenceOp(Node node) throws Exceptions.TypeMismatchException, Exceptions.InvalidOperationException, Exceptions.InitializationException
    {
        ArrayList<Node> nodes = node.getAdj();
        ArrayList<Resources.DataType> dataTypes = new ArrayList<>();

        for(Node n : nodes){
            if(n.getType() == cli.Resources.JSONType.OPERATION){
                Resources.DataType dt = typeInferenceOp(n);
                dataTypes.add(dt);
            }
            else if(n.getType() == cli.Resources.JSONType.IDENTIFIER || n.getType() == cli.Resources.JSONType.LITERAL){
                dataTypes.add(n.getReference().getType());
            }
            else if(n.getType() == cli.Resources.JSONType.ARRAYLOAD)
            {
                Resources.DataType dt = typeInferenceArray(n);
                dataTypes.add(dt);
            }
        }

        return getDescriptionTypeOp(dataTypes,node.getSpecification());
    }

    private Resources.DataType getDescriptionTypeArrays(Resources.DataType descriptorType)
    {
        if(descriptorType == Resources.DataType.INT){
            return Resources.DataType.ARRAYINT;
        }else if(descriptorType == Resources.DataType.STRING){
            return Resources.DataType.ARRAYSTRING;
        }else if(descriptorType == Resources.DataType.BOOLEAN){
            return (Resources.DataType.ARRAYBOOLEAN);
        }else if(descriptorType == Resources.DataType.DOUBLE){
            return (Resources.DataType.ARRAYDOUBLE);
        }else if(descriptorType == Resources.DataType.ARRAYINT){
            return (Resources.DataType.INT);
        }else if(descriptorType == Resources.DataType.ARRAYSTRING){
            return (Resources.DataType.STRING);
        }else if(descriptorType == Resources.DataType.ARRAYBOOLEAN){
            return (Resources.DataType.BOOLEAN);
        }else if(descriptorType == Resources.DataType.ARRAYDOUBLE){
            return (Resources.DataType.DOUBLE);
        }

        return Resources.DataType.NOTASSIGNED;
    }

    private Resources.DataType getDescriptionTypeOp(ArrayList<Resources.DataType> dataTypes, String op) throws Exceptions.InitializationException, Exceptions.InvalidOperationException
    {
        Resources.DataType dtLeft = dataTypes.get(0);

        //if some variables are NOT ASSIGNED -> problems with initialization
        if(dtLeft == Resources.DataType.NOTASSIGNED){
            throw new Exceptions.InitializationException("resolver");
        }
        //- / * only allowed for numbers
        if((op.equals("/") || op.equals("-") || op.equals("*")) && !(dtLeft == Resources.DataType.INT || dtLeft == Resources.DataType.DOUBLE)) {
            throw new Exceptions.InvalidOperationException();
        }
        // + not allowed for booleans
        if(op.equals("+") && (dtLeft == Resources.DataType.BOOLEAN )) {
            throw new Exceptions.InvalidOperationException();
        }

        Resources.DataType dtRight = dataTypes.get(1);

        //if some variables are NOT ASSIGNED -> problems with initialization
        if(dtRight == Resources.DataType.NOTASSIGNED){
            throw new Exceptions.InitializationException("resolver");
        }
        // - / * only allowed for numbers
        System.out.println("zzz"+dtRight.name());
        if((op.equals("/") || op.equals("-") || op.equals("*")) && !(dtRight == Resources.DataType.INT || dtRight == Resources.DataType.DOUBLE)) {
            throw new Exceptions.InvalidOperationException();
        }
        // + not allowed for booleans
        if(op.equals("+") && dtRight == Resources.DataType.BOOLEAN) {
            throw new Exceptions.InvalidOperationException();
        }

        // division --> always double
        if(op.equals("/"))
            return Resources.DataType.DOUBLE;

        //if different -> must apply type inference
        if((dtLeft != dtRight))
        {
            //string + (int|double)
            if(dtLeft == Resources.DataType.STRING || dtRight == Resources.DataType.STRING) {
                return Resources.DataType.STRING;
            }
            //all the other options : (double|int) (+ - *) (double|int)
            else{
                return Resources.DataType.DOUBLE;
            }
        }
        else
            return dtRight;
    }

}
