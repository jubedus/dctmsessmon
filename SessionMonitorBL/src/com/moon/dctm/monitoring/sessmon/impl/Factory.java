/**
 * 
 */
package com.moon.dctm.monitoring.sessmon.impl;

import com.documentum.fc.common.DfException;
import com.moon.dctm.monitoring.sessmon.IDocbroker;

/**
 * Factory class.
 * This class provides methods to instantiate
 * objects used by the application.
 * 
 * @author afilippov001
 *
 */
public class Factory {

	/**
	 * Factory method to instantiate a docbroker.
	 * This method should be used to get a 
	 * new instance of docbroker.
	 * @return docbroker instance.
	 * @throws DfException in case of a failure
	 * during initialization of docbroker.
	 */
	public static IDocbroker newDocbroker() throws DfException
	{
		return new Docbroker();
	}
	
}
