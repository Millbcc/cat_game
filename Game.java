import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class Game extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int FPS = 60;
    
    private Player player;
    private ArrayList<Obstacle> obstacles;
    private ArrayList<Food> foods;
    private ArrayList<Bullet> bullets;

    private boolean running = false;
    private int score = 0;

    private Timer timer;

    private Image playerImg,backgroundImg, strayCatImg[], friendlyCatImg[], trashCanImg, foodImg, bulletImg;

    public Game() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.CYAN);
        setFocusable(true);
        addKeyListener(this);

        backgroundImg = new ImageIcon("background.png").getImage();
        playerImg = new ImageIcon("player.png").getImage();
        strayCatImg = new Image[] {
            new ImageIcon("stray_cat1.png").getImage(),
            new ImageIcon("stray_cat2.png").getImage(),
            new ImageIcon("stray_cat3.png").getImage()
        };
        friendlyCatImg = new Image[] {
            new ImageIcon("friendly_cat1.png").getImage(),
            new ImageIcon("friendly_cat2.png").getImage(),
            new ImageIcon("friendly_cat3.png").getImage()
        };
        trashCanImg = new ImageIcon("trash_can.png").getImage();
        foodImg = new ImageIcon("cat_food.png").getImage();
        bulletImg = new ImageIcon("bullet.png").getImage();

        initGame();
    }

    private void initGame() {
        player = new Player(100, HEIGHT - 80);
        obstacles = new ArrayList<>();
        foods = new ArrayList<>();
        bullets = new ArrayList<>();
        score = 0;
        running = true;

        timer = new Timer(1000 / FPS, this);
        timer.start();

        new Thread(() -> {
            Random random = new Random();
            while (running) {
                try {
                    Thread.sleep(3000);
                    int chance = random.nextInt(10);

                    if (chance < 6) { 
                        obstacles.add(new Obstacle(WIDTH, HEIGHT - 80, 0, random.nextInt(3))); // แมวจรจัด
                    } else if (chance < 8) { 
                        obstacles.add(new Obstacle(WIDTH, HEIGHT - 80, 2, 0)); // ถังขยะ
                    } else { 
                        foods.add(new Food(WIDTH, HEIGHT - 120)); // อาหารแมว
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImg, 0, 0, WIDTH, HEIGHT, null);
        if (running) {
            player.draw(g);
            for (Obstacle obs : obstacles) obs.draw(g);
            for (Food food : foods) food.draw(g);
            for (Bullet bullet : bullets) bullet.draw(g);
            g.setFont(new Font("Verdana", Font.PLAIN, 16));
            g.setColor(Color.BLACK);
            g.drawString("Score: " + score, 10, 20);
            g.drawString("Food: " + player.getFood(), 10, 40);
        } else {
            g.setFont(new Font("Verdana", Font.PLAIN, 16));
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over", WIDTH / 2 - 100, HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Score: " + score, WIDTH / 2 - 40, HEIGHT / 2 + 30);
            g.drawString("Press R to Restart", WIDTH / 2 - 70, HEIGHT / 2 + 60);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            player.update();

            for (int i = 0; i < obstacles.size(); i++) {
                Obstacle obs = obstacles.get(i);
                obs.update();

                if (obs.getBounds().intersects(player.getBounds())) {
                    if (obs.getType() == 0 && obs.isFriendly()) {
                        score++;
                        obstacles.remove(i);
                    } else if (obs.getType() == 0 || obs.getType() == 2) {
                        running = false;
                    }
                }

                if (obs.getX() < 0) obstacles.remove(i);
            }

            for (int i = 0; i < foods.size(); i++) {
                Food food = foods.get(i);
                food.update();

                if (food.getBounds().intersects(player.getBounds())) {
                    player.collectFood();
                    foods.remove(i);
                }

                if (food.getX() < 0) foods.remove(i);
            }

            for (int i = 0; i < bullets.size(); i++) {
                Bullet bullet = bullets.get(i);
                bullet.update();

                for (Obstacle obs : obstacles) {
                    if (bullet.getBounds().intersects(obs.getBounds()) && obs.getType() == 0 && !obs.isFriendly()) {
                        obs.setFriendly(true);
                        bullets.remove(i);
                        break;
                    }
                }

                if (bullet.getX() > WIDTH) bullets.remove(i);
            }

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && running) {
            player.jump();
        }
        if (e.getKeyCode() == KeyEvent.VK_R && !running) {
            initGame();
        }
        if (e.getKeyCode() == KeyEvent.VK_F && player.getFood() > 0) {
            bullets.add(new Bullet(player.getX() + 50, player.getY() + 20));
            player.useFood();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Adopted cat");
        Game game = new Game();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    class Player {
        private int x;
        private double y, vy; 
        private int food;
        private boolean isJumping;
    
        public Player(int x, int y) {
            this.x = x;
            this.y = y;
            this.vy = 0;
            this.food = 0;
            this.isJumping = false;
        }
    
        public void jump() {
            if (!isJumping) {  
                vy = -15;
                isJumping = true;
            }
        }
    
        public void update() {
            y += vy;
            vy += 0.6;
    
            if (y >= Game.HEIGHT - 80) {
                y = Game.HEIGHT - 80;
                vy = 0;
                isJumping = false;
            }
        }
    
        public int getY() {
            return (int) y;
        }
    
        public void collectFood() { food++; }
        public void useFood() { if (food > 0) food--; }
        public int getFood() { return food; }
        public int getX() { return x; }
        public Rectangle getBounds() { return new Rectangle(x, (int) y, 50, 50); }
        public void draw(Graphics g) { g.drawImage(playerImg, x, (int) y, 50, 50, null); }
    }
    
    

    class Obstacle {
        private int x, y;
        private int type;
        private boolean friendly = false;
        private int colorIndex;

        public Obstacle(int x, int y, int type, int colorIndex) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.colorIndex = colorIndex;
        }

        public void update() { x -= 3; }
        
        public int getType() { return type; }
        public int getX() { return x; }
        public void setFriendly(boolean friendly) { this.friendly = friendly; }
        public boolean isFriendly() { return friendly; }
        public Rectangle getBounds() { return new Rectangle(x, y, 50, 50); }
        public void draw(Graphics g) {
            if (type == 0) {
                g.drawImage(friendly ? friendlyCatImg[colorIndex] : strayCatImg[colorIndex], x, y, 50, 50, null);
            } else if (type == 2) {
                g.drawImage(trashCanImg, x, y, 50, 50, null);
            }
        }
    }

    class Food {
        private int x, y;
        public Food(int x, int y) { this.x = x; this.y = y; }

        public void update() { x -= 2; }

        public int getX() { return x; }
        public Rectangle getBounds() { return new Rectangle(x, y, 30, 30); }
        public void draw(Graphics g) { g.drawImage(foodImg, x, y, 30, 30, null); }
    }

    class Bullet {
        private int x, y;
        public Bullet(int x, int y) { this.x = x; this.y = y; }

        public void update() { x += 6; }

        public int getX() { return x; }
        public Rectangle getBounds() { return new Rectangle(x, y, 10, 10); }
        public void draw(Graphics g) { g.drawImage(bulletImg, x, y, 10, 10, null); }
    }
}