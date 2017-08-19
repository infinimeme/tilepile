package com.infinimeme.tilepile.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** 
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class StatesFactory {

	public static final String BACKUP_EXTENSION = ".tps";

	//~ Methods ************************************************************************************

	public static final States make(Mural mural) {

		return new States(mural);
	}

	public static final void save(States states) throws IOException {

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(states.getName() + BACKUP_EXTENSION)));

		bw.write(states.getName());
		bw.newLine();

		int[][] tiles = states.getStates();

		bw.write(Integer.toString(tiles.length));
		bw.newLine();

		bw.write(Integer.toString(tiles[0].length));
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
