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

