package psd.globox.web;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.RateLimiter;

import psd.globox.rmi.GloBoxInterApp;
import psd.globox.utils.GloBoxDefVals;

public class GloBoxClientThread extends Thread {
	private boolean logging;
	private boolean sync;
	private boolean buffered;
	private boolean objects;
	private short batchSize; 
	private boolean broadcast;
	private boolean replicateDb;
	private long nRequests;
	private long customer;
	private long sendRateLimit;
	private AtomicLong missedRequests;
	private ArrayList<GloBoxInterApp> appServers;

	public GloBoxClientThread(long customer, long rateLimit, AtomicLong missedRequests, long nRequests, ArrayList<GloBoxInterApp> appServers, boolean logging, boolean sync, boolean buffered, boolean objects, short batchSize, boolean broadcast, boolean replicateDb) {
		this.nRequests = nRequests;
		this.appServers = appServers;
		this.logging = logging;
		this.sync = sync; 
		this.buffered = buffered; 
		this.objects = objects;
		this.batchSize = batchSize;
		this.broadcast = broadcast;
		this.replicateDb = replicateDb;
		this.missedRequests = missedRequests;
		this.customer = customer;
		this.sendRateLimit = rateLimit;
		setDaemon(true);
	}

	private static long nextRandomID() {
		return (long) (Math.random() * GloBoxDefVals.MAX_CLIENT_ID);
	}

	private static short randomSeat() {
		return (short) (Math.random() * (GloBoxDefVals.THEATER_SEATS));
	}

	private static short randomTheater() {
		return (short) (Math.random() * GloBoxDefVals.N_THEATERS);
	}

	public void run() {
		
		if (sendRateLimit > 0) {
			RateLimiter sendRate = RateLimiter.create(sendRateLimit); // 100/s
			for (int n = 0; n < nRequests; n++) {
				for (int i = 0; i < appServers.size(); i++) {
					short theater = randomTheater();
					sendRate.acquire();
					try {
						String freePlaces = appServers.get(i).appMakeNewReservation(theater, this.customer, broadcast);
						boolean confirmPurchase = appServers.get(i).appConfirmPurchase(theater, randomSeat(), this.customer, sync, buffered, objects, batchSize, replicateDb);
						if (confirmPurchase != true || freePlaces == null) 
							missedRequests.addAndGet(1);
					} catch (Exception e) {
						missedRequests.addAndGet(1);
					}
				}
			}
		} else {
			for (int n = 0; n < nRequests; n++) {
				for (int i = 0; i < appServers.size(); i++) {
					short theater = randomTheater();
					try {
						String freePlaces = appServers.get(i).appMakeNewReservation(theater, this.customer, broadcast);
						boolean confirmPurchase = appServers.get(i).appConfirmPurchase(theater, randomSeat(), this.customer, sync, buffered, objects, batchSize, replicateDb);
						if (confirmPurchase != true || freePlaces == null) 
							missedRequests.addAndGet(1);
					} catch (Exception e) {
						missedRequests.addAndGet(1);
					}
				}
			}
		}
	}

	public void runTest() {
		try {
			for (int n = 0; n < nRequests; n++) {
				for (int i = 0; i < appServers.size(); i++) {
					// make round robin calls
					if (logging) {
						System.out.println("APP TS: "
						        + appServers.get(i).getMessageReceiptTS(this.customer,"Give me [th:" + Long.toString(Thread.currentThread().getId()) + "] your TS",
						                nextRandomID(), logging));
						System.out.println("DB TS: "
						        + appServers.get(i).getDbMessageReceiptTS(this.customer,"Give me [th:" + Long.toString(Thread.currentThread().getId()) + "] your TS",
						                nextRandomID(), logging));
					} else {
						appServers.get(i).getMessageReceiptTS(this.customer,"Give me [th:" + Long.toString(Thread.currentThread().getId()) + "] your TS", nextRandomID(),
						        logging);
						appServers.get(i).getDbMessageReceiptTS(this.customer,"Give me [th:" + Long.toString(Thread.currentThread().getId()) + "] your TS", nextRandomID(),
						        logging);
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
