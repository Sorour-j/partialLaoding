package org.eclipse.epsilon.neo4j.partitioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.epsilon.effectivemetamodel.EffectiveFeature;
import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodel;
import org.eclipse.epsilon.effectivemetamodel.EffectiveType;
import org.eclipse.epsilon.eol.m3.StructuralFeature;
import org.eclipse.epsilon.evl.dom.Constraint;

import scala.reflect.runtime.SynchronizedSymbols.SynchronizedClassSymbol;

public class PartitioningHandler {

	// protected Set<Partition> partitions = new HashSet<Partition>();
	protected LinkedHashMap<Constraint, EffectiveMetamodel> constraintModelMap = new LinkedHashMap<Constraint, EffectiveMetamodel>();
	protected HashMap<Set<Constraint>, EffectiveMetamodel> constraintsGroups = new HashMap<Set<Constraint>, EffectiveMetamodel>();
	protected HashMap<String, Integer> weight = new HashMap<String, Integer>();

	public PartitioningHandler(LinkedHashMap<Constraint, EffectiveMetamodel> effectiveMetamodels) {

		this.constraintModelMap = effectiveMetamodels;
		checkSubsets();
		weight.put("Integer", 16);
		weight.put("Float", 32);
		weight.put("Real", 64);
		weight.put("String", 64);
		weight.put("boolean", 1);

		// long timeA = System.currentTimeMillis();
		setPartitions();
		// long timeB = System.currentTimeMillis();
		// System.out.println("Partitioning time: " + (timeB - timeA) + " ms");

	}

	// Distance (A,B) : If A is going to be loaded, how much is the cost to load B
	// as well
	public int getDistance(EffectiveMetamodel ef1, EffectiveMetamodel ef2) {

		int distance = 0;

		for (EffectiveType type : ef2.getAllOfKind()) {
			if (!ef1.allOfKindContains(type.getName())) {
				distance += 5;
			}
			for (EffectiveFeature atr : type.getAttributes()) {
				if (ef1.getFromAllOfKind(type.getName()) == null
						|| !ef1.getFromAllOfKind(type.getName()).containsAttribute(atr.getName())) {
					distance += 1;
				}
			}
			for (EffectiveFeature ref : type.getReferences()) {
				if (ef1.getFromAllOfKind(type.getName()) == null
						|| !ef1.getFromAllOfKind(type.getName()).containsReference(ref.getName())) {
					distance += 1;
				}
			}
		}
		for (EffectiveType type : ef2.getAllOfType()) {
			if (!ef1.allOfTypeContains(type.getName())) {
				distance += 5;

				for (EffectiveFeature atr : type.getAttributes())
					if (ef1.getFromAllOfType(type.getName()) == null
							|| !ef1.getFromAllOfType(type.getName()).containsAttribute(atr.getName()))
						distance += 1;

				for (EffectiveFeature ref : type.getReferences())
					if (ef1.getFromAllOfType(type.getName()) == null
							|| !ef1.getFromAllOfType(type.getName()).containsReference(ref.getName()))
						distance += 1;
			}
		}
		for (EffectiveType type : ef2.getTypes()) {
			for (EffectiveFeature atr : type.getAttributes())
				if (ef1.getFromTypes(type.getName()) == null
						|| !ef1.getFromTypes(type.getName()).containsAttribute(atr.getName())) {
					distance += 1;
				}
			for (EffectiveFeature ref : type.getReferences())
				if (ef1.getFromTypes(type.getName()) == null
						|| !ef1.getFromTypes(type.getName()).containsReference(ref.getName())) {
					distance += 1;
				}
		}

		return distance;
	}

	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
		Set<Set<T>> sets = new HashSet<Set<T>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
		for (Set<T> set : powerSet(rest)) {
			Set<T> newSet = new HashSet<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

	// If distance is less than the threshold, merge effective meta-models!
	public EffectiveMetamodel mergeMetamodels(EffectiveMetamodel ef1, EffectiveMetamodel ef2) {
		EffectiveMetamodel efmm = new EffectiveMetamodel();
		EffectiveMetamodel merged = new EffectiveMetamodel();

		for (EffectiveType t : ef1.getAllOfKind()) {
			EffectiveType type = new EffectiveType(t.getName());
			for (EffectiveFeature fe : t.getAttributes())
				type.getAttributes().add(fe);
			for (EffectiveFeature fe : t.getReferences())
				type.getReferences().add(fe);
			merged.addToAllOfKind(type);
		}
		for (EffectiveType t : ef1.getTypes()) {
			EffectiveType type = new EffectiveType(t.getName());
			for (EffectiveFeature fe : t.getAttributes())
				type.getAttributes().add(fe);
			for (EffectiveFeature fe : t.getReferences())
				type.getReferences().add(fe);
			merged.addToTypes(type);
		}

		for (EffectiveType t : ef2.getAllOfKind()) {
			EffectiveType type = new EffectiveType(t.getName());
			for (EffectiveFeature fe : t.getAttributes())
				type.getAttributes().add(fe);
			for (EffectiveFeature fe : t.getReferences())
				type.getReferences().add(fe);
			merged.addToAllOfKind(type);
		}
		for (EffectiveType t : ef2.getTypes()) {
			EffectiveType type = new EffectiveType(t.getName());
			for (EffectiveFeature fe : t.getAttributes())
				type.getAttributes().add(fe);
			for (EffectiveFeature fe : t.getReferences())
				type.getReferences().add(fe);
			merged.addToTypes(type);
		}
		merged.setName(ef1.getName() + ef2.getName());
		return merged;
	}

	// Ask user to partition the constraints
	public Set<Set<Constraint>> setPartitions() {

		EffectiveMetamodel efMax = getEffectiveMetamodelMax(constraintModelMap.values());
		int treshold = getEffectiveMetamodelSize(efMax);

		EffectiveMetamodel merge = new EffectiveMetamodel();
		Set<Constraint> gp = new HashSet<Constraint>();

		HashMap<Set<Constraint>, Integer> sets = new HashMap<Set<Constraint>, Integer>();

		for (Set<Constraint> cons : constraintsGroups.keySet()) {
			sets.put(cons, getEffectiveMetamodelSize(constraintsGroups.get(cons)));
		}

		for (Set<Constraint> set1 : sets.keySet()) {
			gp = new HashSet<Constraint>();
			for (Set<Constraint> set2 : sets.keySet())
				if (!set1.equals(set2) && constraintsGroups.containsKey(set1) && constraintsGroups.containsKey(set2)) {// &&
					merge = mergeMetamodels(constraintsGroups.get(set1), constraintsGroups.get(set2));

					if (merge.getAllOfKind().size() < 2 && getEffectiveMetamodelSize(merge) <= treshold) {

						constraintsGroups.remove(set1);
						constraintsGroups.remove(set2);
						for (Constraint c : set1)
							gp.add(c);
						for (Constraint c : set2)
							gp.add(c);
						if (!constraintsGroups.containsKey(gp))
							constraintsGroups.put(gp, merge);
					}
				}
		}
		return constraintsGroups.keySet();

	}

	// Get the constraint by name
	public Constraint getConstraint(String conName) {

		for (Entry<Constraint, EffectiveMetamodel> entry : constraintModelMap.entrySet()) {
			if (entry.getKey().getName().equals(conName)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public void checkSubsets() {

		Set<Constraint> cons = constraintModelMap.keySet();
		Set<Constraint> groupedCons = new HashSet<Constraint>();
		
		for (Constraint con : cons) {
			
				Set<Constraint> gp = new HashSet<Constraint>();
				Set<Constraint> deleteGp = new HashSet<Constraint>();
				EffectiveMetamodel ef1 = constraintModelMap.get(con);
				EffectiveMetamodel gpModel = new EffectiveMetamodel();
				ef1.copyEffectiveMetamodel(gpModel);
				gp.add(con);
				groupedCons.add(con);
				
				for (Constraint con2 : cons) {
					EffectiveMetamodel ef2 = constraintModelMap.get(con2);

					if (ef1 != ef2 && (getDistance(gpModel, ef2) == 0 || getDistance(ef2, gpModel) == 0)) {
						
						gp.add(con2);
						for (Set<Constraint> cs : constraintsGroups.keySet()) {
							if (cs.contains(con)) {
								constraintsGroups.get(cs).copyEffectiveMetamodel(gpModel);
								gp.remove(con);
								deleteGp = cs;
								gp.addAll(cs);
							}
							else if (cs.contains(con2)) {
								constraintsGroups.get(cs).copyEffectiveMetamodel(ef2);
								gp.remove(con2);
								deleteGp = cs;
								gp.addAll(cs);
							}
						}
						constraintsGroups.remove(deleteGp);
						gpModel = mergeMetamodels(gpModel, ef2);
						groupedCons.add(con2);
					}
				}

				if (!constraintsGroups.containsKey(gp))
					constraintsGroups.put(gp, gpModel);
			}
		//System.out.print(false);
	}

	// Get a group of constraints mapped to a specific effective meta-model
	public Set<Constraint> getConstraints(EffectiveMetamodel efModel) {

		for (Entry<Set<Constraint>, EffectiveMetamodel> entry : constraintsGroups.entrySet()) {
			if (efModel.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	// Get the mapped effective meta-model of a specific constraint
	public EffectiveMetamodel getEffectivemetamodel(Constraint cons) {

		for (Set<Constraint> entry : constraintsGroups.keySet()) {
			for (Constraint c : entry) {
				if (c.equals(cons)) {
					return constraintsGroups.get(entry);
				}
			}
		}
		return null;
	}

	// Find the other a partition that a specific constraint belongs to
	public Set<Constraint> getPartition(Constraint cons) {

		for (Set<Constraint> entry : constraintsGroups.keySet()) {
			for (Constraint c : entry) {
				if (c.equals(cons)) {
					return entry;
				}
			}
		}

		return null;
	}

	public Set<Set<Constraint>> getConstraintPartitions() {
		return this.constraintsGroups.keySet();
	}

	public HashMap<Set<Constraint>, EffectiveMetamodel> getPartitions() {
		return constraintsGroups;
	}

	public EffectiveMetamodel getEffectiveMetamodelMax(Collection<EffectiveMetamodel> models) {

		EffectiveMetamodel efMax = null;
		for (EffectiveMetamodel ef : constraintModelMap.values()) {
			if (efMax == null)
				efMax = ef;
			else if (ef.getAllOfKind().size() > efMax.getAllOfKind().size()) {
				efMax = ef;
			} else if (getEffectiveMetamodelSize(ef) > getEffectiveMetamodelSize(efMax))
				efMax = ef;
		}
		return efMax;
	}

	protected int getEffectiveMetamodelSize(EffectiveMetamodel efModel) {
		int size = 0;
		for (EffectiveType et : efModel.getAllOfKind()) {
			for (EffectiveFeature feature : et.getAttributes()) {
				if (weight.containsKey(feature.getType()))
					size += weight.get(feature.getType());
				else
					size += weight.get("String");
			}
			size += et.getReferences().size();// + 1;
		}
		for (EffectiveType et : efModel.getAllOfType()) {
			for (EffectiveFeature feature : et.getAttributes()) {
				size += weight.get(feature.getType());
			}
			size += et.getReferences().size();// + 1;
		}

		for (EffectiveType et : efModel.getTypes()) {
			for (EffectiveFeature feature : et.getAttributes()) {
				//System.out.println(feature.getType());
				if (weight.containsKey(feature.getType()))
					size += weight.get(feature.getType());
				else
					size += weight.get("String");
			//	size += weight.get(feature.getType());
			}
			size += et.getReferences().size();
		}
		return size;
	}

}
