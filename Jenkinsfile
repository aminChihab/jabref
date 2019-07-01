pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Building jabref...'
                sh './gradlew build'
            }
        }
        stage('Test') {
            steps {
                echo 'Running unit tests for ASV'
                sh './gradlew test --tests org.jabref.asv.AsvUnitTest'
            }
        }
    }
}
