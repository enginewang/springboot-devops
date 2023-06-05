pipeline {
  agent {
    node {
      label 'maven'
    }
  }

    parameters {
        string(name:'TAG_NAME',defaultValue: '',description:'版本号')
    }

    environment {
        DOCKER_CREDENTIAL_ID = 'dockerhub-id'
        GITHUB_CREDENTIAL_ID = 'github-token'
        KUBECONFIG_CREDENTIAL_ID = 'devops-kubeconfig'
        REGISTRY = "https://harbor.engine.wang"
        HARBOR_NAMESPACE = "public"
        HARBOR_CREDENTIAL = credentials('harbor-robot')
        DOCKER_REPO_NAME = 'springboot-devops'
        GITHUB_ACCOUNT = 'enginewang'
        GIT_REPO_NAME = 'springboot-devops'
    }

    stages {
        stage ('checkout scm') {
            steps {
                checkout(scm)
            }
        }

        stage ('unit test') {
            steps {
                container ('maven') {
                    sh 'mvn clean test'
                }
            }
        }
 
        stage ('build & push') {
            steps {
                container ('maven') {
                    sh 'mvn clean package -DskipTests'
                    sh 'docker build -f Dockerfile -t $REGISTRY/$HARBOR_NAMESPACE/$DOCKER_REPO_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
                    withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
                        sh '''echo "$HARBOR_CREDENTIAL_PSW" | docker login $REGISTRY -u "robot$robot" --password-stdin'''
                        sh 'docker push $REGISTRY/$HARBOR_NAMESPACE/$DOCKER_REPO_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
                    }
                }
            }
        }

        stage('push latest'){
           when{
             branch 'master'
           }
           steps{
                container ('maven') {
                  sh 'docker tag $REGISTRY/$HARBOR_NAMESPACE/$DOCKER_REPO_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$HARBOR_NAMESPACE/$DOCKER_REPO_NAME:latest '
                  sh 'docker push $REGISTRY/$HARBOR_NAMESPACE/$DOCKER_REPO_NAME:latest '
                }
           }
        }

        stage('deploy to dev') {
          when{
            branch 'master'
          }
          steps {
            input(id: 'deploy-to-dev', message: 'deploy to dev?')
            container ('maven') {
                withCredentials([
                    kubeconfigFile(
                    credentialsId: env.KUBECONFIG_CREDENTIAL_ID,
                    variable: 'KUBECONFIG')
                    ]) {
                    sh 'envsubst < deploy/dev/devops.yaml | kubectl apply -f -'
                }
            }
          }
        }
        stage('push with tag'){
          when{
            expression{
              return params.TAG_NAME =~ /v.*/
            }
          }
          steps {
              container ('maven') {
                input(id: 'release-image-with-tag', message: 'release image with tag?')
                  withCredentials([usernamePassword(credentialsId: "$GITHUB_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh 'git config --global user.email "kubesphere@yunify.com" '
                    sh 'git config --global user.name "kubesphere" '
                    sh 'git tag -a $TAG_NAME -m "$TAG_NAME" '
                    sh 'git push https://$GIT_PASSWORD@github.com/$GITHUB_ACCOUNT/$GIT_REPO_NAME.git --tags --ipv4'
                  }
                sh 'docker tag $REGISTRY/$HARBOR_NAMESPACE/$DOCKER_REPO_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$HARBOR_NAMESPACE/$DOCKER_REPO_NAME:$TAG_NAME '
                sh 'docker push $REGISTRY/$HARBOR_NAMESPACE/$DOCKER_REPO_NAME:$TAG_NAME '
          }
          }
        }
        stage('deploy to production') {
          when{
            expression{
              return params.TAG_NAME =~ /v.*/
            }
          }
          steps {
            input(id: 'deploy-to-production', message: 'deploy to production?')
            container ('maven') {
                withCredentials([
                    kubeconfigFile(
                    credentialsId: env.KUBECONFIG_CREDENTIAL_ID,
                    variable: 'KUBECONFIG')
                    ]) {
                    sh 'envsubst < deploy/prod/devops.yaml | kubectl apply -f -'
                }
            }
          }
        }
    }
}
