/**
 * 
 */
package com.infinimeme.tilepile.station;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.util.List;

import com.infinimeme.tilepile.common.Mural.Section;
import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.States;
import com.infinimeme.tilepile.common.TilepileUtils;

/**
 * @author Greg Barton The contents of this file are released under the GPL.
 *         Copyright 2004-2014 Greg Barton
 */
public class MuralSectionPanel extends StaticSectionPanel {

	private static final long serialVersionUID = 795293400971681135L;

	public MuralSectionPanel(Section section, Palette palette, boolean withGrid, GraphicsConfiguration graphicsConfiguration, int x, int y) {
		super(section, palette, withGrid, graphicsConfiguration);
		makeToolTip(x, y);
	}

	public MuralSectionPanel(Section section, Palette palette, boolean withGrid, int state, GraphicsConfiguration graphicsConfiguration, int x, int y) {
		super(section, palette, withGrid, state, graphicsConfiguration);
		makeToolTip(x, y);
	}

	@Override
	public Dimension getMaximumSize() {
		return getMinimumSize();
	}

	/**
	 * Get preferred size
	 * 
	 * @return Sreferred size Dimension
	 */
	@Override
	public Dimension getPreferredSize() {
		return getMinimumSize();

	}

	private void makeToolTip(int x, int y) {

		StringBuffer sb = new StringBuffer();
		sb.append(x);
		sb.append("-");
		sb.append(TilepileUtils.indexToCharacter(y));

		if(States.isFinished(getState())) {
			sb.append(" FINISHED ");
		}

		if(States.isLocked(getState())) {
			sb.append(" LOCKED ");
		}

		List<Palette.PaletteColor> colors = getUnstockedColors();

		if(!colors.isEmpty()) {

			sb.append(" ");

			for(Palette.PaletteColor pc : colors) {
				sb.append(pc.getName());
				sb.append(" ");
			}
		}

		setToolTipText(sb.toString());
	}
}
