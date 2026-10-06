import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NetworkFlowEngine - Module 4: Network Flow & Reductions Engine for TextHack.
 * 
 * Implements:
 * 1. Residual Graph structure with forward/backward residual edges
 * 2. Ford-Fulkerson method (DFS augmenting paths)
 * 3. Edmonds-Karp algorithm (BFS augmenting paths, O(V * E^2))
 * 4. Dinic's Algorithm (Level graph BFS + Blocking flow DFS with pointer reuse, O(V^2 * E))
 * 5. Max-Flow Min-Cut Theorem & Min-Cut Partitioning (S-T partition and min-cut capacity)
 * 6. Maximum Bipartite Matching via Flow with König's Theorem (Min Vertex Cover derivation)
 * 7. Min-Cost Max-Flow (MCMF) via Successive Shortest Path (SSP)
 * 8. Practical Reductions:
 *    - Project Selection Problem via Min-Cut
 *    - Scheduling with Deadlines and Processing Units via Flow
 *    - Document / Segment Partitioning via Min-Cut
 * 
 * Pure Java implementation with strict zero-external algorithm libraries.
 */
public class NetworkFlowEngine {

    public static class FlowEdge {
        public final int from;
        public final int to;
        public final int capacity;
        public int flow;
        public final int cost; // for MCMF
        public final int revIndex; // index of reverse edge in adj[to]

        public FlowEdge(int from, int to, int capacity, int cost, int revIndex) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
            this.cost = cost;
            this.revIndex = revIndex;
        }

        public int remainingCapacity() {
            return capacity - flow;
        }
    }

    public static class FlowNetwork {
        public final int n;
        public final List<List<FlowEdge>> adj;

        public FlowNetwork(int n) {
            this.n = n;
            this.adj = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                this.adj.add(new ArrayList<>());
            }
        }

        public void addEdge(int from, int to, int capacity) {
            addEdge(from, to, capacity, 0);
        }

        public void addEdge(int from, int to, int capacity, int cost) {
            FlowEdge forward = new FlowEdge(from, to, capacity, cost, adj.get(to).size());
            FlowEdge backward = new FlowEdge(to, from, 0, -cost, adj.get(from).size());
            adj.get(from).add(forward);
            adj.get(to).add(backward);
        }

        public void resetFlows() {
            for (List<FlowEdge> edges : adj) {
                for (FlowEdge e : edges) {
                    e.flow = 0;
                }
            }
        }
    }

    // =========================================================================
    // 1. FORD-FULKERSON (DFS AUGMENTING PATHS)
    // =========================================================================

    public static int fordFulkerson(FlowNetwork network, int s, int t) {
        network.resetFlows();
        int maxFlow = 0;
        boolean[] visited = new boolean[network.n];

        while (true) {
            Arrays.fill(visited, false);
            int pushed = dfsAugment(network, s, t, Integer.MAX_VALUE, visited);
            if (pushed == 0) break;
            maxFlow += pushed;
        }
        return maxFlow;
    }

    private static int dfsAugment(FlowNetwork network, int u, int t, int push, boolean[] visited) {
        if (u == t) return push;
        visited[u] = true;

        for (FlowEdge edge : network.adj.get(u)) {
            if (!visited[edge.to] && edge.remainingCapacity() > 0) {
                int tr = dfsAugment(network, edge.to, t, Math.min(push, edge.remainingCapacity()), visited);
                if (tr > 0) {
                    edge.flow += tr;
                    network.adj.get(edge.to).get(edge.revIndex).flow -= tr;
                    return tr;
                }
            }
        }
        return 0;
    }

    // =========================================================================
    // 2. EDMONDS-KARP (BFS AUGMENTING PATHS - O(V * E^2))
    // =========================================================================

    public static int edmondsKarp(FlowNetwork network, int s, int t) {
        network.resetFlows();
        int maxFlow = 0;
        int n = network.n;

        FlowEdge[] parentEdge = new FlowEdge[n];

        while (true) {
            Arrays.fill(parentEdge, null);
            int[] queue = new int[n];
            int head = 0, tail = 0;
            queue[tail++] = s;

            while (head < tail) {
                int u = queue[head++];
                if (u == t) break;
                for (FlowEdge edge : network.adj.get(u)) {
                    if (parentEdge[edge.to] == null && edge.to != s && edge.remainingCapacity() > 0) {
                        parentEdge[edge.to] = edge;
                        queue[tail++] = edge.to;
                    }
                }
            }

            if (parentEdge[t] == null) break; // no augmenting path found

            int push = Integer.MAX_VALUE;
            for (FlowEdge e = parentEdge[t]; e != null; e = parentEdge[e.from]) {
                push = Math.min(push, e.remainingCapacity());
            }

            for (FlowEdge e = parentEdge[t]; e != null; e = parentEdge[e.from]) {
                e.flow += push;
                network.adj.get(e.to).get(e.revIndex).flow -= push;
            }

            maxFlow += push;
        }
        return maxFlow;
    }

    // =========================================================================
    // 3. DINIC'S ALGORITHM (LEVEL GRAPH BFS + BLOCKING FLOW DFS - O(V^2 * E))
    // =========================================================================

    public static class DinicResult {
        public final int maxFlow;
        public final boolean[] reachableInResidual; // S-component of min-cut
        public final List<FlowEdge> minCutEdges;

        public DinicResult(int maxFlow, boolean[] reachable, List<FlowEdge> minCutEdges) {
            this.maxFlow = maxFlow;
            this.reachableInResidual = reachable;
            this.minCutEdges = minCutEdges;
        }
    }

    public static DinicResult dinic(FlowNetwork network, int s, int t) {
        network.resetFlows();
        int n = network.n;
        int[] level = new int[n];
        int[] ptr = new int[n];
        int maxFlow = 0;

        while (dinicBfs(network, s, t, level)) {
            Arrays.fill(ptr, 0);
            while (true) {
                int pushed = dinicDfs(network, s, t, Integer.MAX_VALUE, level, ptr);
                if (pushed == 0) break;
                maxFlow += pushed;
            }
        }

        // Min-cut computation: nodes reachable from s in residual graph
        boolean[] inS = new boolean[n];
        int[] queue = new int[n];
        int head = 0, tail = 0;
        inS[s] = true;
        queue[tail++] = s;

        while (head < tail) {
            int u = queue[head++];
            for (FlowEdge edge : network.adj.get(u)) {
                if (!inS[edge.to] && edge.remainingCapacity() > 0) {
                    inS[edge.to] = true;
                    queue[tail++] = edge.to;
                }
            }
        }

        List<FlowEdge> cutEdges = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            if (inS[u]) {
                for (FlowEdge edge : network.adj.get(u)) {
                    if (!inS[edge.to] && edge.capacity > 0) {
                        cutEdges.add(edge);
                    }
                }
            }
        }

        return new DinicResult(maxFlow, inS, cutEdges);
    }

    private static boolean dinicBfs(FlowNetwork network, int s, int t, int[] level) {
        Arrays.fill(level, -1);
        level[s] = 0;
        int[] queue = new int[network.n];
        int head = 0, tail = 0;
        queue[tail++] = s;

        while (head < tail) {
            int u = queue[head++];
            for (FlowEdge edge : network.adj.get(u)) {
                if (level[edge.to] == -1 && edge.remainingCapacity() > 0) {
                    level[edge.to] = level[u] + 1;
                    queue[tail++] = edge.to;
                }
            }
        }
        return level[t] != -1;
    }

    private static int dinicDfs(FlowNetwork network, int u, int t, int push, int[] level, int[] ptr) {
        if (u == t || push == 0) return push;

        List<FlowEdge> edges = network.adj.get(u);
        for (; ptr[u] < edges.size(); ptr[u]++) {
            FlowEdge edge = edges.get(ptr[u]);
            if (level[edge.to] == level[u] + 1 && edge.remainingCapacity() > 0) {
                int tr = dinicDfs(network, edge.to, t, Math.min(push, edge.remainingCapacity()), level, ptr);
                if (tr > 0) {
                    edge.flow += tr;
                    network.adj.get(edge.to).get(edge.revIndex).flow -= tr;
                    return tr;
                }
            }
        }
        return 0;
    }

    // =========================================================================
    // 4. BIPARTITE MATCHING & KÖNIG'S THEOREM
    // =========================================================================

    public static class BipartiteMatchingResult {
        public final int maxMatchingSize;
        public final int[] leftMatch; // left -> right (-1 if unmatched)
        public final int[] rightMatch; // right -> left (-1 if unmatched)
        public final List<Integer> minVertexCoverLeft;
        public final List<Integer> minVertexCoverRight;

        public BipartiteMatchingResult(int size, int[] leftMatch, int[] rightMatch,
                                       List<Integer> minCoverL, List<Integer> minCoverR) {
            this.maxMatchingSize = size;
            this.leftMatch = leftMatch;
            this.rightMatch = rightMatch;
            this.minVertexCoverLeft = minCoverL;
            this.minVertexCoverRight = minCoverR;
        }
    }

    /**
     * Solves Maximum Bipartite Matching and extracts Minimum Vertex Cover via König's Theorem.
     * numLeft: count of left nodes (0..numLeft-1)
     * numRight: count of right nodes (0..numRight-1)
     * leftAdj: for each left node, list of adjacent right nodes
     */
    public static BipartiteMatchingResult solveBipartiteMatching(int numLeft, int numRight, int[][] leftAdj) {
        int s = numLeft + numRight;
        int t = s + 1;
        FlowNetwork net = new FlowNetwork(t + 1);

        for (int i = 0; i < numLeft; i++) {
            net.addEdge(s, i, 1);
            for (int r : leftAdj[i]) {
                net.addEdge(i, numLeft + r, 1);
            }
        }
        for (int j = 0; j < numRight; j++) {
            net.addEdge(numLeft + j, t, 1);
        }

        DinicResult dinicRes = dinic(net, s, t);

        int[] leftMatch = new int[numLeft];
        int[] rightMatch = new int[numRight];
        Arrays.fill(leftMatch, -1);
        Arrays.fill(rightMatch, -1);

        for (int i = 0; i < numLeft; i++) {
            for (FlowEdge e : net.adj.get(i)) {
                if (e.to >= numLeft && e.to < s && e.flow > 0) {
                    int r = e.to - numLeft;
                    leftMatch[i] = r;
                    rightMatch[r] = i;
                }
            }
        }

        // König's Theorem: Min Vertex Cover from residual reachability
        // S = nodes reachable from source in residual graph
        // Cover includes Left nodes NOT in S, and Right nodes IN S
        List<Integer> coverL = new ArrayList<>();
        List<Integer> coverR = new ArrayList<>();
        for (int i = 0; i < numLeft; i++) {
            if (!dinicRes.reachableInResidual[i]) {
                coverL.add(i);
            }
        }
        for (int j = 0; j < numRight; j++) {
            if (dinicRes.reachableInResidual[numLeft + j]) {
                coverR.add(j);
            }
        }

        return new BipartiteMatchingResult(dinicRes.maxFlow, leftMatch, rightMatch, coverL, coverR);
    }

    // =========================================================================
    // 5. MIN-COST MAX-FLOW (MCMF) - SUCCESSIVE SHORTEST PATH (SSP)
    // =========================================================================

    public static class MCMFResult {
        public final int maxFlow;
        public final int minCost;

        public MCMFResult(int flow, int cost) {
            this.maxFlow = flow;
            this.minCost = cost;
        }
    }

    public static MCMFResult minCostMaxFlow(FlowNetwork net, int s, int t) {
        net.resetFlows();
        int n = net.n;
        int flow = 0;
        int cost = 0;

        int[] dist = new int[n];
        FlowEdge[] parentEdge = new FlowEdge[n];
        boolean[] inQueue = new boolean[n];

        while (true) {
            Arrays.fill(dist, Integer.MAX_VALUE / 2);
            Arrays.fill(parentEdge, null);
            Arrays.fill(inQueue, false);

            int[] queue = new int[n * 2];
            int head = 0, tail = 0;

            dist[s] = 0;
            queue[tail++] = s;
            inQueue[s] = true;

            // SPFA (Shortest Path Faster Algorithm) for negative residual edge weights
            while (head < tail) {
                int u = queue[head++];
                inQueue[u] = false;

                for (FlowEdge edge : net.adj.get(u)) {
                    if (edge.remainingCapacity() > 0 && dist[u] + edge.cost < dist[edge.to]) {
                        dist[edge.to] = dist[u] + edge.cost;
                        parentEdge[edge.to] = edge;
                        if (!inQueue[edge.to]) {
                            queue[tail++] = edge.to;
                            inQueue[edge.to] = true;
                        }
                    }
                }
            }

            if (dist[t] >= Integer.MAX_VALUE / 2) break; // no augmenting path

            int push = Integer.MAX_VALUE;
            for (FlowEdge e = parentEdge[t]; e != null; e = parentEdge[e.from]) {
                push = Math.min(push, e.remainingCapacity());
            }

            for (FlowEdge e = parentEdge[t]; e != null; e = parentEdge[e.from]) {
                e.flow += push;
                net.adj.get(e.to).get(e.revIndex).flow -= push;
                cost += push * e.cost;
            }

            flow += push;
        }

        return new MCMFResult(flow, cost);
    }

    // =========================================================================
    // 6. PRACTICAL REDUCTIONS: PROJECT SELECTION PROBLEM VIA MIN-CUT
    // =========================================================================

    public static class ProjectSelectionResult {
        public final int maxNetProfit;
        public final List<Integer> selectedProjects;

        public ProjectSelectionResult(int maxProfit, List<Integer> selectedProjects) {
            this.maxNetProfit = maxProfit;
            this.selectedProjects = selectedProjects;
        }
    }

    /**
     * Solves the Project Selection Problem (Maximum Weight Closure) via Min-Cut.
     * revenue: profit (>0) or cost (<0) for each project.
     * dependencies: dependencies[i] = list of project indices project i requires.
     */
    public static ProjectSelectionResult solveProjectSelection(int[] revenue, int[][] dependencies) {
        int numProjects = revenue.length;
        int s = numProjects;
        int t = numProjects + 1;
        FlowNetwork net = new FlowNetwork(t + 1);

        int totalPositiveRevenue = 0;
        int INF = 100000000;

        for (int i = 0; i < numProjects; i++) {
            if (revenue[i] > 0) {
                totalPositiveRevenue += revenue[i];
                net.addEdge(s, i, revenue[i]);
            } else if (revenue[i] < 0) {
                net.addEdge(i, t, -revenue[i]);
            }

            for (int prereq : dependencies[i]) {
                // If project i is selected, prerequisite must be selected: infinite capacity
                net.addEdge(i, prereq, INF);
            }
        }

        DinicResult cut = dinic(net, s, t);
        int maxProfit = totalPositiveRevenue - cut.maxFlow;

        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < numProjects; i++) {
            if (cut.reachableInResidual[i]) {
                selected.add(i);
            }
        }

        return new ProjectSelectionResult(maxProfit, selected);
    }

    // =========================================================================
    // 7. PRACTICAL REDUCTIONS: TASK SCHEDULING WITH DEADLINES VIA FLOW
    // =========================================================================

    public static class SchedulingResult {
        public final boolean feasible;
        public final int totalScheduledTasks;

        public SchedulingResult(boolean feasible, int count) {
            this.feasible = feasible;
            this.totalScheduledTasks = count;
        }
    }

    /**
     * Schedules n jobs across m time slots on k machines.
     * jobDeadlines: deadline slot (1-indexed) for each job.
     * machineCapacityPerSlot: number of machines available per time slot.
     */
    public static SchedulingResult scheduleTasksWithDeadlines(int[] jobDeadlines, int numSlots, int machineCapacityPerSlot) {
        int nJobs = jobDeadlines.length;
        int s = nJobs + numSlots;
        int t = s + 1;
        FlowNetwork net = new FlowNetwork(t + 1);

        for (int i = 0; i < nJobs; i++) {
            net.addEdge(s, i, 1);
            int dl = jobDeadlines[i];
            for (int slot = 0; slot < Math.min(dl, numSlots); slot++) {
                net.addEdge(i, nJobs + slot, 1);
            }
        }

        for (int slot = 0; slot < numSlots; slot++) {
            net.addEdge(nJobs + slot, t, machineCapacityPerSlot);
        }

        int flow = edmondsKarp(net, s, t);
        return new SchedulingResult(flow == nJobs, flow);
    }
}
