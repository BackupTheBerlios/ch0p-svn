/*
 * Created on 14.09.2004
 */
package ch0p.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Gelhausen
 * @version $Id$
 */
class MemoryList implements SimpleList
{
	protected List<String> memList;

	public MemoryList()
	{
		memList = new ArrayList<String>();
	}
	
	public MemoryList(int size)
	{
		memList = new ArrayList<String>(size);
	}
	
	/* (non-Javadoc)
	 * @see de.fzi.swt.sprachbaukasten.util.SimpleList#append(java.lang.String)
	 */
	public long append(String s) throws IOException
	{
		memList.add(s);
		return memList.size()-1;
	}

	/* (non-Javadoc)
	 * @see de.fzi.swt.sprachbaukasten.util.SimpleList#getLine(long)
	 */
	public String getLine(long lineNr) throws IOException
	{
		if (lineNr>Integer.MAX_VALUE) throw new IllegalArgumentException("MemoryList can only handle "+Integer.MAX_VALUE+" entries.");
		return memList.get((int)lineNr);
	}

	/* (non-Javadoc)
	 * @see de.fzi.swt.sprachbaukasten.util.SimpleList#size()
	 */
	public long size()
	{
		return memList.size();
	}
}
