# Fusion gRPC TLS Setup

This guide will set you up so that you have everything you need to set up TLS between your Connectors-rpc server(s) and Connector JVM(s).

## Install OpenSSL binaries for your operating system

gRPC makes TLS more efficient by using native openssl binaries while doing SSL. So you must install OpenSSL for your operating system.

**Ubuntu/Debian:**

`sudo apt-get install openssl`

**CentOS/Redhat/Amazon EC2:**

`sudo yum install openssl`

**Windows:**

* Install [https://slproweb.com/download/Win64OpenSSL-1_1_0g.exe](https://slproweb.com/download/Win64OpenSSL-1_1_0g.exe)
* Add the installed binaries to your path.

## Set up the Certificates

### Linux setup:

Create a new folder, open a terminal, cd to that folder then run this bash script:

```bash
# Changes these CN's to match your hosts in your environment if needed.
SERVER_CN=myhost
CLIENT_CN=myhost # Used when doing mutual TLS

echo Generate CA key:
openssl genrsa -passout pass:1111 -des3 -out ca.key 4096
echo Generate CA certificate:
# Generates ca.crt which is the trustCertCollectionFile
openssl req -passin pass:1111 -new -x509 -days 365 -key ca.key -out ca.crt -subj "/CN=${SERVER_CN}"
echo Generate server key:
openssl genrsa -passout pass:1111 -des3 -out server.key 4096
echo Generate server signing request:
openssl req -passin pass:1111 -new -key server.key -out server.csr -subj "/CN=${SERVER_CN}"
echo Self-signed server certificate:
# Generates server.crt which is the certChainFile for the server
openssl x509 -req -passin pass:1111 -days 365 -in server.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out server.crt
echo Remove passphrase from server key:
openssl rsa -passin pass:1111 -in server.key -out server.key
echo Generate client key
openssl genrsa -passout pass:1111 -des3 -out client.key 4096
echo Generate client signing request:
openssl req -passin pass:1111 -new -key client.key -out client.csr -subj "/CN=${CLIENT_CN}"
echo Self-signed client certificate:
# Generates client.crt which is the clientCertChainFile for the client (need for mutual TLS only)
openssl x509 -passin pass:1111 -req -days 365 -in client.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out client.crt
echo Remove passphrase from client key:
openssl rsa -passin pass:1111 -in client.key -out client.key
echo Converting the private keys to X.509:
# Generates client.pem which is the clientPrivateKeyFile for the Client (needed for mutual TLS only)
openssl pkcs8 -topk8 -nocrypt -in client.key -out client.pem
# Generates server.pem which is the privateKeyFile for the Server
openssl pkcs8 -topk8 -nocrypt -in server.key -out server.pem
```

### Windows Setup:

To the default location: **C:\OpenSSL-Win64**

Create a new directory, cd to that directory in a cmd shell and run this batch:


```bash
@echo off
set OPENSSL_CONF=c:\OpenSSL-Win64\bin\openssl.cfg

# Changes these CN's to match your hosts in your environment if needed.
set SERVER_CN=myhost
set CLIENT_CN=myhost # Used when doing mutual TLS

echo Generate CA key:
openssl genrsa -passout pass:1111 -des3 -out ca.key 4096
echo Generate CA certificate:
REM Generates ca.crt which is the trustCertCollectionFile
openssl req -passin pass:1111 -new -x509 -days 365 -key ca.key -out ca.crt -subj "/CN=%SERVER_CN%"
echo Generate server key:
openssl genrsa -passout pass:1111 -des3 -out server.key 4096
echo Generate server signing request:
openssl req -passin pass:1111 -new -key server.key -out server.csr -subj "/CN=${SERVER_CN}"
echo Self-signed server certificate:
REM Generates server.crt which is the certChainFile for the server
openssl x509 -req -passin pass:1111 -days 365 -in server.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out server.crt
echo Remove passphrase from server key:
openssl rsa -passin pass:1111 -in server.key -out server.key
echo Generate client key
openssl genrsa -passout pass:1111 -des3 -out client.key 4096
echo Generate client signing request:
openssl req -passin pass:1111 -new -key client.key -out client.csr -subj "/CN=%CLIENT_CN%"
echo Self-signed client certificate:
REM Generates client.crt which is the clientCertChainFile for the client (need for mutual TLS only)
openssl x509 -passin pass:1111 -req -days 365 -in client.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out client.crt
echo Remove passphrase from client key:
openssl rsa -passin pass:1111 -in client.key -out client.key
echo Converting the private keys to X.509:
REM Generates client.pem which is the clientPrivateKeyFile for the Client (needed for mutual TLS only)
openssl pkcs8 -topk8 -nocrypt -in client.key -out client.pem
REM Generates server.pem which is the privateKeyFile for the Server
openssl pkcs8 -topk8 -nocrypt -in server.key -out server.pem</td>
```

## Specify connectors-rpc system properties to provide the certs

Now that we have the certs, we set them in the properties.

**Example with Mutual TLS auth and private key passwords:**

```
-Dcom.lucidworks.apollo.app.hostname=myhost
-Dcom.lucidworks.fusion.tls.server.certChain=./sslcerts/server.crt
-Dcom.lucidworks.fusion.tls.server.privateKey=./sslcerts/server.pem
-Dcom.lucidworks.fusion.tls.server.privateKeyPassword=password123
-Dcom.lucidworks.fusion.tls.client.certChain=./sslcerts/client.crt
-Dcom.lucidworks.fusion.tls.requireMutualAuth=false
```

**Example without TLS auth and no private key passwords****:**

```
-Dcom.lucidworks.apollo.app.hostname=myhost
-Dcom.lucidworks.fusion.tls.server.certChain=./sslcerts/server.crt
-Dcom.lucidworks.fusion.tls.server.privateKey=./sslcerts/server.pem
```
