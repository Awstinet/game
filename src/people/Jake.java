package src.people;

import java.util.ArrayList;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Jake extends NPC{
    public int attack = 5;
    private long lastAttackTime = 0;
    private int sensibility = 55;
    private int range = 16;
    private BufferedImage image;

    //Moments clefs
    public ArrayList<Boolean> skeletonDead = new ArrayList<Boolean>(List.of(false, false));

    public Jake(int x, int y, int width, int height, ArrayList<String> dialogs, String pathImage, int... cos){
        super(x, y, width, height, dialogs, pathImage, cos);

        try {
            image = ImageIO.read(new File(pathImage));
        }
        catch (IOException exception){
            exception.printStackTrace();
        }

    }

    public void draw(Graphics g){
        g.drawImage(image, x, y, null); // Affiche la première frame
    }

    public void followPlayer(Player player){
        int px = player.getX() - 15;
        int py = player.getY() - 15;

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

    public void moveToEnemy(Enemy enemy){
        int ex = enemy.getX();
        int ey = enemy.getY();

        int speed = 1;

        if (x < ex) {
            x += speed;
            if (x > ex) x = ex;
        } else if (x > ex) {
            x -= speed;
            if (x < ex) x = ex;
        }

        //Déplacement sur l'axe Y
        if (y < ey) {
            y += speed;
            if (y > ey) y = ey;
        } else if (y > ey) {
            y -= speed;
            if (y < ey) y = ey;
        }
    }

    public void attackEnemy(Enemy enemy){
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastAttackTime >= 2500) {
            lastAttackTime = currentTime;
            enemy.changeHP(attack);
            System.out.println("PV de l'ennemi : " + enemy.hp);
        }
    }

    //Si l'ennemi est détectable par Jake
    public boolean isEnemyNear(Enemy e){
        Rectangle enemyBounds = new Rectangle(e.getX(), e.getY(), e.width, e.height);
        Rectangle jakeSensibility = new Rectangle(x - sensibility, y - sensibility , width + 2 * sensibility, height + 2 * sensibility);
        return enemyBounds.intersects(jakeSensibility);        
    }

    //Si l'ennemi est attaquable par Jake
    public boolean isEnemyInHisRange(Enemy e){
        Rectangle enemyBounds = new Rectangle(e.getX(), e.getY(), e.width, e.height);
        Rectangle jakeRange = new Rectangle(x - range, y - range , width + 2 * range, height + 2 * range);
        return enemyBounds.intersects(jakeRange);
    }

    public String dialogSkeleton(){
        return "Bien joué, le squelette a été vaincu !";
    }

}
