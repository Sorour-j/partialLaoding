package org.eclipse.epsilon.neo4j.partitioning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.common.parse.AST;
import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodel;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.emc.neo4j.Neo4jModel;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.ExecutorFactory;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.ModelRepository;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.dom.Constraint;
import org.eclipse.epsilon.evl.dom.ConstraintContext;
import org.eclipse.epsilon.evl.execute.context.IEvlContext;

public class PartialEvlModule extends EvlModule {

	private boolean isPartitioned = false;

	public PartialEvlModule() {
		super();
	}

	@Override
	public ModuleElement adapt(AST cst, ModuleElement parentAst) {
		ModuleElement me = super.adapt(cst, parentAst);

		if (me instanceof ConstraintContext)
			return new PartialConstraintContext();
		else
			return me;
	}

	public boolean isPartitioned() {
		return isPartitioned;
	}

	public void setIsPartitioned(boolean isPartitioned) {
		this.isPartitioned = isPartitioned;
	}

//	protected void checkConstraints() throws EolRuntimeException {
//		IEvlContext context = getContext();
//		IModel model = context.getModelRepository().getModels().get(0);
//		InMemoryEmfModel emfModel = null;
//		Neo4jModel neo4jModel = (Neo4jModel) model;
//		HashMap<Set<Constraint>, EffectiveMetamodel> partitionSets = neo4jModel.getPartitioningHandler()
//				.getPartitions();
//		// Set<Constraint> executedConstraints = new HashSet<Constraint>();
//		ExecutorFactory executorFactory = context.getExecutorFactory();
//
//		for (Set<Constraint> sets : partitionSets.keySet()) {
//			
//			
//				//EffectiveMetamodel efModel = neo4jModel.getPartitioningHandler().getEffectivemetamodel(constraint);
//				EffectiveMetamodel efModel = partitionSets.get(sets);
//				neo4jModel.load(efModel);
//
//				emfModel = new InMemoryEmfModel(model.getName(), ((Neo4jModel) model).getResource());
//				emfModel.setName(model.getName());
//
//				context.setModelRepository(new ModelRepository());
//				context.getModelRepository().addModel(emfModel);
//
//				//for (Constraint c : neo4jModel.getPartitioningHandler().getConstraints(efModel)) {
//				for (Constraint constraint : sets) {
//					ConstraintContext conContext = constraint.getConstraintContext();
//					for (Object modelElement : conContext.getAllOfSourceKind(context)) {
//						if (conContext.appliesTo(modelElement, context)) {
//							executorFactory.execute(constraint, context, modelElement);
//						}
//					}
//					// executedConstraints.add(c);
//				}
//				// if there is no constraints to execute, this stage can be skipped. How?!
//				((Neo4jModel) model).getResource().unload();
//				context.getModelRepository().removeModel(emfModel);
//				context.getModelRepository().addModel(model);
//				System.gc();
//			}
//	}
}
