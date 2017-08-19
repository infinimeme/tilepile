package com.infinimeme.tilepile.data;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.infinimeme.tilepile.common.MainStation;
import com.infinimeme.tilepile.common.Mural;
import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.States;
import com.infinimeme.tilepile.common.Station;
import com.infinimeme.tilepile.common.TilepileObject;
import com.infinimeme.tilepile.common.TilepileUtils;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 */
public class DataManager extends UnicastRemoteObject implements DataConstants, DataManagerRemote {

	// ~ Static fields/initializers
	// *****************************************************************

	private static final long serialVersionUID = -6185854062232657260L;

	private static final FileFilter DATA_OBJECT_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(DATA_FILE_EXTENSION) && pathname.isFile();
		}
	};

	private static DataManager INSTANCE = null;

	// ~ Instance fields
	// ****************************************************************************

	/** Backing store for Mural objects. */
	private Map<String, Mural> murals = new TreeMap<String, Mural>();

	private Map<String, Palette> palettes = new TreeMap<String, Palette>();

	private Map<String, States> states = new TreeMap<String, States>();

	private Map<String, Station> stations = new TreeMap<String, Station>();

	private Map<String, MainStation> mainStations = new TreeMap<String, MainStation>();

	// ~ Constructors
	// *******************************************************************************

	private final void load(final File dataDir) {

		class Loader<T extends TilepileObject> {
			public void load(PersistentType type, Class<T> clazz, Map<String, T> map) {
				File dir = new File(dataDir, type.getType().getKeyPrefix());

				if(dir.exists() && dir.isDirectory()) {

					File[] members = dir.listFiles(DATA_OBJECT_FILE_FILTER);

					for(File member : members) {

						try {
							TilepileUtils.getLogger().info("Loading " + member.getAbsolutePath());
							T object = DataUtils.readFile(member, clazz);

							TilepileUtils.getLogger().info("Loading " + type + " with " + object);

							map.put(object.getName(), object);

						} catch(ClassNotFoundException cnfe) {
							TilepileUtils.exceptionReport(cnfe);
						} catch(IOException ioe) {
							TilepileUtils.exceptionReport(ioe);
						}
					}
				}
			}
		}
		new Loader<Mural>().load(PersistentType.MURAL, Mural.class, murals);
		new Loader<Palette>().load(PersistentType.PALETTE, Palette.class, palettes);
		new Loader<States>().load(PersistentType.STATES, States.class, states);
	}

	/**
	 * Creates a new DataManager object.
	 * 
	 * @param persistent
	*/
	private DataManager() throws RemoteException {

		super();

		DATA_DIRECTORY.mkdirs();

		// Init object categories
		for(PersistentType type : PersistentType.values()) {
			new File(DATA_DIRECTORY, type.getType().getKeyPrefix()).mkdirs();
		}

		load(DATA_DIRECTORY);

	}

	// ~ Methods
	// ************************************************************************************

	static final DataManager getInstance() throws RemoteException {

		if(INSTANCE == null) {
			INSTANCE = new DataManager();
		}

		return INSTANCE;
	}

	@Override
	public final MainStation getMainStation(String name) {

		return mainStations.get(name);
	}

	@Override
	public final Mural getMural(String name) {

		return murals.get(name);
	}

	@Override
	public final Palette getPalette(String name) {

		return palettes.get(name);
	}

	@Override
	public final States getStates(String name) {

		return states.get(name);
	}

	@Override
	public final Station getStation(String name) {

		return stations.get(name);
	}

	@Override
	public final boolean containsMainStation(String name) {

		return mainStations.containsKey(name);
	}

	@Override
	public final boolean containsMural(String name) {

		return murals.containsKey(name);
	}

	@Override
	public final boolean containsPalette(String name) {

		return palettes.containsKey(name);
	}

	@Override
	public final boolean containsStates(String name) {

		return states.containsKey(name);
	}

	@Override
	public final boolean containsStation(String name) {

		return stations.containsKey(name);
	}

	@Override
	public final Collection<MainStation> instancesOfMainStation() {
		return new LinkedList<MainStation>(mainStations.values());
	}

	@Override
	public final Collection<Mural> instancesOfMural() {
		return new LinkedList<Mural>(murals.values());
	}

	@Override
	public final Collection<Palette> instancesOfPalette() {
		return new LinkedList<Palette>(palettes.values());
	}

	@Override
	public final Collection<States> instancesOfStates() {
		return new LinkedList<States>(states.values());
	}

	@Override
	public final Collection<Station> instancesOfStation() {
		return new LinkedList<Station>(stations.values());
	}

	@Override
	public final Set<String> namesOfMainStation() {
		return new TreeSet<String>(mainStations.keySet());
	}

	@Override
	public final Set<String> namesOfMural() {
		return new TreeSet<String>(murals.keySet());
	}

	@Override
	public final Set<String> namesOfPalette() {
		return new TreeSet<String>(palettes.keySet());
	}

	@Override
	public final Set<String> namesOfStates() {
		return new TreeSet<String>(states.keySet());
	}

	@Override
	public final Set<String> namesOfStation() {
		return new TreeSet<String>(stations.keySet());
	}

	@Override
	public final void addMainStation(MainStation mainStation) {
		setMainStation(mainStation);
	}

	@Override
	public final void addMural(Mural mural) {
		setMural(mural);
	}

	@Override
	public final void addPalette(Palette palette) {
		setPalette(palette);
	}

	@Override
	public final void addStates(States state) {
		setStates(state);
	}

	@Override
	public final void addStation(Station station) {
		setStation(station);
	}

	@Override
	public final void setMainStation(MainStation mainStation) {
		mainStations.put(mainStation.getName(), mainStation);
	}

	@Override
	public final void setMural(Mural mural) {
		murals.put(mural.getName(), mural);
		DataUtils.save(Type.MURAL, mural);
	}

	@Override
	public final void setPalette(Palette palette) {
		palettes.put(palette.getName(), palette);
		DataUtils.save(Type.PALETTE, palette);
	}

	@Override
	public final void setStates(States state) {
		states.put(state.getName(), state);
		DataUtils.save(Type.STATES, state);
	}

	@Override
	public final void setStation(Station station) {
		stations.put(station.getName(), station);
	}

	@Override
	public final void removeMainStation(String name) {
		mainStations.remove(name);
	}

	@Override
	public final void removeMural(String name) {
		murals.remove(name);
		DataUtils.delete(Type.MURAL, name);
	}

	@Override
	public final void removePalette(String name) {
		palettes.remove(name);
		DataUtils.delete(Type.PALETTE, name);
	}

	@Override
	public final void removeStates(String name) {
		states.remove(name);
		DataUtils.delete(Type.STATES, name);
	}

	@Override
	public final void removeStation(String name) {
		stations.remove(name);
	}
}
