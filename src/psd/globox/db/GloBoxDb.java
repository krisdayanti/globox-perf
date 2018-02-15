package psd.globox.db;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import psd.globox.rmi.GloBoxInterDb;
import psd.globox.utils.GloBoxCounters;
import psd.globox.utils.GloBoxDefVals;

public class GloBoxDb extends java.rmi.server.UnicastRemoteObject implements GloBoxInterDb {
	
    private static final long serialVersionUID = 1L;

    private String myID;
	private GloBoxCounters dbCounters;
	private static AtomicLong missedRep;
	private static ArrayList<GloBoxDbFile> dbMasterFiles;
	private static ArrayList<GloBoxDbFile> dbReplicaFiles;
	private ArrayList<GloBoxInterDb> dbRmiReplicas;
	

	public GloBoxDb() throws RemoteException {
		dbMasterFiles = new ArrayList<GloBoxDbFile>();
		dbReplicaFiles = new ArrayList<GloBoxDbFile>();
		dbRmiReplicas = new ArrayList<GloBoxInterDb>();
		missedRep = new AtomicLong(0);
	}

	public void connectToDbReplicas(String dbRepIPs[]) {
		for (int i = 0; i < dbRepIPs.length && dbRepIPs[i] != null; i++) {
			try {
				Registry registry = LocateRegistry.getRegistry(dbRepIPs[i], GloBoxDefVals.DB_DEFAULT_PORT);
				dbRmiReplicas.add((GloBoxInterDb) (registry.lookup("GloBoxDbServer")));
				System.out.println("GloBox DB: registering within DB replica " + dbRepIPs[i]);
			} catch (Exception e) {
				System.out.println("GloBox DB: ERROR trying to register within DB replica " + dbRepIPs[i]);
			} 
		}
	}

	public void createTmpFiles() {
		String tmpDir = "db";
		File file = new File(tmpDir);
		if (!file.exists())
			file.mkdirs();
		long runNumber = (long) (Math.random() * GloBoxDefVals.MAX_RANDOM_NUMBER);
		tmpDir = "db/masterFiles" + runNumber;
		file = new File(tmpDir);
		file.mkdirs();
		try {
			for (int i = 0; i < (GloBoxDefVals.N_THEATERS); i++) {
				dbMasterFiles.add(new GloBoxDbFile(tmpDir, i));
			}
			tmpDir = "db/replicaFiles" + runNumber;
			file = new File(tmpDir);
			file.mkdirs();
			for (int i = 0; i < (GloBoxDefVals.N_THEATERS); i++) {
				dbReplicaFiles.add(new GloBoxDbFile(tmpDir, i));
			}
		} catch (Exception e) {
			System.out.println("GloBox DB: [ERROR] trying to create " + (GloBoxDefVals.N_THEATERS * 2) +  " files!");
		}
	}

	public void startDbRmiServer() {
		String address = null;
		try {
			//address = (InetAddress.getLocalHost()).toString();
			address = System.getProperty("java.rmi.server.hostname");
		} catch (Exception e) {
			System.out.println("GloBox DB: can't get inet address.");
		}

		dbCounters = new GloBoxCounters("DB", address);
		
		myID = new String(address + ":" + GloBoxDefVals.DB_DEFAULT_PORT + ":" + (long) (Math.random() * GloBoxDefVals.MAX_RANDOM_NUMBER));

		System.out.println("GloBox DB: this address=" + address + ",port=" + GloBoxDefVals.DB_DEFAULT_PORT);

		Registry registry = null;
		try {
			registry = LocateRegistry.createRegistry(GloBoxDefVals.DB_DEFAULT_PORT);
			registry.rebind("GloBoxDbServer", this);
		} catch (Exception e) {
			System.out.println("GloBox DB: ERROR trying to start DB server!");
		}
	}

	@Override
	public String dbReserveOneSeat(short theater, long customer) throws RemoteException {
		dbCounters.incRecvCounter(customer);
		String seats = null;
		try {
			seats = dbMasterFiles.get((int) (Math.random() * (GloBoxDefVals.N_THEATERS))).getTheaterSeats();
		} catch (Exception e) {
			System.out.println("GloBox DB: ERROR trying to get theater data");
			return null;
		}
		return seats;
	}

	@Override
	public boolean dbConfirmPurchase(short theater, short seat, long customer, boolean sync, boolean buffered, boolean objects, short batchSize, boolean replicateDb) throws RemoteException {
		dbCounters.incRecvCounter(customer);
		try {
			if (objects) {
				dbMasterFiles.get((short) (Math.random() * (GloBoxDefVals.N_THEATERS))).writeObjectToFile(seat, batchSize);
			} else if (buffered) {
				dbMasterFiles.get((short) (Math.random() * (GloBoxDefVals.N_THEATERS))).writeToFileBufferedHistory(seat, batchSize);
			} else if (!sync) {
				dbMasterFiles.get((short) (Math.random() * (GloBoxDefVals.N_THEATERS))).writeToFile(seat, false, batchSize);
			} else {
				dbMasterFiles.get((short) (Math.random() * (GloBoxDefVals.N_THEATERS))).writeToFile(seat, true, batchSize);
			}
		} catch (IOException e) {
			dbCounters.incWriteFailure(customer);
			return false;
		}
		if (replicateDb) {
			try {
				if (dbSendToReplicas(theater, seat, customer, sync, buffered, objects, batchSize, replicateDb) < dbRmiReplicas.size()) {
					System.out.println("GloBox DB: could NOT send request to DB's replica (failure # " + missedRep.addAndGet(1) + ")");
					return false;
				}
			} catch (Exception e) {
				System.out.println("GloBox: a FAILURE occured while trying to SEND data to DB REPLICAS!");
				return false;
			}
		}
		return true;
	}

	@Override
	public String getMessageReceiptTS(long customer, String msg, long id, boolean logging) throws RemoteException {
		dbCounters.incRecvCounter(customer);
		if (logging)
			System.out.println("=> Client[" + id + "] msg: " + msg);
		try {
			dbMasterFiles.get((int) (Math.random() * (GloBoxDefVals.N_THEATERS))).writeToFile((short)1, false, (short)1);
		} catch (IOException e) {
			dbCounters.incWriteFailure(customer);
			return "DB SERVER: ERROR writing to disk!";
		}
	return Long.toString(System.currentTimeMillis());
	}

	@Override
	public String getCounters(long customer) throws RemoteException {
		String message = null;
		try {
			message = "[DB SERVER] " + myID 
	        		+ "\n  Master Recv Reqs Count: " 
	        		+ Long.toString(dbCounters.getRecvCounter(customer))
	        		+ "\n  Replica Recv Reqs Count: " 
	        		+ Long.toString(dbCounters.getSendCounter(customer));
        } catch (IOException e) {
	        System.out.println("DB SERVER: ERROR trying to get counters!");
        }
		return message;
	}

	@Override
    public int dbSendToReplicas(short theater, short seat, long customer, boolean sync, boolean buffered, boolean objects, short batchSize, boolean replicateDb) throws RemoteException {
		int i = 0, errors = 0;
		for (i = 0; i < dbRmiReplicas.size(); i++) {
			try {
				dbCounters.incSendCounter(customer);
				dbRmiReplicas.get(i).dbReplicaConfirmPurchase(theater, seat, customer, sync, buffered, objects, batchSize, replicateDb);
			} catch (Exception e) {
				dbCounters.incSendFailCounter(customer);
				errors++;
			}
		}
	    return (i - errors);
    }
	
	public boolean dbReplicaConfirmPurchase(short theater, short seat, long customer, boolean sync, boolean buffered, boolean objects, short batchSize, boolean replicateDb) throws RemoteException {
		dbCounters.incRecvCounterReplica(customer);
		try {
			if (objects) {
				dbMasterFiles.get((short) (Math.random() * (GloBoxDefVals.N_THEATERS))).writeObjectToFile(seat, batchSize);
			} else if (buffered) {
				dbMasterFiles.get((short) (Math.random() * (GloBoxDefVals.N_THEATERS))).writeToFileBufferedHistory(seat, batchSize);
			} else if (!sync) {
				dbMasterFiles.get((short) (Math.random() * (GloBoxDefVals.N_THEATERS))).writeToFile(seat, false, batchSize);
			} else {
				dbMasterFiles.get((short) (Math.random() * (GloBoxDefVals.N_THEATERS))).writeToFile(seat, true, batchSize);
			}
		} catch (Exception e) {
			dbCounters.incWriteFailure(customer);
			return false;
		}
		return true;
	}

	@Override
    public GloBoxCounters getServerCounters() throws RemoteException {
	    return dbCounters;
    }
}

