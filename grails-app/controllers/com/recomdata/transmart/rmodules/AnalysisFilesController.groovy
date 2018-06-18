package com.recomdata.transmart.rmodules

import org.transmartproject.jobs.access.JobsAccessChecksService

class AnalysisFilesController {

    def sendFileService

    def RModulesOutputRenderService

    JobsAccessChecksService jobsAccessChecksService

    def download() {
        String jobName = params.analysisName

        if (!jobsAccessChecksService.canDownload(jobName)) {
            render status: 403
            return
        }

        File analysisDirectory = new File(jobsDirectory, jobName)
        if (analysisDirectory.parentFile != jobsDirectory) {
            // just some sanity checking... should always happen
            log.error "Unexpected analysis directory: $analysisDirectory"
            render status: 404
            return
        }
        if (!analysisDirectory.exists()) {
            log.warn "Could not find directory for job " +
                    "$jobName: $analysisDirectory"
            render status: 404
            return
        }
        if (!analysisDirectory.isDirectory()) {
            log.error "Analysis directory is surprisingly " +
                    "not a directory: $analysisDirectory"
            render status: 404
            return
        }

        // Only expose files under the analysis directory
        File targetFile = new File(analysisDirectory, params.path)
        //canonical path does not end with separator
        if (!targetFile.canonicalPath
                .startsWith(analysisDirectory.canonicalPath + File.separator)) {

            log.warn "Request for $targetFile, but it's not " +
                    "under $analysisDirectory"
            render status: 404
            return
        }

        if (!targetFile.isFile()) {
            log.warn "Request for $targetFile, but such file does not exist"
            render status: 404
            return
        }

        sendFileService.sendFile servletContext, request, response, targetFile
    }

    private File getJobsDirectory() {
        new File(RModulesOutputRenderService.tempFolderDirectory)
    }
}
