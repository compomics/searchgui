2016/03/29

Comet version "2016.01 rev. 1".
This is a maintenance release of Comet.
http://comet-ms.sourceforge.net

- Fixes a bug where variable terminal modifications on decoy peptides are
  not properly generated leading to possible program crash due to calculating
  fragment ions that are too large for the decoy peptide.  Reported by the
  Villen lab.
- Fixes a bug where peptide mass comparisons are failing, leading to
  duplicate peptides being stored and reported as separate entries.  Reported
  by S. Michalakopoulos.


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
