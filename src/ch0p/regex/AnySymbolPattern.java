/*
 * Created on 01.12.2005
 */
package ch0p.regex;

import java.util.Collection;

import ch0p.datastrutures.Epsilon;
import ch0p.datastrutures.Symbol;

/**
 * @author Tom Gelhausen
 */
public class AnySymbolPattern extends Pattern {
	protected Collection<Symbol> alphabet;

	public AnySymbolPattern(Collection<Symbol> alphabet) {
		this.alphabet = alphabet;
	}

	@Override
	protected NFAState appendTo(NFAState n, Collection<NFAState> appendNewStatesTo) {
		NFAState newNode = new NFAState();
		if (appendNewStatesTo!=null) {
			appendNewStatesTo.add(newNode);
		}
		for (Symbol sym : alphabet) {
			newNode.addTransitionFrom(n, sym);
			if (isOptional()) {
				newNode.addTransitionFrom(n, Epsilon.instance);
			}
			if (isRepeatable()) {
				newNode.addTransitionTo(newNode, sym);
			}
		}
		return newNode;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("(");
		for (Symbol sym:alphabet) {
			sym.append(result);
			result.append("|");
		}
		if (alphabet.size()>0) {
			result.deleteCharAt(result.length()-1);
		}
		result.append(")");
		result.append(getFlagsSuffix());
		return result.toString();
	}
}
