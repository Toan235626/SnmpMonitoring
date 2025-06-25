pipeline {
    agent any
    environment {
        SONAR_TOKEN = credentials('sonar-token')
        SONAR_HOST_URL = 'http://localhost:9000'
    }
    stages {
        stage('Build backend') {
            steps { sh 'docker build -t snmp-backend ./backend' }
        }
        stage('Run backend') {
            steps { sh 'docker run -d --network=host --name snmp-backend -e SERVER_PORT=8086 snmp-backend' }
        }
        stage('Build frontend') {
            steps { sh 'docker build -t snmp-frontend --build-arg VITE_API_BASE_URL=http://localhost:8086/api ./frontend/client' }
        }
        stage('SonarQube (backend)') {
            steps {
                dir('backend') {
                    withSonarQubeEnv('MySonarQubeServer') {
                        sh '''
                          mvn clean verify sonar:sonar                           -Dsonar.projectKey=snmp-backend                           -Dsonar.login=$SONAR_TOKEN                           -Dsonar.host.url=$SONAR_HOST_URL
                        '''
                    }
                }
            }
        }
        stage('Cleanup') {
            steps {
                sh 'docker stop snmp-backend || true'
                sh 'docker rm snmp-backend || true'
                sh 'docker image prune -f'
            }
        }
    }
}
```
