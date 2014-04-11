package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import util.GifAnimation;
import bn.Animation;

public class AnimationBox extends JComponent {
	private static final long serialVersionUID = 1L;
	private static final Color defaultColor =
			new Color(0xf2, 0xf2, 0xf2); // wiki light gray
	private static final int DELAY = 5;

	private Animation anim;
	private Animation[] anims;
	private Timer timer;
	protected int tick;
	private Color backgroundColor;
	private double scale = 1;
	private BufferedImage backgroundImage;
	private int numFrames;

	public AnimationBox() {
		anims = new Animation[0];
		timer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tick++;
				if (tick >= numFrames)
					tick = 0;
				repaint();
			}
		});
		buildPopup();
	}

	private void buildPopup() {
		final JPopupMenu popup = new JPopupMenu();

		JMenuItem export = new JMenuItem("Export Raw Bitmap");
		export.addActionListener(new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveCurrentRawBitmap();
			}
		});
		popup.add(export);

		JMenuItem replace = new JMenuItem("Replace Raw Bitmap");
		replace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				replaceRawBitmap();
			}
		});
		popup.add(replace);

		JMenuItem restore = new JMenuItem("Restore Raw Bitmap");
		restore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (anim == null) return;
				anim.getBitmap().restoreTexture();
				repaint();
			}
		});
		popup.add(restore);

		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					popup.show(e.getComponent(), e.getX(), e.getY());
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Dimension dim = getSize();
		Rectangle2D.Double bounds = getAnimBounds(-1);
		drawFrame(tick, g2, bounds, dim.width, dim.height, true);
	}

	private Rectangle2D.Double getAnimBounds(int frame) {
		Rectangle2D.Double bounds = null;
		for (Animation a : anims) {
			Rectangle2D.Double b = frame < 0 ? a.getBounds()
					: a.getBounds(frame);
			if (b != null) {
				if (bounds == null)
					bounds = b;
				else
					Rectangle2D.union(bounds, b, bounds);
			}
		}

		BufferedImage im = backgroundImage;
		if (im != null) {
			int width = im.getWidth() - 2;
			int height = im.getHeight() - 2;
			Rectangle2D.Double b = new Rectangle2D.Double(
					-width/2, -height/2, width, height);
			if (bounds == null)
				bounds = b;
			else
				Rectangle2D.intersect(bounds, b, bounds);
		}

		if (bounds != null) {
			bounds.x *= scale;
			bounds.y *= scale;
			bounds.width *= scale;
			bounds.height *= scale;
		}
		return bounds;
	}

	private void drawFrame(int frame, Graphics2D g,
			Rectangle2D.Double bounds, int width, int height,
			boolean transparent) {
		BufferedImage im = backgroundImage;
		if (im == null) {
			Color bg = backgroundColor;
			if (bg != null || !transparent) {
				if (bg == null)
					bg = defaultColor;
				g.setColor(bg);
				g.fillRect(0, 0, width, height);
			}
		}

		if (bounds == null) return;
		g.translate(0.5*(width - bounds.width) - bounds.x,
				0.5*(height - bounds.height) - bounds.y);
		g.scale(scale, scale);

		if (im != null)
			g.drawImage(im, -im.getWidth()/2, -im.getHeight()/2, null);

		// This makes drawing the background image outrageously slow
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		for (Animation a : anims)
			a.drawFrame(frame, g);
	}

	public void setAnimation(Animation anim) {
		setAnimation(anim, null);
	}

	public void setAnimation(Animation anim, List<Animation> anims) {
		timer.stop();
		tick = 0;
		this.anim = anim;
		numFrames = 0;
		if (anim == null) {
			this.anims = new Animation[0];
		}
		else {
			this.anims = (anims == null) ? new Animation[] { anim }
			: anims.toArray(new Animation[anims.size()]);
			for (Animation a : this.anims)
				if (a.getEnd() > numFrames)
					numFrames = a.getEnd();
			timer.start();
		}
		repaint();
	}

	public void setBackgroundColor(Color color) {
		backgroundColor = color;
		backgroundImage = null;
		repaint();
	}

	public void setBackgroundImage(BufferedImage image) {
		backgroundColor = null;
		backgroundImage = image;
		repaint();
	}

	public void setScale(double scale) {
		if (scale <= 0 || scale > 2)
			throw new IllegalArgumentException("Invalid scale");
		this.scale  = scale;
		repaint();
	}

	private File selectOutputFile(String suggest, String ext) {
		String uc = ext.toUpperCase();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter(uc + " Images", ext));
		if (suggest != null)
			fileChooser.setSelectedFile(new File(suggest + "." + ext));
		fileChooser.setDialogTitle("Save as " + uc);
		int userOption = fileChooser.showSaveDialog(this);
		if (userOption != JFileChooser.APPROVE_OPTION)
			return null;
		File file = fileChooser.getSelectedFile();
		if (file.exists()) {
			userOption = JOptionPane.showConfirmDialog(this,
					file + "\nalready exists. Overwrite?",
					"File Exists",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (userOption != JOptionPane.YES_OPTION)
				return null;
		}
		return file;
	}

	public void saveCurrentAsGif() {
		if (anim == null) return;
		File file = selectOutputFile(anim.getName(), "gif");
		if (file == null) return;
		try {
			writeGif(file);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Error writing file.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void saveCurrentAsPng() {
		if (anim == null) return;
		File file = selectOutputFile(anim.getName(), "png");
		if (file == null) return;
		try {
			writePng(file);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Error writing file.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void saveCurrentRawBitmap() {
		if (anim == null) return;
		File file = selectOutputFile(anim.getBitmap().getName(), "png");
		if (file == null) return;
		try {
			BufferedImage im = anim.getBitmap().getTexture().getImage();
			ImageIO.write(im, "png", file);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Error writing file.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void writeGif(File file) throws IOException {
		Rectangle2D.Double bounds = getAnimBounds(-1);
		if (bounds == null) return;
		int width = (int) Math.ceil(bounds.width) + 2;
		int height = (int) Math.ceil(bounds.height) + 2;
		GifAnimation out = new GifAnimation(width, height, numFrames, DELAY);
		for (int i = 0; i < numFrames; i++) {
			Graphics2D g = out.getFrame(i).createGraphics();
			drawFrame(i, g, bounds, width, height, false);
		}
		file.delete();
		out.write(file);
	}

	private void writePng(File file) throws IOException {
		Rectangle2D.Double bounds = getAnimBounds(0);
		if (bounds == null) return;
		int width = (int) Math.ceil(bounds.width) + 2;
		int height = (int) Math.ceil(bounds.height) + 2;
		int type = (backgroundImage == null && backgroundColor == null
				|| backgroundImage.getTransparency() != Transparency.OPAQUE)
				? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		BufferedImage frame = new BufferedImage(width, height, type);
		Graphics2D g = frame.createGraphics();
		drawFrame(0, g, bounds, width, height, true);
		file.delete();
		ImageIO.write(frame, "png", file);
	}

	private void replaceRawBitmap() {
		if (anim == null) return;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
		fileChooser.setDialogTitle("Load PNG");
		int userOption = fileChooser.showOpenDialog(this);
		if (userOption != JFileChooser.APPROVE_OPTION) return;
		File file = fileChooser.getSelectedFile();
		if (file == null) return;

		try {
			BufferedImage im = ImageIO.read(file);
			anim.getBitmap().replaceTexture(im);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Error reading file.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}

		repaint();
	}

}