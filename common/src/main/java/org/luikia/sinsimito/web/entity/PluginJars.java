package org.luikia.sinsimito.web.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;

@Data
@AllArgsConstructor
public class PluginJars {

    private String name;

    private long size;


}
