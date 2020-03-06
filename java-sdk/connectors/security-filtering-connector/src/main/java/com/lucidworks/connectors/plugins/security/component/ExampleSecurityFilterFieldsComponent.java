package com.lucidworks.connectors.plugins.security.component;

import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilterFieldsComponent;

import java.util.ArrayList;
import java.util.List;

public class ExampleSecurityFilterFieldsComponent implements SecurityFilterFieldsComponent {


  @Override
  public List<String> getSecurityFilterFields() {
    List<String> securityFilterFields = new ArrayList<>();
    securityFilterFields.add("AC_SAM_s");
    return securityFilterFields;
  }

}
