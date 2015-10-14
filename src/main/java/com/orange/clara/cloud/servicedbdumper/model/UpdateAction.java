package com.orange.clara.cloud.servicedbdumper.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2015 Arthur Halet
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 13/10/2015
 */
public enum UpdateAction {
    DUMP, RESTORE;

    public static String showValues() {
        List<String> updateActionStrings = new ArrayList<>();
        for (UpdateAction updateAction : UpdateAction.values()) {
            updateActionStrings.add(updateAction.toString());
        }
        return String.join(", ", updateActionStrings);
    }
}
