package org.eclipse.epsilon.neo4j.dataset;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class LoadModel {

	public static void main(final String[] args) throws IOException {
		ResourceSet xmiResourceSet = new ResourceSetImpl();
		ResourceSet ecoreResourceSet = new ResourceSetImpl();
		Resource resource;
		String EcoreMetamodel = "model/java_findbugs.ecore";
		String XmiModel = "model/eclipseModel-2.0.xmi";

		System.out.println("Registering Ecore:......");
		ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource ecoreResource = ecoreResourceSet
				.createResource(URI.createFileURI(new File(EcoreMetamodel).getAbsolutePath()));
		try {
			ecoreResource.load(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (EObject o : ecoreResource.getContents()) {
			EPackage ePackage = (EPackage) o;
			xmiResourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);

			System.out.println("Registeration Done!......");

			System.out.println("Start loading xmi:......");
			xmiResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
					new XMIResourceFactoryImpl());
			resource = xmiResourceSet.createResource(URI.createFileURI(new File(XmiModel).getAbsolutePath()));
			try {
				resource.load(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Loading done!......");
		}
	}
}
