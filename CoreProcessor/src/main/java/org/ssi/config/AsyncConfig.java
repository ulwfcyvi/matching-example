package org.ssi.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
	@Bean(name = "commonAsync")
	public Executor commonAsync() {
		Executor executor = Executors.newWorkStealingPool();
		return executor;
	}
}
