2016/03/15

Comet version "2016.01 rev. 0".
This is a full release of Comet.
http://comet-ms.sourceforge.net

- Add direct selenocysteine support by retiring parameter entry 
  add_U_user_amino_acid" and adding add_U_selenocysteine" per
  doi:10.1021/acs.jproteome.5b01028.
- Allow negative numbers for the "num_threads" parameter; will subtract
  that many threads from # CPU cores. For example, to use 3 threads for
  a 4-core CPU or 7 threads for an 8-core CPU, set "num_threads = -1".
  This allows your computer to have a CPU core free when running searches.
- Fix bug in the expectation score calculation when "print_expect_score = 0"
  and pep.xml, pin, and txt files are specified (but not .out files).  Under
  this scenario, the cross correlation histogram was not being fully
  accumulated which is what is used to calculate the E-value. This fix
  should result in more accurate E-values. Reported by A. Cheng.
- Update deltaCn calculation for non top-ranked peptides or .pep.xml, .pin
  and .txt outputs.  The deltaCn values for the top ranked hits do not
  change. The lower hit entries had incorrect values associated with them
  (normalized xcorr difference between consecutive entries instead of the
  normalized xcorr difference from the top hit, the latter which is correct).
- This version of Comet will run with comet.params files generated from
  version 2015.02. If you use these parameter files, you will receive a
  warning message about an unknown parameter "add_U_user_amino_acid" that
  you can safely ignore.


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
