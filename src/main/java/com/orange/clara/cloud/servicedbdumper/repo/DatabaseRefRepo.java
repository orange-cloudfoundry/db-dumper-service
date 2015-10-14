package com.orange.clara.cloud.servicedbdumper.repo;

import com.orange.clara.cloud.servicedbdumper.model.DatabaseRef;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p/>
 * Author: Arthur Halet
 * Date: 03/06/2015
 */
public interface DatabaseRefRepo extends
        PagingAndSortingRepository<DatabaseRef, String> {
}
