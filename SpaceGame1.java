import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceShipGame extends JPanel implements KeyListener, MouseListener, ActionListener {
    private boolean up, down, left, right;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private Timer timer;
    private Random random = new Random();

    public SpaceShipGame() {
        JFrame frame = new JFrame("戰艦駕駛 - 第一人稱 (背景移動版)");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.addKeyListener(this);
        this.addMouseListener(this);
        timer = new Timer(16, this); // 約60FPS
        timer.start();
        frame.setVisible(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);

        // 畫敵人
        g.setColor(Color.RED);
        for (Enemy e : enemies) {
            g.fillRect(e.x, e.y, 20, 20);
        }

        // 畫子彈
        g.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            g.fillOval(b.x, b.y, 5, 5);
        }

        // 畫準星
        g.setColor(Color.GREEN);
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        g.drawLine(centerX - 10, centerY, centerX + 10, centerY);
        g.drawLine(centerX, centerY - 10, centerX, centerY + 10);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 產生敵人
        if (getWidth() > 30 && random.nextInt(100) < 2) {
            enemies.add(new Enemy(random.nextInt(getWidth() - 30), -20));
        }

        // 背景（敵人）隨玩家移動方向反向移動
        int speed = 5;
        int moveX = 0, moveY = 0;
        if (up) moveY = speed;
        if (down) moveY = -speed;
        if (left) moveX = speed;
        if (right) moveX = -speed;

        for (Enemy e1 : enemies) {
            e1.x += moveX;
            e1.y += moveY + 2; // 敵人自然向下漂移
        }

        for (Bullet b : bullets) {
            b.move();
        }

        bullets.removeIf(b -> b.x < 0 || b.x > getWidth() || b.y < 0 || b.y > getHeight());
        enemies.removeIf(e1 -> e1.y > getHeight() + 20);

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) up = true;
        if (code == KeyEvent.VK_S) down = true;
        if (code == KeyEvent.VK_A) left = true;
        if (code == KeyEvent.VK_D) right = true;
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
    public void mouseClicked(MouseEvent e) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        bullets.add(new Bullet(centerX, centerY, e.getX(), e.getY()));
    }

    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
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
