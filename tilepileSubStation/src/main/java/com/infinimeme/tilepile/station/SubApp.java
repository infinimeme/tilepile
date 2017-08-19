package com.infinimeme.tilepile.station;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.infinimeme.tilepile.common.Location;
import com.infinimeme.tilepile.common.MainStation;
import com.infinimeme.tilepile.common.MainStationRemote;
import com.infinimeme.tilepile.common.Mural;
import com.infinimeme.tilepile.common.MuralLocation;
import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.Station;
import com.infinimeme.tilepile.common.StationFactory;
import com.infinimeme.tilepile.common.StationRemote;
import com.infinimeme.tilepile.common.StationRemoteImpl;
import com.infinimeme.tilepile.common.StationRemoteListener;
import com.infinimeme.tilepile.common.TilepileException;
import com.infinimeme.tilepile.common.TilepileUtils;
import com.infinimeme.tilepile.data.DataManagerDiscovery;
import com.infinimeme.tilepile.data.DataManagerRemote;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.
 * Copyright 2004-2014 Greg Barton
 */
public class SubApp extends JFrame implements StationRemoteListener {

	// ~ Static fields/initializers
	// *****************************************************************

	private static final long serialVersionUID = 7366442369467484934L;

	static Preferences PACKAGE_PREFS = Preferences.userNodeForPackage(SubApp.class);

	public static final String PREF_KEY_X = "x";

	public static final String PREF_KEY_Y = "y";

	public static final String PREF_KEY_WIDTH = "width";

	public static final String PREF_KEY_HEIGHT = "height";

	static {

		// Setup codebase property for RMI stub loading
		String stationURL = StationRemote.class.getClassLoader().getResource(StationRemote.class.getName().replace('.', '/') + ".class").toString();
		stationURL = stationURL.substring(0, stationURL.indexOf('!') + 2);

		String mainStationURL = StationRemote.class.getClassLoader().getResource(MainStationRemote.class.getName().replace('.', '/') + ".class").toString();
		mainStationURL = mainStationURL.substring(0, mainStationURL.indexOf('!') + 2);

		System.out.println("RESOURCE URL: " + stationURL + " " + mainStationURL);
		System.setProperty("java.rmi.server.codebase", stationURL + " " + mainStationURL);
	}

	// ~ Instance fields
	// ****************************************************************************

	private GraphicsConfiguration graphicsConfiguration = null;

	private DataManagerRemote dataManager = null;

	private JPanel tilePanelHolder = new JPanel();
	private TilePanel tilePanel = null;
	private JLabel stationNumberLabel = null;

	private Station station = null;

	private StationRemoteImpl stationRemote = null;

	private byte stationNumber = 0;

	final KeyListener globalKeyListener;

	// ~ Constructors
	// *******************************************************************************

	SubApp(GraphicsConfiguration gc, DataManagerRemote dataManager) throws RemoteException {
		super(gc);

		byte[] address = TilepileUtils.getDefaultLocalAddress().getAddress();
		setStationNumber(address[address.length - 1]);

		setTitle("Tilepile SubStation " + getStationNumber());

		final Station station = StationFactory.make(getDataManager(), gc, getStationNumber());

		stationNumberLabel = new JLabel(Byte.toString(getStationNumber()), SwingConstants.CENTER);
		stationNumberLabel.setFont(new Font("MONOSPACED", Font.BOLD, 512));
		stationNumberLabel.setForeground(TilepileUtils.getContrasting(station.getColor()));

		setGraphicsConfiguration(gc);
		setDataManager(dataManager);

		StationRemoteImpl stationRemote = new StationRemoteImpl(getDataManager(), this);

		TilepileUtils.logInfo("Binding: " + station.getRemoteNameStub());
		TilepileUtils.getRegistry().rebind(station.getRemoteNameStub(), stationRemote);

		if(getDataManager().containsStation(station.getName())) {
			getDataManager().setStation(station);
		} else {
			getDataManager().addStation(station);
		}

		TilepileUtils.logInfo("Station added");

		setStation(station);
		setStationRemote(stationRemote);

		setBackground(getStation().getColor());
		tilePanelHolder.setBackground(getStation().getColor());

		Container content = getContentPane();

		content.setLayout(new BorderLayout());

		content.setBackground(getStation().getColor());

		tilePanelHolder.setLayout(new BorderLayout());
		tilePanelHolder.add(stationNumberLabel, BorderLayout.CENTER);
		content.add(tilePanelHolder, BorderLayout.CENTER);

		final Rectangle maxBounds = gc.getBounds();
		/*
				setBounds(new Rectangle((int) PACKAGE_PREFS.getDouble(PREF_KEY_X, maxBounds.x), (int) PACKAGE_PREFS.getDouble(PREF_KEY_Y, maxBounds.y), (int) PACKAGE_PREFS.getDouble(PREF_KEY_WIDTH,
				maxBounds.width), (int) PACKAGE_PREFS.getDouble(PREF_KEY_HEIGHT, maxBounds.height)));
		*/
		//setBounds(maxBounds);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				Rectangle bounds = SubApp.this.getBounds();
				PACKAGE_PREFS.putDouble(PREF_KEY_X, bounds.getX());
				PACKAGE_PREFS.putDouble(PREF_KEY_Y, bounds.getY());
			}

			@Override
			public void componentResized(ComponentEvent e) {
				Rectangle bounds = SubApp.this.getBounds();
				PACKAGE_PREFS.putDouble(PREF_KEY_WIDTH, bounds.getWidth());
				PACKAGE_PREFS.putDouble(PREF_KEY_HEIGHT, bounds.getHeight());
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				cleanup();
			}
		}));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setUndecorated(true);

		addKeyListener(globalKeyListener = new KeyListener() {
			private final AtomicBoolean toggle = new AtomicBoolean();

			@Override
			public void keyPressed(KeyEvent ke) {
				int code = ke.getKeyCode();
				switch(code) {
				case KeyEvent.VK_T:
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if(toggle.compareAndSet(true, false)) {
								getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
							} else if(toggle.compareAndSet(false, true)) {
								getRootPane().setWindowDecorationStyle(JRootPane.NONE);
							}
						}
					});
					break;
				case KeyEvent.VK_C:
					setBounds(new Rectangle(maxBounds.x + maxBounds.width / 4, maxBounds.y + maxBounds.height / 4, maxBounds.width / 2, maxBounds.height / 2));
					break;
				case KeyEvent.VK_M:
					setBounds(new Rectangle(maxBounds.x, maxBounds.y, maxBounds.width, maxBounds.height));
					break;
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_UP:
				case KeyEvent.VK_LEFT:
					int xadd = code == KeyEvent.VK_DOWN || code == KeyEvent.VK_UP ? 0 : code == KeyEvent.VK_RIGHT ? 1 : -1;
					int yadd = code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_LEFT ? 0 : code == KeyEvent.VK_DOWN ? 1 : -1;
					Rectangle bounds = getBounds();
					if(ke.isShiftDown()) {
						setBounds(new Rectangle(bounds.x + xadd, bounds.y + yadd, bounds.width, bounds.height));
					} else if(ke.isAltDown()) {
						setBounds(new Rectangle(bounds.x, bounds.y, bounds.width + xadd, bounds.height + yadd));
					}
				}

			}

			@Override
			public void keyReleased(KeyEvent ke) {
			}

			@Override
			public void keyTyped(KeyEvent ke) {
			}
		});

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent arg0) {

				SwingUtilities.invokeLater(new Runnable() {
					TilePanel tp = tilePanel;

					@Override
					public void run() {
						if(tp != null && tp.controlsDialog != null) {
							tp.controlsDialog.setVisible(false);
							tp.controlsDialog.dispose();
						}
					}
				});
			}
		});

		TilepileUtils.debugDataManager(getDataManager());
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

	public static final GraphicsDevice[] getAvailableGraphicsDevices(DataManagerRemote dataManager) throws RemoteException {

		Set<GraphicsDevice> gdSet = new HashSet<GraphicsDevice>();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		GraphicsDevice[] gds = ge.getScreenDevices();

		for(GraphicsDevice gd : gds) {
			gdSet.add(gd);
		}

		Collection<Station> stations = dataManager.instancesOfStation();

		for(Iterator<GraphicsDevice> i = gdSet.iterator(); i.hasNext();) {

			GraphicsDevice gd = i.next();

			for(Station station : stations) {

				// We don't care about stations at other addresses.
				if(station.getName().indexOf(TilepileUtils.getDefaultLocalAddress().getHostAddress()) == -1) {
					continue;
				}

				// Remove graphics device if another station is on it.
				if(station.getName().indexOf(TilepileUtils.getNormalizedName(gd.getIDstring())) != -1) {
					i.remove();
					break;
				}
			}
		}

		return gdSet.toArray(new GraphicsDevice[0]);
	}

	public static final GraphicsDevice pickAvailableGraphicsDevice(DataManagerRemote dataManager) throws RemoteException {

		GraphicsDevice[] gds = getAvailableGraphicsDevices(dataManager);

		GraphicsDevice gd = null;

		if(gds.length > 1) {

			Object[] options = new Object[gds.length];

			for(int i = 0; i < gds.length; i++) {
				options[i] = gds[i].getIDstring();
			}

			int option = JOptionPane.showOptionDialog(null, "Which screen should the SubStation use?", "Pick screen", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
			options[0]);

			if(option == JOptionPane.CLOSED_OPTION || option == JOptionPane.CANCEL_OPTION) {
				JOptionPane.showMessageDialog(null, "OK.  Thanks anyway...", "SubStation canceled", JOptionPane.INFORMATION_MESSAGE);

				return null;
			}

			gd = gds[option];
		} else if(gds.length == 1) {
			gd = gds[0];
		}

		return gd;
	}

	/**
	 * Main method for app.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {

		try {

			TilepileUtils.appPrep(SubApp.class);

			DataManagerRemote dataManager = DataManagerDiscovery.getDataManager(DataManagerDiscovery.DiscoverableType.STATION);

			GraphicsDevice gd = pickAvailableGraphicsDevice(dataManager);

			if(gd == null) {
				JOptionPane.showMessageDialog(null, "No screens available for Station on this computer!");

				return;
			}

			TilepileUtils.showSplashScreen(gd);

			try {

				// Set global look and feel
				TilepileUtils.setLookAndFeel();
			} catch(TilepileException te) {
				TilepileUtils.exceptionReport(te);
			}

			final SubApp app = new SubApp(gd.getDefaultConfiguration(), dataManager);

			app.setVisible(true);

			app.setExtendedState(app.getExtendedState() | JFrame.MAXIMIZED_BOTH);

		} catch(RemoteException re) {
			TilepileUtils.exceptionReport(re);
			DataManagerDiscovery.clearStoredAddress();
			System.exit(1);
		} catch(NotBoundException nbe) {
			TilepileUtils.exceptionReport(nbe);
			DataManagerDiscovery.clearStoredAddress();
			System.exit(1);
		} catch(IOException ioe) {
			TilepileUtils.exceptionReport(ioe);
			System.exit(1);
		}
	}

	public void cleanup() {

		try {
			TilepileUtils.logInfo("Removing station");
			getDataManager().removeStation(getStation().getName());
			TilepileUtils.logInfo("Unbinding station");
			TilepileUtils.getRegistry().unbind(station.getRemoteNameStub());
		} catch(RemoteException re) {
			TilepileUtils.exceptionReport(re);
		} catch(NotBoundException nbe) {
			TilepileUtils.exceptionReport(nbe);
		}
	}

	@Override
	public void muralLocationSet(MainStation mainStation, MuralLocation muralLocation) {

		if(muralLocation == null) {
			tilePanelHolder.removeAll();
			if(tilePanel != null) {
				SwingUtilities.invokeLater(new Runnable() {
					TilePanel tp = tilePanel;

					@Override
					public void run() {
						tp.controlsDialog.setVisible(false);
						tp.controlsDialog.dispose();
					}
				});
				tilePanel = null;
			}
			tilePanelHolder.add(stationNumberLabel, BorderLayout.CENTER);
		} else {

			try {
				Location location = muralLocation.getLocation();
				Mural mural = dataManager.getMural(muralLocation.getMuralName());
				Palette palette = dataManager.getPalette(mural.getPaletteName());

				tilePanelHolder.removeAll();
				if(tilePanel != null) {
					SwingUtilities.invokeLater(new Runnable() {
						TilePanel tp = tilePanel;

						@Override
						public void run() {
							tp.controlsDialog.setVisible(false);
							tp.controlsDialog.dispose();
						}
					});
				}
				tilePanel = new TilePanel(this, muralLocation, new DynamicSectionPanel(mural.getSections()[location.getY()][location.getX()], palette, true), getStation(), getStationRemote(),
				mainStation, dataManager);
				tilePanelHolder.add(tilePanel, BorderLayout.CENTER);
				tilePanelHolder.validate();
				SwingUtilities.invokeLater(new Runnable() {
					TilePanel tp = tilePanel;

					@Override
					public void run() {
						tp.controlsDialog.setBounds(0, 0, getWidth() / 8, getHeight() / 4);
						tp.controlsDialog.setVisible(true);
					}
				});
			} catch(TilepileException tpe) {
				TilepileUtils.exceptionReport(tpe);
			} catch(RemoteException re) {
				TilepileUtils.exceptionReport(re);
			}
		}

		TilepileUtils.logInfo("muralLocationSet: " + muralLocation);

		tilePanelHolder.validate();
		tilePanelHolder.repaint();
	}

	private void setDataManager(DataManagerRemote dataManager) {
		this.dataManager = dataManager;
	}

	private DataManagerRemote getDataManager() {
		return dataManager;
	}

	private void setStation(Station station) {
		this.station = station;
	}

	private Station getStation() {
		return station;
	}

	private void setStationRemote(StationRemoteImpl stationRemote) {
		this.stationRemote = stationRemote;
		stationRemote.setListener(this);
	}

	private StationRemoteImpl getStationRemote() {
		return stationRemote;
	}

	public byte getStationNumber() {
		return this.stationNumber;
	}

	public void setStationNumber(byte stationNumber) {
		this.stationNumber = stationNumber;
	}
}
