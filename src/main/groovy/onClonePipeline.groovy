#!groovy
import groovy.json.JsonSlurperClassic
import java.text.SimpleDateFormat

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETER)

pipeline {
	parameters {
		string(name: 'PARAMETER', description: "JSON parameters")
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
					paramsAsJson.buildParameters.each { bp ->
						def dateInfo = new SimpleDateFormat("yyyyMMdd_HHmmss_S").format(new Date())
						def revisionClonedPath = "${env.GRADLE_USER_HOME_PATH}/onclone_${paramsAsJson.target}_patch${bp.patchNumber}_${dateInfo}"
						commonPatchFunctions.createFolder(revisionClonedPath)
						parallel(
								"db-build": {
										patchfunctions.patchBuildDbZip(bp)
								},
								"java-build": {
										bp.buildUrl = env.BUILD_URL
										patchfunctions.patchBuildsConcurrent(bp, revisionClonedPath)
								}
						)
						commonPatchFunctions.deleteFolder(revisionClonedPath)
					}
				}
			}
		}
		//JHE (17.06.2021): This is not really a stage ... but Jenkins won't accept to have step done before parallel tasks (below)
		stage("Logging before assembleAndDeploy starts") {
			steps {
				script {
					onCloneFunctions.logAssembleAndDeployPatchActivity(paramsAsJson.adParameters, "Started", env.BUILD_URL)
				}
			}
		}
		stage("Assemble and Deploy") {
			steps {
				parallel(
						"db-assemble": {
							script {
								assembleAndDeployPatchFunctions.assembleAndDeployDb(paramsAsJson.adParameters)
							}
						},
						"java-assemble": {
							script {
								assembleAndDeployPatchFunctions.assembleAndDeployJavaService(paramsAsJson.adParameters)
							}
						}
				)
			}
		}
		//JHE (17.06.2021): This is not really a stage ... but Jenkins won't accept to have step done before parallel tasks (below)
		stage("Logging assembleAndDeploy done") {
			steps {
				script {
					onCloneFunctions.logAssembleAndDeployPatchActivity(paramsAsJson.adParameters, "Done", env.BUILD_URL)
				}
			}
		}
	}
}