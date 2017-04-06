package parser;

import java.util.ArrayList;
import java.util.HashMap;

public class Info {
	//Only one of these 3 can be not null
	private HashMap<String,Info> scope = null;		
	private String value = null;
	private ArrayList<Info> array = null;

	public Info(){						//if it's a value
	}
	
	public Info(String value){						//if it's a value
		this.value = value;
	}
	
	public Info(HashMap<String,Info> scope){	//if it's a new scope
		this.scope = scope;
	}
	
	public Info(ArrayList<Info> array){	//if it's a new scope
		this.array = array;
	}
	
	public boolean isValue(){
		return value != null;
	}
	
	public boolean isScope(){
		return scope != null;
	}
	
	public boolean isArray(){
		return array != null;
	}
	
	public HashMap<String,Info> getScope(){
		return scope;
	}
	
	public String getValue(){
		return value;
	}
	
	public ArrayList<Info> getArray(){
		return array;
	}
}
