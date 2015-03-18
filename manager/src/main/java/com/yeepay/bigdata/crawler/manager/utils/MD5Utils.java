package com.yeepay.bigdata.crawler.manager.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    public static String md5Hex(String message) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] bs = md5.digest(message.getBytes());
            return hex(bs);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String hex(byte[] bs) {
        StringBuilder sb = new StringBuilder(32);
        for (byte b : bs) {
            sb.append(Integer.toHexString((b & 0xff) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // byte a = -1;
        // System.out.println(Integer.toBinaryString(a));
        // System.out.println(Integer.toHexString((a & 0xff) |
        // 0x100).substring(1,
        // 3));

        System.out.println(md5Hex(""));
    }
}
