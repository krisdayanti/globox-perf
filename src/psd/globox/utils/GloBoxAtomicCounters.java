package psd.globox.utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class GloBoxAtomicCounters implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String id, type;
	private AtomicLong recvCounter, sendCounter, 
					   sendFailCounter, recvCounterReplica, 
					   writeFailue, sendBroadcastCounter, recvBroadcastCounter;
	
	public GloBoxAtomicCounters() {
		 recvCounter = new AtomicLong(0);
		 sendCounter = new AtomicLong(0);
		 sendFailCounter = new AtomicLong(0);
		 recvCounterReplica = new AtomicLong(0);
		 writeFailue = new AtomicLong(0);
		 sendBroadcastCounter = new AtomicLong(0);
		 recvBroadcastCounter = new AtomicLong(0);
	}

	public void incSendCounter() {
		sendCounter.incrementAndGet();
	}
	
	public void incSendBroadcastCounter() {
		sendBroadcastCounter.incrementAndGet();
	}
	
	public void incRecvCounter() {
		recvCounter.incrementAndGet();
	}
	
	public void incSendFailCounter() {
		sendFailCounter.incrementAndGet();
	}
	
	public long getSendCounter() throws IOException {
		return sendCounter.get();
	}
	
	public long getRecvCounter() throws IOException {
		return recvCounter.get();
	}
	
	public long getSendFailCounter() throws IOException {
		return sendFailCounter.get();
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getType() {
		return this.type;
	}

	public void incRecvCounterReplica() {
		recvCounterReplica.incrementAndGet();
    }
	
	public long getRecvCounterReplica() throws IOException {
		return recvCounterReplica.get();
	}

	public void incWriteFailure() {
		writeFailue.incrementAndGet();    
    }
	
	public long getWriteFailure() throws IOException {
		return writeFailue.get();
	}

	public void addSendCounter(long delta) {
		sendCounter.addAndGet(delta);
    }

	public long getSendBroadcastCounter() {
	    return sendBroadcastCounter.get();
    }

	public void incRecvBroadcastCounter() {
		recvBroadcastCounter.incrementAndGet();
    }
	
	public long getRecvBroadcastCounter() {
		return recvBroadcastCounter.get();
    }
}
