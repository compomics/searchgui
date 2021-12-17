Comet is an open source MS/MS database search engine released under the Apache
2.0 license.

Current supported input formats are mzXML, mzML, mgf, ms2, cms2, and Thermo RAW
files (under Windows).  Current supported output formats are pepXML, Percolator
pin (tsv), SQT, tab-delimited text, and mzIdentML.

To run a search on an input file requires the Comet binary, a search parameters
file (comet.params), an input file, and a protein sequence database in FASTA
format.  The syntax of a search assuming the binary is named "comet.exe":

   comet.exe input.mzXML
   comet.exe input.ms2

Search parameters, such as which sequence database to query, modifications to
consider, mass tolerances, output format, etc. are defined in the comet.params
file.

One can generate a parameters file with the command

   comet.exe -p

Rename the created file "comet.params.new" to "comet.params".

See https://github.com/UWPR/Comet for more details.