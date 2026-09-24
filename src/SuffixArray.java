import java.util.Arrays;
import java.util.Comparator;

/**
 * SuffixArray - Practical O(n log^2 n) Suffix Array Construction via Prefix Doubling.
 * 
 * Algorithm Concept:
 * A Suffix Array is an array of integers giving the starting indices of the suffixes
 * of a string, sorted in lexicographical order.
 * 
 * Suffix Array vs Suffix Tree:
 * - A Suffix Tree requires substantial memory overhead (pointer-heavy, 20x to 40x the text size).
 * - A Suffix Array stores just n integers (4n bytes) and, combined with the Kasai LCP array,
 *   can perform all queries that a suffix tree does with much better cache locality.
 * 
 * Prefix Doubling / Ranking Technique (Viva Explanation):
 * 1. At step k = 0, suffixes are ranked by their first character (2^0 = 1).
 * 2. At step k, we already know the lexicographical order of all substrings of length 2^(k-1).
 * 3. To compare substrings of length 2^k starting at index i and j, we represent each by a pair of ranks:
 *       ( rank[i],  rank[i + 2^(k-1)] )
 *    Because two halves of length 2^(k-1) uniquely determine the order of length 2^k!
 * 4. We sort these n rank pairs in O(n log n) time, update the ranks, and double k.
 * 5. This doubling repeats at most ceil(log2 n) times.
 * 
 * Overall Time Complexity:   O(n log^2 n)
 * Space Complexity:          O(n)
 */
public class SuffixArray {

    /**
     * Helper structure representing a suffix during the prefix doubling passes.
     */
    private static class Suffix {
        int index;       // Original starting position in the text
        int firstRank;   // Rank of first half of length 2^(k-1)
        int secondRank;  // Rank of second half of length 2^(k-1)
    }

    /**
     * Constructs the suffix array for a given string text.
     * 
     * @param text The input string (supports Indian languages / Unicode).
     * @return An integer array containing the 0-based suffix starting positions in sorted order.
     */
    public static int[] buildSuffixArray(String text) {
        if (text == null) {
            return new int[0];
        }

        int n = text.length();
        if (n == 0) {
            return new int[0];
        }

        Suffix[] suffixes = new Suffix[n];

        // Step 1: Initial ranking based on the first character
        for (int i = 0; i < n; i++) {
            suffixes[i] = new Suffix();
            suffixes[i].index = i;
            suffixes[i].firstRank = (int) text.charAt(i);
            suffixes[i].secondRank = (i + 1 < n) ? (int) text.charAt(i + 1) : -1;
        }

        // Initial sort based on first 2 characters
        Arrays.sort(suffixes, new SuffixComparator());

        // Array to store the new ranks of suffixes
        int[] indexToRank = new int[n];

        // Step 2: Prefix doubling loop (lengths 4, 8, 16, ...)
        for (int len = 4; len < 2 * n; len *= 2) {
            // Assign ranks to suffixes based on sorted order from previous step
            int rank = 0;
            int prevFirstRank = suffixes[0].firstRank;
            int prevSecondRank = suffixes[0].secondRank;

            suffixes[0].firstRank = rank;
            indexToRank[suffixes[0].index] = rank;

            for (int i = 1; i < n; i++) {
                // If this suffix has the exact same rank pair as previous, assign same rank
                if (suffixes[i].firstRank == prevFirstRank && suffixes[i].secondRank == prevSecondRank) {
                    suffixes[i].firstRank = rank;
                } else {
                    // Otherwise, increment rank
                    rank++;
                    prevFirstRank = suffixes[i].firstRank;
                    prevSecondRank = suffixes[i].secondRank;
                    suffixes[i].firstRank = rank;
                }
                indexToRank[suffixes[i].index] = rank;
            }

            // Early exit optimization: if all ranks are distinct, sorting is complete!
            if (rank == n - 1) {
                break;
            }

            // Assign the second half rank using the updated ranks
            int half = len / 2;
            for (int i = 0; i < n; i++) {
                int nextIndex = suffixes[i].index + half;
                suffixes[i].secondRank = (nextIndex < n) ? indexToRank[nextIndex] : -1;
            }

            // Sort suffixes using the updated (firstRank, secondRank) pairs
            Arrays.sort(suffixes, new SuffixComparator());
        }

        // Step 3: Extract the final sorted starting indices into the suffix array
        int[] sa = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = suffixes[i].index;
        }

        return sa;
    }

    /**
     * Comparator for sorting suffix rank pairs.
     */
    private static class SuffixComparator implements Comparator<Suffix> {
        @Override
        public int compare(Suffix a, Suffix b) {
            if (a.firstRank != b.firstRank) {
                return Integer.compare(a.firstRank, b.firstRank);
            }
            return Integer.compare(a.secondRank, b.secondRank);
        }
    }

    /**
     * Formats the suffix array and suffixes for display in viva or CMD.
     * @param text The input text.
     * @param sa The suffix array.
     * @param maxDisplay Maximum number of suffixes to display.
     * @return Formatted multi-line string.
     */
    public static String formatSuffixes(String text, int[] sa, int maxDisplay) {
        StringBuilder sb = new StringBuilder();
        int n = sa.length;
        int limit = Math.min(n, maxDisplay);

        sb.append(String.format("%-6s | %-8s | %s\n", "SA[i]", "Index", "Suffix"));
        sb.append("------------------------------------------------------------\n");

        for (int i = 0; i < limit; i++) {
            int idx = sa[i];
            String suf = text.substring(idx);
            // Replace newlines for clean printing
            suf = suf.replace("\n", "\\n").replace("\r", "");
            if (suf.length() > 40) {
                suf = suf.substring(0, 37) + "...";
            }
            sb.append(String.format("%-6d | %-8d | %s\n", i, idx, suf));
        }

        if (limit < n) {
            sb.append(String.format("... and %d more suffixes (display truncated)\n", n - limit));
        }

        return sb.toString();
    }

    /**
     * Standalone main method to test SuffixArray from CMD.
     */
    public static void main(String[] args) {
        String text = (args.length > 0) ? args[0] : "banana";

        System.out.println("============================================================");
        System.out.println("SUFFIX ARRAY CONSTRUCTION DEMO");
        System.out.println("============================================================");
        System.out.println("Text:         " + text);
        System.out.println("Length:       " + text.length());

        long startTime = System.nanoTime();
        int[] sa = buildSuffixArray(text);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;

        System.out.println("\nSuffix Array:");
        System.out.println(Arrays.toString(sa));

        System.out.println("\nSorted Suffixes Table:");
        System.out.print(formatSuffixes(text, sa, 20));

        System.out.printf("\nTime:         %.4f ms\n", elapsedMs);
        System.out.println("Complexity:   Time: O(n log^2 n) | Space: O(n)");
        System.out.println("============================================================");
    }
}
