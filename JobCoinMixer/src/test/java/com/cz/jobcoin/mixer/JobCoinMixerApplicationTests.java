package com.cz.jobcoin.mixer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cz.jobcoin.mixer.data.model.JobCoinAddressInfoResponse;
import com.cz.jobcoin.mixer.data.model.PendingTransfer;

@SpringBootTest
class JobCoinMixerApplicationTests {

	@Test
	void testRestTemplate() throws URISyntaxException {
		
		
		PriorityQueue<PendingTransfer> queue = new PriorityQueue<PendingTransfer>();
		
		PendingTransfer tr1 = new PendingTransfer();
		tr1.setSender("1");
		tr1.setLockTime(Instant.now().plusSeconds(10000));
		queue.add(tr1);
		
		PendingTransfer tr2 = new PendingTransfer();
		tr2.setSender("2");
		tr2.setLockTime(Instant.now().minusSeconds(10000));
		queue.add(tr2);
		
		PendingTransfer tr3 = new PendingTransfer();
		tr3.setSender("3");
		tr3.setLockTime(Instant.now());
		queue.add(tr3);
		
		while(!queue.isEmpty()) {
			System.out.println(queue.poll());
		}
		
		
		BigDecimal principalAmount = new  BigDecimal("9.0");
		List<BigDecimal>  randomizedAmounts = new ArrayList<BigDecimal>();
		randomTransfers(principalAmount,randomizedAmounts,3,1,1);
		randomizedAmounts.forEach(amt->System.out.println(amt.toString()));
		BigDecimal sumAmounts = randomizedAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		assert(principalAmount.compareTo(sumAmounts) ==0);
		
		
		RestTemplate restTemplate = new RestTemplate();
		String addressesURL	  = "http://jobcoin.gemini.com/frozen-backdrop/api/addresses/";
		String adddress = "cz1";
		ResponseEntity<JobCoinAddressInfoResponse> response
		  = restTemplate.getForEntity(addressesURL + adddress, JobCoinAddressInfoResponse.class);
		assert(response.getStatusCode().equals(HttpStatus.OK));
		String postURL = "http://jobcoin.gemini.com/frozen-backdrop/api/transactions";
		URI postURI = new URI(postURL);
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();

	    map.add("fromAddress", "cz1");
	    map.add("toAddress", "t2" );
	    map.add("amount", "3.2" );

	    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
	        map, headers);

	    ResponseEntity<String> postTransactionResponse = restTemplate.postForEntity(postURI, request,
	        String.class);
	    
	    assert(postTransactionResponse.getStatusCode().equals(HttpStatus.OK));
	}
	
	private void randomTransfers(BigDecimal principalAmount, List<BigDecimal>  randomizedAmounts , int numberOfAddresses , int minimumTransfersPerAddress , int maximumTransfersPerAddress) {
		BigDecimal minimumPercent =BigDecimal.ONE.divide(new BigDecimal(minimumTransfersPerAddress*numberOfAddresses) ,8, RoundingMode.HALF_EVEN);
		BigDecimal maximumPercent =BigDecimal.ONE.divide(new BigDecimal(maximumTransfersPerAddress*numberOfAddresses),8, RoundingMode.HALF_EVEN);
		BigDecimal minimumTransferAmount = principalAmount.multiply(minimumPercent);
		BigDecimal maximumTransferAmount = principalAmount.multiply(maximumPercent);
		BigDecimal remainder = principalAmount;
		while(remainder.compareTo(minimumTransferAmount) >0) {
			BigDecimal currentRandomAmount = generateRandomBigDecimalFromRange(minimumTransferAmount,maximumTransferAmount);
			randomizedAmounts.add(currentRandomAmount);
			remainder = remainder.subtract(currentRandomAmount);
			if(maximumTransferAmount.compareTo(remainder) >0) {
				maximumTransferAmount =remainder;
			}
		}
		if(remainder.compareTo(BigDecimal.ZERO) >0) {
			randomizedAmounts.add(remainder);
		}
	}
	
	public static BigDecimal generateRandomBigDecimalFromRange(BigDecimal min, BigDecimal max) {
	    BigDecimal randomBigDecimal = min.add(new BigDecimal(Math.random()).multiply(max.subtract(min)));
	    return randomBigDecimal;
	}
	

}
