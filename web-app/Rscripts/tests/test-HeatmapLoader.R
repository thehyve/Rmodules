require("tools")

context("Heatmap analyses (vanilla, HClustered, and KMeans)")

test_that("function Heatmap.loader", {
    source("../Heatmap/HeatmapLoader.R")
    fileListBefore <- list.files()
    
    test.input.filenames <- c("datasets/HeatmapLoader-test1.input")
    
    for (test.input.filename in test.input.filenames) {
        input.filename.stem <- file_path_sans_ext(test.input.filename)
        
        result <- Heatmap.loader(test.input.filename)
        
        expect_true(file.exists("Heatmap.png") && file.info("Heatmap.png")$size > 0)
        expect_true(file.exists("Heatmap.svg") && file.info("Heatmap.svg")$size > 0)
        
        answer.filename <- paste(input.filename.stem, ".answer", sep = "")
        load(answer.filename)
        
        expect_identical(result, answer)
        
        file.remove(setdiff(list.files(), fileListBefore))
    }
})

CreateHeatmapDataSets <- function(input.filename) {
    source("../Heatmap/HeatmapLoader.R")
    
    require("tools")
    input.filename.stem <- file_path_sans_ext(input.filename)
    
    answer <- Heatmap.loader(input.filename)
    
    answer.filename <- paste(input.filename.stem, ".answer", sep = "")
    save(answer, file = answer.filename)
}



