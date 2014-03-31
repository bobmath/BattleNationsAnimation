package ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bn.Animation;
import bn.GameFiles;
import bn.Unit;

public class ImageGetter {

	public static class ImageBox extends JComponent implements ListSelectionListener {
		private static final long serialVersionUID = 1L;

		private JList<Unit> list;
		private Animation anim;
		private int size;
		public int tick = 0;
		public Timer timer;

		public ImageBox(int size, JList<Unit> list) {
			this.size = size;
			this.list = list;
			list.addListSelectionListener(this);
		}
		
		public Dimension getPreferredSize() {
			return new Dimension(size, size);
		}
		
		public void paint(Graphics g) {
			if (anim != null) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.translate(0.5*size, 0.5*size);
				Animation.Frame frame = anim.getFrame(0);
				frame.scale(size, g2);
				frame.center(g2);
				anim.drawFrame(tick, g2);

				if (timer != null) {
					timer.stop();
				}
				//Change the number in the line below to modify anim speed
				timer = new Timer(50, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {

						tick += 1;

						repaint(0, 0, 0, size, size);
					}
				});
				timer.start();
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent ev) {
			if (anim != null) {
				anim.freeBitmap();
				if (timer != null) {
					timer.stop();
					tick = 0;
				}
				anim = null;
			}
			
			Unit unit = list.getSelectedValue();
			if (unit != null) {
				try {
					anim = unit.getFrontAnimation();
					if (anim != null)
						anim.loadBitmap();
				}
				catch (IOException ex) { }
			}
			
			repaint(0, 0, 0, size, size);
		}
	}

	public static void main(String[] args)
	{
		try {
			GameFiles.load();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Unable to read Battle Nations game files.",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JList<Unit> list = new JList<Unit>(Unit.getPlayer());
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(400, 300));
		
		ImageBox img = new ImageBox(200, list);
		
        JFrame frame = new JFrame("ImageGetter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container content = frame.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.LINE_AXIS));
        content.add(listScroller);
        content.add(img);
        frame.pack();
        frame.setVisible(true);
	}

}
