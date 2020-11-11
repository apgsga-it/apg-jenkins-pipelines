#!groovy

node {
    def file_in_workspace = unstashFileParameter "patchFile.json"
    fileOperations([fileDeleteOperation(includes: 'PatchFile.json')])
    fileOperations([fileRenameOperation(source: "${file_in_workspace}", destination: 'PatchFile.json')])
    sh "cat PatchFile.json"
    stash name: "PatchFile", includes: 'PatchFile.json'

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

        stage("BuildDbZip") {
            steps {
                script {
                    patchfunctions.patchBuildDbZip(patchConfig)
                }
            }
        }

        stage("Notify DB") {
            steps {
                script {
                    println "TODO JHE (11.11.2020): need to notify for which status ?"
                }
            }
        }

    }
}