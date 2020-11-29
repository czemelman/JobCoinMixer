package com.cz.jobcoin.mixer.services;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cz.jobcoin.mixer.data.model.JobCoinAddressInfoResponse;

/**
 * 
 * @author Constantin Zemelman
 * JobCoinService will interact with job coin APIs
 */
@Service
public class JobCoinAPIService {
	private static String JOB_COIN_API_URI = "http://jobcoin.gemini.com/frozen-backdrop/api/";
	private static String ADDRESSES_URI_PATH = "addresses/";
	private static String TRANSACTIONS_URI_PATH = "transactions";
	private static String ADDRESSES_URI = JOB_COIN_API_URI + ADDRESSES_URI_PATH;
	private static String TRANSACTIONS_URI = JOB_COIN_API_URI + TRANSACTIONS_URI_PATH;
	/**
	 * returns balance and list of transactions for a given address
	 * @param address - job coin address to inquire
	 * @return HTTP Response entity 
	 */
	public ResponseEntity<JobCoinAddressInfoResponse> getAddressInfo(String address){
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<JobCoinAddressInfoResponse> response
		  = restTemplate.getForEntity(ADDRESSES_URI + address, JobCoinAddressInfoResponse.class);
		return response;
	}
	
	/**
	 * performs job token transfer between from and to addresses
	 * @param fromAddress - sender address
	 * @param toAddress - receiver address
	 * @param amount - amount to be sent
	 * @return
	 * @throws URISyntaxException 
	 */
	public ResponseEntity<String> transfer(String fromAddress , String toAddress, BigDecimal amount) throws URISyntaxException {
		RestTemplate restTemplate = new RestTemplate();
		URI postURI = new URI(TRANSACTIONS_URI);
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();

	    map.add("fromAddress", fromAddress);
	    map.add("toAddress", toAddress );
	    map.add("amount", amount.toPlainString() );

	    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
	        map, headers);

	    ResponseEntity<String> transferResponse = restTemplate.postForEntity(postURI, request,
	        String.class);
	    return transferResponse;
	}
}