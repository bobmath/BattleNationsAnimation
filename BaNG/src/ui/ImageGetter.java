package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import bn.Animation;
import bn.Building;
import bn.GameFiles;
import bn.Unit;

public class ImageGetter {

	public static void main(String[] args) {
		if (!GameFiles.init()) {
			JOptionPane.showMessageDialog(null,
					"Unable to find Battle Nations directory.",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			GameFiles.load();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Unable to read Battle Nations game files.",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		ImageGetter ui = new ImageGetter();
		ui.buildUI();
		ui.showUI();
	}

	public ImageGetter() {
	}

	private Object source;

	private JFrame frame;
	private AnimationBox animBox;
	private JTree tree;
	private JPanel rightPanel;

	private JComboBox<String> frontCtrl;

	private JComboBox<String> busyCtrl;

	public void buildUI() {
		frame = new JFrame("ImageGetter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		tree = new JTree(AnimationTree.buildTree());
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				updateSource();
			}
		});
		JScrollPane scroller = new JScrollPane(tree);
		scroller.setPreferredSize(new Dimension(300, 400));

		animBox = new AnimationBox();
		JPanel controlPanel = buildControls();

		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setPreferredSize(new Dimension(300, 400));
		rightPanel.add(animBox, BorderLayout.CENTER);
		rightPanel.add(controlPanel, BorderLayout.PAGE_END);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				scroller, rightPanel);
		splitPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		frame.setContentPane(splitPane);

		frame.pack();
	}

	private JPanel buildControls() {
		JPanel controlPanel = new JPanel(new FlowLayout());

		JButton exportGif = new JButton("Export Animation");
		exportGif.addActionListener(new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent e) {
				animBox.saveCurrentAsGif();
			}
		});
		controlPanel.add(exportGif);

		JButton exportPng = new JButton("Export Image");
		exportPng.addActionListener(new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent e) {
				animBox.saveCurrentAsPng();
			}
		});
		controlPanel.add(exportPng);

		ActionListener update = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateAnimation();
			}
		};

		frontCtrl = new JComboBox<String>(new String[] { "Front", "Back" });
		frontCtrl.addActionListener(update);
		frontCtrl.setVisible(false);
		controlPanel.add(frontCtrl);

		busyCtrl = new JComboBox<String>(new String[] { "Busy", "Idle" });
		busyCtrl.addActionListener(update);
		busyCtrl.setVisible(false);
		controlPanel.add(busyCtrl);

		return controlPanel;
	}

	private void updateSource() {
		AnimationTree.TreeNode node = (AnimationTree.TreeNode)
				tree.getLastSelectedPathComponent();
		Object src = node.getValue();
		if (src == source) return;
		source = src;
		updateControls();
		updateAnimation();
	}

	private void updateControls() {
		if (source instanceof Unit) {
			showUnitControls(true);
			showBuildingControls(false);
			switch (((Unit) source).getSide()) {
			case "Player": case "Hero":
				frontCtrl.setSelectedItem("Back");
				break;
			case "Hostile": case "Villain":
				frontCtrl.setSelectedItem("Front");
				break;
			}
		}
		else if (source instanceof Building) {
			showUnitControls(false);
			showBuildingControls(true);
		}
		else {
			showUnitControls(false);
			showBuildingControls(false);
		}
		rightPanel.revalidate();
	}

	private void showUnitControls(boolean visible) {
		frontCtrl.setVisible(visible);
	}

	private void showBuildingControls(boolean visible) {
		busyCtrl.setVisible(visible);
	}

	private void updateAnimation() {
		Animation anim = null;
		try {
			if (source instanceof Unit) {
				Unit unit = (Unit) source;
				boolean front = "Front".equals(frontCtrl.getSelectedItem());
				anim = front ? unit.getFrontAnimation()
						: unit.getBackAnimation();
			}
			else if (source instanceof Building) {
				Building bld = (Building) source;
				boolean busy = "Busy".equals(busyCtrl.getSelectedItem());
				if (busy)
					anim = bld.getBusyAnimation();
				if (anim == null)
					anim = bld.getIdleAnimation();
			}
			else if (source instanceof String) {
				anim = Animation.get((String) source);
			}
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Unable to load animation",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		animBox.setAnimation(anim);
	}

	public void showUI() {
		frame.setVisible(true);
	}

}
