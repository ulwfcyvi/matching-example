package org.ssi.model;

import org.openjdk.jol.info.ClassLayout;

public class MatchingResult {
	public long p001, p002,p003,p004=1;
	public int p005=2;
	public byte type;
	public byte makerTradeOption;
	public byte makerOrderType;

	public long price;
	public long amount;
	public long timestamp;

	public long makerStopPrice;
	public long makerId;
	public long makerOrderId;
	public long makerAmount;
	public long makerFilled;
	public double makerFilledCost;

	public MatchingResult next;

	@Override
	public String toString() {
		return new StringBuffer("MatchingResult [type=").append(type).append(", price=").append(price).append(", stopPrice=").append(makerStopPrice).append(", orderType="
				+ makerOrderType).append(", amount=").append(amount).append(", timestamp=").append(timestamp).append(", makerId=").append(makerId
				+ ", makerOrderId=").append(makerOrderId).append(", makerAmount=").append(makerAmount).append(", makerFilled=").append(makerFilled
				+ ", makerFilledCost=").append(makerFilledCost).append(", next=").append(next).append("]").toString();
	}

	public static void main(String args[]){
		System.out.println(ClassLayout.parseClass(MatchingResult.class).toPrintable());
	}


}
