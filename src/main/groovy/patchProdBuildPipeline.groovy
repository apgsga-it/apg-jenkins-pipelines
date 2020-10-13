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
def Notify_InformatiktestBuildDone = "InformatiktestBuildDone"
def AnwendertestBuild = "Anwendertest"
def Approve_AnwendertestBuild = "AnwendertestApprove"
def Notify_AnwendertestBuildDone = "AnwendertestBuildDone"
def ProduktionBuild = "Produktion"
def Approve_ProduktionBuild = "ProduktionApprove"
def Notify_ProduktionBuildDone = "ProduktionBuildDone"
def patchConfig = commonPatchFunctions.readPatchJsonFileFromStash("PatchFile")

pipeline {
	options {
		preserveStashes(buildCount: 2)
		timestamps()
	}

	agent any

	stages {

		// TODO JHE (09.10.2020): To be verified with UGE, but I believe we don't have to wait for any approval here, we can just prepare the ZIPs
		stage("BuildDbZip") {
			steps {
				script {
					patchfunctions.patchBuildDbZip(patchConfig)
				}
			}
		}

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

		stage(Notify_InformatiktestBuildDone) {
			steps {
				script {
					println "TODO : Notify DB for ${Notify_InformatiktestBuildDone}"
				}
			}
		}

		stage(Approve_AnwendertestBuild) {
			steps {
				input message: "Ok to Build for ${AnwendertestBuild}?"
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

		stage(Notify_AnwendertestBuildDone) {
			steps {
				script {
					println "TODO : Notify DB for ${Notify_AnwendertestBuildDone}"
				}
			}
		}

		stage(Approve_ProduktionBuild) {
			steps {
				input message: "Ok to Build for ${ProduktionBuild}?"
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

		stage(Notify_ProduktionBuildDone) {
			steps {
				script {
					println "TODO : Notify DB for ${Notify_ProduktionBuildDone}"
				}
			}
		}
	}
}