package com.orange.clara.cloud.servicedbdumper.task.boot.sequences;

import com.orange.clara.cloud.servicedbdumper.exception.BootSequenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Copyright (C) 2016 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/02/2016
 */
@Component
@Order(3)
public class BootSequenceBinaries implements BootSequence {
    @Value("classpath:binaries")
    protected File binariesPath;
    private Logger logger = LoggerFactory.getLogger(BootSequenceBinaries.class);

    @Override
    public void runSequence() throws BootSequenceException {
        String[] commands = {"/bin/chmod", "-R", "+x", binariesPath.getAbsolutePath()};
        logger.debug("Running command: " + String.join(" ", commands) + " ...");
        ProcessBuilder pb = new ProcessBuilder(commands);
        Process p = null;
        try {
            p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new BootSequenceException("Error when booting: " + e.getMessage(), e);
        }
        logger.debug("Finished Command: " + String.join(" ", commands) + ".");
    }
}
