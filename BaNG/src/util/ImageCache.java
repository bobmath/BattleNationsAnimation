package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageCache {
	private static Map<File,SoftReference<BufferedImage>> cache =
			new HashMap<File,SoftReference<BufferedImage>>();

	public static BufferedImage read(File file) throws IOException {
		SoftReference<BufferedImage> ref = cache.get(file);
		if (ref != null) {
			BufferedImage img = ref.get();
			if (img != null) return img;
		}

		BufferedImage img = ImageIO.read(file);
		cache.put(file, new SoftReference<BufferedImage>(img));
		return img;
	}

}
