import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * AhoCorasick - Multi-Pattern Exact String Matching Automaton.
 * 
 * Algorithm Concept:
 * The Aho-Corasick algorithm constructs a finite state machine that matches multiple
 * patterns simultaneously in a single pass over the input text.
 * 
 * Core Components:
 * 1. Trie (Prefix Tree):
 *    All search patterns are inserted into a keyword tree where edges represent characters.
 * 2. Failure Links (Suffix Transitions):
 *    Constructed via Breadth-First Search (BFS). If a mismatch occurs, the failure link
 *    directs the automaton to the longest proper suffix of the current prefix that exists
 *    in the trie (analogous to KMP's LPS, but generalized for trees).
 * 3. Output Links / Dictionary Collection:
 *    Each state collects patterns that end at that node, as well as patterns ending at
 *    all states reachable via its failure links.
 * 
 * Preprocessing Time:  O(M) where M = sum of lengths of all patterns
 * Search Time:         O(n + M + z) where n = text length, z = number of occurrences
 * Space Complexity:    O(M) for the trie states and transitions
 */
public class AhoCorasick {

    /**
     * Represents a single node / state in the Aho-Corasick Trie.
     */
    public static class Node {
        // Character transitions to child nodes
        Map<Character, Node> children;
        // Failure link: points to the state representing the longest proper suffix
        Node fail;
        // List of pattern strings that match when arriving at this state
        List<String> output;

        public Node() {
            this.children = new HashMap<>();
            this.fail = null;
            this.output = new ArrayList<>();
        }
    }

    private final Node root;
    private final List<String> patterns;

    public AhoCorasick() {
        this.root = new Node();
        this.patterns = new ArrayList<>();
    }

    public AhoCorasick(List<String> patternList) {
        this.root = new Node();
        this.patterns = new ArrayList<>();
        if (patternList != null) {
            for (String p : patternList) {
                addPattern(p);
            }
            buildFailureLinks();
        }
    }

    /**
     * Adds a pattern into the trie.
     */
    public void addPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return;
        }

        if (!patterns.contains(pattern)) {
            patterns.add(pattern);
        }

        Node curr = root;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (!curr.children.containsKey(c)) {
                curr.children.put(c, new Node());
            }
            curr = curr.children.get(c);
        }

        // Mark the pattern ending at this node
        curr.output.add(pattern);
    }

    /**
     * Builds failure links using Breadth-First Search (BFS).
     * 
     * Why BFS is required (Viva point):
     * The failure link of a node at depth d points to a node at depth < d.
     * Therefore, all shallower failure links must be completely computed before
     * computing failure links at the current depth!
     */
    public void buildFailureLinks() {
        Queue<Node> queue = new ArrayDeque<>();

        // Step 1: Depth 1 children of root have failure link pointing to root
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        // Step 2: BFS for deeper nodes
        while (!queue.isEmpty()) {
            Node curr = queue.poll();

            for (Map.Entry<Character, Node> entry : curr.children.entrySet()) {
                char ch = entry.getKey();
                Node child = entry.getValue();

                // Find failure state for child by tracing failure links from curr
                Node fallback = curr.fail;
                while (fallback != null && !fallback.children.containsKey(ch)) {
                    fallback = fallback.fail;
                }

                if (fallback != null) {
                    child.fail = fallback.children.get(ch);
                } else {
                    child.fail = root;
                }

                // Suffix output collection:
                // If the failure state represents a completed pattern,
                // that pattern also matches at this child state!
                if (child.fail != null && !child.fail.output.isEmpty()) {
                    child.output.addAll(child.fail.output);
                }

                queue.add(child);
            }
        }
    }

    /**
     * Searches the text in a single pass for all configured patterns.
     * 
     * @param text The input text to search.
     * @return A map of each pattern to the list of 0-based starting match positions.
     */
    public Map<String, List<Integer>> search(String text) {
        // Initialize results with empty lists for all patterns
        Map<String, List<Integer>> results = new LinkedHashMap<>();
        for (String p : patterns) {
            results.put(p, new ArrayList<>());
        }

        if (text == null || text.isEmpty() || patterns.isEmpty()) {
            return results;
        }

        Node state = root;
        int n = text.length();

        for (int i = 0; i < n; i++) {
            char c = text.charAt(i);

            // Follow failure links until a valid transition for char c is found or root reached
            while (state != null && !state.children.containsKey(c)) {
                state = state.fail;
            }

            if (state == null) {
                state = root;
                continue;
            }

            // Transition to the matching child state
            state = state.children.get(c);

            // Collect all matching patterns that end at this state
            if (!state.output.isEmpty()) {
                for (String matchedPattern : state.output) {
                    // Start position in text: current index - pattern length + 1
                    int startPos = i - matchedPattern.length() + 1;
                    results.get(matchedPattern).add(startPos);
                }
            }
        }

        return results;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    /**
     * Standalone main method to test AhoCorasick from CMD.
     */
    public static void main(String[] args) {
        String text = (args.length > 0) ? args[0] : "ushers";
        List<String> keywords = new ArrayList<>();
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                keywords.add(args[i]);
            }
        } else {
            // Classic Aho-Corasick example
            keywords.add("he");
            keywords.add("she");
            keywords.add("his");
            keywords.add("hers");
        }

        System.out.println("============================================================");
        System.out.println("AHO-CORASICK MULTI-PATTERN SEARCH DEMO");
        System.out.println("============================================================");
        System.out.println("Text:             " + text);
        System.out.println("Patterns:         " + keywords);

        AhoCorasick ac = new AhoCorasick(keywords);

        long startTime = System.nanoTime();
        Map<String, List<Integer>> matches = ac.search(text);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;

        System.out.println("\nResults:");
        for (Map.Entry<String, List<Integer>> entry : matches.entrySet()) {
            System.out.printf("Pattern: %-12s | Matches: %2d | Positions: %s\n",
                    entry.getKey(), entry.getValue().size(), entry.getValue());
        }

        System.out.printf("\nTime:             %.4f ms\n", elapsedMs);
        System.out.println("Complexity:       O(n + total pattern length + matches)");
        System.out.println("============================================================");
    }
}
