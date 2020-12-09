pipeline {
    agent any

    parameters {
        string(name: 'TARGET')
        string(name: 'PARAMETER', description: 'JSON String containing all required info')
    }

    stages {
        stage("Assemble and Deploy for ${params.TARGET}") {
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