package com.infinimeme.tilepile.common;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 */
public abstract class AbstractSectionPanel extends JPanel {

	// ~ Static fields/initializers
	// *****************************************************************

	private static final long serialVersionUID = -1813158242200424232L;

	/** Active color */
	public static final Color ACTIVE = Color.WHITE;

	/** Inactive color */
	public static final Color INACTIVE = Color.BLACK;

	/** Background color */
	public static final Color BACKGROUND = Color.BLACK;

	/**
	 * Map (cache) between color numbers and TextLayout objects.
	 * 
	 * @see drawNumber(String, Color, Graphics2D, Font font, double, double,
	 *      double, double height)
	 */
	private static final Map<String, TextLayout> numTextLayoutMap = new TreeMap<String, TextLayout>();

	// ~ Instance fields
	// ****************************************************************************

	/** Flag for whether numbers are displayed */
	boolean showNumbers = false;

	/** Palette for the section displayed */
	private Palette palette = null;

	/** The Mural section displayed */
	private Mural.Section section = null;

	/** Bit array for which colors are "active" */
	private boolean[] activeColors = null;

	/** Flag for whether active states are inverted. */
	private boolean inverted = false;

	/** Flag for whether the "no color" color is active */
	private boolean nonColorActive = false;

	/** Flag for whether the grid is displayed between tiles */
	private boolean withGrid = false;

	// ~ Constructors
	// *******************************************************************************

	/**
	 * Creates a new AbstractSectionPanel object.
	 * 
	 * @param section
	 *            Mural.Section to be displayed
	 * @param palette
	 *            Palette backing the section
	 */
	public AbstractSectionPanel(Mural.Section section, Palette palette) {
		super();
		setPalette(palette);
		setSection(section);
		setBackground(BACKGROUND);
	}

	/**
	 * Creates a new AbstractSectionPanel object.
	 * 
	 * @param section
	 *            Mural.Section to be displayed
	 * @param palette
	 *            Palette backing the section
	 * @param withGrid
	 *            Display section colors with grid
	 */
	public AbstractSectionPanel(Mural.Section section, Palette palette, boolean withGrid) {
		this(section, palette);
		setWithGrid(withGrid);
	}

	// ~ Methods
	// ************************************************************************************

	/**
	 * Check whether a given color is active.
	 * 
	 * @param color
	 *            Color to check
	 * @return Whether given color is active.
	 * @throws IllegalArgumentException
	 *             A color is input that's out of range.
	 */
	public final boolean isActiveColor(int color) {

		if(color == Palette.NON_COLOR_INDEX) {

			return nonColorActive;
		}

		if(color < 0 || color >= activeColors.length) {
			throw new IllegalArgumentException("Illegal color " + color);
		}

		return activeColors[color];
	}

	/**
	 * Set flag for whether active states are inverted.
	 * 
	 * @param inverted
	 *            Flag for whether active states are inverted.
	 */
	public final void setInverted(boolean inverted) {
		this.inverted = inverted;
	}

	/**
	 * Get flag for whether active states are inverted.
	 * 
	 * @return Flag for whether active states are inverted.
	 */
	public final boolean getInverted() {

		return inverted;
	}

	/**
	 * Get flag for whether active states are inverted.
	 * 
	 * @return Flag for whether active states are inverted.
	 */
	public final boolean isInverted() {

		return inverted;
	}

	/**
	 * Get Palette for the section displayed.
	 * 
	 * @return Palette for the section displayed
	 */
	public final Palette getPalette() {

		return palette;
	}

	/**
	 * Get the Mural section displayed.
	 * 
	 * @return The Mural section displayed
	 */
	public final Mural.Section getSection() {

		return section;
	}

	/**
	 * Set flag for whether numbers are displayed.
	 * 
	 * @param showNumbers
	 *            Flag for whether numbers are displayed
	 */
	public final void setShowNumbers(boolean showNumbers) {
		this.showNumbers = showNumbers;
	}

	/**
	 * Get flag for whether numbers are displayed.
	 * 
	 * @return Flag for whether numbers are displayed
	 */
	public final boolean getShowNumbers() {

		return showNumbers;
	}

	/**
	 * Get flag for whether numbers are displayed.
	 * 
	 * @return Flag for whether numbers are displayed
	 */
	public final boolean isShowNumbers() {

		return showNumbers;
	}

	/**
	 * Get flag for whether the grid is displayed between tiles.
	 * 
	 * @return Flag for whether the grid is displayed between tiles
	 */
	public final boolean getWithGrid() {

		return withGrid;
	}

	/**
	 * Get flag for whether the grid is displayed between tiles.
	 * 
	 * @return Flag for whether the grid is displayed between tiles
	 */
	public final boolean isWithGrid() {

		return withGrid;
	}

	/**
	 * Add a color to list of active colors.
	 * 
	 * @param color
	 *            Color to add.
	 * @throws IllegalArgumentException
	 *             A color is input that's out of range.
	 */
	public final void addActiveColor(int color) {

		if(color == Palette.NON_COLOR_INDEX) {
			nonColorActive = true;
		} else {

			if(color < 0 || color >= activeColors.length) {
				throw new IllegalArgumentException("Illegal color " + color);
			}

			activeColors[color] = true;
		}
	}

	/**
	 * Test for equality.
	 * 
	 * @param o
	 *            Object to test for equality.
	 * @return true if equals
	 */
	@Override
	public boolean equals(Object o) {

		if(!(o instanceof AbstractSectionPanel)) {

			return false;
		}

		return hashCode() == o.hashCode();
	}

	/**
	 * Get panels's hash code.
	 * 
	 * @return Panel's hash code.
	 */
	@Override
	public int hashCode() {

		return getSection().hashCode();
	}

	/**
	 * Get number of active colors.
	 * 
	*/
	public final int numActiveColors() {

		int count = nonColorActive ? 1 : 0;

		for(boolean activeColor : activeColors) {

			if(activeColor) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Remove a color from list of active colors.
	 * 
	 * @param color
	 *            Color to remove
	 * @throws IllegalArgumentException
	 *             A color is input that's out of range.
	 */
	public final void removeActiveColor(int color) {

		if(color == Palette.NON_COLOR_INDEX) {
			nonColorActive = false;
		} else {

			if(color < 0 || color >= activeColors.length) {
				throw new IllegalArgumentException("Illegal color " + color);
			}

			activeColors[color] = false;
		}
	}

	/**
	 * Remove all active colors.
	 */
	public final void resetActiveColors() {
		nonColorActive = false;
		activeColors = new boolean[palette.getSize()];
	}

	/**
	 * Draw the all colors of the section to the graphics context within the
	 * bounds provided.
	 * 
	 * @param g
	 *            Graphics context
	 * @param bounds
	 *            Bounds within which to draw section
	 * @throws TilepileException
	 *             Incorrect color numbers retrieved from section.
	 */
	protected void drawAll(Graphics2D g, Dimension bounds) throws TilepileException {

		// No nice rendering needed when drawing tiles. :)

		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);

		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

		Mural.Section section = getSection();

		// Calculate the height of each tile on the screen
		float tileHeight = (float) (bounds.getHeight() / section.getHeight());

		// Calculate the width of each tile on the screen
		float tileWidth = (float) (bounds.getWidth() / section.getWidth());

		float gridHeight = 0.0f;
		float gridWidth = 0.0f;

		// Calculate grid spacing
		if(isWithGrid()) {
			gridHeight = tileHeight / 64;
			gridWidth = tileWidth / 64;
		}

		// Create one rectangle object and reuse it for each tile
		Rectangle2D.Float tile = new Rectangle2D.Float(0.0f, 0.0f, 0.0f, 0.0f);

		// Assign height and width to tile
		tile.height = tileHeight - 2 * gridHeight;
		tile.width = tileWidth - 2 * gridWidth;

		//Bring multiplication out of inner loop
		float[] lefts = new float[section.getWidth()];
		float[] xs = new float[section.getWidth()];
		for(int x = 0; x < section.getWidth(); x++) {
			// Calculate tile X positions
			lefts[x] = x * tileWidth;
			xs[x] = lefts[x] + gridWidth;
		}

		boolean offsetRows = getSection().getMural().isOffsetRows();

		for(int y = 0; y < section.getHeight(); y++) {

			// Calculate tile Y position
			float top = y * tileHeight;

			// Assign Y to tile
			tile.y = top + gridHeight;

			boolean offset = offsetRows && y % 2 == 0;

			for(int x = 0; x < section.getWidth(); x++) {

				int colorNum = section.getColorNumber(x, y);
				Color c = palette.getColor(colorNum);

				// Set painting color
				g.setColor(c);

				// Assign X to tile
				tile.x = xs[x];

				if(offset) {
					tile.x += tile.width / 2;
				}

				// Paint
				g.fill(tile);

				// If numbers are to be shown, do it
				if(getShowNumbers()) {

					// Get color number string
					String name = palette.getName(colorNum);

					// Draw it
					//drawNumber(name, c, g, makeNumFont(tileWidth, tileHeight), lefts[x], top, tileWidth, tileHeight);
					drawNumber(name, c, g, makeNumFont(tileWidth, tileHeight), tile.x, tile.y, tileWidth, tileHeight);
				}
			}
		}
	}

	/**
	 * Draw only the active colors of the section to the graphics context within
	 * the bounds provided.
	 * 
	 * @param g
	 *            Graphics context
	 * @param bounds
	 *            Bounds within which to draw section
	 * @throws TilepileException
	 *             Incorrect color numbers retrieved from section.
	 */
	protected void drawColor(Graphics2D g, Dimension bounds) throws TilepileException {

		// No nice rendering needed when drawing tiles. :)

		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);

		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

		//g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		Mural.Section section = getSection();

		// Calculate the height of each tile on the screen
		float tileHeight = (float) (bounds.getHeight() / section.getHeight());

		// Calculate the width of each tile on the screen
		float tileWidth = (float) (bounds.getWidth() / section.getWidth());

		float gridHeight = 0.0f;
		float gridWidth = 0.0f;

		// Calculate grid spacing
		if(isWithGrid()) {
			gridHeight = tileHeight / 64;
			gridWidth = tileWidth / 64;
		}

		// Create one rectangle object and reuse it for each tile
		Rectangle2D.Float tile = new Rectangle2D.Float(0.0f, 0.0f, 0.0f, 0.0f);

		// Assign height and width to tile
		tile.height = tileHeight - 2 * gridHeight;
		tile.width = tileWidth - 2 * gridWidth;

		//Bring multiplication out of inner loop
		float[] lefts = new float[section.getWidth()];
		float[] xs = new float[section.getWidth()];
		for(int x = 0; x < section.getWidth(); x++) {
			// Calculate tile X positions
			lefts[x] = x * tileWidth;
			xs[x] = lefts[x] + gridWidth;
		}

		boolean offsetRows = getSection().getMural().isOffsetRows();

		for(int y = 0; y < section.getHeight(); y++) {

			// Calculate tile Y position
			float top = y * tileHeight;

			// Assign Y to tile
			tile.y = top + gridHeight;

			boolean offset = offsetRows && y % 2 == 0;

			for(int x = 0; x < section.getWidth(); x++) {

				Color c = null;

				// Get color number for this coordinate
				int colorNumber = section.getColorNumber(x, y);

				// Check if it's active
				boolean active = isActiveColor(colorNumber);

				// Determine painting color
				if(active) {

					if(inverted) {
						c = INACTIVE;
					} else {
						c = ACTIVE;
					}
				} else {

					if(inverted) {
						c = ACTIVE;
					} else {
						c = INACTIVE;
					}
				}

				// Get painting color
				g.setColor(c);

				// Assign X to tile
				tile.x = xs[x];

				if(offset) {
					tile.x += tile.width / 2;
				}

				// Paint
				g.fill(tile);

				// If numbers are to be shown and on an active color, do it
				if(active && getShowNumbers()) {

					// Get color number string
					String name = palette.getName(section.getColorNumber(x, y));

					// Draw it
					//drawNumber(name, c, g, makeNumFont(tileWidth, tileHeight), lefts[x], top, tileWidth, tileHeight);
					drawNumber(name, c, g, makeNumFont(tileWidth, tileHeight), tile.x, tile.y, tileWidth, tileHeight);
				}
			}
		}
	}

	/**
	 * Redraw one tile within a section.
	 * 
	 * @param g
	 *            Graphics context
	 * @param bounds
	 *            Bounds within which to draw section
	 * @param sectionX
	 *            X location in the section to draw
	 * @param sectionY
	 *            Y location in the section to draw
	 * @throws TilepileException
	 *             Incorrect color numbers retrieved from section.
	 */
	protected void drawLocation(Graphics2D g, Dimension bounds, int sectionX, int sectionY) throws TilepileException {

		Mural.Section section = getSection();

		// Calculate the height of the tile on the screen
		float tileHeight = (float) (bounds.getHeight() / section.getHeight());

		// Calculate the width of the tile on the screen
		float tileWidth = (float) (bounds.getWidth() / section.getWidth());

		float gridHeight = 0.0f;
		float gridWidth = 0.0f;

		// Calculate grid spacing
		if(isWithGrid()) {
			gridHeight = tileHeight / 64;
			gridWidth = tileWidth / 64;
		}

		// Create one rectangle object and reuse it for each tile
		Rectangle2D.Float tile = new Rectangle2D.Float(0.0f, 0.0f, 0.0f, 0.0f);

		// Calculate tile Y position
		float top = sectionY * tileHeight;

		// Calculate tile X position
		float left = sectionX * tileWidth;

		Color c = palette.getColor(section.getColorNumber(sectionX, sectionY));

		// Set painting color
		g.setColor(c);

		// No antialiasing needed when drawing tiles. :)
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		// Assign X and Y to tile
		tile.y = top + gridHeight;
		tile.x = left + gridWidth;

		boolean offsetRows = getSection().getMural().isOffsetRows();

		if(offsetRows && sectionY % 2 == 0) {
			tile.x += tile.width / 2;
		}

		// Assign width and height to tile
		tile.height = tileHeight - 2 * gridHeight;
		tile.width = tileWidth - 2 * gridWidth;

		// Paint
		g.fill(tile);

		// If numbers are to be shown, do it
		if(getShowNumbers()) {

			// Get color number string
			String name = palette.getName(section.getColorNumber(sectionX, sectionY));

			// Draw it
			//drawNumber(name, c, g, makeNumFont(tileWidth, tileHeight), left, top, tileWidth, tileHeight);
			drawNumber(name, c, g, makeNumFont(tileWidth, tileHeight), tile.x, tile.y, tileWidth, tileHeight);

		}
	}

	/**
	 * Draw all tiles in color
	 * 
	 * @param g
	 *            Graphics context
	 */
	protected abstract void showAll(Graphics2D g) throws TilepileException;

	/**
	 * Draw all tiles of active colors
	 * 
	 * @param g
	 *            Graphics context
	 */
	protected abstract void showColors(Graphics2D g) throws TilepileException;

	/**
	 * Set Palette for the section displayed.
	 * 
	 * @param palette
	 *            Palette for the section displayed
	 */
	private void setPalette(Palette palette) {
		this.palette = palette;
	}

	/**
	 * Set the Mural section displayed.
	 * 
	 * @param section
	 *            The Mural section displayed
	 */
	private void setSection(Mural.Section section) {
		this.section = section;

		resetActiveColors();

		repaint();
	}

	/**
	 * Set flag for whether the grid is displayed between tiles
	 * 
	 * @param withGrid
	 *            Flag for whether the grid is displayed between tiles
	 */
	private void setWithGrid(boolean withGrid) {
		this.withGrid = withGrid;
	}

	/**
	 * Paint the panel to the graphics context.
	 * 
	 * @param g
	 *            Graphics context
	 */
	@Override
	public void paint(Graphics g) {

		// Clear panel
		g.setColor(Palette.NON_COLOR_APPEARANCE);
		g.fillRect(0, 0, getWidth(), getHeight());

		try {

			if(numActiveColors() == 0) {

				// If no active colors, show all
				showAll((Graphics2D) g);
			} else {

				// Otherwise, show active colors
				showColors((Graphics2D) g);
			}
		} catch(TilepileException te) {
			TilepileUtils.exceptionReport(te);
		}
	}

	/**
	 * Draw a color number within a tile
	 * 
	 * @param name
	 *            Color name
	 * @param color
	 *            Tile color
	 * @param g
	 *            Graphics context
	 * @param font
	 *            Font to use when drawing name
	 * @param x
	 *            X coordinate of tile on screen
	 * @param y
	 *            Y coordinate of tile on screen
	 * @param width
	 *            Width of tile on screen
	 * @param height
	 *            Height of tile on screen
	 */
	private static final void drawNumber(String name, Color color, Graphics2D g, Font font, double x, double y, double width, double height) {

		TextLayout tl = null;

		String num = name.substring(0, Math.min(name.length(), 5));

		if(numTextLayoutMap.containsKey(num)) {
			tl = numTextLayoutMap.get(num);
		} else {

			FontRenderContext frc = g.getFontRenderContext();

			AttributedString as = new AttributedString(num);
			as.addAttribute(TextAttribute.FONT, font, 0, num.length());

			AttributedCharacterIterator aci = as.getIterator();

			tl = new TextLayout(aci, frc);

			numTextLayoutMap.put(num, tl);
		}

		Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(x + width / 2 - num.length() * font.getSize2D() / 3, y + height / 2 + font.getSize2D() / 3));

		// Set number text color depending on brightness
		g.setColor(TilepileUtils.getContrasting(color));

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setStroke(new BasicStroke(1.5f));
		g.fill(sha);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	/**
	 * Make a font for drawing color number
	 * 
	 * @param width
	 *            Width within which to make font
	 * @param height
	 *            Height within which to make font
	 * @return Font for drawing color number
	 */
	private static final Font makeNumFont(double width, double height) {

		// Make a font that is guaranteed to fit within the height and width
		// and look "good"
		return new Font("monospaced", Font.PLAIN, (int) (Math.min(width, height) / 2.4));
	}
}
