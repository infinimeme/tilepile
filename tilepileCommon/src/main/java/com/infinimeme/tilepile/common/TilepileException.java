package com.infinimeme.tilepile.common;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class TilepileException extends Exception {

	//~ Constructors *******************************************************************************

	private static final long serialVersionUID = 7859828086258531492L;

	public TilepileException() {
		super();
	}

	public TilepileException(String message) {
		super(message);
	}

	public TilepileException(Throwable cause) {
		super(cause);
	}

	public TilepileException(String message, Throwable cause) {
		super(message, cause);
	}
}
