package com.tutorial.crud.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorial.crud.entity.Product;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


public class CommunicationHTTP {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Product> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/product/list"))
                .GET().build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        if(response.statusCode()==200){
            return mapper.readValue(response.body(), new TypeReference<List<Product>>() {});
        }
        else{
            throw new RuntimeException("Error in get : " + response.statusCode());
        }
    }

    public Product getById(String id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/product/detail/"+id))
                .GET().build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        if(response.statusCode()==200){
            return mapper.readValue(response.body(), Product.class);
        }
        else{
            throw new RuntimeException("Error in get : " + response.statusCode());
        }
    }

    public void create(Product product) throws IOException, InterruptedException {
        String jsonBody = mapper.writeValueAsString(product);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/product/create"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        if(response.statusCode()==200){
            System.out.println("Success creation");
        }
        else{
            throw new RuntimeException("Error in create : " + response.statusCode());
        }

    }

    public void update(Product product) throws IOException, InterruptedException {
        String jsonBody = mapper.writeValueAsString(product);
        System.out.println(product.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/product/update/"+product.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        if(response.statusCode()==200){
            System.out.println("Success update");
        }
        else{
            throw new RuntimeException("Error in update : " + response.statusCode());
        }
    }

    public void delete(int id) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/product/delete/"+ id))
                .DELETE().build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        if(response.statusCode()==200){
            System.out.println("Success deletion");
        }
        else{
            throw new RuntimeException("Error in delete : " + response.statusCode());
        }

    }

    public void backupDb() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/db/backup"))
                .POST(HttpRequest.BodyPublishers.noBody()).build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode()==200){
            System.out.println("Success backup");
        }else{

            throw new RuntimeException("Error in backup : " + response.statusCode());
        }
    }

    public void restoreDb() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/db/restore"))
                .POST(HttpRequest.BodyPublishers.noBody()).build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode()==200){
            System.out.println("Success restore");
        }else{
            throw new RuntimeException("Error in restore : " + response.statusCode());
        }
    }
}
