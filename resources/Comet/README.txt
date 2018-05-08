2018/04/26

Comet version "2018.01 rev. 0".
This is a full release of Comet.
http://comet-ms.sourceforge.net

release 2017.01 rev. 4 (2017.01.4), release date 2018/02/20
- In the interest of run time performance, only a single PEFF modification at a
  time is applied to any given peptide. PEFF modifications are also not applied
  to a peptide that contains a PEFF variant (amino acid substitution).
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
  so. This also triggers a Comet Google analytics hit . Setting
  "skip_updatecheck = 1" will skip this.
- "isotope_error = 5" will search the -1/0/1/2/3 C13 offsets which is what
  used to be available prior to versions 2017.01. The -1 C13 isotope offset
  really makes no sense but we've seen cases where a wrong isotope peak, one
  C13 less than the monoisotopic peak, is listed as the precursor peak. This
  can occur when a noise peak or a peak from a different peptide appears at the
  -1 mass location.
- Bug fix: PEFF parsing of modifications would previously terminate at the
  first space which could occur within the text of a modification description
  causing an incomplete set of PEFF modifications to be analyzed; this is now
  fixed.
- Bug fix: Starting with 2017.01 rev. 0, not all permutations of variable
  modifications get analyzed when multiple variable modification is specified.
  The permutations of modifications would be terminated at the first permutation
  occurrence of two variable modifications on the same residue. This is fixed.
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
