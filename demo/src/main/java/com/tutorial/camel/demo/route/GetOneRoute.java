package com.tutorial.camel.demo.route;

import com.tutorial.camel.demo.config.AppConfig;
import com.tutorial.camel.demo.processor.JwtAuthenticationProcessor;
import com.tutorial.camel.demo.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.spring.security.SpringSecurityAuthorizationPolicy;
import org.springframework.stereotype.Component;

import javax.swing.*;


@Component
public class GetOneRoute extends RouteBuilder {
    private final ResponseProcessor responseProcessor;
    private final JwtAuthenticationProcessor jwtAuthenticationProcessor;

    public GetOneRoute(ResponseProcessor responseProcessor, JwtAuthenticationProcessor jwtAuthenticationProcessor) {
        this.responseProcessor = responseProcessor;
        this.jwtAuthenticationProcessor=jwtAuthenticationProcessor;
    }

    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("ERROR in get-id-" + AppConfig.ENTITY_NAME + ": ${exception.message}")
                .setBody(simple("""
                         {"error": "${exception.message}", "routeId": "get-id-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=get-id-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR, AppConfig.ENTITY_NAME, AppConfig.TIMESTAMP_FORMAT));

        from("direct:get-one-"+AppConfig.ENTITY_NAME)
                .routeId("proxy-in-get-one-"+AppConfig.ENTITY_NAME)
                .process(jwtAuthenticationProcessor)
                .process(exchange -> {
                    var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

                    if (auth == null) {
                        throw new org.springframework.security.access.AccessDeniedException("Access Denied: Anonymous access not allowed");
                    }

                    boolean hasAccess = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_USER") || a.getAuthority().equals("ROLE_ADMIN"));

                    if (!hasAccess) {
                        throw new org.springframework.security.access.AccessDeniedException("Access Denied: Insufficient Route Permissions");
                    }
                })
                .log(">>> [Proxy IN] Request received. Send to the queue...")
                .to("activemq:queue:product.get.id?exchangePattern=InOut")
                .log("<<< [Proxy IN] Final response send back to client");

        from("activemq:queue:product.get.id")
                .routeId("proxy-out-get-one-" + AppConfig.ENTITY_NAME)
                .log(">>> Triggered: get-one-" + AppConfig.ENTITY_NAME + " (id=${header.targetId})")

                .setProperty(ResponseProcessor.OP_METHOD, constant("GET"))
                .setProperty(ResponseProcessor.OP_ENDPOINT,
                        simple(AppConfig.BASE_URL + "/product/detail" + "/${header.targetId}"))

                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader(Exchange.HTTP_PATH, simple("/${header.targetId}"))

                .to(AppConfig.BASE_URL + "/product/detail" + "?bridgeEndpoint=true&throwExceptionOnFailure=false")

                .process(responseProcessor)
                .to("file:%s?fileName=get-one-%s-id${header.targetId}-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))

                .log("<<< [Proxy OUT] Queue treatment successfully finished");

    }
}