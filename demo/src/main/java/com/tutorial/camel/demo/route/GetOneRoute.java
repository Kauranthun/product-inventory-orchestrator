package com.tutorial.camel.demo.route;

import com.tutorial.camel.demo.config.AppConfig;
import com.tutorial.camel.demo.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;


@Component
public class GetOneRoute extends RouteBuilder {
    private final ResponseProcessor responseProcessor;
    public GetOneRoute(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
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