package src.mains;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import src.background.Arbre;
import src.background.Map;
import src.background.Obstacle;
import src.background.Portal;
import src.people.Enemy;
import src.people.NPC;
import src.people.Player;
import src.people.Jake;
import src.objects.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.AffineTransform;

public class GamePanel extends JPanel implements Runnable, KeyListener, MouseListener {

    private Thread gameThread;
    private final int FPS = 60;

    private Player player;
    private NPC npc;
    private Jake jake;

    ArrayList<Integer> lastPos = new ArrayList<Integer>();

    private BufferedImage dialogBox;
    private Font dialogFont;
    private int currentDialogIndex = 0;

    private BufferedImage arbreImg, rockImg;

    private Map actualMap;

    public ArrayList<Portal> lstPortals = new ArrayList<Portal>();

    public boolean playerIsDead = false;
    private BufferedImage gameOverScreen;
    private long gameOverStartTime = 0;
    private boolean showGameOverScreen = false;
    private boolean readyToRestart = false;
    private int restartMessageAlpha = 0; //Pour le fondu.
    private BufferedImage backpackIcon;
    private BufferedImage mapIcon;

    private boolean showSkeletonDialog = false; 

    private Cursor invisibleCursor;
    private Cursor defaultCursor;

    public boolean isMenuOn = false;
    public boolean isInventoryOn = false;
    public boolean isMapOn = false;

    public Tool key;

    //Les boutons du menu
    Rectangle leaveButton = new Rectangle(325, 275, 150, 50);



    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setFocusable(true);
        this.addKeyListener(this);
        this.addMouseListener(this);

        //Chargement du sprite du joueur.
        try {
            player = new Player(20, 20);
            player.spritePlayerLoader(4, 4);
            player.swordSpritePlayerLoader(5, 1);

            lastPos.add(20); //Coordonnée X
            lastPos.add(20); //Coordonnée Y

        } catch (IOException e) {
            e.printStackTrace();
        }

        

        //Chargement du background (et obstacles), de la boîte de dialogue et de la police d'écriture des dialogues
        try {
            dialogBox = ImageIO.read(new File("assets/sprites/divers/dialog_box.png"));
            arbreImg = ImageIO.read(new File("assets/sprites/obstacles/arbre.png"));
            rockImg = ImageIO.read(new File("assets/sprites/obstacles/rocher.png"));
            gameOverScreen = ImageIO.read(new File("assets/sprites/divers/gameOverScreen.png"));
            backpackIcon = ImageIO.read(new File("assets/sprites/divers/backpackIcon.png"));
            mapIcon = ImageIO.read(new File("assets/sprites/divers/mapIcon.png"));


            File fontFile = new File("assets/fonts/pixelify/PixelifySans-SemiBold.ttf");
            Font pixelifyFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            dialogFont = pixelifyFont.deriveFont(Font.PLAIN, 22);

        } 
        catch (Exception e) {
            e.printStackTrace();
            if (e instanceof FontFormatException){
                dialogFont = new Font("Arial", Font.PLAIN, 22);
            }
        }     
        
        //Création de tous les obstacles
        Arbre arbre = new Arbre(32, 32, 56, 48, 22, 11, 22, arbreImg);
        Obstacle rocher = new Obstacle(100, 100, 32, 10, rockImg);

        //Création des objets
        key = new Tool("clef", 10, 16, "assets/sprites/objets/key.png");

        player.addPlayerObject(new Tool("key2", 22, 35, "assets/sprites/objets/key2.png"));
        player.addPlayerObject(new Tool("key3", 22, 35, "assets/sprites/objets/key2.png"));
        player.addPlayerObject(new Tool("key4", 22, 35, "assets/sprites/objets/key2.png"));
        player.addPlayerObject(new Tool("key5", 22, 35, "assets/sprites/objets/key2.png"));
        player.addPlayerObject(new Tool("key6", 22, 35, "assets/sprites/objets/key2.png"));


        
        //Création des NPCs.
        npc = new NPC(100, 100, 16, 16, new ArrayList<String>(List.of("Bonjour", "Caca", "ABABABA")), "assets/sprites/personnages/amogus.png",100, 100, 300, 100, 300, 300, 100, 300);
        Enemy squelette = new Enemy(100, 100, 11, 19, new ArrayList<>(), "assets/sprites/personnages/squelette.png", 
        25, 50, 1, 80, 100, 100, 300, 100, 300, 300, 100, 300);

        jake = new Jake(8,8, 14, 15, new ArrayList<>(), "assets/sprites/personnages/Jake.png", 0,0);


        //Création de toutes les maps.
        Map map1 = new Map("test", 1600, 1200, "assets/maps/mapPaint.png",
            new ArrayList<NPC>(List.of(npc)),
            new ArrayList<Obstacle>(List.of(arbre, rocher))
        );

        Map map2 = new Map("map2", 1600, 1200, "assets/maps/mapDesertTest.png",
            new ArrayList<NPC>(List.of(squelette)),
            new ArrayList<Obstacle>()
        );


        //Création des portails
        lstPortals.add(new Portal(map1, map2, 150, 150));
        lstPortals.add(new Portal(map2, map1, 170, 170));
        
        actualMap = map1; //On met la map 1 comme map actuelle.

        //Curseurs
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB); //Curseau invisible
        invisibleCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "invisible");
        defaultCursor = Cursor.getDefaultCursor(); //Curseur normal
        setFocusable(true);
        requestFocusInWindow();
        setCursor(invisibleCursor); //Curseur invisible par défaut

    }

    public void startGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                //Si l'écran de Game Over est actif
                if (showGameOverScreen) {
                    long elapsed = System.currentTimeMillis() - gameOverStartTime;

                    if (elapsed >= 5000) { //Si ça fait 5 secondes qu'on est sur l'écran de game over, on a le droit de restart
                        readyToRestart = true;
                    }

                    //Fait un fade-in progressif du texte sur 2 secondes (2000 ms)
                    if (elapsed >= 5000 && restartMessageAlpha < 255) {
                        restartMessageAlpha += 5;
                        if (restartMessageAlpha > 255) restartMessageAlpha = 255;
                    }

                    repaint();
                    delta--;
                    continue;
                }

                //Si on est dans le menu
                if (isMenuOn){
                    //Arrête le temps
                    delta--;
                    repaint();
                    continue;
                }

                if (isInventoryOn){
                    delta--;
                    repaint();
                    continue;
                }

                if (isMapOn){
                    delta--;
                    repaint();
                    continue;
                }

                //Si le joueur n'a plus de PVs, on affichera l'écran de game over à la prochaine itération.
                if (player.hp <= 0 && !showGameOverScreen) {
                    playerIsDead = true;
                    showGameOverScreen = true;
                    gameOverStartTime = System.currentTimeMillis();
                }


                //Sauvegarde la position AVANT le déplacement
                int lastX = player.getX();
                int lastY = player.getY();

                //Mise à jour (déplacement)
                player.update();
                jake.followPlayer(player);

                boolean hasCollided = false;
                Rectangle playerBounds = new Rectangle(player.getX(), player.getY(), player.width, player.height);

                //Si le joueur passer sur un portail du monde dans lequel il est, la map actuelle change.
                for (Portal portal : lstPortals){
                    if (portal.actualMap.equals(actualMap)){
                        if (portal.stepOnPortal(player)){
                        actualMap = portal.targetMap;
                        }
                    }    
                }

                //Si le rectangle du joueur entre en collision avec celui d'un obstacle, on change la valeur de hasCollised.
                for (Obstacle obs : actualMap.getObstacles()){
                    if (playerBounds.intersects(obs.getBounds())){
                        hasCollided = true;
                        break;
                    }
                }


                //Vérifie la collision avec un obstacle
                if(hasCollided){
                    player.setPosition(lastX, lastY);
                }

                for (NPC n : actualMap.getNPCs()){
                    //Vérifie la collision avec un NPC
                    if (player.collidesWithNPC(n)) {
                        //Si collision, rollback
                        player.setPosition(lastX, lastY);
                    }
                    //Si c'est un ennemi et que le joueur est dans sa zone de sensibilité
                    else if (n instanceof Enemy){
                        Enemy e = (Enemy) n;
                        
                        if (e.isDead) { continue; }

                        //Si le joueur se trouve dans la zone d'attaque de l'ennemi
                        if (e.isPlayerInHisRange(player)){
                            e.attackPlayer(player);
                        }

                        //Si le joueur est dans la zone de sensibilité de l'ennemi
                        else if (e.isPlayerNear(player)){
                            e.moveToPlayer(player);
                        }
                        else{
                            e.npcMove();
                        }

                        //Si Jake est proche de l'ennemi
                        if (jake.isEnemyNear(e)){
                            jake.moveToEnemy(e);

                            //S'il peut l'attaquer
                            if (jake.isEnemyInHisRange(e)){
                                jake.attackEnemy(e);
                            }
                        }

                        if (e.hp <= 0) {
                            e.isDead = true; //L'ennemi est mort
                            jake.skeletonDead.set(0, true); //Le dialogue après la mort du squelette peut être montré
                            
                            //Puisque le squelette drop la clef, on l'ajoute dans l'inventaire du joueur : 
                            player.addPlayerObject(key);
                        }

                    } 
                    //Si pas collision, le npc bouge.
                    else{
                        n.npcMove();
                    }
                }

                

                repaint();
                delta--;
            }
        }   
    }

    public void update() {
        player.update();
    }


    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        //Fond noir
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        //Sauvegarde la transform de base (écran)
        AffineTransform originalTransform = g2.getTransform();

        //Appliquer translation + zoom pour la "vue monde"
        double zoom = 2.0;
        int screenCenterX = getWidth() / 2;
        int screenCenterY = getHeight() / 2;
        double translateX = screenCenterX - player.getX() * zoom;
        double translateY = screenCenterY - player.getY() * zoom;

        g2.translate(translateX, translateY);
        g2.scale(zoom, zoom);

        //Dessin de la map et du monde
        actualMap.draw(g2);

        for (Portal portal : lstPortals){
            if (portal.actualMap.equals(actualMap)){
                portal.draw(g2);
            }
        }

        //Dessine tous les obstacles de la map actuelle
        for (Obstacle obs : actualMap.getObstacles()) {
            if (obs instanceof Arbre) {
                ((Arbre) obs).drawTrunk(g2);
            } else {
                obs.draw(g2);
            }
        }

        //Dessine le joueur, sa healthbar et Jake
        player.draw(g2);
        player.drawHealthBar(g2);
        jake.draw(g2);

        //Pour tous les NPCs de la map actuelle
        for (NPC n : actualMap.getNPCs()){
            if (n instanceof Enemy){ //Si le NPC en question est un ennemi
                Enemy e = (Enemy) n;
                if (!e.isDead){ //S'il n'est pas mort, on le dessine lui et sa barre de vie
                    e.draw(g2);
                    e.drawHealthBar(g2);
                }
            }
            else{ //On dessine les autres NPCs
                n.draw(g2);
            }
        }
        
        //Pour les arbres, dessine le feuillage
        for (Obstacle obs : actualMap.getObstacles()) {
            if (obs instanceof Arbre) {
                ((Arbre) obs).drawFoliage(g2);
            }
        }

        for (NPC n : actualMap.getNPCs()){
            //Si le joueur est à proximité du NPC, on affiche une message comme quoi il peut lui parler.
            if (n.isNear(player)){
                if (n.allDialogs().size() > 0){
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2.drawString("Appuyez sur [E] pour parler", n.getX()-45, n.getY()-10);
                }
            }
        }

        //Retour en "coordonnées écran"
        g2.setTransform(originalTransform);

        if (jake.skeletonDead.get(0)) { //Si le squelette est mort et que le dialogue ne s'est pas affiché
            if (!jake.skeletonDead.get(1)) {
                jake.skeletonDead.set(1, true);
                showSkeletonDialog = true;
            }

            //Montre le dialogue après la mort du squelette
            if (showSkeletonDialog) {
                int width = (int)(dialogBox.getWidth() * zoom);
                int height = (int)(dialogBox.getHeight() * zoom);

                int dialogX = (getWidth() - width) / 2;
                int dialogY = getHeight() - height - 20;

                g2.drawImage(dialogBox, dialogX, dialogY, width, height, null);
                g2.setColor(Color.BLACK);
                g2.setFont(dialogFont);
                FontMetrics fm = g2.getFontMetrics();

                String message = jake.dialogSkeleton();
                int lineHeight = fm.getHeight();
                int totalTextHeight = lineHeight;
                int lineY = dialogY + (height - totalTextHeight) / 2 + fm.getAscent();
                g2.drawString(message, dialogX + 50, lineY);
            }
        }


        //Pour afficher les dialogues avec un NPC
        for (NPC n : actualMap.getNPCs()){
            if (n.isNear(player)) {
                if (player.isTalking() && !n.hasTalk()) {

                    int width = (int)(dialogBox.getWidth() * zoom);
                    int height = (int)(dialogBox.getHeight() * zoom);

                    int dialogX = (getWidth() - width) / 2;
                    int dialogY = getHeight() - height - 20;

                    //Affiche la boîte de dialogue centrée en bas
                    g2.drawImage(dialogBox, dialogX, dialogY, width, height, null);

                    //Récupère les dialogues à afficher
                    ArrayList<String> lines = new ArrayList<>();
                    lines.add(n.allDialogs().get(currentDialogIndex));

                    g2.setColor(Color.BLACK);
                    g2.setFont(dialogFont);
                    FontMetrics fm = g2.getFontMetrics();

                    int lineHeight = fm.getHeight();

                    //Calcule la hauteur totale du texte
                    int totalTextHeight = lines.size() * lineHeight;

                    //Texte verticalement centré dans la boîte
                    int lineY = dialogY + (height - totalTextHeight) / 2 + fm.getAscent();

                    for (String line : lines) {
                        g2.drawString(line, dialogX + 50, lineY);
                        lineY += lineHeight;
                    }
                }
            }
        }

        //En gros c'est l'écran de Game Over
        if (showGameOverScreen && gameOverScreen != null) {
            g.drawImage(gameOverScreen, 0, 0, getWidth(), getHeight(), null);

            //Afficher le texte pour recommencer avec un fondu.
            if (readyToRestart) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                String restartText = "Appuyez sur [E] pour recommencer";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(restartText);
                int x = (getWidth() - textWidth) / 2;
                int y = getHeight() - 50;

                //Applique la transparence
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, restartMessageAlpha / 255f));
                g2d.setColor(Color.BLACK);
                g2d.drawString(restartText, x, y);

                g2d.dispose();
            }
        }

        if (!isInventoryOn && !isMapOn && !isMenuOn){
            g2.scale(2, 2);
            g2.drawImage(backpackIcon, 3, 6, null);
            g2.drawImage(mapIcon, 30, 3, null);
        }

        g2.setTransform(originalTransform);


        if (isInventoryOn){
            drawInventory(g2);
        }

        if (isMapOn){
            drawMap(g2);
        }

        if (isMenuOn){
            drawMenu(g2);
        }           
        
    }

    @Override
    public void mousePressed(MouseEvent e){}

    @Override
    public void mouseEntered(MouseEvent e){}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override 
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {

        //Si on a pas mis sur pause
        if (!isMenuOn && !isInventoryOn){
            //Pour chacun des NPCs de la carte :
            for (NPC npc : actualMap.getNPCs()){
                if (npc instanceof Enemy){ //Si le NPC en question est un ennemi :
                    Enemy enemy = (Enemy) npc;
                    //Si l'ennemi est à 8 pixel du joueur et qu'il presse clique gauche
                    if (player.isEnemyInHisRange(enemy, 16) && e.getButton() == MouseEvent.BUTTON1){
                        player.attackEnemy(enemy);
                        System.out.println("PV de l'ennemi : " + enemy.hp);
                    }
                    //Si l'ennemi est à moins de 50 pixels du joueur et qu'il fait clique droit
                    else if (player.isEnemyInHisRange(enemy, 100) && e.getButton() == MouseEvent.BUTTON3){
                        player.attackEnemy(enemy);
                        System.out.println("PV de l'ennemi : " + enemy.hp);
                    }
                }
            }
        }   
        else{
            if (leaveButton.contains(e.getPoint())){
                System.exit(0);
            }
        }
    }


    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
            isMenuOn = !isMenuOn; //Change le statut de notre variable.
            updateCursor(); //Et celui du curseur.
            return;
        }

        if (!isMenuOn){
            if (showSkeletonDialog && e.getKeyCode() == KeyEvent.VK_E) {
                showSkeletonDialog = false;
                return; //Ne pas traiter d'autres touches tant que ce message est affiché
            }

            if (showGameOverScreen && readyToRestart && e.getKeyCode() == KeyEvent.VK_E) {
                restartGame();
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_A){
                isInventoryOn = !isInventoryOn;
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_M){
                isMapOn = !isMapOn;
                updateCursor();
                return;
            }

            for(NPC n : actualMap.getNPCs()){
                if (e.getKeyCode() == KeyEvent.VK_E && n.isNear(player) && n.allDialogs().size() > 0) {
                    if (!player.isTalking()) {
                        player.changeTalk();
                        currentDialogIndex = 0; //Démarrer au début du dialogue
                    } else {
                        currentDialogIndex++;
                        if (currentDialogIndex >= n.allDialogs().size()) {
                            //Fin du dialogue
                            player.changeTalk();
                            n.resetTalk();
                            currentDialogIndex = 0;
                        }
                    }
                }
                else {
                    currentDialogIndex = 0;
                    n.resetTalk();
                    player.talk = false;
                }
            }
        }        
        player.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}


    private void restartGame() {
        player.x = 0;
        player.y = 0;
        player.hp = 100;
        player.reset();

        actualMap = lstPortals.get(0).actualMap;

        showGameOverScreen = false;
        playerIsDead = false;
        readyToRestart = false;
        restartMessageAlpha = 0;
    }

    private void drawMenu(Graphics2D g){

        Composite originalComposite = g.getComposite();

        //Filtre noir
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g.setColor(Color.BLACK); 
        g.fillRect(0, 0, getWidth(), getHeight()); 
            
        g.setComposite(originalComposite);
        
        //Titre du menu
        g.setColor(Color.white);
        g.setFont(dialogFont);
        String txtMenu = "JEU MIS EN PAUSE";
        FontMetrics fm = g.getFontMetrics();
        int txtWidth = fm.stringWidth(txtMenu);
        int x = (getWidth() - txtWidth) / 2;
        g.drawString(txtMenu, x, 50);

        //Bouton pour leave
        g.setColor(Color.GRAY);
        g.fillRect(leaveButton.x, leaveButton.y, leaveButton.width, leaveButton.height);
        g.setColor(Color.WHITE);
        g.drawString("Quitter", leaveButton.x + 33, leaveButton.y + 31);
    }

    public void updateCursor(){
        //Affiche le curseur si le menu est ouvert, sinon le fait disparaitre
        if (isMenuOn || isMapOn){
            setCursor(defaultCursor);
        }
        else{
            setCursor(invisibleCursor);
        }
    }

    private void drawInventory(Graphics2D graphics){

        BufferedImage imageInventory;
        Composite originalComposite = graphics.getComposite();

        BufferedImage imageObject;

        //Filtre noir
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        graphics.setColor(Color.BLACK); 
        graphics.fillRect(0, 0, getWidth(), getHeight()); 

        graphics.setComposite(originalComposite);

        try {
            imageInventory = ImageIO.read(new File("assets/sprites/divers/inventaire.png"));
            graphics.drawImage(imageInventory, (getWidth() - imageInventory.getWidth())/2, (getHeight() - imageInventory.getHeight())/2, null);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        //Coordonnées de la première case
        int xSlot = 85;
        int ySlot = 126;

        for (Tool object : player.lstObjects){
            imageObject = object.image;
            graphics.drawImage(imageObject, xSlot +  (35 - object.width) / 2, ySlot + (35 - object.height) / 2, null);

            xSlot += 84;
            if (xSlot >= 85*4){
                xSlot = 85;
                ySlot += 88;
            }
        }

        graphics.setFont(dialogFont);
        graphics.setColor(Color.WHITE);
        graphics.drawString(String.valueOf(player.nbCoins), 91, 436);
        graphics.drawString(String.valueOf(player.nbArrows), 295, 436);

    }

    private void drawMap(Graphics2D g){

        Composite originalComposite = g.getComposite();

        //Filtre noir
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g.setColor(Color.BLACK); 
        g.fillRect(0, 0, getWidth(), getHeight()); 

        g.setComposite(originalComposite);

        g.drawImage(actualMap.image.getScaledInstance(600, 450, Image.SCALE_DEFAULT), (getWidth() - 600)/2, (getHeight()-450)/2, null);
        
        //Dessiner les obstacles
        double scaleX = 600.0 / 1600.0;
        double scaleY = 450.0 / 1200.0;

        int mapX = (getWidth() - 600) / 2;
        int mapY = (getHeight() - 450) / 2;

        for (Obstacle o : actualMap.getObstacles()) {
            int scaledX = (int)(o.x * scaleX) + mapX;
            int scaledY = (int)(o.y * scaleY) + mapY;
            int scaledWidth = (int)(o.width * scaleX);
            int scaledHeight = (int)(o.height * scaleY);

            g.drawImage(
                o.image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_DEFAULT),
                scaledX,
                scaledY,
                null
            );
        }

        g.setColor(Color.RED);
        g.fillRect((int)(player.x * 0.375 + (getWidth() - 600) / 2), 
        (int)(player.y * 0.375 + (getHeight() - 450) / 2),  
        6, 
        6);
    }

}
