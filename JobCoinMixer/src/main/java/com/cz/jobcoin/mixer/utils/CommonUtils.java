package com.cz.jobcoin.mixer.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CommonUtils {
	public static BigDecimal generateRandomBigDecimalFromRange(BigDecimal min, BigDecimal max) {
	    BigDecimal randomBigDecimal = min.add(new BigDecimal(Math.random()).multiply(max.subtract(min))).round(new MathContext(8, RoundingMode.CEILING));
	    return randomBigDecimal;
	}
	
	public static List<BigDecimal> randomizeIntoMultipleAmounts(BigDecimal principalAmount,  int numberOfAddresses , int minimumTransfersPerAddress , int maximumTransfersPerAddress) {
		List<BigDecimal>  randomizedAmounts  = new ArrayList<BigDecimal>();
		BigDecimal minimumPercent =BigDecimal.ONE.divide(new BigDecimal(minimumTransfersPerAddress*numberOfAddresses) ,8, RoundingMode.HALF_EVEN);
		BigDecimal maximumPercent =BigDecimal.ONE.divide(new BigDecimal(maximumTransfersPerAddress*numberOfAddresses),8, RoundingMode.HALF_EVEN);
		BigDecimal minimumTransferAmount = principalAmount.multiply(minimumPercent);
		BigDecimal maximumTransferAmount = principalAmount.multiply(maximumPercent);
		BigDecimal remainder = principalAmount;
		while(remainder.compareTo(minimumTransferAmount) >0) {
			BigDecimal currentRandomAmount = generateRandomBigDecimalFromRange(minimumTransferAmount,maximumTransferAmount);
			randomizedAmounts.add(currentRandomAmount);
			remainder = remainder.subtract(currentRandomAmount);
			if(maximumTransferAmount.compareTo(remainder) >0) {
				maximumTransferAmount =remainder;
			}
		}
		if(remainder.compareTo(BigDecimal.ZERO) >0) {
			randomizedAmounts.add(remainder);
		}
		return randomizedAmounts;
	}
}
