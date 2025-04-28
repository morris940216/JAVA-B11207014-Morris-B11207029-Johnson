import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceShipGame extends JPanel implements KeyListener, MouseListener, MouseMotionListener, ActionListener {
    private boolean firing, paused;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private Timer timer, fireTimer;
    private Random random = new Random();
    private int mouseX, mouseY;
    private int playerX, playerY;
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

        playerX = frame.getWidth() / 2;
        playerY = frame.getHeight() - 100;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), 50);
        g.fillRect(0, getHeight() - 50, getWidth(), 50);
        g.fillRect(0, 0, 50, getHeight());
        g.fillRect(getWidth() - 50, 0, 50, getHeight());

        g.setColor(Color.CYAN);
        g.fillOval(playerX - 10, playerY - 10, 20, 20);

        g.setColor(Color.RED);
        for (Enemy e : enemies) {
            double scale = 300 / e.z;
            int drawX = (int)(640 + (e.x - 640) * scale);
            int drawY = (int)(360 + (e.y - 360) * scale);
            int size = (int)(20 * scale);
            if (size > 0) {
                g.fillOval(drawX - size/2, drawY - size/2, size, size);
            }
        }

        g.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
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

        if (random.nextInt(100) < 3) {
            double startX = getWidth() / 2 + random.nextInt(400) - 200;
            double startY = getHeight() / 2 + random.nextInt(300) - 150;
            enemies.add(new Enemy(startX, startY));
        }

        for (Enemy e1 : enemies) {
            e1.move();
        }

        for (Bullet b : bullets) {
            b.move();
        }

        bullets.removeIf(b -> b.x < 0 || b.x > getWidth() || b.y < 0 || b.y > getHeight());
        enemies.removeIf(e1 -> e1.z <= 50);

        List<Enemy> enemiesToRemove = new ArrayList<>();
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet b : bullets) {
            for (Enemy e1 : enemies) {
                double scale = 300 / e1.z;
                int ex = (int)(640 + (e1.x - 640) * scale);
                int ey = (int)(360 + (e1.y - 360) * scale);
                int size = (int)(20 * scale);
                double dist = Math.hypot(b.x - ex, b.y - ey);
                if (dist < size / 2 + 3) {
                    enemiesToRemove.add(e1);
                    bulletsToRemove.add(b);
                    score += 10;
                }
            }
        }

        enemies.removeAll(enemiesToRemove);
        bullets.removeAll(bulletsToRemove);

        repaint();
    }

    private void fireBullet() {
        bullets.add(new Bullet(playerX, playerY, mouseX, mouseY));
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
    }

    @Override
    public void keyReleased(KeyEvent e) {}

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