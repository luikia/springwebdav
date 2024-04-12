package org.luikia.sinsimito.resource;

import io.milton.servlet.Config;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ProxyNameSpaceConfig extends Config {

    private final Config baseConfig;

    private final Map<String, String> config = new HashMap<>();

    public ProxyNameSpaceConfig(String name, Config baseConfig) {
        this.baseConfig = baseConfig;
        String prefix = String.format("%s.", name);
        Map<String, String> nsConfig = new HashMap<>();
        baseConfig.getInitParameterNames().forEach(e -> {
            String value = baseConfig.getInitParameter(e);
            this.config.put(e, value);
            if (StringUtils.startsWith(e, prefix)) {
                nsConfig.put(StringUtils.removeStart(e, prefix), value);
            }
        });
        this.config.putAll(nsConfig);

    }

    @Override
    public ServletContext getServletContext() {
        return this.baseConfig.getServletContext();
    }

    @Override
    public String getInitParameter(String string) {
        return this.config.get(string);
    }

    @Override
    protected Enumeration initParamNames() {
        return Collections.enumeration(this.config.keySet());
    }

    public void addConfig(String key, String value) {
        this.config.put(key, value);
    }

    public void addConfigs(Map<String, String> configMap) {
        this.config.putAll(configMap);
    }
}
