package com.orange.clara.cloud.servicedbdumper.dbdumper;

import com.orange.clara.cloud.servicedbdumper.exception.DatabaseExtractionException;
import com.orange.clara.cloud.servicedbdumper.exception.ServiceKeyException;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseService;
import com.orange.clara.cloud.servicedbdumper.model.DatabaseType;
import com.orange.clara.cloud.servicedbdumper.model.Job;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseRefRepo;
import com.orange.clara.cloud.servicedbdumper.repo.DatabaseServiceRepo;
import com.orange.clara.cloud.servicedbdumper.service.servicekey.ServiceKeyManager;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
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
 * Date: 18/02/2016
 */
@Component
public class DatabaseRefManager {

    private final static String URI_KEY_REGEX = "(uri|url)";
    private final static String HOST_KEY_REGEX = ".*host.*";
    private final static String PASSWORD_KEY_REGEX = ".*pass.*";
    private final static String USERNAME_KEY_REGEX = ".*user.*";
    private final static String PORT_KEY_REGEX = ".*port.*";
    private final static String DATABASE_NAME_KEY_REGEX = ".*name.*";

    private Logger logger = LoggerFactory.getLogger(DatabaseRefManager.class);
    @Autowired
    private DatabaseRefRepo databaseRefRepo;

    @Autowired
    private DatabaseServiceRepo databaseServiceRepo;

    @Autowired
    private ServiceKeyManager serviceKeyManager;

    public DatabaseRef getDatabaseRef(String uriOrServiceName, String token, String org, String space) throws ServiceKeyException, DatabaseExtractionException {
        if (this.isUri(uriOrServiceName)) {
            return this.getDatabaseRefFromUrl(uriOrServiceName, this.generateDatabaseRefName(uriOrServiceName));
        }
        if (token == null || token.isEmpty()) {
            throw new ServiceKeyException("You must pass your token (param: token)");
        }
        CloudServiceKey cloudServiceKey = this.serviceKeyManager.createServiceKey(uriOrServiceName, token, org, space);
        return this.getDatabaseRefFromServiceKey(cloudServiceKey, org, space);
    }

    public DatabaseRef updateDatabaseRef(DatabaseRef databaseRef) throws ServiceKeyException, DatabaseExtractionException {
        if (databaseRef.getDatabaseService() == null) {
            return databaseRef;
        }
        DatabaseService databaseService = databaseRef.getDatabaseService();
        CloudServiceKey cloudServiceKey = this.serviceKeyManager.createServiceKey(databaseService);
        return this.getDatabaseRefFromServiceKey(cloudServiceKey, databaseService.getOrg(), databaseService.getSpace());
    }


    private boolean isUri(String possibleUri) {
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

    private DatabaseRef getDatabaseRefFromUrl(String dbUrl, String name) {
        DatabaseRef databaseRef = new DatabaseRef(name, URI.create(dbUrl));
        DatabaseRef databaseRefDao = null;
        if (!this.databaseRefRepo.exists(name)) {
            this.databaseRefRepo.save(databaseRef);
            return databaseRef;
        }
        databaseRefDao = this.databaseRefRepo.findOne(name);
        this.updateDatabaseRef(databaseRef, databaseRefDao);

        return databaseRefDao;
    }

    public void deleteServiceKey(Job job) {
        if (job.getDatabaseRefSrc() != null) {
            this.deleteServiceKey(job.getDatabaseRefSrc());
        }
        if (job.getDatabaseRefTarget() != null) {
            this.deleteServiceKey(job.getDatabaseRefTarget());
        }
    }

    public void deleteServiceKey(DatabaseRef databaseRef) {
        if (databaseRef.getDatabaseService() == null) {
            return;
        }
        DatabaseService databaseService = databaseRef.getDatabaseService();
        logger.info(String.format("Remove service key for service '%s' ...", databaseService.getName()));
        try {
            this.serviceKeyManager.deleteServiceKey(databaseService);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info(String.format("Removed service key for service '%s'.", databaseService.getName()));
    }

    private DatabaseRef getDatabaseRefFromServiceKey(CloudServiceKey cloudServiceKey, String org, String space) throws DatabaseExtractionException {
        String dbUrl = this.extractUriFromCredentialKey(cloudServiceKey.getCredentials());
        if (dbUrl.isEmpty()) {
            dbUrl = this.extractUriFromCloudServiceKey(cloudServiceKey);
        }
        DatabaseRef databaseRef = this.getDatabaseRefFromUrl(dbUrl, cloudServiceKey.getService().getMeta().getGuid().toString());
        DatabaseService databaseService = this.getDatabaseServiceFromServiceKey(cloudServiceKey, databaseRef, org, space);
        databaseRef.setDatabaseService(databaseService);
        this.databaseRefRepo.save(databaseRef);
        return databaseRef;
    }

    private DatabaseService getDatabaseServiceFromServiceKey(CloudServiceKey cloudServiceKey, DatabaseRef databaseRef, String org, String space) {
        CloudService cloudService = cloudServiceKey.getService();
        DatabaseService databaseService = new DatabaseService(
                cloudService.getMeta().getGuid().toString(),
                cloudService.getName(),
                org,
                space,
                cloudServiceKey.getMeta().getGuid().toString()
        );
        DatabaseService databaseServiceDao = null;
        if (!this.databaseServiceRepo.exists(cloudService.getMeta().getGuid().toString())) {
            databaseService.setDatabaseRef(databaseRef);
            this.databaseServiceRepo.save(databaseService);
            return databaseService;
        }
        databaseServiceDao = this.databaseServiceRepo.findOne(cloudService.getMeta().getGuid().toString());
        this.updateDatabaseService(databaseService, databaseServiceDao);
        return databaseServiceDao;
    }

    private DatabaseType extractDatabaseTypeFromLabel(String label) {
        for (DatabaseType databaseType : DatabaseType.values()) {
            if (label.matches(databaseType.getMatcher())) {
                return databaseType;
            }
        }
        return null;
    }

    private String extractUriFromCloudServiceKey(CloudServiceKey cloudServiceKey) throws DatabaseExtractionException {
        CloudService cloudService = cloudServiceKey.getService();
        logger.debug("Service found: " + cloudService.getMeta() + " with label: " + cloudService.getLabel());
        Map<String, Object> credentials = cloudServiceKey.getCredentials();

        DatabaseType databaseType = this.extractDatabaseTypeFromLabel(cloudService.getLabel());
        if (databaseType == null) {
            throw new DatabaseExtractionException("Database type cannot be extracted '" + cloudService.getName() + "'");
        }
        String host = this.extractHostFromCredentials(credentials);
        if (host.isEmpty()) {
            throw new DatabaseExtractionException("Hostname cannot be extracted from service '" + cloudService.getName() + "'");
        }

        String databaseName = "/" + this.extractDatabaseNameFromCredentials(credentials);
        String username = this.extractUsernameFromCredentials(credentials);
        String password = this.extractPasswordFromCredentials(credentials);
        String portString = this.extractPortFromCredentials(credentials);
        int port = -1;
        if (!portString.isEmpty()) {
            port = Integer.parseInt(portString);
        }
        String userInfo = "";
        if (!username.isEmpty() && !password.isEmpty()) {
            userInfo = username + ":" + password;
        }
        if (!password.isEmpty() && username.isEmpty()) {
            userInfo = password;
        }
        try {
            URI generatedUri = new URI(databaseType.name().toLowerCase(), userInfo, host, port, databaseName, null, null);
            return generatedUri.toString();
        } catch (URISyntaxException e) {
            throw new DatabaseExtractionException("Database cannot be extracted from service '" + cloudService.getName() + "' - Error: " + e.getMessage(), e);
        }

    }

    private String extractUriFromCredentialKey(Map<String, Object> credentials) {
        return this.extractValueFromCredentials(credentials, URI_KEY_REGEX);
    }

    private String extractHostFromCredentials(Map<String, Object> credentials) {
        return this.extractValueFromCredentials(credentials, HOST_KEY_REGEX);
    }

    private String extractUsernameFromCredentials(Map<String, Object> credentials) {
        return this.extractValueFromCredentials(credentials, USERNAME_KEY_REGEX);
    }

    private String extractPasswordFromCredentials(Map<String, Object> credentials) {
        return this.extractValueFromCredentials(credentials, PASSWORD_KEY_REGEX);
    }

    private String extractPortFromCredentials(Map<String, Object> credentials) {
        return this.extractValueFromCredentials(credentials, PORT_KEY_REGEX);
    }

    private String extractDatabaseNameFromCredentials(Map<String, Object> credentials) {
        return this.extractValueFromCredentials(credentials, DATABASE_NAME_KEY_REGEX);
    }

    private String extractValueFromCredentials(Map<String, Object> credentials, String keyToFind) {
        for (String key : credentials.keySet()) {
            if (key.matches(keyToFind)) {
                return credentials.get(key).toString();
            }
        }
        return "";
    }

    private void updateDatabaseRef(DatabaseRef databaseRefTemp, DatabaseRef databaseRefDao) {
        if (databaseRefDao.equals(databaseRefTemp)) {
            return;
        }
        databaseRefDao.setDatabaseName(databaseRefTemp.getDatabaseName());
        databaseRefDao.setHost(databaseRefTemp.getHost());
        databaseRefDao.setPassword(databaseRefTemp.getPassword());
        databaseRefDao.setPort(databaseRefTemp.getPort());
        databaseRefDao.setType(databaseRefTemp.getType());
        databaseRefDao.setUser(databaseRefTemp.getUser());
        this.databaseRefRepo.save(databaseRefDao);
    }

    private void updateDatabaseService(DatabaseService databaseServiceTemp, DatabaseService databaseServiceDao) {
        databaseServiceDao.setServiceKeyGuid(databaseServiceTemp.getServiceKeyGuid());
        this.databaseServiceRepo.save(databaseServiceDao);
    }

    private String generateDatabaseRefName(String srcUrl) {
        UUID dbRefName = UUID.nameUUIDFromBytes(srcUrl.getBytes());
        return dbRefName.toString();
    }
}
