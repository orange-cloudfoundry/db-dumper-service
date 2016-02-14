package com.orange.clara.cloud.servicedbdumper.interceptor;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.model.MappedRequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p/>
 * Author: Arthur Halet
 * Date: 10/12/2015
 */
public class AddAdminUrlsInterceptor extends HandlerInterceptorAdapter {

    private final static String DEFAULT_ADMIN_URL = "/admin";
    List<MappedRequestInfo> mappedRequests = Lists.newArrayList();
    @Autowired
    private RequestMappingHandlerMapping handlerMapping;
    @Autowired
    private EndpointHandlerMapping endpointHandlerMapping;


    private void loadMappedRequestFromRequestMappingInfoSet(Set<RequestMappingInfo> requestMappingInfoSet) {
        for (RequestMappingInfo requestMappingInfo : requestMappingInfoSet) {
            String patternUrl = this.stringifyPatternsCondition(requestMappingInfo.getPatternsCondition());
            if (patternUrl.contains("{")
                    || patternUrl.contains("}")
                    || !patternUrl.startsWith(DEFAULT_ADMIN_URL)
                    || patternUrl.equals(DEFAULT_ADMIN_URL)) {
                continue;
            }
            String name = patternUrl.replace(DEFAULT_ADMIN_URL + "/", "");
            name = name.replace("/", "-");
            MappedRequestInfo mappedRequestInfo = new MappedRequestInfo(name, patternUrl);
            if (mappedRequests.contains(mappedRequestInfo) || mappedRequestInfo.getName().equals("welcome")) {
                continue;
            }
            mappedRequests.add(mappedRequestInfo);
        }
    }

    private String stringifyPatternsCondition(PatternsRequestCondition patternsRequestCondition) {
        StringBuilder builder = new StringBuilder();
        Iterator iterator = patternsRequestCondition.getPatterns().iterator();
        Object expression = iterator.next();
        builder.append(expression.toString());
        return builder.toString();
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (!modelAndView.hasView()) {
            return;
        }
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.handlerMapping.getHandlerMethods();
        this.loadMappedRequestFromRequestMappingInfoSet(handlerMethods.keySet());
        this.loadMappedRequestFromRequestMappingInfoSet(endpointHandlerMapping.getHandlerMethods().keySet());
        Collections.sort(mappedRequests, (mappedRequestInfo1, mappedRequestInfo2)
                -> mappedRequestInfo1.getName().compareTo(mappedRequestInfo2.getName()));
        modelAndView.addObject("mappedRequests", mappedRequests);
    }
}
