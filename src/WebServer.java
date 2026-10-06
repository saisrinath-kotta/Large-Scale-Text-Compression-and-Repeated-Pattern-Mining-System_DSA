import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * WebServer - Full-Stack HTTP REST Backend for TextHack.
 * 
 * Powered by pure Java standard library (com.sun.net.httpserver.HttpServer).
 * Zero external JARs, zero Maven/Gradle dependencies needed!
 * 
 * Exposes full interactive APIs for:
 * - Module 1 & 2: Text Compression & Suffix Pattern Mining
 * - Module 3: Advanced Dynamic Programming
 * - Module 4: Network Flow & Practical Reductions
 * - Module 5: NP-Completeness & Approximation Schemes
 * - Module 6: Randomised & Parallel Algorithms
 * - Static frontend serving from the web/ directory.
 */
public class WebServer {

    public static final int DEFAULT_PORT = 8080;
    private static String corpusText = "";
    private static CorpusLoader corpusLoader;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        // Preload corpus
        try {
            File corpusFile = new File("data/corpus.txt");
            if (!corpusFile.exists()) {
                corpusFile = new File("data/test_corpus.txt");
            }
            if (corpusFile.exists()) {
                corpusLoader = new CorpusLoader(corpusFile.getPath());
                corpusLoader.load();
                corpusText = corpusLoader.getText();
                System.out.println("[SERVER] Loaded corpus: " + corpusFile.getPath() + " (" + corpusText.length() + " chars)");
            }
        } catch (Exception e) {
            System.err.println("[SERVER] Warning loading corpus: " + e.getMessage());
            corpusText = "TextHack Advanced Algorithms System. Aho-Corasick, Suffix Array, Kasai LCP, Wagner-Fischer DP.";
        }

        HttpServer server = null;
        int activePort = port;
        for (int p = port; p < port + 20; p++) {
            try {
                server = HttpServer.create(new InetSocketAddress(p), 0);
                activePort = p;
                break;
            } catch (IOException ex) {
                // Try next port
            }
        }

        if (server == null) {
            System.err.println("[SERVER ERROR] Could not bind to any port between " + port + " and " + (port + 20));
            return;
        }

        try {
            // Static files handler
            server.createContext("/", new StaticFileHandler());

            // REST API Handlers
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/corpus", new CorpusHandler());
            server.createContext("/api/search", new SearchHandler());
            server.createContext("/api/dp", new DPHandler());
            server.createContext("/api/flow", new FlowHandler());
            server.createContext("/api/np", new NPHandler());
            server.createContext("/api/random-parallel", new RandomParallelHandler());

            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();

            // Write active port to file for scripts/launchers
            try (FileWriter fw = new FileWriter("active_port.txt")) {
                fw.write(String.valueOf(activePort));
            } catch (Exception ignored) {}

            System.out.println("============================================================");
            System.out.println("🚀 TEXTHACK FULL-STACK WEB SERVER RUNNING");
            System.out.println("👉 Open in browser: http://localhost:" + activePort + "/");
            System.out.println("============================================================");

        } catch (Exception e) {
            System.err.println("[SERVER ERROR] Could not start server: " + e.getMessage());
        }
    }

    // =========================================================================
    // STATIC FILE HANDLER
    // =========================================================================
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File("web" + path);
            if (!file.exists() || file.isDirectory()) {
                sendJsonResponse(exchange, 404, "{\"error\": \"Not Found: " + escapeJson(path) + "\"}");
                return;
            }

            String contentType = "text/plain";
            if (path.endsWith(".html")) contentType = "text/html; charset=UTF-8";
            else if (path.endsWith(".css")) contentType = "text/css; charset=UTF-8";
            else if (path.endsWith(".js")) contentType = "application/javascript; charset=UTF-8";
            else if (path.endsWith(".json")) contentType = "application/json; charset=UTF-8";
            else if (path.endsWith(".png")) contentType = "image/png";
            else if (path.endsWith(".svg")) contentType = "image/svg+xml";

            byte[] bytes = readFileToByteArray(file);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // =========================================================================
    // API: SYSTEM STATUS
    // =========================================================================
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Runtime rt = Runtime.getRuntime();
            long totalMem = rt.totalMemory() / (1024 * 1024);
            long freeMem = rt.freeMemory() / (1024 * 1024);
            long usedMem = totalMem - freeMem;

            String json = "{"
                    + "\"status\": \"ONLINE\","
                    + "\"jvm\": \"" + escapeJson(System.getProperty("java.version")) + "\","
                    + "\"os\": \"" + escapeJson(System.getProperty("os.name")) + "\","
                    + "\"availableCores\": " + rt.availableProcessors() + ","
                    + "\"memoryUsedMB\": " + usedMem + ","
                    + "\"corpusLength\": " + corpusText.length() + ","
                    + "\"modules\": ["
                    + "  {\"id\": 1, \"name\": \"Module 1: TextHack Engine & Corpus Pipeline\", \"status\": \"READY\"},"
                    + "  {\"id\": 2, \"name\": \"Module 2: String Algorithms & Suffix Structures\", \"status\": \"READY\"},"
                    + "  {\"id\": 3, \"name\": \"Module 3: Advanced Dynamic Programming\", \"status\": \"READY\"},"
                    + "  {\"id\": 4, \"name\": \"Module 4: Network Flow & Reductions\", \"status\": \"READY\"},"
                    + "  {\"id\": 5, \"name\": \"Module 5: NP-Completeness & Approximation\", \"status\": \"READY\"},"
                    + "  {\"id\": 6, \"name\": \"Module 6: Randomised & Parallel Algorithms\", \"status\": \"READY\"}"
                    + "]"
                    + "}";

            sendJsonResponse(exchange, 200, json);
        }
    }

    // =========================================================================
    // API: CORPUS STATS & PREVIEW
    // =========================================================================
    static class CorpusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int previewLen = Math.min(1000, corpusText.length());
            String preview = corpusText.substring(0, previewLen);

            String json = "{"
                    + "\"totalCharacters\": " + corpusText.length() + ","
                    + "\"preview\": \"" + escapeJson(preview) + "\""
                    + "}";
            sendJsonResponse(exchange, 200, json);
        }
    }

    // =========================================================================
    // API: STRING ALGORITHMS (MODULE 1 & 2)
    // =========================================================================
    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendJsonResponse(exchange, 405, "{\"error\":\"POST required\"}");
                return;
            }

            Map<String, String> params = parseJsonBody(exchange);
            String algo = params.getOrDefault("algorithm", "kmp").toLowerCase();
            String pattern = params.getOrDefault("pattern", "");
            String customText = params.getOrDefault("text", "");
            String text = customText.isEmpty() ? corpusText : customText;

            long start = System.nanoTime();
            String resultJson = "";

            if (algo.equals("kmp")) {
                List<Integer> matches = KMP.search(text, pattern);
                int[] lps = KMP.buildLPS(pattern);
                resultJson = "{\"algorithm\":\"KMP\",\"matches\":" + listIntToJson(matches)
                        + ",\"lps\":" + intArrayToJson(lps)
                        + ",\"matchCount\":" + matches.size() + "}";
            } else if (algo.equals("z")) {
                List<Integer> matches = ZFunction.search(text, pattern);
                int[] zBox = ZFunction.buildZArray(pattern);
                resultJson = "{\"algorithm\":\"Z-Algorithm\",\"matches\":" + listIntToJson(matches)
                        + ",\"zArray\":" + intArrayToJson(zBox)
                        + ",\"matchCount\":" + matches.size() + "}";
            } else if (algo.equals("rabinkarp")) {
                List<Integer> matches = RabinKarp.search(text, pattern);
                resultJson = "{\"algorithm\":\"Rabin-Karp Double Hash\",\"matches\":" + listIntToJson(matches)
                        + ",\"matchCount\":" + matches.size() + "}";
            } else if (algo.equals("ahocorasick")) {
                String[] patternArr = pattern.split("[,\\s]+");
                AhoCorasick ac = new AhoCorasick(Arrays.asList(patternArr));
                Map<String, List<Integer>> acMatches = ac.search(text);
                StringBuilder sb = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<String, List<Integer>> e : acMatches.entrySet()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("\"").append(escapeJson(e.getKey())).append("\":").append(listIntToJson(e.getValue()));
                }
                sb.append("}");
                resultJson = "{\"algorithm\":\"Aho-Corasick\",\"matches\":" + sb.toString() + "}";
            } else if (algo.equals("suffixarray")) {
                int[] sa = SuffixArray.buildSuffixArray(text);
                int[] lcp = KasaiLCP.buildLCP(text, sa);
                int limit = Math.min(sa.length, 50);
                StringBuilder saJson = new StringBuilder("[");
                for (int i = 0; i < limit; i++) {
                    if (i > 0) saJson.append(",");
                    String suf = text.substring(sa[i], Math.min(text.length(), sa[i] + 30));
                    saJson.append("{\"rank\":").append(i)
                            .append(",\"sa\":").append(sa[i])
                            .append(",\"lcp\":").append(lcp[i])
                            .append(",\"suffix\":\"").append(escapeJson(suf)).append("\"}");
                }
                saJson.append("]");
                resultJson = "{\"algorithm\":\"SuffixArray & Kasai LCP\",\"table\":" + saJson.toString() + "}";
            } else if (algo.equals("patternminer")) {
                int minLen = 5;
                try {
                    minLen = Integer.parseInt(params.getOrDefault("minLength", "5"));
                } catch (Exception ignored) {}
                int[] sa = SuffixArray.buildSuffixArray(text);
                int[] lcp = KasaiLCP.buildLCP(text, sa);
                List<PatternMiner.RepeatedPattern> patterns = PatternMiner.mineRepeatedPatterns(text, sa, lcp, minLen);
                StringBuilder patJson = new StringBuilder("[");
                int limit = Math.min(patterns.size(), 30);
                for (int i = 0; i < limit; i++) {
                    if (i > 0) patJson.append(",");
                    PatternMiner.RepeatedPattern rp = patterns.get(i);
                    patJson.append("{\"pattern\":\"").append(escapeJson(rp.getPattern()))
                            .append("\",\"length\":").append(rp.getLength())
                            .append(",\"occurrences\":").append(rp.getOccurrences())
                            .append(",\"positions\":").append(listIntToJson(rp.getPositions())).append("}");
                }
                patJson.append("]");
                resultJson = "{\"algorithm\":\"PatternMiner\",\"repeats\":" + patJson.toString() + "}";
            }

            long elapsedNs = System.nanoTime() - start;
            String response = "{\"success\":true,\"timeNs\":" + elapsedNs + ",\"timeMs\":" + (elapsedNs / 1000000.0) + ",\"data\":" + resultJson + "}";
            sendJsonResponse(exchange, 200, response);
        }
    }

    // =========================================================================
    // API: ADVANCED DYNAMIC PROGRAMMING (MODULE 3)
    // =========================================================================
    static class DPHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendJsonResponse(exchange, 405, "{\"error\":\"POST required\"}");
                return;
            }

            Map<String, String> params = parseJsonBody(exchange);
            String type = params.getOrDefault("type", "edit").toLowerCase();
            String s1 = params.getOrDefault("s1", "kitten");
            String s2 = params.getOrDefault("s2", "sitting");

            String result = "{}";

            if (type.equals("levenshtein")) {
                int dist = EditDistance.compute(s1, s2);
                int[][] table = EditDistance.computeTable(s1, s2);
                result = "{\"distance\":" + dist + ",\"table\":" + matrixToJson(table) + ",\"s1\":\"" + escapeJson(s1) + "\",\"s2\":\"" + escapeJson(s2) + "\"}";
            } else if (type.equals("damerau")) {
                int dist = AdvancedDP.damerauLevenshtein(s1, s2);
                result = "{\"distance\":" + dist + ",\"type\":\"Damerau-Levenshtein (with adjacent transpositions)\"}";
            } else if (type.equals("needleman")) {
                AdvancedDP.AlignmentResult nw = AdvancedDP.needlemanWunsch(s1, s2, 1, -1, -1);
                result = "{\"score\":" + nw.score + ",\"alignedSeq1\":\"" + nw.alignedSeq1 + "\",\"alignedSeq2\":\"" + nw.alignedSeq2 + "\",\"table\":" + matrixToJson(nw.scoreMatrix) + "}";
            } else if (type.equals("smithwaterman")) {
                AdvancedDP.AlignmentResult sw = AdvancedDP.smithWaterman(s1, s2, 2, -1, -2);
                result = "{\"score\":" + sw.score + ",\"alignedSeq1\":\"" + sw.alignedSeq1 + "\",\"alignedSeq2\":\"" + sw.alignedSeq2 + "\",\"table\":" + matrixToJson(sw.scoreMatrix) + "}";
            } else if (type.equals("matrixchain")) {
                int[] p = {10, 20, 30, 40, 30};
                AdvancedDP.MatrixChainResult mcm = AdvancedDP.matrixChainMultiplication(p);
                result = "{\"minOperations\":" + mcm.minOperations + ",\"parenthesization\":\"" + mcm.parenthesization + "\"}";
            } else if (type.equals("obst")) {
                double[] p = {0, 0.15, 0.10, 0.05, 0.10, 0.20};
                double[] q = {0.05, 0.10, 0.05, 0.05, 0.05, 0.10};
                AdvancedDP.OBSTResult obst = AdvancedDP.optimalBST(p, q, 5);
                result = "{\"minExpectedCost\":" + obst.minCost + ",\"rootTable\":" + matrixToJson(obst.rootTable) + "}";
            } else if (type.equals("tsp")) {
                int[][] tspDist = {
                    {0, 10, 15, 20},
                    {10, 0, 35, 25},
                    {15, 35, 0, 30},
                    {20, 25, 30, 0}
                };
                AdvancedDP.TSPResult tsp = AdvancedDP.tspBitmask(tspDist);
                result = "{\"minCost\":" + tsp.minCost + ",\"tour\":" + intArrayToJson(tsp.tour) + "}";
            } else if (type.equals("treedp")) {
                int[][] treeAdj = {{1, 2}, {0, 3, 4}, {0}, {1}, {1}};
                int[] weights = {5, 10, 3, 4, 6};
                AdvancedDP.TreeDPResult tree = AdvancedDP.solveTreeDP(treeAdj, weights);
                result = "{\"mwis\":" + tree.maxWeightIndependentSet + ",\"diameter\":" + tree.treeDiameter + ",\"allRootsDistances\":" + longArrayToJson(tree.allRootsDistances) + "}";
            } else if (type.equals("sosdp")) {
                int[] a = {1, 2, 4, 8};
                int[] sos = AdvancedDP.sumOverSubsets(a, 2);
                result = "{\"input\":" + intArrayToJson(a) + ",\"sosResult\":" + intArrayToJson(sos) + "}";
            } else if (type.equals("diagnostic")) {
                AdvancedDP.DPDiagnostic diag = AdvancedDP.evaluateProblemSuitability(true, true, false, false);
                result = "{\"isDPSuitable\":" + diag.isDPSuitable + ",\"recommendation\":\"" + diag.recommendation + "\",\"rationale\":\"" + diag.rationale + "\"}";
            }

            sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + result + "}");
        }
    }

    // =========================================================================
    // API: NETWORK FLOW & REDUCTIONS (MODULE 4)
    // =========================================================================
    static class FlowHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendJsonResponse(exchange, 405, "{\"error\":\"POST required\"}");
                return;
            }

            Map<String, String> params = parseJsonBody(exchange);
            String type = params.getOrDefault("type", "maxflow").toLowerCase();
            String result = "{}";

            if (type.equals("maxflow") || type.equals("dinic") || type.equals("edmonds")) {
                NetworkFlowEngine.FlowNetwork net = new NetworkFlowEngine.FlowNetwork(6);
                net.addEdge(0, 1, 10);
                net.addEdge(0, 2, 10);
                net.addEdge(1, 2, 2);
                net.addEdge(1, 3, 4);
                net.addEdge(1, 4, 8);
                net.addEdge(2, 4, 9);
                net.addEdge(3, 5, 10);
                net.addEdge(4, 5, 10);

                int ff = NetworkFlowEngine.fordFulkerson(net, 0, 5);
                int ek = NetworkFlowEngine.edmondsKarp(net, 0, 5);
                NetworkFlowEngine.DinicResult dinic = NetworkFlowEngine.dinic(net, 0, 5);

                StringBuilder cutEdgesJson = new StringBuilder("[");
                for (int i = 0; i < dinic.minCutEdges.size(); i++) {
                    if (i > 0) cutEdgesJson.append(",");
                    NetworkFlowEngine.FlowEdge e = dinic.minCutEdges.get(i);
                    cutEdgesJson.append("{\"from\":").append(e.from).append(",\"to\":").append(e.to).append(",\"capacity\":").append(e.capacity).append("}");
                }
                cutEdgesJson.append("]");

                result = "{\"fordFulkerson\":" + ff + ",\"edmondsKarp\":" + ek + ",\"dinicMaxFlow\":" + dinic.maxFlow
                        + ",\"minCutEdges\":" + cutEdgesJson.toString() + "}";
            } else if (type.equals("bipartite")) {
                int[][] leftAdj = {{0, 1}, {1}, {1, 2}};
                NetworkFlowEngine.BipartiteMatchingResult bm = NetworkFlowEngine.solveBipartiteMatching(3, 3, leftAdj);
                result = "{\"maxMatchingSize\":" + bm.maxMatchingSize
                        + ",\"leftMatch\":" + intArrayToJson(bm.leftMatch)
                        + ",\"rightMatch\":" + intArrayToJson(bm.rightMatch)
                        + ",\"minVertexCoverL\":" + listIntToJson(bm.minVertexCoverLeft)
                        + ",\"minVertexCoverR\":" + listIntToJson(bm.minVertexCoverRight) + "}";
            } else if (type.equals("mcmf")) {
                NetworkFlowEngine.FlowNetwork net = new NetworkFlowEngine.FlowNetwork(4);
                net.addEdge(0, 1, 3, 1);
                net.addEdge(0, 2, 2, 5);
                net.addEdge(1, 2, 1, 2);
                net.addEdge(1, 3, 2, 2);
                net.addEdge(2, 3, 2, 4);
                NetworkFlowEngine.MCMFResult mcmf = NetworkFlowEngine.minCostMaxFlow(net, 0, 3);
                result = "{\"maxFlow\":" + mcmf.maxFlow + ",\"minCost\":" + mcmf.minCost + "}";
            } else if (type.equals("projectselection")) {
                int[] revenue = {100, 200, -150, -50};
                int[][] deps = {{2}, {2, 3}, {}, {}};
                NetworkFlowEngine.ProjectSelectionResult ps = NetworkFlowEngine.solveProjectSelection(revenue, deps);
                result = "{\"maxNetProfit\":" + ps.maxNetProfit + ",\"selectedProjects\":" + listIntToJson(ps.selectedProjects) + "}";
            } else if (type.equals("scheduling")) {
                int[] deadlines = {1, 2, 2, 3};
                NetworkFlowEngine.SchedulingResult sched = NetworkFlowEngine.scheduleTasksWithDeadlines(deadlines, 3, 2);
                result = "{\"feasible\":" + sched.feasible + ",\"totalScheduledTasks\":" + sched.totalScheduledTasks + "}";
            }

            sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + result + "}");
        }
    }

    // =========================================================================
    // API: NP-COMPLETENESS & APPROXIMATION (MODULE 5)
    // =========================================================================
    static class NPHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendJsonResponse(exchange, 405, "{\"error\":\"POST required\"}");
                return;
            }

            Map<String, String> params = parseJsonBody(exchange);
            String type = params.getOrDefault("type", "reduction").toLowerCase();
            String result = "{}";

            if (type.equals("reduction")) {
                List<NPApproximationEngine.Clause3> satFormula = new ArrayList<>();
                satFormula.add(new NPApproximationEngine.Clause3(1, 2, -3));
                satFormula.add(new NPApproximationEngine.Clause3(-1, 2, 3));
                NPApproximationEngine.GraphReduction red = NPApproximationEngine.reduce3SATtoIndependentSet(satFormula);
                result = "{\"reductionName\":\"" + red.reductionName + "\",\"numVertices\":" + red.numVertices + ",\"targetK\":" + red.targetK + ",\"edgesCount\":" + red.edges.size() + "}";
            } else if (type.equals("vertexcover2approx")) {
                int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 0}};
                NPApproximationEngine.ApproxVertexCoverResult vc = NPApproximationEngine.vertexCover2Approx(5, edges);
                result = "{\"coverSize\":" + vc.coverSize + ",\"cover\":" + setIntToJson(vc.vertexCover) + ",\"ratioBound\":2.0}";
            } else if (type.equals("metrictsp")) {
                double[][] dist = {
                    {0, 2, 9, 10},
                    {2, 0, 6, 4},
                    {9, 6, 0, 8},
                    {10, 4, 8, 0}
                };
                NPApproximationEngine.MetricTSPResult tsp = NPApproximationEngine.metricTSP2Approx(dist);
                result = "{\"tour\":" + intArrayToJson(tsp.tour) + ",\"tourCost\":" + tsp.tourCost + ",\"ratioBound\":2.0}";
            } else if (type.equals("setcover")) {
                List<Set<Integer>> subsets = new ArrayList<>();
                subsets.add(new HashSet<>(Arrays.asList(0, 1, 2)));
                subsets.add(new HashSet<>(Arrays.asList(2, 3, 4)));
                subsets.add(new HashSet<>(Arrays.asList(0, 4)));
                NPApproximationEngine.SetCoverResult sc = NPApproximationEngine.greedySetCover(5, subsets);
                result = "{\"chosenSubsets\":" + listIntToJson(sc.chosenSubsets) + ",\"harmonicBound\":" + sc.harmonicBound + "}";
            } else if (type.equals("knapsackfptas")) {
                int[] w = {10, 20, 30};
                int[] v = {60, 100, 120};
                double eps = 0.2;
                try {
                    eps = Double.parseDouble(params.getOrDefault("epsilon", "0.2"));
                } catch (Exception ignored) {}
                NPApproximationEngine.KnapsackFPTASResult fptas = NPApproximationEngine.knapsackFPTAS(w, v, 50, eps);
                result = "{\"totalValue\":" + fptas.totalValue + ",\"totalWeight\":" + fptas.totalWeight + ",\"chosenItems\":" + listIntToJson(fptas.chosenItems) + ",\"epsilon\":" + fptas.epsilon + ",\"theoreticalLowerBoundRatio\":" + fptas.theoreticalLowerBoundRatio + "}";
            } else if (type.equals("kernelization")) {
                int[][] edges = {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {1, 2}};
                NPApproximationEngine.KernelResult kern = NPApproximationEngine.kernelizeVertexCover(5, edges, 2);
                Set<Integer> fpt = NPApproximationEngine.solveVertexCoverFPT(5, edges, 2);
                result = "{\"mandatoryInCover\":" + setIntToJson(kern.mandatoryInCover) + ",\"reducedK\":" + kern.reducedK + ",\"kernelEdgesCount\":" + kern.kernelEdges.size() + ",\"exactFPTCover\":" + setIntToJson(fpt) + "}";
            }

            sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + result + "}");
        }
    }

    // =========================================================================
    // API: RANDOMISED & PARALLEL ALGORITHMS (MODULE 6)
    // =========================================================================
    static class RandomParallelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendJsonResponse(exchange, 405, "{\"error\":\"POST required\"}");
                return;
            }

            Map<String, String> params = parseJsonBody(exchange);
            String type = params.getOrDefault("type", "qsort").toLowerCase();
            String result = "{}";

            if (type.equals("qsort")) {
                int[] arr = {9, 2, 7, 1, 5, 8, 3, 6, 4, 0};
                RandomisedParallelEngine.QuickSortStats qs = RandomisedParallelEngine.randomisedQuickSort(arr);
                result = "{\"sorted\":" + intArrayToJson(qs.sortedArray) + ",\"comparisonCount\":" + qs.comparisonCount + ",\"isLasVegas\":true}";
            } else if (type.equals("millerrabin")) {
                long num = 1000000007L;
                try {
                    num = Long.parseLong(params.getOrDefault("number", "1000000007"));
                } catch (Exception ignored) {}
                RandomisedParallelEngine.MillerRabinResult mr = RandomisedParallelEngine.millerRabin(num, 20);
                result = "{\"number\":" + num + ",\"isPrime\":" + mr.isPrime + ",\"explanation\":\"" + escapeJson(mr.witnessExplanation) + "\",\"isMonteCarlo\":true}";
            } else if (type.equals("fks")) {
                long[] keys = {101L, 205L, 309L, 412L, 550L, 680L, 777L};
                RandomisedParallelEngine.FKSTwoLevelTable fks = new RandomisedParallelEngine.FKSTwoLevelTable(keys);
                result = "{\"keys\":" + longArrayToJson(keys) + ",\"primaryBuckets\":" + fks.primaryM + ",\"worstCaseLookupTime\":\"O(1) Guaranteed\"}";
            } else if (type.equals("reservoir")) {
                List<String> stream = Arrays.asList("alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota");
                int k = 4;
                RandomisedParallelEngine.ReservoirResult<String> res = RandomisedParallelEngine.reservoirSample(stream, k, System.currentTimeMillis());
                result = "{\"sample\":" + stringListToJson(res.sample) + ",\"elementsSeen\":" + res.streamElementsSeen + ",\"uniformProbability\":" + res.uniformProbabilityPerItem + "}";
            } else if (type.equals("blelloch")) {
                int[] input = {3, 1, 7, 0, 4, 1, 6, 3};
                int[] scan = RandomisedParallelEngine.blellochParallelScan(input);
                result = "{\"input\":" + intArrayToJson(input) + ",\"exclusivePrefixSum\":" + intArrayToJson(scan) + ",\"work\":\"O(n)\",\"span\":\"O(log n)\"}";
            } else if (type.equals("parallelsearch")) {
                String pattern = params.getOrDefault("pattern", "banana");
                String text = params.getOrDefault("text", "banana is a yellow fruit. eating a banana gives energy. banana banana!");
                int cores = Runtime.getRuntime().availableProcessors();
                RandomisedParallelEngine.ParallelSearchResult ps = RandomisedParallelEngine.parallelTextSearch(text, pattern, cores);
                result = "{\"matches\":" + listIntToJson(ps.matchPositions) + ",\"matchCount\":" + ps.matchPositions.size() + ",\"executionTimeNs\":" + ps.executionTimeNs + ",\"chunksUsed\":" + ps.chunksUsed + ",\"workT1\":" + ps.metrics.workT1 + ",\"spanTInf\":" + ps.metrics.spanTInf + ",\"brentSpeedupCeiling\":" + ps.metrics.brentSpeedupCeiling + "}";
            }

            sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + result + "}");
        }
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseJsonBody(HttpExchange exchange) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (InputStream is = exchange.getRequestBody();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String raw = sb.toString().trim();
            if (raw.startsWith("{") && raw.endsWith("}")) {
                raw = raw.substring(1, raw.length() - 1);
                String[] parts = raw.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (String p : parts) {
                    int colon = p.indexOf(':');
                    if (colon > 0) {
                        String k = p.substring(0, colon).trim().replaceAll("^\"|\"$", "");
                        String v = p.substring(colon + 1).trim().replaceAll("^\"|\"$", "");
                        map.put(k, v);
                    }
                }
            }
        }
        return map;
    }

    private static byte[] readFileToByteArray(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toByteArray();
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String intArrayToJson(int[] arr) {
        if (arr == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private static String longArrayToJson(long[] arr) {
        if (arr == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private static String listIntToJson(List<Integer> list) {
        if (list == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String setIntToJson(Set<Integer> set) {
        if (set == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (int item : set) {
            if (!first) sb.append(",");
            first = false;
            sb.append(item);
        }
        sb.append("]");
        return sb.toString();
    }

    private static String stringListToJson(List<String> list) {
        if (list == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(list.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String matrixToJson(int[][] mat) {
        if (mat == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < mat.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(intArrayToJson(mat[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
