package org.eclipse.epsilon.emc.neo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodel;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import org.eclipse.epsilon.evl.dom.Constraint;
import org.eclipse.epsilon.neo4j.partitioning.PartitioningHandler;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

import com.sun.xml.bind.v2.model.core.TypeRef;

public class Neo4jModel extends EmfModel implements AutoCloseable {

	public static final String PROPERTY_URI = "uri";
	public static final String PROPERTY_USERNAME = "username";
	public static final String PROPERTY_PASSWORD = "password";
	public static final String PROPERTY_DATABASE = "database";
	public static final String PROPERTY_DATABASEPATH = "databasepath";
	int counter = 0;
	protected String uri;
	protected String username;
	protected String password;
	protected String database;
	protected String databasePath;
	protected String metamodelNsuri;
	protected EffectiveMetamodel effectiveMetamodel = null;
	
	ResourceSet resourceSet = new ResourceSetImpl();
	ResourceSet metamodelResource = new ResourceSetImpl();
	Resource resource = new XMIResourceImpl();
	EPackage pkg;
	EFactory factory;
	ConnectionSetting connection;
	private Driver driver;
	int num = 0;
	// Name of Constraint, Effective meta-model
	protected HashMap<Set<Constraint>, EffectiveMetamodel> effectiveMetamodels = new HashMap<Set<Constraint>, EffectiveMetamodel>();

	protected HashMap<String, EObject> createdObjects = new HashMap<String, EObject>();
	public Set<EObject> resourceList = new HashSet<EObject>();
	public List<String> labels = new ArrayList<String>();
	Stack<String> variables = new Stack<String>();

	public Neo4jEffectiveMetamodelReconciler effectiveMetamodelReconciler = new Neo4jEffectiveMetamodelReconciler();
	protected HashMap<EClass, ArrayList<EStructuralFeature>> actualObjectsAndFeaturesToLoad = new HashMap<EClass, ArrayList<EStructuralFeature>>();
	protected HashMap<EClass, ArrayList<EStructuralFeature>> objectsToLoad = new HashMap<EClass, ArrayList<EStructuralFeature>>();
	protected QueryHandler queryhandler;
	protected PartitioningHandler partitionHandler;

	public Neo4jModel() {

		propertyGetter = new ResultPropertyGetter(this);
	}

	public void setProperties(StringProperties properties) {
		this.uri = properties.getProperty(PROPERTY_URI);
		this.username = properties.getProperty(PROPERTY_USERNAME);
		this.password = properties.getProperty(PROPERTY_PASSWORD);
		this.database = properties.getProperty(PROPERTY_DATABASE);
		this.databasePath = properties.getProperty(PROPERTY_DATABASEPATH);
	}

	@Override
	public void load(StringProperties properties, IRelativePathResolver resolver) throws EolModelLoadingException {
		super.load(properties, resolver);
		this.uri = properties.getProperty(PROPERTY_URI);
		this.username = properties.getProperty(PROPERTY_USERNAME);
		this.password = properties.getProperty(PROPERTY_PASSWORD);
		this.database = properties.getProperty(PROPERTY_DATABASE);
		this.databasePath = properties.getProperty(PROPERTY_DATABASEPATH);
		load();
	}

	public void load(EffectiveMetamodel efMetamodel) {
		this.effectiveMetamodel = efMetamodel;

		effectiveMetamodel.setNsuri(metamodelUris.get(0).toString());
		try {
			load();
		} catch (EolModelLoadingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void load() throws EolModelLoadingException {

		if (effectiveMetamodel == null)
			return;

		createResource(this.getMetamodelUris().get(0));
		if (connection == null) {
			connection = new ConnectionSetting(uri, username, password, databasePath, 7687);
			driver = connection.getDriver();
		}
		queryhandler = new QueryHandler();

		if (labels.isEmpty())
			labels.addAll(queryhandler.loadLabels(driver, database));

		createdObjects = new HashMap<String, EObject>();

		ArrayList<String> queries = queryhandler.generateQuery(pkg, labels, actualObjectsAndFeaturesToLoad, objectsToLoad);
		//num += queries.size(); 
		//System.out.println("Number of Queries: " + num);
		for (String query : queries) {
			try (Session session = driver.session(SessionConfig.forDatabase(database))) {

				Result result = session.run(query);
				System.out.println(query);
				while (result.hasNext()) {
					Record record = result.next();
					createObjectFromRecord(record);
				}
			}
		}
		this.setResource(resource);
	}

	void createObjectFromRecord(Record record) {

		for (EClassifier c : pkg.getEClassifiers()) {

			if (objectsToLoad.containsKey(c)) {

				String source = c.getName().toLowerCase();
				String id = record.get("ID(" + source + ")").toString();

				if (id != "NULL") {
					EObject object;

					String eclassType = record.get(source + "Type.name").toString().replaceAll("\"", "");

					if (createdObjects.get(id) == null) {

						object = factory.create((EClass) pkg.getEClassifier(eclassType));
						createdObjects.put(id, object);
					} else {
						object = createdObjects.get(id);
					}

					ArrayList<EStructuralFeature> features = new ArrayList<EStructuralFeature>();
					features.addAll(objectsToLoad.get((EClass) c));

					variables.push(source);
					handleFeatures(eclassType, record, c, object);
					/*
					 * for (EStructuralFeature f : features) {
					 * 
					 * 
					 * if (f instanceof EAttribute && ((EClass)
					 * pkg.getEClassifier(eclassType)).getEAllStructuralFeatures().contains(f)) {
					 * 
					 * if (((EAttribute) f).getEAttributeType() instanceof EEnum) { String type =
					 * ((EAttribute) f).getEAttributeType().getName(); EEnum eEnum = (EEnum)
					 * pkg.getEClassifier(type);
					 * 
					 * object.eSet(f,
					 * eEnum.getEEnumLiteralByLiteral(record.get(c.getName().toLowerCase() + "." +
					 * f.getName()) .toString().replaceAll("\"", ""))); } else if (((EAttribute)
					 * f).getEAttributeType() == EcorePackage.Literals.EBOOLEAN) {
					 * 
					 * boolean type = Boolean .parseBoolean(record.get(c.getName().toLowerCase() +
					 * "." + f.getName()) .toString().replaceAll("\"", "")); object.eSet(f, type); }
					 * else if (((EAttribute) f).getEAttributeType() == EcorePackage.Literals.EINT)
					 * {
					 * 
					 * int type = Integer.parseInt(record.get(c.getName().toLowerCase() + "." +
					 * f.getName()) .toString().replaceAll("\"", "")); object.eSet(f, type); } else
					 * if (((EAttribute) f).getEAttributeType() ==
					 * EcorePackage.Literals.EDOUBLE_OBJECT) {
					 * 
					 * double type = Double .parseDouble(record.get(c.getName().toLowerCase() + "."
					 * + f.getName()) .toString().replaceAll("\"", "")); object.eSet(f, type); }
					 * else object.eSet(f, record.get(c.getName().toLowerCase() + "." +
					 * f.getName()).toString()); } else if (f instanceof EReference && ((EClass)
					 * pkg.getEClassifier(eclassType)).getEAllStructuralFeatures().contains(f)) {
					 * 
					 * EReference ref = (EReference) f; String type =
					 * record.get(f.getName().toLowerCase() + f.getEType().getName().toLowerCase() +
					 * "Type.name").toString() .replaceAll("\"", "");//f.getName().toLowerCase() +
					 * EClass cls = (EClass) pkg.getEClassifier(type); // If there are two
					 * references with the same type, the variable will be same?! // How we can
					 * manage? String idT = record.get("ID(" + f.getName().toLowerCase() +
					 * f.getEType().getName().toLowerCase() + ")").toString();//
					 * 
					 * if (idT != "NULL") { // when the ref is Empty >> Id and Type will be returned
					 * as NULL
					 * 
					 * EObject obj; if (createdObjects.get(idT) == null) { obj =
					 * factory.create(cls); createdObjects.put(idT, obj); } else { obj =
					 * createdObjects.get(idT);
					 * 
					 * }
					 * 
					 * if (ref.isMany()) { ((EList) object.eGet(f)).add(obj); } else {
					 * object.eSet(f, obj); } }
					 * 
					 * }
					 * 
					 * // Maybe the Object is created by the Container fist >> It will be added to
					 * // EAllContents and then add to content >> Duplication!! /* So it should be
					 * checked and if Object is already there, it should be replaced Not Add Again!
					 */
					// }
					if (object.eContainer() == null)
						resource.getContents().add(object);
					if (!object.eContents().isEmpty()) {
						resource.getContents().removeAll(object.eContents());
					}
				}
			}
		}
	}

	public EObject handleFeatures(String eclassType, Record record, EClassifier c, EObject object) {

		ArrayList<EStructuralFeature> features = new ArrayList<EStructuralFeature>();
	//	System.out.println(c.getName());
		if (objectsToLoad.containsKey(c))
			features.addAll(objectsToLoad.get((EClass) c));
		else {
			c = pkg.getEClassifier("MethodDeclaration");
			features.addAll(objectsToLoad.get((EClass) pkg.getEClassifier("MethodDeclaration")));
		}
		String source = variables.pop();

		for (EStructuralFeature feature : features) {

			if (feature instanceof EAttribute
					&& ((EClass) pkg.getEClassifier(eclassType)).getEAllStructuralFeatures().contains(feature)) {

				if (((EAttribute) feature).getEAttributeType() instanceof EEnum) {
					String type = ((EAttribute) feature).getEAttributeType().getName();
					EEnum eEnum = (EEnum) pkg.getEClassifier(type);

					object.eSet(feature, eEnum.getEEnumLiteralByLiteral(
							record.get(source + "." + feature.getName()).toString().replaceAll("\"", "")));
				} else if (((EAttribute) feature).getEAttributeType() == EcorePackage.Literals.EBOOLEAN ) {

					boolean type = Boolean
							.parseBoolean(record.get(source + "." + feature.getName()).toString().replaceAll("\"", ""));
					object.eSet(feature, type);
				} else if (((EAttribute) feature).getEAttributeType() == EcorePackage.Literals.EINT) {

					int type = Integer
							.parseInt(record.get(source + "." + feature.getName()).toString().replaceAll("\"", ""));
					object.eSet(feature, type);
				} else if (((EAttribute) feature).getEAttributeType() == EcorePackage.Literals.EDOUBLE_OBJECT) {

					double type = Double
							.parseDouble(record.get(source + "." + feature.getName()).toString().replaceAll("\"", ""));
					object.eSet(feature, type);
				} else {
					 EAttribute atr = (EAttribute) feature; 
					// System.out.println(atr.getEAttributeType());System.out.println(EcorePackage.Literals.EBOOLEAN);
					object.eSet(feature, record.get(source + "." + feature.getName()).toString());
				}
			} else if (feature instanceof EReference
					&& ((EClass) pkg.getEClassifier(eclassType)).getEAllStructuralFeatures().contains(feature)) {

				EReference ref = (EReference) feature;

				String typeRef = c.getName();// ref.getName() + c.getName();//.toLowerCase();

				String type = record.get(feature.getName().toLowerCase() + typeRef.toLowerCase() + "Type.name")
						.toString().replaceAll("\"", "");
				EClass cls = (EClass) pkg.getEClassifier(type);
				// If there are two references with the same type, the variable will be same?!
				// How we can manage?
				String target = ref.getName().toLowerCase() + typeRef.toLowerCase();
				// feature.getEType().getName().toLowerCase()
				String idT = record.get("ID(" + target + ")").toString();//

				if (idT != "NULL") { // when the ref is Empty >> Id and Type will be returned as NULL

					EObject obj;
					if (createdObjects.get(idT) == null) {
						//factory.create(cls);
						obj = factory.create((EClass) pkg.getEClassifier(cls.getName()));
						createdObjects.put(idT, obj);
						
						if ((objectsToLoad.containsKey(cls)) && !objectsToLoad.get(cls).isEmpty())
						{
							variables.push(target);
							obj = handleFeatures(type, record, pkg.getEClassifier(ref.getEType().getName()), obj);
						}
					} else {
						obj = createdObjects.get(idT);

					}

					if (ref.isMany()) {
						((EList) object.eGet(feature)).add(obj);
					} else {
						object.eSet(feature, obj);
					}
				}
				// return object;
			}
		}
		if (object.eContainer() == null)
			resource.getContents().add(object);
		if (!object.eContents().isEmpty()) {
			resource.getContents().removeAll(object.eContents());
		}
		return object;
	}

	@Override
	public String toString() {
		return "Neo4j Model [name=" + getName() + "]";
	}

	@Override
	public void close() {
		connection.registerShutdownHook();
		try {
			connection.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("driver is not closed");
		}
		driver.close();
	}

	public void setEffectiveMteamodel(EffectiveMetamodel efMetamodel) {
		this.effectiveMetamodel = efMetamodel;
		effectiveMetamodel.setName(this.name);
	}

	public EffectiveMetamodel getEffectiveMteamodel() {
		return this.effectiveMetamodel;
	}
	public EffectiveMetamodel getEffectiveMteamodel(Constraint c) {
		for (Set<Constraint> con : effectiveMetamodels.keySet()) {
			if (con.contains(c)) {
				return effectiveMetamodels.get(con);
			}
		}
		return null;
	}

	public void setEffectiveMteamodels(LinkedHashMap<Constraint, EffectiveMetamodel> efmetamodels, boolean isPartitioned) {
		effectiveMetamodels = new HashMap<Set<Constraint>, EffectiveMetamodel>();
		if (isPartitioned) {
			partitionHandler = new PartitioningHandler(efmetamodels);
			effectiveMetamodels.putAll(partitionHandler.getPartitions());
		}
		else {
			for (Entry<Constraint, EffectiveMetamodel> en : efmetamodels.entrySet()) {
					Set<Constraint> cons = new HashSet<Constraint>();
					cons.add(en.getKey());
					effectiveMetamodels.put(cons, en.getValue());
			}
		}
	}

	public HashMap<Set<Constraint>, EffectiveMetamodel> getEffectiveMteamodels() {
		return effectiveMetamodels;
	}

	public void setMetamodelResource(ResourceSet resourceSet) {
		this.metamodelResource = resourceSet;
	}

	public void createResource(String packageName) {

		// if (this.effectiveMetamodelReconciler.getPackage(packageName) != null)
		resource = new XMIResourceImpl();
		effectiveMetamodelReconciler = new Neo4jEffectiveMetamodelReconciler();
		this.effectiveMetamodelReconciler.addPackages(metamodelResource.getPackageRegistry().values());
		this.effectiveMetamodelReconciler.addEffectiveMetamodel(getEffectiveMteamodel());
		this.effectiveMetamodelReconciler.reconcile();
		objectsToLoad.clear();

		if (!effectiveMetamodelReconciler.getActualObjectsToLoad().isEmpty()) {
			actualObjectsAndFeaturesToLoad = effectiveMetamodelReconciler.getActualObjectsToLoad().get(packageName);
			objectsToLoad.putAll(effectiveMetamodelReconciler.getActualObjectsToLoad().get(packageName));
		}
		if (!effectiveMetamodelReconciler.getTypesToLoad().isEmpty())
			objectsToLoad.putAll(effectiveMetamodelReconciler.getTypesToLoad().get(packageName));

		pkg = effectiveMetamodelReconciler.getPackage(packageName);
		factory = pkg.getEFactoryInstance();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(pkg.getNsURI(), pkg);
		resource = resourceSet.createResource(URI.createFileURI(this.name));
	}

	public PartitioningHandler getPartitioningHandler() {
		return partitionHandler;
	}

	public QueryHandler getqartitioningHandler() {
		return queryhandler;
	}
}
