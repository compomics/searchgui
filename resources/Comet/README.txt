2015/09/30

Comet version "2015.02 rev. 1".
This is a maintenance release of Comet.
http://comet-ms.sourceforge.net

- Modify behavior the binary modifications which is controlled by the third
  parameter entry in the variable modifications (e.g. "variable_mod01"). Instead
  of a binary 0 or 1 value to turn off or on each binary modification, one can
  now set the third parameter entry to the same value across multiple variable
  modifications effectively allowing an all-modified binary behavior across
  multiple variable modifications. See the examples at the bottom of the
  variable modification help pages for further explanation.
- Wide mass tolerance searches, such as those performed by the Gygi lab's
  recent mass-tolerant search paper, are now supported by Comet. Previous
  versions of Comet would crash when given large tolerances.
- Update MSToolkit to support "possible charge state" cvParam in mzML files as
  implemented by M. Hoopmann.

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
