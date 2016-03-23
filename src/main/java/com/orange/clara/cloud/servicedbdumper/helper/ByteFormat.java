package com.orange.clara.cloud.servicedbdumper.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/02/2016
 */
public class ByteFormat {

    public static long parse(String sizeInText) {
        String formatedSizeInText = sizeInText.trim().toLowerCase();
        formatedSizeInText = formatedSizeInText.replaceAll(",", ".");
        try {
            return Long.parseLong(formatedSizeInText);
        } catch (NumberFormatException e) {

        }
        final Matcher m = Pattern.compile("([\\d.,]+)\\s*(\\w)").matcher(formatedSizeInText);
        m.find();
        long scale = 1;
        switch (m.group(2).charAt(0)) {
            case 't':
                scale = 1024 * 1024 * 1024 * 1024;
                break;
            case 'g':
                scale = 1024 * 1024 * 1024;
                break;
            case 'm':
                scale = 1024 * 1024;
                break;
            case 'k':
                scale = 1024;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return Math.round(Double.parseDouble(m.group(1))) * scale;
    }
}
