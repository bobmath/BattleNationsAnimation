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
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import util.GifAnimation;
import bn.Animation;
import bn.Drawable;

public class AnimationBox extends JComponent {

	private static final long serialVersionUID = 1L;
	private static final Color defaultColor =
			new Color(0xf2, 0xf2, 0xf2); // wiki light gray
	private static final int DELAY = 5;

	private static final Comparator<Drawable> drawOrder =
			new Comparator<Drawable>() {
		@Override
		public int compare(Drawable obj1, Drawable obj2) {
			double pos1 = obj1.getSortPosition();
			double pos2 = obj2.getSortPosition();
			return (pos1 < pos2) ? -1
					: (pos1 > pos2) ? 1 : 0;
		}
	};

	private Animation anim;
	private Drawable[] objects;
	private Timer timer;
	protected int tick;
	private Color backgroundColor;
	private double scale = 1;
	private BufferedImage backgroundImage;
	private int numFrames;
	private boolean paused;
	private JSlider frameSlider;

	public AnimationBox() {
		objects = new Drawable[0];
		timer = new Timer(32, new ActionListener() { //This should be closely synced with in-game anims
			@Override
			public void actionPerformed(ActionEvent e) {
				tick++;
				if (tick >= numFrames)
					tick = 0;
				repaint();
				if (frameSlider != null)
					frameSlider.setValue(tick);
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

	public void setSlider(JSlider frameSlider) {
		this.frameSlider = frameSlider;
		if (frameSlider != null) {
			frameSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					int value = AnimationBox.this.frameSlider.getValue();
					if (value != tick) {
						tick = value;
						repaint();
					}
					if (!paused) {
						if (AnimationBox.this.frameSlider.getValueIsAdjusting())
							timer.stop();
						else if (!timer.isRunning())
							timer.start();
					}
				}
			});
		}
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Dimension dim = getSize();
		Rectangle2D.Double bounds = getAnimBounds(-1);
		drawFrame(tick, g2, bounds, dim, true);
	}

	public Rectangle2D.Double getAnimBounds(int frame) {
		Rectangle2D.Double bounds = null;
		for (Drawable obj : objects) {
			Rectangle2D.Double bnd = frame < 0 ? obj.getBounds()
					: obj.getBounds(frame);
			if (bnd != null) {
				if (bounds == null)
					bounds = bnd;
				else
					Rectangle2D.union(bounds, bnd, bounds);
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

	public static Dimension roundSize(Rectangle2D.Double bounds) {
		if (bounds == null) return null;
		return new Dimension(
				(int) Math.ceil(bounds.width) + 2,
				(int) Math.ceil(bounds.height) + 2);
	}

	private void drawFrame(int frame, Graphics2D g,
			Rectangle2D.Double bounds, Dimension dim,
			boolean transparent) {
		BufferedImage im = backgroundImage;
		if (im == null) {
			Color bg = backgroundColor;
			if (bg != null || !transparent) {
				if (bg == null)
					bg = defaultColor;
				g.setColor(bg);
				g.fillRect(0, 0, dim.width, dim.height);
			}
		}

		if (bounds == null) return;
		g.translate(0.5*(dim.width - bounds.width) - bounds.x,
				0.5*(dim.height - bounds.height) - bounds.y);
		g.scale(scale, scale);

		if (im != null)
			g.drawImage(im, -im.getWidth()/2, -im.getHeight()/2, null);

		// This makes drawing the background image outrageously slow
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		AffineTransform oldTrans = g.getTransform();
		for (Drawable obj : objects) {
			obj.drawFrame(frame, g);
			g.setTransform(oldTrans);
		}
	}

	public void setAnimation(Animation anim) {
		setAnimation(anim, null);
	}

	public void setAnimation(Animation anim, List<Drawable> objects) {
		timer.stop();
		tick = 0;
		this.anim = anim;
		numFrames = 0;
		if (anim == null) {
			this.objects = new Drawable[0];
		}
		else {
			this.objects = (objects == null) ? new Drawable[] { anim }
			: objects.toArray(new Drawable[objects.size()]);
			Arrays.sort(this.objects, drawOrder);
			for (Drawable obj : this.objects)
				if (obj.getEndFrame() > numFrames)
					numFrames = obj.getEndFrame();
			if (frameSlider != null) {
				frameSlider.setMinimum(0);
				frameSlider.setMaximum(numFrames <= 1 ? 1 : numFrames - 1);
				frameSlider.setValue(tick);
			}
			if (!paused)
				timer.start();
		}
		repaint();
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
		if (paused)
			timer.stop();
		else
			timer.start();
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
		Dimension dim = roundSize(bounds);
		GifAnimation out = new GifAnimation(dim.width, dim.height,
				numFrames, DELAY);
		for (int i = 0; i < numFrames; i++) {
			Graphics2D g = out.getFrame(i).createGraphics();
			drawFrame(i, g, bounds, dim, false);
		}
		file.delete();
		out.write(file);
	}

	private void writePng(File file) throws IOException {
		int num = tick;
		Rectangle2D.Double bounds = getAnimBounds(num);
		if (bounds == null) return;
		Dimension dim = roundSize(bounds);
		boolean opaque = backgroundImage == null ? backgroundColor != null
				: backgroundImage.getTransparency() == Transparency.OPAQUE;
		int type = opaque ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage frame = new BufferedImage(dim.width, dim.height, type);
		Graphics2D g = frame.createGraphics();
		drawFrame(num, g, bounds, dim, true);
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
