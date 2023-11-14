package org.eclipse.epsilon.neo4j.dataset;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.batchinsert.BatchInserter;
import org.neo4j.batchinsert.BatchInserters;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.io.layout.DatabaseLayout;

public class Neo4jBatchInserter {

	public static void main(final String[] args) throws IOException {
	BatchInserter inserter = null;
	
	Path databaseDirectory = null;
	try
	{
		databaseDirectory = Paths.get("target/TestInsert/data/databases/neo4j");
	    inserter = BatchInserters.inserter(DatabaseLayout.ofFlat(databaseDirectory));
	    System.out.println("Batch: " + inserter);
	    Label personLabel = Label.label( "Person" );
	    
//	    inserter.createDeferredSchemaIndex( personLabel ).on( "name" ).create();

	    Map<String, Object> properties = new HashMap<>();
	    List<Label> lbls = new ArrayList<Label>();
	    properties.put( "name", "Mattias" );
	    lbls.add(Label.label( "Person" ));
	    lbls.add(Label.label( "He" ));
	    long mattiasNode = inserter.createNode( properties, lbls.toArray(new Label[0]));
	    
	    
	    properties.put( "name", "Chris" );
	    long chrisNode = inserter.createNode( properties, personLabel );
	    inserter.setNodeLabels(chrisNode, Label.label( "She" ));
	    RelationshipType knows = RelationshipType.withName( "KNOWS" );
	    inserter.createRelationship( mattiasNode, chrisNode, knows, null );
	    
	    System.out.println("Dir: " + inserter.getStoreDir());
	    System.out.println("Node: " + inserter.nodeExists(chrisNode));
	    System.out.println("Node: " + inserter.nodeExists(mattiasNode));
	}
	
	finally
	{
	    if ( inserter != null )
	    {
	        inserter.shutdown();
	    }
	}
	}
	
	
}
