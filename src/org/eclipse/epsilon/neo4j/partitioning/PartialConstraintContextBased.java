package org.eclipse.epsilon.neo4j.partitioning;

import java.util.Collection;
import org.eclipse.epsilon.effectivemetamodel.XMIN;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.emc.neo4j.Neo4jModel;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.ExecutorFactory;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.evl.dom.Constraint;
import org.eclipse.epsilon.evl.dom.ConstraintContext;
import org.eclipse.epsilon.evl.execute.context.IEvlContext;

public class PartialConstraintContextBased extends ConstraintContext{

		@Override
		public void execute(Collection<Constraint> constraintsToCheck, IEvlContext context) throws EolRuntimeException {
			
			
			if (!isLazy(context)) {
			
				IModel model = context.getModelRepository().getModels().get(0);				
				InMemoryEmfModel emfModel = null;
				
				
					if (model instanceof Neo4jModel) {
						
						((Neo4jModel)model).load(((Neo4jModel)model).getEffectiveMteamodels().get(""));//super.getTypeName()
							emfModel = new InMemoryEmfModel(model.getName(),((Neo4jModel) model).getResource());
							emfModel.setName(model.getName());
							context.getModelRepository().addModel(emfModel);
							context.getModelRepository().removeModel(model);
					}
					
					if (!isLazy(context)) {
						ExecutorFactory executorFactory = context.getExecutorFactory();
						for (Object modelElement : getAllOfSourceKind(context)) {
							if (appliesTo(modelElement, context)) {
								for (Constraint constraint : constraintsToCheck) {
									executorFactory.execute(constraint, context, modelElement);
								}
							}
						}
					}
					context.getModelRepository().removeModel(emfModel);
					context.getModelRepository().addModel(model);
					System.gc();
	
			}
		}
}
