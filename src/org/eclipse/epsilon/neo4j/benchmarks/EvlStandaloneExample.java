/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.neo4j.benchmarks;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.effectivemetamodel.XMIN;
import org.eclipse.epsilon.eol.launch.EolRunConfiguration;
import org.eclipse.epsilon.evl.launch.EvlRunConfiguration;
import org.eclipse.epsilon.emc.emf.EmfModel;

/**
 * This example demonstrates using the Epsilon Object Language, the core language of Epsilon, in a stand-alone manner 
 * 
 * @author Sina Madani
 * @author Dimitrios Kolovos
 */
public class EvlStandaloneExample {
	
	public static void main(String[] args) throws Exception {
		Path root = Paths.get(EvlStandaloneExample.class.getResource("").toURI()),
			modelsRoot = root.getParent().resolve("standalone");
		
		StringProperties modelProperties = new StringProperties();
		modelProperties.setProperty(EmfModel.PROPERTY_NAME, "movies");
		modelProperties.setProperty(EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI,
			modelsRoot.resolve("/Users/sorourjahanbin/git/mainandstaticanalysis/org.eclipse.epsilon.neo4j/src/org/eclipse/epsilon/neo4j/benchmarks/movies.ecore").toAbsolutePath().toUri().toString()
		);

		modelProperties.setProperty("type", "EMF");
		modelProperties.setProperty(EmfModel.PROPERTY_MODEL_URI,
			modelsRoot.resolve("/Users/sorourjahanbin/git/mainandstaticanalysis/org.eclipse.epsilon.neo4j/model/imdb-all.xmi").toAbsolutePath().toUri().toString()
		);
		
		EvlRunConfiguration runConfig = EvlRunConfiguration.Builder()
			.withScript(root.resolve("/Users/sorourjahanbin/git/mainandstaticanalysis/org.eclipse.epsilon.neo4j/src/org/eclipse/epsilon/neo4j/benchmarks/imdbValidator.evl"))
			.withModel(new EmfModel(), modelProperties)
			.withProfiling()
			.build();
	
		runConfig.run();
	}
	
}
