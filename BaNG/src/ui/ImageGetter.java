package ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bn.DisplayAnimation;
import bn.GameFiles;
import bn.Unit;

public class ImageGetter {

	public static class ImageBox extends JComponent implements ListSelectionListener {
		private static final long serialVersionUID = 1L;

		private JList<Unit> list;
		private DisplayAnimation anim;
		private int size;

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
				Rectangle2D bounds = anim.getBounds(0);
				anim.setPosition((size - bounds.getWidth())/2 - bounds.getX(),
						(size - bounds.getHeight())/2 - bounds.getY());
				anim.drawFrame(0, g2);
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent ev) {
			anim = null;			
			Unit unit = list.getSelectedValue();
			if (unit != null) {
				try {
					anim = unit.getBackAnimation();
				}
				catch (IOException ex) {
					JOptionPane.showMessageDialog(null,
							"Unable to load animation",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			repaint();
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
        listScroller.setPreferredSize(new Dimension(300, 300));
		
		ImageBox img = new ImageBox(300, list);
		
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
