import java.util.Arrays;

/**
 * KasaiLCP - Linear-Time O(n) Longest Common Prefix (LCP) Array Construction.
 * 
 * Algorithm Concept:
 * Given a text and its Suffix Array (SA), the LCP array stores the length of the
 * longest common prefix between consecutive suffixes in the sorted suffix array:
 *    lcp[i] = length of common prefix between suffix SA[i-1] and suffix SA[i].
 * By definition, lcp[0] = 0.
 * 
 * Why Kasai's Algorithm runs in O(n) Time (Crucial for Viva):
 * - Naively comparing adjacent suffixes would take O(n^2) worst case.
 * - Kasai processes suffixes in TEXT order (from suffix 0 to suffix n-1), NOT in SA order.
 * - Key Observation:
 *   If suffix i has an LCP of h with its predecessor in the suffix array, then suffix i+1
 *   (which is suffix i minus its first character) is guaranteed to share at least (h - 1)
 *   characters with its predecessor!
 * - Therefore, we can start our character comparisons directly from (h - 1) instead of 0.
 * - Since h can increase at most n times (the length of the string) and decreases at most n times,
 *   the inner comparison loop executes at most 2n times overall!
 * 
 * Time Complexity:   O(n)
 * Space Complexity:  O(n) for the rank and LCP arrays
 */
public class KasaiLCP {

    /**
     * Constructs the LCP array for a given text and its precomputed suffix array.
     * 
     * @param text The input text string.
     * @param sa The precomputed suffix array of text.
     * @return The integer array lcp of length n.
     */
    public static int[] buildLCP(String text, int[] sa) {
        if (text == null || sa == null) {
            return new int[0];
        }

        int n = text.length();
        if (n == 0 || sa.length != n) {
            return new int[0];
        }

        // Step 1: Construct the Rank array (inverse of Suffix Array)
        // rank[i] gives the position of suffix starting at text index i in the sorted SA
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }

        int[] lcp = new int[n];
        lcp[0] = 0; // First suffix has no predecessor

        // h tracks the current longest common prefix length
        int h = 0;

        // Step 2: Iterate through suffixes in TEXT order (i = 0, 1, ..., n-1)
        for (int i = 0; i < n; i++) {
            // Suffix at text index i has position rank[i] in the suffix array
            if (rank[i] > 0) {
                // Predecessor suffix in sorted suffix array
                int prevSuffixIndex = sa[rank[i] - 1];

                // Extend matching characters starting from index h
                while (i + h < n && prevSuffixIndex + h < n 
                        && text.charAt(i + h) == text.charAt(prevSuffixIndex + h)) {
                    h++;
                }

                // Store computed LCP value at this position in the LCP array
                lcp[rank[i]] = h;

                // For the next suffix (i + 1), LCP is at least (h - 1)
                // We reuse the previous match length!
                if (h > 0) {
                    h--;
                }
            } else {
                // If this is the very first suffix in sorted order, reset h to 0
                h = 0;
            }
        }

        return lcp;
    }

    /**
     * Builds the rank array (inverse suffix array).
     */
    public static int[] buildRank(int[] sa) {
        int n = sa.length;
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }
        return rank;
    }

    /**
     * Formats Suffix Array and LCP Array side-by-side for clear display.
     */
    public static String formatSAndLCP(String text, int[] sa, int[] lcp, int maxDisplay) {
        StringBuilder sb = new StringBuilder();
        int n = sa.length;
        int limit = Math.min(n, maxDisplay);

        sb.append(String.format("%-6s | %-8s | %-6s | %s\n", "Pos", "SA[i]", "LCP[i]", "Suffix"));
        sb.append("--------------------------------------------------------------------\n");

        for (int i = 0; i < limit; i++) {
            int idx = sa[i];
            String suf = text.substring(idx).replace("\n", "\\n").replace("\r", "");
            if (suf.length() > 35) {
                suf = suf.substring(0, 32) + "...";
            }
            sb.append(String.format("%-6d | %-8d | %-6d | %s\n", i, idx, lcp[i], suf));
        }

        if (limit < n) {
            sb.append(String.format("... and %d more entries (display truncated)\n", n - limit));
        }

        return sb.toString();
    }

    /**
     * Standalone main method to test KasaiLCP from CMD.
     */
    public static void main(String[] args) {
        String text = (args.length > 0) ? args[0] : "banana";

        System.out.println("============================================================");
        System.out.println("KASAI LCP ARRAY CONSTRUCTION DEMO");
        System.out.println("============================================================");
        System.out.println("Text:         " + text);

        int[] sa = SuffixArray.buildSuffixArray(text);
        int[] lcp = buildLCP(text, sa);

        System.out.println("\nSuffix Array: " + Arrays.toString(sa));
        System.out.println("LCP Array:    " + Arrays.toString(lcp));

        System.out.println("\nCombined Table:");
        System.out.print(formatSAndLCP(text, sa, lcp, 20));

        System.out.println("\nComplexity:   Time: O(n) | Space: O(n)");
        System.out.println("============================================================");
    }
}
