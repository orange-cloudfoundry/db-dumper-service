package com.orange.clara.cloud.servicedbdumper.helper;

import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;

import java.util.Map;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 16/09/2016
 */
public class ParameterParser {

    public static String getParameter(Map<String, Object> parameters, String parameter) throws ServiceBrokerException {
        String param = getParameter(parameters, parameter, null);
        if (param == null) {
            throw new ServiceBrokerException("You need to set '" + parameter + "' parameter.");
        }
        return param;
    }

    public static String getParameter(Map<String, Object> parameters, String parameter, String defaultValue) throws ServiceBrokerException {
        if (parameters == null) {
            return defaultValue;
        }
        Object paramObject = parameters.get(parameter);
        if (paramObject == null) {
            return defaultValue;
        }
        return paramObject.toString();
    }
}
