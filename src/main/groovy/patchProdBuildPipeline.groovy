#!groovy

node {
	def file_in_workspace = unstashFileParameter "patchFile.json"
	fileOperations([fileDeleteOperation(includes: 'PatchFile.json')])
	fileOperations([fileRenameOperation(source: "${file_in_workspace}",  destination: 'PatchFile.json')])
	sh "cat PatchFile.json"
	stash name: "PatchFile" , includes:  'PatchFile.json'

}

// JHE (08.10.2020) : Stage names in declarativ pipeline .... https://issues.jenkins-ci.org/browse/JENKINS-43820
//                  : So important to stick with variableName=variableValue
def InformatiktestBuild = "Informatiktest"
def Approve_InformatikTestBuild = "InformatiktestApprove"
def AnwendertestBuild = "Anwendertest"
def Approve_AnwendertestBuild = "AnwendertestApprove"
def ProduktionBuild = "Produktion"
def Approve_ProduktionBuild = "ProduktionApprove"
def patchConfig = commonPatchFunctions.readPatchJsonFileFromStash("PatchFile")

pipeline {
	options {
		preserveStashes(buildCount: 2)
		timestamps()
	}

	agent any

	stages {

		stage(Approve_InformatikTestBuild) {
			steps {
				input message: "Ok to Build for ${InformatiktestBuild}?"
			}
		}

		stage(InformatiktestBuild) {
			steps {
				script {
					patchConfig.currentTarget = patchConfig.stageMappings.get(InformatiktestBuild)
					patchfunctions.patchBuildsConcurrent(patchConfig)
				}
			}
		}

		stage(Approve_AnwendertestBuild) {
			steps {
				println "TODO : approve"
			}
		}

		stage(AnwendertestBuild) {
			steps {
				script {
					patchConfig.currentTarget = patchConfig.stageMappings.get(AnwendertestBuild)
					patchfunctions.patchBuildsConcurrent(patchConfig)
				}
			}
		}

		stage(Approve_ProduktionBuild) {
			steps {
				println "TODO : approve"
			}
		}

		stage(ProduktionBuild) {
			steps {
				script {
					patchConfig.currentTarget = patchConfig.stageMappings.get(ProduktionBuild)
					patchfunctions.patchBuildsConcurrent(patchConfig)
				}
			}
		}
	}
}