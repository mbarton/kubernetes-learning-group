# Kubernetes Learning Group 02 - Running a common Guardian scala app

1) Build a Docker image

```
lazy val root = (project in file("."))
  .enablePlugins(PlayScala, DockerPlugin)

dockerExposedPorts += 9000
```

```
sbt docker:publishLocal

docker image list | grep kubernetes-play-example
kubernetes-play-example 1.0-SNAPSHOT 4ab07214911c 17 seconds ago 532MB

docker run \
    --env APPLICATION_SECRET=local-dev-testing \
    --expose 9000 \
    kubernetes-play-example:1.0-SNAPSHOT
```