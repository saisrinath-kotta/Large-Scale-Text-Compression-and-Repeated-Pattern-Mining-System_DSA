import java.util.ArrayList;
import java.util.List;

/**
 * ZFunction - Linear-Time Z-Algorithm for Exact Pattern Matching.
 * 
 * Algorithm Concept:
 * Given a string S of length N, the Z-array stores at each index i the length of the
 * longest substring starting at S[i] that is also a prefix of S.
 * In other words: Z[i] is the greatest number such that S[0...Z[i]-1] == S[i...i+Z[i]-1].
 * 
 * To perform exact pattern matching:
 * 1. Form a concatenated string: S = Pattern + delimiter + Text
 *    (delimiter is a character not found in Pattern or Text, e.g. '\u0000' or '$').
 * 2. Compute the Z-array for S in O(N) = O(n + m) time using an active [L, R] window.
 * 3. Any index i in the Text section where Z[i] == pattern.length() marks an occurrence!
 * 
 * Time Complexity:  O(n + m)
 * Space Complexity: O(n + m) for the concatenated string and Z-array
 */
public class ZFunction {

    /**
     * Computes the Z-array for a given string S in O(N) linear time.
     * 
     * Why it works for Viva (The [L, R] Window / Z-box):
     * - We maintain an interval [L, R] which is the segment of S that matches a prefix of S
     *   and has the maximum right boundary R encountered so far.
     * - For each index i:
     *   1. If i > R: we have no prior information. We compare characters starting from S[i]
     *      with S[0] naively. If we find matches, we set [L, R] = [i, i + Z[i] - 1].
     *   2. If i <= R: index i falls inside the already-matched window [L, R].
     *      Let k = i - L. Because S[L...R] matches S[0...R-L], S[i] corresponds to S[k].
     *      - If Z[k] < R - i + 1: the match at k does not extend beyond R. Thus, Z[i] = Z[k].
     *      - If Z[k] >= R - i + 1: the match extends at least to R. We set L = i, and we only
     *        need to compare characters starting from R + 1 onward. We then update R.
     * Every character comparison that succeeds extends R, so at most 2N comparisons are made!
     * 
     * @param s Input string.
     * @return Z-array of length s.length().
     */
    public static int[] buildZArray(String s) {
        if (s == null || s.length() == 0) {
            return new int[0];
        }

        int n = s.length();
        int[] z = new int[n];
        z[0] = 0; // By convention, Z[0] = 0 for pattern matching purposes

        int l = 0; // Left boundary of active Z-box
        int r = 0; // Right boundary of active Z-box

        for (int i = 1; i < n; i++) {
            if (i > r) {
                // Case 1: i is outside the current Z-box
                l = i;
                r = i;
                while (r < n && s.charAt(r - l) == s.charAt(r)) {
                    r++;
                }
                z[i] = r - l;
                r--; // r was pointing to first mismatch, step back
            } else {
                // Case 2: i is inside [L, R]
                int k = i - l; // Corresponding index in the prefix

                // Subcase 2a: Value at k fits strictly inside the remaining window
                if (z[k] < r - i + 1) {
                    z[i] = z[k];
                } else {
                    // Subcase 2b: Value at k reaches or exceeds R
                    // Reset L to i and expand beyond R
                    l = i;
                    while (r < n && s.charAt(r - l) == s.charAt(r)) {
                        r++;
                    }
                    z[i] = r - l;
                    r--;
                }
            }
        }

        return z;
    }

    /**
     * Searches for all occurrences of pattern in text using the Z-algorithm.
     * 
     * @param text Corpus or string to search in.
     * @param pattern Pattern to locate.
     * @return List of 0-based starting indices where pattern matches in text.
     */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();

        if (text == null || pattern == null) {
            return matches;
        }

        int n = text.length();
        int m = pattern.length();

        // Edge case checks
        if (m == 0 || m > n) {
            return matches;
        }

        // Construct concatenated string: pattern + delimiter + text
        // Delimiter '\u0000' is a null character safe for text and Unicode
        char delimiter = '\u0000';
        String concat = pattern + delimiter + text;

        int[] z = buildZArray(concat);

        // Indices in 'concat':
        // 0 ... m - 1      : pattern
        // m                : delimiter
        // m + 1 ... end    : text
        int totalLen = concat.length();
        for (int i = m + 1; i < totalLen; i++) {
            if (z[i] == m) {
                // The pattern of length m matched starting at index i
                // Text index is i - (m + 1)
                matches.add(i - (m + 1));
            }
        }

        return matches;
    }

    /**
     * Formats the Z-array for visual demonstration in viva or CMD output.
     * @param s The string.
     * @param z The computed Z-array.
     * @return Formatted multi-line string.
     */
    public static String formatZ(String s, int[] z) {
        StringBuilder sb = new StringBuilder();
        sb.append("Index: ");
        for (int i = 0; i < s.length(); i++) {
            sb.append(String.format("%3d ", i));
        }
        sb.append("\nChar:  ");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\u0000') {
                sb.append(String.format("%3s ", "$"));
            } else {
                sb.append(String.format("%3c ", c));
            }
        }
        sb.append("\nZ:     ");
        for (int val : z) {
            sb.append(String.format("%3d ", val));
        }
        return sb.toString();
    }

    /**
     * Standalone main method to test ZFunction from CMD.
     */
    public static void main(String[] args) {
        String text = (args.length > 0) ? args[0] : "ABABABAB";
        String pattern = (args.length > 1) ? args[1] : "ABAB";

        System.out.println("============================================================");
        System.out.println("Z-FUNCTION PATTERN SEARCH DEMO");
        System.out.println("============================================================");
        System.out.println("Text:    " + text);
        System.out.println("Pattern: " + pattern);

        String sample = pattern + "$" + text;
        int[] zSample = buildZArray(sample);
        System.out.println("\nZ-Array on concatenated (pattern + $ + text):");
        System.out.println(formatZ(sample, zSample));

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
        System.out.println("Complexity:    Time: O(n + m) | Space: O(n + m)");
        System.out.println("============================================================");
    }
}
