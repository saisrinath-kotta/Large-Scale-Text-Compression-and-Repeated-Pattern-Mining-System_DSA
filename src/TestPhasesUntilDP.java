import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * TestPhasesUntilDP - Comprehensive test suite for all string & suffix mining algorithms
 * up to the Dynamic Programming phase:
 * 1. CorpusLoader
 * 2. KMP
 * 3. ZFunction
 * 4. RabinKarp (Double rolling hash + character verification)
 * 5. AhoCorasick (Trie + BFS failure transitions)
 * 6. SuffixArray (O(n log^2 n) prefix doubling)
 * 7. KasaiLCP (O(n) linear LCP)
 * 8. PatternMiner (SA + LCP repeated pattern extraction)
 */
public class TestPhasesUntilDP {

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
            System.err.println("  [FAIL] " + testName + " -> Expected: " + Arrays.toString(expected) + ", Got: " + actual);
        }
    }

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("RUNNING VERIFICATION TESTS: STRING & PATTERN MINING SUITE");
        System.out.println("============================================================");

        CorpusLoader loader = new CorpusLoader("data/corpus.txt");
        boolean corpusLoaded = loader.load();
        assertTrue("CorpusLoader loads data/corpus.txt", corpusLoaded);
        String corpus = loader.getText();

        String hindiText = "भारत एक विशाल देश है। भारतीय संस्कृति समृद्ध है।";
        String teluguText = "భారతదేశం ఒక గొప్ప దేశం. భారతీయ సంస్కృతి ఎంతో గొప్పది.";

        // ---------------------------------------------------------
        // 1. Rabin-Karp Tests
        // ---------------------------------------------------------
        System.out.println("\n[1] Testing Rabin-Karp (Double Rolling Hash + Verification)...");
        assertMatches("RK: Overlapping ABABABAB / ABAB", RabinKarp.search("ABABABAB", "ABAB"), new int[]{0, 2, 4});
        assertMatches("RK: Beginning match banana / ba", RabinKarp.search("banana", "ba"), new int[]{0});
        assertMatches("RK: Ending match banana / na", RabinKarp.search("banana", "na"), new int[]{2, 4});
        assertMatches("RK: No match banana / apple", RabinKarp.search("banana", "apple"), new int[]{});
        assertMatches("RK: Hindi Unicode 'भारत'", RabinKarp.search(hindiText, "भारत"), new int[]{0, 22});
        assertMatches("RK: Telugu Unicode 'భారత'", RabinKarp.search(teluguText, "భారత"), new int[]{0, 24});

        // Verify equivalence of KMP, Z, and Rabin-Karp on full corpus
        List<Integer> kmpCorpus = KMP.search(corpus, "algorithm");
        List<Integer> zCorpus = ZFunction.search(corpus, "algorithm");
        List<Integer> rkCorpus = RabinKarp.search(corpus, "algorithm");
        assertTrue("KMP, Z, and RK return identical match counts for 'algorithm'", 
            kmpCorpus.size() == zCorpus.size() && zCorpus.size() == rkCorpus.size());
        assertTrue("KMP, Z, and RK return identical positions for 'algorithm'", 
            kmpCorpus.equals(zCorpus) && zCorpus.equals(rkCorpus));

        List<Integer> kmpHindi = KMP.search(corpus, "भारत");
        List<Integer> rkHindi = RabinKarp.search(corpus, "भारत");
        assertTrue("KMP and RK return identical positions for Hindi 'भारत'", kmpHindi.equals(rkHindi));

        // ---------------------------------------------------------
        // 2. Aho-Corasick Multi-Pattern Matching Tests
        // ---------------------------------------------------------
        System.out.println("\n[2] Testing Aho-Corasick Multi-Pattern Automaton...");
        AhoCorasick ac = new AhoCorasick(Arrays.asList("he", "she", "his", "hers"));
        Map<String, List<Integer>> acResults = ac.search("ushers");

        // In "ushers":
        // "she" is at index 1 ("usher" -> u-s-h-e-r-s)
        // "he" is at index 2 ("ushers")
        // "hers" is at index 2 ("ushers")
        // "his" has 0 matches
        assertMatches("AC: 'she' in 'ushers'", acResults.get("she"), new int[]{1});
        assertMatches("AC: 'he' in 'ushers'", acResults.get("he"), new int[]{2});
        assertMatches("AC: 'hers' in 'ushers'", acResults.get("hers"), new int[]{2});
        assertMatches("AC: 'his' in 'ushers'", acResults.get("his"), new int[]{});

        // Indian language multi-pattern test
        AhoCorasick acIndian = new AhoCorasick(Arrays.asList("भारत", "भारतीय", "संस्कृति"));
        Map<String, List<Integer>> acIndianRes = acIndian.search(hindiText);
        assertMatches("AC: Hindi 'भारत' in hindiText", acIndianRes.get("भारत"), new int[]{0, 22});
        assertMatches("AC: Hindi 'भारतीय' in hindiText", acIndianRes.get("भारतीय"), new int[]{22});
        assertMatches("AC: Hindi 'संस्कृति' in hindiText", acIndianRes.get("संस्कृति"), new int[]{29});

        // ---------------------------------------------------------
        // 3. Suffix Array Tests
        // ---------------------------------------------------------
        System.out.println("\n[3] Testing Suffix Array (Prefix Doubling O(n log^2 n))...");
        int[] bananaSA = SuffixArray.buildSuffixArray("banana");
        // Expected for "banana": [5, 3, 1, 0, 4, 2]
        // 5: a, 3: ana, 1: anana, 0: banana, 4: na, 2: nana
        int[] expectedBananaSA = new int[]{5, 3, 1, 0, 4, 2};
        assertMatches("Suffix Array for 'banana' matches [5, 3, 1, 0, 4, 2]", 
            toIntegerList(bananaSA), expectedBananaSA);

        // Verify that every suffix in SA is lexicographically <= the next
        String saVerifyText = "abracadabra";
        int[] abraSA = SuffixArray.buildSuffixArray(saVerifyText);
        boolean saSorted = true;
        for (int i = 0; i < abraSA.length - 1; i++) {
            String s1 = saVerifyText.substring(abraSA[i]);
            String s2 = saVerifyText.substring(abraSA[i + 1]);
            if (s1.compareTo(s2) > 0) {
                saSorted = false;
                break;
            }
        }
        assertTrue("Suffix Array for 'abracadabra' is strictly in lexicographical order", saSorted);

        // ---------------------------------------------------------
        // 4. Kasai LCP Tests
        // ---------------------------------------------------------
        System.out.println("\n[4] Testing Kasai LCP (Linear O(n))...");
        int[] bananaLCP = KasaiLCP.buildLCP("banana", bananaSA);
        // Expected LCP for "banana" with SA [5, 3, 1, 0, 4, 2]:
        // LCP(a, ana) = 1
        // LCP(ana, anana) = 3
        // LCP(anana, banana) = 0
        // LCP(banana, na) = 0
        // LCP(na, nana) = 2
        // So LCP array: [0, 1, 3, 0, 0, 2]
        int[] expectedBananaLCP = new int[]{0, 1, 3, 0, 0, 2};
        assertMatches("Kasai LCP for 'banana' matches [0, 1, 3, 0, 0, 2]", 
            toIntegerList(bananaLCP), expectedBananaLCP);

        // Verify LCP correctness on corpus text
        String corpusSample = corpus.substring(0, Math.min(300, corpus.length()));
        int[] sampleSA = SuffixArray.buildSuffixArray(corpusSample);
        int[] sampleLCP = KasaiLCP.buildLCP(corpusSample, sampleSA);
        boolean lcpValid = true;
        for (int i = 1; i < sampleSA.length; i++) {
            int idx1 = sampleSA[i - 1];
            int idx2 = sampleSA[i];
            int expectedL = 0;
            while (idx1 + expectedL < corpusSample.length() && idx2 + expectedL < corpusSample.length()
                    && corpusSample.charAt(idx1 + expectedL) == corpusSample.charAt(idx2 + expectedL)) {
                expectedL++;
            }
            if (sampleLCP[i] != expectedL) {
                lcpValid = false;
                break;
            }
        }
        assertTrue("Kasai LCP values strictly match actual common prefix lengths on corpus", lcpValid);

        // ---------------------------------------------------------
        // 5. Pattern Mining Tests (SA + Kasai LCP)
        // ---------------------------------------------------------
        System.out.println("\n[5] Testing Repeated Pattern Mining (SA + LCP)...");
        String testRepeats = "banana is a banana. banana patterns are repeated.";
        int[] repSA = SuffixArray.buildSuffixArray(testRepeats);
        int[] repLCP = KasaiLCP.buildLCP(testRepeats, repSA);
        List<PatternMiner.RepeatedPattern> mined = PatternMiner.mineRepeatedPatterns(testRepeats, repSA, repLCP, 6);

        assertTrue("Pattern miner discovered repeated patterns of length >= 6", !mined.isEmpty());
        boolean foundBanana = false;
        for (PatternMiner.RepeatedPattern rp : mined) {
            if (rp.getPattern().equals("banana")) {
                foundBanana = true;
                assertTrue("Top repeated pattern 'banana' occurs 3 times", rp.getOccurrences() == 3);
                assertTrue("Positions for 'banana' contain 0, 12, 20", 
                    rp.getPositions().contains(0) && rp.getPositions().contains(12) && rp.getPositions().contains(20));
                break;
            }
        }
        assertTrue("Pattern miner correctly discovered repeated pattern 'banana' with all occurrences", foundBanana);

        System.out.println("\n============================================================");
        System.out.printf("TOTAL SUITE TESTS PASSED: %d / %d\n", passedTests, totalTests);
        System.out.println("============================================================");

        if (passedTests == totalTests) {
            System.out.println("ALL PHASES UNTIL DYNAMIC PROGRAMMING FULLY VERIFIED!");
        } else {
            System.err.println("Some tests failed!");
            System.exit(1);
        }
    }

    private static List<Integer> toIntegerList(int[] arr) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int v : arr) {
            list.add(v);
        }
        return list;
    }
}
