/*
 * Created on 14.09.2004
 */
package ch0p.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * This class implements a static runlevel destructor. It ensures thar a certain method
 * (<code>cleanUp()</code>) is called of registered objects before the VM shuts down. All
 * <code>cleanUp()</code> methods of any certain runlevel are garanteed to be called before those of a
 * runlevel with a higher number.
 * 
 * @author Tom Gelhausen
 * @version $Id$
 */
public class RunlevelDestructor
{
	public static interface CleanUp
	{
		public void cleanUp() throws Throwable;
	}
	
	public static final int RUNLEVEL_DEFAULT = 100;

	private static final Map<Integer,Collection<WeakReference<CleanUp>>> runLevels = new HashMap<Integer,Collection<WeakReference<CleanUp>>>();

	{
		Thread cleanupHook = new Thread() {
			@Override
			public void run() {
				TreeSet<Integer> order = new TreeSet<Integer>(); 
				order.addAll(runLevels.keySet());
				for (Integer current : order)
				{
					Collection<WeakReference<CleanUp>> currentRunLevel = runLevels.get(current);
					for (WeakReference<CleanUp> r : currentRunLevel)
					{
						try
						{
							CleanUp c = r.get();
							if (c!=null)
							{
								c.cleanUp();
							}
						}
						catch (Throwable t) {}
					}
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(cleanupHook);
	}

	public static void add(CleanUp c)
	{
		add(c, RUNLEVEL_DEFAULT);
	}

	public static void add(CleanUp c, int runlevel)
	{
		Integer Irunlevel = new Integer(runlevel);
		Collection<WeakReference<CleanUp>> runLevel = runLevels.get(Irunlevel);
		if (runLevel==null)
		{
			runLevel = new ArrayList<WeakReference<CleanUp>>();
			runLevels.put(Irunlevel, runLevel);
		}
		WeakReference<CleanUp> r = new WeakReference<CleanUp>(c);
		runLevel.add(r);
	}
}
