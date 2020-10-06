#!groovy

node {
	def file_in_workspace = unstashFileParameter "patchFile.json"
	fileOperations([fileDeleteOperation(includes: 'PatchFile.json')])
	fileOperations([fileRenameOperation(source: "${file_in_workspace}",  destination: 'PatchFile.json')])
	sh "cat PatchFile.json"
	stash name: "PatchFile" , includes:  'PatchFile.json'
}

pipeline {
	options {
		preserveStashes(buildCount: 2)
		timestamps()
	}

	agent any

	stages {
		// JHE (05.10.2020): We need a kind of wrapper stage in order to dynamically look within stageMappings
		stage('Starting') {
			steps {
				script {
					//TODO JHE (01.10.2020): Unstash file first
					def patchConfig = commonPatchFunctions.readPatchJsonFile(new File("${WORKSPACE}/PatchFile.json"))
					def stageMappings = patchConfig.stageMappings

					//TODO JHE (05.10.2020) : Do we need to call saveTarget ??? Not sure, probably the information will be stored differently since we have separated JSON files
					println "TODO : check if we have to call saveTarget"

					stageMappings.removeElement("Entwicklung")

					// TODO JHE (06.10.2020) : Remove this !!! only useful while developping
					stageMappings.removeElement("Anwendertest")
					stageMappings.removeElement("Produktion")



					stageMappings.each { s ->

						stage("Approve ${s} Build") {
							//TODO JHE (05.10.2020) : Here we need to call approveBuild function
							println "TODO : approveBuild"
						}

						stage("Build for ${s}") {
							patchfunctions.patchBuildsConcurrent(patchConfig)
						}
					}
				}
			}
		}
	}
}

