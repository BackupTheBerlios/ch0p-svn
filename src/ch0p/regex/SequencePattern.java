/*
 * Created on 23.11.2005
 */
package ch0p.regex;

import java.util.Collection;
import java.util.List;

import ch0p.datastrutures.Epsilon;

/**
 * @author Tom Gelhausen
 */
public class SequencePattern extends Pattern {
	protected List<Pattern> patterns;

	public SequencePattern(List<Pattern> patterns) {
		this.patterns = patterns;
	}

	@Override
	protected NFAState appendTo(NFAState n, Collection<NFAState> appendNewStatesTo) {
		NFAState newState = n;
		for (Pattern p : patterns) {
			newState = p.appendTo(newState, appendNewStatesTo);
		}
		if (isOptional()) {
			newState.addTransitionFrom(n, Epsilon.instance);
		}
		if (isRepeatable()) {
			NFAState temp = newState;
			for (Pattern p : patterns) {
				temp = p.appendTo(temp, appendNewStatesTo);
			}
			temp.addTransitionTo(newState, Epsilon.instance);
		}
		return newState;
	}

	/**
	 * @return Returns the patterns.
	 */
	public final List<Pattern> getPatterns() {
		return patterns;
	}
	

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("(");
		for (int i=0; i<patterns.size()-1; i++) {
			result.append(patterns.get(i).toString());
			//result.append(" ");
		}
		result.append(patterns.get(patterns.size()-1).toString());
		result.append(")");
		result.append(getFlagsSuffix());
		return result.toString();
	}

}
