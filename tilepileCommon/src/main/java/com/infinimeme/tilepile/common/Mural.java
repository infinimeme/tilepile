package com.infinimeme.tilepile.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class Mural implements TilepileObject {

	//~ Static fields/initializers *****************************************************************

	public static final int NO_COLOR = Palette.NON_COLOR_INDEX;

	static final long serialVersionUID = 3808560687392385523L;

	//~ Instance fields ****************************************************************************

	private String name = null;

	private String paletteName = null;

	private Section[][] sections = null;

	private int[][] tiles = null;

	private int gridHeight = 0;

	private int gridWidth = 0;

	private boolean offsetRows = false;

	//~ Constructors *******************************************************************************

	Mural(String name, int gridWidth, int gridHeight, boolean offsetRows, int[][] tiles, Palette palette) {
		setName(name);
		setGridWidth(gridWidth);
		setGridHeight(gridHeight);
		setOffsetRows(offsetRows);
		setTiles(tiles);
		setPaletteName(palette.getName());
	}

	//~ Methods ************************************************************************************

	public int getGridHeight() {
		return gridHeight;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public boolean isOffsetRows() {
		return offsetRows;
	}

	public int getHeight() {
		return tiles.length;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getPaletteName() {
		return paletteName;
	}

	public Section getSection(Location location, int width, int height) throws TilepileException {
		return new Section(location, width, height);
	}

	public Section getSection(Location location) throws TilepileException {
		return new Section(location);
	}

	public Set<Location> getSectionLocationSet() {

		Set<Location> locationSet = new TreeSet<Location>(new LocationComparator());

		int dataWidth = getWidth();
		int dataHeight = getHeight();
		int gridWidth = getGridWidth();
		int gridHeight = getGridHeight();

		int tx = (int) Math.ceil((double) dataWidth / gridWidth);
		int ty = (int) Math.ceil((double) dataHeight / gridHeight);

		Section[][] s = getSections();

		for(int y = 0; y < ty; y++) {

			for(int x = 0; x < tx; x++) {
				locationSet.add(s[y][x].getLocation());
			}
		}

		return locationSet;
	}

	public int getSectionX(int muralX) {
		return muralX / getGridWidth();
	}

	public int getSectionY(int muralY) {
		return muralY / getGridWidth();
	}

	public Section[][] getSections() {

		if(sections == null) {
			initSections();
		}

		return sections;
	}

	public int[][] getTiles() {
		return tiles;
	}

	public int getWidth() {
		return tiles[0].length;
	}

	@Override
	public String toString() {
		return "Mural: " + getName() + " (" + getHeight() + "x" + getWidth() + ") usng palette: " + getPaletteName();
	}

	void setTiles(int[][] tiles) {
		this.tiles = tiles;
	}

	private void setGridHeight(int gridHeight) {
		this.gridHeight = gridHeight;
	}

	private void setGridWidth(int gridWidth) {
		this.gridWidth = gridWidth;
	}

	private void setOffsetRows(boolean offsetRows) {
		this.offsetRows = offsetRows;
	}

	private void setName(String name) {

		if(name == null || name.equals("")) {
			throw new IllegalArgumentException("Mural name can not be blank!");
		}

		this.name = name;
	}

	private void setPaletteName(String paletteName) {
		this.paletteName = paletteName;
	}

	private void initSections() {

		int dataWidth = getWidth();
		int dataHeight = getHeight();
		int gridWidth = getGridWidth();
		int gridHeight = getGridHeight();

		int tx = (int) Math.ceil((double) dataWidth / gridWidth);
		int ty = (int) Math.ceil((double) dataHeight / gridHeight);

		sections = new Section[ty][tx];

		for(int x = 0; x < tx; x++) {

			int muralX = x * gridWidth;

			for(int y = 0; y < ty; y++) {

				int muralY = y * gridHeight;

				sections[y][x] = new Section(new Location(muralX, muralY));
			}
		}
	}

	//~ Inner Classes ******************************************************************************

	public class Counter implements Comparable<Counter> {

		//~ Instance fields ************************************************************************

		private final int color;

		private int count = 0;

		public Counter(int color) {
			super();
			this.color = color;
		}

		//~ Methods ********************************************************************************

		public int getCount() {
			return count;
		}

		public int getColor() {
			return color;
		}

		public void inc() {
			count++;
		}

		public void add(int num) {
			count += num;
		}

		@Override
		public String toString() {
			return Integer.toString(count);
		}

		@Override
		public int compareTo(Counter other) {
			return count > other.count ? 1 : count < other.count ? -1 : color > other.color ? 1 : color < other.color ? -1 : 0;
		}
	}

	public class Section implements Serializable {

		//~ Instance fields ************************************************************************

		private static final long serialVersionUID = 9051704698638277250L;

		private Location location = null;

		private Map<Integer, Counter> histogram = null;
		private Map<Integer, Counter> histogram1 = null;
		private Map<Integer, Counter> histogram2 = null;

		/** TODO: make transient in next version */
		private int[][] colorCache = null;

		private int height = getGridHeight();

		private int precalculatedHash = Integer.MIN_VALUE;

		private int width = getGridWidth();

		//~ Constructors ***************************************************************************

		public Section(Location location) {
			setLocation(location);
			initColorCache();
		}

		public Section(Location location, int width, int height) {
			setLocation(location);
			setWidth(width);
			setHeight(height);
			initColorCache();
		}

		//~ Methods ********************************************************************************

		public final Location getAbsoluteLocation(Location loc) {

			return new Location(getLocation().getX() + loc.getX(), getLocation().getY() + loc.getY());
		}

		public final int getAbsoluteX(int x) {
			return getLocation().getX() + x;
		}

		public final int getAbsoluteY(int y) {
			return getLocation().getY() + y;
		}

		public final void setColorNumber(int x, int y, int color) {

			colorCache[x][y] = color;

			int absoluteX = getAbsoluteX(x);

			if(absoluteX >= getMural().getWidth()) {
				throw new IllegalArgumentException("Attempted to set color number " + color + " at location " + x + "," + y);
			}

			int absoluteY = getAbsoluteY(y);

			if(absoluteY >= getMural().getHeight()) {
				throw new IllegalArgumentException("Attempted to set color number " + color + " at location " + x + "," + y);
			}

			int[][] tiles = getTiles();

			tiles[absoluteY][absoluteX] = color;
		}

		public final int getColorNumber(int x, int y) {

			int color = colorCache[x][y];

			if(color == NO_COLOR) {

				int absoluteX = getAbsoluteX(x);

				if(absoluteX >= getMural().getWidth()) {

					return NO_COLOR;
				}

				int absoluteY = getAbsoluteY(y);

				if(absoluteY >= getMural().getHeight()) {

					return NO_COLOR;
				}

				int[][] tiles = getTiles();

				color = tiles[absoluteY][absoluteX];

				colorCache[x][y] = color;
			}

			return color;
		}

		public int getHeight() {
			return height;
		}

		public Map<Integer, Counter> getHistogram() {

			if(histogram == null) {
				calcHistograms();
			}

			return histogram;
		}

		public Map<Integer, Counter> getHistogram1() {

			if(histogram1 == null) {
				calcHistograms();
			}

			return histogram1;
		}

		public Map<Integer, Counter> getHistogram2() {

			if(histogram2 == null) {
				calcHistograms();
			}

			return histogram2;
		}

		public Location getLocation() {
			return location;
		}

		public Mural getMural() {
			return Mural.this;
		}

		public int getMuralX() {
			return getLocation().getX();
		}

		public int getMuralY() {
			return getLocation().getY();
		}

		public int getWidth() {
			return width;
		}

		@Override
		public int hashCode() {

			if(precalculatedHash == Integer.MIN_VALUE) {

				int locationCode = getMuralX();
				locationCode <<= 16;
				locationCode ^= getMuralY();

				int tilesCode = 1;

				for(int x = 0; x < getWidth(); x++) {

					for(int y = 0; y < getHeight(); y++) {
						tilesCode *= getColorNumber(x, y);
						tilesCode >>>= 1;
					}
				}

				precalculatedHash = tilesCode ^ locationCode;
			}

			return precalculatedHash;
		}

		private void setHeight(int height) {
			this.height = height;
		}

		private void setLocation(Location location) {
			this.location = location;
		}

		private void setWidth(int width) {
			this.width = width;
		}

		public void calcHistograms() {

			Map<Integer, Counter> histRaw = new HashMap<Integer, Counter>();
			Map<Integer, Counter> hist1 = new HashMap<Integer, Counter>();
			Map<Integer, Counter> hist2 = new HashMap<Integer, Counter>();

			final int h = getHeight(), w = getWidth();

			for(int y = 0; y < h; y++) {

				int runColor = 0;
				int runSize = 0;
				int lastColorNumber = 0;

				for(int x = 0; x < w; x++) {

					int colorNumber = getColorNumber(x, y);

					{
						Integer key = new Integer(colorNumber);
						Counter counter = histRaw.get(key);

						if(counter == null) {
							counter = new Counter(key);
							histRaw.put(key, counter);
						}

						counter.inc();
					}

					if(x == 0) {
						runSize = 1;
						runColor = colorNumber;
					} else {
						if(colorNumber == lastColorNumber) {
							runSize++;
						} else {
							int doubleSize = runSize / 2;
							int singleSize = runSize % 2;
							Integer key = new Integer(lastColorNumber);
							if(singleSize > 0) {
								Counter counter = hist1.get(key);

								if(counter == null) {
									counter = new Counter(key);
									hist1.put(key, counter);
								}

								counter.add(singleSize);
							}
							if(doubleSize > 0) {
								Counter counter = hist2.get(key);

								if(counter == null) {
									counter = new Counter(key);
									hist2.put(key, counter);
								}

								counter.add(doubleSize);
							}
							runSize = 1;
							runColor = colorNumber;
						}
					}

					lastColorNumber = colorNumber;
				}

				int doubleSize = runSize / 2;
				int singleSize = runSize % 2;
				Integer key = new Integer(lastColorNumber);
				if(singleSize > 0) {
					Counter counter = hist1.get(key);

					if(counter == null) {
						counter = new Counter(key);
						hist1.put(key, counter);
					}

					counter.add(singleSize);
				}
				if(doubleSize > 0) {
					Counter counter = hist2.get(key);

					if(counter == null) {
						counter = new Counter(key);
						hist2.put(key, counter);
					}

					counter.add(doubleSize);
				}
			}

			histogram = histRaw;
			histogram1 = hist1;
			histogram2 = hist2;
		}

		private void initColorCache() {

			colorCache = new int[getWidth()][getHeight()];

			for(int[] element : colorCache) {
				Arrays.fill(element, NO_COLOR);
			}
		}

		@Override
		public String toString() {
			return "Section " + getLocation();
		}
	}
}
