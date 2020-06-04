# Kubernetes Learning Group 02 - Running a common Guardian scala app

### 1 - Build a Docker image

```
lazy val root = (project in file("."))
  .enablePlugins(PlayScala, DockerPlugin)

dockerExposedPorts += 9000
```

`dockerExposedPorts` writes an `EXPOSE` command to the Dockerfile. From [the docs](https://docs.docker.com/engine/reference/builder/):

> The EXPOSE instruction does not actually publish the port. It functions as a type of documentation between the person who builds the image and the person who runs the container, about which ports are intended to be published

```
sbt docker:publishLocal

docker image list | grep kubernetes-play-example
kubernetes-play-example 1.0-SNAPSHOT 4ab07214911c 17 seconds ago 532MB

docker run \
    --env APPLICATION_SECRET=local-dev-testing \
    -p 9000:9000 \
    kubernetes-play-example:1.0-SNAPSHOT
```

### 2 - Save the application secret in the cluster

```
sbt playGenerateSecret | \
    grep  "Generated new secret" | \
    sed 's/Generated new secret\: /APPLICATION_SECRET="/' | \
    sed 's/$/"/' > secret.env

kubectl create secret generic kubernetes-play-example --from-env-file=./secret.env
rm secret.env
```

There are [various different ways of creating secrets](https://kubernetes.io/docs/concepts/configuration/secret/).
I've used an env file to make it easy to test locally by [passing the file to Docker run](https://docs.docker.com/engine/reference/commandline/run/#set-environment-variables--e---env---env-file).

Secrets are not [encrypted at rest in the default configuration](https://kubernetes.io/docs/concepts/configuration/secret/#security-properties)
which is often required at the level of GDPR compliance we target. In practical terms, this means the default configuration
is providing something akin to the "NoEcho" protection on Cloudformation Parameters, a simple protection against leaving the
secret around in logs and on developer machines.

That said, Google Kubernetes Engine encrypts secrets at rest by default (and [is customisable](https://cloud.google.com/kubernetes-engine/docs/how-to/encrypting-secrets)).
AWS EKS also encrypts with managed keys and supports [custom keys since March 5th 2020](https://aws.amazon.com/about-aws/whats-new/2020/03/amazon-eks-adds-envelope-encryption-for-secrets-with-aws-kms/).


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

### 4 - Deploy a new version

*These changes are available by checking out the `02-set-image` branch*

Change `index.scala.html`:

```
@()

@main("Welcome to Kubernetes") {
  <h1>Welcome to Kubernetes!</h1>
}
```

Change the version in `build.sbt`:

```
version := "1.1-SNAPSHOT"
```

Roll it out!

```
kubectl set image deployments/kubernetes-play-example kubernetes-play-example=mrbbarton/kubernetes-play-example:1.1-SNAPSHOT
```

### 5 - Set a healthcheck

*The changes to the app are available by checking out the `02-healthcheck` branch*

```yaml
livenessProbe:
    httpGet:
      path: /healthcheck
      port: 9000
    initialDelaySeconds: 10
    periodSeconds: 3
```

Kubernetes has a number of [probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
for various different situations:

- `livenessProbe`: is the app up and responsive (ie should it be available through the service or load balancer)
- `startupProbe`: has the app started yet? Useful for slow start or if you can't combine this with the normal healthcheck
- `readinessProbe`: as with the `livenessProbe` but the pod won't be killed if it fails, just taken out of service   