package org.eclipse.epsilon.neo4j.partitioning;

import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodel;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.similarity.ZhangShashaTreeEditDistance;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;


public class GraphSimilarityTest {

	public static void main(String[] args) {

		Graph<Node, DefaultEdge> tree1 = new DefaultUndirectedWeightedGraph<Node, DefaultEdge>(DefaultEdge.class);// = new Graph<Node, DefaultEdge>(DefaultEdge.class);
		
        Node a = new Node("a");
        Node b = new Node("b");
        Node c = new Node("c");
        Node d = new Node("d");
        
        
        tree1.addVertex(a);
		tree1.addVertex(b);
		tree1.addVertex(c);
		tree1.addVertex(d);
		
		tree1.addEdge(a,b);
        tree1.setEdgeWeight(a, b, 5.0);
		tree1.addEdge(b,c);
		tree1.setEdgeWeight(b,c, 1);
		tree1.addEdge(b,d);
		tree1.setEdgeWeight(b,d, 1);
		
		Graph<Node, DefaultEdge> tree2 = new DefaultUndirectedWeightedGraph<Node, DefaultEdge>(DefaultEdge.class);
		Node a2 = new Node("a");
        Node b2 = new Node("b");
        Node e2 = new Node("e");
       
        tree2.addVertex(a2);
		tree2.addVertex(b2);
		tree2.addVertex(e2);

		tree2.addEdge(a2,b2);
		tree2.addEdge(b2,e2);

		ZhangShashaTreeEditDistance<Node, DefaultEdge> SimilarityCal = new ZhangShashaTreeEditDistance<Node, DefaultEdge>(tree1,a,tree2,a2);
		System.out.println(SimilarityCal.getDistance());
		
	}
}
