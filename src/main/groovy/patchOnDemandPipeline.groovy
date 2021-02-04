#!groovy
import groovy.json.JsonSlurperClassic

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETERS)
def revisionClonedPath = "/var/jenkins/gradle/home/patch${paramsAsJson.patchNumber}_${paramsAsJson.target}"

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
								patchfunctions.patchBuildsConcurrent(paramsAsJson)
							}
						}
				)
			}
		}
		stage("Assemble and Deploy") {
			steps {
				println "HERE WILL THE ASSEMBLE AND DEPLOY RUN"
			}
		}
		stage("Install") {
			steps {
				println "HERE WILL THE INSTALL RUN"
			}
		}
	}
	post {
		success {
			script {
				println "TODO JHE"
			}
		}
		unsuccessful {
			script {
				println "TODO JHE"
			}
		}
		always {
			script {
				commonPatchFunctions.deleteFolder(revisionClonedPath)
			}
		}

	}
}