package com.recomdata.transmart.rmodules

import com.recomdata.transmart.data.association.RModulesOutputRenderService
import grails.test.mixin.TestFor
import org.gmock.WithGMock
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.jobs.access.JobsAccessChecksService

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

@TestFor(AnalysisFilesController)
@WithGMock
class AnalysisFilesControllerTests {

    private static final String EXISTING_FILE_NAME = 'file_that_exists'
    private static final String FILE_CONTENTS = 'file contents\n'
    private static final String ANALYSIS_NAME = "user-Analysis-100"

    File temporaryDirectory
    File analysisDirectory
    File targetFile
    def sendFileServiceMock

    @Before
    void before() {
        temporaryDirectory = File.createTempDir('analysis_file_test', '')
        analysisDirectory = new File(temporaryDirectory, ANALYSIS_NAME)
        analysisDirectory.mkdir()

        controller.RModulesOutputRenderService = mock RModulesOutputRenderService
        controller.RModulesOutputRenderService.tempFolderDirectory.
                returns(temporaryDirectory.absolutePath).stub()

        sendFileServiceMock = mock()
        controller.sendFileService = sendFileServiceMock

        controller.jobsAccessChecksService = mock JobsAccessChecksService

        params.analysisName = ANALYSIS_NAME
    }

    void setFile(String filename) {
        targetFile = new File(analysisDirectory, filename)
        targetFile << FILE_CONTENTS

        params.path = filename
    }

    @After
    void after() {
        temporaryDirectory.deleteDir()
    }

    @Test
    void basicTest() {
        // test the normal circumstances (file exists and is allowed)
        controller.jobsAccessChecksService.canDownload(ANALYSIS_NAME).returns(true)
        file = EXISTING_FILE_NAME

        sendFileServiceMock.sendFile(isA(ServletContext),
                isA(HttpServletRequest), isA(HttpServletResponse),
                is(equalTo(targetFile)))

        play {
            controller.download()
        }

        assertThat response.status, is(200)
    }

    @Test
    void testNoPermission() {
        controller.jobsAccessChecksService.canDownload(ANALYSIS_NAME).returns(false)

        play {
            controller.download()
        }

        assertThat response.status, is(403)
    }

    @Test
    void testInexistingAnalysisName() {
        params.analysisName = ANALYSIS_NAME + '1'
        controller.jobsAccessChecksService.canDownload(params.analysisName).returns(true)

        play {
            controller.download()
        }

        assertThat response.status, is(404)
    }

    @Test
    void testAccessToExternalFilesNotAllowed() {
        controller.jobsAccessChecksService.canDownload(ANALYSIS_NAME).returns(true)
        file = '../test'

        play {
            controller.download()
        }

        assertThat response.status, is(404)
    }

    @Test
    void testNonExistingFile() {
        controller.jobsAccessChecksService.canDownload(ANALYSIS_NAME).returns(true)
        params.path = 'file_that_does_not_exist'

        play {
            controller.download()
        }

        assertThat response.status, is(404)
    }


}
