package org.luikia.sinsimito.filter;

import io.milton.http.ResourceFactory;
import io.milton.servlet.FilterConfigWrapper;
import io.milton.servlet.MiltonFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

@Slf4j
@Component
public class MiltonExtendsFilter extends MiltonFilter {

    private ResourceFactory resourceFactory;

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        this.configurator = new MiltonExtendsConfigurator(this.resourceFactory);
        FilterConfigWrapper configWrapper = new FilterConfigWrapper(config);
        this.httpManager = this.configurator.configure(configWrapper);
    }

    @Resource
    public void setResourceFactory(ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }
}
