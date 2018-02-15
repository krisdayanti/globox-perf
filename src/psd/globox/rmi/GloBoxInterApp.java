package psd.globox.rmi;

import java.rmi.*;
import java.util.ArrayList;

import psd.globox.utils.GloBoxCounters;

public interface GloBoxInterApp extends java.rmi.Remote{
	String getMessageReceiptTS(long customer, String msg, long id, boolean logging) throws RemoteException;
	String getDbMessageReceiptTS(long customer, String msg, long id, boolean logging) throws RemoteException;
	
	String getCounters(long customer) throws RemoteException;
	String getCountersDBs(long customer) throws RemoteException;
	void btToApps(long customer, short theater, short seat, boolean b) throws RemoteException;
	
	GloBoxCounters getServerCounters() throws RemoteException;
	ArrayList<GloBoxCounters> getServerCountersDBs() throws RemoteException;
			
	// GloBox App Interface
	String appMakeNewReservation(short theater, long customer, boolean broadcast) throws RemoteException;
	boolean appConfirmPurchase(short theater, short seat, long customer, boolean sync, boolean buffered, boolean objects, short batchSize, boolean replicateDb) throws RemoteException;
}