package ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import bn.Building;
import bn.Timeline;
import bn.Unit;

public class AnimationTree {

	public static TreeNode buildTree() {
		TreeBuilder builder = new TreeBuilder("Animations");
		for (Building bld : Building.getAll()) {
			if (!bld.hasAnimation()) continue;
			String name = bld.getName();
			if (name.startsWith("comp_"))
				builder.add(bld, false, "Buildings", "comp", name);
			else if (name.startsWith("deco_"))
				builder.add(bld, false, "Buildings", "deco", name);
			else if (name.startsWith("map_"))
				builder.add(bld, false, "Buildings", "map", name);
			else if (name.startsWith("terrain_"))
				builder.add(bld, false, "Buildings", "terrain", name);
			else {
				String menu = bld.getBuildMenu();
				if (menu == null) menu = "Other";
				builder.add(bld, true, "Buildings", menu, name, bld.getTag());
			}
		}
		for (Unit unit : Unit.getAll()) {
			builder.add(unit, true, unit.getSide() + " Units",
					unit.getName(), unit.getTag());
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
