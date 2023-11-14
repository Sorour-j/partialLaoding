package org.eclipse.epsilon.emc.neo4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.epsilon.effectivemetamodel.EffectiveFeature;
import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodel;
import org.eclipse.epsilon.effectivemetamodel.EffectiveType;
import org.eclipse.epsilon.eol.parse.Eol_EolParserRules.returnStatement_return;


public class Neo4jEffectiveMetamodelReconciler {

	//effective metamodels
		protected ArrayList<EffectiveMetamodel> effectiveMetamodels = new ArrayList<EffectiveMetamodel>();
		
		//epackages
		protected ArrayList<EPackage> packages = new ArrayList<EPackage>();
		protected HashMap<String, HashMap<EClass, ArrayList<EStructuralFeature>>> actualObjectsAndFeaturesToLoad = new HashMap<String, HashMap<EClass,ArrayList<EStructuralFeature>>>();
		protected HashMap<String, HashMap<EClass, ArrayList<EStructuralFeature>>> typesToLoad = new HashMap<String, HashMap<EClass,ArrayList<EStructuralFeature>>>();

		public void reconcile()
		{
			for(EPackage ePackage: packages)
			{
				//for each eclassifier
				for(EClassifier eClassifier: ePackage.getEClassifiers())
				{
					//if eclassifier is a eclass
					if (eClassifier instanceof EClass) {
						
						//if the class is an actual eclass to load, add to the map
						if (actualObjectToLoad(ePackage, (EClass) eClassifier)) {
							addActualObjectToLoad((EClass) eClassifier);
						}
						if (typesToLoad(ePackage, (EClass) eClassifier)) {
							addTypesToLoad((EClass) eClassifier);
						}
						EClass eClass = (EClass) eClassifier;

					}
				}
			}
		}
		public boolean typesToLoad(EPackage ePackage, EClass eClass)
		{
			//for each effective metamodel in the container
			for(EffectiveMetamodel em: effectiveMetamodels)
			{
					
					//for each type in all of type
					for(EffectiveType et: em.getTypes())
					{
						//get name
						String elementName = et.getName();
						//if name equals, return true
						if (elementName.equals(eClass.getName())) {
							return true;
						}			
					EClass kind = (EClass) ePackage.getEClassifier(et.getName());
					
					//if the eclass's super types contains the type also return true
					if(eClass.getEAllSuperTypes().contains(kind))
					{
						return true;
					}
				}
				}
			return false;
		}
		public void print(String packageName) {
			System.out.println("******AllOfKind*******: ");
			for (EClass cls : getActualObjectsToLoad().get(packageName).keySet()) {
				System.out.print(cls.getName() + " : ");
				for (EStructuralFeature f : getActualObjectsToLoad().get(packageName).get(cls))
					System.out.println(" feature = " + f.getName());
			}
			System.out.println("*******Types******: ");
			if (!getTypesToLoad().isEmpty()) {
			for (EClass cls : getTypesToLoad().get(packageName).keySet()) {
				System.out.print(cls.getName() + " : ");
				if (getTypesToLoad().get(packageName).get(cls) != null)
				for (EStructuralFeature f : getTypesToLoad().get(packageName).get(cls))
					System.out.println(" feature = " + f.getName());
			}
		}
		}
		public void addTypesToLoad(EClass eClass)
		{
			//get the epackage name
			String epackage = eClass.getEPackage().getNsURI();
			
			//get the submap with the epackage name
			HashMap<EClass, ArrayList<EStructuralFeature>> subMap = typesToLoad.get(epackage);
		//	HashMap<EClass, ArrayList<EStructuralFeature>> Actual = actualObjectsAndFeaturesToLoad.get(epackage);
			ArrayList<EStructuralFeature> refs = new ArrayList<EStructuralFeature>();
			
			//if sub map is null and eClass doesn't exist in ActualObjToLoad
			
			if (subMap == null) {
					//create new sub map
					subMap = new HashMap<EClass, ArrayList<EStructuralFeature>>();
					
					refs = getFeaturesForTypeToLoad(eClass);
						//add the ref to the sub map
						subMap.put(eClass, refs);
				
						//add the sub map to objectsAndRefNamesToVisit
							typesToLoad.put(epackage, subMap);
					}
			else
			{	
				//if sub map is not null, get the refs by class name
				refs = subMap.get(eClass);
				//if refs is null, create new refs and add the ref and then add to sub map
				if (refs == null) {
					
					//the features should be loaded accprding to effective metamdel
					refs = getFeaturesForTypeToLoad(eClass);
					if (!actualObjectsAndFeaturesToLoad.isEmpty() && actualObjectsAndFeaturesToLoad.get(epackage).containsKey(eClass))
						refs.addAll(actualObjectsAndFeaturesToLoad.get(epackage).get(eClass));
					subMap.put(eClass, refs);
				}
				
				
			}

//					}
	}
		public ArrayList<EStructuralFeature> getFeaturesForTypeToLoad(EClass eClass)
		{
			//get the package
			EPackage ePackage = eClass.getEPackage();
			//prepare the result
			ArrayList<EStructuralFeature> result = new ArrayList<EStructuralFeature>();
			
			//for all model containers
			for(EffectiveMetamodel em: effectiveMetamodels)
			{
				//if the container is the container needed
			//	if (em.getName().equals(ePackage.getName())) {
					for(EffectiveType et: em.getTypes())
					{
						//if class name equals, add all references and attributes
						if (eClass.getName().equals(et.getName())) {
							for(EffectiveFeature ef: et.getAttributes())
							{
								//result.add(ef.getName());
								if (!result.contains(eClass.getEStructuralFeature(ef.getName())))
								result.add(eClass.getEStructuralFeature(ef.getName()));
							}
							for(EffectiveFeature ef: et.getReferences())
							{
								//result.add(ef.getName());
								if (!result.contains(eClass.getEStructuralFeature(ef.getName())))
								result.add(eClass.getEStructuralFeature(ef.getName()));
							}
							//break loop2;
						}
						
						//if eclass is a sub class of the kind, add all attributes and references
						EClass kind = (EClass) ePackage.getEClassifier(et.getName());
						if (eClass.getEAllSuperTypes().contains(kind)) {
							for(EffectiveFeature ef: et.getAttributes())
							{
								//add by Sorour
								if (!result.contains(eClass.getEStructuralFeature(ef.getName())))
										//result.add(ef.getName());
									result.add(eClass.getEStructuralFeature(ef.getName()));
							}
							for(EffectiveFeature ef: et.getReferences())
							{
								if (!result.contains(eClass.getEStructuralFeature(ef.getName())))
									//result.add(ef.getName());
									result.add(eClass.getEStructuralFeature(ef.getName()));
							}
							//break loop1;
						}
//						if (kind.getEAllSuperTypes().contains(eClass)) {
//							for(EffectiveFeature ef: et.getAttributes())
//							{
//								//add by Sorour
//								if (!result.contains(kind.getEStructuralFeature(ef.getName())))
//										//result.add(ef.getName());
//									result.add(kind.getEStructuralFeature(ef.getName()));
//							}
//							for(EffectiveFeature ef: et.getReferences())
//							{
//								if (!result.contains(kind.getEStructuralFeature(ef.getName())))
//									//result.add(ef.getName());
//									result.add(kind.getEStructuralFeature(ef.getName()));
//							}
//							//break loop1;
//						}
					}
			//	}
			}
			return result;
		}
		public boolean actualObjectToLoad(EPackage ePackage, EClass eClass)
		{
			//for each effective metamodel in the container
			for(EffectiveMetamodel em: effectiveMetamodels)
			{
				//if em's name is equal to epack's name
				//if (em.getName().equalsIgnoreCase(ePackage.getName())) {
					
					//for each type in all of kind
					for(EffectiveType et: em.getAllOfKind())
					{
						//get the element name
						String elementName = et.getName();
						//if the element's name is equal to the eclass's name, return true
						if (elementName.equals(eClass.getName())) {
							return true;
						}
						
						//get the eclass by name
						EClass kind = (EClass) ePackage.getEClassifier(elementName);
						
						/*By Sorour*/
//					    if (eClass.getEAllSuperTypes().contains(kind))
//							return true;
						/**/
					}
					
					//for each type in all of type
					for(EffectiveType et: em.getAllOfType())
					{
						//get name
						String elementName = et.getName();
						//if name equals, return true
						if (elementName.equals(eClass.getName())) {
							return true;
						}
					}
				//}
			}
			return false;
		}
		public void addActualObjectToLoad(EClass eClass)
		{
			//get the epackage name
			String epackage = eClass.getEPackage().getNsURI();
			
			//get the submap with the epackage name
			HashMap<EClass, ArrayList<EStructuralFeature>> subMap = actualObjectsAndFeaturesToLoad.get(epackage);
			//HashMap<EClass, ArrayList<EStructuralFeature>> types = typesToLoad.get(epackage);
			ArrayList<EStructuralFeature> refs = new ArrayList<EStructuralFeature>();
					
			//if sub map is null
			if (subMap == null) {
				
				//create new sub map
				subMap = new HashMap<EClass, ArrayList<EStructuralFeature>>();
				
				//create new refs for the map
				refs = getFeaturesForClassToLoad(eClass);
				
				//add the ref to the sub map
				subMap.put(eClass, refs);
				
				//add the sub map to objectsAndRefNamesToVisit
				actualObjectsAndFeaturesToLoad.put(epackage, subMap);
			}
			else
			{
				//if sub map is not null, get the refs by class name
				refs = subMap.get(eClass);

				//if refs is null, create new refs and add the ref and then add to sub map
				if (refs == null) {
					//get refs from allOfKind or allOfType
					refs = getFeaturesForClassToLoad(eClass);
		
					subMap.put(eClass, refs);
				}
			}
		}
		public ArrayList<EStructuralFeature> getFeaturesForClassToLoad(EClass eClass)
		{
			//get the package
			EPackage ePackage = eClass.getEPackage();
			//prepare the result
			ArrayList<EStructuralFeature> result = new ArrayList<EStructuralFeature>();
			
			//for all model containers
			for(EffectiveMetamodel em: effectiveMetamodels)
			{
				//if the container is the container needed
			//	if (em.getName().equals(ePackage.getName())) {
					//for elements all of kind
					//loop1:
					for(EffectiveType et: em.getAllOfKind())
					{
						//if class name equals, add all attributes and references
						if (eClass.getName().equals(et.getName())) {
							for(EffectiveFeature ef: et.getAttributes())
							{
								//result.add(ef.getName());
								if (!result.contains(eClass.getEStructuralFeature(ef.getName())))
									result.add(eClass.getEStructuralFeature(ef.getName()));
							}
							for(EffectiveFeature ef: et.getReferences())
							{
								//result.add(ef.getName());
								if (!result.contains(eClass.getEStructuralFeature(ef.getName())))
									result.add(eClass.getEStructuralFeature(ef.getName()));
							}
							//break loop1;
						}
						
						//if eclass is a sub class of the kind, add all attributes and references
						EClass kind = (EClass) ePackage.getEClassifier(et.getName());
						if (eClass.getEAllSuperTypes().contains(kind) || kind.getEAllSuperTypes().contains(eClass)) {
							for(EffectiveFeature ef: et.getAttributes())
							{
								//Add recently
								if (!result.contains(eClass.getEStructuralFeature(ef.getName())))
									//result.add(ef.getName());
									result.add(eClass.getEStructuralFeature(ef.getName()));
							}
							for(EffectiveFeature ef: et.getReferences())
							{
								//Add recently
								if (!result.contains(eClass.getEStructuralFeature(ef.getName())))
									//result.add(ef.getName());
									result.add(eClass.getEStructuralFeature(ef.getName()));
							}
							//break loop1;
						}
					}
					
					//for elements all of type
					//loop2:
					for(EffectiveType et: em.getAllOfType())
					{
						//if class name equals, add all references and attributes
						if (eClass.getName().equals(et.getName())) {
							for(EffectiveFeature ef: et.getAttributes())
							{
								//result.add(ef.getName());
								result.add(eClass.getEStructuralFeature(ef.getName()));
							}
							for(EffectiveFeature ef: et.getReferences())
							{
								//result.add(ef.getName());
								result.add(eClass.getEStructuralFeature(ef.getName()));
							}
							//break loop2;
						}
					}
			//	}
			}
			return result;
		}
		public ArrayList<EffectiveMetamodel> getEffectiveMetamodels() {
			return effectiveMetamodels;
		}
		
		public void addEffectiveMetamodel(EffectiveMetamodel effectiveMetamodel)
		{
			effectiveMetamodels.add(effectiveMetamodel);
		}
		
		public void addPackage(EPackage ePackage)
		{
			packages.add(ePackage);
		}
		
		public void addPackages(Collection<?> packages)
		{
			this.packages.addAll((Collection<? extends EPackage>) packages);
		}
		public EPackage getPackage(String packageName) {
			for (EPackage pkg : packages)
				if (pkg.getNsURI().equals(packageName))
					return pkg;
			return null;
		}
		public HashMap<String, HashMap<EClass, ArrayList<EStructuralFeature>>> getActualObjectsToLoad() {
			return actualObjectsAndFeaturesToLoad;
		}
		public HashMap<String, HashMap<EClass, ArrayList<EStructuralFeature>>> getTypesToLoad() {
			return typesToLoad;
		}
}
