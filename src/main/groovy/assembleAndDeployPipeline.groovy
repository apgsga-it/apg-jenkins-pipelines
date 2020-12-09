pipeline {
    agent any

    parameters {
        string(name: 'TARGET')
        string(name: 'PARAMETER', description: 'JSON String containing all required info')
    }

    stages {
        // JHE (09.12.2020): Seems not easily possible to use variable in stage name for declarative pipeline : https://issues.jenkins.io/browse/JENKINS-43820
        stage("Assemble and Deploy") {
            steps {
                script {
                    assembleAndDeployPatchFunctions.assembleAndDeploy(params.TARGET, params.PARAMETER)
                }
            }
        }
    }
    post {
        success {
            script {
                println "TODO JHE: implement success post job"
                // commonPatchFunctions.notifyDb(patchConfig,params.STAGE,params.SUCCESS_NOTIFICATION,null)
            }
        }
        unsuccessful {
            script {
                println "TODO JHE: implement fail post job"
                // commonPatchFunctions.notifyDb(patchConfig,params.STAGE,null,params.ERROR_NOTIFICATION)
            }
        }

    }
}