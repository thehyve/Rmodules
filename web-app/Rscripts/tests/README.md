This readme file describes the planned and ongoing work undertaken in the tranSMART Rmodules branch 'features/RScriptsAddTestsAndCleanUp'.

Work is done by:
Tim Dorscheidt, tim@thehyve.nl


Goals:

The first goal will be to encase each R analysis in a functional testsuite. Each R-script is run by being provided one or more data files and some arguments, after which data and image files are expected in return. To ensure minimal stability of the R code under active development, each analysis needs a set of input data that can be fed to the analysis to check whether the returned results still correspond to pregenerated and validated answers. 

After implementing these general behaviour tests, it is my intention to start modularising the code and testing the more specific functions.

After that, I would like to add more consistent checking and error handling.


Adding tests:

I have chosen the 'testthat' package as a testing framework:
http://cran.r-project.org/web/packages/testthat
This package has been created by Hadley Wickham, the author of some of the most popular R packages, and is under active development (in contrast to 'RUnit', the only other serious testing package I could find).


Design:

All tests are placed within:
../Rmodules/web-app/Rscripts/tests
And the input and corresponding answer data files are located in:
../Rmodules/web-app/Rscripts/tests/datasets
Within the 'tests' folder, each analysis gets its own test file:
test-CorrelationLoader.R
This test file will also contain code to generate new datasets if needed.


Notes:

All testing files expect your active working directory within your R session to be set to:
../Rmodules/web-app/Rscripts/tests

Most analyses do not return results in a form other than an image. To enable easy testing, I have chosen to have these methods return a list containing all the results relevant for testing.
