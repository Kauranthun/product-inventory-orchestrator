package com.tutorial.camel.demo.controller;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/camel")
public class CamelTriggerController {
    private static final Logger LOG = LoggerFactory.getLogger(CamelTriggerController.class);
    private final ProducerTemplate producerTemplate;

    public CamelTriggerController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @GetMapping(value = "/product/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> triggerGetAll(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        LOG.info("HTTP trigger received: GET /camel/product/list");

        Map<String, Object> headers = new HashMap<>();
        if (authHeader != null) {
            headers.put("Authorization", authHeader);
        }

        String result = producerTemplate.requestBodyAndHeaders("direct:get-all-product", null, headers, String.class);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/product/detail/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> triggerGetOne(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        LOG.info("HTTP trigger received: GET /camel/product/detail/{}", id);

        Map<String, Object> headers = new HashMap<>();
        headers.put("targetId", id);
        if (authHeader != null) {
            headers.put("Authorization", authHeader);
        }

        String result = producerTemplate.requestBodyAndHeaders("direct:get-one-product", null, headers, String.class);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/product/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> triggerCreate(
            @RequestBody String productJson,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        LOG.info("HTTP trigger received: POST /camel/product/create");

        Map<String, Object> headers = new HashMap<>();
        if (authHeader != null) {
            headers.put("Authorization", authHeader);
        }

        Exchange exchange = producerTemplate.request("direct:create-product", ex -> {
            ex.getIn().setBody(productJson);
            ex.getIn().setHeaders(headers);
        });

        String responseBody = exchange.getMessage().getBody(String.class);

        Integer camelHttpStatus = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

        if (camelHttpStatus != null) {
            return ResponseEntity.status(camelHttpStatus).body(responseBody);
        }

        return ResponseEntity.ok(responseBody);
    }

    @PutMapping(value = "/product/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> triggerUpdate(
            @RequestBody String productJson,
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        LOG.info("HTTP trigger received: PUT /camel/product/update/{}", id);

        Map<String, Object> headers = new HashMap<>();
        headers.put("targetId", id);
        if (authHeader != null) {
            headers.put("Authorization", authHeader);
        }

        Exchange exchange = producerTemplate.request("direct:update-product", ex -> {
            ex.getIn().setBody(productJson);
            ex.getIn().setHeaders(headers);
        });

        String responseBody = exchange.getMessage().getBody(String.class);
        Integer camelHttpStatus = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

        if (camelHttpStatus != null) {
            return ResponseEntity.status(camelHttpStatus).body(responseBody);
        }

        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping(value = "/product/delete/{id}")
    public ResponseEntity<String> triggerDelete(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        LOG.info("HTTP trigger received: DELETE /camel/product/delete/{}", id);

        Map<String, Object> headers = new HashMap<>();
        headers.put("targetId", id);
        if (authHeader != null) {
            headers.put("Authorization", authHeader);
        }

        Exchange exchange = producerTemplate.request("direct:delete-product", ex -> {
            ex.getIn().setBody(null);
            ex.getIn().setHeaders(headers);
        });

        String responseBody = exchange.getMessage().getBody(String.class);
        Integer camelHttpStatus = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

        if (camelHttpStatus != null) {
            return ResponseEntity.status(camelHttpStatus).body(responseBody);
        }

        return ResponseEntity.ok(responseBody);
    }
}