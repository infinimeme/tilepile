package com.infinimeme.tilepile.common;

import java.awt.Color;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class Station implements TilepileObject {

	//~ Static fields/initializers *****************************************************************

	static final long serialVersionUID = -573805948011321798L;

	//~ Instance fields ****************************************************************************

	private Color color = null;

	/** Name of this Station */
	private String name = null;

	private int number = 0;

	private String remoteName = null;

	//~ Constructors *******************************************************************************

	Station(String name, Color color, int number, String remoteNameStub) {
		setName(name);
		setColor(color);
		setNumber(number);
		setRemoteName("//" + TilepileUtils.getDefaultLocalAddress().getHostAddress() + "/" + remoteNameStub);
		TilepileUtils.logInfo("INFO: Creating station " + getName() + " with remote name " + getRemoteName());
	}

	//~ Methods ************************************************************************************

	public Color getColor() {

		return color;
	}

	public void setName(String name) {

		if(name == null || name.equals("")) {
			throw new IllegalArgumentException("Station name can not be blank!");
		}

		this.name = name;
	}

	@Override
	public String getName() {

		return name;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getNumber() {

		return number;
	}

	public void setRemoteName(String remoteName) {
		this.remoteName = remoteName;
	}

	public String getRemoteName() {

		return remoteName;
	}

	public String getRemoteNameStub() {

		return getRemoteName().substring(getRemoteName().lastIndexOf('/') + 1);
	}

	@Override
	public boolean equals(Object o) {

		return o instanceof Station && o.hashCode() == hashCode();
	}

	@Override
	public int hashCode() {

		return getRemoteName().hashCode() ^ getName().hashCode() ^ getColor().hashCode();
	}

	@Override
	public String toString() {

		return "Station: " + getName();
	}

	private void setColor(Color color) {
		this.color = color;
	}
}
