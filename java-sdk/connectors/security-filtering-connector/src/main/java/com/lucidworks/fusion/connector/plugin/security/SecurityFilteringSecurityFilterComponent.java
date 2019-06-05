package com.lucidworks.fusion.connector.plugin.security;

import com.lucidworks.fusion.connector.plugin.api.security.GraphJoinSecurityFilterBuilder;
import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilter;
import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilterComponent;
import com.lucidworks.fusion.connector.plugin.api.security.Subject;

import javax.inject.Inject;

public class SecurityFilteringSecurityFilterComponent implements SecurityFilterComponent {

  private final GraphJoinSecurityFilterBuilder builder;

  @Inject
  public SecurityFilteringSecurityFilterComponent(GraphJoinSecurityFilterBuilder builder) {
    this.builder = builder;
  }


  @Override
  public SecurityFilter buildSecurityFilter(Subject subject) {
    return builder.withAccessControl(subject.getPrincipal()).build();
  }
}
