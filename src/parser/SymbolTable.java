package parser;

import java.util.ArrayList;

public class SymbolTable 
{
	public String functionName;
	public ArrayList<Descriptor> params = new ArrayList<>();
	public ArrayList<Descriptor> locals = new ArrayList<>();
	public Descriptor functionReturn;
	
	public SymbolTable(){}
	
	public void addName(String name){
		this.functionName = name;
	}
	
	public void addParam(Descriptor d){
		params.add(d);
	}
	
	public void addLocal(Descriptor d){
		locals.add(d);
	}
	
	public void addReturn(Descriptor d){
		this.functionReturn = d;
	}

	public Descriptor findParam(String value) {
		for(Descriptor d : params)
			if(d.name.equals(value))
				return d;
		return null;
	}

	public Descriptor findLocal(String value) {
		for(Descriptor d : locals)
		{
			if(d.name.equals(value))
				return d;
		}
		return null;
	}
}
