package org.luikia.sinsimito.resource;

import io.milton.http.HttpManager;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import io.milton.servlet.Config;

public class EmptyResourceFactory extends AbstractResourceFactory {

    public static final EmptyResourceFactory INSTANCE = new EmptyResourceFactory();

    private EmptyResourceFactory() {
    }

    @Override
    public String name() {
        return "empty";
    }

    @Override
    public Resource getResource(String host, String path) throws NotAuthorizedException, BadRequestException {
        return null;
    }

    @Override
    public void init(Config config, HttpManager manager) {

    }

    @Override
    public void destroy(HttpManager manager) {

    }
}
