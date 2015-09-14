package com.orange.clara.cloud.repo;

import com.orange.clara.cloud.model.DatabaseRef;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DatabaseRefRepo extends
        PagingAndSortingRepository<DatabaseRef, String> {
}
