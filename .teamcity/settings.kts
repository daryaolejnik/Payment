import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.MavenBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.dockerRegistry
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

version = "2019.2"

project {

    vcsRoot(Gateway)

    features {
        dockerRegistry {
            id = "PROJECT_EXT_5"
            name = "Docker Registry"
            url = "https://docker.io"
            userName = "artemkulish"
            password = "credentialsJSON:91a788d6-72b3-405f-a9df-03389f20d48c"
        }
    }

    subProject(Discovery)   
}

object Gateway : GitVcsRoot({
    name = "Gateway"
    url = "https://github.com/DevOps-DP-185/Gateway.git"
    authMethod = password {
        userName = "ArtemKulish"
        password = "credentialsJSON:91a788d6-72b3-405f-a9df-03389f20d48c"
    }
})


object Discovery : Project({
    name = "Discovery"

    vcsRoot(Discovery_HttpsGithubComDevOpsDp185DiscoveryRefsHeadsMaster)

    buildType(Discovery_Deploy)
    buildType(Discovery_DiscoveryBuild)
})

object Discovery_Deploy : BuildType({
    name = "Deploy"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    vcs {
        root(Discovery_HttpsGithubComDevOpsDp185DiscoveryRefsHeadsMaster)
    }

    steps {
        step {
            type = "ssh-exec-runner"
            param("jetbrains.buildServer.sshexec.command", "ls")
            param("jetbrains.buildServer.deployer.targetUrl", "35.184.252.223")
            param("jetbrains.buildServer.sshexec.authMethod", "DEFAULT_KEY")
            param("jetbrains.buildServer.sshexec.keyFile", "/home/artemkulish123/")
        }
    }

    triggers {
        vcs {
        }
    }

    dependencies {
        snapshot(Discovery_DiscoveryBuild) {
        }
    }
})

object Discovery_DiscoveryBuild : BuildType({
    name = "Build"

    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(Discovery_HttpsGithubComDevOpsDp185DiscoveryRefsHeadsMaster)
    }

    steps {
        maven {
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            localRepoScope = MavenBuildStep.RepositoryScope.MAVEN_DEFAULT
            jdkHome = "%env.JDK_11%"
        }
        dockerCommand {
            commandType = build {
                source = file {
                    path = "Dockerfile"
                }
                namesAndTags = "artemkulish/demo4:discovery"
                commandArgs = "--pull"
            }
            param("dockerImage.platform", "linux")
        }
        dockerCommand {
            commandType = push {
                namesAndTags = "artemkulish/demo4:discovery"
            }
            param("dockerfile.path", "Dockerfile")
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_5"
            }
        }
    }
})

object Discovery_HttpsGithubComDevOpsDp185DiscoveryRefsHeadsMaster : GitVcsRoot({
    name = "https://github.com/DevOps-DP-185/Discovery#refs/heads/master"
    url = "https://github.com/DevOps-DP-185/Discovery"
    authMethod = password {
        userName = "ArtemKulish"
        password = "credentialsJSON:91a788d6-72b3-405f-a9df-03389f20d48c"
    }
})
