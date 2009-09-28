package com.moon.dctm.monitoring.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface MonitoringServiceAsync {
	
	  void getServers(String docbaseName, AsyncCallback<DocbaseServer[]> callback);
}
