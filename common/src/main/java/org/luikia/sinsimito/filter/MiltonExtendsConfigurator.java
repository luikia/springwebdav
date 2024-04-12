package org.luikia.sinsimito.filter;

import io.milton.http.ResourceFactory;
import io.milton.servlet.DefaultMiltonConfigurator;

public class MiltonExtendsConfigurator extends DefaultMiltonConfigurator {

    private ResourceFactory resourceFactory;

    public MiltonExtendsConfigurator(ResourceFactory resourceFactory) {
        super();
        this.resourceFactory = resourceFactory;
        this.builder = new HttpExtendsManagerBuilder(resourceFactory);
    }

    @Override
    protected void build() {
        this.builder.setMainResourceFactory(this.resourceFactory);
        super.build();
    }
}
