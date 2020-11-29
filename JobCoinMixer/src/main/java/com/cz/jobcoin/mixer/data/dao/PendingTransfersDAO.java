package com.cz.jobcoin.mixer.data.dao;

import java.util.Collection;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.springframework.stereotype.Component;

import com.cz.jobcoin.mixer.data.model.PendingTransfer;
@Component
public class PendingTransfersDAO {
	PriorityBlockingQueue<PendingTransfer> queue = new PriorityBlockingQueue<PendingTransfer>();
	public void add(PendingTransfer transfer) {
		queue.add(transfer);
	}
	public void addAll(Collection<PendingTransfer> transfers) {
		queue.addAll(transfers);
	}
	public PendingTransfer peek() {
		return queue.peek();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public PendingTransfer poll() {
		return queue.poll();
	}
	
}
