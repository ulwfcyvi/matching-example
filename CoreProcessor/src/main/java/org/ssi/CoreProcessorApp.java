package org.ssi;

import org.agrona.concurrent.ShutdownSignalBarrier;
import org.ssi.config.BaseEventConfig;
import org.ssi.core.BaseEventProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = {"org.ssi"})
@PropertySource("application.properties")
public class CoreProcessorApp implements CommandLineRunner {

	@Autowired
	private BaseEventConfig baseEventConfig;

	@Autowired
	private BaseEventProcessor baseEventProcessor;

	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(CoreProcessorApp.class, args);
		// shutdown hook doesn't work on windows so we have to handle shutdown differently
		
		if (OS.contains("win")) {
			new ShutdownSignalBarrier().await();
			int exitCode = SpringApplication.exit(ctx, new ExitCodeGenerator() {
				@Override
				public int getExitCode() {
					// no errors
					return 0;
				}
			});
			System.exit(exitCode);
		}
	}

	@Override
	public void run(String... args) throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					baseEventProcessor.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		try {
			// disruptorProcessor.runTest();
			baseEventProcessor.start();			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// disruptorProcessor.stop();
		}
	}

}