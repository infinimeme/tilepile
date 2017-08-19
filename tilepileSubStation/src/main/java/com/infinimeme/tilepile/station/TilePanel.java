package com.infinimeme.tilepile.station;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.infinimeme.tilepile.common.Location;
import com.infinimeme.tilepile.common.MainStation;
import com.infinimeme.tilepile.common.MainStationRemote;
import com.infinimeme.tilepile.common.Mural;
import com.infinimeme.tilepile.common.MuralLocation;
import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.Station;
import com.infinimeme.tilepile.common.StationRemoteImpl;
import com.infinimeme.tilepile.common.TilepileException;
import com.infinimeme.tilepile.common.TilepileUtils;
import com.infinimeme.tilepile.data.DataManagerRemote;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 */
public class TilePanel extends JPanel {

	// ~ Static fields/initializers
	// *****************************************************************

	private static final long serialVersionUID = 6278957919666906593L;

	private static final String NO_COLOR_LABEL = "   ";

	public JDialog controlsDialog;

	// ~ Constructors
	// *******************************************************************************

	public TilePanel(SubApp app, final MuralLocation muralLocation, final DynamicSectionPanel panel, final Station station, final StationRemoteImpl stationRemote, final MainStation mainStation,
	final DataManagerRemote dataManager) throws TilepileException, RemoteException {
		super(false);

		// Panel that holds frame controls
		final JPanel controlPanel = new JPanel();

		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

		controlPanel.setBackground(station.getColor());

		Location location = muralLocation.getLocation();

		JPanel upperPanel = new JPanel();

		upperPanel.setLayout(new GridLayout(2, 2));

		upperPanel.setBackground(station.getColor());

		controlPanel.add(upperPanel);

		JLabel lblSectionLocation = new JLabel(location.getX() + ":" + TilepileUtils.indexToCharacter(location.getY()));

		lblSectionLocation.setBackground(station.getColor());
		lblSectionLocation.setOpaque(true);

		lblSectionLocation.setAlignmentX(Component.CENTER_ALIGNMENT);

		upperPanel.add(lblSectionLocation);

		// The current color label shows the current color being dispalyed in
		// the TilePanel
		final JLabel[] lblsCurrentColor = {
		new JLabel(NO_COLOR_LABEL, JLabel.CENTER), new JLabel(NO_COLOR_LABEL, JLabel.CENTER), new JLabel(NO_COLOR_LABEL, JLabel.CENTER)
		};

		for(JLabel lblCurrentColor : lblsCurrentColor) {
			lblCurrentColor.setOpaque(true);

			lblCurrentColor.setAlignmentX(Component.CENTER_ALIGNMENT);
			lblCurrentColor.setHorizontalTextPosition(JLabel.CENTER);
		}

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(true);

		// The Finished button indicates that all tiles have been
		// placed in the grid
		final JButton btnFinished = new JButton("Finished");
		btnFinished.addKeyListener(app.globalKeyListener);
		btnFinished.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				try {

					MainStationRemote mainStationRemote = (MainStationRemote) Naming.lookup(mainStation.getRemoteName());

					mainStationRemote.releaseSection(muralLocation);

					stationRemote.setMuralLocation(mainStation.getName(), null);

				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				} catch(NotBoundException nbe) {
					TilepileUtils.exceptionReport(nbe);
				} catch(MalformedURLException mue) {
					TilepileUtils.exceptionReport(mue);
				}
			}
		});

		btnFinished.setAlignmentX(Component.CENTER_ALIGNMENT);

		upperPanel.add(btnFinished);

		final ButtonGroup colorGroup = new ButtonGroup();

		final JButton btnShowAll = new JButton("Show All");
		btnShowAll.addKeyListener(app.globalKeyListener);
		// The "Show All" button causes the TilePanel to show all colors
		btnShowAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.resetActiveColors();
				for(JLabel lblCurrentColor : lblsCurrentColor) {
					lblCurrentColor.setText(NO_COLOR_LABEL);
					lblCurrentColor.setForeground(Color.BLACK);
					lblCurrentColor.setBackground(controlPanel.getBackground());
				}
				repaint();

				ButtonModel model = colorGroup.getSelection();

				if(model != null) {
					colorGroup.setSelected(model, false);
				}
			}
		});

		btnShowAll.setAlignmentX(Component.CENTER_ALIGNMENT);

		upperPanel.add(btnShowAll);

		final JToggleButton togShowNumbers = new JToggleButton("Numbers");
		togShowNumbers.addKeyListener(app.globalKeyListener);
		// The "Numbers" button causes the TilePanel to display color numbers
		togShowNumbers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setShowNumbers(togShowNumbers.isSelected());
				panel.repaint();
			}
		});

		togShowNumbers.setAlignmentX(Component.CENTER_ALIGNMENT);

		upperPanel.add(togShowNumbers);

		final JToggleButton togMakeresizable = new JToggleButton("Resize");
		togMakeresizable.addKeyListener(app.globalKeyListener);
		// The "Numbers" button causes the TilePanel to display color numbers
		togMakeresizable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, TilePanel.this);
				if(togMakeresizable.isSelected()) {
					frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
				} else {
					frame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
				}
			}
		});

		togMakeresizable.setAlignmentX(Component.CENTER_ALIGNMENT);

		upperPanel.add(togMakeresizable);

		Mural.Section section = panel.getSection();

		final Palette palette = dataManager.getPalette(section.getMural().getPaletteName());

		Map<Integer, Mural.Counter> histogram = section.getHistogram();

		List<Mural.Counter> counters = new ArrayList<Mural.Counter>();

		counters.addAll(histogram.values());

		Collections.sort(counters);
		Collections.reverse(counters);

		buttonPanel.setLayout(new GridLayout(histogram.keySet().size(), 1));

		controlPanel.add(new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		final LinkedList<Integer> keyList = new LinkedList<Integer>();

		for(final Mural.Counter counter : counters) {

			keyList.add(counter.getColor());

			final Color color = palette.getColor(counter.getColor());
			final String name = palette.getName(counter.getColor());

			StringBuffer sb = new StringBuffer();
			sb.append(name);

			// Append number of tiles of each color to name
			// sb.append(" (");
			// sb.append(histogram.get(key));
			// sb.append(")");
			final JToggleButton button = new JToggleButton(sb.toString());
			colorGroup.add(button);
			button.addKeyListener(app.globalKeyListener);
			button.setBackground(color);

			button.setForeground(TilepileUtils.getContrasting(color));

			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					if(button.isSelected()) {

						// Tell TilePanel to display a color
						panel.resetActiveColors();
						panel.addActiveColor(counter.getColor());
						panel.repaint();

						boolean oneTextSet = false;
						// Report currently displayed color
						for(JLabel lblCurrentColor : lblsCurrentColor) {
							lblCurrentColor.setBackground(color);
							if(!oneTextSet) {
								lblCurrentColor.setText(name);
								// Set button text color depending on brightness
								lblCurrentColor.setForeground(TilepileUtils.getContrasting(color));
								oneTextSet = true;
							}
						}
					}
				}
			});

			buttonPanel.add(button);
		}

		setLayout(new BorderLayout());

		controlsDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "SubStation Controls", true);
		controlsDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		controlsDialog.getContentPane().setLayout(new BorderLayout());

		controlsDialog.getContentPane().add(controlPanel, BorderLayout.CENTER);
		controlsDialog.getContentPane().add(lblsCurrentColor[0], BorderLayout.SOUTH);
		controlsDialog.getContentPane().add(lblsCurrentColor[1], BorderLayout.WEST);
		controlsDialog.getContentPane().add(lblsCurrentColor[2], BorderLayout.EAST);

		add(panel, BorderLayout.CENTER);

		getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
		getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), "UP");
		getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "UP");
		getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "UP");
		getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DOWN");
		getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "DOWN");
		getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "DOWN");
		getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "DOWN");
		getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "FINISHED");
		getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "SHOWALL");
		getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "NUMBERS");

		getActionMap().put("UP", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {

				ButtonModel selected = colorGroup.getSelection();

				if(selected != null) {

					AbstractButton prev = null;

					for(Enumeration<AbstractButton> en = colorGroup.getElements(); en.hasMoreElements();) {

						AbstractButton button = en.nextElement();

						if(button.getModel().equals(selected)) {

							break;
						}
						prev = button;
					}

					if(prev != null) {
						prev.doClick();
					}
				}
			}
		});

		getActionMap().put("DOWN", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {

				ButtonModel selected = colorGroup.getSelection();

				AbstractButton next = null;

				if(selected == null) {

					next = colorGroup.getElements().nextElement();

				} else {

					for(Enumeration<AbstractButton> en = colorGroup.getElements(); en.hasMoreElements();) {

						AbstractButton button = en.nextElement();

						if(button.getModel().equals(selected)) {

							if(en.hasMoreElements()) {
								next = en.nextElement();

								break;
							}
						}
					}
				}

				if(next != null) {
					next.doClick();
				}
			}
		});

		getActionMap().put("FINISHED", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				btnFinished.doClick();
			}
		});

		getActionMap().put("SHOWALL", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				btnShowAll.doClick();
			}
		});

		getActionMap().put("NUMBERS", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				togShowNumbers.doClick();
			}
		});

		invalidate();

		//Forces focus
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				btnShowAll.requestFocusInWindow();
				setVisible(true);
			}
		});
	}
}
