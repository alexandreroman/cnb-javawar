# Building a Java WAR container image with Cloud-Native Buildpacks

This project shows how to build a Java WAR container image with
[Cloud-Native Buildpacks](https://buildpacks.io) (CNB) and
[Paketo Buildpacks](https://paketo.io).

You don't need to write a `Dockerfile` anymore: using CNB you get
secured up-to-date container images out of your source code.

You don't need to care about the runtime environment (JVM, JavaEE server)
or optimizing the container (such as Java memory settings):
CNB will automatically provision dependencies and configure your container.

## How to use it?

[Download and install the `pack` CLI](https://github.com/buildpacks/pack/releases).
You'll need a Docker daemon running to build container images.

Use the Paketo builder:
```bash
$ pack set-default-builder gcr.io/paketo-buildpacks/builder:base
```

You're now ready to use CNB with Paketo.

Run this command to build a container image:
```bash
$ pack build myorg/cnb-javawar
...
Successfully built image myorg/cnb-javawar
```

A lot of dependencies are downloaded in the first run.
These dependencies will be cached so that next runs are faster.

After a couple of minutes, your container image will be published 
to your local Docker daemon:
```bash
$ docker image ls
REPOSITORY                             TAG                  IMAGE ID    
myorg/cnb-javawar                      latest               b1a0d242e5ec
```

You can run this container right away:
```bash
$ docker run --rm -p 8080:8080/tcp myorg/cnb-javawar
Container memory limit unset. Configuring JVM for 1G container.
Calculated JVM Memory Configuration: -XX:MaxDirectMemorySize=10M -XX:MaxMetaspaceSize=69553K -XX:ReservedCodeCacheSize=240M -Xss1M -Xmx467022K (Head Room: 0%, Loaded Class Count: 9866, Thread Count: 250, Total Memory: 1073741824)
NOTE: Picked up JDK_JAVA_OPTIONS:  --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED
[CONTAINER] org.apache.coyote.http11.Http11NioProtocol         INFO    Initializing ProtocolHandler ["http-nio-8080"]
[CONTAINER] org.apache.catalina.startup.Catalina               INFO    Server initialization in [1,520] milliseconds
[CONTAINER] org.apache.catalina.core.StandardService           INFO    Starting service [Catalina]
[CONTAINER] org.apache.catalina.core.StandardEngine            INFO    Starting Servlet engine: [Apache Tomcat/9.0.34]
[CONTAINER] org.apache.catalina.startup.HostConfig             INFO    Deploying web application directory [/layers/paketo-buildpacks_apache-tomcat/catalina-base/webapps/ROOT]
[CONTAINER] org.apache.jasper.servlet.TldScanner               INFO    At least one JAR was scanned for TLDs yet contained no TLDs. Enable debug logging for this logger for a complete list of JARs that were scanned but no TLDs were found in them. Skipping unneeded JARs during scanning can improve startup time and JSP compilation time.
[CONTAINER] org.apache.catalina.startup.HostConfig             INFO    Deployment of web application directory [/layers/paketo-buildpacks_apache-tomcat/catalina-base/webapps/ROOT] has finished in [2,649] ms
[CONTAINER] org.apache.coyote.http11.Http11NioProtocol         INFO    Starting ProtocolHandler ["http-nio-8080"]
[CONTAINER] org.apache.catalina.startup.Catalina               INFO    Server startup in [2,836] milliseconds
```

## Deploying to Kubernetes

Use these Kubernetes descriptors to deploy this app to your cluster:
```bash
$ kubectl apply -f k8s
```

This app will be deployed to namespace `cnb-javawar`:
```bash
kubectl -n cnb-javawar get pod,deployment,svc
NAME                       READY   STATUS    RESTARTS   AGE
pod/app-7c7957cb94-lpmk8   1/1     Running   0          35m

NAME                        READY   UP-TO-DATE   AVAILABLE   AGE
deployment.extensions/app   1/1     1            1           35m

NAME          TYPE           CLUSTER-IP      EXTERNAL-IP      PORT(S)        AGE
service/app   LoadBalancer   10.100.200.35   35.187.115.254   80:30355/TCP   35m
```

## Contribute

Contributions are always welcome!

Feel free to open issues & send PR.

## License

Copyright &copy; 2020 [VMware, Inc. or its affiliates](https://vmware.com).

This project is licensed under the [Apache Software License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
