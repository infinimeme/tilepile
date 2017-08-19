package com.infinimeme.tilepile.common;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.rmi.RemoteException;

import com.infinimeme.tilepile.data.DataManagerRemote;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class StationFactory {

	//~ Static fields/initializers *****************************************************************

	private static final Color[] COLOR_WHEEL = {
	Color.BLUE, Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.BLUE.brighter(), Color.RED.brighter(), Color.YELLOW.brighter(), Color.CYAN.brighter(), Color.GREEN
	.brighter(), Color.MAGENTA.brighter(), Color.ORANGE.brighter(), Color.BLUE.darker(), Color.RED.darker(), Color.YELLOW.darker(), Color.CYAN.darker(), Color.GREEN.darker(), Color.MAGENTA.darker(), Color.ORANGE
	.darker(), Color.BLUE.brighter().brighter(), Color.RED.brighter().brighter(), Color.YELLOW.brighter().brighter(), Color.CYAN.brighter().brighter(), Color.GREEN.brighter().brighter(), Color.MAGENTA
	.brighter().brighter(), Color.ORANGE.brighter().brighter(), Color.BLUE.darker().darker(), Color.RED.darker().darker(), Color.YELLOW.darker().darker(), Color.CYAN.darker().darker(), Color.GREEN
	.darker().darker(), Color.MAGENTA.darker().darker(), Color.ORANGE.darker().darker()
	};

	//~ Methods ************************************************************************************

	public static final String getDefaultName(GraphicsConfiguration gc) {

		return TilepileUtils.getNormalizedName(TilepileUtils.getDefaultLocalAddress().getHostAddress()) + TilepileUtils.getNormalizedName(gc.getDevice().getIDstring());
	}

	public static final Station make(DataManagerRemote dataManager, GraphicsConfiguration gc, int number) throws RemoteException {

		Color color = COLOR_WHEEL[Math.abs(number) % COLOR_WHEEL.length];

		//Color color = COLOR_WHEEL[dataManager.namesOfStation().size() % COLOR_WHEEL.length];

		return new Station(getDefaultName(gc), color, number, StationRemote.REMOTE_NAME + TilepileUtils.getNormalizedName(gc.getDevice().getIDstring()));
	}
}
