package com.infinimeme.tilepile.common;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JFrame;

import com.infinimeme.tilepile.data.DataManagerRemote;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class StationRemoteImpl extends UnicastRemoteObject implements StationRemote {

	private static final long serialVersionUID = 1594502302456487172L;

	//~ Instance fields ****************************************************************************

	private DataManagerRemote dataManager = null;

	private JFrame app = null;

	private MuralLocation muralLocation = null;

	private StationRemoteListener listener = null;

	//~ Constructors *******************************************************************************

	public StationRemoteImpl(DataManagerRemote dataManager, JFrame app) throws RemoteException {
		super();
		setDataManager(dataManager);
		setApp(app);
	}

	//~ Methods ************************************************************************************

	public void setListener(StationRemoteListener listener) {
		this.listener = listener;
	}

	@Override
	public void setMuralLocation(String mainStationName, MuralLocation muralLocation) throws RemoteException {
		this.muralLocation = muralLocation;
		listener.muralLocationSet(getDataManager().getMainStation(mainStationName), muralLocation);
	}

	@Override
	public MuralLocation getMuralLocation() {

		return muralLocation;
	}

	@Override
	public void close() {
		getApp().setVisible(false);
	}

	private void setApp(JFrame app) {
		this.app = app;
	}

	private JFrame getApp() {

		return app;
	}

	private void setDataManager(DataManagerRemote dataManager) {
		this.dataManager = dataManager;
	}

	private DataManagerRemote getDataManager() {

		return dataManager;
	}
}
