/*
 * Created on 09.09.2004
 */
package ch0p.util;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Tom Gelhausen
 * @version $Id$
 */
public class LineSorter
{
	/**
	 * The maximum size of a primitive sorting block in characters (2 bytes!)
	 */
	public static final long MAX_BLOCK_SIZE = 64*1024*1024;
//	public static final long MAX_BLOCK_SIZE = 4*1024;

	/**
	 * This only sorts single blocks, not the whole list!
	 */
	public static void inMemorySort(BigList src, BigList target, long linesPerBlock) throws IOException
	{
		long totalNrOfLines = src.size();
		long pos = 0;
		while (pos<totalNrOfLines)
		{
			SortedSet<String> ss = new TreeSet<String>();
			
			long j = 0;
			while (j<linesPerBlock && pos<totalNrOfLines)
			{
				ss.add(src.getLine(pos));
				pos++;
				j++;
			}
			
			for (String s : ss)
			{
				target.append(s);
			}
		}
	}

	public static void main(String[] args)
	{
		try
		{
			File fin = new File(args[0]);
			File fout = new File(args[1]);
			sort(fin, fout);
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.out.println("java ch0p.util.LineSorter <fileNameIn> <fileNameOut>");
			System.out.println("This program sorts the lines of a text file lexicographically. It does some in memory sorting\r\n" + 
				" first, see MAX_BLOCK_SIZE for the required memory consumption. Afterwards, mergesort\r\n" + 
				" on disk is done, this implementation requires 3x the size of the original file: the original file\r\n" + 
				" is never changed, and mergesort requires two working copies of the data. One of those (the finally\r\n" + 
				" sorted one) will be renamed to the file of the second parameter");
		}
	}
	
	/**
	 * The blocks already need to be sorted!!!
	 */
	public static void onDiskSort(BigList src, BigList target, long linesPerBlock) throws IOException
	{
		long totalNrOfLines = src.size();
		long totalNrOfBlocks = (long)Math.ceil(((double)totalNrOfLines/linesPerBlock));

		BigList tempList1 = src;
		BigList tempList2;

		for (long blocksPerSuperblock=1; blocksPerSuperblock<totalNrOfBlocks; blocksPerSuperblock*=2)
		{
			tempList2 = new BigList();
			long nrOfSuperblocks = (long)Math.ceil(((double)totalNrOfBlocks/blocksPerSuperblock));
			for (long superBlock=0; superBlock<nrOfSuperblocks; superBlock+=2)
			{
				long leftEnd  = getSuperBlockStartIndex(superBlock, linesPerBlock, blocksPerSuperblock);
				long mid	  = getSuperBlockStartIndex(superBlock+1, linesPerBlock, blocksPerSuperblock);
				long rightEnd = getSuperBlockStartIndex(superBlock+2, linesPerBlock, blocksPerSuperblock);
				
				assert (leftEnd<totalNrOfLines)  : "leftEnd:"+leftEnd+" mid:"+mid+" rightEnd:"+rightEnd;

				if (mid<totalNrOfLines)
				{
					// more than one superblock left
					if (rightEnd>totalNrOfLines) rightEnd = totalNrOfLines;

					assert (walkWhileSortedAsc(tempList1, leftEnd, mid) == mid) :
						"blocksPerSuperblock="+blocksPerSuperblock+", superBlock="+superBlock+", leftEnd="+leftEnd+", mid="+mid+", rightEnd="+rightEnd+", pos="+walkWhileSortedAsc(tempList1, leftEnd, rightEnd);
					assert (walkWhileSortedAsc(tempList1, mid, rightEnd) == rightEnd) :
						"blocksPerSuperblock="+blocksPerSuperblock+", superBlock="+superBlock+", leftEnd="+leftEnd+", mid="+mid+", rightEnd="+rightEnd+", pos="+walkWhileSortedAsc(tempList1, leftEnd, rightEnd);

					merge(tempList1,tempList2,leftEnd,mid,rightEnd);

					assert (walkWhileSortedAsc(tempList2, leftEnd, rightEnd) == rightEnd) :
						"blocksPerSuperblock="+blocksPerSuperblock+", superBlock="+superBlock+", leftEnd="+leftEnd+", mid="+mid+", rightEnd="+rightEnd+", pos="+walkWhileSortedAsc(tempList2, min(leftEnd, totalNrOfLines), min(rightEnd, totalNrOfLines));

				}
				else
				{
					// less than one superblock left so just copy rest
					copy(tempList1, tempList2, leftEnd, totalNrOfLines);
				}
				assert (walkWhileSortedAsc(tempList2, min(leftEnd, totalNrOfLines),
					min(rightEnd, totalNrOfLines))
					== min(rightEnd, totalNrOfLines)) :
						"blocksPerSuperblock="+blocksPerSuperblock+", superBlock="+superBlock+", leftEnd="+leftEnd+", mid="+mid+", rightEnd="+rightEnd+", pos="+walkWhileSortedAsc(tempList2, min(leftEnd, totalNrOfLines), min(rightEnd, totalNrOfLines));

			}
			tempList1.delete();
			tempList1 = tempList2;
		}

		assert (walkWhileSortedAsc(tempList1, 0, totalNrOfLines) == totalNrOfLines) :
			"pos="+walkWhileSortedAsc(tempList1, 0, totalNrOfLines);
		
		copy(tempList1, target, 0, tempList1.size());
	}

	/**
	 * This method sorts the lines of a text file lexicographically. It does some in memory sorting first, see
	 * <code>MAX_BLOCK_SIZE</code> for the required memory consumption. Afterwards, mergesort on disk is
	 * done, this implementation requires 3x the size of the original file: the original file is never
	 * changed, and mergesort requires two working copies of the data. One of those (the finally sorted one)
	 * will be renamed to the file of the parameter out.
	 * 
	 * @param in
	 *            Source file containing the lines to sort.
	 * @param out
	 *            Target file for the sorted data.
	 * @throws IOException
	 */
	public static void sort(File in, File out) throws IOException
	{
		File result;
		BigList dlin = new BigList(in);
		BigList dltemp = new BigList();
		BigList dlout = new BigList(out);

		long fileSize = in.length();
		long totalNrOfLines = dlin.size();
		double avgLineLength = ((double)fileSize/(double)totalNrOfLines);
		long linesPerBlock = (long)(MAX_BLOCK_SIZE/avgLineLength);

		// first do a bit in memory sorting
		inMemorySort(dlin, dltemp, linesPerBlock);

		// now do merge sort on disk
		onDiskSort(dltemp, dlout, linesPerBlock);
		dlout.switchToDisk();

		result = dlout.getFile();
		if (!result.renameTo(out))
		{
			Copy.copy(result, out);
		}
	}
	
	public static void sort(BigList in, BigList out) throws IOException
	{
		BigList temp = new BigList();

		long linesPerBlock = 10000;

		// first do a bit in memory sorting
		inMemorySort(in, temp, linesPerBlock);

		// now do merge sort on disk
		onDiskSort(temp, out, linesPerBlock);
	}

	/**
	 * This method wals throug the given DiskList starting from startIndex (incl.) up to the position of
	 * endIndex (excl.) as long as the elements in that interval are sorted ascending.
	 * @param in
	 * @param startIndex
	 * @param endIndex
	 * @return Returns the value of endIndex if the DiskList is correctly sorted in the parameter intervall.
	 * Returns the index of the first out of order element otherwise.
	 * @throws IOException
	 */
	public static long walkWhileSortedAsc(SimpleList in, long startIndex, long endIndex) throws IOException
	{
		if (endIndex<startIndex) throw new IllegalArgumentException("startIndex>endIndex: "
			+startIndex+" > "+endIndex);
		if (startIndex==endIndex) return endIndex;
		String last = in.getLine(startIndex);
		for (long i=startIndex+1; i<endIndex; i++)
		{
			String current = in.getLine(i);
			int result = current.compareTo(last);
			if (result<0) return i;
			last = current;
		}
		return endIndex;
	}
	
	protected static void merge(SimpleList dlin, SimpleList dlout, long leftEnd, long mid, long rightEnd) throws IOException
	{
		long posL = leftEnd;
		long endL = mid;
		long posR = mid;
		long endR = rightEnd;
		while (posL<endL && posR<endR)
		{
			String sL = dlin.getLine(posL);
			String sR = dlin.getLine(posR);
			int result = sL.compareTo(sR);
			if (result<0)
			{
				dlout.append(sL);
				posL++;
			} else if (result>0)
			{
				dlout.append(sR);
				posR++;
			} else
			{
				dlout.append(sL);
				posL++;
				dlout.append(sR);
				posR++;
			}
		}
		// copy rest if any, only one of both actually does sth.
		copy(dlin, dlout, posL, endL);
		copy(dlin, dlout, posR, endR);
	}
	
	private static final void copy(SimpleList dlin, SimpleList dlout, long pos, long end) throws IOException
	{
		if (pos>dlin.size()) throw new IllegalArgumentException("pos>dlin.size(): "
			+pos+" > "+dlin.size());
		if (end>dlin.size()) throw new IllegalArgumentException("end>dlin.size(): "
			+end+" > "+dlin.size());
		if (pos>end) throw new IllegalArgumentException("pos>end: "
			+pos+" > "+end);
		while (pos<end)
		{
			String s = dlin.getLine(pos);
			dlout.append(s);
			pos++;
		}
	}

	private static final long getSuperBlockStartIndex(long superBlockNumber, long blockSize, long blocksPerSuperblock)
	{
		return ((superBlockNumber*blockSize*blocksPerSuperblock));
	}
	
	private static final long min(long l1, long l2)
	{
		if (l1<l2) return l1;
		return l2;
	}
}