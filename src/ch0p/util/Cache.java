/*
 * Created on 13.09.2004
 */
package ch0p.util;

import java.util.ArrayList;

/**
 * This class implements a generic object cache that is organized in n rows of m slots. If an associated row
 * is full and another object is to be stored, the oldest entry in that row is discarded.
 * 
 * @author Tom Gelhausen
 * @version $Id$
 */
public class Cache<T>
{
	public static final int DEFAULT_ROWS = 250;
	public static final int DEFAULT_SLOTS = 20;
	
	private static final class CacheItem<T>
	{
		private static long ID_COUNTER = Long.MAX_VALUE;
		long age;
		final long id;
		final T value;
		public CacheItem(long id, T value)
		{
			this.id = id;
			this.value = value;
			age = getID();
		}
		public synchronized static final long getID()
		{
			return ID_COUNTER--;
		}
	}

	private static final class CacheItemSet<T>
	{
		private final int SLOTS;
		private ArrayList<Cache.CacheItem<T>> items;
		public CacheItemSet(int slots)
		{
			SLOTS = slots;
			items = new ArrayList<Cache.CacheItem<T>>(SLOTS);
		}
		public final CacheItem<T> get(long id)
		{
			synchronized(items) {
				for (CacheItem<T> i : items)
				{
					if (i.id==id)
					{
						i.age = CacheItem.getID();
						return i;
					}
				}
			}
			return null;
		}

		/**
		 *  
		 * @param id
		 * @param value
		 * @return Returns true if an entry has been replaced, false if not
		 */
		public final boolean set(long id, T value)
		{
			CacheItem<T> new_ci = new CacheItem<T>(id, value);
			synchronized(items) {
				if (items.size()<SLOTS)
				{
					items.add(new_ci);
					return false;
				}
				assert items.size()>=1; // since items.size >= SLOTS >= 1
				int oldestI = 0;
				CacheItem oldest = items.get(0);
				for (int i=1; i<items.size(); i++)
				{
					CacheItem ci = items.get(i);
					if (ci.age>oldest.age)
					{
						oldestI = i;
						oldest = ci;
					}
				}
				items.set(oldestI, new_ci);
				return true;
			}
		}
	}

	/*
	 * An amount of entries is keept in JVM memory to accelerate access to frequently requested lines. ROWS
	 * determines the number of ROWS.
	 */
	private final int ROWS;
	/*
	 * The cache is organized in ROWS with this number of entries. It'symbol allways the oldest entry of one row
	 * that is replaced.
	 */
	private final int SLOTS;

	private ArrayList<CacheItemSet<T>> cache;
	private long hits = 0;
	private long misses = 0;

	public Cache()
	{
		this(DEFAULT_ROWS, DEFAULT_SLOTS);
	}
	
	public Cache(int rows, int slots)
	{
		if (rows<0) throw new IllegalArgumentException("The number of rows may not be less than 0.");
		if (slots<1) throw new IllegalArgumentException("The number of slot per row may not be less than 1.");
		ROWS = rows;
		SLOTS = slots;
		cache = new ArrayList<Cache.CacheItemSet<T>>(ROWS);
		for (int i=0; i<ROWS; i++)
		{
			cache.add(new CacheItemSet<T>(SLOTS));
		}
	}

	/**
	 * 
	 * @param id
	 * @return Returns null if the item is no longer in the cache.
	 */
	public T get(long id)
	{
		int index = (int) id%ROWS;
		CacheItemSet<T> cis = cache.get(index);
		if (cis.get(id)==null) {
			misses++;
			return null;
		}
		hits++;
		return cis.get(id).value;
	}
	
	/**
	 * 
	 * @param id
	 * @param value
	 * @return Returns true if an item has been replaced, false otherwise.
	 */
	public boolean put(long id, T value)
	{
		int index = (int) id%ROWS;
		CacheItemSet<T> cis = cache.get(index);
		return cis.set(id, value);
	}

	/**
	 * @return Returns the hits.
	 */
	public long getHits() {
		return hits;
	}

	/**
	 * @return Returns the misses.
	 */
	public long getMisses() {
		return misses;
	}
	
}
