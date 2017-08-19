package com.infinimeme.tilepile.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import com.infinimeme.tilepile.common.AbstractSectionPanel;
import com.infinimeme.tilepile.common.Location;
import com.infinimeme.tilepile.common.LocationComparator;
import com.infinimeme.tilepile.common.Mural;
import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.Palette.PaletteColor;
import com.infinimeme.tilepile.common.States;
import com.infinimeme.tilepile.common.StatesFactory;
import com.infinimeme.tilepile.common.TilepileException;
import com.infinimeme.tilepile.common.TilepileUtils;
import com.infinimeme.tilepile.data.DataManagerRemote;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  Copyright
 * 2004-2014 Greg Barton
 **/
public class MuralEditor extends JInternalFrame {

	//~ Static fields/initializers *****************************************************************

	private static final long serialVersionUID = 8594620254478414285L;

	private static Preferences PACKAGE_PREFS = Preferences.userNodeForPackage(MuralEditor.class);

	//~ Instance fields ****************************************************************************

	private Mural mural = null;

	private MuralPanel muralPanel = null;

	private DataManagerRemote dataManager = null;

	//~ Constructors *******************************************************************************

	public MuralEditor(final Mural mural, DataManagerRemote dataManager) {
		super("Editing mural " + mural.getName(), true, true, true, true);

		setMural(mural);
		setDataManager(dataManager);

		try {
			muralPanel = new MuralPanel(mural.getSection(new Location(0, 0), mural.getWidth(), mural.getHeight()));

			JButton zoomInButton = new JButton("Zoom IN");

			zoomInButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					muralPanel.zoomIn();
				}
			});

			JButton zoomOutButton = new JButton("Zoom OUT");

			zoomOutButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					muralPanel.zoomOut();
				}
			});

			Container container = getContentPane();

			container.setLayout(new BorderLayout());

			container.add(new JScrollPane(muralPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);

			container.add(zoomInButton, BorderLayout.NORTH);
			container.add(zoomOutButton, BorderLayout.SOUTH);

			Preferences classPrefs = PACKAGE_PREFS.node("MuralEditor");
			Preferences prefs = classPrefs.node(mural.getName());

			TilepileUtils.componentResizeTracking(this, prefs);

			createMenus();

			setVisible(true);

		} catch(TilepileException te) {
			TilepileUtils.exceptionReport(te);
		} catch(RemoteException re) {
			TilepileUtils.exceptionReport(re);
		}
	}

	//~ Methods ************************************************************************************

	private void setDataManager(DataManagerRemote dataManager) {
		this.dataManager = dataManager;
	}

	private DataManagerRemote getDataManager() {

		return dataManager;
	}

	public Mural getMural() {

		return mural;
	}

	public MuralPanel getMuralPanel() {

		return muralPanel;
	}

	private void setMural(Mural mural) {
		this.mural = mural;
	}

	private void createMenus() {

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem saveItem = new JMenuItem("Save");

		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					getDataManager().setMural(mural);
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				}
			}
		});

		fileMenu.add(saveItem);

		JMenuItem saveImageItem = new JMenuItem("Save Image");

		saveImageItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					muralPanel.zoomInAll();
					ImageIO.write(muralPanel.getAllImage(), "png", new File(mural.getName() + ".png"));
				} catch(IOException ioe) {
					TilepileUtils.exceptionReport(ioe);
				} catch(TilepileException te) {
					TilepileUtils.exceptionReport(te);
				}
			}
		});

		fileMenu.add(saveImageItem);

		final UndoManager undo = new UndoManager();
		undo.setLimit(Integer.MAX_VALUE);

		muralPanel.addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent evt) {
				undo.addEdit(evt.getEdit());
			}
		});

		JMenu editMenu = new JMenu("Edit");
		menuBar.add(editMenu);

		JMenuItem undoItem = new JMenuItem("Undo");
		editMenu.add(undoItem);

		undoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				try {

					if(undo.canUndo()) {
						undo.undo();
					}
				} catch(CannotUndoException cue) {
					TilepileUtils.exceptionReport(cue);
				}
			}
		});

		undoItem.setAccelerator(KeyStroke.getKeyStroke(new Character('u'), InputEvent.CTRL_MASK));

		JMenuItem redoItem = new JMenuItem("Redo");
		editMenu.add(redoItem);

		redoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				try {

					if(undo.canRedo()) {
						undo.redo();
					}
				} catch(CannotRedoException cre) {
					TilepileUtils.exceptionReport(cre);
				}
			}
		});

		redoItem.setAccelerator(KeyStroke.getKeyStroke(new Character('r'), InputEvent.CTRL_MASK));

		JMenu toolsMenu = new JMenu("Tools");
		menuBar.add(toolsMenu);

		final JMenuItem sectionsItem = new JMenuItem("Sections");
		final JMenuItem tilesItem = new JMenuItem("Tiles");
		final JMenuItem editingItem = new JMenuItem("Editing");

		final JMenuItem[] toolsItems = new JMenuItem[] {
		sectionsItem, tilesItem, editingItem
		};

		sectionsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					getParent().add(new SectionsTool(toolsItems));

					for(JMenuItem toolsItem : toolsItems) {
						toolsItem.setEnabled(false);
					}
				} catch(TilepileException te) {
					TilepileUtils.exceptionReport(te);
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				}
			}
		});

		tilesItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					getParent().add(new TilesTool(toolsItems));

					for(JMenuItem toolsItem : toolsItems) {
						toolsItem.setEnabled(false);
					}
				} catch(TilepileException tpe) {
					TilepileUtils.exceptionReport(tpe);
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				}
			}
		});

		editingItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					getParent().add(new EditingTool(toolsItems));

					for(JMenuItem toolsItem : toolsItems) {
						toolsItem.setEnabled(false);
					}
				} catch(TilepileException te) {
					TilepileUtils.exceptionReport(te);
				} catch(RemoteException re) {
					TilepileUtils.exceptionReport(re);
				}
			}
		});

		toolsMenu.add(sectionsItem);
		toolsMenu.add(tilesItem);
		toolsMenu.add(editingItem);
	}

	//~ Inner Classes ******************************************************************************

	class MuralPanel extends AbstractSectionPanel {

		//~ Instance fields ************************************************************************

		private static final long serialVersionUID = -1619421746359580437L;

		private List<UndoableEditListener> editListeners = new LinkedList<UndoableEditListener>();

		private Map<Integer, SoftReference<BufferedImage>> imageCache = Collections.synchronizedMap(new TreeMap<Integer, SoftReference<BufferedImage>>());

		private boolean editing = true;

		private int lastX = -1;

		private int lastY = -1;

		private int muralX = -1;

		private int muralY = -1;

		private int tileSize = 1;

		//~ Constructors ***************************************************************************

		/**
		 * Creates a new MuralPanel object.
		 **/
		public MuralPanel(Mural.Section section) throws TilepileException, RemoteException {
			super(section, getDataManager().getPalette(section.getMural().getPaletteName()), false);
			initImageCache();
		}

		//~ Methods ********************************************************************************

		public void setEditingColor(final int editingColor) {
			clearMouseListeners();

			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					lastX = -1;
					lastY = -1;
				}

				@Override
				public void mousePressed(MouseEvent e) {
					panelPointToMural(e.getPoint());

					if(!(lastX == muralX && lastY == muralY)) {
						lastX = muralX;
						lastY = muralY;

						try {

							if(isEditing()) {
								edit(muralX, muralY, editingColor);
							} else {
								paint(muralX, muralY, editingColor);
							}
						} catch(TilepileException tpe) {
							TilepileUtils.exceptionReport(tpe);
						}
					}
				}
			});

			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {

					if(isEditing()) {
						panelPointToMural(e.getPoint());

						if(!(lastX == muralX && lastY == muralY)) {
							lastX = muralX;
							lastY = muralY;

							try {
								edit(muralX, muralY, editingColor);
							} catch(TilepileException tpe) {
								TilepileUtils.exceptionReport(tpe);
							}
						}
					}
				}
			});
		}

		@Override
		public Dimension getMaximumSize() {

			return getPreferredSize();
		}

		@Override
		public Dimension getMinimumSize() {

			return getPreferredSize();
		}

		public void setEditing(boolean editing) {
			this.editing = editing;
		}

		public boolean getEditing() {

			return editing;
		}

		public boolean isEditing() {

			return editing;
		}

		public Dimension getPreferredScrollableViewportSize() {

			return getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {

			return getTilesizeBounds(getSection(), tileSize);
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {

			if(orientation == SwingConstants.VERTICAL) {

				return tileSize * getSection().getHeight();
			} else {

				return tileSize * getSection().getWidth();
			}
		}

		public boolean getScrollableTracksViewportHeight() {

			return true;
		}

		public boolean getScrollableTracksViewportWidth() {

			return true;
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {

			return tileSize;
		}

		public void addUndoableEditListener(UndoableEditListener listener) {
			editListeners.add(listener);
		}

		public void clearMouseListeners() {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			MouseMotionListener[] mouseMotionListeners = getMouseMotionListeners();

			for(MouseMotionListener mouseMotionListener2 : mouseMotionListeners) {
				removeMouseMotionListener(mouseMotionListener2);
			}

			MouseListener[] mouseListeners = getMouseListeners();

			for(MouseListener mouseListener2 : mouseListeners) {
				removeMouseListener(mouseListener2);
			}
		}

		/**
		 * Zoom in on mural
		 **/
		public void zoomIn() {
			tileSize++;
			revalidate();
			repaint();
		}

		/**
		 * Zoom in all the way
		 **/
		public void zoomInAll() {
			tileSize = 1;
			revalidate();
			repaint();
		}

		/**
		 * Zoom out from mural
		 **/
		public void zoomOut() {
			tileSize--;
			tileSize = tileSize > 0 ? tileSize : 1;
			revalidate();
			repaint();
		}

		protected BufferedImage getAllImage() throws TilepileException {

			Integer key = new Integer(tileSize);

			SoftReference<BufferedImage> imageRef = imageCache.get(key);

			BufferedImage image = imageRef == null ? null : imageRef.get();

			if(image == null) {

				Dimension bounds = getPreferredSize();

				image = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_BYTE_INDEXED, getPalette().getColorModel());
				drawAll(image.createGraphics(), bounds);

				imageCache.put(key, new SoftReference<BufferedImage>(image));
			}

			return image;

		}

		@Override
		protected void showAll(Graphics2D g) throws TilepileException {

			Dimension bounds = getPreferredSize();
			g.drawImage(getAllImage(), 0, 0, bounds.width, bounds.height, Color.BLACK, null);
		}

		@Override
		protected void showColors(Graphics2D g) throws TilepileException {

			if(numActiveColors() == 0) {
				throw new IllegalStateException("No active colors set!");
			}

			drawColor(g, getPreferredSize());
		}

		private final Dimension getTilesizeBounds(Mural.Section section, int tileSize) {

			return new Dimension(section.getWidth() * tileSize, section.getHeight() * tileSize);
		}

		private void edit(final int x, final int y, final int color) throws TilepileException {

			final Mural.Section section = getSection();

			final int oldColor = section.getColorNumber(x, y);

			section.setColorNumber(x, y, color);

			updateImageCache(x, y);
			repaint();

			UndoableEdit edit = new AbstractUndoableEdit() {

				private static final long serialVersionUID = 1L;

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					section.setColorNumber(x, y, color);

					try {
						updateImageCache(x, y);
					} catch(TilepileException tpe) {
						TilepileUtils.exceptionReport(tpe);
					}
					repaint();
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					section.setColorNumber(x, y, oldColor);

					try {
						updateImageCache(x, y);
					} catch(TilepileException tpe) {
						TilepileUtils.exceptionReport(tpe);
					}
					repaint();
				}

				@Override
				public String getPresentationName() {

					StringBuffer sb = new StringBuffer();
					sb.append("Color edit ");
					sb.append(x);
					sb.append(",");
					sb.append(y);
					sb.append(" from ");
					sb.append(oldColor);
					sb.append(" to ");
					sb.append(color);

					return sb.toString();
				}
			};

			UndoableEditEvent event = new UndoableEditEvent(this, edit);

			for(UndoableEditListener listener : editListeners) {
				listener.undoableEditHappened(event);
			}
		}

		private void initImageCache() {

			for(SoftReference<BufferedImage> ref : imageCache.values()) {
				ref.clear();
			}
		}

		private void paint(final int x, final int y, final int color) {

			final Mural.Section section = getSection();

			final int changeColor = section.getColorNumber(x, y);

			paintHelper(x, y, color, changeColor, section);

			UndoableEdit edit = new AbstractUndoableEdit() {

				private static final long serialVersionUID = 1L;

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					paintHelper(x, y, color, changeColor, section);
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					paintHelper(x, y, changeColor, color, section);
				}

				@Override
				public String getPresentationName() {

					StringBuffer sb = new StringBuffer();
					sb.append("Color paint ");
					sb.append(x);
					sb.append(",");
					sb.append(y);
					sb.append(" from ");
					sb.append(changeColor);
					sb.append(" to ");
					sb.append(color);

					return sb.toString();
				}
			};

			UndoableEditEvent event = new UndoableEditEvent(this, edit);

			for(UndoableEditListener listener : editListeners) {
				listener.undoableEditHappened(event);
			}
		}

		private void paintHelper(int x, int y, int color, int changeColor, Mural.Section section) {

			int width = section.getWidth();
			int height = section.getHeight();

			int minX = x;
			int minY = y;
			int maxX = x;
			int maxY = y;

			class Loc {

				public int x;

				public int y;

				public Loc(int x, int y) {
					this.x = x;
					this.y = y;
				}
			}

			LinkedList<Loc> locs = new LinkedList<Loc>();

			locs.addLast(new Loc(x, y));

			while(locs.size() > 0) {

				Loc loc = locs.removeLast();

				if(loc.x < minX) {
					minX = loc.x;
				}

				if(loc.y < minY) {
					minY = loc.y;
				}

				if(loc.x > maxX) {
					maxX = loc.x;
				}

				if(loc.y > maxY) {
					maxY = loc.y;
				}

				section.setColorNumber(loc.x, loc.y, color);

				int newColor = Mural.NO_COLOR;

				if(loc.x + 1 < width) {
					newColor = section.getColorNumber(loc.x + 1, loc.y);

					if(newColor == changeColor) {
						locs.addLast(new Loc(loc.x + 1, loc.y));
					}
				}

				if(loc.x - 1 > 0) {
					newColor = section.getColorNumber(loc.x - 1, loc.y);

					if(newColor == changeColor) {
						locs.addLast(new Loc(loc.x - 1, loc.y));
					}
				}

				if(loc.y + 1 < height) {
					newColor = section.getColorNumber(loc.x, loc.y + 1);

					if(newColor == changeColor) {
						locs.addLast(new Loc(loc.x, loc.y + 1));
					}
				}

				if(loc.y - 1 > 0) {
					newColor = section.getColorNumber(loc.x, loc.y - 1);

					if(newColor == changeColor) {
						locs.addLast(new Loc(loc.x, loc.y - 1));
					}
				}
			}

			initImageCache();
			repaint();
		}

		private void panelPointToMural(Point p) {

			Point2D point = p;

			muralX = (int) (point.getX() / tileSize);
			muralY = (int) (point.getY() / tileSize);
		}

		/**
		 * Update cached tile image
		 *
		 * @param x X coordinate in the tile section grid.
		 * @param y Y coordinate in the tile section grid.
		 **/
		private void updateImageCache(int x, int y) throws TilepileException {

			for(Integer tileSizeKey : imageCache.keySet()) {

				BufferedImage bi = imageCache.get(tileSizeKey).get();

				if(bi != null) {
					drawLocation((Graphics2D) bi.getGraphics(), getTilesizeBounds(getSection(), tileSizeKey.intValue()), x, y);
				}
			}
		}

		private void updateImageCache(int minX, int minY, int maxX, int maxY) throws TilepileException {

			for(Integer tileSizeKey : imageCache.keySet()) {

				SoftReference<BufferedImage> ref = imageCache.get(tileSizeKey);
				BufferedImage bi = ref.get();

				Dimension tileBounds = getTilesizeBounds(getSection(), tileSizeKey.intValue());

				Dimension changeBounds = new Dimension((int) (tileBounds.getWidth() * (maxX - minX)), (int) (tileBounds.getHeight() * (maxY - minY)));

				if(bi != null) {
					drawLocation((Graphics2D) bi.getGraphics(), changeBounds, minX, minY);
				}
			}
		}
	}

	abstract class Tool extends JInternalFrame {

		//~ Constructors ***************************************************************************

		private static final long serialVersionUID = -3372446306843421443L;

		/**
		 * Creates a new Tool object.
		 **/
		public Tool(String title, final JMenuItem[] toolsItems) {
			super(title, true, true, true, true);
			addInternalFrameListener(new InternalFrameAdapter() {
				@Override
				public void internalFrameClosed(InternalFrameEvent e) {

					for(JMenuItem toolsItem : toolsItems) {
						toolsItem.setEnabled(true);
					}

					muralPanel.resetActiveColors();
				}
			});
		}
	}

	class EditingTool extends Tool {

		//~ Static fields/initializers *************************************************************

		private static final long serialVersionUID = 7685719503551149488L;

		public static final String NO_COLOR_CHANGING_LABEL = "No editing color";

		public static final String COLOR_CHANGING_LABEL = "Changing color to ";

		//~ Constructors ***************************************************************************

		/**
		 * Creates a new EditingTool object.
		 **/
		public EditingTool(final JMenuItem[] toolsItems) throws TilepileException, RemoteException {
			super("Editing Tool for " + getMural().getName(), toolsItems);

			addInternalFrameListener(new InternalFrameAdapter() {
				@Override
				public void internalFrameClosed(InternalFrameEvent e) {
					muralPanel.clearMouseListeners();
				}
			});

			ButtonGroup buttonGroup = new ButtonGroup();

			JPanel buttonPanel = new JPanel();

			final JLabel editingColorLabel = new JLabel(NO_COLOR_CHANGING_LABEL);
			editingColorLabel.setOpaque(true);

			Palette palette = getDataManager().getPalette(getMural().getPaletteName());

			final Map<Integer, PaletteColor> colors = palette.getColors();

			buttonPanel.setLayout(new GridLayout(palette.getSize() + 1, 1));

			final JToggleButton noColorButton = new JToggleButton(Palette.NON_COLOR_NAME);

			noColorButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					if(noColorButton.isSelected()) {
						muralPanel.setEditingColor((byte) Palette.NON_COLOR_INDEX);
						editingColorLabel.setText(COLOR_CHANGING_LABEL + Palette.NON_COLOR_NAME);
						editingColorLabel.setBackground(Palette.NON_COLOR_APPEARANCE);
						editingColorLabel.setForeground(TilepileUtils.getContrasting(Palette.NON_COLOR_APPEARANCE));
					} else {
						muralPanel.clearMouseListeners();
					}
				}
			});

			buttonPanel.add(noColorButton);
			buttonGroup.add(noColorButton);

			for(Palette.PaletteColor pc : colors.values()) {

				final int index = pc.getIndex();

				final String name = pc.getName();

				final JToggleButton colorButton = new JToggleButton(name);

				final Color color = pc.getColor();

				final Color contrastingColor = TilepileUtils.getContrasting(color);

				colorButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {

						if(colorButton.isSelected()) {
							muralPanel.setEditingColor((byte) index);
							editingColorLabel.setText(COLOR_CHANGING_LABEL + name);
							editingColorLabel.setBackground(color);
							editingColorLabel.setForeground(contrastingColor);
						} else {
							muralPanel.clearMouseListeners();
						}
					}
				});

				colorButton.setBackground(color);

				colorButton.setForeground(contrastingColor);

				buttonPanel.add(colorButton);

				buttonGroup.add(colorButton);
			}

			final JToggleButton paintButton = new JToggleButton("Single tile editing");

			paintButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					if(paintButton.isSelected()) {
						muralPanel.setEditing(false);
						paintButton.setText("Painting");
					} else {
						muralPanel.setEditing(true);
						paintButton.setText("Single tile editing");
					}
				}
			});

			Container container = getContentPane();

			container.setLayout(new BorderLayout());

			container.add(editingColorLabel, BorderLayout.CENTER);

			container.add(new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.EAST);

			container.add(paintButton, BorderLayout.SOUTH);

			TilepileUtils.componentResizeTracking(this, PACKAGE_PREFS.node("EditingTool"));

			setVisible(true);
		}
	}

	class SectionsTool extends Tool {

		//~ Static fields/initializers *************************************************************

		private static final long serialVersionUID = -2298488122885472258L;

		private static final String CONTAINS_COLOR_LABEL = "Finding sections that contain colors";

		private static final String NONCONTAINS_COLOR_LABEL = "Finding sections that do not contain colors";

		private static final String SELECTED_COLOR_LABEL = "Finding selected colors";

		private static final String UNSELECTED_COLOR_LABEL = "Finding unselected colors";

		//~ Instance fields ************************************************************************

		private final Comparator<Location> LOCATION_COMPARATOR = new LocationComparator();

		private JPanel sectionPanel = null;

		private JScrollPane sectionScrollPane = null;

		private JToggleButton btnContains = null;

		private JToggleButton btnSelected = null;

		//~ Constructors ***************************************************************************

		public SectionsTool(final JMenuItem[] toolsItems) throws TilepileException, RemoteException {
			super("Sections Tool for " + getMural().getName(), toolsItems);

			JPanel buttonPanel = new JPanel();

			sectionPanel = new JPanel();

			sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));

			sectionScrollPane = new JScrollPane(sectionPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			Palette palette = getDataManager().getPalette(getMural().getPaletteName());

			final Map<Integer, Palette.PaletteColor> colors = palette.getColors();

			buttonPanel.setLayout(new GridLayout(palette.getSize(), 1));

			btnSelected = new JToggleButton(SELECTED_COLOR_LABEL);

			btnSelected.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					if(btnSelected.isSelected()) {
						btnSelected.setText(UNSELECTED_COLOR_LABEL);
					} else {
						btnSelected.setText(SELECTED_COLOR_LABEL);
					}

					displaySections();

					muralPanel.setInverted(btnSelected.isSelected());
					muralPanel.repaint();
				}
			});

			btnContains = new JToggleButton(CONTAINS_COLOR_LABEL);

			btnContains.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					if(btnContains.isSelected()) {
						btnContains.setText(NONCONTAINS_COLOR_LABEL);
					} else {
						btnContains.setText(CONTAINS_COLOR_LABEL);
					}

					displaySections();
				}
			});

			for(Palette.PaletteColor pc : colors.values()) {

				final int index = pc.getIndex();

				final String name = pc.getName();

				final JToggleButton colorButton = new JToggleButton(name);

				colorButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {

						if(colorButton.isSelected()) {
							muralPanel.addActiveColor(index);
						} else {
							muralPanel.removeActiveColor(index);
						}

						displaySections();
					}
				});

				final Color color = pc.getColor();

				colorButton.setBackground(color);

				colorButton.setForeground(TilepileUtils.getContrasting(color));

				buttonPanel.add(colorButton);
			}

			Container container = getContentPane();

			container.setLayout(new BorderLayout());

			container.add(sectionScrollPane, BorderLayout.CENTER);

			JPanel togglePanel = new JPanel();
			togglePanel.setLayout(new GridLayout(1, 2));
			togglePanel.add(btnSelected);
			togglePanel.add(btnContains);

			container.add(togglePanel, BorderLayout.SOUTH);

			container.add(new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.EAST);

			JPanel infoPanel = new JPanel();
			togglePanel.setLayout(new GridLayout(1, 3));

			States states = getDataManager().getStates(getMural().getName());
			if(states == null) {
				states = StatesFactory.make(getMural());
				getDataManager().setStates(states);
			}

			Mural.Section[][] sections = getMural().getSections();

			int numSections = 0;
			int numFinished = 0;

			for(int i = 0; i < sections.length; i++) {
				for(int j = 0; j < sections[i].length; j++) {

					Mural.Section section = sections[i][j];

					boolean blank = true;

					blankLoop: for(int x = 0; x < section.getWidth(); x++) {
						for(int y = 0; y < section.getHeight(); y++) {
							if(section.getColorNumber(x, y) != Palette.NON_COLOR_INDEX) {
								blank = false;
								break blankLoop;
							}
						}
					}

					if(!blank) {
						numSections++;
						if(States.isFinished(states.get(j, i))) {
							numFinished++;
						}
					}
				}
			}

			infoPanel.add(new JLabel("Sections: " + numSections));
			infoPanel.add(new JLabel("Fabricated: " + numFinished));
			infoPanel.add(new JLabel("Unfabricated: " + (numSections - numFinished)));

			container.add(infoPanel, BorderLayout.NORTH);

			JMenuBar menuBar = new JMenuBar();
			setJMenuBar(menuBar);

			JMenu menu = new JMenu("File");
			menuBar.add(menu);

			JMenuItem printSectionsItem = new JMenuItem("Print Sections");

			printSectionsItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					try {
						TilepileUtils.printComponent(sectionPanel);
					} catch(PrinterException pe) {
						TilepileUtils.exceptionReport(pe);
					}
				}
			});

			printSectionsItem.setAccelerator(KeyStroke.getKeyStroke(new Character('p'), InputEvent.CTRL_MASK));

			menu.add(printSectionsItem);

			TilepileUtils.componentResizeTracking(this, PACKAGE_PREFS.node("SectionsTool"));

			setVisible(true);
		}

		//~ Methods ********************************************************************************

		/*
		private final String getSectionsString() {

		    Set<Location> locationSet = calcSectionLocations();

		    StringBuffer sb = new StringBuffer();

		    for(Location loc : locationSet) {

		        String locString = TilepileUtils.toString(loc);
		        sb.append(locString);
		        sb.append("\n");
		    }

		    return sb.toString();
		}*/

		private final Set<Location> calcSectionLocations() {

			Mural.Section section = muralPanel.getSection();

			boolean getSelected = !btnSelected.isSelected();
			boolean getContains = !btnContains.isSelected();

			Set<Location> locationSet = null;

			if(getContains) {
				locationSet = new TreeSet<Location>(LOCATION_COMPARATOR);
			} else {
				locationSet = mural.getSectionLocationSet();
			}

			for(int x = 0; x < section.getWidth(); x++) {

				for(int y = 0; y < section.getHeight(); y++) {

					int color = section.getColorNumber(x, y);

					boolean active = muralPanel.isActiveColor(color);

					Location loc = new Location(mural.getSectionX(section.getAbsoluteX(x)), mural.getSectionY(section.getAbsoluteY(y)));

					if(getSelected) {

						if(getContains) {

							if(active) {
								locationSet.add(loc);
							}
						} else {

							if(active) {
								locationSet.remove(loc);
							}
						}
					} else {

						if(getContains) {

							if(!active) {
								locationSet.add(loc);
							}
						} else {

							if(!active) {
								locationSet.remove(loc);
							}
						}
					}
				}
			}

			return locationSet;
		}

		private final void displaySections() {

			Set<Location> locationSet = calcSectionLocations();

			sectionPanel.removeAll();

			sectionPanel.add(new JLabel("SECTIONS"));

			for(Location location : locationSet) {

				JLabel label = new JLabel(TilepileUtils.toString(location));
				label.setBackground(Color.WHITE);
				sectionPanel.add(label);
			}

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					sectionPanel.validate();
					sectionScrollPane.validate();
					validate();
					sectionPanel.repaint();
					sectionScrollPane.repaint();
					repaint();
				}
			});

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					muralPanel.repaint();
				}
			});
		}
	}

	class TilesTool extends Tool {

		//~ Constructors ***************************************************************************

		private static final long serialVersionUID = -5212116593301277164L;

		public TilesTool(final JMenuItem[] toolsItems) throws TilepileException, RemoteException {
			super("Tile Tool for " + getMural().getName(), toolsItems);

			addInternalFrameListener(new InternalFrameAdapter() {
				@Override
				public void internalFrameClosed(InternalFrameEvent e) {
					muralPanel.clearMouseListeners();
				}
			});

			Palette palette = getDataManager().getPalette(getMural().getPaletteName());

			States states = getDataManager().getStates(mural.getName());

			JPanel labelPanel = new JPanel();

			labelPanel.setLayout(new GridLayout(1, 7));

			labelPanel.add(new JLabel("Color"));
			labelPanel.add(new JLabel("Raw"));
			labelPanel.add(new JLabel("Singles"));
			labelPanel.add(new JLabel("Doubles"));
			labelPanel.add(new JLabel("Finished Raw"));
			labelPanel.add(new JLabel("Finished Singles"));
			labelPanel.add(new JLabel("Finished Doubles"));

			JPanel dataPanel = new JPanel();

			final Map<Integer, Mural.Counter> globalHistogram = getMuralPanel().getSection().getHistogram();

			dataPanel.setLayout(new GridLayout(globalHistogram.keySet().size(), 7));

			final StringBuffer exportBuffer = new StringBuffer();

			for(Integer key : globalHistogram.keySet()) {

				Color color = palette.getColor(key.intValue());
				Color constrasting = TilepileUtils.getContrasting(color);
				String name = palette.getName(key.intValue());

				Mural mural = getMuralPanel().getSection().getMural();

				Mural.Section[][] sections = mural.getSections();

				int totalCountRaw = 0;
				int totalCountSingles = 0;
				int totalCountDoubles = 0;
				int finishedCountRaw = 0;
				int finishedCountSingles = 0;
				int finishedCountDoubles = 0;

				for(int y = 0; y < sections.length; y++) {

					for(int x = 0; x < sections[y].length; x++) {

						Mural.Section section = sections[y][x];

						final Map<Integer, Mural.Counter> rawHistogram = section.getHistogram();
						final Map<Integer, Mural.Counter> histogram1 = section.getHistogram1();
						final Map<Integer, Mural.Counter> histogram2 = section.getHistogram2();

						Mural.Counter counterRaw = rawHistogram.get(key);
						Mural.Counter counter1 = histogram1.get(key);
						Mural.Counter counter2 = histogram2.get(key);

						int localCountRaw = counterRaw == null ? 0 : counterRaw.getCount();
						int localCountSingles = counter1 == null ? 0 : counter1.getCount();
						int localCountDoubles = counter2 == null ? 0 : counter2.getCount();

						totalCountRaw += localCountRaw;
						totalCountSingles += localCountSingles;
						totalCountDoubles += localCountDoubles;

						if(states != null && states.isFinished(x, y)) {

							if(counterRaw != null) {
								finishedCountRaw += localCountRaw;
							}

							if(counter1 != null) {
								finishedCountSingles += localCountSingles;
							}

							if(counter2 != null) {
								finishedCountDoubles += localCountDoubles;
							}
						}
					}
				}

				JLabel labelColor = new JLabel(name);
				labelColor.setOpaque(true);
				labelColor.setBackground(color);
				labelColor.setForeground(constrasting);
				dataPanel.add(labelColor);
				JLabel labelRaw = new JLabel(Integer.toString(totalCountRaw));
				labelRaw.setOpaque(true);
				labelRaw.setBackground(color);
				labelRaw.setForeground(constrasting);
				dataPanel.add(labelRaw);
				JLabel labelSingles = new JLabel(Integer.toString(totalCountSingles));
				labelSingles.setOpaque(true);
				labelSingles.setBackground(color);
				labelSingles.setForeground(constrasting);
				dataPanel.add(labelSingles);
				JLabel labelDoubles = new JLabel(Integer.toString(totalCountDoubles));
				labelDoubles.setOpaque(true);
				labelDoubles.setBackground(color);
				labelDoubles.setForeground(constrasting);
				dataPanel.add(labelDoubles);
				JLabel labelFinishedRaw = new JLabel(Integer.toString(finishedCountRaw));
				labelFinishedRaw.setOpaque(true);
				labelFinishedRaw.setBackground(color);
				labelFinishedRaw.setForeground(constrasting);
				dataPanel.add(labelFinishedRaw);
				JLabel labelFinishedSingles = new JLabel(Integer.toString(finishedCountSingles));
				labelFinishedSingles.setOpaque(true);
				labelFinishedSingles.setBackground(color);
				labelFinishedSingles.setForeground(constrasting);
				dataPanel.add(labelFinishedSingles);
				JLabel labelFinishedDoubles = new JLabel(Integer.toString(finishedCountDoubles));
				labelFinishedDoubles.setOpaque(true);
				labelFinishedDoubles.setBackground(color);
				labelFinishedDoubles.setForeground(constrasting);
				dataPanel.add(labelFinishedDoubles);

				exportBuffer.append(name);
				exportBuffer.append(',');
				exportBuffer.append(totalCountRaw);
				exportBuffer.append(',');
				exportBuffer.append(totalCountSingles);
				exportBuffer.append(',');
				exportBuffer.append(totalCountDoubles);
				exportBuffer.append(',');
				exportBuffer.append(finishedCountRaw);
				exportBuffer.append(',');
				exportBuffer.append(finishedCountSingles);
				exportBuffer.append(',');
				exportBuffer.append(finishedCountDoubles);
				exportBuffer.append('\n');
			}

			JButton exportButton = new JButton("Export");

			exportButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					java.io.File exportFile = new File(mural.getName() + ".csv");
					javax.swing.JFileChooser chooser = new javax.swing.JFileChooser("Export the tiles...");

					chooser.setSelectedFile(exportFile);

					int returnVal = chooser.showSaveDialog(MuralEditor.this);

					if(returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {

						try {

							java.io.FileWriter fw = new java.io.FileWriter(chooser.getSelectedFile());
							fw.write(exportBuffer.toString());
							fw.flush();
							fw.close();

						} catch(FileNotFoundException fnfe) {
							TilepileUtils.exceptionReport(fnfe);
						} catch(IOException ioe) {
							TilepileUtils.exceptionReport(ioe);
						}
					}
				}

			});

			Container container = getContentPane();

			container.setLayout(new BorderLayout());

			container.add(labelPanel, BorderLayout.NORTH);

			container.add(new JScrollPane(dataPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

			container.add(exportButton, BorderLayout.SOUTH);

			TilepileUtils.componentResizeTracking(this, PACKAGE_PREFS.node("TilesTool"));

			setVisible(true);
		}
	}
}
