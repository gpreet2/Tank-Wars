package tankrotationexample.game;

public class ShieldPowerUp extends PowerUp {
    ShieldPowerUp(float x, float y) {
        super(x, y, "powerups/shield.png");
    }

    @Override
    public void applyEffect(Tank tank) {
        tank.activateShield();
    }
}
