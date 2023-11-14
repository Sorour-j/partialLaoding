package org.eclipse.epsilon.neo4j.dataset;

import static org.neo4j.configuration.SettingImpl.newBuilder;
import static org.neo4j.configuration.SettingValueParsers.BOOL;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.connectors.HttpConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;

public class RunDB {
	private static Thread shutdownHook;
	static GraphDatabaseService graphDb ;
	public static void main(final String[] args) throws IOException {
	//	ManagementService hello = new ManagementService();
		final Path databaseDirectory = Paths.get("target/imdb-all");
		System.out.println("Starting database ...");
		
	    DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(databaseDirectory)
		.setConfig( BoltConnector.enabled, true )
	    .setConfig( HttpConnector.enabled, true )
	    .setConfig(GraphDatabaseSettings.allow_upgrade, true)
	    .setConfig(GraphDatabaseSettings.fail_on_missing_files,false)
	    .setConfig(newBuilder( "unsupported.dbms.tx_log.fail_on_corrupted_log_files", BOOL, false ).dynamic().build(), false)
	    .setConfig( BoltConnector.listen_address, new SocketAddress( "localhost", 7687 ) )
	    .build();
		
		
	/*	registerShutdownHook(managementService);
		//registerShutdownHook(managementService);
		graphDb = managementService.database("neo4j");

		List<String> labels = new ArrayList<String>();
		Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
		Object re ;
		try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
			
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
			}
			
//		session.close();
//		}
		System.out.println(labels.size());
//		driver.close();
		System.out.println("Shutdown");
		managementService.shutdown();
		}*/
	}
	protected static void registerShutdownHook(DatabaseManagementService managementService) {
		shutdownHook = new Thread() {
			@Override
			public void run() {
				//managementService.shutdownDatabase(DEFAULT_DATABASE_NAME);
				managementService.shutdown();
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}
	
}
