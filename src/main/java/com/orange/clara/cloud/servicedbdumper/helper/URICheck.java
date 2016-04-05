package com.orange.clara.cloud.servicedbdumper.helper;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 05/04/2016
 */
public class URICheck {

    public static boolean isUri(String possibleUri) {
        try {
            URI uri = new URI(possibleUri);
            if (uri.getScheme() == null || uri.getScheme().isEmpty()) {
                return false;
            }
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }
}
