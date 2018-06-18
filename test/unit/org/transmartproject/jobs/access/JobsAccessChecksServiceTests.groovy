package org.transmartproject.jobs.access

import grails.test.mixin.TestFor
import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.exceptions.InvalidRequestException

@TestFor(JobsAccessChecksService)
@WithGMock
class JobsAccessChecksServiceTests {

    @Test
    void testOwnerCanReadData() {
        testUsername = 'test_user'
        admin = false

        play {
            assertTrue service.canDownload('test_user-Analysis-100')
        }
    }

    @Test
    void testOtherUserCanNotReadData() {
        testUsername = 'other_user'
        admin = false

        play {
            assertFalse service.canDownload('test_user-Analysis-100')
        }
    }

    @Test
    void testAdminCanReadData() {
        testUsername = 'admin_user'
        admin = true

        play {
            assertTrue service.canDownload('test_user-Analysis-100')
        }
    }

    @Test
    void testNotLoggedInUserCanNotReadData() {
        service.springSecurityService = mock()
        service.springSecurityService.principal.returns(null).stub()

        play {
            assertFalse service.canDownload('test_user-Analysis-100')
        }
    }

    @Test
    void testBadAnalysisName() {
        testUsername = 'test_user'
        admin = false

        play {
            shouldFail InvalidRequestException, {
                assertFalse service.canDownload('not_a_valid_analysis_name')
            }
        }
    }

    def mockGrailsUser

    @Before
    void before() {
        mockGrailsUser = mock()
        service.springSecurityService = mock()
        service.springSecurityService.principal.returns(mockGrailsUser).stub()
    }

    void setTestUsername(String username) {
        mockGrailsUser.username.returns(username).stub()
    }

    void setAdmin(boolean value) {
        def authorities = []
        if (value) {
            def adminAuthority = mock()
            adminAuthority.authority.returns JobsAccessChecksService.ROLE_ADMIN
            authorities.add(adminAuthority)
        }
        mockGrailsUser.authorities.returns(authorities).stub()
    }
}
