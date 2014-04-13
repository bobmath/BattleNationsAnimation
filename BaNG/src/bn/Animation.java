package bn;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

public class Animation {

	private Timeline timeline;
	private Bitmap bitmap;
	private int numFrames, delay;
	private double xPos, yPos;

	public static Animation get(String name) throws IOException {
		Timeline timeline = Timeline.get(name);
		if (timeline == null) return null;
		return new Animation(timeline);
	}

	public Animation(Timeline timeline) throws IOException {
		this.timeline = timeline;
		this.numFrames = timeline.getNumFrames();
		bitmap = Bitmap.get(timeline.getPackageName());
	}

	public String getName() {
		return timeline.getName();
	}

	public Timeline getTimeline() {
		return timeline;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public int getNumFrames() {
		return numFrames;
	}

	public Frame getFrame(int num) {
		num -= delay;
		if (num < 0 || num >= numFrames)
			return null;
		return timeline.getFrame(num);
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

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getEnd() {
		return numFrames + delay;
	}

	public void earlyStop(int frame) {
		frame -= delay;
		if (frame < 1) frame = 1;
		Frame first = timeline.getFrame(0);
		while (frame < numFrames) {
			if (timeline.getFrame(frame) == first) {
				numFrames = frame;
				break;
			}
			frame++;
		}
	}

	public Rectangle2D.Double getBounds() {
		Rectangle2D.Double bounds = timeline.getBounds();
		bounds.x += xPos;
		bounds.y += yPos;
		return bounds;
	}

	public Rectangle2D.Double getBounds(int frame) {
		frame -= delay;
		if (frame < 0 || frame >= numFrames)
			return null;
		Rectangle2D.Double bounds = timeline.getBounds(frame);
		if (bounds != null) {
			bounds.x += xPos;
			bounds.y += yPos;
		}
		return bounds;
	}

	public void drawFrame(int num, Graphics2D g) {
		num -= delay;
		if (num < 0 || num >= numFrames)
			return;
		AffineTransform oldTrans = g.getTransform();
		g.translate(xPos, yPos);
		Paint oldPaint = g.getPaint();
		g.setPaint(bitmap.getTexture());
		timeline.drawFrame(num, g);
		g.setPaint(oldPaint);
		g.setTransform(oldTrans);
	}

}
