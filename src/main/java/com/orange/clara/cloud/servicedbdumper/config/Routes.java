package com.orange.clara.cloud.servicedbdumper.config;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 22/03/2016
 */
public interface Routes {
    String MANAGE_ROOT = "/manage";
    String MANAGE_LIST = "/list";
    String MANAGE_LIST_DATABASE_ROOT = "/list/database";
    String MANAGE_ADMIN_ROOT = "/manage/admin";
    String MANAGE_ADMIN_ROOT_ALTERNATIVE = "/admin/control";
    String RAW_DUMP_FILE_ROOT = "/raw";
    String SHOW_DUMP_FILE_ROOT = "/show";
    String DELETE_DUMP_FILE_ROOT = "/delete";
    String DOWNLOAD_DUMP_FILE_ROOT = "/download";
    String JOB_CONTROL_ROOT = "/admin/control/jobs";
    String JOB_CONTROL_DETAILS_ROOT = "/details";
    String JOB_CONTROL_DELETE_ROOT = "/delete";
    String JOB_CONTROL_UPDATE_ROOT = "/update";

}
