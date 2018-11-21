import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

public interface Sprite {
	/** Gets the shape of the sprite */
	public Shape getShape();
	/** Sets the velocity of the sprite.
		The sprite will move this far per millisecond. */
	public void setVelocity(float dxPerMilli, float dyPerMilli);
	/** Updates the sprite. Moves it according to its velocity. */
	public void update(int mills);
	/** Determines if the sprite is compltely outside of the supplied bounds. */
	public boolean isOutOfBounds();
	/** Determines if the sprite is completely within the supplied bounds. */
	public boolean isInBounds();
	/** Draw the sprite on the given graphics canvas. */
	public void draw(Graphics2D g2);
	/** Determines if this sprite intersects with the given sprite.
		a/k/a collission detection. */
	public boolean intersects(Sprite other);
}
