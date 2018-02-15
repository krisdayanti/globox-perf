package psd.globox.app;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import psd.globox.rmi.GloBoxInterApp;
import psd.globox.rmi.GloBoxInterDb;
import psd.globox.utils.GloBoxCounters;
import psd.globox.utils.GloBoxDefVals;

public class GloBoxApp extends java.rmi.server.UnicastRemoteObject implements GloBoxInterApp {
	private static final long serialVersionUID = 1L;

	private String myID;
	private int nextDbIdx;
	private String address;
	private static AtomicLong missedBTs;
	private GloBoxCounters appCounters;
	private ArrayList<GloBoxInterDb> dbServers;
	private ArrayList<GloBoxInterApp> appServers;
	
	public GloBoxApp() throws RemoteException {
		nextDbIdx = 0;
		dbServers = new ArrayList<GloBoxInterDb>();
		appServers = new ArrayList<GloBoxInterApp>();
		missedBTs = new AtomicLong(0);
	}

	public void connectToDbServers(String dbIPs[]) {
		for (int i = 0; i < dbIPs.length && dbIPs[i] != null; i++) {
			try {
				Registry registry = LocateRegistry.getRegistry(dbIPs[i], GloBoxDefVals.DB_DEFAULT_PORT);
				dbServers.add((GloBoxInterDb) (registry.lookup("GloBoxDbServer")));
				System.out.println("GloBox APP: registering within DB server " + dbIPs[i]);
			} catch (Exception e) {
				System.out.println("GloBox APP: RMI ERROR trying to lookup for DB server " + dbIPs[i]);
			}
		}
	}

	public void connectToAppServers(String appIPs[]) {
		for (int i = 0; i < appIPs.length && appIPs[i] != null; i++) {
			try {
				Registry registry = LocateRegistry.getRegistry(appIPs[i], GloBoxDefVals.APP_DEFAULT_PORT);
				appServers.add((GloBoxInterApp) (registry.lookup("GloBoxAppServer")));
				System.out.println("GloBox APP: registering within APP server " + appIPs[i]);
			} catch (Exception e) {
				System.out.println("GloBox APP: RMI ERROR trying to lookup for APP server " + appIPs[i]);
			}
		}
	}

	public void startAppRmiServer() throws RemoteException {
		try {
			//address = (InetAddress.getLocalHost()).toString();
			address = System.getProperty("java.rmi.server.hostname");
		} catch (Exception e) {
			System.out.println("GloBox APP: can't get inet address.");
		}

		appCounters = new GloBoxCounters("APP", address);
		
		myID = new String(address + ":" + GloBoxDefVals.APP_DEFAULT_PORT + ":" + (long) (Math.random() * GloBoxDefVals.MAX_RANDOM_NUMBER));

		System.out.println("GloBox APP: this address=" + address + ",port=" + GloBoxDefVals.APP_DEFAULT_PORT);
		try {
			Registry registry = LocateRegistry.createRegistry(GloBoxDefVals.APP_DEFAULT_PORT);
			registry.rebind("GloBoxAppServer", this);
		} catch (Exception e) {
			System.out.println("GloBox APP: ERROR trying to rebind to GloBoxAppServer!");
		}
	}
	
		@Override
	public String appMakeNewReservation(short theater, long customer, boolean broadcast) throws RemoteException {
		appCounters.incRecvCounter(customer);
			
		if (dbServers.size() < 1) {
			System.out.println("GloBox App: [WARNING] no DB servers found for reservation!");
			return null;
		}

		int dbIdx;
		synchronized (this) {
			dbIdx = nextDbIdx++;
			if (nextDbIdx >= dbServers.size()) {
				nextDbIdx = 0;
			}
		}
		
		String value = null;
		try {
			value = dbServers.get(dbIdx).dbReserveOneSeat(theater, customer);
		} catch (Exception e) {
			return null;
		}
		appCounters.incSendCounter(customer);
		
		if (broadcast) {
			appCounters.incSendBroadcastCounter(customer);
			GloBoxAppBT rmiAppT[] = new GloBoxAppBT[appServers.size()];
			for (int i = 0; i < appServers.size(); i++) {
				rmiAppT[i] = new  GloBoxAppBT(missedBTs, appServers, i, customer);
			}
			for (int i = 0; i < appServers.size(); i++) {
				rmiAppT[i].start();
			}
		}
		return value;
	}

	@Override
	public boolean appConfirmPurchase(short theater, short seat, long customer, boolean sync, boolean buffered, boolean objects, short batchSize, boolean replicateDb) throws RemoteException {
		appCounters.incRecvCounter(customer);

		if (dbServers.size() < 1)
			return false;

		int dbIdx;
		synchronized (this) {
			dbIdx = nextDbIdx++;
			if (nextDbIdx >= dbServers.size()) {
				nextDbIdx = 0;
			}
		}
		boolean value = false;
		try {
			value = dbServers.get(dbIdx).dbConfirmPurchase(theater, seat, customer, sync, buffered, objects, batchSize, replicateDb);
		} catch (Exception e) {
			return false;
		}
		
		appCounters.incSendCounter(customer);
		
		return value;
	}

	@Override
	public String getMessageReceiptTS(long customer, String msg, long id, boolean logging) throws RemoteException {
		appCounters.incRecvCounter(customer);
		if (logging)
			System.out.println("=> Client [" + id + "] msg: " + msg);
		return Long.toString(System.currentTimeMillis());
	}

	@Override
	public String getDbMessageReceiptTS(long customer, String msg, long id, boolean logging) throws RemoteException {
		appCounters.incSendCounter(customer);
		if (logging)
			System.out.println("=> Client [" + id + "] is requesting DB TS");
		if (dbServers.size() < 1)
			return "WARNING: no DB servers found";

		int dbIdx;
		synchronized (this) {
			dbIdx = nextDbIdx++;
			if (nextDbIdx >= dbServers.size()) {
				nextDbIdx = 0;
			}
		}

		return dbServers.get(dbIdx).getMessageReceiptTS(customer, msg, id, logging);
	}

	@Override
	public String getCounters(long customer) throws RemoteException {
		String message = null;
		try {
	        message = "[APP SERVER] " + myID + "\n  Sent Reqs Count: " + Long.toString(appCounters.getSendCounter(customer)) 
	        + "\n  Recv Reqs Count: "
	        + Long.toString(appCounters.getRecvCounter(customer)) + "";
        } catch (IOException e) {
	       System.out.println("APP SERVER: ERROR trying to get counters!");
        }
		return message;
	}

	@Override
	public String getCountersDBs(long customer) throws RemoteException {
		String counters = new String();
		for (int i = 0; i < dbServers.size(); i++) {
			String dbCounters = null;
			try {
				dbCounters = dbServers.get(i).getCounters(customer);
				if (counters.length() > 1)
					counters = counters + "\n" + dbCounters;
				else 
					counters = dbCounters;
			} catch (Exception e) {
				System.out.println("GloBox APP: [ERROR] couldn't get counter for DB server " + i);
				counters = counters + "\n" + "GloBox APP: [ERROR] couldn't get counter for DB server " + i;
			}
		}
		return counters;
	}

	@Override
    public void btToApps(long customer, short theater, short seat, boolean status) throws RemoteException {
	    theater = (short) (theater + seat);
	    appCounters.incRecvBroadcastCounter(customer);
    }

	@Override
    public GloBoxCounters getServerCounters() throws RemoteException {
	    return appCounters;
    }
	
	@Override
    public ArrayList<GloBoxCounters> getServerCountersDBs() {
		ArrayList<GloBoxCounters> dbCounters = new ArrayList<GloBoxCounters>();
		for (int i = 0; i < dbServers.size(); i++) {
			GloBoxCounters tmp = null;
			try {
				tmp = dbServers.get(i).getServerCounters();
				dbCounters.add(tmp);
			} catch (Exception e) {
				System.out.println("APP SERVER: WARNING trying to get DB " + i + " server counters");
			}
		}
	    return dbCounters;
    }
}

