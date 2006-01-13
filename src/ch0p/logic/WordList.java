/*
 * Created on 09.09.2004
 */
package ch0p.logic;

import java.io.File;
import java.io.IOException;

import ch0p.datastrutures.Grammar;
import ch0p.datastrutures.SymbolSequence;
import ch0p.datastrutures.mapping.StringMapper;
import ch0p.util.BigList;
import ch0p.util.LineSorter;

/**
 * @author Tom Gelhausen
 * @version $Id$
 */
public class WordList
{
	protected BigList list;
	protected Grammar g;
	protected StringMapper sm;
	
	protected WordList()
	{}
	
	public WordList(File datafile, Grammar g) throws IOException
	{
		list = new BigList(datafile);
		this.g = g;
		sm = StringMapper.getInstance(g);
	}
	
	public void add(SymbolSequence word) throws IOException
	{
		String s = sm.toString(word);
		list.append(s);
	}
	
	public SymbolSequence get(long id) throws IOException
	{
		String s = list.getLine(id);
		SymbolSequence word = sm.toSymbolSequence(s);
		return word;
	}

	public long size()
	{
		return list.size();
	}

	public void sortTo(BigList target) throws IOException
	{
		LineSorter.sort(list, target);
	}
}
