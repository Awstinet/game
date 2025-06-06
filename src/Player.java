package src;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
        spriteSheet = ImageIO.read(new File("assets/sprites/playerSprite.png"));
    }

    public void spritePlayerLoader(int rows, int columns) {
        int spriteWidth = 16;
        int spriteHeight = 16;
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

        int spriteWidth = 16;
        int spriteHeight = 16;

        if (left && x - speed >= 0) x -= speed;
        if (right && x + speed + spriteWidth <= 1600) x += speed;
        if (up && y - speed >= 0) y -= speed;
        if (down && y + speed + spriteHeight <= 1200) y += speed;
    }

    public void draw(Graphics g) {
        g.drawImage(sprites[0], x, y, null); // Affiche la première frame
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_Q) left = true;
        if (code == KeyEvent.VK_D) right = true;
        if (code == KeyEvent.VK_Z) up = true;
        if (code == KeyEvent.VK_S) down = true;
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_Q) left = false;
        if (code == KeyEvent.VK_D) right = false;
        if (code == KeyEvent.VK_Z) up = false;
        if (code == KeyEvent.VK_S) down = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setPosition(int nx, int ny){
        this.x = nx;
        this.y = ny;
    }


    public boolean collidesWithNPC(NPC npc) {
        Rectangle playerRect = new Rectangle(x, y, 16, 16);

        Rectangle npcRect = new Rectangle(npc.getX(), npc.getY(), npc.getWidth(), npc.getHeight());
        return playerRect.intersects(npcRect);
    }

    public ArrayList<Integer> getLastPosition(){
        ArrayList<Integer> coordinates = new ArrayList<Integer>();
        coordinates.add(getX());
        coordinates.add(getY());
        return coordinates;
    }
    
}
