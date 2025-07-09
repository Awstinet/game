package src;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.AffineTransform;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    private Thread gameThread;
    private final int FPS = 60;

    private Player player;
    private NPC npc;

    ArrayList<Integer> lastPos = new ArrayList<Integer>();

    private BufferedImage dialogBox;
    private Font dialogFont;
    private int currentDialogIndex = 0;

    private BufferedImage arbreImg, rockImg;

    private Map actualMap;

    public ArrayList<Portal> lstPortals = new ArrayList<Portal>();


    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setFocusable(true);
        this.addKeyListener(this);

        //Chargement du sprite du joueur.
        try {
            player = new Player(0, 0);
            player.spritePlayerLoader(1, 1); // 1 ligne, 1 colonne

            lastPos.add(0); //Coordonnée X
            lastPos.add(0); //Coordonnée Y

        } catch (IOException e) {
            e.printStackTrace();
        }

        

        //Chargement du background (et obstacles), de la boîte de dialogue et de la police d'écriture des dialogues
        try {
            dialogBox = ImageIO.read(new File("assets/sprites/divers/dialog_box.png"));
            arbreImg = ImageIO.read(new File("assets/sprites/obstacles/arbre.png"));
            rockImg = ImageIO.read(new File("assets/sprites/obstacles/rocher.png"));


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

        //Création des NPCs.
        npc = new NPC(100, 100, 16, 16, new ArrayList<String>(List.of("Bonjour", "Caca", "ABABABA")), "assets/sprites/personnages/amogus.png",100, 100, 300, 100, 300, 300, 100, 300 );
       

        //Création de toutes les maps.
        Map map1 = new Map("test", 1600, 1200, "assets/maps/mapPaint.png",
            new ArrayList<NPC>(List.of(npc)),
            new ArrayList<Obstacle>(List.of(arbre, rocher))
        );

        Map map2 = new Map("map2", 1600, 1200, "assets/maps/mapDesertTest.png",
            new ArrayList<NPC>(),
            new ArrayList<Obstacle>()
        );


        //Création des portails
        lstPortals.add(new Portal(map1, map2, 150, 150));
        lstPortals.add(new Portal(map2, map1, 170, 170));

        //On met la map 1 comme map actuelle.
        actualMap = map1;

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
                //Sauvegarde la position AVANT le déplacement
                int lastX = player.getX();
                int lastY = player.getY();

                //Mise à jour (déplacement)
                player.update();

                boolean hasCollided = false;
                Rectangle playerBounds = new Rectangle(player.getX(), player.getY(), 16, 16);

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

        for (Obstacle obs : actualMap.getObstacles()) {
            if (obs instanceof Arbre) {
                ((Arbre) obs).drawTrunk(g2);
            } else {
                obs.draw(g2);
            }
        }
        player.draw(g2);

        for (NPC n : actualMap.getNPCs()){
            n.draw(g2);
        }
        

        for (Obstacle obs : actualMap.getObstacles()) {
            if (obs instanceof Arbre) {
                ((Arbre) obs).drawFoliage(g2);
            }
        }

        for (NPC n : actualMap.getNPCs()){
            //Si le joueur est à proximité du NPC, on affiche une message comme quoi il peut lui parler.
            if (n.isNear(player)){
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.PLAIN, 10));
                g2.drawString("Appuyez sur [E] pour parler", n.getX()-45, n.getY()-10);
            }
        }

        



        //Retour en "coordonnées écran"
        g2.setTransform(originalTransform);

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
        
    }




    @Override
    public void keyPressed(KeyEvent e) {
        for(NPC n : actualMap.getNPCs()){
            if (e.getKeyCode() == KeyEvent.VK_E && n.isNear(player)) {
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
        }
        

        player.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
