package org.luikia.sinsimito.web;

import io.milton.http.ResourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.luikia.sinsimito.resource.ProxyResourceFactory;
import org.luikia.sinsimito.web.entity.PluginInfo;
import org.luikia.sinsimito.web.entity.PluginJars;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/config")
public class ResourceFactoryController {
    private ProxyResourceFactory resourceFactory;
    @Value("${webdav.plugin.max:10}")
    private int maxPluginCount;

    private Map<String, PluginJars> pluginJarsMap = new ConcurrentHashMap<>();

    @RequestMapping(value = "/mounted", method = RequestMethod.GET)
    public List<PluginInfo> loadedPlugins() {
        Map<String, ResourceFactory> factoryMap = this.resourceFactory.getFactoryMap();
        List<PluginInfo> list = new ArrayList<>();
        factoryMap.forEach((k, v) -> {
            if (Objects.nonNull(v)) {
                list.add(new PluginInfo(k, v.getClass().getName()));
            }
        });
        return list;
    }

    @RequestMapping(value = "/mounted/{name}", method = RequestMethod.GET)
    public PluginInfo loadedPlugin(@PathVariable("name") String name) {
        Map<String, ResourceFactory> factoryMap = this.resourceFactory.getFactoryMap();
        ResourceFactory rf = factoryMap.get(name);
        if (Objects.isNull(rf)) return null;
        return new PluginInfo(name, rf.getClass().getName());
    }

    @RequestMapping(value = "/mount/{name}", method = RequestMethod.PUT)
    public PluginInfo mountPlugin(@PathVariable("name") String name,
                                  @RequestParam(value = "class") String plugin,
                                  @RequestParam(value = "file") MultipartFile[] multipartFiles,
                                  @RequestParam Map<String, String> params) {
        if (this.pluginJarsMap.size() >= this.maxPluginCount) {
            throw new RuntimeException("current has " + this.pluginJarsMap.size() + " plugin,max:" + this.maxPluginCount);
        }
        PluginInfo info = new PluginInfo(name, plugin);
        try {
            AtomicLong size = new AtomicLong(0L);
            List<byte[]> jarInputs = new ArrayList<>(multipartFiles.length);
            Arrays.stream(multipartFiles).peek(e -> size.addAndGet(e.getSize())).forEach(e -> {
                try {
                    jarInputs.add(e.getBytes());
                } catch (IOException ex) {
                }
            });
            this.resourceFactory.addPlugin(name, plugin, jarInputs, params);
            this.pluginJarsMap.put(plugin, new PluginJars(plugin, size.get()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return info;
    }

    @RequestMapping(value = "/mount", method = RequestMethod.POST)
    public PluginInfo mountPluginWithConfigFile(@RequestParam(value = "file") MultipartFile[] multipartFile,
                                                @RequestParam(value = "config_file") MultipartFile[] configFile) throws IOException {
        Map<String, String> map = this.fileToConfigMap(configFile);
        String name = map.get("name");
        String plugin = map.get("class");
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(plugin)) {
            throw new RuntimeException("config:name and class must not null");
        }
        map.remove("name");
        map.remove("class");
        return this.mountPlugin(name, plugin, multipartFile, map);
    }

    @RequestMapping(value = "/mount_exists/{name}", method = RequestMethod.PUT)
    public PluginInfo mountExistsPlugin(@PathVariable("name") String name,
                                        @RequestParam(value = "class") String plugin,
                                        @RequestParam Map<String, String> params) {
        if (this.pluginJarsMap.size() >= this.maxPluginCount) {
            throw new RuntimeException("current has " + this.pluginJarsMap.size() + " plugin,max:" + this.maxPluginCount);
        }
        if (!this.pluginJarsMap.containsKey(plugin)) {
            throw new RuntimeException("class " + plugin + " not exists");
        }
        try {
            this.resourceFactory.addPlugin(name, plugin, null, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new PluginInfo(name, plugin);
    }

    @RequestMapping(value = "/mount_exists", method = RequestMethod.POST)
    public PluginInfo mountExistsPluginWithConfig(@RequestParam(value = "config_file") MultipartFile[] configFile) {
        try {
            Map<String, String> map = this.fileToConfigMap(configFile);
            String name = map.get("name");
            String plugin = map.get("class");
            if (StringUtils.isEmpty(name) || StringUtils.isEmpty(plugin)) {
                throw new RuntimeException("config:name and class must not null");
            }
            map.remove("name");
            map.remove("class");
            this.mountExistsPlugin(name, plugin, map);
            return new PluginInfo(name, plugin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/mount/{name}", method = RequestMethod.DELETE)
    public PluginInfo umount(@PathVariable("name") String name) {
        String className = this.resourceFactory.removePlugin(name);
        log.info("remove plugin {} success", name);
        return new PluginInfo(name, className);
    }

    @RequestMapping(value = "/plugin", method = RequestMethod.GET)
    public Collection<PluginJars> plugins() {
        return this.pluginJarsMap.values();
    }

    @RequestMapping(value = "/plugin/{name}")
    public PluginJars pluginWithName(@PathVariable("name") String name) {
        return this.pluginJarsMap.get(name);
    }

    private Map<String, String> fileToConfigMap(MultipartFile[] configFile) {
        Map<String, String> map = new HashMap<>();
        for (MultipartFile cf : configFile) {
            Properties properties = new Properties();
            try {
                properties.load(cf.getInputStream());
                properties.stringPropertyNames().forEach(e -> map.put(e, properties.getProperty(e)));
            } catch (Exception ex) {
                log.info("load config file error,skip", ex);
            }
        }
        return map;
    }

    @Resource
    public void setResourceFactory(ProxyResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }
}
