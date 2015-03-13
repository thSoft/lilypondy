# Introduction #

A Java Servlet implementation of the LilyPond server is a part of lilypondy. This servlet can be easily deployed to a running Tomcat instance.

# Build #

  * Check out the `lilywaveservlet` project
  * Set the `CATALINA_HOME` environment variable to your tomcat installation folder
  * Run `ant` in the project folder

# Deploy #
  * in `tmp/dist` folder of the lilywave servlet project a `lilywaveservlet.war` file is created. Copy this file to the `webapps` folder of the Tomcat server.
  * This should work out-of-the-box with the default Tomcat installation. `conf/server.xml` should contain a `Host` element like this:

```
<Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="true" xmlValidation="false" xmlNamespaceAware="false">
</Host>
```

# Use #
  * After copying the war to the right place, the `catalina.out` log should show the following:
```
Nov 20, 2009 10:46:11 PM org.apache.catalina.startup.HostConfig deployWAR
INFO: Deploying web application archive lilywaveservlet.war
```
  * This means the servlet is successfully deployed. Now you can access it with `http://yourserver.yourdomain.com/lilywaveservlet/LilyWaveServlet?q={ c' }` assuming that your Tomcat is serving the `yourserver.yourdomain.com` host (which should be the case if the `server.xml` file is configured for the 80 port (using the `Connector` field)

# Configuration #
The default configuration settings are found in `default_settings.properties`. The servlet can be configured using a `settings.properties` file put into the `resources` folder, where you can override these default settings. See that file's comments for descriptions.