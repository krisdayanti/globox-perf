package psd.globox.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import psd.globox.rmi.GloBoxInterApp;

public class GloBoxAppBT extends Thread {
	
	int serverIdx;
	long customer;
	private AtomicLong missedBTs;
	private ArrayList<GloBoxInterApp> appServers;
	
	public GloBoxAppBT(AtomicLong missedBTs, ArrayList<GloBoxInterApp> appServers, int serverIdx, long customer) {
		this.appServers = appServers;
		this.serverIdx = serverIdx;
		this.customer = customer;
		this.missedBTs = missedBTs;
		setDaemon(true);
	}

	public void run() {
   		try {
	        appServers.get(serverIdx).btToApps(customer, (short)1, (short)1, true);
        } catch (RemoteException e) {
	        System.out.println("GloBox APP: could NOT successfuly send a broadcast message (failure # " + missedBTs.addAndGet(1) + ")");
        }
	}
}
