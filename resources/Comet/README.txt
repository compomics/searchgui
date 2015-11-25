2015/11/24

Comet version "2015.02 rev. 3".
This is a maintenance release of Comet.
http://comet-ms.sourceforge.net

- Fix incorrect MGF parsing where blank lines in the MGF file would cause
  an error.
- Fix n-term distance constraint variable mod searches.
- Change output file extension to ".pin" from ".tsv" for Percolator output
  files.
- Fix negative deltaCn values in text file output when no second hit is
  present.
- Fix case where double decoy string is appended to the protein accession
  for decoy matches.

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
