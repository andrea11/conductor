package com.netflix.conductor.contribs.metrics;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Spectator;

public class PrometheusMetricsModuleTest {
  @Before
  public void setUp() {
    Guice.createInjector(new PrometheusMetricsModule()).injectMembers(this);
  }

  @Test
  public void testCollector() throws IllegalAccessException {
    final Optional<Field> registries = Arrays.stream(Spectator.globalRegistry().getClass().getDeclaredFields())
        .filter(f -> f.getName().equals("registries")).findFirst();
    Assert.assertTrue(registries.isPresent());
    registries.get().setAccessible(true);
    Assert.assertEquals(1, ((List<Meter>)registries.get().get(Spectator.globalRegistry())).size());
  }
}
