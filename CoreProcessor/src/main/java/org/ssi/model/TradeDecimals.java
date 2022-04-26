package org.ssi.model;

public class TradeDecimals {
	public int baseDec;
	public int counterDec;
	public int priceDec;
	
	public boolean equals(Object o) {
		
		if(o == this) {
			return true;
		}
		
		if(!(o instanceof TradeDecimals)) {
			return false;
		}
		
		TradeDecimals td = (TradeDecimals)o;		
		
		return 	baseDec == td.baseDec
				&& counterDec == td.counterDec
				&& priceDec == td.priceDec;
	}
}
