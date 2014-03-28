package bn;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

public class Animation {

	private static Map<String,String> packageIndex;
	private static Map<String,Animation> animCache;
	
	private String packageName, name;
	private int xMin, xMax, yMin, yMax;
	private Frame[] frames;
	private int[] sequence;
	private Bitmap bitmap;
	
	private static void buildIndex() throws IOException
	{
		packageIndex = new HashMap<String,String>();
		animCache = new HashMap<String,Animation>();
		try {
			JsonObject packs = (JsonObject) GameFiles.readJson("AnimationPacks.json");
			for (JsonValue packname : packs.getJsonArray("animationPacks")) {
				String packstr = ((JsonString) packname).getString();
				JsonObject meta = (JsonObject) GameFiles.readJson(packstr + "_Metadata.json");
				for (JsonValue animname : meta.getJsonArray("animationNames")) {
					String animstr = ((JsonString) animname).getString();
					packageIndex.put(animstr.toLowerCase(), packstr);
				}
			}
		}
		catch (ClassCastException e) {
			throw new FileFormatException("Json type error", e);
		}
	}
	
	public static Animation get(String name) throws IOException
	{
		if (packageIndex == null) buildIndex();
		String lc = name.toLowerCase();
		if (!animCache.containsKey(lc)) {
			String packname = packageIndex.get(lc);
			if (packname == null) return null;
			animCache.put(lc, null);
			readTimeline(packname);
		}
		return animCache.get(lc);
	}
	
	private static void readTimeline(String name) throws IOException
	{
		LittleEndianInputStream in = new LittleEndianInputStream(
				GameFiles.open(name + "_Timeline.bin"), 256);
		try {
			int ver = in.readShort();
			if (ver != 4 && ver != 6 && ver != 8)
				throw new FileFormatException("Unknown version");
			in.readByte();
			int num = in.readShort();
			in.readShort();
			for (int i = 0; i < num; i++)
				new Animation(name).read(in, ver);
		}
		finally {
			in.close();
		}
	}
	
	private Animation(String name) {
		packageName = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getName() {
		return name;
	}
	
	public int getWidth() {
		return xMax - xMin;
	}
	
	public int getHeight() {
		return yMax - yMin;
	}
	
	public int getSize() {
		return Math.max(getWidth(), getHeight());
	}

	public void scale(double size, Graphics2D g) {
		double sc = size / getSize();
		g.scale(sc, sc);
	}
	
	public void center(Graphics2D g) {
		g.translate(-0.5*(xMin + xMax), -0.5*(yMin + yMax));
	}
	
	private void read(LittleEndianInputStream in, int ver) throws IOException
	{
		name = in.readCString(256);
		animCache.put(name.toLowerCase(), this);
		in.readShort();
		readFrames(in, ver);
		readSequence(in, ver);
	}
	
	private void readFrames(LittleEndianInputStream in, int ver) throws IOException
	{
		int numCoords = in.readShort() * 4;
		int extra = 0;
		if (ver == 8) {
			switch (in.readShort()) {
			case 0: break;
			case 1: extra = 4; break;
			case 0x101: extra = 16; break;
			default: throw new FileFormatException("Unknown point size");
			}
		}
		
		int[] coords = new int[numCoords];
		for (int i = 0; i < numCoords; i += 4) {
			coords[i] = in.readShort();
			coords[i+1] = in.readShort();
			if (ver == 4) in.readShort();
			coords[i+2] = in.readShort();
			coords[i+3] = in.readShort();
			for (int j = 0; j < extra; j++)
				in.readByte();
		}
		
		xMin = in.readShort();
		xMax = in.readShort();
		yMin = in.readShort();
		yMax = in.readShort();
		
		int numFrames = in.readShort();
		frames = new Frame[numFrames];
		for (int i = 0; i < numFrames; i++) {
			frames[i] = new Frame();
			frames[i].read(in, ver, coords);
		}
	}

	private void readSequence(LittleEndianInputStream in, int ver) throws IOException
	{
		if (ver == 4) {
			sequence = new int[frames.length];
			for (int i = 0; i < sequence.length; i++)
				sequence[i] = (short) i;
		}
		else {
			int numSeq = in.readShort();
			in.readShort();
			sequence = new int[numSeq];
			for (int i = 0; i < numSeq; i++)
				sequence[i] = in.readShort();
		}
	}

	public Bitmap loadBitmap() throws IOException {
		if (bitmap == null)
			bitmap = Bitmap.get(packageName);
		return bitmap;
	}
	
	public void freeBitmap() {
		bitmap = null;
	}
	
	public Frame getFrame(int num) {
		return frames[num];
	}
	
	public void drawFrame(int num, Graphics2D g) {
		Paint oldPaint = g.getPaint();
		g.setPaint(bitmap.getTexture());
		frames[num].draw(g);
		g.setPaint(oldPaint);
	}
	
	public static class Frame {
		private AffineTransform[] transforms;
		private Polygon[] polys;
		private int xMin, xMax, yMin, yMax;
		
		protected void read(LittleEndianInputStream in, int ver, int[] coords) throws IOException
		{
			int numPts = in.readShort();
			if (numPts % 6 != 0)
				throw new FileFormatException("Unexpected frame size");
			int numPolys = numPts / 6;
			polys = new Polygon[numPolys];
			transforms = new AffineTransform[numPolys];
			if (ver > 4) in.readByte();
			
			xMin = Integer.MAX_VALUE;
			xMax = Integer.MIN_VALUE;
			yMin = Integer.MAX_VALUE;
			yMax = Integer.MIN_VALUE;
			
			int[] p = new int[6];
			int[] x = new int[4];
			int[] y = new int[4];
			for (int i = 0; i < numPolys; i++) {
				for (int j = 0; j < 6; j++) {
					p[j] = in.readShort();
					int p0 = p[j] * 4;
					int x0 = coords[p0];
					int y0 = coords[p0+1];
					if (x0 < xMin) xMin = x0;
					if (x0 > xMax) xMax = x0;
					if (y0 < yMin) yMin = y0;
					if (y0 > yMax) yMax = y0;
				}
				if (p[3] != p[0] || p[4] != p[2])
					throw new FileFormatException("Unexpected frame arrangement");
				int p0 = p[0] * 4;
				int p1 = p[1] * 4;
				int p2 = p[2] * 4;
				int p3 = p[5] * 4;

				int x0 = coords[p0];
				int y0 = coords[p0+1];
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
					throw new FileFormatException("Bad transform");
				}
				transforms[i] = t;
				
				x[0] = coords[p0+2];  y[0] = coords[p0+3];
				x[1] = coords[p1+2];  y[1] = coords[p1+3];
				x[2] = coords[p2+2];  y[2] = coords[p2+3];
				x[3] = coords[p3+2];  y[3] = coords[p3+3];
				polys[i] = new Polygon(x, y, 4);
			}
		}
		
		public int getWidth() {
			return xMax - xMin;
		}
		
		public int getHeight() {
			return yMax - yMin;
		}
		
		public int getSize() {
			return Math.max(getWidth(), getHeight());
		}

		public void scale(double size, Graphics2D g) {
			double sc = size / getSize();
			g.scale(sc, sc);
		}
		
		public void center(Graphics2D g) {
			g.translate(-0.5*(xMin + xMax), -0.5*(yMin + yMax));
		}
		
		public double getScale()
		{
			double[] scale = new double[transforms.length];
			for (int i = 0; i < scale.length; i++)
				scale[i] = Math.abs(transforms[i].getDeterminant());
			Arrays.sort(scale);
			return Math.sqrt(scale[scale.length / 2]);
		}
		
		public void draw(Graphics2D g)
		{
			AffineTransform oldTrans = g.getTransform();
			for (int i = 0; i < polys.length; i++) {
				g.transform(transforms[i]);
				g.fillPolygon(polys[i]);
				g.setTransform(oldTrans);
			}
		}

	} // class Frame

}
