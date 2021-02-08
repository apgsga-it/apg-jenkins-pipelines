#!groovy
import groovy.json.JsonSlurperClassic

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETERS)

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
				println "todo : here we build"
				println "paramsAsJson = ${paramsAsJson}"
				/*
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

				 */
			}
		}
		stage("Assemble and Deploy") {
			steps {
				println "todo : here we assemble and deploy"
				/*
				parallel(
						"db-assemble": {
							script {
								assembleAndDeployPatchFunctions.assembleAndDeployDb(paramsAsJson)
							}
						},
						"java-assemble": {
							script {
								assembleAndDeployPatchFunctions.assembleAndDeployJavaService(paramsAsJson)
							}
						}
				)

				 */
			}
		}
	}
	post {
		always {
			script {
				println "to do .... probably send mail"
				//commonPatchFunctions.deleteFolder(revisionClonedPath)
			}
		}
	}
}