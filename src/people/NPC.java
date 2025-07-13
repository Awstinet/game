package src.people;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import javax.imageio.ImageIO;

public class NPC {
    public int x, y;
    public int width, height;
    private ArrayList<ArrayList<Integer>> cosMove = new ArrayList<ArrayList<Integer>>();
    private int currentPoint = 0;
    private int nbPoint;
    private ArrayList<String> dialogs = new ArrayList<String>();
    private boolean hasTalk;
    public String pathImage;
    private BufferedImage image;

    public NPC(int x, int y, int width, int height, ArrayList<String> dialogs, String pathImage, int... cos) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.dialogs = dialogs;
        this.hasTalk = false;

        //Intègre toutes les coordonnées du NPC dans un ArrayList
        for (int i = 0; i < cos.length-1; i+=2){
            ArrayList<Integer> tempArray = new ArrayList<>();
            tempArray.add(cos[i]);
            tempArray.add(cos[i+1]);
            this.cosMove.add(tempArray);
        }

        this.nbPoint = this.cosMove.size(); //Nombre de coordonnées différentes

        try {
            image = ImageIO.read(new File(pathImage));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }

    public int getX() {return x;}
    public int getY() {return y;}

    public int getWidth() {return width;}
    public int getHeight() {return height;}


    public void npcMove(){
        //Si aucun point (NPC ne bouge pas), on fait rien.
        if (nbPoint == 0){
            return;
        }

        ArrayList<Integer> target = cosMove.get(currentPoint); //on récupère le point dans lequel il va aller
        int targetX = target.get(0);
        int targetY = target.get(1);

        int speed = 1;

        //Déplacement sur l'axe X
        if (x < targetX) {
            x += speed;
            if (x > targetX) x = targetX;
        } else if (x > targetX) {
            x -= speed;
            if (x < targetX) x = targetX;
        }

        //Déplacement sur l'axe Y
        if (y < targetY) {
            y += speed;
            if (y > targetY) y = targetY;
        } else if (y > targetY) {
            y -= speed;
            if (y < targetY) y = targetY;
        }

        //Une fois arrivé au point cible, il passe au suivant
        if (x == targetX && y == targetY) {
            currentPoint = (currentPoint + 1) % nbPoint;
        }
    }

    //Récupère tous ses dialogues et les affiches. Change le statut de hasTalk pour pas qu'il se répète indéfiniment.
    public ArrayList<String> allDialogs(){
        return dialogs;
    }

    //Change le statut de hasTalk en true.
    public void markTalked(){
        hasTalk = true;
    }

    public boolean hasTalk() {return hasTalk;}
    public void resetTalk() {hasTalk = false;}

    //Si il proche du joueur.
    public boolean isNear(Player p) {
        int dx = this.x - p.getX();
        int dy = this.y - p.getY();
        int distanceSquared = dx * dx + dy * dy;

        int proximityThreshold = 40; //Distance en pixel
        return distanceSquared <= proximityThreshold * proximityThreshold;
    }

}

