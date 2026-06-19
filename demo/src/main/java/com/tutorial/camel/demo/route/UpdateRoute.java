package com.tutorial.camel.demo.route;

import com.tutorial.camel.demo.config.AppConfig;
import com.tutorial.camel.demo.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.PGPDataFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class UpdateRoute extends RouteBuilder{

    private final ResponseProcessor responseProcessor;

    public UpdateRoute(ResponseProcessor responseProcessor){
        this.responseProcessor=responseProcessor;
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
                .log("ERROR in update-" + AppConfig.ENTITY_NAME + ": ${exception.message}")
                .setBody(simple("""
                         {"error": "${exception.message}", "routeId": "update-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=update-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR, AppConfig.ENTITY_NAME, AppConfig.TIMESTAMP_FORMAT));

        from("direct:update-"+AppConfig.ENTITY_NAME)
                .routeId("proxy-in-update-"+AppConfig.ENTITY_NAME)
                .marshal(encryptor)
                .log(">>> [Proxy IN] Request received. Send to the queue...")
                .to("activemq:queue:product.update?exchangePattern=InOut")
                .log("<<< [Proxy IN] Final response send back to client");
        from("activemq:queue:product.update")
                .routeId("proxy-out-update-" + AppConfig.ENTITY_NAME)
                .unmarshal(decryptor)
                .log(">>> Triggered: update-" + AppConfig.ENTITY_NAME + " (id=${header.targetId})" + " with body: ${body}")

                .setProperty(ResponseProcessor.OP_METHOD, constant("PUT"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, simple(AppConfig.BASE_URL + "/product/update" +"/${header.targetId}"))

                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .setHeader(Exchange.HTTP_PATH, simple("/${header.targetId}"))

                .to(AppConfig.BASE_URL + "/product/update?bridgeEndpoint=true&throwExceptionOnFailure=false")

                .process(responseProcessor)
                .to("file:%s?fileName=update-%s-id${header.targetId}-res-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))

                .log("<<< [Proxy OUT] Queue treatment successfully finished");

    }

}
