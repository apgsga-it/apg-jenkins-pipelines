import hudson.model.*
def patchName = "Patch${patchnumber}"
def jobName = patchName
def downLoadJobName = jobName + "OnDemand"
def stageList = stages.split(",")

stageList.each {stage ->
	pipelineJob(jobName + "_build_" + stage) {
		authenticationToken(patchName)
		concurrentBuild(false)
		definition {
			cps {
				script(readFileFromWorkspace('src/main/groovy/patchProdBuildPipeline.groovy'))
				sandbox(true)
			}
		}
		logRotator(3653, 10, 3653, -1) // ten years legal retention period
		description("Patch Pipeline for : ${patchName}")
		parameters {
			fileParam('patchFile.json', 'JSON file for the patch corresponding to this Pipeline')
		}
		properties {
		}
	}
}
/*
pipelineJob (downLoadJobName) {
	authenticationToken(downLoadJobName)
	concurrentBuild(false)
	definition {
		cps {
			script(readFileFromWorkspace('src/main/groovy/patchOnDemandPipeline.groovy'))
			sandbox(true)
		}
	}
	logRotator(3653,10,3653,-1) // ten years legal retention period
	description("OnDemand Patch Pipeline for : ${patchName}")
	parameters {
		stringParam('PARAMETER', "", "String mit dem die PatchConfig Parameter als JSON transportiert werden")
	}
	properties {
	}
}

 */
