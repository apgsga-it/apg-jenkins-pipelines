def now = new Date()
timeAsString = now.format("yyyyMMdd-HHmmss")
def jobName = "${jobPreFix}-${target}-${timeAsString}"
pipelineJob (jobName) {
    concurrentBuild(false)
    definition {
        cpsScm {
            lightweight(true)
            scm {
                github("${github_repo}","${github_repo_branch}")
            }
            scriptPath("${script_path}")
        }
    }
    logRotator(3653,10,3653,-1) // ten years legal retention period
    description("Pipeline for ${jobPreFix}")
    parameters {
        stringParam('TARGET',"${target}", "Target for ${jobPreFix} Pipeline ")
        stringParam('PARAMETER', "${parameter}", "JSON Parameter for ${jobPreFix} Pipeline")
    }
    properties {
    }
}
queue(jobName)