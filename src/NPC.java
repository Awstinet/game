package src;

import java.awt.*;
import java.util.ArrayList;

public class NPC {
    private int x, y;
    private int width, height;
    private ArrayList<ArrayList<Integer>> cosMove = new ArrayList<ArrayList<Integer>>();
    private int currentPoint = 0;
    private int nbPoint;
    private Color color;

    public NPC(int x, int y, int width, int height, int... cos) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = new Color(255,0,0);

        for (int i = 0; i < cos.length-1; i+=2){
            ArrayList<Integer> tempArray = new ArrayList<>();
            tempArray.add(cos[i]);
            tempArray.add(cos[i+1]);
            this.cosMove.add(tempArray);
        }

        this.nbPoint = this.cosMove.size();
    }

    public void draw(Graphics g) {
        g.setColor(this.color);
        g.fillRect(x, y, width, height);

    }

    public int getX() {return x;}
    public int getY() {return y;}
    public int getWidth() {return width;}
    public int getHeight() {return height;}


    public void npcMove(){

        if (nbPoint == 0){
            return;
        }

        ArrayList<Integer> target = cosMove.get(currentPoint);
        int targetX = target.get(0);
        int targetY = target.get(1);

        int speed = 1;

        // Déplacement sur l'axe X
        if (x < targetX) {
            color = new Color(0,255,0);
            x += speed;
            if (x > targetX) x = targetX;
        } else if (x > targetX) {
            x -= speed;
            color = new Color(0,0,255);
            if (x < targetX) x = targetX;
        }

        // Déplacement sur l'axe Y
        if (y < targetY) {
            y += speed;
            color = new Color(125,125,200);
            if (y > targetY) y = targetY;
        } else if (y > targetY) {
            color = new Color(125,200,200);
            y -= speed;
            if (y < targetY) y = targetY;
        }

        // Une fois arrivé au point cible, passer au suivant
        if (x == targetX && y == targetY) {
            currentPoint = (currentPoint + 1) % nbPoint;
        }
    }

}

