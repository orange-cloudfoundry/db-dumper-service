package com.orange.clara.cloud.servicedbdumper.model;

import javax.persistence.Entity;
import javax.persistence.Id;

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
@Entity
public class DbDumperPlan {
    @Id
    public String id;
    public String name;
    public Long size;
    public Float cost;

    public DbDumperPlan() {
        this.cost = 0.0F;
    }

    public DbDumperPlan(String id) {
        this();
        this.id = id;
    }

    public DbDumperPlan(String id, String name, Long size) {
        this(id);
        this.name = name;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Float getCost() {
        return cost;
    }

    public void setCost(Float cost) {
        this.cost = cost;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DbDumperPlan that = (DbDumperPlan) o;

        return id.equals(that.id);

    }
}
