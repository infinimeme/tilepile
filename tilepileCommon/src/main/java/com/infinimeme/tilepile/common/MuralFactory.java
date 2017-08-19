package com.infinimeme.tilepile.common;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import com.infinimeme.tilepile.common.Palette.PaletteColor;
import com.infinimeme.tilepile.data.DataManagerRemote;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class MuralFactory {

	//~ Static fields/initializers *****************************************************************

	public static final String BACKUP_EXTENSION = ".tpm";

	//~ Methods ************************************************************************************

	public static final Mural make(String name, int gridWidth, int gridHeight, boolean offsetRows, BufferedImage originalImage, Palette palette, double muralHeight, double tileSize, double groutSize)
	throws IOException, TilepileException {

		double scale = muralHeight / (tileSize + groutSize) / originalImage.getHeight();

		AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale), AffineTransformOp.TYPE_BILINEAR);

		BufferedImage transformedImage = op.createCompatibleDestImage(originalImage, palette.getColorModel());

		transformedImage = op.filter(originalImage, transformedImage);

		Raster raster = transformedImage.getRaster();

		final int width = transformedImage.getWidth();
		final int height = transformedImage.getHeight();

		int[][] tiles = new int[height][width];

		int[] pixel = new int[1];

		Set<Integer> indexSet = new TreeSet<Integer>(palette.getColors().keySet());

		for(int y = 0; y < height; y++) {

			for(int x = 0; x < width; x++) {
				raster.getPixel(x, y, pixel);

				int b = pixel[0];

				if(indexSet.contains(new Integer(b))) {
					tiles[y][x] = b;
				} else {
					tiles[y][x] = palette.getBlack();
				}
			}
		}

		return new Mural(name, gridWidth, gridHeight, offsetRows, tiles, palette);
	}

	/**
	 * Pixel-to-tile mural creation
	 **/
	public static final Mural make(String name, int gridWidth, int gridHeight, boolean offsetRows, BufferedImage originalImage, Palette palette, boolean useBackupColors) throws IOException,
	TilepileException {

		Raster raster = originalImage.getRaster();

		if(!palette.getColorModel().isCompatibleRaster(raster)) {
			throw new TilepileException("Incompatible color model in import image!");
		}

		IndexColorModel imageCM = (IndexColorModel) originalImage.getColorModel();

		if(imageCM.getMapSize() != palette.getSize()) {

			int option = JOptionPane.showConfirmDialog(null, "Wrong number of colors in image: " + imageCM.getMapSize() + " Should be " + palette.getSize() + ". Proceed?");

			if(option != JOptionPane.YES_OPTION) {

				return null;
			}
		}

		int[] imageColors = new int[imageCM.getMapSize()];
		imageCM.getRGBs(imageColors);

		int[] paletteImageMap = new int[imageCM.getMapSize()];
		Arrays.fill(paletteImageMap, Palette.NON_COLOR_INDEX);

		List<String> mapMessages = new LinkedList<String>();
		mapMessages.add("Image index color to palette mapping");

		boolean allMatched = true;
		List<String> matchedMessages = new LinkedList<String>();
		matchedMessages.add("Colors not present in tilepile palette");

		Map<Integer, PaletteColor> index2pc = palette.getColors();

		//Check only the first palette.getSize() colors...
		for(int i = 0; i < imageColors.length; i++) {

			Color imageColor = new Color(imageColors[i]);

			boolean matchFound = false;

			for(Palette.PaletteColor pc : index2pc.values()) {

				if(pc.getColor().equals(imageColor)) {
					paletteImageMap[i] = pc.getIndex();
					matchFound = true;
					mapMessages.add("Image color index " + i + " to palette color " + paletteImageMap[i] + " (" + pc.getName() + ")");

					break;
				}
			}

			if(!matchFound) {
				allMatched = false;
				matchedMessages.add("Index " + i + ": " + imageColor);
			}
		}

		if(!allMatched) {
			JOptionPane.showMessageDialog(null, new javax.swing.JComboBox(matchedMessages.toArray()), "Color Mismatch!", JOptionPane.WARNING_MESSAGE);
		}

		JOptionPane.showMessageDialog(null, new javax.swing.JComboBox(mapMessages.toArray()), "Here's how the colors match", JOptionPane.INFORMATION_MESSAGE);

		final int width = originalImage.getWidth();
		final int height = originalImage.getHeight();

		int[][] tiles = new int[height][width];

		int[] pixel = new int[1];

		for(int y = 0; y < height; y++) {

			for(int x = 0; x < width; x++) {
				raster.getPixel(x, y, pixel);

				tiles[y][x] = paletteImageMap[pixel[0]];
			}
		}

		if(useBackupColors) {

			class Counter {
				int count = 0;
			}

			Map<Integer, Counter> counts = new HashMap<Integer, Counter>();

			for(Palette.PaletteColor pc : index2pc.values()) {
				counts.put(pc.getIndex(), new Counter());
			}

			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					counts.get(tiles[y][x]).count++;
				}
			}

			for(Palette.PaletteColor pc : index2pc.values()) {
				Counter count = counts.get(pc.getIndex());
				if(count.count > pc.getCountInStock()) {
					System.err.println("Oooooooooh, boy: " + count.count + " > " + pc.getCountInStock() + " for " + pc.getIndex());
				}
			}
		}

		return new Mural(name, gridWidth, gridHeight, offsetRows, tiles, palette);
	}

	public static final Mural make(File dataFile, DataManagerRemote dataManager) throws TilepileException, IOException {

		BufferedReader br = new BufferedReader(new FileReader(dataFile));

		String name = br.readLine();

		String paletteName = br.readLine();

		Palette palette = dataManager.getPalette(paletteName);

		if(palette == null) {
			throw new TilepileException("No palette named " + paletteName);
		}

		int height = Integer.parseInt(br.readLine());
		/*int width = */Integer.parseInt(br.readLine());
		int gridWidth = Integer.parseInt(br.readLine());
		int gridHeight = Integer.parseInt(br.readLine());
		boolean offsetRows = Boolean.parseBoolean(br.readLine());

		int[][] tiles = new int[height][];

		int index = 0;
		String line = null;

		while((line = br.readLine()) != null && !"".equals(line)) {

			String[] data = line.split("\t");
			tiles[index] = new int[data.length];

			for(int i = 0; i < data.length; i++) {
				tiles[index][i] = Integer.parseInt(data[i]);
			}

			index++;
		}

		br.close();

		return new Mural(name, gridWidth, gridHeight, offsetRows, tiles, palette);

	}

	public static final Mural make(String name, int gridWidth, int gridHeight, boolean offsetRows, File dataFile, Palette palette) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(dataFile));

		List<int[]> dataLines = new LinkedList<int[]>();

		String line = null;

		int lineWidth = Integer.MIN_VALUE;
		int lineNumber = 0;

		while((line = br.readLine()) != null && !"".equals(line)) {

			String[] fields = line.split(",");
			int[] dataLine = new int[fields.length];

			int currentWidth = fields.length;

			if(!(lineWidth == Integer.MIN_VALUE || lineWidth == currentWidth)) {
				throw new IllegalStateException("Uneven line widths at line " + lineNumber + " in file " + dataFile + ". Width is " + currentWidth + " and should be " + lineWidth + "!");
			}

			lineWidth = currentWidth;

			for(int i = 0; i < fields.length; i++) {
				dataLine[i] = Integer.parseInt(fields[i].trim());
			}

			dataLines.add(dataLine);
			lineNumber++;
		}

		int[][] tiles = new int[lineWidth][dataLines.size()];

		int y = 0;

		for(int[] lineData : dataLines) {

			for(int x = 0; x < lineData.length; x++) {
				tiles[y][x] = lineData[x];
			}

			y++;
		}

		return new Mural(name, gridWidth, gridHeight, offsetRows, tiles, palette);
	}

	public static final Mural replace(Mural mural, BufferedImage originalImage, Palette palette) throws IOException, TilepileException {

		Raster raster = originalImage.getRaster();

		if(!palette.getColorModel().isCompatibleRaster(raster)) {
			throw new TilepileException("Incompatible color model in import image!");
		}

		IndexColorModel imageCM = (IndexColorModel) originalImage.getColorModel();

		int[] imageColors = new int[imageCM.getMapSize()];

		StringBuffer messageBuffer = new StringBuffer();

		boolean mismatch = false;

		for(int i = 0; i < imageColors.length; i++) {
			TilepileUtils.logInfo("Comparing colors at index " + i);

			Color paletteColor = palette.getColor(i);
			Color imageColor = new Color(imageColors[i]);

			if(!paletteColor.equals(imageColor)) {
				mismatch = true;

				messageBuffer.append("Color mismatch at index " + i);
				messageBuffer.append('\n');
				messageBuffer.append("Palette '" + palette.getName(i) + "' : " + paletteColor);
				messageBuffer.append('\n');
				messageBuffer.append("Image: " + imageColor);
				messageBuffer.append('\n');
			}
		}

		if(mismatch) {
			throw new TilepileException(messageBuffer.toString());
		}

		final int width = originalImage.getWidth();
		final int height = originalImage.getHeight();

		if(width != mural.getWidth()) {
			throw new TilepileException("Incompatible width in import image!");
		}

		if(height != mural.getHeight()) {
			throw new TilepileException("Incompatible height in import image!");
		}

		int[][] tiles = new int[height][width];

		int[] pixel = new int[1];

		Set<Integer> indexSet = new TreeSet<Integer>(palette.getColors().keySet());

		for(int y = 0; y < height; y++) {

			for(int x = 0; x < width; x++) {
				raster.getPixel(x, y, pixel);

				if(indexSet.contains(new Integer(pixel[0]))) {
					tiles[y][x] = pixel[0];
				} else {
					tiles[y][x] = palette.getBlack();
				}
			}
		}

		return new Mural(mural.getName(), mural.getGridWidth(), mural.getGridHeight(), mural.isOffsetRows(), tiles, palette);
	}

	public static final void save(Mural mural) throws IOException {

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(mural.getName() + BACKUP_EXTENSION)));

		bw.write(mural.getName());
		bw.newLine();

		bw.write(mural.getPaletteName());
		bw.newLine();

		int[][] tiles = mural.getTiles();

		bw.write(Integer.toString(tiles.length));
		bw.newLine();

		bw.write(Integer.toString(tiles[0].length));
		bw.newLine();

		bw.write(Integer.toString(mural.getGridWidth()));
		bw.newLine();

		bw.write(Integer.toString(mural.getGridHeight()));
		bw.newLine();

		bw.write(Boolean.toString(mural.isOffsetRows()));
		bw.newLine();

		for(int[] tile : tiles) {

			for(int element : tile) {
				bw.write(Integer.toString(element));
				bw.write("\t");
			}
			bw.newLine();
		}

		bw.flush();
		bw.close();
	}
}
