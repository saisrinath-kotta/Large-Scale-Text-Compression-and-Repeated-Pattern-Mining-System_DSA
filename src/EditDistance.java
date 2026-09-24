import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * EditDistance - Wagner-Fischer Dynamic Programming Algorithm for Levenshtein Edit Distance.
 * 
 * Algorithm Concept:
 * The edit distance between two strings s1 and s2 is the minimum number of single-character
 * operations (insertions, deletions, or substitutions) needed to transform s1 into s2.
 * 
 * Dynamic Programming Recurrence (Viva Explanation):
 * Let dp[i][j] be the edit distance between prefix s1[0...i-1] and prefix s2[0...j-1].
 * 
 * 1. Base Cases:
 *    - dp[i][0] = i: transforming s1[0...i-1] to empty string requires i deletions.
 *    - dp[0][j] = j: transforming empty string to s2[0...j-1] requires j insertions.
 * 
 * 2. Recurrence:
 *    For i > 0 and j > 0:
 *    - If s1.charAt(i - 1) == s2.charAt(j - 1):
 *         dp[i][j] = dp[i - 1][j - 1] (characters match, zero cost)
 *    - If characters differ:
 *         dp[i][j] = 1 + min(
 *             dp[i - 1][j],      // Deletion from s1
 *             dp[i][j - 1],      // Insertion into s1
 *             dp[i - 1][j - 1]   // Substitution / Replacement
 *         )
 * 
 * Role in TextHack:
 * Used for Fuzzy Matching, Near-Duplicate Detection, and Spell-Tolerant Search
 * across Indian-language Wikipedia text and multilingual corpora.
 * 
 * Time Complexity:   O(m * n) where m = s1.length(), n = s2.length()
 * Space Complexity:  O(m * n) for the 2D DP matrix
 */
public class EditDistance {

    /**
     * Computes the Levenshtein edit distance between two strings using Wagner-Fischer DP.
     * 
     * @param s1 First string.
     * @param s2 Second string.
     * @return The minimum edit distance.
     */
    public static int compute(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return s2.length();
        if (s2 == null) return s1.length();

        int m = s1.length();
        int n = s2.length();

        int[][] dp = computeTable(s1, s2);
        return dp[m][n];
    }

    /**
     * Builds and returns the complete 2D Wagner-Fischer DP table.
     * Useful for visual inspection, tracebacks, and viva demonstration.
     * 
     * @param s1 First string.
     * @param s2 Second string.
     * @return A 2D integer matrix of size (m + 1) x (n + 1).
     */
    public static int[][] computeTable(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";

        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        // Base cases
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i; // i deletions
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j; // j insertions
        }

        // Fill the DP table row by row
        for (int i = 1; i <= m; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= n; j++) {
                char c2 = s2.charAt(j - 1);

                if (c1 == c2) {
                    // Match: no operation needed
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int deletion = dp[i - 1][j];
                    int insertion = dp[i][j - 1];
                    int substitution = dp[i - 1][j - 1];

                    // Take the minimum of the three operations plus cost 1
                    int minOp = Math.min(deletion, Math.min(insertion, substitution));
                    dp[i][j] = 1 + minOp;
                }
            }
        }

        return dp;
    }

    /**
     * Formats the 2D DP matrix for display in the CMD terminal or viva demo.
     * 
     * @param s1 First string (row headers).
     * @param s2 Second string (column headers).
     * @param dp The precomputed (m+1) x (n+1) DP table.
     * @return Formatted multi-line grid string.
     */
    public static String formatDPTable(String s1, String s2, int[][] dp) {
        StringBuilder sb = new StringBuilder();
        int m = s1.length();
        int n = s2.length();

        // Header row with s2 characters
        sb.append("       # ");
        for (int j = 0; j < n; j++) {
            sb.append(String.format("  %c ", s2.charAt(j)));
        }
        sb.append("\n");

        sb.append("   +");
        for (int j = 0; j <= n; j++) {
            sb.append("----");
        }
        sb.append("\n");

        for (int i = 0; i <= m; i++) {
            char rowLabel = (i == 0) ? '#' : s1.charAt(i - 1);
            sb.append(String.format(" %c |", rowLabel));
            for (int j = 0; j <= n; j++) {
                sb.append(String.format("%3d ", dp[i][j]));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Performs simple fuzzy matching across corpus words using Wagner-Fischer edit distance.
     * 
     * @param corpus Full corpus text.
     * @param query Search query word.
     * @param maxDistance Maximum permissible edit distance.
     * @return List of unique matching candidate words found in the corpus.
     */
    public static List<String> fuzzySearch(String corpus, String query, int maxDistance) {
        Set<String> matches = new LinkedHashSet<>();
        if (corpus == null || query == null || query.isEmpty()) {
            return new ArrayList<>(matches);
        }

        // Split corpus into words/tokens by whitespace and common punctuation
        String[] tokens = corpus.split("[\\s,;:.।!?()\\[\\]{}\"']+");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) continue;

            // Length difference pruning: if abs(len(token) - len(query)) > maxDistance, skip
            if (Math.abs(trimmed.length() - query.length()) > maxDistance) {
                continue;
            }

            int dist = compute(trimmed, query);
            if (dist <= maxDistance) {
                matches.add(trimmed + " (dist=" + dist + ")");
            }
        }

        return new ArrayList<>(matches);
    }

    /**
     * Standalone main method to test EditDistance from CMD.
     */
    public static void main(String[] args) {
        String s1 = (args.length > 0) ? args[0] : "kitten";
        String s2 = (args.length > 1) ? args[1] : "sitting";

        System.out.println("============================================================");
        System.out.println("WAGNER-FISCHER EDIT DISTANCE DEMO");
        System.out.println("============================================================");
        System.out.println("String 1: " + s1);
        System.out.println("String 2: " + s2);

        int[][] dp = computeTable(s1, s2);
        int distance = dp[s1.length()][s2.length()];

        // Display DP matrix for strings up to length 20
        if (s1.length() <= 20 && s2.length() <= 20) {
            System.out.println("\nDynamic Programming 2D Table:");
            System.out.println(formatDPTable(s1, s2, dp));
        }

        System.out.println("Edit Distance:       " + distance);
        System.out.println("Operations Allowed:  Insertion, Deletion, Substitution");
        System.out.println("Time Complexity:     O(m * n)");
        System.out.println("Space Complexity:    O(m * n)");
        System.out.println("============================================================");
    }
}
