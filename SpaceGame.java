import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class SpaceGame extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private int spaceshipX = 250;
    private int spaceshipY = 400;
    private boolean left, right, up, down, shooting;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private Random random = new Random();
    private int score = 0;
    private boolean gameOver = false;

    public SpaceGame() {
        this.setFocusable(true);
        this.addKeyListener(this);
        timer = new Timer(15, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Game Over!", 200, 250);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Score: " + score, 240, 300);
            return;
        }

        // 畫太空船
        g.setColor(Color.CYAN);
        g.fillRect(spaceshipX, spaceshipY, 40, 20);
        g.fillRect(spaceshipX + 15, spaceshipY - 10, 10, 10);

        // 畫子彈
        g.setColor(Color.YELLOW);
        for (Bullet bullet : bullets) {
            g.fillRect(bullet.x, bullet.y, 5, 10);
        }

        // 畫敵人
        g.setColor(Color.RED);
        for (Enemy enemy : enemies) {
            g.fillRect(enemy.x, enemy.y, 30, 30);
        }

        // 畫分數
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Score: " + score, 10, 20);
    }

    @Override
   public void actionPerformed(ActionEvent e) {
    if (gameOver) return;

    if (left && spaceshipX > 0) spaceshipX -= 5;
    if (right && spaceshipX < getWidth() - 40) spaceshipX += 5;
    if (up && spaceshipY > 0) spaceshipY -= 5;
    if (down && spaceshipY < getHeight() - 20) spaceshipY += 5;

    Iterator<Bullet> bulletIterator = bullets.iterator();
    while (bulletIterator.hasNext()) {
        Bullet bullet = bulletIterator.next();
        bullet.y -= 10;
        if (bullet.y < 0) {
            bulletIterator.remove();
        }
    }

    Iterator<Enemy> enemyIterator = enemies.iterator();
    while (enemyIterator.hasNext()) {
        Enemy enemy = enemyIterator.next();
        enemy.y += 3;

        if (enemy.getBounds().intersects(getSpaceshipBounds())) {
            gameOver = true;
        }

        if (enemy.y > getHeight()) {
            gameOver = true;
        }
    }

    // ** 修正子彈打到敵人的邏輯 **
    ArrayList<Bullet> bulletsToRemove = new ArrayList<>();
    ArrayList<Enemy> enemiesToRemove = new ArrayList<>();

    for (Bullet bullet : bullets) {
        for (Enemy enemy : enemies) {
            if (bullet.getBounds().intersects(enemy.getBounds())) {
                bulletsToRemove.add(bullet);
                enemiesToRemove.add(enemy);
                score += 10;
            }
        }
    }
    bullets.removeAll(bulletsToRemove);
    enemies.removeAll(enemiesToRemove);

    if (random.nextInt(100) < 2) {
        enemies.add(new Enemy(random.nextInt(getWidth() - 30), 0));
    }

    repaint();
}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) left = true;
        if (key == KeyEvent.VK_RIGHT) right = true;
        if (key == KeyEvent.VK_UP) up = true;
        if (key == KeyEvent.VK_DOWN) down = true;
        if (key == KeyEvent.VK_SPACE) shooting = true;

        if (shooting) {
            bullets.add(new Bullet(spaceshipX + 18, spaceshipY - 10));
            shooting = false; // 一次只射一發
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_RIGHT) right = false;
        if (key == KeyEvent.VK_UP) up = false;
        if (key == KeyEvent.VK_DOWN) down = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    class Bullet {
        int x, y;

        Bullet(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Rectangle getBounds() {
            return new Rectangle(x, y, 5, 10);
        }
    }

    class Enemy {
        int x, y;

        Enemy(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Rectangle getBounds() {
            return new Rectangle(x, y, 30, 30);
        }
    }

    Rectangle getSpaceshipBounds() {
        return new Rectangle(spaceshipX, spaceshipY, 40, 20);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Star Wars: Space Battle");
        SpaceGame game = new SpaceGame();
        frame.add(game);
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
