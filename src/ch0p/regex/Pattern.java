/*
 * Created on 23.11.2005
 */
package ch0p.regex;

import java.util.Collection;

/**
 * @author Tom Gelhausen
 */
public abstract class Pattern {
	protected boolean optional = false;

	protected boolean repeatable = false;

	protected Pattern() {
	}

	/**
	 * @return Returns the optional.
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * @param optional
	 *            The optional to set.
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	/**
	 * @return Returns the repeatable.
	 */
	public boolean isRepeatable() {
		return repeatable;
	}

	/**
	 * @param repeatable
	 *            The repeatable to set.
	 */
	public void setRepeatable(boolean repeatable) {
		this.repeatable = repeatable;
	}

	protected String getFlagsSuffix() {
		if (optional) {
			if (repeatable) {
				return "*";
			} else {
				return "?";
			}
		} else {
			if (repeatable) {
				return "+";
			} else {
				return "";
			}
		}
	}

	/**
	 * This function appends this pattern to a NFA at the state node
	 * <code>start</code>. The resulting State node is to be regarded as the
	 * acceptance state up to then. (Thus further Patterns are to be appended to
	 * this node.)<br>
	 * 
	 * The subgraph appended to this node is nither required to be
	 * deterministic, nor required to be epsilon-free, nor required to be
	 * complete. Missing transitions are to be interpreted as transitions into
	 * error states. (Thus state nodes without outgoing transitions are to be
	 * regarded as error states, as long as they are not identical with the
	 * accepting state node returned.)<br>
	 * 
	 * All state nodes created by this method will inserted into the collection
	 * <code>appendNewStatesTo</code> if it is not <code>null</code>.
	 * 
	 * @param start
	 *            the node to wich the NFA acception this pattern is appended to
	 * @param appendNewStatesTo
	 *            the state nodes created by this method call will be inserted
	 *            here if the parameter is not <code>null</code>
	 * @return the acceptance state node
	 */
	protected abstract NFAState appendTo(NFAState start,
			Collection<NFAState> appendNewStatesTo);

}
