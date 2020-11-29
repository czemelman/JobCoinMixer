package com.cz.jobcoin.mixer.data.dao;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.cz.jobcoin.mixer.data.model.DepositWatchItem;
@Component
public class DepositWatchDAO {
	private Map<String, DepositWatchItem> depositAddressesToWatch = new ConcurrentHashMap<>();

	public void addDepositAddress(String address, List<String> withdrawalAddresses) {
		DepositWatchItem newDepositWatch = new DepositWatchItem();
		newDepositWatch.setAddress(address);
		newDepositWatch.setWatchStartTime(Instant.now());
		newDepositWatch.setWithdrawalAddresses(withdrawalAddresses);
		depositAddressesToWatch.put(address, newDepositWatch);
	}

	public DepositWatchItem removeDepositAddress(String address) {
		return depositAddressesToWatch.remove(address);
	}

	
	public Set<String> getAllDepositAdddresses(){
		return depositAddressesToWatch.keySet();
	}
	
	public DepositWatchItem get(String address) {
		return depositAddressesToWatch.get(address);
	}
	
	
}
