package com.orange.clara.cloud.servicedbdumper.integrations;

import com.google.common.collect.Maps;
import com.orange.clara.cloud.servicedbdumper.helper.URICheck;
import com.orange.clara.cloud.servicedbdumper.model.UpdateAction;
import com.orange.clara.cloud.servicedbdumper.service.DbDumperServiceInstanceService;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

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
@Service
public class ServiceBrokerRequestForge {

    public final static String SERVICE_DEFINITION_ID = "db-dumper-service";
    public final static String SERVICE_PLAN_ID = "db-dumper-service-plan-experimental";
    public final static String SPACE_GUID = "space-1";
    public final static String ORG_GUID = "org-1";
    public final static String USER_TOKEN = "faketoken";
    public final static String ORG = "org";
    public final static String SPACE = "space";

    public CreateServiceInstanceRequest createNewDumpRequest(String db, String serviceInstanceId) {
        return new CreateServiceInstanceRequest(SERVICE_DEFINITION_ID, SERVICE_PLAN_ID, ORG_GUID, SPACE_GUID, true, this.getParams(db)).withServiceInstanceId(serviceInstanceId);
    }

    public UpdateServiceInstanceRequest createDumpFromExistingServiceRequest(String db, String serviceInstanceId) {
        Map<String, Object> params = this.getParams(db);
        params.put(DbDumperServiceInstanceService.ACTION_PARAMETER, UpdateAction.DUMP.toString());
        return new UpdateServiceInstanceRequest(SERVICE_PLAN_ID, true, params).withInstanceId(serviceInstanceId);
    }

    public UpdateServiceInstanceRequest createRestoreRequest(String db, String serviceInstanceId) {
        Map<String, Object> params = this.getParams(db);
        params.put(DbDumperServiceInstanceService.ACTION_PARAMETER, UpdateAction.RESTORE.toString());
        return new UpdateServiceInstanceRequest(SERVICE_PLAN_ID, true, params).withInstanceId(serviceInstanceId);
    }

    public DeleteServiceInstanceRequest createDeleteServiceRequest(String serviceInstanceId) {
        return new DeleteServiceInstanceRequest(serviceInstanceId, SERVICE_DEFINITION_ID, SERVICE_PLAN_ID, false);
    }

    public Map<String, Object> getParams(String db) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(DbDumperServiceInstanceService.NEW_SRC_URL_PARAMETER, db);
        if (URICheck.isUri(db)) {
            return params;
        }
        params.put(DbDumperServiceInstanceService.CF_USER_TOKEN_PARAMETER, USER_TOKEN);
        params.put(DbDumperServiceInstanceService.ORG_PARAMETER, ORG);
        params.put(DbDumperServiceInstanceService.SPACE_PARAMETER, SPACE);
        return params;
    }


}
