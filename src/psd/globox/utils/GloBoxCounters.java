package psd.globox.utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class GloBoxCounters implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String id, type;
	private HashMap<Long, GloBoxAtomicCounters> atomicCounters = new HashMap<Long, GloBoxAtomicCounters>();
	
	private AtomicLong recvCounter, sendCounter, 
					   sendFailCounter, recvCounterReplica, 
					   writeFailue, sendBroadcastCounter, recvBroadcastCounter;
	
	public GloBoxCounters(String type, String id) {
		 recvCounter = new AtomicLong(0);
		 sendCounter = new AtomicLong(0);
		 sendFailCounter = new AtomicLong(0);
		 recvCounterReplica = new AtomicLong(0);
		 writeFailue = new AtomicLong(0);
		 sendBroadcastCounter = new AtomicLong(0);
		 recvBroadcastCounter = new AtomicLong(0);
		 this.type = type;
		 this.id = id;
	}

	private void createIfDoesNotExist(long clientId) {
		if (!atomicCounters.containsKey(clientId)) {
			synchronized (this) {
				if (!atomicCounters.containsKey(clientId))
					atomicCounters.put(clientId, new GloBoxAtomicCounters());
			}		
		}
	}
	
	public void incSendCounter(long clientId) {
		createIfDoesNotExist(clientId);
		atomicCounters.get(clientId).incSendCounter();
		sendCounter.incrementAndGet();
	}
	
	public void incSendBroadcastCounter(long clientId) {
		createIfDoesNotExist(clientId);
		atomicCounters.get(clientId).incSendBroadcastCounter();
		sendBroadcastCounter.incrementAndGet();
	}
	
	public void incRecvCounter(long clientId) {
		createIfDoesNotExist(clientId);
		atomicCounters.get(clientId).incRecvCounter();
		recvCounter.incrementAndGet();
	}
	
	public void incSendFailCounter(long clientId) {
		createIfDoesNotExist(clientId);
		atomicCounters.get(clientId).incSendFailCounter();
		sendFailCounter.incrementAndGet();
	}
	
	public long getSendCounter(long clientId) throws IOException {
		if (clientId == GloBoxDefVals.COUNTERS_GLOBAL_VALUE) {
			return sendCounter.get();
		}
		if (!atomicCounters.containsKey(clientId)) 
			return 0;
		return atomicCounters.get(clientId).getSendCounter();
	}
	
	public long getRecvCounter(long clientId) throws IOException {
		if (clientId == GloBoxDefVals.COUNTERS_GLOBAL_VALUE) {
			return recvCounter.get();
		}
		if (!atomicCounters.containsKey(clientId)) 
			return 0;
		return atomicCounters.get(clientId).getRecvCounter();
	}
	
	public long getSendFailCounter(long clientId) throws IOException {
		if (clientId == GloBoxDefVals.COUNTERS_GLOBAL_VALUE) {
			return sendFailCounter.get();
		}
		if (!atomicCounters.containsKey(clientId)) 
			return 0;
		return atomicCounters.get(clientId).getSendFailCounter();
	}
	
	public String getId(long clientId) {
		return this.id;
	}
	
	public String getType(long clientId) {
		return this.type;
	}

	public void incRecvCounterReplica(long clientId) {
		atomicCounters.get(clientId).incRecvCounterReplica();
		recvCounterReplica.incrementAndGet();
    }
	
	public long getRecvCounterReplica(long clientId) throws IOException {
		if (clientId == GloBoxDefVals.COUNTERS_GLOBAL_VALUE) {
			return recvCounterReplica.get();
		}
		if (!atomicCounters.containsKey(clientId)) 
			return 0;
		return atomicCounters.get(clientId).getRecvCounterReplica();
	}

	public void incWriteFailure(long clientId) {
		atomicCounters.get(clientId).incWriteFailure();
		writeFailue.incrementAndGet();    
    }
	
	public long getWriteFailure(long clientId) throws IOException {
		if (clientId == GloBoxDefVals.COUNTERS_GLOBAL_VALUE) {
			return writeFailue.get();
		}
		if (!atomicCounters.containsKey(clientId)) 
			return 0;
		return atomicCounters.get(clientId).getWriteFailure();

	}

	public void addSendCounter(long clientId, long delta) {
		atomicCounters.get(clientId).addSendCounter(delta);
		sendCounter.addAndGet(delta);
    }

	public long getSendBroadcastCounter(long clientId) {
		if (clientId == GloBoxDefVals.COUNTERS_GLOBAL_VALUE) {
			return sendBroadcastCounter.get();
		}
		if (!atomicCounters.containsKey(clientId)) 
			return 0;
		return atomicCounters.get(clientId).getSendBroadcastCounter();
    }

	public void incRecvBroadcastCounter(long clientId) {
		atomicCounters.get(clientId).incRecvBroadcastCounter();
		recvBroadcastCounter.incrementAndGet();
    }
	
	public long getRecvBroadcastCounter(long clientId) {
		if (clientId == GloBoxDefVals.COUNTERS_GLOBAL_VALUE) {
			return recvBroadcastCounter.get();
		}
		if (!atomicCounters.containsKey(clientId)) 
			return 0;
		return atomicCounters.get(clientId).getRecvBroadcastCounter();
    }
}
