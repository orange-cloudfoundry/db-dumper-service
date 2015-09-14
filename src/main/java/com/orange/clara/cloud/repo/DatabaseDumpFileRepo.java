package com.orange.clara.cloud.repo;

import com.orange.clara.cloud.model.DatabaseDumpFile;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DatabaseDumpFileRepo extends
        PagingAndSortingRepository<DatabaseDumpFile, Integer> {
}
