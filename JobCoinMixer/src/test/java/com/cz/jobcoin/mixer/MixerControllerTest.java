package com.cz.jobcoin.mixer;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.cz.jobcoin.mixer.controller.MixerController;
import com.cz.jobcoin.mixer.data.model.MixRequest;
import com.cz.jobcoin.mixer.data.model.MixResponse;
import com.cz.jobcoin.mixer.services.JobCoinAPIService;
import com.cz.jobcoin.mixer.services.JobCoinService;
import com.cz.jobcoin.mixer.utils.CommonUtils;
import com.cz.jobcoin.mixer.utils.GlobalConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class MixerControllerTest {
	@Autowired
	private MockMvc mvc;

	@Autowired
	JobCoinAPIService jobCoinAPIService;
	@Autowired
	JobCoinService jobCoinService;
	
	@Value("${transfer.delay.seconds.max}")
	private int transferDelaySecondsMax;
	
	@Test
	void testMixRequestSuccess() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		MixRequest request = new MixRequest();
		int numberOfWithdrawalAddresses = 3;
		List<String> withdrawalAddresses = new ArrayList<String>();
		for (int i = 0; i < numberOfWithdrawalAddresses; i++) {
			String newAddress = RandomStringUtils.random(40, true, true);
			withdrawalAddresses.add(newAddress);
		}
		request.setWithdrawalAddresses(withdrawalAddresses);
		String requestJson = objectMapper.writeValueAsString(request);
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/mixer/api/mix")
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(requestJson)).andReturn();
		int status = mvcResult.getResponse().getStatus();
		assertEquals(HttpStatus.OK.value(), status);
		String content = mvcResult.getResponse().getContentAsString();
		System.out.println("response: " + content);
		MixResponse response = objectMapper.readValue(content, MixResponse.class);
		assertEquals(GlobalConstants.RESPONSE_OK, response.getErrorCode());
		assert (StringUtils.isNotEmpty(response.getDepositAddress()));
	}

	@Test
	void testMixRequestNoWithdrawalList() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		MixRequest request = new MixRequest();
		String requestJson = objectMapper.writeValueAsString(request);
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/mixer/api/mix")
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(requestJson)).andReturn();
		int status = mvcResult.getResponse().getStatus();
		assertEquals(HttpStatus.OK.value(), status);
		String content = mvcResult.getResponse().getContentAsString();
		System.out.println("response: " + content);
		MixResponse response = objectMapper.readValue(content, MixResponse.class);
		assertEquals(GlobalConstants.RESPONSE_EMPTY_WITHDRAWAL_LIST, response.getErrorCode());
		assert (StringUtils.isEmpty(response.getDepositAddress()));
	}

	@Test
	void testMixRequestDuplicateWithdrawals() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		MixRequest request = new MixRequest();
		int numberOfWithdrawalAddresses = 3;
		List<String> withdrawalAddresses = new ArrayList<String>();
		for (int i = 0; i < numberOfWithdrawalAddresses; i++) {
			String newAddress = RandomStringUtils.random(40, true, true);
			withdrawalAddresses.add(newAddress);
		}
		request.setWithdrawalAddresses(withdrawalAddresses);
		String requestJson = objectMapper.writeValueAsString(request);
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/mixer/api/mix")
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(requestJson)).andReturn();
		int status = mvcResult.getResponse().getStatus();
		assertEquals(HttpStatus.OK.value(), status);
		String content = mvcResult.getResponse().getContentAsString();
		System.out.println("response: " + content);
		MixResponse response = objectMapper.readValue(content, MixResponse.class);
		assertEquals(GlobalConstants.RESPONSE_OK, response.getErrorCode());
		assert (StringUtils.isNotEmpty(response.getDepositAddress()));

		// request with duplicate withdrawal addresses
		String additionalAddress = RandomStringUtils.random(40, true, true);
		withdrawalAddresses.add(additionalAddress);

		request.setWithdrawalAddresses(withdrawalAddresses);
		requestJson = objectMapper.writeValueAsString(request);
		mvcResult = mvc.perform(MockMvcRequestBuilders.post("/mixer/api/mix")
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(requestJson)).andReturn();
		status = mvcResult.getResponse().getStatus();
		assertEquals(HttpStatus.OK.value(), status);
		content = mvcResult.getResponse().getContentAsString();
		System.out.println("response: " + content);
		response = objectMapper.readValue(content, MixResponse.class);
		assertEquals(GlobalConstants.RESPONSE_WITHDRAWAL_ADDRESS_ALREADY_USED, response.getErrorCode());
		
	}


}
