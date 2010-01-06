package com.moon.dctm.monitoring.client.rpc;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DocbaseServer   implements IsSerializable{
	
	String name;
	String host;
	String docbaseName;
	String ID;
	int maxSessCount;
	int currSessCount;
	int prevSessCount;
	Date lastUpdate;
	ServiceException lastException;


	public DocbaseServer() {
	  }
	  
	  public DocbaseServer(String docbaseName,String name,String host ) {
		  	this.docbaseName=docbaseName;
		    this.name=name;
		    this.host=host;
		    this.maxSessCount = 0;
		    this.currSessCount=0;
		    this.prevSessCount=0;
		    this.lastUpdate=null;
		    
		  }
	  
	  public DocbaseServer(String name,String host, int sessionsMax,int sessionsCurrent, int sessionsPrev, Date refreshDate) {
	    this.name=name;
	    this.host=host;
	    this.maxSessCount = sessionsMax;
	    this.currSessCount=sessionsCurrent;
	    this.prevSessCount=sessionsPrev;
	    this.lastUpdate=refreshDate;
	    
	  }

	  public DocbaseServer(String docbaseName, String name,String host, int sessionsMax,int sessionsCurrent, int sessionsPrev, Date refreshDate) {
		    this.docbaseName=docbaseName;
		    this.name=name;
		    this.host=host;
		    this.maxSessCount = sessionsMax;
		    this.currSessCount=sessionsCurrent;
		    this.prevSessCount=sessionsPrev;
		    this.lastUpdate=refreshDate;
		    
		  }
//	  public DocbaseServer(IServer server) {
//		    this.name=server.getName();
//		    this.host=server.getHost();
//		    this.maxSessCount = server.getMaxSessionCount();
//		    ActiveSessionCounter activeSession = (ActiveSessionCounter)server.getFilter("ActiveSessionCounter");
//		    this.currSessCount=activeSession.getActiveSessionCount();
//		    this.prevSessCount=activeSession.getPreviousSessionCount();
//		    this.lastUpdate=activeSession.getInspectTime();
//		    
//		  }
	  
	/**
	 * @return Returns the currSessCount.
	 */
	public int getCurrSessCount() {
		return currSessCount;
	}

	/**
	 * @return Returns the host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return Returns the lastUpdate.
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @return Returns the maxSessCount.
	 */
	public int getMaxSessCount() {
		return maxSessCount;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the prevSessCount.
	 */
	public int getPrevSessCount() {
		return prevSessCount;
	}

	/**
	 * @return Returns the docbaseName.
	 */
	public String getDocbaseName() {
		return docbaseName;
	}

	/**
	 * @return Returns the iD.
	 */
	public String getID() {
		return (this.name+this.host);
	}
	
	/**
	 * @return Returns the change in session count.
	 */
	public int getChange() {
		return (this.currSessCount - this.prevSessCount);
	}
	
	/**
	 * @return Returns the number of active sessions
	 * as a per cent of the maximum allowed sessions.
	 */
	public int getPercentUsed() {
		float percentUsed = this.currSessCount*100.0f/this.maxSessCount;
		return Math.round(percentUsed);
	}
	
	/**
	 * Returns the last encountered exception.
	 * @return Last exception or null if no exception was encountered.
	 */
	
	  public ServiceException getLastException() {
		return lastException;
	}

	  /**
	   * Sets the last encountered exception.
	   * @param lastException exception to set.
	   */
	public void setLastException(ServiceException lastException) {
		this.lastException = lastException;
	}

	/**
	 * Sets the maximum session count supported
	 * by the server.
	 * @param maxSessCount Maximum session count
	 */
	public void setMaxSessCount(int maxSessCount) {
		this.maxSessCount = maxSessCount;
	}
	
	/**
	 * Sets the current session count.
	 * @param currSessCount current session count.
	 */
	public void setCurrSessCount(int currSessCount) {
		this.currSessCount = currSessCount;
	}
	
	/**
	 * Sets the previous session count.
	 * @param prevSessCount previous session count.
	 */
	public void setPrevSessCount(int prevSessCount) {
		this.prevSessCount = prevSessCount;
	}
	
	/**
	 * Set the date of last update.
	 * @param lastUpdate last update date.
	 */
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
}
