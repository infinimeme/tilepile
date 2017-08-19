package com.infinimeme.tilepile.common;


/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class MainStation implements TilepileObject {

	//~ Static fields/initializers *****************************************************************

	static final long serialVersionUID = -1362094956127367266L;

	//~ Instance fields ****************************************************************************

	/** Name of this Station */
	private String name = null;

	private String remoteName = null;

	//~ Constructors *******************************************************************************

	/**
	 * Creates a new MainStation object.
	 **/
	MainStation() {
		setName(getDefaultName());
		setRemoteName("//" + getName() + "/" + MainStationRemote.REMOTE_NAME);
	}

	//~ Methods ************************************************************************************

	/**
	*
	**/
	public static final String getDefaultName() {

		return TilepileUtils.getDefaultLocalAddress().getHostAddress();
	}

	/**
	*
	**/
	public void setName(String name) {

		if(name == null || name.equals("")) {
			throw new IllegalArgumentException("MainStation name can not be blank!");
		}

		this.name = name;
	}

	/**
	 * Get the name of this Station
	 *
	 * @return name of this Station
	 **/
	@Override
	public String getName() {
		return name;
	}

	/**
	*
	**/
	public void setRemoteName(String remoteName) {
		this.remoteName = remoteName;
	}

	/**
	*
	**/
	public String getRemoteName() {

		return remoteName;
	}

	/**
	*
	*
	**/
	@Override
	public boolean equals(Object o) {

		return o instanceof MainStation && o.hashCode() == hashCode();
	}

	/**
	*
	**/
	@Override
	public int hashCode() {

		return getRemoteName().hashCode() ^ getName().hashCode();
	}

	/**
	*
	**/
	@Override
	public String toString() {

		return "MainStation: " + getName();
	}
}
