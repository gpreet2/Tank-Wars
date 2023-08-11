package tankrotationexample.game;

import tankrotationexample.GameConstants;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.awt.Rectangle;
import java.util.Objects;

public class Tank {
    private boolean shield = false;
    private boolean speedBoostActive = false;
    private long immunityTimer = 0;
    private static final float SPEED_BOOST = 2.0f;
    private static final int BULLET_DAMAGE = 10;


    private List<Bullet> bullets = new ArrayList<>();
    private float x;
    private float y;
    private float vx;
    private float vy;
    private float angle;

    private float R = 5;
    private float ROTATIONSPEED = 3.0f;

    private final int MAX_HEALTH = 100;
    private int health;
    private int lives = 3;

    private BufferedImage img;
    private boolean UpPressed;
    private boolean DownPressed;
    private boolean RightPressed;
    private boolean LeftPressed;

    Tank(float x, float y, float vx, float vy, float angle, BufferedImage img) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.img = img;
        this.angle = angle;
        this.R = 5;
        this.health = MAX_HEALTH;
        this.lives = 3;
    }

    void setX(float x){ this.x = x; }

    void setY(float y) { this.y = y; }

    void toggleUpPressed() {
        this.UpPressed = true;
    }

    void toggleDownPressed() {
        this.DownPressed = true;
    }

    void toggleRightPressed() {
        this.RightPressed = true;
    }

    void toggleLeftPressed() {
        this.LeftPressed = true;
    }

    void unToggleUpPressed() {
        this.UpPressed = false;
    }

    void unToggleDownPressed() {
        this.DownPressed = false;
    }

    void unToggleRightPressed() {
        this.RightPressed = false;
    }

    void unToggleLeftPressed() {
        this.LeftPressed = false;
    }

    void update() {
        if (this.UpPressed) {
            this.moveForwards();
        }

        if (this.DownPressed) {
            this.moveBackwards();
        }

        if (this.LeftPressed) {
            this.rotateLeft();
        }

        if (this.RightPressed) {
            this.rotateRight();
        }

        if (System.currentTimeMillis() >= immunityTimer && speedBoostActive) {
            this.R /= SPEED_BOOST;
            speedBoostActive = false;
        }

        if (System.currentTimeMillis() >= immunityTimer && shield) {
            this.shield = false;
        }
    }

    public int getLives() { return this.lives; }
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, img.getWidth(), img.getHeight());
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public void increaseSpeed() {
        this.R *= SPEED_BOOST;
        this.immunityTimer = System.currentTimeMillis() + 15000;
        this.speedBoostActive = true;
    }
    public void addExtraLife() {
        this.lives += 1;
    }

    public void shoot() {
        playShootSound();
        BufferedImage bulletImage = null;
        try {
            bulletImage = ImageIO.read(
                    Objects.requireNonNull(getClass().getClassLoader().getResource("bullet/bullet.jpg"))
            );
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        Bullet bullet = new Bullet(x, y, angle, 10, BULLET_DAMAGE, bulletImage);
        bullets.add(bullet);
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    private void playShootSound() {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("sounds/bullet_shoot.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void rotateLeft() {
        this.angle -= this.ROTATIONSPEED;
    }

    private void rotateRight() {
        this.angle += this.ROTATIONSPEED;
    }

    private void moveBackwards() {
        vx = R * (float) Math.cos(Math.toRadians(angle));
        vy = R * (float) Math.sin(Math.toRadians(angle));
        x -= vx;
        y -= vy;
        checkBorder();
    }

    private void moveForwards() {
        vx = R * (float) Math.cos(Math.toRadians(angle));
        vy = R * (float) Math.sin(Math.toRadians(angle));
        x += vx;
        y += vy;
        checkBorder();
    }

    private void checkBorder() {
        if (x < 30) {
            x = 30;
        }
        if (x >= GameConstants.GAME_SCREEN_WIDTH - 88) {
            x = GameConstants.GAME_SCREEN_WIDTH - 88;
        }
        if (y < 40) {
            y = 40;
        }
        if (y >= GameConstants.GAME_SCREEN_HEIGHT - 80) {
            y = GameConstants.GAME_SCREEN_HEIGHT - 80;
        }
    }

    public void activateShield() {
        this.shield = true;
        this.immunityTimer = System.currentTimeMillis() + 15000;
    }

    void decreaseHealth(int amount) {
        if (System.currentTimeMillis() < immunityTimer || shield) return;
        this.health -= amount;
        if (this.health < 0) {
            this.health = 0;
            this.lives--;
            if (this.lives >= 0) {
                this.health = MAX_HEALTH;
            } else {

            }
        }
    }

    public BufferedImage getImg() {
        return this.img;
    }


    @Override
    public String toString() {
        return "x=" + x + ", y=" + y + ", angle=" + angle;
    }

    void drawImage(Graphics g) {
        AffineTransform rotation = AffineTransform.getTranslateInstance(x, y);
        rotation.rotate(Math.toRadians(angle), this.img.getWidth() / 2.0, this.img.getHeight() / 2.0);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img, rotation, null);

        g2d.setColor(Color.RED);
        g2d.fillRect((int)x, (int)y - 20, this.img.getWidth(), 10);

        g2d.setColor(Color.GREEN);
        int healthWidth = (int)((this.health / (double)MAX_HEALTH) * this.img.getWidth());
        g2d.fillRect((int)x, (int)y - 20, healthWidth, 10);

        //g2d.setColor(Color.RED);
        //g2d.drawRect((int)x,(int)y,this.img.getWidth(), this.img.getHeight());

        g2d.setColor(Color.WHITE);
        g2d.drawString("Lives: " + this.lives, (int)x, (int)y - 30);
    }
}
