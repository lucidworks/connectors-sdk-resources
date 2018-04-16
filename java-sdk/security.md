# Java SDK Connector SSL Support

Please see the main [SSL README](../security.md) for general instructions on how to configur SSL/TLS support for Fusion.

## Java Plugin Client
The information here is specific to running a Java SDK plugin outside of Fusion.

The [plugin-client](plugin-client.md) supports several variations of SSL/TLS auth. The examples below show the relevant Java properties.

**Example with Mutual TLS auth and private key passwords:**

```
-Dcom.lucidworks.apollo.app.hostname=myhost
-Dcom.lucidworks.fusion.tls.trustCertCollection=./sslcerts/ca.crt
-Dcom.lucidworks.fusion.tls.client.certChain=./sslcerts/client.crt
-Dcom.lucidworks.fusion.tls.client.privateKey=./sslcerts/client.pem
-Dcom.lucidworks.fusion.tls.client.privateKeyPassword=password123
-Dcom.lucidworks.fusion.tls.enabled=true</td>
```

**Example without TLS auth and no private key passwords****:**

```
-Dcom.lucidworks.apollo.app.hostname=myhost
-Dcom.lucidworks.fusion.tls.trustCertCollection=./sslcerts/ca.crt
-Dcom.lucidworks.fusion.tls.enabled=true
```