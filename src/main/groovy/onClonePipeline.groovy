#!groovy
import groovy.json.JsonSlurperClassic
import java.text.SimpleDateFormat

@Library('onCloneFunctions')

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


					def buildGradle = libraryResource("build.gradle.resetRevision")
					writeFile(file: "build.gradle", text: buildGradle)



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
										patchfunctions.patchBuildsConcurrent(bp, revisionClonedPath)
								}
						)
						commonPatchFunctions.deleteFolder(revisionClonedPath)
					}
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
	}
}