package org.transmartproject.rmodules

import org.transmartproject.core.users.User

class AuditLogFilters {

    def accessLogService
    def auditLogService
    User currentUserBean

    def filters = {
        download(controller: 'analysisFiles', action:'download') {
            after = { model ->
                if (params.path.toLowerCase().endsWith('.zip')) {
                    def ip = request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr
                    accessLogService.report(currentUserBean, 'Raw R Data Export',
                            eventMessage: "User (IP: ${ip}) downloaded ${params.path} for ${params.analysisName}",
                            requestURL: "${request.forwardURI}${request.queryString ? '?' + request.queryString : ''}")
                }
            }
        }
        jobs(controller: 'asyncJob', action: 'createnewjob') {
            after = { model ->
                def workflow = params.jobType
                def action = params.variablesConceptPaths
                auditLogService.report("Advanced Workflow - ${workflow}", request,
                    user: currentUserBean,
                    action: action as String,
                )
            }
        }
    }

}
