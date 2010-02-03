package com.moon.dctm.monitoring.sessmon;

import java.util.List;

public interface IDocbroker {


	/**
	 * Returns the number of docbases  
	 * accessible through this docbroker. 
	 * @return Docbase count.
	 */
	int getDocbaseCount();
	
	/**
	 * Return a list of docbases
	 * accessible trough this
	 * docbroker.
	 * @return docbase list.
	 */
	List getDobases();
}
