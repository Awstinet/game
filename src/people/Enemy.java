package src.people;

import java.util.ArrayList;
import java.awt.*;

public class Enemy extends NPC{
    
    public int attack, hp, range, totalHP;
    public int sensibility; //Nombre de pixels duquel le joueur doit se tenir avant d'être repéré.
    public boolean isDead = false;

    private long lastAttackTime = 0;

    public Enemy(int x, int y, int width, int height, ArrayList<String> dialogs, String pathImage, int attack, int hp, int range, int sensibility, int... cos){
        super(x, y, width, height, dialogs, pathImage, cos);
        this.attack = attack;
        this.hp = hp;
        this.range = range;
        this.sensibility = sensibility;
        this.totalHP = hp;
    }

    public Rectangle getBounds(){
        return new Rectangle(x, y, width, height);
    }

    public boolean isPlayerNear(Player p){
        Rectangle playerBounds = new Rectangle(p.getX(), p.getY(), 16, 16);
        Rectangle enemySensibility = new Rectangle(x - sensibility, y - sensibility , width + 2 * sensibility, height + 2 * sensibility);
        return playerBounds.intersects(enemySensibility);        
    }

    public boolean isPlayerInHisRange(Player p){
        Rectangle playerBounds = new Rectangle(p.getX(), p.getY(), 16, 16);
        Rectangle enemyRange = new Rectangle(x - range, y - range , width + 2 * range, height + 2 * range);
        return playerBounds.intersects(enemyRange);
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

    public void attackPlayer (Player p){
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastAttackTime >= 2000) {
            lastAttackTime = currentTime;
            p.changeHP(attack);
            System.out.println("PV du joueur : " + p.hp);
        }
    }

    public void changeHP(int n){
        hp -= n;
    }

    public void drawHealthBar(Graphics g){
        g.setColor(Color.red);
        g.fillRect(x-8, y-10, 32, 5);
        g.setColor(new Color(32,247,18));
        g.fillRect(x-8, y-10, hp*32/totalHP, 5);
        g.setColor(Color.black);
        g.drawRect(x-8, y-10, 32, 5);
    }

}
