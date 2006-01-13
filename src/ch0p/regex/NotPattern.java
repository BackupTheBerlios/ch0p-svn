package ch0p.regex;

import java.util.ArrayList;
import java.util.Collection;

import ch0p.datastrutures.Epsilon;
import ch0p.datastrutures.Symbol;

/**
 * @author Tom Gelhausen
 */
public class NotPattern extends Pattern {

	protected Pattern pattern;

	public NotPattern(Pattern p) {
		pattern = p;
	}

	@Override
	protected NFAState appendTo(NFAState start,
			Collection<NFAState> appendNewStatesTo) {
		Collection<NFAState> subPatternStates = new ArrayList<NFAState>();
		NFAState errorstate = pattern.appendTo(start, subPatternStates);
		NFAState continueState = new NFAState();
		for (NFAState s : subPatternStates) {
			if (s != errorstate) {
				s.addTransitionTo(continueState, Epsilon.instance);
			}
		}

		if (appendNewStatesTo != null) {
			appendNewStatesTo.addAll(subPatternStates);
			subPatternStates.add(continueState);
		}

		return continueState;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("^(");
		result.append(pattern.toString());
		result.append("(");
		result.append(getFlagsSuffix());
		return result.toString();
	}
}
