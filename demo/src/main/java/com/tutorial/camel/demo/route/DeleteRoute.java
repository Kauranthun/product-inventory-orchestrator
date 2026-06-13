package com.tutorial.camel.demo.route;

import com.tutorial.camel.demo.config.AppConfig;
import com.tutorial.camel.demo.processor.JwtAuthenticationProcessor;
import com.tutorial.camel.demo.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.spring.security.SpringSecurityAuthorizationPolicy;
import org.springframework.stereotype.Component;


@Component
public class DeleteRoute extends RouteBuilder {
    private final ResponseProcessor responseProcessor;
    private final JwtAuthenticationProcessor jwtAuthenticationProcessor;

    public DeleteRoute(ResponseProcessor responseProcessor, JwtAuthenticationProcessor jwtAuthenticationProcessor) {
        this.responseProcessor = responseProcessor;
        this.jwtAuthenticationProcessor = jwtAuthenticationProcessor;
    }

    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("ERROR in delete-" + AppConfig.ENTITY_NAME + ": ${exception.message}")
                .setBody(simple("""
                         {"error": "${exception.message}", "routeId": "delete-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=delete-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR, AppConfig.ENTITY_NAME, AppConfig.TIMESTAMP_FORMAT));

        from("direct:delete-"+AppConfig.ENTITY_NAME)
                .routeId("proxy-in-delete-"+AppConfig.ENTITY_NAME)
                .process(jwtAuthenticationProcessor)
                .process(exchange -> {
                    var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        throw new org.springframework.security.access.AccessDeniedException("Access Denied: Insufficient Route Permissions");
                    }
                })
                .log(">>> [Proxy IN] Request received. Send to the queue...")
                .to("activemq:queue:product.delete?exchangePattern=InOut")
                .log("<<< [Proxy IN] Final response send back to client");

        from("activemq:queue:product.delete")
                .routeId("proxy-out-delete-" + AppConfig.ENTITY_NAME)
                .log(">>> Triggered: delete-" + AppConfig.ENTITY_NAME + " (id=${header.targetId})")

                .setProperty(ResponseProcessor.OP_METHOD, constant("DELETE"))
                .setProperty(ResponseProcessor.OP_ENDPOINT,
                        simple(AppConfig.BASE_URL + "/product/delete" + "/${header.targetId}"))

                .setHeader(Exchange.HTTP_METHOD, constant("DELETE"))
                .setHeader(Exchange.HTTP_PATH, simple("/${header.targetId}"))

                .to(AppConfig.BASE_URL + "/product/delete" + "?bridgeEndpoint=true&throwExceptionOnFailure=false")

                .setProperty("RAW_BACKEND_RESPONSE", body())

                .process(responseProcessor)
                .to("file:%s?fileName=delete-%s-id${header.targetId}-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))

                .setBody(exchangeProperty("RAW_BACKEND_RESPONSE"))

                .log("<<< [Proxy OUT] Queue treatment successfully finished");

    }
}
