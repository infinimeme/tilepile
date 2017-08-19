package com.infinimeme.tilepile.station;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.filechooser.FileFilter;

import com.infinimeme.tilepile.common.Location;
import com.infinimeme.tilepile.common.MainStation;
import com.infinimeme.tilepile.common.MainStationRemoteImpl;
import com.infinimeme.tilepile.common.MainStationRemoteListener;
import com.infinimeme.tilepile.common.Mural;
import com.infinimeme.tilepile.common.MuralLocation;
import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.States;
import com.infinimeme.tilepile.common.StatesFactory;
import com.infinimeme.tilepile.common.Station;
import com.infinimeme.tilepile.common.StationRemote;
import com.infinimeme.tilepile.common.TilepileException;
import com.infinimeme.tilepile.common.TilepileUtils;
import com.infinimeme.tilepile.data.DataConstants;
import com.infinimeme.tilepile.data.DataManagerRemote;
import com.infinimeme.tilepile.data.DataUtils;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 */
public class MuralFrame extends JInternalFrame implements MainStationRemoteListener {

	// ~ Instance fields
	// ****************************************************************************

	private static final long serialVersionUID = 1L;

	private DataManagerRemote dataManager = null;

	private MainStation mainStation = null;

	private MainStationRemoteImpl mainStationRemote = null;

	private Mural mural = null;

	private MuralSectionPanel[][] panels = null;

	private GraphicsConfiguration graphicsConfiguration = null;

	private static final int HGAP = 1;
	private static final int VGAP = 1;

	private static final boolean DEFAULT_FINISHED = true;
	private static final boolean DEFAULT_LOCKED = false;
	private static final boolean DEFAULT_STOCK = false;
	private static final boolean DEFAULT_GRID = false;

	private static final int DEFAULT_SHOW_STATE = StatefulSectionPanel.SHOW_FINISHED;

	private boolean showFinished = DEFAULT_FINISHED;
	private boolean showLocked = DEFAULT_LOCKED;
	private boolean showStock = DEFAULT_STOCK;
	private boolean gridActive = DEFAULT_GRID;

	// ~ Constructors
	// *******************************************************************************

	/**
	 * Creates a new MuralFrame object.
	 * 
	 * @param mural
	mainStation
	mainStationRemote
	dataManager
	TilepileException
	*/
	public MuralFrame(final Mural mural, MainStation mainStation, MainStationRemoteImpl mainStationRemote, final DataManagerRemote dataManager, GraphicsConfiguration graphicsConfiguration)
	throws TilepileException, RemoteException {
		setMural(mural);
		setMainStation(mainStation);
		setMainStationRemote(mainStationRemote);
		setDataManager(dataManager);
		setGraphicsConfiguration(graphicsConfiguration);

		Container content = getContentPane();

		content.setLayout(new BorderLayout());

		final JPanel muralPanel = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {

				super.paint(g);

				if(gridActive) {

					Graphics2D g2d = (Graphics2D) g;

					double panelHeight = panels[0][0].getHeight() + VGAP;
					double panelWidth = panels[0][0].getWidth() + HGAP;

					double gridHeight = panelHeight * 4;
					double gridWidth = panelWidth * 5;

					Rectangle2D.Double rec = new Rectangle2D.Double(0, 0, gridWidth, gridHeight);
					Color lightGray = new Color(192, 192, 192, 96);
					Color darkGray = new Color(64, 64, 64, 96);
					Color numColor = new Color(255, 255, 255, 224);

					Object antialiasingHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					FontRenderContext frc = g2d.getFontRenderContext();
					Font font = new Font("MONOSPACED", Font.BOLD, (int) (Math.min(gridHeight, gridWidth) / 2));

					int index = 0;

					int gridX = (int) Math.ceil((double) panels[0].length / (double) 5);
					int gridY = (int) Math.ceil((double) panels.length / (double) 4);

					for(int i = 0; i < gridX; i++) {

						rec.x = gridWidth * i + panelWidth;

						for(int j = 0; j < gridY; j++) {

							rec.y = gridHeight * j + panelHeight;

							if((i + j) % 2 == 0) {
								g2d.setColor(lightGray);
							} else {
								g2d.setColor(darkGray);
							}

							g2d.fill(rec);

							String num = Integer.toString(++index);

							AttributedString as = new AttributedString(num);
							as.addAttribute(TextAttribute.FONT, font, 0, num.length());

							Shape text = new TextLayout(as.getIterator(), frc).getOutline(AffineTransform.getTranslateInstance(rec.x + rec.width / 2 - num.length() * font.getSize2D() / 3, rec.y
							+ rec.height / 2 + font.getSize2D() / 3));

							g2d.setColor(numColor);
							g2d.fill(text);
						}
					}

					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasingHint);
				}
			}
		};

		content.add(new JScrollPane(muralPanel), BorderLayout.CENTER);

		final JLabel statusLabel = new JLabel("Status");

		content.add(statusLabel, BorderLayout.SOUTH);

		Mural.Section[][] sections = mural.getSections();

		GridLayout layout = new GridLayout(sections.length + 2, sections[0].length + 2);

		layout.setHgap(HGAP);
		layout.setVgap(VGAP);
		muralPanel.setLayout(layout);

		final List<JLabel> xUpperLabels = new ArrayList<JLabel>();
		final List<JLabel> yUpperLabels = new ArrayList<JLabel>();
		final List<JLabel> xLowerLabels = new ArrayList<JLabel>();
		final List<JLabel> yLowerLabels = new ArrayList<JLabel>();

		JLabel lblX1 = new JLabel("X");
		lblX1.setVerticalAlignment(SwingConstants.CENTER);
		lblX1.setHorizontalAlignment(SwingConstants.CENTER);

		final Color defaultLabelColor = lblX1.getBackground();

		lblX1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				if(e.getClickCount() == 1) {

					for(JLabel label : xUpperLabels) {
						label.setBackground(defaultLabelColor);
					}

					for(JLabel label : yUpperLabels) {
						label.setBackground(defaultLabelColor);
					}

					for(JLabel label : xLowerLabels) {
						label.setBackground(defaultLabelColor);
					}

					for(JLabel label : yLowerLabels) {
						label.setBackground(defaultLabelColor);
					}
				}
			}
		});

		muralPanel.add(lblX1);

		Font font = lblX1.getFont().deriveFont(lblX1.getFont().getSize2D() / (float) 1.25);

		for(int x = 0; x < sections[0].length; x++) {

			JLabel label = new JLabel(Integer.toString(x));
			xUpperLabels.add(label);
			label.setOpaque(true);
			label.setVerticalAlignment(SwingConstants.CENTER);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setFont(font);
			muralPanel.add(label);
		}

		JLabel lblX2 = new JLabel("X");
		lblX2.setVerticalAlignment(SwingConstants.CENTER);
		lblX2.setHorizontalAlignment(SwingConstants.CENTER);

		muralPanel.add(lblX2);

		panels = new MuralSectionPanel[sections.length][];

		final States states = getOrMakeStates(dataManager, mural);

		Palette palette = dataManager.getPalette(mural.getPaletteName());

		for(int y = 0; y < sections.length; y++) {

			JLabel labelUpper = new JLabel(TilepileUtils.indexToCharacter(y));
			JLabel labelLower = new JLabel(TilepileUtils.indexToCharacter(y));
			labelUpper.setOpaque(true);
			labelLower.setOpaque(true);
			labelUpper.setVerticalAlignment(SwingConstants.CENTER);
			labelLower.setVerticalAlignment(SwingConstants.CENTER);
			labelUpper.setHorizontalAlignment(SwingConstants.CENTER);
			labelLower.setHorizontalAlignment(SwingConstants.CENTER);
			labelUpper.setFont(font);
			labelLower.setFont(font);
			yUpperLabels.add(labelUpper);
			yLowerLabels.add(labelLower);
			muralPanel.add(labelUpper);

			panels[y] = new MuralSectionPanel[sections[y].length];

			for(int x = 0; x < sections[y].length; x++) {

				final Mural.Section section = sections[y][x];

				final int gridX = x;
				final int gridY = y;

				final MuralSectionPanel panel = new MuralSectionPanel(section, palette, false, states.get(x, y), getGraphicsConfiguration(), x, y);
				panel.activateShowStates(DEFAULT_SHOW_STATE);

				panels[y][x] = panel;

				panel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {

						if(gridActive) {
							return;
						}

						TilepileUtils.getLogger().info("Mouse modifiers: " + MouseEvent.getModifiersExText(e.getModifiersEx()));

						if(e.isMetaDown()) {

							TilepileUtils.getLogger().info("Exporting " + gridX + '-' + gridY);

							JFileChooser chooser = new JFileChooser("Get output directory");
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							chooser.setAcceptAllFileFilterUsed(false);
							int returnVal = chooser.showSaveDialog(MuralFrame.this);
							if(returnVal == JFileChooser.APPROVE_OPTION) {

								DynamicSectionPanel bigPanel = new DynamicSectionPanel(section, panel.getPalette(), false);
								bigPanel.setShowNumbers(true);

								int width = section.getWidth() * 100;
								int height = section.getHeight() * 100;

								bigPanel.setSize(width, height);
								File directory = chooser.getSelectedFile();

								Map<Integer, Mural.Counter> histogram = section.getHistogram();
								for(final Integer key : histogram.keySet()) {

									bigPanel.resetActiveColors();
									bigPanel.addActiveColor(key.intValue());

									BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
									bigPanel.paint(bi.getGraphics());
									try {
										ImageIO.write(bi, "png", new File(directory, mural.getName() + "-" + gridX + "-" + gridY + "-" + panel.getPalette().getName(key) + ".png"));
									} catch(IOException e1) {
										e1.printStackTrace();
									}
								}
							}

						} else if(e.isControlDown()) {

							TilepileUtils.getLogger().info("Locking " + gridX + '-' + gridY);
							states.toggleLocked(gridX, gridY);
							panel.setState(states.get(gridX, gridY));
							panel.repaint();

							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										dataManager.setStates(states);
									} catch(RemoteException re) {
										TilepileUtils.exceptionReport(re);
									}
								}
							}).start();

						} else if(e.isShiftDown()) {

							TilepileUtils.getLogger().info("Finishing " + gridX + '-' + gridY);
							states.toggleFinished(gridX, gridY);
							panel.setState(states.get(gridX, gridY));
							panel.repaint();

							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										dataManager.setStates(states);
									} catch(RemoteException re) {
										TilepileUtils.exceptionReport(re);
									}
								}
							}).start();

						} else if(e.getClickCount() == 1) {

							for(JLabel label : xUpperLabels) {
								label.setBackground(defaultLabelColor);
							}

							for(JLabel label : yUpperLabels) {
								label.setBackground(defaultLabelColor);
							}

							for(JLabel label : xLowerLabels) {
								label.setBackground(defaultLabelColor);
							}

							for(JLabel label : yLowerLabels) {
								label.setBackground(defaultLabelColor);
							}

							xUpperLabels.get(gridX).setBackground(Color.RED);
							yUpperLabels.get(gridY).setBackground(Color.RED);
							xLowerLabels.get(gridX).setBackground(Color.RED);
							yLowerLabels.get(gridY).setBackground(Color.RED);

						} else if(e.getClickCount() == 2 && !states.isLocked(gridX, gridY)) {

							try {
								if(states.isFinished(gridX, gridY)) {

									int decision = JOptionPane.showConfirmDialog(panel, "This section has already been finished.  Continue?");

									if(decision != JOptionPane.YES_OPTION) {

										return;
									}
								}

								Collection<Station> stations = dataManager.instancesOfStation();

								Frame parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, MuralFrame.this);

								final JDialog dialog = new JDialog(parentFrame, "Pick Station", true);

								Rectangle pfBounds = parentFrame.getBounds();

								dialog.setBounds(pfBounds.x + pfBounds.width / 4, pfBounds.y + pfBounds.height / 4, pfBounds.width / 2, pfBounds.height / 2);

								Container dialogContent = dialog.getContentPane();

								List<JButton> stationButtons = new LinkedList<JButton>();

								for(final Station station : stations) {

									try {

										final StationRemote stationRemote = (StationRemote) Naming.lookup(station.getRemoteName());

										// Include only Stations that are idle
										if(stationRemote.getMuralLocation() == null) {

											JButton stationButton = new JButton(Integer.toString(station.getNumber()));
											Color stationColor = station.getColor();
											stationButton.setFont(new Font("MONOSPACED", Font.BOLD, 32));

											stationButton.setBackground(stationColor);
											stationButton.setForeground(TilepileUtils.getContrasting(stationColor));

											stationButton.addActionListener(new ActionListener() {
												@Override
												public void actionPerformed(ActionEvent e) {

													try {

														String stationName = station.getName();

														MuralLocation muralLocation = new MuralLocation(new Location(gridX, gridY), getMural().getName());

														stationRemote.setMuralLocation(getMainStation().getName(), muralLocation);
														TilepileUtils.logInfo("Station " + stationName + " assigned " + muralLocation);
													} catch(RemoteException re) {
														TilepileUtils.exceptionReport(re);
													}

													panel.setStation(station);

													panel.repaint();

													dialog.setVisible(false);
												}
											});

											stationButtons.add(stationButton);
										}
									} catch(RemoteException re) {
										TilepileUtils.exceptionReport(re);
										dataManager.removeStation(station.getName());
									} catch(NotBoundException nbe) {
										TilepileUtils.exceptionReport(nbe);
									} catch(MalformedURLException mue) {
										TilepileUtils.exceptionReport(mue);
									}
								}

								if(stationButtons.size() > 0) {

									dialogContent.setLayout(new GridLayout(stationButtons.size(), 1));

									for(JButton button : stationButtons) {
										dialogContent.add(button);
									}

									dialog.setVisible(true);
								} else {
									JOptionPane.showMessageDialog(parentFrame, "No available substations on network", "Open a substation", JOptionPane.WARNING_MESSAGE);
								}

							} catch(RemoteException re) {
								TilepileUtils.exceptionReport(re);
							}
						}
					}
				});

				muralPanel.add(panel);
			}

			muralPanel.add(labelLower);
		}

		// Check active stations to see of they're working on a section.
		// This can take a while, so do it in a separate thread.
		new Thread() {
			@Override
			public void run() {

				try {
					Collection<Station> stations = dataManager.instancesOfStation();

					for(Station station : stations) {

						try {
							TilepileUtils.getLogger().info("Looking up " + station.getRemoteName());

							StationRemote stationRemote = (StationRemote) Naming.lookup(station.getRemoteName());

							MuralLocation muralLocation = stationRemote.getMuralLocation();

							// Find Stations that are working on a section of
							// current mural
							if(muralLocation != null && muralLocation.getMuralName().equals(mural.getName())) {

								Location location = muralLocation.getLocation();
								StatefulSectionPanel panel = panels[location.getY()][location.getX()];
								panel.setStation(station);
								panel.repaint();
							}
						} catch(RemoteException re) {
							TilepileUtils.logWarning("Removed station " + station.getName(), re);
							dataManager.removeStation(station.getName());
						} catch(NotBoundException nbe) {
							TilepileUtils.exceptionReport(nbe);
						} catch(MalformedURLException mue) {
							TilepileUtils.exceptionReport(mue);
						}
					}
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				}
			}
		}.start();

		JLabel lblX3 = new JLabel("X");
		lblX3.setVerticalAlignment(SwingConstants.CENTER);
		lblX3.setHorizontalAlignment(SwingConstants.CENTER);
		muralPanel.add(lblX3);

		for(int x = 0; x < sections[0].length; x++) {

			JLabel label = new JLabel(Integer.toString(x));
			xLowerLabels.add(label);
			label.setVerticalAlignment(SwingConstants.CENTER);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setOpaque(true);
			label.setFont(font);
			muralPanel.add(label);
		}

		JLabel lblX4 = new JLabel("X");
		lblX4.setVerticalAlignment(SwingConstants.CENTER);
		lblX4.setHorizontalAlignment(SwingConstants.CENTER);
		muralPanel.add(lblX4);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem saveItem = new JMenuItem("Save Image");

		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Dimension size = muralPanel.getSize();
				BufferedImage image = new BufferedImage((int) size.getWidth(), (int) size.getHeight(), BufferedImage.TYPE_INT_RGB);
				muralPanel.printAll(image.getGraphics());
				try {
					ImageIO.write(image, "png", new File(mural.getName() + "_Grid.png"));
				} catch(IOException ioe) {
					TilepileUtils.exceptionReport(ioe);
				}
			}
		});

		fileMenu.add(saveItem);

		JMenuItem loadStatesItem = new JMenuItem("Load States");

		loadStatesItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				JFileChooser chooser = new JFileChooser();

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

						return f.isDirectory() || f.isFile() && f.getName().equals(mural.getName() + DataConstants.DATA_FILE_EXTENSION);
					}

					@Override
					public String getDescription() {

						return mural.getName() + DataConstants.DATA_FILE_EXTENSION + " States data file";
					}
				});

				int returnVal = chooser.showOpenDialog(MuralFrame.this);

				if(returnVal == JFileChooser.APPROVE_OPTION) {

					try {

						States newStates = DataUtils.readFile(chooser.getSelectedFile(), States.class);

						States oldStates = getDataManager().getStates(mural.getName());

						if(oldStates.add(newStates)) {
							getDataManager().setStates(oldStates);
						} else {
							JOptionPane.showInternalMessageDialog(MuralFrame.this, "File " + chooser.getSelectedFile().getName() + " incompatible with states from " + mural.getName(),
							"States files incompatible!", JOptionPane.ERROR_MESSAGE);
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

		fileMenu.add(loadStatesItem);

		JMenu statesMenu = new JMenu("States");
		menuBar.add(statesMenu);

		final JCheckBoxMenuItem gridItem = new JCheckBoxMenuItem("Grid", gridActive);
		gridItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gridActive = gridItem.getState();
				muralPanel.repaint();
			}
		});
		statesMenu.add(gridItem);

		final JCheckBoxMenuItem blockedItem = new JCheckBoxMenuItem("Blocked", showLocked);
		blockedItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(MuralSectionPanel[] panel : panels) {
					for(MuralSectionPanel element : panel) {
						if(blockedItem.getState()) {
							element.activateShowStates(MuralSectionPanel.SHOW_LOCKED);
						} else {
							element.deactivateShowStates(MuralSectionPanel.SHOW_LOCKED);
						}
					}
				}
				muralPanel.repaint();
			}
		});
		statesMenu.add(blockedItem);

		final JCheckBoxMenuItem fabricatedItem = new JCheckBoxMenuItem("Fabricated", showFinished);
		fabricatedItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(MuralSectionPanel[] panel : panels) {
					for(MuralSectionPanel element : panel) {
						if(fabricatedItem.getState()) {
							element.activateShowStates(MuralSectionPanel.SHOW_FINISHED);
						} else {
							element.deactivateShowStates(MuralSectionPanel.SHOW_FINISHED);
						}
					}
				}
				muralPanel.repaint();
			}
		});
		statesMenu.add(fabricatedItem);

		final JCheckBoxMenuItem stockItem = new JCheckBoxMenuItem("Stock", showStock);
		stockItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(MuralSectionPanel[] panel : panels) {
					for(MuralSectionPanel element : panel) {
						if(stockItem.getState()) {
							element.activateShowStates(MuralSectionPanel.SHOW_STOCK);
						} else {
							element.deactivateShowStates(MuralSectionPanel.SHOW_STOCK);
						}
					}
				}
				muralPanel.repaint();
			}
		});
		statesMenu.add(stockItem);

		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				getMainStationRemote().removeListener(MuralFrame.this);
			}
		});

		setBounds(0, 0, mural.getWidth() + 60, mural.getHeight() + 140);

		setResizable(true);
		setClosable(true);
		setIconifiable(true);
		setMaximizable(true);

		setVisible(true);

		muralPanel.validate();
		validate();
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

	public static final States getOrMakeStates(DataManagerRemote dataManager, Mural mural) throws RemoteException {

		States states = dataManager.getStates(mural.getName());

		if(states == null) {
			states = StatesFactory.make(mural);
			dataManager.addStates(states);
		}

		return states;
	}

	/**

	*/
	public Mural getMural() {
		return mural;
	}

	/**

	 * @param muralLocation
	*/
	@Override
	public void sectionReleased(MuralLocation muralLocation) {

		if(muralLocation.getMuralName().equals(getMural().getName())) {

			Location location = muralLocation.getLocation();
			StatefulSectionPanel panel = panels[location.getY()][location.getX()];
			panel.setStation(null);
			panel.setState(States.STATE_FINISHED);

			try {
				States states = getDataManager().getStates(mural.getName());
				states.setFinished(location);
				getDataManager().addStates(states);
				panel.repaint();
			} catch(RemoteException re) {
				TilepileUtils.exceptionReport(re);
			}
		}
	}

	/**

	 * @param dataManager
	*/
	private void setDataManager(DataManagerRemote dataManager) {
		this.dataManager = dataManager;
	}

	/**

	*/
	private DataManagerRemote getDataManager() {
		return dataManager;
	}

	/**

	 * @param mainStation
	*/
	private void setMainStation(MainStation mainStation) {
		this.mainStation = mainStation;
	}

	/**

	*/
	private MainStation getMainStation() {
		return mainStation;
	}

	/**

	 * @param mainStationRemote
	*/
	private void setMainStationRemote(MainStationRemoteImpl mainStationRemote) {
		this.mainStationRemote = mainStationRemote;
		mainStationRemote.addListener(this);
	}

	/**

	*/
	private MainStationRemoteImpl getMainStationRemote() {
		return mainStationRemote;
	}

	/**

	 * @param mural
	*/
	private void setMural(Mural mural) {
		this.mural = mural;
	}
}
