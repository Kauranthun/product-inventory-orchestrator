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
public class UpdateRoute extends RouteBuilder{

    private final ResponseProcessor responseProcessor;
    private final JwtAuthenticationProcessor jwtAuthenticationProcessor;
    private final CryptoService cryptoService;

    public UpdateRoute(ResponseProcessor responseProcessor, JwtAuthenticationProcessor jwtAuthenticationProcessor, CryptoService cryptoService){
        this.responseProcessor=responseProcessor;
        this.jwtAuthenticationProcessor=jwtAuthenticationProcessor;
        this.cryptoService = cryptoService;
    }

    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("ERROR in update-" + AppConfig.ENTITY_NAME + ": ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("""
                         {"error": "${exception.message}", "routeId": "update-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=update-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR, AppConfig.ENTITY_NAME, AppConfig.TIMESTAMP_FORMAT));

        from("direct:update-"+AppConfig.ENTITY_NAME)
                .routeId("proxy-in-update-"+AppConfig.ENTITY_NAME)
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
                .to("activemq:queue:product.update?exchangePattern=InOut")
                .log("<<< [Proxy IN] Final response send back to client");
        from("activemq:queue:product.update")
                .routeId("proxy-out-update-" + AppConfig.ENTITY_NAME)
                .process(exchange -> {
                    String encryptedJson = exchange.getIn().getBody(String.class);
                    String decryptedJson = cryptoService.decrypt(encryptedJson);
                    exchange.getIn().setBody(decryptedJson);
                })
                .log(">>> Triggered: update-" + AppConfig.ENTITY_NAME + " (id=${header.targetId})" + " with body: ${body}")

                .setProperty(ResponseProcessor.OP_METHOD, constant("PUT"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, simple(AppConfig.BASE_URL + "/product/update" +"/${header.targetId}"))

                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .setHeader(Exchange.HTTP_PATH, simple("/${header.targetId}"))

                .to(AppConfig.BASE_URL + "/product/update?bridgeEndpoint=true&throwExceptionOnFailure=false")

                .setProperty("RAW_BACKEND_RESPONSE", body())

                .process(responseProcessor)
                .to("file:%s?fileName=update-%s-id${header.targetId}-res-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))

                .setBody(exchangeProperty("RAW_BACKEND_RESPONSE"))

                .log("<<< [Proxy OUT] Queue treatment successfully finished");

    }

}
