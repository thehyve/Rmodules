require("tools")

context("LineGraph analysis")

test_that("function LineGraph.loader", {
    source("../LineGraph/LineGraphLoader.r")
    fileListBefore <- list.files()

    test.input.filenames <- c("datasets/LineGraphLoader_test1.input")

    for (test.input.filename in test.input.filenames) {

        result <- LineGraph.loader(test.input.filename)

        expect_true(file.exists("LineGraph.png") && file.info("LineGraph.png")$size > 0)

        answer.filename <- paste(file_path_sans_ext(test.input.filename), ".answer", sep = "")
        load(answer.filename)

        expect_identical(result, answer)

        file.remove(setdiff(list.files(), fileListBefore))
    }
})

CreateLineGraphDataSets <- function(input.filename) {
    source("../LineGraph/LineGraphLoader.r")
    save(LineGraph.loader(input.filename), file = paste(file_path_sans_ext(input.filename), ".answer", sep = ""))
}




