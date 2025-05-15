import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import java.util.Objects;   



public class SpaceShipGame extends JPanel implements KeyListener, ActionListener {
    private boolean firing, paused, gameOver;
    private boolean up, down, left, right;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Bullet> enemyBullets = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private List<Star> stars = new ArrayList<>();
    private boolean warningOn      = false; 
    private int     warningFrames  = 0;     
    private final int WARNING_LEN  = 60;    
    
    private int  wave             = 1;   
    private int  enemiesToSpawn   = 0;   
    private int  spawnCooldown    = 0;   
    private int  enemiesAlive     = 0;   
    private int  interWaveTimer   = 0;   
    private final int COUNTDOWN_FRAMES = 180; 

  
    private boolean soundOn        = true;  
    private int     warnDistance   = 200;   
    private final int WARN_MIN     = 100;    
    private final int WARN_MAX     = 700;   

    private Timer timer, fireTimer;
    private Random random = new Random();
    private int offsetX, offsetY;
    private int score = 0;
    private int hp = 100;
    private Clip bgmClip;
    private boolean shopOpen = false;
    private int coins = 0;          
    
   private double overheat = 0;              
   private double heatPerShot = 3;   
   private double coolRate    = 0.4;   
   private boolean overheated = false; 
   private int cockpitShakeX = 0, cockpitShakeY = 0;       
   private boolean cockpitVisible = true;
   private boolean inital = false;

    

    private double velocityX = 0, velocityY = 0;
    private final double ACCELERATION = 0.3;
    private final double MAX_SPEED = 10;
   
    public SpaceShipGame() {
        JFrame frame = new JFrame("SpaceShip 3D FPS");
        frame.setSize(1920, 1080);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.add(this);
        startBackgroundMusic("/bgm.wav");
        if(inital==false){
            interWaveTimer = 0;
            inital = true;
            }
         
        this.addKeyListener(this);
        this.setFocusable(true);

        timer = new Timer(16, this);
        fireTimer = new Timer(50, e -> fireBullet());
        timer.start();
        frame.setVisible(true);
        this.requestFocusInWindow();
        this.requestFocus();
        startWave(); 
        for (int i = 0; i < 100; i++) {
            stars.add(new Star(random.nextInt(1180) + 50, random.nextInt(620) + 50, random.nextInt(800) + 200));
        }
    }
    
   private void playSound(String path) {
      try (AudioInputStream in = AudioSystem.getAudioInputStream(
            Objects.requireNonNull(getClass().getResource(path)))) {

        Clip clip = AudioSystem.getClip();
        clip.open(in);
        clip.start();

    } catch (Exception ex) {    
        ex.printStackTrace();
    }
}  
   @Override
public void actionPerformed(ActionEvent e) {
    if (paused || gameOver) {
        repaint();
        return;
    }
    if (enemiesAlive < 0) enemiesAlive = 0;
    if (up)    offsetY += 5;
    if (down)  offsetY -= 5;
    if (left)  offsetX += 5;
    if (right) offsetX -= 5;
    if (warningOn && --warningFrames <= 0) warningOn = false;

    offsetX += velocityX;
    offsetY += velocityY;

   
    overheat = Math.max(0, overheat - coolRate);
    if (overheated && overheat < 60) {
        overheated = false;
        if (firing) fireTimer.start();
    }

 
    cockpitShakeX = (int) (-velocityX * 0.8);
    cockpitShakeY = (int) (-velocityY * 0.4);

  
    updateGameObjects();
    repaint();
}


    private void fireBullet() {
      if (overheated) return;                
         bullets.add(new Bullet(640 - offsetX, 360 - offsetY));
         playSound("/shoot.wav");
         overheat += heatPerShot;             
      if (overheat >= 100) {                 
        overheat = 100;
        overheated = true;
        fireTimer.stop();
    }
}
   
    
   private void startWave() {
    enemiesToSpawn = 5 + wave * 3;        
    spawnCooldown  = 60;                  
    enemiesAlive   = 0;
}


   private Enemy createRandomEnemy() {
    double sx = 640 + random.nextInt(400) - 200;
    double sy = 360 + random.nextInt(300) - 150;

    int roll = random.nextInt(100);
   
    int shooterWeight = Math.min(40, 10 + wave * 4);
    int tankWeight    = Math.min(30, 5  + wave * 3);
   
    if (roll < shooterWeight)         return new ShooterEnemy(sx, sy);
    else if (roll < shooterWeight + tankWeight) return new TankEnemy(sx, sy);
    else                              return new ScoutEnemy(sx, sy);
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
   
    cockpitImage = new ImageIcon(getClass().getResource("/cockpit.png")).getImage();
}

@Override
public void paintComponent(Graphics g) {
    super.paintComponent(g);
    setBackground(Color.BLACK);
    if (warningOn && (warningFrames / 6) % 2 == 0) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(new Color(255, 0, 0, 120));        
    g2.fillRect(0, 0, getWidth(), getHeight());     

    g2.setFont(new Font("Arial", Font.BOLD, 72));
    String msg = "WARNING!";
    int strW = g2.getFontMetrics().stringWidth(msg);
    g2.drawString(msg, (getWidth() - strW) / 2, getHeight() / 2);
}
    g.setFont(new Font("Arial", Font.PLAIN, 14));
    int imgW = cockpitImage.getWidth(null);
    int imgH = cockpitImage.getHeight(null);

    double imgscale = (double) getWidth() / imgW;      
    int destH   = getHeight() / 2;                  
    int srcH    = (int) (destH / imgscale);            
    int sy1     = imgH - srcH;                      
    int sy2     = imgH;                             
    int cockpitY = getHeight() - destH;                   

   
    g.setColor(Color.WHITE);
    for (Star s : stars) {
        double scale = 300.0 / s.z;
        int x = (int) (640 + (s.x - 640 + offsetX) * scale);
        int y = (int) (360 + (s.y - 360 + offsetY) * scale);
        if (x >= 50 && x <= getWidth() - 50 && y >= 50 && y <= getHeight() - 50) {
            int size = (int) (2 * scale);
            g.fillOval(x, y, size > 0 ? size : 1, size > 0 ? size : 1);
        }
    }

  
    g.setColor(Color.RED);
    for (Bullet b : enemyBullets) {
        double scale = 300.0 / b.z;
        int size = Math.min((int)(10 * scale), 100); 
        g.fillOval((int)b.x - size/2, (int)b.y - size/2, size, size);
    }

   
    for (Enemy e : enemies) {
        g.setColor(e.color); 
        double scale = 300.0 / e.z;
        int cx = (int)(640 + (e.x - 640 + offsetX) * scale);
        int cy = (int)(360 + (e.y - 360 + offsetY) * scale);
        int size = (int)(20 * scale);
        if (size > 0 && cx >= 50 && cx <= getWidth()-50 && cy >= 50 && cy <= getHeight()-50) {
            int[] xs = {cx, cx - size/2, cx + size/2};
            int[] ys = {cy - size/2, cy + size/2, cy + size/2};
            g.fillPolygon(xs, ys, 3);
        }
    }

   
    g.setColor(Color.YELLOW);
    for (Bullet b : bullets) {
        double scale = 300.0 / b.z;
        int size = Math.min((int)(20 * scale), 22);  
        g.fillOval((int)b.x + offsetX+60 - size/2,
                   (int)b.y + offsetY - size/2, size, size);
    }

    
    g.setColor(Color.ORANGE);
    for (Explosion ex : explosions) {
        g.fillOval(ex.x - ex.radius/2, ex.y - ex.radius/2, ex.radius, ex.radius);
    }
    
        g.setColor(Color.DARK_GRAY);
    g.fillRect(0, 0, getWidth(), 50);
    g.fillRect(0, getHeight()-50, getWidth(), 50);
    g.fillRect(0, 0, 50, getHeight());
    g.fillRect(getWidth()-50, 0, 50, getHeight());


   
    if (cockpitVisible) {
    g.drawImage(
        cockpitImage,20,-50,1500,950,null
    );}

    
    int barW = 300, barH = 20;
    int barX = 80 + cockpitShakeX;
    int barY = cockpitY + 60 + cockpitShakeY;     
    int heatY = barY + 30;

    int radarR = 80;
    int radarX = getWidth() - radarR - 70 + cockpitShakeX;
    int radarY = cockpitY + 60 + cockpitShakeY;


    g.setColor(Color.GRAY);
    g.fillRect(barX, barY+200, barW, barH);
    g.setColor(Color.GREEN);
    g.fillRect(barX, barY+200, (int)(barW * hp / 100.0), barH);
    g.setColor(Color.WHITE);
    g.drawRect(barX, barY+200, barW, barH);
    g.drawString("HP", barX - 40, barY + 16+200);

    

    float heatRatio = (float) overheat / 100f;
    g.setColor(heatRatio < 0.6 ? Color.CYAN :
               heatRatio < 0.9 ? Color.ORANGE : Color.RED);
    g.fillRect(barX, heatY+200, (int)(barW * heatRatio), barH);
    g.setColor(Color.WHITE);
    g.drawRect(barX, heatY+200, barW, barH);
    g.drawString("HEAT", barX - 55, heatY + 16+200);
    if (overheated) {
        g.setColor(Color.RED);
        g.drawString("OVERHEATED!", barX + barW + 15, heatY + 16+200);
    }
    g.drawString(String.format("Heat/Shot: %.1f", heatPerShot), barX + barW + 40, heatY + 16 + 200);

    g.setColor(new Color(0, 32, 0, 180));
    g.fillOval(radarX-120, radarY+155, radarR * 2, radarR * 2);
    g.setColor(Color.CYAN);
    g.drawOval(radarX-120, radarY+155, radarR * 2, radarR * 2);
    g.drawLine(radarX-120 + radarR, radarY+155, radarX + radarR-120, radarY +155+ radarR * 2);
    g.drawLine(radarX-120, radarY+155 + radarR, radarX-120 + radarR * 2, radarY+155 + radarR);

    for (Enemy en : enemies) {
        double sx = 640 + (en.x - 640 + offsetX) * 300 / en.z;
        double sy = 360 + (en.y - 360 + offsetY) * 300 / en.z;
        double dx = (sx - 640) * 0.15;
        double dy = (sy - 360) * 0.15;
        if (Math.hypot(dx, dy) < radarR) {
            int dotX = (int)(radarX + radarR + dx);
            int dotY = (int)(radarY + radarR + dy);
            g.fillOval(dotX - 3-120, dotY +155- 3, 6, 6);
           
        }
    if (shopOpen) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(new Color(0,0,0,200));             
    g2.fillRect(100, 100, getWidth()-200, getHeight()-200);

    g2.setColor(Color.WHITE);
    g2.setFont(new Font("Arial", Font.BOLD, 28));
    g2.drawString("SPACE SHOP", 150, 150);

    g2.setFont(new Font("Arial", Font.PLAIN, 20));
    g2.drawString("[1] Repair 20 HP  (Cost 50)",   150, 210);
    g2.drawString("[2] Faster Fire   (Cost 80)",   150, 250);
    g2.drawString("[3] HeatRate decrease  (Cost 100)",   150, 290);
    g2.drawString("Coins: " + coins,               150, 340);
}

    }

  

    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.BOLD, 24));
    g.drawString("Score: " + score, 60, 40);
    g.drawString("Coins: " + coins, 60, 70);

    g.setFont(new Font("Arial", Font.PLAIN, 18));
    g.drawString("[W] Up [S] Down [A] Left [D] Right [Space] Fire [ESC] Pause [C] Cockpit",
                 120, getHeight() - 20);

    if (paused && !shopOpen) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(new Color(0, 0, 0, 180));
    g2.fillRect(getWidth()/2 - 250, getHeight()/2 - 160, 500, 260);

    g2.setColor(Color.WHITE);
    g2.setFont(new Font("Arial", Font.BOLD, 40));
    g2.drawString("PAUSED", getWidth()/2 - 90, getHeight()/2 - 90);

    g2.setFont(new Font("Arial", Font.PLAIN, 24));
    g2.drawString(String.format("[M] BGM : %s",
                 soundOn ? "ON" : "OFF"),
                 getWidth()/2 - 200, getHeight()/2 - 30);

    g2.drawString(String.format("[Left Arrow/Right Arrow] Warning Dist : %d",
                 warnDistance),
                 getWidth()/2 - 200, getHeight()/2 + 20);

    g2.drawString("[Esc] Return  [B] Shop",
                 getWidth()/2 - 200, getHeight()/2 + 70);
}
    if (gameOver) {
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("GAME OVER", getWidth()/2 - 150, getHeight()/2);
    }
    
if (interWaveTimer > 0) {                       
    g.setFont(new Font("Arial", Font.BOLD, 36));
    g.setColor(Color.YELLOW);

    int sec = interWaveTimer / 60 + 1;          
    String msg = "NEXT WAVE IN " + sec;
    int w = g.getFontMetrics().stringWidth(msg);

    g.drawString(msg, getWidth() - w - 60, 50); 
} else {                                        
    g.setFont(new Font("Arial", Font.BOLD, 36));
    g.setColor(Color.CYAN);

    String msg1 = "WAVE  " + wave;
    g.drawString(msg1, 700, 50);
    String msg = "Still need " + enemiesToSpawn +" to the next Stage";
    g.drawString(msg, 540, 80);                     
}
}


   

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            paused = !paused;
            return;                            }

    
    if (paused && !shopOpen) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_M:              
            soundOn = !soundOn;
            if (soundOn)  startBackgroundMusic("/bgm.wav");
            else if (bgmClip != null && bgmClip.isRunning()) bgmClip.stop();
            break;
        case KeyEvent.VK_B:
             shopOpen = !shopOpen;
             paused = shopOpen;
             if (shopOpen) {
          switch (e.getKeyCode()) {
        case KeyEvent.VK_1:
            if (coins >= 50 && hp <= 80) { hp += 20; coins -= 50; }
            break;
        case KeyEvent.VK_2:
            if (coins >= 80) { fireTimer.setDelay(30); coins -= 80; }
            break;
        case KeyEvent.VK_3:
             if (coins >= 100 && heatPerShot > 1.0) {  
        heatPerShot -= 0.5;                  
        coins -= 100;
    }
          break;
        case KeyEvent.VK_B: 
            break;
        default: return;
    }
    repaint();
    return;           
}
  
             
        case KeyEvent.VK_OPEN_BRACKET:      
        case KeyEvent.VK_LEFT:
            warnDistance = Math.max(WARN_MIN, warnDistance - 10);
            break;
        case KeyEvent.VK_CLOSE_BRACKET:    
        case KeyEvent.VK_RIGHT:
            warnDistance = Math.min(WARN_MAX, warnDistance + 10);
            break;
        default: 
            return;
        }
        repaint();                          
          return;
    }
        if (e.getKeyCode() == KeyEvent.VK_W) up = true;
        if (e.getKeyCode() == KeyEvent.VK_S) down = true;
        if (e.getKeyCode() == KeyEvent.VK_A) left = true;
        if (e.getKeyCode() == KeyEvent.VK_D) right = true;
        
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !paused) {
            firing = true;
            fireTimer.start();
        }
        if (e.getKeyCode() == KeyEvent.VK_C) cockpitVisible = !cockpitVisible;
        if (e.getKeyCode() == KeyEvent.VK_B) {
         shopOpen = !shopOpen;
         paused = shopOpen;          
}

      if (shopOpen) {
    switch (e.getKeyCode()) {
        case KeyEvent.VK_1:
            if (coins >= 50 && hp <= 80) { hp += 20; coins -= 50; }
            break;
        case KeyEvent.VK_2:
            if (coins >= 80) { fireTimer.setDelay(30); coins -= 80; }
            break;
        case KeyEvent.VK_3:
             if (coins >= 100 && heatPerShot > 1.0) {  
        heatPerShot -= 0.5;                  
        coins -= 100;
    }
          break;
        case KeyEvent.VK_B: 
            break;
        default: return;
    }
    repaint();
    return;           
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
    
         if (overheat < 100) overheated = false;
}

    }
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) { new SpaceShipGame(); }

    

  class Bullet {
    double x, y, z;
    double dx, dy, dz;      
    int    lifetime = 0;
    boolean isEnemy;

    
    Bullet(int startX, int startY) {
        x = startX;  y = startY;  z = 100;
        dx = 0;      dy = 0;      dz = 20;
        isEnemy = false;
    }

   
    Bullet(int startX, int startY, int playerX, int playerY) {
        x = startX;  y = startY;  z = 1000;
        double vx = playerX - startX;
        double vy = playerY - startY;
        double len = Math.hypot(vx, vy);
        dx = vx / len * 8;         
        dy = vy / len * 8;
        dz = -20;              
        isEnemy = true;
    }

    void move() {
        x += dx;
        y += dy;
        z += dz;
        lifetime++;
    }
}


  
   


    abstract class Enemy {
    double x, y, z;
    int hp, size;
    Color color;
    int scoreValue, coinValue;

    Enemy(double x, double y, int z,
          int hp, int size, Color color,
          int scoreValue, int coinValue) {
        this.x = x; this.y = y; this.z = z;
        this.hp = hp;
        this.size = size;
        this.color = color;
        this.scoreValue = scoreValue;
        this.coinValue  = coinValue;
    }

    void move()  { z -= 10; }             
    void shoot(List<Bullet> list) { }    
}                                          

  
class ScoutEnemy extends Enemy {
    ScoutEnemy(double x, double y) {
        super(x, y, 800,  
              1, 12, Color.RED,
              10, 5);      
    }
    @Override void move() { z -= 20; }
}

class ShooterEnemy extends Enemy {
    private int cooldown = 0;

    ShooterEnemy(double x, double y) {
        super(x, y, 1000, 3, 18, new Color(255,140,0), 25, 15);
    }

    @Override void shoot(List<Bullet> list) {
        if (--cooldown <= 0) {
            int px = 640 - offsetX;     
            int py = 360 - offsetY;
            list.add(new Bullet((int)x, (int)y, px, py));
            cooldown = 60;             
        }
    }
}

class TankEnemy extends Enemy {
    private int cooldown = 0;               

    TankEnemy(double x, double y) {
        super(x, y, 1200,                     
              6, 26, Color.MAGENTA,          
              50, 30);                    
    }

    @Override void move() { z -= 6; }         

   
    @Override
    void shoot(List<Bullet> list) {
        if (--cooldown > 0) return;         

        int playerX = 640 - offsetX;        
        int playerY = 360 - offsetY;

        for (double angle = -0.3; angle <= 0.3; angle += 0.15) {

           
            double vx = playerX - x;
            double vy = playerY - y;
            double len = Math.hypot(vx, vy);
            double nx = vx / len;
            double ny = vy / len;

         
            double cos = Math.cos(angle), sin = Math.sin(angle);
            double rx = nx * cos - ny * sin;
            double ry = nx * sin + ny * cos;

            
            Bullet b = new Bullet((int) x, (int) y, 0, 0);
            b.dx = rx * 6;        
            b.dy = ry * 6;
            b.dz = -15;           
            b.isEnemy = true;

            list.add(b);
        }
        cooldown = 70;     
    }
}




    class Star { int x, y, z;
        public Star(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    }

    class Explosion { int x, y, radius = 10;
        public Explosion(int x, int y) { this.x = x; this.y = y; }
        public void update() { radius += 2; }
    }

    private void updateGameObjects() {
    List<Enemy> enemiesToAdd = new ArrayList<>();

   

if (enemiesToSpawn > 0) {
    if (--spawnCooldown <= 0) {
        Enemy e = createRandomEnemy();
        enemies.add(e);
        
        enemiesAlive++;
        spawnCooldown = 30 + random.nextInt(40); 
    }
}

else if (enemiesAlive == 0 && interWaveTimer == 0) {
    interWaveTimer = COUNTDOWN_FRAMES;
}


else if (interWaveTimer > 0 && --interWaveTimer == 0) {
    wave++;
    startWave();
}


    for (Star s : stars) {
        s.z -= 5;
        if (s.z <= 50) {
            s.x = random.nextInt(getWidth() - 100) + 50;
            s.y = random.nextInt(getHeight() - 100) + 50;
            s.z = random.nextInt(800) + 200;
        }
    }

    List<Enemy> enemiesToRemove = new ArrayList<>();
    for (Enemy e : enemies) {
        e.move();
        e.shoot(enemyBullets);
        if (e.z <= 50){
         enemiesToRemove.add(e);
         
         enemiesAlive--; }
    }

    bullets.forEach(Bullet::move);
    enemyBullets.forEach(Bullet::move);
    explosions.forEach(Explosion::update);

    bullets.removeIf(b -> b.z > 1000 || b.lifetime > 300);
    enemyBullets.removeIf(b -> b.z < 0);
    explosions.removeIf(ex -> ex.radius > 50);

    List<Bullet> bulletsToRemove = new ArrayList<>();
    for (Bullet b : bullets) {
        for (Enemy e : enemies) {
            if (Math.abs(e.z - b.z) < 30 &&
                Math.hypot(b.x - e.x, b.y - e.y) < 30) {
                e.hp--;
                bulletsToRemove.add(b);
                if (e.hp <= 0) {
                    enemiesToRemove.add(e);
                    double scale = 300.0 / e.z;                
                    int exScr = (int)(640 + (e.x - 640 + offsetX) * scale);
                    int eyScr = (int)(360 + (e.y - 360 + offsetY) * scale);
                    explosions.add(new Explosion(exScr, eyScr));
                    enemiesToSpawn--;
                    enemiesAlive--; 
                    playSound("/explosion.wav");
                    score += e.scoreValue;
                    coins += e.coinValue;
                }
                break;
            }
        }
    }

    int shipX = 640 - offsetX-60;
    int shipY = 360 - offsetY;
    for (Enemy e : enemies) {
    if (!warningOn &&
        e.z <= warnDistance &&
        e.z >= 50  &&
        Math.hypot(e.x - shipX, e.y - shipY) < e.size + 80) {

        warningOn     = true;
        warningFrames = WARNING_LEN;
        playSound("/warning.wav");   
    }
    if (e.z <= 60 && Math.hypot(e.x - shipX, e.y - shipY) < e.size+80) {
        enemiesAlive--; 
        hp -= 20;                         
        e.hp = 0;   
        enemiesToSpawn--;                    
        enemiesToRemove.add(e);         
        playSound("/hit.wav");
       
        explosions.add(new Explosion(shipX, shipY));
        if (hp <= 0) gameOver = true;
        if (gameOver == true&&bgmClip != null && bgmClip.isRunning()) bgmClip.stop();
    }
}
    for (Bullet b : enemyBullets) {
        if (b.z <= 20 && Math.hypot(b.x - shipX, b.y - shipY) < 30) {
            hp -= 10;
            playSound("/hit.wav");
            explosions.add(new Explosion((int) b.x, (int) b.y));
            b.z = -999;
            if (hp <= 0) gameOver = true;
        }
    }

    enemies.removeAll(enemiesToRemove);
    bullets.removeAll(bulletsToRemove);
    enemies.addAll(enemiesToAdd);

}}
