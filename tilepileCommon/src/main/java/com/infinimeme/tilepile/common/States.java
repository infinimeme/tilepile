package com.infinimeme.tilepile.common;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class States implements TilepileObject {

	public static final int STATE_NEUTRAL = 0;
	public static final int STATE_FINISHED = 1;
	public static final int STATE_LOCKED = STATE_FINISHED << 1;

	//~ Static fields/initializers *****************************************************************

	static final long serialVersionUID = -8977274065946888827L;

	//~ Instance fields ****************************************************************************

	private String name = null;

	private int[][] states = null;

	//~ Constructors *******************************************************************************

	/**
	 * Creates a new SectionStates object.
	 *
	**/
	States(Mural mural) {
		setName(mural.getName());
		init(mural);
	}

	//~ Methods ************************************************************************************

	public int[][] getStates() {
		return states;
	}

	public void setFinished(int x, int y) {
		states[y][x] |= STATE_FINISHED;
	}

	public void setFinished(Location location) {
		states[location.getY()][location.getX()] |= STATE_FINISHED;
	}

	public void toggleFinished(int x, int y) {
		states[y][x] ^= STATE_FINISHED;
	}

	public void toggleFinished(Location location) {
		states[location.getY()][location.getX()] ^= STATE_FINISHED;
	}

	public void setLocked(int x, int y) {
		states[y][x] |= STATE_LOCKED;
	}

	public void setLocked(Location location) {
		states[location.getY()][location.getX()] |= STATE_LOCKED;
	}

	public void toggleLocked(int x, int y) {
		states[y][x] ^= STATE_LOCKED;
	}

	public void toggleLocked(Location location) {
		states[location.getY()][location.getX()] ^= STATE_LOCKED;
	}

	public static final boolean isFinished(int state) {
		return (state & STATE_FINISHED) == STATE_FINISHED;
	}

	public boolean isFinished(int x, int y) {
		return isFinished(get(x, y));
	}

	public boolean isFinished(Location location) {
		return isFinished(get(location));
	}

	public static final boolean isLocked(int state) {
		return (state & STATE_LOCKED) == STATE_LOCKED;
	}

	public boolean isLocked(int x, int y) {
		return isLocked(get(x, y));
	}

	public boolean isLocked(Location location) {
		return isLocked(get(location));
	}

	public int getHeight() {
		return states.length;
	}

	@Override
	public String getName() {
		return name;
	}

	public int getWidth() {
		return states[0].length;
	}

	public boolean add(States other) {

		if(getHeight() != other.getHeight()) {
			return false;
		}

		if(getWidth() != other.getWidth()) {
			return false;
		}

		for(int y = 0; y < states.length; y++) {

			for(int x = 0; x < states[y].length; x++) {
				states[y][x] = states[y][x] | other.states[y][x];
			}
		}

		return true;
	}

	public int get(int x, int y) {
		return states[y][x];
	}

	public int get(Location location) {
		return states[location.getY()][location.getX()];
	}

	public void reset() {

		for(int y = 0; y < states.length; y++) {

			for(int x = 0; x < states[y].length; x++) {
				states[y][x] = STATE_NEUTRAL;
			}
		}
	}

	@Override
	public String toString() {
		return "States: " + getName();
	}

	private void setName(String name) {

		if(name == null || name.equals("")) {
			throw new IllegalArgumentException("States name can not be blank!");
		}

		this.name = name;
	}

	private void init(Mural mural) {

		Mural.Section[][] sections = mural.getSections();

		states = new int[sections.length][];

		for(int i = 0; i < sections.length; i++) {
			states[i] = new int[sections[i].length];

			for(int j = 0; j < states[i].length; j++) {
				states[i][j] = STATE_NEUTRAL;
			}
		}
	}
}
