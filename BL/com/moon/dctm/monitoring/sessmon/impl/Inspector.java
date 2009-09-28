/**
 * 
 */
package com.moon.dctm.monitoring.sessmon.impl;

import com.moon.dctm.monitoring.sessmon.IServer;

/**
 * @author afilippov001
 *
 */
public class Inspector implements Runnable{

	IServer docbaseServer = null;
	//Default the sleeping period to 30 seconds
	long timeToSleep = 60000;
	
	public Inspector(IServer serverToInspect) 
	{	
		//Store the server instance
		docbaseServer = serverToInspect;
	}
	
	public Inspector(IServer serverToInspect, long refreshInterval) 
	{	
		//Store the server instance
		docbaseServer = serverToInspect;
		//Store the refresh interfal
		timeToSleep = refreshInterval;
	}
	
	public void inspect()
	{
		docbaseServer.updateFilters();
	}


	public void run() {
		
		while(true){
			
			inspect();
			
			try {
				Thread.sleep(timeToSleep);
			} catch (InterruptedException e) {
				// Ignore the interruption
			}
		}
	}

}
