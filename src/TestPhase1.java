import java.util.List;

/**
 * TestPhase1 - Verification test suite for Phase 1 components:
 * 1. CorpusLoader
 * 2. KMP (Knuth-Morris-Pratt)
 * 3. ZFunction
 */
public class TestPhase1 {

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

    private static void assertMatches(String testName, List<Integer> actual, int[] expected) {
        totalTests++;
        boolean match = (actual.size() == expected.length);
        if (match) {
            for (int i = 0; i < expected.length; i++) {
                if (actual.get(i) != expected[i]) {
                    match = false;
                    break;
                }
            }
        }
        if (match) {
            System.out.println("  [PASS] " + testName + " -> " + actual);
            passedTests++;
        } else {
            System.err.println("  [FAIL] " + testName + " -> Expected: " + java.util.Arrays.toString(expected) + ", Got: " + actual);
        }
    }

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("RUNNING PHASE 1 VERIFICATION TESTS");
        System.out.println("============================================================");

        // ---------------------------------------------------------
        // 1. CorpusLoader Tests
        // ---------------------------------------------------------
        System.out.println("\n[1] Testing CorpusLoader...");
        CorpusLoader loader = new CorpusLoader("data/test_corpus.txt");
        boolean loaded = loader.load();
        assertTrue("CorpusLoader loads data/test_corpus.txt", loaded);
        assertTrue("CorpusLoader character count > 0", loader.getCharCount() > 0);
        assertTrue("CorpusLoader line count > 0", loader.getLineCount() > 0);

        CorpusLoader mainLoader = new CorpusLoader("data/corpus.txt");
        boolean mainLoaded = mainLoader.load();
        assertTrue("CorpusLoader loads data/corpus.txt", mainLoaded);
        assertTrue("Main corpus contains Indian Unicode text", mainLoader.getText().contains("भारत") && mainLoader.getText().contains("భారతదేశం"));

        // ---------------------------------------------------------
        // 2. KMP Tests
        // ---------------------------------------------------------
        System.out.println("\n[2] Testing KMP Pattern Search...");
        // Overlapping match
        assertMatches("KMP: ABABABAB / ABAB", KMP.search("ABABABAB", "ABAB"), new int[]{0, 2, 4});

        // LPS array verification
        int[] lps = KMP.buildLPS("ABAB");
        assertTrue("KMP LPS for ABAB is [0, 0, 1, 2]", 
            lps.length == 4 && lps[0] == 0 && lps[1] == 0 && lps[2] == 1 && lps[3] == 2);

        // Beginning match
        assertMatches("KMP: Beginning match", KMP.search("banana", "ba"), new int[]{0});

        // Ending match
        assertMatches("KMP: Ending match", KMP.search("banana", "na"), new int[]{2, 4});

        // No match
        assertMatches("KMP: No match", KMP.search("banana", "apple"), new int[]{});

        // Unicode Hindi test
        // "भारत एक विशाल देश है। भारतीय संस्कृति समृद्ध है।"
        // First "भारत" at index 0
        // Second "भारत" inside "भारतीय" starts at index 22
        String hindiText = "भारत एक विशाल देश है। भारतीय संस्कृति समृद्ध है।";
        assertMatches("KMP: Hindi Unicode 'भारत'", KMP.search(hindiText, "भारत"), new int[]{0, 22});

        // Unicode Telugu test
        // "భారతదేశం ఒక గొప్ప దేశం. భారతీయ సంస్కృతి ఎంతో గొప్పది."
        // First "భారత" inside "భారతదేశం" at index 0
        // Second "భారత" inside "భారతీయ" at index 24
        String teluguText = "భారతదేశం ఒక గొప్ప దేశం. భారతీయ సంస్కృతి ఎంతో గొప్పది.";
        assertMatches("KMP: Telugu Unicode 'భారత'", KMP.search(teluguText, "భారత"), new int[]{0, 24});

        // Edge case: empty pattern or text
        assertMatches("KMP: Empty pattern", KMP.search("banana", ""), new int[]{});
        assertMatches("KMP: Pattern longer than text", KMP.search("hi", "hello world"), new int[]{});

        // ---------------------------------------------------------
        // 3. Z-Function Tests
        // ---------------------------------------------------------
        System.out.println("\n[3] Testing ZFunction Pattern Search...");
        // Overlapping match
        assertMatches("Z: ABABABAB / ABAB", ZFunction.search("ABABABAB", "ABAB"), new int[]{0, 2, 4});

        // Beginning match
        assertMatches("Z: Beginning match", ZFunction.search("banana", "ba"), new int[]{0});

        // Ending match
        assertMatches("Z: Ending match", ZFunction.search("banana", "na"), new int[]{2, 4});

        // No match
        assertMatches("Z: No match", ZFunction.search("banana", "apple"), new int[]{});

        // Unicode Hindi test
        assertMatches("Z: Hindi Unicode 'भारत'", ZFunction.search(hindiText, "भारत"), new int[]{0, 22});

        // Unicode Telugu test
        assertMatches("Z: Telugu Unicode 'భారత'", ZFunction.search(teluguText, "భారత"), new int[]{0, 24});

        // Edge case: empty pattern or text
        assertMatches("Z: Empty pattern", ZFunction.search("banana", ""), new int[]{});
        assertMatches("Z: Pattern longer than text", ZFunction.search("hi", "hello world"), new int[]{});

        // Check equivalence between KMP and Z-search on main corpus
        List<Integer> kmpResults = KMP.search(mainLoader.getText(), "algorithm");
        List<Integer> zResults = ZFunction.search(mainLoader.getText(), "algorithm");
        assertTrue("KMP and ZFunction yield identical match count on corpus ('algorithm')", 
            kmpResults.size() == zResults.size() && kmpResults.size() > 0);
        assertTrue("KMP and ZFunction yield identical positions on corpus ('algorithm')", 
            kmpResults.equals(zResults));

        // Hindi word check on corpus
        List<Integer> kmpHindi = KMP.search(mainLoader.getText(), "भारत");
        List<Integer> zHindi = ZFunction.search(mainLoader.getText(), "भारत");
        assertTrue("KMP and ZFunction yield identical match count on corpus ('भारत')", 
            kmpHindi.size() == zHindi.size() && kmpHindi.size() > 0);
        assertTrue("KMP and ZFunction yield identical positions on corpus ('भारत')", 
            kmpHindi.equals(zHindi));

        System.out.println("\n============================================================");
        System.out.printf("PHASE 1 TEST SUMMARY: %d / %d TESTS PASSED\n", passedTests, totalTests);
        System.out.println("============================================================");

        if (passedTests == totalTests) {
            System.out.println("All Phase 1 tests passed successfully!");
        } else {
            System.err.println("Some tests failed!");
            System.exit(1);
        }
    }
}
