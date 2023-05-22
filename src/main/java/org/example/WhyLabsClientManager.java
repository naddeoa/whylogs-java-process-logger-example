package org.example;

import ai.whylabs.service.api.LogApi;
import ai.whylabs.service.invoker.ApiClient;
import ai.whylabs.service.invoker.Configuration;
import ai.whylabs.service.invoker.auth.ApiKeyAuth;

public class WhyLabsClientManager {
    private final ApiClient defaultClient = Configuration.getDefaultApiClient();
    public final LogApi logApi = new LogApi(defaultClient);

    public WhyLabsClientManager() {
        // TODO disable this path when whylabs isn't configured
        defaultClient.setBasePath("https://api.whylabsapp.com");

        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        apiKeyAuth.setApiKey("KfZIgVH5BC.O4CVvC5ggXkWwEhVzcrFmGUdpLdFKOIRkQInvoOCJ2wtcVWX2VLwL");
    }
}

