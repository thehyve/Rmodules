package jobs.steps

import au.com.bytecode.opencsv.CSVWriter
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm

/**
 * Created by carlos on 1/27/14.
 */
class BuildConceptTimeValuesStep implements Step {

    @Autowired
    ConceptsResource conceptsResource

    List<String> timeValuesConcepts

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

        def table = computeMap(timeValuesConcepts)

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

    /**
     * @return map of concept_fullname -> series_meta map, or null if not enabled or metadata not applicable
     */
    private Map<String,Map> computeMap(List<String> conceptPaths) {

        //get all the OntologyTerms for the concepts
        Set<OntologyTerm> terms = conceptPaths.collect {
            conceptsResource.getByKey(getConceptKey(it))} as Set

        //get all the SeriesMeta mapped by concept name
        Map<String, Map> nameToSeriesMeta = terms.collectEntries {[it.fullName, it.metadata?.seriesMeta as Map]}

        if (nameToSeriesMeta.size() > 0) {
            String firstUnit = nameToSeriesMeta.values().first()?.unit?.toString()

            //if all the units are the same and not null, and with numerical values
            if (firstUnit != null &&
                    nameToSeriesMeta.values().every { it?.value?.isInteger() && firstUnit == it?.unit?.toString() }) {

                return nameToSeriesMeta
            }
        }

        return null //nothing to return
    }

    public static String getConceptKey(String path) {
        OpenHighDimensionalDataStep.createConceptKeyFrom(path)
    }


}
