package com.sapfir.helpers;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v130.network.Network;
import org.openqa.selenium.devtools.v130.network.model.Headers;
import org.openqa.selenium.devtools.v130.network.model.RequestId;

import java.util.HashMap;
import java.util.Objects;

public class DevToolsHelpers {

    private String responseBody;
    private final HashMap<String, String> requestHeaders = new HashMap<>();

    public void captureRequestHeaders(DevTools devTools, String requestUrl) {
        devTools.addListener(Network.requestWillBeSent(), request -> {
            if (request.getRequest().getUrl().contains(requestUrl)) {

                // Getting request headers
                Headers headers = request.getRequest().getHeaders();

                // Parsing through request headers to save them in a variable
                for (String key : headers.keySet()) {

                    // Making Chrome NOT headless in headers
                    if (key.equals("sec-ch-ua")) {
                        String newValue = Objects.requireNonNull(headers.get(key)).toString().replace("HeadlessChrome", "Google Chrome");
                        headers.replace(key, newValue);
                    }

                    requestHeaders.put(key, Objects.requireNonNull(headers.get(key)).toString());
                }
            }
        });
    }

    public void captureResponseBody(DevTools devTools, String url) {
        devTools.addListener(Network.responseReceived(), response -> {
            if (response.getResponse().getUrl().contains(url)) {
                RequestId requestId = response.getRequestId();
                this.responseBody = devTools.send(Network.getResponseBody(requestId)).getBody();
            }
        });
    }

    public String getResponseBody() {
        return responseBody;
    }

    public HashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }
}
