package com.cz.jobcoin.mixer.scheduler;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cz.jobcoin.mixer.data.dao.DepositWatchDAO;
import com.cz.jobcoin.mixer.data.model.DepositWatchItem;
import com.cz.jobcoin.mixer.services.JobCoinService;
import com.cz.jobcoin.mixer.services.MixerService;
@Component
public class DepositWatchScheduler {
	@Autowired
	DepositWatchDAO depositWatchDAO;
	@Autowired
	JobCoinService jobCoinService;
	
	@Autowired
	MixerService mixerService;
	
	@Value("${deposit.watch.expiration.hours}")
	int hoursToExpiration;
	
	@Scheduled(fixedDelayString = "${scheduler.deposit.delay}")
	public void depositWatchTask() throws URISyntaxException {	
		//loop thru all currently available deposit addresses
		Set<String> depositAddresses = depositWatchDAO.getAllDepositAdddresses();
		for(String currentAddress : depositAddresses) {
			// check balance for each
			BigDecimal currentBalance = jobCoinService.getBalance(currentAddress);
			if(currentBalance.compareTo(BigDecimal.ZERO) >0) {
				//balance greater than 0 indicates
				//that deposit was made and mixing should be started
				DepositWatchItem depositItem =  depositWatchDAO.removeDepositAddress(currentAddress);
				mixerService.mixFromDepositAddressAsync(depositItem);
			}else {
				//check if deposit address entry is not expired
				DepositWatchItem depositItem =  depositWatchDAO.get(currentAddress);
				Duration watchDuration = Duration.between(depositItem.getWatchStartTime(), Instant.now());
				if(watchDuration.toHours() >hoursToExpiration) {
					// if deposit watch entry is expired - remove it from the list
					depositWatchDAO.removeDepositAddress(currentAddress);
				}
			}
		}
	}
}
