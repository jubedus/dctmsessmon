/**
 * 
 */
package com.moon.dctm.monitoring.sessmon.filters;

import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;

/**
 * @author AFILIPPOV001
 *
 */
public class ActiveSessionCounter extends AbstractFilter {

	final static String ATTR_SESSION_STATUS = "session_status";
	final static String VALUE_ACTIVE_SESSION_STATUS = "Active";
	
	int activeSessionCount = 0;
	int finalActiveSessionCount = 0;
	int previousActiveSessionCount = 0;

	/* (non-Javadoc)
	 * @see com.moon.dctm.monitoring.sessmon.IFilter#processSession(com.documentum.fc.client.IDfTypedObject)
	 */
	public void processSession(IDfTypedObject sessionToProcess) throws DfException {
		
		String sessStatus = sessionToProcess.getString(ATTR_SESSION_STATUS);
		if(sessStatus!=null && sessStatus.equalsIgnoreCase(VALUE_ACTIVE_SESSION_STATUS)){
			activeSessionCount++;
		}
	}

	
	public int getActiveSessionCount() {
		// Return the active session count
		return finalActiveSessionCount;
	}

	public int getPreviousSessionCount() {
		// Return the active session count
		return previousActiveSessionCount;
	}
	
	public void complete() {
		//Call super implementation
		super.complete();
		//Update actual counters
		previousActiveSessionCount = finalActiveSessionCount; 
		finalActiveSessionCount = activeSessionCount;
		//Reset the active session count
		activeSessionCount = 0;
	}


	/* (non-Javadoc)
	 * @see com.moon.dctm.monitoring.sessmon.filters.AbstractFilter#toString()
	 */
	public String toString() {
		StringBuffer stringFilter = new StringBuffer();
		stringFilter.append(super.toString());
		stringFilter.append(": Active session count =");
		stringFilter.append(getActiveSessionCount());
		
		return stringFilter.toString();
		
	}
	
	
}
