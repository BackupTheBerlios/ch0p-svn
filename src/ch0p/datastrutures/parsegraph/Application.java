/*
 * Created on 20.10.2005
 */
package ch0p.datastrutures.parsegraph;

import java.util.List;

import ch0p.datastrutures.Rule;

/**
 * @author Tom Gelhausen
 */
public class Application {
	protected List<Occurrence> parents = null;
	protected List<Occurrence> children = null;
	protected Rule rule;

	/**
	 * @return Returns the children.
	 */
	public final List<Occurrence> getChildren() {
		return children;
	}

	/**
	 * @return Returns the parents.
	 */
	public final List<Occurrence> getParents() {
		return parents;
	}

	/**
	 * @return Returns the rule.
	 */
	public final Rule getRule() {
		return rule;
	}

	/**
	 * @return Returns the isLeaf.
	 */
	public final boolean isLeaf() {
		if (children==null) return true;
		if (children.size()==0) return true;
		return false;
	}

	/**
	 * @return Returns the isRoot.
	 */
	public final boolean isRoot() {
		if (parents==null) return true;
		if (parents.size()==0) return true;
		return false;
	}
}
