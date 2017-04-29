package parser;

import java.util.ArrayList;

import cli.Resources.JSONType;

public class Node 
{
	private JSONType type;
	private String specification;
	private Descriptor reference;
	private ArrayList<Node> adj = new ArrayList<>();
	
	public Node(JSONType type)
	{
		this.type = type;
		this.specification = null;
		this.reference = null;
	}

	public void addAdj(Node node) {
		adj.add(node);
	}

	public JSONType getType() {
		return type;
	}

	public void setType(JSONType type) {
		this.type = type;
	}

	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}

	public Descriptor getReference() {
		return reference;
	}

	public void setReference(Descriptor reference) {
		this.reference = reference;
	}

	public ArrayList<Node> getAdj() {
		return adj;
	}

	public void setAdj(ArrayList<Node> adj) {
		this.adj = adj;
	}
}
