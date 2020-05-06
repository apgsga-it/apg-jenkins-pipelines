#!groovy
library 'patch-global-functions'

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

def patchConfig = patchfunctions.readPatchFile(params.PARAMETER)
patchfunctions.initPatchConfig(patchConfig,params)
println "PatchConfig:"
println patchConfig.toString()
//
// Load Target System Mappings
def targetSystemsMap = patchfunctions.loadTargetsMap()
patchfunctions.log("TargetSystemsMap : ${targetSystemsMap} ")
// Create a local Maven Repo for Pipeline
// TODO (che, jhe) 5.5.20: This does'nt make sense, mavenLocal should not be Node bound
patchfunctions.mavenLocalRepo(patchConfig)
// Retrieve event. State, which will re - done
patchfunctions.redoToState(patchConfig)

//// Artefacts are tagged = ready to be built and deployed with start of Patch Pipeline
patchfunctions.stage(target,"Installationsbereit",patchConfig,"Build", patchfunctions.&patchBuildsConcurrent)
