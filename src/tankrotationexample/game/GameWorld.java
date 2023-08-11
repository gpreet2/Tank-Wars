package tankrotationexample.game;

import tankrotationexample.GameConstants;
import tankrotationexample.Launcher;
import javax.sound.sampled.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.Objects;

public class GameWorld extends JPanel implements Runnable {

    private List<Wall> walls = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Animation> animations = new ArrayList<>();
    private BufferedImage world;
    private Tank t1;
    private Tank t2;
    private final Launcher lf;
    private long tick = 0;
    private boolean gameover = false;
    private JFrame gameoverFrame;
    private BufferedImage floorImg;


    public GameWorld(Launcher lf) {
        this.lf = lf;
        loadImages();
        playBackgroundMusic();
    }

    BufferedImage[] bulletHitFrames = new BufferedImage[24];
    BufferedImage[] bulletShootFrames = new BufferedImage[24];
    BufferedImage[] powerPickFrames = new BufferedImage[32];

    public void loadImages() {
        try {
            for (int i = 0; i < 24; i++) {
                bulletHitFrames[i] = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("animations/bullethit/bullethit_" + String.format("%04d", i) + ".png")));
            }
            for (int i = 0; i < 24; i++) {
                String path = "animations/bulletshoot/bulletshoot_" + String.format("%04d", i) + ".png";
                bulletShootFrames[i] = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource(path)));
            }
            for (int i = 0; i < 32; i++) {
                powerPickFrames[i] = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("animations/powerpick/powerpick_" + String.format("%04d", i) + ".png")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            while (!gameover) {
                this.tick++;

                int t1PreviousX = (int) t1.getX();
                int t1PreviousY = (int) t1.getY();
                int t2PreviousX = (int) t2.getX();
                int t2PreviousY = (int) t2.getY();

                this.t1.update();
                this.t2.update();



                if (t1.getBounds().intersects(t2.getBounds())) {
                    t1.setX(t1PreviousX);
                    t1.setY(t1PreviousY);
                    t2.setX(t2PreviousX);
                    t2.setY(t2PreviousY);
                }

                for (Wall wall : walls) {
                    if (t1.getBounds().intersects(wall.getBounds())) {
                        t1.setX(t1PreviousX);
                        t1.setY(t1PreviousY);
                    }
                    if (t2.getBounds().intersects(wall.getBounds())) {
                        t2.setX(t2PreviousX);
                        t2.setY(t2PreviousY);
                    }
                }

                java.util.List<Wall> wallsToRemove = new ArrayList<>();
                java.util.List<Bullet> toRemove1 = new java.util.ArrayList<>();

                for (Bullet bullet : this.t1.getBullets()) {
                    bullet.update();

                    if (bullet.getBounds().intersects(this.t2.getBounds())) {
                        this.t2.decreaseHealth(bullet.getDamage());
                        int animationX = (int) bullet.getX() - bulletHitFrames[0].getWidth() / 2;
                        int animationY = (int) bullet.getY() - bulletHitFrames[0].getHeight() / 2;
                        animations.add(new Animation(bulletHitFrames, animationX, animationY));
                        toRemove1.add(bullet);
                    }

                    for (Wall wall : walls) {
                        if (bullet.getBounds().intersects(wall.getBounds())) {
                            int animationX = (int) bullet.getX() - bulletHitFrames[0].getWidth() / 2;
                            int animationY = (int) bullet.getY() - bulletHitFrames[0].getHeight() / 2;
                            animations.add(new Animation(bulletHitFrames, animationX, animationY));
                            toRemove1.add(bullet);
                            if (wall instanceof BreakableWall) {
                                wallsToRemove.add(wall);
                            }
                        }
                    }
                }

                this.t1.getBullets().removeAll(toRemove1);

                java.util.List<Bullet> toRemove2 = new java.util.ArrayList<>();
                Iterator<Bullet> bulletIterator = this.t2.getBullets().iterator();
                while (bulletIterator.hasNext()) {
                    Bullet bullet = bulletIterator.next();
                    bullet.update();

                    if (bullet.getBounds().intersects(this.t1.getBounds())) {
                        this.t1.decreaseHealth(bullet.getDamage());
                        int animationX = (int) bullet.getX() - bulletHitFrames[0].getWidth() / 2;
                        int animationY = (int) bullet.getY() - bulletHitFrames[0].getHeight() / 2;
                        animations.add(new Animation(bulletHitFrames, animationX, animationY));
                        toRemove2.add(bullet);
                    }

                    for (Wall wall : walls) {
                        if (bullet.getBounds().intersects(wall.getBounds())) {
                            int animationX = (int) bullet.getX() - bulletHitFrames[0].getWidth() / 2;
                            int animationY = (int) bullet.getY() - bulletHitFrames[0].getHeight() / 2;
                            animations.add(new Animation(bulletHitFrames, animationX, animationY)); // Animation for hitting wall
                            toRemove2.add(bullet);
                            if (wall instanceof BreakableWall) {
                                wallsToRemove.add(wall);
                            }
                        }
                    }
                }


                java.util.List<PowerUp> powerUpsToRemove = new java.util.ArrayList<>();
                for (PowerUp powerUp : powerUps) {
                    if (powerUp.getBounds().intersects(this.t1.getBounds()) || powerUp.getBounds().intersects(this.t2.getBounds())) {
                        Tank affectedTank = powerUp.getBounds().intersects(this.t1.getBounds()) ? this.t1 : this.t2;

                        int offsetX = (affectedTank.getImg().getWidth() - powerPickFrames[0].getWidth()) / 2;
                        int offsetY = (affectedTank.getImg().getHeight() - powerPickFrames[0].getHeight()) / 2;
                        int animX = (int) affectedTank.getX() + offsetX;
                        int animY = (int) affectedTank.getY() + offsetY;
                        animations.add(new Animation(powerPickFrames, animX, animY)); // Using affected tank's position
                        powerUp.applyEffect(affectedTank);
                        playPickupSound();
                        powerUpsToRemove.add(powerUp);
                    }
                }

                powerUps.removeAll(powerUpsToRemove);


                this.t2.getBullets().removeAll(toRemove2);

                walls.removeAll(wallsToRemove);

                if (this.t1.getLives() <= 0 || this.t2.getLives() <= 0) {
                    gameover = true;
                    Tank winner = (this.t1.getLives() > 0) ? t1 : t2;
                    createGameOverScreen(winner);
                } else {
                    this.repaint();
                }

                Thread.sleep(1000 / 144);
            }
        } catch (InterruptedException ignored) {
            System.out.println(ignored);
        }
    }

    private void createGameOverScreen(Tank winner) {
        gameoverFrame = new JFrame();
        gameoverFrame.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel gameOverMessage = new JLabel("Game Over! The winner is " + ((winner == t1) ? "Tank 1" : "Tank 2"));
        contentPanel.add(gameOverMessage, gbc);

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> {
            gameoverFrame.dispose();
            System.exit(0);
        });
        gbc.gridy = 1;
        contentPanel.add(quitButton, gbc);

        gameoverFrame.add(contentPanel, BorderLayout.CENTER);

        gameoverFrame.setSize(500, 500);
        gameoverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameoverFrame.setLocationRelativeTo(null);
        gameoverFrame.setVisible(true);
    }


    private void playPickupSound() {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("sounds/pickup.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void InitializeGame() {
        this.world = new BufferedImage(GameConstants.GAME_SCREEN_WIDTH,
                GameConstants.GAME_SCREEN_HEIGHT,
                BufferedImage.TYPE_INT_RGB);

        BufferedImage t1img = null;
        BufferedImage t2img = null;
        BufferedImage unbreakableWallImg = null;
        BufferedImage breakableWallImg = null;

        try {
            BufferedImage shieldImg = ImageIO.read(Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("powerups/shield.png")));
            BufferedImage healthImg = ImageIO.read(Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("powerups/health.png")));
            BufferedImage speedImg = ImageIO.read(Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("powerups/speed.png")));

            floorImg = ImageIO.read(
                    Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("floor/bg.bmp"),
                            "Could not find bg.bmp")
            );


            t1img = ImageIO.read(
                    Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("tank/tank1.png"),
                            "Could not find tank1.png")
            );

            t2img = ImageIO.read(
                    Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("tank/tank2.png"),
                            "Could not find tank2.png")
            );

            unbreakableWallImg = ImageIO.read(
                    Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("walls/unbreak.jpg"),
                            "Could not find unbreak.jpg")
            );

            breakableWallImg = ImageIO.read(
                    Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("walls/break2.jpg"),
                            "Could not find break2.jpg")
            );

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        powerUps.add(new ShieldPowerUp(200, 200));
        powerUps.add(new HealthPowerUp(400, 400));
        powerUps.add(new SpeedPowerUp(600, 600));


        t1 = new Tank(300, 300, 0, 0, (short) 0, t1img);
        t2 = new Tank(600, 300, 0, 0, (short) 0, t2img);

        for (int x = 0; x < GameConstants.GAME_SCREEN_WIDTH; x += unbreakableWallImg.getWidth()) {
            walls.add(new UnbreakableWall(x, 0, unbreakableWallImg));
        }

        for (int x = 0; x < GameConstants.GAME_SCREEN_WIDTH; x += unbreakableWallImg.getWidth()) {
            walls.add(new UnbreakableWall(x, GameConstants.GAME_SCREEN_HEIGHT - unbreakableWallImg.getHeight(), unbreakableWallImg));
        }

        for (int y = 0; y < GameConstants.GAME_SCREEN_HEIGHT; y += unbreakableWallImg.getHeight()) {
            walls.add(new UnbreakableWall(0, y, unbreakableWallImg));
        }

        for (int y = 0; y < GameConstants.GAME_SCREEN_HEIGHT; y += unbreakableWallImg.getHeight()) {
            walls.add(new UnbreakableWall(GameConstants.GAME_SCREEN_WIDTH - unbreakableWallImg.getWidth(), y, unbreakableWallImg));
        }


        int wallSize = unbreakableWallImg.getWidth();
        int padding = 10;


        for (PowerUp powerUp : powerUps) {
            int x = (int) powerUp.getX();
            int y = (int) powerUp.getY();

            for (int i = x - wallSize - padding; i <= x + wallSize + padding; i += wallSize) {
                for (int j = y - wallSize - padding; j <= y + wallSize + padding; j += wallSize) {

                    boolean isBoundary = i == x - wallSize - padding || i == x + wallSize + padding
                            || j == y - wallSize - padding || j == y + wallSize + padding;

                    if (isBoundary) {
                        if ((i + j) % (2 * wallSize) == 0) {
                            walls.add(new UnbreakableWall(i, j, unbreakableWallImg));
                        } else {
                            walls.add(new BreakableWall(i, j, breakableWallImg));
                        }
                    }
                }
            }
        }

        TankControl tc1 = new TankControl(t1, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE);
        TankControl tc2 = new TankControl(t2, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER);
        this.lf.getJf().addKeyListener(tc1);
        this.lf.getJf().addKeyListener(tc2);
    }

    private void playBackgroundMusic() {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("sounds/Music.mid"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Graphics2D buffer = world.createGraphics();

        // Clear the buffer
        buffer.setColor(Color.BLACK);
        buffer.fillRect(0, 0, world.getWidth(), world.getHeight());

        // Draw the floor
        for (int i = 0; i < world.getWidth(); i += floorImg.getWidth()) {
            for (int j = 0; j < world.getHeight(); j += floorImg.getHeight()) {
                buffer.drawImage(floorImg, i, j, null);
            }
        }

        this.t1.drawImage(buffer);
        this.t2.drawImage(buffer);

        for (Bullet bullet : this.t1.getBullets()) {
            bullet.drawImage(buffer);
        }

        for (Bullet bullet : this.t2.getBullets()) {
            bullet.drawImage(buffer);
        }

        for (PowerUp powerUp : powerUps) {
            powerUp.drawImage(buffer);
        }

        for (Wall wall : walls) {
            wall.drawImage(buffer);
        }

        Iterator<Animation> iter = animations.iterator();
        while (iter.hasNext()) {
            Animation anim = iter.next();
            anim.update();
            anim.draw(buffer);
            if (anim.isFinished()) {
                iter.remove();
            }
        }

        int subImageWidth = this.getWidth() / 2;
        int subImageHeight = this.getHeight();
        int subImageX = Math.max(0, (int) t1.getX() - subImageWidth / 2);
        int subImageY = Math.max(0, (int) t1.getY() - subImageHeight / 2);

        subImageX = Math.min(subImageX, world.getWidth() - subImageWidth);
        subImageY = Math.min(subImageY, world.getHeight() - subImageHeight);

        BufferedImage leftView = world.getSubimage(subImageX, subImageY, subImageWidth, subImageHeight);
        g2.drawImage(leftView, 0, 0, null);

        subImageX = Math.max(0, (int) t2.getX() - subImageWidth / 2);
        subImageY = Math.max(0, (int) t2.getY() - subImageHeight / 2);

        subImageX = Math.min(subImageX, world.getWidth() - subImageWidth);
        subImageY = Math.min(subImageY, world.getHeight() - subImageHeight);

        BufferedImage rightView = world.getSubimage(subImageX, subImageY, subImageWidth, subImageHeight);
        g2.drawImage(rightView, GameConstants.GAME_SCREEN_WIDTH / 2, 0, null); // Draw without resizing

        g2.setColor(Color.WHITE);
        int splitX = GameConstants.GAME_SCREEN_WIDTH / 2; // The X coordinate of the split line
        g2.drawLine(splitX, 0, splitX, GameConstants.GAME_SCREEN_HEIGHT);

        final int miniMapScale = 4;
        int miniMapWidth = world.getWidth() / miniMapScale;
        int miniMapHeight = world.getHeight() / miniMapScale;
        int miniMapPosX = (GameConstants.GAME_SCREEN_WIDTH - miniMapWidth) / 2;
        int miniMapPosY = GameConstants.GAME_SCREEN_HEIGHT - miniMapHeight - 10;

        g2.drawImage(world, miniMapPosX, miniMapPosY, miniMapWidth, miniMapHeight, null);

        g2.drawRect(miniMapPosX - 1, miniMapPosY - 1, miniMapWidth + 1, miniMapHeight + 1);
    }


}
