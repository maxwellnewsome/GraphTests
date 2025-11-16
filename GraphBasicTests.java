package cpen221.graphs;

import cpen221.graphs.graph.*;   // to access Graph, Vertex, Edge, etc.
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Basic correctness tests for AdjacencyListGraph.
 * These tests cover vertex and edge operations, path cost calculations,
 * connected components, and diameter/center logic.
 */
public class GraphBasicTests {

    /** Simple concrete Vertex subclass for testing */
    private static class SimpleVertex extends Vertex {
        private final String name;

        public SimpleVertex(String name) {
            super(0); // 👈 or any dummy ID you want
            this.name = name;
        }

        @Override public String toString() { return name; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof SimpleVertex)) return false;
            return name.equals(((SimpleVertex) o).name);
        }
        @Override public int hashCode() { return Objects.hash(name); }
    }


    /** Simple concrete Edge subclass for testing */
    private static class SimpleEdge extends Edge<SimpleVertex> {
        public SimpleEdge(SimpleVertex v1, SimpleVertex v2, double len) { super(v1, v2, len); }
    }

    private AdjacencyListGraph<SimpleVertex, SimpleEdge> makeTriangle() {
        AdjacencyListGraph<SimpleVertex, SimpleEdge> g = new AdjacencyListGraph<>();
        SimpleVertex a = new SimpleVertex("A");
        SimpleVertex b = new SimpleVertex("B");
        SimpleVertex c = new SimpleVertex("C");
        g.addVertex(a);
        g.addVertex(b);
        g.addVertex(c);
        g.addEdge(new SimpleEdge(a, b, 2));
        g.addEdge(new SimpleEdge(b, c, 3));
        g.addEdge(new SimpleEdge(a, c, 4));
        return g;
    }

    @Test
    public void testAddAndHasVertex() {
        var g = new AdjacencyListGraph<SimpleVertex, SimpleEdge>();
        var a = new SimpleVertex("A");
        assertTrue(g.addVertex(a));
        assertTrue(g.hasVertex(a));
        assertFalse(g.addVertex(a)); // duplicate should fail
        assertFalse(g.hasVertex(new SimpleVertex("B"))); // unknown vertex
    }

    @Test
    public void testAddAndHasEdge() {
        var g = new AdjacencyListGraph<SimpleVertex, SimpleEdge>();
        var a = new SimpleVertex("A");
        var b = new SimpleVertex("B");
        g.addVertex(a); g.addVertex(b);
        var e = new SimpleEdge(a, b, 1.5);
        assertTrue(g.addEdge(e));
        assertTrue(g.hasEdge(a, b));
        assertFalse(g.addEdge(e)); // duplicate edge blocked
        assertEquals(1.5, g.edgeLength(a, b), 1e-6);
    }

    @Test
    public void testEdgeLengthSumAndRemoval() {
        var g = makeTriangle();
        double sum = g.edgeLengthSum();
        assertEquals(9.0, sum, 1e-6); // 2 + 3 + 4
        var vertices = new ArrayList<>(g.getVertices());
        g.removeEdge(vertices.get(0), vertices.get(1));
        assertFalse(g.hasEdge(vertices.get(0), vertices.get(1)));
        assertTrue(g.removeVertex(vertices.get(2)));
        assertFalse(g.hasVertex(vertices.get(2)));
    }

    @Test
    public void testMinimumCostPathSumEdges() {
        var g = makeTriangle();
        var vertices = new ArrayList<>(g.getVertices());
        var a = vertices.get(0);
        var b = vertices.get(1);
        var c = vertices.get(2);
        List<SimpleVertex> path = g.minimumCostPath(a, c, PathCostType.SUM_EDGES);
        // Direct edge a–c length 4 vs a–b–c length 5 → choose direct path
        assertEquals(List.of(a, c), path);
        assertEquals(4.0, g.pathCost(path, PathCostType.SUM_EDGES), 1e-6);
    }

    @Test
    public void testMinimumCostPathMaxEdge() {
        var g = makeTriangle();
        var vertices = new ArrayList<>(g.getVertices());
        var a = vertices.get(0);
        var b = vertices.get(1);
        var c = vertices.get(2);
        List<SimpleVertex> path = g.minimumCostPath(a, c, PathCostType.MAX_EDGE);
        // For MAX_EDGE metric, a–b–c path uses edges 2 and 3 → max 3 < 4
        assertEquals(List.of(a, b, c), path);
        assertEquals(3.0, g.pathCost(path, PathCostType.MAX_EDGE), 1e-6);
    }

    @Test
    public void testDiameterAndCenter() {
        var g = makeTriangle();
        double dia = g.getDiameter(PathCostType.SUM_EDGES);
        assertTrue(dia > 0);
        SimpleVertex center = g.getCenter(PathCostType.SUM_EDGES);
        assertNotNull(center);
        assertTrue(g.getVertices().contains(center));
    }

    @Test
    public void testComponentsDisconnected() {
        var g = new AdjacencyListGraph<SimpleVertex, SimpleEdge>();
        var a = new SimpleVertex("A");
        var b = new SimpleVertex("B");
        var c = new SimpleVertex("C");
        g.addVertex(a);
        g.addVertex(b);
        g.addVertex(c);
        g.addEdge(new SimpleEdge(a, b, 1));
        Set<Set<SimpleVertex>> comps = g.getComponents();
        // Expect two components: {A,B} and {C}
        assertEquals(2, comps.size());
        boolean foundIsolated = comps.stream().anyMatch(s -> s.size() == 1 && s.contains(c));
        assertTrue(foundIsolated);
    }

    @Test
    public void testEdgeAndVertexCollections() {
        var g = makeTriangle();
        var vertices = g.getVertices();
        var edges = g.getEdges();
        assertEquals(3, vertices.size());
        assertEquals(3, edges.size());
        var first = vertices.iterator().next();
        assertTrue(g.getEdges(first).size() >= 1);
        assertTrue(g.getNeighbours(first).size() >= 1);
    }

    @Test
    public void testInvalidEdgeQueriesReturnDefaults() {
        var g = makeTriangle();
        var x = new SimpleVertex("X");
        var y = new SimpleVertex("Y");
        assertFalse(g.hasEdge(x, y));
        assertEquals(Double.POSITIVE_INFINITY, g.edgeLength(x, y));
        assertNull(g.getEdge(x, y));
        assertTrue(g.getEdges(x).isEmpty());
        assertTrue(g.getNeighbours(x).isEmpty());
    }

}

