package org.eclipse.epsilon.neo4j.partitioning;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.effectivemetamodel.extraction.EvlPartitioningEffectiveMetamodelComputationVisitor;
import org.eclipse.epsilon.effectivemetamodel.extraction.PartitioningEffectiveMetamodelVisitor;
import org.eclipse.epsilon.emc.neo4j.Neo4jModel;
import org.eclipse.epsilon.emc.neo4j.SubModelFactory;
import org.eclipse.epsilon.evl.dom.Constraint;
import org.eclipse.epsilon.evl.launch.EvlRunConfiguration;
import org.eclipse.epsilon.evl.staticanalyser.EvlStaticAnalyser;

public class EvlPartitioningRunConfiguration extends EvlRunConfiguration {

	PartialEvlModule module;
	EvlStaticAnalyser staticanalyser = new EvlStaticAnalyser();
	Neo4jModel model;
	
	public EvlPartitioningRunConfiguration(EvlRunConfiguration other) {
		super(other);
		module = (PartialEvlModule) super.getModule();
	}

	@Override
	public void preExecute() throws Exception {
		
		super.preExecute();
		module.setIsPartitioned(true);
		
		//which model?
		model = (Neo4jModel)module.getContext().getModelRepository().getModels().get(0);
		//Register meta-model
		String metamodel = model.getMetamodelFiles().get(0);
		ResourceSet resourceSet = new ResourceSetImpl();
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
		}
		long timeA = System.currentTimeMillis();
		//analyse the program and resolve the types
		staticanalyser.getContext().setModelFactory(new SubModelFactory());
		staticanalyser.validate(module);
		long timeB = System.currentTimeMillis();
		System.out.println("Analysis time: " + (timeB - timeA) + " ms");
		
		timeA = System.currentTimeMillis();
		//compute all effective meta-models >> each constraint : one meta-model
		model.setEffectiveMteamodels(new PartitioningEffectiveMetamodelVisitor().setpartitionExtractor((PartialEvlModule)module, staticanalyser), module.isPartitioned());
		timeB = System.currentTimeMillis();
		System.out.println("Effective metamodel extraction time: " + (timeB - timeA) + " ms");
		model.setMetamodelResource(resourceSet);
		staticanalyser.postValidate(module);
	}
	
	@Override
	public void postExecute() throws Exception {
		super.postExecute();
		model.close();
	}
}