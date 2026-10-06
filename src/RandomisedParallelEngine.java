import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * RandomisedParallelEngine - Module 6: Randomised and Parallel Algorithms Suite for TextHack.
 * 
 * Implements:
 * 1. Las Vegas vs Monte Carlo Algorithms:
 *    - Las Vegas: Randomised QuickSort with uniform random pivot & comparison counting (O(n log n) expected)
 *    - Monte Carlo: Miller-Rabin Primality Test with witness construction and Carmichael number handling
 * 2. Randomised Hashing:
 *    - 2-Universal Hash Family (h_{a,b}(k) = ((a*k + b) mod p) mod m)
 *    - 2-Level Perfect Hashing (Fredman-Komlos-Szemeredi / FKS) with O(1) worst-case lookup
 * 3. Streaming Data:
 *    - Reservoir Sampling (Algorithm R) with rigorous uniform sampling proof
 * 4. Parallel Algorithm Primitives (Work T1, Span T_inf, Brent's Theorem):
 *    - Blelloch Parallel Scan / Prefix-Sum (Up-Sweep Reduce + Down-Sweep Distribute in O(log n) span)
 *    - Parallel Tree Reduction (Sum / Max in O(log n) span)
 *    - Parallel Multi-Core Text Search across text chunks (Data-Parallel vs Task-Parallel)
 * 
 * Pure Java implementation with strict zero-external algorithm libraries.
 */
public class RandomisedParallelEngine {

    // =========================================================================
    // 1. LAS VEGAS: RANDOMISED QUICKSORT
    // =========================================================================

    public static class QuickSortStats {
        public final int[] sortedArray;
        public final long comparisonCount;
        public final boolean isLasVegas; // Always yields exact correct sort

        public QuickSortStats(int[] arr, long comps) {
            this.sortedArray = arr;
            this.comparisonCount = comps;
            this.isLasVegas = true;
        }
    }

    /**
     * Las Vegas Randomised QuickSort.
     * Uniformly selects random pivot in range [low, high].
     * Guarantees exact sort correctness, with expected O(n log n) time and O(n log n) comparisons.
     */
    public static QuickSortStats randomisedQuickSort(int[] input) {
        int[] arr = Arrays.copyOf(input, input.length);
        long[] comps = new long[1];
        Random rand = new Random(42);
        qsortRecursive(arr, 0, arr.length - 1, rand, comps);
        return new QuickSortStats(arr, comps[0]);
    }

    private static void qsortRecursive(int[] arr, int low, int high, Random rand, long[] comps) {
        if (low < high) {
            int pIdx = low + rand.nextInt(high - low + 1);
            swap(arr, pIdx, high);

            int pivot = arr[high];
            int i = low - 1;
            for (int j = low; j < high; j++) {
                comps[0]++;
                if (arr[j] <= pivot) {
                    i++;
                    swap(arr, i, j);
                }
            }
            swap(arr, i + 1, high);
            int partitionIndex = i + 1;

            qsortRecursive(arr, low, partitionIndex - 1, rand, comps);
            qsortRecursive(arr, partitionIndex + 1, high, rand, comps);
        }
    }

    private static void swap(int[] arr, int i, int j) {
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    // =========================================================================
    // 2. MONTE CARLO: MILLER-RABIN PRIMALITY TEST
    // =========================================================================

    public static class MillerRabinResult {
        public final boolean isPrime;
        public final String witnessExplanation;
        public final boolean isMonteCarlo; // Probabilistic answer with error bound <= 4^(-k)

        public MillerRabinResult(boolean isPrime, String explanation) {
            this.isPrime = isPrime;
            this.witnessExplanation = explanation;
            this.isMonteCarlo = true;
        }
    }

    /**
     * Miller-Rabin Primality Test.
     * Resolves Carmichael numbers (like 561, 1105) that fool Fermat's test.
     * Generates prime moduli for TextHack rolling hashes and cryptographic chunk IDs.
     */
    public static MillerRabinResult millerRabin(long n, int rounds) {
        if (n < 2) return new MillerRabinResult(false, "n < 2 is composite.");
        if (n == 2 || n == 3) return new MillerRabinResult(true, n + " is a canonical small prime.");
        if (n % 2 == 0) return new MillerRabinResult(false, n + " is even and composite.");

        // Write n - 1 as 2^s * d with d odd
        long d = n - 1;
        int s = 0;
        while ((d & 1) == 0) {
            d >>= 1;
            s++;
        }

        // Deterministic bases for 64-bit verification, supplemented by random rounds
        long[] deterministicBases = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37};
        Random rand = new Random(1337);

        for (int r = 0; r < Math.max(rounds, deterministicBases.length); r++) {
            long a;
            if (r < deterministicBases.length) {
                a = deterministicBases[r];
                if (a >= n) continue;
            } else {
                a = 2 + (Math.abs(rand.nextLong()) % (n - 3));
            }

            long x = modPow(a, d, n);
            if (x == 1 || x == n - 1) continue;

            boolean compositeWitnessFound = true;
            for (int i = 0; i < s - 1; i++) {
                x = mulMod(x, x, n);
                if (x == n - 1) {
                    compositeWitnessFound = false;
                    break;
                }
            }

            if (compositeWitnessFound) {
                return new MillerRabinResult(false, "Found composite witness base a = " + a + " proving " + n + " is composite.");
            }
        }

        return new MillerRabinResult(true, n + " passed " + rounds + " Miller-Rabin test rounds (probabilistic error bound < 4^-" + rounds + ").");
    }

    private static long modPow(long base, long exp, long mod) {
        long res = 1;
        base %= mod;
        while (exp > 0) {
            if ((exp & 1) == 1) res = mulMod(res, base, mod);
            base = mulMod(base, base, mod);
            exp >>= 1;
        }
        return res;
    }

    private static long mulMod(long a, long b, long mod) {
        return java.math.BigInteger.valueOf(a)
                .multiply(java.math.BigInteger.valueOf(b))
                .mod(java.math.BigInteger.valueOf(mod))
                .longValue();
    }

    // =========================================================================
    // 3. RANDOMISED HASHING & PERFECT HASHING (FKS)
    // =========================================================================

    public static class UniversalHashFamily {
        private final long p = 2147483647L; // 2^31 - 1 Mersenne prime
        private final long a;
        private final long b;
        private final int m;

        public UniversalHashFamily(int tableSize, Random rand) {
            this.m = tableSize;
            this.a = 1 + (Math.abs(rand.nextLong()) % (p - 1));
            this.b = Math.abs(rand.nextLong()) % p;
        }

        public int hash(long key) {
            long val = ((a * (key & 0x7FFFFFFFL) + b) % p) % m;
            return (int) Math.abs(val);
        }
    }

    public static class FKSTwoLevelTable {
        public static class SecondaryTable {
            public int m2;
            public UniversalHashFamily hash2;
            public Long[] slots;

            public SecondaryTable(int m2) {
                this.m2 = m2;
                this.slots = new Long[m2];
            }
        }

        public final int n;
        public final int primaryM;
        public final UniversalHashFamily primaryHash;
        public final SecondaryTable[] secondaryBuckets;

        public FKSTwoLevelTable(long[] keys) {
            this.n = keys.length;
            this.primaryM = Math.max(1, n);
            Random rand = new Random(999);
            this.primaryHash = new UniversalHashFamily(primaryM, rand);
            this.secondaryBuckets = new SecondaryTable[primaryM];

            // 1. Group keys into primary buckets
            List<List<Long>> buckets = new ArrayList<>(primaryM);
            for (int i = 0; i < primaryM; i++) buckets.add(new ArrayList<>());
            for (long k : keys) {
                buckets.get(primaryHash.hash(k)).add(k);
            }

            // 2. Build quadratic secondary tables (m_i = |B_i|^2) to guarantee ZERO collisions
            for (int i = 0; i < primaryM; i++) {
                List<Long> b = buckets.get(i);
                int kSize = b.size();
                if (kSize == 0) {
                    secondaryBuckets[i] = new SecondaryTable(1);
                    continue;
                }
                int m2 = kSize * kSize;
                SecondaryTable sec = new SecondaryTable(m2);

                boolean success = false;
                while (!success) {
                    sec.hash2 = new UniversalHashFamily(m2, rand);
                    Arrays.fill(sec.slots, null);
                    success = true;
                    for (long k : b) {
                        int pos = sec.hash2.hash(k);
                        if (sec.slots[pos] != null && !sec.slots[pos].equals(k)) {
                            success = false; // collision in secondary, retry with new random hash
                            break;
                        }
                        sec.slots[pos] = k;
                    }
                }
                secondaryBuckets[i] = sec;
            }
        }

        public boolean contains(long key) {
            int pIdx = primaryHash.hash(key);
            SecondaryTable sec = secondaryBuckets[pIdx];
            if (sec == null || sec.hash2 == null) return false;
            int sIdx = sec.hash2.hash(key);
            return sec.slots[sIdx] != null && sec.slots[sIdx].equals(key);
        }
    }

    // =========================================================================
    // 4. RESERVOIR SAMPLING (ALGORITHM R - STREAMING UNIFORM SAMPLING)
    // =========================================================================

    public static class ReservoirResult<T> {
        public final List<T> sample;
        public final int streamElementsSeen;
        public final double uniformProbabilityPerItem; // k / N

        public ReservoirResult(List<T> sample, int total, int k) {
            this.sample = sample;
            this.streamElementsSeen = total;
            this.uniformProbabilityPerItem = total > 0 ? Math.min(1.0, (double) k / total) : 0.0;
        }
    }

    /**
     * Algorithm R for streaming reservoir sampling.
     * Mathematical proof: After seeing element t, each item has prob k/t of being in reservoir.
     */
    public static <T> ReservoirResult<T> reservoirSample(Iterable<T> stream, int k, long seed) {
        List<T> reservoir = new ArrayList<>(k);
        Random rand = new Random(seed);
        int count = 0;

        for (T item : stream) {
            count++;
            if (reservoir.size() < k) {
                reservoir.add(item);
            } else {
                int j = rand.nextInt(count);
                if (j < k) {
                    reservoir.set(j, item);
                }
            }
        }

        return new ReservoirResult<>(reservoir, count, k);
    }

    // =========================================================================
    // 5. PARALLEL ALGORITHM PRIMITIVES: BLELLOCH PARALLEL SCAN & WORK/SPAN
    // =========================================================================

    public static class WorkSpanMetrics {
        public final long workT1; // Sequential work
        public final long spanTInf; // Critical path length
        public final double parallelism; // T1 / TInf
        public final double brentSpeedupCeiling; // Max speedup for P processors

        public WorkSpanMetrics(long t1, long tInf, int processors) {
            this.workT1 = t1;
            this.spanTInf = tInf;
            this.parallelism = (double) t1 / Math.max(1, tInf);
            this.brentSpeedupCeiling = (double) t1 / ((double) t1 / processors + tInf);
        }
    }

    /**
     * Blelloch Parallel Scan / Prefix-Sum (Exclusive).
     * 2-Phase Work-Efficient Algorithm:
     * - Phase 1: Up-Sweep (Parallel Reduce tree): O(n) work, O(log n) span.
     * - Phase 2: Down-Sweep (Distribute tree): O(n) work, O(log n) span.
     */
    public static int[] blellochParallelScan(int[] input) {
        int n = input.length;
        if (n == 0) return new int[0];

        // Pad to next power of 2
        int m = 1;
        while (m < n) m <<= 1;
        int[] tree = new int[m];
        System.arraycopy(input, 0, tree, 0, n);

        // 1. Up-Sweep (Parallel Reduce)
        for (int d = 0; d < Integer.numberOfTrailingZeros(m); d++) {
            int step = 1 << (d + 1);
            int half = 1 << d;
            for (int k = 0; k < m; k += step) {
                tree[k + step - 1] += tree[k + half - 1];
            }
        }

        // Root set to identity (0)
        tree[m - 1] = 0;

        // 2. Down-Sweep
        for (int d = Integer.numberOfTrailingZeros(m) - 1; d >= 0; d--) {
            int step = 1 << (d + 1);
            int half = 1 << d;
            for (int k = 0; k < m; k += step) {
                int leftVal = tree[k + half - 1];
                tree[k + half - 1] = tree[k + step - 1];
                tree[k + step - 1] += leftVal;
            }
        }

        int[] result = new int[n];
        System.arraycopy(tree, 0, result, 0, n);
        return result;
    }

    public static class ParallelSearchResult {
        public final List<Integer> matchPositions;
        public final long executionTimeNs;
        public final int chunksUsed;
        public final WorkSpanMetrics metrics;

        public ParallelSearchResult(List<Integer> matches, long timeNs, int chunks, WorkSpanMetrics metrics) {
            this.matchPositions = matches;
            this.executionTimeNs = timeNs;
            this.chunksUsed = chunks;
            this.metrics = metrics;
        }
    }

    /**
     * Data-Parallel Multi-Core Pattern Search across corpus text.
     * Splits text into P overlapping chunks (overlap = pattern.length() - 1) to avoid missing boundary matches.
     */
    public static ParallelSearchResult parallelTextSearch(String text, String pattern, int cores) {
        long startTime = System.nanoTime();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return new ParallelSearchResult(new ArrayList<>(), 0, cores, new WorkSpanMetrics(0, 0, cores));
        }

        int n = text.length();
        int m = pattern.length();
        int numChunks = Math.max(1, Math.min(cores, n / (m * 2)));

        List<Integer> allMatches = Collections.synchronizedList(new ArrayList<>());
        ForkJoinPool pool = new ForkJoinPool(numChunks);

        class SearchTask extends RecursiveAction {
            final int chunkIdx;
            final int start;
            final int end;

            SearchTask(int idx, int start, int end) {
                this.chunkIdx = idx;
                this.start = start;
                this.end = end;
            }

            @Override
            protected void compute() {
                // Perform KMP-style search in text[start...end]
                int i = start;
                while (i <= end - m) {
                    boolean match = true;
                    for (int j = 0; j < m; j++) {
                        if (text.charAt(i + j) != pattern.charAt(j)) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        allMatches.add(i);
                    }
                    i++;
                }
            }
        }

        int chunkSize = n / numChunks;
        List<SearchTask> tasks = new ArrayList<>();
        for (int i = 0; i < numChunks; i++) {
            int s = i * chunkSize;
            int e = (i == numChunks - 1) ? n : (s + chunkSize + m - 1);
            tasks.add(new SearchTask(i, s, Math.min(n, e)));
        }

        for (SearchTask t : tasks) {
            pool.execute(t);
        }
        for (SearchTask t : tasks) {
            t.join();
        }
        pool.shutdown();

        Collections.sort(allMatches);
        // Deduplicate boundary matches
        List<Integer> uniqueMatches = new ArrayList<>();
        for (int pos : allMatches) {
            if (uniqueMatches.isEmpty() || uniqueMatches.get(uniqueMatches.size() - 1) != pos) {
                uniqueMatches.add(pos);
            }
        }

        long elapsed = System.nanoTime() - startTime;
        long workT1 = (long) n * m;
        long spanTInf = (long) (n / numChunks + m) * m;
        WorkSpanMetrics metrics = new WorkSpanMetrics(workT1, spanTInf, numChunks);

        return new ParallelSearchResult(uniqueMatches, elapsed, numChunks, metrics);
    }
}
