package bn;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

public class Animation implements Drawable, Cloneable {

	private Timeline timeline;
	private Bitmap bitmap;
	private int numFrames, delay;
	private double xPos, yPos;
	private boolean loop;

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

	public void setGridPosition(int x, int y) {
		GridPoint p = new GridPoint(x, y);
		xPos = p.x;
		yPos = p.y + GridPoint.GRID_Y;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	@Override
	public int getEndFrame() {
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

	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	@Override
	public Rectangle2D.Double getBounds() {
		Rectangle2D.Double bounds = timeline.getBounds();
		bounds.x += xPos;
		bounds.y += yPos;
		return bounds;
	}

	@Override
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

	@Override
	public void drawFrame(int num, Graphics2D g) {
		num -= delay;
		if (num < 0)
			return;
		if (loop)
			num %= numFrames;
		else if (num >= numFrames)
			return;
		g.translate(xPos, yPos);
		g.setPaint(bitmap.getTexture());
		timeline.drawFrame(num, g);
	}

	@Override
	public double getSortPosition() {
		return yPos;
	}

	@Override
	public Animation clone() {
		try {
			return (Animation) super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException("Can't happen", e);
		}
	}

}
