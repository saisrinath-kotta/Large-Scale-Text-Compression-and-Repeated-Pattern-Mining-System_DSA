import java.util.List;

/**
 * TestDPPhase - Verification test suite for the Dynamic Programming phase:
 * Wagner-Fischer Edit Distance and Corpus Fuzzy Matching.
 */
public class TestDPPhase {

    private static int totalTests = 0;
    private static int passedTests = 0;

    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passedTests++;
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void assertEquals(String testName, int actual, int expected) {
        totalTests++;
        if (actual == expected) {
            System.out.println("  [PASS] " + testName + " -> " + actual);
            passedTests++;
        } else {
            System.err.println("  [FAIL] " + testName + " -> Expected: " + expected + ", Got: " + actual);
        }
    }

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("RUNNING DYNAMIC PROGRAMMING (EDIT DISTANCE) VERIFICATION");
        System.out.println("============================================================");

        // 1. Classic textbook test
        assertEquals("Classic: 'kitten' -> 'sitting'", EditDistance.compute("kitten", "sitting"), 3);

        // 2. Identical strings
        assertEquals("Identical: 'algorithm' -> 'algorithm'", EditDistance.compute("algorithm", "algorithm"), 0);

        // 3. Single insertion
        assertEquals("Insertion: 'cat' -> 'cats'", EditDistance.compute("cat", "cats"), 1);

        // 4. Single deletion
        assertEquals("Deletion: 'cats' -> 'cat'", EditDistance.compute("cats", "cat"), 1);

        // 5. Single replacement
        assertEquals("Replacement: 'cat' -> 'cut'", EditDistance.compute("cat", "cut"), 1);

        // 6. Completely different strings
        assertEquals("Completely different: 'abc' -> 'xyz'", EditDistance.compute("abc", "xyz"), 3);

        // 7. Empty string edge cases
        assertEquals("Empty string 1: '' -> 'hello'", EditDistance.compute("", "hello"), 5);
        assertEquals("Empty string 2: 'world' -> ''", EditDistance.compute("world", ""), 5);
        assertEquals("Both empty: '' -> ''", EditDistance.compute("", ""), 0);

        // 8. Indian language tests
        // Hindi: "भारत" (4 chars) -> "भारतीय" (6 chars: adds 'ी', 'य') -> distance 2
        assertEquals("Hindi: 'भारत' -> 'भारतीय'", EditDistance.compute("भारत", "भारतीय"), 2);

        // Telugu: "భారత" (4 chars) -> "భారతం" (5 chars: adds 'ం') -> distance 1
        assertEquals("Telugu: 'భారత' -> 'భారతం'", EditDistance.compute("భారత", "భారతం"), 1);

        // 9. Fuzzy matching test on corpus
        CorpusLoader loader = new CorpusLoader("data/corpus.txt");
        loader.load();
        String corpus = loader.getText();

        List<String> fuzzyResults = EditDistance.fuzzySearch(corpus, "algoritm", 2);
        assertTrue("Fuzzy search for 'algoritm' finds matches in corpus", !fuzzyResults.isEmpty());
        boolean foundAlgorithm = false;
        for (String match : fuzzyResults) {
            if (match.startsWith("algorithm")) {
                foundAlgorithm = true;
                break;
            }
        }
        assertTrue("Fuzzy search successfully matched misspelled 'algoritm' to 'algorithm'", foundAlgorithm);

        System.out.println("\n============================================================");
        System.out.printf("TOTAL DP TESTS PASSED: %d / %d\n", passedTests, totalTests);
        System.out.println("============================================================");

        if (passedTests == totalTests) {
            System.out.println("DYNAMIC PROGRAMMING PHASE FULLY VERIFIED!");
        } else {
            System.err.println("Some DP tests failed!");
            System.exit(1);
        }
    }
}
