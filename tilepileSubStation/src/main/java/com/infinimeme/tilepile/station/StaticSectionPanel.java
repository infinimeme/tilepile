package com.infinimeme.tilepile.station;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.image.VolatileImage;

import com.infinimeme.tilepile.common.Mural;
import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.TilepileException;

/**
 * A section panel that never changes it's bounds. Buffered images are cached
 * for each possible combination of states.
 * 
 * @author Greg Barton The contents of this file are released under the GPL.
 *         Copyright 2004-2014 Greg Barton
 */
public class StaticSectionPanel extends StatefulSectionPanel {

	// ~ Instance fields
	// ****************************************************************************

	private static final long serialVersionUID = -185418861905771676L;

	/** Bounds of this section panel that do not change */
	private Dimension staticBounds = null;

	/** Cached image for all colors */
	private VolatileImage allImage = null;

	/** Cached image for all colors with numbers */
	private VolatileImage allImageNum = null;

	/** Cached image for all colors in working state */
	private VolatileImage workingImage = null;

	/** Cached image for all colors in working state with numbers */
	private VolatileImage workingImageNum = null;

	private GraphicsConfiguration graphicsConfiguration = null;

	// ~ Constructors
	// *******************************************************************************

	/**
	 * Creates a new StaticSectionPanel object.
	 * 
	 * @param section
	 *            Mural.Section to be displayed
	 * @param palette
	*/
	public StaticSectionPanel(Mural.Section section, Palette palette, boolean withGrid, GraphicsConfiguration graphicsConfiguration) {
		super(section, palette, withGrid);

		setGraphicsConfiguration(graphicsConfiguration);
		setStaticBounds(new Dimension(section.getWidth(), section.getHeight()));
		//initAllImage();
	}

	/**
	 * Creates a new StaticSectionPanel object.
	 */
	public StaticSectionPanel(Mural.Section section, Palette palette, boolean withGrid, int state, GraphicsConfiguration graphicsConfiguration) {
		super(section, palette, withGrid, state);

		setGraphicsConfiguration(graphicsConfiguration);
		setStaticBounds(new Dimension(section.getWidth(), section.getHeight()));
		//initAllImage();
	}

	// ~ Methods
	// ************************************************************************************

	private void setGraphicsConfiguration(GraphicsConfiguration graphicsConfiguration) {
		this.graphicsConfiguration = graphicsConfiguration;
	}

	@Override
	public GraphicsConfiguration getGraphicsConfiguration() {
		return graphicsConfiguration;
	}

	/**
	 * Return the static bounds.
	 * 
	 * @return The static bounds.
	 */
	@Override
	public Dimension getMaximumSize() {
		return getStaticBounds();
	}

	/**
	 * Return the static bounds.
	 * 
	 * @return The static bounds.
	 */
	@Override
	public Dimension getMinimumSize() {
		return getStaticBounds();
	}

	/**
	 * Return the static bounds.
	 * 
	 * @return The static bounds.
	 */
	@Override
	public Dimension getPreferredSize() {
		return getStaticBounds();
	}

	/**
	 * Return the static bounds.
	 * 
	 * @return The static bounds.
	 */
	public Dimension getStaticBounds() {
		return staticBounds;
	}

	/**
	*/
	public void clearStaticImages() {
		allImage = null;
		allImageNum = null;
		workingImage = null;
		workingImageNum = null;
	}

	/*
	    private void initAllImage() {
	        try {
	            getAllImage();
	        } catch (TilepileException te) {
	            TilepileUtils.exceptionReport(te);
	        }
	    }
	*.
	    /**
	     * Return a DynamicSectionPanel that backs the same section as this panel.
	     * 
	     * @return A DynamicSectionPanel
	     */
	public DynamicSectionPanel makeDynamic() {
		return new DynamicSectionPanel(getSection(), getPalette(), isWithGrid(), getState());
	}

	/**
	 * Draw all tiles in color
	 * 
	 * @param g
	 *            Graphics context
	 * @throws TilepileException
	 *             See getWorkingImage() or getAllImage()
	 */
	@Override
	protected void showAll(Graphics2D g) throws TilepileException {

		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);

		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

		if(getStation() == null) {
			g.drawRenderedImage(getAllImage().getSnapshot(), null);
		} else {
			g.drawRenderedImage(getWorkingImage().getSnapshot(), null);
		}
	}

	/**
	 * Draw all tiles of active colors
	 * 
	 * @param g
	 *            Graphics context
	 * @throws TilepileException
	 *             See drawColor()
	 * @throws IllegalStateException
	 *             If no active colors set
	 */
	@Override
	protected void showColors(Graphics2D g) throws TilepileException {

		if(numActiveColors() == 0) {
			throw new IllegalStateException("No active colors set!");
		}

		drawColor(g, getBounds().getSize());
	}

	/**
	 * Produce an image with all colors displayed. Get cached images if
	 * possible.
	 * 
	 * @return Image with all colors displayed.
	 * @throws TilepileException
	 *             See drawAll()
	 * @throws IllegalStateException
	 *             If no active colors set
	 */
	private VolatileImage getAllImage() throws TilepileException {

		if(numActiveColors() != 0) {
			throw new IllegalStateException("getAllImage called when colors are active!");
		}

		VolatileImage image = null;

		if(isShowNumbers()) {

			if(allImageNum == null) {

				allImage = graphicsConfiguration.createCompatibleVolatileImage(staticBounds.width, staticBounds.height);

				allImage.setAccelerationPriority(1f);

				Graphics2D graphics = allImageNum.createGraphics();
				drawAll(graphics, staticBounds);
				graphics.dispose();
			}

			image = allImageNum;

		} else {

			if(allImage == null) {

				allImage = graphicsConfiguration.createCompatibleVolatileImage(staticBounds.width, staticBounds.height);

				allImage.setAccelerationPriority(1f);

				Graphics2D graphics = allImage.createGraphics();
				drawAll(graphics, staticBounds);
				graphics.dispose();
			}

			image = allImage;
		}

		return image;
	}

	/**
	 * Set the bounds of this panel. May only be called once.
	 * 
	 * @param staticBounds
	 *            Bounds to set.
	 * @throws IllegalStateException
	 *             If bounds already set.
	 */
	private void setStaticBounds(Dimension staticBounds) {

		if(this.staticBounds != null) {
			throw new IllegalStateException("Bounds already set!");
		}

		this.staticBounds = staticBounds;
	}

	/**
	 * Produce an image with all colors displayed in working mode. Get cached
	 * images if possible.
	 * 
	 * @return Image with all colors displayed in working mode.
	 * @throws TilepileException
	 *             See drawAll()
	 */
	private VolatileImage getWorkingImage() throws TilepileException {

		VolatileImage image = null;

		if(isShowNumbers()) {

			if(workingImageNum == null) {

				workingImage = graphicsConfiguration.createCompatibleVolatileImage(staticBounds.width, staticBounds.height);

				workingImage.setAccelerationPriority(1f);

				Graphics2D graphics = workingImageNum.createGraphics();
				drawAll(graphics, staticBounds);
				graphics.dispose();

			}

			image = workingImageNum;

		} else {

			if(workingImage == null) {

				workingImage = graphicsConfiguration.createCompatibleVolatileImage(staticBounds.width, staticBounds.height);

				workingImage.setAccelerationPriority(1f);

				Graphics2D graphics = workingImage.createGraphics();
				drawAll(graphics, staticBounds);
				graphics.dispose();
			}

			image = workingImage;
		}

		return image;
	}
}
