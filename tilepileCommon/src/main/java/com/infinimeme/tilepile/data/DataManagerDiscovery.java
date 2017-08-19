package com.infinimeme.tilepile.data;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.prefs.Preferences;

import com.infinimeme.tilepile.common.TilepileUtils;

public class DataManagerDiscovery {

	static Preferences PACKAGE_PREFS = Preferences.userNodeForPackage(DataManagerDiscovery.class);

	public static final String DATAMANAGER_ADDRESS_KEY = "DATAMANAGER_ADDRESS_KEY";
	public static final String RESET_ADDRESS_PROPERTY_KEY = "DataManagerDiscovery.resetAddress";

	public static final String DISCOVERY_ADDRESS = "224.10.10.10";

	public static final int DISCOVERY_PORT = 45445;

	public static enum DiscoverableType {
		SERVER {
			@Override
			public byte getByte() {
				return 0;
			}
		},
		ADMIN {
			@Override
			public byte getByte() {
				return 1;
			}
		},
		STATION {
			@Override
			public byte getByte() {
				return 2;
			}
		},
		MAIN_STATION {
			@Override
			public byte getByte() {
				return 3;
			}
		};
		public abstract byte getByte();
	}

	private static final InetAddress getDiscoveryAddress() throws UnknownHostException {
		return InetAddress.getByName(DISCOVERY_ADDRESS);
	}

	private static final MulticastSocket getSocket() throws IOException {

		MulticastSocket socket = new MulticastSocket(new InetSocketAddress(DISCOVERY_PORT));

		socket.setInterface(TilepileUtils.getDefaultLocalAddress());
		socket.joinGroup(getDiscoveryAddress());
		// socket.setLoopbackMode(true);

		return socket;
	}

	public static final void clearStoredAddress() {
		PACKAGE_PREFS.putByteArray(DATAMANAGER_ADDRESS_KEY, new byte[0]);
	}

	public static final void setStoredAddress(InetAddress address) {
		PACKAGE_PREFS.putByteArray(DATAMANAGER_ADDRESS_KEY, address.getAddress());
	}

	public static final InetAddress getStoredAddress() throws UnknownHostException {

		byte[] storedAddress = PACKAGE_PREFS.getByteArray(DATAMANAGER_ADDRESS_KEY, null);

		if(storedAddress == null || storedAddress.length == 0) {
			return null;
		} else {
			return InetAddress.getByAddress(storedAddress);
		}
	}

	public static final InetAddress discover(DiscoverableType type) throws UnknownHostException {
		/*
		        if(new Boolean(System.getProperty(RESET_ADDRESS_PROPERTY_KEY, "false")).booleanValue()) {
		            clearStoredAddress();
		        }
		        
		        //Try to get stored address
		        InetAddress storedAddress = getStoredAddress();
		        
		        if(storedAddress == null) {
		            
		            String addressString = 
		                JOptionPane.showInputDialog(
		                    "Input DataServer address.  Leave blank to discover automatically."
		                );
		            
		            if(!(addressString == null || addressString.length() == 0)) {
		                InetAddress newAddress = InetAddress.getByName(addressString);
		                PACKAGE_PREFS.putByteArray(DATAMANAGER_ADDRESS_KEY, newAddress.getAddress());
		                return newAddress;
		            }
		            
		        } else {
		            return storedAddress;
		        } 
		*/
		//Default discovery to localhost
		InetAddress discoveryAddress = InetAddress.getLocalHost();

		try {

			MulticastSocket socket = getSocket();

			byte[] addressBytes = TilepileUtils.getDefaultLocalAddress().getAddress();
			byte[] datagramBytes = new byte[addressBytes.length + 1];

			datagramBytes[0] = type.getByte();
			System.arraycopy(addressBytes, 0, datagramBytes, 1, addressBytes.length);

			DatagramPacket sdp = new DatagramPacket(datagramBytes, datagramBytes.length, getDiscoveryAddress(), DISCOVERY_PORT);

			socket.send(sdp);

			DatagramPacket rdp = new DatagramPacket(datagramBytes, datagramBytes.length);

			boolean serverDiscovered = false;

			int index = 0;

			while(!serverDiscovered) {

				TilepileUtils.logInfo("Discovery loop " + index++);

				socket.receive(rdp);
				datagramBytes = rdp.getData();

				System.arraycopy(datagramBytes, 1, addressBytes, 0, addressBytes.length);

				discoveryAddress = InetAddress.getByAddress(addressBytes);

				if(datagramBytes[0] == DiscoverableType.SERVER.getByte()) {
					serverDiscovered = true;
					TilepileUtils.logInfo("Server discovered: " + discoveryAddress);
				} else {
					TilepileUtils.logInfo("Nonserver discovered: " + discoveryAddress);
				}
			}

			PACKAGE_PREFS.putByteArray(DATAMANAGER_ADDRESS_KEY, discoveryAddress.getAddress());

		} catch(IOException ioe) {
			TilepileUtils.exceptionReport(ioe);
		}

		return discoveryAddress;
	}

	public static final DataManagerRemote getDataManager(DiscoverableType type) throws RemoteException, NotBoundException, IOException {
		TilepileUtils.getRegistry();

		String remoteName = "//" + discover(type).getHostAddress() + "/" + DataManagerRemote.REMOTE_NAME;

		TilepileUtils.getLogger().info(remoteName);

		return (DataManagerRemote) Naming.lookup(remoteName);
	}

	public static void startDiscoveryServer(final DataManager localDataManager) {

		Thread serverThread = new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					MulticastSocket socket = getSocket();

					byte[] addressBytes = TilepileUtils.getDefaultLocalAddress().getAddress();
					byte[] datagramBytes = new byte[addressBytes.length + 1];

					datagramBytes[0] = DiscoverableType.SERVER.getByte();
					System.arraycopy(addressBytes, 0, datagramBytes, 1, addressBytes.length);

					DatagramPacket sdp = new DatagramPacket(datagramBytes, datagramBytes.length, getDiscoveryAddress(), DISCOVERY_PORT);

					DatagramPacket rdp = new DatagramPacket(new byte[datagramBytes.length], datagramBytes.length);

					boolean serverDiscovered = false;

					int index = 0;

					while(!serverDiscovered) {

						TilepileUtils.logInfo("Discovery loop " + index++);

						socket.receive(rdp);
						byte[] receivedBytes = rdp.getData();
						byte[] receivedAddressBytes = new byte[receivedBytes.length - 1];

						System.arraycopy(receivedBytes, 1, receivedAddressBytes, 0, receivedAddressBytes.length);

						InetAddress address = InetAddress.getByAddress(receivedAddressBytes);

						byte type = receivedBytes[0];

						if(type == DiscoverableType.SERVER.getByte()) {
							if(address.equals(TilepileUtils.getDefaultLocalAddress())) {
								TilepileUtils.logInfo("Server message loopback");
							} else {
								serverDiscovered = true;
								TilepileUtils.logWarning("Other Server discovered: " + address + " My address: " + TilepileUtils.getDefaultLocalAddress());
							}
						} else {

							TilepileUtils.logInfo("Nonserver discovered: " + address);

							socket.send(sdp);

							if(type == DiscoverableType.STATION.getByte()) {
								// TODO
							} else if(type == DiscoverableType.MAIN_STATION.getByte()) {
								// TODO
							}
						}
					}
				} catch(IOException ioe) {
					TilepileUtils.exceptionReport(ioe);
				}
			}
		});

		serverThread.setDaemon(true);
		serverThread.start();

	}
}
