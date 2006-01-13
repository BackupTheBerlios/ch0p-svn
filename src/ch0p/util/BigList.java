/*
 * Created on 14.09.2004
 */
package ch0p.util;

import java.io.File;
import java.io.IOException;

/**
 * @author Tom Gelhausen
 * @version $Id$
 */
public class BigList implements SimpleList, RunlevelDestructor.CleanUp
{
	public static final int RUNLEVEL = 2345;
	
	public static final int DEFAULT_WATERMARK = 10000;

	protected File backupFile;
	protected SimpleList currentImpl;
	protected MemoryList memList;
	protected DiskList diskList;

	protected boolean keepFile;
	protected boolean changed = false;
	protected int watermark;
	
	public BigList()
	{
		this(DEFAULT_WATERMARK);
	}

	public BigList(File datafile) throws IOException
	{
		this(datafile, DEFAULT_WATERMARK);
	}
	
	public BigList(File datafile, int watermark) throws IOException
	{
		System.out.println("Creating new file based BigList: "+datafile);
		keepFile = true;
		this.watermark = watermark;
		backupFile = datafile;
		DiskList temp = new DiskList(datafile);
		if (temp.size()<watermark)
		{
			memList = new MemoryList(watermark);
			copyTo(memList, temp, 0, temp.size());
			currentImpl = memList;
		}
		else
		{
			diskList = temp;
			currentImpl = temp;
		}
		RunlevelDestructor.add(this, RUNLEVEL);
	}
	
	public BigList(int watermark)
	{
		keepFile = false;
		this.watermark = watermark;
		memList = new MemoryList(watermark);
		currentImpl = memList;
	}

	/* (non-Javadoc)
	 * @see de.fzi.swt.sprachbaukasten.util.SimpleList#append(java.lang.String)
	 */
	public synchronized long append(String s) throws IOException
	{
		changed = true;
		if ((currentImpl==memList)&&(memList.size()>=watermark))
		{
			// copy everything from memory to disk
			switchToDisk();
		}
		return currentImpl.append(s);
	}
	

	/* (non-Javadoc)
	 * @see de.fzi.swt.sprachbaukasten.util.SimpleList#discard()
	 */
	public void delete() throws IOException
	{
		if (diskList!=null) diskList.delete();
	}

	/* (non-Javadoc)
	 * @see de.fzi.swt.sprachbaukasten.util.SimpleList#getFile()
	 */
	public File getFile()
	{
		return backupFile;
	}

	/* (non-Javadoc)
	 * @see de.fzi.swt.sprachbaukasten.util.SimpleList#getLine(long)
	 */
	public String getLine(long lineNr) throws IOException
	{
		return currentImpl.getLine(lineNr);
	}

	/* (non-Javadoc)
	 * @see de.fzi.swt.sprachbaukasten.util.SimpleList#size()
	 */
	public long size()
	{
		return currentImpl.size();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();
	}
	
	public synchronized void switchToDisk() throws IOException
	{
		if (currentImpl==diskList) return;
		if (!changed) return;
		if (backupFile==null)
		{
			diskList = new DiskList();
			backupFile = diskList.getBackingFile();
		}
		else
		{
			diskList = new DiskList(backupFile);
		}
		System.out.print("Switching to disk ("+backupFile+")... ");
		// only write parts that are not already in list
		copyTo(diskList, memList, diskList.size(), memList.size());
		memList = null;
		System.out.println("done.");
		currentImpl = diskList;
	}

	private static final void copyTo(SimpleList target, SimpleList src, long start, long end) throws IOException
	{
		if (start>src.size())
		{
			throw new IllegalArgumentException("Invalid call of copyTo: start=" +start+", end="+end);
		}
		if (end>src.size()) end = src.size();
		for (long i=start; i<end; i++)
		{
			String s = src.getLine(i);
			target.append(s);
		}
	}

	public void close() throws IOException
	{
		if (keepFile && changed && (currentImpl==memList))
		{
			// copy everything from memory to disk
			switchToDisk();
		}
	}

	/* (non-Javadoc)
	 * @see de.fzi.swt.sprachbaukasten.util.RunlevelDestructor.CleanUp#cleanUp()
	 */
	public void cleanUp() throws Throwable
	{
		close();
	}

}
