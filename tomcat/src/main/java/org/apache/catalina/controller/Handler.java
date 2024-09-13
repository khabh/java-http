package org.apache.catalina.controller;

import java.util.function.BiConsumer;
import org.apache.coyote.http11.request.HttpMethod;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.response.HttpResponse;

public class Handler {

    private final HttpEndpoint endpoint;
    private final BiConsumer<HttpRequest, HttpResponse> handler;

    private Handler(HttpEndpoint httpEndpoint, BiConsumer<HttpRequest, HttpResponse> handler) {
        this.endpoint = httpEndpoint;
        this.handler = handler;
    }

    public static Handler ofGet(String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        HttpEndpoint endpoint = new HttpEndpoint(path, HttpMethod.GET);
        return new Handler(endpoint, handler);
    }

    public static Handler ofPost(String path, BiConsumer<HttpRequest, HttpResponse> handler) {
        HttpEndpoint endpoint = new HttpEndpoint(path, HttpMethod.POST);
        return new Handler(endpoint, handler);
    }

    public void handle(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (!canHandle(httpRequest)) {
            throw new IllegalArgumentException("요청을 처리할 수 없는 핸들러입니다.");
        }
        handler.accept(httpRequest, httpResponse);
    }

    public boolean canHandle(HttpRequest request) {
        return endpoint.matches(request);
    }

    public HttpEndpoint getEndpoint() {
        return endpoint;
    }
}
