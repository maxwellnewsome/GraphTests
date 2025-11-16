package cpen221.graphs;

import cpen221.graphs.applications.sealevels.GridLocation;
import cpen221.graphs.applications.sealevels.SeaLevels;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SeaLevelsTests {

    /** Example terrain with a center valley */
    private double[][] terrain = {
        {5, 4, 5},
        {4, 2, 4},
        {5, 4, 5}
    };

    @Test
    public void testBasicFlooding() {
        GridLocation[] sources = { new GridLocation(1, 1) };
        boolean[][] flooded = SeaLevels.isSubmerged(terrain, sources, 4.0);
        assertTrue(flooded[1][1]);  // center floods
        assertTrue(flooded[1][0]);  // adjacent cell floods
        assertFalse(flooded[0][0]); // corner stays dry
    }

    @Test
    public void testAllFloodedAtHighLevel() {
        GridLocation[] sources = { new GridLocation(0, 0) };
        boolean[][] flooded = SeaLevels.isSubmerged(terrain, sources, 6.0);
        for (boolean[] row : flooded)
            for (boolean cell : row)
                assertTrue(cell);
    }

    @Test
    public void testMultipleWaterSources() {
        double[][] map = {
            {2, 2, 2},
            {2, 5, 2},
            {2, 2, 2}
        };
        GridLocation[] src = {
            new GridLocation(0, 0),
            new GridLocation(2, 2)
        };
        boolean[][] flooded = SeaLevels.isSubmerged(map, src, 3.0);
        assertTrue(flooded[0][1]);
        assertFalse(flooded[1][1]);
    }

    @Test
    public void testDangerLevelSourceEqualsTerrain() {
        GridLocation[] src = { new GridLocation(1, 1) };
        double[][] danger = SeaLevels.dangerLevel(terrain, src);
        assertEquals(2.0, danger[1][1], 1e-9);
    }

    @Test
    public void testDangerLevelIncreasesWithHeight() {
        GridLocation[] src = { new GridLocation(1, 1) };
        double[][] danger = SeaLevels.dangerLevel(terrain, src);
        assertTrue(danger[0][0] > danger[1][1]);
        assertTrue(danger[2][2] > danger[1][1]);
    }

    @Test
    public void testEmptyGridThrows() {
        assertThrows(Exception.class, () ->
            SeaLevels.isSubmerged(new double[0][0], new GridLocation[]{}, 1.0));
    }

    @Test
    public void testSingleCellFlooded() {
        double[][] grid = { {1.0} };
        GridLocation[] src = { new GridLocation(0, 0) };
        boolean[][] flooded = SeaLevels.isSubmerged(grid, src, 2.0);
        assertTrue(flooded[0][0]);
    }
}
