package org.ssi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CoreGlobalValue {
	
	public static int MAX_SYMBOL_NUM = 1 << 7;
	public static int MAX_OPEN_ORDER_NUM = 1 << 20;
	public static int MAX_LEVEL_NUM = 1 << 15;
	
	@Value("${matching-engine.max-open-order-num}")
    public void setMaxOpenOrderNumber(int maxNum) {
		MAX_OPEN_ORDER_NUM = maxNum;
    }
	
	@Value("${matching-engine.max-symbol-num}")
    public void setMaxSymbolNumber(int maxNum) {
		MAX_SYMBOL_NUM = maxNum;
    }
	
	@Value("${matching-engine.max-level-num}")
    public void setMaxLevelNumber(int maxNum) {
		MAX_LEVEL_NUM = maxNum;
    }
}
