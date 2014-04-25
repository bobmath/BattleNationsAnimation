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

	private DamagePattern(int x, int y) {
		xPos = x;
		yPos = y;
		keep = true;
	}

	public static void buildAnimation(Attack attack, int pos, int range,
			List<Drawable> list) {
		boolean flip = pos < 0;
		Ability abil = attack.getAbility();
		GridPoint center = new GridPoint(0, pos + range);
		int yMin, yMax, yLim;
		if ("Weapon".equals(abil.getTargetType())) {
			yMin = Math.max(-attack.getMaxRange(), -5);
			yLim = -5;
			yMax = 0;
			center = center.translate(0, -range + (flip ? 1 : -1));
		}
		else {
			yLim = yMin = -2;
			yMax = Math.min(attack.getMaxRange() - 1, 2);
		}

		double damage = abil.getRandomTarget() ? 0
				: attack.getAverageDamage(attack.getMinRank());

		TargetSquare[] targetArea = abil.getTargetArea();
		TargetSquare[] damageArea = abil.getDamageArea();
		if (targetArea  == null || targetArea.length == 1) {
			if (damageArea != null)
				targetArea = damageArea;
			else if (targetArea == null)
				targetArea = new TargetSquare[] { TargetSquare.SINGLE_TARGET };
		}
		else if (damageArea != null && damageArea.length != 1) {
			targetArea = TargetSquare.convolution(targetArea, damageArea);
		}

		double max = 0;
		for (TargetSquare sq : targetArea)
			if (sq.getValue() > max)
				max = sq.getValue();
		max -= 1e-6; // fudge

		for (TargetSquare sq : targetArea) {
			int x = sq.getX();
			int y = sq.getY();
			if (y < yMin || y > yMax || x < -4 || x > 4) continue;
			if ((y == yMin || y == yLim) && (x == -4 || x == 4)) continue;
			if (flip) {
				x = -x;
				y = -y;
			}
			GridPoint loc = center.translate(x, y);
			DamagePattern pat = new DamagePattern(loc.x, loc.y);

			if (damage == 0) {
				int pct = (int) Math.round(100 * sq.getValue());
				if (pct < 1) pct = 1;
				pat.label = pct + "%";
				pat.color = (sq.getValue() >= max)
						? DARK_CYAN : Color.CYAN;
			}
			else {
				int dmg = (int) Math.round(damage * sq.getValue());
				if (dmg < 1) dmg = 1;
				pat.label = String.valueOf(dmg);
				pat.color = (sq.getValue() >= max)
						? REDDISH : Color.YELLOW;
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
