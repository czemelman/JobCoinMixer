package com.cz.jobcoin.mixer.data.model;

import java.math.BigDecimal;
import java.util.Date;

public class JobCoinTransaction {
	    public Date timestamp;
	    public String fromAddress;
	    public String toAddress;
	    public BigDecimal amount;
		public Date getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}
		public String getFromAddress() {
			return fromAddress;
		}
		public void setFromAddress(String fromAddress) {
			this.fromAddress = fromAddress;
		}
		public String getToAddress() {
			return toAddress;
		}
		public void setToAddress(String toAddress) {
			this.toAddress = toAddress;
		}
		public BigDecimal getAmount() {
			return amount;
		}
		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}
	    
}
