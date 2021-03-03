#!groovy
import groovy.json.JsonSlurperClassic

import java.text.SimpleDateFormat

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETERS)
def dateInfo = new SimpleDateFormat("yyyyMMdd_HHmmss_S").format(new Date())
def revisionClonedPath = "${env.GRADLE_USER_HOME_PATH}/patch${paramsAsJson.patchNumber}_${paramsAsJson.target}_${dateInfo}"

pipeline {
	parameters {
		string(name: 'PARAMETERS', description: "JSON parameters")
	}

	agent any

	stages {
		stage("Create local Revision folder") {
			steps {
				script {
					commonPatchFunctions.createFolder(revisionClonedPath)
				}
			}
		}
		stage("Build") {
			steps {
				parallel(
						"db-build": {
							script {
								patchfunctions.patchBuildDbZip(paramsAsJson)
							}
						},
						"java-build": {
							script {
								patchfunctions.patchBuildsConcurrent(paramsAsJson,revisionClonedPath)
							}
						}
				)
			}
		}
	}
	post {
		success {
				script {
					commonPatchFunctions.notifyDb(paramsAsJson.patchNumber,paramsAsJson.target,paramsAsJson.successNotification)
				}
		}
		unsuccessful {
				script {
					commonPatchFunctions.notifyDb(paramsAsJson.patchNumber,paramsAsJson.target,paramsAsJson.errorNotification)
				}
		}
		always {
			script {
				commonPatchFunctions.deleteFolder(revisionClonedPath)
			}
		}

	}
}