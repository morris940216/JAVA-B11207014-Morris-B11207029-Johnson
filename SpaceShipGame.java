import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class SpaceShipGame extends JPanel implements KeyListener, MouseListener, MouseMotionListener, ActionListener {

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        boolean dangerIncoming = false;
        for (Bullet b : enemyBullets) {
            if (b.dz != 0) {
                double timeToImpact = (b.z - 0) / -b.dz;
                double futureX = b.x + b.dx * timeToImpact;
                double futureY = b.y + b.dy * timeToImpact;
                if (b.z > 0 && timeToImpact > 0 && Math.hypot(futureX - 640, futureY - 360) < 30) {
                    dangerIncoming = true;
                    break;
                }
            }
        }
        setBackground(Color.BLACK);

        g.setColor(Color.WHITE);
        for (Star s : stars) {
            double scale = 300.0 / s.z;
            int x = (int)(640 + (s.x - 640 + offsetX) * scale);
            int y = (int)(360 + (s.y - 360 + offsetY) * scale);
            if (x >= 50 && x <= getWidth() - 50 && y >= 50 && y <= getHeight() - 50) {
                int size = (int)(2 * scale);
                g.fillOval(x, y, size > 0 ? size : 1, size > 0 ? size : 1);
            }
        }

        g.setColor(Color.RED);
        for (Bullet b : enemyBullets) {
            double scale = 300 / b.z;
            int drawX = (int)(640 + ((b.x - 640 + offsetX) * scale));
            int drawY = (int)(360 + ((b.y - 360 + offsetY) * scale));
            int size = (int)(10 * scale);
            g.fillOval(drawX - size / 2, drawY - size / 2, size, size);
        }

        if (dangerIncoming) {
            g.setColor(new Color(255, 50, 50));
        } else {
            g.setColor(Color.DARK_GRAY);
        }
        g.fillRect(0, 0, getWidth(), 50);
        g.fillRect(0, getHeight() - 50, getWidth(), 50);
        g.fillRect(0, 0, 50, getHeight());
        g.fillRect(getWidth() - 50, 0, 50, getHeight());

        g.setColor(Color.RED);
        for (Enemy enemy : enemies) {
            double scale = 300 / enemy.z;
            int cx = (int)(640 + (enemy.x - 640 + offsetX) * scale);
            int cy = (int)(360 + (enemy.y - 360 + offsetY) * scale);
            int size = (int)(20 * scale);
            if (size > 0 && cx >= 50 && cx <= getWidth() - 50 && cy >= 50 && cy <= getHeight() - 50) {
                int[] xPoints = {cx, cx - size / 2, cx + size / 2};
                int[] yPoints = {cy - size / 2, cy + size / 2, cy + size / 2};
                g.fillPolygon(xPoints, yPoints, 3);
            }
        }

        g.setColor(Color.ORANGE);
        for (Explosion ex : explosions) {
            g.fillOval(ex.x - ex.radius / 2, ex.y - ex.radius / 2, ex.radius, ex.radius);
        }

        g.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            int x = (int)(b.x + offsetX);
            int y = (int)(b.y + offsetY);
            g.fillOval(x, y, 5, 5);
        }

        g.setColor(Color.GRAY);
        g.fillRect(600, 610, 10, 30); // left turret
        g.fillRect(670, 610, 10, 30); // right turret

        g.setColor(Color.GREEN);
        g.drawLine(mouseX - 10, mouseY, mouseX + 10, mouseY);
        g.drawLine(mouseX, mouseY - 10, mouseX, mouseY + 10);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 60, 40);
        g.drawString("HP: " + hp, 60, 70);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("[W] Climb [S] Decent [A] Left [D] Right [Mouse] Aim [Click] Fire [ESC] Pause", 200, getHeight() - 20);

        if (paused) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("PAUSED", getWidth() / 2 - 100, getHeight() / 2);
        }

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", getWidth() / 2 - 150, getHeight() / 2);
        }
    }

    private boolean firing, paused, gameOver;
    private boolean up, down, left, right;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Bullet> enemyBullets = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private List<Star> stars = new ArrayList<>();
    private Timer timer, fireTimer;
    private Random random = new Random();
    private int mouseX, mouseY;
    private int offsetX, offsetY;
    private int score = 0;
    private int hp = 100;

    public SpaceShipGame() {
        JFrame frame = new JFrame("SpaceShip 3D FPS");
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setFocusable(true);
        timer = new Timer(16, this);
        fireTimer = new Timer(50, e -> fireBullet());
        timer.start();
        frame.setVisible(true);
        this.requestFocusInWindow();
        this.requestFocus();
        for (int i = 0; i < 100; i++) {
            stars.add(new Star(random.nextInt(1180) + 50, random.nextInt(620) + 50, random.nextInt(800) + 200));
        }
    }

    private void fireBullet() {
        // Draw gun positions relative to player at bottom center with spacing
        int gunBaseY = 360 + 250; // slightly below center
        int gunOffsetX = 40; // spacing from center

        int leftGunX = 640 - gunOffsetX;
        int rightGunX = 640 + gunOffsetX;
        int gunY = gunBaseY;

        bullets.add(new Bullet(leftGunX, gunY, mouseX, mouseY, false));
        bullets.add(new Bullet(rightGunX, gunY, mouseX, mouseY, false));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (paused || gameOver) {
            repaint();
            return;
        }
        if (up) offsetY += 5;
        if (down) offsetY -= 5;
        if (left) offsetX += 5;
        if (right) offsetX -= 5;

        for (Star s : stars) {
            s.z -= 5;
            if (s.z <= 50) {
                s.x = random.nextInt(1180) + 50;
                s.y = random.nextInt(620) + 50;
                s.z = random.nextInt(800) + 200;
            }
        }

        if (random.nextInt(100) < 3) {
            double startX = 640 + random.nextInt(400) - 200;
            double startY = 360 + random.nextInt(300) - 150;
            enemies.add(new Enemy(startX, startY));
        }

        for (Enemy enemy : enemies) {
            enemy.move();
            if (random.nextInt(100) < 2) {
                int startX = (int)(640 + (enemy.x - 640 + offsetX) * 300 / enemy.z);
                int startY = (int)(360 + (enemy.y - 360 + offsetY) * 300 / enemy.z);
                enemyBullets.add(new Bullet(startX, startY, 640, 360, true));
            }
        }

        bullets.forEach(Bullet::move);
        enemyBullets.forEach(Bullet::move);
        explosions.forEach(Explosion::update);

        bullets.removeIf(b -> b.x < 0 || b.x > getWidth() || b.y < 0 || b.y > getHeight());
        enemyBullets.removeIf(b -> b.x < 0 || b.x > getWidth() || b.y < 0 || b.y > getHeight());
        enemies.removeIf(enemy -> enemy.z <= 50);
        explosions.removeIf(ex -> ex.radius > 50);

        List<Enemy> enemiesToRemove = new ArrayList<>();
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet b : bullets) {
            for (Enemy enemy : enemies) {
                double scale = 300 / enemy.z;
                int ex = (int)(640 + (enemy.x - 640 + offsetX) * scale);
                int ey = (int)(360 + (enemy.y - 360 + offsetY) * scale);
                int size = (int)(20 * scale);
                if (Math.hypot(b.x - ex, b.y - ey) < size / 2 + 3) {
                    enemiesToRemove.add(enemy);
                    bulletsToRemove.add(b);
                    explosions.add(new Explosion(ex, ey));
                    score += 10;
                }
            }
        }
        enemies.removeAll(enemiesToRemove);
        bullets.removeAll(bulletsToRemove);

        for (Bullet b : enemyBullets) {
            if (Math.hypot(b.x - 640, b.y - 360) < 20) {
                hp -= 10;
                explosions.add(new Explosion((int)b.x, (int)b.y));
                b.x = -1000;
                b.y = -1000;
                if (hp <= 0) gameOver = true;
            }
        }

        repaint();
    }

    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) paused = !paused;
        if (e.getKeyCode() == KeyEvent.VK_W) up = true;
        if (e.getKeyCode() == KeyEvent.VK_S) down = true;
        if (e.getKeyCode() == KeyEvent.VK_A) left = true;
        if (e.getKeyCode() == KeyEvent.VK_D) right = true;
    }

    @Override public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) up = false;
        if (e.getKeyCode() == KeyEvent.VK_S) down = false;
        if (e.getKeyCode() == KeyEvent.VK_A) left = false;
        if (e.getKeyCode() == KeyEvent.VK_D) right = false;
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) { firing = true; if (!paused) fireTimer.start(); }
    @Override public void mouseReleased(MouseEvent e) { firing = false; fireTimer.stop(); }
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) { new SpaceShipGame(); }

    class Bullet {
        public double x, y, z;
        double dx, dy, dz;
        int lifetime = 0;
        boolean isEnemy = false;

        public Bullet(int startX, int startY, int targetX, int targetY, boolean enemy) {
            x = startX;
            y = startY;
            isEnemy = enemy;
            if (!enemy) {
                double angle = Math.atan2(targetY - startY, targetX - startX);
                dx = 10 * Math.cos(angle);
                dy = 10 * Math.sin(angle);
                dz = 0;
                z = 0;
            } else {
                z = 1000;
                dx = 0;
                dy = 0;
                dz = -20;
            }
        }

        public void move() {
            x += dx;
            y += dy;
            z += dz;
            lifetime++;
        }
    }

    class Enemy {
        double x, y, z = 1000;
        public Enemy(double x, double y) {
            this.x = x;
            this.y = y;
        }
        public void move() {
            z -= 5;
        }
    }

    class Explosion {
        int x, y, radius = 10;
        public Explosion(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public void update() {
            radius += 2;
        }
    }

    class Star {
        int x, y, z;
        public Star(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
