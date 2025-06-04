package src;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Player {
    private int x, y;
    private int speed = 2;
    private boolean left, right, up, down;

    private BufferedImage spriteSheet;
    private BufferedImage[] sprites;

    public Player(int x, int y) throws IOException {
        this.x = x;
        this.y = y;
        spriteSheet = ImageIO.read(new File("assets/sprites/Soldier-Walk.png"));
    }

    public void spritePlayerLoader(int rows, int columns) {
        int spriteWidth = 64;
        int spriteHeight = 64;
        sprites = new BufferedImage[rows * columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                sprites[i * columns + j] = spriteSheet.getSubimage(
                    j * spriteWidth, i * spriteHeight, spriteWidth, spriteHeight
                );
            }
        }
    }

    public void update() {
        if (left) x -= speed;
        if (right) x += speed;
        if (up) y -= speed;
        if (down) y += speed;
    }

    public void draw(Graphics g) {
        g.drawImage(sprites[0], x, y, null); // Affiche la première frame
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) left = true;
        if (code == KeyEvent.VK_RIGHT) right = true;
        if (code == KeyEvent.VK_UP) up = true;
        if (code == KeyEvent.VK_DOWN) down = true;
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) left = false;
        if (code == KeyEvent.VK_RIGHT) right = false;
        if (code == KeyEvent.VK_UP) up = false;
        if (code == KeyEvent.VK_DOWN) down = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }

}
