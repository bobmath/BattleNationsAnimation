package bn;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

public class Animation {
	private Timeline anim;
	private Bitmap bitmap;
	private double xPos, yPos;

	public static Animation get(String name) throws IOException {
		Timeline tl = Timeline.get(name);
		if (tl == null) return null;
		return new Animation(tl);
	}

	public Animation(Timeline anim) throws IOException {
		this.anim = anim;
		bitmap = Bitmap.get(anim.getPackageName());
	}

	public String getName() {
		return anim.getName();
	}

	public Timeline getTimeline() {
		return anim;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public int getNumFrames() {
		return anim.getNumFrames();
	}

	public Frame getFrame(int num) {
		return anim.getFrame(num);
	}

	public double getX() {
		return xPos;
	}

	public double getY() {
		return yPos;
	}

	public void setPosition(double x, double y) {
		xPos = x;
		yPos = y;
	}

	public Rectangle2D.Double getBounds() {
		Rectangle2D.Double bounds = anim.getBounds();
		bounds.x += xPos;
		bounds.y += yPos;
		return bounds;
	}

	public Rectangle2D.Double getBounds(int frame) {
		Rectangle2D.Double bounds = anim.getBounds(frame);
		bounds.x += xPos;
		bounds.y += yPos;
		return bounds;
	}

	public void drawFrame(int num, Graphics2D g) {
		AffineTransform oldTrans = g.getTransform();
		g.translate(xPos, yPos);
		Paint oldPaint = g.getPaint();
		g.setPaint(bitmap.getTexture());
		anim.drawFrame(num, g);
		g.setPaint(oldPaint);
		g.setTransform(oldTrans);
	}

}
