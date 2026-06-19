package com.tutorial.camel.demo.route;

import com.tutorial.camel.demo.config.AppConfig;
import com.tutorial.camel.demo.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class GetAllRoute extends RouteBuilder {
    private final ResponseProcessor responseProcessor;

    public GetAllRoute(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("ERROR in get-all-" + AppConfig.ENTITY_NAME + ": ${exception.message}")
                .setBody(simple("""
                         {"error": "${exception.message}", "routeId": "get-all-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=get-all-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR, AppConfig.ENTITY_NAME, AppConfig.TIMESTAMP_FORMAT));

        // From client to active mq
        from("direct:get-all-"+AppConfig.ENTITY_NAME)
                .routeId("proxy-in-get-all-"+AppConfig.ENTITY_NAME)
                .log(">>> [Proxy IN] Request received. Send to the queue...")
                .to("activemq:queue:product.get.all?exchangePattern=InOut")
                .log("<<< [Proxy IN] Final response send back to client");

        // From active mq to backend
        from("activemq:queue:product.get.all")
                .routeId("proxy-out-get-all-" + AppConfig.ENTITY_NAME)
                .log(">>> Triggered: get-all-" + AppConfig.ENTITY_NAME)

                .setProperty(ResponseProcessor.OP_METHOD, constant("GET"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, simple(AppConfig.BASE_URL + "/product/list"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .to(AppConfig.BASE_URL +"/product/list"+ "?bridgeEndpoint=true&throwExceptionOnFailure=false")

                .process(responseProcessor)
                .to("file:%s?fileName=get-all-%s-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))

                .log("<<< [Proxy OUT] Queue treatment successfully finished");
    }
}

