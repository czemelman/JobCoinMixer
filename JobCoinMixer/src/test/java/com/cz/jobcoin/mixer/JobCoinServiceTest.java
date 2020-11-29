package com.cz.jobcoin.mixer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cz.jobcoin.mixer.data.dao.ReservedAdressesDAO;
import com.cz.jobcoin.mixer.data.model.JobCoinAddressInfoResponse;
import com.cz.jobcoin.mixer.services.JobCoinAPIService;
import com.cz.jobcoin.mixer.services.JobCoinService;
import com.cz.jobcoin.mixer.utils.CommonUtils;

@SpringBootTest
class JobCoinServiceTest {

	
	@Autowired
	JobCoinAPIService jobCoinAPIService;
	@Autowired
	ReservedAdressesDAO reservedAddressesService;
	
	@Autowired
	JobCoinService jobCoinService;
	@Test
	void testApis() throws URISyntaxException {
		//generate new unique address
		String newAddress = RandomStringUtils.random(40, true, true);
		ResponseEntity<JobCoinAddressInfoResponse> initialInfoResponse = jobCoinAPIService.getAddressInfo(newAddress);
		assert(initialInfoResponse.getStatusCode().equals(HttpStatus.OK));
		JobCoinAddressInfoResponse initialResponseBody = initialInfoResponse.getBody();
		assert(initialResponseBody != null);
		assert(initialResponseBody.getBalance().equals(BigDecimal.ZERO));
		assert(initialResponseBody.getTransactions() == null || initialResponseBody.getTransactions().length==0);
		
		//transfer random amount between 0.01 and 0.1 jobcoin to new address
		BigDecimal amount = CommonUtils.generateRandomBigDecimalFromRange(new BigDecimal("0.01"), new BigDecimal("0.1")).round(new MathContext(6, RoundingMode.CEILING));
		ResponseEntity<String> transferResponse = jobCoinAPIService.transfer(TestUtils.SYSTEM_TEST_ACCOUNT_ADDRESS , newAddress, amount);
		assert(transferResponse.getStatusCode().equals(HttpStatus.OK));
		
		//check info of new address after transfering some money into it
		ResponseEntity<JobCoinAddressInfoResponse> afterTransferResponse = jobCoinAPIService.getAddressInfo(newAddress);
		assert(afterTransferResponse.getStatusCode().equals(HttpStatus.OK));
		JobCoinAddressInfoResponse afterTransferResponseBody = afterTransferResponse.getBody();
		assert(afterTransferResponseBody != null);
		assert(afterTransferResponseBody.getBalance().equals(amount));
		assert(afterTransferResponseBody.getTransactions() != null || initialResponseBody.getTransactions().length==1);
	}
	
	@Test
	void testAddresInUse() {
		// test non existing addresses
		String newAddress = RandomStringUtils.random(40, true, true);
		String reservedAdddress = RandomStringUtils.random(50, true, true);
		assertFalse(jobCoinService.isAddressInUse(newAddress));
		//add address to reserved addresses
		assertFalse(jobCoinService.isAddressInUse(reservedAdddress));
		reservedAddressesService.addAddress(reservedAdddress);
		assertTrue(jobCoinService.isAddressInUse(reservedAdddress));
		
		//test on chain address that we know exist
		assertTrue(jobCoinService.isAddressInUse(TestUtils.SYSTEM_TEST_ACCOUNT_ADDRESS));
		
		//test list of addresses in use
		List<String>  listToCheck = new ArrayList<String>();
		listToCheck.add(newAddress);
		listToCheck.add(reservedAdddress);
		listToCheck.add(TestUtils.SYSTEM_TEST_ACCOUNT_ADDRESS);
		List<String> inUseAddressses = jobCoinService.getAddressesInUse(listToCheck);
		assertTrue(inUseAddressses.size()==2);
		
	}

}
