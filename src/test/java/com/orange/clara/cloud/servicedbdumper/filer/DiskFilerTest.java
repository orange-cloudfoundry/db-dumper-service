package com.orange.clara.cloud.servicedbdumper.filer;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 14/03/2016
 */
public class DiskFilerTest extends AbstractFilerTest {


    public DiskFilerTest() {
        super(System.getProperty("java.io.tmpdir"), "testfilerdisk/myfile.txt", new DiskFiler());
    }


}