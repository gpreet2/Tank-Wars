package tankrotationexample.game;

public class HealthPowerUp extends PowerUp {
    HealthPowerUp(float x, float y) {
        super(x, y, "powerups/health.png");
    }

    @Override
    public void applyEffect(Tank tank) {
        tank.addExtraLife();
    }
}
