package org.eclipse.epsilon.neo4j.test.unit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodel;
import org.eclipse.epsilon.effectivemetamodel.extraction.PartitioningEffectiveMetamodelVisitor;
import org.eclipse.epsilon.emc.neo4j.SubModelFactory;
import org.eclipse.epsilon.evl.dom.Constraint;
import org.eclipse.epsilon.evl.staticanalyser.EvlStaticAnalyser;
import org.eclipse.epsilon.neo4j.partitioning.PartialEvlModule;
import org.eclipse.epsilon.neo4j.partitioning.PartitioningHandler;
import org.junit.Test;

public class TestJUnitPartitioningAutoEFmetamodel {
	String metamodel = "model/movies.ecore";

	ResourceSet resourceSet = new ResourceSetImpl();
	PartialEvlModule module;

	@Test
	public void noSubset() throws Exception {
		String evFile = "testConstraints/test1.evl";
		module = getModule(evFile);
		HashMap<Constraint, EffectiveMetamodel> program = null;
		metamodelRegisteration();
		program = analysis();
		String expectedPartitions = null;

		expectedPartitions = "[hasTitle][isValidName]";

		ArrayList<Set<Constraint>> actualPartitions = new ArrayList<Set<Constraint>>();

		PartitioningHandler handler = new PartitioningHandler(program);
		actualPartitions.addAll(handler.getPartitions().keySet());
		System.out.println(actualPartitions);
		assertEquals(equalPartitions(actualPartitions, expectedPartitions), true);
	}

	@Test
	public void oneSubset() throws Exception {
		String evFile = "testConstraints/test2.evl";
		module = getModule(evFile);
		HashMap<Constraint, EffectiveMetamodel> program = null;
		metamodelRegisteration();
		program = analysis();
		String expectedPartitions = null;
		ArrayList<String> gp = new ArrayList<String>();

		ArrayList<ArrayList<String>> exp = new ArrayList<ArrayList<String>>();
		gp.add("titleSize");
		gp.add("hasTitle");
		exp.add(gp);
		gp = new ArrayList<String>();
		gp.add("isValidName");
		exp.add(gp);
		expectedPartitions = "[hasTitle,titleSize][isValidName]";

		ArrayList<Set<Constraint>> actualPartitions = new ArrayList<Set<Constraint>>();

		PartitioningHandler handler = new PartitioningHandler(program);
		actualPartitions.addAll(handler.getPartitions().keySet());
		System.out.println(actualPartitions);
		equalPartitions(actualPartitions, exp);
		assertEquals(equalPartitions(actualPartitions, expectedPartitions), true);
	}

	@Test
	public void twoSubset() throws Exception {
		String evFile = "testConstraints/test2.evl";
		module = getModule(evFile);
		HashMap<Constraint, EffectiveMetamodel> program = null;
		metamodelRegisteration();
		program = analysis();
		String expectedPartitions = null;

		expectedPartitions = "[hasTitle,titleSize][isValidName]";

		ArrayList<Set<Constraint>> actualPartitions = new ArrayList<Set<Constraint>>();

		PartitioningHandler handler = new PartitioningHandler(program);
		actualPartitions.addAll(handler.getPartitions().keySet());
		System.out.println(actualPartitions);
		assertEquals(equalPartitions(actualPartitions, expectedPartitions), true);
	}

	@Test
	public void threeSubset() throws Exception {
		String evFile = "testConstraints/test4.evl";
		module = getModule(evFile);
		HashMap<Constraint, EffectiveMetamodel> program = null;
		metamodelRegisteration();
		program = analysis();
		String expectedPartitions = null;

		expectedPartitions = "[isValidName,hasMovies,nameMovie][titleSize,hasTitle]";

		ArrayList<Set<Constraint>> actualPartitions = new ArrayList<Set<Constraint>>();

		PartitioningHandler handler = new PartitioningHandler(program);
		actualPartitions.addAll(handler.getPartitions().keySet());
		System.out.println(actualPartitions);
		assertEquals(equalPartitions(actualPartitions, expectedPartitions), true);
	}

	@Test
	public void oneSubsetNoMerge() throws Exception {
		String evFile = "testConstraints/test3.evl";
		module = getModule(evFile);
		HashMap<Constraint, EffectiveMetamodel> program = null;
		metamodelRegisteration();
		program = analysis();
		String expectedPartitions = null;

		expectedPartitions = "[hasMovies][hasTitle,titleSize][isValidName]";

		ArrayList<Set<Constraint>> actualPartitions = new ArrayList<Set<Constraint>>();

		PartitioningHandler handler = new PartitioningHandler(program);
		actualPartitions.addAll(handler.getPartitions().keySet());
		System.out.println(actualPartitions);
		assertEquals(equalPartitions(actualPartitions, expectedPartitions), true);
	}

	public PartialEvlModule getModule(String evl) throws Exception {
		PartialEvlModule module = new PartialEvlModule();
		module.parse(new File(evl));

		return module;
	}

	public void metamodelRegisteration() {

		ResourceSet ecoreResourceSet = new ResourceSetImpl();
		ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource ecoreResource = ecoreResourceSet
				.createResource(URI.createFileURI(new File(metamodel).getAbsolutePath()));
		try {
			ecoreResource.load(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (EObject o : ecoreResource.getContents()) {
			EPackage ePackage = (EPackage) o;
			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
			EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);

		}
	}

	public HashMap<Constraint, EffectiveMetamodel> analysis() {
		EvlStaticAnalyser staticanalyser = new EvlStaticAnalyser();
		staticanalyser.getContext().setModelFactory(new SubModelFactory());
		staticanalyser.validate(module);
		return new PartitioningEffectiveMetamodelVisitor().setpartitionExtractor((PartialEvlModule) module,
				staticanalyser);
	}

	public boolean equalPartitions(ArrayList<Set<Constraint>> act, String exp) {
		String partition = "[";

		for (Set<Constraint> cons : act) {
			for (Constraint c : cons)
				partition += c.getName() + ",";
			partition = partition.substring(0, partition.length() - 1);
			partition += "][";
		}
		partition = partition.substring(0, partition.length() - 1);
		if (partition.equals(exp))
			return true;
		return false;
	}

	public boolean equalPartitions(ArrayList<Set<Constraint>> act, ArrayList<ArrayList<String>> exp) {
		String partition = "[";
		boolean match = true;

		for (Set<Constraint> cons : act) {
			for (Constraint c : cons) {
				for (ArrayList<String> arr : exp) {
						if (!arr.contains(c.getName())) {
							match = false;
							continue;
						}
				}
			}
			// partition += c.getName()+",";
			partition = partition.substring(0, partition.length() - 1);
			partition += "][";
		}
		partition = partition.substring(0, partition.length() - 1);
		if (partition.equals(exp))
			return true;
		return false;
	}
}
