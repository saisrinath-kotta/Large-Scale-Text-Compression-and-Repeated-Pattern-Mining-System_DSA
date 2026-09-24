import java.util.ArrayList;
import java.util.List;

/**
 * KMP - Knuth-Morris-Pratt Exact Pattern Matching Algorithm.
 * 
 * Algorithm Concept:
 * The KMP algorithm searches for occurrences of a pattern P within text T in linear time.
 * Instead of restarting comparisons from the beginning of the pattern upon a mismatch
 * (which takes O(n * m) worst-case in naive search), KMP uses the Longest Proper Prefix
 * which is also a Suffix (LPS) array to skip redundant comparisons.
 * 
 * Preprocessing Time:  O(m) where m = pattern length
 * Search Time:         O(n) where n = text length
 * Overall Time:        O(n + m)
 * Space Complexity:    O(m) for the LPS array
 */
public class KMP {

    /**
     * Builds the LPS (Longest Proper Prefix which is also a Suffix) array.
     * 
     * Definition:
     * lps[i] stores the length of the longest proper prefix of pattern[0...i]
     * that is also a suffix of pattern[0...i].
     * 
     * Why it works for Viva:
     * When a mismatch occurs at pattern index j, the characters pattern[0...j-1]
     * have already matched text[i-j...i-1]. The value lps[j-1] gives us the length
     * of the longest prefix that matches the end of this matched segment.
     * Therefore, we can slide the pattern forward and resume matching from index lps[j-1],
     * without ever moving the text pointer i backward!
     * 
     * @param pattern The search pattern string.
     * @return An integer array representing the LPS table.
     */
    public static int[] buildLPS(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            return new int[0];
        }

        int m = pattern.length();
        int[] lps = new int[m];

        // Length of the previous longest prefix suffix
        int len = 0;
        lps[0] = 0; // Base case: a single character has no proper prefix

        int i = 1;
        while (i < m) {
            // Case 1: Current characters match
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                // Case 2: Mismatch between pattern.charAt(i) and pattern.charAt(len)
                if (len != 0) {
                    // Fall back to the previous known prefix suffix length
                    // LPS tells us how much of the pattern prefix is still valid
                    // so we do NOT restart from zero.
                    len = lps[len - 1];
                    // Note: i is NOT incremented here; we re-test pattern.charAt(i) with the shorter prefix
                } else {
                    // No prefix matches; LPS value is 0
                    lps[i] = 0;
                    i++;
                }
            }
        }

        return lps;
    }

    /**
     * Searches for all occurrences of pattern in text using the KMP algorithm.
     * 
     * @param text The corpus or text string to search within.
     * @param pattern The pattern string to find.
     * @return A list of 0-based starting indices where the pattern occurs.
     */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();

        if (text == null || pattern == null) {
            return matches;
        }

        int n = text.length();
        int m = pattern.length();

        // Edge case: Empty pattern or pattern longer than text
        if (m == 0 || m > n) {
            return matches;
        }

        // Step 1: Precompute the LPS array in O(m) time
        int[] lps = buildLPS(pattern);

        int i = 0; // Pointer in text (NEVER moves backward)
        int j = 0; // Pointer in pattern

        // Step 2: Scan through the text in O(n) time
        while (i < n) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
            }

            // A full match is found when j reaches pattern length m
            if (j == m) {
                matches.add(i - j); // Starting index in text

                // To find subsequent / overlapping matches, use LPS to shift j
                j = lps[j - 1];
            } else if (i < n && text.charAt(i) != pattern.charAt(j)) {
                // Mismatch after j matches
                if (j != 0) {
                    // Shift pattern pointer using LPS array without moving text pointer i
                    j = lps[j - 1];
                } else {
                    // If no characters matched, advance text pointer
                    i++;
                }
            }
        }

        return matches;
    }

    /**
     * Formats the LPS array for visual demonstration in viva or CMD output.
     * @param pattern The pattern string.
     * @param lps The computed LPS array.
     * @return A formatted multi-line string.
     */
    public static String formatLPS(String pattern, int[] lps) {
        StringBuilder sb = new StringBuilder();
        sb.append("Index:   ");
        for (int i = 0; i < pattern.length(); i++) {
            sb.append(String.format("%3d ", i));
        }
        sb.append("\nChar:    ");
        for (int i = 0; i < pattern.length(); i++) {
            sb.append(String.format("%3c ", pattern.charAt(i)));
        }
        sb.append("\nLPS:     ");
        for (int val : lps) {
            sb.append(String.format("%3d ", val));
        }
        return sb.toString();
    }

    /**
     * Standalone main method to test KMP from CMD.
     */
    public static void main(String[] args) {
        String text = (args.length > 0) ? args[0] : "ABABABAB";
        String pattern = (args.length > 1) ? args[1] : "ABAB";

        System.out.println("============================================================");
        System.out.println("KMP PATTERN SEARCH DEMO");
        System.out.println("============================================================");
        System.out.println("Text:    " + text);
        System.out.println("Pattern: " + pattern);

        int[] lps = buildLPS(pattern);
        System.out.println("\nLPS Array:");
        System.out.println(formatLPS(pattern, lps));

        long startTime = System.nanoTime();
        List<Integer> matches = search(text, pattern);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;

        System.out.println("\nMatches found: " + matches.size());
        System.out.print("Positions:     ");
        if (matches.isEmpty()) {
            System.out.println("None");
        } else {
            System.out.println(matches);
        }

        System.out.printf("Time:          %.4f ms\n", elapsedMs);
        System.out.println("Complexity:    Time: O(n + m) | Space: O(m)");
        System.out.println("============================================================");
    }
}
