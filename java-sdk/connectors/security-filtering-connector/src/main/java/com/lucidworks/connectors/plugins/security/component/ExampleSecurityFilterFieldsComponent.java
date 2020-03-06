package com.lucidworks.connectors.plugins.security.component;

import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilterQueryFieldsComponent;

import java.util.ArrayList;
import java.util.List;

public class ExampleSecurityFilterFieldsComponent implements SecurityFilterQueryFieldsComponent {


  @Override
  public List<String> getSecurityFilterQueryFields() {
    List<String> securityFilterFields = new ArrayList<>();
    securityFilterFields.add("AC_SAM_s");
    return securityFilterFields;
  }

}
