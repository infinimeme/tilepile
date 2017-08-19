package com.infinimeme.tilepile.common;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class MainStationRemoteImpl extends UnicastRemoteObject implements MainStationRemote {

	//~ Instance fields ****************************************************************************

	private static final long serialVersionUID = 2995984304314561396L;

	private List<MainStationRemoteListener> listeners = new LinkedList<MainStationRemoteListener>();

	//~ Constructors *******************************************************************************

	/**
	 * Creates a new MainStationRemoteImpl object.
	 *
	**/
	public MainStationRemoteImpl() throws RemoteException {
		super();
	}

	//~ Methods ************************************************************************************

	/**
	*
	**/
	public void addListener(MainStationRemoteListener listener) {
		listeners.add(listener);
	}

	/**
	*
	**/
	public void removeListener(MainStationRemoteListener listener) {
		listeners.remove(listener);
	}

	/**
	*
	**/
	@Override
	public void releaseSection(MuralLocation muralLocation) {
		for(MainStationRemoteListener listener : listeners) {
			listener.sectionReleased(muralLocation);
		}
	}
}
