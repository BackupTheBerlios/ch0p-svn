package ch0p.datastrutures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch0p.Const;

/**
 * @author Tom Gelhausen
 */
public class Rule implements Comparable<Rule> {
	public class Match implements Comparable<Match> {
		public final Context.Match left;
		public final int leftHandSideStart;
		public final Context.Match right;
		public final Rule rule = Rule.this;

		public Match(Context.Match leftContextMatch, int leftHandSideStart,
				Context.Match rightContextMatch) {
			this.left = leftContextMatch;
			this.leftHandSideStart = leftHandSideStart;
			this.right = rightContextMatch;
		}

		public final int compareTo(Match that) {
			if (this == that)
				return 0;
			if (this.leftHandSideStart < that.leftHandSideStart)
				return -1;
			if (this.leftHandSideStart > that.leftHandSideStart)
				return 1;
			int l = this.left.compareTo(right);
			if (l != 0)
				return l;
			int r = this.right.compareTo(that.right);
			if (r != 0)
				return r;
			return this.rule.compareTo(that.rule);
		}

		@Override
		public final boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof Match))
				return false;
			Match that = (Match) obj;
			return (this.left.equals(that)
					&& (this.leftHandSideStart == that.leftHandSideStart)
					&& this.right.equals(that)
					&& this.rule.equals(that.rule));
		}

		@Override
		public final int hashCode() {
			return this.left.hashCode() ^ this.leftHandSideStart
					^ this.right.hashCode() ^ this.rule.hashCode();
		}

		@Override
		public final String toString() {
			StringBuilder result = new StringBuilder();
			rule.append(result);
			result.append(" @ ");
			result.append(left.toString());
			result.append("/");
			result.append(leftHandSideStart);
			result.append("/");
			result.append(right.toString());
			return result.toString();
		}
	}

	public static class NotApplicableException extends Exception {
		private static final long serialVersionUID = 5904964539870254897L;

		public NotApplicableException() {
			super();
		}

		public NotApplicableException(String message) {
			super(message);
		}
	}
	protected final Context leftContext;
	protected final SymbolSequence leftHandSide;
	protected final Context rightContext;
	protected final SymbolSequence rightHandSide;

	public Rule(Context leftContext, SymbolSequence leftHandSide,
			Context rightContext, SymbolSequence rightHandSide) {
		this.leftContext = leftContext;
		this.leftHandSide = leftHandSide;
		this.rightContext = rightContext;
		this.rightHandSide = rightHandSide;
	}

	public Rule(Context leftContext, SymbolSequence leftHandSide,
			SymbolSequence rightHandSide) {
		this(leftContext, leftHandSide, AnyContext.instance, rightHandSide);
	}

	public Rule(SymbolSequence leftHandSide, Context rightContext,
			SymbolSequence rightHandSide) {
		this(AnyContext.instance, leftHandSide, rightContext, rightHandSide);
	}

	public Rule(SymbolSequence leftHandSide, SymbolSequence rightHandSide) {
		this(AnyContext.instance, leftHandSide, AnyContext.instance,
				rightHandSide);
	}

	public void append(StringBuilder sb) {
		for (Symbol s : leftHandSide) {
			s.append(sb);
			sb.append(" ");
		}
		sb.append(Const.RULE_SEPAPRATOR);
		for (Symbol s : rightHandSide) {
			sb.append(" ");
			s.append(sb);
		}
	}

	public SymbolSequence applyTo(SymbolSequence seq, Match m) throws NotApplicableException {
		if (m.rule!=this) throw new NotApplicableException("The given Match instance does not belong to this rule");
		SymbolSequence result = seq.replace(leftHandSide, m.leftHandSideStart,
				rightHandSide);
		return result;
	}

	public int compareTo(Rule that) {
		int r1 = this.leftHandSide.compareTo(that.leftHandSide);
		if (r1 != 0)
			return r1;
		int r2 = this.rightHandSide.compareTo(that.rightHandSide);
		if (r2 != 0)
			return r2;
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Rule))
			return false;
		Rule that = (Rule) obj;
		return ((this.leftHandSide.equals(that.leftHandSide)) && (this.rightHandSide
				.equals(that.rightHandSide)));
	}

	public SymbolSequence getLeftHandSide() {
		return leftHandSide;
	}

	public Collection<Match> getMatches(SymbolSequence seq) {
		Collection<Match> result = new ArrayList<Match>();
		List<Integer> occurrencesOfLeftHandSide = seq.contains(leftHandSide);
		for (int occurrenceOfLeftHandSide : occurrencesOfLeftHandSide) {
			int leftCStart = 0;
			int leftCEnd = occurrenceOfLeftHandSide;
			int rightCStart = occurrenceOfLeftHandSide
					+ leftHandSide.length(false);
			int rightCEnd = seq.length(false);
			Collection<Context.Match> leftCMatches = leftContext.getMatches(
					seq, leftCStart, leftCEnd);
			Collection<Context.Match> rightCMatches = rightContext.getMatches(
					seq, rightCStart, rightCEnd);
			for (Context.Match leftCMatch : leftCMatches) {
				for (Context.Match rightCMatch : rightCMatches) {
					result.add(new Match(leftCMatch, occurrenceOfLeftHandSide,
							rightCMatch));
				}
			}

		}
		return result;
	}

	public SymbolSequence getRightHandSide() {
		return rightHandSide;
	}

	@Override
	public int hashCode() {
		return this.leftHandSide.hashCode() ^ this.rightHandSide.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		append(sb);
		return sb.toString();
	}

	public Context getLeftContext() {
		return leftContext;
	}

	public Context getRightContext() {
		return rightContext;
	}

}
