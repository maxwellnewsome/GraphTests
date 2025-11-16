package cpen221.graphs;import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;

import cpen221.graphs.applications.textsimilarity.Document;


import cpen221.graphs.applications.textsimilarity.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.*;

/**
 * Test suite for Similarity.java
 * Uses a minimal DummyDocument subclass that bypasses Document's constructor logic.
 */
public class TextSimilarityTests {

    /** Minimal fake subclass for testing Similarity. */
    private static class DummyDocument extends Document {
        private final Map<String, Integer> counts;
        private final String name;

        // Bypass Document's complex constructor
        public DummyDocument(String name, Map<String, Integer> counts) throws IOException {
            // Call Document's no-arg constructor (even if incomplete)
            super(name,name);
            this.name = name;
            this.counts = counts;
        }

        @Override
        public Map<String, Integer> getCountMap() {
            return counts;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // === Tests ===

    @Test
    public void testIdenticalDocumentsHaveZeroDivergence() throws IOException {
        Map<String, Integer> w = Map.of("a", 2, "b", 2);
        Document d1 = new DummyDocument("D1", w);
        Document d2 = new DummyDocument("D2", w);
        double jsd = Similarity.getJensenShannonDivergence(d1, d2);
        assertEquals(0.0, jsd, 1e-9);
    }

    @Test
    public void testCompletelyDifferentDocumentsHaveHighDivergence() throws IOException {
        Document d1 = new DummyDocument("D1", Map.of("cat", 5, "dog", 5));
        Document d2 = new DummyDocument("D2", Map.of("apple", 5, "banana", 5));
        double jsd = Similarity.getJensenShannonDivergence(d1, d2);
        assertTrue(jsd > 300);
    }

    @Test
    public void testPartialVocabularyOverlap() throws IOException {
        Document d1 = new DummyDocument("D1", Map.of("cat", 2, "dog", 2));
        Document d2 = new DummyDocument("D2", Map.of("dog", 3, "fish", 1));
        double jsd = Similarity.getJensenShannonDivergence(d1, d2);
        assertTrue(jsd > 0 && jsd < 700);
    }

    @Test
    public void testEmptyDocumentDoesNotCrash() throws IOException {
        Document d1 = new DummyDocument("Empty", new HashMap<>());
        Document d2 = new DummyDocument("NonEmpty", Map.of("word", 1));
        double jsd = Similarity.getJensenShannonDivergence(d1, d2);
        assertTrue(jsd >= 0);
    }

    @Test
    public void testJSDIsSymmetric() throws IOException {
        Document d1 = new DummyDocument("A", Map.of("x", 3, "y", 1));
        Document d2 = new DummyDocument("B", Map.of("x", 1, "y", 3));
        double jsd1 = Similarity.getJensenShannonDivergence(d1, d2);
        double jsd2 = Similarity.getJensenShannonDivergence(d2, d1);
        assertEquals(jsd1, jsd2, 1e-9);
    }

    @Test
    public void testSingleDocumentFormsOneGroup() throws IOException {
        Set<Document> docs = Set.of(new DummyDocument("Solo", Map.of("word", 1)));
        var groups = Similarity.groupSimilarDocuments(docs, 1);
        assertEquals(1, groups.size());
    }

    @Test
    public void testEmptySetProducesNoGroups() {
        Set<Document> docs = new HashSet<>();
        var groups = Similarity.groupSimilarDocuments(docs, 3);
        assertTrue(groups.isEmpty());
    }

    @Test
    public void testSimilarDocsClusterTogether() throws IOException {
        Document d1 = new DummyDocument("A", Map.of("cat", 2, "dog", 1));
        Document d2 = new DummyDocument("B", Map.of("cat", 3, "dog", 2));
        Document d3 = new DummyDocument("C", Map.of("apple", 5, "banana", 4));

        Set<Document> docs = Set.of(d1, d2, d3);
        var groups = Similarity.groupSimilarDocuments(docs, 2);

        assertEquals(2, groups.size());
        boolean abTogether = groups.stream()
            .anyMatch(g -> g.contains(d1) && g.contains(d2));
        assertTrue(abTogether);
    }

    @Test
    public void testGroupsContainAllDocuments() throws IOException {
        Document d1 = new DummyDocument("A", Map.of("x", 2));
        Document d2 = new DummyDocument("B", Map.of("y", 2));
        Document d3 = new DummyDocument("C", Map.of("z", 2));

        Set<Document> docs = Set.of(d1, d2, d3);
        var groups = Similarity.groupSimilarDocuments(docs, 2);

        Set<Document> combined = new HashSet<>();
        for (var g : groups) combined.addAll(g);

        assertEquals(docs, combined);
    }
    @Test
    public void testDocumentFromSimpleText() throws IOException {
        Document doc = new Document("D1", "Hello hello world world world");
        Map<String, Integer> counts = doc.getCountMap();

        assertEquals(2, counts.get("hello"));
        assertEquals(3, counts.get("world"));
        assertEquals(2, counts.size()); // only two unique words
    }

    @Test
    public void testEmptyDocumentIsHandled() throws IOException {
        Document empty = new Document("Empty", "");
        assertTrue(empty.getCountMap().isEmpty());
    }

    @Test
    public void testToStringReturnsId() throws IOException {
        Document doc = new Document("ID1", "some text here");
        assertEquals("ID1", doc.toString());
    }

    @Test
    public void testJSDivergenceIdenticalDocs() throws IOException {
        Document d1 = new Document("A", "one two three");
        Document d2 = new Document("B", "one two three");
        assertEquals(0, d1.computeJSDiv(d2));
    }

    @Test
    public void testJSDivergenceDifferentDocs() throws IOException {
        Document d1 = new Document("A", "cat cat cat dog");
        Document d2 = new Document("B", "bird fish");
        assertTrue(d1.computeJSDiv(d2) > 0);
    }

    @Test
    public void testJSDivergenceSymmetry() throws IOException {
        Document d1 = new Document("A", "red blue green");
        Document d2 = new Document("B", "red green yellow");
        long js1 = d1.computeJSDiv(d2);
        long js2 = d2.computeJSDiv(d1);
        assertEquals(js1, js2);
    }
    @Test
    public void testWordCountingBasic() throws IOException {
        Document doc = new Document("Doc1", "The cat and the hat");
        var map = doc.getCountMap();

        assertEquals(4, map.size(), "Should find 4 unique words");
        assertEquals(2, map.get("the"), "Word 'the' should appear twice");
        assertTrue(map.containsKey("cat"));
        assertTrue(map.containsKey("hat"));
    }

    // 2. Verify that invalid URLs are handled without throwing exceptions

    // 3. Check that computeJSDiv() returns 0 for identical documents and >0 for different ones
    @Test
    public void testJSDivergenceBehavior() throws IOException {
        Document sameA = new Document("A", "apple orange apple");
        Document sameB = new Document("B", "apple orange apple");
        Document diff  = new Document("C", "banana pear grape");

        assertEquals(0, sameA.computeJSDiv(sameB),
            "Identical documents should have 0 divergence");
        assertTrue(sameA.computeJSDiv(diff) > 0,
            "Different documents should have positive divergence");
    }
    @Test
    public void testSingleArgConstructorUsesURLAsId() throws IOException {
        String text = "hello world";
        Document doc = new Document(text);
        assertEquals(text, doc.getDocumentId(),
            "Single-argument constructor should set id = url");
        assertEquals(text, doc.getDocumentURL(),
            "Single-argument constructor should set url = id");
        assertFalse(doc.getCountMap().isEmpty(),
            "Should still process inline text correctly");
    }

    // 5. Ensure empty text creates an empty map
    @Test
    public void testEmptyTextHandled() throws IOException {
        Document doc = new Document("EmptyDoc", "");
        assertTrue(doc.getCountMap().isEmpty(),
            "Empty text should produce an empty count map");
        assertNotNull(doc.getDocumentURL());
        assertNotNull(doc.getDocumentId());
    }

    // 6. Verify that JSD with an empty document gives a positive value
    @Test
    public void testJSDivergenceWithEmptyDoc() throws IOException {
        Document nonEmpty = new Document("Filled", "apple apple banana");
        Document empty = new Document("Empty", "");
        long divergence = nonEmpty.computeJSDiv(empty);

        assertTrue(divergence > 0 || divergence == -1,
            "Divergence with an empty document should be nonzero or -1");
    }
    // 7. Confirm that Document equality and toString behave consistently
    @Test
    public void testToStringAndIdentifiers() throws IOException {
        Document doc = new Document("DocA", "Some random words here");
        assertEquals("DocA", doc.toString(),
            "toString() should return the documentId");
        assertEquals("DocA", doc.getDocumentId());
        assertEquals("Some random words here", doc.getDocumentURL());
    }

    // 8. Test JSD when both docs have totally disjoint vocabularies
    @Test
    public void testJSDivergenceCompletelyDifferentDocs() throws IOException {
        Document d1 = new Document("D1", "apple banana orange");
        Document d2 = new Document("D2", "x y z q");
        long jsd = d1.computeJSDiv(d2);

        assertTrue(jsd > 0,
            "Completely disjoint vocabularies should yield positive divergence");
        assertNotEquals(0, jsd,
            "Divergence should not be 0 for disjoint docs");
    }
    @Test
    public void testEmptyDocumentProducesEmptyCountMap() throws IOException {
        Document empty = new Document("EmptyDoc", "");
        assertNotNull(empty.getCountMap(), "Count map should not be null even if text empty");
        assertTrue(empty.getCountMap().isEmpty(), "Empty string should produce empty count map");

        // Comparing to itself — JS divergence should be 0
        assertEquals(0, empty.computeJSDiv(empty), "Empty document divergence with itself should be 0");
    }
    @Test
    public void testDocumentTreatsTextAsLiteralWhenNotURL() throws IOException {
        String sample = "Alpha beta alpha"; // lowercase normalization check
        Document doc = new Document("Sample", sample);

        Map<String, Integer> counts = doc.getCountMap();
        assertEquals(2, counts.get("alpha"));
        assertEquals(1, counts.get("beta"));
        assertEquals(2, counts.size());

        // Should not throw when comparing with itself
        long jsd = doc.computeJSDiv(doc);
        assertEquals(0, jsd, "Identical documents should have divergence 0");
    }
    @Test
    public void testComputeJSDivForDistinctDocuments() throws IOException {
        Document d1 = new Document("Doc1", "apple orange banana");
        Document d2 = new Document("Doc2", "grape apple orange orange");

        long jsd = d1.computeJSDiv(d2);

        assertTrue(jsd > 0, "Divergence should be positive for non-identical documents");
        assertTrue(jsd < 100, "JSD should be normalized and not exceed 100");
    }
    @Test
    public void testDocumentFromValidFileURI() throws Exception {
        // Create a small temporary file with text content
        File temp = File.createTempFile("doc_test_", ".txt");
        try (java.io.FileWriter fw = new java.io.FileWriter(temp)) {
            fw.write("Hello world hello test");
        }

        String fileURL = temp.toURI().toString();  // yields "file:/..."
        Document doc = new Document("FileDoc", fileURL);

        Map<String, Integer> counts = doc.getCountMap();
        assertEquals(3, counts.size(), "Should find three unique words");
        assertEquals(2, counts.get("hello"));
        assertTrue(counts.containsKey("world"));

        temp.delete();
    }
    @Test
    public void testInvalidURLFormatThrowsIOException() {
        assertThrows(IOException.class, () -> {
            new Document("BadDoc", "not.a.valid.pathlike");
        });
    }



}
