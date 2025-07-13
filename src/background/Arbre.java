package src.background;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Arbre extends Obstacle {

    private int foliageHeight;
    private int trunkWidth;
    private int trunkXOffset;

    private BufferedImage trunk;
    private BufferedImage foliage;

    public Arbre(int x, int y, int width, int height, int foliageHeight, int trunkWidth, int trunkXOffset, BufferedImage image) {
        super(x, y, width, height, image);
        this.foliageHeight = foliageHeight;
        this.trunkWidth = trunkWidth;
        this.trunkXOffset = trunkXOffset;

        separateTree(); //Séparer feuillage & tronc dès la création de l'objet
    }

    private void separateTree() {
        foliage = image.getSubimage(0, 0, width, foliageHeight);
        trunk = image.getSubimage(0, foliageHeight, width, height - foliageHeight);
    }

    @Override
    public Rectangle getBounds() {
        //Retourne uniquement la hitbox du tronc
        return new Rectangle(x + trunkXOffset, y + foliageHeight, trunkWidth, height - foliageHeight);
    }

    public void drawTrunk(Graphics2D g2) {
        g2.drawImage(trunk, x, y + foliageHeight, null);
    }

    public void drawFoliage(Graphics2D g2) {
        g2.drawImage(foliage, x, y, null);
    }
}
