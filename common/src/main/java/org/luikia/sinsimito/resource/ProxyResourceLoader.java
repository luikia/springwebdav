package org.luikia.sinsimito.resource;

import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.servlet.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.luikia.sinsimito.plugin.PluginClassLoader;

import javax.servlet.ServletException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class ProxyResourceLoader {

    private final Map<String, String> resourceNameMap = new HashMap<>();
    private final Config config;

    private final HttpManager manager;

    private final String basePluginPath;

    public ProxyResourceLoader(Config config, HttpManager manager) {
        this.config = config;
        this.manager = manager;
        this.basePluginPath = config.getInitParameter("proxy.plugin.path");
    }

    public void addResourceFactory(String name, String className) {
        this.resourceNameMap.put(name, className);
    }

    public ResourceFactory loadResourceFactory(String name) {
        String className = this.resourceNameMap.get(name);
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader pluginLoader = this.pluginJarClassLoader(className, contextClassLoader);
        Thread.currentThread().setContextClassLoader(pluginLoader);
        try {
            AbstractResourceFactory f = loadFromSpi(className, pluginLoader);
            if (Objects.isNull(f)) {
                f = loadFromClassName(className, pluginLoader);
            }
            f.init(new ProxyNameSpaceConfig(name, this.config), this.manager);
            return f;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

    }

    public static AbstractResourceFactory loadFromSpi(String name, ClassLoader classLoader) {
        try {
            ServiceLoader<AbstractResourceFactory> load = ServiceLoader.load(AbstractResourceFactory.class, classLoader);
            Iterator<AbstractResourceFactory> it = load.iterator();
            while (it.hasNext()) {
                AbstractResourceFactory factory = it.next();
                if (StringUtils.equals(name, factory.name())) {
                    return factory;
                }
            }
        } catch (Exception ex) {
            log.error("load spi error", ex);
            return null;
        }
        return null;
    }

    public static AbstractResourceFactory loadFromClassName(String className, ClassLoader classLoader) throws ServletException {
        try {
            Class c;
            try {
                c = Class.forName(className, true, classLoader);
            } catch (ClassNotFoundException e) {
                c = Class.forName(className, true, ClassLoader.getSystemClassLoader());
            }
            return (AbstractResourceFactory) c.newInstance();
        } catch (Throwable ex) {
            throw new ServletException("Failed to instantiate: " + className, ex);
        }
    }

    private ClassLoader pluginJarClassLoader(String name, ClassLoader parentClassLoader) {
        try {
            Path pluginPath = Paths.get(this.basePluginPath, String.format("%s-plugin.jar", name));
            if (!Files.exists(pluginPath)) {
                return parentClassLoader;
            }
            return new PluginClassLoader(pluginPath.toUri().toURL(), parentClassLoader);
        } catch (Exception ex) {
            log.error("load plugin jar error", ex);
        }
        return parentClassLoader;
    }

    public HttpManager getManager() {
        return manager;
    }
}
