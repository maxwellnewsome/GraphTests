package cpen221.graphs;

import cpen221.graphs.graph.AdjacencyListGraph;
import cpen221.graphs.graph.AdjacencyMatrixGraph;
import cpen221.graphs.graph.Edge;
import cpen221.graphs.graph.Vertex;
import cpen221.graphs.graph.PathCostType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PathFindingTests:
 * Comprehensive tests for pathfinding and cost computations in AdjacencyListGraph.
 * Covers both SUM_EDGES and MAX_EDGE, disconnected graphs, and defensive cases.
 */
public class PathFindingTests {

    private AdjacencyListGraph<SimpleVertex, SimpleEdge> graph;
    private SimpleVertex a, b, c, d, e;
    private SimpleEdge eAB, eBC, eCD, eAD, eBE;

    // === Minimal helper classes ===
    private static class SimpleVertex extends Vertex {
        private final String label;
        public SimpleVertex(int id, String label) {
            super(id);
            this.label = label;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SimpleVertex other)) return false;
            return id() == other.id() && Objects.equals(label, other.label);
        }
        @Override
        public int hashCode() {
            return Objects.hash(id(), label);
        }
        @Override
        public String toString() {
            return label;
        }
    }

    private static class SimpleEdge extends Edge<SimpleVertex> {
        public SimpleEdge(SimpleVertex v1, SimpleVertex v2, double len) {
            super(v1, v2, len);
        }
    }

    // === Setup ===
    @BeforeEach
    public void setup() {
        graph = new AdjacencyListGraph<>();

        a = new SimpleVertex(1, "A");
        b = new SimpleVertex(2, "B");
        c = new SimpleVertex(3, "C");
        d = new SimpleVertex(4, "D");
        e = new SimpleVertex(5, "E");

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);
        graph.addVertex(e);

        eAB = new SimpleEdge(a, b, 1.0);
        eBC = new SimpleEdge(b, c, 2.0);
        eCD = new SimpleEdge(c, d, 3.0);
        eAD = new SimpleEdge(a, d, 6.0);
        eBE = new SimpleEdge(b, e, 4.0);

        graph.addEdge(eAB);
        graph.addEdge(eBC);
        graph.addEdge(eCD);
        graph.addEdge(eAD);
        graph.addEdge(eBE);
    }

    // === Tests ===

    @Test
    public void testShortestPathSumEdges() {
        List<SimpleVertex> path = graph.minimumCostPath(a, d, PathCostType.SUM_EDGES);
        double cost = graph.pathCost(path, PathCostType.SUM_EDGES);

        assertEquals(6.0, cost, 1e-9);
        // any path with total cost 6 is valid
        assertTrue(
            path.equals(Arrays.asList(a, d)) ||
                path.equals(Arrays.asList(a, b, c, d)),
            "Graph may return any minimum-cost path"
        );
    }


    @Test
    public void testShortestPathMaxEdge() {
        List<SimpleVertex> path = graph.minimumCostPath(a, d, PathCostType.MAX_EDGE);
        assertEquals(Arrays.asList(a, b, c, d), path);
        assertEquals(3.0, graph.pathCost(path, PathCostType.MAX_EDGE), 1e-9);
    }

    @Test
    public void testDirectVsIndirectPath() {
        // a–d direct is 6.0, indirect a–b–c–d is 6.0 but with smaller edges
        List<SimpleVertex> direct = Arrays.asList(a, d);
        List<SimpleVertex> indirect = Arrays.asList(a, b, c, d);

        assertTrue(graph.pathCost(indirect, PathCostType.MAX_EDGE)
            < graph.pathCost(direct, PathCostType.MAX_EDGE));
    }

    @Test
    public void testDisconnectedVertices() {
        SimpleVertex x = new SimpleVertex(6, "X");
        graph.addVertex(x);
        List<SimpleVertex> path = graph.minimumCostPath(a, x, PathCostType.SUM_EDGES);
        assertTrue(path.isEmpty());
    }

    @Test
    public void testSymmetryOfPathCost() {
        double ab = graph.edgeLength(a, b);
        double ba = graph.edgeLength(b, a);
        assertEquals(ab, ba);
    }

    @Test
    public void testPathCostForSingleVertex() {
        List<SimpleVertex> path = List.of(a);
        assertEquals(0.0, graph.pathCost(path, PathCostType.SUM_EDGES));
    }

    @Test
    public void testPathCostForInvalidPath() {
        List<SimpleVertex> path = Arrays.asList(a, c); // no direct edge
        assertEquals(Double.POSITIVE_INFINITY,
            graph.pathCost(path, PathCostType.SUM_EDGES));
    }

    @Test
    public void testNullInputsReturnEmpty() {
        assertEquals(List.of(), graph.minimumCostPath(null, a, PathCostType.SUM_EDGES));
        assertEquals(List.of(), graph.minimumCostPath(a, null, PathCostType.SUM_EDGES));
    }

    @Test
    public void testPathConsistencyAfterEdgeRemoval() {
        graph.removeEdge(eBC);
        List<SimpleVertex> path = graph.minimumCostPath(a, d, PathCostType.SUM_EDGES);
        assertEquals(Arrays.asList(a, d), path); // now only direct path remains
    }

    @Test
    public void testDiameterConsistency() {
        double diameter = graph.getDiameter(PathCostType.SUM_EDGES);
        assertTrue(diameter > 0);
        assertNotNull(graph.getCenter(PathCostType.SUM_EDGES));
    }

    @Test
    public void testPerformanceOnLargerGraph() {
        AdjacencyListGraph<SimpleVertex, SimpleEdge> large = new AdjacencyListGraph<>();
        List<SimpleVertex> verts = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            SimpleVertex v = new SimpleVertex(i, "V" + i);
            large.addVertex(v);
            verts.add(v);
        }
        for (int i = 0; i < 49; i++) {
            large.addEdge(new SimpleEdge(verts.get(i), verts.get(i + 1), 1.0));
        }

        List<SimpleVertex> path = large.minimumCostPath(verts.get(0), verts.get(49), PathCostType.SUM_EDGES);
        assertEquals(50, path.size());
        assertEquals(49.0, large.pathCost(path, PathCostType.SUM_EDGES), 1e-9);
    }
    // === Extra Branch Coverage Tests ===

    // Covers MAX_EDGE cost type
    @Test
    public void testPathCostMaxEdgeBranch() {
        List<SimpleVertex> path = List.of(a, b, c);
        double cost = graph.pathCost(path, PathCostType.MAX_EDGE);
        assertEquals(2.0, cost, 1e-9);
    }

    // Covers SUM_EDGES cost type
    @Test
    public void testPathCostSumEdgesBranch() {
        List<SimpleVertex> path = List.of(a, b, c, d);
        double cost = graph.pathCost(path, PathCostType.SUM_EDGES);
        assertEquals(6.0, cost, 1e-9);
    }

    // Covers unreachable vertex case (no path)
    @Test
    public void testNoPathToDisconnectedVertex() {
        SimpleVertex x = new SimpleVertex(5, "X");
        List<SimpleVertex> path = graph.minimumCostPath(a, x, PathCostType.SUM_EDGES);
        assertTrue(path.isEmpty(), "Expected no path to disconnected vertex");
    }

    // Covers self-path (start == end)
    @Test
    public void testSelfPathTrivial() {
        List<SimpleVertex> path = graph.minimumCostPath(a, a, PathCostType.SUM_EDGES);
        assertEquals(List.of(a), path);
    }

    // Covers null arguments safely
    @Test
    public void testNullArgsPath() {
        assertEquals(List.of(), graph.minimumCostPath(null, a, PathCostType.SUM_EDGES));
        assertEquals(List.of(), graph.minimumCostPath(a, null, PathCostType.SUM_EDGES));
    }

    // Covers tie-breaking branch when two equal-cost paths exist
    @Test
    public void testEqualCostPathsHandled() {
        SimpleEdge eAC = new SimpleEdge(a, c, 3.0); // alternate route same total
        Set<SimpleVertex> vertices = Set.of(a, b, c, d);
        Set<SimpleEdge> edges = Set.of(eAB, eBC, eCD, eAD, eAC);
        AdjacencyMatrixGraph<SimpleVertex, SimpleEdge>
            g2 = new AdjacencyMatrixGraph<>(vertices, edges);

        List<SimpleVertex> path = g2.minimumCostPath(a, d, PathCostType.SUM_EDGES);
        double cost = g2.pathCost(path, PathCostType.SUM_EDGES);

        assertEquals(6.0, cost, 1e-9);
        assertFalse(path.isEmpty());
    }

}
