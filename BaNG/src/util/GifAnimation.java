package util;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

public class GifAnimation {
	private int width, height, numFrames, delay, grid;
	private BufferedImage image;

	public GifAnimation(int width, int height, int frames, int delay) {
		if (width <= 0) throw new IllegalArgumentException("Invalid width");
		if (height <= 0) throw new IllegalArgumentException("Invalid height");
		if (frames <= 0) throw new IllegalArgumentException("Invalid frames");
		if (delay <= 0) throw new IllegalArgumentException("Invalid delay");
		this.width = width;
		this.height = height;
		this.numFrames = frames;
		this.delay = delay;
		// Get an extra frame that's left completely transparent,
		// to ensure that there's space in the color palette.
		this.grid = (int) Math.ceil(Math.sqrt(numFrames + 1));
		this.image = new BufferedImage(width*grid, height*grid, BufferedImage.TYPE_INT_ARGB);
	}

	public BufferedImage getFrame(int frame) {
		if (frame < 0 || frame >= numFrames)
			throw new IllegalArgumentException("Invalid frame number");
		return image.getSubimage((frame % grid) * width,
				(frame / grid) * height, width, height);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getNumFrames() {
		return numFrames;
	}

	public int getDelay() {
		return delay;
	}

	public void copyBackground() {
		Graphics2D g = image.createGraphics();
		for (int i = 1; i < numFrames; i++) {
			g.copyArea(0, 0, width, height, (i % grid) * width,
					(i / grid) * height);
		}
	}

	public void write(File file) throws IOException {
		ImageWriter writer = getWriter();
		IIOImage[] images = getImageSequence(writer);
		file.delete();
		ImageOutputStream out = new FileImageOutputStream(file);
		try {
			writer.setOutput(out);
			writer.prepareWriteSequence(null);
			for (IIOImage img : images)
				writer.writeToSequence(img, null);
			writer.endWriteSequence();
		}
		finally {
			out.close();
		}
	}

	private static final String metadataFormat = "javax_imageio_gif_image_1.0";
	private IIOImage[] getImageSequence(ImageWriter writer) throws IOException {
		BufferedImage img = getIndexedImage();
		ImageTypeSpecifier imageType = new ImageTypeSpecifier(img);
		Rectangle[] frameRects = new Rectangle[numFrames];
		int[] holdFrame = new int[numFrames];
		int prevIndex = numFrames;
		int numImages = 1;
		frameRects[0] = new Rectangle(0, 0, width, height);
		for (int i = numFrames - 1; i > 0; i--) {
			frameRects[i] = findDiffRect(img, i);
			if (frameRects[i] != null) {
				holdFrame[i] = prevIndex;
				prevIndex = i;
				numImages++;
			}
		}
		holdFrame[0] = prevIndex;

		IIOImage[] images = new IIOImage[numImages];
		int j = 0;
		for (int i = 0; i < numFrames; i++) {
			Rectangle rect = frameRects[i];
			if (rect == null) continue;
			BufferedImage frame = img.getSubimage(
					rect.x + (i % grid) * width,
					rect.y + (i / grid) * height,
					rect.width, rect.height);

			// Don't try to "optimize" this, the round-off needs to be right.
			int wait = (holdFrame[i] * delay) / 10 - (i * delay) / 10;

			IIOMetadata meta = writer.getDefaultImageMetadata(imageType, null);
			IIOMetadataNode root = new IIOMetadataNode(metadataFormat);

			if (i == 0) {
				IIOMetadataNode apps = new IIOMetadataNode("ApplicationExtensions");
				IIOMetadataNode app = new IIOMetadataNode("ApplicationExtension");
				app.setAttribute("applicationID", "NETSCAPE");
				app.setAttribute("authenticationCode", "2.0");
				app.setUserObject(new byte[]{1,0,0});
				apps.appendChild(app);
				root.appendChild(apps);
			}

			IIOMetadataNode desc = new IIOMetadataNode("ImageDescriptor");
			desc.setAttribute("imageLeftPosition", String.valueOf(rect.x));
			desc.setAttribute("imageTopPosition", String.valueOf(rect.y));
			desc.setAttribute("imageWidth", String.valueOf(rect.width));
			desc.setAttribute("imageHeight", String.valueOf(rect.height));
			desc.setAttribute("interlaceFlag", "true");  // imageio bug workaround
			root.appendChild(desc);

			IIOMetadataNode gce = new IIOMetadataNode("GraphicControlExtension");
			gce.setAttribute("disposalMethod", "doNotDispose");
			gce.setAttribute("userInputFlag", "false");
			gce.setAttribute("transparentColorFlag",
					String.valueOf(i > 0));
			gce.setAttribute("transparentColorIndex", "0");
			gce.setAttribute("delayTime", String.valueOf(wait));
			root.appendChild(gce);

			meta.mergeTree(metadataFormat, root);
			images[j] = new IIOImage(frame, null, meta);
			j++;
		}
		return images;
	}

	private BufferedImage getIndexedImage() throws IOException {
		// Write to gif, then read back.
		// This is stupid, but the needed functionality isn't exposed.
		File file = File.createTempFile("anim", ".gif");
		try {
			ImageIO.write(image, "gif", file);
			return ImageIO.read(file);
		}
		finally {
			file.delete();
		}
	}

	private Rectangle findDiffRect(BufferedImage img, int frame) {
		int left = width;
		int top = height;
		int right = 0;
		int bottom = 0;
		int x1 = (frame % grid) * width;
		int y1 = (frame / grid) * height;
		frame--;
		int x2 = (frame % grid) * width;
		int y2 = (frame / grid) * height;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (img.getRGB(x+x1, y+y1) == img.getRGB(x+x2, y+y2))
					img.setRGB(x+x1, y+y1, 0);  // transparent
				else {
					if (y < top)
						top = y;
					bottom = y;
					if (x < left)
						left = x;
					if (x > right)
						right = x;
				}
			}
		}
		if (bottom < top)
			return null;
		return new Rectangle(left, top, right-left+1, bottom-top+1);
	}

	private ImageWriter getWriter() throws IIOException {
		Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("gif");
		if (!writers.hasNext())
			throw new IIOException("No gif writer found");
		return writers.next();
	}

}
