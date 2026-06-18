package com.tutorial.camel.demo.route;

import com.tutorial.camel.demo.config.AppConfig;
import com.tutorial.camel.demo.processor.JwtAuthenticationProcessor;
import com.tutorial.camel.demo.processor.ResponseProcessor;
import com.tutorial.camel.demo.service.CryptoService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.spring.security.SpringSecurityAuthorizationPolicy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;


@Component
public class CreateRoute extends RouteBuilder {
    private final ResponseProcessor responseProcessor;
    private final JwtAuthenticationProcessor jwtAuthenticationProcessor;
    private final CryptoService cryptoService;

    public CreateRoute(ResponseProcessor responseProcessor, JwtAuthenticationProcessor jwtAuthenticationProcessor, CryptoService cryptoService) {
        this.responseProcessor = responseProcessor;
        this.jwtAuthenticationProcessor = jwtAuthenticationProcessor;
        this.cryptoService = cryptoService;
    }

    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("ERROR in create-" + AppConfig.ENTITY_NAME + ": ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("""
                         {"error": "${exception.message}", "routeId": "create-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=create-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR, AppConfig.ENTITY_NAME, AppConfig.TIMESTAMP_FORMAT));

        // From client to active mq
        from("direct:create-" + AppConfig.ENTITY_NAME)
                .routeId("proxy-in-create-" + AppConfig.ENTITY_NAME)
                .process(jwtAuthenticationProcessor)
                .process(exchange -> {
                    var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        throw new org.springframework.security.access.AccessDeniedException("Access Denied: Insufficient Route Permissions");
                    }
                })
                .process(exchange -> {
                    String plainJson = exchange.getIn().getBody(String.class);
                    String encryptedJson = cryptoService.encrypt(plainJson);
                    exchange.getIn().setBody(encryptedJson);
                })
                .log(">>> [Proxy IN] Request received. Send to the queue...")
                .to("activemq:queue:product.create?exchangePattern=InOut")
                .log("<<< [Proxy IN] Final response send back to client");

        // From active mq to backend
        from("activemq:queue:product.create")
                .routeId("proxy-out-create-" + AppConfig.ENTITY_NAME)
                .process(exchange -> {
                    String encryptedJson = exchange.getIn().getBody(String.class);
                    String decryptedJson = cryptoService.decrypt(encryptedJson);
                    exchange.getIn().setBody(decryptedJson);
                })
                .log(">>> Triggered: create-" + AppConfig.ENTITY_NAME + " with body: ${body}")

                .setProperty(ResponseProcessor.OP_METHOD, constant("POST"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, constant(AppConfig.BASE_URL + "/product/create"))

                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))

                .to(AppConfig.BASE_URL + "/product/create?bridgeEndpoint=true&throwExceptionOnFailure=false")

                .setProperty("RAW_BACKEND_RESPONSE", body())

                .process(responseProcessor)
                .to("file:%s?fileName=create-%s-res-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))

                .setBody(exchangeProperty("RAW_BACKEND_RESPONSE"))

                .log("<<< [Proxy OUT] Queue treatment successfully finished");

    }
}