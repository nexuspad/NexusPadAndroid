/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.dataservice.ServiceConstants;

/**
 * Wraps the {@code *_MODULE} in {@link ServiceConstants}, and an
 * {@link EntryTemplate} together.
 * 
 * @author Edmond
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleId {
    /**
     * @return one of the {@code *_MODULE} constants in {@link ServiceConstants}
     */
    int moduleId();

    /**
     * Defaults to {@link EntryTemplate#NOT_ASSIGNED}
     * 
     * @return should correspond with {@link #moduleId()}
     * 
     */
    EntryTemplate template() default EntryTemplate.NOT_ASSIGNED;
}
