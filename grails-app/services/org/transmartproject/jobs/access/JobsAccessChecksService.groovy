package org.transmartproject.jobs.access

import org.transmartproject.core.exceptions.InvalidRequestException

import java.util.regex.Matcher
import java.util.regex.Pattern

class JobsAccessChecksService {

    static transactional = false

    public static final String ROLE_ADMIN = 'ROLE_ADMIN'

    def springSecurityService

    boolean canDownload(String jobName) {
        String userName = extractUserFromJobName(jobName)

        def loggedInUser = springSecurityService.principal?.username
        if (!loggedInUser) {
            log.error 'Could not determine current logged in user\'s name'
            return false
        }

        if (userName == loggedInUser || admin) {
            return true
        }

        log.warn "User $loggedInUser has no access for job $jobName; refusing " +
                "request for job $jobName"
        false
    }

    private boolean isAdmin() {
        springSecurityService.principal.authorities.any {
            it.authority == ROLE_ADMIN
        }
    }

    private static String extractUserFromJobName(String jobName) {
        Pattern pattern = ~/(.+)-[a-zA-Z]+-\d+/
        Matcher matcher = pattern.matcher(jobName)

        if (!matcher.matches()) {
            //should never happen due to url mapping
            throw new InvalidRequestException('Invalid job name')
        }

        matcher.group(1)
    }
}
