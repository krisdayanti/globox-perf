package psd.globox.app;

import psd.globox.utils.GloBoxDefVals;

public class GloBoxAppMain {
	
	static public void main(String args[]) {
		String[] dbIPs = new String[args.length];
		String[] appIPs = new String[args.length];
		
		int i;
		for (i = 0; i < args.length; i++) {
			if (!args[i].contains("xx")) {
				appIPs[i] = args[i];
			}
			if (args[i].contains("xx")) {
				break;
			}
		}
		i++;
		for (int j = 0; j < args.length && i < args.length; j++, i++) {
			dbIPs[j] = args[i];
		}
		
		try {
			GloBoxApp server = new GloBoxApp();
			server.connectToDbServers(dbIPs);
			server.startAppRmiServer();
			System.out.print("GloBox APP: waiting 10s for APP servers ");
			for (int sleepRound = 0; sleepRound < 10; sleepRound++) {
				System.out.print(".");
				Thread.sleep(GloBoxDefVals.STARTUP_WAITING_IN_MS/10);
			}
			System.out.println(" done.");
			server.connectToAppServers(appIPs);
		} catch (Exception e) {
			System.out.println("GloBox App: ERROR trying to initialize GloBoxApp server!");
		}
	}
}

