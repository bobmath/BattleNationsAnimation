package bn;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.IOException;

public class Frame {
	private AffineTransform[] transforms;
	private Polygon[] polys;
	private int xMin, xMax, yMin, yMax;

	protected Frame() {
	}

	public Rectangle getBounds() {
		return new Rectangle(xMin, yMin, xMax-xMin+1, yMax-yMin+1);
	}

	public void draw(Graphics2D g) {
		AffineTransform oldTrans = g.getTransform();
		for (int i = 0; i < polys.length; i++) {
			g.transform(transforms[i]);
			g.fillPolygon(polys[i]);
			g.setTransform(oldTrans);
		}
	}

	protected void read(LittleEndianInputStream in, int ver, int[] coords)
			throws IOException
			{
		int numPts = in.readShort();
		if (numPts < 0 || numPts % 6 != 0)
			throw new FileFormatException("Unexpected frame size");
		int numPolys = numPts / 6;
		polys = new Polygon[numPolys];
		transforms = new AffineTransform[numPolys];
		if (ver > 4) in.readByte();
		if (numPolys == 0) return;

		xMin = Integer.MAX_VALUE;
		xMax = Integer.MIN_VALUE;
		yMin = Integer.MAX_VALUE;
		yMax = Integer.MIN_VALUE;

		int[] p = new int[6];
		int[] x = new int[4];
		int[] y = new int[4];
		for (int i = 0; i < numPolys; i++) {
			for (int j = 0; j < 6; j++)
				p[j] = in.readShort();
			if (p[3] != p[0] || p[4] != p[2])
				throw new FileFormatException("Unexpected frame arrangement");
			int p0 = p[0] * 4;
			int p1 = p[1] * 4;
			int p2 = p[2] * 4;
			int p3 = p[5] * 4;

			int x0 = coords[p0];
			int y0 = coords[p0+1];
			stretchBounds(x0, y0);
			stretchBounds(coords[p1], coords[p1+1]);
			stretchBounds(coords[p2], coords[p2+1]);
			stretchBounds(coords[p3], coords[p3+1]);
			AffineTransform t = new AffineTransform(
					coords[p1] - x0, coords[p1+1] - y0,
					coords[p2] - x0, coords[p2+1] - y0,
					x0, y0);
			x0 = coords[p0+2];
			y0 = coords[p0+3];
			AffineTransform t2 = new AffineTransform(
					coords[p1+2] - x0, coords[p1+3] - y0,
					coords[p2+2] - x0, coords[p2+3] - y0,
					x0, y0);
			try {
				t.concatenate(t2.createInverse());
			}
			catch (NoninvertibleTransformException e) {
				throw new FileFormatException("Bad transform", e);
			}
			transforms[i] = t;

			x[0] = coords[p0+2];  y[0] = coords[p0+3];
			x[1] = coords[p1+2];  y[1] = coords[p1+3];
			x[2] = coords[p2+2];  y[2] = coords[p2+3];
			x[3] = coords[p3+2];  y[3] = coords[p3+3];
			for (int j = 0; j < 4; j++) {
				x0 = x[j];
				y0 = y[j];
			}
			polys[i] = new Polygon(x, y, 4);
		}
	} // read

	private void stretchBounds(int x, int y) {
		if (x < xMin) xMin = x;
		if (x > xMax) xMax = x;
		if (y < yMin) yMin = y;
		if (y > yMax) yMax = y;
	}

}