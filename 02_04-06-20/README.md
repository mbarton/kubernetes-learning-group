# Kubernetes Learning Group 02 - Running a common Guardian scala app

### 1 - Build a Docker image

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

### 2 - Save the application secret in the cluster

```
sbt playGenerateSecret | grep  "Generated new secret" | sed 's/Generated new secret\: /APPLICATION_SECRET="/' | sed 's/$/"/' > secret.env
kubectl create secret generic kubernetes-play-example --from-env-file=./secret.env
rm secret.env
```

### 3 - Start the deployment

```
kubectl apply -f deployment.yaml

kubectl get pods
NAME                                       READY   STATUS    RESTARTS   AGE
kubernetes-play-example-64b4b68c56-bdmzl   1/1     Running   0          90s
kubernetes-play-example-64b4b68c56-dsr8v   1/1     Running   0          88s
kubernetes-play-example-64b4b68c56-k87h9   1/1     Running   0          91s

curl http://localhost:30009


<!DOCTYPE html>
<html lang="en">
    <head>

        <title>Welcome to Play</title>
        <link rel="stylesheet" media="screen" href="/assets/stylesheets/main.css">
        <link rel="shortcut icon" type="image/png" href="/assets/images/favicon.png">

    </head>
    <body>


  <h1>Welcome to Play!</h1>


      <script src="/assets/javascripts/main.js" type="text/javascript"></script>
    </body>
</html>
```