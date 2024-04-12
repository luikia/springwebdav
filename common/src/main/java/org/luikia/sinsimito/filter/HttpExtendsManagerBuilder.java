package org.luikia.sinsimito.filter;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.ResourceFactory;

public class HttpExtendsManagerBuilder extends HttpManagerBuilder {

    private ResourceFactory resourceFactory;


    public HttpExtendsManagerBuilder(ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }
}
