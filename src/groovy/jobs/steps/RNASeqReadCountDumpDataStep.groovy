package jobs.steps

import com.google.common.collect.Lists
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.rnaseq.RnaSeqValues

class RNASeqReadCountDumpDataStep extends AbstractDumpHighDimensionalDataStep {

    RNASeqReadCountDumpDataStep() {
        callPerColumn = false
    }

    @Override
    protected computeCsvRow(String subsetName,
                            String seriesName,
                            DataRow genericRow,
                            AssayColumn column /* null */,
                            Object cell /* null */) {

        RegionRow<RnaSeqValues> row = genericRow
        // +1 because the first column has no header
        def line = Lists.newArrayListWithCapacity(csvHeader.size() + 1)
        line[0] = row.name as String

        int j = 1

        assays.each { AssayColumn assay ->
            line[j++] = row.getAt(assay).readcount as String
        }

        line
    }

    @Lazy List<String> csvHeader = {
        List<String> r = [ 'genesymbol' ]

        assays.each { AssayColumn assay ->
            r << assay.patientInTrialId
        }

        r
    }()

    @Lazy def assays = {
        results.values().iterator().next().indicesList
    }()

}
