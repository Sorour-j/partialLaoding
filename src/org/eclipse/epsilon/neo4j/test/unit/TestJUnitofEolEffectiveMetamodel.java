package org.eclipse.epsilon.neo4j.test.unit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodel;
import org.eclipse.epsilon.emc.neo4j.Neo4jModel;
import org.junit.Test;


public class TestJUnitofEolEffectiveMetamodel{
	String metamodel = "model/componentLanguage.ecore";
	ResourceSet resourceSet = new ResourceSetImpl();
	ArrayList<String> labels = new ArrayList<String>();
	@Test
	public void OneAttribute() throws Exception {
		
		String actualQuery,expectedQuery;
		EffectiveMetamodel efMM;
		metamodelRegisteration();
		
		/*Effective Meta-model*/
		efMM = new EffectiveMetamodel();
		efMM.addToAllOfKind("Port");
		efMM.getFromAllOfKind("Port").addToAttributes("dataType");
		
		/*Expected*/
		expectedQuery = "MATCH (port:Port),(port)-[portins:instanceOf]->(portType) RETURN portType.name,ID(port),port.dataType";
		/*Actual*/
		Neo4jModel model = new Neo4jModel();
		model.setName("componentLanguage");
		efMM.setName("componentLanguage");
	
		model.setEffectiveMteamodel(efMM);
		model.effectiveMetamodelReconciler.addPackages(resourceSet.getPackageRegistry().values());
		model.effectiveMetamodelReconciler.addEffectiveMetamodel(efMM);
		model.effectiveMetamodelReconciler.reconcile();

		model.createResource("componentLanguage");
		model.labels.addAll(labels);
	//	actualQuery = model.getqartitioningHandler().generateQuery().get(0);
		
	//	assertEquals(expectedQuery, actualQuery);
}
	@Test
	public void nonContainmentReference() throws Exception {
		
		String actualQuery,expectedQuery;
		EffectiveMetamodel efMM;
		metamodelRegisteration();
		
		/*Effective Meta-model*/
		efMM = new EffectiveMetamodel();
		efMM.addToAllOfKind("Connector");
		efMM.getFromAllOfKind("Connector").addToReferences("target");
		
		/*Expected*/
		expectedQuery = "MATCH (connector:Connector),(connector)-[connectorins:instanceOf]->(connectorType),(port)-[portins:instanceOf]->(portType),(connector)-[targetref:target]->(port:Port) RETURN connectorType.name,ID(connector),portType.name,ID(port)";
		/*Actual*/
		Neo4jModel model = new Neo4jModel();
		model.setName("componentLanguage");
		efMM.setName("componentLanguage");
		model.setEffectiveMteamodel(efMM);
		model.effectiveMetamodelReconciler.addPackages(resourceSet.getPackageRegistry().values());
		model.effectiveMetamodelReconciler.addEffectiveMetamodel(efMM);
		model.effectiveMetamodelReconciler.reconcile();

		model.createResource("componentLanguage");
		model.labels.addAll(labels);
		//actualQuery = model.generateQuery().get(0);
		
		//assertEquals(expectedQuery, actualQuery);
}
//	@Test
//	public void ContainmentReference() throws Exception {
//		
//		String actualQuery,expectedQuery;
//		EffectiveMetamodel efMM;
//		metamodelRegister();
//		
//		/*Effective Meta-model*/
//		efMM = new EffectiveMetamodel();
//		efMM.addToAllOfKind("Movie");
//		efMM.getFromAllOfKind("Movie").addToReferences("persons");
//		
//		/*Expected*/
//		expectedQuery = "MATCH (movie:Movie),(movie)-[persons:persons]->(person:Person) RETURN labels(person),ID(movie),ID(person),labels(movie)";
//		/*Actual*/
//		Neo4jModel model = new Neo4jModel();
//		model.setName("movies");
//		efMM.setName("movies");
//		model.setEffectiveMteamodel(efMM);
//		model.effectiveMetamodelReconciler.addPackages(resourceSet.getPackageRegistry().values());
//		model.effectiveMetamodelReconciler.addEffectiveMetamodel(efMM);
//		model.effectiveMetamodelReconciler.reconcile();
//
//		//model.createResource();
//		actualQuery = model.generateQuery().get(0);
//		
//		assertEquals(expectedQuery, actualQuery);
//}
	@Test
	public void NonContainmenteReferenceWithAttribute() throws Exception {
		
		String actualQuery,expectedQuery;
		EffectiveMetamodel efMM;
		metamodelRegisteration();
		
		/*Effective Meta-model*/
		efMM = new EffectiveMetamodel();
		efMM.addToAllOfKind("Connector");
		efMM.getFromAllOfKind("Connector").addToReferences("target");
		efMM.addToAllOfKind("Port");
		efMM.getFromAllOfKind("Port").addToAttributes("dataType");
		
		
		Neo4jModel model = new Neo4jModel();
		model.setName("componentLanguage");
		efMM.setName("componentLanguage");
		model.setEffectiveMteamodel(efMM);
		model.effectiveMetamodelReconciler.addPackages(resourceSet.getPackageRegistry().values());
		model.effectiveMetamodelReconciler.addEffectiveMetamodel(efMM);
		model.effectiveMetamodelReconciler.reconcile();

		model.createResource("componentLanguage");
		model.labels.addAll(labels);
		
		/*Expected*/
		expectedQuery = "MATCH (port:Port),(port)-[portins:instanceOf]->(portType) RETURN portType.name,ID(port),port.dataType";
		/*Actual*/
	//	actualQuery = model.generateQuery().get(0);
	//	assertEquals(expectedQuery, actualQuery);
		expectedQuery = "MATCH (connector:Connector),(connector)-[connectorins:instanceOf]->(connectorType),(port)-[portins:instanceOf]->(portType),(connector)-[targetref:target]->(port:Port) RETURN connectorType.name,ID(connector),portType.name,ID(port)";
	//	actualQuery = model.generateQuery().get(1);
	//	assertEquals(expectedQuery, actualQuery);
}
@Test
public void ContainmenteReferenceWithAttribute() throws Exception {
		
		String actualQuery,expectedQuery;
		EffectiveMetamodel efMM;
		metamodelRegisteration();
		
		/*Effective Meta-model*/
		efMM = new EffectiveMetamodel();
		efMM.addToAllOfKind("Component");
		efMM.getFromAllOfKind("Component").addToReferences("ports");
		efMM.addToTypes("Port");
		efMM.getFromTypes("Port").addToAttributes("dataType");
		
		Neo4jModel model = new Neo4jModel();
		model.setName("componentLanguage");
		efMM.setName("componentLanguage");
		model.setEffectiveMteamodel(efMM);
		model.effectiveMetamodelReconciler.addPackages(resourceSet.getPackageRegistry().values());
		model.effectiveMetamodelReconciler.addEffectiveMetamodel(efMM);
		model.effectiveMetamodelReconciler.reconcile();

		model.createResource("componentLanguage");
		model.labels.addAll(labels);
		
		expectedQuery = "MATCH (component:Component),(component)-[componentins:instanceOf]->(componentType),(port)-[portins:instanceOf]->(portType),(component)-[portsref:ports]->(port:Port) RETURN ID(component),componentType.name,portType.name,ID(port)";
		//actualQuery = model.generateQuery().get(0);
	//	assertEquals(expectedQuery, actualQuery);
		/*Expected*/
		expectedQuery = "MATCH (port:Port),(port)-[portins:instanceOf]->(portType) RETURN portType.name,ID(port),port.dataType";
		/*Actual*/
	//	actualQuery = model.generateQuery().get(1);
	//	assertEquals(expectedQuery, actualQuery);
		
}
public void metamodelRegisteration() {
	
	ResourceSet ecoreResourceSet = new ResourceSetImpl();
	ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
	Resource ecoreResource = ecoreResourceSet.
			createResource(URI.createFileURI(new File(metamodel).getAbsolutePath()));
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
		for (EClassifier c : ePackage.getEClassifiers())
			if (!(c instanceof EEnum))
				labels.add(c.getName());
	}
}
}


