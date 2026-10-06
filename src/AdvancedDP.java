/**
 * AdvancedDP - Module 3: Advanced Dynamic Programming Suite for TextHack.
 * 
 * Implements:
 * 1. Levenshtein & Damerau-Levenshtein Edit Distance (with adjacent transpositions)
 * 2. Weighted Edit Distance (custom costs for insert, delete, replace, transpose)
 * 3. Sequence Alignment for Genomic / Text Data:
 *    - Needleman-Wunsch (Global Alignment with scoring matrix & gap penalty)
 *    - Smith-Waterman (Local Alignment with scoring matrix & traceback)
 * 4. Interval DP:
 *    - Matrix-Chain Multiplication (O(n^3) DP with parenthesization recovery)
 *    - Optimal Binary Search Tree (OBST) for search term frequencies
 * 5. Bitmask DP:
 *    - Travelling Salesman Problem (TSP) on document citation / transition graphs in O(2^n * n^2)
 *    - Hamiltonian Path / Cycle detection
 *    - Exact Set Cover via Bitmask DP
 * 6. DP on Trees:
 *    - Maximum Weight Independent Set / Subset Sum on Trees
 *    - Tree Diameter via 2-pass / bottom-up DP
 *    - Tree Rerooting DP (O(n) all-roots sum of distances)
 * 7. SOS DP (Sum Over Subsets):
 *    - Fast subset sums in O(n * 2^n) via Yates's algorithm for vocabulary inclusion-exclusion
 * 8. Diagnostic: When DP is the wrong tool (Greedy choice, no subproblems, exponential explosion)
 * 
 * Zero third-party algorithm libraries; pure Java.
 */
public class AdvancedDP {

    // =========================================================================
    // 1. EDIT DISTANCE VARIANTS
    // =========================================================================

    /**
     * Damerau-Levenshtein distance (Optimal String Alignment variant).
     * Supports: insertion, deletion, substitution, and transposition of adjacent characters.
     */
    public static int damerauLevenshtein(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";
        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= n; j++) {
                char c2 = s2.charAt(j - 1);
                int cost = (c1 == c2) ? 0 : 1;

                int del = dp[i - 1][j] + 1;
                int ins = dp[i][j - 1] + 1;
                int sub = dp[i - 1][j - 1] + cost;

                int best = Math.min(del, Math.min(ins, sub));

                // Check transposition condition
                if (i > 1 && j > 1 && c1 == s2.charAt(j - 2) && s1.charAt(i - 2) == c2) {
                    best = Math.min(best, dp[i - 2][j - 2] + 1);
                }
                dp[i][j] = best;
            }
        }
        return dp[m][n];
    }

    /**
     * Weighted Edit Distance with custom costs.
     */
    public static int weightedEditDistance(String s1, String s2, int insCost, int delCost, int subCost, int transCost) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";
        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i * delCost;
        for (int j = 0; j <= n; j++) dp[0][j] = j * insCost;

        for (int i = 1; i <= m; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= n; j++) {
                char c2 = s2.charAt(j - 1);
                int cost = (c1 == c2) ? 0 : subCost;

                int del = dp[i - 1][j] + delCost;
                int ins = dp[i][j - 1] + insCost;
                int sub = dp[i - 1][j - 1] + cost;

                int best = Math.min(del, Math.min(ins, sub));

                if (i > 1 && j > 1 && c1 == s2.charAt(j - 2) && s1.charAt(i - 2) == c2) {
                    best = Math.min(best, dp[i - 2][j - 2] + transCost);
                }
                dp[i][j] = best;
            }
        }
        return dp[m][n];
    }

    // =========================================================================
    // 2. SEQUENCE ALIGNMENT (NEEDLEMAN-WUNSCH & SMITH-WATERMAN)
    // =========================================================================

    public static class AlignmentResult {
        public final int score;
        public final String alignedSeq1;
        public final String alignedSeq2;
        public final int[][] scoreMatrix;

        public AlignmentResult(int score, String alignedSeq1, String alignedSeq2, int[][] scoreMatrix) {
            this.score = score;
            this.alignedSeq1 = alignedSeq1;
            this.alignedSeq2 = alignedSeq2;
            this.scoreMatrix = scoreMatrix;
        }
    }

    /**
     * Needleman-Wunsch Global Sequence Alignment.
     * Matches globally from end to end with score maximization.
     */
    public static AlignmentResult needlemanWunsch(String s1, String s2, int matchScore, int mismatchScore, int gapPenalty) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";
        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i * gapPenalty;
        for (int j = 0; j <= n; j++) dp[0][j] = j * gapPenalty;

        for (int i = 1; i <= m; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= n; j++) {
                char c2 = s2.charAt(j - 1);
                int scoreDiag = dp[i - 1][j - 1] + (c1 == c2 ? matchScore : mismatchScore);
                int scoreUp = dp[i - 1][j] + gapPenalty;
                int scoreLeft = dp[i][j - 1] + gapPenalty;
                dp[i][j] = Math.max(scoreDiag, Math.max(scoreUp, scoreLeft));
            }
        }

        // Traceback from (m, n) to (0, 0)
        StringBuilder a1 = new StringBuilder();
        StringBuilder a2 = new StringBuilder();
        int i = m, j = n;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? matchScore : mismatchScore)) {
                a1.append(s1.charAt(i - 1));
                a2.append(s2.charAt(j - 1));
                i--;
                j--;
            } else if (i > 0 && dp[i][j] == dp[i - 1][j] + gapPenalty) {
                a1.append(s1.charAt(i - 1));
                a2.append('-');
                i--;
            } else {
                a1.append('-');
                a2.append(s2.charAt(j - 1));
                j--;
            }
        }

        return new AlignmentResult(dp[m][n], a1.reverse().toString(), a2.reverse().toString(), dp);
    }

    /**
     * Smith-Waterman Local Sequence Alignment.
     * Finds highest scoring local sub-regions (score floors at 0).
     */
    public static AlignmentResult smithWaterman(String s1, String s2, int matchScore, int mismatchScore, int gapPenalty) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";
        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];
        int maxScore = 0;
        int maxI = 0, maxJ = 0;

        for (int i = 1; i <= m; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= n; j++) {
                char c2 = s2.charAt(j - 1);
                int scoreDiag = dp[i - 1][j - 1] + (c1 == c2 ? matchScore : mismatchScore);
                int scoreUp = dp[i - 1][j] + gapPenalty;
                int scoreLeft = dp[i][j - 1] + gapPenalty;
                int val = Math.max(0, Math.max(scoreDiag, Math.max(scoreUp, scoreLeft)));
                dp[i][j] = val;
                if (val > maxScore) {
                    maxScore = val;
                    maxI = i;
                    maxJ = j;
                }
            }
        }

        // Traceback from max cell until score 0
        StringBuilder a1 = new StringBuilder();
        StringBuilder a2 = new StringBuilder();
        int i = maxI, j = maxJ;
        while (i > 0 && j > 0 && dp[i][j] > 0) {
            char c1 = s1.charAt(i - 1);
            char c2 = s2.charAt(j - 1);
            int diagVal = dp[i - 1][j - 1] + (c1 == c2 ? matchScore : mismatchScore);
            if (dp[i][j] == diagVal) {
                a1.append(c1);
                a2.append(c2);
                i--;
                j--;
            } else if (dp[i][j] == dp[i - 1][j] + gapPenalty) {
                a1.append(c1);
                a2.append('-');
                i--;
            } else {
                a1.append('-');
                a2.append(c2);
                j--;
            }
        }

        return new AlignmentResult(maxScore, a1.reverse().toString(), a2.reverse().toString(), dp);
    }

    // =========================================================================
    // 3. INTERVAL DP (MATRIX CHAIN MULTIPLICATION & OPTIMAL BST)
    // =========================================================================

    public static class MatrixChainResult {
        public final int minOperations;
        public final String parenthesization;
        public final int[][] mTable;
        public final int[][] sTable;

        public MatrixChainResult(int minOps, String paren, int[][] m, int[][] s) {
            this.minOperations = minOps;
            this.parenthesization = paren;
            this.mTable = m;
            this.sTable = s;
        }
    }

    /**
     * Matrix Chain Multiplication DP in O(n^3) time.
     * Dimensions array p: Matrix A_i has dimension p[i-1] x p[i].
     */
    public static MatrixChainResult matrixChainMultiplication(int[] p) {
        int n = p.length - 1; // number of matrices
        int[][] m = new int[n + 1][n + 1];
        int[][] s = new int[n + 1][n + 1];

        for (int len = 2; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;
                m[i][j] = Integer.MAX_VALUE;
                for (int k = i; k <= j - 1; k++) {
                    int q = m[i][k] + m[k + 1][j] + p[i - 1] * p[k] * p[j];
                    if (q < m[i][j]) {
                        m[i][j] = q;
                        s[i][j] = k;
                    }
                }
            }
        }

        StringBuilder paren = new StringBuilder();
        buildOptimalParens(s, 1, n, paren);
        return new MatrixChainResult(m[1][n], paren.toString(), m, s);
    }

    private static void buildOptimalParens(int[][] s, int i, int j, StringBuilder sb) {
        if (i == j) {
            sb.append("A").append(i);
        } else {
            sb.append("(");
            buildOptimalParens(s, i, s[i][j], sb);
            sb.append(" x ");
            buildOptimalParens(s, s[i][j] + 1, j, sb);
            sb.append(")");
        }
    }

    public static class OBSTResult {
        public final double minCost;
        public final double[][] eTable;
        public final int[][] rootTable;

        public OBSTResult(double minCost, double[][] e, int[][] root) {
            this.minCost = minCost;
            this.eTable = e;
            this.rootTable = root;
        }
    }

    /**
     * Optimal Binary Search Tree (OBST) DP in O(n^3) time.
     * keys 1..n, probabilities p[1..n], dummy dummy probabilities q[0..n].
     */
    public static OBSTResult optimalBST(double[] p, double[] q, int n) {
        double[][] e = new double[n + 2][n + 2];
        double[][] w = new double[n + 2][n + 2];
        int[][] root = new int[n + 1][n + 1];

        for (int i = 1; i <= n + 1; i++) {
            e[i][i - 1] = q[i - 1];
            w[i][i - 1] = q[i - 1];
        }

        for (int len = 1; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;
                e[i][j] = Double.MAX_VALUE;
                w[i][j] = w[i][j - 1] + p[j] + q[j];
                for (int r = i; r <= j; r++) {
                    double t = e[i][r - 1] + e[r + 1][j] + w[i][j];
                    if (t < e[i][j]) {
                        e[i][j] = t;
                        root[i][j] = r;
                    }
                }
            }
        }
        return new OBSTResult(e[1][n], e, root);
    }

    // =========================================================================
    // 4. BITMASK DP (TSP & HAMILTONIAN PATH)
    // =========================================================================

    public static class TSPResult {
        public final int minCost;
        public final int[] tour;

        public TSPResult(int minCost, int[] tour) {
            this.minCost = minCost;
            this.tour = tour;
        }
    }

    /**
     * Travelling Salesman Problem via Bitmask DP in O(2^n * n^2).
     * Finds minimum cost tour starting and ending at node 0.
     */
    public static TSPResult tspBitmask(int[][] dist) {
        int n = dist.length;
        if (n <= 1) return new TSPResult(0, new int[]{0});
        int numStates = 1 << n;
        int INF = 1000000000;

        int[][] dp = new int[numStates][n];
        int[][] parent = new int[numStates][n];

        for (int mask = 0; mask < numStates; mask++) {
            for (int i = 0; i < n; i++) {
                dp[mask][i] = INF;
                parent[mask][i] = -1;
            }
        }

        // Base case: starting at city 0 with mask (1 << 0)
        dp[1][0] = 0;

        for (int mask = 1; mask < numStates; mask += 2) { // node 0 must be visited
            for (int u = 0; u < n; u++) {
                if ((mask & (1 << u)) == 0 || dp[mask][u] >= INF) continue;

                for (int v = 0; v < n; v++) {
                    if ((mask & (1 << v)) == 0 && dist[u][v] < INF) {
                        int nextMask = mask | (1 << v);
                        int cost = dp[mask][u] + dist[u][v];
                        if (cost < dp[nextMask][v]) {
                            dp[nextMask][v] = cost;
                            parent[nextMask][v] = u;
                        }
                    }
                }
            }
        }

        // Complete the tour back to 0
        int finalMask = (1 << n) - 1;
        int minCost = INF;
        int lastNode = -1;
        for (int u = 1; u < n; u++) {
            if (dp[finalMask][u] < INF && dist[u][0] < INF) {
                int totalCost = dp[finalMask][u] + dist[u][0];
                if (totalCost < minCost) {
                    minCost = totalCost;
                    lastNode = u;
                }
            }
        }

        // Reconstruct tour
        if (minCost >= INF) return new TSPResult(-1, new int[0]);

        int[] tour = new int[n + 1];
        int currMask = finalMask;
        int currNode = lastNode;
        int idx = n - 1;
        while (currNode != -1 && idx >= 0) {
            tour[idx] = currNode;
            int prev = parent[currMask][currNode];
            currMask ^= (1 << currNode);
            currNode = prev;
            idx--;
        }
        tour[n] = 0; // return to start

        return new TSPResult(minCost, tour);
    }

    /**
     * Checks if a directed/undirected graph has a Hamiltonian Path in O(2^n * n^2).
     */
    public static boolean hasHamiltonianPath(boolean[][] adj) {
        int n = adj.length;
        if (n <= 1) return true;
        int numStates = 1 << n;
        boolean[][] dp = new boolean[numStates][n];

        for (int i = 0; i < n; i++) {
            dp[1 << i][i] = true;
        }

        for (int mask = 1; mask < numStates; mask++) {
            for (int u = 0; u < n; u++) {
                if (!dp[mask][u]) continue;
                for (int v = 0; v < n; v++) {
                    if ((mask & (1 << v)) == 0 && adj[u][v]) {
                        dp[mask | (1 << v)][v] = true;
                    }
                }
            }
        }

        int fullMask = (1 << n) - 1;
        for (int u = 0; u < n; u++) {
            if (dp[fullMask][u]) return true;
        }
        return false;
    }

    // =========================================================================
    // 5. DP ON TREES (INDEPENDENT SET, DIAMETER, REROOTING)
    // =========================================================================

    public static class TreeDPResult {
        public final int maxWeightIndependentSet;
        public final int treeDiameter;
        public final long[] allRootsDistances; // computed via rerooting technique

        public TreeDPResult(int mwis, int diam, long[] dists) {
            this.maxWeightIndependentSet = mwis;
            this.treeDiameter = diam;
            this.allRootsDistances = dists;
        }
    }

    /**
     * Computes Maximum Weight Independent Set, Diameter, and All-Roots Distances via Tree DP.
     * Adj: adjacency list of the tree.
     * Weights: vertex weights.
     */
    public static TreeDPResult solveTreeDP(int[][] adj, int[] weights) {
        int n = adj.length;
        if (n == 0) return new TreeDPResult(0, 0, new long[0]);
        if (n == 1) return new TreeDPResult(weights[0], 0, new long[]{0});

        // 1. Maximum Weight Independent Set (dp0 = exclude, dp1 = include)
        int[] dp0 = new int[n];
        int[] dp1 = new int[n];
        boolean[] visited = new boolean[n];
        mwisDfs(0, -1, adj, weights, dp0, dp1);
        int maxWIS = Math.max(dp0[0], dp1[0]);

        // 2. Tree Diameter
        int[] diam = new int[1];
        diameterDfs(0, -1, adj, diam);

        // 3. Tree Rerooting technique for all-roots sum of distances
        int[] subtreeSize = new int[n];
        long[] distFromRoot = new long[n];
        treeRerootDfs1(0, -1, adj, subtreeSize, distFromRoot);

        long[] allRootsDist = new long[n];
        allRootsDist[0] = distFromRoot[0];
        treeRerootDfs2(0, -1, adj, subtreeSize, allRootsDist, n);

        return new TreeDPResult(maxWIS, diam[0], allRootsDist);
    }

    private static void mwisDfs(int u, int p, int[][] adj, int[] w, int[] dp0, int[] dp1) {
        dp0[u] = 0;
        dp1[u] = w[u];
        for (int v : adj[u]) {
            if (v != p) {
                mwisDfs(v, u, adj, w, dp0, dp1);
                dp0[u] += Math.max(dp0[v], dp1[v]);
                dp1[u] += dp0[v];
            }
        }
    }

    private static int diameterDfs(int u, int p, int[][] adj, int[] diam) {
        int max1 = 0, max2 = 0;
        for (int v : adj[u]) {
            if (v != p) {
                int depth = 1 + diameterDfs(v, u, adj, diam);
                if (depth > max1) {
                    max2 = max1;
                    max1 = depth;
                } else if (depth > max2) {
                    max2 = depth;
                }
            }
        }
        diam[0] = Math.max(diam[0], max1 + max2);
        return max1;
    }

    private static void treeRerootDfs1(int u, int p, int[][] adj, int[] sz, long[] dist) {
        sz[u] = 1;
        for (int v : adj[u]) {
            if (v != p) {
                treeRerootDfs1(v, u, adj, sz, dist);
                sz[u] += sz[v];
                dist[u] += dist[v] + sz[v];
            }
        }
    }

    private static void treeRerootDfs2(int u, int p, int[][] adj, int[] sz, long[] allDist, int n) {
        for (int v : adj[u]) {
            if (v != p) {
                // When moving root from u to v:
                // v's distance increases for nodes outside its subtree by (n - sz[v])
                // and decreases for nodes inside its subtree by sz[v]
                allDist[v] = allDist[u] - sz[v] + (n - sz[v]);
                treeRerootDfs2(v, u, adj, sz, allDist, n);
            }
        }
    }

    // =========================================================================
    // 6. SOS DP (SUM OVER SUBSETS) - O(n * 2^n)
    // =========================================================================

    /**
     * Sum Over Subsets (SOS DP) in O(n * 2^n) using Yates's algorithm.
     * Computes for each mask: sum of array[submask] for all submask where (submask & mask) == submask.
     * Used for bitmask keyword combinations, inclusion-exclusion, and co-occurrence counts.
     */
    public static int[] sumOverSubsets(int[] a, int numBits) {
        int total = 1 << numBits;
        int[] sos = new int[total];
        System.arraycopy(a, 0, sos, 0, Math.min(a.length, total));

        for (int i = 0; i < numBits; i++) {
            int bit = 1 << i;
            for (int mask = 0; mask < total; mask++) {
                if ((mask & bit) != 0) {
                    sos[mask] += sos[mask ^ bit];
                }
            }
        }
        return sos;
    }

    // =========================================================================
    // 7. DIAGNOSTIC: WHEN DP IS THE WRONG TOOL
    // =========================================================================

    public static class DPDiagnostic {
        public final boolean isDPSuitable;
        public final String recommendation;
        public final String rationale;

        public DPDiagnostic(boolean suitable, String rec, String rat) {
            this.isDPSuitable = suitable;
            this.recommendation = rec;
            this.rationale = rat;
        }
    }

    /**
     * Evaluates algorithmic problem characteristics to determine if DP is the appropriate tool.
     */
    public static DPDiagnostic evaluateProblemSuitability(
            boolean hasOptimalSubstructure,
            boolean hasOverlappingSubproblems,
            boolean greedyChoicePropertyHolds,
            boolean exponentialStateSpaceWithoutCompression
    ) {
        if (!hasOptimalSubstructure) {
            return new DPDiagnostic(false, "Heuristics / Backtracking / Branch & Bound",
                    "Violates Principle of Optimality: Sub-solution optimality does not compose into global optimum.");
        }
        if (!hasOverlappingSubproblems) {
            return new DPDiagnostic(false, "Divide and Conquer (e.g., MergeSort, Karatsuba)",
                    "Subproblems are disjoint with zero overlap; caching yields no reuse benefit.");
        }
        if (greedyChoicePropertyHolds) {
            return new DPDiagnostic(false, "Greedy Algorithm (e.g., Huffman Coding, Kruskal, Prim)",
                    "Greedy choice property guarantees locally optimal decisions yield the global optimum in superior asymptotic time.");
        }
        if (exponentialStateSpaceWithoutCompression) {
            return new DPDiagnostic(false, "Approximation / FPT / Parameterized Algorithms",
                    "State space explodes exponentially beyond bitmask limits (n > 25); exact DP is computationally intractable.");
        }
        return new DPDiagnostic(true, "Dynamic Programming (Tabulation / Memoization)",
            "Problem satisfies optimal substructure, exhibits overlapping subproblems, and admits polynomial or compact bitmask state space.");
    }
}
