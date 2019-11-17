package com.lucidworks.connector.plugins.fetcher;

import javax.inject.Provider;
import java.net.InetAddress;

public class HostnameProvider implements Provider<String> {

  @Override
  public String get() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (Exception ex) {
      return "no-hostname";
    }
  }
}
