package org.eclipse.epsilon.emc.neo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

import org.apache.lucene.analysis.CharArrayMap.EntrySet;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.epsilon.eol.m3.StructuralFeature;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

import scala.concurrent.duration.DurationConversions.Classifier;

public class QueryHandler {

	public QueryHandler() {
	}

	
	List<String> variables = new ArrayList<String>();
	List<EClassifier> addedClsses = new ArrayList<EClassifier>();
	HashSet<String> returns = new HashSet<String>();
	LinkedHashSet<String> matchString = new LinkedHashSet<String>(); // LinkedHashSet keeps the order => important for
																		// generating query
	LinkedHashSet<String> optionalMatch = new LinkedHashSet<String>(); // LinkedHashSet keeps the order => important for
																		// generating query
	HashMap<EClass, ArrayList<EStructuralFeature>> actualObjectsAndFeaturesToLoad = new HashMap<EClass, ArrayList<EStructuralFeature>>();
	HashMap<EClass, ArrayList<EStructuralFeature>> objectsToLoad = new HashMap<EClass, ArrayList<EStructuralFeature>>();
	EPackage pkg;
	ArrayList<String> loopTrack = new ArrayList<String>();

	List<String> labels = new ArrayList<String>();
	HashSet<EClassifier> queryClasses = new HashSet<EClassifier>(); // Classes that should be considered for query
																	// generation!
	Stack<String> varNames = new Stack<String>(); // The type of reference can be same for two reference, so we need another variabel name
												// to set for target of ref, but we should manage it when we are following the references
	HashMap<String, HashSet<String>> matches = new HashMap<String, HashSet<String>>();
	ArrayList<String> queries = new ArrayList<String>(); // List of generated queries
	String refQuery = "";

	public List<String> loadLabels(Driver driver, String database) {

		try (Session session = driver.session(SessionConfig.forDatabase(database))) {

			Result result = session.run("MATCH (n) RETURN distinct labels(n)");
			while (result.hasNext()) {
				Record record = result.next();
				String st = record.get(0).toString().replaceAll("\\[", "");
				st = st.replaceAll("\\]", "");
				st = st.replaceAll("\"", "");

				while (st != "") {
					if (st.contains(",")) {
						String label;
						label = st.substring(0, st.indexOf(","));
						labels.add(label);
						st = st.replaceAll(label + ", ", "");
					} else {
						labels.add(st);
						break;
					}
				}
				labels.remove("");
			}
		}
		return labels;
	}
	
	public ArrayList<String> generateQuery(EPackage pkg, List<String> labels,
			HashMap<EClass, ArrayList<EStructuralFeature>> actualObjectsAndFeaturesToLoad,
			HashMap<EClass, ArrayList<EStructuralFeature>> objectsToLoad) {

		this.objectsToLoad = objectsToLoad;
		this.actualObjectsAndFeaturesToLoad = actualObjectsAndFeaturesToLoad;
		this.pkg = pkg;
		this.labels = labels;

		queryClasses = new LinkedHashSet<EClassifier>();

		queryClasses.addAll(actualObjectsAndFeaturesToLoad.keySet());

		// If Superclass exists in Effective meta-model, remove the subclass
		for (Iterator<EClassifier> it = queryClasses.iterator(); it.hasNext();) {
			EClass cls = (EClass) it.next();
			for (EClass c : cls.getEAllSuperTypes())
				if (queryClasses.contains(c)) {
					for (Iterator<EStructuralFeature> itt = actualObjectsAndFeaturesToLoad.get(cls).iterator(); itt
							.hasNext();) {
						EStructuralFeature f = itt.next();
						if (actualObjectsAndFeaturesToLoad.get(c).contains(f)) {
							itt.remove();
							if (actualObjectsAndFeaturesToLoad.get(cls).isEmpty())
								it.remove();
						}
					}
				}
		}

		for (EClassifier cls : queryClasses) {
			optionalMatch.clear();
			queryGenerator(cls);
		}

		return queries;
	}

	public void handleFeatures(EClass cls) {

		loopTrack.add(cls.getName());
		
		String source = varNames.pop();//cls.getName().toLowerCase();// 
		
		
		if (objectsToLoad.containsKey(cls) && objectsToLoad.get(cls) != null) {
			
			ArrayList<EStructuralFeature> features = objectsToLoad.get(cls);
			
			for (EClass c : objectsToLoad.keySet()) {
				if (cls.isSuperTypeOf(c) && cls != c)
					features.addAll(objectsToLoad.get(c));
			}
			
			for (EStructuralFeature f : features) {
			
				if (optionalMatch.isEmpty())
					 refQuery = " OPTIONAL MATCH ";

				if (f instanceof EReference){
					
					refQuery += "(" + source + ")-[";
					
					// var for reference is ( lowercase of name of ref + "ref" + source )
					String reference = f.getName().toLowerCase() + "ref" + source;

					if (!variables.contains(reference + ":" + f.getName())) {
						refQuery += reference + ":" + f.getName() + "]->";
						variables.add(reference + ":" + f.getName());
					} else
						refQuery += reference + "]->";
					
					// var for target of reference > name of ref + source of reference
					String targetVar =  f.getName().toLowerCase() + cls.getName().toLowerCase(); //;typeRef.toLowerCase();//
					
					
					// Type of reference: based on resolved Type
					String typeRef = f.getEType().getName();
				//	String className = cls.getName();
					EClass typeCls = (EClass) f.getEType();
					
					// If the type of reference is not in Object to load, its super classes will be checked
					if (!objectsToLoad.containsKey(f.getEType())) {
						typeCls = (EClass) pkg.getEClassifier("MethodDeclaration");
						typeRef = "MethodDeclaration";
						/*for (EClass c : objectsToLoad.keySet()) {
							if (c.getEAllSuperTypes().contains(f.getEType()) && !c.getName().equals(cls.getName())) {
								typeRef = c.getName();
								typeCls =c;
							}
						}*/
					}
					
					
					if (!variables.contains(targetVar + ":" + typeRef)) {
						refQuery += "(" + targetVar + ":" + typeRef + "),";
						variables.add(targetVar + ":" + typeRef);
						optionalMatch.add(refQuery);
						refQuery = " OPTIONAL MATCH";
						returns.add("ID(" + targetVar + ")");
						optionalMatch.add(
								"(" + targetVar + ")-[" + targetVar + "ins:instanceOf]->(" + targetVar + "Type" + "),");
						returns.add(targetVar + "Type.name");
					} else {
						refQuery += "(" + targetVar + "),";
						optionalMatch.add(refQuery);
					}
					

					if (!loopTrack.contains(typeCls.getName()) && !actualObjectsAndFeaturesToLoad.containsKey(typeCls)) {
						varNames.push(targetVar);
						refQuery = "";
						handleFeatures(typeCls);
						refQuery = " OPTIONAL MATCH";
					}

				} else if (f instanceof EAttribute ) {//&& ((EClass) pkg.getEClassifier(cls.getName())).getEAllStructuralFeatures().contains(f)) {
					returns.add(source + "." + f.getName());
				}
			}
		}
	}

	public void queryGenerator(EClassifier cls) {
		String source = null;

		if (labels.contains(cls.getName())) {
			
			EClass c = (EClass) cls;
			if (objectsToLoad != null && objectsToLoad.containsKey(c)) {

				matchString = new LinkedHashSet<String>();
				returns = new HashSet<String>();
				variables = new ArrayList<String>();
				loopTrack = new ArrayList<String>();
				ArrayList<EStructuralFeature> features = new ArrayList<EStructuralFeature>();
				features.addAll(objectsToLoad.get(c));
				// var for class to MATCH, lowercase of name of class
				source = cls.getName().toLowerCase();

				if (!variables.contains(source + ":" + cls.getName())) {
					matchString.add("(" + source + ":" + cls.getName() + ")");
					variables.add(source + ":" + cls.getName());
					returns.add("ID(" + source + ")");
					matchString.add("(" + source + ")-[" + source + "ins:instanceOf]->(" + source + "Type" + ")");
					returns.add(source + "Type.name");
				}
				varNames.push(source);
				
				handleFeatures(c);

				String oneQuery = "";
				if (!matchString.isEmpty()) {
					StringBuilder sb;
					oneQuery = "MATCH ";
					for (String st : matchString)
						oneQuery += st + ",";
					sb = new StringBuilder(oneQuery);
					sb.deleteCharAt(oneQuery.length() - 1);
					oneQuery = sb.toString();

					if (!optionalMatch.isEmpty()) {
					//	oneQuery = oneQuery + " OPTIONAL MATCH ";
						for (String st : optionalMatch)
			//				oneQuery += st + ",";
							if (st.contains("OPTIONAL MATCH") && oneQuery.substring(oneQuery.length() - 1).equals(",")) {
								sb = new StringBuilder(oneQuery);
								sb.deleteCharAt(oneQuery.length() - 1);
								oneQuery = sb.toString();
								
								oneQuery += st;// + ",";
							}
							else
								oneQuery += st ;//+ ",";
						
						sb = new StringBuilder(oneQuery);
						sb.deleteCharAt(oneQuery.length() - 1);
						oneQuery = sb.toString();
					}

					oneQuery = oneQuery + " RETURN ";

					for (String st : returns)
						oneQuery += st + ",";

					sb = new StringBuilder(oneQuery);
					sb.deleteCharAt(oneQuery.length() - 1);
					oneQuery = sb.toString();
					queries.add(oneQuery);
				}
			//System.out.println(oneQuery + "\n");
			}
		}
	}
}