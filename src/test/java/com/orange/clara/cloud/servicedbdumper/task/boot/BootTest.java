package com.orange.clara.cloud.servicedbdumper.task.boot;

import com.google.common.collect.Lists;
import com.orange.clara.cloud.servicedbdumper.exception.BootSequenceException;
import com.orange.clara.cloud.servicedbdumper.task.boot.sequences.BootSequence;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 21/03/2016
 */
public class BootTest {
    private static String content;
    Boot boot = new Boot();

    @Before
    public void init() {
        initMocks(this);
        content = "content";
        List<BootSequence> bootSequences = Lists.newArrayList();
        bootSequences.add(() -> {
            content = "altered";
        });
        boot.bootSequences = bootSequences;
    }

    @Test
    public void ensure_boot_have_annotation_post_construct() throws NoSuchMethodException {
        assertThat(boot.getClass().getMethod("boot").isAnnotationPresent(PostConstruct.class)).isTrue();
    }

    @Test
    public void ensure_sequences_are_ran() throws BootSequenceException {
        assertThat(content).isEqualTo("content");
        this.boot.boot();
        assertThat(content).isNotEqualTo("content");
    }
}