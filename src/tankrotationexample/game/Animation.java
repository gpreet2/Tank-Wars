package tankrotationexample.game;

import java.awt.*;
import java.awt.image.BufferedImage;

class Animation {
    private BufferedImage[] frames;
    private int currentFrame;
    private int x, y;
    private boolean finished = false;

    public Animation(BufferedImage[] frames, int x, int y) {
        this.frames = frames;
        this.x = x;
        this.y = y;
    }

    public void update() {
        currentFrame++;
        if (currentFrame >= frames.length) {
            finished = true;
        }
    }

    public void draw(Graphics2D g) {
        if (!finished && currentFrame < frames.length) {
            g.drawImage(frames[currentFrame], x, y, null);
        }
    }

    public boolean isFinished() {
        return finished;
    }
}

