pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Building jabref...'
                sh 'gradle build'
            }
        }
        stage('Test') {
            steps {
                echo 'Running unit tests for ASV'
                sh 'gradle test --tests org.jabref.asv.AsvUnitTest'
            }
        }
    }
}
