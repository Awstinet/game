package src.background;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Obstacle {
    
    public int x, y, width, height;
    public BufferedImage image;

    public Obstacle(int x, int y, int width, int height, BufferedImage image){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    public void draw(Graphics g){
        g.drawImage(image, x, y, null);
    }

    public Rectangle getBounds(){
        return new Rectangle(x, y, width, height);
    }
}
