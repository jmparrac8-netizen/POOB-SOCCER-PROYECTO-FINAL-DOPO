package TEST;

// ParraJ
// Corre todas las pruebas de unidad, una clase despues de otra.
public class AllTests
{
    public static void main(String[] args)
    {
        int failed = 0;
        Check.reset();
        failed += BallTest.run();
        Check.reset();
        failed += GoalTest.run();
        Check.reset();
        failed += PlayerTest.run();
        Check.reset();
        failed += PoobSoccerTest.run();
        Check.reset();
        failed += CharacterTest.run();
        Check.reset();
        failed += MatchTest.run();
        Check.reset();
        failed += PowerTest.run();
        System.out.println();
        System.out.println(failed == 0 ? "TODAS LAS PRUEBAS PASARON" : "HAY PRUEBAS QUE FALLARON: " + failed);
        System.exit(failed == 0 ? 0 : 1);
    }
}
