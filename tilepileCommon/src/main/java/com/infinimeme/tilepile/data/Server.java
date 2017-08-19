package com.infinimeme.tilepile.data;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.infinimeme.tilepile.common.TilepileException;
import com.infinimeme.tilepile.common.TilepileUtils;

/**
 * @author Greg Barton 
 * The contents of this file are released under the GPL.  
 * Copyright 2004-2014 Greg Barton
 */
public class Server {

	// ~ Methods
	// ************************************************************************************

	public static void main(String[] args) throws TilepileException {

		TilepileUtils.setupLogging(Server.class);

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gds = ge.getScreenDevices();

		GraphicsDevice gd = gds[0];

		TilepileUtils.showSplashScreen(gd);

		try {
			String remoteName = DataManagerRemote.REMOTE_NAME;

			TilepileUtils.getLogger().info(remoteName);

			TilepileUtils.getRegistry().rebind(remoteName, DataManager.getInstance());

			DataManagerDiscovery.startDiscoveryServer(DataManager.getInstance());

		} catch(RemoteException re) {
			TilepileUtils.exceptionReport(re);
		}

		TilepileUtils.setLookAndFeel();

		JFrame frame = new JFrame("Tilepile data server");

		Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();

		frame.getContentPane().setLayout(new BorderLayout());

		JLabel northLabel = new JLabel("Running Tilepile data server on address:");
		northLabel.setFont(new Font("MONOSPACED", Font.BOLD, 18));

		frame.getContentPane().add(northLabel, BorderLayout.NORTH);

		JLabel addressLabel = new JLabel(TilepileUtils.getDefaultLocalAddress().getHostAddress());
		addressLabel.setFont(new Font("MONOSPACED", Font.BOLD, 72));

		frame.getContentPane().add(addressLabel, BorderLayout.CENTER);

		JLabel southLabel = new JLabel("Close window to quit.");
		southLabel.setFont(new Font("MONOSPACED", Font.BOLD, 18));

		frame.getContentPane().add(southLabel, BorderLayout.SOUTH);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setSize(screenBounds.getSize());
		frame.setExtendedState(JFrame.ICONIFIED);
		frame.setVisible(true);

	}

	public static final void cleanup() {

		try {
			TilepileUtils.getRegistry().unbind(DataManagerRemote.REMOTE_NAME);
		} catch(RemoteException re) {
			TilepileUtils.exceptionReport(re);
		} catch(NotBoundException nbe) {
			TilepileUtils.exceptionReport(nbe);
		}
	}

}
