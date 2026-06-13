package com.tutorial.crud.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorial.crud.entity.JSONWebToken;
import com.tutorial.crud.entity.Product;
import com.tutorial.crud.entity.Credentials;
import com.tutorial.crud.entity.RefreshToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


public class CommunicationHTTP {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final JSONWebToken userAct = new JSONWebToken();


    public List<Product> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:8081/camel/product/list"))
                .header("Authorization","Bearer " + userAct.getAccessToken())
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
                .uri(URI.create("https://localhost:8081/camel/product/detail/"+id))
                .header("Authorization","Bearer " + userAct.getAccessToken())
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
                .uri(URI.create("https://localhost:8081/camel/product/create"))
                .header("Content-Type", "application/json")
                .header("Authorization","Bearer " + userAct.getAccessToken())
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
                .uri(URI.create("https://localhost:8081/camel/product/update/"+product.getId()))
                .header("Content-Type", "application/json")
                .header("Authorization","Bearer " + userAct.getAccessToken())
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
                .uri(URI.create("https://localhost:8081/camel/product/delete/"+ id))
                .header("Authorization","Bearer " + userAct.getAccessToken())
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
                .uri(URI.create("http://localhost:8082/db/backup"))
                .header("Authorization","Bearer " + userAct.getAccessToken())
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
                .uri(URI.create("http://localhost:8082/db/restore"))
                .header("Authorization","Bearer " + userAct.getAccessToken())
                .POST(HttpRequest.BodyPublishers.noBody()).build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode()==200){
            System.out.println("Success restore");
        }else{
            throw new RuntimeException("Error in restore : " + response.statusCode());
        }
    }

    public void Signup(Credentials newUser) throws IOException, InterruptedException {
        String jsonBody = mapper.writeValueAsString(newUser);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/auth/signup"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONWebToken tokenObj = mapper.readValue(response.body(), JSONWebToken.class);
            this.userAct.setAccessToken(tokenObj.getAccessToken());
            this.userAct.setRefreshToken(tokenObj.getRefreshToken());
        } else {
            throw new RuntimeException("Connection failed : " + response.statusCode());
        }
    }



    public void login(Credentials credentials) throws IOException, InterruptedException {
        String jsonBody = mapper.writeValueAsString(credentials);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONWebToken tokenObj = mapper.readValue(response.body(), JSONWebToken.class);
            this.userAct.setAccessToken(tokenObj.getAccessToken());
            this.userAct.setRefreshToken(tokenObj.getRefreshToken());

        } else {
            throw new RuntimeException("Connection failed : " + response.statusCode());
        }
    }

    public void refreshTokens() throws IOException, InterruptedException {

        var bodyMap = java.util.Map.of("refresh_token", userAct.getRefreshToken());
        String jsonBody = mapper.writeValueAsString(bodyMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/auth/refresh"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONWebToken tokenObj = mapper.readValue(response.body(), JSONWebToken.class);
            this.userAct.setAccessToken(tokenObj.getAccessToken());
        } else {
            throw new RuntimeException("Refresh failed : " + response.statusCode());
        }
    }

    public void logout() throws IOException, InterruptedException {

        RefreshToken dto = new RefreshToken();
        dto.setRefreshToken(userAct.getRefreshToken());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/auth/logout"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(dto)))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        this.userAct.setAccessToken(null);
        this.userAct.setRefreshToken(null);
    }
}
