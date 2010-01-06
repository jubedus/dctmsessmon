package com.moon.dctm.monitoring.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.moon.dctm.monitoring.client.constants.UIStrings;
import com.moon.dctm.monitoring.client.constants.WarningLevels;
import com.moon.dctm.monitoring.client.rpc.DocbaseServer;
import com.moon.dctm.monitoring.client.rpc.MonitoringService;
import com.moon.dctm.monitoring.client.rpc.MonitoringServiceAsync;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SessionMonitor implements EntryPoint {

	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable serversTable = new FlexTable();
	private Label refreshTime = new Label();
	private Label errorMsg = new Label();

	private static final int DEFAULT_REFRESH_INTERVAL = 60 * 1000; // ms
	private MonitoringServiceAsync stockPriceSvc;
	private ArrayList<String> serverIDs = new ArrayList<String>();

	private UIStrings titles;
	private WarningLevels levels;

	private boolean isInit=true;
	
	/**
	 * Entry point to the UI.
	 * This method builds the User Interface.
	 */
	public void onModuleLoad() {
		//Initialize constants
		titles = GWT.create(UIStrings.class);		
		levels = GWT.create(WarningLevels.class);

		// Initialize the UI
		initSessMonView();
	}

	/**
	 * Initializes UI controls.
	 * This method builds and initializes
	 * all UI controls.
	 */
	private void initSessMonView() {
		
		serversTable.addStyleName("watchList");
		
		// Set up the first header row
		serversTable.setText(0, 0, titles.Docbase());
		serversTable.getFlexCellFormatter().setRowSpan(0, 0, 2);

		serversTable.setText(0, 1, titles.Server());
		serversTable.getFlexCellFormatter().setColSpan(0, 1, 2);
		
		serversTable.setText(0, 2, titles.Sessions());
		serversTable.getFlexCellFormatter().setColSpan(0, 2, 5);
		
		// Set up the second header row
		serversTable.setText(1, 0, titles.Name());
		serversTable.setText(1, 1, titles.Host());
		serversTable.setText(1, 2, titles.MaxCount());
		serversTable.setText(1, 3, titles.ActiveCount());
		serversTable.setText(1, 4, titles.PercentUsed());
		serversTable.setText(1, 5, titles.CountChange());
		serversTable.setText(1, 6, titles.CountTime());

		//Apply styles	
		RowFormatter formatRow = serversTable.getRowFormatter();
		formatRow.addStyleName(0, "watchListHeader");
		formatRow.addStyleName(1, "watchListHeader");
		
		CellFormatter formatCell = serversTable.getCellFormatter();
		formatCell.addStyleName(1, 2, "watchListNumericColumn");
		formatCell.addStyleName(1, 3, "watchListNumericColumn");
		formatCell.addStyleName(1, 4, "watchListNumericColumn");
		formatCell.addStyleName(1, 5, "watchListNumericColumn");
		
		errorMsg.setStyleName("ErrorMsg");
		
		refreshTime.addStyleName("refreshLabel");
		
		// Assemble the main panel
		
		// Add the Page Refresh label
		mainPanel.add(refreshTime);
		
		// Add the grid
		mainPanel.add(serversTable);

		// Add the Error Message label
		mainPanel.add(errorMsg);
		
		// add the main panel to the HTML element with the id "stockList"
		RootPanel.get(titles.MainPanelName()).add(mainPanel);

		queryServers();
	}

	/**
	 * Requests update.
	 * This method sends a RPC request to 
	 * get the latest information about available
	 * servers.
	 */
	private void queryServers() {
		// lazy initialization of service proxy
		if (stockPriceSvc == null) {
			// stockPriceSvc = GWT.create(SchoolCalendarService.class);
			stockPriceSvc = (MonitoringServiceAsync) GWT
					.create(MonitoringService.class);
			// By default, we assume we'll make RPCs to a servlet, but see
			// updateRowData(). There is special support for canned RPC
			// responses.
			// (Which is a totally demo hack, by the way :-)
			// 
			ServiceDefTarget target = (ServiceDefTarget) stockPriceSvc;

			// Use a module-relative URLs to ensure that this client code can
			// find
			// its way home, even when the URL changes (as might happen when you
			// deploy this as a webapp under an external servlet container).
			String moduleRelativeURL = GWT.getModuleBaseURL() + "monitor";
			target.setServiceEntryPoint(moduleRelativeURL);
		}

		AsyncCallback<DocbaseServer[]> callback = new AsyncCallback<DocbaseServer[]>() {
			public void onFailure(Throwable caught) {
				handleException(caught);
				startMonitoring();
			}

			public void onSuccess(DocbaseServer[] result) {
				updateServers(result);
				startMonitoring();
			}
		};

		stockPriceSvc.getServers(null, callback);
	}

	/**
	 * Updates displayed servers.
	 * This method adds or updates
	 * the table of displayed servers.
	 * @param servers - list of servers.
	 */
	private void updateServers(DocbaseServer[] servers) {
		if (servers != null) {
			
			//Clear the error message
			errorMsg.setText("");
			
			for (int i = 0; i < servers.length; i++) {
				DocbaseServer server = servers[i];
				String serverID = server.getID();
				int row = 0;
				// make sure the server is still in our watch list
				if (!serverIDs.contains(serverID)) {
					// Add server to the list
					serverIDs.add(serverID);
					row = serverIDs.size()+1;
					serversTable.setText(row, 0, server.getDocbaseName());
					serversTable.setText(row, 1, server.getName());
					serversTable.setText(row, 2, server.getHost());
				} else {
					// Update the existing server
					row = serverIDs.indexOf(serverID) + 2;
				}

				// apply nice formatting to numbers
				NumberFormat countFormat = NumberFormat.getFormat("#,##0");
				String countNow = countFormat.format(server.getCurrSessCount());
				String countMax = countFormat.format(server.getMaxSessCount());
				NumberFormat changeFormat = NumberFormat
						.getFormat("+#,##0;-#,##0");
				String changeText = changeFormat.format(server.getChange());
				
				String updateTime = "NA";
				if (server.getLastUpdate()!=null){
					updateTime = DateTimeFormat.getMediumTimeFormat()
					.format(server.getLastUpdate());				
				}


				// update the watch list with the new values
				serversTable.setText(row, 3, countMax);
				serversTable.setText(row, 4, countNow);
				serversTable.setText(row, 5, Integer.toString(server.getPercentUsed())+"%");
				Label changeTextlabel = new Label(changeText);
				serversTable.setWidget(row, 6, changeTextlabel);
				Label timestamplabel = new Label(updateTime);
				serversTable.setWidget(row, 7, timestamplabel);

				// stocksFlexTable.getRowFormatter().addStyleName(row,
				// "watchListHeader");
				CellFormatter formatCell = serversTable.getCellFormatter();				
				formatCell.addStyleName(row, 3, "watchListNumericColumn");
				formatCell.addStyleName(row, 4, "watchListNumericColumn");
				formatCell.addStyleName(row, 5, "watchListNumericColumn");				
				formatCell.addStyleName(row, 6, "watchListNumericColumn");
				
				//Apply Trend formatting
				String changeStyleName = "noChange";
				if (server.getChange() > 0.0f) {
					changeStyleName = "positiveChange";
				} else if (server.getChange() < 0.0f) {
					changeStyleName = "negativeChange";
				}
				changeTextlabel.setStyleName(changeStyleName);
				
				RowFormatter formatRow = serversTable.getRowFormatter();
				//Apply alternating fomratting
				if((row % 2) == 1){
					formatRow.addStyleName(row, "alterRow");
				}
				
				//Apply Threshold formatting
				int percentUsed = server.getPercentUsed();	
				
				if (percentUsed>=levels.LevelHigh()){
					formatRow.addStyleName(row, "usageLimitHigh");
				}else if (percentUsed>=levels.LevelLow()){
					formatRow.addStyleName(row, "usageLimitLow");
				}else{
					formatRow.removeStyleName(row, "usageLimitHigh");	
					formatRow.removeStyleName(row, "usageLimitLow");
				}
				
				//List errors
				if(server.getLastException() !=null){
					//Construct error messages
					StringBuffer errMsg = new StringBuffer();
					//Start with existing error message
					errMsg.append(errorMsg.getText());
					errMsg.append(server.getLastException().getMessage());
					errMsg.append("\n");
					
					//Print to the error label
					errorMsg.setText(errMsg.toString());
				}
 			}

			// Update the page refresh timestamp
			updatePageRefresh();
		}
	}

	/**
	 * Handles RPC exception.
	 * This is a callback method, which is called
	 * in case of a RPC problem.
	 * @param caughtException
	 */
	private void handleException(Throwable caughtException){
		errorMsg.setText(caughtException.getMessage());
		updatePageRefresh();
	}
	
	/**
	 * Updates page refresh timestamp.
	 * This method updates label
	 * that displays page refresh timestamp.
	 */
	private void updatePageRefresh(){
		String refreshTimestamp = DateTimeFormat.getMediumDateTimeFormat()
		.format(new Date());

		refreshTime.setText("Page refreshed at " + refreshTimestamp);
	}
	
	/**
	 * Launches monitoring.
	 * This method initializes timer to 
	 * submit regular RPC requests to the server.
	 */
	private void startMonitoring(){
		if(isInit){
			int pageRefresh = DEFAULT_REFRESH_INTERVAL;
			if(levels.PageRefreshInterval()>0){
				pageRefresh = levels.PageRefreshInterval()* 1000;
			}
			
			// setup timer to refresh list automatically
			Timer refreshServerTimer = new Timer() {
				public void run() {
					queryServers();
				}
			};
			refreshServerTimer.scheduleRepeating(pageRefresh);
			isInit=false;
		}
	}
}
