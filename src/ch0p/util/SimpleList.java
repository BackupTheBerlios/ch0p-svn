/*
 * Created on 14.09.2004
 */
package ch0p.util;

import java.io.IOException;

/**
 * @author Tom Gelhausen
 * @version $Id$
 */
public interface SimpleList
{
	/**
	 * Appends a String to the end of this list. The line number it got is returned. The
	 * first line gets the index 0.
	 * 
	 * @param symbol
	 *            The String to append to the end of this list.
	 * @return Returns the line number this string got.
	 * @throws IOException
	 */
	public abstract long append(String s) throws IOException;

	/**
	 * Obtains the String at the line lineNr. The first line has the index 0.
	 * 
	 * @param lineNr
	 *            The number of the line to return.
	 * @return The line with the specified line number.
	 * @throws IOException
	 */
	public abstract String getLine(long lineNr) throws IOException;

	/**
	 * Returns the number of lines currently in this list.
	 * 
	 * @return the number of lines currently in this list.
	 */
	public abstract long size();

}