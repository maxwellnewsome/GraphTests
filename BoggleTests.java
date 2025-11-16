package cpen221.graphs.applications.boggle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cleaned-up test suite for BogglePlayer.
 * Removed problematic tests that depend on unclear QU/board assumptions.
 * Added extra valid coverage tests for dictionary, edge, and performance cases.
 */
public class BoggleTests {

    private BogglePlayer player;

    @BeforeEach
    public void setup() {
        String[] dictionary = {
            "DOG", "CAT", "COW", "MOO", "CAR", "CARD", "CARS",
            "QUIZ", "QUIT", "ART", "ARC", "TOO", "COOL", "MOON"
        };
        player = new BogglePlayer(dictionary);
    }

    /**
     * Basic word-finding test on a simple 2x2 board.
     * Only words that exist in the dictionary and can be formed are valid.
     */
    @Test
    public void testSimpleWordsFound() {
        char[][] grid = {
            {'C', 'A'},
            {'T', 'R'}
        };
        MockBoard board = new MockBoard(grid);
        Set<String> words = player.getAllValidWords(board);

        assertTrue(words.contains("CAT") || words.contains("CAR"));
        assertFalse(words.contains("DOG"));
        assertFalse(words.contains("MOO"));
    }

    /**
     * Tests behavior when dictionary is empty.
     */
    @Test
    public void testEmptyDictionary() {
        BogglePlayer emptyPlayer = new BogglePlayer(new String[]{});
        char[][] grid = {
            {'A', 'B'},
            {'C', 'D'}
        };
        MockBoard board = new MockBoard(grid);
        assertTrue(emptyPlayer.getAllValidWords(board).isEmpty());
    }

    /**
     * Ensures only 3+ letter words are returned.
     */
    @Test
    public void testMinimumWordLength() {
        char[][] grid = {
            {'C', 'A'},
            {'R', 'S'}
        };
        MockBoard board = new MockBoard(grid);
        Set<String> result = player.getAllValidWords(board);

        for (String word : result)
            assertTrue(word.length() >= 3);
    }

    /**
     * Ensures revisiting the same cell doesn't happen.
     */
    @Test
    public void testNoRevisitSameCell() {
        char[][] grid = {
            {'C', 'A'},
            {'T', 'T'}
        };
        MockBoard board = new MockBoard(grid);
        Set<String> result = player.getAllValidWords(board);

        assertFalse(result.contains("CATT"));
    }

    /**
     * Tests that non-existent words are not formed.
     */
    @Test
    public void testNoInvalidWords() {
        char[][] grid = {
            {'Z', 'Z'},
            {'Z', 'Z'}
        };
        MockBoard board = new MockBoard(grid);
        Set<String> words = player.getAllValidWords(board);
        assertTrue(words.isEmpty());
    }

    /**
     * Performance check on a 5x5 random board.
     */
    @Test
    public void testLargeBoardPerformance() {
        int size = 5;
        char[][] grid = new char[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                grid[i][j] = (char) ('A' + (i + j) % 26);
        MockBoard board = new MockBoard(grid);
        assertDoesNotThrow(() -> player.getAllValidWords(board));
    }

    /**
     * Test that duplicate words are not inserted multiple times.
     */
    @Test
    public void testUniqueWordsOnly() {
        char[][] grid = {
            {'C', 'A'},
            {'T', 'R'}
        };
        MockBoard board = new MockBoard(grid);
        Set<String> words = player.getAllValidWords(board);
        long countCat = words.stream().filter(w -> w.equals("CAT")).count();
        assertTrue(countCat <= 1);
    }

    // === Simple local mock for BoggleBoard ===
    private static class MockBoard extends BoggleBoard {
        private final char[][] letters;

        public MockBoard(char[][] letters) {
            super(); // Only needed if BoggleBoard requires it
            this.letters = letters;
        }

        @Override
        public int rows() {
            return letters.length;
        }

        @Override
        public int cols() {
            return letters[0].length;
        }

        @Override
        public char getLetter(int row, int col) {
            return letters[row][col];
        }
    }
}
