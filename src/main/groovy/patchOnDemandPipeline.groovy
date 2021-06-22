#!groovy
import groovy.json.JsonSlurperClassic

import java.text.SimpleDateFormat

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETERS)
paramsAsJson.patchNumbers = [paramsAsJson.patchNumber] // For onDemand, we only have one parameter.However, assembleAndDeploy and install are dealing with a list
def dateInfo = new SimpleDateFormat("yyyyMMdd_HHmmss_S").format(new Date())
def revisionClonedPath = "${env.GRADLE_USER_HOME_PATH}/patch${paramsAsJson.patchNumber}_${paramsAsJson.target}_${dateInfo}"

pipeline {
	parameters {
		string(name: 'PARAMETERS', description: "JSON parameters")
	}

	agent any

	stages {
		//JHE (17.06.2021): This is not really a stage ... but Jenkins won't accept to have step done before parallel tasks (below)
		stage("Starting logged") {
			steps {
				script {
					commonPatchFunctions.log("Pipeline started with following parameter : ${paramsAsJson}")
				}
			}
		}
		stage("Create local Revision folder") {
			steps {
				script {
					commonPatchFunctions.createFolder(revisionClonedPath)
				}
			}
		}
		stage("Clean workspace") {
			steps {
				script {
					deleteDir()
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
								paramsAsJson.buildUrl = env.BUILD_URL
								patchfunctions.patchBuildsConcurrent(paramsAsJson,revisionClonedPath)
							}
						}
				)
			}
		}
		//JHE (17.06.2021): This is not really a stage ... but Jenkins won't accept to have step done before parallel tasks (below)
		stage("Logging before assembleAndDeploy starts") {
			steps {
				script {
					assembleAndDeployPatchFunctions.logPatchActivity(paramsAsJson.patchNumbers, paramsAsJson.target, "Started", env.BUILD_URL)
				}
			}
		}
		stage("Assemble and Deploy") {
			steps {
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
			}
		}
		//JHE (17.06.2021): This is not really a stage ... but Jenkins won't accept to have step done before parallel tasks (below)
		stage("Logging assembleAndDeploy done") {
			steps {
				script {
					assembleAndDeployPatchFunctions.logPatchActivity(paramsAsJson.patchNumbers, paramsAsJson.target, "Done", env.BUILD_URL)
				}
			}
		}
		//JHE (17.06.2021): This is not really a stage ... but Jenkins won't accept to have step done before parallel tasks (below)
		stage("Logging before Install starts") {
			steps {
				script {
					installPatchFunctions.logPatchActivity(paramsAsJson.patchNumbers, paramsAsJson.target, "Started", env.BUILD_URL)
				}
			}
		}
		stage("Install") {
			steps {
				parallel(
						"db-install": {
							script {
								installPatchFunctions.installDb(paramsAsJson)
							}
						},
						"java-install": {
							script {
								installPatchFunctions.installJavaServices(paramsAsJson)
							}
						},
						"docker-install": {
							script {
								installPatchFunctions.installDockerServices(paramsAsJson)
							}
						}
				)
			}
		}
		//JHE (17.06.2021): This is not really a stage ... but Jenkins won't accept to have step done before parallel tasks (below)
		stage("Logging Install done") {
			steps {
				script {
					installPatchFunctions.logPatchActivity(paramsAsJson.patchNumbers, paramsAsJson.target, "Done", env.BUILD_URL)
				}
			}
		}
	}
	post {
		always {
			script {
				commonPatchFunctions.deleteFolder(revisionClonedPath)
			}
		}
	}
}