package cpen221.graphs.graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for {@link Algorithms#partitionGraph}.
 *
 * Combines and improves both:
 *  - Adam’s "graph-theoretic" correctness tests
 *  - Robust "defensive behavior" tests from AlgorithmsTests
 *
 * Coverage goals:
 *  ✓ Defensive error handling (null / invalid k)
 *  ✓ k = 1, k = |V|, k > |V| edge cases
 *  ✓ Empty graph handling
 *  ✓ Graphs with no edges
 *  ✓ Shortest-edge (Kruskal-style) merging logic
 *  ✓ Correct edge containment per partition
 *  ✓ Overload variant with (graph, k, true)
 */
public class AlgorithmsTests {

    private AdjacencyListGraph<Vertex, Edge<Vertex>> listGraph;
    private AdjacencyMatrixGraph<SimpleVertex, SimpleEdge> matrixGraph;

    private Vertex vA, vB, vC, vD;
    private Edge<Vertex> eAB, eBC, eCD;
    private SimpleVertex a, b, c, d;
    private SimpleEdge e1, e2, e3;

    // --- helper SimpleVertex/SimpleEdge for matrix version ---
    private static class SimpleVertex extends Vertex {
        private final String label;
        public SimpleVertex(int id, String label) {
            super(id);
            this.label = label;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SimpleVertex other)) return false;
            return this.id() == other.id() && Objects.equals(label, other.label);
        }
        @Override
        public int hashCode() { return Objects.hash(id(), label); }
        @Override
        public String toString() { return label; }
    }

    private static class SimpleEdge extends Edge<SimpleVertex> {
        public SimpleEdge(SimpleVertex v1, SimpleVertex v2, double len) {
            super(v1, v2, len);
        }
    }

    @BeforeEach
    public void setup() {
        // --- list-based graph setup (Adam’s version) ---
        listGraph = new AdjacencyListGraph<>();
        vA = new Vertex(1);
        vB = new Vertex(2);
        vC = new Vertex(3);
        vD = new Vertex(4);
        eAB = new Edge<>(vA, vB, 5);
        eBC = new Edge<>(vB, vC, 3);
        eCD = new Edge<>(vC, vD, 1);

        listGraph.addVertex(vA);
        listGraph.addVertex(vB);
        listGraph.addVertex(vC);
        listGraph.addVertex(vD);
        listGraph.addEdge(eAB);
        listGraph.addEdge(eBC);
        listGraph.addEdge(eCD);

        // --- matrix-based graph setup (your version) ---
        a = new SimpleVertex(1, "A");
        b = new SimpleVertex(2, "B");
        c = new SimpleVertex(3, "C");
        d = new SimpleVertex(4, "D");
        e1 = new SimpleEdge(a, b, 1.0);
        e2 = new SimpleEdge(b, c, 2.0);
        e3 = new SimpleEdge(c, d, 3.0);
        Set<SimpleVertex> verts = new HashSet<>(Arrays.asList(a, b, c, d));
        Set<SimpleEdge> edges = new HashSet<>(Arrays.asList(e1, e2, e3));
        matrixGraph = new AdjacencyMatrixGraph<>(verts, edges);
    }

    // ------------------------------------------------------------------------
    // Defensive tests (from both versions)
    // ------------------------------------------------------------------------
    @Test
    public void testNullGraphThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> Algorithms.partitionGraph(null, 2));
    }

    @Test
    public void testInvalidPartitionCount() {
        // k = 0
        assertThrows(IllegalArgumentException.class,
            () -> Algorithms.partitionGraph(listGraph, 0));
        // negative
        assertThrows(IllegalArgumentException.class,
            () -> Algorithms.partitionGraph(matrixGraph, -1));
    }

    @Test
    public void testEmptyGraphHandledGracefully() {
        Set<SimpleVertex> emptyVerts = new HashSet<>();
        Set<SimpleEdge> emptyEdges = new HashSet<>();
        AdjacencyMatrixGraph<SimpleVertex, SimpleEdge> emptyGraph =
            new AdjacencyMatrixGraph<>(emptyVerts, emptyEdges);

        List<Graph<SimpleVertex, SimpleEdge>> parts = Algorithms.partitionGraph(emptyGraph, 3);
        assertEquals(3, parts.size());
        for (Graph<SimpleVertex, SimpleEdge> g : parts) {
            assertTrue(g.getVertices().isEmpty());
            assertTrue(g.getEdges().isEmpty());
        }
    }

    // ------------------------------------------------------------------------
    // Partition behavior for valid graphs
    // ------------------------------------------------------------------------
    @Test
    public void testFullPartitionCase() {
        List<Graph<Vertex, Edge<Vertex>>> parts = Algorithms.partitionGraph(listGraph, 4);
        assertEquals(4, parts.size());
        Set<Set<Vertex>> expected = Set.of(
            Set.of(vA), Set.of(vB), Set.of(vC), Set.of(vD)
        );
        assertEquals(expected, getVertexSets(parts));
    }

    @Test
    public void testSinglePartitionMergesAll() {
        List<Graph<Vertex, Edge<Vertex>>> parts = Algorithms.partitionGraph(listGraph, 1);
        assertEquals(1, parts.size());
        assertEquals(Set.of(Set.of(vA, vB, vC, vD)), getVertexSets(parts));
        assertEquals(3, parts.get(0).getEdges().size());
    }

    @Test
    public void testShortestEdgeCondition() {
        List<Graph<Vertex, Edge<Vertex>>> parts = Algorithms.partitionGraph(listGraph, 3);
        assertEquals(3, parts.size());
        Set<Set<Vertex>> expected = Set.of(
            Set.of(vA), Set.of(vB), Set.of(vC, vD)
        );
        assertEquals(expected, getVertexSets(parts));
    }

    @Test
    public void testNearestNeighbourCondition() {
        List<Graph<Vertex, Edge<Vertex>>> parts = Algorithms.partitionGraph(listGraph, 2);
        assertEquals(2, parts.size());
        Set<Set<Vertex>> expected = Set.of(
            Set.of(vA), Set.of(vB, vC, vD)
        );
        assertEquals(expected, getVertexSets(parts));
        for (Graph<Vertex, Edge<Vertex>> g : parts) {
            if (g.getVertices().size() == 3) {
                assertEquals(2, g.getEdges().size());
                assertTrue(g.hasEdge(vB, vC));
                assertTrue(g.hasEdge(vC, vD));
                assertFalse(g.hasEdge(vA, vB));
            }
        }
    }

    @Test
    public void testGraphWithNoEdges() {
        AdjacencyListGraph<Vertex, Edge<Vertex>> g = new AdjacencyListGraph<>();
        g.addVertex(vA);
        g.addVertex(vB);
        List<Graph<Vertex, Edge<Vertex>>> p2 = Algorithms.partitionGraph(g, 2);
        assertEquals(2, p2.size());
        assertEquals(Set.of(Set.of(vA), Set.of(vB)), getVertexSets(p2));

        List<Graph<Vertex, Edge<Vertex>>> p1 = Algorithms.partitionGraph(g, 1);
        assertEquals(2, p1.size()); // no edges -> cannot merge
    }

    @Test
    public void testMorePartitionsThanVertices() {
        List<Graph<SimpleVertex, SimpleEdge>> parts = Algorithms.partitionGraph(matrixGraph, 10);
        assertEquals(10, parts.size());
        long nonEmpty = parts.stream().filter(p -> !p.getVertices().isEmpty()).count();
        assertTrue(nonEmpty <= 4);
    }

    // ------------------------------------------------------------------------
    // Overload variant and utility checks
    // ------------------------------------------------------------------------
    @Test
    public void testSinglePartitionOverload() {
        Graph<SimpleVertex, SimpleEdge> g = Algorithms.partitionGraph(matrixGraph, 2, true);
        assertNotNull(g);
        assertTrue(g.getVertices().size() > 0);
    }

    @Test
    public void testEdgeContainmentWithinPartitions() {
        List<Graph<SimpleVertex, SimpleEdge>> parts = Algorithms.partitionGraph(matrixGraph, 2);
        for (Graph<SimpleVertex, SimpleEdge> part : parts) {
            for (SimpleEdge e : part.getEdges()) {
                assertTrue(part.hasVertex(e.v1()) && part.hasVertex(e.v2()),
                    "Edge should connect vertices in same partition");
            }
        }
    }

    // ------------------------------------------------------------------------
    // Utility helper
    // ------------------------------------------------------------------------
    private Set<Set<Vertex>> getVertexSets(List<Graph<Vertex, Edge<Vertex>>> parts) {
        Set<Set<Vertex>> sets = new HashSet<>();
        for (Graph<Vertex, Edge<Vertex>> g : parts)
            sets.add(new HashSet<>(g.getVertices()));
        return sets;
    }
}
