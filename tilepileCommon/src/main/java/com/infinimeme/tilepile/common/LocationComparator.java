package com.infinimeme.tilepile.common;

import java.util.Comparator;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class LocationComparator implements Comparator<Location> {

	//~ Methods ************************************************************************************

	@Override
	public int compare(Location l1, Location l2) {

		if(l1.getX() > l2.getX()) {

			return 1;
		} else if(l1.getX() < l2.getX()) {

			return -1;
		} else {

			if(l1.getY() > l2.getY()) {

				return 1;
			} else if(l1.getY() < l2.getY()) {

				return -1;
			} else {

				return 0;
			}
		}
	}

	@Override
	public boolean equals(Object obj) {

		if(obj instanceof LocationComparator) {

			return true;
		} else {

			return false;
		}
	}
}
