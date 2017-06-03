package parser;

import cli.Resources.DataType;

import java.util.ArrayList;

public class SymbolTable 
{
    private String functionName;
	public ArrayList<Descriptor> params = new ArrayList<>();
	public ArrayList<Descriptor> locals = new ArrayList<>();
    private DataType functionReturn;
	
	public SymbolTable(){
	    //default settings
        functionName = "main";
        functionReturn = DataType.NOTASSIGNED;
    }
	
	public void addName(String name){
		this.functionName = name;
	}
	
	public void addParam(Descriptor d){
		params.add(d);
	}
	
	public void addLocal(Descriptor d){
		locals.add(d);
	}
	
	public void addReturn(DataType d){
		this.functionReturn = d;
	}

	public Descriptor findParam(String value) {
		for(Descriptor d : params)
			if(d.getName().equals(value))
				return d;
		return null;
	}

	public Descriptor findLocal(String value) {
		for(Descriptor d : locals) {
			if(d.getName().equals(value))
				return d;
		}
		return null;
	}

    public DataType getFunctionReturn() {
        return functionReturn;
    }

    public void setFunctionReturn(DataType functionReturn) {
        this.functionReturn = functionReturn;
    }

    public String getFunctionName()
    {
        return functionName;
    }

    public void setFunctionName(String functionName)
    {
        this.functionName = functionName;
    }
}
