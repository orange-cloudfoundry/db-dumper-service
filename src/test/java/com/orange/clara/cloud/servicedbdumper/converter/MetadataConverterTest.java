package com.orange.clara.cloud.servicedbdumper.converter;

import com.orange.clara.cloud.servicedbdumper.model.Metadata;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Copyright (C) 2016 Arthur Halet
 * <p>
 * This software is distributed under the terms and conditions of the 'MIT'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://opensource.org/licenses/MIT'.
 * <p>
 * Author: Arthur Halet
 * Date: 17/09/2016
 */
public class MetadataConverterTest {
    private final static Metadata metadata = new Metadata();
    private final static String tag = "mytag";
    private MetadataConverter metadataConverter = new MetadataConverter();

    @Before
    public void setUp() throws Exception {
        metadata.setTags(Arrays.asList(tag));
    }

    @Test
    public void testConvertToDatabaseColumn() throws Exception {
        assertThat(metadataConverter.convertToDatabaseColumn(null)).isEqualTo(null);
        assertThat(metadataConverter.convertToDatabaseColumn(metadata)).isEqualTo("{\"tags\":[\"" + tag + "\"]}");
    }

    @Test
    public void testConvertToEntityAttribute() throws Exception {
        assertThat(metadataConverter.convertToEntityAttribute(null)).isEqualTo(null);
        Metadata convertedMetadata = metadataConverter.convertToEntityAttribute("{\"tags\":[\"" + tag + "\"]}");
        assertThat(convertedMetadata).isNotNull();
        assertThat(convertedMetadata.getTags()).hasSize(1);
        assertThat(convertedMetadata.getTags().get(0)).isEqualTo(tag);
    }

}