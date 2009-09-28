package com.moon.dctm.monitoring.sessmon;

import java.util.Date;

import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;

public interface IFilter {

	/**
	 * Initializes filter.
	 * 
	 */
//	void init();
	
	/**
	 * Inspects the session.
	 * This method is called for
	 * every active session. 
	 * @param sessionToProcess session object to inspect
	 * @throws DfException in case of unexpected DFC error.
	 */
	void processSession(IDfTypedObject sessionToProcess) throws DfException;
	
	/**
	 * Returns the time of last inspection.
	 * @return inspection time.
	 */
	Date getInspectTime();	
	
	/**
	 * Complete the session processing.
	 * This method is called after all 
	 * active sessions have been inspected.
	 */
	void complete();
}
