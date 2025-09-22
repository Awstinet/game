package src.objects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.*;

//On renommera Item dans le prochain jeu.
public class Tool {
    
    public String pathImage;
    public BufferedImage image;
    public int width, height;
    public String name;


    public Tool(String name, int width, int height, String pathImage){
        this.name = name;
        this.width = width;
        this.height = height;

        try {
            image = ImageIO.read(new File(pathImage));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics g, int x, int y){
        //Mettre l'item en question au bon endroit dans l'inventaire (selon l'ordre dans lequel ça a été récupéré, si y a un item avant ou non, etc.)
        g.drawImage(image, x, y, null);
    }

    public String getToolName(){return name;}


}
