package org.luikia.sinsimito.utils;

import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class HashUtils {

    public static String hash(List<byte[]> data) {
        List<byte[]> cloneData = new ArrayList<>(data);
        cloneData.sort(Comparator.comparingInt(Arrays::hashCode));
        int size = cloneData.stream().mapToInt(d -> d.length).sum();
        ByteBuffer bf = ByteBuffer.allocate(size);
        cloneData.forEach(bf::put);
        bf.flip();
        return Hashing.sha256().hashBytes(bf).toString();
    }
}
