import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceShipGame extends JPanel implements KeyListener, MouseListener, MouseMotionListener, ActionListener {
    private boolean up, down, left, right, firing, paused;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private Timer timer, fireTimer;
    private Random random = new Random();
    private int mouseX, mouseY;
    private int playerX, playerY;
    private int score = 0;

    public SpaceShipGame() {
        JFrame frame = new JFrame("戰艦駕駛 - 自由移動版");
        frame.setSize(1280, 720); // 改為固定大小，不要全螢幕
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);

        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setFocusable(true);

        timer = new Timer(16, this); // 約60FPS
        fireTimer = new Timer(1, e -> fireBullet()); // 快速射擊
        timer.start();
        frame.setVisible(true);
        this.requestFocusInWindow();
        this.requestFocus();

        playerX = frame.getWidth() / 2;
        playerY = frame.getHeight() / 2;
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
            g.fillRect(e.x, e.y, 20, 20);
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

        if (getWidth() > 30 && random.nextInt(100) < 2) {
            enemies.add(new Enemy(random.nextInt(getWidth() - 100) + 50, -20));
        }

        int speed = 5;
        if (up && playerY > 50) playerY -= speed;
        if (down && playerY < getHeight() - 50) playerY += speed;
        if (left && playerX > 50) playerX -= speed;
        if (right && playerX < getWidth() - 50) playerX += speed;

        for (Enemy e1 : enemies) {
            e1.y += 2;
        }

        for (Bullet b : bullets) {
            b.move();
        }

        bullets.removeIf(b -> b.x < 0 || b.x > getWidth() || b.y < 0 || b.y > getHeight());
        enemies.removeIf(e1 -> e1.y > getHeight() + 20);

        List<Enemy> enemiesToRemove = new ArrayList<>();
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet b : bullets) {
            for (Enemy e1 : enemies) {
                if (new Rectangle(b.x, b.y, 5, 5).intersects(new Rectangle(e1.x, e1.y, 20, 20))) {
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
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) up = true;
        if (code == KeyEvent.VK_S) down = true;
        if (code == KeyEvent.VK_A) left = true;
        if (code == KeyEvent.VK_D) right = true;
        if (code == KeyEvent.VK_ESCAPE) {
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
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) up = false;
        if (code == KeyEvent.VK_S) down = false;
        if (code == KeyEvent.VK_A) left = false;
        if (code == KeyEvent.VK_D) right = false;
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
    int x, y;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }
}