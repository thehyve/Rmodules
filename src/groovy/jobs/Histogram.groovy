package jobs

import jobs.steps.BuildTableResultStep
import jobs.steps.ParametersFileStep
import jobs.steps.SimpleDumpTableResultStep
import jobs.steps.Step
import jobs.steps.helpers.NumericColumnConfigurator
import jobs.steps.helpers.SimpleAddColumnConfigurator
import jobs.table.Table
import jobs.table.columns.PrimaryKeyColumn
import nl.vumc.biomedbridges.core.Workflow
import nl.vumc.biomedbridges.core.WorkflowEngine
import nl.vumc.biomedbridges.core.WorkflowEngineFactory
import nl.vumc.biomedbridges.galaxy.configuration.GalaxyConfiguration
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

import static jobs.steps.AbstractDumpStep.DEFAULT_OUTPUT_FILE_NAME

/**
 * Note: Remember to register galaxy plugin with name 'Galaxy'(searchapp.plugin)
 * and plugin module (searchapp.plugin_module) with name 'histogram'
 */
@Component
@Scope('job')
class Histogram extends AbstractAnalysisJob {

    @Autowired
    GrailsApplication grailsApplication

    @Autowired
    SimpleAddColumnConfigurator primaryKeyColumnConfigurator

    @Autowired
    NumericColumnConfigurator numericVariableConfigurator

    @Autowired
    Table table

    final static String GALAXY_WORKFLOW_NAME = 'histogram'
    final static String WORKFLOW_DATA_FILE_INPUT_NAME = 'Input Dataset'
    final static int HISTOGRAM_STEP_ID = 1
    final static String WORKFLOW_BREAKS_PARAM_NAME = 'breaks'
    final static String WORKFLOW_TITLE_PARAM_NAME = 'title'
    final static String WORKFLOW_X_LABLEL_PARAM_NAME = 'xlab'

    WorkflowEngine workflowEngine
    Workflow workflow
    Histogram thisJob = this

    @Override
    protected getForwardPath() {
        "/Histogram/histogramOut?jobName=${name}"
    }

    @Override
    protected List<Step> prepareSteps() {
        List<Step> steps = []

        steps << new ParametersFileStep(
                temporaryDirectory: temporaryDirectory,
                params: params)

        steps << new BuildTableResultStep(
                table: table,
                configurators: [primaryKeyColumnConfigurator,
                                numericVariableConfigurator])

        steps << new SimpleDumpTableResultStep(table: table,
                temporaryDirectory: temporaryDirectory,
                outputFileName: DEFAULT_OUTPUT_FILE_NAME,
                noQuotes: true
        )

        steps << new Step() {
            String statusName = 'Running galaxy workflow'

            @Override
            void execute() {
                workflow.addInput(WORKFLOW_DATA_FILE_INPUT_NAME, new File(thisJob.temporaryDirectory, DEFAULT_OUTPUT_FILE_NAME))
                workflow.setParameter(HISTOGRAM_STEP_ID, WORKFLOW_BREAKS_PARAM_NAME, thisJob.params['numOfBreaks'] ?: '0')
                workflow.setParameter(HISTOGRAM_STEP_ID, WORKFLOW_TITLE_PARAM_NAME, thisJob.params['plotTitle'] ?: '')
                workflow.setParameter(HISTOGRAM_STEP_ID, WORKFLOW_X_LABLEL_PARAM_NAME, thisJob.params['xLabel'] ?: '')
                workflowEngine.runWorkflow(workflow)
                def outputs = workflow.getOutputMap()
                outputs.each {
                    if(it.value instanceof File) {
                        it.value.renameTo(new File(thisJob.temporaryDirectory, it.value.name))
                    }
                }
            }
        }

        steps
    }

    @PostConstruct
    void init() {
        def galaxyInstanceUrl = grailsApplication.config.galaxy.instance_url
        def apiKey = grailsApplication.config.galaxy.api_key
        assert 'Galaxy credentials are not specified', galaxyInstanceUrl && apiKey

        def historyName = params['jobName']
        String configuration = GalaxyConfiguration.buildConfiguration(galaxyInstanceUrl, apiKey, historyName)
        workflowEngine = WorkflowEngineFactory.getWorkflowEngine(WorkflowEngineFactory.GALAXY_TYPE, configuration)
        workflow = workflowEngine.getWorkflow(GALAXY_WORKFLOW_NAME)

        primaryKeyColumnConfigurator.column = new PrimaryKeyColumn(header: 'PATIENT_NUM')

        numericVariableConfigurator.header = 'VALUE'
        numericVariableConfigurator.keyForConceptPath = 'variablesConceptPath'
        numericVariableConfigurator.alwaysClinical = true
    }

}
