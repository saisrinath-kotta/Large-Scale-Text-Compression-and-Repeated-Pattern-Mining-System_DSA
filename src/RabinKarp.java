import java.util.ArrayList;
import java.util.List;

/**
 * RabinKarp - Polynomial Rolling Hash Pattern Matching with Double Hashing.
 * 
 * Algorithm Concept:
 * Rabin-Karp treats a string of length m as a number in a polynomial base B,
 * computed modulo a large prime M.
 * When sliding a window of length m across text of length n, the hash of the
 * new window is computed from the previous window in O(1) time by:
 * 1. Subtracting the contribution of the outgoing character.
 * 2. Multiplying by the base B.
 * 3. Adding the contribution of the incoming character.
 * 
 * Double Hashing & Collision Handling (Critical for Viva):
 * 1. Why double hashing?
 *    Using two independent hash functions with distinct large primes (M1 and M2)
 *    drastically reduces the probability of a hash collision from 1/M to 1/(M1 * M2).
 * 2. Why character verification?
 *    Even with double hashing, two different substrings might theoretically yield
 *    identical hash values (hash collision). To guarantee zero false positives,
 *    whenever hash equality is detected, we perform an exact character-by-character
 *    verification of the window against the pattern.
 * 
 * Average Time Complexity:  O(n + m)
 * Worst-Case Time:          O(n * m) (only if hash collisions occur constantly)
 * Space Complexity:         O(1)
 */
public class RabinKarp {

    // Base for polynomial rolling hash (large enough to accommodate Unicode characters)
    private static final long BASE = 10007L;

    // Two distinct large 64-bit primes for double hashing
    private static final long MOD1 = 1_000_000_007L;
    private static final long MOD2 = 1_000_000_009L;

    /**
     * Searches for all occurrences of pattern in text using Rabin-Karp double rolling hash.
     * 
     * @param text Corpus or string to search.
     * @param pattern Pattern to find.
     * @return List of 0-based starting indices where pattern occurs.
     */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();

        if (text == null || pattern == null) {
            return matches;
        }

        int n = text.length();
        int m = pattern.length();

        // Edge cases: empty pattern or pattern longer than text
        if (m == 0 || m > n) {
            return matches;
        }

        // Step 1: Precompute BASE^(m-1) % MOD for both hash functions
        // This represents the multiplier for the highest-order character in a window of size m
        long power1 = 1;
        long power2 = 1;
        for (int i = 0; i < m - 1; i++) {
            power1 = (power1 * BASE) % MOD1;
            power2 = (power2 * BASE) % MOD2;
        }

        // Step 2: Compute initial hash values for pattern and first text window of size m
        long patHash1 = 0;
        long patHash2 = 0;
        long textHash1 = 0;
        long textHash2 = 0;

        for (int i = 0; i < m; i++) {
            char pChar = pattern.charAt(i);
            char tChar = text.charAt(i);

            patHash1 = (patHash1 * BASE + pChar) % MOD1;
            patHash2 = (patHash2 * BASE + pChar) % MOD2;

            textHash1 = (textHash1 * BASE + tChar) % MOD1;
            textHash2 = (textHash2 * BASE + tChar) % MOD2;
        }

        // Step 3: Slide the window across text from index 0 to n - m
        for (int i = 0; i <= n - m; i++) {
            // Check if both hash values match
            if (patHash1 == textHash1 && patHash2 == textHash2) {
                // HASH EQUALITY DETECTED:
                // Perform exact character verification to eliminate any false positive collisions!
                if (verifyMatch(text, i, pattern)) {
                    matches.add(i);
                }
            }

            // Compute hash for the next window: remove text[i], add text[i + m]
            if (i < n - m) {
                char outChar = text.charAt(i);
                char inChar = text.charAt(i + m);

                // Rolling hash update for MOD1:
                // textHash1 = ((textHash1 - outChar * power1) * BASE + inChar) % MOD1
                long term1 = (outChar * power1) % MOD1;
                textHash1 = (textHash1 - term1 + MOD1) % MOD1; // Ensure non-negative before multiply
                textHash1 = (textHash1 * BASE + inChar) % MOD1;

                // Rolling hash update for MOD2:
                long term2 = (outChar * power2) % MOD2;
                textHash2 = (textHash2 - term2 + MOD2) % MOD2;
                textHash2 = (textHash2 * BASE + inChar) % MOD2;
            }
        }

        return matches;
    }

    /**
     * Exact character-by-character verification of pattern against text[offset...offset+m-1].
     */
    private static boolean verifyMatch(String text, int offset, String pattern) {
        int m = pattern.length();
        for (int j = 0; j < m; j++) {
            if (text.charAt(offset + j) != pattern.charAt(j)) {
                return false; // Collision detected and rejected!
            }
        }
        return true;
    }

    /**
     * Standalone main method to test RabinKarp from CMD.
     */
    public static void main(String[] args) {
        String text = (args.length > 0) ? args[0] : "ABABABAB";
        String pattern = (args.length > 1) ? args[1] : "ABAB";

        System.out.println("============================================================");
        System.out.println("RABIN-KARP PATTERN SEARCH DEMO");
        System.out.println("============================================================");
        System.out.println("Text:               " + text);
        System.out.println("Pattern:            " + pattern);
        System.out.println("Double Hashing:     Enabled (MOD1=10^9+7, MOD2=10^9+9)");
        System.out.println("Hash Verification:  Enabled (Character-by-character)");

        long startTime = System.nanoTime();
        List<Integer> matches = search(text, pattern);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;

        System.out.println("\nMatches found:      " + matches.size());
        System.out.print("Positions:          ");
        if (matches.isEmpty()) {
            System.out.println("None");
        } else {
            System.out.println(matches);
        }

        System.out.printf("Time:               %.4f ms\n", elapsedMs);
        System.out.println("Average Complexity: Time: O(n + m) | Space: O(1)");
        System.out.println("============================================================");
    }
}
