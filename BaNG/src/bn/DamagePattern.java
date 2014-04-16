package bn;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;

import bn.Ability.TargetSquare;
import bn.Unit.Attack;

public class DamagePattern implements Drawable {

	private static final Font FONT = new Font("Arial", Font.PLAIN, 40);
	private static final Polygon DIAMOND = new Polygon(
			new int[] { 0, GridPoint.GRID_X, 0, -GridPoint.GRID_X },
			new int[] { GridPoint.GRID_Y, 0, -GridPoint.GRID_Y, 0 }, 4);
	private static final AffineTransform SKEW =
			new AffineTransform(1, 0.5, -1, 0.5, 0, 0);
	private static final BasicStroke STROKE = new BasicStroke(2);
	private static final Color REDDISH = new Color(0xff, 0x4c, 0x4c);
	private static final Color DARK_CYAN = new Color(0x00, 0xcc, 0xcc);

	private int startFrame, endFrame;
	private boolean keep;
	private double xPos, yPos;
	private Color color;
	private String label;

	private DamagePattern(int x, int y, int start) {
		xPos = x;
		yPos = y;
		startFrame = start;
		endFrame = start + 40;
	}

	public static void buildAnimation(Attack attack, int pos, int range,
			List<Drawable> list) {
		boolean flip = pos < 0;
		Ability abil = attack.getAbility();
		GridPoint p = new GridPoint(0, pos + range);
		if ("Weapon".equals(abil.getTargetType()))
			p = p.translate(0, -range + (flip ? 1 : -1));
		double damage = attack.getAverageDamage(attack.getMinRank());
		TargetSquare[] targetArea = abil.getTargetArea();
		if (targetArea  != null)
			buildPattern(damage, targetArea, p, flip, abil.getRandomTarget(), list);
	}

	private static void buildPattern(double damage, TargetSquare[] area,
			GridPoint center,
			boolean flip, boolean random, List<Drawable> list) {
		for (TargetSquare sq : area) {
			int x = sq.getX();
			int y = sq.getY();
			if (flip) {
				x = -x;
				y = -y;
			}
			GridPoint loc = center.translate(x, y);
			DamagePattern pat = new DamagePattern(loc.x, loc.y, 0);
			pat.keep = true;

			if (random) {
				int pct = (int) Math.round(100 * sq.getValue());
				if (pct < 1) pct = 1;
				pat.label = pct + "%";
				pat.color = (x == 0 && y == 0) ? DARK_CYAN : Color.CYAN;
			}
			else {
				int dmg = (int) Math.round(damage * sq.getValue());
				if (dmg < 1) dmg = 1;
				pat.label = String.valueOf(dmg);
				pat.color = (x == 0 && y == 0) ? REDDISH : Color.YELLOW;
			}

			list.add(pat);
		}
	}

	private boolean isVisible(int frame) {
		return frame >= startFrame && (keep || frame < endFrame);
	}

	@Override
	public void drawFrame(int frame, Graphics2D g) {
		if (!isVisible(frame)) return;
		g.translate(xPos, yPos);
		g.setColor(color);
		g.fillPolygon(DIAMOND);
		g.setColor(Color.BLACK);
		g.setStroke(STROKE);
		g.drawPolygon(DIAMOND);
		g.setFont(FONT);
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(label, g);
		g.transform(SKEW);
		g.translate(-bounds.getCenterX(), -bounds.getCenterY());
		g.drawString(label, 0, 0);
	}

	@Override
	public double getSortPosition() {
		return yPos - 1000;
	}

	@Override
	public Rectangle2D.Double getBounds() {
		return new Rectangle2D.Double(xPos - GridPoint.GRID_X,
				yPos - GridPoint.GRID_Y,
				2*GridPoint.GRID_X, 2*GridPoint.GRID_Y);
	}

	@Override
	public Rectangle2D.Double getBounds(int frame) {
		return isVisible(frame) ? getBounds() : null;
	}

	@Override
	public int getEndFrame() {
		return endFrame;
	}

}
