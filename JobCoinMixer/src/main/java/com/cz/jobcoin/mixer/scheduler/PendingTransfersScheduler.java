package com.cz.jobcoin.mixer.scheduler;

import java.net.URISyntaxException;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cz.jobcoin.mixer.data.dao.PendingTransfersDAO;
import com.cz.jobcoin.mixer.data.dao.ReservedAdressesDAO;
import com.cz.jobcoin.mixer.data.model.PendingTransfer;
import com.cz.jobcoin.mixer.services.JobCoinAPIService;

@Component
public class PendingTransfersScheduler {
	@Autowired
	PendingTransfersDAO pendingTransfersDAO;
	@Autowired
	JobCoinAPIService jobCoinAPIService;
	@Autowired
	ReservedAdressesDAO reservedAdressesDAO;
	 
	@Scheduled(fixedDelayString = "${scheduler.pending.transfers.delay}")
	public void launchCurrentPendingTransfers() throws URISyntaxException {
		Instant currentTime = Instant.now();
		PendingTransfer frontTransfer = pendingTransfersDAO.peek();
		if(frontTransfer!=null) {
			if(frontTransfer.getLockTime().compareTo(currentTime)<=0) {
				 frontTransfer = pendingTransfersDAO.poll();
				 if(frontTransfer.getLockTime().compareTo(currentTime)<=0) {
					 //transfer
					 launchTransfer(frontTransfer); // TODO add return type annd logic if failed
				 }else {
					 //reinsert back into the queue
					 pendingTransfersDAO.add(frontTransfer);
				 }
			}
		}
	}
	
	public void launchTransfer(PendingTransfer transfer) throws URISyntaxException {
		ResponseEntity<String> transferResponse = jobCoinAPIService.transfer(transfer.getSender(), transfer.getReceiver(), transfer.getAmount());
		if (transferResponse.getStatusCode().equals(HttpStatus.OK)) {
			//remove addresses from reserved addresses list
			//because they should be present on chain now
			reservedAdressesDAO.removeAddress(transfer.getReceiver());
		}
	}
}
