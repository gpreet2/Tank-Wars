package tankrotationexample.game;

public class SpeedPowerUp extends PowerUp {

    SpeedPowerUp(float x, float y) {
        super(x, y, "powerups/speed.png");
    }

    public void applyEffect(Tank tank) {
        tank.increaseSpeed();
    }


}
