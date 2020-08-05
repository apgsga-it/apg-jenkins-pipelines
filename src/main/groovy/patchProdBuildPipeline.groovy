#!groovy
library 'patch-global-functions'
#! WORK IN PROGRESS NOT READY FOR TESTING
properties([
	parameters([
		stringParam(
		defaultValue: "",
		description: 'Path to Patch*.json File',
		name: 'PARAMETER'
		),
		stringParam(
		defaultValue: "FALSE",
		description: 'Indicator, if the Pipeline should be restartet to the last successful state',
		name: 'RESTART'
		)
	])
])

def patchConfig = commonPatchFuntions.readPatchFile(params.PARAMETER)
commonPatchFuntions.initPatchConfig(patchConfig,params)
println "PatchConfig:"
println patchConfig.toString()
//
// Load Target System Mappings
def targetSystemsMap = commonPatchFuntions.loadTargetsMap()
commonPatchFuntions.log("TargetSystemsMap : ${targetSystemsMap} ")
// Create a local Maven Repo for Pipeline
// Retrieve event. State, which will re - done
commonPatchFuntions.redoToState(patchConfig)

//// Artefacts are tagged = ready to be built and deployed with start of Patch Pipeline
patchfunctions.stage(target,"Installationsbereit",patchConfig,"Build", patchBuildFunctions.&buildServicesConcurrent)
