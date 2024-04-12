package org.luikia.sinsimito.plugin;

import java.net.URL;
import java.net.URLClassLoader;

public class PluginClassLoader extends URLClassLoader {

    public PluginClassLoader(URL[] pluginDirectory) {
        this(pluginDirectory, Thread.currentThread().getContextClassLoader());
    }

    public PluginClassLoader(URL[] pluginDirectory, ClassLoader parentClassLoader) {
        super(pluginDirectory, parentClassLoader);
    }

    public PluginClassLoader(URL pluginDirectory) {
        this(new URL[]{pluginDirectory}, Thread.currentThread().getContextClassLoader());
    }

    public PluginClassLoader(URL pluginDirectory, ClassLoader parentClassLoader) {
        this(new URL[]{pluginDirectory}, parentClassLoader);
    }
}
