/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.neo4j.example.standalone;

import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.neo4j.Neo4jModel;
import org.eclipse.epsilon.eol.launch.EolRunConfiguration;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.IEvlModule;
import org.eclipse.epsilon.evl.launch.EvlRunConfiguration;
public class EvlNeo4jModelStandaloneExample {
	
	public static void main(String[] args) throws Exception {
		String db = "/Users/sorourjahanbin/git/mainandstaticanalysis/org.eclipse.epsilon.neo4j/target/ProjectDB";
		String metamodel = "/Users/sorourjahanbin/git/mainandstaticanalysis/org.eclipse.epsilon.neo4j/model/psl.ecore";//args[1];
		String script = "/Users/sorourjahanbin/git/mainandstaticanalysis/org.eclipse.epsilon.neo4j/src/org/eclipse/epsilon/neo4j/benchmarks/psl.evl";//args[2];
	
		StringProperties modelProperties = new StringProperties();
		modelProperties.setProperty(Neo4jModel.PROPERTY_NAME, "Neo4jModel");
		modelProperties.setProperty(Neo4jModel.PROPERTY_USERNAME, "neo4j");
		modelProperties.setProperty(Neo4jModel.PROPERTY_URI, "bolt://localhost:7687");
		modelProperties.setProperty(Neo4jModel.PROPERTY_PASSWORD,"password");
		modelProperties.setProperty(Neo4jModel.PROPERTY_DATABASE, "neo4j");
		modelProperties.setProperty("type", "Neo4jModel");
		modelProperties.setProperty(Neo4jModel.PROPERTY_DATABASEPATH, db);
		modelProperties.setProperty(Neo4jModel.PROPERTY_METAMODEL_FILE, metamodel);
		modelProperties.setProperty(Neo4jModel.PROPERTY_METAMODEL_URI, "psl");
		
		EvlRunConfiguration runConfig = EvlRunConfiguration.Builder()
			.withScript(script)
			.withModel(new Neo4jModel(), modelProperties)
			.withModule(new EvlModule())
			.withParameter("Thread", Thread.class)
			.withProfiling()
			.build();
		EvlNeoEMFRunConfiguration neo = new EvlNeoEMFRunConfiguration(runConfig);
		neo.run();
	}
}