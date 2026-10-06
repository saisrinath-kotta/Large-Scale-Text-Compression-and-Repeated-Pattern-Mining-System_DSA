/**
 * RunAllTests - Master test runner executing all algorithm test suites.
 */
public class RunAllTests {
    public static void main(String[] args) {
        System.out.println("############################################################");
        System.out.println("#      TEXTHACK MASTER ALGORITHM TEST VERIFICATION         #");
        System.out.println("############################################################");

        System.out.println("\n>>> [SUITE 1/4] Running Phase 1 (CorpusLoader, KMP, Z-Function):");
        TestPhase1.main(args);

        System.out.println("\n>>> [SUITE 2/4] Running Suffix & Multi-Pattern Mining Suite (RK, AC, SA, Kasai, PatternMiner):");
        TestPhasesUntilDP.main(args);

        System.out.println("\n>>> [SUITE 3/4] Running Dynamic Programming Suite (Wagner-Fischer Edit Distance):");
        TestDPPhase.main(args);

        System.out.println("\n>>> [SUITE 4/4] Running Full Syllabus Master Suite (Modules 3, 4, 5, 6):");
        TestMasterSuite.main(args);

        System.out.println("\n############################################################");
        System.out.println("#      ALL 4 ALGORITHMIC SUITES COMPLETED AND VERIFIED!    #");
        System.out.println("############################################################");
    }
}
