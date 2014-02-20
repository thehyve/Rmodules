package jobs.table

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import jobs.steps.BuildConceptTimeValuesStep
import org.gmock.GMockTestCase
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm

/**
 * Created by carlos on 1/31/14.
 */
@TestMixin(GrailsUnitTestMixin)
class ConceptTimeValuesTableTest extends GMockTestCase {

    BuildConceptTimeValuesStep table

    ConceptsResource conceptsResource

    String path1 = "\\foo\\"
    String path2 = "\\bar\\"

    @Before
    void setUp() {
        table = new BuildConceptTimeValuesStep()
        conceptsResource = mock(ConceptsResource)
        table.conceptsResource = conceptsResource
    }

    @Test
    void testSuccessWith2Concepts() {

        String unit = 'days'
        OntologyTerm ot1 = setConceptResourceKeyExpect(path1, unit, '1')
        OntologyTerm ot2 = setConceptResourceKeyExpect(path2, unit, '2')
        def args = [path1, path2]

        play {
            Map<String,Map> result = table.computeMap(args)
            assertNotNull result

            assertEquals args.size(), result.size()
            assertHasScalingEntry(result, ot1)
            assertHasScalingEntry(result, ot2)
        }
    }

    @Test
    void testFailNoCommonUnit() {

        OntologyTerm ot1 = setConceptResourceKeyExpect(path1, 'days', '1')
        OntologyTerm ot2 = setConceptResourceKeyExpect(path2, 'weeks', '2')
        assertNoResult([path1, path2])
    }

    @Test
    void testFailWithNonNumericValue() {

        def args = setConceptResourceKeyExpect(path1, 'unit', 'string')
        assertNoResult([path1])
    }

    @Test
    void testFailWithoutMetadata() {

        OntologyTerm ot1 = setConceptResourceKeyExpect(path1, null)
        assertNoResult([path1])
    }

    private void assertNoResult(args) {
        play {
            assertNull table.computeMap(args)
        }
    }

    /**
     * Creates a OntologyTerm mock with metadata, setting all the necessary expectations
     * @param path
     * @param unit
     * @param value
     * @return
     */
    private OntologyTerm setConceptResourceKeyExpect(String path, String unit, String value) {
        setConceptResourceKeyExpect(path, createMetadata(unit, value))
    }

    private void assertHasScalingEntry(Map map, OntologyTerm ot) {
        Map expected = ot.getMetadata().seriesMeta
        Map actual = map.get(ot.getFullName())
        assertNotNull actual
        assertEquals expected, actual
    }

    private OntologyTerm setConceptResourceKeyExpect(String path, Map metadata) {
        String fullname = "$path fullname"
        OntologyTerm ot = createMockOntologyTerm(fullname, metadata)
        String key = BuildConceptTimeValuesStep.getConceptKey(path)
        conceptsResource.getByKey(key).returns(ot).stub()
        ot
    }

    private Map createMetadata(String unit, String value) {
        [
            seriesMeta: [
                    "unit": unit,
                    "value": value,
                    "label": "label for $value $unit",
            ]
        ]
    }

    private OntologyTerm createMockOntologyTerm(String fullname, Map metadata) {

        OntologyTerm ot = mock(OntologyTerm)
        ot.fullName.returns(fullname).stub()
        ot.metadata.returns(metadata).stub()
        ot
    }

}
