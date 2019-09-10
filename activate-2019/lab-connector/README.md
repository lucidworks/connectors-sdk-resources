# Activate 2019 Lab Connector Project

## Step 1
This step is for setting up the basic build configuration required for building a Fusion connector.

## Step 2
Step 2 defines the basic plugin class, as well as the mandatory configuration class.

### Build the connector plugin

```shell script
./gradlew clean build
./gradlew assemblePlugin
```

Inspect the plugin's content:

```shell script
zipinfo build/libs/lab-connector.zip
```

The results should be similar to the following:

```shell script
Archive:  build/libs/lab-connector.zip
Zip file size: 2513 bytes, number of entries: 4
drwxr-xr-x  2.0 unx        0 b- defN 19-Sep-10 12:53 META-INF/
-rw-r--r--  2.0 unx      245 b- defN 19-Sep-10 12:53 META-INF/MANIFEST.MF
drwxr-xr-x  2.0 unx        0 b- defN 19-Sep-10 12:53 lib/
-rw-r--r--  2.0 unx     2971 b- defN 19-Sep-10 12:53 lib/lab-connector-1.0.0.jar
4 files, 3216 bytes uncompressed, 2067 bytes compressed:  35.7%
```

### Deploy to Fusion

Now that the plugin has been built, and packaged into the required zip format, it can easily be deployed to Fusion.
The Gradle task, `deploy` handles the request details. Note that `$LAB_HOST` is your lab machine's hostname:

```shell script
./gradlew deploy -PfusionApiTarget=$LAB_HOST:8765
```

Now view the schema:

```shell script
curl http://$LAB_HOST:8765/api/v1/connectors-experimental/plugins/lucidworks.labs.lab-connector-plugin
```