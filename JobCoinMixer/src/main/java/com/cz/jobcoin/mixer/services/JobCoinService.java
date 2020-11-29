package com.cz.jobcoin.mixer.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cz.jobcoin.mixer.data.dao.ReservedAdressesDAO;
import com.cz.jobcoin.mixer.data.model.JobCoinAddressInfoResponse;

@Service
public class JobCoinService {
	@Autowired
	JobCoinAPIService jobCoiAPIService;
	@Autowired
	ReservedAdressesDAO reservedAdressesService;
	
	/**
	 * checks if given address was used on chain or reserved to be used as
	 * reserved address (as withdrawal or deposit )
	 * @param address
	 * @return true if in use
	 */
	public boolean isAddressInUse(String address) {
		boolean addressInUse = false;
		//check is address is one of the reserved addresses
		addressInUse = reservedAdressesService.exist(address);
		if(!addressInUse) {
			//if not resent in reserved addresses check if there are any on chain transactions 
			//associated with this address
			ResponseEntity<JobCoinAddressInfoResponse> addressResponse = jobCoiAPIService.getAddressInfo(address);
			if(addressResponse.getStatusCode().equals(HttpStatus.OK)){
				JobCoinAddressInfoResponse addressResponseBody = addressResponse.getBody();
				if(addressResponseBody != null) {
					addressInUse = (addressResponseBody.getTransactions() != null 
							&& addressResponseBody.getTransactions().length >0);
				}
			}
		}
		return addressInUse;
	}
	
	/**
	 * returns only addresses that are in use ( reserved or present on chain)
	 * @param addresses - list of addresses to check
	 * @return list of addresses in use
	 */
	public List<String> getAddressesInUse(List<String> addresses){
		List<String> addressesInUse = new ArrayList<String>();
		if(addresses !=null) {
			addressesInUse = addresses.stream()
				      .filter(currentAddress -> isAddressInUse(currentAddress))
				      .collect(Collectors.toList());
		}
		return addressesInUse;
		
	}
	
	/**
	 * 
	 * @param address
	 * @return current balance for a given address
	 */
	public BigDecimal getBalance(String address) {
		BigDecimal returnBalance = BigDecimal.ZERO;
		ResponseEntity<JobCoinAddressInfoResponse> addressResponse = jobCoiAPIService.getAddressInfo(address);
		if(addressResponse.getStatusCode().equals(HttpStatus.OK)){
			JobCoinAddressInfoResponse addressResponseBody = addressResponse.getBody();
			if(addressResponseBody != null) {
				returnBalance = addressResponseBody.getBalance();
			}
		}
		return returnBalance;
	}
	
	/**
	 * 
	 * @param addresses- list of addresses
	 * @return Cumulative balance for the given list of addresses
	 */
	public BigDecimal getTotalBalance(List<String> addresses) {
		BigDecimal returnBalance = BigDecimal.ZERO;
		for(String currAdddres :addresses) {
			returnBalance = returnBalance.add(getBalance(currAdddres));
		}
		return returnBalance;
	}
	
	public String generateNewDepositAddress() {
		String newAddress = RandomStringUtils.random(40, true, true);
		while(isAddressInUse(newAddress)) {
			newAddress = RandomStringUtils.random(40, true, true);
		}
		return newAddress;
	}
	
}
