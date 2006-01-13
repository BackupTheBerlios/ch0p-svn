/*
 * Created on 23.11.2005
 */
package ch0p.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ch0p.datastrutures.Epsilon;
import ch0p.datastrutures.Symbol;

/**
 * @author Tom Gelhausen
 */
class NFAState {
	private static int IDcounter = 0;

	protected String name;

	protected Map<Symbol, Collection<NFAState>> outgoing;

	protected Map<Symbol, Collection<NFAState>> incomming;

	protected boolean endState = false;

	public NFAState() {
		this("S" + IDcounter++);
	}

	public NFAState(String name) {
		this.name = name;
		outgoing = new TreeMap<Symbol, Collection<NFAState>>();
		incomming = new TreeMap<Symbol, Collection<NFAState>>();
	}

	/**
	 * This method returns the collection of states that is directly reachable
	 * from this state by the symbol reachedBy. The Collection is a the internal
	 * data structure and may thus NOT be manipulated. If no transition has been
	 * registered with the parameter symbol null is returned.
	 * 
	 * @param reachedBy
	 * @return
	 */
	public Collection<NFAState> getSuccessors(Symbol reachedBy) {
		return outgoing.get(reachedBy);
	}

	/**
	 * Returns one arbitrary State which is reachable from this state by
	 * reachedBy. Especially interesting for states of a DFA. If no transition
	 * is registered with reachedBy, null is returned.
	 * 
	 * @param reachedBy
	 * @return
	 */
	public NFAState getSuccessor(Symbol reachedBy) {
		Collection<NFAState> temp = outgoing.get(reachedBy);
		if (temp == null || temp.size() == 0) {
			return null;
		}
		return temp.iterator().next();
	}

	/**
	 * This method returns the collection of states from which this state is is
	 * directly reachable by the symbol reachedBy. The Collection is a the
	 * internal data structure and may thus NOT be manipulated. If no transition
	 * has been registered with the parameter symbol null is returned.
	 * 
	 * @param reachedBy
	 * @return
	 */
	public Collection<NFAState> getPredecessors(Symbol reachedBy) {
		return incomming.get(reachedBy);
	}

	/**
	 * This mehtod returns all states that are reacheble from this state by an
	 * arbitrary sequence of epsilon transitions.
	 * 
	 * @return
	 */
	public Collection<NFAState> getEpsilonClosure() {
		Set<NFAState> result = new HashSet<NFAState>();
		result.add(this);
		// add epsilon-closure
		Set<NFAState> current = new HashSet<NFAState>();
		while (current.size() < result.size()) {
			current.clear();
			for (NFAState s : result) {
				current.add(s);
				Collection<NFAState> succ = s.outgoing.get(Epsilon.instance);
				if (succ != null && succ.size() > 0) {
					current.addAll(succ);
				}
			}
			Set<NFAState> old = result;
			result = current;
			current = old;
		}
		return result;
	}

	/**
	 * This method returns the states that are reachable from at least one of
	 * the given states by an arbitrary sequence of epsilon transitions.
	 * 
	 * @param states
	 * @return
	 */
	public static Collection<NFAState> getEpsilonClosure(
			Collection<NFAState> states) {
		Set<NFAState> temp = new HashSet<NFAState>();
		temp.addAll(states);
		for (NFAState s : states) {
			Collection<NFAState> closure = s.getEpsilonClosure();
			temp.addAll(closure);
		}
		Collection<NFAState> result = new ArrayList<NFAState>(temp.size());
		result.addAll(temp);
		return result;
	}

	public void addTransitionFrom(NFAState from, Symbol by) {
		this.addIncomming(by, from);
		from.addOutgoing(by, this);
	}

	public final void addTransitionTo(NFAState to, Symbol by) {
		this.addOutgoing(by, to);
		to.addIncomming(by, this);
	}

	private void addIncomming(Symbol by, NFAState from) {
		Collection<NFAState> c = incomming.get(by);
		if (c == null) {
			c = new ArrayList<NFAState>();
			incomming.put(by, c);
			c.add(from);
		} else {
			if (!c.contains(from))
				c.add(from);
		}
	}

	private void addOutgoing(Symbol by, NFAState to) {
		Collection<NFAState> c = outgoing.get(by);
		if (c == null) {
			c = new ArrayList<NFAState>();
			outgoing.put(by, c);
			c.add(to);
		} else {
			if (!c.contains(to))
				c.add(to);
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEndState() {
		return endState;
	}

	/**
	 * @return <code>true</code> iff. this is not an end state and no end
	 *         state is reachable from here. <code>false</code> otherwise.
	 */
	public boolean isErrorState() {
		if (this.isEndState())
			return false;

		Collection<NFAState> nextSet = new ArrayList<NFAState>(1);
		nextSet.add(this);
		Collection<NFAState> currentSet;

		do {
			currentSet = nextSet;
			for (NFAState s : currentSet) {
				if (s.isEndState())
					return false;
			}
			nextSet = getReachableStates(currentSet);
			// if the size did not increase, no new States have been added
			// -> nothing else is reachable, we can abort the search
		} while (currentSet.size() < nextSet.size());
		return true;
	}

	/**
	 * @return a Collection that contains at least the NFAState references from
	 *         the parameter Collection plus the states that are reachable from
	 *         any of these states
	 */
	private static Collection<NFAState> getReachableStates(
			Collection<NFAState> states) {
		Collection<NFAState> result = new HashSet<NFAState>();
		result.addAll(states);
		for (NFAState state : states) {
			for (Collection<NFAState> c : state.outgoing.values()) {
				result.addAll(c);
			}
		}
		return result;
	}

	public void setEndState(boolean endState) {
		this.endState = endState;
	}

	// public void setErrorState(boolean errorState) {
	// this.errorState = errorState;
	// }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append(" [");
		boolean del1 = false;
		for (Symbol s : outgoing.keySet()) {
			sb.append("(");
			s.append(sb);
			sb.append(",{");
			Collection<NFAState> states = outgoing.get(s);
			if (states != null) {
				boolean del2 = false;
				for (NFAState state : states) {
					sb.append(state.getName());
					sb.append(",");
					del2 = true;
				}
				if (del2)
					sb.delete(sb.length() - 1, sb.length());
			}
			sb.append("}), ");
			del1 = true;
		}
		if (del1)
			sb.delete(sb.length() - 2, sb.length());
		sb.append("]");
		if (isEndState())
			sb.append(" endstate");
		if (isErrorState())
			sb.append(" errorstate");
		return sb.toString();
	}
}
