package org.luikia.sinsimito.resource;

import io.milton.http.ResourceFactory;
import io.milton.servlet.Initable;

public abstract class AbstractResourceFactory implements ResourceFactory, Initable {


    public abstract String name();

}
