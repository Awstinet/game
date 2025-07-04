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
    private BufferedImage background;
    private int currentDialogIndex = 0;

    ArrayList<Integer> lastPos = new ArrayList<>();
    private BufferedImage dialogBox;

    private Font dialogFont;


    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setFocusable(true);
        this.addKeyListener(this);

        npc = new NPC(100, 100, 32, 32, new ArrayList<String>(List.of("Bonjour", "Caca", "ABABABA")), 100 ,100, 300, 100, 300, 300, 100, 300 );

        //Chargement du sprite du joueur.
        try {
            player = new Player(0, 0);
            player.spritePlayerLoader(1, 1); // 1 ligne, 1 colonne

            lastPos.add(0);
            lastPos.add(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Chargement du background & de la boîte de dialogue
        try {
            background = ImageIO.read(new File("assets/maps/mapPaint.png"));
            dialogBox = ImageIO.read(new File("assets/sprites/dialog_box.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Pour charger la police décriture des dialogues.
        try {
            File fontFile = new File("assets/fonts/pixelify/PixelifySans-SemiBold.ttf");
            Font pixelifyFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            dialogFont = pixelifyFont.deriveFont(Font.PLAIN, 22);
        }
        catch (FontFormatException | IOException e){
            e.printStackTrace();
            dialogFont = new Font("Arial", Font.PLAIN, 22);
        }
        


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

                //Vérifie la collision
                if (player.collidesWithNPC(npc)) {
                    //Si collision, rollback
                    player.setPosition(lastX, lastY);
                }
                //Si pas collision, le npc bouge et son dialogue est reset.
                else{
                    npc.npcMove();
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
        g2.drawImage(background, 0, 0, null);
        player.draw(g2);
        npc.draw(g2);

        //Si le joueur est à proximité du NPC, on affiche une message comme quoi il peut lui parler.
        if (npc.isNear(player)){
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.drawString("Appuyez sur [E] pour parler", npc.getX()-45, npc.getY()-10);
        }



        // ------ Revenir en coordonnées écran ------
        g2.setTransform(originalTransform);

        //Affichage de la boîte de dialogue à l’écran, en bas au centre
        if (npc.isNear(player)) {
            if (player.isTalking() && !npc.hasTalk()) {

                int width = (int)(dialogBox.getWidth() * zoom);
                int height = (int)(dialogBox.getHeight() * zoom);

                int dialogX = (getWidth() - width) / 2;
                int dialogY = getHeight() - height - 20;

                //Affiche la boîte de dialogue centrée en bas
                g2.drawImage(dialogBox, dialogX, dialogY, width, height, null);

                //Récupère les dialogues à afficher
                ArrayList<String> lines = new ArrayList<>();
                lines.add(npc.allDialogs().get(currentDialogIndex));

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




    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_E && npc.isNear(player)) {
            if (!player.isTalking()) {
                player.changeTalk();
                currentDialogIndex = 0; //Démarrer au début du dialogue
            } else {
                currentDialogIndex++;
                if (currentDialogIndex >= npc.allDialogs().size()) {
                    //Fin du dialogue
                    player.changeTalk();
                    npc.resetTalk();
                    currentDialogIndex = 0;
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
