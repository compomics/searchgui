# SearchGUI

 * [Introduction](#introduction)
 * [Read Me](#read-me)
 * [Troubleshooting](#troubleshooting)

 * [Bioinformatics for Proteomics Tutorial](http://compomics.com/bioinformatics-for-proteomics)

----

## SearchGUI Publications:
 * Barsnes H and Vaudel M: SearchGUI: a highly adaptable common interface for proteomics search and de novo engines. [J Proteome Res. 2018;17(7):2552-2555](https://www.ncbi.nlm.nih.gov/pubmed/29774740).
 * Vaudel M, Barsnes H, Berven FS, Sickmann A, Martens L: SearchGUI: An open-source graphical user interface for simultaneous OMSSA and X!Tandem searches. [Proteomics 2011;11(5):996-9](http://www.ncbi.nlm.nih.gov/pubmed/21337703). 
 * If you use SearchGUI as part of a publication, please refer to the most recent publication.

----

|   |   |   |
| :------------------------- | :--------------- | :--: |
| [![download](https://github.com/compomics/searchgui/wiki/images/download_button_windows.png)](https://genesis.ugent.be/maven2/eu/isas/searchgui/SearchGUI/4.3.15/SearchGUI-4.3.15-windows.zip) | *v4.3.15 - Windows* | [ReleaseNotes](https://github.com/compomics/searchgui/wiki/ReleaseNotes) | 
| [![download](https://github.com/compomics/searchgui/wiki/images/download_button_unix.png)](https://genesis.ugent.be/maven2/eu/isas/searchgui/SearchGUI/4.3.15/SearchGUI-4.3.15-mac_and_linux.tar.gz) | *v4.3.15 - Mac and Linux* |[ReleaseNotes](https://github.com/compomics/searchgui/wiki/ReleaseNotes) | 

----

|  |  |  |
|:--:|:--:|:--:|
| [![](https://github.com/compomics/searchgui/wiki/images/TaskEditor_small.png)](https://github.com/compomics/searchgui/wiki/images/TaskEditor.png) | [![](https://github.com/compomics/searchgui/wiki/images/ParametersEditor_small.png)](https://github.com/compomics/searchgui/wiki/images/ParametersEditor.png) | [![](https://github.com/compomics/searchgui/wiki/images/ProgressDialog_small.png)](https://github.com/compomics/searchgui/wiki/images/ProgressDialog.png) |

(Click on figure to see the full size version)

----

## Introduction

SearchGUI is a a highly adaptable open-source common interface for configuring and running proteomics search and de novo engines, currently supporting [X! Tandem](http://www.thegpm.org/tandem), [MyriMatch](http://forge.fenchurch.mc.vanderbilt.edu/scm/viewvc.php/*checkout*/trunk/doc/index.html?root=myrimatch), [MS Amanda](http://ms.imp.ac.at/?goto=msamanda), [MS-GF+](https://github.com/MSGFPlus/msgfplus), [OMSSA](http://www.ncbi.nlm.nih.gov/pubmed/15473683), [Comet](https://uwpr.github.io/Comet), [Tide](http://cruxtoolkit.sourceforge.net), [Andromeda](http://www.andromeda-search.org), [MetaMorpheus](https://github.com/smith-chem-wisc/MetaMorpheus), [Sage](https://github.com/lazear/sage), [Novor](http://rapidnovor.com) and [DirecTag](http://fenchurch.mc.vanderbilt.edu/bumbershoot/directag/).

To start using SearchGUI, unzip the downloaded file, and double-click the `SearchGUI-X.Y.Z.jar file`. No additional installation required!

To visualize and analyze the search results we recommend [PeptideShaker](http://compomics.github.io/projects/peptide-shaker.html).

For developer access to the search results we recommend the use of [compomics-utilities](http://compomics.github.io/projects/compomics-utilities.html).

[Go to top of page](#searchgui)

----

## Read Me
 
 * [From the Command Line](#from-the-command-line)
 * [Bioconda](#bioconda)
 * [Docker](#docker)
 * [Easybuild](#easybuild)
 * [Database Help](https://github.com/compomics/searchgui/wiki/DatabaseHelp)
 * [User Defined Modifications](#user-defined-modifications)
 * [Spectrum Data Formats](#spectrum-data-formats)
 * [Result Analysis](#result-analysis)

To start identifying peptides and proteins using SearchGUI, download the latest version, unzip the downloaded file, and double-click on the SearchGUI-X.Y.Z.jar file.

### From the Command Line

The main purpose of SearchGUI is to make it simpler to use multiple search engines at the same time. A graphical user interface is the best choice for smaller projects. SearchGUI can also be used _via_ the command line, and be incorporated in different analysis pipelines.

For details about the command line see: [SearchCLI](https://github.com/compomics/searchgui/wiki/SearchCLI).

[Go to top of page](#searchgui)

----

### Bioconda
[![install with bioconda](https://img.shields.io/badge/install%20with-bioconda-brightgreen.svg?style=flat-square)](http://bioconda.github.io/recipes/searchgui/README.html)
[![install with bioconda](https://anaconda.org/bioconda/searchgui/badges/latest_release_relative_date.svg)](http://bioconda.github.io/recipes/searchgui/README.html)
[![install with bioconda](https://anaconda.org/bioconda/searchgui/badges/downloads.svg)](http://bioconda.github.io/recipes/searchgui/README.html)

SearchGUI is available as a [Miniconda package](http://conda.pydata.org/miniconda.html) in the [bioconda](https://bioconda.github.io) channel [here](https://anaconda.org/bioconda/searchgui). 

You can install SearchGUI with:

```bash
conda install -c conda-forge -c bioconda searchgui 
```
[Go to top of page](#searchgui)

----

### Docker

A [Docker](https://www.docker.com/) container is available via the [Biocontainers](https://quay.io/repository/biocontainers/) repository. You can make use of the container via:

```bash
docker run quay.io/biocontainers/searchgui:X.Y.Z--1  
searchgui eu.isas.searchgui.cmd.IdentificationParametersCLI
```

Replace X.Y.Z with the wanted SearchGUI version number.

You need to have in mind that Docker images don't contain your data into them. If you want to use any data file into a dockerised tool, you will need to map (using `-v` Docker parameter) your local folder containing it into the Docker internal file system, like


```bash
docker run -v /home/my_user/resources:/myresources 
quay.io/biocontainers/searchgui:X.Y.Z--1 
searchgui eu.isas.searchgui.cmd.IdentificationParametersCLI 
-out myresources/parameters_output 
-db /myresources/uniprot-human-reviewed.fasta
```

In this example we are also writing the ouput of the command (`-out` parameter) into the mapped folder in order to write it into our own file system (instead on Docker's container one) and have access to it from our computer after the execution.

[Go to top of page](#searchgui)

----

### Easybuild

A [Easybuild](http://easybuilders.github.io/easybuild) easyconfig file is available in the [Easybuild development branch](https://github.com/easybuilders/easybuild-easyconfigs/blob/develop/easybuild/easyconfigs/s/SearchGUI/SearchGUI-3.3.3-Java-1.8.0_152.eb). SearchGUI can be installed with:


```bash
eb -S SearchGUI-X.Y.Z-Java-1.8.0_152.eb
module load SearchGUI/X.Y.Z-Java-1.8.0_152
```

Replace X.Y.Z with the wanted SearchGUI version number.

The easyconfig provides aliases for the common CLI commands:

```bash
SearchCLI
PathSettingsCLI
FastaCLI
IdentificationParametersCLI
```

[Go to top of page](#searchgui)

----

### User Defined Modifications

It is straightforward to add/edit modifications via the graphical user interface. Modifications will be available in other instances of SearchGUI and [PeptideShaker](http://compomics.github.io/projects/peptide-shaker.html) for the same user/computer. Not all modifications are correctly handled by the search engines. For example, X! Tandem is not compatible with modifications at termini on specific amino acids. Using such a modification will result in nonsense matches which can be filtered out afterwards. This functionality is available by default in [PeptideShaker](http://compomics.github.io/projects/peptide-shaker.html).

[Go to top of page](#searchgui)

----

### Spectrum Data Formats

SearchGUI supports mzML and mgf files as the direct input format for the spectrum files. In addition, [ThermoRawFileParser](https://github.com/compomics/ThermoRawFileParser) is  included, which supports out-of-the-box conversion of Thermo raw files into mzML and mgf.

Furthermore, by referencing the location of your [ProteoWizard](http://proteowizard.sourceforge.net) installtion you may also provide additional raw file types as input, which will then be converted using [msconvert](http://proteowizard.sourceforge.net). Note that this option is only available via the graphical user interface. From the command line you have to run the msconvert command line separatelty.

[Go to top of page](#searchgui)

----

### Result Analysis

To visualize and analyze the SearchGUI results we recommend the use of [PeptideShaker](http://compomics.github.io/projects/peptide-shaker.html). PeptideShaker is a search engine independent platform for visualization of peptide and protein identification results from multiple search engines.

[Go to top of page](#searchgui)

----

## Troubleshooting

 * **Does Not Start I** - Do you have Java installed? Download the latest version of Java [here](https://adoptium.net/temurin/releases) and try again. (You only need the JRE version (and not the JDK version) to run SearchGUI.)

 * **Does Not Start II** - Have you unzipped the zip file? You need to unzip the file before double clicking the jar file. If you get the message "A Java Exception has occurred", you are most likely trying to run SearchGUI from within the zip file. Unzip the file and try again.

 * **Does Not Start III** - Is SearchGUI installed in a path containing special characters, i.e. `[`, `%`, æ, ø, å, etc? If so, move the whole folder to a different location or rename the folder(s) causing the problem and try again. (Note that on Linux SearchGUI has to be run from a path not containing spaces).

 * **Unidentified Developer** - If you run SearchGUI on a Mac you can get the warning _"SearchGUI" can't be opened because it is from an unidentified developer_. To escape this warning control-click on the file icon and then select "Open." This will give you the option of opening it regardless of its unidentified source. This only has to be done once for each SearchGUI version.

 * **Search Engine Issues** - Important: If you have problems with the search engines, please verify that the search engines are working outside of SearchGUI first. To test your installation run the search engine executable on the command line. This should result in output describing what the script does. If you get this, it works, and SearchGUI should run without problems. If not, see below.

 * **X! Tandem XML Syntax Error** - If X! Tandem gives the error "Syntax error parsing XML", the problems is most likely that the path to your database or mgf files contains special characters not supported on your operating system. If this happens try renaming the folders containing the special characters or move the files to folders not containing special characters.

 * **Linux Support** - Users wanting to use SearchGUI on Linux may have to install the search engines first, see the tools web pages for available search engine versions. *Important:* Please verify that the search engines are working outside of SearchGUI before using them inside SearchGUI.

 * **Linux Support II** - If you get problems running makeblastdb (need to prepare FASTA files for OMSSA searches) make sure that you have the required 32 bit libraries. To install the libraries you can use "sudo apt-get install ia32-libs".

 * **MS Amanda Log** - If you encounter problems with MS Amanda it may help to inspect the MS Amanda log files. On Windows these are located here: `C:\ProgramData\MSAmanda`.

 * **MS Amanda on Linux and Mac** - Running MS Amanda on Linux or Mac requires that you have [.NET Core](https://dotnet.microsoft.com/download/dotnet?utm_source=getdotnetcorecli&utm_medium=referral) installed. .NET 5.0 or newer is required.

 * **MyriMatch on Linux** - If you get the error "`locale::facet::_S_create_c_locale name not valid`", this can be fixed by running the command "`export LC_ALL=C`" before running SearchGUI/MyriMatch. To make this fix permanent, put the export line in your .bash_profile (~/.bash_profile). If you still have problems, please contact the [MyriMatch developers](http://www.mc.vanderbilt.edu/root/vumc.php?site=msrc/bioinformatics&doc=27121).

 * **MyriMatch on Linux II** - If you get the error "`myrimatch: loadlocale.c:129: _nl_intern_locale_data: Assertion cnt < (sizeof (_nl_value_type_LC_TIME) / sizeof (_nl_value_type_LC_TIME[0]))' failed.`" (or other locale-related variable than `LC_TIME`), this can be fixed by running the command "`export LC_TIME=C`" before running SearchGUI/MyriMatch. To make this fix permanent, put the export line in your .bash_profile (~/.bash_profile). 

 * **MyriMatch on Windows** - If Myrimatch finishes almost immediately and SearchGUI log shows something like "`MyriMatch finished for * (47.0 milliseconds). Could not find MyriMatch result file`", myrimatch executable may not be running properly. Into its internal path (similar to <SearchGUI>\resources\MyriMatch\windows\windows_64bit, depending on your platform) you can execute it just writing `myriMatch` . If it throws errors about missing libraries like MSVCR100.dll or MSVCP100.dll you will need to install the last version available of them from Microsoft. MSVCR100.dll and MSVCP100.dll need this specific Microsoft Visual C++ package: [Microsoft Visual C++ 2010 Service Pack 1 Redistributable Package MFC Security Update](https://www.microsoft.com/en-us/download/details.aspx?id=26999). Other versions of Visual C++ redistributable libraries may be obtained here:[The latest supported Visual C++ downloads](https://support.microsoft.com/en-us/help/2977003/the-latest-supported-visual-c-downloads)

 * **MetaMorpheus requirements** - 64-bit operating system and .NET Core 3.1. See [https://github.com/smith-chem-wisc/MetaMorpheus](https://github.com/smith-chem-wisc/MetaMorpheus) for more details.

 * **Linux and Mac OSX File Permissions** - On Linux and Mac OSX you may have to edit the permissions for the executable files in order for SearchGUI to work. Allow execution for all users.

 * **32 bits vs 64 bits** - Please make sure that your using versions of the search engines that are compatible with your OS. Note that the latest releases of OMSSA are only available in 64 bits versions. For older versions of OMSSA see the [OMSSA archive](ftp://ftp.ncbi.nlm.nih.gov/pub/lewisg/omssa). However, it is always recommended to use the latest version if possible. Also note that OMSSA versions up until version 2.1.9 does not support precursor mass tolerance in ppm.

 * **Xlib/X11 errorrs** - When running the command lines on systems without a grahpical user interface you may get errors related to X11. If that happens try adding `-Djava.awt.headless=true` to the command line.

 * **Problem Not Solved? Or Problem Not List?** - Contact the developers of SearchGUI by setting up an [issue](https://github.com/compomics/searchgui/issues) describing the problem. If the issue is related to the installation of the search engines, please contact the search engine developers directly.

[Go to top of page](#searchgui)

----
