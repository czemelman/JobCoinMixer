package com.cz.jobcoin.mixer.data.model;

import java.util.List;

public class MixRequest {
	private List<String> withdrawalAddresses;

	public List<String> getWithdrawalAddresses() {
		return withdrawalAddresses;
	}

	public void setWithdrawalAddresses(List<String> withdrawalAddresses) {
		this.withdrawalAddresses = withdrawalAddresses;
	}
	
}
