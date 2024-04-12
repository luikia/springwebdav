package org.luikia.sinsimito.conf;

import org.luikia.sinsimito.filter.MiltonExtendsFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class WebDavFilterConfig {

    private Properties webDavConfig;


    private MiltonExtendsFilter miltonExtendsFilter;

    @Bean
    public FilterRegistrationBean registerFilter() {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        String webDavPath = this.webDavConfig.getProperty("path", "");
        bean.addUrlPatterns(String.format("%s/*", webDavPath));
        Map<String, String> filterConfig = new HashMap<>();
        this.webDavConfig.stringPropertyNames().forEach(e -> filterConfig.put(e, this.webDavConfig.getProperty(e)));
        bean.setFilter(miltonExtendsFilter);
        bean.setName("webdav");
        bean.setInitParameters(filterConfig);
        return bean;
    }

    @Resource(name = "webDavConfig")
    public void setWebDavConfig(Properties webDavConfig) {
        this.webDavConfig = webDavConfig;
    }

    @Resource
    public void setMiltonExtendsFilter(MiltonExtendsFilter miltonExtendsFilter) {
        this.miltonExtendsFilter = miltonExtendsFilter;
    }
}
