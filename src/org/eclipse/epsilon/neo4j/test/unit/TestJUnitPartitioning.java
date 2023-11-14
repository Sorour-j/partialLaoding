package org.eclipse.epsilon.neo4j.test.unit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodel;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.epsilon.evl.dom.Constraint;
import org.eclipse.epsilon.neo4j.partitioning.PartialEvlModule;
import org.eclipse.epsilon.neo4j.partitioning.PartitioningHandler;
import org.junit.Test;

public class TestJUnitPartitioning {
		
		ResourceSet resourceSet = new ResourceSetImpl();
		ArrayList<String> labels = new ArrayList<String>();
		PartialEvlModule module;
		@Test
		public void oneAtrOneRef() throws Exception {
			
			module = getModule("context Movie {\n"
					+ "	constraint hasTitle {\n"
					+ "		check : self.`title` <> \"\"\n"
					+ "	}\n"
					+"constraint titleSize {\n"
					+ "		check : self.`title`.size() > 2 \n}"
					+ "}context Person {\n"
					+ "	constraint isValidName {\n"
					+ "		check:\n"
					+ "			self.name.length() > 3\n"
					+ "	}\n"
					+ "}");
			
			HashMap<Constraint, EffectiveMetamodel> program = new HashMap<Constraint, EffectiveMetamodel>();
			ArrayList<Set<Constraint>> actualPartitions = new ArrayList<Set<Constraint>>();
			ArrayList<Set<Constraint>> expectedPartitions = new ArrayList<Set<Constraint>>();
			
			Set<Constraint> part1 = new HashSet<Constraint>();
			Set<Constraint> part2 = new HashSet<Constraint>();
			EffectiveMetamodel efMM1 = new EffectiveMetamodel();
			/*Effective Meta-model*/
			Constraint con1 = module.getConstraints().get(0);
			
			//con1.getNameExpression().setName("hasTitle");
			efMM1 = new EffectiveMetamodel();
			efMM1.addToAllOfKind("Movie");
			efMM1.getFromAllOfKind("Movie").addToAttributes("title");
			program.put(con1, efMM1);
			
			EffectiveMetamodel efMM2 = new EffectiveMetamodel();
			Constraint con2 = module.getConstraints().get(1);
	//		con1.getNameExpression().setName("hasActor");
			efMM2 = new EffectiveMetamodel();
			efMM2.addToAllOfKind("Person");
			efMM2.getFromAllOfKind("Person").addToAttributes("name");
			program.put(con2, efMM2);
			
			part1.add(con1);
			part2.add(con2);
			expectedPartitions.add(part1);
			expectedPartitions.add(part2);
			
			PartitioningHandler handler = new PartitioningHandler(program);
			actualPartitions.addAll(handler.getPartitions().keySet());
			
			assertEquals(expectedPartitions, actualPartitions);
	}
		public PartialEvlModule getModule(String evl) throws Exception {
			PartialEvlModule module = new PartialEvlModule();
			module.parse(evl);
			return module;
		}
}
