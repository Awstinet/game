package src;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    private Thread gameThread;
    private final int FPS = 60;
    private Player player;
    private NPC npc;
    private BufferedImage background;

    ArrayList<Integer> lastPos = new ArrayList<>();

    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setFocusable(true);
        this.addKeyListener(this);

        npc = new NPC(100, 100, 32, 32);

        //Chargement du sprite du joueur.
        try {
            player = new Player(0, 0);
            player.spritePlayerLoader(1, 1); // 1 ligne, 8 colonnes

            lastPos.add(0);
            lastPos.add(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Chargement du background
        try {
            background = ImageIO.read(new File("assets/maps/mapPaint.png"));
        } catch (IOException e) {
            e.printStackTrace();
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
                // Sauvegarde la position AVANT le déplacement
                int lastX = player.getX();
                int lastY = player.getY();

                // Mise à jour (déplacement)
                player.update();

                // Vérifie la collision
                if (player.collidesWithNPC(npc)) {
                    // Si collision, rollback
                    player.setPosition(lastX, lastY);
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

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        double zoom = 2.0;
        int screenCenterX = getWidth() / 2;
        int screenCenterY = getHeight() / 2;
        double translateX = screenCenterX - player.getX() * zoom;
        double translateY = screenCenterY - player.getY() * zoom;

        g2.translate(translateX, translateY);
        g2.scale(zoom, zoom);

        // Dessiner le fond
        g2.drawImage(background, 0, 0, null);

        // Dessiner le joueur
        player.draw(g2);
        npc.draw(g2);
    }



    @Override
    public void keyPressed(KeyEvent e) {
        player.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
