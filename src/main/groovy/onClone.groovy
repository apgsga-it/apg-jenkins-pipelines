#!groovy
import groovy.json.JsonSlurperClassic

import java.text.SimpleDateFormat

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETERS)

pipeline {
	parameters {
		string(name: 'PARAMETERS', description: "JSON parameters")
	}

	agent any

	stages {
		stage("Pre-process verification") {
			steps {
				script {
					if(paramsAsJson.target.equalsIgnoreCase("chpi211")) {
						error("Target cannot be production environment !!")
					}
				}
			}
		}
		stage("Reset revision") {
			steps {
				script {
					onCloneFunctions.resetRevisionFor(paramsAsJson)
				}
			}
		}
		stage("Build") {
			steps {
				script {
					paramsAsJson.patches.each { p ->
						def dateInfo = new SimpleDateFormat("yyyyMMdd_HHmmss_S").format(new Date())
						def revisionClonedPath = "${env.GRADLE_USER_HOME_PATH}/onclone_${paramsAsJson.target}_patch${p.patchNumber}_${dateInfo}"
						commonPatchFunctions.createFolder(revisionClonedPath)
						parallel(
								"db-build": {
										patchfunctions.patchBuildDbZip(p)
								},
								"java-build": {
										patchfunctions.patchBuildsConcurrent(p, revisionClonedPath)
								}
						)
						commonPatchFunctions.deleteFolder(revisionClonedPath)
					}
				}
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