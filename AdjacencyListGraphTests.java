package cpen221.graphs;

import cpen221.graphs.graph.AdjacencyListGraph;
import cpen221.graphs.graph.Edge;
import cpen221.graphs.graph.PathCostType;
import cpen221.graphs.graph.Vertex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for AdjacencyListGraph.
 *
 * <p>This suite combines functional, structural, and defensive tests
 * to ensure complete coverage across all tasks (1–5). It adheres to
 * the Google Java Style Guide and Oracle conventions.</p>
 *
 * <ul>
 *   <li>Vertex and edge management (add, remove, has)</li>
 *   <li>Edge accessors and symmetry</li>
 *   <li>Graph metrics (path cost, min path, diameter, center)</li>
 *   <li>Component detection and edge sum</li>
 *   <li>Defensive handling for nulls, invalid vertices, and self-loops</li>
 * </ul>
 */
public class AdjacencyListGraphTests {

    private AdjacencyListGraph<Vertex, Edge<Vertex>> graph;
    private Vertex v1, v2, v3, v4, v5, v6;
    private Edge<Vertex> e12, e23, e34, e14, e15;

    /**
     * Initializes a connected graph for testing:
     * v1—v2—v3—v4 and v1—v4, v1—v5
     */
    @BeforeEach
    public void setup() {
        graph = new AdjacencyListGraph<>();
        v1 = new Vertex(1);
        v2 = new Vertex(2);
        v3 = new Vertex(3);
        v4 = new Vertex(4);
        v5 = new Vertex(5);
        v6 = new Vertex(6);

        for (Vertex v : List.of(v1, v2, v3, v4, v5)) {
            graph.addVertex(v);
        }

        e12 = new Edge<>(v1, v2, 5);
        e23 = new Edge<>(v2, v3, 7);
        e34 = new Edge<>(v3, v4, 8);
        e14 = new Edge<>(v1, v4, 9);
        e15 = new Edge<>(v1, v5, 2);

        for (Edge<Vertex> e : List.of(e12, e23, e34, e14, e15)) {
            graph.addEdge(e);
        }
    }

    // ============================================================
    // Task 1: Vertex and Edge Addition, Access, and Validation
    // ============================================================

    @Test
    public void testAddVertexValidAndDuplicate() {
        Vertex vNew = new Vertex(10);
        assertTrue(graph.addVertex(vNew));
        assertFalse(graph.addVertex(vNew));
    }

    @Test
    public void testAddVertexNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> graph.addVertex(null));
    }

    @Test
    public void testAddEdgeValidAndDuplicate() {
        Edge<Vertex> eNew = new Edge<>(v2, v4, 10);
        assertTrue(graph.addEdge(eNew));
        assertFalse(graph.addEdge(eNew)); // duplicate
    }

    @Test
    public void testAddEdgeWithUnknownVertexThrows() {
        Edge<Vertex> invalidEdge = new Edge<>(v1, v6, 3);
        assertThrows(IllegalStateException.class, () -> graph.addEdge(invalidEdge));
    }

    @Test
    public void testAddEdgeSelfLoopThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Edge<>(v1, v1, 1));
    }

    @Test
    public void testHasVertex() {
        assertTrue(graph.hasVertex(v1));
        assertFalse(graph.hasVertex(new Vertex(99)));
    }

    @Test
    public void testHasEdgeByVerticesAndSymmetry() {
        assertTrue(graph.hasEdge(v1, v2));
        assertTrue(graph.hasEdge(v2, v1)); // undirected symmetry
        assertFalse(graph.hasEdge(v1, v6));
    }

    @Test
    public void testHasEdgeByObject() {
        assertTrue(graph.hasEdge(e12));
        Edge<Vertex> fake = new Edge<>(v1, v3, 4);
        assertFalse(graph.hasEdge(fake));
    }

    @Test
    public void testGetEdgeAndInvalidCases() {
        assertEquals(e12, graph.getEdge(v1, v2));
        assertThrows(IllegalStateException.class, () -> graph.getEdge(v1, v6));
        assertThrows(IllegalArgumentException.class, () -> graph.getEdge(v1, null));
    }

    @Test
    public void testEdgeLengthSymmetry() {
        assertEquals(5, graph.edgeLength(v1, v2));
        assertEquals(5, graph.edgeLength(v2, v1));
    }

    // ============================================================
    // Task 2: Removal and Accessors
    // ============================================================

    @Test
    public void testRemoveEdgeByObjectAndVertices() {
        assertTrue(graph.removeEdge(e12));
        assertFalse(graph.hasEdge(v1, v2));
        assertTrue(graph.addEdge(e12)); // re-add valid edge
        assertTrue(graph.removeEdge(v1, v2));
        assertFalse(graph.hasEdge(v1, v2));
    }

    @Test
    public void testRemoveEdgeInvalidInputs() {
        assertFalse(graph.removeEdge(v1, v6));
        assertFalse(graph.removeEdge(null, v2));
        assertFalse(graph.removeEdge((Edge<Vertex>) null));
    }

    @Test
    public void testRemoveVertexCascadesEdges() {
        assertTrue(graph.removeVertex(v1));
        assertFalse(graph.hasEdge(v1, v2));
        assertFalse(graph.hasEdge(v4, v1));
        assertFalse(graph.hasVertex(v1));
    }

    @Test
    public void testRemoveVertexNonExistent() {
        assertFalse(graph.removeVertex(v6));
    }

    @Test
    public void testGetVerticesAndEdgesUnmodifiable() {
        Set<Vertex> vertices = graph.getVertices();
        Set<Edge<Vertex>> edges = graph.getEdges();
        assertThrows(UnsupportedOperationException.class,
            () -> vertices.add(new Vertex(9)));
        assertThrows(UnsupportedOperationException.class, edges::clear);
    }

    @Test
    public void testGetEdgesOfVertexValidAndEmpty() {
        Set<Edge<Vertex>> edges = graph.getEdges(v5);
        assertEquals(1, edges.size());
        graph.removeEdge(e15);
        assertTrue(graph.getEdges(v5).isEmpty());
    }

    @Test
    public void testGetEdgesNullOrInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> graph.getEdges(null));
        assertThrows(IllegalStateException.class, () -> graph.getEdges(v6));
    }

    @Test
    public void testGetNeighboursValidAndInvalid() {
        Map<Vertex, Edge<Vertex>> neighbours = graph.getNeighbours(v2);
        assertTrue(neighbours.containsKey(v1));
        assertTrue(neighbours.containsKey(v3));

        assertThrows(IllegalArgumentException.class, () -> graph.getNeighbours(null));
        assertThrows(IllegalStateException.class, () -> graph.getNeighbours(v6));
    }

    @Test
    public void testEdgeLengthSumComprehensive() {
        assertEquals(31, graph.edgeLengthSum());
        graph.removeEdge(e14);
        assertEquals(22, graph.edgeLengthSum());
        AdjacencyListGraph<Vertex, Edge<Vertex>> empty = new AdjacencyListGraph<>();
        assertEquals(0.0, empty.edgeLengthSum());
    }

    // ============================================================
    // Task 3: Path Cost and Minimum Cost Path
    // ============================================================

    @Test
    public void testPathCostSumAndMaxEdge() {
        List<Vertex> path = List.of(v1, v2, v3, v4);
        assertEquals(20, graph.pathCost(path, PathCostType.SUM_EDGES));
        assertEquals(8, graph.pathCost(path, PathCostType.MAX_EDGE));
    }

    @Test
    public void testPathCostTrivialOrInvalid() {
        assertEquals(0.0, graph.pathCost(null, PathCostType.SUM_EDGES));
        assertEquals(0.0, graph.pathCost(List.of(), PathCostType.SUM_EDGES));
        assertThrows(IllegalStateException.class,
            () -> graph.pathCost(List.of(v1, v6), PathCostType.SUM_EDGES));
    }

    @Test
    public void testMinimumCostPathSumEdges() {
        List<Vertex> path = graph.minimumCostPath(v3, v5, PathCostType.SUM_EDGES);
        assertEquals(List.of(v3, v2, v1, v5), path);
        assertEquals(14, graph.pathCost(path, PathCostType.SUM_EDGES));
    }

    @Test
    public void testMinimumCostPathMaxEdge() {
        List<Vertex> path = graph.minimumCostPath(v3, v5, PathCostType.MAX_EDGE);
        assertEquals(List.of(v3, v2, v1, v5), path);
        assertEquals(7, graph.pathCost(path, PathCostType.MAX_EDGE));
    }

    @Test
    public void testMinimumCostPathInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
            () -> graph.minimumCostPath(v1, null, PathCostType.SUM_EDGES));
        assertThrows(IllegalStateException.class,
            () -> graph.minimumCostPath(v1, v6, PathCostType.SUM_EDGES));
    }

    @Test
    public void testMinimumCostPathUnreachable() {
        AdjacencyListGraph<Vertex, Edge<Vertex>> g = new AdjacencyListGraph<>();
        Vertex a = new Vertex(1);
        Vertex b = new Vertex(2);
        g.addVertex(a);
        g.addVertex(b);
        assertTrue(g.minimumCostPath(a, b, PathCostType.SUM_EDGES).isEmpty());
    }

    // ============================================================
    // Task 4: Diameter and Center
    // ============================================================

    @Test
    public void testGetDiameterSumEdges() {
        assertEquals(20, graph.getDiameter(PathCostType.SUM_EDGES));
    }

    @Test
    public void testGetDiameterMaxEdge() {
        assertEquals(9, graph.getDiameter(PathCostType.MAX_EDGE));
    }

    @Test
    public void testGetDiameterEmptyGraph() {
        AdjacencyListGraph<Vertex, Edge<Vertex>> empty = new AdjacencyListGraph<>();
        assertEquals(0.0, empty.getDiameter(PathCostType.SUM_EDGES));
    }

    @Test
    public void testGetCenterSumEdges() {
        Vertex center = graph.getCenter(PathCostType.SUM_EDGES);
        assertNotNull(center);
        assertTrue(Set.of(v2, v3, v1).contains(center));
    }

    @Test
    public void testGetCenterEmptyGraphReturnsNull() {
        AdjacencyListGraph<Vertex, Edge<Vertex>> empty = new AdjacencyListGraph<>();
        assertNull(empty.getCenter(PathCostType.SUM_EDGES));
    }

    // ============================================================
    // Task 5: Components
    // ============================================================

    @Test
    public void testGetComponentsConnectedAndIsolated() {
        Set<Set<Vertex>> comps = graph.getComponents();
        assertEquals(1, comps.size());
        graph.removeEdge(e15);
        graph.removeVertex(v5);
        Set<Set<Vertex>> comps2 = graph.getComponents();
        assertEquals(1, comps2.size());
    }

    @Test
    public void testGetComponentsDisconnectedGraph() {
        AdjacencyListGraph<Vertex, Edge<Vertex>> g2 = new AdjacencyListGraph<>();
        Vertex a = new Vertex(1);
        Vertex b = new Vertex(2);
        Vertex c = new Vertex(3);
        Vertex d = new Vertex(4);
        for (Vertex v : List.of(a, b, c, d)) {
            g2.addVertex(v);
        }
        g2.addEdge(new Edge<>(a, b, 1));
        g2.addEdge(new Edge<>(c, d, 1));

        Set<Set<Vertex>> comps = g2.getComponents();
        assertEquals(2, comps.size());
        assertTrue(comps.stream().anyMatch(s -> s.containsAll(List.of(a, b))));
        assertTrue(comps.stream().anyMatch(s -> s.containsAll(List.of(c, d))));
    }

    @Test
    public void testGetComponentsEmptyGraph() {
        AdjacencyListGraph<Vertex, Edge<Vertex>> gEmpty = new AdjacencyListGraph<>();
        assertTrue(gEmpty.getComponents().isEmpty());
    }

    // ============================================================
    // Defensive Behavior and Edge Cases
    // ============================================================

    @Test
    public void testDefensiveBehaviorNullsAndUnknowns() {
        assertFalse(graph.hasEdge((Edge<Vertex>) null));
        assertFalse(graph.hasEdge((Vertex) null, null));
        assertThrows(IllegalArgumentException.class, () -> graph.getNeighbours(null));
        assertThrows(IllegalStateException.class, () -> graph.getNeighbours(v6));
    }

    @Test
    public void testSafeEmptyGraphAccessors() {
        AdjacencyListGraph<Vertex, Edge<Vertex>> empty = new AdjacencyListGraph<>();
        Vertex x = new Vertex(100);
        assertFalse(empty.hasVertex(x));
        assertFalse(empty.hasEdge(x, x));
        assertEquals(0.0, empty.edgeLengthSum());
        assertEquals(Set.of(), empty.getEdges());
        assertEquals(Set.of(), empty.getEdges(x));
        assertEquals(Map.of(), empty.getNeighbours(x));
    }
}
