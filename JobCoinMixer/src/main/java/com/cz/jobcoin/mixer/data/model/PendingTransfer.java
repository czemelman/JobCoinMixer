package com.cz.jobcoin.mixer.data.model;

import java.math.BigDecimal;
import java.time.Instant;

public class PendingTransfer implements Comparable {
	private String sender;
	private String receiver;
	private BigDecimal amount;
	private Instant lockTime;
	
	public PendingTransfer() {
		
	}
	
	public PendingTransfer(String sender, String receiver, BigDecimal amount, Instant lockTime) {

		this.sender = sender;
		this.receiver = receiver;
		this.amount = amount;
		this.lockTime = lockTime;
	}

	@Override
	public int compareTo(Object o) {
		PendingTransfer compareToTransfer = (PendingTransfer) o;
		return lockTime.compareTo(compareToTransfer.getLockTime());
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public Instant getLockTime() {
		return lockTime;
	}
	public void setLockTime(Instant lockTime) {
		this.lockTime = lockTime;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Lock Time: ");
		sb.append(lockTime);
		sb.append(" From:");
		sb.append(this.sender);
		sb.append(" To:");
		sb.append(this.receiver);
		sb.append(" Amount:");
		sb.append(this.amount);
		return sb.toString();
	}
	
	
}
