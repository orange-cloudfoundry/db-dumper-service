package com.orange.clara.cloud.repo;

import com.orange.clara.cloud.model.DatabaseRef;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "databases", path = "databases")
public interface DatabaseRefRepo extends
        PagingAndSortingRepository<DatabaseRef, String> {
}
