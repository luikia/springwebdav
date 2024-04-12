package org.luikia.sinsimito.resource;

import io.milton.resource.Resource;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Optional;

public class ChildUtils {

    public static Resource child(String childName, Collection<? extends Resource> children) {
        Optional<? extends Resource> first = children.stream()
                .filter(r -> StringUtils.equals(r.getName(), childName)).findFirst();
        return first.orElse(null);
    }
}
