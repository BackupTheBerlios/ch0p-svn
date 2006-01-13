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
public class OnePattern extends Pattern{
	protected List<Pattern> patterns;
	
	public OnePattern(List<Pattern> patterns) {
		this.patterns = patterns;
	}

	/**
	 * @return Returns the p1.
	 */
	public final List<Pattern> getPatterns() {
		return patterns;
	}

	@Override
	protected NFAState appendTo(NFAState n, Collection<NFAState> appendNewStatesTo) {
		NFAState newState = new NFAState();
		if (appendNewStatesTo!=null) {
			appendNewStatesTo.add(newState);
		}
		for (Pattern p:patterns) {
			NFAState temp = p.appendTo(n, appendNewStatesTo);
			temp.addTransitionTo(newState, Epsilon.instance);
		}
		if (isOptional()) {
			newState.addTransitionFrom(n, Epsilon.instance);
		}
		if (isRepeatable()) {
			for (Pattern p : patterns) {
				NFAState temp = p.appendTo(newState, appendNewStatesTo);
				temp.addTransitionTo(newState, Epsilon.instance);
			}
		}
		return newState;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("(");
		for (int i=0; i<patterns.size()-1; i++) {
			result.append(patterns.get(i).toString());
			result.append("|");
		}
		result.append(patterns.get(patterns.size()-1).toString());
		result.append(")");
		result.append(getFlagsSuffix());
		return result.toString();
	}
	
}
