2017/11/08

Comet version "2017.01 rev. 2".
This is a maintenance release of Comet.
http://comet-ms.sourceforge.net

release 2017.01 rev. 2 (2017.01.2), release date 2017/11/08
- In SQT, text and Percolator outputs, static n-term and c-term modifications
  were being reported in the peptide string.  These are no longer being
  reported and the peptide string now correctly only includes variable
  modification mass differences if present.
- In the SQT output, a third entry in the L protein lines is an integer
  position of the start position of the peptide in the protein sequence.  The
  first L entry reported the correct start position but the position was off by
  one in subsequent duplicate proteins.  This is corrected.  I want to thank
  P. Wilmarth for reporting these issues.

Comet is an open source MS/MS database search engine released under the
Apache 2.0 license.

Current supported input formats are mzXML, mzML, mgf, ms2, and cms2.
Current supported output formats are pepXML, Percolator tsv, SQT,
tab-delimited text, and .out files.

To run a search on an input file requires the Comet binary, a search
parameters file (comet.params), an input file, and a protein sequence
database in FASTA format.  The syntax of a search:

   comet.exe input.mzXML
   comet.exe input.ms2

Search parameters, such as which sequence database to query, modifications to
consider, mass tolerances, output format, etc. are defined in the comet.params
file.

One can generate a parameters file with the command

   comet.exe -p

Rename the created file "comet.params.new" to "comet.params".

Windows and linux command line binaries are included with this release.
