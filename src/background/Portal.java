package src.background;
import java.awt.*;

import src.people.Player;

public class Portal {
    
    //Monde de départ, monde d'arrivée, coordonnées de spawn, 
    //adapter le code des Maps pour qu'elles puissent recevoir un ou plusieurs portails en arguments

    public Map actualMap, targetMap;
    public int x,y;

    public Portal(Map actualMap, Map targetMap, int x, int y){
        this.actualMap = actualMap;
        this.targetMap = targetMap;
        this.x = x;
        this.y = y;
    }

    public Rectangle getBounds(){
        return new Rectangle(x, y, 16, 16);
    }

    public boolean stepOnPortal(Player p){
        Rectangle playerBounds = new Rectangle(p.getX(), p.getY(), 16, 16);
        return playerBounds.intersects(getBounds());
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(x, y, 16, 16);
    }

}
