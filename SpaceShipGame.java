import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;


public class SpaceShipGame extends JPanel implements KeyListener, ActionListener {
    private boolean firing, paused, gameOver;
    private boolean up, down, left, right;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Bullet> enemyBullets = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private List<Star> stars = new ArrayList<>();
    private Timer timer, fireTimer;
    private Random random = new Random();
    private int offsetX, offsetY;
    private int score = 0;
    private int hp = 100;
    private Clip bgmClip;
    

    private double velocityX = 0, velocityY = 0;
    private final double ACCELERATION = 0.3;
    private final double MAX_SPEED = 10;

    public SpaceShipGame() {
        JFrame frame = new JFrame("SpaceShip 3D FPS");
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        startBackgroundMusic("/bgm.wav");
        

        this.addKeyListener(this);
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
    
    private void startBackgroundMusic(String soundFile) {
    try {
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/bgm.wav"));
        bgmClip = AudioSystem.getClip();
        bgmClip.open(audioIn);
        bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
    } catch (Exception e) {
        e.printStackTrace();
    }
}
      private Image cockpitImage;

{
    // 載入圖片，在建構子或初始化區塊中
    cockpitImage = new ImageIcon(getClass().getResource("/cockpit.png")).getImage();
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

      g.setColor(Color.RED);
      for (Bullet b : enemyBullets) {
            double scale = 300 / b.z;
            int drawX = (int)(640 + ((b.x - 640 + offsetX) * scale));
            int drawY = (int)(360 + ((b.y - 360 + offsetY) * scale));
            int size = (int)(10 * scale);
            g.fillOval(drawX - size / 2, drawY - size / 2, size, size);
        }


        

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
        
        
        g.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            double scale = 300 / b.z;
            int drawX = (int)(b.x + offsetX);
            int drawY = (int)(b.y + offsetY);
            int size = (int)(20 * scale);
            g.fillOval(drawX - size / 2, drawY - size / 2, size, size);
        }

        g.setColor(Color.ORANGE);
        for (Explosion ex : explosions) {
            g.fillOval(ex.x - ex.radius/2, ex.y - ex.radius/2, ex.radius, ex.radius);
        }
        
        g.drawImage(cockpitImage, 0, 450, getWidth(), getHeight()/2, null);
        
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), 50);
        g.fillRect(0, getHeight() - 50, getWidth(), 50);
        g.fillRect(0, 0, 50, getHeight());
        g.fillRect(getWidth() - 50, 0, 50, getHeight());

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 60, 40);
        g.drawString("HP: " + hp, 60, 70);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("[W] Up [S] Down [A] Left [D] Right [Space] Fire [ESC] Pause", 200, getHeight() - 20);

        if (paused) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("PAUSED", getWidth() / 2 - 100, getHeight() / 2);
        }

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", getWidth() / 2 - 150, getHeight() / 2);
        }
    }
    
    private void playSound(String soundFile) {
    try {
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(soundFile));
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
    } catch (Exception e) {
        e.printStackTrace();
    }
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

        offsetX += velocityX;
        offsetY += velocityY;

        updateGameObjects();
        repaint();
    }

    private void fireBullet() {
        bullets.add(new Bullet(640-offsetX,360-offsetY));
        playSound("/shoot.wav");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) paused = !paused;
        if (e.getKeyCode() == KeyEvent.VK_W) up = true;
        if (e.getKeyCode() == KeyEvent.VK_S) down = true;
        if (e.getKeyCode() == KeyEvent.VK_A) left = true;
        if (e.getKeyCode() == KeyEvent.VK_D) right = true;
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !paused) {
            firing = true;
            fireTimer.start();
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) up = false;
        if (e.getKeyCode() == KeyEvent.VK_S) down = false;
        if (e.getKeyCode() == KeyEvent.VK_A) left = false;
        if (e.getKeyCode() == KeyEvent.VK_D) right = false;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            firing = false;
            fireTimer.stop();
        }
    }
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) { new SpaceShipGame(); }

    // --- 下面是子類別和更新邏輯 ---

    class Bullet {
    public double x, y, z;
    double dx, dy, dz;
    int lifetime = 0;
    boolean isEnemy = false;

    // 玩家子彈建構子
    public Bullet(int startX, int startY) {
    x = startX;
    y = startY;
    z = 100;
    dx = 0;
    dy = 0;
    dz = 20;
    isEnemy = false;
}


    // 敵人子彈建構子
    public Bullet(int startX, int startY, int targetX, int targetY) {
        x = startX;
        y = startY;
        z = 1000;
       dx = 0;
       dy = 0;
        dz = -20;
        isEnemy = true;
    }

        public void move() {
    z += dz;
    lifetime++;
    
}

}
    class Enemy { double x, y, z;
        public Enemy(double x, double y) { this.x = x; this.y = y; this.z = 1000; }
        public void move() { z -= 10; }
    }

    class Star { int x, y, z;
        public Star(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    }

    class Explosion { int x, y, radius = 10;
        public Explosion(int x, int y) { this.x = x; this.y = y; }
        public void update() { radius += 2; }
    }

    private void updateGameObjects() {
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
        for (Enemy e : enemies) {
            e.move();
            if (random.nextInt(100) < 2) {
               int startX = (int)(640 + (e.x - 640 + offsetX) * 300 / e.z);
               int startY = (int)(360 + (e.y - 360 + offsetY) * 300 / e.z);
               int dx = startX - 640;
               int dy = startY - 360;
               double length = Math.hypot(dx, dy);
               int targetX = startX + (int)(dx / length * 100);
               int targetY = startY + (int)(dy / length * 100);
               enemyBullets.add(new Bullet(startX, startY, targetX, targetY));
            }
        }
        
        bullets.forEach(Bullet::move);
        enemyBullets.forEach(Bullet::move);
        explosions.forEach(Explosion::update);

        bullets.removeIf(b -> b.x < 0 || b.x > getWidth() || b.y < 0 || b.y > getHeight() || b.z > 1000);
        enemyBullets.removeIf(b -> b.x < 0 || b.x > getWidth() || b.y < 0 || b.y > getHeight());
        enemies.removeIf(e -> e.z <= 50);
        explosions.removeIf(ex -> ex.radius > 50);

        List<Enemy> enemiesToRemove = new ArrayList<>();
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet b : bullets) {
            for (Enemy e : enemies) {
                  int ex = (int)e.x;
                  int ey = (int)e.y;
                  int ez = (int)e.z;
                  if(ez==b.z){
                     if (Math.hypot(b.x - ex, b.y - ey) < 30) {
                       enemiesToRemove.add(e);
                       bulletsToRemove.add(b);
                       explosions.add(new Explosion(ex, ey));
                       playSound("/explosion.wav");
                       score += 10;
                     }
                  }
            }
        }
        enemies.removeAll(enemiesToRemove);
        bullets.removeAll(bulletsToRemove);
        for (Bullet b : enemyBullets) {
        if(b.z ==20){
            int shipX = 640 -offsetX;
            int shipY = 360 -offsetY;
            if (Math.hypot(b.x - shipX, b.y - shipY) < 20) {
                hp -= 10;
                playSound("/hit.wav");
                explosions.add(new Explosion((int)b.x, (int)b.y));
                b.x = -1000;
                b.y = -1000;
                if (hp <= 0) gameOver = true;
            }
        }
        }
    }
}
