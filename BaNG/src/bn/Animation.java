package bn;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

public class Animation {
	private FrameSequence anim;
	private Bitmap bitmap;
	private double xPos, yPos, scale;
	
	public static Animation get(String name) throws IOException {
		return new Animation(FrameSequence.get(name));
	}
	
	public Animation(FrameSequence anim) throws IOException {
		this.anim = anim;
		bitmap = Bitmap.get(anim.getPackageName());
		scale = bitmap.getMeanSize() / anim.getScale();
	}
	
	public FrameSequence getAnimation() {
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
	
	public double getScale() {
		return scale;
	}
	
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public double getMagnification() {
		return scale * anim.getScale() / bitmap.getMeanSize();
	}
	
	public Rectangle2D.Double getBounds() {
		Rectangle bounds = anim.getBounds();
		return new Rectangle2D.Double(bounds.x * scale, bounds.y * scale,
				bounds.width * scale, bounds.height * scale);
	}
	
	public Rectangle2D.Double getBounds(int frame) {
		Rectangle bounds = anim.getFrame(frame).getBounds();
		return new Rectangle2D.Double(bounds.x * scale, bounds.y * scale,
				bounds.width * scale, bounds.height * scale);
	}

	public void drawFrame(int num, Graphics2D g) {
		AffineTransform oldTrans = g.getTransform();
		g.translate(xPos, yPos);
		g.scale(scale, scale);
		Paint oldPaint = g.getPaint();
		g.setPaint(bitmap.getTexture());
		anim.drawFrame(num, g);
		g.setPaint(oldPaint);
		g.setTransform(oldTrans);
	}
	
}
