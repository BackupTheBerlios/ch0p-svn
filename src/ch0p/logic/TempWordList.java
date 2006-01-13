/*
 * Created on 13.09.2004
 */
package ch0p.logic;

import ch0p.datastrutures.Grammar;
import ch0p.datastrutures.mapping.StringMapper;
import ch0p.util.BigList;


/**
 * @author Tom Gelhausen
 * @version $Id$
 */
public class TempWordList extends WordList
{
	public TempWordList(Grammar g)
	{
		list = new BigList();
		this.g = g;
		sm = StringMapper.getInstance(g);

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		this.list.delete();
		super.finalize();
	}
}
