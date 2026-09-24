/**
 * RunAllTests - Master test runner executing all algorithm test suites.
 */
public class RunAllTests {
    public static void main(String[] args) {
        System.out.println("############################################################");
        System.out.println("#      TEXTHACK MASTER ALGORITHM TEST VERIFICATION         #");
        System.out.println("############################################################");

        System.out.println("\n>>> [SUITE 1/3] Running Phase 1 (CorpusLoader, KMP, Z-Function):");
        TestPhase1.main(args);

        System.out.println("\n>>> [SUITE 2/3] Running Suffix & Multi-Pattern Mining Suite (RK, AC, SA, Kasai, PatternMiner):");
        TestPhasesUntilDP.main(args);

        System.out.println("\n>>> [SUITE 3/3] Running Dynamic Programming Suite (Wagner-Fischer Edit Distance):");
        TestDPPhase.main(args);

        System.out.println("\n############################################################");
        System.out.println("#      ALL ALGORITHMIC SUITES COMPLETED AND VERIFIED!      #");
        System.out.println("############################################################");
    }
}
