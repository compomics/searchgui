2021/06/23

Comet version "2021.01 rev. 0".  This is a full release.
http://comet-ms.sourceforge.net

comet.VERSION.win32.exe:  Windows 32-bit binary compiled with VS2019 v142 build tools
comet.VERSION.win64.exe:  Windows 64-bit binary compiled with VS2019 v142 build tools
comet.VERSION.linux.exe:  Linux binary compiled on Centos 6.9 (glibc 2.12)
comet.VERSION.debian.exe: Linux binary compiled on Debian 10.0 (glibc 2.28)

release 2021.01 rev. 0 (2021.01.0), release date 2021/06/23
- Update the expectation value (E-value) calculation by improving the
  determination of the tail region of the xcorr cumulative distribution for
  the linear regression fit.
- New ThreadPool code by D. Shteynberg. The previous code apparently has
  intermittent issues when using many (70+) threads.
- Make flanking (previous and next) residue reporting consistent when a
  peptide is present in a protein multiple times and thus could have different
  flanking residues within the same protein. Previous versions did not
  consistently report the same set of flanking residues in repeated/replicate
  searches. The flanking residues for the first occurrence of the peptide in a
  protein will now always be reported.
- Added a parameter "old_mods_encoding" to enable using the old character
  based modification encodings (e.g. DLYM*NCK) instead mass based encodings
  (e.g. DLYM[15.9949]NCK) in the SQT output files. Add "old_mods_encoding = 1"
  to the comet.params file to use the old modification character encodings.
  This functionality was added to support post-processing tools that have not
  been updated to handle the numeric modification encodings.
- The "print_expect_score" parameter is now deprecated; it will be treated as
  a hidden parameter. Anyone using SQT output who would rather have the Sp
  score instead of the E-value reported will now have to manually add
  "print_expect_score = 0" to their params file.
- Added a no digestion (aka "no_cut", aka don't cleave anywhere) entry to the
  comet.params file.
- The Windows Visual Studio solution is updated to compile with v142 build
  tools using Visual Studio 2019.
- This version of Comet will also run using comet.params files from the
  2020.01 releases as there have been no significant changes to the parameter
  entries.

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

Windows and linux command line binaries are included with this release.


To compile under linux, just type "make"; this should work on the vast majority
of systems. The resulting binary is "comet.exe" in the root directory.

To compile with Windows Visual Studio 2019, load the Comet.sln file, set the
target as "Release" and "x64" (or "Win32"), and build "Comet".  Building the
entire solution may work but I've seen build issues with CometUI which is not
necessary.  For Windows, an additional dependency is Thermo's MSFileReader
library to read RAW files directly.  MSFileReader can be downloaded under "Other
Software Releases" at https://thermo.flexnetoperations.com/control/thmo/login Or
simply skip MSFileReader and the ability to read RAW files directly by addding
"#define _NO_THERMO_RAW" at the head of "MSToolkit/include/Spectrum.h".
