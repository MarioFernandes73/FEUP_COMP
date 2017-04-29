package parser;

import cli.Resources.DataType;

public class Descriptor 
{
	public String name;
	public DataType type;
	
	public Descriptor(String name){
		this.name = name;
		this.type = DataType.NOTASSIGNED;
	}
	
	public Descriptor(String name, DataType type){
		this.name = name;
		this.type = type;
	}
}
