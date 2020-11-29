package com.cz.jobcoin.mixer.services;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cz.jobcoin.mixer.data.dao.DepositWatchDAO;
import com.cz.jobcoin.mixer.data.dao.PendingTransfersDAO;
import com.cz.jobcoin.mixer.data.dao.ReservedAdressesDAO;
import com.cz.jobcoin.mixer.data.model.DepositWatchItem;
import com.cz.jobcoin.mixer.data.model.MixRequest;
import com.cz.jobcoin.mixer.data.model.MixResponse;
import com.cz.jobcoin.mixer.data.model.PendingTransfer;
import com.cz.jobcoin.mixer.utils.CommonUtils;
import com.cz.jobcoin.mixer.utils.GlobalConstants;

@Service
public class MixerService {
	@Autowired
	private DepositWatchDAO depositWatchDAO;
	@Autowired
	private ReservedAdressesDAO reservedAdressesDAO;
	@Autowired
	private JobCoinService jobCoinService;
	@Autowired
	private JobCoinAPIService jobCoinAPIService;
	@Autowired
	private PendingTransfersDAO pendingTransfersDAO;

	@Value("${house.account.address}")
	private String houseAccountAddress;

	@Value("${house.fee.account.address}")
	private String houseFeeAccountAddress;

	@Value("${dole.out.maximum.transfers.per.account}")
	private int maxTransfersPerAccount;

	@Value("${dole.out.minimum.transfers.per.account}")
	private int minTransfersPerAccount;
	
	@Value("${transfer.delay.seconds.min}")
	private int transferDelaySecondsMin;
	
	@Value("${transfer.delay.seconds.max}")
	private int transferDelaySecondsMax;
	
	/**
	 * Process request to mix from API endpoint
	 * Checks if all submitted withdrawal addresses are unused
	 * generates and returns new deposit address and adds it to watch list
	 * @param request
	 * @return
	 */
	public MixResponse processMixRequest(MixRequest request) {
		MixResponse response = new MixResponse();
		response.setErrorCode(GlobalConstants.RESPONSE_OK);
		response = validateMixRequest(request);
		if (response.getErrorCode() == GlobalConstants.RESPONSE_OK) {
			// generate new deposit address;
			String depositAddress = jobCoinService.generateNewDepositAddress();
			// add deposit address to reserved addresses;
			reservedAdressesDAO.addAddress(depositAddress);
			// add withdrawal addresses to reserved addresses
			reservedAdressesDAO.addAll(request.getWithdrawalAddresses());
			// add deposit address to watch list
			depositWatchDAO.addDepositAddress(depositAddress, request.getWithdrawalAddresses());

			response.setDepositAddress(depositAddress);
		}

		return response;
	}

	private MixResponse validateMixRequest(MixRequest request) {
		MixResponse response = new MixResponse();
		response.setErrorCode(GlobalConstants.RESPONSE_OK);
		// check that withdrawal addresses are present
		if (request.getWithdrawalAddresses() == null || request.getWithdrawalAddresses().size() == 0) {
			response.setErrorCode(GlobalConstants.RESPONSE_EMPTY_WITHDRAWAL_LIST);
		}
		if (response.getErrorCode() == GlobalConstants.RESPONSE_OK) {
			// validate that withdrawal addresses are unused
			List<String> addressesInUse = jobCoinService.getAddressesInUse(request.getWithdrawalAddresses());
			if (addressesInUse.size() > 0) {
				response.setErrorCode(GlobalConstants.RESPONSE_WITHDRAWAL_ADDRESS_ALREADY_USED);
				String addressesInUseCommaSeparated = String.join(",", addressesInUse);
				response.setErrorDescription(
						"The following addresses are already in use: " + addressesInUseCommaSeparated);

			}
		}

		return response;
	}
	/**
	 * This async method is called when deposit is detected
	 * supposed to transfer all the funds from deposit account
	 * into big house account 
	 * deducts fee that is transfered into specific fee collection account
	 * generates random list of transfers into withdrawal accounts with randomized time delay
	 * and puts it into pending transfers queue
	 * @param depositWatchItem
	 * @throws URISyntaxException
	 */
	@Async
	public void mixFromDepositAddressAsync(DepositWatchItem depositWatchItem) throws URISyntaxException {
		// read current balance on deposit account and transfer to house account
		BigDecimal balance = jobCoinService.getBalance(depositWatchItem.getAddress());
		ResponseEntity<String> transferResponse = jobCoinAPIService.transfer(depositWatchItem.getAddress(),
				houseAccountAddress, balance);
		if (transferResponse.getStatusCode().equals(HttpStatus.OK)) {
			//remove deposit address from reserved addresses list
			reservedAdressesDAO.removeAddress(depositWatchItem.getAddress());
			// if transfer is successful deduct fee and dole out from house account into
			// withdrawal accounts
			doleOut(balance, depositWatchItem);
		}

	}

	public void doleOut(BigDecimal totalAmout, DepositWatchItem depositWatchItem) {
		// Calculate fee amount

		BigDecimal feeAmount = totalAmout.multiply(GlobalConstants.FEE_COEFFICIENT);
		BigDecimal principalAmount = totalAmout.subtract(feeAmount);

		// generate fee transfer with current locking time ( no delay)
		PendingTransfer feeTransfer = new PendingTransfer(houseAccountAddress, houseFeeAccountAddress, feeAmount,
				Instant.now());

		// get total number of withdrawal accounts and calculate parameters to break
		// into x random amounts
		int numberOfWithdrawalAccouts = depositWatchItem.getWithdrawalAddresses().size();

		List<BigDecimal> randomizedAmounts = CommonUtils.randomizeIntoMultipleAmounts(principalAmount,
				numberOfWithdrawalAccouts, minTransfersPerAccount, maxTransfersPerAccount);
		
		List<PendingTransfer> transferList =  distributeAmongAccountsWithRandomLockTime(randomizedAmounts,depositWatchItem.getWithdrawalAddresses());
		
		//add fee transfer to the list
		transferList.add(feeTransfer);
		
		//add to Pending transfers queue
		pendingTransfersDAO.addAll(transferList);
		

	}
	
	private List<PendingTransfer> distributeAmongAccountsWithRandomLockTime(List<BigDecimal> amounts , List<String> accountAddresses){
		List<PendingTransfer> transfersToReturn = new ArrayList<>(accountAddresses.size()+ 1);
		int currentRoundRobinIndex=0;
		for(BigDecimal currentAmount :amounts) {
			String destinationAccount = accountAddresses.get(currentRoundRobinIndex);
			Instant currentLockTime = generateRandomLockTime();
			PendingTransfer  currentTransfer = new PendingTransfer(houseAccountAddress,destinationAccount,currentAmount,currentLockTime);
			transfersToReturn.add(currentTransfer);
			if(currentRoundRobinIndex ==accountAddresses.size()-1) {
				currentRoundRobinIndex = 0;
			}else {
				currentRoundRobinIndex++;
			}
		}
		return transfersToReturn;
	}
	
	
	private Instant generateRandomLockTime() {
		Random rand = new Random();
		int randomOffsetInSec = rand.nextInt((transferDelaySecondsMax - transferDelaySecondsMin) + 1) + transferDelaySecondsMin;
		Instant generatedTime = Instant.now().plusSeconds(randomOffsetInSec);
		return generatedTime;
	}
}
