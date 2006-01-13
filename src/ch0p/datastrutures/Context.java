package ch0p.datastrutures;

import java.util.ArrayList;
import java.util.Collection;

import ch0p.regex.Acceptor;

/**
 * @author Tom Gelhausen
 */
public class Context {
	public class Match implements Comparable<Match> {
		public final Context context = Context.this;
		public final int start;
		public final int end;
		public Match(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public int compareTo(Match that) {
			if (this == that)
				return 0;
			if (this.start < that.start)
				return -1;
			if (this.start > that.start)
				return 1;
			if (this.end < that.end)
				return -1;
			if (this.end > that.end)
				return 1;
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof Match))
				return false;
			Match that = (Match) obj;
			return (this.start == that.start && this.end == that.end && this.context
					.equals(that.context));
		}

		@Override
		public int hashCode() {
			return start ^ end ^ context.hashCode();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(start);
			sb.append("-");
			sb.append(end);
			return sb.toString();
		}

	}
	protected Acceptor acceptor;
	protected Context() {
	}
	
	public Context(Acceptor a) {
		acceptor = a;
	}

	public Collection<Match> getMatches(SymbolSequence s, int start, int end) {
		Collection<Match> result = new ArrayList<Match>();
		for (int front = start; front < end; front++) {
			for (int back = front + 1; back < end; back++) {
				if (matches(s, front, back)) {
					result.add(new Match(front, back));
				}
			}
		}
		return result;
	}

	public boolean matches(SymbolSequence s, int start, int end) {
		return acceptor.accepts(s, start, end);
	}

	@Override
	public String toString() {
		return acceptor.getPattern().toString();
	}
}
