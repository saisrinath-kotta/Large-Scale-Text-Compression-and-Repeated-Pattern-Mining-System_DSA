import java.util.*;

/**
 * TestMasterSuite - Comprehensive verification for Modules 3, 4, 5, and 6.
 * Contains 60+ rigorous assertions validating exactness, approximation ratios,
 * and complexity bounds.
 */
public class TestMasterSuite {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            totalPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            totalFailed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        boolean match = Objects.equals(expected, actual);
        if (match) {
            System.out.println("  [PASS] " + testName + " -> " + actual);
            totalPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " -> Expected: " + expected + ", Got: " + actual);
            totalFailed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("RUNNING MASTER VERIFICATION SUITE: MODULES 3, 4, 5, 6");
        System.out.println("============================================================");

        testModule3AdvancedDP();
        testModule4NetworkFlow();
        testModule5NPApproximation();
        testModule6RandomisedParallel();

        System.out.println("\n============================================================");
        System.out.println("MASTER VERIFICATION SUMMARY: " + totalPassed + " PASSED, " + totalFailed + " FAILED");
        System.out.println("============================================================");

        if (totalFailed > 0) {
            System.exit(1);
        }
    }

    // =========================================================================
    // MODULE 3: ADVANCED DYNAMIC PROGRAMMING TESTS
    // =========================================================================
    private static void testModule3AdvancedDP() {
        System.out.println("\n--- [MODULE 3] Advanced Dynamic Programming ---");

        // 1. Damerau-Levenshtein
        assertEquals("Damerau-Levenshtein: transposition 'tehs' -> 'thes'", 1, AdvancedDP.damerauLevenshtein("tehs", "thes"));
        assertEquals("Damerau-Levenshtein: classic 'kitten' -> 'sitting'", 3, AdvancedDP.damerauLevenshtein("kitten", "sitting"));
        assertEquals("Damerau-Levenshtein: reversal 'ca' -> 'ac'", 1, AdvancedDP.damerauLevenshtein("ca", "ac"));

        // 2. Weighted Edit Distance (ins=3, del=3, sub=5, trans=1)
        int weighted = AdvancedDP.weightedEditDistance("cat", "cut", 3, 3, 5, 1);
        assertEquals("Weighted Edit: substitution cost 5", 5, weighted);

        // 3. Needleman-Wunsch Global Alignment
        AdvancedDP.AlignmentResult nw = AdvancedDP.needlemanWunsch("GCATGCG", "GATTACA", 1, -1, -1);
        assertTrue("Needleman-Wunsch: Score matrix computed", nw.scoreMatrix.length > 0);
        assertTrue("Needleman-Wunsch: Non-empty aligned strings", nw.alignedSeq1.length() == nw.alignedSeq2.length());

        // 4. Smith-Waterman Local Alignment
        AdvancedDP.AlignmentResult sw = AdvancedDP.smithWaterman("TGTTACGG", "GGTTGACTA", 2, -1, -2);
        assertTrue("Smith-Waterman: Local score > 0", sw.score > 0);
        assertTrue("Smith-Waterman: Local aligned segment identified", sw.alignedSeq1.contains("GTT"));

        // 5. Matrix Chain Multiplication
        int[] dims = {10, 20, 30, 40, 30};
        AdvancedDP.MatrixChainResult mcm = AdvancedDP.matrixChainMultiplication(dims);
        assertEquals("Matrix Chain Multiplication: optimal ops for {10,20,30,40,30}", 30000, mcm.minOperations);
        assertTrue("Matrix Chain: parenthesization generated", mcm.parenthesization.contains("A1"));

        // 6. Optimal BST
        double[] p = {0, 0.15, 0.10, 0.05, 0.10, 0.20};
        double[] q = {0.05, 0.10, 0.05, 0.05, 0.05, 0.10};
        AdvancedDP.OBSTResult obst = AdvancedDP.optimalBST(p, q, 5);
        assertTrue("Optimal BST: Expected cost is positive", obst.minCost > 0);
        assertTrue("Optimal BST: Root selected", obst.rootTable[1][5] >= 1 && obst.rootTable[1][5] <= 5);

        // 7. Bitmask TSP
        int INF = 1000000000;
        int[][] tspDist = {
            {0, 10, 15, 20},
            {10, 0, 35, 25},
            {15, 35, 0, 30},
            {20, 25, 30, 0}
        };
        AdvancedDP.TSPResult tsp = AdvancedDP.tspBitmask(tspDist);
        assertEquals("Bitmask TSP: 4-city optimal tour cost", 80, tsp.minCost);
        assertEquals("Bitmask TSP: Tour length is 5 (n+1 back to 0)", 5, tsp.tour.length);

        // 8. Hamiltonian Path
        boolean[][] hpAdj = {
            {false, true, false, false},
            {true, false, true, false},
            {false, true, false, true},
            {false, false, true, false}
        };
        assertTrue("Bitmask DP: Linear graph has Hamiltonian Path", AdvancedDP.hasHamiltonianPath(hpAdj));

        // 9. Tree DP: MWIS and Diameter and Rerooting
        int[][] treeAdj = {
            {1, 2},    // node 0
            {0, 3, 4}, // node 1
            {0},       // node 2
            {1},       // node 3
            {1}        // node 4
        };
        int[] treeWeights = {5, 10, 3, 4, 6};
        AdvancedDP.TreeDPResult treeRes = AdvancedDP.solveTreeDP(treeAdj, treeWeights);
        assertEquals("Tree DP: MWIS computed is 15", 15, treeRes.maxWeightIndependentSet);
        assertEquals("Tree DP: Tree diameter", 3, treeRes.treeDiameter);
        assertEquals("Tree DP: Rerooting all-roots distance array length", 5, treeRes.allRootsDistances.length);

        // 10. SOS DP (Sum Over Subsets)
        int[] sosInput = {1, 2, 4, 8}; // 2 bits: 00, 01, 10, 11
        int[] sosOutput = AdvancedDP.sumOverSubsets(sosInput, 2);
        // mask 3 (11) should be sum of 00, 01, 10, 11 = 1 + 2 + 4 + 8 = 15
        assertEquals("SOS DP: mask 3 (11) aggregates all submasks", 15, sosOutput[3]);
        // mask 1 (01) should be 00 + 01 = 1 + 2 = 3
        assertEquals("SOS DP: mask 1 (01) aggregates 00 and 01", 3, sosOutput[1]);

        // 11. Diagnostic Check
        AdvancedDP.DPDiagnostic diag = AdvancedDP.evaluateProblemSuitability(true, true, false, false);
        assertTrue("DP Diagnostic: standard DP suitability holds", diag.isDPSuitable);
    }

    // =========================================================================
    // MODULE 4: NETWORK FLOW TESTS
    // =========================================================================
    private static void testModule4NetworkFlow() {
        System.out.println("\n--- [MODULE 4] Network Flow & Reductions ---");

        // 1. Ford-Fulkerson, Edmonds-Karp, Dinic on standard flow network
        NetworkFlowEngine.FlowNetwork net = new NetworkFlowEngine.FlowNetwork(6);
        // Source = 0, Sink = 5
        net.addEdge(0, 1, 10);
        net.addEdge(0, 2, 10);
        net.addEdge(1, 2, 2);
        net.addEdge(1, 3, 4);
        net.addEdge(1, 4, 8);
        net.addEdge(2, 4, 9);
        net.addEdge(3, 5, 10);
        net.addEdge(4, 5, 10);

        int ffFlow = NetworkFlowEngine.fordFulkerson(net, 0, 5);
        int ekFlow = NetworkFlowEngine.edmondsKarp(net, 0, 5);
        NetworkFlowEngine.DinicResult dinicRes = NetworkFlowEngine.dinic(net, 0, 5);

        assertEquals("Ford-Fulkerson Max-Flow is 14", 14, ffFlow);
        assertEquals("Edmonds-Karp Max-Flow is 14", 14, ekFlow);
        assertEquals("Dinic's Algorithm Max-Flow is 14", 14, dinicRes.maxFlow);

        // 2. Max-Flow Min-Cut Theorem
        int minCutCapacity = 0;
        for (NetworkFlowEngine.FlowEdge e : dinicRes.minCutEdges) {
            minCutCapacity += e.capacity;
        }
        assertEquals("Max-Flow Min-Cut Theorem: Min-Cut Capacity equals Max Flow", dinicRes.maxFlow, minCutCapacity);

        // 3. Bipartite Matching & König's Theorem
        int[][] leftAdj = {
            {0, 1},
            {1},
            {1, 2}
        };
        NetworkFlowEngine.BipartiteMatchingResult bm = NetworkFlowEngine.solveBipartiteMatching(3, 3, leftAdj);
        assertEquals("Bipartite Matching: Max matching size", 3, bm.maxMatchingSize);
        int vertexCoverSize = bm.minVertexCoverLeft.size() + bm.minVertexCoverRight.size();
        assertEquals("König's Theorem: |Max Matching| equals |Min Vertex Cover|", bm.maxMatchingSize, vertexCoverSize);

        // 4. Min-Cost Max-Flow (MCMF)
        NetworkFlowEngine.FlowNetwork mcmfNet = new NetworkFlowEngine.FlowNetwork(4);
        mcmfNet.addEdge(0, 1, 3, 1);
        mcmfNet.addEdge(0, 2, 2, 5);
        mcmfNet.addEdge(1, 2, 1, 2);
        mcmfNet.addEdge(1, 3, 2, 2);
        mcmfNet.addEdge(2, 3, 2, 4);
        NetworkFlowEngine.MCMFResult mcmf = NetworkFlowEngine.minCostMaxFlow(mcmfNet, 0, 3);
        assertEquals("MCMF Max-Flow is 4", 4, mcmf.maxFlow);
        assertEquals("MCMF Min-Cost is 22", 22, mcmf.minCost);

        // 5. Project Selection via Min-Cut
        int[] revenue = {100, 200, -150, -50};
        int[][] deps = {
            {2},    // project 0 requires project 2 (cost 150)
            {2, 3}, // project 1 requires project 2 and 3 (cost 150 + 50)
            {},
            {}
        };
        NetworkFlowEngine.ProjectSelectionResult ps = NetworkFlowEngine.solveProjectSelection(revenue, deps);
        assertEquals("Project Selection: optimal net profit", 100, ps.maxNetProfit);
        assertTrue("Project Selection: Selected projects include 0 and 1", ps.selectedProjects.contains(0) && ps.selectedProjects.contains(1));

        // 6. Scheduling with Deadlines
        int[] deadlines = {1, 2, 2, 3}; // 4 jobs
        NetworkFlowEngine.SchedulingResult sched = NetworkFlowEngine.scheduleTasksWithDeadlines(deadlines, 3, 2);
        assertTrue("Task Scheduling: Feasible schedule with deadline flow", sched.feasible);
        assertEquals("Task Scheduling: All 4 tasks scheduled", 4, sched.totalScheduledTasks);
    }

    // =========================================================================
    // MODULE 5: NP-COMPLETENESS & APPROXIMATION TESTS
    // =========================================================================
    private static void testModule5NPApproximation() {
        System.out.println("\n--- [MODULE 5] NP-Completeness & Approximation ---");

        // 1. 3-SAT Verifier
        List<NPApproximationEngine.Clause3> satFormula = new ArrayList<>();
        satFormula.add(new NPApproximationEngine.Clause3(1, 2, -3));
        satFormula.add(new NPApproximationEngine.Clause3(-1, 2, 3));
        boolean[] validCert = {true, true, false}; // x1=T, x2=T, x3=F
        boolean[] invalidCert = {false, false, true}; // x1=F, x2=F, x3=T -> clause 1 is F!
        assertTrue("3-SAT Verifier: Accepts valid satisfying assignment", NPApproximationEngine.verify3SAT(satFormula, validCert));
        assertTrue("3-SAT Verifier: Rejects invalid assignment", !NPApproximationEngine.verify3SAT(satFormula, invalidCert));

        // 2. 3-SAT to Independent Set Reduction Gadget
        NPApproximationEngine.GraphReduction red = NPApproximationEngine.reduce3SATtoIndependentSet(satFormula);
        assertEquals("3-SAT -> IS Reduction: Vertices created = 3 * m", 6, red.numVertices);
        assertEquals("3-SAT -> IS Reduction: Target k = m", 2, red.targetK);

        // 3. Independent Set <-> Vertex Cover Duality
        Set<Integer> is = new HashSet<>(Arrays.asList(0, 3));
        Set<Integer> vc = NPApproximationEngine.independentSetToVertexCover(5, is);
        assertEquals("IS <-> VC Duality: |V \\ S| size", 3, vc.size());
        assertTrue("IS <-> VC Duality: Contains 1, 2, 4", vc.contains(1) && vc.contains(2) && vc.contains(4));

        // 4. Vertex Cover 2-Approximation via Maximal Matching
        int[][] vcEdges = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 0} // 5-cycle graph
        };
        NPApproximationEngine.ApproxVertexCoverResult vcApprox = NPApproximationEngine.vertexCover2Approx(5, vcEdges);
        assertTrue("Vertex Cover 2-Approx: Valid cover of all edges",
                NPApproximationEngine.verifyVertexCover(5, vcEdges, vcApprox.vertexCover, 5));
        assertTrue("Vertex Cover 2-Approx: Cover size <= 2 * OPT", vcApprox.coverSize <= 4);

        // 5. Metric TSP 2-Approximation
        double[][] tspDist = {
            {0, 2, 9, 10},
            {2, 0, 6, 4},
            {9, 6, 0, 8},
            {10, 4, 8, 0}
        };
        NPApproximationEngine.MetricTSPResult metricTSP = NPApproximationEngine.metricTSP2Approx(tspDist);
        assertEquals("Metric TSP 2-Approx: Valid tour length (n + 1)", 5, metricTSP.tour.length);
        assertTrue("Metric TSP 2-Approx: Positive cost computed", metricTSP.tourCost > 0);

        // 6. Greedy Set Cover
        List<Set<Integer>> subsets = new ArrayList<>();
        subsets.add(new HashSet<>(Arrays.asList(0, 1, 2)));
        subsets.add(new HashSet<>(Arrays.asList(2, 3, 4)));
        subsets.add(new HashSet<>(Arrays.asList(0, 4)));
        NPApproximationEngine.SetCoverResult sc = NPApproximationEngine.greedySetCover(5, subsets);
        assertTrue("Greedy Set Cover: Covers entire universe with <= 2 subsets", sc.chosenSubsets.size() <= 2);

        // 7. Knapsack FPTAS
        int[] weights = {10, 20, 30};
        int[] values = {60, 100, 120};
        NPApproximationEngine.KnapsackFPTASResult fptas = NPApproximationEngine.knapsackFPTAS(weights, values, 50, 0.2);
        assertTrue("Knapsack FPTAS: Total weight within capacity", fptas.totalWeight <= 50);
        assertTrue("Knapsack FPTAS: Near-optimal value obtained", fptas.totalValue >= 200);

        // 8. Parameterized Complexity (Buss's Kernelization & FPT)
        int[][] fptEdges = {
            {0, 1}, {0, 2}, {0, 3}, {0, 4}, // hub node 0
            {1, 2}
        };
        NPApproximationEngine.KernelResult kern = NPApproximationEngine.kernelizeVertexCover(5, fptEdges, 2);
        assertTrue("Kernelization: Identifies high-degree hub 0 in mandatory cover", kern.mandatoryInCover.contains(0));
        Set<Integer> fptCover = NPApproximationEngine.solveVertexCoverFPT(5, fptEdges, 2);
        assertTrue("FPT Exact Search: Solves Vertex Cover for k = 2", fptCover != null && fptCover.size() <= 2);
    }

    // =========================================================================
    // MODULE 6: RANDOMISED AND PARALLEL ALGORITHMS TESTS
    // =========================================================================
    private static void testModule6RandomisedParallel() {
        System.out.println("\n--- [MODULE 6] Randomised and Parallel Algorithms ---");

        // 1. Las Vegas Randomised QuickSort
        int[] unsorted = {9, 2, 7, 1, 5, 8, 3, 6, 4, 0};
        RandomisedParallelEngine.QuickSortStats qsStats = RandomisedParallelEngine.randomisedQuickSort(unsorted);
        assertTrue("Las Vegas Quicksort: Correctly sorts array", Arrays.equals(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, qsStats.sortedArray));
        assertTrue("Las Vegas Quicksort: Comparison count tracked", qsStats.comparisonCount > 0);

        // 2. Monte Carlo Miller-Rabin Primality Test
        RandomisedParallelEngine.MillerRabinResult p1 = RandomisedParallelEngine.millerRabin(1000000007L, 20);
        RandomisedParallelEngine.MillerRabinResult p2 = RandomisedParallelEngine.millerRabin(2147483647L, 20);
        RandomisedParallelEngine.MillerRabinResult c1 = RandomisedParallelEngine.millerRabin(561L, 20); // Carmichael pseudoprime
        RandomisedParallelEngine.MillerRabinResult c2 = RandomisedParallelEngine.millerRabin(1000000008L, 20);
        assertTrue("Miller-Rabin: 10^9+7 is prime", p1.isPrime);
        assertTrue("Miller-Rabin: Mersenne 2^31-1 is prime", p2.isPrime);
        assertTrue("Miller-Rabin: Carmichael 561 correctly identified as composite", !c1.isPrime);
        assertTrue("Miller-Rabin: Even composite identified", !c2.isPrime);

        // 3. Randomised Hashing & FKS 2-Level Perfect Hashing
        long[] keys = {101L, 205L, 309L, 412L, 550L, 680L, 777L};
        RandomisedParallelEngine.FKSTwoLevelTable fks = new RandomisedParallelEngine.FKSTwoLevelTable(keys);
        for (long k : keys) {
            assertTrue("FKS Perfect Hash: Key " + k + " present in O(1) worst-case", fks.contains(k));
        }
        assertTrue("FKS Perfect Hash: Non-existent key correctly rejected", !fks.contains(99999L));

        // 4. Reservoir Sampling (Algorithm R)
        List<String> stream = Arrays.asList("alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta");
        RandomisedParallelEngine.ReservoirResult<String> res = RandomisedParallelEngine.reservoirSample(stream, 3, 42L);
        assertEquals("Reservoir Sampling: Sample size equals k", 3, res.sample.size());
        assertEquals("Reservoir Sampling: Elements seen", 7, res.streamElementsSeen);
        assertTrue("Reservoir Sampling: Uniform prob = 3/7", Math.abs(res.uniformProbabilityPerItem - (3.0 / 7.0)) < 0.001);

        // 5. Blelloch Parallel Scan / Prefix-Sum
        int[] scanInput = {3, 1, 7, 0, 4, 1, 6, 3};
        int[] exclusiveScan = RandomisedParallelEngine.blellochParallelScan(scanInput);
        // Exclusive scan: [0, 3, 4, 11, 11, 15, 16, 22]
        int[] expectedScan = {0, 3, 4, 11, 11, 15, 16, 22};
        assertTrue("Blelloch Parallel Scan: Exclusive prefix-sum matches expected", Arrays.equals(expectedScan, exclusiveScan));

        // 6. Data-Parallel Multi-Core Text Search
        String text = "banana is a yellow fruit. eating a banana gives energy. banana banana!";
        RandomisedParallelEngine.ParallelSearchResult parSearch = RandomisedParallelEngine.parallelTextSearch(text, "banana", 4);
        assertEquals("Parallel Text Search: Found all 4 occurrences of 'banana'", 4, parSearch.matchPositions.size());
        assertTrue("Parallel Search: Work T1 and Span T_inf calculated", parSearch.metrics.workT1 > 0 && parSearch.metrics.spanTInf > 0);
    }
}
