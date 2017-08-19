package com.infinimeme.tilepile.admin;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;

import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.Palette.PaletteColor;
import com.infinimeme.tilepile.common.TilepileUtils;
import com.infinimeme.tilepile.data.DataManagerRemote;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.
 * Copyright 2004-2014 Greg Barton
 */
public class PaletteEditor extends JInternalFrame {

	// ~ Static fields/initializers
	// *****************************************************************

	private static final long serialVersionUID = -7603120797171562179L;

	private static Preferences PACKAGE_PREFS = Preferences.userNodeForPackage(PaletteEditor.class);

	// ~ Instance fields
	// ****************************************************************************

	private Palette palette = null;

	private boolean dirty = false;

	private DataManagerRemote dataManager = null;

	// ~ Constructors
	// *******************************************************************************

	/**
	 * Creates a new PaletteEditor object.
	 * 
	 * @param palette
	*/
	public PaletteEditor(Palette palette, DataManagerRemote dataManager) {
		this.palette = palette;
		setDataManager(dataManager);

		createMenus();
		setup(palette);

		setTitle("Editing palette " + palette.getName());

		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				exitNicely();
			}
		});

		Preferences classPrefs = PACKAGE_PREFS.node("PaletteEditor");
		final Preferences prefs = classPrefs.node(palette.getName());

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {

				Rectangle bounds = getBounds();
				prefs.putInt("X", (int) bounds.getX());
				prefs.putInt("Y", (int) bounds.getY());
			}

			@Override
			public void componentResized(ComponentEvent e) {

				Rectangle bounds = getBounds();
				prefs.putInt("WIDTH", (int) bounds.getWidth());
				prefs.putInt("HEIGHT", (int) bounds.getHeight());
			}
		});

		int x = prefs.getInt("X", 0);
		int y = prefs.getInt("Y", 0);
		int width = prefs.getInt("WIDTH", 640);
		int height = prefs.getInt("HEIGHT", 480);

		setBounds(x, y, width, height);

		setResizable(true);
		setClosable(true);
		setIconifiable(true);
		setMaximizable(true);

		setVisible(true);
	}

	// ~ Methods
	// ************************************************************************************

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
	*/
	private void createMenus() {

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem saveItem = new JMenuItem("Save");

		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if(dirty) {
					try {
						getDataManager().setPalette(palette);
						dirty = false;
					} catch(RemoteException re) {
						TilepileUtils.exceptionReport(re);
					}
				}
			}
		});

		fileMenu.add(saveItem);

		JMenu paletteMenu = new JMenu("Palette");
		menuBar.add(paletteMenu);

		JMenuItem addColorItem = new JMenuItem("Add Color");

		addColorItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Palette.PaletteColor color = new Palette.PaletteColor(Palette.getMaxIndex(palette), "NEW COLOR", Color.BLACK);
				palette.addColor(color);
				setup(palette);

				validate();
				repaint();
			}
		});

		paletteMenu.add(addColorItem);

		/*  TODO: JOptionPane to select color
				JMenuItem deleteColorItem = new JMenuItem("Delete Color");

				deleteColorItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Palette.PaletteColor color = 
						
						palette.removeColor(color);
						setup(palette);
						validate();
						repaint();
					}
				});

				paletteMenu.add(deleteColorItem);
		*/
	}

	/**
	*/
	private final void exitNicely() {

		if(dirty) {

			int decision = JOptionPane.showConfirmDialog(getParent(), "Do you want to save changes?", "Save", JOptionPane.YES_NO_OPTION);

			if(decision == JOptionPane.YES_OPTION) {
				try {
					getDataManager().setPalette(palette);
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				}
			}
		}
	}

	/**

	 * @param palette
	*/
	private void setup(final Palette palette) {

		final Map<Integer, PaletteColor> colors = palette.getColors();

		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		JPanel indexPanel = new JPanel();
		JPanel namePanel = new JPanel();
		JPanel colorButtonPanel = new JPanel();
		JPanel colorHexPanel = new JPanel();
		JPanel inStockPanel = new JPanel();
		JPanel substitutionPanel = new JPanel();

		TilepileUtils.logInfo("LAYING OUT PALETTE SIZE " + palette.getSize());
		indexPanel.setLayout(new GridLayout(palette.getSize() + 1, 1));
		namePanel.setLayout(new GridLayout(palette.getSize() + 1, 1));
		colorButtonPanel.setLayout(new GridLayout(palette.getSize() + 1, 1));
		colorHexPanel.setLayout(new GridLayout(palette.getSize() + 1, 1));
		inStockPanel.setLayout(new GridLayout(palette.getSize() + 1, 1));
		substitutionPanel.setLayout(new GridLayout(palette.getSize() + 1, 1));

		buttonPanel.add(indexPanel);
		buttonPanel.add(namePanel);
		buttonPanel.add(colorButtonPanel);
		buttonPanel.add(colorHexPanel);
		buttonPanel.add(inStockPanel);
		buttonPanel.add(substitutionPanel);

		indexPanel.add(new JLabel("Index"));
		namePanel.add(new JLabel("Name"));
		colorButtonPanel.add(new JLabel("Color"));
		colorHexPanel.add(new JLabel("HexValue"));
		inStockPanel.add(new JLabel("In Stock (lb)"));
		substitutionPanel.add(new JLabel("Substitution"));

		JFormattedTextField.AbstractFormatter hexFormat = null;

		try {
			hexFormat = new MaskFormatter("HHHHHH");
		} catch(ParseException pe) {
			TilepileUtils.exceptionReport(pe);
		}

		for(final Palette.PaletteColor pc : colors.values()) {

			final int index = pc.getIndex();

			final Color color = pc.getColor();
			final String name = pc.getName();

			indexPanel.add(new JLabel(Integer.toString(index)));

			final JTextField nameText = new JTextField(name);
			final JButton colorButton = new JButton("Color: " + name);

			String hex = Integer.toHexString(color.getRGB()).substring(2);

			final JFormattedTextField hexText = new JFormattedTextField(hexFormat);

			hexText.setText(hex);

			nameText.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					String newName = nameText.getText();

					if(newName != null && newName.length() > 0 && !newName.equals(name)) {
						dirty = true;
						pc.setName(newName);
						colorButton.setText("Color: " + newName);
					}
				}
			});

			nameText.addFocusListener(new FocusListener() {

				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {

					String newName = nameText.getText();

					if(newName != null && newName.length() > 0 && !newName.equals(name)) {
						dirty = true;
						pc.setName(newName);
						colorButton.setText("Color: " + newName);
					}
				}

			});
			namePanel.add(nameText);

			colorButton.setBackground(color);

			colorButton.setForeground(TilepileUtils.getContrasting(color));

			colorButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					Color result = JColorChooser.showDialog(getParent(), "Edit color " + index, pc.getColor());

					if(!(result == null || pc.getColor().equals(result))) {
						dirty = true;
						pc.setColor(result);

						colorButton.setBackground(result);
						colorButton.setForeground(TilepileUtils.getContrasting(result));
						hexText.setText(Integer.toHexString(result.getRGB()).substring(2));
					}
				}
			});

			colorButtonPanel.add(colorButton);

			hexText.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					Color result = new Color(Integer.parseInt(hexText.getText(), 16));

					if(!pc.getColor().equals(result)) {
						dirty = true;
						pc.setColor(result);

						colorButton.setBackground(result);
						colorButton.setForeground(TilepileUtils.getContrasting(result));
					}
				}
			});

			hexText.addFocusListener(new FocusListener() {

				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {

					Color result = new Color(Integer.parseInt(hexText.getText(), 16));

					if(!pc.getColor().equals(result)) {
						dirty = true;
						pc.setColor(result);

						colorButton.setBackground(result);
						colorButton.setForeground(TilepileUtils.getContrasting(result));
					}
				}

			});

			colorHexPanel.add(hexText);

			final JFormattedTextField countInStockText = new JFormattedTextField(new NumberFormatter(NumberFormat.getIntegerInstance()));
			countInStockText.setText(Integer.toString(pc.getCountInStock()));

			countInStockText.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					int count = Integer.parseInt(countInStockText.getText());

					if(pc.getCountInStock() != count) {
						dirty = true;
						pc.setCountInStock(count);
					}
				}
			});

			inStockPanel.add(countInStockText);

			final JTextField substitutionText = new JTextField(pc.getSubstitution());

			substitutionText.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					String substitution = substitutionText.getText();

					if(substitution != null && !substitution.equals(pc.getSubstitution())) {
						dirty = true;
						pc.setSubstitution(substitution);
					}
				}
			});

			substitutionPanel.add(substitutionText);

		}

		getContentPane().removeAll();

		getContentPane().add(new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

	}
}
