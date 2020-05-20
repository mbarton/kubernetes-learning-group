01 - Into and create a deployment
=================================

Accompanying slides: https://docs.google.com/presentation/d/1EM9tb0UHrhpLSh1OPhEmvQarqzr1jG3u_WGS8c_VarE

To build the phpinfo container:

```
docker build -t phpinfo .
```

**Check that Kubernetes in Docker is running**

```
kubectl version
Client Version: version.Info{Major:"1", Minor:"16+", GitVersion:"v1.16.6-beta.0", GitCommit:"e7f962ba86f4ce7033828210ca3556393c377bcc", GitTreeState:"clean", BuildDate:"2020-01-15T08:26:26Z", GoVersion:"go1.13.5", Compiler:"gc", Platform:"darwin/amd64"}
Server Version: version.Info{Major:"1", Minor:"16+", GitVersion:"v1.16.6-beta.0", GitCommit:"e7f962ba86f4ce7033828210ca3556393c377bcc", GitTreeState:"clean", BuildDate:"2020-01-15T08:18:29Z", GoVersion:"go1.13.5", Compiler:"gc", Platform:"linux/amd64"}
```

Pods
----

**What's running by default?**

```bash
kubectl get pods
No resources found in default namespace

kubectl get pods --all-namespaces
NAMESPACE     NAME                                     READY   STATUS    RESTARTS   AGE
docker        compose-78f95d4f8c-dctkl                 1/1     Running   0          6d6h
docker        compose-api-6ffb89dc58-8rc2r             1/1     Running   0          6d6h
kube-system   coredns-5644d7b6d9-cxcdm                 1/1     Running   0          6d7h
kube-system   coredns-5644d7b6d9-f7zsx                 1/1     Running   0          6d7h
kube-system   etcd-docker-desktop                      1/1     Running   0          6d6h
kube-system   kube-apiserver-docker-desktop            1/1     Running   0          6d6h
kube-system   kube-controller-manager-docker-desktop   1/1     Running   0          6d6h
kube-system   kube-proxy-nvfzk                         1/1     Running   0          6d7h
kube-system   kube-scheduler-docker-desktop            1/1     Running   0          6d6h
kube-system   storage-provisioner                      1/1     Running   1          6d6h
kube-system   vpnkit-controller                        1/1     Running   0          6d6h
```

**Run an instance of nginx**

```bash
kubectl create deployment learning-group --image=nginx
kdeployment.apps/learning-group created

kubectl get pods --watch
NAME                              READY   STATUS    RESTARTS   AGE
learning-group-7d565689c7-4q7vm   1/1     Running   0          10s
```

The actual container is run by Docker locally:

```bash
docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS               NAMES
1989c0dd7d85        nginx               "nginx -g 'daemon of…"   50 seconds ago      Up 49 seconds                           k8s_nginx_learning-group-7d565689c7-4q7vm_default_e3055ad4-a05d-4c1b-8f07-b883ff906f78_0
```

**`get` and `describe`**

The API follows a convention where `get` returns a nice summary table and `describe` returns more in depth information:

```bash
kubectl get pods
NAME                              READY   STATUS    RESTARTS   AGE
learning-group-7d565689c7-4q7vm   1/1     Running   0          2m6s

kubectl describe pods
Name:         learning-group-7d565689c7-4q7vm
Namespace:    default
Priority:     0
Node:         docker-desktop/192.168.65.3
Start Time:   Tue, 19 May 2020 16:25:39 +0100
Labels:       app=learning-group
              pod-template-hash=7d565689c7
Annotations:  <none>
Status:       Running
IP:           10.1.0.49
IPs:
  IP:           10.1.0.49
Controlled By:  ReplicaSet/learning-group-7d565689c7
Containers:
  nginx:
    Container ID:   docker://1989c0dd7d8511a3aa2a542fdf7b049fafdaf1b144b3a240d9baf3810e2c0bd2
    Image:          nginx
    Image ID:       docker-pullable://nginx@sha256:30dfa439718a17baafefadf16c5e7c9d0a1cde97b4fd84f63b69e13513be7097
    Port:           <none>
    Host Port:      <none>
    State:          Running
      Started:      Tue, 19 May 2020 16:25:42 +0100
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from default-token-5nb9c (ro)
Conditions:
  Type              Status
  Initialized       True 
  Ready             True 
  ContainersReady   True 
  PodScheduled      True 
Volumes:
  default-token-5nb9c:
    Type:        Secret (a volume populated by a Secret)
    SecretName:  default-token-5nb9c
    Optional:    false
QoS Class:       BestEffort
Node-Selectors:  <none>
Tolerations:     node.kubernetes.io/not-ready:NoExecute for 300s
                 node.kubernetes.io/unreachable:NoExecute for 300s
Events:
  Type    Reason     Age    From                     Message
  ----    ------     ----   ----                     -------
  Normal  Scheduled  2m27s  default-scheduler        Successfully assigned default/learning-group-7d565689c7-4q7vm to docker-desktop
  Normal  Pulling    2m27s  kubelet, docker-desktop  Pulling image "nginx"
  Normal  Pulled     2m25s  kubelet, docker-desktop  Successfully pulled image "nginx"
  Normal  Created    2m25s  kubelet, docker-desktop  Created container nginx
  Normal  Started    2m25s  kubelet, docker-desktop  Started container nginx
```

**Access the API directly**

```bash
kubectl proxy
Starting to serve on 127.0.0.1:8001
```

Browse the API in a browser. It's self describing. Some interesting URLs to look at:

- http://localhost:8001/api/v1
- http://localhost:8001/api/v1/pods (`kubectl describe pods`)

**Delete the deployment**

```bash
kubectl delete deployments learning-group
deployment.apps "learning-group" deleted

kubectl get deployments
No resources found in default namespace.
```

Deployments
-----------

```bash
kubectl apply -f ./01-replicated-deployment.yaml
deployment.apps/phpinfo-deployment created

kubectl get deployments
NAME                 READY   UP-TO-DATE   AVAILABLE   AGE
phpinfo-deployment   3/3     3            3           10s

kubectl get pods
NAME                                  READY   STATUS    RESTARTS   AGE
phpinfo-deployment-5b855b4497-kfg82   1/1     Running   0          20s
phpinfo-deployment-5b855b4497-nwg2r   1/1     Running   0          20s
phpinfo-deployment-5b855b4497-qhmmv   1/1     Running   0          20s

kubectl get replicasets
NAME                            DESIRED   CURRENT   READY   AGE
phpinfo-deployment-5b855b4497   3         3         3       29s

kubectl describe replicasets
Name:           phpinfo-deployment-5b855b4497
Namespace:      default
Selector:       app=phpinfo,pod-template-hash=5b855b4497
Labels:         app=phpinfo
                pod-template-hash=5b855b4497
Annotations:    deployment.kubernetes.io/desired-replicas: 3
                deployment.kubernetes.io/max-replicas: 4
                deployment.kubernetes.io/revision: 1
Controlled By:  Deployment/phpinfo-deployment
Replicas:       3 current / 3 desired
Pods Status:    3 Running / 0 Waiting / 0 Succeeded / 0 Failed
Pod Template:
  Labels:  app=phpinfo
           pod-template-hash=5b855b4497
  Containers:
   phpinfo:
    Image:        mrbbarton/phpinfo:0.0.1
    Port:         80/TCP
    Host Port:    0/TCP
    Environment:  <none>
    Mounts:       <none>
  Volumes:        <none>
Events:
  Type    Reason            Age   From                   Message
  ----    ------            ----  ----                   -------
  Normal  SuccessfulCreate  41s   replicaset-controller  Created pod: phpinfo-deployment-5b855b4497-nwg2r
  Normal  SuccessfulCreate  41s   replicaset-controller  Created pod: phpinfo-deployment-5b855b4497-kfg82
  Normal  SuccessfulCreate  41s   replicaset-controller  Created pod: phpinfo-deployment-5b855b4497-qhmmv
```

Accessing instances
-------------------

**Port forward directly to a pod**

```bash
kubectl get pods
NAME                                  READY   STATUS    RESTARTS   AGE
phpinfo-deployment-5b855b4497-kfg82   1/1     Running   0          2m5s
phpinfo-deployment-5b855b4497-nwg2r   1/1     Running   0          2m5s
phpinfo-deployment-5b855b4497-qhmmv   1/1     Running   0          2m5s

kubectl port-forward phpinfo-deployment-5b855b4497-kfg82 8080:80
Forwarding from 127.0.0.1:8080 -> 80
Forwarding from [::1]:8080 -> 80
```

**Service**

```bash
kubectl apply -f ./02-service.yaml
service/nginx-service configured

kubectl get services
NAME              TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)   AGE
kubernetes        ClusterIP   10.96.0.1      <none>        443/TCP   6d23h
phpinfo-service   ClusterIP   10.99.198.58   <none>        80/TCP    15h

kubectl port-forward services/phpinfo-service 8080:80
```

**Node port**

```bash
apply -f ./02-service.nodePort.yaml
service/phpinfo-service configured

kubectl get services
NAME              TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)        AGE
kubernetes        ClusterIP   10.96.0.1      <none>        443/TCP        6d23h
phpinfo-service   NodePort    10.99.198.58   <none>        80:30007/TCP   16h
```

Scale up and down
-----------------

```bash
kubectl scale --replicas=6 deployment/phpinfo-deployment
deployment.apps/phpinfo-deployment scaled

kubectl get pods
NAME                                  READY   STATUS    RESTARTS   AGE
phpinfo-deployment-5b855b4497-5295d   1/1     Running   0          10s
phpinfo-deployment-5b855b4497-7qfzr   1/1     Running   0          10s
phpinfo-deployment-5b855b4497-kfg82   1/1     Running   0          85s
phpinfo-deployment-5b855b4497-l2rf2   1/1     Running   0          10s
phpinfo-deployment-5b855b4497-nwg2r   1/1     Running   0          85s
phpinfo-deployment-5b855b4497-qhmmv   1/1     Running   0          85s

kubectl scale --replicas=3 deployment/phpinfo-deployment
deployment.apps/phpinfo-deployment scaled

kubectl get pods --watch
NAME                                  READY   STATUS        RESTARTS   AGE
phpinfo-deployment-5b855b4497-5295d   0/1     Terminating   0          29s
phpinfo-deployment-5b855b4497-7qfzr   0/1     Terminating   0          29s
phpinfo-deployment-5b855b4497-kfg82   1/1     Running       0          104s
phpinfo-deployment-5b855b4497-l2rf2   0/1     Terminating   0          29s
phpinfo-deployment-5b855b4497-nwg2r   1/1     Running       0          104s
phpinfo-deployment-5b855b4497-qhmmv   1/1     Running       0          104s
```