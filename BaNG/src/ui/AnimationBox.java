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
	private static final int GRID_X = 100;
	private static final int GRID_Y = 50;
	private static final int DELAY = 5;

	private Animation anim, hitAnim;
	private Timer timer;
	protected int tick;
	private Color backgroundColor;
	private double scale = 1;
	private BufferedImage backgroundImage;
	private int hitDelay, hitEnd, hitRange;

	public AnimationBox() {
		timer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tick++;
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
		if (anim == null) return;
		if (tick > anim.getNumFrames() && tick > hitEnd)
			tick = 0;
		Graphics2D g2 = (Graphics2D) g;
		Dimension dim = getSize();
		drawFrame(tick, g2, getAnimBounds(), dim.width, dim.height, true);
	}

	private Rectangle2D.Double getAnimBounds() {
		double range = hitRange + 0.5 * Math.signum((double) hitRange);
		anim.setPosition(-0.5*GRID_X*range, 0.5*GRID_Y*range + GRID_Y);
		Rectangle2D.Double bounds = anim.getBounds();

		if (hitAnim != null) {
			hitAnim.setPosition(0.5*GRID_X*range, -0.5*GRID_Y*range + GRID_Y);
			Rectangle2D.Double hitBounds = hitAnim.getBounds();
			Rectangle2D.union(bounds, hitBounds, bounds);
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

		g.translate(0.5*width, 0.5*height);
		g.scale(scale, scale);
		g.translate(-0.5*bounds.width - bounds.x,
				-0.5*bounds.height - bounds.y);

		if (im != null)
			g.drawImage(im, -im.getWidth()/2, -im.getHeight()/2, null);

		// This makes drawing the background image outrageously slow
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		int num = anim.getNumFrames();
		anim.drawFrame(frame < num ? frame : num-1, g);

		if (frame >= hitDelay && frame < hitEnd)
			hitAnim.drawFrame(frame - hitDelay, g);
	}

	public void setAnimation(Animation anim) {
		if (anim == this.anim) return;
		this.anim = anim;
		timer.stop();
		tick = 0;
		if (anim != null)
			timer.start();
		repaint();
	}

	public void setHitAnimation(Animation hitAnim, int hitDelay) {
		if (hitAnim == this.hitAnim) return;
		this.hitAnim = hitAnim;
		this.hitDelay = hitDelay;
		hitEnd = (hitAnim == null) ? 0 : hitDelay + hitAnim.getNumFrames();
		repaint();
	}

	public void setHitRange(int hitRange) {
		this.hitRange = hitRange;
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

	public File selectOutputFile(String suggest, String ext) {
		if (suggest == null) suggest = anim.getName();
		String uc = ext.toUpperCase();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter(uc + " Images", ext));
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
		Rectangle2D.Double bounds = getAnimBounds();
		int width = (int) Math.ceil(bounds.width) + 2;
		int height = (int) Math.ceil(bounds.getHeight()) + 2;
		int frames = Math.max(anim.getNumFrames(), hitEnd);
		GifAnimation out = new GifAnimation(width, height, frames, DELAY);
		for (int i = 0; i < frames; i++) {
			Graphics2D g = out.getFrame(i).createGraphics();
			drawFrame(i, g, bounds, width, height, false);
		}
		file.delete();
		out.write(file);
	}

	private void writePng(File file) throws IOException {
		Rectangle2D.Double bounds = anim.getBounds(0);
		int width = (int) Math.ceil(bounds.getWidth()) + 2;
		int height = (int) Math.ceil(bounds.getHeight()) + 2;
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