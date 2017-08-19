package com.infinimeme.tilepile.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.infinimeme.tilepile.data.DataManagerRemote;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.LightGray;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 **/
public class TilepileUtils {

	//~ Static fields/initializers *****************************************************************

	public static final int NOT_CALCULATED = Integer.MIN_VALUE;

	public static final int RMI_PORT = 45446;

	private static InetAddress defaultAddress = null;

	private static Registry registry = null;

	private static Logger LOGGER = null;

	//~ Methods ************************************************************************************

	public static final void appPrep(Class<?> clazz) {

		//System.setProperty("com.apple.macos.useScreenMenuBar", "true");

		//Use OpenGL acceleration
		System.setProperty("sun.java2d.opengl", "true");

		TilepileUtils.setupLogging(clazz);
	}

	public static final Logger getLogger() {

		if(LOGGER == null) {
			setupLogging(TilepileUtils.class);
			LOGGER.warning("Logging activity before application setup!");
		}

		return LOGGER;
	}

	public static final void setupLogging(Class<?> clazz) {

		try {
			File logDir = new File("log");
			logDir.mkdirs();
			LOGGER = Logger.getLogger(clazz.getName());
			LOGGER.addHandler(new FileHandler(new File(logDir, clazz.getName() + ".log").getPath()));
			LOGGER.info("Logger started");
		} catch(IOException ioe) {
			exceptionReport(ioe);
		}
	}

	public static final Color getContrasting(Color color) {

		if((color.getRed() + color.getGreen() + color.getBlue()) / 3 < 128) {

			return Color.WHITE;
		} else {

			return Color.BLACK;
		}
	}

	public static final InetAddress getDefaultLocalAddress() {

		if(defaultAddress == null) {

			try {

				InetAddress loopback = null;

				for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {

					for(Enumeration<InetAddress> ei = en.nextElement().getInetAddresses(); ei.hasMoreElements();) {

						InetAddress i = ei.nextElement();

						if(i instanceof Inet4Address) {

							if(i.isLoopbackAddress()) {

								//Hold on to loopback address, just in case
								loopback = i;
							} else {

								//Found a viable address.
								defaultAddress = i;
							}
						}
					}
				}

				if(defaultAddress == null) {
					defaultAddress = loopback;
				}

				if(defaultAddress == null) {
					throw new IllegalStateException("FATAL: Default network address could not be found!");
				}

				logInfo("Network address initialized: " + defaultAddress.getHostAddress());

			} catch(SocketException se) {
				exceptionReport(se);
			}
		}

		return defaultAddress;
	}

	public static final void setLookAndFeel() throws TilepileException {
		PlasticLookAndFeel.setPlasticTheme(new LightGray());
		try {
			UIManager.setLookAndFeel(new PlasticLookAndFeel());
		} catch(UnsupportedLookAndFeelException ulfe) {
			throw new TilepileException(ulfe);
		}
	}

	public static final void fixFont(JMenu menu, JMenuItem item) {
		item.setFont(menu.getFont());
	}

	public static final String getNormalizedName(String name) {

		return name.replace(':', '_').replace('/', '_').replace('\\', '_').replace(File.separatorChar, '_').replace(File.pathSeparatorChar, '_').replace('<', '_').replace('>', '_').trim()
		.replace(' ', '_');
	}

	public static final Registry getRegistry() throws RemoteException {

		System.setProperty("java.rmi.server.hostname", TilepileUtils.getDefaultLocalAddress().getHostAddress());

		if(registry == null) {

			try {
				registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			} catch(RemoteException re) {
				registry = LocateRegistry.getRegistry(getDefaultLocalAddress().getHostAddress(), Registry.REGISTRY_PORT);
			}
		}

		return registry;
	}

	public static final void componentResizeTracking(final Component component, final Preferences prefs) {
		component.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {

				Rectangle bounds = component.getBounds();
				prefs.putInt("X", (int) bounds.getX());
				prefs.putInt("Y", (int) bounds.getY());
			}

			@Override
			public void componentResized(ComponentEvent e) {

				Rectangle bounds = component.getBounds();
				prefs.putInt("WIDTH", (int) bounds.getWidth());
				prefs.putInt("HEIGHT", (int) bounds.getHeight());
			}
		});

		int x = prefs.getInt("X", 0);
		int y = prefs.getInt("Y", 0);
		int width = prefs.getInt("WIDTH", 320);
		int height = prefs.getInt("HEIGHT", 240);

		component.setBounds(x, y, width, height);

	}

	public static final void exceptionReport(Exception e) {
		JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
		LOGGER.log(Level.SEVERE, e.getMessage(), e);
	}

	public static final void exceptionReport(RemoteException e) {
		JOptionPane.showMessageDialog(null, "There was a problem with the network: " + e, "Network Error", JOptionPane.ERROR_MESSAGE);
		LOGGER.log(Level.SEVERE, e.getMessage(), e);
	}

	public static final void logWarning(String message, Throwable t) {
		LOGGER.log(Level.WARNING, message, t);
	}

	public static final void logWarning(String message) {
		LOGGER.log(Level.WARNING, message);
	}

	public static final void logInfo(String message) {
		LOGGER.log(Level.INFO, message);
	}

	public static final void debugDataManager(DataManagerRemote dataManager) throws RemoteException {
		logInfo("AVAILABLE MURALS: " + dataManager.instancesOfMural());
		logInfo("AVAILABLE PALETTES: " + dataManager.instancesOfPalette());
		logInfo("AVAILABLE STATES: " + dataManager.instancesOfStates());
		logInfo("AVAILABLE MAIN STATIONS: " + dataManager.instancesOfMainStation());
		logInfo("AVAILABLE STATIONS: " + dataManager.instancesOfStation());
	}

	public static final byte i2b(int i) {

		return (byte) (i < 128 ? i : i - 256);
	}

	public static final int b2i(byte b) {
		return b < 0 ? b + 256 : b;
	}

	public static final String indexToCharacter(int index) {

		//return new Character((char)('A' + index)).toString();
		String string = Integer.toString(index, 26);

		string = string.replace('p', 'Z');
		string = string.replace('o', 'Y');
		string = string.replace('n', 'X');
		string = string.replace('m', 'W');
		string = string.replace('l', 'V');
		string = string.replace('k', 'U');
		string = string.replace('j', 'T');
		string = string.replace('i', 'S');
		string = string.replace('h', 'R');
		string = string.replace('g', 'Q');
		string = string.replace('f', 'P');
		string = string.replace('e', 'O');
		string = string.replace('d', 'N');
		string = string.replace('c', 'M');
		string = string.replace('b', 'L');
		string = string.replace('a', 'K');
		string = string.replace('9', 'J');
		string = string.replace('8', 'I');
		string = string.replace('7', 'H');
		string = string.replace('6', 'G');
		string = string.replace('5', 'F');
		string = string.replace('4', 'E');
		string = string.replace('3', 'D');
		string = string.replace('2', 'C');
		string = string.replace('1', 'B');
		string = string.replace('0', 'A');

		return string;
	}

	public static final void showSplashScreen(GraphicsDevice gd) {

		try {

			GraphicsConfiguration gc = gd.getDefaultConfiguration();

			final BufferedImage image = ImageIO.read(TilepileUtils.class.getClassLoader().getResourceAsStream("splash.png"));

			final JWindow splashWindow = new JWindow(gc);

			splashWindow.getContentPane().add(new JPanel() {

				private static final long serialVersionUID = 1L;

				@Override
				public void paint(Graphics g) {
					super.paint(g);
					g.drawImage(image, 0, 0, Color.BLACK, null);
				}
			});

			splashWindow.setSize(image.getWidth(), image.getHeight());
			splashWindow.setLocation(gc.getBounds().width / 2 - image.getWidth() / 2, gc.getBounds().height / 2 - image.getHeight() / 2);

			splashWindow.setVisible(true);

			new Thread() {
				@Override
				public void run() {

					try {
						sleep(2500);
					} catch(InterruptedException ie) {
					}
					splashWindow.setVisible(false);
				}
			}.start();

		} catch(IOException ioe) {
			exceptionReport(ioe);
		}
	}

	public static void main(String[] args) {
		System.out.println(getDefaultLocalAddress());
	}

	public static final void printComponent(Component component) throws PrinterException {

		PrinterJob pjob = PrinterJob.getPrinterJob();
		PageFormat pf = pjob.defaultPage();
		pjob.setPrintable(new ComponentPrintable(component), pf);

		if(pjob.printDialog()) {
			pjob.print();
		}
	}

	public static final void printString(String toPrint) throws PrinterException {

		PrinterJob pjob = PrinterJob.getPrinterJob();
		PageFormat pf = pjob.defaultPage();
		pjob.setPrintable(new StringPrintable(toPrint), pf);

		if(pjob.printDialog()) {
			pjob.print();
		}
	}

	public static final String toString(Location location) {

		StringBuffer sb = new StringBuffer();
		sb.append(location.getX());
		sb.append(",");
		sb.append(indexToCharacter(location.getY()));

		return sb.toString();
	}

	//~ Inner Classes ******************************************************************************

	static class ComponentPrintable implements Printable {

		//~ Instance fields ************************************************************************

		private Component component = null;

		//~ Constructors ***************************************************************************

		public ComponentPrintable(Component component) {
			this.component = component;
		}

		//~ Methods ********************************************************************************

		@Override
		public int print(Graphics g, PageFormat pf, int pageIndex) {

			if(pageIndex > 0) {

				return Printable.NO_SUCH_PAGE;
			}

			((Graphics2D) g).translate(pf.getImageableX(), pf.getImageableY());

			component.paint(g);

			return Printable.PAGE_EXISTS;
		}
	}

	static class StringPrintable implements Printable {

		//~ Instance fields ************************************************************************

		private String printString = null;

		//~ Constructors ***************************************************************************

		public StringPrintable(String printString) {
			this.printString = printString;
		}

		//~ Methods ********************************************************************************

		@Override
		public int print(Graphics g, PageFormat pf, int pageIndex) {

			if(pageIndex > 0) {

				return Printable.NO_SUCH_PAGE;
			}

			Graphics2D g2d = (Graphics2D) g;

			g2d.drawString(printString, (int) pf.getImageableX(), (int) pf.getImageableY());

			return Printable.PAGE_EXISTS;
		}
	}
}
