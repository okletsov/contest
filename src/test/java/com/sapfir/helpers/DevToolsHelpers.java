package com.sapfir.helpers;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v115.network.Network;
import org.openqa.selenium.devtools.v115.network.model.RequestId;

public class DevToolsHelpers {

    private String responseBody;

    public void getRequestHeaders(DevTools devTools, String url) {
        devTools.addListener(Network.requestWillBeSent(), request -> {
            if (request.getRequest().getUrl().contains(url)) {
                // todo: save request headers to a variable, create a public getter to access headers
                System.out.println("Stop here");
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
}
