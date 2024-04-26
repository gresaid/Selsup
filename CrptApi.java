package com.artem.crudchad.config;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

public class CrptApi {
  private final HttpClient httpClient;
  private final RateLimiter rateLimiter;

  /**
   * Constructs a new CrptApi instance.
   * @param timeUnit the time unit for defining the rate limit
   * @param requestLimit maximum number of requests allowed within the specified time unit
   */
  public CrptApi(TimeUnit timeUnit, int requestLimit) {
    if (timeUnit == null || requestLimit <= 0 ) {
      throw new IllegalArgumentException();
    }
    int permitsPerSecond = requestLimit / (int) timeUnit.toSeconds(1);
    this.httpClient = HttpClient.newHttpClient();
    this.rateLimiter = RateLimiter.create(permitsPerSecond);
  }


  /**
   * Creates a document by sending a POST request to the CRPT API.
   * @param documentJson JSON representation of the document to be created
   * @param signature the signature of the document
   */
  public void createDocument(String documentJson, String signature) {
    try {
      if (rateLimiter.tryAcquire()) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(documentJson)))
            .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
            HttpResponse.BodyHandlers.ofString());
        System.out.println(httpResponse.body());
      } else {
        System.out.println("Limit exceeded");
      }
    } catch (InterruptedException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 10);
    String documentJson = "{\n"
        + "  \"description\": {\n"
        + "    \"participantInn\": \"string\"\n"
        + "  },\n"
        + "  \"doc_id\": \"string\",\n"
        + "  \"doc_status\": \"string\",\n"
        + "  \"doc_type\": \"LP_INTRODUCE_GOODS\",\n"
        + "  \"importRequest\": true,\n"
        + "  \"owner_inn\": \"string\",\n"
        + "  \"participant_inn\": \"string\",\n"
        + "  \"producer_inn\": \"string\",\n"
        + "  \"production_date\": \"2020-01-23\",\n"
        + "  \"production_type\": \"string\",\n"
        + "  \"products\": [\n"
        + "    {\n"
        + "      \"certificate_document\": \"string\",\n"
        + "      \"certificate_document_date\": \"2020-01-23\",\n"
        + "      \"certificate_document_number\": \"string\",\n"
        + "      \"owner_inn\": \"string\",\n"
        + "      \"producer_inn\": \"string\",\n"
        + "      \"production_date\": \"2020-01-23\",\n"
        + "      \"tnved_code\": \"string\",\n"
        + "      \"uit_code\": \"string\",\n"
        + "      \"uitu_code\": \"string\"\n"
        + "    }\n"
        + "  ],\n"
        + "  \"reg_date\": \"2020-01-23\",\n"
        + "  \"reg_number\": \"string\"\n"
        + "}";
        String signature = "smth sig";
        crptApi.createDocument(documentJson, signature);
  }
}
