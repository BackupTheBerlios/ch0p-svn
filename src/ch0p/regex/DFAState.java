/*
 * Created on 30.11.2005
 */
package ch0p.regex;

import java.util.Map;
import java.util.TreeMap;

import ch0p.datastrutures.Symbol;

/**
 * @author Tom Gelhausen
 */
class DFAState {
	protected String name;
	protected Map<Symbol, DFAState> outgoing;
	protected boolean errorState = false;
	protected boolean endState = false;
	
	public DFAState(String name) {
		this.name = name;
		outgoing = new TreeMap<Symbol, DFAState>();
	}
	
	protected void register(Symbol s, DFAState successor) {
		outgoing.put(s, successor);
	}
	
	public DFAState getSuccessor(Symbol s) {
		return outgoing.get(s);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append(" [");
		boolean del = false;
		for (Symbol s : outgoing.keySet()) {
			sb.append("(");
			s.append(sb);
			sb.append(",");
			DFAState state = outgoing.get(s);
			sb.append(state.getName());
			sb.append("), ");
			del = true;
		}
		if (del) sb.delete(sb.length() - 2, sb.length());
		sb.append("]");
		if (isEndState()) sb.append(" endstate");
		if (isErrorState()) sb.append(" errorstate");
		return sb.toString();
	}

	public boolean isEndState() {
		return endState;
	}

	public boolean isErrorState() {
		return errorState;
	}

	public String getName() {
		return name;
	}
}
