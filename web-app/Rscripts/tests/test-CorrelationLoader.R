require("tools")

context("Correlation analysis")

test_that("the function Correlation.loader works properly", {
    source("../Correlation/CorrelationLoader.r")

    test.input.filenames <- c("datasets/CorrelationLoader_test1.input", "datasets/CorrelationLoader_test2.input")
    options.correlation.by = c("subject", "variable")
    options.correlation.method = c("pearson", "kendall", "spearman") # see ?cor

    for (test.input.filename in test.input.filenames) {
        input.filename.stem <- file_path_sans_ext(test.input.filename)
        for (by in options.correlation.by) {
            for (method in options.correlation.method) {
                Correlation.loader(test.input.filename, correlation.by = by, correlation.method = method)
                answer.filename <- paste(input.filename.stem, "_", by, "_", method, ".answer", sep = "")
                expect_that(file.exists(plot.image.filename), is_true())
                expect_equivalent(md5sum(correlation.result.filename), md5sum(answer.filename))
            }
        }
    }
})


CreateCorrelationDataSets <- function(input.filename) {
    source("../Correlation/CorrelationLoader.r")
    
    options.correlation.by = c("subject", "variable")
    options.correlation.method = c("pearson", "kendall", "spearman")  # see ?cor
    
    require("tools")
    input.filename.stem <- file_path_sans_ext(input.filename)
    
    for (by in options.correlation.by) {
        for (method in options.correlation.method) {
            Correlation.loader(input.filename, correlation.by = by, correlation.method = method)
            
            answer.filename <- paste(input.filename.stem, "_", by, "_", method, ".answer", sep = "")
            file.rename(from = correlation.result.filename, to = answer.filename)
        }
    }
}


