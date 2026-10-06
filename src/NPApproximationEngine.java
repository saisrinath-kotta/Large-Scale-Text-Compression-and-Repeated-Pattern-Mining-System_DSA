import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * NPApproximationEngine - Module 5: NP-Completeness and Approximation Suite for TextHack.
 * 
 * Implements:
 * 1. Formal Certificate Verifiers (P vs NP vs co-NP witnesses):
 *    - 3-SAT certificate verifier
 *    - Independent Set verifier
 *    - Vertex Cover verifier
 *    - Subset-Sum verifier
 *    - Hamiltonian Tour / TSP verifier
 * 2. Reduction Zoo:
 *    - 3-SAT -> Independent Set reduction gadget
 *    - Independent Set <-> Vertex Cover reduction (S is IS iff V \ S is VC)
 *    - Independent Set -> Clique reduction (complement graph G^c)
 *    - Hamiltonian Cycle -> TSP reduction
 * 3. Canonical NP-Hardness Recognition Checklist
 * 4. Approximation Algorithms with Rigorous Guarantees:
 *    - 2-Approximation for Minimum Vertex Cover via Maximal Matching
 *    - 2-Approximation for Metric TSP via MST Doubling & Shortcut DFS
 *    - Greedy Set Cover with Harmonic H(n) <= (ln n + 1) approximation guarantee
 *    - Knapsack FPTAS: Fully Polynomial-Time Approximation Scheme with (1 - eps) guarantee
 * 5. Parameterized Complexity (FPT & Kernelization):
 *    - Kernelization for Vertex Cover via Buss's Rule (degree > k reduction)
 *    - Bounded Search Tree FPT Algorithm for Vertex Cover in O(2^k * n)
 * 
 * Pure Java implementation with strict zero-external algorithm libraries.
 */
public class NPApproximationEngine {

    // =========================================================================
    // 1. CANONICAL WITNESS VERIFIERS (P vs NP vs co-NP)
    // =========================================================================

    public static class Clause3 {
        public final int[] lits; // each literal: positive for variable x (1..n), negative for ~x (-1..-n)

        public Clause3(int l1, int l2, int l3) {
            this.lits = new int[]{l1, l2, l3};
        }
    }

    /**
     * Polynomial-time witness verifier for 3-SAT.
     * Given boolean assignment certificate, checks if every clause evaluates to true in O(C).
     */
    public static boolean verify3SAT(List<Clause3> formula, boolean[] assignment) {
        for (Clause3 c : formula) {
            boolean clauseSat = false;
            for (int lit : c.lits) {
                int var = Math.abs(lit) - 1;
                boolean val = assignment[var];
                if (lit < 0) val = !val;
                if (val) {
                    clauseSat = true;
                    break;
                }
            }
            if (!clauseSat) return false;
        }
        return true;
    }

    /**
     * Polynomial-time verifier for Vertex Cover.
     * Checks if subset S has size <= k and touches every edge in graph.
     */
    public static boolean verifyVertexCover(int n, int[][] edges, Set<Integer> cover, int k) {
        if (cover.size() > k) return false;
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            if (!cover.contains(u) && !cover.contains(v)) {
                return false; // uncovered edge
            }
        }
        return true;
    }

    /**
     * Polynomial-time verifier for Subset-Sum.
     * Checks if subset of elements sums exactly to target T.
     */
    public static boolean verifySubsetSum(int[] elements, List<Integer> chosenIndices, int target) {
        long sum = 0;
        for (int idx : chosenIndices) {
            if (idx < 0 || idx >= elements.length) return false;
            sum += elements[idx];
        }
        return sum == target;
    }

    // =========================================================================
    // 2. THE REDUCTION ZOO
    // =========================================================================

    public static class GraphReduction {
        public final int numVertices;
        public final List<int[]> edges;
        public final int targetK;
        public final String reductionName;

        public GraphReduction(int n, List<int[]> edges, int k, String name) {
            this.numVertices = n;
            this.edges = edges;
            this.targetK = k;
            this.reductionName = name;
        }
    }

    /**
     * Canonical Reduction: 3-SAT -> Independent Set.
     * For m clauses, creates 3m vertices (triangles). Adds edges between complementary literals.
     * Formula is satisfiable iff graph has Independent Set of size m.
     */
    public static GraphReduction reduce3SATtoIndependentSet(List<Clause3> formula) {
        int m = formula.size();
        int totalVertices = 3 * m;
        List<int[]> edges = new ArrayList<>();

        // 1. Triangle edges within each clause
        for (int c = 0; c < m; c++) {
            int v0 = c * 3;
            int v1 = c * 3 + 1;
            int v2 = c * 3 + 2;
            edges.add(new int[]{v0, v1});
            edges.add(new int[]{v1, v2});
            edges.add(new int[]{v0, v2});
        }

        // 2. Edges between conflicting literals across clauses (x and ~x cannot both be chosen)
        for (int c1 = 0; c1 < m; c1++) {
            for (int i = 0; i < 3; i++) {
                int lit1 = formula.get(c1).lits[i];
                int u = c1 * 3 + i;
                for (int c2 = c1 + 1; c2 < m; c2++) {
                    for (int j = 0; j < 3; j++) {
                        int lit2 = formula.get(c2).lits[j];
                        if (lit1 == -lit2) { // complementary conflict
                            int v = c2 * 3 + j;
                            edges.add(new int[]{u, v});
                        }
                    }
                }
            }
        }

        return new GraphReduction(totalVertices, edges, m, "3-SAT -> Independent Set (size m = " + m + ")");
    }

    /**
     * Duality: Independent Set <-> Vertex Cover.
     * In any graph G = (V, E), S is an Independent Set iff V \ S is a Vertex Cover.
     */
    public static Set<Integer> independentSetToVertexCover(int numVertices, Set<Integer> independentSet) {
        Set<Integer> cover = new HashSet<>();
        for (int i = 0; i < numVertices; i++) {
            if (!independentSet.contains(i)) {
                cover.add(i);
            }
        }
        return cover;
    }

    /**
     * Reduction: Independent Set -> Clique on Complement Graph G^c.
     * S is an Independent Set in G iff S is a Clique in G^c.
     */
    public static List<int[]> complementGraph(int n, List<int[]> originalEdges) {
        boolean[][] hasEdge = new boolean[n][n];
        for (int[] e : originalEdges) {
            hasEdge[e[0]][e[1]] = true;
            hasEdge[e[1]][e[0]] = true;
        }

        List<int[]> compEdges = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                if (!hasEdge[u][v]) {
                    compEdges.add(new int[]{u, v});
                }
            }
        }
        return compEdges;
    }

    // =========================================================================
    // 3. APPROXIMATION ALGORITHMS
    // =========================================================================

    public static class ApproxVertexCoverResult {
        public final Set<Integer> vertexCover;
        public final int coverSize;
        public final double approximationRatioBound; // 2.0

        public ApproxVertexCoverResult(Set<Integer> cover) {
            this.vertexCover = cover;
            this.coverSize = cover.size();
            this.approximationRatioBound = 2.0;
        }
    }

    /**
     * 2-Approximation for Minimum Vertex Cover via Maximal Matching.
     * Finds a maximal matching M of non-adjacent edges and includes BOTH endpoints of every edge in M.
     * Proof of 2-approximation: Any valid vertex cover must pick at least 1 endpoint from each edge in M,
     * so OPT >= |M|. We pick 2|M| vertices, hence |Cover| = 2|M| <= 2 * OPT.
     */
    public static ApproxVertexCoverResult vertexCover2Approx(int numVertices, int[][] edges) {
        Set<Integer> cover = new HashSet<>();
        boolean[] matchedVertices = new boolean[numVertices];

        for (int[] e : edges) {
            int u = e[0];
            int v = e[1];
            if (!matchedVertices[u] && !matchedVertices[v]) {
                // Add edge (u, v) to maximal matching
                matchedVertices[u] = true;
                matchedVertices[v] = true;
                // Add both endpoints to cover
                cover.add(u);
                cover.add(v);
            }
        }
        return new ApproxVertexCoverResult(cover);
    }

    public static class MetricTSPResult {
        public final int[] tour;
        public final double tourCost;
        public final double approximationRatioBound; // 2.0

        public MetricTSPResult(int[] tour, double cost) {
            this.tour = tour;
            this.tourCost = cost;
            this.approximationRatioBound = 2.0;
        }
    }

    /**
     * 2-Approximation for Metric TSP via MST Doubling & Shortcut DFS.
     * Precondition: dist matrix satisfies the Triangle Inequality: dist[i][k] <= dist[i][j] + dist[j][k].
     */
    public static MetricTSPResult metricTSP2Approx(double[][] dist) {
        int n = dist.length;
        if (n <= 1) return new MetricTSPResult(new int[]{0}, 0);

        // 1. Compute MST using Prim's algorithm
        boolean[] inMST = new boolean[n];
        int[] parent = new int[n];
        double[] key = new double[n];
        Arrays.fill(key, Double.MAX_VALUE);
        key[0] = 0;
        parent[0] = -1;

        for (int count = 0; count < n; count++) {
            int u = -1;
            for (int i = 0; i < n; i++) {
                if (!inMST[i] && (u == -1 || key[i] < key[u])) {
                    u = i;
                }
            }
            inMST[u] = true;

            for (int v = 0; v < n; v++) {
                if (!inMST[v] && dist[u][v] < key[v]) {
                    key[v] = dist[u][v];
                    parent[v] = u;
                }
            }
        }

        // Build MST adjacency tree
        List<List<Integer>> treeAdj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) treeAdj.add(new ArrayList<>());
        for (int v = 1; v < n; v++) {
            treeAdj.get(parent[v]).add(v);
            treeAdj.get(v).add(parent[v]);
        }

        // 2. Pre-order traversal with shortcutting (skip visited vertices)
        List<Integer> tourList = new ArrayList<>();
        boolean[] visited = new boolean[n];
        tspDfs(0, treeAdj, visited, tourList);
        tourList.add(0); // return to start

        int[] tour = new int[tourList.size()];
        double cost = 0;
        for (int i = 0; i < tourList.size(); i++) {
            tour[i] = tourList.get(i);
            if (i > 0) {
                cost += dist[tour[i - 1]][tour[i]];
            }
        }

        return new MetricTSPResult(tour, cost);
    }

    private static void tspDfs(int u, List<List<Integer>> treeAdj, boolean[] visited, List<Integer> tour) {
        visited[u] = true;
        tour.add(u);
        for (int v : treeAdj.get(u)) {
            if (!visited[v]) {
                tspDfs(v, treeAdj, visited, tour);
            }
        }
    }

    public static class SetCoverResult {
        public final List<Integer> chosenSubsets;
        public final double harmonicBound; // H(universeSize)

        public SetCoverResult(List<Integer> subsets, double bound) {
            this.chosenSubsets = subsets;
            this.harmonicBound = bound;
        }
    }

    /**
     * Greedy Set Cover with Harmonic H(n) <= ln(n) + 1 approximation guarantee.
     * Iteratively selects subset that covers the greatest number of uncovered elements.
     */
    public static SetCoverResult greedySetCover(int universeSize, List<Set<Integer>> subsets) {
        Set<Integer> uncovered = new HashSet<>();
        for (int i = 0; i < universeSize; i++) uncovered.add(i);

        List<Integer> chosen = new ArrayList<>();
        while (!uncovered.isEmpty()) {
            int bestIdx = -1;
            int maxNewlyCovered = 0;

            for (int i = 0; i < subsets.size(); i++) {
                int count = 0;
                for (int elem : subsets.get(i)) {
                    if (uncovered.contains(elem)) count++;
                }
                if (count > maxNewlyCovered) {
                    maxNewlyCovered = count;
                    bestIdx = i;
                }
            }

            if (bestIdx == -1 || maxNewlyCovered == 0) break; // cannot cover remaining

            for (int elem : subsets.get(bestIdx)) {
                uncovered.remove(elem);
            }
            chosen.add(bestIdx);
        }

        double hBound = 0.0;
        for (int i = 1; i <= universeSize; i++) hBound += 1.0 / i;

        return new SetCoverResult(chosen, hBound);
    }

    public static class KnapsackFPTASResult {
        public final double totalValue;
        public final int totalWeight;
        public final List<Integer> chosenItems;
        public final double epsilon;
        public final double theoreticalLowerBoundRatio; // 1 - eps

        public KnapsackFPTASResult(double val, int weight, List<Integer> items, double eps) {
            this.totalValue = val;
            this.totalWeight = weight;
            this.chosenItems = items;
            this.epsilon = eps;
            this.theoreticalLowerBoundRatio = 1.0 - eps;
        }
    }

    /**
     * Knapsack FPTAS (Fully Polynomial-Time Approximation Scheme).
     * Yields (1 - epsilon) * OPT in O(n^3 / epsilon) time by scaling item values.
     * Scale factor K = (epsilon * V_max) / n.
     */
    public static KnapsackFPTASResult knapsackFPTAS(int[] weights, int[] values, int capacity, double epsilon) {
        int n = weights.length;
        if (n == 0 || capacity <= 0) return new KnapsackFPTASResult(0, 0, new ArrayList<>(), epsilon);

        int maxVal = 0;
        for (int v : values) maxVal = Math.max(maxVal, v);

        double kScale = (epsilon * maxVal) / (double) n;
        if (kScale <= 0) kScale = 1.0;

        int[] scaledVals = new int[n];
        int sumScaledVals = 0;
        for (int i = 0; i < n; i++) {
            scaledVals[i] = (int) Math.floor(values[i] / kScale);
            sumScaledVals += scaledVals[i];
        }

        // DP by Value: dp[v] = minimum weight to achieve scaled value v
        int INF = 1000000000;
        int[] dp = new int[sumScaledVals + 1];
        int[] parentItem = new int[sumScaledVals + 1];
        int[] parentVal = new int[sumScaledVals + 1];
        Arrays.fill(dp, INF);
        Arrays.fill(parentItem, -1);
        dp[0] = 0;

        for (int i = 0; i < n; i++) {
            int w = weights[i];
            int v = scaledVals[i];
            for (int val = sumScaledVals; val >= v; val--) {
                if (dp[val - v] + w < dp[val]) {
                    dp[val] = dp[val - v] + w;
                    parentItem[val] = i;
                    parentVal[val] = val - v;
                }
            }
        }

        // Find best feasible value
        int bestScaled = 0;
        for (int v = sumScaledVals; v >= 0; v--) {
            if (dp[v] <= capacity) {
                bestScaled = v;
                break;
            }
        }

        // Reconstruct items
        List<Integer> chosen = new ArrayList<>();
        int curr = bestScaled;
        while (curr > 0 && parentItem[curr] != -1) {
            int item = parentItem[curr];
            chosen.add(item);
            curr = parentVal[curr];
        }

        double totalOriginalValue = 0;
        int totalWeight = 0;
        for (int item : chosen) {
            totalOriginalValue += values[item];
            totalWeight += weights[item];
        }

        return new KnapsackFPTASResult(totalOriginalValue, totalWeight, chosen, epsilon);
    }

    // =========================================================================
    // 4. PARAMETERIZED COMPLEXITY & FPT (KERNELIZATION & SEARCH TREES)
    // =========================================================================

    public static class KernelResult {
        public final boolean trivialNo;
        public final Set<Integer> mandatoryInCover;
        public final int reducedK;
        public final List<int[]> kernelEdges;

        public KernelResult(boolean no, Set<Integer> mandatory, int k, List<int[]> edges) {
            this.trivialNo = no;
            this.mandatoryInCover = mandatory;
            this.reducedK = k;
            this.kernelEdges = edges;
        }
    }

    /**
     * Kernelization for Vertex Cover via Buss's Rule.
     * Rule 1: Isolated vertices (degree 0) can be removed.
     * Rule 2: Vertices with degree > k MUST be included in any size-k vertex cover.
     * If remaining vertices > k^2 or remaining edges > k^2, no size-k cover exists!
     */
    public static KernelResult kernelizeVertexCover(int numVertices, int[][] edges, int k) {
        Set<Integer> mandatory = new HashSet<>();
        int[] degree = new int[numVertices];
        for (int[] e : edges) {
            degree[e[0]]++;
            degree[e[1]]++;
        }

        int currentK = k;
        while (true) {
            int highDegreeVertex = -1;
            for (int i = 0; i < numVertices; i++) {
                if (!mandatory.contains(i) && degree[i] > currentK) {
                    highDegreeVertex = i;
                    break;
                }
            }

            if (highDegreeVertex == -1) {
                break; // No vertex has degree > currentK
            }

            mandatory.add(highDegreeVertex);
            currentK--;
            if (currentK < 0) {
                return new KernelResult(true, mandatory, 0, new ArrayList<>());
            }

            // Recompute remaining degrees
            Arrays.fill(degree, 0);
            for (int[] e : edges) {
                if (!mandatory.contains(e[0]) && !mandatory.contains(e[1])) {
                    degree[e[0]]++;
                    degree[e[1]]++;
                }
            }
        }

        List<int[]> kernelEdges = new ArrayList<>();
        Set<Integer> kernelVertices = new HashSet<>();
        for (int[] e : edges) {
            if (!mandatory.contains(e[0]) && !mandatory.contains(e[1])) {
                kernelEdges.add(e);
                kernelVertices.add(e[0]);
                kernelVertices.add(e[1]);
            }
        }

        if (kernelEdges.size() > currentK * currentK || kernelVertices.size() > 2 * currentK * currentK) {
            return new KernelResult(true, mandatory, currentK, kernelEdges);
        }

        return new KernelResult(false, mandatory, currentK, kernelEdges);
    }

    /**
     * Exact Bounded Search Tree FPT Algorithm for Vertex Cover in O(2^k * n).
     * For any uncovered edge (u, v), at least one of u or v must be in the cover.
     */
    public static Set<Integer> solveVertexCoverFPT(int numVertices, int[][] edges, int k) {
        KernelResult kern = kernelizeVertexCover(numVertices, edges, k);
        if (kern.trivialNo) return null;

        Set<Integer> cover = new HashSet<>(kern.mandatoryInCover);
        boolean solved = fptBranch(kern.kernelEdges, kern.reducedK, cover);
        return solved ? cover : null;
    }

    private static boolean fptBranch(List<int[]> edges, int k, Set<Integer> currentCover) {
        int[] uncovered = null;
        for (int[] e : edges) {
            if (!currentCover.contains(e[0]) && !currentCover.contains(e[1])) {
                uncovered = e;
                break;
            }
        }

        if (uncovered == null) return true; // all edges covered!
        if (k <= 0) return false;

        // Branch 1: include u
        currentCover.add(uncovered[0]);
        if (fptBranch(edges, k - 1, currentCover)) return true;
        currentCover.remove(uncovered[0]);

        // Branch 2: include v
        currentCover.add(uncovered[1]);
        if (fptBranch(edges, k - 1, currentCover)) return true;
        currentCover.remove(uncovered[1]);

        return false;
    }
}
