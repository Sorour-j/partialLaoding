/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.neo4j.partitioning;

import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.neo4j.Neo4jModel;
import org.eclipse.epsilon.evl.launch.EvlRunConfiguration;

public class EvlPartitioningNeo4jModelStandaloneExample {

	public static void main(String[] args) throws Exception {
		String db = "/Users/sorourjahanbin/git/mainandstaticanalysis/org.eclipse.epsilon.neo4j/target/FindBugs-1.5";
		String metamodel = "/Users/sorourjahanbin/git/mainandstaticanalysis/org.eclipse.epsilon.neo4j/model/java_findbugs.ecore";// args[1];
		String script = "/Users/sorourjahanbin/git/mainandstaticanalysis/org.eclipse.epsilon.neo4j/testConstraints/findbugs.evl";// args[2];

		StringProperties modelProperties = new StringProperties();
		modelProperties.setProperty(Neo4jModel.PROPERTY_NAME, "Neo4jModel");
		modelProperties.setProperty(Neo4jModel.PROPERTY_USERNAME, "neo4j");   
		modelProperties.setProperty(Neo4jModel.PROPERTY_URI, "bolt://localhost:7687");
		modelProperties.setProperty(Neo4jModel.PROPERTY_PASSWORD, "password");
		modelProperties.setProperty(Neo4jModel.PROPERTY_DATABASE, "neo4j");
		modelProperties.setProperty("type", "Neo4jModel");
		modelProperties.setProperty(Neo4jModel.PROPERTY_DATABASEPATH, db);
		modelProperties.setProperty(Neo4jModel.PROPERTY_METAMODEL_FILE, metamodel);
		modelProperties.setProperty(Neo4jModel.PROPERTY_METAMODEL_URI,
				"http://www.eclipse.org/MoDisco/Java/0.2.incubation/java");

		EvlRunConfiguration runConfig = EvlRunConfiguration.Builder().withModule(new PartialEvlModule())
				.withScript(script).withModel(new Neo4jModel(), modelProperties).withParameter("Thread", Thread.class)
				 //.withResults()
				.withProfiling().build();
		EvlPartitioningRunConfiguration neo = new EvlPartitioningRunConfiguration(runConfig);
		neo.run();
	}
}