/*
 * Created on 30.11.2005
 */
package ch0p.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Tom Gelhausen
 */
public class SomePattern extends Pattern {
	protected Pattern resolved;

	public SomePattern(List<Pattern> patterns) {
		List<List<Pattern>> permutations = getPermutations(patterns);
		List<Pattern> sequences = new ArrayList<Pattern>();
		for (List<Pattern> permutation : permutations) {
			SequencePattern sp = new SequencePattern(permutation);
			sequences.add(sp);
		}
		OnePattern xor = new OnePattern(sequences);
		resolved = xor;
	}

	@Override
	protected NFAState appendTo(NFAState n, Collection<NFAState> appendNewStatesTo) {
		return resolved.appendTo(n, appendNewStatesTo);
	}

	protected List<List<Pattern>> getPermutations(List<Pattern> patterns) {
		List<List<Pattern>> result = new ArrayList<List<Pattern>>();
		
		if (patterns.size()==1) {
			List<Pattern> temp = new ArrayList<Pattern>(1);
			temp.add(patterns.get(0));
			result.add(temp);
			return result;
		}
		
		if (patterns.size()==2) {
			List<Pattern> temp = new ArrayList<Pattern>(1);
			temp.add(patterns.get(0));
			result.add(temp);
			temp = new ArrayList<Pattern>(1);
			temp.add(patterns.get(1));
			result.add(temp);
			temp = new ArrayList<Pattern>(2);
			temp.add(patterns.get(0));
			temp.add(patterns.get(1));
			result.add(temp);
			temp = new ArrayList<Pattern>(2);
			temp.add(patterns.get(1));
			temp.add(patterns.get(0));
			result.add(temp);
			return result;
		}
		
		List<Pattern> newList = new ArrayList<Pattern>(patterns.size()-1);
		for (int i=0; i<patterns.size(); i++) {
			newList.clear();
			for (int j=0; j<i; j++) newList.add(patterns.get(j));
			Pattern p = patterns.get(i);
			for (int j=i+1; j<patterns.size(); j++) newList.add(patterns.get(j));
			List<List<Pattern>> temp = getPermutations(newList);
			for (List<Pattern> perm : temp) {
				List<Pattern> resultI = new ArrayList<Pattern>();
				resultI.add(p);
				resultI.addAll(perm);
				result.add(resultI);
				result.add(perm);
			}
		}
		return result;
	}


	@Override
	public String toString() {
		return resolved.toString();
	}

}
