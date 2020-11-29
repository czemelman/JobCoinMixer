package com.cz.jobcoin.mixer.data.dao;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
/**
 * Purpose: to account for addresses that are in use but not on the chain yet
 * either as deposit or withdrawal addresses
 * @author Constantin Zemelman
 *
 */
@Component
public class ReservedAdressesDAO {
	private Set<String> reservedAddresses =  ConcurrentHashMap.newKeySet();
	
	public void addAddress(String address) {
		reservedAddresses.add(address);
	}
	
	public void removeAddress(String address) {
		reservedAddresses.remove(address);
	}
	
	public boolean exist(String address) {
		return reservedAddresses.contains(address);
	}
	
	public void addAll(Collection<String> addresses) {
		reservedAddresses.addAll(addresses);
	}
}
