package ch0p.datastrutures;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * @author Tom Gelhausen
 */
public class AnyContext extends Context {
	public static final int MATCHVAL = -1;
	public static final AnyContext instance = new AnyContext();
	public static final Match anywhere = instance.new Match(MATCHVAL,MATCHVAL);
	
	private AnyContext() {		
	}

	@Override
	public boolean matches(@SuppressWarnings("unused")
	SymbolSequence s, @SuppressWarnings("unused")
	int start, @SuppressWarnings("unused")
	int end) {
		return true;
	}

	@Override
	public Collection<Match> getMatches(SymbolSequence s, int start, int end) {
		ArrayList<Match> result = new ArrayList<Match>(1);
		result.add(anywhere);
		return result;
	}

}
