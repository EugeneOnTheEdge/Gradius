import java.awt.*;
import java.awt.geom.*;
import java.util.Random;
import java.util.function.Supplier;

public class SpriteFactory {

	private SpriteFactory() {}

	// ****** Helper

	private static float random(float min, float max) {
		if(max > min) { return random(max, min); }
		float range = max - min;
		Random rand = java.util.concurrent.ThreadLocalRandom.current();
		return rand.nextFloat() * range + min;
	}

	// ***** SHIP

	public static Sprite makeShip(Supplier<Rectangle> bounds) {
		int shipScale = Config.getInt("shipScale");
		int xPos = Config.getInt("shipInitX");
		int yPos = Config.getInt("shipInitY");
		int xPoints[] = {xPos, xPos + (int) Config.getInt("shipScale"), xPos};
		int yPoints[] = {yPos, yPos + (int) (0.5*Config.getInt("shipScale")), yPos + (int) Config.getInt("shipScale")};
		int nPoints = 3;
		Color shipBorder = Config.getColor("shipColorBorder"); // white
		Color shipFill = Config.getColor("shipColorFill"); // green

		Shape spaceship = new Polygon(xPoints, yPoints, nPoints);

		return new SpriteImpl(spaceship, bounds, true, shipBorder, shipFill);
	}

	// ***** ASTEROID

	private static class AsteroidImpl extends SpriteImpl {
		public AsteroidImpl(int x, float y, float vX, float vY,
			Supplier<Rectangle> bounds) {

			super(makeShape(x,y), bounds, false, Config.getColor("roidColorBorder"), Config.getColor("roidColorFill"));
			setVelocity(vX, vY);
		}
		private static Shape makeShape(int x, float y) {
			Ellipse2D asteroid = new Ellipse2D.Double(x, y, random(Config.getFloat("roidSizeMin"), Config.getFloat("roidSizeMax")), random(Config.getFloat("roidSizeMin"), Config.getFloat("roidSizeMax")));
			return asteroid;
		}

		@Override
		public boolean isOutOfBounds() {
			return (getShape().getBounds().getX() + getShape().getBounds().getWidth() < 0);
		}
	}

	public static Sprite makeAsteroid(Supplier<Rectangle> bounds, double speedMultiplier) {
		return new AsteroidImpl(
			(int) (bounds.get().getX() + bounds.get().getWidth()), 
			(float) ( random((float) bounds.get().getY(), (float) bounds.get().getHeight()) ),
			(float) ( speedMultiplier * random(Config.getFloat("roidVelXMin"), Config.getFloat("roidVelXMax")) ),
			(float) ( speedMultiplier * random(Config.getFloat("roidVelYMin"), Config.getFloat("roidVelYMax")) ), 
			bounds
		);
	}

	// ***** BULLETS

	private static class BulletImpl extends SpriteImpl {
		public BulletImpl(int x, float y, float vX, float vY,
			Supplier<Rectangle> bounds) {

			super(makeShape(x,y), bounds, false, Config.getColor("bulletColorBorder"), Config.getColor("bulletColorFill"));
			setVelocity(vX, vY);
		}
		private static Shape makeShape(int x, float y) {
			Ellipse2D bullet = new Ellipse2D.Double(x, y, Config.getInt("bulletRadius"), Config.getInt("bulletRadius"));
			return bullet;
		}

		@Override
		public boolean isOutOfBounds() {
			return (getShape().getBounds().getX() > 1500);
		}
	}

	public static Sprite makeBullet(int x, float y, float vX, float vY,
			Supplier<Rectangle> bounds) {
		return new BulletImpl(x, y, vX, vY, bounds);
	}
}
