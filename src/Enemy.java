package src;

import java.util.ArrayList;
import java.awt.*;

public class Enemy extends NPC{
    
    public int attack, hp, range;
    public int sensibility; //Nombre de pixels duquel le joueur doit se tenir avant d'être repéré.

    public Enemy(int x, int y, int width, int height, ArrayList<String> dialogs, String pathImage, int attack, int hp, int range, int sensibility, int... cos){
        super(x, y, width, height, dialogs, pathImage, cos);
        this.attack = attack;
        this.hp = hp;
        this.range = range;
        this.sensibility = sensibility;
    }

    public Rectangle getBounds(){
        return new Rectangle(x, y, width, height);
    }

    public boolean isPlayerNear(Player p){
        Rectangle playerBounds = new Rectangle(p.getX(), p.getY(), 16, 16);
        Rectangle enemySensibility = new Rectangle(x - sensibility, y - sensibility , width + 2 * sensibility, height + 2 * sensibility);
        return playerBounds.intersects(enemySensibility);        
    }

    public void moveToPlayer(Player p){
        int px = p.getX();
        int py = p.getY();

        int speed = 1;

        if (x < px) {
            x += speed;
            if (x > px) x = px;
        } else if (x > px) {
            x -= speed;
            if (x < px) x = px;
        }

        //Déplacement sur l'axe Y
        if (y < py) {
            y += speed;
            if (y > py) y = py;
        } else if (y > py) {
            y -= speed;
            if (y < py) y = py;
        }

    }

}
