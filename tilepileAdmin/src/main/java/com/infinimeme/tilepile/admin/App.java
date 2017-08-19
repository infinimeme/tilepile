package com.infinimeme.tilepile.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.AbstractListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import com.infinimeme.tilepile.common.Mural;
import com.infinimeme.tilepile.common.MuralFactory;
import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.PaletteFactory;
import com.infinimeme.tilepile.common.States;
import com.infinimeme.tilepile.common.StatesFactory;
import com.infinimeme.tilepile.common.TilepileException;
import com.infinimeme.tilepile.common.TilepileUtils;
import com.infinimeme.tilepile.data.DataConstants;
import com.infinimeme.tilepile.data.DataManagerDiscovery;
import com.infinimeme.tilepile.data.DataManagerRemote;
import com.infinimeme.tilepile.data.DataUtils;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.
 * Copyright 2004-2014 Greg Barton
 */
public class App {

	// ~ Static fields/initializers
	// *****************************************************************

	// ~ Static fields/initializers
	// *****************************************************************

	private static DataManagerRemote DATA_MANAGER = null;

	static Preferences PACKAGE_PREFS = Preferences.userNodeForPackage(App.class);

	private static final String PREF_KEY_LAST_MURAL_IMAGE_FILE = "lastMuralImageFile";

	private static final String PREF_KEY_LAST_SCREEN_HEIGHT = "lastScreenHeight";
	private static final String PREF_KEY_LAST_SCREEN_HEIGHT_UNITS = "lastScreenHeightUnits";

	private static final String PREF_KEY_LAST_SCREEN_WIDTH = "lastScreenWidth";
	private static final String PREF_KEY_LAST_SCREEN_WIDTH_UNITS = "lastScreenWidthUnits";

	private static final String PREF_KEY_LAST_TILE_SIZE = "lastTileSize";
	private static final String PREF_KEY_LAST_TILE_SIZE_UNITS = "lastTileSizeUnits";

	private static final String PREF_KEY_LAST_GROUT_SIZE = "lastGroutSize";
	private static final String PREF_KEY_LAST_GROUT_SIZE_UNITS = "lastGroutSizeUnits";

	private static final String PREF_KEY_LAST_OFFSET_ROWS = "lastOffsetRows";

	/** Application desktop */
	private static final JDesktopPane desktop = new JDesktopPane();

	/** Menu for opening murals. */
	private static final JMenu openMuralMenu = new JMenu("Open");

	/** Menu for deleting murals. */
	private static final JMenu deleteMuralMenu = new JMenu("Delete");

	/** Menu for backing up murals. */
	private static final JMenu backupMuralMenu = new JMenu("Backup");

	/** Menu for merging states to murals. */
	private static final JMenu statesMuralMenu = new JMenu("States");

	/** Menu for backing up murals. */
	private static final JMenu backupStatesMenu = new JMenu("Backup");

	/** Menu for opening palettes. */
	private static final JMenu openPaletteMenu = new JMenu("Open");

	/** Menu for deleting palettes. */
	private static final JMenu deletePaletteMenu = new JMenu("Delete");

	/** Menu for backing up palettes. */
	private static final JMenu backupPaletteMenu = new JMenu("Backup");

	/** Menu for exporting palettes to spreadsheet. */
	private static final JMenu exportPaletteMenu = new JMenu("Export");

	// ~ Methods
	// ************************************************************************************

	// ~ Methods
	// ************************************************************************************

	/**
	 * Main method for app.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {

		TilepileUtils.appPrep(App.class);

		try {
			DATA_MANAGER = DataManagerDiscovery.getDataManager(DataManagerDiscovery.DiscoverableType.ADMIN);

			JFrame frame = new JFrame("Tilepile Admin");
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gds = ge.getScreenDevices();
			Rectangle screenBounds = gds[0].getDefaultConfiguration().getBounds();

			// desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
			frame.getContentPane().add(desktop);

			// Close app when main frame closed
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			try {

				// Set global look and feel
				TilepileUtils.setLookAndFeel();

				// Setup menus
				frame.setJMenuBar(createMenu(desktop, frame));

			} catch(TilepileException tpe) {
				TilepileUtils.exceptionReport(tpe);
			}

			frame.setSize(screenBounds.getSize());
			frame.setVisible(true);

		} catch(RemoteException re) {
			TilepileUtils.exceptionReport(re);
			DataManagerDiscovery.clearStoredAddress();
			System.exit(1);
		} catch(IOException ioe) {
			TilepileUtils.exceptionReport(ioe);
			System.exit(1);
		} catch(NotBoundException nbe) {
			TilepileUtils.exceptionReport(nbe);
			DataManagerDiscovery.clearStoredAddress();
			System.exit(1);
		}
	}

	/**
	 * Setup menu items for a new mural.
	 * 
	 * @param name
	 *            Name of new mural
	 */
	private static final void addMuralMenuItems(final String name) {

		// Setup "open" menu
		JMenuItem openMuralMenuItem = new JMenuItem(name);
		openMuralMenu.add(openMuralMenuItem);

		openMuralMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					desktop.add(new MuralEditor(DATA_MANAGER.getMural(name), DATA_MANAGER));
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				}
			}
		});

		// Setup "backup" menu
		JMenuItem backupMuralMenuItem = new JMenuItem(name);
		backupMuralMenu.add(backupMuralMenuItem);

		backupMuralMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				try {
					MuralFactory.save(DATA_MANAGER.getMural(name));
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				} catch(IOException ioe) {
					TilepileUtils.exceptionReport(ioe);
				}
			}
		});

		// Setup "delete" menu
		JMenuItem deleteMuralMenuItem = new JMenuItem(name);
		deleteMuralMenu.add(deleteMuralMenuItem);

		deleteMuralMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				int option = JOptionPane.showConfirmDialog(desktop, "Are you sure?", "Really delete " + name + "?", JOptionPane.YES_NO_OPTION);

				if(option == JOptionPane.YES_OPTION) {

					try {
						Mural mural = DATA_MANAGER.getMural(name);

						if(mural != null) {
							DATA_MANAGER.removeMural(mural.getName());
							deleteMuralMenuItems(name);
						}
					} catch(RemoteException re) {
						TilepileUtils.exceptionReport(re);
					}
				}
			}
		});

		// Setup "states" menu
		JMenuItem statesMuralMenuItem = new JMenuItem(name);
		statesMuralMenu.add(statesMuralMenuItem);

		statesMuralMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				JFileChooser chooser = new JFileChooser();

				String lastFile = PACKAGE_PREFS.get(PREF_KEY_LAST_MURAL_IMAGE_FILE, null);

				if(lastFile != null) {
					chooser.setSelectedFile(new File(lastFile));
				}

				chooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {

						return f.isDirectory() || f.isFile() && f.getName().endsWith(DataConstants.DATA_FILE_EXTENSION);
					}

					@Override
					public String getDescription() {

						return "Data files";
					}
				});

				chooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {

						return f.isDirectory() || f.isFile() && f.getName().equals(name + DataConstants.DATA_FILE_EXTENSION);
					}

					@Override
					public String getDescription() {

						return name + DataConstants.DATA_FILE_EXTENSION + " States data file";
					}
				});

				int returnVal = chooser.showOpenDialog(desktop);

				if(returnVal == JFileChooser.APPROVE_OPTION) {

					try {

						File selectedFile = chooser.getSelectedFile();

						PACKAGE_PREFS.put(PREF_KEY_LAST_MURAL_IMAGE_FILE, selectedFile.getAbsolutePath());

						States newStates = DataUtils.readFile(selectedFile, States.class);

						States oldStates = DATA_MANAGER.getStates(name);

						if(oldStates.add(newStates)) {
							DATA_MANAGER.setStates(oldStates);
						} else {
							JOptionPane.showInternalMessageDialog(desktop, "File " + chooser.getSelectedFile().getName() + " incompatible with states from " + name, "States files incompatible!",
							JOptionPane.ERROR_MESSAGE);
						}

					} catch(IOException ioe) {
						TilepileUtils.exceptionReport(ioe);
					} catch(ClassNotFoundException cnfe) {
						TilepileUtils.exceptionReport(cnfe);
					} catch(ClassCastException cce) {
						TilepileUtils.exceptionReport(cce);
					}
				}
			}
		});

		JMenuItem backupStatesMenuItem = new JMenuItem(name);
		backupStatesMenu.add(backupStatesMenuItem);

		backupStatesMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				try {
					StatesFactory.save(DATA_MANAGER.getStates(name));
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				} catch(IOException ioe) {
					TilepileUtils.exceptionReport(ioe);
				}
			}
		});

	}

	/**
	 * Setup menu items for a new palette.
	 * 
	 * @param name
	 *            Name of new palette
	 */
	private static final void addPaletteMenuItems(final String name) {

		// Setup "open" menu
		JMenuItem openPaletteMenuItem = new JMenuItem(name);
		openPaletteMenu.add(openPaletteMenuItem);

		openPaletteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					desktop.add(new PaletteEditor(DATA_MANAGER.getPalette(name), DATA_MANAGER));
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				}
			}
		});

		// Setup "backup" menu
		JMenuItem backupPaletteMenuItem = new JMenuItem(name);
		backupPaletteMenu.add(backupPaletteMenuItem);
		backupPaletteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				try {
					Palette palette = DATA_MANAGER.getPalette(name);

					try {
						PaletteFactory.saveAsBackup(palette);
					} catch(IOException ioe) {
						TilepileUtils.exceptionReport(ioe);
					} catch(TilepileException tpe) {
						TilepileUtils.exceptionReport(tpe);
					}
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				}
			}
		});

		JMenuItem exportPaletteMenuItem = new JMenuItem(name);
		exportPaletteMenu.add(exportPaletteMenuItem);
		exportPaletteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				try {
					Palette palette = DATA_MANAGER.getPalette(name);

					try {
						PaletteFactory.saveAsSpreadsheet(palette);
					} catch(IOException ioe) {
						TilepileUtils.exceptionReport(ioe);
					} catch(TilepileException tpe) {
						TilepileUtils.exceptionReport(tpe);
					}
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				}
			}
		});

		// Setup "delete" menu
		JMenuItem deletePaletteMenuItem = new JMenuItem(name);
		deletePaletteMenu.add(deletePaletteMenuItem);

		deletePaletteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				int option = JOptionPane.showConfirmDialog(desktop, "Are you sure?", "Really delete " + name + "?", JOptionPane.YES_NO_OPTION);

				if(option == JOptionPane.YES_OPTION) {

					try {
						Palette palette = DATA_MANAGER.getPalette(name);

						if(palette != null) {
							DATA_MANAGER.removePalette(palette.getName());
							deletePaletteMenuItems(name);
						}
					} catch(RemoteException re) {
						TilepileUtils.exceptionReport(re);
					}
				}
			}
		});
	}

	/**
	 * Create application menus.
	 * 
	 * @param desktop
	 *            Application desktop
	 * @return Application menu bar
	 * @throws TilepileException
	 */
	private static final JMenuBar createMenu(final JDesktopPane desktop, final JFrame frame) throws TilepileException {

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		/*
				JMenuItem resetItem = new JMenuItem("Reset DataServer");

				resetItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						DataManagerDiscovery.clearStoredAddress();
					}
				});

				resetItem.setAccelerator(KeyStroke.getKeyStroke(new Character('r'), InputEvent.CTRL_MASK));

				fileMenu.add(resetItem);
		*/
		JMenuItem quitItem = new JMenuItem("Quit");

		quitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		quitItem.setAccelerator(KeyStroke.getKeyStroke(new Character('q'), InputEvent.CTRL_MASK));

		fileMenu.add(quitItem);

		JMenu muralMenu = new JMenu("Mural");
		menuBar.add(muralMenu);

		JMenuItem loadMuralMenuItem = new JMenuItem("Load Data");
		JMenuItem restoreMuralMenuItem = new JMenuItem("Restore Data");
		JMenuItem importMuralMenuItem = new JMenuItem("Import Image");
		JMenuItem importMuralForScreenMenuItem = new JMenuItem("Import For Screen");

		muralMenu.add(loadMuralMenuItem);
		muralMenu.add(restoreMuralMenuItem);
		muralMenu.add(importMuralMenuItem);
		muralMenu.add(importMuralForScreenMenuItem);
		muralMenu.add(openMuralMenu);
		muralMenu.add(backupMuralMenu);
		muralMenu.add(deleteMuralMenu);
		muralMenu.add(statesMuralMenu);

		try {
			for(String name : DATA_MANAGER.namesOfMural()) {
				addMuralMenuItems(name);
			}
		} catch(RemoteException re) {
			TilepileUtils.exceptionReport(re);
		}

		// Gather input to create a new mural
		loadMuralMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							LoadMuralDialog dialog = new LoadMuralDialog(frame);
							dialog.setLocationRelativeTo(frame);
							dialog.setVisible(true);
						} catch(RemoteException re) {
							TilepileUtils.exceptionReport(re);
						}
					}
				}).run();
			}
		});

		restoreMuralMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				try {

					JFileChooser chooser = new JFileChooser();

					chooser.setDialogTitle("Find a mural backup file");

					int returnVal = chooser.showOpenDialog(desktop);

					if(returnVal == JFileChooser.APPROVE_OPTION) {

						Mural mural = MuralFactory.make(chooser.getSelectedFile(), DATA_MANAGER);

						if(DATA_MANAGER.containsMural(mural.getName())) {
							DATA_MANAGER.setMural(mural);
						} else {
							addMuralMenuItems(mural.getName());
							DATA_MANAGER.addMural(mural);
						}

					}
				} catch(IOException ioe) {
					TilepileUtils.exceptionReport(ioe);
				} catch(TilepileException tpe) {
					TilepileUtils.exceptionReport(tpe);
				}
			}
		});

		importMuralMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							ImportMuralDialog dialog = new ImportMuralDialog(frame);
							dialog.setLocationRelativeTo(frame);
							dialog.setVisible(true);
						} catch(RemoteException re) {
							TilepileUtils.exceptionReport(re);
						}
					}
				}).run();
			}
		});

		importMuralForScreenMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							ImportMuralForScreenDialog dialog = new ImportMuralForScreenDialog(frame);
							dialog.setLocationRelativeTo(frame);
							dialog.setVisible(true);
						} catch(RemoteException re) {
							TilepileUtils.exceptionReport(re);
						}
					}
				}).run();
			}
		});

		JMenu paletteMenu = new JMenu("Palette");
		menuBar.add(paletteMenu);

		JMenuItem addPaletteMenuItem = new JMenuItem("Add");
		JMenuItem importPaletteFileMenuItem = new JMenuItem("Import File");
		JMenuItem importPaletteImageMenuItem = new JMenuItem("Import Image");
		JMenuItem importPaletteSpreadsheetMenuItem = new JMenuItem("Import Spreadsheet");
		JMenuItem mergePaletteMenuItem = new JMenuItem("Merge");
		JMenuItem restorePaletteMenuItem = new JMenuItem("Restore");

		paletteMenu.add(addPaletteMenuItem);
		paletteMenu.add(importPaletteFileMenuItem);
		paletteMenu.add(importPaletteImageMenuItem);
		paletteMenu.add(importPaletteSpreadsheetMenuItem);
		paletteMenu.add(mergePaletteMenuItem);
		paletteMenu.add(restorePaletteMenuItem);
		paletteMenu.add(openPaletteMenu);
		paletteMenu.add(backupPaletteMenu);
		paletteMenu.add(exportPaletteMenu);
		paletteMenu.add(deletePaletteMenu);

		try {

			Collection<String> paletteNames = DATA_MANAGER.namesOfPalette();

			for(String name : paletteNames) {
				addPaletteMenuItems(name);
			}

		} catch(RemoteException re) {
			TilepileUtils.exceptionReport(re);
		}

		addPaletteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				try {
					String name = JOptionPane.showInputDialog(desktop, "What is the new palette name?");

					if(name == null) {

						return;
					}

					if(DATA_MANAGER.containsPalette(name)) {
						JOptionPane.showMessageDialog(desktop, "Palette name " + name + " already used", "Palette name in use", JOptionPane.ERROR_MESSAGE);
					}

					String num = JOptionPane.showInputDialog(desktop, "How many colors in palette?");

					if(num == null) {

						return;
					}

					Palette palette = PaletteFactory.make(name, Integer.parseInt(num));

					DATA_MANAGER.addPalette(palette);

					addPaletteMenuItems(name);
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				}
			}
		});

		importPaletteFileMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				try {

					String name = JOptionPane.showInputDialog(desktop, "What is the new palette name?");

					if(name != null && !DATA_MANAGER.containsPalette(name)) {

						JFileChooser chooser = new JFileChooser();
						int returnVal = chooser.showOpenDialog(desktop);

						if(returnVal == JFileChooser.APPROVE_OPTION) {

							Palette palette = PaletteFactory.makeFromPhotoshopPalette(name, chooser.getSelectedFile());

							DATA_MANAGER.addPalette(palette);

							addPaletteMenuItems(name);
						}
					} else {
						JOptionPane.showMessageDialog(desktop, "Palette name " + name + " already used", "Palette name in use", JOptionPane.ERROR_MESSAGE);

					}
				} catch(IOException ioe) {
					TilepileUtils.exceptionReport(ioe);
				}
			}
		});

		importPaletteImageMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				try {

					String name = JOptionPane.showInputDialog(desktop, "What is the new palette name?");

					if(name != null && !DATA_MANAGER.containsPalette(name)) {

						JFileChooser chooser = new JFileChooser();
						int returnVal = chooser.showOpenDialog(desktop);

						if(returnVal == JFileChooser.APPROVE_OPTION) {

							BufferedImage image = null;

							File imageFile = chooser.getSelectedFile();
							try {
								image = ImageIO.read(imageFile);
							} catch(IOException e) {
								TilepileUtils.exceptionReport(e);
							}

							if(image == null) {
								JOptionPane.showMessageDialog(desktop, "Cannot read image format: " + imageFile, "Invalid image file", JOptionPane.ERROR_MESSAGE);
								return;
							}

							Palette palette = null;

							try {
								palette = PaletteFactory.make(name, image);
							} catch(TilepileException e) {
								TilepileUtils.exceptionReport(e);
							}

							if(palette == null) {
								JOptionPane.showMessageDialog(desktop, "Cannot convert image to palette: " + imageFile, "Invalid image file", JOptionPane.ERROR_MESSAGE);
								return;
							} else {
								DATA_MANAGER.addPalette(palette);
							}
						}

						addPaletteMenuItems(name);
					} else {
						JOptionPane.showMessageDialog(desktop, "Palette name " + name + " already used", "Palette name in use", JOptionPane.ERROR_MESSAGE);

					}
				} catch(IOException ioe) {
					TilepileUtils.exceptionReport(ioe);
				}
			}
		});

		importPaletteSpreadsheetMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				try {

					String name = JOptionPane.showInputDialog(desktop, "What is the new palette name?");

					if(name != null && !DATA_MANAGER.containsPalette(name)) {

						JFileChooser chooser = new JFileChooser();
						int returnVal = chooser.showOpenDialog(desktop);

						if(returnVal == JFileChooser.APPROVE_OPTION) {

							Palette palette = PaletteFactory.makeFromSpreadsheet(name, chooser.getSelectedFile());

							DATA_MANAGER.addPalette(palette);

							addPaletteMenuItems(name);
						}

					} else {
						JOptionPane.showMessageDialog(desktop, "Palette name " + name + " already used", "Palette name in use", JOptionPane.ERROR_MESSAGE);

					}
				} catch(IOException ioe) {
					TilepileUtils.exceptionReport(ioe);
				} catch(TilepileException te) {
					TilepileUtils.exceptionReport(te);
				}
			}
		});

		mergePaletteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							MergePalettesDialog dialog = new MergePalettesDialog(frame);
							dialog.setLocationRelativeTo(frame);
							dialog.setVisible(true);
							if(!dialog.isCancelled()) {

								Palette palette = PaletteFactory.make(dialog.getPaletteName(), dialog.isMergeNames(), dialog.isMergeColors(), dialog.getPalettes());

								if(DATA_MANAGER.containsPalette(palette.getName())) {
									DATA_MANAGER.setPalette(palette);
								} else {
									addPaletteMenuItems(palette.getName());
									DATA_MANAGER.addPalette(palette);
								}

							}
						} catch(RemoteException re) {
							TilepileUtils.exceptionReport(re);
						}
					}
				}).run();
			}
		});

		restorePaletteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				try {

					JFileChooser chooser = new JFileChooser();
					int returnVal = chooser.showOpenDialog(desktop);

					if(returnVal == JFileChooser.APPROVE_OPTION) {

						Palette palette = PaletteFactory.make(chooser.getSelectedFile());

						if(DATA_MANAGER.containsPalette(palette.getName())) {
							DATA_MANAGER.setPalette(palette);
						} else {
							addPaletteMenuItems(palette.getName());
							DATA_MANAGER.addPalette(palette);
						}

					}
				} catch(Exception e) {
					TilepileUtils.exceptionReport(e);
				}
			}
		});

		return menuBar;
	}

	private static final void deleteMuralMenuItems(final String name) {

		for(int i = 0; i < openMuralMenu.getItemCount(); i++) {

			JMenuItem item = openMuralMenu.getItem(i);

			if(name.equals(item.getText())) {
				openMuralMenu.remove(item);
			}
		}

		for(int i = 0; i < backupMuralMenu.getItemCount(); i++) {

			JMenuItem item = backupMuralMenu.getItem(i);

			if(name.equals(item.getText())) {
				backupMuralMenu.remove(item);
			}
		}

		for(int i = 0; i < deleteMuralMenu.getItemCount(); i++) {

			JMenuItem item = deleteMuralMenu.getItem(i);

			if(name.equals(item.getText())) {
				deleteMuralMenu.remove(item);
			}
		}

		for(int i = 0; i < statesMuralMenu.getItemCount(); i++) {

			JMenuItem item = statesMuralMenu.getItem(i);

			if(name.equals(item.getText())) {
				statesMuralMenu.remove(item);
			}
		}
	}

	private static final void deletePaletteMenuItems(final String name) {

		for(int i = 0; i < openPaletteMenu.getItemCount(); i++) {

			JMenuItem item = openPaletteMenu.getItem(i);

			if(name.equals(item.getText())) {
				openPaletteMenu.remove(item);
			}
		}

		for(int i = 0; i < backupPaletteMenu.getItemCount(); i++) {

			JMenuItem item = backupPaletteMenu.getItem(i);

			if(name.equals(item.getText())) {
				backupPaletteMenu.remove(item);
			}
		}

		for(int i = 0; i < exportPaletteMenu.getItemCount(); i++) {

			JMenuItem item = exportPaletteMenu.getItem(i);

			if(name.equals(item.getText())) {
				exportPaletteMenu.remove(item);
			}
		}

		for(int i = 0; i < deletePaletteMenu.getItemCount(); i++) {

			JMenuItem item = deletePaletteMenu.getItem(i);

			if(name.equals(item.getText())) {
				deletePaletteMenu.remove(item);
			}
		}
	}

	private static class MergePalettesDialog extends JDialog {

		private static final long serialVersionUID = 2334839615679643976L;

		private String paletteName = null;
		private boolean mergeNames = false;
		private boolean mergeColors = false;
		private final Collection<Palette> palettes = new HashSet<Palette>();
		private boolean cancelled = false;

		public MergePalettesDialog(final JFrame frame) throws RemoteException {

			super(frame, "Merge Palettes", true);

			JLabel paletteNameLabel = new JLabel("Palette Name");
			JLabel mergeNamesLabel = new JLabel("Merge Names");
			JLabel palettesLabel = new JLabel("Palettes");
			JLabel mergeColorsLabel = new JLabel("Merge Colors");

			final JTextField paletteNameText = new JTextField();
			final JCheckBox mergeNamesCheck = new JCheckBox();
			final JCheckBox mergeColorsCheck = new JCheckBox();
			class PaletteTuple {
				public Palette palette;

				@Override
				public String toString() {
					return palette.getName();
				}
			}
			final JList palettesList = new JList(new AbstractListModel() {
				private static final long serialVersionUID = -9164908772545242748L;
				PaletteTuple[] data = makeTuples();

				private PaletteTuple[] makeTuples() throws RemoteException {
					Palette[] palettes = DATA_MANAGER.instancesOfPalette().toArray(new Palette[0]);
					PaletteTuple[] d = new PaletteTuple[palettes.length];
					for(int i = 0; i < palettes.length; i++) {
						d[i] = new PaletteTuple();
						d[i].palette = palettes[i];
					}
					return d;
				}

				@Override
				public Object getElementAt(int index) {
					return data[index];
				}

				@Override
				public int getSize() {
					return data.length;
				}
			});
			palettesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			JButton cancelButton = new JButton("Cancel");
			JButton okButton = new JButton("OK");

			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cancelled = true;
					MergePalettesDialog.this.setVisible(false);
				}
			});

			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					paletteName = paletteNameText.getText();
					mergeNames = mergeNamesCheck.isSelected();
					mergeColors = mergeColorsCheck.isSelected();
					for(Object o : palettesList.getSelectedValues()) {
						palettes.add(((PaletteTuple) o).palette);
					}
					MergePalettesDialog.this.setVisible(false);
				}
			});

			JPanel pane = new JPanel();
			getContentPane().add(pane);
			GroupLayout layout = new GroupLayout(pane);
			pane.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
			hGroup.addGroup(layout.createParallelGroup().addComponent(paletteNameLabel).addComponent(mergeNamesLabel).addComponent(mergeColorsLabel).addComponent(palettesLabel)
			.addComponent(cancelButton));
			hGroup.addGroup(layout.createParallelGroup().addComponent(paletteNameText).addComponent(mergeNamesCheck).addComponent(mergeColorsCheck).addComponent(palettesList).addComponent(okButton));
			layout.setHorizontalGroup(hGroup);

			GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(paletteNameLabel).addComponent(paletteNameText));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(mergeNamesLabel).addComponent(mergeNamesCheck));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(mergeColorsLabel).addComponent(mergeColorsCheck));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(palettesLabel).addComponent(palettesList));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(cancelButton).addComponent(okButton));
			layout.setVerticalGroup(vGroup);

			pack();

		}

		public Collection<Palette> getPalettes() {
			return palettes;
		}

		public String getPaletteName() {
			return paletteName;
		}

		public boolean isMergeNames() {
			return mergeNames;
		}

		public boolean isMergeColors() {
			return mergeColors;
		}

		public boolean isCancelled() {
			return cancelled;
		}
	}

	private static class LoadMuralDialog extends JDialog {

		private static final long serialVersionUID = 7990165245602672354L;
		private File muralFile = null;

		public LoadMuralDialog(final JFrame frame) throws RemoteException {

			super(frame, "Load Mural", true);

			JLabel muralNameLabel = new JLabel("Mural Name");
			final JTextField muralName = new JTextField();
			muralName.setInputVerifier(new InputVerifier() {
				private final Color defaultColor = muralName.getBackground();

				@Override
				public boolean verify(JComponent input) {
					JTextField field = (JTextField) input;
					String name = field.getText();
					boolean used = false;
					try {
						used = DATA_MANAGER.namesOfMural().contains(name);
					} catch(RemoteException re) {
						TilepileUtils.exceptionReport(re);
					}
					if(used) {
						field.setBackground(Color.RED);
						JOptionPane.showMessageDialog(LoadMuralDialog.this, "Mural name " + name + " already in use", "Mural Name Error", JOptionPane.ERROR_MESSAGE);
					} else {
						field.setBackground(defaultColor);
					}
					return !used;
				}
			});

			JLabel paletteNameLabel = new JLabel("Palette Name");
			final JComboBox paletteName = new JComboBox(DATA_MANAGER.namesOfPalette().toArray());

			JLabel imageFileLabel = new JLabel("Data File");
			JButton imageFileButton = new JButton("Browse");
			imageFileButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();

					chooser.setFileFilter(new FileFilter() {

						@Override
						public boolean accept(File f) {
							return f.isDirectory() || f.getName().toUpperCase().endsWith(MuralFactory.BACKUP_EXTENSION);
						}

						@Override
						public String getDescription() {
							return "Image File";
						}
					});

					int returnVal = chooser.showOpenDialog(frame);
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						muralFile = chooser.getSelectedFile();
					}
				}
			});

			JLabel gridWidthLabel = new JLabel("Grid Width");
			final JTextField gridWidth = new JTextField("15");
			gridWidth.setInputVerifier(new InputVerifier() {
				private final Color defaultColor = gridWidth.getBackground();

				@Override
				public boolean verify(JComponent input) {
					JTextField field = (JTextField) input;
					boolean parsed = true;
					try {
						Integer.parseInt(field.getText());
						field.setBackground(defaultColor);
					} catch(NumberFormatException e) {
						parsed = false;
						field.setBackground(Color.RED);
						JOptionPane.showMessageDialog(LoadMuralDialog.this, "Please enter an integer", "Grid Width Error", JOptionPane.ERROR_MESSAGE);
					}
					return parsed;
				}
			});

			JLabel gridHeightLabel = new JLabel("Grid Height");
			final JTextField gridHeight = new JTextField("15");
			gridHeight.setInputVerifier(new InputVerifier() {
				private final Color defaultColor = gridHeight.getBackground();

				@Override
				public boolean verify(JComponent input) {
					JTextField field = (JTextField) input;
					boolean parsed = true;
					try {
						Integer.parseInt(field.getText());
						field.setBackground(defaultColor);
					} catch(NumberFormatException e) {
						parsed = false;
						field.setBackground(Color.RED);
						JOptionPane.showMessageDialog(LoadMuralDialog.this, "Please enter an integer", "Grid Height Error", JOptionPane.ERROR_MESSAGE);
					}
					return parsed;
				}
			});

			final JCheckBox offsetRows = new JCheckBox("Offset Rows");

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					LoadMuralDialog.this.setVisible(false);
				}
			});

			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(muralFile == null) {
						JOptionPane.showMessageDialog(desktop, "Please select a mural backup file", "No mural backup file selected", JOptionPane.ERROR_MESSAGE);
					} else {

						try {
							Mural mural = MuralFactory.make(muralName.getText(), Integer.parseInt(gridWidth.getText()), Integer.parseInt(gridHeight.getText()), offsetRows.isSelected(), muralFile,
							DATA_MANAGER.getPalette((String) paletteName.getSelectedItem()));
							DATA_MANAGER.addMural(mural);
							addMuralMenuItems(muralName.getText());

							LoadMuralDialog.this.setVisible(false);

						} catch(NumberFormatException nfe) {
							nfe.printStackTrace();
						} catch(RemoteException re) {
							re.printStackTrace();
						} catch(IOException ioe) {
							ioe.printStackTrace();
						}
					}
				}
			});

			JPanel pane = new JPanel();
			getContentPane().add(pane);
			GroupLayout layout = new GroupLayout(pane);
			pane.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
			hGroup.addGroup(layout.createParallelGroup().addComponent(muralNameLabel).addComponent(paletteNameLabel).addComponent(imageFileLabel).addComponent(gridWidthLabel)
			.addComponent(gridHeightLabel).addComponent(cancelButton));
			hGroup.addGroup(layout.createParallelGroup().addComponent(muralName).addComponent(paletteName).addComponent(imageFileButton).addComponent(gridWidth).addComponent(gridHeight)
			.addComponent(okButton));
			layout.setHorizontalGroup(hGroup);

			GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(muralNameLabel).addComponent(muralName));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(paletteNameLabel).addComponent(paletteName));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(imageFileLabel).addComponent(imageFileButton));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(gridWidthLabel).addComponent(gridWidth));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(gridHeightLabel).addComponent(gridHeight));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(gridHeightLabel).addComponent(offsetRows));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(cancelButton).addComponent(okButton));
			layout.setVerticalGroup(vGroup);

			pack();
		}
	}

	private static class ImportMuralForScreenDialog extends JDialog {

		private static final long serialVersionUID = 6408922859162771868L;

		private File imageFile = null;
		private BufferedImage image = null;

		public ImportMuralForScreenDialog(final JFrame frame) throws RemoteException {

			super(frame, "Import Mural For Screen", true);

			final String lastFile = PACKAGE_PREFS.get(PREF_KEY_LAST_MURAL_IMAGE_FILE, null);

			if(lastFile != null) {
				imageFile = new File(lastFile);
				try {
					image = ImageIO.read(imageFile);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}

			final JPanel pane = new JPanel();
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(pane, BorderLayout.WEST);

			final ImagePreviewPanel imagePreview = new ImagePreviewPanel();
			getContentPane().add(imagePreview, BorderLayout.CENTER);

			JLabel replaceLabel = new JLabel("Replace Mural");
			final JCheckBox replace = new JCheckBox();

			JLabel muralNameLabel = new JLabel("Mural Name");
			final JTextField muralName = new JTextField();

			JLabel paletteNameLabel = new JLabel("Palette Name");
			final JComboBox paletteName = new JComboBox(DATA_MANAGER.namesOfPalette().toArray());

			JLabel screenHeightLabel = new JLabel("Screen Height");
			final UnitField screenHeight = new UnitField(PACKAGE_PREFS.getDouble(PREF_KEY_LAST_SCREEN_HEIGHT, 0.0), UnitField.Units.valueOf(PACKAGE_PREFS.get(PREF_KEY_LAST_SCREEN_HEIGHT_UNITS,
			UnitField.Units.INCHES.name())));

			JLabel screenWidthLabel = new JLabel("Screen Width");
			final UnitField screenWidth = new UnitField(PACKAGE_PREFS.getDouble(PREF_KEY_LAST_SCREEN_WIDTH, 0.0), UnitField.Units.valueOf(PACKAGE_PREFS.get(PREF_KEY_LAST_SCREEN_WIDTH_UNITS,
			UnitField.Units.INCHES.name())));

			JLabel useBackupColorsLabel = new JLabel("Use backup colors");
			final JCheckBox useBackupColors = new JCheckBox();

			final JCheckBox offsetRows = new JCheckBox("Offset Rows", PACKAGE_PREFS.getBoolean(PREF_KEY_LAST_OFFSET_ROWS, false));

			JLabel imageFileLabel = new JLabel("Image File");
			JButton imageFileButton = new JButton("Browse");

			imageFileButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();

					if(lastFile != null) {
						chooser.setSelectedFile(new File(lastFile));
					}

					chooser.setFileFilter(new FileFilter() {

						@Override
						public boolean accept(File f) {
							return f.isDirectory() || f.getName().toUpperCase().endsWith(".PNG") || f.getName().toUpperCase().endsWith(".JPG");
						}

						@Override
						public String getDescription() {
							return "Image File";
						}
					});

					int returnVal = chooser.showOpenDialog(frame);
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						imageFile = chooser.getSelectedFile();
						PACKAGE_PREFS.put(PREF_KEY_LAST_MURAL_IMAGE_FILE, imageFile.getAbsolutePath());
						try {
							image = ImageIO.read(imageFile);
						} catch(IOException e1) {
							TilepileUtils.exceptionReport(e1);
						}

						if(image == null) {
							JOptionPane.showMessageDialog(desktop, "Cannot read image format: " + imageFile, "Invalid image file", JOptionPane.ERROR_MESSAGE);
							return;
						}
						imagePreview.setImage(image);
					}
				}
			});

			JLabel tileSizeLabel = new JLabel("Tile Size");
			final UnitField tileSize = new UnitField(PACKAGE_PREFS.getDouble(PREF_KEY_LAST_TILE_SIZE, 0.0), UnitField.Units.valueOf(PACKAGE_PREFS.get(PREF_KEY_LAST_TILE_SIZE_UNITS,
			UnitField.Units.INCHES.name())));

			JLabel groutSizeLabel = new JLabel("Grout Size");
			final UnitField groutSize = new UnitField(PACKAGE_PREFS.getDouble(PREF_KEY_LAST_GROUT_SIZE, 0.0), UnitField.Units.valueOf(PACKAGE_PREFS.get(PREF_KEY_LAST_GROUT_SIZE_UNITS,
			UnitField.Units.INCHES.name())));

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ImportMuralForScreenDialog.this.setVisible(false);
				}
			});

			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					try {

						if(image == null) {
							JOptionPane.showMessageDialog(desktop, "Please specify an image to load", "No Image Specified", JOptionPane.ERROR_MESSAGE);
							return;
						}
						String name = muralName.getText();

						String selectedPaletteName = (String) paletteName.getSelectedItem();

						if(selectedPaletteName == null || "".equals(selectedPaletteName.trim())) {
							if(JOptionPane.showConfirmDialog(desktop, "Palette name is blank.  Do you wish to create a palette from the image?", "Blank Palette", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

								selectedPaletteName = JOptionPane.showInputDialog(desktop, "What is the new palette name?", name + "-autoPalette");

								if(name == null) {
									JOptionPane.showMessageDialog(desktop, "Cancelling mural import due to blank palette", "Invalid Palette", JOptionPane.ERROR_MESSAGE);
									return;
								}

								selectedPaletteName = name + "-palette";
								Palette p = PaletteFactory.make(selectedPaletteName, image);
								DATA_MANAGER.addPalette(p);
							} else {
								JOptionPane.showMessageDialog(desktop, "Cancelling mural import due to blank palette", "Invalid Palette", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}

						Palette palette = DATA_MANAGER.getPalette(selectedPaletteName);

						double gridHeight = Math.ceil(screenHeight.getAmountInInches() / (tileSize.getAmountInInches() + groutSize.getAmountInInches()));
						double gridWidth = Math.ceil(screenWidth.getAmountInInches() / (tileSize.getAmountInInches() + groutSize.getAmountInInches()));

						PACKAGE_PREFS.putBoolean(PREF_KEY_LAST_OFFSET_ROWS, offsetRows.isSelected());

						Mural mural = MuralFactory.make(name, (int) gridWidth, (int) gridHeight, offsetRows.isSelected(), image, palette, useBackupColors.isSelected());

						PACKAGE_PREFS.putDouble(PREF_KEY_LAST_SCREEN_HEIGHT, screenHeight.getAmount());
						PACKAGE_PREFS.put(PREF_KEY_LAST_SCREEN_HEIGHT_UNITS, screenHeight.getUnits().name());

						PACKAGE_PREFS.putDouble(PREF_KEY_LAST_SCREEN_WIDTH, screenWidth.getAmount());
						PACKAGE_PREFS.put(PREF_KEY_LAST_SCREEN_WIDTH_UNITS, screenWidth.getUnits().name());

						PACKAGE_PREFS.putDouble(PREF_KEY_LAST_TILE_SIZE, tileSize.getAmount());
						PACKAGE_PREFS.put(PREF_KEY_LAST_TILE_SIZE_UNITS, tileSize.getUnits().name());

						PACKAGE_PREFS.putDouble(PREF_KEY_LAST_GROUT_SIZE, groutSize.getAmount());
						PACKAGE_PREFS.put(PREF_KEY_LAST_GROUT_SIZE_UNITS, groutSize.getUnits().name());

						DATA_MANAGER.addMural(mural);
						addMuralMenuItems(name);
						ImportMuralForScreenDialog.this.setVisible(false);

					} catch(NumberFormatException nfe) {
						TilepileUtils.exceptionReport(nfe);
					} catch(RemoteException re) {
						TilepileUtils.exceptionReport(re);
					} catch(IOException ioe) {
						TilepileUtils.exceptionReport(ioe);
					} catch(TilepileException tpe) {
						TilepileUtils.exceptionReport(tpe);
					}
				}
			});

			JLabel offsetRowsLabel = new JLabel();
			GroupLayout layout = new GroupLayout(pane);
			pane.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
			hGroup.addGroup(layout.createParallelGroup().addComponent(muralNameLabel).addComponent(replaceLabel).addComponent(paletteNameLabel).addComponent(imageFileLabel)
			.addComponent(screenWidthLabel).addComponent(screenHeightLabel).addComponent(useBackupColorsLabel).addComponent(tileSizeLabel).addComponent(groutSizeLabel).addComponent(offsetRowsLabel)
			.addComponent(okButton));
			hGroup.addGroup(layout.createParallelGroup().addComponent(muralName).addComponent(replace).addComponent(paletteName).addComponent(imageFileButton).addComponent(screenWidth)
			.addComponent(screenHeight).addComponent(useBackupColors).addComponent(tileSize).addComponent(groutSize).addComponent(offsetRows).addComponent(cancelButton));
			layout.setHorizontalGroup(hGroup);

			GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(muralNameLabel).addComponent(muralName));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(replaceLabel).addComponent(replace));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(paletteNameLabel).addComponent(paletteName));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(imageFileLabel).addComponent(imageFileButton));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(screenWidthLabel).addComponent(screenWidth));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(screenHeightLabel).addComponent(screenHeight));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(useBackupColorsLabel).addComponent(useBackupColors));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(tileSizeLabel).addComponent(tileSize));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(groutSizeLabel).addComponent(groutSize));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(offsetRowsLabel).addComponent(offsetRows));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(okButton).addComponent(cancelButton));
			layout.setVerticalGroup(vGroup);

			pack();

		}
	}

	private static class ImportMuralDialog extends JDialog {

		private static final long serialVersionUID = 7990165245602672354L;
		private File imageFile = null;
		private BufferedImage image = null;

		private static final boolean checkForm(final JCheckBox box, final JTextField field) {

			boolean verified = false;
			boolean replaced = box.isSelected();

			String name = field.getText();

			if("".equals(name)) {
				return true; // bail on empty name field
			}

			boolean nameUsed = false;
			try {
				nameUsed = DATA_MANAGER.namesOfMural().contains(name);
			} catch(RemoteException re) {
				TilepileUtils.exceptionReport(re);
			}

			if(nameUsed && !replaced) {
				field.setBackground(Color.RED);
				box.setBackground(Color.RED);
				int results = JOptionPane.showConfirmDialog(box, "Mural name '" + name + "' already in use.  Replace?", "Mural Name Error", JOptionPane.YES_NO_OPTION);
				if(results == JOptionPane.YES_OPTION) {
					box.setSelected(true);
					verified = true;
				} else {
					verified = false;
				}
			} else if(!nameUsed && replaced) {
				field.setBackground(Color.RED);
				box.setBackground(Color.RED);
				int results = JOptionPane.showConfirmDialog(box, "Mural name '" + name + "' is NOT used, but replace is selected.  Deselect?", "Mural Name Error", JOptionPane.YES_NO_OPTION);
				if(results == JOptionPane.YES_OPTION) {
					box.setSelected(false);
					verified = true;
				} else {
					verified = false;
				}
			} else {
				verified = true;
			}
			return verified;
		}

		public ImportMuralDialog(final JFrame frame) throws RemoteException {

			super(frame, "Import Mural", true);

			final String lastFile = PACKAGE_PREFS.get(PREF_KEY_LAST_MURAL_IMAGE_FILE, null);

			if(lastFile != null) {
				imageFile = new File(lastFile);
				try {
					image = ImageIO.read(imageFile);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}

			final JPanel pane = new JPanel();
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(pane, BorderLayout.WEST);

			final ImagePreviewPanel imagePreview = new ImagePreviewPanel();
			getContentPane().add(imagePreview, BorderLayout.CENTER);

			JLabel replaceLabel = new JLabel("Replace Mural");
			final JCheckBox replace = new JCheckBox();

			JLabel muralNameLabel = new JLabel("Mural Name");
			final JTextField muralName = new JTextField();

			JLabel paletteNameLabel = new JLabel("Palette Name");
			final JComboBox paletteName = new JComboBox(DATA_MANAGER.namesOfPalette().toArray());

			JLabel gridWidthLabel = new JLabel("Grid Width");
			final JTextField gridWidth = new JTextField("15");
			gridWidth.setInputVerifier(new InputVerifier() {
				private final Color defaultColor = gridWidth.getBackground();

				@Override
				public boolean verify(JComponent input) {
					JTextField field = (JTextField) input;
					boolean parsed = true;
					try {
						Integer.parseInt(field.getText());
						field.setBackground(defaultColor);
					} catch(NumberFormatException e) {
						parsed = false;
						field.setBackground(Color.RED);
						JOptionPane.showMessageDialog(ImportMuralDialog.this, "Please enter an integer", "Grid Width Error", JOptionPane.ERROR_MESSAGE);
					}
					return parsed;
				}
			});

			JLabel gridHeightLabel = new JLabel("Grid Height");
			final JTextField gridHeight = new JTextField("15");
			gridHeight.setInputVerifier(new InputVerifier() {
				private final Color defaultColor = gridHeight.getBackground();

				@Override
				public boolean verify(JComponent input) {
					JTextField field = (JTextField) input;
					boolean parsed = true;
					try {
						Integer.parseInt(field.getText());
						field.setBackground(defaultColor);
					} catch(NumberFormatException e) {
						parsed = false;
						field.setBackground(Color.RED);
						JOptionPane.showMessageDialog(ImportMuralDialog.this, "Please enter an integer", "Grid Height Error", JOptionPane.ERROR_MESSAGE);
					}
					return parsed;
				}
			});

			final JCheckBox offsetRows = new JCheckBox("Offset Rows");

			JLabel imageFileLabel = new JLabel("Image File");
			JButton imageFileButton = new JButton("Browse");
			imageFileButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();

					if(lastFile != null) {
						chooser.setSelectedFile(new File(lastFile));
					}

					chooser.setFileFilter(new FileFilter() {

						@Override
						public boolean accept(File f) {
							return f.isDirectory() || f.getName().toUpperCase().endsWith(".PNG") || f.getName().toUpperCase().endsWith(".JPG");
						}

						@Override
						public String getDescription() {
							return "Image File";
						}
					});

					int returnVal = chooser.showOpenDialog(frame);
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						imageFile = chooser.getSelectedFile();
						PACKAGE_PREFS.put(PREF_KEY_LAST_MURAL_IMAGE_FILE, imageFile.getAbsolutePath());
						try {
							image = ImageIO.read(imageFile);
						} catch(IOException e1) {
							TilepileUtils.exceptionReport(e1);
						}

						if(image == null) {
							JOptionPane.showMessageDialog(desktop, "Cannot read image format: " + imageFile, "Invalid image file", JOptionPane.ERROR_MESSAGE);
							return;
						}
						imagePreview.setImage(image);
					}
				}
			});

			JLabel pixelToTileLabel = new JLabel("Pixel to Tile");
			final JCheckBox pixelToTile = new JCheckBox();

			JLabel useBackupColorsLabel = new JLabel("Use backup colors");
			final JCheckBox useBackupColors = new JCheckBox();

			JLabel muralHeightLabel = new JLabel("Mural Height");
			final UnitField muralHeight = new UnitField(0.0, UnitField.Units.FEET);

			JLabel tileSizeLabel = new JLabel("Tile Size");
			final UnitField tileSize = new UnitField();

			JLabel groutSizeLabel = new JLabel("Grout Size");
			final UnitField groutSize = new UnitField();

			replace.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {

					boolean rep = replace.isSelected();

					paletteName.setEnabled(!rep);
					gridWidth.setEnabled(!rep);
					gridHeight.setEnabled(!rep);

					boolean ptt = pixelToTile.isSelected();

					muralHeight.setEnabled(!(rep || ptt));
					tileSize.setEnabled(!(rep || ptt));
					groutSize.setEnabled(!(rep || ptt));
					useBackupColors.setEnabled(ptt);
				}
			});

			pixelToTile.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {

					boolean rep = replace.isSelected();
					boolean ptt = pixelToTile.isSelected();

					muralHeight.setEnabled(!(rep || ptt));
					tileSize.setEnabled(!(rep || ptt));
					groutSize.setEnabled(!(rep || ptt));
					useBackupColors.setEnabled(ptt);
				}
			});

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ImportMuralDialog.this.setVisible(false);
				}
			});

			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					if(!checkForm(replace, muralName)) {
						return;
					}

					if(imageFile == null) {
						JOptionPane.showMessageDialog(desktop, "Please select a mural image file", "No mural image file selected", JOptionPane.ERROR_MESSAGE);
					} else {

						try {

							Mural mural = null;

							String name = muralName.getText();

							if(replace.isSelected()) {

								if(DATA_MANAGER.namesOfMural().contains(name)) {
									Mural m = DATA_MANAGER.getMural(name);
									Palette palette = DATA_MANAGER.getPalette(m.getPaletteName());
									mural = MuralFactory.replace(m, image, palette);
								} else {
									JOptionPane.showMessageDialog(desktop, "'" + name + "' is not an existing mural.  Please enter an existing mural name or deselect 'Replace'", "Invalid mural name",
									JOptionPane.ERROR_MESSAGE);
								}
							} else {

								String selectedPaletteName = (String) paletteName.getSelectedItem();

								if(selectedPaletteName == null || "".equals(selectedPaletteName.trim())) {
									if(JOptionPane.showConfirmDialog(desktop, "Palette name is blank.  Do you wish to create a palette from the image?", "Blank Palette", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

										selectedPaletteName = JOptionPane.showInputDialog(desktop, "What is the new palette name?", name + "-autoPalette");

										if(name == null) {
											JOptionPane.showMessageDialog(desktop, "Cancelling mural import due to blank palette", "Invalid Palette", JOptionPane.ERROR_MESSAGE);
											return;
										}

										selectedPaletteName = name + "-palette";
										Palette p = PaletteFactory.make(selectedPaletteName, image);
										DATA_MANAGER.addPalette(p);
									} else {
										JOptionPane.showMessageDialog(desktop, "Cancelling mural import due to blank palette", "Invalid Palette", JOptionPane.ERROR_MESSAGE);
										return;
									}
								}

								Palette palette = DATA_MANAGER.getPalette(selectedPaletteName);

								if(pixelToTile.isSelected()) {

									mural = MuralFactory.make(name, Integer.parseInt(gridWidth.getText()), Integer.parseInt(gridHeight.getText()), offsetRows.isSelected(), image, palette,
									useBackupColors.isSelected());

								} else {

									double height = muralHeight.getAmountInInches();
									double tile = tileSize.getAmountInInches();
									double grout = groutSize.getAmountInInches();

									if(JOptionPane.showConfirmDialog(desktop,
									String.format("Your mural will be %f feet tall and %f feet wide. Is this correct?", height / 12, height / image.getHeight() * image.getWidth() / 12),
									"Is this correct?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
										mural = MuralFactory.make(name, Integer.parseInt(gridWidth.getText()), Integer.parseInt(gridHeight.getText()), offsetRows.isSelected(), image, palette, height,
										tile, grout);
									}
								}
							}

							if(mural != null) {
								DATA_MANAGER.addMural(mural);
								addMuralMenuItems(name);
								ImportMuralDialog.this.setVisible(false);
							}

						} catch(NumberFormatException nfe) {
							TilepileUtils.exceptionReport(nfe);
						} catch(RemoteException re) {
							TilepileUtils.exceptionReport(re);
						} catch(IOException ioe) {
							TilepileUtils.exceptionReport(ioe);
						} catch(TilepileException tpe) {
							TilepileUtils.exceptionReport(tpe);
						}
					}
				}
			});

			JLabel offsetRowsLabel = new JLabel();
			GroupLayout layout = new GroupLayout(pane);
			pane.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
			hGroup.addGroup(layout.createParallelGroup().addComponent(muralNameLabel).addComponent(replaceLabel).addComponent(paletteNameLabel).addComponent(imageFileLabel)
			.addComponent(gridWidthLabel).addComponent(gridHeightLabel).addComponent(pixelToTileLabel).addComponent(useBackupColorsLabel).addComponent(muralHeightLabel).addComponent(tileSizeLabel)
			.addComponent(groutSizeLabel).addComponent(offsetRowsLabel).addComponent(okButton));
			hGroup.addGroup(layout.createParallelGroup().addComponent(muralName).addComponent(replace).addComponent(paletteName).addComponent(imageFileButton).addComponent(gridWidth)
			.addComponent(gridHeight).addComponent(pixelToTile).addComponent(useBackupColors).addComponent(muralHeight).addComponent(tileSize).addComponent(groutSize).addComponent(offsetRows)
			.addComponent(cancelButton));
			layout.setHorizontalGroup(hGroup);

			GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(muralNameLabel).addComponent(muralName));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(replaceLabel).addComponent(replace));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(paletteNameLabel).addComponent(paletteName));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(imageFileLabel).addComponent(imageFileButton));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(gridWidthLabel).addComponent(gridWidth));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(gridHeightLabel).addComponent(gridHeight));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(pixelToTileLabel).addComponent(pixelToTile));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(useBackupColorsLabel).addComponent(useBackupColors));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(muralHeightLabel).addComponent(muralHeight));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(tileSizeLabel).addComponent(tileSize));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(groutSizeLabel).addComponent(groutSize));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(offsetRowsLabel).addComponent(offsetRows));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(okButton).addComponent(cancelButton));
			layout.setVerticalGroup(vGroup);

			pack();
		}
	}

	private static class UnitField extends JPanel {

		private static final long serialVersionUID = 2644351297558025689L;

		public enum Units {
			INCHES {
				@Override
				public double toInches(double in) {
					return in;
				}

				@Override
				public String toString() {
					return "IN";
				}
			},
			FEET {
				@Override
				public double toInches(double in) {
					return in * 12;
				}

				@Override
				public String toString() {
					return "FT";
				}
			},
			CENTIMETERS {
				@Override
				public double toInches(double in) {
					return in / 2.54;
				}

				@Override
				public String toString() {
					return "CM";
				}
			},
			MILLIMETERS {
				@Override
				public double toInches(double in) {
					return in / 2.54 / 10;
				}

				@Override
				public String toString() {
					return "MM";
				}
			};

			public abstract double toInches(double in);
		}

		private final JTextField amountField = new JTextField();
		private final JComboBox unitsField = new JComboBox(Units.values());

		public UnitField() {
			this(0.0, Units.INCHES);
		}

		public UnitField(double amount, Units units) {
			super(new BorderLayout());
			amountField.setText(Double.toString(amount));
			amountField.setColumns(4);
			unitsField.setSelectedItem(units);
			amountField.setInputVerifier(new InputVerifier() {
				private final Color defaultColor = amountField.getBackground();

				@Override
				public boolean verify(JComponent input) {
					JTextField field = (JTextField) input;
					boolean parsed = true;
					try {
						Double.parseDouble(field.getText());
						field.setBackground(defaultColor);
					} catch(NumberFormatException e) {
						parsed = false;
						field.setBackground(Color.RED);
						JOptionPane.showMessageDialog(UnitField.this, "Please enter a number", "Unit Field Error", JOptionPane.ERROR_MESSAGE);
					}
					return parsed;
				}
			});

			add(amountField, BorderLayout.CENTER);
			add(unitsField, BorderLayout.EAST);
		}

		public double getAmount() {
			return Double.parseDouble(amountField.getText());
		}

		public double getAmountInInches() {
			return getUnits().toInches(getAmount());
		}

		public Units getUnits() {
			return (Units) unitsField.getSelectedItem();
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			for(Component component : getComponents()) {
				component.setEnabled(enabled);
			}
		}
	}

	public static class ImagePreviewPanel extends JPanel {

		private static final long serialVersionUID = 6455990563398366847L;

		private BufferedImage image = null;
		private final JScrollPane scrollPane;
		private final JComponent imagePanel;

		public ImagePreviewPanel() {
			setLayout(new BorderLayout());
			imagePanel = new JComponent() {

				private static final long serialVersionUID = -2297626321730885154L;

				@Override
				public void update(Graphics g) {
					g.clearRect(0, 0, getWidth(), getHeight());
					paintComponent(g);
				}

				@Override
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					Graphics2D g2D = (Graphics2D) g;
					if(image != null) {
						g2D.drawRenderedImage(image, null);
					}
				}
			};

			scrollPane = new JScrollPane(imagePanel);
			add(scrollPane, BorderLayout.CENTER);
		}

		public void setImage(final BufferedImage image) {
			this.image = image;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					imagePanel.setSize(image.getWidth(), image.getHeight());
					imagePanel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
					imagePanel.repaint();
					scrollPane.invalidate();
					scrollPane.validate();
				}
			});
		}

	}
}
