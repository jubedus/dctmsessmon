package com.moon.dctm.monitoring.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.moon.dctm.monitoring.shared.DocbaseServer;

/**
 * The async counterpart of <code>MonitoringService</code>.
 */
public interface MonitoringServiceAsync {
	
	  void getServers(String docbaseName, AsyncCallback<DocbaseServer[]> callback);
}
