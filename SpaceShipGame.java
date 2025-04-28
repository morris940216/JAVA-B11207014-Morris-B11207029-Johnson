import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;

public class SpaceShipGame extends JPanel implements KeyListener, MouseListener, MouseMotionListener, ActionListener {
    private boolean firing, paused;
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

    public SpaceShipGame() {
        JFrame frame = new JFrame("戰艦駕駛 - 真3D版");
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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
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

        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), 50);
        g.fillRect(0, getHeight() - 50, getWidth(), 50);
        g.fillRect(0, 0, 50, getHeight());
        g.fillRect(getWidth() - 50, 0, 50, getHeight());

        g.setColor(Color.RED);
        for (Enemy e : enemies) {
            double scale = 300 / e.z;
            int cx = (int)(640 + (e.x - 640 + offsetX) * scale);
            int cy = (int)(360 + (e.y - 360 + offsetY) * scale);
            int size = (int)(20 * scale);
            if (size > 0 && cx >= 50 && cx <= getWidth() - 50 && cy >= 50 && cy <= getHeight() - 50) {
                int[] xPoints = {cx, cx - size/2, cx + size/2};
                int[] yPoints = {cy - size/2, cy + size/2, cy + size/2};
                g.fillPolygon(xPoints, yPoints, 3);
            }
        }

        g.setColor(Color.ORANGE);
        for (Explosion ex : explosions) {
            g.fillOval(ex.x - ex.radius/2, ex.y - ex.radius/2, ex.radius, ex.radius);
        }

        g.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            g.fillOval(b.x, b.y, 5, 5);
        }

        g.setColor(Color.PINK);
        for (Bullet b : enemyBullets) {
            g.fillOval(b.x, b.y, 5, 5);
        }

        g.setColor(Color.GREEN);
        g.drawLine(mouseX - 10, mouseY, mouseX + 10, mouseY);
        g.drawLine(mouseX, mouseY - 10, mouseX, mouseY + 10);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 60, 40);

        if (paused) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("PAUSED", getWidth() / 2 - 100, getHeight() / 2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (paused) {
            repaint();
            return;
        }

        for (Star s : stars) {
            s.z -= 5;
            if (s.z <= 50) {
                s.x = random.nextInt(1180) + 50;
                s.y = random.nextInt(620) + 50;
                s.z = random.nextInt(800) + 200;
            }
        }

        if (random.nextInt(100) < 3) {
            double startX = getWidth() / 2 + random.nextInt(400) - 200;
            double startY = getHeight() / 2 + random.nextInt(300) - 150;
            enemies.add(new Enemy(startX, startY));
        }

        for (Enemy e1 : enemies) {
            e1.move();
            if (random.nextInt(100) < 2) {
                enemyBullets.add(new Bullet((int)(640 + (e1.x - 640 + offsetX) * 300 / e1.z), (int)(360 + (e1.y - 360 + offsetY) * 300 / e1.z), 640 + offsetX, 360 + offsetY));
            }
        }

        for (Bullet b : bullets) {
            b.move();
        }

        for (Bullet b : enemyBullets) {
            b.move();
        }

        for (Explosion ex : explosions) {
            ex.update();
        }

        bullets.removeIf(b -> b.x < 0 || b.x > getWidth() || b.y < 0 || b.y > getHeight());
        enemyBullets.removeIf(b -> b.x < 0 || b.x > getWidth() || b.y < 0 || b.y > getHeight());
        enemies.removeIf(e1 -> e1.z <= 50);
        explosions.removeIf(ex -> ex.radius > 50);

        List<Enemy> enemiesToRemove = new ArrayList<>();
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet b : bullets) {
            for (Enemy e1 : enemies) {
                double scale = 300 / e1.z;
                int ex = (int)(640 + (e1.x - 640 + offsetX) * scale);
                int ey = (int)(360 + (e1.y - 360 + offsetY) * scale);
                int size = (int)(20 * scale);
                double dist = Math.hypot(b.x - ex, b.y - ey);
                if (dist < size / 2 + 3) {
                    enemiesToRemove.add(e1);
                    bulletsToRemove.add(b);
                    explosions.add(new Explosion(ex, ey));
                    playSound("explosion.wav");
                    score += 10;
                }
            }
        }

        enemies.removeAll(enemiesToRemove);
        bullets.removeAll(bulletsToRemove);

        if (up) offsetY -= 5;
        if (down) offsetY += 5;
        if (left) offsetX -= 5;
        if (right) offsetX += 5;

        repaint();
    }

    private void fireBullet() {
        bullets.add(new Bullet(640, 360, mouseX, mouseY));
    }

    private void playSound(String soundFile) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(soundFile)));
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            paused = !paused;
            if (!paused && firing) {
                fireTimer.start();
            } else {
                fireTimer.stop();
            }
            repaint();
        }
        if (e.getKeyCode() == KeyEvent.VK_W) up = true;
        if (e.getKeyCode() == KeyEvent.VK_S) down = true;
        if (e.getKeyCode() == KeyEvent.VK_A) left = true;
        if (e.getKeyCode() == KeyEvent.VK_D) right = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) up = false;
        if (e.getKeyCode() == KeyEvent.VK_S) down = false;
        if (e.getKeyCode() == KeyEvent.VK_A) left = false;
        if (e.getKeyCode() == KeyEvent.VK_D) right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        firing = true;
        if (!paused) fireTimer.start();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        firing = false;
        fireTimer.stop();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        new SpaceShipGame();
    }
}

class Bullet {
    int x, y;
    double dx, dy;

    public Bullet(int startX, int startY, int targetX, int targetY) {
        x = startX;
        y = startY;
        double angle = Math.atan2(targetY - startY, targetX - startX);
        dx = 10 * Math.cos(angle);
        dy = 10 * Math.sin(angle);
    }

    public void move() {
        x += dx;
        y += dy;
    }
}

class Enemy {
    double x, y, z;

    public Enemy(double x, double y) {
        this.x = x;
        this.y = y;
        this.z = 1000;
    }

    public void move() {
        z -= 5;
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