package src.people;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import src.objects.*;

public class Player {
    public int x, y;
    private int speed = 2;
    private boolean left, right, up, down, clickAttack;
    public boolean talk = false;
    public int hp = 100;
    public int attack = 10;
    public long lastAttackTime = 0;
    public int nbArrows = 999;
    public int nbCoins = 1789;

    public long lastClickTime = 0;

    public int width = 20;
    public int height = 30;

    private BufferedImage spriteSheet;    
    private BufferedImage swordSpriteSheet;
    private BufferedImage[][] sprites;

    

    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private int frameDelay = 200;
    private int currentRow = 1;

    private boolean isMoving = false;
    public boolean isAttacking = false;

    public ArrayList<Tool> lstObjects = new ArrayList<Tool>();

    public Player(int x, int y) throws IOException {
        this.x = x;
        this.y = y;
        spriteSheet = ImageIO.read(new File("assets/sprites/personnages/playerSpriteSheet.png"));
        swordSpriteSheet = ImageIO.read(new File("assets/sprites/personnages/playerSwordSpriteSheet.png"));
    }

    //Pour charger individuellement chacune des images du spritesheet.
    public void spritePlayerLoader(int rows, int columns){

        sprites = new BufferedImage[rows+1][columns+1];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                sprites[i][j] = spriteSheet.getSubimage(
                    j * width, i * height, width, height
                );
            }
        }

        //1 = nb de lignes actuelles pour le spriteSheet avec épée. À changer pour plus tard.
        for (int i = rows; i < rows + 1; i++){
            // 5 colonnes pour le sprite à l'épée.
            for (int j = 0 ; j < 5; j++){
                sprites[i][j] = swordSpriteSheet.getSubimage(
                    j * 38, 39*0, 38, 39
                );
            }
        }
    }

    public void update() {

        isMoving = false;
        isAttacking = false;

        if (left && x - speed >= 0 && !clickAttack){ 
            x -= speed;
            isMoving = true;
            currentRow = 3;
        }
        if (right && x + speed + width <= 1600 && !clickAttack){
            x += speed;
            isMoving = true;
            currentRow = 1;
        } 
        if (up && y - speed >= 0 && !clickAttack) {
            y -= speed;  
            isMoving = true;
            currentRow = 2;
        }

        if (down && y + speed + height <= 1200 && !clickAttack) {
            y += speed;
            isMoving = true;
            currentRow = 0;
        } 

        //Si on a cliqué sur le bouton d'attaque
        if (clickAttack){
            isAttacking = true;
            currentRow = 4;
        }

        //Si on a cliqué pour attaqué, après 0.5sec, la valeur change, ce qui va cancel l'animation quand terminée + pas besoin de rester appuyer.
        if (clickAttack && System.currentTimeMillis() - lastClickTime >= 500) {
            clickAttack = false;
        }

        updateAnimation();
    }

    public void draw(Graphics g) {
        g.drawImage(sprites[currentRow][currentFrame], x, y, null);
    }


    private void updateAnimation() {

        //Si le joueur ne fait rien
        if (!isMoving && !isAttacking) {
            currentFrame = 0;
            currentRow = 0;
            return;
        }

        //S'il attaque
        if (isAttacking) {
            long now = System.currentTimeMillis();
            if (now - lastFrameTime >= frameDelay*0.5) {
                currentFrame = (currentFrame + 1) % 5;
                lastFrameTime = now;
            }  
        }

        //S'il marche simplement.
        if (isMoving){
            long now = System.currentTimeMillis();
            if (now - lastFrameTime >= frameDelay) {
                currentFrame = (currentFrame + 1) % 4;
                lastFrameTime = now;
            }  
        }
        
    }

    public void drawHealthBar(Graphics g){
        g.setColor(Color.red);
        g.fillRect(x-8, y-10, 32, 5);
        g.setColor(new Color(32,247,18));
        g.fillRect(x-8, y-10, hp*32/100, 5);
        g.setColor(Color.black);
        g.drawRect(x-8, y-10, 32, 5);
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_Q) left = true;
        if (code == KeyEvent.VK_D) right = true;
        if (code == KeyEvent.VK_Z) up = true;
        if (code == KeyEvent.VK_S) down = true;
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_Q) left = false;
        if (code == KeyEvent.VK_D) right = false;
        if (code == KeyEvent.VK_Z) up = false;
        if (code == KeyEvent.VK_S) down = false;
    }

    public void mousePressed(MouseEvent e){
        int code = e.getButton();
        if ((code == MouseEvent.BUTTON1 || code == MouseEvent.BUTTON3)&& !clickAttack){
            currentFrame = 0; //On se met à la première frame d'attaque
            clickAttack = true; //La variable qui stock le clique d'attaque devient true
            lastClickTime = System.currentTimeMillis();
        } 
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setPosition(int nx, int ny){
        this.x = nx;
        this.y = ny;
    }


    public boolean collidesWithNPC(NPC npc) {

        if (npc instanceof Enemy) {
            Enemy e = (Enemy) npc;
            if (e.isDead){return false;} //Si c'est un ennemi mort, alors il n'y aura jamais de collision avec lui.
        }

        Rectangle playerRect = new Rectangle(x, y, width, height);

        Rectangle npcRect = new Rectangle(npc.getX(), npc.getY(), npc.getWidth(), npc.getHeight());
        return playerRect.intersects(npcRect);
    }

    public ArrayList<Integer> getLastPosition(){
        ArrayList<Integer> coordinates = new ArrayList<Integer>();
        coordinates.add(getX());
        coordinates.add(getY());
        return coordinates;
    }

    public boolean isTalking(){
        return talk;
    }

    public void changeTalk(){
        talk = (talk == true) ? talk = false : true;
    }

    public void changeHP(int attack){
        hp -= attack;
    }

    public void reset() {
        if (talk) changeTalk(); //Arrêter de parler
    }

    public void attackEnemy(Enemy e){
        long currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis - lastAttackTime >= 500) {
            lastAttackTime = currentTimeMillis;
            e.changeHP(attack);
        }
    }

    public boolean isEnemyInHisRange(Enemy e, int range){
        Rectangle playerBounds = new Rectangle(getX(), getY(), width, height);
        Rectangle enemyBounds = new Rectangle(e.x - range, e.y - range , e.width + 2 * range, e.height + 2 * range);
        return playerBounds.intersects(enemyBounds);
    }

    public ArrayList<String> getPlayerObjects(){
        ArrayList<String> newArrayList = new ArrayList<>(); 
        lstObjects.forEach((key) -> newArrayList.add(key.name));
        return newArrayList;
    }

    public void addPlayerObject(Tool object){
        lstObjects.add(object);
    }
    
}
