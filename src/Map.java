package src;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class Map {

    public String pathImage;
    public int width, height;
    public String name;
    private BufferedImage image;

    public Map(String name, int width, int height, String pathImage){
        this.name = name;
        this.width = width;
        this.height = height;
        this.pathImage = pathImage;

        try {
            image = ImageIO.read(new File(pathImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics g){
        g.drawImage(image, 0, 0, null);
    }

    public ArrayList<Integer> getDimensions(){
        return new ArrayList<Integer>(List.of(width, height));
    }

    public String getName(){return name;}

    public BufferedImage getImage() throws IOException{
        return ImageIO.read(new File(pathImage));
    }
    
}
