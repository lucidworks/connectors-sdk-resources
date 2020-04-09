package com.lucidworks.connector.plugins.security.security;

import com.lucidworks.fusion.connector.plugin.api.security.AclGraphJoinSecurityFilterBuilder;
import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilter;
import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilterComponent;
import com.lucidworks.fusion.connector.plugin.api.security.Subject;

import javax.inject.Inject;

public class SecurityFilteringSecurityFilterComponent implements SecurityFilterComponent {

  private final AclGraphJoinSecurityFilterBuilder builder;

  @Inject
  public SecurityFilteringSecurityFilterComponent(AclGraphJoinSecurityFilterBuilder builder) {
    this.builder = builder;
  }

  @Override
  public SecurityFilter buildSecurityFilter(Subject subject) {
    return builder.withAccessControl(subject.getPrincipal()).build();
  }
}
