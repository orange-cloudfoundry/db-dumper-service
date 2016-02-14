package com.orange.clara.cloud.servicedbdumper.filer.factory;

import com.orange.clara.cloud.servicedbdumper.filer.Filer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (C) 2016 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 10/02/2016
 */
public class FactoryFiler {

    private static Logger logger = LoggerFactory.getLogger(FactoryFiler.class);

    public static Filer createFiler(FilerType filerType) throws IllegalAccessException, InstantiationException {
        logger.debug("Filer: " + filerType.getClazz().toString() + " loaded.");
        return filerType.getClazz().newInstance();
    }

    public static Filer createFiler(String filerTypeString) throws IllegalAccessException, InstantiationException {

        FilerType filerType;
        try {
            filerType = FilerType.valueOf(filerTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Filer '" + filerTypeString + "' not found defaulting to '" + FilerType.GZIPS3.name() + "'.");
            filerType = FilerType.GZIPS3;
        }
        return createFiler(filerType);
    }
}
