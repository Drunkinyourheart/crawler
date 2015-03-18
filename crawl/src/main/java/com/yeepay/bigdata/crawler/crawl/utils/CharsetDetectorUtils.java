package com.yeepay.bigdata.crawler.crawl.utils;

import org.apache.commons.io.IOUtils;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.txt.Icu4jEncodingDetector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * 类CharsetDetectorUtils.java的实现描述：TODO 类实现描述
 */
public final class CharsetDetectorUtils {

    private CharsetDetectorUtils() {

    }

    public static final String UTF8_BOM = "\uFEFF";

    public static Charset detect(InputStream input) throws IOException {
        if (input == null) {
            return null;
        }
        // AutoEncodingDetector universalEncodingDetector = new AutoEncodingDetector();
        EncodingDetector universalEncodingDetector = new Icu4jEncodingDetector();
        return universalEncodingDetector.detect(input, new Metadata());
    }

    public static Charset detect(byte[] bytes) throws IOException {
        if (bytes == null) {
            return null;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Charset charset = detect(byteArrayInputStream);
        IOUtils.closeQuietly(byteArrayInputStream);
        return charset;
    }

    public static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

}
