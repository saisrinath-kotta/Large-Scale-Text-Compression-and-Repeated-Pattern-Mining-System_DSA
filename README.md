# TextHack: Large-Scale Text Compression & Advanced DSA-3 System

**Course:** DSA-3 (Advanced Algorithms)  
**Project Name:** TextHack Enterprise  
**Language:** Pure Java (Zero third-party algorithm libraries, zero `java.util.*` data structures inside core algorithms)  
**Target Environment:** Windows Command Prompt (CMD) & Modern Web Browser  

---

## 👥 Project Team

* **Anvith** — `2510030191`
* **Sai Srinath** — `2510030261`
* **Keerthana S** — `2510030039`
* **Keerthana V** — `2510030041`

---

## 📚 Complete DSA-3 Syllabus Mapping (Modules 1 to 6)

### Module 1: TextHack as a System — The Advanced-Algorithm Question Bank
* **Multilingual Corpus Pipeline:** UTF-8 ingestion of Hindi, Telugu, and English text preserving Indian Unicode scripts.
* **Algorithmic Engine Architecture:** Core algorithms built with zero external libraries.
* **Mapped Textbook Canon:** **CLRS Part VII**, **Kleinberg-Tardos Chapters 6–13**, and **Erickson Chapters 4–12**.

### Module 2: String Algorithms & Suffix Structures
* **Knuth-Morris-Pratt (KMP):** Linear $O(n)$ search via Longest Prefix-Suffix (LPS) array skipping redundant rollbacks.
* **Linear Z-Algorithm:** Linear-time exact matching using active $[L, R]$ window (Z-box) over $P + \$ + T$.
* **Rabin-Karp Rolling Hash:** Average $O(n+m)$ double rolling hash ($10^9+7$, $10^9+9$) with exact character verification.
* **Aho-Corasick Automaton:** Multi-pattern Trie with BFS failure and dictionary transitions in $O(n + \sum m + z)$.
* **Suffix Arrays:** Prefix doubling $O(n \log^2 n)$ index with compact $4n$ byte memory footprint.
* **Kasai's LCP Algorithm:** Linear $O(n)$ LCP computation reusing preceding suffix match lengths ($h - 1$).
* **Repeated Pattern Miner:** $O(n)$ interval decomposition stack discovering all maximal repeated substrings.

### Module 3: Advanced Dynamic Programming (`AdvancedDP.java`)
* **Edit Distance & Variants:**
  * Levenshtein Distance & Wagner-Fischer 2D DP.
  * Damerau-Levenshtein Distance supporting adjacent transpositions.
  * Weighted Edit Distance with custom insert, delete, substitution, and transposition costs.
* **Sequence Alignment for Genomic & Text Data:**
  * **Needleman-Wunsch:** Global alignment with scoring matrix and gap penalties.
  * **Smith-Waterman:** Local sequence alignment finding highest scoring sub-regions.
* **Interval DP:**
  * **Matrix-Chain Multiplication:** $O(n^3)$ optimal parenthesization recovery.
  * **Optimal Binary Search Tree (OBST):** Knuth-style $O(n^3)$ DP for search frequencies.
* **Bitmask DP:**
  * **Travelling Salesman Problem (TSP):** $O(2^n \cdot n^2)$ exact Hamiltonian cycle tour.
  * **Hamiltonian Path Detection:** $O(2^n \cdot n^2)$ connectivity verification.
* **Tree DP:**
  * **Maximum Weight Independent Set on Trees:** Two-state include/exclude DP.
  * **Tree Diameter:** Bottom-up longest path computation.
  * **Tree Rerooting Technique:** $O(n)$ all-roots sum of distances via 2-pass DFS.
* **SOS DP (Sum-Over-Subsets):**
  * Yates's algorithm computing submask feature sums in $O(n \cdot 2^n)$ for inclusion-exclusion.
* **Diagnostic Analyzer:** Formal evaluation checklist of when DP is the wrong tool (Greedy choice, no subproblems, exponential explosion).

### Module 4: Network Flow & Reductions (`NetworkFlowEngine.java`)
* **Core Flow Formulations:** Source, sink, integer capacities, conservation constraints.
* **Algorithms:**
  * **Ford-Fulkerson Method:** Augmenting paths in residual graph via DFS.
  * **Edmonds-Karp Algorithm:** BFS augmenting paths in $O(V \cdot E^2)$.
  * **Dinic's Algorithm:** BFS level graphs + DFS blocking flows with pointer pruning in $O(V^2 \cdot E)$.
* **Max-Flow Min-Cut Duality:** Identifies minimum cut capacity ($= \text{Max Flow}$) and partitions reachable $S$ and unreachable $T$.
* **Bipartite Matching & König's Theorem:** Flow reduction proving $|\text{Max Matching}| = |\text{Min Vertex Cover}|$ with cover extraction.
* **Min-Cost Max-Flow (MCMF):** Successive Shortest Path (SSP) with SPFA augmenting paths.
* **Practical Reductions:**
  * **Project Selection Problem:** Maximum weight closure via min-cut.
  * **Task Scheduling with Deadlines:** Worker capacity constraints as network flow.

### Module 5: NP-Completeness and Approximation (`NPApproximationEngine.java`)
* **Decision vs Optimization & Verification:**
  * Formal polynomial-time witness (certificate) verifiers for 3-SAT, Vertex Cover, and Subset-Sum.
* **The Reduction Zoo:**
  * $3\text{-SAT} \longrightarrow \text{Independent Set}$ gadget construction (triangles + conflict edges).
  * $\text{Independent Set} \longleftrightarrow \text{Vertex Cover}$ duality ($S$ is IS $\iff V \setminus S$ is VC).
  * $\text{Independent Set} \longrightarrow \text{Clique}$ on complement graph $G^c$.
  * Hamiltonian Cycle $\longrightarrow$ TSP reduction.
* **Approximation Algorithms with Ratio Guarantees:**
  * **2-Approximation for Minimum Vertex Cover:** Via Maximal Matching (guaranteed $\le 2 \cdot \text{OPT}$).
  * **2-Approximation for Metric TSP:** Via MST Doubling and Shortcut DFS.
  * **Greedy Set Cover:** Harmonic $H(n) \le \ln n + 1$ bound.
  * **Knapsack FPTAS:** Fully Polynomial-Time Approximation Scheme achieving $(1 - \epsilon) \cdot \text{OPT}$ in $O(n^3 / \epsilon)$ time.
* **Parameterized Complexity (FPT & Kernelization):**
  * **Buss's Rule Kernelization:** Degree $> k$ reduction bounding kernel edges to $k^2$.
  * **Bounded Search Tree FPT:** Exact Vertex Cover in $O(2^k \cdot n)$.

### Module 6: Randomised and Parallel Algorithms (`RandomisedParallelEngine.java`)
* **Las Vegas vs Monte Carlo:**
  * **Las Vegas:** Randomised QuickSort with uniform pivot selection and exact comparison tracking ($O(n \log n)$ expected).
  * **Monte Carlo:** Miller-Rabin Primality Test resolving Carmichael numbers (e.g. 561) with error bound $\le 4^{-k}$.
* **Randomised & Perfect Hashing:**
  * 2-Universal Hash Family: $h_{a,b}(x) = ((a \cdot x + b) \bmod p) \bmod m$.
  * **2-Level Perfect Hashing (FKS):** Primary table + quadratic secondary tables ($m_i = c_i^2$) guaranteeing $O(1)$ worst-case lookup with $O(n)$ space.
* **Streaming Data Algorithms:**
  * **Reservoir Sampling (Algorithm R):** Single-pass uniform sampling of $k$ items from unbounded corpus streams.
* **Parallel Algorithm Primitives (Work $T_1$, Span $T_\infty$, Brent's Theorem):**
  * **Blelloch Parallel Scan:** 2-phase up-sweep (reduce) and down-sweep tree scan with $O(n)$ work and $O(\log n)$ span.
  * **Data-Parallel Multi-Core Pattern Search:** Text chunking across CPU cores with theoretical work/span calculation and Brent speedup ceiling.

---

## ⚡ Quick Start: Windows CMD Commands

### 1. Build the Entire System
```cmd
compile.bat
```
*(Compiles all Java source files into `bin/` with UTF-8 encoding).*

### 2. Run All Automated Unit Tests (132 / 132 Tests Passing)
```cmd
run_tests.bat
```
*(Or via direct Java command: `java -Dfile.encoding=UTF-8 -cp bin RunAllTests`)*.

### 3. Launch the Full-Stack Web Application (Frontend + Backend)
```cmd
run_server.bat
```
*(Starts the HTTP server and opens `http://localhost:8081/` in your browser).*

---

## 📊 Asymptotic Complexity Table

| Algorithm | Module | Time Complexity | Space Complexity | Practical Role |
| :--- | :--- | :--- | :--- | :--- |
| **KMP** | Module 2 | $O(n + m)$ | $O(m)$ | Linear single-pattern exact search |
| **Z-Algorithm** | Module 2 | $O(n + m)$ | $O(n + m)$ | Linear $[L, R]$ window exact search |
| **Rabin-Karp** | Module 2 & 6 | Avg $O(n + m)$ | $O(1)$ | Double rolling hash + verification |
| **Aho-Corasick** | Module 2 | $O(n + \sum m + z)$ | $O(\sum m)$ | Multi-pattern Trie + BFS scanning |
| **Suffix Array** | Module 2 | $O(n \log^2 n)$ | $O(n)$ | Practical prefix doubling text index |
| **Kasai LCP** | Module 2 | $O(n)$ | $O(n)$ | Linear LCP build reusing $(h - 1)$ |
| **Pattern Miner** | Module 2 | $O(n)$ | $O(\text{repeats})$ | Repeated substring mining via interval stack |
| **Edit Distance** | Module 3 | $O(m \cdot n)$ | $O(m \cdot n)$ | Wagner-Fischer 2D DP fuzzy matching |
| **Needleman-Wunsch** | Module 3 | $O(m \cdot n)$ | $O(m \cdot n)$ | Global sequence alignment |
| **Smith-Waterman** | Module 3 | $O(m \cdot n)$ | $O(m \cdot n)$ | Local sequence alignment |
| **Matrix Chain DP** | Module 3 | $O(n^3)$ | $O(n^2)$ | Optimal parenthesization |
| **Bitmask TSP** | Module 3 | $O(2^n \cdot n^2)$ | $O(2^n \cdot n)$ | Exact minimum Hamiltonian tour |
| **Tree DP (MWIS)** | Module 3 | $O(n)$ | $O(n)$ | Independent set on hierarchy trees |
| **SOS DP (Yates)** | Module 3 | $O(n \cdot 2^n)$ | $O(2^n)$ | Sum-over-subsets feature lattice |
| **Dinic's Max-Flow** | Module 4 | $O(V^2 \cdot E)$ | $O(V + E)$ | Blocking flows on level graphs |
| **Bipartite Matching** | Module 4 | $O(V \cdot E)$ | $O(V + E)$ | König's max matching & min vertex cover |
| **MCMF (SSP)** | Module 4 | $O(F \cdot E \log V)$ | $O(V + E)$ | Min-cost max-flow assignment |
| **Vertex Cover 2-Approx** | Module 5 | $O(V + E)$ | $O(V)$ | Maximal matching 2-approximation |
| **Metric TSP 2-Approx** | Module 5 | $O(V^2)$ | $O(V)$ | MST doubling + shortcut DFS |
| **Knapsack FPTAS** | Module 5 | $O(n^3 / \epsilon)$ | $O(n^2 / \epsilon)$ | $(1-\epsilon)$ approximation scheme |
| **Buss Kernelization** | Module 5 | $O(V + E)$ | $O(k^2)$ | Parameterized FPT kernel reduction |
| **Randomised QuickSort** | Module 6 | Expected $O(n \log n)$ | $O(\log n)$ | Las Vegas exact sorting |
| **Miller-Rabin Test** | Module 6 | $O(k \log^3 n)$ | $O(1)$ | Monte Carlo primality testing |
| **FKS Perfect Hashing** | Module 6 | Worst-case $O(1)$ | $O(n)$ space | 2-level collision-free hash table |
| **Reservoir Sampling** | Module 6 | $O(N)$ streaming | $O(k)$ sample | Unbounded stream uniform sampling |
| **Blelloch Parallel Scan** | Module 6 | $O(n)$ work, $O(\log n)$ span | $O(n)$ | Work-efficient parallel prefix sum |

---

## 🎯 Viva Examination Preparation Highlights

1. **Why does KMP achieve strictly $O(n)$ search time?**  
   The text pointer monotonically advances from $0$ to $n-1$ and never decrements. Upon mismatch, only the pattern pointer falls back via $lps[j-1]$.
2. **Why does Kasai's algorithm take strict $O(n)$ time?**  
   Moving from suffix $i$ to $i+1$ drops common prefix length with predecessor by at most 1 ($h \ge h - 1$). Since $h$ decrements at most $n$ times and cannot exceed $n$, comparisons are bounded by $2n$.
3. **What is the difference between Las Vegas and Monte Carlo algorithms?**  
   Las Vegas algorithms (e.g. Randomised QuickSort) are always correct with randomized runtimes. Monte Carlo algorithms (e.g. Miller-Rabin) have bounded running times and are correct with high probability.
4. **How does FKS Perfect Hashing guarantee $O(1)$ worst-case lookup?**  
   Primary universal hashing maps $n$ items into $n$ buckets. Each bucket $i$ with $c_i$ items uses a secondary table of quadratic size $m_i = c_i^2$, which guarantees zero collisions by the Birthday Paradox while preserving $O(n)$ expected total space.
5. **How does König's Theorem derive from Max-Flow Min-Cut?**  
   Bipartite matching is modeled as a unit network flow. Residual reachability from the Source yields an $S-T$ cut; left vertices outside $S$ and right vertices inside $S$ form a vertex cover whose size equals the max flow and max matching.
6. **Explain Work, Span, and Brent's Theorem.**  
   Work $T_1$ is sequential operations; Span $T_\infty$ is the critical dependency path. Brent's theorem states $T_p \le (T_1 / P) + T_\infty$, proving the speedup ceiling is bounded by $T_1 / T_\infty$.
