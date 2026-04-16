package com.paybridge.recon.integration.paybridge;

import com.paybridge.recon.support.http.CorrelationIdSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

@Configuration
public class PayBridgeClientConfiguration {

    @Bean
    RestClient payBridgeRestClient(RestClient.Builder builder, PayBridgeClientProperties properties) {
        Assert.hasText(properties.getBaseUrl(), "paybridge-recon.integration.paybridge.base-url must be configured");
        Assert.hasText(properties.getOperatorUsername(), "paybridge-recon.integration.paybridge.operator-username must be configured");
        Assert.hasText(properties.getOperatorPassword(), "paybridge-recon.integration.paybridge.operator-password must be configured");

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.getConnectTimeout().toMillis());
        requestFactory.setReadTimeout((int) properties.getReadTimeout().toMillis());

        return builder
            .baseUrl(properties.getBaseUrl())
            .requestFactory(requestFactory)
            .requestInterceptor((request, body, execution) -> {
                request.getHeaders().setBasicAuth(
                    properties.getOperatorUsername(),
                    properties.getOperatorPassword()
                );
                String correlationId = CorrelationIdSupport.current();
                if (correlationId != null && !correlationId.isBlank()) {
                    request.getHeaders().set(CorrelationIdSupport.HEADER_NAME, correlationId);
                }
                return execution.execute(request, body);
            })
            .build();
    }
}
