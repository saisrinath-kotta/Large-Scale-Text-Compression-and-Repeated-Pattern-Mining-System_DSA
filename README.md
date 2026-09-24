# TextHack: Large-Scale Text Compression & Repeated Pattern Mining System

**Course:** DSA-3 (Advanced Algorithms)  
**Project Name:** TextHack  
**Language:** Pure Java (Zero third-party algorithm libraries, zero `java.util.*` data structures inside core algorithms)  
**Target Environment:** Windows Command Prompt (CMD)  

---

## 👥 Project Team

* **Anvith** — `2510030191`
* **Sai Srinath** — `2510030261`
* **Keerthana S** — `2510030039`
* **Keerthana V** — `2510030041`

---

## 📚 

### Module 1: TextHack as a System — The Advanced-Algorithm Question Bank
* **A Working Engine on Day One:** Users issue queries against an Indian-language Wikipedia corpus (Hindi, Telugu, English) and inspect results before delving into internal mechanics.
* **Query Classes & Algorithm Family Mapping:**
  * **Pattern Search** $\longrightarrow$ **String Algorithms** (KMP, Z-Algorithm, Rabin-Karp)
  * **Multi-Pattern Keyword Scan** $\longrightarrow$ **Automaton Matching** (Aho-Corasick)
  * **Repeated Pattern Mining** $\longrightarrow$ **Suffix Indexing** (Suffix Array + Kasai LCP)
  * **Fuzzy Matching / Alignment** $\longrightarrow$ **Dynamic Programming** (Wagner-Fischer Edit Distance)
  * *Document Similarity & Compression (Phase 2)* $\longrightarrow$ Suffix Factorisation & LZ-style parsing
  * *Citation-Flow Analysis (Phase 2)* $\longrightarrow$ Max-Flow
  * *Project Scheduling (Phase 2)* $\longrightarrow$ NP-Hard with Approximation
  * *Large-Prime Primality Testing (Phase 2)* $\longrightarrow$ Randomised Algorithms
* **Why the Advanced Canon Exists:** Linear and tree/graph algorithms from DSA-1/DSA-2 (BSTs, HashMaps, naive graphs) cannot solve sub-quadratic substring search, optimal sequence alignment, or capacity-constrained assignment.
* **Textbook Canon Territory:** Mapped to **CLRS Part VII**, **Kleinberg-Tardos Chapters 6–13**, and **Erickson Chapters 4–12**.
* **The Engine Constraint:** External collections or regex are strictly forbidden inside algorithmic engines; all structures and algorithms are hand-built.
* **Public API First:** Every algorithm defines strict theoretical time/space bounds before implementation.

### Module 2: String Algorithms & Suffix Structures
* **The String-Matching Problem:** Finding pattern $P$ in text $T$; why the naive $O(nm)$ algorithm fails on large corpora.
* **Knuth-Morris-Pratt (KMP):** The failure function (LPS array), why the search is strictly linear $O(n)$, and the preprocessing intuition ($len = lps[len-1]$).
* **Linear Z-Algorithm:** Modern linear-time alternative to KMP using an active $[L, R]$ window (Z-box) over $P + \$ + T$.
* **Rabin-Karp Rolling Hash:** Average-case linear running time $O(n+m)$, strict modular arithmetic discipline, double-hashing ($10^9+7$ and $10^9+9$) to prevent collisions, and exact character verification to eliminate false positives.
* **Aho-Corasick Automaton:** Multi-pattern search via Trie with BFS-constructed failure transitions and dictionary links.
* **Suffix Arrays:** Practical $O(n \log^2 n)$ prefix doubling implementation; conceptual overview of $O(n)$ SA-IS induced sorting.
* **Kasai's LCP Algorithm:** Linear $O(n)$ LCP computation reusing preceding suffix match lengths ($h - 1$).
* **Suffix Automata & Trees Preview:** Conceptual advantages and trade-offs (Suffix Trees require 20x–40x pointer memory; Suffix Automata require $O(n)$ transitions; Suffix Array + LCP provides the optimal $4n$ byte memory footprint).

### Module 3: Advanced Dynamic Programming
* **DP Framework Recap:** Overlapping subproblems and optimal substructure.
* **Edit Distance & Variants:** Levenshtein, Damerau-Levenshtein, weighted edit distance; the Wagner-Fischer 2D DP algorithm.
* **Sequence Alignment for Genomic Data:** Global alignment (Needleman-Wunsch), local alignment (Smith-Waterman), and affine gap-penalty discussions.
* **Interval DP:** Matrix-chain multiplication and optimal binary search trees ($O(n^3)$ and $O(n^2 \log n)$).
* **Bitmask DP:** Travelling Salesman Problem in $O(2^n \cdot n^2)$, Hamiltonian paths, and set cover.
* **Tree DP:** Subset sums on trees, diameter/centroid problems, and the tree rerooting technique.
* **SOS DP (Sum-Over-Subsets):** $O(n \cdot 2^n)$ evaluation for inclusion-exclusion and subset convolutions.
* **When DP is the Wrong Tool:**
  1. No overlapping subproblems (Divide & Conquer suffices).
  2. Greedy choice property holds (e.g., Huffman encoding, MST).
  3. Exponential state space without bitmask compression ($NP$-hard).

---

## 🏗 System Architecture

```
                    INDIAN LANGUAGE CORPUS (data/corpus.txt)
                                       │
                                       ▼
                        CorpusLoader (UTF-8 Reader)
                                       │
                ┌──────────────────────┼──────────────────────┐
                ▼                      ▼                      ▼
           Exact Search           Multi-Pattern          Fuzzy Search
        (KMP / Z / RK)           (Aho-Corasick)        (Wagner-Fischer DP)
                │                      │                      │
                └──────────────────────┴──────────────────────┘
                                       │
                                       ▼
                              Suffix Array Index
                            [O(n log² n) Doubling]
                                       │
                                       ▼
                                   Kasai LCP
                                 [O(n) Linear]
                                       │
                                       ▼
                             Repeated Pattern Miner
                              [LCP Interval Stack]
```

---

## 📂 Project Structure

```
TextHack/
├── data/
│   ├── corpus.txt               # Full multilingual corpus (Hindi, Telugu, English)
│   └── test_corpus.txt          # Verification corpus
├── src/
│   ├── CorpusLoader.java        # UTF-8 corpus reader & statistics
│   ├── KMP.java                 # Knuth-Morris-Pratt exact search
│   ├── ZFunction.java           # Linear-time Z-algorithm
│   ├── RabinKarp.java           # Double rolling hash with verification
│   ├── AhoCorasick.java         # Trie + BFS failure links multi-pattern search
│   ├── SuffixArray.java         # O(n log² n) prefix doubling suffix index
│   ├── KasaiLCP.java            # Linear O(n) LCP array via (h - 1) reuse
│   ├── PatternMiner.java        # Repeated pattern mining via LCP interval stack
│   ├── EditDistance.java        # Wagner-Fischer 2D DP edit distance
│   ├── TestPhase1.java          # Phase 1 unit test suite (26 assertions)
│   ├── TestPhasesUntilDP.java   # Suffix & pattern mining test suite (25 assertions)
│   ├── TestDPPhase.java         # Dynamic programming test suite (13 assertions)
│   └── RunAllTests.java         # Master test runner (64 assertions total)
├── bin/                         # Compiled bytecode (.class files)
├── compile.bat                  # Universal Windows CMD compilation script
├── run_tests.bat                # Windows CMD test execution script
├── generate_pptx.py             # Python script generating the presentation
├── TextHack_DSA3_Review.pptx    # Complete 12-slide review presentation
└── README.md                    # Project documentation
```

---

## ⚡ Quick Start: Windows CMD Commands

### 1. Compilation
Navigate to the project root in CMD:
```cmd
cd C:\Users\Lenovo\Downloads\DSA_Project
compile.bat
```
*(Or via standard javac: `javac --release 8 -encoding UTF-8 -d bin -sourcepath src src\*.java`)*

### 2. Run All Automated Unit Tests (64/64 Pass)
```cmd
java -cp bin RunAllTests
```

### 3. Run Individual Algorithm Demonstrations
```cmd
# Exact String Search
java -cp bin KMP ABABABAB ABAB
java -cp bin ZFunction ABABABAB ABAB
java -cp bin RabinKarp ABABABAB ABAB

# Multi-Pattern Search
java -cp bin AhoCorasick ushers he she his hers

# Suffix Array & Kasai LCP
java -cp bin SuffixArray banana
java -cp bin KasaiLCP banana

# Repeated Substring Mining (Min Length 6)
java -cp bin PatternMiner "banana is a banana. banana patterns are repeated." 6

# Dynamic Programming Edit Distance
java -cp bin EditDistance kitten sitting

# Multilingual Corpus Ingestion
java -cp bin CorpusLoader data/corpus.txt
```

---

## 📊 Asymptotic Complexity Table

| Algorithm | Module | Time Complexity | Space Complexity | Practical Role |
| :--- | :--- | :--- | :--- | :--- |
| **KMP** | Module 2 (Strings) | $O(n + m)$ | $O(m)$ | Linear single-pattern exact search |
| **Z-Algorithm** | Module 2 (Strings) | $O(n + m)$ | $O(n + m)$ | Linear $[L, R]$ window exact search |
| **Rabin-Karp** | Module 2 & 6 | Average $O(n + m)$ | $O(1)$ | Double rolling hash + character verification |
| **Aho-Corasick** | Module 2 (Strings) | $O(n + \sum m + z)$ | $O(\sum m)$ | Multi-pattern Trie + BFS scanning |
| **Suffix Array** | Module 2 (Strings) | $O(n \log^2 n)$ | $O(n)$ | Practical prefix doubling text index |
| **Kasai LCP** | Module 2 (Strings) | $O(n)$ | $O(n)$ | Linear LCP build reusing $(h - 1)$ |
| **Pattern Miner**| Module 2 (Strings) | $O(n)$ on SA+LCP | $O(\text{repeats})$ | Repeated substring mining via interval stack |
| **Edit Distance**| Module 3 (DP) | $O(m \cdot n)$ | $O(m \cdot n)$ | Wagner-Fischer 2D DP fuzzy matching |

---

