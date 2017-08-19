package com.infinimeme.tilepile.common;

import java.io.Serializable;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class Location implements Serializable, Comparable<Location> {

	//~ Instance fields ****************************************************************************

	private static final long serialVersionUID = -2852427180879061445L;

	private int x;

	private int y;

	//~ Constructors *******************************************************************************

	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}

	//~ Methods ************************************************************************************

	public final void setX(int x) {
		this.x = x;
	}

	public final int getX() {

		return this.x;
	}

	public final void setY(int y) {
		this.y = y;
	}

	public final int getY() {

		return this.y;
	}

	@Override
	public int compareTo(Location other) {

		if(x < other.x) {

			return -1;
		} else if(x > other.x) {

			return 1;
		} else {

			if(y < other.y) {

				return -1;
			} else if(y > other.y) {

				return 1;
			} else {

				return 0;
			}
		}
	}

	@Override
	public boolean equals(Object o) {

		if(o instanceof Location) {

			Location other = (Location) o;

			return x == other.x && y == other.y;
		} else {

			return false;
		}
	}

	@Override
	public int hashCode() {

		long bits = getX();
		bits ^= (long) getY() * 31;

		return (int) bits ^ (int) (bits >> 32);
	}

	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(x);
		sb.append(",");
		sb.append(y);

		return sb.toString();
	}
}
