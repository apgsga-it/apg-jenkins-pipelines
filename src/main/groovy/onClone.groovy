#!groovy
import groovy.json.JsonSlurperClassic

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETERS)
def src = paramsAsJson.src
def target = paramsAsJson.target

pipeline {
	parameters {
		string(name: 'PARAMETERS', description: "JSON parameters")
	}

	agent any

	stages {
		stage("Pre-process verification") {
			steps {
				script {
					if(target.equalsIgnoreCase("chpi211")) {
						error("Target cannot be production environment !!")
					}

					// We consider chqi211 same as chpi211 (from source point of view only)
					if(src.equalsIgnoreCase("chqi211")) {
						src = "CHPI211"
					}

				}
			}
		}
		stage("Reset revision") {
			steps {
				script {
					commonPatchFunctions.resetRevisionFor(src,target)
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