package jobs.steps

import com.recomdata.transmart.data.association.RModulesController
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import jobs.UserParameters

class ParametersFileStep implements Step{

    File temporaryDirectory
    UserParameters params

    final String statusName = null

    @Override
    void execute() {
        File jobInfoFile = new File(temporaryDirectory, 'jobInfo.txt')

        jobInfoFile.withWriter { BufferedWriter it ->
            it.writeLine 'Parameters'
            params.each { key, value ->
                it.writeLine "\t$key -> $value"
            }
        }
        File paramsFile = new File(temporaryDirectory, 'request.json')
        paramsFile << getRequestJson()
    }

    private String getRequestJson() {
        JsonBuilder builder = new JsonBuilder()
        //sorting the map so its easier to compare visually
        builder(new TreeMap(params[RModulesController.ORIGINAL_REQUEST_PARAMS]))
        //pretty printing so its easier to read
        return builder.toPrettyString()
    }
}
