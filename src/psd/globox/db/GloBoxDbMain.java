package psd.globox.db;

import psd.globox.utils.GloBoxDefVals;


public class GloBoxDbMain {

	static public void main(String args[]) {
			GloBoxDb dbMainServer;
			try {
		        dbMainServer = new GloBoxDb();
	        	dbMainServer.createTmpFiles();
				dbMainServer.startDbRmiServer();
				System.out.print("GloBox DB: waiting 10s for DB servers ");
				for (int sleepRound = 0; sleepRound < 10; sleepRound++) {
					System.out.print(".");
				        Thread.sleep(GloBoxDefVals.STARTUP_WAITING_IN_MS/10);
	                
				}
				System.out.println(" done.");
				dbMainServer.connectToDbReplicas(args);
			} catch (Exception e) {
            	System.out.println("GloBox DB: one ERROR occured during DB server init!");
            }
	}

}

