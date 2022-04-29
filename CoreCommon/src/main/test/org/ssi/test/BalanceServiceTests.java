package org.ssi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ssi.balance.UserBalance;
import org.ssi.model.BaseEvent;
import org.ssi.service.BalanceService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class BalanceServiceTests {

	@Mock
	private BalanceService balanceService;
	@Before
	public void init() {
		balanceService = new BalanceService();
		balanceService.init();
	}
	@Test
	public void testPlaceOrder() {
		UserBalance userBalance = balanceService.getBalance(0);
		assertEquals(userBalance.balance[0], 1000000);
	}

}
