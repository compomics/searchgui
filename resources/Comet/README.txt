2020/04/06

Comet version "2019.01 rev. 5".
This is a minor maintenance release.
http://comet-ms.sourceforge.net

comet.VERSION.win32.exe:  Windows 32-bit binary compiled with VS2017
comet.VERSION.win64.exe:  Windows 64-bit binary compiled with VS2017
comet.VERSION.linux.exe:  Linux binary compiled on Centos 6.9 (glibc 2.12)
comet.VERSION.debian.exe: Linux binary compiled on Debian 10.0 (glibc 2.28)

release 2019.01 rev. 5 (2019.01.5), release date 2020/04/06
- Bug fix: a bug in the code could randomly set the "minimum_peaks"
  parameter value to a random large number, causing no peaks to be read
  in, resulting in blank search results reported. Thanks to C. Bielow
  for not only reporting the bug but also debugging the code and
  implementing the fix.

release 2019.01 rev. 4 (2019.01.4), release date 2020/01/15
- Add support for searching mzML and mzXML files that do not contain the
  optional scan index.  Such files would previously not have been searched
  as MSToolkit would throw an error message about the missing index.
- Bug fix: PEFF substitutions on flanking residues, generating a new
  peptide that would otherwise not be analyzed, are treated more rigorously.

release 2019.01 rev. 3 (2019.01.3), release date 2019/12/06
- Bug fix: Searches against mzML files would report the wrong scan
  number. The issue was due to the MSToolkit update for release 2019.01.0.
  Using the latest MSToolkit commit 2021e7e from 12/3/19 fixes this error.
  Thanks to Z. Sun for reporting the bug.

release 2019.01 rev. 2 (2019.01.2), release date 2019/11/18
- Bug fix: introduced in the 2019.01 rev. 0 release, Comet would not
  properly handle a "clip_nterm_methionine_" search. This bug would
  manifest as either a segmentation fault or as a NULL character
  reported for a flanking residue due to not properly tracking the
  shortened protein length when the start methione is clipped off.
  Thanks to the Villen Lab and R. Johnson for reporting the issue.

release 2019.01 rev. 1 (2019.01.1), release date 2019/09/06
- Known bug: a NULL character can show up as the flanking residue
  (peptide_next_aa attribute in pep.xml output) for an internal decoy
  match.
- In Percolator .pin output, change ExpMass and CalcMass from neutral
  masses to singly protonated masses.  Thanks to W. Fondrie and the
  Crux team for reporting that Percolator expects these masses to be
  singly protonated.
- Bug fix:  correct missing residue in StaticMod header entry of SQT
  output.  Thanks to A. Zelter for reporting the issue.
- Bug fix:  update database indexing and index search to correctly
  handle terminal variable modifications.  Thanks to T. Zhao for
  reporting the issue.

release 2019.01 rev. 0 (2018.01.0), release date 2019/08/19
- Add support for user specified fragment neutral loss ions (such 
  as phosphate neutral loss) via the addition of an 8th field to 
  the "variable_mod01 to variable_mod09" parameters.
- Add support for user specified precursor neutral loss ions using 
  the "precursor_NL_ions" parameter.
- Add "max_duplicate_proteins" parameter which can limit the number 
  of protein identifiers reported/returned for a given peptide.
- Add "peptide_length_range" parameter. This parameter allows the 
  specification of a minimum and maximum peptide length.
- Add "search_enzyme2_number" parameter. Allows optional selection 
  of a second digestion enzyme. Enzyme specificity and missed 
  cleavage settings are are shared between both 
  "search_enzyme_number" and "search_enzyme2_number".
- Update "max_variable_mods_in_peptide" parameter to support value 0.
- In the example comet.params files available here. and the when 
  generated using "comet -p", the "spectrum_batch_size" parameter 
  is now set to 15000 instead of 0. For high-res "fragment_bin_tol" 
  settings, Comet should use less than 6GB of memory with a 15K 
  batch size with very little impact on search times compared to 
  loading and searching all spectra at once. If you have a computer 
  with lots of ram, feel free to set this parameter value to 0 to 
  gain a bit of search speed. Note that when this parameter is 
  missing, its default value is 0.
- Added indexed database support. This was implemented to support 
  real-time searching. A database index can be created with the 
  command "comet.exe -i" which will create an indexed database of 
  the database specified in the comet.params file. Static and 
  variable modifications specified in the comet.params file will be 
  stored in the index database. Note an indexed database (with .idx 
  extension) can be searched if you specify the indexed database 
  file as the search database ("database_name"). Searching an 
  indexed database only uses a single thread so the "num_threads" 
  parameter is ignored if an indexed database is specified.
- Support real-time searching of single spectra against an indexed 
  database with a C# interface for CometWrapper.dll. This includes 
  an example C# program (RealtimeSearch/Search.cs) that loops 
  through all scans of a Thermo raw file using Thermo's 
  RawFileReader and sequentially calling DoSingleSpectrumSearch() 
  to run the search.
- Bug fix: error with logic in analysis in target-decoy searches 
  where erroneous target and decoy fragment ions could be 
  generated.
- Bug fix: add support for parsing '*' in the sequence which was 
  removed in the 2018.01 rev. 0 release. This character is treated 
  as a stop codon; residues flanking this character are valid 
  search enzyme termini.
- Features that didn't make the cut and are targeted for the next 
  release: mzIdentML output and Comet-PTM functionality.


Comet is an open source MS/MS database search engine released under 
the Apache 2.0 license.

Current supported input formats are mzXML, mzML, mgf, ms2, cms2, 
and Thermo RAW files (under Windows).  Current supported output 
formats are pepXML, Percolator pin (tsv), SQT, tab-delimited text, 
and .out files.

To run a search on an input file requires the Comet binary, a 
search parameters file (comet.params), an input file, and a protein 
sequence database in FASTA format.  The syntax of a search:

   comet.exe input.mzXML
   comet.exe input.ms2

Search parameters, such as which sequence database to query, modifications
to consider, mass tolerances, output format, etc. are defined in the
comet.params file.

One can generate a parameters file with the command

   comet.exe -p

Rename the created file "comet.params.new" to "comet.params".

Windows and linux command line binaries are included with this release.


To compile under linux, just type "make"; this should work on the vast
majority of systems. The resulting binary is "comet.exe" in the root
directory.

To compile with Windows Visual Studio 2017, load the Comet.sln file, set the
target as "Release" and "x64" (or "Win32"), and build "Comet".  Building the
entire solution may work but I've seen build issues with CometUI which is not
necessary.  For Windows, an additional dependency is Thermo's MSFileReader
library to read RAW files directly.  MSFileReader can be downloaded under
"Other Software Releases" at
   https://thermo.flexnetoperations.com/control/thmo/login
Or simply skip MSFileReader and the ability to read RAW files directly by
addding "# define _NO_THERMO_RAW" at the head of these files
   MSToolkit/include/MSReader.h
   MSToolkit/src/RAWReader.cpp
