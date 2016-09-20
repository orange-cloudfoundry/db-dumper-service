package com.orange.clara.cloud.servicedbdumper.repo;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseDumpFile;
import com.orange.clara.cloud.servicedbdumper.model.DbDumperServiceInstance;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
@Repository
public interface DatabaseDumpFileRepo extends
        PagingAndSortingRepository<DatabaseDumpFile, Integer> {

    DatabaseDumpFile findFirstByDbDumperServiceInstanceOrderByCreatedAtDesc(DbDumperServiceInstance dbDumperServiceInstance);

    List<DatabaseDumpFile> findByDeletedTrueOrderByDeletedAtAsc();

    List<DatabaseDumpFile> findByDeletedFalseOrderByCreatedAtAsc();

    DatabaseDumpFile findByDbDumperServiceInstanceAndCreatedAt(DbDumperServiceInstance dbDumperServiceInstance, Date date);

    DatabaseDumpFile findFirstByDbDumperServiceInstanceAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(DbDumperServiceInstance dbDumperServiceInstance, Date date);
}
