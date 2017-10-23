2017/10/17

Comet version "2017.01 rev. 1".
This is a maintenance release of Comet.
http://comet-ms.sourceforge.net

release 2017.01 rev. 1 (2017.01.1), release date 2017/10/17
- Report all duplicate proteins as a comma separated list under the "proteinId1" column in
  Percolator output (output_percolatorfile); rev. 0 release reported just a single protein
  identifier.
- Bug fix: a decoy peptide that appears multiple times within a decoy protein would
  previously cause that decoy protein to be reported multiple times.
- Bug fix: add a missing comma separating protein list in text output (output_txtfile) when
  performing a combined target-decoy search ("decoy_search = 2"). The missing comma was
  between the target protein list and the decoy protein list.
- Bug fix: the modification mass reported in the "mass" attribute of the "mod_aminoacid_mass"
  element in pep.xml output could be wrong due to an error in mixing static and variable
  modifications inappropriately.  This has been fixed.  Bug reported by D. Shteynberg.

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
