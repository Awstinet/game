package src.background;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import src.people.NPC;

public class Map {

    public String pathImage;
    public int width, height;
    public String name;
    private BufferedImage image;
    private ArrayList<NPC> lstNPCs;
    private ArrayList<Obstacle> lstObstacles;

    public Map(String name, int width, int height, String pathImage, ArrayList<NPC> lstNPCs, ArrayList<Obstacle> lstObstacles){
        this.name = name;
        this.width = width;
        this.height = height;
        this.pathImage = pathImage;
        this.lstNPCs = lstNPCs;
        this.lstObstacles = lstObstacles;

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

    public String getMapName(){return name;}

    public BufferedImage getImage() throws IOException{
        return ImageIO.read(new File(pathImage));
    }

    public ArrayList<NPC> getNPCs(){return lstNPCs;}
    
    public ArrayList<Obstacle> getObstacles(){return lstObstacles;}
    
}
