import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.swing.JComponent;
import javax.swing.Timer;

@SuppressWarnings("serial")	
public class GradiusComp extends JComponent {

	private Sprite spaceship;
	private Collection<Sprite> roids;
	private Collection<Sprite> bullets;

	private final int FPS;

	private final Timer timer;
	private final Timer timerAsteroids;
	protected Timer timerBullets;
	protected final Timer timerFuel;
	private final Timer timerMakeHarder;

	private long lastUpdate;
	private ShipKeyListener shipListener;

	// My Creation: STATS
	private double health;
	protected int bulletsAvailable;
	protected float fuel;
	private int score;
	private double speedMultiplier;
	private int asteroidSpawnCount;

	private final static Font GAME_OVER_FONT =
		new Font(Config.getString("gameOverFontName"), Font.BOLD,
		Config.getInt("gameOverFontSize"));

	public GradiusComp() {
		setPreferredSize(new Dimension(
			Config.getInt("compWidth"), Config.getInt("compHeight")));
		setBackground(Color.BLACK);
		setOpaque(true);

		this.FPS = Config.getInt("gameTicksPerSecond");

		this.spaceship = SpriteFactory.makeShip(this::getBounds);
		this.roids = new HashSet<Sprite>();
		this.bullets = new HashSet<Sprite>();

		this.health = 100.0;
		this.bulletsAvailable = 5;
		this.fuel = (float) 100.0;
		this.score = 0;
		this.speedMultiplier = 1;
		this.asteroidSpawnCount = Config.getInt("asteroidMakePerSecond");

		this.timer = new Timer(1000/this.FPS, this::update);
		this.timerAsteroids = new Timer(1000, this::asteroidMakePerSecond);
		this.timerBullets = new Timer(5000, this::refillBullet);
		this.timerFuel = new Timer(1500, this::refillFuel);
		this.timerMakeHarder = new Timer(10000, this::makeHarder);

		this.shipListener = new ShipKeyListener();
		this.shipListener.setBounds(this::getBounds);

		super.addKeyListener(this.shipListener);
	}

	public void update(ActionEvent ae) {
		long now = System.currentTimeMillis();
		int dt = (int) (now - this.lastUpdate);
		this.update(dt);
		this.lastUpdate = now;
		super.repaint();
		if (this.score >= 2500) {
			this.timerMakeHarder.start();
		}
	}

	public void update(int millis) {
		this.spaceship.update(millis);
		this.roids.forEach( (asteroid) -> asteroid.update(millis) );
		this.roids.removeIf( (asteroid) -> asteroid.isOutOfBounds() );
		this.bullets.forEach( (bullet) -> bullet.update(millis) );
		this.bullets.removeIf( (bullet) -> bullet.isOutOfBounds() );
		this.onCollisionDetection();
	}

	// my own method: what to do on detected collision(s).
	public void onCollisionDetection() {
		Collection<Sprite> asteroidsToRemove = new HashSet<Sprite>();
		Collection<Sprite> bulletsToRemove = new HashSet<Sprite>();

		this.roids.forEach( (asteroid) -> {
			if (this.spaceship.intersects(asteroid)) {
				asteroidsToRemove.add(asteroid);
				double asteroidAvgSize = (asteroid.getShape().getBounds().getWidth() + asteroid.getShape().getBounds().getBounds().getHeight()) / 2.0;
				this.health -= asteroidAvgSize;
				if (this.health <= 0.0) {
					this.health = 0.0;
					this.stop();
				}
			}

			this.bullets.forEach( (bullet) -> {
				if (bullet.intersects(asteroid)) {
					bulletsToRemove.add(bullet);
					asteroidsToRemove.add(asteroid);
				}
			});
		});

		asteroidsToRemove.forEach( (a) -> {
			this.roids.remove(a);
			int asteroidAvgSize = (int) (a.getShape().getBounds().getWidth() + a.getShape().getBounds().getBounds().getHeight()) / 2;
			this.score += asteroidAvgSize;
		});
		bulletsToRemove.forEach( (b) -> this.bullets.remove(b));
	}

	public void start() {
		this.lastUpdate = System.currentTimeMillis(); 
		this.timer.start();
		this.timerAsteroids.start();
		this.timerFuel.start();
	}

	public void stop() {
		this.timer.stop();
		this.timerAsteroids.stop();
		this.timerBullets.stop();
		this.timerFuel.stop();
		this.timerMakeHarder.stop();
	}

	public void asteroidMakePerSecond(ActionEvent ae) { //also increments the scoring system
		this.score += this.asteroidSpawnCount * 10;
		for (int i = 0 ; i < this.asteroidSpawnCount ; i++) {
			this.roids.add(SpriteFactory.makeAsteroid(this::getBounds, this.speedMultiplier));
		}
	}

	public void refillBullet(ActionEvent ae) {
		if (this.bulletsAvailable < 5) {
			this.bulletsAvailable++;
		}
		else {
			this.timerBullets.stop();
			this.timerBullets = new Timer(5000, this::refillBullet); // resets the timer
		}
	}

	public void refillFuel(ActionEvent ae) {
		if (this.fuel < 100) {
			this.fuel += Config.getFloat("fuelRefillIncrement");
		}
		if (this.fuel > 100) {
			this.fuel = (float) 100.0;
		}
	}

	public void makeHarder(ActionEvent ae) {
		this.asteroidSpawnCount = this.asteroidSpawnCount < Config.getInt("roidMaxCount") ? this.asteroidSpawnCount + 1 : this.asteroidSpawnCount;
		this.speedMultiplier = this.speedMultiplier < Config.getInt("roidMaxSpeedMultiplier") ? (double) (this.speedMultiplier + Config.getFloat("roidSpeedIncrementer")) : this.speedMultiplier;
		this.health = this.health + Config.getInt("healthIncrement") >= 100 ? 100 : this.health + Config.getInt("healthIncrement");
	}

	public void paintComponent(Graphics g) {
		requestFocusInWindow();
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.paintComponent(g2);
	}
	private void paintComponent(Graphics2D g2) {
		this.spaceship.draw(g2);
		this.roids.forEach( (asteroid) -> asteroid.draw(g2) );
		this.bullets.forEach( (bullet) -> bullet.draw(g2) );

		g2.setColor(Config.getColor("healthFontColor"));
		g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, Config.getInt("healthFontSize")));
		g2.drawString("HP: "+this.health, 25, 25);

		g2.setColor(Config.getColor("bulletColorFill"));
		g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, Config.getInt("healthFontSize")));
		g2.drawString("Bullets [", 200, 25);
		for (int c = 0 ; c < this.bulletsAvailable ; c++) {
			g2.drawString("|", 350 + c*20, 25);
		}
		g2.drawString("]", 460, 25);

		g2.setColor(Config.getColor("fuelFontColor"));
		g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, Config.getInt("fuelFontSize")));
		g2.drawString("Fuel: "+this.fuel+"%", 500, 25);

		g2.setColor(Config.getColor("scoreFontColor"));
		g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, Config.getInt("scoreFontSize")));
		g2.drawString("SCORE: "+this.score, 300, 720);
	}

	private class ShipKeyListener extends KeyAdapter {

		private final float SHIP_VEL_FAST = Config.getFloat("shipVelFast");
		private final float SHIP_VEL_SLOW = Config.getFloat("shipVelSlow");

		private boolean up;
		private boolean down;
		private boolean left;
		private boolean right;

		private float velX = 0;
		private float velY = 0;

		private Supplier<Rectangle> bounds;

		public void setBounds(Supplier<Rectangle> bounds) {
			this.bounds = bounds;
		}

		@Override
		public void keyPressed(KeyEvent key) {
			if (key.getKeyCode() == KeyEvent.VK_SPACE) {
				if (bulletsAvailable > 0) {
					timerBullets.start(); // only refill bullets when there's less than 5 bullets
					bullets.add(SpriteFactory.makeBullet(
						(int) (spaceship.getShape().getBounds().getX() + Config.getInt("shipScale")),
						(float) (spaceship.getShape().getBounds().getY() + (int) (0.5*Config.getInt("shipScale"))),
						Config.getFloat("bulletVelX"),
						(float) 0,
						this.bounds
					));
					bulletsAvailable--;
				}
			}

			if (key.getKeyCode() == KeyEvent.VK_W || key.getKeyCode() == KeyEvent.VK_UP) {
				this.up = true;
				if (key.isShiftDown() && fuel > 0) {
					this.velY = -this.SHIP_VEL_FAST;
				}
				else {
					this.velY = -this.SHIP_VEL_SLOW;
				}
			}
			if (key.getKeyCode() == KeyEvent.VK_S || key.getKeyCode() == KeyEvent.VK_DOWN) {
				this.down = true;
				if (key.isShiftDown() && fuel > 0) {
					this.velY = this.SHIP_VEL_FAST;
				}
				else {
					this.velY = this.SHIP_VEL_SLOW;
				}
			}
			if (key.getKeyCode() == KeyEvent.VK_A || key.getKeyCode() == KeyEvent.VK_LEFT) {
				this.left = true;
				if (key.isShiftDown() && fuel > 0) {
					this.velX = -this.SHIP_VEL_FAST;
				}
				else {
					this.velX = -this.SHIP_VEL_SLOW;
				}
			}
			if (key.getKeyCode() == KeyEvent.VK_D || key.getKeyCode() == KeyEvent.VK_RIGHT) {
				this.right = true;
				if (key.isShiftDown() && fuel > 0) {
					this.velX = this.SHIP_VEL_FAST;
				}
				else {
					this.velX = this.SHIP_VEL_SLOW;	
				}
			}

			if (this.velX == Math.abs(this.SHIP_VEL_FAST) || this.velY == Math.abs(this.SHIP_VEL_FAST)) {
				fuel -= Config.getFloat("fuelUsePerMillisecond");
				if (fuel < 0) {
					fuel = (float) 0.0;
				}
				timerFuel.stop();
			}
			else {
				timerFuel.start();
			}
			spaceship.setVelocity(this.velX, this.velY);
			repaint();
		}

		@Override
		public void keyReleased(KeyEvent key) {
			if (key.getKeyCode() == KeyEvent.VK_W || key.getKeyCode() == KeyEvent.VK_UP) {
				this.up = false;
				this.velY = 0;
			}
			if (key.getKeyCode() == KeyEvent.VK_S || key.getKeyCode() == KeyEvent.VK_DOWN) {
				this.down = false;
				this.velY = 0;
			}
			if (key.getKeyCode() == KeyEvent.VK_A || key.getKeyCode() == KeyEvent.VK_LEFT) {
				this.left = false;
				this.velX = 0;
			}
			if (key.getKeyCode() == KeyEvent.VK_D || key.getKeyCode() == KeyEvent.VK_RIGHT) {
				this.right = false;
				this.velX = 0;
			}
			spaceship.setVelocity(this.velX, this.velY);
			repaint();
		}
	}
}
