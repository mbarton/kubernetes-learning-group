# Kubernetes Learning Group 03 - EKS

[eksctl](https://github.com/weaveworks/eksctl) is the official way to create and interact with managed Kubernetes clusters.

```
eksctl create cluster --region eu-west-2 --profile developerPlayground
```

Lets see the nodes created:

```
kubectl get nodes
NAME                                           STATUS   ROLES    AGE     VERSION
ip-192-168-1-176.eu-west-2.compute.internal    Ready    <none>   4m23s   v1.16.12-eks-904af05
ip-192-168-95-146.eu-west-2.compute.internal   Ready    <none>   4m27s   v1.16.12-eks-904af05
```

and their backing EC2 instances:

```
aws ec2 describe-instances --profile developerPlayground --region eu-west-2

{
    "Reservations": [
        {
            "Groups": [],
            "Instances": [
                {
                    "AmiLaunchIndex": 0,
                    "ImageId": "ami-0eb74373fe68e96a1",
                    "InstanceId": "i-053ecea1a1900f4cf",
                    "InstanceType": "m5.large",
                    "LaunchTime": "2020-07-17T10:44:06+00:00",
                    "Monitoring": {
                        "State": "disabled"
                    },
                    "Placement": {
                        "AvailabilityZone": "eu-west-2b",
                        "GroupName": "",
                        "Tenancy": "default"
                    },
                    "PrivateDnsName": "ip-192-168-95-146.eu-west-2.compute.internal",
                    "PrivateIpAddress": "192.168.95.146",
                    "ProductCodes": [],
                    "PublicDnsName": "ec2-18-130-204-67.eu-west-2.compute.amazonaws.com",
                    "PublicIpAddress": "18.130.204.67",
                    "State": {
                        "Code": 16,
                        "Name": "running"
                    },

... output elided
```

How can we run commands automatically. eksctl has written IAM authentication configuration for us:

```
cat ~/.kube/config

- name: michael.barton@wonderful-mongoose-1594981717.eu-west-2.eksctl.io
  user:
    exec:
      apiVersion: client.authentication.k8s.io/v1alpha1
      args:
      - token
      - -i
      - wonderful-mongoose-1594981717
      command: aws-iam-authenticator
      env:
      - name: AWS_STS_REGIONAL_ENDPOINTS
        value: regional
      - name: AWS_DEFAULT_REGION
        value: eu-west-2
      - name: AWS_PROFILE
        value: developerPlayground
```

Good stuff. Let's deploy our example app from last time:

```
cd ../02_04-06-20
sbt playGenerateSecret | \
    grep  "Generated new secret" | \
    sed 's/Generated new secret\: /APPLICATION_SECRET="/' | \
    sed 's/$/"/' > secret.env

kubectl create secret generic kubernetes-play-example --from-env-file=./secret.env
rm secret.env

cd ../03_21-07-20
kubectl apply -f deployment.yaml

kubectl get pods --watch

NAME                                       READY   STATUS    RESTARTS   AGE
kubernetes-play-example-64b4b68c56-djr2c   1/1     Running   0          7s
kubernetes-play-example-64b4b68c56-wbjwh   1/1     Running   0          7s
kubernetes-play-example-64b4b68c56-zwk9l   1/1     Running   0          7s

kubectl port-forward deployment/kubernetes-play-example 9000:9000
curl http://localhost:9000
```

This would be much more useful if users could reach it!

```

```