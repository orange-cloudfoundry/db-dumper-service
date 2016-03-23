package com.orange.clara.cloud.servicedbdumper.fake.cloudfoundry;

import org.cloudfoundry.client.lib.*;
import org.cloudfoundry.client.lib.archive.ApplicationArchive;
import org.cloudfoundry.client.lib.domain.*;
import org.cloudfoundry.client.lib.rest.CloudControllerClient;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 18/03/2016
 */
public class CloudFoundryClientFake extends CloudFoundryClient {
    public final static String SERVICE_NOT_ACCESSIBLE = "not-accessible";

    public CloudFoundryClientFake() {
        super((CloudControllerClient) null);
    }

    @Override
    public void addDomain(String domainName) {

    }

    @Override
    public void addRoute(String host, String domainName) {

    }

    @Override
    public void associateAuditorWithSpace(String spaceName) {

    }

    @Override
    public void associateAuditorWithSpace(String orgName, String spaceName) {

    }

    @Override
    public void associateAuditorWithSpace(String orgName, String spaceName, String userGuid) {

    }

    @Override
    public void associateDeveloperWithSpace(String spaceName) {

    }

    @Override
    public void associateDeveloperWithSpace(String orgName, String spaceName) {

    }

    @Override
    public void associateDeveloperWithSpace(String orgName, String spaceName, String userGuid) {

    }

    @Override
    public void associateManagerWithSpace(String spaceName) {

    }

    @Override
    public void associateManagerWithSpace(String orgName, String spaceName) {

    }

    @Override
    public void associateManagerWithSpace(String orgName, String spaceName, String userGuid) {

    }

    @Override
    public void bindRunningSecurityGroup(String securityGroupName) {

    }

    @Override
    public void bindSecurityGroup(String orgName, String spaceName, String securityGroupName) {

    }

    @Override
    public void bindService(String appName, String serviceName) {

    }

    @Override
    public void bindStagingSecurityGroup(String securityGroupName) {

    }

    @Override
    public boolean checkUserPermission(CloudService service) {
        return true;
    }

    @Override
    public boolean checkUserPermission(String guid) {
        return true;
    }

    @Override
    public void createApplication(String appName, Staging staging, Integer memory, List<String> uris, List<String> serviceNames) {

    }

    @Override
    public void createApplication(String appName, Staging staging, Integer disk, Integer memory, List<String> uris, List<String> serviceNames) {

    }

    @Override
    public void createQuota(CloudQuota quota) {

    }

    @Override
    public void createSecurityGroup(CloudSecurityGroup securityGroup) {

    }

    @Override
    public void createSecurityGroup(String name, InputStream jsonRulesFile) {

    }

    @Override
    public void createService(CloudService service) {

    }

    @Override
    public void createServiceBroker(CloudServiceBroker serviceBroker) {

    }

    @Override
    public CloudServiceKey createServiceKey(String guid, String name) {
        return null;
    }

    @Override
    public CloudServiceKey createServiceKey(CloudService cloudService, String name) {
        return null;
    }

    @Override
    public CloudServiceKey createServiceKey(String guid, String name, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public CloudServiceKey createServiceKey(CloudService cloudService, String name, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public void createSpace(String spaceName) {

    }

    @Override
    public void createUserProvidedService(CloudService service, Map<String, Object> credentials) {

    }

    @Override
    public void createUserProvidedService(CloudService service, Map<String, Object> credentials, String syslogDrainUrl) {

    }

    @Override
    public void debugApplication(String appName, CloudApplication.DebugMode mode) {

    }

    @Override
    public void deleteAllApplications() {

    }

    @Override
    public void deleteAllServices() {

    }

    @Override
    public void deleteApplication(String appName) {

    }

    @Override
    public void deleteDomain(String domainName) {

    }

    @Override
    public List<CloudRoute> deleteOrphanedRoutes() {
        return null;
    }

    @Override
    public void deleteQuota(String quotaName) {

    }

    @Override
    public void deleteRoute(String host, String domainName) {

    }

    @Override
    public void deleteSecurityGroup(String securityGroupName) {

    }

    @Override
    public void deleteService(String service) {

    }

    @Override
    public void deleteServiceBroker(String name) {

    }

    @Override
    public void deleteServiceKey(String guid) {

    }

    @Override
    public void deleteServiceKey(CloudServiceKey cloudServiceKey) {

    }

    @Override
    public void deleteSpace(String spaceName) {

    }

    @Override
    public CloudApplication getApplication(String appName) {
        return null;
    }

    @Override
    public CloudApplication getApplication(UUID appGuid) {
        return null;
    }

    @Override
    public Map<String, Object> getApplicationEnvironment(UUID appGuid) {
        return null;
    }

    @Override
    public Map<String, Object> getApplicationEnvironment(String appName) {
        return null;
    }

    @Override
    public List<CloudEvent> getApplicationEvents(String appName) {
        return null;
    }

    @Override
    public InstancesInfo getApplicationInstances(String appName) {
        return null;
    }

    @Override
    public InstancesInfo getApplicationInstances(CloudApplication app) {
        return null;
    }

    @Override
    public ApplicationStats getApplicationStats(String appName) {
        return null;
    }

    @Override
    public List<CloudApplication> getApplications() {
        return null;
    }

    @Override
    public URL getCloudControllerUrl() {
        return null;
    }

    @Override
    public CloudInfo getCloudInfo() {
        return null;
    }

    @Override
    public Map<String, String> getCrashLogs(String appName) {
        return null;
    }

    @Override
    public CrashesInfo getCrashes(String appName) {
        return null;
    }

    @Override
    public CloudDomain getDefaultDomain() {
        return null;
    }

    @Override
    public List<CloudDomain> getDomains() {
        return null;
    }

    @Override
    public List<CloudDomain> getDomainsForOrg() {
        return null;
    }

    @Override
    public List<CloudEvent> getEvents() {
        return null;
    }

    @Override
    public String getFile(String appName, int instanceIndex, String filePath) {
        return null;
    }

    @Override
    public String getFile(String appName, int instanceIndex, String filePath, int startPosition) {
        return null;
    }

    @Override
    public String getFile(String appName, int instanceIndex, String filePath, int startPosition, int endPosition) {
        return null;
    }

    @Override
    public String getFileTail(String appName, int instanceIndex, String filePath, int length) {
        return null;
    }

    @Override
    public Map<String, String> getLogs(String appName) {
        return null;
    }

    @Override
    public CloudOrganization getOrgByName(String orgName, boolean required) {
        return null;
    }

    @Override
    public Map<String, CloudUser> getOrganizationUsers(String orgName) {
        return null;
    }

    @Override
    public List<CloudOrganization> getOrganizations() {
        return null;
    }

    @Override
    public List<CloudDomain> getPrivateDomains() {
        return null;
    }

    @Override
    public CloudQuota getQuotaByName(String quotaName, boolean required) {
        return null;
    }

    @Override
    public List<CloudQuota> getQuotas() {
        return null;
    }

    @Override
    public List<ApplicationLog> getRecentLogs(String appName) {
        return null;
    }

    @Override
    public List<CloudRoute> getRoutes(String domainName) {
        return null;
    }

    @Override
    public List<CloudSecurityGroup> getRunningSecurityGroups() {
        return null;
    }

    @Override
    public CloudSecurityGroup getSecurityGroup(String securityGroupName) {
        return null;
    }

    @Override
    public List<CloudSecurityGroup> getSecurityGroups() {
        return null;
    }

    @Override
    public CloudService getService(String service) {
        if (service.equals(SERVICE_NOT_ACCESSIBLE)) {
            return null;
        }
        return new CloudService(new CloudEntity.Meta(UUID.randomUUID(), new Date(), new Date()), service);
    }

    @Override
    public CloudServiceBroker getServiceBroker(String name) {
        return null;
    }

    @Override
    public List<CloudServiceBroker> getServiceBrokers() {
        return null;
    }

    @Override
    public CloudServiceInstance getServiceInstance(String service) {
        return null;
    }

    @Override
    public CloudServiceKey getServiceKey(String guid) {
        return null;
    }

    @Override
    public List<CloudServiceKey> getServiceKeys() {
        return null;
    }

    @Override
    public List<CloudServiceOffering> getServiceOfferings() {
        return null;
    }

    @Override
    public List<CloudService> getServices() {
        return null;
    }

    @Override
    public List<CloudDomain> getSharedDomains() {
        return null;
    }

    @Override
    public CloudSpace getSpace(String spaceName) {
        return null;
    }

    @Override
    public List<UUID> getSpaceAuditors(String spaceName) {
        return null;
    }

    @Override
    public List<UUID> getSpaceAuditors(String orgName, String spaceName) {
        return null;
    }

    @Override
    public List<UUID> getSpaceDevelopers(String spaceName) {
        return null;
    }

    @Override
    public List<UUID> getSpaceDevelopers(String orgName, String spaceName) {
        return null;
    }

    @Override
    public List<UUID> getSpaceManagers(String spaceName) {
        return null;
    }

    @Override
    public List<UUID> getSpaceManagers(String orgName, String spaceName) {
        return null;
    }

    @Override
    public List<CloudSpace> getSpaces() {
        return null;
    }

    @Override
    public List<CloudSpace> getSpacesBoundToSecurityGroup(String securityGroupName) {
        return null;
    }

    @Override
    public CloudStack getStack(String name) {
        return null;
    }

    @Override
    public List<CloudStack> getStacks() {
        return null;
    }

    @Override
    public String getStagingLogs(StartingInfo info, int offset) {
        return null;
    }

    @Override
    public List<CloudSecurityGroup> getStagingSecurityGroups() {
        return null;
    }

    @Override
    public OAuth2AccessToken login() {
        return null;
    }

    @Override
    public void logout() {

    }

    @Override
    public void openFile(String appName, int instanceIndex, String filePath, ClientHttpResponseCallback callback) {

    }

    @Override
    public void register(String email, String password) {

    }

    @Override
    public void registerRestLogListener(RestLogCallback callBack) {

    }

    @Override
    public void removeDomain(String domainName) {

    }

    @Override
    public void rename(String appName, String newName) {

    }

    @Override
    public StartingInfo restartApplication(String appName) {
        return null;
    }

    @Override
    public void setQuotaToOrg(String orgName, String quotaName) {

    }

    @Override
    public void setResponseErrorHandler(ResponseErrorHandler errorHandler) {

    }

    @Override
    public StartingInfo startApplication(String appName) {
        return null;
    }

    @Override
    public void stopApplication(String appName) {

    }

    @Override
    public StreamingLogToken streamLogs(String appName, ApplicationLogListener listener) {
        return null;
    }

    @Override
    public void unRegisterRestLogListener(RestLogCallback callBack) {

    }

    @Override
    public void unbindRunningSecurityGroup(String securityGroupName) {

    }

    @Override
    public void unbindSecurityGroup(String orgName, String spaceName, String securityGroupName) {

    }

    @Override
    public void unbindService(String appName, String serviceName) {

    }

    @Override
    public void unbindStagingSecurityGroup(String securityGroupName) {

    }

    @Override
    public void unregister() {

    }

    @Override
    public void updateApplicationDiskQuota(String appName, int disk) {

    }

    @Override
    public void updateApplicationEnv(String appName, Map<String, String> env) {

    }

    @Override
    public void updateApplicationEnv(String appName, List<String> env) {

    }

    @Override
    public void updateApplicationInstances(String appName, int instances) {

    }

    @Override
    public void updateApplicationMemory(String appName, int memory) {

    }

    @Override
    public void updateApplicationServices(String appName, List<String> services) {

    }

    @Override
    public void updateApplicationStaging(String appName, Staging staging) {

    }

    @Override
    public void updateApplicationUris(String appName, List<String> uris) {

    }

    @Override
    public void updatePassword(String newPassword) {

    }

    @Override
    public void updatePassword(CloudCredentials credentials, String newPassword) {

    }

    @Override
    public void updateQuota(CloudQuota quota, String name) {

    }

    @Override
    public void updateSecurityGroup(CloudSecurityGroup securityGroup) {

    }

    @Override
    public void updateSecurityGroup(String name, InputStream jsonRulesFile) {

    }

    @Override
    public void updateServiceBroker(CloudServiceBroker serviceBroker) {

    }

    @Override
    public void updateServicePlanVisibilityForBroker(String name, boolean visibility) {

    }

    @Override
    public void uploadApplication(String appName, String file) throws IOException {

    }

    @Override
    public void uploadApplication(String appName, File file) throws IOException {

    }

    @Override
    public void uploadApplication(String appName, File file, UploadStatusCallback callback) throws IOException {

    }

    @Override
    public void uploadApplication(String appName, String fileName, InputStream inputStream) throws IOException {

    }

    @Override
    public void uploadApplication(String appName, String fileName, InputStream inputStream, UploadStatusCallback callback) throws IOException {

    }

    @Override
    public void uploadApplication(String appName, ApplicationArchive archive) throws IOException {

    }

    @Override
    public void uploadApplication(String appName, ApplicationArchive archive, UploadStatusCallback callback) throws IOException {

    }

}
