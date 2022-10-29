package com.github.Vaapukkax.kuphack.flagclash.sheets.widgets;

import java.util.Arrays;
import java.util.Iterator;

public abstract class Tree<T extends Widget> {
	
	private final T item;
	private final Tree<T>[] paths;
	
	@SafeVarargs
	public Tree(T item, Tree<T>... paths) {
		this.item = item;
		this.paths = paths;
	}
	
	public int indexOf(T item) {
		int i = 0;
		for (Tree<T> tree : paths) {
			if (tree.getItem() == item) return i;
			i++;
		}
		return -1;
	}
	public boolean contains(T item) {
		for (Tree<T> tree : paths) {
			if (tree.getItem() == item) return true;
		}
		return false;
	}
	
	public Tree<T>[] getPaths() {
		return this.paths;
	}
	
	public int getPathsGoneThrough(Tree<T> root) {
		Tree<T> tree = this;
		int i = 0;
		while ((tree = tree.getParent(root)) != null) i++;
		return i;
	}
	
	protected Tree<T> getParent(Tree<T> tree) {
		if (tree.contains(item)) return tree;
		
		for (Tree<T> path : tree.paths) {
			Tree<T> found = getParent(path);
			if (found != null) return found;
		}
		return null;
	}

	public static <T extends Widget> Tree<T> find(T item, Tree<T> tree) {
		if (tree.getItem() == item) return tree;
		
		for (Tree<T> path : tree.paths) {
			Tree<T> found = find(item, path);
			if (found != null) return found;
		}
		return null;
	}
	
	public T getItem() {
		return item;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<Tree<T>> it = Arrays.asList(paths).iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) sb.append(", ");
		}
		return getItem()+"["+sb+"]";
		
	}

}
