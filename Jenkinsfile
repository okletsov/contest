pipeline {
    agent any
    options {
        datadog(tags: ["foo:bar", "bar:baz"])
    }
    parameters {
        booleanParam(name: 'nightly_run', defaultValue: false, description: 'Whether the was triggered by a nightly cron schedule')
    }
    triggers {
        parameterizedCron('''
                TZ=America/New_York
                30 09 * * * %nightly_run=true
        ''')
    }
    stages {
        stage('build') {
            steps {
                sh 'mvn --version'
                sh 'echo "Hello World"'
                sh '''
                    echo "Multiline shell steps works too"
                    ls -lah
                '''
            }
        }
        stage('Tests Execution') {
            steps {
                sh '''DD_GIT_DEFAULT_BRANCH='master'
                      DD_LOGS_ENABLED=true
                      DD_CIVISIBILITY_ENABLED=true\
                      DD_SITE=us5.datadoghq.com\
                      DD_ENV=ci\
                      DD_SERVICE=contest\
                      MAVEN_OPTS=-javaagent:$HOME/.datadog/dd-java-agent.jar\
                      mvn test -Dtest=com/sapfir/tests/Test_Sandbox#testSimpleLogin '''
            }
        }
    }
    post {
            always {
                echo 'This will always run!'
            }
            success {
                echo 'This will run only if successful'
            }
            failure {
                echo 'This will run only if failed'
            }
            unstable {
                echo 'This will run only if the run was marked as unstable'
            }
            changed {
                echo 'This will run only if the state of the Pipeline has changed'
                echo 'For example, if the Pipeline was previously failing but is now successful'
            }
        }
}