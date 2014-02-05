package jobs.steps

import au.com.bytecode.opencsv.CSVWriter

/**
 * Created by carlos on 1/27/14.
 */
class BuildConceptTimeValuesStep implements Step {

    Map<String,Map> table

    String[] header

    File outputFile

    @Override
    String getStatusName() {
        return 'Creating concept time values table'
    }

    @Override
    void execute() {

        //makes sure the file is not there
        outputFile.delete()

        if (table != null) {
            writeToFile(table)
        }
    }

    private void writeToFile(Map<String, Map> map) {

        outputFile.withWriter { writer ->
            CSVWriter csvWriter = new CSVWriter(writer, '\t' as char)
            csvWriter.writeNext(header)

            map.each {
                def line = [it.key, it.value.value] as String[]
                csvWriter.writeNext(line)
            }
        }
    }

}
