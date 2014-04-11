package bn;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

public class Frame {
	private AffineTransform[] transforms;
	private Polygon[] polys;
	private AlphaComposite[] alpha;
	private int xMin, xMax, yMin, yMax;

	protected Frame() {
	}

	public Rectangle2D.Double getBounds() {
		if (xMax < xMin) return null;
		return new Rectangle2D.Double(xMin, yMin, xMax-xMin+1, yMax-yMin+1);
	}

	public void draw(Graphics2D g) {
		AffineTransform oldTrans = g.getTransform();
		if (alpha == null) {
			for (int i = 0; i < polys.length; i++) {
				g.transform(transforms[i]);
				g.fillPolygon(polys[i]);
				g.setTransform(oldTrans);
			}
		}
		else {
			Composite oldComp = g.getComposite();
			for (int i = 0; i < polys.length; i++) {
				g.transform(transforms[i]);
				g.setComposite(alpha[i]);
				g.fillPolygon(polys[i]);
				g.setTransform(oldTrans);
			}
			g.setComposite(oldComp);
		}
	}

	protected void read(LittleEndianInputStream in, int ver,
			Timeline.Vertex[] coords) throws IOException {
		int numPts = in.readShort();
		if (numPts < 0 || numPts % 6 != 0)
			throw new FileFormatException("Unexpected frame size");
		int numPolys = numPts / 6;
		polys = new Polygon[numPolys];
		transforms = new AffineTransform[numPolys];
		alpha = new AlphaComposite[numPolys];
		if (ver > 4) in.readByte();

		xMin = Integer.MAX_VALUE;
		xMax = Integer.MIN_VALUE;
		yMin = Integer.MAX_VALUE;
		yMax = Integer.MIN_VALUE;

		double scale = (ver > 4) ? 1.0/32 : 1;
		boolean hasAlpha = false;
		int[] p = new int[6];
		int[] x = new int[4];
		int[] y = new int[4];
		for (int i = 0; i < numPolys; i++) {
			for (int j = 0; j < 6; j++)
				p[j] = in.readShort();
			if (p[3] != p[0] || p[4] != p[2])
				throw new FileFormatException("Unexpected frame arrangement");
			Timeline.Vertex p0 = coords[p[0]], p1 = coords[p[1]],
					p2 = coords[p[2]], p3 = coords[p[5]];

			if (p0.alpha != 1) hasAlpha = true;
			alpha[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p0.alpha);

			stretchBounds(p0.x1, p0.y1);
			stretchBounds(p1.x1, p1.y1);
			stretchBounds(p2.x1, p2.y1);
			stretchBounds(p3.x1, p3.y1);

			AffineTransform t = new AffineTransform(
					(p1.x1 - p0.x1) * scale, (p1.y1 - p0.y1) * scale,
					(p2.x1 - p0.x1) * scale, (p2.y1 - p0.y1) * scale,
					p0.x1 * scale, p0.y1 * scale);
			AffineTransform t2 = new AffineTransform(
					p1.x2 - p0.x2, p1.y2 - p0.y2,
					p2.x2 - p0.x2, p2.y2 - p0.y2,
					p0.x2, p0.y2);
			try {
				t.concatenate(t2.createInverse());
			}
			catch (NoninvertibleTransformException e) {
				throw new FileFormatException("Bad transform", e);
			}
			transforms[i] = t;

			x[0] = p0.x2;  y[0] = p0.y2;
			x[1] = p1.x2;  y[1] = p1.y2;
			x[2] = p2.x2;  y[2] = p2.y2;
			x[3] = p3.x2;  y[3] = p3.y2;
			polys[i] = new Polygon(x, y, 4);
		}

		if (!hasAlpha)
			alpha = null;
	} // read

	private void stretchBounds(int x, int y) {
		if (x < xMin) xMin = x;
		if (x > xMax) xMax = x;
		if (y < yMin) yMin = y;
		if (y > yMax) yMax = y;
	}

}