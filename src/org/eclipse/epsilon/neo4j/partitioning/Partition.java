package org.eclipse.epsilon.neo4j.partitioning;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodel;
import org.eclipse.epsilon.evl.dom.Constraint;

public class Partition {

	Set<Constraint> group = new HashSet<Constraint>();
	EffectiveMetamodel effectiveMetamodel;
	int cost = 0; // cost of merge
	
	public Partition(){
	}
	protected void setGroup(Set<Constraint> gp) {
		this.group = gp;
	}
	protected void setEffectiveMetamodel(EffectiveMetamodel ef) {
		this.effectiveMetamodel = ef;
	}
	protected void setCost(int cost) {
		this.cost = cost;
	}
	protected Set<Constraint> getGroup() {
		return this.group;
	}
	protected EffectiveMetamodel getEffectiveMetamodel() {
		return this.effectiveMetamodel;
	}
	protected int getCost() {
		return this.cost;
	}
}

