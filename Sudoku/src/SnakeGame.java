import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Random;

/**
 * SnakeGame.java
 * Jeu Snake simple en Java Swing + FlatLaf.
 * - Un seul fichier exécutable
 * - Utilise FlatLaf pour le look
 * - Touches : Flèches haut/bas/gauche/droite pour diriger, P pour pause, R pour recommencer
 *
 * Pour compiler/exécuter:
 * 1) Télécharge flatlaf-x.y.z.jar depuis https://www.formdev.com/flatlaf/ et ajoute-le au classpath
 *    ou utilisez Maven/Gradle : "com.formdev:flatlaf:VERSION"
 * 2) javac -cp flatlaf-x.y.z.jar SnakeGame.java
 *    java -cp .:flatlaf-x.y.z.jar SnakeGame
 */
public class SnakeGame extends JFrame {

    public SnakeGame() {
        super("Snake - Java Swing + FlatLaf");
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("FlatLaf non disponible, look and feel par défaut utilisé.");
        }

        GamePanel panel = new GamePanel();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        panel.startGame();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SnakeGame::new);
    }

    // ---------------- GamePanel ----------------
    static class GamePanel extends JPanel {
        // Configuration du jeu
        private final int TILE_SIZE = 20;            // taille d'une case en pixels
        private final int GRID_WIDTH = 30;           // nombre de cases horizontalement
        private final int GRID_HEIGHT = 24;          // nombre de cases verticalement
        private final int PANEL_WIDTH = TILE_SIZE * GRID_WIDTH;
        private final int PANEL_HEIGHT = TILE_SIZE * GRID_HEIGHT;
        private final int INITIAL_SNAKE_LENGTH = 5;
        private final int GAME_SPEED_MS = 100;       // délai du timer en ms (plus petit = plus rapide)

        // État du jeu
        private LinkedList<Point> snake;
        private Direction direction = Direction.RIGHT;
        private boolean growing = false;
        private Point apple;
        private boolean running = false;
        private boolean paused = false;
        private Timer timer;
        private Random rand = new Random();
        private int score = 0;

        // Lois du jeu (règles)
        // - Le serpent avance automatiquement à chaque tick
        // - Si le serpent mange une pomme, il grandit (taille++), score++ et une nouvelle pomme apparaît
        // - Si le serpent touche les bords ou se mord la queue, c'est la fin de partie
        // - P pour pause / dépause, R pour recommencer

        public GamePanel() {
            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
            setBackground(Color.black);
            setFocusable(true);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    handleKey(e);
                }
            });

            initTimer();
            initGameState();
        }

        private void initTimer() {
            timer = new Timer(GAME_SPEED_MS, e -> {
                if (running && !paused) {
                    step();
                }
                repaint();
            });
        }

        private void initGameState() {
            snake = new LinkedList<>();
            int startX = GRID_WIDTH / 2;
            int startY = GRID_HEIGHT / 2;
            for (int i = 0; i < INITIAL_SNAKE_LENGTH; i++) {
                snake.add(new Point(startX - i, startY));
            }
            direction = Direction.RIGHT;
            spawnApple();
            score = 0;
            running = false;
            paused = false;
        }

        public void startGame() {
            initGameState();
            running = true;
            timer.start();
        }

        public void restartGame() {
            timer.stop();
            initGameState();
            running = true;
            timer.start();
        }

        private void handleKey(KeyEvent e) {
            int key = e.getKeyCode();
            // Directions (interdit de faire demi-tour instantanément)
            switch (key) {
                case KeyEvent.VK_UP -> {
                    if (direction != Direction.DOWN) direction = Direction.UP;
                }
                case KeyEvent.VK_DOWN -> {
                    if (direction != Direction.UP) direction = Direction.DOWN;
                }
                case KeyEvent.VK_LEFT -> {
                    if (direction != Direction.RIGHT) direction = Direction.LEFT;
                }
                case KeyEvent.VK_RIGHT -> {
                    if (direction != Direction.LEFT) direction = Direction.RIGHT;
                }
                case KeyEvent.VK_P -> {
                    paused = !paused;
                }
                case KeyEvent.VK_R -> {
                    restartGame();
                }
            }
        }

        private void step() {
            Point head = snake.getFirst();
            Point newHead = new Point(head.x, head.y);
            switch (direction) {
                case UP -> newHead.y--;
                case DOWN -> newHead.y++;
                case LEFT -> newHead.x--;
                case RIGHT -> newHead.x++;
            }

            // Règle : collision avec les bords -> game over
            if (newHead.x < 0 || newHead.x >= GRID_WIDTH || newHead.y < 0 || newHead.y >= GRID_HEIGHT) {
                running = false;
                timer.stop();
                return;
            }

            // Règle : collision avec soi-même -> game over
            if (snake.contains(newHead)) {
                running = false;
                timer.stop();
                return;
            }

            // Avancer
            snake.addFirst(newHead);

            // Règle : si pomme mangée
            if (newHead.equals(apple)) {
                growing = true;
                score += 10;
                spawnApple();
            }

            if (!growing) {
                snake.removeLast();
            } else {
                // on a grandi juste après avoir mangé
                growing = false;
            }
        }

        private void spawnApple() {
            Point p;
            do {
                p = new Point(rand.nextInt(GRID_WIDTH), rand.nextInt(GRID_HEIGHT));
            } while (snake.contains(p));
            apple = p;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            // Rendement visuel
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fond quadrillé léger (facultatif)
            g2.setColor(new Color(30, 30, 30));
            for (int x = 0; x <= PANEL_WIDTH; x += TILE_SIZE) g2.drawLine(x, 0, x, PANEL_HEIGHT);
            for (int y = 0; y <= PANEL_HEIGHT; y += TILE_SIZE) g2.drawLine(0, y, PANEL_WIDTH, y);

            // Dessiner la pomme
            if (apple != null) {
                int ax = apple.x * TILE_SIZE;
                int ay = apple.y * TILE_SIZE;
                g2.setColor(Color.red);
                g2.fillOval(ax + 3, ay + 3, TILE_SIZE - 6, TILE_SIZE - 6);
            }

            // Dessiner le serpent
            boolean headDrawn = false;
            int i = 0;
            for (Point p : snake) {
                int px = p.x * TILE_SIZE;
                int py = p.y * TILE_SIZE;
                if (!headDrawn) {
                    // tête
                    g2.setColor(new Color(100, 220, 100));
                    g2.fillRoundRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2, 6, 6);
                    headDrawn = true;
                } else {
                    // corps
                    float t = Math.min(1.0f, 0.3f + (i / (float)Math.max(1, snake.size())));
                    // nuance simple
                    g2.setColor(new Color(40, 180 - (int)(t*60), 40));
                    g2.fillRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                }
                i++;
            }

            // UI (score, état)
            g2.setColor(Color.white);
            g2.setFont(new Font("Consolas", Font.PLAIN, 14));
            String status = "Score: " + score + "    Taille: " + snake.size();
            g2.drawString(status, 8, 18);

            if (!running) {
                drawCenteredString(g2, "Game Over - appuyez sur R pour recommencer", getWidth(), getHeight());
            } else if (paused) {
                drawCenteredString(g2, "PAUSE - appuyez sur P pour reprendre", getWidth(), getHeight());
            }

            g2.dispose();
        }

        private void drawCenteredString(Graphics2D g, String text, int width, int height) {
            FontMetrics fm = g.getFontMetrics();
            int x = (width - fm.stringWidth(text)) / 2;
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();
            // Encadré semi-transparent
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRoundRect(x - 10, y - fm.getAscent() - 10, fm.stringWidth(text) + 20, fm.getHeight() + 16, 10, 10);
            g.setColor(Color.white);
            g.drawString(text, x, y);
        }

        // Direction enum
        private enum Direction {
            UP, DOWN, LEFT, RIGHT
        }
    }
}

