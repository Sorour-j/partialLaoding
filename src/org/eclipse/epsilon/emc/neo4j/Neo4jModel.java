package org.eclipse.epsilon.emc.neo4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodel;
import org.eclipse.epsilon.effectivemetamodel.EffectiveType;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.emf.EmfModelMetamodel;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolEnumerationValueNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.exceptions.models.EolNotInstantiableModelElementTypeException;
import org.eclipse.epsilon.eol.m3.Metamodel;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import org.eclipse.epsilon.eol.models.Model;
import org.eclipse.epsilon.smartsaxparser.EffectiveMetamodelReconciler;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;

public class Neo4jModel extends EmfModel implements AutoCloseable {

	public static final String PROPERTY_URI = "uri";
	public static final String PROPERTY_USERNAME = "username";
	public static final String PROPERTY_PASSWORD = "password";
	public static final String PROPERTY_DATABASE= "database";
	public static final String PROPERTY_DATABASEPATH= "databasepath";
	
	protected String uri;
	protected String username;
	protected String password;
	protected String database;
	protected String databasePath;
	protected EffectiveMetamodel effectiveMetamodel = null;

	protected Collection<Record> records = new ArrayList<Record>();
	HashMap<String, Collection<Record>> objects = new HashMap<String, Collection<Record>>();
	
	private Driver driver;
	EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
	EFactory factory = EcoreFactory.eINSTANCE.createEFactory();
	
	public EffectiveMetamodelReconciler effectiveMetamodelReconciler = new EffectiveMetamodelReconciler();

	public Neo4jModel() {
		
		propertyGetter = new ResultPropertyGetter(this);
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
	@Override
	public void load() throws EolModelLoadingException {

		if ( effectiveMetamodel == null)
			return;
		
		ConnectionSetting connection = new ConnectionSetting(uri, username, password,databasePath,7687);
		
		
		//actual objects to load from reconciler would be a better idea, Maybe!
		
		for (EffectiveType type : effectiveMetamodel.getAllOfKind()) {
			
			String query = "MATCH ( t : " + type.getName() + ") RETURN ";
			Set<EClass> classes = effectiveMetamodelReconciler.getActualObjectsToLoad().get(name).keySet();
			for (EClass c : classes) {

				if (c.getName().equals(type.getName())) {
					for (EStructuralFeature f : effectiveMetamodelReconciler.getActualObjectsToLoad().get(name)
							.get(c)) {

						query += "t." + f.getName() + " As " + f.getName() + ",";
					}
				}
			}
			StringBuilder sb = new StringBuilder(query);
			sb.deleteCharAt(query.length() - 1);
			query = sb.toString();
			System.out.println(query);
			
			try (Session session = driver.session(SessionConfig.forDatabase(database))) {

				Result result = session.run(query);
				while (result.hasNext()) {
//					EffectiveRecord record = (EffectiveRecord)result.next();
//					record.setTypeName(type.getName());
//					records.add(record);
					Record record = result.next();
					createObjectFromRecord(record);
					this.getResource().getContents().add(pkg);
				}
			}
			objects.put(type.getName(), records);
		}
		
		
		pkg.setNsURI("Neo4jModel");
		pkg.setName("javaMM");
		pkg.setEFactoryInstance(factory);
		
	}

	 void createObjectFromRecord(Record record) {
		
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(record.get("label").toString());
		pkg.getEClassifiers().add(eClass);
 		EObject cls = factory.create(eClass);
 		
		for (String s : record.keys()) {
			EAttribute attr = EcoreFactory.eINSTANCE.createEAttribute();
			attr.setName(s);
			attr.setEType(EcorePackage.Literals.ESTRING);
			eClass.getEStructuralFeatures().add(attr);		
	 		cls.eSet(attr,record.values().get(0).toString());
			}
	}
	@Override
	public String toString() {
		return "Neo4j Model [name=" + getName() + "]";
	}

	@Override
	public void close() {
		driver.close();
	}

	public void setEffectiveMteamodel(EffectiveMetamodel efMetamodel) {
		this.effectiveMetamodel = efMetamodel;
		effectiveMetamodel.setName(this.name);
	}
	
	public EffectiveMetamodel getEffectiveMteamodel() {
		return this.effectiveMetamodel;
	}
}
