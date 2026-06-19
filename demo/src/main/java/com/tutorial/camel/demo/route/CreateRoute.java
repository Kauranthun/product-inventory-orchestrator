package com.tutorial.camel.demo.route;

import com.tutorial.camel.demo.config.AppConfig;
import com.tutorial.camel.demo.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.PGPDataFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class CreateRoute extends RouteBuilder {

    private final ResponseProcessor responseProcessor;

    public CreateRoute(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public void configure() {

        PGPDataFormat encryptor = new PGPDataFormat();
        encryptor.setKeyFileName("classpath:keys/partner-pubring.gpg");
        encryptor.setKeyUserid("corentin.loret29@gmail.com");

        PGPDataFormat decryptor = new PGPDataFormat();
        decryptor.setKeyFileName("classpath:keys/private-key.gpg");
        encryptor.setKeyUserid("corentin.loret29@gmail.com");
        decryptor.setPassword("{{camel.pgp.passphrase}}");

        onException(Exception.class)
                .handled(true)
                .log("ERROR in create-" + AppConfig.ENTITY_NAME + ": ${exception.message}")
                .setBody(simple("""
                         {"error": "${exception.message}", "routeId": "create-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=create-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR, AppConfig.ENTITY_NAME, AppConfig.TIMESTAMP_FORMAT));

        // From client to active mq
        from("direct:create-" + AppConfig.ENTITY_NAME)
                .routeId("proxy-in-create-" + AppConfig.ENTITY_NAME)
                .marshal(encryptor)
                .log(">>> [Proxy IN] Request received. Send to the queue...")
                .to("activemq:queue:product.create?exchangePattern=InOut")
                .log("<<< [Proxy IN] Final response send back to client");

        // From active mq to backend
        from("activemq:queue:product.create")
                .routeId("proxy-out-create-" + AppConfig.ENTITY_NAME)

                .unmarshal(decryptor)

                .log(">>> Triggered: create-" + AppConfig.ENTITY_NAME + " with body: ${body}")

                .setProperty(ResponseProcessor.OP_METHOD, constant("POST"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, constant(AppConfig.BASE_URL + "/product/create"))

                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))

                .to(AppConfig.BASE_URL + "/product/create?bridgeEndpoint=true&throwExceptionOnFailure=false")

                .process(responseProcessor)
                .to("file:%s?fileName=create-%s-res-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))

                .log("<<< [Proxy OUT] Queue treatment successfully finished");

    }
}