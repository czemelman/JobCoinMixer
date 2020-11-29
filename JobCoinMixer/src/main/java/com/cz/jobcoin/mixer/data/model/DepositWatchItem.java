package com.cz.jobcoin.mixer.data.model;

import java.time.Instant;
import java.util.List;

public class DepositWatchItem {
	Instant watchStartTime;
	String address;
	List<String> withdrawalAddresses;

	public Instant getWatchStartTime() {
		return watchStartTime;
	}

	public void setWatchStartTime(Instant watchStartTime) {
		this.watchStartTime = watchStartTime;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<String> getWithdrawalAddresses() {
		return withdrawalAddresses;
	}

	public void setWithdrawalAddresses(List<String> withdrawalAddresses) {
		this.withdrawalAddresses = withdrawalAddresses;
	}

}
