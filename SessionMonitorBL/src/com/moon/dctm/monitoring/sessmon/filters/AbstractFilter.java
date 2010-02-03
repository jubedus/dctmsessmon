package com.moon.dctm.monitoring.sessmon.filters;

import java.util.Date;

import com.moon.dctm.monitoring.sessmon.IFilter;


public abstract class AbstractFilter implements IFilter {

	Date InspectionTime = null;
	
	/* (non-Javadoc)
	 * @see com.moon.dctm.monitoring.sessmon.IFilter#getInspectTime()
	 */
	public Date getInspectTime() {
		// Return the ispection time
		return InspectionTime;
	}
	
	/* (non-Javadoc)
	 * @see com.moon.dctm.monitoring.sessmon.IFilter#complete()
	 */
	public void complete() {
		//Reset the inspection time
		InspectionTime = new Date();		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		//Construction the string representation
		//of the filter
		StringBuffer stringFilter = new StringBuffer();
		stringFilter.append("Inspection time=");
		stringFilter.append(InspectionTime);
		
		return stringFilter.toString();
	}
	
	
}
