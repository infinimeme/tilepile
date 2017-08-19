package com.infinimeme.tilepile.common;

import java.io.Serializable;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class MuralLocation implements Serializable {

	//~ Instance fields ****************************************************************************

	private static final long serialVersionUID = 6306353769670136771L;

	private Location location = null;

	private String muralName = null;

	//~ Constructors *******************************************************************************

	public MuralLocation(Location location, String muralName) {
		setLocation(location);
		setMuralName(muralName);
	}

	//~ Methods ************************************************************************************

	public Location getLocation() {

		return location;
	}

	public String getMuralName() {

		return muralName;
	}

	@Override
	public boolean equals(Object o) {

		if(o instanceof MuralLocation) {

			MuralLocation other = (MuralLocation) o;

			return getMuralName().equals(other.getMuralName()) && getLocation().equals(other.getLocation());
		} else {

			return false;
		}
	}

	/**
	*
	**/
	@Override
	public int hashCode() {

		return getMuralName().hashCode() ^ getLocation().hashCode();
	}

	/**
	*
	**/
	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(getMuralName());
		sb.append(":");
		sb.append(getLocation());

		return sb.toString();
	}

	/**
	*
	**/
	private void setLocation(Location location) {
		this.location = location;
	}

	/**
	*
	**/
	private void setMuralName(String muralName) {
		this.muralName = muralName;
	}
}
