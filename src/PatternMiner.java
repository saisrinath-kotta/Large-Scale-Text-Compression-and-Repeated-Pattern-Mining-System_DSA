import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PatternMiner - Repeated Substring and Pattern Mining using Suffix Array + Kasai LCP.
 * 
 * Algorithm Concept:
 * In a text corpus, repeated substrings correspond directly to prefixes shared between
 * two or more suffixes.
 * When suffixes are sorted in the Suffix Array, suffixes that share common prefixes
 * are placed adjacent to each other!
 * 
 * LCP Interval Decomposition (The Standard Academic Algorithm):
 * - An LCP interval [start, end] with LCP value L (where min(LCP[start+1...end]) >= L)
 *   represents a maximal substring of length L that appears in all suffixes from
 *   SA[start] to SA[end].
 * - The number of occurrences of this repeated substring is exactly: (end - start + 1).
 * - The positions in the corpus are exactly: SA[start], SA[start+1], ..., SA[end].
 * 
 * We process LCP intervals using a linear O(n) interval stack.
 * For each interval with L >= minLength and occurrences >= 2:
 * 1. Extract the pattern: text.substring(SA[start], SA[start] + L)
 * 2. Record its occurrences and all occurrence positions
 * 
 * Complexity:
 * Discovery Time:  O(n) on top of precomputed Suffix Array and LCP
 * Space:           O(number of distinct repeated patterns)
 */
public class PatternMiner {

    /**
     * Data class to store information about a mined repeated pattern.
     */
    public static class RepeatedPattern {
        private final String pattern;
        private final int length;
        private final int occurrences;
        private final List<Integer> positions;

        public RepeatedPattern(String pattern, int occurrences, List<Integer> positions) {
            this.pattern = pattern;
            this.length = pattern.length();
            this.occurrences = occurrences;
            this.positions = positions;
        }

        public String getPattern() {
            return pattern;
        }

        public int getLength() {
            return length;
        }

        public int getOccurrences() {
            return occurrences;
        }

        public List<Integer> getPositions() {
            return positions;
        }
    }

    /**
     * Mines repeated substrings of length >= minLength from text using SA and Kasai LCP intervals.
     * 
     * @param text The input text corpus.
     * @param sa The precomputed suffix array.
     * @param lcp The precomputed Kasai LCP array.
     * @param minLength Minimum length of repeated substrings to extract.
     * @return List of RepeatedPattern objects sorted by occurrences (descending) and length.
     */
    public static List<RepeatedPattern> mineRepeatedPatterns(String text, int[] sa, int[] lcp, int minLength) {
        List<RepeatedPattern> results = new ArrayList<>();

        if (text == null || sa == null || lcp == null || minLength <= 0) {
            return results;
        }

        int n = text.length();
        if (n == 0 || sa.length != n || lcp.length != n) {
            return results;
        }

        // Map to record maximal patterns and their occurrence positions
        Map<String, Set<Integer>> patternPositions = new HashMap<>();

        // Stack to maintain LCP intervals: stackLcp stores LCP value, stackStart stores start SA index
        int[] stackLcp = new int[n + 2];
        int[] stackStart = new int[n + 2];
        int top = 0;

        stackLcp[0] = 0;
        stackStart[0] = 0;

        // Process suffixes from 1 to n (at index n, currentLcp = 0 to flush the stack)
        for (int i = 1; i <= n; i++) {
            int currentLcp = (i < n) ? lcp[i] : 0;
            int lastStart = i - 1;

            while (top >= 0 && stackLcp[top] > currentLcp) {
                int poppedLcp = stackLcp[top];
                int startIdx = stackStart[top];
                top--;

                if (poppedLcp >= minLength) {
                    int endIdx = i - 1;
                    int occurrences = endIdx - startIdx + 1;
                    if (occurrences >= 2) {
                        String pat = text.substring(sa[startIdx], sa[startIdx] + poppedLcp);
                        Set<Integer> positions = patternPositions.computeIfAbsent(pat, k -> new HashSet<>());
                        for (int k = startIdx; k <= endIdx; k++) {
                            positions.add(sa[k]);
                        }
                    }
                }
                lastStart = startIdx;
            }

            if (currentLcp > 0 && (top < 0 || currentLcp > stackLcp[top])) {
                top++;
                stackLcp[top] = currentLcp;
                stackStart[top] = lastStart;
            }
        }

        // Convert grouped patterns into RepeatedPattern objects
        for (Map.Entry<String, Set<Integer>> entry : patternPositions.entrySet()) {
            String pat = entry.getKey();
            List<Integer> posList = new ArrayList<>(entry.getValue());
            Collections.sort(posList);
            results.add(new RepeatedPattern(pat, posList.size(), posList));
        }

        // Sort by occurrences (descending), then by pattern length (descending)
        results.sort((a, b) -> {
            if (b.getOccurrences() != a.getOccurrences()) {
                return Integer.compare(b.getOccurrences(), a.getOccurrences());
            }
            return Integer.compare(b.getLength(), a.getLength());
        });

        return results;
    }

    /**
     * Formats the list of mined patterns into a clean table for CMD output.
     */
    public static String formatSummaryTable(List<RepeatedPattern> patterns, int maxRows) {
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(patterns.size(), maxRows);

        sb.append(String.format("%-4s | %-28s | %-8s | %-12s\n", "No.", "Pattern", "Length", "Occurrences"));
        sb.append("--------------------------------------------------------------------\n");

        for (int i = 0; i < limit; i++) {
            RepeatedPattern p = patterns.get(i);
            String displayPat = p.getPattern().replace("\n", "\\n").replace("\r", "");
            if (displayPat.length() > 26) {
                displayPat = displayPat.substring(0, 23) + "...";
            }
            sb.append(String.format("%-4d | %-28s | %-8d | %-12d\n",
                    (i + 1), displayPat, p.getLength(), p.getOccurrences()));
        }

        if (limit < patterns.size()) {
            sb.append(String.format("... and %d more patterns found (display truncated)\n", patterns.size() - limit));
        }

        return sb.toString();
    }

    /**
     * Standalone main method to test PatternMiner from CMD.
     */
    public static void main(String[] args) {
        String text = (args.length > 0) ? args[0] : 
            "banana is a banana. banana patterns are repeated. algorithms help in pattern searching.";
        int minLen = (args.length > 1) ? Integer.parseInt(args[1]) : 5;

        System.out.println("============================================================");
        System.out.println("REPEATED PATTERN MINING DEMO (SA + KASAI LCP)");
        System.out.println("============================================================");
        System.out.println("Text Length:            " + text.length());
        System.out.println("Minimum Pattern Length: " + minLen);

        int[] sa = SuffixArray.buildSuffixArray(text);
        int[] lcp = KasaiLCP.buildLCP(text, sa);

        long startTime = System.nanoTime();
        List<RepeatedPattern> patterns = mineRepeatedPatterns(text, sa, lcp, minLen);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;

        System.out.println("\nRepeated Patterns Found: " + patterns.size());
        System.out.println();
        System.out.print(formatSummaryTable(patterns, 10));

        if (!patterns.isEmpty()) {
            RepeatedPattern top = patterns.get(0);
            System.out.println("\nDetailed view of top repeated pattern:");
            System.out.println("Pattern:     \"" + top.getPattern() + "\"");
            System.out.println("Length:      " + top.getLength());
            System.out.println("Occurrences: " + top.getOccurrences());
            System.out.println("Positions:   " + top.getPositions());
        }

        System.out.printf("\nMining Time:            %.4f ms\n", elapsedMs);
        System.out.println("============================================================");
    }
}
