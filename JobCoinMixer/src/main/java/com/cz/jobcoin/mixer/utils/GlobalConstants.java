package com.cz.jobcoin.mixer.utils;

import java.math.BigDecimal;

public interface GlobalConstants {
	public static int RESPONSE_OK =0;
	public static int RESPONSE_EMPTY_WITHDRAWAL_LIST = 1000;
	public static int RESPONSE_WITHDRAWAL_ADDRESS_ALREADY_USED = 1100;
	
	public static BigDecimal FEE_COEFFICIENT = new BigDecimal("0.05"); //5%
}
