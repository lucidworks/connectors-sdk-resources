package com.lucidworks.connector.plugins.security.component;

import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilterComponent;
import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilterSpec;
import com.lucidworks.fusion.connector.plugin.api.security.spec.FieldsSecurityFilterSpec;
import java.util.ArrayList;

public class ExampleSecurityFilteringComponent implements SecurityFilterComponent {

  @Override
  public SecurityFilterSpec getSpec() {
    return new FieldsSecurityFilterSpec(new ArrayList<String>() {{
      add("AC_SAM_s");
    }});
  }

}
