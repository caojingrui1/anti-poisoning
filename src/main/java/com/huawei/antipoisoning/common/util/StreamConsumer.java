package com.huawei.antipoisoning.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author liuwugang LWX1222007
 *
 * @ClassName StreamConsumer
 * @since 2023/1/17 11:25
 */
public class StreamConsumer extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamConsumer.class);
    InputStream is;
    List<String> strList;

    public StreamConsumer(InputStream is, List<String> strList) {
        this.is = is;
        this.strList = strList;
    }

    @Override
    public void run() {
        try {
            InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8);
            LineNumberReader input = new LineNumberReader(ir);
            String line;
            while ((line = input.readLine()) != null) {
                strList.add(line);
                LOGGER.info(line);
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}
