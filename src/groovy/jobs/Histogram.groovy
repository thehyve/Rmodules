package jobs

import jobs.steps.BuildTableResultStep
import jobs.steps.ParametersFileStep
import jobs.steps.SimpleDumpTableResultStep
import jobs.steps.Step
import jobs.steps.helpers.NumericColumnConfigurator
import jobs.steps.helpers.SimpleAddColumnConfigurator
import jobs.table.Table
import jobs.table.columns.PrimaryKeyColumn
import nl.vumc.biomedbridges.v2.core.Workflow
import nl.vumc.biomedbridges.v2.core.WorkflowEngine
import nl.vumc.biomedbridges.v2.core.WorkflowFactory
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

import static jobs.steps.AbstractDumpStep.DEFAULT_OUTPUT_FILE_NAME

/**
 * Note: It's work in progress.
 * For now to make this job work you need 2 prerequisites:
 * 1. Register galaxy plugin with name 'Galaxy'(searchapp.plugin)
 * and plugin module (searchapp.plugin_module) with name 'histogram'
 * 2. Specify your galaxy api credentials (test.galaxy.instance and test.galaxy.key) in .blend.properties
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

    final static GALAXY_WORKFLOW_ID = 'histogram'
    final static GALAXY_WORKFLOW_INPUT_TITLE = 'Input Dataset'
    final String WORKFLOW_TYPE = WorkflowFactory.GALAXY_TYPE

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
                workflow.addInput(GALAXY_WORKFLOW_INPUT_TITLE, new File(thisJob.temporaryDirectory, DEFAULT_OUTPUT_FILE_NAME))
                workflowEngine.runWorkflow(workflow)
                File outputFile = workflow.getOutput('output')
                outputFile.renameTo(new File(thisJob.temporaryDirectory, outputFile.name))
            }
        }

        steps
    }

    @PostConstruct
    void init() {
        //TODO Register plugin/plugin module automatically?

        workflowEngine = WorkflowFactory.getWorkflowEngine(WORKFLOW_TYPE)
        workflow = WorkflowFactory.getWorkflow(WORKFLOW_TYPE, GALAXY_WORKFLOW_ID)

        primaryKeyColumnConfigurator.column = new PrimaryKeyColumn(header: 'PATIENT_NUM')

        numericVariableConfigurator.header = 'VALUE'
        numericVariableConfigurator.keyForConceptPath = 'variablesConceptPath'
        numericVariableConfigurator.alwaysClinical = true
    }

}
