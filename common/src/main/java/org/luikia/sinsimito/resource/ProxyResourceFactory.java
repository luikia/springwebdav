package org.luikia.sinsimito.resource;

import io.milton.common.Path;
import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import io.milton.servlet.Config;
import io.milton.servlet.Initable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.luikia.sinsimito.plugin.ProxyPluginResourceLoader;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ProxyResourceFactory extends AbstractResourceFactory implements Scheduled {


    private ScheduledExecutorService scheduledExecutor;

    private final Map<String, ResourceFactory> factoryMap = new HashMap<>();

    private final List<Runnable> shutdownCallBack = new ArrayList<>();

    private ProxyResourceLoader proxyResourceLoader;

    private ProxyPluginResourceLoader proxyPluginResourceLoader;

    private final List<Scheduled> scheduledTasks = new ArrayList<>();

    private String prefix;
    private Config config;

    private HttpManager manager;

    @Override
    public void init(Config config, HttpManager manager) {
        this.config = config;
        this.manager = manager;
        this.prefix = config.getInitParameter("path");
        this.proxyResourceLoader = new ProxyResourceLoader(config, manager);
        this.proxyPluginResourceLoader = new ProxyPluginResourceLoader(config, manager);
        String factories = StringUtils.trimToEmpty(config.getInitParameter("proxy.factories"));
        String[] resourceFactories = StringUtils.split(factories, ",");
        long interval = NumberUtils.toLong(config.getInitParameter("schedule.interval.ms"), -1);
        for (String resourceFactory : resourceFactories) {
            String[] split = StringUtils.split(resourceFactory, ":");
            String subPath = StringUtils.trim(split[0]);
            String className = StringUtils.trim(split[1]);
            this.initResourceFactory(subPath, className);
        }
        if (interval > 0L) {
            int threadCount = NumberUtils.toInt(config.getInitParameter("schedule.thread.count"), 1);
            this.scheduledExecutor = Executors.newScheduledThreadPool(threadCount);
            this.scheduledExecutor.scheduleAtFixedRate(() -> this.schedule(), 0L, interval, TimeUnit.MILLISECONDS);
        }
    }


    private void initResourceFactory(String name, String className) {
        this.factoryMap.put(name, null);
        this.proxyResourceLoader.addResourceFactory(name, className);
    }


    @Override
    public Resource getResource(String host, String path) throws NotAuthorizedException, BadRequestException {
        String removePrefixPath = path;
        if (path.startsWith(this.prefix)) {
            removePrefixPath = StringUtils.removeStart(path, prefix);
        }
        if (Path.path(removePrefixPath).isRoot()) {
            return new ProxyResource("", this.factoryMap.keySet(), this.config);
        }
        removePrefixPath = StringUtils.removeStart(removePrefixPath, "/");
        String[] split = StringUtils.splitByWholeSeparator(removePrefixPath, "/", 2);
        String name = split[0];
        if (!this.factoryMap.containsKey(name)) {
            return null;
        }
        ResourceFactory resourceFactory = this.factoryMap.get(name);
        if (Objects.isNull(resourceFactory)) {
            resourceFactory = this.proxyResourceLoader.loadResourceFactory(name);
            if (Objects.nonNull(resourceFactory)) {
                this.registerResourceFactory(name, resourceFactory);
            } else {
                this.factoryMap.put(name, EmptyResourceFactory.INSTANCE);
                return null;
            }
        }
        String s = StringUtils.removeStart(removePrefixPath, name);
        return resourceFactory.getResource(host, s);
    }

    private void registerResourceFactory(String name, ResourceFactory resourceFactory) {
        this.factoryMap.put(name, resourceFactory);
        if (resourceFactory instanceof Scheduled) {
            this.scheduledTasks.add((Scheduled) resourceFactory);
        }
    }


    @Override
    public void destroy(HttpManager manager) {
        Collection<ResourceFactory> fs = factoryMap.values();
        for (ResourceFactory f : fs) {
            if (f instanceof Initable) {
                ((Initable) f).destroy(manager);
            }
        }
        if (!this.scheduledExecutor.isShutdown()) {
            this.scheduledExecutor.shutdown();
        }
        this.shutdownCallBack.forEach(Runnable::run);
    }

    @Override
    public String name() {
        return "proxy";
    }

    private void stopSchedule() {
        if (CollectionUtils.isNotEmpty(this.scheduledTasks) || Objects.isNull(this.scheduledExecutor)) {
            return;
        }
        boolean stopSchedule = true;
        for (ResourceFactory value : this.factoryMap.values()) {
            if (Objects.isNull(value)) {
                stopSchedule = false;
                break;
            }
        }
        if (stopSchedule && !this.scheduledExecutor.isShutdown()) {
            this.scheduledExecutor.shutdown();
        }
    }

    @Override
    public void schedule() {
        //this.stopSchedule();
        if (CollectionUtils.isEmpty(this.scheduledTasks)) {
            return;
        }
        this.scheduledTasks.forEach(Scheduled::schedule);

    }

    public Map<String, ResourceFactory> getFactoryMap() {
        return this.factoryMap;
    }

    public void addPlugin(String name, String className, List<byte[]> jarInputs, Map<String, String> config) {
        ResourceFactory resourceFactory = this.proxyPluginResourceLoader.loadResourceFactory(name, className, jarInputs, config);
        if (Objects.nonNull(resourceFactory)) {
            log.info("plugin {} register,class:{}", name, className);
            this.registerResourceFactory(name, resourceFactory);
        }
    }

    public String removePlugin(String name) {
        if (!this.factoryMap.containsKey(name)) {
            return null;
        }
        String className = null;
        ResourceFactory resourceFactory = this.factoryMap.get(name);
        if (Objects.nonNull(resourceFactory)) {
            className = resourceFactory.getClass().getName();
            if (resourceFactory instanceof Initable) {
                ((Initable) resourceFactory).destroy(this.manager);
            }
        }
        this.factoryMap.remove(name);
        return className;
    }
}
