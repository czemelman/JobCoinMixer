package com.cz.jobcoin.mixer.data.model;

import java.math.BigDecimal;

public class JobCoinAddressInfoResponse {
	public BigDecimal balance;
	public JobCoinTransaction[] transactions;
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public JobCoinTransaction[] getTransactions() {
		return transactions;
	}
	public void setTransactions(JobCoinTransaction[] transactions) {
		this.transactions = transactions;
	}
	
}
