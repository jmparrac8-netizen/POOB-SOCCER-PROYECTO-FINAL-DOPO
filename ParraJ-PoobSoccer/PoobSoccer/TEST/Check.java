package TEST;

// ParraJ
// Verificador minimo para las pruebas de unidad (sin JUnit).
// Cuenta las pruebas que pasan y las que fallan.
public class Check
{
    public static final double DELTA = 1e-9;

    private static int passed = 0;
    private static int failed = 0;

    // Registra el resultado de una prueba
    public static void that(String name, boolean condition)
    {
        if (condition) {
            passed++;
            System.out.println("  OK    " + name);
        } else {
            failed++;
            System.out.println("  FALLA " + name);
        }
    }

    // Verifica que dos numeros sean iguales
    public static void equal(String name, double expected, double actual)
    {
        boolean same = Math.abs(expected - actual) <= DELTA;
        that(same ? name : name + " (esperado " + expected + ", real " + actual + ")", same);
    }

    // Verifica que una prueba no lance excepciones
    public static void noError(String name, Runnable test)
    {
        try {
            test.run();
        } catch (Throwable t) {
            failed++;
            System.out.println("  FALLA " + name + " (excepcion " + t + ")");
        }
    }

    // Imprime el resumen y devuelve cuantas fallaron
    public static int summary()
    {
        System.out.println("Pasaron: " + passed + "  Fallaron: " + failed);
        return failed;
    }

    // Reinicia los contadores
    public static void reset()
    {
        passed = 0;
        failed = 0;
    }
}
