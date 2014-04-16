package bn;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public interface Drawable {
	public void drawFrame(int frame, Graphics2D g);
	public double getSortPosition();
	public Rectangle2D.Double getBounds();
	public Rectangle2D.Double getBounds(int frame);
	public int getEndFrame();
}
