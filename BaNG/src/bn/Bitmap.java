package bn;

import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.FileFormatException;
import util.LittleEndianInputStream;

public class Bitmap {

	private static Map<String,SoftReference<Bitmap>> cache =
			new HashMap<String,SoftReference<Bitmap>>();
	private static Set<Bitmap> modified = new HashSet<Bitmap>();

	private String name;
	private int width, height, bits;
	private TexturePaint texture, originalTexture;

	public static Bitmap get(String name) throws IOException {
		String lc = name.toLowerCase();
		Bitmap bmp = null;
		SoftReference<Bitmap> bmpRef = cache.get(lc);
		if (bmpRef != null)
			bmp = bmpRef.get();
		if (bmp == null) {
			bmp = new Bitmap(name);
			bmp.read();
			cache.put(lc, new SoftReference<Bitmap>(bmp));
		}
		return bmp;
	}

	private Bitmap(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getBits() {
		return bits;
	}

	public TexturePaint getTexture() {
		return texture;
	}

	public void replaceTexture(BufferedImage im) {
		texture = new TexturePaint(im, new Rectangle2D.Double(0, 0, 0x8000, 0x8000));
		modified.add(this);  // keep change in memory
	}

	public void restoreTexture() {
		texture = originalTexture;
		modified.remove(this);
	}

	private void read() throws IOException {
		LittleEndianInputStream in = new LittleEndianInputStream(
				GameFiles.open(name + "_0.z2raw"));
		try {
			int ver = in.readInt();
			if (ver < 0 || ver > 1)
				throw new FileFormatException("Unrecognized version");
			width = in.readInt();
			height = in.readInt();
			bits = in.readInt();
			BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			if (ver == 0)
				readRaw(im, in);
			else
				readRLE(im, in);
			texture = new TexturePaint(im, new Rectangle2D.Double(0, 0, 0x8000, 0x8000));
			originalTexture = texture;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			throw new FileFormatException("Invalid array index", e);
		}
		finally {
			in.close();
		}
	}

	private void readRaw(BufferedImage im, LittleEndianInputStream in)
			throws IOException {
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				im.setRGB(x, y, readPix(in));
	}

	private void readRLE(BufferedImage im, LittleEndianInputStream in)
			throws IOException {
		in.readInt(); // length
		int palSize = in.readInt();
		if (palSize < 1 || palSize > 256)
			throw new FileFormatException("Invalid palette size");
		int[] pal = new int[palSize];
		for (int i = 0; i < palSize; i++)
			pal[i] = readPix(in);

		int x = 0;
		int y = 0;
		while (y < height) {
			int c = in.readByte();
			int num = (c >> 1) + 1;
			if ((c & 1) == 0) {
				for (int i = 0; i < num; i++) {
					im.setRGB(x, y, pal[in.readByte()]);
					if (++x >= width) { x = 0; y++; }
				}
			}
			else {
				int pix = pal[in.readByte()];
				for (int i = 0; i < num; i++) {
					im.setRGB(x, y, pix);
					if (++x >= width) { x = 0; y++; }
				}
			}
		}
	}

	private int readPix(LittleEndianInputStream in) throws IOException {
		int r, g, b, a;
		if (bits == 4) {
			int p = in.readByte();
			a = (p & 0xf) * 0x11;
			b = ((p >> 4) & 0xf) * 0x11;
			p = in.readByte();
			g = (p & 0xf) * 0x11;
			r = ((p >> 4) & 0xf) * 0x11;
		}
		else {
			r = in.readByte();
			g = in.readByte();
			b = in.readByte();
			a = in.readByte();
		}
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

}
