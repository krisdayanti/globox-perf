package psd.globox.web;

// new sockets are created on-demand depending on the number of remote method invocations
// http://download.java.net/jdk7u6/docs/technotes/guides/rmi/faq.html#numsockets

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import psd.globox.rmi.GloBoxInterApp;
import psd.globox.utils.GloBoxCounters;
import psd.globox.utils.GloBoxDefVals;
import psd.globox.utils.GloBoxProgressBarT;

import com.google.common.base.Stopwatch;

@SuppressWarnings("unused")
public class GloBoxClientMain {

	private static boolean logging = false;
	private static boolean sync = false;
	private static boolean buffered = false;
	private static boolean objects = false;
	private static short batchSize = 1;
	private static boolean broadcast = false;
	private static boolean replicateDb = false;
	private static AtomicLong missedRequests = new AtomicLong(0);
	private static long customers[];
	
	private static ArrayList<GloBoxInterApp> appServers = new ArrayList<GloBoxInterApp>();
	
	@SuppressWarnings({ "deprecation", "static-access" })
    static public void main(String args[]) {

		if (Integer.parseInt(args[0]) != 0)
			logging = true;
		if (Integer.parseInt(args[1]) != 0)
			sync = true;
		if (Integer.parseInt(args[2]) != 0)
			buffered = true;
		if (Integer.parseInt(args[3]) != 0)
			objects = true;
		if (Integer.parseInt(args[4]) > 1 && Integer.parseInt(args[4]) <= 1000)
			batchSize = (short)Integer.parseInt(args[4]);
		if (Integer.parseInt(args[5]) != 0)
			broadcast = true;
		if (Integer.parseInt(args[6]) != 0) {
			replicateDb = true;
		}
		int nThreads = Integer.parseInt(args[7]);
		if (nThreads < 1 || nThreads > 1000) {
			nThreads = 1;
		}
		
		customers = new long[nThreads];
		
		int i;
		for (i = 0; i < nThreads; i++) {
			customers[i] = (long) (Math.random() * GloBoxDefVals.MAX_CLIENT_ID);
		}
		
		int nRequests = Integer.parseInt(args[8]);
		if (nRequests < 1 || nRequests > 1000000) {
			nRequests = 1;
		}
		
		long sendRateLimit = Long.parseLong(args[9]);
				
		for (i = 10; i < args.length; i++) {
			System.out.println("------------------------------------------------------------");
			try {
				System.out.println("GloBox Client: trying to register within App server " + args[i]);
				Registry registry = LocateRegistry.getRegistry(args[i], GloBoxDefVals.APP_DEFAULT_PORT);
				appServers.add((GloBoxInterApp) (registry.lookup("GloBoxAppServer")));
				System.out.println("GloBox Client: successfully registered within App server " + args[i]);
			} catch (Exception e) {
				System.out.println("GloBox Client: ERROR trying to register with App Server " + args[i]);
				System.out.println("    => please, check if the server is up and reachable.");
				System.out.println("------------------------------------------------------------");
				System.exit(-1);
			}
			System.out.println("------------------------------------------------------------");
		}

		System.out.println();
		
		GloBoxProgressBarT progressBar = new GloBoxProgressBarT("Buying " + nThreads * nRequests + " GloBox tickets");
		progressBar.start();

		GloBoxClientThread thread[] = new GloBoxClientThread[nThreads];
		for (i = 0; i < nThreads; i++) {
			thread[i] = new GloBoxClientThread(customers[i], sendRateLimit, missedRequests, nRequests, appServers, logging, sync, buffered, objects, batchSize, broadcast, replicateDb);
		}

		Stopwatch stopwatch = Stopwatch.createStarted();
		for (i = 0; i < nThreads; i++) {
			thread[i].start();
		}
		for (i = 0; i < nThreads; i++) {
			try {
				thread[i].join();
			} catch (InterruptedException e) {
	        	System.out.println("GloBox Client: [ERROR] join for thread idx " + i + " FAILED");
			}
		}
		stopwatch.stop();

		progressBar.stop();

		System.out.println("\n");
		
		try {
	        Thread.currentThread().sleep(GloBoxDefVals.STARTUP_WAITING_IN_MS / 2);
        } catch (InterruptedException e1) {
        	System.out.println("GloBox Client: [ERROR] thread couldn't sleep for " + (GloBoxDefVals.STARTUP_WAITING_IN_MS / 2) + " ms");
        }

		boolean getCountersFailed = false;
		for (i = 0; i < appServers.size(); i++) {
			try {
				//System.out.println();
				appServers.get(i).getCounters(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
			} catch (RemoteException e) {
				System.out.println("GloBox Client: [WARNING] couldn't get counter from App Server " + i);
				System.out.println("    => the server may be down or too busy.");
				getCountersFailed = true;
			}
		}
		
		for (i = 0; i < appServers.size(); i++) {
			try {
				//System.out.println();
				appServers.get(i).getCountersDBs(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
				break;
			} catch (RemoteException e) {
				System.out.println("GloBox Client: [WARNING] couldn't get counter from App/DB Server " + i);
				System.out.println("    => the server may be down or too busy.");
				getCountersFailed = true;
			}
		}
		
		ArrayList<GloBoxCounters> appCounters = new ArrayList<GloBoxCounters>();
		for (i = 0; i < appServers.size(); i++) {
			try {
				GloBoxCounters counters = appServers.get(i).getServerCounters();
				appCounters.add(counters);
			} catch (RemoteException e) {
				System.out.println("GloBox Client: [WARNING] couldn't get counters from App Server " + i);
				System.out.println("    => the server may be down or too busy.");
				getCountersFailed = true;
			}
		}
		
		ArrayList<GloBoxCounters> dbCounters = new ArrayList<GloBoxCounters>();
		for (i = 0; i < appServers.size(); i++) {
			try {
				dbCounters = appServers.get(i).getServerCountersDBs();
				break;
			} catch (RemoteException e) {
				System.out.println("GloBox Client: [WARNING] couldn't get counter from App/DB Server " + i);
				System.out.println("    => the server may be down or too busy.");
				getCountersFailed = true;
			}
		}
		
		long nRecvsApps = 0, nRecvsDbs = 0, 
			 nSendsApps = 0, nSendsDbs = 0, 
			 nWriteFailures = 0, nRecvReplicaDBs = 0,
			 nSendFailuresApps = 0, nSendFailuresDbs = 0,
			 nSendBroadcastApps = 0, nRecvBroadcastApps = 0;

		for (i = 0; i < appCounters.size(); i++) {
			try {
	            nSendsApps += appCounters.get(i).getSendCounter(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
				nRecvsApps += appCounters.get(i).getRecvCounter(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
				nSendFailuresApps += appCounters.get(i).getSendFailCounter(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
				nSendBroadcastApps += appCounters.get(i).getSendBroadcastCounter(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
				nRecvBroadcastApps += appCounters.get(i).getRecvBroadcastCounter(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
            } catch (IOException e) {
	            e.printStackTrace();
            }
		}
		
		for (i = 0; i < dbCounters.size(); i++) {
			try {
				nSendsDbs += dbCounters.get(i).getSendCounter(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
	            nRecvsDbs += dbCounters.get(i).getRecvCounter(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
	            nSendFailuresDbs += dbCounters.get(i).getSendFailCounter(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
	            nWriteFailures += dbCounters.get(i).getWriteFailure(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
	            nRecvReplicaDBs += dbCounters.get(i).getRecvCounterReplica(GloBoxDefVals.COUNTERS_GLOBAL_VALUE);
            } catch (IOException e) {
	            e.printStackTrace();
            }
		}
		
		System.out.println("----------------------------------------------------------------------");
		System.out.println("GloBox Global (CUMULATIVE - all client execs) Stats:");
		System.out.println("\t [DBs] # of recv: " + nRecvsDbs);
		System.out.println("\t [DBs] # of send to replicas: " + nSendsDbs);
		System.out.println("\t [DBs] # of recv at replicas: " + nRecvReplicaDBs);
		System.out.println("\t [DBs] # of send to replicas failures: " + nSendFailuresDbs);
		System.out.println("\t [DBs] # of disk write failures: " + nWriteFailures);
		System.out.println("\t [APPs] # of recv from clients: " + nRecvsApps);
		System.out.println("\t [APPs] # of send broadcast: " + nSendBroadcastApps);
		System.out.println("\t [APPs] # of recv broadcast: " + nRecvBroadcastApps);
		System.out.println("\t [APPs] # of FAILED broadcasts: " + (nSendBroadcastApps-nRecvBroadcastApps));
		if (nSendBroadcastApps == 0)
			System.out.println("\t [APPs] % of FAILED broadcasts: " + 0.0 + "%");
		else 
			System.out.println("\t [APPs] % of FAILED broadcasts: " + (float)((nSendBroadcastApps-nRecvBroadcastApps)*100/nSendBroadcastApps) + "%");		
		System.out.println("\t [APPs] # of successful send/recv to/from DBs: " + nSendsApps);
		//System.out.println("\t [APPs] # of send to DBs failures: " + nSendFailuresApps);
		System.out.println("\t # of FAILED reqs between APPs and DBs: " + Math.abs(nSendsApps - nRecvsDbs));
		if ((nSendsApps - nRecvsDbs) == 0)
			System.out.println("\t % of FAILED reqs between APPs and DBs: "+ 0.0 + "%");
		else
			System.out.println("\t % of FAILED reqs between APPs and DBs: " + Math.abs((float)((nSendsApps - nRecvsDbs) * 100) / nSendsApps )+ "%");
		System.out.println("\t # of FAILED reqs between DBs and DB Replicas: " + Math.abs(nSendsDbs - nRecvReplicaDBs));
		if ((nSendsDbs - nRecvReplicaDBs) == 0)
			System.out.println("\t % of FAILED reqs between DBs and DB Replicas: " + 0.0 + "%");
		else
			System.out.println("\t % of FAILED reqs between DBs and DB Replicas: " + Math.abs((float)((nSendsDbs - nRecvReplicaDBs) * 100) / nSendsDbs) + "%");
		System.out.println("----------------------------------------------------------------------");
		if (getCountersFailed) {
			System.out.println("GloBox Client: [WARNING] some of the counter could not be retrived!");
			System.out.println("GloBox Client: [WARNING] the correctness of the stats might have been compromised!");
			System.out.println("GloBox Client: [WARNING] please, check the warning messages above!");
		}
		nRecvsApps = nRecvsDbs = nSendsApps = nSendsDbs = 0;
		nWriteFailures = nRecvReplicaDBs = nSendFailuresApps = 0;
		nSendFailuresDbs = nSendBroadcastApps = nRecvBroadcastApps = 0;
		
		for (i = 0; i < appCounters.size(); i++) {
			for (int custIdx = 0; custIdx < nThreads; custIdx++) {
				try {
		            nSendsApps += appCounters.get(i).getSendCounter(customers[custIdx]);
					nRecvsApps += appCounters.get(i).getRecvCounter(customers[custIdx]);
					nSendFailuresApps += appCounters.get(i).getSendFailCounter(customers[custIdx]);
					nSendBroadcastApps += appCounters.get(i).getSendBroadcastCounter(customers[custIdx]);
					nRecvBroadcastApps += appCounters.get(i).getRecvBroadcastCounter(customers[custIdx]);
	            } catch (IOException e) {
		            System.out.println("GloBox Client: ERROR trying to get APP Server counters for client " + customers[i]);
	            }
			}
		}
		
		for (i = 0; i < dbCounters.size(); i++) {
			for (int custIdx = 0; custIdx < nThreads; custIdx++) {
				try {
					nSendsDbs += dbCounters.get(i).getSendCounter(customers[custIdx]);
		            nRecvsDbs += dbCounters.get(i).getRecvCounter(customers[custIdx]);
		            nSendFailuresDbs += dbCounters.get(i).getSendFailCounter(customers[custIdx]);
		            nWriteFailures += dbCounters.get(i).getWriteFailure(customers[custIdx]);
		            nRecvReplicaDBs += dbCounters.get(i).getRecvCounterReplica(customers[custIdx]);
	            } catch (IOException e) {
		            System.out.println("GloBox Client: ERROR trying to get DB Server counters for client " + customers[i]);
	            }
			}
		}
		
		System.out.println("----------------------------------------------------------------------");
		System.out.println("GloBox Stats for " + customers.length + " Client Threads (current exec):");
		System.out.println("\t [DBs] # of recv: " + nRecvsDbs);
		System.out.println("\t [DBs] # of send to replicas: " + nSendsDbs);
		System.out.println("\t [DBs] # of recv at replicas: " + nRecvReplicaDBs);
		System.out.println("\t [DBs] # of send to replicas failures: " + nSendFailuresDbs);
		System.out.println("\t [DBs] # of write to disk failures: " + nWriteFailures);
		System.out.println("\t [APPs] # of recv: " + nRecvsApps);
		System.out.println("\t [APPs] # of send broadcast: " + nSendBroadcastApps);
		System.out.println("\t [APPs] # of recv broadcast: " + nRecvBroadcastApps);
		System.out.println("\t [APPs] # of FAILED broadcasts: " + (nSendBroadcastApps-nRecvBroadcastApps));
		if (nSendBroadcastApps == 0)
			System.out.println("\t [APPs] % of FAILED broadcasts: " + 0.0 + "%");
		else 
			System.out.println("\t [APPs] % of FAILED broadcasts: " + (float)((nSendBroadcastApps-nRecvBroadcastApps)*100.0/nSendBroadcastApps) + "%");		
		System.out.println("\t [APPs] of successful send/recv to/from DBs: " + nSendsApps);
		//System.out.println("\t [APPs] # of send to DBs failures: " + nSendFailuresApps);
		System.out.println("\t # of FAILED reqs between APPs and DBs: " + Math.abs(nSendsApps - nRecvsDbs));
		if ((nSendsApps - nRecvsDbs) == 0)
			System.out.println("\t % of FAILED reqs between APPs and DBs: " + 0.0 + "%");
		else
			System.out.println("\t % of FAILED reqs between APPs and DBs: " + Math.abs((float)((nSendsApps - nRecvsDbs) * 100.0) / nSendsApps) + "%");
		System.out.println("\t # of FAILED reqs between DBs and DB Replicas: " + Math.abs(nSendsDbs - nRecvReplicaDBs));
		if ((nSendsDbs - nRecvReplicaDBs) == 0) 
			System.out.println("\t % of FAILED reqs between DBs and DB Replicas: " + 0.0 + "%");
		else
			System.out.println("\t % of FAILED reqs between DBs and DB Replicas: " + Math.abs((float)((nSendsDbs - nRecvReplicaDBs) * 100.0) / nSendsDbs) + "%");
		System.out.println("----------------------------------------------------------------------");
		if (getCountersFailed) {
			System.out.println("GloBox Client: [WARNING] some of the counter could not be retrived!");
			System.out.println("GloBox Client: [WARNING] the correctness of the stats might have been compromised!");
			System.out.println("GloBox Client: [WARNING] please, check the warning messages above!");
		}
		
		long milliseconds = stopwatch.elapsed(java.util.concurrent.TimeUnit.MILLISECONDS);
		float seconds = (float) (milliseconds / 1000.0);
		
		System.out.println("----------------------------------------------------------------------");
		System.out.println("\nGloBoxClient: " + (nThreads * nRequests) + " (number of requested operations)");
		System.out.println("\nGloBoxClient: " + (nThreads) + " (number of concurrent threads)");
		System.out.println("\nGloBoxClient: " + missedRequests.get() + " FAILED operations");
		System.out.println("GloBoxClient: " + (float)(missedRequests.get() * 100.0) / (nThreads * nRequests) + "% of FAILED operations");
		System.out.println("\nGloBoxClient: " + (float)(((float)(nThreads * nRequests) - missedRequests.get()) / seconds) + " operations(reservation+purchase)/s\n");
		System.out.println("----------------------------------------------------------------------");
		
	}
}

