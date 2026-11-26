package DOMINIO;

// ParraJ
// Efecto que un tiro de poder deja en el rival que golpea.
// Un jugador solo tiene un efecto a la vez: el nuevo reemplaza al anterior.
public enum Effect
{
    NONE,          // sin efecto
    ELECTROCUTED,  // Camerun: no puede saltar y se mueve al 50 % de su velocidad
    KNOCKED_OUT    // Nigeria: lanzado al aire, no puede moverse, saltar ni patear
}
