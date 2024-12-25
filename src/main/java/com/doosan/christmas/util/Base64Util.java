package com.doosan.christmas.util;

import java.util.Base64;

public class Base64Util {

    public static boolean isBase64Encoded(String data) {
        try {
            Base64.getDecoder().decode(data);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
