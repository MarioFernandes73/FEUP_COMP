package semantic;

import cli.Resources.DataType;
import cli.Resources.JSONType;
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
            e.printStackTrace();
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
            if(nodes.get(0).getType() == JSONType.OPERATION) {
                DataType dt = typeInferenceOp(firstNode);
                node.setDescriptorType(dt);
            }
            //if arrays
            else if(firstNode.getSpecification().equals("loadarray")){
                DataType dt = typeInferenceArray(firstNode);
                System.out.println("-1 - "+dt);
                node.setDescriptorType(dt);
            }
            else if(node.getSpecification().equals("storearray")){
                DataType dt = typeInferenceArray(firstNode);
                node.setDescriptorType(getDescriptionTypeArrays(dt));
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
        //- / * only allowed for numbers
        if((op.equals("/") || op.equals("-") || op.equals("*")) && !(dtLeft == DataType.INT || dtLeft == DataType.DOUBLE)) {
            throw new Exceptions.InvalidOperationException();
        }
        // + not allowed for booleans
        if(op.equals("+") && (dtLeft == DataType.BOOLEAN )) {
            throw new Exceptions.InvalidOperationException();
        }

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

}
