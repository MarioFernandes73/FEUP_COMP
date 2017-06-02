package parser;

import cli.Resources.DataType;

public class Descriptor 
{
    private String   name;
    private DataType type;
	
	public Descriptor(String name){
		this.setName(name);
		this.setType(DataType.NOTASSIGNED);
	}
	
	public Descriptor(String name, DataType type){
		this.setName(name);
		this.setType(type);
	}

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public DataType getType()
    {
        return type;
    }

    public void setType(DataType type)
    {
        this.type = type;
    }
}
