package org.bedrock.common.auth.config;

import org.bedrock.common.auth.props.JwtProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

}
