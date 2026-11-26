package DOMINIO;

// ParraJ
// Jugador: se mueve de izquierda a derecha sobre el piso, salta y patea.
// El salto sube y baja solo por la gravedad.
// Dos jugadores no se pueden traspasar: cuando uno choca con el otro se detiene como contra una pared,
// y si cae encima de su cabeza se queda parado ahi.
// Tiene un personaje (con sus estadisticas fijas), vida, barra de poder y puede quedar aturdido
// o bajo el efecto de un tiro de poder del rival.
// (x, y) es la esquina superior izquierda y y crece hacia abajo.
// No sabe dibujarse: de eso se encarga la presentacion.
public class Player
{
    public static final double WIDTH = 60;
    public static final double HEIGHT = 90;
    public static final double FOOT_WIDTH = 50;
    public static final double FOOT_HEIGHT = 45;
    public static final double POWER_BAR_SECONDS = 10;
    public static final double GOAL_CONCEDED_BAR = 0.25;
    public static final double STUN_SECONDS = 3;

    private static final double SPEED = 6;
    private static final double JUMP_SPEED = -16;
    private static final double GRAVITY = 0.8;
    private static final double EPSILON = 1e-9;
    private static final double ELECTROCUTED_SPEED = 0.5;
    private static final int KICK_TICKS = 10;
    private static final int KICK_COOLDOWN_TICKS = 25;

    private final double startX;
    private final double startY;
    private final String color;
    private final GameCharacter character;
    private final boolean startFacingRight;

    private double x;
    private double y;
    private double vx;
    private double vy;
    private boolean movingLeft;
    private boolean movingRight;
    private boolean onGround;
    private boolean facingRight;

    private double life;
    private double powerBar;
    private boolean hasPower;
    private double stunTime;
    private Effect effect;
    private double effectTime;
    private int kickTicks;
    private int kickCooldown;
    private boolean kickResolved;

    // Crea un jugador del color dado en la posicion x, parado sobre el piso, con el personaje neutro
    public Player(double x, double groundY, String color)
    {
        this(x, groundY, color, GameCharacter.neutral(), true);
    }

    // Crea un jugador con el personaje dado. Empieza mirando hacia el lado indicado
    public Player(double x, double groundY, String color, GameCharacter character, boolean facingRight)
    {
        if (character.isUnderConstruction()) {
            throw new IllegalArgumentException("Personaje en construccion: " + character.getName());
        }
        this.startX = x;
        this.startY = groundY - HEIGHT;
        this.x = startX;
        this.y = startY;
        this.onGround = true;
        this.color = color;
        this.character = character;
        this.startFacingRight = facingRight;
        this.facingRight = facingRight;
        this.life = character.getMaxLife();
        this.effect = Effect.NONE;
    }

    // Devuelve al jugador a su lugar de salida, quieto y parado en el piso.
    // Las teclas que sigan presionadas siguen valiendo. La vida, la barra de poder y los efectos no cambian
    public void reset()
    {
        x = startX;
        y = startY;
        vx = 0;
        vy = 0;
        onGround = true;
        facingRight = startFacingRight;
        kickTicks = 0;
    }

    // Indica si el jugador debe moverse a la izquierda
    public void setMovingLeft(boolean movingLeft)
    {
        this.movingLeft = movingLeft;
    }

    // Indica si el jugador debe moverse a la derecha
    public void setMovingRight(boolean movingRight)
    {
        this.movingRight = movingRight;
    }

    // Indica si el jugador puede moverse y patear (no esta aturdido ni inconsciente)
    public boolean canAct()
    {
        return stunTime <= 0 && effect != Effect.KNOCKED_OUT;
    }

    // Hace saltar al jugador. No hace nada si ya esta en el aire, si no puede actuar o si esta electrocutado
    public void jump()
    {
        if (onGround && canAct() && effect != Effect.ELECTROCUTED) {
            vy = JUMP_SPEED * character.getJumpMultiplier();
            onGround = false;
        }
    }

    // Empieza una patada hacia donde mira el jugador. Devuelve false si no pudo patear
    // (esta aturdido, inconsciente o todavia esta pateando)
    public boolean kick()
    {
        if (!canAct() || kickTicks > 0 || kickCooldown > 0) {
            return false;
        }
        kickTicks = KICK_TICKS;
        kickCooldown = KICK_COOLDOWN_TICKS;
        kickResolved = false;
        return true;
    }

    // Indica si la patada esta en curso
    public boolean isKicking()
    {
        return kickTicks > 0;
    }

    // Indica si hay una patada en curso que todavia no le pego a nada
    public boolean isKickPending()
    {
        return kickTicks > 0 && !kickResolved;
    }

    // Marca que la patada en curso ya golpeo (al balon o al rival) y no vuelve a hacer dano
    public void markKickResolved()
    {
        kickResolved = true;
    }

    // Avanza al jugador un paso: se mueve, no se sale del campo y cae por la gravedad
    public void update(double fieldWidth, double groundY)
    {
        if (kickTicks > 0) {
            kickTicks--;
        }
        if (kickCooldown > 0) {
            kickCooldown--;
        }

        vx = 0;
        if (canAct()) {
            double speed = SPEED * character.getSpeedMultiplier();
            if (effect == Effect.ELECTROCUTED) {
                speed *= ELECTROCUTED_SPEED;
            }
            if (movingLeft) {
                vx -= speed;
            }
            if (movingRight) {
                vx += speed;
            }
            if (movingLeft && !movingRight) {
                facingRight = false;
            } else if (movingRight && !movingLeft) {
                facingRight = true;
            }
        }
        x += vx;
        keepInsideField(fieldWidth);

        if (onGround && y + HEIGHT < groundY - EPSILON) {
            onGround = false;
        }
        if (!onGround) {
            vy += GRAVITY;
            y += vy;
            if (y + HEIGHT >= groundY) {
                y = groundY - HEIGHT;
                vy = 0;
                onGround = true;
            }
        }
    }

    // Descuenta del tiempo real que paso: carga la barra de poder y termina el aturdimiento y los efectos.
    // Al terminar el aturdimiento la vida vuelve a su maximo
    public void advanceTime(double seconds)
    {
        powerBar = Math.min(1, powerBar + seconds / POWER_BAR_SECONDS);

        if (stunTime > 0) {
            stunTime -= seconds;
            if (stunTime <= 0) {
                stunTime = 0;
                life = character.getMaxLife();
            }
        }
        if (effect != Effect.NONE) {
            effectTime -= seconds;
            if (effectTime <= 0) {
                effect = Effect.NONE;
                effectTime = 0;
            }
        }
    }

    // Le quita vida al jugador (nunca baja de cero). Si llega a cero queda aturdido 3 s
    // (el aturdimiento reemplaza cualquier efecto). Si ya esta aturdido no pasa nada
    public void takeDamage(double damage)
    {
        if (stunTime > 0 || damage <= 0) {
            return;
        }
        life = Math.max(0, life - damage);
        if (life <= 0) {
            stunTime = STUN_SECONDS;
            effect = Effect.NONE;
            effectTime = 0;
            kickTicks = 0;
        }
    }

    // Deja al jugador bajo un efecto durante los segundos dados. El efecto nuevo reemplaza al anterior.
    // Un jugador aturdido no recibe efectos
    public void applyEffect(Effect newEffect, double seconds)
    {
        if (stunTime > 0 || newEffect == Effect.NONE) {
            return;
        }
        effect = newEffect;
        effectTime = seconds;
        if (newEffect == Effect.KNOCKED_OUT) {
            kickTicks = 0;
        }
    }

    // Lanza al jugador hacia arriba con la velocidad dada
    public void throwIntoAir(double speed)
    {
        vy = -speed;
        onGround = false;
    }

    // El jugador recibio un gol: su barra de poder sube un cuarto de inmediato
    public void goalConceded()
    {
        powerBar = Math.min(1, powerBar + GOAL_CONCEDED_BAR);
    }

    // Activa el poder si la barra esta llena, el personaje tiene poder, no tiene ya uno activo
    // y puede actuar. La barra se vacia y empieza a recargarse. Devuelve true si se activo
    public boolean activatePower()
    {
        if (!character.hasPower() || hasPower || !canAct() || powerBar < 1 - EPSILON) {
            return false;
        }
        hasPower = true;
        powerBar = 0;
        return true;
    }

    // Gasta el poder activo (se llama cuando ejecuta el tiro de poder). El aura desaparece
    public void consumePower()
    {
        hasPower = false;
    }

    // Choca contra otro jugador como contra una pared: este jugador se acomoda y el otro no se mueve.
    // Si venia de lado se detiene junto a el; si cae encima queda parado sobre su cabeza;
    // si sube y se golpea con sus pies, no lo atraviesa. Devuelve true si estaban encimados
    public boolean collideWith(Player other, double fieldWidth, double groundY)
    {
        double overlapX = Math.min(x + WIDTH - other.x, other.x + WIDTH - x);
        double overlapY = Math.min(y + HEIGHT - other.y, other.y + HEIGHT - y);
        if (overlapX <= 0 || overlapY <= 0) {
            return false;
        }

        boolean above = y < other.y;
        boolean vertical = overlapY <= overlapX;
        if (vertical && !above && other.y + 2 * HEIGHT > groundY + EPSILON) {
            vertical = false;
        }

        if (vertical && above) {
            landOn(other.y);
        } else if (vertical) {
            y = other.y + HEIGHT;
            if (vy < 0) {
                vy = 0;
            }
        } else {
            if (x < other.x) {
                x = other.x - WIDTH;
            } else {
                x = other.x + WIDTH;
            }
            vx = 0;
            keepInsideField(fieldWidth);
            if (x + WIDTH > other.x && x < other.x + WIDTH) {
                landOn(other.y);
            }
        }
        return true;
    }

    // Se apoya sobre una superficie que esta a la altura surfaceY (el piso o la cabeza de otro jugador)
    private void landOn(double surfaceY)
    {
        y = surfaceY - HEIGHT;
        vy = 0;
        onGround = true;
    }

    // No deja que el jugador se salga del campo por los costados
    private void keepInsideField(double fieldWidth)
    {
        if (x < 0) {
            x = 0;
        }
        if (x + WIDTH > fieldWidth) {
            x = fieldWidth - WIDTH;
        }
    }

    // Devuelve la posicion x de la esquina superior izquierda
    public double getX()
    {
        return x;
    }

    // Devuelve la posicion y de la esquina superior izquierda
    public double getY()
    {
        return y;
    }

    // Devuelve la velocidad horizontal
    public double getVx()
    {
        return vx;
    }

    // Devuelve la velocidad vertical
    public double getVy()
    {
        return vy;
    }

    // Indica si el jugador esta en el piso
    public boolean isOnGround()
    {
        return onGround;
    }

    // Devuelve el color del jugador (el nombre que entiende el canvas)
    public String getColor()
    {
        return color;
    }

    // Devuelve el personaje del jugador
    public GameCharacter getCharacter()
    {
        return character;
    }

    // Indica si el jugador mira hacia la derecha
    public boolean isFacingRight()
    {
        return facingRight;
    }

    // Devuelve la posicion x de la esquina superior izquierda de la zona que alcanza el pie al patear
    public double getFootX()
    {
        return facingRight ? x + WIDTH : x - FOOT_WIDTH;
    }

    // Devuelve la posicion y de la esquina superior izquierda de la zona que alcanza el pie
    public double getFootY()
    {
        return y + HEIGHT - FOOT_HEIGHT;
    }

    // Devuelve la vida actual
    public double getLife()
    {
        return life;
    }

    // Devuelve la vida maxima
    public double getMaxLife()
    {
        return character.getMaxLife();
    }

    // Devuelve el progreso de la barra de poder (de 0 a 1)
    public double getPowerBar()
    {
        return powerBar;
    }

    // Indica si el jugador tiene el poder activo (el aura)
    public boolean hasPower()
    {
        return hasPower;
    }

    // Indica si el jugador esta aturdido
    public boolean isStunned()
    {
        return stunTime > 0;
    }

    // Devuelve el efecto de un tiro de poder que tiene el jugador
    public Effect getEffect()
    {
        return effect;
    }
}
