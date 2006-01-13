/*
 * Created on 23.11.2005
 */
package ch0p.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import ch0p.datastrutures.Symbol;

/**
 * @author Tom Gelhausen
 */
class Automaton {
	protected Collection<NFAState> states = new ArrayList<NFAState>();

	protected NFAState start;

	protected boolean inDFAState = false;

	protected Automaton() {
	}

	public static Automaton createFrom(Pattern p) {
		Automaton result = new Automaton();
		result.start = new NFAState();
		NFAState end = p.appendTo(result.start, result.states);
		end.setEndState(true);
		return result;
	}

	public boolean accepts(Iterable<Symbol> symbolsequence) {
		if (!inDFAState)
			throw new IllegalStateException("Not in DFA state!");
		NFAState current = start;
		for (Symbol s : symbolsequence) {
			NFAState next = current.getSuccessor(s);
			if (next == null) {
				throw new IllegalStateException("What was that?????!");
			} else if (next.isErrorState()) {
				return false;
			}
			current = next;
		}
		return current.isEndState();
	}

	public Automaton convertToDFA(List<Symbol> alphabet) {
		Automaton result = new Automaton();
		result.start = result.getOrCreateMacroState(toCollection(this.start));
		MacroState error = new MacroState();
		for (Symbol s : alphabet) {
			error.addTransitionTo(error, s);
		}
		boolean ready = false;
		do {
			List<NFAState> currentStates = new ArrayList<NFAState>(
					result.states.size());
			currentStates.addAll(result.states);
			for (NFAState state : currentStates) {
				MacroState currentMacroState = (MacroState) state;
				Collection<NFAState> from = NFAState
						.getEpsilonClosure(currentMacroState);
				for (Symbol sym : alphabet) {
					Collection<NFAState> reachableStates = getReachableStates(
							from, sym);
					if (reachableStates.size() == 0) {
						// no transition found for symbol sym
						currentMacroState.addTransitionTo(error, sym);
					} else {
						MacroState nextMacroState = result
								.getOrCreateMacroState(reachableStates);
						currentMacroState.addTransitionTo(nextMacroState, sym);
					}
				}
			}
			ready = currentStates.size() == result.states.size();
		} while (!ready);
		// save some memory
		for (NFAState s : result.states) {
			MacroState m = (MacroState) s;
			m.finish();
		}
		System.gc();
		result.inDFAState = true;
		return result;
	}

	/**
	 * @return the epsilon closure of all that is reachable from any of the
	 *         states in the Collection <code>from</code> by the Symbol
	 *         <code>bySymbol</code>.
	 */
	private Collection<NFAState> getReachableStates(Collection<NFAState> from,
			Symbol bySymbol) {
		Collection<NFAState> reachableStates = new HashSet<NFAState>();
		for (NFAState s : from) {
			Collection<NFAState> succ = s.getSuccessors(bySymbol);
			if (succ != null && succ.size() > 0) {
				reachableStates.addAll(succ);
			}
		}
		reachableStates = NFAState.getEpsilonClosure(reachableStates);
		return reachableStates;
	}

	protected MacroState getOrCreateMacroState(Collection<NFAState> representing) {
		for (NFAState s : this.states) {
			if (s instanceof MacroState) {
				MacroState m = (MacroState) s;
				if (m.size() == representing.size()) {
					if (m.containsAll(representing)) {
						return m;
					}
				}
			}
		}
		MacroState m = new MacroState();
		m.addAll(representing);
		this.states.add(m);
		return m;
	}

	private static final Collection<NFAState> toCollection(NFAState s) {
		Collection<NFAState> c = new ArrayList<NFAState>(1);
		c.add(s);
		return c;
	}

	/**
	 * @return Returns the nodes.
	 */
	public Collection<NFAState> getStates() {
		return states;
	}

	/**
	 * @param states
	 *            The nodes to set.
	 */
	public void addState(NFAState s) {
		this.states.add(s);
	}

	public void removeState(NFAState s) {
		this.states.remove(s);
	}

	/**
	 * @return Returns the start.
	 */
	public NFAState getStart() {
		return start;
	}

	/**
	 * @param start
	 *            The start to set.
	 */
	public void setStart(NFAState start) {
		this.start = start;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Start: ");
		if (start != null)
			sb.append(start.getName());
		else
			sb.append("null");
		sb.append("\n");
		sb.append("States:\n");
		for (NFAState s : states) {
			sb.append("  ");
			sb.append(s);
			sb.append("\n");
		}
		return sb.toString();
	}
}
