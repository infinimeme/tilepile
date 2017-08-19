package com.infinimeme.tilepile.station;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.infinimeme.tilepile.common.AbstractSectionPanel;
import com.infinimeme.tilepile.common.Mural;
import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.States;
import com.infinimeme.tilepile.common.Station;
import com.infinimeme.tilepile.common.TilepileException;
import com.infinimeme.tilepile.common.TilepileUtils;

/**
 * Abstract base class of DynamicSectionPanel and StaticSectionPanel, both used
 * to display one Mural Section in the mural displays of the Admin or Station
 * apps. Encapsulates basic operations such as storing working and finished
 * states and painting of the Mural Section accordingly.
 * 
 * @author Greg Barton The contents of this file are released under the GPL.
 *         Copyright 2004-2014 Greg Barton
 */
abstract class StatefulSectionPanel extends AbstractSectionPanel {

	// ~ Instance fields
	// ****************************************************************************

	private static final long serialVersionUID = 4808660462755279206L;

	/** The Station this section panel is being displayed. */
	private Station station = null;

	private int state = States.STATE_NEUTRAL;

	private List<Palette.PaletteColor> unstockedColors = null;
	private int showState = States.STATE_NEUTRAL;

	public static final int SHOW_NONE = States.STATE_NEUTRAL;
	public static final int SHOW_FINISHED = States.STATE_FINISHED;
	public static final int SHOW_LOCKED = States.STATE_LOCKED;
	public static final int SHOW_STOCK = SHOW_LOCKED << 1;

	public static final boolean isStocked(int state) {
		return (state & SHOW_STOCK) == SHOW_STOCK;
	}

	// ~ Constructors
	// *******************************************************************************

	/**
	 * Creates a new StatefulSectionPanel object.
	 * 
	 * @param section
	 *            The underlying section for this panel.
	 * @param palette
	 *            The underlying palette for this section.
	 */
	protected StatefulSectionPanel(Mural.Section section, Palette palette, boolean withGrid) {
		super(section, palette, withGrid);
		calcUnstocked();
	}

	/**
	 * Creates a new StatefulSectionPanel object.
	 * 
	 * @param section
	palette
	finished
	*/
	protected StatefulSectionPanel(Mural.Section section, Palette palette, boolean withGrid, int state) {
		super(section, palette, withGrid);
		setState(state);
		calcUnstocked();
	}

	// ~ Methods
	// ************************************************************************************

	public List<Palette.PaletteColor> getUnstockedColors() {
		return Collections.unmodifiableList(unstockedColors);
	}

	public void toggleShowStates(int state) {
		showState ^= state;
	}

	public void activateShowStates(int state) {
		showState |= state;
	}

	public void deactivateShowStates(int state) {
		showState &= ~state;
	}

	private void calcUnstocked() {

		List<Palette.PaletteColor> unstocked = new LinkedList<Palette.PaletteColor>();

		Map<Integer, Mural.Counter> histogram = getSection().getHistogram();

		for(Integer colorNum : histogram.keySet()) {

			Palette.PaletteColor pc = getPalette().getPaletteColor(colorNum);

			if(pc.getCountInStock() <= 0) {
				unstocked.add(pc);
			}
		}

		unstockedColors = unstocked;
	}

	/**

	 * @param finished
	*/
	public void setState(int state) {
		this.state = state;
	}

	/**

	*/
	public int getState() {
		return state;
	}

	/**
	 * Set Working flag.
	 * 
	 * @param station
	 *            Working flag.
	 */
	public void setStation(Station station) {
		this.station = station;
	}

	/**

	*/
	public Station getStation() {
		return station;
	}

	/**
	 * Paint this panel.
	 * 
	 * @param g
	 *            Graphics context.
	 */
	@Override
	public void paint(Graphics g) {

		Graphics2D g2d = (Graphics2D) g.create();

		try {
			// Clear panel
			if(getStation() == null) {
				g.setColor(Color.BLACK);
			} else {
				g.setColor(getStation().getColor());
			}

			// Fill rectangle
			g.fillRect(0, 0, getWidth(), getHeight());

			// If section new, draw full mural section
			if(getStation() == null && !(States.isFinished(state) && States.isFinished(showState))) {

				try {

					if(!unstockedColors.isEmpty() && isStocked(showState)) {

						g2d.setColor(Color.LIGHT_GRAY);
						g2d.fillRect(0, 0, getWidth(), getHeight());

					} else if(States.isLocked(state) && States.isLocked(showState)) {

						g2d.setColor(Color.PINK);
						g2d.fillRect(0, 0, getWidth(), getHeight());

					} else if(numActiveColors() == 0) {

						// If there are no active colors, show all colors
						showAll(g2d);

					} else {

						// Otherwise, show only active colors.
						showColors(g2d);

					}
				} catch(TilepileException tpe) {
					TilepileUtils.exceptionReport(tpe);
				}
			}
		} finally {
			g2d.dispose();
		}
	}
}
