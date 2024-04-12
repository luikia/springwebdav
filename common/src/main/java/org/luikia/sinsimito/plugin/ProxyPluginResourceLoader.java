package org.luikia.sinsimito.plugin;

import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.servlet.Config;
import org.apache.commons.lang3.StringUtils;
import org.luikia.sinsimito.resource.AbstractResourceFactory;
import org.luikia.sinsimito.resource.ProxyNameSpaceConfig;
import org.luikia.sinsimito.resource.ProxyResourceLoader;
import org.luikia.sinsimito.utils.HashUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProxyPluginResourceLoader {


    private final Config baseConfig;

    private final HttpManager manager;

    private final String rsaKey;

    private final Map<String, ClassLoader> factoryClassLoaderMapping = new HashMap<>();

    private final Map<String, ClassLoader> classLoaderHashingMapping = new HashMap<>();

    public ProxyPluginResourceLoader(Config baseConfig, HttpManager manager) {
        this.baseConfig = baseConfig;
        this.manager = manager;
        this.rsaKey = baseConfig.getInitParameter("rsa");
    }


    public ResourceFactory loadResourceFactory(String name, String className, List<byte[]> jarDatas, Map<String, String> config) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader pluginClassLoader;
        if (Objects.isNull(jarDatas)) {
            pluginClassLoader = this.factoryClassLoaderMapping.get(className);
            if (Objects.isNull(pluginClassLoader)) {
                throw new RuntimeException("class:" + className + " not loaded");
            }
        } else {
            String hash = HashUtils.hash(jarDatas);
            if (this.factoryClassLoaderMapping.containsKey(hash)) {
                pluginClassLoader = this.classLoaderHashingMapping.get(hash);
                this.factoryClassLoaderMapping.put(className, pluginClassLoader);
            } else {
                try {
                    if (StringUtils.isEmpty(this.rsaKey)) {
                        pluginClassLoader = JarByteArrayClassLoader.of(contextClassLoader, jarDatas);
                    } else {
                        pluginClassLoader = JarRsaSecureClassLoader.of(contextClassLoader, this.rsaKey, jarDatas);
                    }
                } catch (Exception ex) {
                    pluginClassLoader = JarByteArrayClassLoader.of(contextClassLoader, jarDatas);
                }
                this.factoryClassLoaderMapping.put(className, pluginClassLoader);
                this.classLoaderHashingMapping.put(hash, pluginClassLoader);
            }
        }
        Thread.currentThread().setContextClassLoader(pluginClassLoader);
        try {
            AbstractResourceFactory f = ProxyResourceLoader.loadFromSpi(className, pluginClassLoader);
            if (Objects.isNull(f)) {
                f = ProxyResourceLoader.loadFromClassName(className, pluginClassLoader);
            }
            ProxyNameSpaceConfig conf = new ProxyNameSpaceConfig(name, baseConfig);
            conf.addConfigs(config);
            f.init(conf, this.manager);
            return f;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

    }

}
