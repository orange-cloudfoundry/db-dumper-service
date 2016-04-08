package com.orange.clara.cloud.servicedbdumper.integrations;

import com.google.common.collect.Maps;
import com.orange.clara.cloud.servicedbdumper.helper.URICheck;
import com.orange.clara.cloud.servicedbdumper.model.UpdateAction;
import com.orange.clara.cloud.servicedbdumper.service.DbDumperServiceInstanceService;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

    public String SERVICE_DEFINITION_ID = "db-dumper-service";
    public String SERVICE_PLAN_ID = "db-dumper-service-plan-experimental";
    private String spaceGuid;
    private String orgGuid;
    private String userToken;
    private String org;
    private String space;

    public CreateServiceInstanceRequest createNewDumpRequest(String db, String serviceInstanceId) {
        return new CreateServiceInstanceRequest(SERVICE_DEFINITION_ID, SERVICE_PLAN_ID, orgGuid, spaceGuid, true, this.getParams(db)).withServiceInstanceId(serviceInstanceId);
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
        params.put(DbDumperServiceInstanceService.CF_USER_TOKEN_PARAMETER, userToken);
        params.put(DbDumperServiceInstanceService.ORG_PARAMETER, org);
        params.put(DbDumperServiceInstanceService.SPACE_PARAMETER, space);
        return params;
    }

    @PostConstruct
    public void createDefaultData() {
        spaceGuid = "space-1";
        orgGuid = "org-1";
        userToken = "faketoken";
        org = "org";
        space = "space";
    }

    public String getSpaceGuid() {
        return spaceGuid;
    }

    public void setSpaceGuid(String spaceGuid) {
        this.spaceGuid = spaceGuid;
    }

    public String getOrgGuid() {
        return orgGuid;
    }

    public void setOrgGuid(String orgGuid) {
        this.orgGuid = orgGuid;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }
}
