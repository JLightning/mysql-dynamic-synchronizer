package com.jhl.mds.services.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class UrlService {

    private final HttpServletRequest request;

    @Autowired
    public UrlService(HttpServletRequest request) {
        this.request = request;
    }

    // TODO: implement
    public String getBaseUrl() {
        return "http://localhost:8080";
    }

    public String getUrl(String uri) {
        return getBaseUrl() + uri;
    }
}
