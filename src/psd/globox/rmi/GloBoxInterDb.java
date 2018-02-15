package psd.globox.rmi;

import java.rmi.*;

import psd.globox.utils.GloBoxCounters;

public interface GloBoxInterDb extends java.rmi.Remote{
	String getMessageReceiptTS(long customer, String msg, long id, boolean logging) throws RemoteException;
	
	String getCounters(long customer) throws RemoteException;
	
	GloBoxCounters getServerCounters() throws RemoteException;
	
	// GloBox DB interfaces
	String dbReserveOneSeat(short theater, long customer) throws RemoteException;
	boolean dbConfirmPurchase(short theater, short seat, long customer, boolean sync, boolean buffered, boolean objects, short batchSize, boolean replicateDb) throws RemoteException;
	boolean dbReplicaConfirmPurchase(short theater, short seat, long customer, boolean sync, boolean buffered, boolean objects, short batchSize, boolean replicateDb) throws RemoteException;
	int dbSendToReplicas(short theater, short seat, long customer, boolean sync, boolean buffered, boolean objects, short batchSize, boolean replicateDb) throws RemoteException;
}