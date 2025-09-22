package com.tdtsqlscan.web.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdtsqlscan.web.client.dto.ParseResultDto;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ParserServiceClient {

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String parserServiceUrl;
    // TODO: Replace with dynamic token from User Service
    private final String apiToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzZXJ2aWNlLWFjY291bnQtcGFyc2VyLXNlcnZpY2UiLCJleHAiOjE3MzIwNTQ0MDB9.7sC5n9F3b_D6y_5c_G3e_X9f_2b_Z6d_Y9c_V8e_R0a";

    public ParserServiceClient(ObjectMapper objectMapper, @Value("${client.parser-service.url}") String parserServiceUrl) {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = objectMapper;
        this.parserServiceUrl = parserServiceUrl;
    }

    public ParseResultDto parse(String scriptContent) throws IOException {
        String url = parserServiceUrl + "/api/v1/parse/poc";
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Authorization", "Bearer " + apiToken);
        httpPost.setHeader("Content-Type", "application/json");

        Map<String, String> requestBody = Map.of("script_content", scriptContent);
        String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        httpPost.setEntity(new StringEntity(jsonRequestBody));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new IOException("Failed to call parser service. Status code: " + statusCode);
            }
            String jsonResponse = EntityUtils.toString(response.getEntity());
            return objectMapper.readValue(jsonResponse, ParseResultDto.class);
        }
    }
}
