package com.moon.dctm.monitoring.sessmon;

import java.util.List;

public interface IDocbase {

	/**
	 * Returns docbase server count.
	 * @return returns the number of
	 * servers that constitute this docbase.
	 */
	int getServerCount();
	
	/**
	 * Returns the list of
	 * docbase servers.
	 * @return server list.
	 */
	List getServers();
	
	/**
	 * Returns Docbase name.
	 * @return docbase name.
	 */
	String getName();
	
	/**
	 * Returns Docbase id.
	 * @return docbase id.
	 */
	String getID();
	
	/**
	 * Returns Docbase description.
	 * @return docbase description.
	 */
	String getDescription();
}
