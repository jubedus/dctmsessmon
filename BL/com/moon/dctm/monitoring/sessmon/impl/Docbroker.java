/**
 * 
 */
package com.moon.dctm.monitoring.sessmon.impl;

import java.util.ArrayList;
import java.util.List;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.common.DfException;
import com.moon.dctm.monitoring.sessmon.IDocbase;
import com.moon.dctm.monitoring.sessmon.IDocbroker;

/**
 * @author AFILIPPOV001
 *
 */
public class Docbroker implements IDocbroker {

	IDfDocbaseMap docbases;
	
	int docbaseCount = 0;
	List docbaseList = null;
	

	Docbroker() throws DfException{
		IDfClientX clientx = new DfClientX();
        IDfClient client = clientx.getLocalClient();
        docbases = client.getDocbaseMap();;
		
		docbaseCount=docbases.getDocbaseCount();
		
		docbaseList = new ArrayList();
		for(int i=0;i<docbaseCount;i++){
			String docbaseName = docbases.getDocbaseName(i);
			String docbaseId = docbases.getDocbaseId(i);
			String docbaseDesc = docbases.getDocbaseDescription(i);
			
			IDocbase docbase = new Docbase(docbaseId, docbaseName, docbaseDesc);	
			docbaseList.add(docbase);				
		}
        
    }
	
	/* (non-Javadoc)
	 * @see com.moon.dctm.monitoring.sessmon.IDocbroker#getDocbaseCount()
	 */
	public int getDocbaseCount() {
		return docbaseCount;
	}

	/* (non-Javadoc)
	 * @see com.moon.dctm.monitoring.sessmon.IDocbroker#getDobases()
	 */
	public List getDobases() {
		return docbaseList;
	}
}
