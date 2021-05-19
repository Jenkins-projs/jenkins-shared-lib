/*
this groovy is used to clean the workspace in periodic basis
*/

import hudson.model.*;
import hudson.util.*;
import jenkins.model.*;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.slaves.OfflineCause;
import hudson.node_monitors.*;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

//Configurable options
deleteStaleMultiBranchJobs = false

//jobMap = Jenkins.instance.itemMap

def padJob(def jobname){
  return "${jobname}".padRight(50,".")
}

def deleteWorkflowBranchJob(def subdir){
  if(deleteStaleMultiBranchJobs){
    //subdir.deleteRecursive()
    return "\u267B removed"
  }else{
    return "\u267B DELETE DISABLED, change 'deleteStaleMultiBranchJobs' to enable"
  }
}

def lookupJob(def lookup, def parent){
  //some jobs have workspaces ending in @tmp, ignore the @tmp part
  if(lookup ==~ /.*@tmp$/){
    lookup = lookup -~ /@tmp$/
  }
  if(lookup ==~ /.*@script$/){
    lookup = lookup -~ /@script$/
  }
  if(lookup ==~ /.*\d$/){
    lookup = lookup
  }
  def job;
  if(parent != null){
    job = parent.getItem(lookup)
  }else{
    job = Jenkins.instance.itemMap[lookup]
  }
  return job;
}

def purgeAncientJob(def job, def dir){
  if (job.isBuilding()) {
  println(".. job " + jobName + " is currently running, skipped")
  continue
  }
  jobMaxAge = 10
  def buildAgeSec = (System.currentTimeMillis() - job.getLastBuild().getTimeInMillis()).intdiv(1000)
  def buildAgeDays = buildAgeSec.intdiv(3600).intdiv(24)
  if(buildAgeDays > jobMaxAge ){
      println padJob(".... $dir.name")+" \u267B removed - build is older than 10 days"
     // dir.deleteRecursive()
  } else{
    println padJob(".... $dir.name")+" \u2713    ${job.getLastBuild().getTimestampString()} - build is less than 10 days "
  }
}

def cleanupNode(def ws){
  for(dir in ws.listDirectories()){
    def job = lookupJob(dir.name,null)
    if(job != null){
      if(job instanceof WorkflowMultiBranchProject){
        println padJob(".. $dir.name")+" existing multibranch pipeline job"
        for(subdir in dir.listDirectories()){
          def subjob = lookupJob(subdir.name, job)
          if(subjob != null){
            purgeAncientJob(subjob, subdir)
          }else{
            println padJob(".... $subdir.name")+" "+deleteWorkflowBranchJob(subdir)
          }
        }
      }else if(job instanceof WorkflowJob){
       println padJob(".. $dir.name")+" existing pipeline job \u2713"
      }else{
        println padJob(".. $dir.name")+" job of type ${job}"
      }
    }else{
	println padJob(".... $dir.name")+" \u267B removed - Blue Ocean/Stale Ws"+dir.lastModified().intValue()
     // dir.deleteRecursive()
    }
    println()
  }
}

def processNode(def node){
  def computer = node.toComputer()
  if (computer == null || computer.channel == null){
    println "[$node.displayName]  OFFLINE"
  }else{       
    try{
      def ws = node.getWorkspaceRoot()
      cleanupNode(ws)
    }finally{
      //computer.setTemporarilyOffline(false, null)
    }
  }
  println("\n")
}

def call(args = [:]) {
    // default parameters
    def defaultArgs = [     
            webexRoomId            : ""
    ]
    args = defaultArgs + args;

    pipeline {
        agent any
        stages {
            stage('CLEAN-WORKSPACE') {
                steps {
                    script {
                    // Clean the workspace on master
			println "[MASTER]"
			cleanupNode(new FilePath(Jenkins.instance.rootPath,'workspace'))
		
		    // Clean the workspace on slave nodes
         		def nodes = Jenkins.instance.nodes
	         	for (node in nodes) {
			processNode(node)
			}                                            
                   }
                }
            }
        }
    }
}
