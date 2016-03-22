package com.orange.clara.cloud.servicedbdumper.task.boot;

import com.orange.clara.cloud.servicedbdumper.exception.BootSequenceException;
import com.orange.clara.cloud.servicedbdumper.task.boot.sequences.BootSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Copyright (C) 2015 Orange
 * <p>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'https://opensource.org/licenses/Apache-2.0'.
 * <p>
 * Author: Arthur Halet
 * Date: 08/12/2015
 */
@Component
public class Boot {

    @Autowired
    protected List<BootSequence> bootSequences;
    private Logger logger = LoggerFactory.getLogger(Boot.class);

    @PostConstruct
    public void boot() throws BootSequenceException {
        for (BootSequence bootSequence : bootSequences) {
            logger.debug("Running boot sequence: " + bootSequence.getClass().toString());
            bootSequence.runSequence();
            logger.debug("Finished boot sequence: " + bootSequence.getClass().toString());
        }
    }
}
