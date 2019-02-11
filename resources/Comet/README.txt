2018/12/05

Comet version "2018.01 rev. 3".
This is a maintenance release of Comet.
http://comet-ms.sourceforge.net


release 2018.01 rev. 3 (2018.01.3), release date 2018/12/05
- Bug fix: the "clip_nterm_methionine" parameter has been broken since the
  2017.01.0 release; it works again.  Thanks to A.T.Guler for reporting the
  bug.
- Bug fix: add a missing tab in Crux-compiled text output
- Bug fix: complex searches with many variable modifications could cause an
  integer counter value to become too large (negative or undefined) resulting
  in an error in post-processing.  Thanks to N. Dong for reporting this bug.

release 2018.01 rev. 2 (2018.01.2), release date 2018/06/13
- Buf fix: remove version update check.  In some instances, timeout and host
  access issues were causing Comet to abort searches.  The "skip_updatecheck"
  parameter is now deprecated.

release 2018.01 rev. 1 (2018.01.1), release date 2018/05/16
- Bug fix: "equal_I_and_L" was not properly implemented in previously releases
  This parameter controls whether Comet treats isoleucine and leucine residues
  as being the same (yes by default) since they cannot be distinguished in
  most data.
- For Crux compiled version, set the output files to be basename.txt instead
  of basename.target.txt for instances when "decoy_search" is not set to "2"
  i.e. targets only or combined targets plus decoys. This also applies to .sqt
  and .pep.xml outputs.

release 2018.01 rev. 0 (2018.01.0), release date 2018/04/26
- In the interest of run time performance, only a single PEFF modification at
  a time is applied to any given peptide. PEFF modifications are also not
  applied to a peptide that contains a PEFF variant (amino acid substitution).
- In the "modified_peptide" peptide string in the "modification_info" element
  of a pep.xml file, static n- and c-terminal modifications were previously
  reported in the peptide (e.g. "n[230]DIGSTK"). As only variable amino acid
  modifications are reported in the "modified_peptide" string, Comet will now
  just report termini modifications in this peptide string if they contain a
  variable modification.
- If any input file reports no spectra searched (such as an mzML files without
  a scan index), the incomplete output files are removed and a non-zero exit
  code is returned.
- Comet will now check if there is an updated version available and report if
  so. This also triggers a Comet Google analytics hit. Setting
  "skip_updatecheck = 1" will skip this.
- "isotope_error = 5" will search the -1/0/1/2/3 C13 offsets which is what
  used to be available prior to versions 2017.01. The -1 C13 isotope offset
  really makes no sense but we've seen cases where a wrong isotope peak, one
  C13 less than the monoisotopic peak, is listed as the precursor peak. This
  can occur when a noise peak or a peak from a different peptide appears at
  the -1 mass location.
- Bug fix: Starting with 2017.01 rev. 0, not all permutations of variable
  modifications get analyzed when multiple variable modification is specified.
  The permutations of modifications would be terminated at the first
  permutation occurrence of two variable modifications on the same residue.
  This is now fixed.
- Bug fix: PEFF parsing of modifications would previously terminate at the
  first space which could occur within the text of a modification description
  causing an incomplete set of PEFF modifications to be analyzed; this is now
  fixed.
- This version of Comet will run with comet.params files from version 2017.01.

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
