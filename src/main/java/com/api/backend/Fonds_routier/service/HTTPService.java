package com.api.backend.Fonds_routier.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPService {

    public void httpRequest(List<String> numeros, String message) throws IOException, InterruptedException {

        Map<Object,Object> body=new HashMap<>();
        body.put("senderId","lmtgroup");
        body.put("message",message);
        body.put("msisdn", numeros);
        body.put("flag","UCS2");
        body.put("maskedMsisdn",false);

        var objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(body);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("X-Api-Key","086D9D04-4E9C-494D-AC11-9D29C94B7D03")
                .header("X-secret","qZX7UoNPOAR0V7obaNafCf89hPaAXNP4aCdvTSLlZsSs")
                .uri(URI.create("https://sms.lmtgroup.com/api/v1/pushes"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Body: " + response.body());

    }
}
