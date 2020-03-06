package com.lucidworks.fusion.connector.plugin.validation;

import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilter;
import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilterQueryFieldsComponent;
import com.lucidworks.fusion.connector.plugin.api.security.Subject;

import java.util.ArrayList;
import java.util.List;

public class ExampleSecurityFilterFieldsComponent implements SecurityFilterQueryFieldsComponent {


  @Override
  public List<String> getSecurityFilterQueryFields() {
    List<String> securityFilterFields = new ArrayList<>();
    securityFilterFields.add("AC_SAM_s");
    return securityFilterFields;
  }

  @Override
  public SecurityFilter buildSecurityFilter(Subject subject) {
    throw new UnsupportedOperationException("buildSecurityFilter is unsupported");
  }
}
