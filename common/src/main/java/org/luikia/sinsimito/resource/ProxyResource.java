package org.luikia.sinsimito.resource;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.GetableResource;
import io.milton.resource.Resource;
import io.milton.servlet.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ProxyResource extends AbstractBaseResource implements CollectionResource {
    private final String name;
    private final List<ProxyResource> children = new ArrayList<>();

    public ProxyResource(String name, Collection<String> children, Config config) {
        super(config);
        this.name = name;
        children.stream().map(e -> new ProxyResource(e, Collections.emptyList(), config)).forEach(this.children::add);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return ChildUtils.child(childName, this.getChildren());
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        return this.children;
    }

    @Override
    protected Class<? extends CollectionResource> directoryResourceClass() {
        return ProxyResource.class;
    }

    @Override
    protected Class<? extends GetableResource> fileResourceClass() {
        return null;
    }
}
