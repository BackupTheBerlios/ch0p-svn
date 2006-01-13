/*
 * Created on 30.11.2005
 */
package ch0p.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch0p.datastrutures.Epsilon;
import ch0p.datastrutures.Symbol;
import ch0p.datastrutures.SymbolSequence;

import sun.security.krb5.internal.s;

/**
 * @author Tom Gelhausen
 */
public class Acceptor {
	protected Collection<DFAState> states = new ArrayList<DFAState>();
	protected DFAState start;
	protected Pattern pattern;
	protected long calls = 0;
	protected long chars = 0;

	protected Acceptor() {}

	public static Acceptor createFrom(Pattern p, List<Symbol> alphabet) {
		Automaton a = Automaton.createFrom(p);
		Automaton dfa = a.convertToDFA(alphabet);
		Acceptor result = createFrom(dfa);
		result.pattern = p;
		return result;
	}

	private static Acceptor createFrom(Automaton a) {
		Acceptor result = new Acceptor();
		Map<NFAState, DFAState> map = new HashMap<NFAState, DFAState>();
		for (NFAState s : a.getStates()) {
			DFAState dfas = new DFAState(s.getName());
			result.states.add(dfas);
			dfas.endState = s.isEndState();
			dfas.errorState = s.isErrorState();
			map.put(s, dfas);
		}
		for (NFAState s : a.getStates()) {
			DFAState dfas = map.get(s);
			for (Symbol sym : s.outgoing.keySet()) {
				NFAState succ = s.getSuccessor(sym);
				DFAState dfasucc = map.get(succ);
				dfas.register(sym, dfasucc);
			}
		}
		result.start = map.get(a.start);
		return result;
	}

	public boolean accepts(Iterable<Symbol> it) {
		calls++;
		DFAState current = start;
		for (Symbol sym : it) {
			if (!(sym == Epsilon.instance)) {
				chars++;
				DFAState next = current.getSuccessor(sym);
				if (next.isErrorState()) return false;
				current = next;
			}
		}
		if (current.isEndState()) return true;
		else return false;
	}


	public boolean accepts(SymbolSequence ss, int from, int to) {
		calls++;
		DFAState current = start;
		for (int i=from; i<to; i++) {
			chars++;
			Symbol sym = ss.get(i, false);
			DFAState next = current.getSuccessor(sym);
			if (next.isErrorState()) return false;
			current = next;
		}
		return (current.isEndState());
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Start: ");
		if (start != null) sb.append(start.getName());
		else sb.append("null");
		sb.append("\n");
		sb.append("States:\n");
		for (DFAState s : states) {
			sb.append("  ");
			sb.append(s);
			sb.append("\n");
		}
		return sb.toString();
	}

	public Pattern getPattern() {
		return pattern;
	}
	
	/**
	 * @return the avarage number of symbol comparisons necessary for a decission
	 */
	public double getStatistics() {
		return (((double) chars)/(double) calls);
	}
}
