import java.awt.*;
import java.awt.geom.*;
import java.util.function.Supplier;

public class SpriteImpl implements Sprite {

	// drawing
	private Shape shape;
	private final Color border;
	private final Color fill;

	// movement
	private float dx, dy;
	private final Supplier<Rectangle> bounds;
	private final boolean isBoundsEnforced;

	protected SpriteImpl(Shape shape, Supplier<Rectangle> bounds, boolean boundsEnforced, Color border, Color fill) {
		this.shape = shape;
		this.bounds = bounds;
		this.isBoundsEnforced = boundsEnforced;
		this.border = border;
		this.fill = fill;
	}

	public Shape getShape() {
		return this.shape;
	}

	public void setVelocity(float dxPerMilli, float dyPerMilli) {
		this.dx = dxPerMilli;
		this.dy = dyPerMilli;
	}

	public void update(int millis) {
		Shape newShape = AffineTransform.getTranslateInstance(this.dx * millis, this.dy * millis).createTransformedShape(this.shape);
		if (!this.isBoundsEnforced || this.isInBounds(newShape)) {
			this.shape = newShape;
		}
		//System.out.println("X: "+this.shape.getBounds().getX()+"    | Y: "+this.shape.getBounds().getY() + "     | dx: " + this.dx*millis + "   |  dy: " + this.dy*millis);
	}

	public boolean isInBounds() {
		return this.isInBounds(this.getShape());
	}
	private boolean isInBounds(Shape s) {
		Rectangle boundaries = s.getBounds();
		return this.bounds.get().intersects(boundaries);
	}
	public boolean isOutOfBounds() {
		return !this.isInBounds();
	}

	public void draw(Graphics2D g2) {
		g2.setColor(this.border);
		g2.draw(this.getShape());
		g2.setColor(this.fill);
		g2.fill(this.getShape());
	}

	public boolean intersects(Sprite other) {
		return this.intersects(other.getShape());
	}
	private boolean intersects(Shape other) {
		if (this.getShape().getBounds().intersects(other.getBounds())) {
			return intersects(new Area(this.getShape()), new Area(other));
		}
		return false;
	}
	private static boolean intersects(Area a, Area b) {
		Area intersectingArea = new Area(a);
		intersectingArea.exclusiveOr(b);
		return !intersectingArea.isEmpty();
	}
}
