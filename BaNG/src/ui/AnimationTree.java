package ui;

import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import bn.Building;
import bn.GameFiles;
import bn.Timeline;
import bn.Unit;

public class AnimationTree {
	public static void main(String[] args) throws IOException {
		GameFiles.load();

		JFrame frame = new JFrame("Tree Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = frame.getContentPane();
		JTree tree = new JTree(buildTree());
		JScrollPane scroller = new JScrollPane(tree);
		scroller.setPreferredSize(new Dimension(300, 300));
		content.add(scroller);
		frame.pack();
		frame.setVisible(true);
	}

	public static TreeNode buildTree() {
		TreeBuilder builder = new TreeBuilder("Animations");
		for (Building bld : Building.getAll()) {
			String name = bld.getName();
			if (name.startsWith("comp_"))
				builder.add(bld, false, "Building", "comp", name);
			else if (name.startsWith("deco_"))
				builder.add(bld, false, "Building", "deco", name);
			else if (name.startsWith("map_"))
				builder.add(bld, false, "Building", "map", name);
			else if (name.startsWith("terrain_"))
				builder.add(bld, false, "Building", "terrain", name);
			else
				builder.add(bld, true, "Building", name, bld.getTag());
		}
		for (Unit unit : Unit.getAll()) {
			builder.add(unit, true, unit.getSide(), unit.getName(), unit.getTag());
		}
		for (String name : Timeline.getAllNames()) {
			builder.add(name, false, "All", name.substring(0,1), name);
		}
		return builder.getTree();
	}

	protected static class TreeBuilder implements Comparable<TreeBuilder> {
		private String name;
		private Object value;
		private boolean collapsible;
		private Map<String,TreeBuilder> subtrees;
		protected TreeBuilder(String name) {
			this.name = name;
			subtrees = new HashMap<String,TreeBuilder>();
		}
		protected void add(Object value, boolean collapsible, String... names) {
			String name = names[0];
			TreeBuilder subtree = subtrees.get(name);
			if (subtree == null) {
				subtree = new TreeBuilder(name);
				subtrees.put(name, subtree);
			}
			if (names.length == 1) {
				subtree.value = value;
				subtree.collapsible = collapsible;
			}
			else {
				String[] tail = new String[names.length-1];
				for (int i = 1; i < names.length; i++)
					tail[i-1] = names[i];
				subtree.add(value, collapsible, tail);
			}
		}
		protected TreeNode getTree() {
			if (value == null && subtrees.size() == 1) {
				TreeBuilder sub = subtrees.values().iterator().next();
				if (sub.collapsible)
					return new TreeNode(sub.value, name);
			}
			TreeNode node = new TreeNode(value, name);
			TreeBuilder[] subs = new TreeBuilder[subtrees.size()];
			subs = subtrees.values().toArray(subs);
			Arrays.sort(subs);
			for (TreeBuilder sub : subs) {
				node.add(sub.getTree());
			}
			return node;
		}
		@Override
		public int compareTo(TreeBuilder that) {
			return this.name.compareTo(that.name);
		}
	}

	public static class TreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 1L;
		private Object value;
		public TreeNode(Object value, String name) {
			super(name);
			this.value = value;
		}
		public Object getValue() {
			return value;
		}
	}
}
