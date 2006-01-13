/*
 * Created on 23.11.2005
 */
package ch0p.regex;

import java.util.Collection;

import ch0p.datastrutures.Epsilon;
import ch0p.datastrutures.Symbol;

/**
 * @author Tom Gelhausen
 */
public class SingleSymbolPattern extends Pattern {
	protected Symbol symbol;
	
	public SingleSymbolPattern(Symbol sym) {
		symbol = sym;
	}

	/**
	 * @return Returns the symbol.
	 */
	public final Symbol getSymbol() {
		return symbol;
	}

	@Override
	protected NFAState appendTo(NFAState n, Collection<NFAState> appendNewStatesTo) {
		NFAState newNode = new NFAState();
		if (appendNewStatesTo!=null) {
			appendNewStatesTo.add(newNode);
		}
		newNode.addTransitionFrom(n, symbol);
		if (isOptional()) {
			newNode.addTransitionFrom(n, Epsilon.instance);
		}
		if (isRepeatable()) {
			newNode.addTransitionTo(newNode, symbol);
		}
		return newNode;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		symbol.append(result);
		result.append(getFlagsSuffix());
		return result.toString();
	}
}
