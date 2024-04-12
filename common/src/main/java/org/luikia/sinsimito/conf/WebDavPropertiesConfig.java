package org.luikia.sinsimito.conf;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

@Configuration
public class WebDavPropertiesConfig {

    private Properties properties;

    @Bean("webDavConfig")
    public Properties webDavConfig() {
        return this.properties;
    }

    @Resource
    public void setEnvironment(StandardServletEnvironment environment) {
        this.properties = new Properties();
        Iterator<PropertySource<?>> iterator = environment.getPropertySources().iterator();
        while (iterator.hasNext()) {
            PropertySource<?> source = iterator.next();
            Object o = source.getSource();
            if (o instanceof Map) {
                Map<Object, Object> m = (Map<Object, Object>) o;
                Object enable = m.get("webdav.enable");
                if (!Boolean.valueOf(String.valueOf(enable))) {
                    continue;
                }
                m.entrySet().stream().filter(e ->
                        StringUtils.startsWith(String.valueOf(e.getKey()), "webdav.")
                ).forEach(e ->
                        this.properties.setProperty(
                                StringUtils.removeStart(String.valueOf(e.getKey()), "webdav."),
                                String.valueOf(e.getValue())
                        )
                );
            }
        }


    }
}
