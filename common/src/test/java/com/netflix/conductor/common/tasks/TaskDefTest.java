/*
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.conductor.common.tasks;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.netflix.conductor.common.metadata.tasks.TaskDef;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Viren
 *
 */
public class TaskDefTest {

    private Validator validator;

    @Before
    public void setup(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

	@Test
	public void test() {
		String name = "test1";
		String description = "desc";
		int retryCount = 10;
		int timeout = 100;
		TaskDef def = new TaskDef(name, description, retryCount, timeout);
		assertEquals(36_00, def.getResponseTimeoutSeconds());
		assertEquals(name, def.getName());
		assertEquals(description, def.getDescription());
		assertEquals(retryCount, def.getRetryCount());
		assertEquals(timeout, def.getTimeoutSeconds());
	}

	@Test
	public void testTaskDef() {
	    TaskDef taskDef = new TaskDef();
	    taskDef.setName("task1");
	    taskDef.setRetryCount(-1);
	    taskDef.setTimeoutSeconds(1000);
	    taskDef.setResponseTimeoutSeconds(1001);

        Set<ConstraintViolation<Object>> result = validator.validate(taskDef);
        assertEquals(3, result.size());

        List<String> validationErrors = new ArrayList<>();
        result.forEach(e -> validationErrors.add(e.getMessage()));

        assertTrue(validationErrors.contains("TaskDef: task1 responseTimeoutSeconds: 1001 must be less than timeoutSeconds: 1000"));
        assertTrue(validationErrors.contains("TaskDef retryCount: 0 must be >= 0"));
        assertTrue(validationErrors.contains("ownerEmail cannot be empty"));
    }

    @Test
    public void testTaskDefNameAndOwnerNotSet() {
        TaskDef taskDef = new TaskDef();
        taskDef.setRetryCount(-1);
        taskDef.setTimeoutSeconds(1000);
        taskDef.setResponseTimeoutSeconds(1);

        Set<ConstraintViolation<Object>> result = validator.validate(taskDef);
        assertEquals(3, result.size());

        List<String> validationErrors = new ArrayList<>();
        result.forEach(e -> validationErrors.add(e.getMessage()));

        assertTrue(validationErrors.contains("TaskDef retryCount: 0 must be >= 0"));
        assertTrue(validationErrors.contains("TaskDef name cannot be null or empty"));
        assertTrue(validationErrors.contains("ownerEmail cannot be empty"));
    }

    @Test
    public void testTaskDefInvalidEmail() {
        TaskDef taskDef = new TaskDef();
        taskDef.setName("test-task");
        taskDef.setRetryCount(1);
        taskDef.setTimeoutSeconds(1000);
        taskDef.setResponseTimeoutSeconds(1);
        taskDef.setOwnerEmail("owner");

        Set<ConstraintViolation<Object>> result = validator.validate(taskDef);
        assertEquals(1, result.size());

        List<String> validationErrors = new ArrayList<>();
        result.forEach(e -> validationErrors.add(e.getMessage()));

        assertTrue(validationErrors.contains("ownerEmail should be valid email address"));
    }

    @Test
    public void testTaskDefValidEmail() {
        TaskDef taskDef = new TaskDef();
        taskDef.setName("test-task");
        taskDef.setRetryCount(1);
        taskDef.setTimeoutSeconds(1000);
        taskDef.setResponseTimeoutSeconds(1);
        taskDef.setOwnerEmail("owner@test.com");

        Set<ConstraintViolation<Object>> result = validator.validate(taskDef);
        assertEquals(0, result.size());
    }

    @Test
    public void testTaskDefValidInputOutput() {
        final Map<String, Object> name = new HashMap<>();
		name.put("type", "string");
		name.put("minLength", 1);
		final Map<String, Object> properties = Collections.singletonMap("name", name);
		final Map<String, Object> definition = Collections.singletonMap("properties", properties);
        TaskDef taskDef = new TaskDef();
        taskDef.setName("test-task");
        taskDef.setRetryCount(1);
        taskDef.setTimeoutSeconds(1000);
        taskDef.setResponseTimeoutSeconds(1);
        taskDef.setOwnerEmail("owner@test.com");
		taskDef.setInputDefinition(definition);
		taskDef.setOutputDefinition(definition);

        Set<ConstraintViolation<Object>> result = validator.validate(taskDef);
        assertEquals(0, result.size());
    }

    @Test
    public void testTaskDefInvalidInputOutput() {
        final Map<String, Object> definition = new HashMap<>();
		definition.put("title", "invalid");
		definition.put("type", "unknown type");
        TaskDef taskDef = new TaskDef();
        taskDef.setName("test-task");
        taskDef.setRetryCount(1);
        taskDef.setTimeoutSeconds(1000);
        taskDef.setResponseTimeoutSeconds(1);
        taskDef.setOwnerEmail("owner@test.com");
		taskDef.setInputDefinition(definition);
		taskDef.setOutputDefinition(definition);

        Set<ConstraintViolation<Object>> result = validator.validate(taskDef);
        assertEquals(6, result.size());

        final List<String> validationErrors = new ArrayList<>();
        result.forEach(e -> validationErrors.add(e.getMessage()));
        assertTrue(validationErrors.contains("#: expected type: Boolean, found: JSONObject"));
        assertTrue(validationErrors.contains("#/type: unknown type is not a valid enum value"));
        assertTrue(validationErrors.contains("#/type: expected type: JSONArray, found: String"));
    }
}
