package tankrotationexample.game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Objects;

public abstract class PowerUp {

    private float x;
    private float y;
    private BufferedImage img;

    PowerUp(float x, float y, String imgFile) {
        this.x = x;
        this.y = y;
        try {
            this.img = ImageIO.read(
                    Objects.requireNonNull(getClass().getClassLoader().getResource(imgFile))
            );
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public abstract void applyEffect(Tank tank);


    public float getX() { // Added getX method
        return x;
    }

    public float getY() { // Added getY method
        return y;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, img.getWidth(), img.getHeight());
    }

    public void drawImage(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img, (int)x, (int)y, null);
    }
}
