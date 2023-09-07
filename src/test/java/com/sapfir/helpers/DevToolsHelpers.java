package com.sapfir.helpers;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v115.network.Network;

public class DevToolsHelpers {

    public void getRequestHeaders(DevTools devTools, String url) {
        devTools.addListener(Network.requestWillBeSent(), request -> {
            if (request.getRequest().getUrl().contains(url)) {
                // todo: save request headers to a variable, create a public getter to access headers
                System.out.println("Stop here");
            }
        });
    }

    public void getResponseBody(DevTools devTools, String url) {
        devTools.addListener(Network.responseReceived(), response -> {
            if (response.getResponse().getUrl().contains(url)) {
                // todo: implement saving response body to a variable, create a public getter to access it
                System.out.println("Stop here");
            }
        });
    }
}
