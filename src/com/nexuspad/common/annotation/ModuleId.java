/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.annotation;

import com.nexuspad.datamodel.EntryTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wraps the {@code *_MODULE} in {@link com.nexuspad.dataservice.ServiceConstants}, and an
 * {@link com.nexuspad.datamodel.EntryTemplate} together.
 *
 * @author Edmond
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleId {
    /**
     * @return one of the {@code *_MODULE} constants in {@link com.nexuspad.dataservice.ServiceConstants}
     */
    int moduleId();

    /**
     * Defaults to {@link com.nexuspad.datamodel.EntryTemplate#NOT_ASSIGNED}
     *
     * @return should correspond with {@link #moduleId()}
     */
    EntryTemplate template() default EntryTemplate.NOT_ASSIGNED;

    ;
}
