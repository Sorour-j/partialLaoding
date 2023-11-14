

package org.eclipse.epsilon.emc.neo4j.example.standalone;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.io.fs.FileUtils;


public class Neo4jCode
{
    private static final Path DB_PATH = Paths.get( "target/neo4j-store-with-new-indexing" );

    public static void main( final String[] args ) throws IOException
    {
        System.out.println( "Starting database ..." );
        FileUtils.deleteDirectory( DB_PATH );

        // tag::startDb[]
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder( DB_PATH )
                .setConfig( BoltConnector.enabled, true )
                .setConfig( BoltConnector.listen_address, new SocketAddress( "localhost", 7687 ) )
                .build();
        // end::startDb[]

      //  managementService.shutdown();
    }
}