/*
 * Created on 09.09.2004
 */
package ch0p.datastrutures;


/**
 * UNUSED. MAY BE DELETED.
 * 
 * @author Tom Gelhausen
 * @version $Id$
 */
public final class Application implements Comparable<Application>
{
	private final Rule	rule;
	private final int	pos;

	public Application(Rule rule, int position)
	{
		this.rule = rule;
		this.pos = position;
	}

	public final int getPosition()
	{
		return pos;
	}

	public final Rule getRule()
	{
		return rule;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		rule.append(result);
		result.append(" @ ");
		result.append(pos);
		return result.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj==this) return true;
		if (!(obj instanceof Application)) return false;
		Application that = (Application) obj;
		return ((this.pos==that.pos)&&(this.rule.equals(that.rule)));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.pos ^ this.rule.hashCode();
	}

	public int compareTo(Application that) {
		if (this==that) return 0;
		if (this.pos<that.pos) return -1;
		if (this.pos>that.pos) return 1;
		return this.rule.compareTo(that.rule);
	}
	
}
