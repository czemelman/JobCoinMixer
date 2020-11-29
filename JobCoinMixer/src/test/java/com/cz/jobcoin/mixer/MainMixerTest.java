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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cz.jobcoin.mixer.data.model.MixRequest;
import com.cz.jobcoin.mixer.data.model.MixResponse;
import com.cz.jobcoin.mixer.services.JobCoinAPIService;
import com.cz.jobcoin.mixer.services.JobCoinService;
import com.cz.jobcoin.mixer.utils.CommonUtils;
import com.cz.jobcoin.mixer.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
@SpringBootTest
@AutoConfigureMockMvc
class MainMixerTest {
	@Autowired
	private MockMvc mvc;
	@Autowired
	JobCoinAPIService jobCoinAPIService;
	@Autowired
	JobCoinService jobCoinService;
	
	@Value("${transfer.delay.seconds.max}")
	private int transferDelaySecondsMax;
	
	@Test
	void mainTest() throws Exception {
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
		System.out.println("Sending Mix request to use withdrawal accounts :" +withdrawalAddresses.toString() );
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/mixer/api/mix")
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(requestJson)).andReturn();
		int status = mvcResult.getResponse().getStatus();
		assertEquals(HttpStatus.OK.value(), status);
		String content = mvcResult.getResponse().getContentAsString();
		System.out.println("response: " + content);
		MixResponse response = objectMapper.readValue(content, MixResponse.class);
		assertEquals(GlobalConstants.RESPONSE_OK, response.getErrorCode());
		assert (StringUtils.isNotEmpty(response.getDepositAddress()));
		System.out.println("Received Deposit address: " + response.getDepositAddress());
		//transfer random amount between 1 and 2 jobcoin to deposit
		BigDecimal amount = CommonUtils.generateRandomBigDecimalFromRange(new BigDecimal("1"), new BigDecimal("2")).round(new MathContext(4, RoundingMode.CEILING));
		ResponseEntity<String> transferResponse = jobCoinAPIService.transfer(TestUtils.SYSTEM_TEST_ACCOUNT_ADDRESS , response.getDepositAddress(), amount);
		System.out.println(amount.toPlainString() + " sent to deposit address " + response.getDepositAddress());
		assert(transferResponse.getStatusCode().equals(HttpStatus.OK));
		

		BigDecimal expectedFee = amount.multiply(GlobalConstants.FEE_COEFFICIENT);
		BigDecimal expectedTotalAmount = amount.subtract(expectedFee);
		System.out.println("Expected to receive after fee deducttion " + expectedTotalAmount.toPlainString());
		//read total balance  for withdrawal accounts
		BigDecimal totalBalance = jobCoinService.getTotalBalance(withdrawalAddresses);
		long startTime = System.currentTimeMillis();
		//loop thru until  max transfer delay + 5 seconds times out
		//to allow transfers to be launched
		System.out.println("Waiting for all transfers to finalize for  " + transferDelaySecondsMax + " seconds");
		while (!(totalBalance.compareTo(expectedTotalAmount) == 0)
				&& (System.currentTimeMillis() - startTime) < transferDelaySecondsMax *1000 + 5000) {
			Thread.sleep(1000);
			totalBalance = jobCoinService.getTotalBalance(withdrawalAddresses);
		}
		assert (totalBalance.compareTo(expectedTotalAmount) == 0);
		System.out.println("Cumulative balance on all withdrawal accounts " + totalBalance);
	}

}
