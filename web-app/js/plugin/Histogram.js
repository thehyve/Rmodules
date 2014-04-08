function loadHistogramView() {
    histogramView.clear_high_dimensional_input('divDataNode');
    histogramView.register_drag_drop();
}

// constructor
var HistogramView = function () {
    RmodulesView.call(this);
}

// inherit RmodulesView
HistogramView.prototype = new RmodulesView();

// correct the pointer
HistogramView.prototype.constructor = HistogramView;

// submit analysis job
HistogramView.prototype.submit_job = function () {
    var formParams = this.get_form_params();
    submitJob(formParams);
}

// get form params
HistogramView.prototype.get_form_params = function () {
    var formParameters = { jobType: 'Histogram' };

    var inputArray = [
        {
            "label" : "Number of breaks",
            "el" : document.getElementById("numOfBreaks"),
            "validations" : [{type:"INTEGER"}]
        },
        {
            "label" : "Plot title",
            "el" : document.getElementById("plotTitle"),
            "validations" : []
        },
        {
            "label" : "Label for x axis",
            "el" : document.getElementById("xLabel"),
            "validations" : [{type: "REQUIRED"}]
        }
    ];

    var formValidator = new FormValidator(inputArray);
    if (!formValidator.validateInputForm()) {
        formValidator.display_errors();
        return;
    }
    formParameters['numOfBreaks'] = inputArray[0].el.value;
    formParameters['plotTitle'] = inputArray[1].el.value;
    formParameters['xLabel'] = inputArray[2].el.value;

    var variablesConceptCode = readConceptVariables("divDataNode");
    if (variablesConceptCode == '') {
        Ext.Msg.alert('Missing input!', 'Please drag a concept into the variable box.');
        return;
    }
    formParameters['variablesConceptPath'] = variablesConceptCode;

    return formParameters;
}


var histogramView = new HistogramView();


