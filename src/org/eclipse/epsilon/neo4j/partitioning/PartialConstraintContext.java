package org.eclipse.epsilon.neo4j.partitioning;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodel;
import org.eclipse.epsilon.effectivemetamodel.XMIN;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.emc.neo4j.Neo4jModel;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.ExecutorFactory;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.ModelRepository;
import org.eclipse.epsilon.evl.dom.Constraint;
import org.eclipse.epsilon.evl.dom.ConstraintContext;
import org.eclipse.epsilon.evl.execute.context.IEvlContext;

public class PartialConstraintContext extends ConstraintContext {

	@Override
	public void execute(Collection<Constraint> constraintsToCheck, IEvlContext context) throws EolRuntimeException {

		if (!isLazy(context)) {

			IModel model = context.getModelRepository().getModels().get(0);
			InMemoryEmfModel emfModel = null;

			ExecutorFactory executorFactory = context.getExecutorFactory();
			if (((PartialEvlModule) (context.getModule())).isPartitioned())
				executeByPartition(constraintsToCheck, context);
			else {
				for (Constraint constraint : constraintsToCheck) {

					if (model instanceof XMIN)
						((XMIN) model).load(((XMIN) model).getEffectiveMteamodels().get(constraint.getName()));

					if (model instanceof Neo4jModel) {

						// Load model based on the effective meta-model which is mapped to this
						// constraint
						((Neo4jModel) model).load(((Neo4jModel) model).getEffectiveMteamodel(constraint));

						// create an EMFModel for Neo4jModel >> use EMFModel execution engine
						emfModel = new InMemoryEmfModel(model.getName(), ((Neo4jModel) model).getResource());
						emfModel.setName(model.getName());

						context.setModelRepository(new ModelRepository());
						context.getModelRepository().addModel(emfModel);
					}

					for (Object modelElement : getAllOfSourceKind(context)) {
						if (appliesTo(modelElement, context)) {
							executorFactory.execute(constraint, context, modelElement);
						}
					}
					((Neo4jModel) model).getResource().unload();
					context.getModelRepository().removeModel(emfModel);
					context.getModelRepository().addModel(model);
					System.gc();
				}
			}
		}
	}

	public void executeByPartition(Collection<Constraint> constraintsToCheck, IEvlContext context)
			throws EolRuntimeException {

		IModel model = context.getModelRepository().getModels().get(0);
		InMemoryEmfModel emfModel = null;
		Neo4jModel neo4jModel = (Neo4jModel) model;
		Set<Constraint> executedConstraints = new HashSet<Constraint>();

		ExecutorFactory executorFactory = context.getExecutorFactory();
		
		for (Constraint constraint : constraintsToCheck) {
			if (!executedConstraints.contains(constraint)) {
				EffectiveMetamodel efModel = neo4jModel.getPartitioningHandler().getEffectivemetamodel(constraint);
				neo4jModel.load(efModel);
				
				emfModel = new InMemoryEmfModel(model.getName(), ((Neo4jModel) model).getResource());
				emfModel.setName(model.getName());

				context.setModelRepository(new ModelRepository());
				context.getModelRepository().addModel(emfModel);

				for (Constraint c : neo4jModel.getPartitioningHandler().getConstraints(efModel)) {
					for (Object modelElement : getAllOfSourceKind(context)) {
						System.out.println(c.getName());
						if (appliesTo(modelElement, context)) {
							executorFactory.execute(c, context, modelElement);
						}
					}
					executedConstraints.add(c);
				}
				// if there is no constraints to execute, this stage can be skipped. How?!
				((Neo4jModel) model).getResource().unload();
				context.getModelRepository().removeModel(emfModel);
				context.getModelRepository().addModel(model);
				System.gc();
			}
		}
	}
}
