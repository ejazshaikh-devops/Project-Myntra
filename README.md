# Project-Myntra (Jenkins, Sonarqube, ArgoCD)
<h1>Creating Myntra replica project</h1>
  <br>
<h3> 1. Create Ec2 instance first (type: c7i-flex.large 
  OS : Ubuntu) </h3>
<h3>2. Dependency : (install in EC2)</h3>
<h3>Fork This Repo Put in Pipeline Code & ArgoCD : https://github.com/ejazshaikh-devops/Project-Myntra-Clone.git </h3>
<br>
<h3>Install NodeJS-16</h3>

          curl -sL https://deb.nodesource.com/setup_16.x | sudo bash -
          sudo apt install nodejs -y

<h3>Note: By default, Jenkins will not be accessible to the external world due to the inbound traffic restriction by AWS. Open port 8080 in the inbound traffic rules as show below.</h3>


<h3>Install Docker </h3>

    # Add Docker's official GPG key:
    sudo apt update
    sudo apt install ca-certificates curl
    sudo install -m 0755 -d /etc/apt/keyrings
    sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
    sudo chmod a+r /etc/apt/keyrings/docker.asc

    # Add the repository to Apt sources:
    sudo tee /etc/apt/sources.list.d/docker.sources <<EOF
    Types: deb
    URIs: https://download.docker.com/linux/ubuntu
    Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
    Components: stable
    Architectures: $(dpkg --print-architecture)
    Signed-By: /etc/apt/keyrings/docker.asc
    EOF
    sudo apt update

    sudo apt install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

  <br>
   
    chmod 777 /var/run/docker.sock

<h3>Install Jenkins </h3>

    sudo apt update
    sudo apt install fontconfig openjdk-21-jre
    java -version

<br>

    sudo wget -O /etc/apt/keyrings/jenkins-keyring.asc \
    https://pkg.jenkins.io/debian-stable/jenkins.io-2026.key
    echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc]" \
    https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
    /etc/apt/sources.list.d/jenkins.list > /dev/null
    sudo apt update
    sudo apt install jenkins

<h3>Install trivy </h3>
Check the latest version of Trivy from the GitHub repository and assign it to a variable:

    TRIVY_VERSION=$(curl -s "https://api.github.com/repos/aquasecurity/trivy/releases/latest" | grep -Po '"tag_name": "v\K[0-9.]+')

Download Trivy archive file:

    wget -qO trivy.tar.gz https://github.com/aquasecurity/trivy/releases/latest/download/trivy_${TRIVY_VERSION}_Linux-64bit.tar.gz

Extract executable to /usr/local/bin directory:

    sudo tar xf trivy.tar.gz -C /usr/local/bin trivy

Here's how you can check the Trivy version:

    trivy --version

Remove unneeded archive file:

    rm -rf trivy.tar.gz

### Login to Jenkins using the below URL:

`http://<ec2-instance-public-ip-address>:8080`

   - Edit the inbound traffic rule to only allow custom TCP port `8080`

After you login to Jenkins, 
      - Run the command to copy the Jenkins Admin Password - `sudo cat /var/lib/jenkins/secrets/initialAdminPassword`
      - Enter the Administrator password
      
<img width="1291" src="https://user-images.githubusercontent.com/43399466/215959008-3ebca431-1f14-4d81-9f12-6bb232bfbee3.png">

Click on Install suggested plugins

Wait for the Jenkins to Install suggested plugins

Jenkins Installation is Successful. You can now starting using the Jenkins 

<img width="990" src="https://user-images.githubusercontent.com/43399466/215961440-3f13f82b-61a2-4117-88bc-0da265a67fa7.png">

<h4> Go to settings -> Plugins -> Avaliable Plugins (Add below Plugins) </h4>

1. CloudBees Docker Build and Publish plugin
2. Docker Pipeline
3. Eclipse Temurin installer Plugin
4. Pipeline Stage View
5. SonarQube Scanner
6. Yet Another Docker Plugin
7. NodeJS Plugin
   
Restart Jenkins

<h4> Go to settings -> Tools </h4>
scroll
<h4> JDK installations -> Add JDk -> Name (This name will be recoil in pipeline tool section) -> Install automatically -> Add installer (Install from adoptium.net) -select-> jdk-17.0.19+10) </h4>
scroll
<h4> SonarQube Scanner installations -> Add SonarQube Scanne -> Name -> Install automatically (only)
 </h4>
scroll
<h4> NodeJS installations -> Add NodeJS -> Name -> Install automatically -> Install from nodejs.org -select-> NodeJS 16.0.0 (leave other section as it is) <h4>
scroll
<h4> Docker installations -> Add Docker -> Name -> Install automatically </h4>

<h3> Install SonarQube & Run its container </h3>
Defination : SonarQube is a code quality and security analysis platform used by developers and DevOps teams to automatically inspect source code for:

1. Bugs
2. Security vulnerabilities
3. Code smells
4. Duplicate code
5. Test coverage
6. Maintainability issues

Think of it as a quality inspector for your code before it goes to production.

Go to instance and run
Sonarqube container :

    docker run -d --name sonar -p 9000:9000 sonarqube:lts-community

If Sonarqube Container stopped exit code (0) then do 
    
    docker ps -a 

Take the Container Id 

    docker restart (container id) 

Make sure 9000 port is enabled in your SG

### Login to SonarQube using the below URL:

`http://<ec2-instance-public-ip-address>:9000` 
   
   - Edit the inbound traffic rule to only allow custom TCP port `9000`

Fahhhhh!! Now you can access the `SonarQube Server` on `http://<ec2-instance-public-ip-address>:9000` 
   
![Screenshot ](https://user-images.githubusercontent.com/129657174/230658262-0a0c8d3a-312d-4423-84e5-a7a1efa9fc68.png)


Login using username: admin, Passsword: admin and Change the password
   
<h4>Scroll right now at the top right corner click profile icon -> Administration -> (click) My Account -> (click) Security -> Generate Tokens -> Name (anyname needed to recoil in piprline) -> Type (User Token) -> (click) Generate </h4>
<br>

New token will appear copy it (keep it we need this in configration section) 
<br>

Final step add webhook :
<h4> Look up and click Administration  -> (click) Configuration -> (click) Webhooks -> Right Corner Create -> Name -> paste this : http://(public-ip of instance):8080/sonarqube-webhook -> create (This will attach with Jenkins server) </h4>
Now all steps completed 
<h3>Back to Jenkins Now </h3>
<h4>Go to settings -> Credentials </h4>

1. We need to add token of sonarqube : Add Credentials -> Secret text -> ID (means recoil name which will show in config section give any name) -> Copy paste the token which we saved -> Description -> Create 
2. Now lets add Docker : Add Credentials -> Docker Registory Auth -> Email of Dockerhub -> Username -> Password -> ID ( Name which will be Recoil) -> Discription -> Create
3. Now add GitHub ID : Add Credentials -> Username with Password -> Username (Put your github id name) -> Password (Generate a token of github and paste here) -> ID (same work as others) -> Create

<img width="1024" height="666" alt="Screenshot 2026-06-12 at 2 28 04 AM" src="https://github.com/user-attachments/assets/fc83512a-d8d6-4232-be11-05be10cb4464" />

<h3>Final setp before building pipeline </h3>
Go to settings -> System -Scroll-> SonarQube servers -> (check mark) Environment variables -> Name ( this will need in pipeline) -> Server URL (Put this : http://(Public Ip of Instance):9000) Now this will connecet the Jenkins to sonarqube -> Server authentication token (Click it and created token name will show select that) -> Apply & Save 

<H2>All set now lets build pipeline </H2>

<h4> New Item -> Enter an item name -> pipeline -> ok </h4>
Now paste this code : 

Change these lines with your values in code before building : 
1. Name which we gave in sonarqube server section
2. Token name of sonar which we gave in credentials section
3. name of docker credential which we gave
4. name of git credential
5. Dont remove '' indentation 

```groovy
pipeline {
    agent any

    tools {
        jdk 'jdk17' //this name we gave in tools section
        nodejs 'node16' //this one as well put those name here 
    }

    environment {
        SCANNER_HOME          = tool 'sonar-scanner' //same we gave the name in tool section put that name
        DOCKER_IMAGE          = 'myntraa' 
        DOCKER_REGISTRY       = 'username' //DockerHubs ID
        DOCKER_CREDENTIALS_ID = 'docker-cred' //Gave that name which we gave in credentails section 
        MANIFEST_FILE         = 'k8s/deployment.yml'
        GIT_REPO_NAME         = 'Project-Myntra-Clone'
        GIT_USER_NAME         = 'username'
        GIT_EMAIL             = 'username@gmail.com'
    }

    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout Code') {
            steps {
                git branch: 'main', url: "https://github.com/${env.GIT_USER_NAME}/${env.GIT_REPO_NAME}.git"
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('Name which we gave in sonarqube server section') {
                    sh """
                        ${SCANNER_HOME}/bin/sonar-scanner \
                        -Dsonar.projectName=Myntra \
                        -Dsonar.projectKey=Myntra
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                waitForQualityGate abortPipeline: false, credentialsId: 'Token name of sonar which we gave in credentials section'
            }
        }

        stage('Install Dependencies') {
            steps {
                sh 'npm install'
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                script {
                    def imageTag = "${DOCKER_IMAGE}:${BUILD_NUMBER}"
                    def registryImageTag = "${DOCKER_REGISTRY}/${imageTag}"

                    sh "docker build -t ${imageTag} ."

                    withDockerRegistry(credentialsId: DOCKER_CREDENTIALS_ID, toolName: 'name of docker credential which we gave') {
                        sh """
                            docker tag ${imageTag} ${registryImageTag}
                            docker push ${registryImageTag}
                        """
                    }
                }
            }
        }

        stage('Update Manifest and Push to GitHub') {
            steps {
                script {
                    def newImage = "${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${BUILD_NUMBER}"
                    withCredentials([usernamePassword(credentialsId: 'name of git credential', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
                        sh """
                            git config user.email "${GIT_EMAIL}"
                            git config user.name "${GIT_USER_NAME}"
                            sed -i 's|image: .*|image: ${newImage}|g' ${MANIFEST_FILE}
                            git add ${MANIFEST_FILE}
                            git commit -m "Update image to ${BUILD_NUMBER}" || echo "No changes"
                            git push https://${GIT_USER}:${GIT_PASS}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME}.git HEAD:main
                        """
                    }
                }
            }
        }
    }
}

```

When Build Success Move to next step 

<h2> Create EKS Cluster </h2>

**1:Install eksctl CLI tool for creating EKS Clusters on AWS**

```` 
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
````

````
sudo mv /tmp/eksctl /usr/local/bin
```` 

```` 
eksctl version
````

**2:Install kubectl**

````
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
````
````
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
````
````
chmod +x kubectl
mkdir -p ~/.local/bin
mv ./kubectl ~/.local/bin/kubectl
````
````
kubectl version --client
````

**3:Install AWS CLI on Ubuntu**
````
sudo apt install unzip -y
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
````


**4:Configure AWS CLI**
````
aws configure
````

**5:Create Amazon EKS cluster using eksctl**
````
eksctl create cluster --name eks-oncdecb31 --region eu-north-1 --version 1.32 --nodegroup-name linux-nodes --node-type c7i-flex.large --nodes 1
````
**6: Log In Into EKS cluster**
````
aws eks update-kubeconfig --name eks-oncdecb31
````

<h2> After this Now Setup ArgoCD </h2> 
Defination : Argo CD is a GitOps Continuous Delivery (CD) tool for Kubernetes.

Its job is to keep your Kubernetes cluster synchronized with the configuration stored in Git.

Instead of manually running:

kubectl apply -f deployment.yml

ArgoCD automatically watches your Git repository and deploys changes to Kubernetes whenever the manifests change.

### Configure ArgoCD


Create a namespace:

```bash
kubectl create namespace argocd
```

Apply official ArgoCD installation:

```bash
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

Wait 2–3 minutes:

```bash
kubectl get pods -n argocd
```

✅ All pods should be in `Running` state.

---

###  Patch `argocd-server` service to use LoadBalancer

```bash
kubectl patch svc argocd-server -n argocd \
  -p '{"spec": {"type": "LoadBalancer"}}'
```

---

### 🔍 Step 3: Get LoadBalancer URL

Run:

```bash
kubectl get svc argocd-server -n argocd
```

### 🌐  Access ArgoCD UI

Open in browser:

```
http://loadbalancer-link
```
Note: Argo CD is not be able to access normally take the endpoint of loadBalancer:80 access in browser it will show warning click on advance and click the endpoint below to access Argo CD 
---

### 🔐 Get ArgoCD admin password

```bash
kubectl get secret argocd-initial-admin-secret -n argocd \
  -o jsonpath="{.data.password}" | base64 -d
```

✅ Login with:

* **Username**: `admin`
* **Password**: (above decoded)

<h3> After Login Click on CREATE APPLICATION </h3>

<h4>General -> Application Name : Give any Name -> Project Name : Default -> Sync Policy : Automatic<h4> 
  
<h4> Source -> Repository URL : Give URL of git repo which has the k8s deployment.yml file (sir's repo fork in our ur account)<h4>
<h4> Path : Gave path where is it present like k8s/ means in that repo there is k8s folder the deployment is present inside it <h4>
<h4> Destination -> Cluster URL : ArgoCD auto detect it coz master node is also present in that same instance thats why k8s and ArgoCD installed in same place -> Namespace : Give the namespace in which ArgoCD is created -scroll up-> Look up left Corner (click) Creater <h4></h4>

<img width="1024" height="666" alt="Screenshot 2026-06-12 at 3 53 22 AM" src="https://github.com/user-attachments/assets/2688fc04-8b20-42d6-834e-5fe7bfcad34d" />


<img width="1024" height="666" alt="Screenshot 2026-06-11 at 10 24 35 PM" src="https://github.com/user-attachments/assets/daa24d18-2e7e-43bb-9ea1-cfb807b105ce" />


<img width="1024" height="666" alt="Screenshot 2026-06-11 at 10 31 20 PM" src="https://github.com/user-attachments/assets/aa770d27-e78a-4f91-b790-5e27b7f7bb28" />

<h2> After this ArgoCD will start working </h2>


1. Go to Instance get svc : myntra-service take the endpoint:80 and access it
2. When ever manifest file (deployment.yaml) get updated it will update the k8s cluster with new image which got updated
3. without running anything it will automaticall do this
4. No need of manually work
5. Also Do :
6. Delete EKS Cluster

````
eksctl delete cluster --name eks-oncdecb31 --region eu-north-1

````








   
   
   


    


 
