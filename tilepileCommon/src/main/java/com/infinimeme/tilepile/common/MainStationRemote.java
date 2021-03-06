package com.infinimeme.tilepile.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public interface MainStationRemote extends Remote {

	//~ Static fields/initializers *****************************************************************

	public static final String REMOTE_NAME = "MSR";

	//~ Methods ************************************************************************************

	/**
	*
	*
	**/
	public void releaseSection(MuralLocation muralLocation) throws RemoteException;
}
