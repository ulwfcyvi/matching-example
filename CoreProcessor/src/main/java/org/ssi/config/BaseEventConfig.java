package org.ssi.config;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.properties")
public class BaseEventConfig implements EnvironmentAware {

	public static final String AERON_CHANNEL_START = "aeron:udp?";
	public static final String AERON_CHANNEL_ENPOINT = "endpoint=";

	static Environment environment;

	@Override
	public void setEnvironment(Environment environment) {
		BaseEventConfig.environment = environment;
	}
	
	public static Environment getEnvironment() {
		return environment;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	public static String getBaseEventChannelEnPointIpSub() {
		return environment.getProperty("server.stream.aeron.sub.channel.enpoint.ip");
	}

	public static String getBaseEventChannelEnPointPortSub() {
		return environment.getProperty("server.stream.aeron.sub.channel.enpoint.port");
	}

	public static String getBaseEventChannelSub() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(AERON_CHANNEL_START);
		strBuffer.append(AERON_CHANNEL_ENPOINT);
		strBuffer.append(getBaseEventChannelEnPointIpSub());
		strBuffer.append(":");
		strBuffer.append(getBaseEventChannelEnPointPortSub());
		return strBuffer.toString();
	}

	public static int getBaseEventStreamIdSub() {
		return Integer.parseInt(environment.getProperty("server.stream.aeron.sub.streamid"));
	}

	public static int getBaseEventFragmentLimit() {
		return Integer.parseInt(environment.getProperty("server.stream.aeron.sub.fragment.limit"));
	}
}
