package org.eclipse.epsilon.neo4j.dataset;

public class RelType implements org.neo4j.graphdb.RelationshipType {

	String name;
	@Override
	public String name() {
		// TODO Auto-generated method stub
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
