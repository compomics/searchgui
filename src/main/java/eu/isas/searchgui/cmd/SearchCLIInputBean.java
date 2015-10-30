package eu.isas.searchgui.cmd;

import com.compomics.software.CommandLineUtils;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.preferences.IdentificationParameters;
import eu.isas.searchgui.preferences.OutputOption;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;

/**
 * The SearchCLIInputBean reads and stores command line options from a command
 * line.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SearchCLIInputBean {

    /**
     * The spectrum files.
     */
    private ArrayList<File> spectrumFiles;
    /**
     * The output folder.
     */
    private File outputFolder;
    /**
     * The identification parameters.
     */
    private IdentificationParameters identificationParameters;
    /**
     * The search parameters file.
     */
    private File searchParametersFile;
    /**
     * The sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * If true, OMSSA is enabled.
     */
    private boolean omssaEnabled = true;
    /**
     * If true, X!Tandem is enabled.
     */
    private boolean xtandemEnabled = true;
    /**
     * If true, MS-GF+ is enabled.
     */
    private boolean msgfEnabled = true;
    /**
     * If true, MS Amanda is enabled.
     */
    private boolean msAmandaEnabled = true;
    /**
     * If true, MyriMatch is enabled.
     */
    private boolean myriMatchEnabled = true;
    /**
     * If true, Comet is enabled.
     */
    private boolean cometEnabled = true;
    /**
     * If true, Tide is enabled.
     */
    private boolean tideEnabled = true;
    /**
     * If true, Andromeda is enabled.
     */
    private boolean andromedaEnabled = true;
    /**
     * If true, PepNovo+ is enabled.
     */
    private boolean pepnovoEnabled = true;
    /**
     * The folder where OMSSA is installed.
     */
    private File omssaLocation = null;
    /**
     * The folder where X!Tandem is installed.
     */
    private File xtandemLocation = null;
    /**
     * The folder where MS-GF+ is installed.
     */
    private File msgfLocation = null;
    /**
     * The folder where MS Amanda is installed.
     */
    private File msAmandaLocation = null;
    /**
     * The folder where MyriMatch is installed.
     */
    private File myriMatchLocation = null;
    /**
     * The folder where Comet is installed.
     */
    private File cometLocation = null;
    /**
     * The folder where Tide is installed.
     */
    private File tideLocation = null;
    /**
     * The folder where Andromeda is installed.
     */
    private File andromedaLocation = null;
    /**
     * The folder where makeblastdb is installed.
     */
    private File makeblastdbLocation = null;
    /**
     * If an mgf file exceeds this limit, the user will be asked for a split.
     */
    private int mgfMaxSize = 1000;
    /**
     * Number of spectra allowed in the split file.
     */
    private int mgfNSpectra = 25000;
    /**
     * How to handle duplicate spectrum titles. 0: do nothing, 1: rename by
     * adding (2), (3), etc, behind the titles of the duplicated titles, or 2:
     * delete spectra with duplicated titles. Rename is the default.
     */
    private int duplicateSpectrumTitleHandling = 1;
    /**
     * How to handle missing spectrum titles. 0: do nothing, 1: add missing
     * titles. Do nothing is the default.
     */
    private int missingSpectrumTitleHandling = 0;
    /**
     * Number of threads to use. Defaults to the number of cores available.
     */
    private int nThreads = Runtime.getRuntime().availableProcessors();
    /**
     * The current species.
     */
    private String species;
    /**
     * The current species type.
     */
    private String speciesType;
    /**
     * If true, the protein tree will be created.
     */
    private boolean generateProteinTree = false;
    /**
     * The path settings.
     */
    private PathSettingsCLIInputBean pathSettingsCLIInputBean;
    /**
     * The way the output should be organized.
     */
    private OutputOption outputOption = null;
    /**
     * Indicates whether the mgf and FASTA files should be included in the
     * output.
     */
    private Boolean outputData = null;
    /**
     * Indicates whether the date should be included in the output file name.
     */
    private Boolean outputDate = null;

    /**
     * Takes all the arguments from a command line.
     *
     * @param aLine the command line
     * @throws FileNotFoundException thrown if the spectrum, search parameter or
     * FASTA files are not found
     * @throws IOException thrown if an error occurred while reading the FASTA
     * file
     * @throws ClassNotFoundException thrown if the search parameters cannot be
     * converted
     */
    public SearchCLIInputBean(CommandLine aLine) throws FileNotFoundException, IOException, ClassNotFoundException {

        // get the files needed for the search
        String spectrumFilesTxt = aLine.getOptionValue(SearchCLIParams.SPECTRUM_FILES.id);
        spectrumFiles = getSpectrumFiles(spectrumFilesTxt);

        // output folder
        String arg = aLine.getOptionValue(SearchCLIParams.OUTPUT_FOLDER.id);
        outputFolder = new File(arg);

        // identification parameters
        String fileTxt = aLine.getOptionValue(SearchCLIParams.IDENTIFICATION_PARAMETERS.id);
        searchParametersFile = new File(fileTxt);
        SearchParameters searchParameters = SearchParameters.getIdentificationParameters(searchParametersFile);
        identificationParameters = new IdentificationParameters(searchParameters);

        // override the fasta file location
        if (aLine.hasOption(SearchCLIParams.FASTA_FILE.id)) {
            String newPath = aLine.getOptionValue(SearchCLIParams.FASTA_FILE.id);
            File fastaFile = new File(newPath);
            searchParameters.setFastaFile(fastaFile);
        }

        // load the fasta file
        sequenceFactory.loadFastaFile(searchParameters.getFastaFile(), null);

        // get the mgf splitting limits
        if (aLine.hasOption(SearchCLIParams.MGF_SPLITTING_LIMIT.id)) {
            arg = aLine.getOptionValue(SearchCLIParams.MGF_SPLITTING_LIMIT.id);
            Integer option = new Integer(arg);
            mgfMaxSize = option;
        }
        if (aLine.hasOption(SearchCLIParams.MGF_MAX_SPECTRA.id)) {
            arg = aLine.getOptionValue(SearchCLIParams.MGF_MAX_SPECTRA.id);
            Integer option = new Integer(arg);
            mgfNSpectra = option;
        }

        // see which search engines to use
        if (aLine.hasOption(SearchCLIParams.OMSSA.id)) {
            String omssaOption = aLine.getOptionValue(SearchCLIParams.OMSSA.id);
            if (omssaOption.trim().equals("0")) {
                omssaEnabled = false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.XTANDEM.id)) {
            String xtandemOption = aLine.getOptionValue(SearchCLIParams.XTANDEM.id);
            if (xtandemOption.trim().equals("0")) {
                xtandemEnabled = false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MSGF.id)) {
            String msgfOption = aLine.getOptionValue(SearchCLIParams.MSGF.id);
            if (msgfOption.trim().equals("0")) {
                msgfEnabled = false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MS_AMANDA.id)) {
            String msAmandaOption = aLine.getOptionValue(SearchCLIParams.MS_AMANDA.id);
            if (msAmandaOption.trim().equals("0")) {
                msAmandaEnabled = false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MYRIMATCH.id)) {
            String myriMatchOption = aLine.getOptionValue(SearchCLIParams.MYRIMATCH.id);
            if (myriMatchOption.trim().equals("0")) {
                myriMatchEnabled = false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.COMET.id)) {
            String cometOption = aLine.getOptionValue(SearchCLIParams.COMET.id);
            if (cometOption.trim().equals("0")) {
                cometEnabled = false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.TIDE.id)) {
            String tideOption = aLine.getOptionValue(SearchCLIParams.TIDE.id);
            if (tideOption.trim().equals("0")) {
                tideEnabled = false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.ANDROMEDA.id)) {
            String andromedaOption = aLine.getOptionValue(SearchCLIParams.ANDROMEDA.id);
            if (andromedaOption.trim().equals("0")) {
                andromedaEnabled = false;
            }
        }

        // search engine folders
        if (aLine.hasOption(SearchCLIParams.OMSSA_LOCATION.id)) {
            String omssaFolder = aLine.getOptionValue(SearchCLIParams.OMSSA_LOCATION.id);
            omssaLocation = new File(omssaFolder);
        }
        if (aLine.hasOption(SearchCLIParams.XTANDEM_LOCATION.id)) {
            String omssaFolder = aLine.getOptionValue(SearchCLIParams.XTANDEM_LOCATION.id);
            xtandemLocation = new File(omssaFolder);
        }
        if (aLine.hasOption(SearchCLIParams.MSGF_LOCATION.id)) {
            String msgfFolder = aLine.getOptionValue(SearchCLIParams.MSGF_LOCATION.id);
            msgfLocation = new File(msgfFolder);
        }
        if (aLine.hasOption(SearchCLIParams.MS_AMANDA_LOCATION.id)) {
            String msAmandaFolder = aLine.getOptionValue(SearchCLIParams.MS_AMANDA_LOCATION.id);
            msAmandaLocation = new File(msAmandaFolder);
        }
        if (aLine.hasOption(SearchCLIParams.MYRIMATCH_LOCATION.id)) {
            String myriMatchFolder = aLine.getOptionValue(SearchCLIParams.MYRIMATCH_LOCATION.id);
            myriMatchLocation = new File(myriMatchFolder);
        }
        if (aLine.hasOption(SearchCLIParams.COMET_LOCATION.id)) {
            String cometFolder = aLine.getOptionValue(SearchCLIParams.COMET_LOCATION.id);
            cometLocation = new File(cometFolder);
        }
        if (aLine.hasOption(SearchCLIParams.TIDE_LOCATION.id)) {
            String tideFolder = aLine.getOptionValue(SearchCLIParams.TIDE_LOCATION.id);
            tideLocation = new File(tideFolder);
        }
        if (aLine.hasOption(SearchCLIParams.ANDROMEDA_LOCATION.id)) {
            String andromedaFolder = aLine.getOptionValue(SearchCLIParams.ANDROMEDA_LOCATION.id);
            andromedaLocation = new File(andromedaFolder);
        }

        // makeblastdb folder
        if (aLine.hasOption(SearchCLIParams.MAKEBLASTDB_LOCATION.id)) {
            String makeblastdbFolder = aLine.getOptionValue(SearchCLIParams.MAKEBLASTDB_LOCATION.id);
            makeblastdbLocation = new File(makeblastdbFolder);
        }

        // check how duplicate spectrum titles should be handled
        if (aLine.hasOption(SearchCLIParams.DUPLICATE_TITLE_HANDLING.id)) {
            arg = aLine.getOptionValue(SearchCLIParams.DUPLICATE_TITLE_HANDLING.id);
            Integer option = new Integer(arg);
            if (option == 0 || option == 1 || option == 2) {
                duplicateSpectrumTitleHandling = option;
            } else {
                throw new IllegalArgumentException("Unknown value \'" + option + "\' for " + SearchCLIParams.DUPLICATE_TITLE_HANDLING.id + ".");
            }
        }

        // check how missing spectrum titles should be handled
        if (aLine.hasOption(SearchCLIParams.MISSING_TITLE_HANDLING.id)) {
            arg = aLine.getOptionValue(SearchCLIParams.MISSING_TITLE_HANDLING.id);
            Integer option = new Integer(arg);
            if (option == 0 || option == 1) {
                missingSpectrumTitleHandling = option;
            } else {
                throw new IllegalArgumentException("Unknown value \'" + option + "\' for " + SearchCLIParams.MISSING_TITLE_HANDLING.id + ".");
            }
        }

        // get the number of threads
        if (aLine.hasOption(SearchCLIParams.THREADS.id)) {
            arg = aLine.getOptionValue(SearchCLIParams.THREADS.id);
            nThreads = new Integer(arg);
        }

        // get if the protein tree is to be generated
        if (aLine.hasOption(SearchCLIParams.PROTEIN_INDEX.id)) {
            arg = aLine.getOptionValue(SearchCLIParams.PROTEIN_INDEX.id);
            Integer option = new Integer(arg);
            if (option == 0 || option == 1) {
                generateProteinTree = option == 1;
            } else {
                throw new IllegalArgumentException("Unknown value \'" + option + "\' for " + SearchCLIParams.PROTEIN_INDEX.id + ".");
            }
        }

        // get the species
        if (aLine.hasOption(SearchCLIParams.SPECIES.id)) {
            species = aLine.getOptionValue(SearchCLIParams.SPECIES.id);
        }
        if (aLine.hasOption(SearchCLIParams.SPECIES_TYPE.id)) {
            speciesType = aLine.getOptionValue(SearchCLIParams.SPECIES_TYPE.id);
        }

        // load the output preference
        if (aLine.hasOption(SearchCLIParams.OUTPUT_OPTION.id)) {
            int option = new Integer(aLine.getOptionValue(SearchCLIParams.OUTPUT_OPTION.id));
            outputOption = OutputOption.getOutputOption(option);
        }
        if (aLine.hasOption(SearchCLIParams.OUTPUT_DATA.id)) {
            int input = new Integer(aLine.getOptionValue(SearchCLIParams.OUTPUT_DATA.id));
            outputData = input == 1;
        }
        if (aLine.hasOption(SearchCLIParams.OUTPUT_DATE.id)) {
            int input = new Integer(aLine.getOptionValue(SearchCLIParams.OUTPUT_DATE.id));
            outputDate = input == 1;
        }

        pathSettingsCLIInputBean = new PathSettingsCLIInputBean(aLine);
    }

    /**
     * Return the spectrum files.
     *
     * @return the spectrum files
     */
    public ArrayList<File> getSpectrumFiles() {
        return spectrumFiles;
    }

    /**
     * Returns the output folder.
     *
     * @return the output folder
     */
    public File getOutputFile() {
        return outputFolder;
    }

    /**
     * Returns the identification parameters.
     *
     * @return the identification parameters
     */
    public IdentificationParameters getIdentificationParameters() {
        return identificationParameters;
    }

    /**
     * Returns the search parameters file.
     *
     * @return the search parameters file
     */
    public File getSearchParametersFile() {
        return searchParametersFile;
    }

    /**
     * Returns a list of spectrum files as imported from the command line
     * option.
     *
     * @param optionInput the command line option
     * @return a list of file candidates
     * @throws FileNotFoundException exception thrown whenever a file is not
     * found
     */
    public static ArrayList<File> getSpectrumFiles(String optionInput) throws FileNotFoundException {
        ArrayList<String> extentions = new ArrayList<String>();
        extentions.add(".mgf");
        return CommandLineUtils.getFiles(optionInput, extentions);
    }

    /**
     * Returns true if OMSSA is to be used.
     *
     * @return true if OMSSA is to be used
     */
    public boolean isOmssaEnabled() {
        return omssaEnabled;
    }

    /**
     * Returns true if X!Tandem is to be used.
     *
     * @return if X!Tandem is to be used
     */
    public boolean isXTandemEnabled() {
        return xtandemEnabled;
    }

    /**
     * Returns true if MS-GF+ is to be used.
     *
     * @return if MS-GF+ is to be used
     */
    public boolean isMsgfEnabled() {
        return msgfEnabled;
    }

    /**
     * Returns true if MS Amanda is to be used.
     *
     * @return if MS Amanda is to be used
     */
    public boolean isMsAmandaEnabled() {
        return msAmandaEnabled;
    }

    /**
     * Returns true if MyriMatch is to be used.
     *
     * @return if MyriMatch is to be used
     */
    public boolean isMyriMatchEnabled() {
        return myriMatchEnabled;
    }

    /**
     * Returns true if Comet is to be used.
     *
     * @return if Comet is to be used
     */
    public boolean isCometEnabled() {
        return cometEnabled;
    }

    /**
     * Returns true if Tide is to be used.
     *
     * @return if Tide is to be used
     */
    public boolean isTideEnabled() {
        return tideEnabled;
    }

    /**
     * Returns true if Andromeda is to be used.
     *
     * @return if Andromeda is to be used
     */
    public boolean isAndromedaEnabled() {
        return andromedaEnabled;
    }

    /**
     * Returns the OMSSA location, null if none is set.
     *
     * @return the OMSSA location
     */
    public File getOmssaLocation() {
        return omssaLocation;
    }

    /**
     * Returns the X!Tandem location.
     *
     * @return the xtandem location
     */
    public File getXtandemLocation() {
        return xtandemLocation;
    }

    /**
     * Returns the MS-GF+ location.
     *
     * @return the msgf location
     */
    public File getMsgfLocation() {
        return msgfLocation;
    }

    /**
     * Returns the MS Amanda location.
     *
     * @return the MS Amanda location
     */
    public File getMsAmandaLocation() {
        return msAmandaLocation;
    }

    /**
     * Returns the MyriMatch location.
     *
     * @return the mMyriMatch location
     */
    public File getMyriMatchLocation() {
        return myriMatchLocation;
    }

    /**
     * Returns the Comet location.
     *
     * @return the cometL location
     */
    public File getCometLocation() {
        return cometLocation;
    }

    /**
     * Returns the Tide location.
     *
     * @return the Tide location
     */
    public File getTideLocation() {
        return tideLocation;
    }

    /**
     * Returns the Andromeda location.
     *
     * @return the Andromeda location
     */
    public File getAndromedaLocation() {
        return andromedaLocation;
    }

    /**
     * Returns the makeblastdb location.
     *
     * @return the makeblastdb location
     */
    public File getMakeblastdbLocation() {
        return makeblastdbLocation;
    }

    /**
     * Returns the max mgf file size before splitting.
     *
     * @return the mgfMaxSize
     */
    public int getMgfMaxSize() {
        return mgfMaxSize;
    }

    /**
     * Get the max number of spectra in an mgf file.
     *
     * @return the mgfNSpectra
     */
    public int getMgfNSpectra() {
        return mgfNSpectra;
    }

    /**
     * Returns how duplicate spectrum titles are to be handled. 0: do nothing
     * (default), 1: rename by adding (2), (3), etc, behind the titles of the
     * duplicated titles, or 2: delete spectra with duplicated titles.
     *
     * @return how duplicate spectrum titles are to be handled
     */
    public int getDuplicateSpectrumTitleHandling() {
        return duplicateSpectrumTitleHandling;
    }

    /**
     * Set how duplicate spectrum titles are to be handled. 0: do nothing, 1:
     * rename by adding (2), (3), etc, behind the titles of the duplicated
     * titles, or 2: delete spectra with duplicated titles.
     *
     * @param duplicateSpectrumTitleHandling the duplicateSpectrumTitleHandling
     * to set
     */
    public void setDuplicateSpectrumTitleHandling(int duplicateSpectrumTitleHandling) {
        this.duplicateSpectrumTitleHandling = duplicateSpectrumTitleHandling;
    }

    /**
     * Returns how missing spectrum titles are to be handled. 0: do nothing, 1:
     * add missing titles.
     *
     * @return how missing spectrum titles are to be handled
     */
    public int getMissingSpectrumTitleHandling() {
        return missingSpectrumTitleHandling;
    }

    /**
     * Set how missing spectrum titles are to be handled. 0: do nothing, 1: add
     * missing titles.
     *
     * @param missingSpectrumTitleHandling the missingSpectrumTitleHandling to
     * set
     */
    public void setMissingSpectrumTitleHandling(int missingSpectrumTitleHandling) {
        this.missingSpectrumTitleHandling = missingSpectrumTitleHandling;
    }

    /**
     * Returns the number of threads to use.
     *
     * @return the number of threads to use
     */
    public int getNThreads() {
        return nThreads;
    }

    /**
     * Returns the species.
     *
     * @return the species
     */
    public String getSpecies() {
        return species;
    }

    /**
     * Returns the species type.
     *
     * @return the species type
     */
    public String getSpeciesType() {
        return speciesType;
    }

    /**
     * Verifies the command line start parameters.
     *
     * @param aLine the command line to validate
     *
     * @return true if the startup was valid
     *
     * @throws IOException if the spectrum file(s) are not found
     */
    public static boolean isValidStartup(CommandLine aLine) throws IOException {

        if (aLine.getOptions().length == 0) {
            return false;
        }

        // check the spectrum files
        if (!aLine.hasOption(SearchCLIParams.SPECTRUM_FILES.id) || ((String) aLine.getOptionValue(SearchCLIParams.SPECTRUM_FILES.id)).equals("")) {
            System.out.println(System.getProperty("line.separator") + "Spectrum files not specified." + System.getProperty("line.separator"));
            return false;
        } else {
            ArrayList<File> tempSpectrumFiles = SearchCLIInputBean.getSpectrumFiles(aLine.getOptionValue(SearchCLIParams.SPECTRUM_FILES.id));
            for (File file : tempSpectrumFiles) {
                if (!file.exists()) {
                    System.out.println(System.getProperty("line.separator") + "Spectrum file \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                    return false;
                }
            }
        }

        // check the output folder
        if (!aLine.hasOption(SearchCLIParams.OUTPUT_FOLDER.id) || ((String) aLine.getOptionValue(SearchCLIParams.OUTPUT_FOLDER.id)).equals("")) {
            System.out.println(System.getProperty("line.separator") + "Output folder not specified." + System.getProperty("line.separator"));
            return false;
        } else {
            File file = new File(((String) aLine.getOptionValue(SearchCLIParams.OUTPUT_FOLDER.id)));
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "Output folder \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the optional fasta
        if (aLine.hasOption(SearchCLIParams.FASTA_FILE.id)) {
            File file = new File(((String) aLine.getOptionValue(SearchCLIParams.FASTA_FILE.id)));
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "FASTA file \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the id params
        if (!aLine.hasOption(SearchCLIParams.IDENTIFICATION_PARAMETERS.id) || ((String) aLine.getOptionValue(SearchCLIParams.IDENTIFICATION_PARAMETERS.id)).equals("")) {
            System.out.println(System.getProperty("line.separator") + "Identification parameters file not specified." + System.getProperty("line.separator"));
            return false;
        } else {
            File file = new File(((String) aLine.getOptionValue(SearchCLIParams.IDENTIFICATION_PARAMETERS.id)));
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "Identification parameters file \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the output option
        if (aLine.hasOption(SearchCLIParams.OUTPUT_OPTION.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.OUTPUT_OPTION.id);
            try {
                int option = new Integer(input);
                if (OutputOption.getOutputOption(option) == null) {
                    System.out.println(System.getProperty("line.separator") + "Output option \'" + option + "\' not recognized." + System.getProperty("line.separator"));
                    return false;
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "Output option \'" + input + "\' not recognized." + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the output data option
        if (aLine.hasOption(SearchCLIParams.OUTPUT_DATA.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.OUTPUT_DATA.id);
            try {
                int option = new Integer(input);
                if (option != 0 && option != 1) {
                    System.out.println(System.getProperty("line.separator") + "Output data argument should be 0 or 1. \'" + option + "\' not recognized." + System.getProperty("line.separator"));
                    return false;
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "Output data argument should be 0 or 1. \'" + input + "\' not recognized." + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the output date option
        if (aLine.hasOption(SearchCLIParams.OUTPUT_DATE.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.OUTPUT_DATE.id);
            try {
                int option = new Integer(input);
                if (option != 0 && option != 1) {
                    System.out.println(System.getProperty("line.separator") + "Output date argument should be 0 or 1. \'" + option + "\' not recognized." + System.getProperty("line.separator"));
                    return false;
                }
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "Output date argument should be 0 or 1. \'" + input + "\' not recognized." + System.getProperty("line.separator"));
                return false;
            }
        }

        return true;
    }

    /**
     * Returns if the protein tree is to be generated or not.
     *
     * @return the generateProteinTree
     */
    public boolean isGenerateProteinTree() {
        return generateProteinTree;
    }

    /**
     * Set if the protein tree is to be generated or not.
     *
     * @param generateProteinTree the generateProteinTree to set
     */
    public void setGenerateProteinTree(boolean generateProteinTree) {
        this.generateProteinTree = generateProteinTree;
    }

    /**
     * Returns the path settings provided by the user.
     *
     * @return the path settings provided by the user
     */
    public PathSettingsCLIInputBean getPathSettingsCLIInputBean() {
        return pathSettingsCLIInputBean;
    }

    /**
     * Returns the output option chosen by the user. Null if not set.
     *
     * @return the output option chosen by the user
     */
    public OutputOption getOutputOption() {
        return outputOption;
    }

    /**
     * Indicates whether input data should be included in the output.
     *
     * @return whether input data should be included in the output
     */
    public Boolean isOutputData() {
        return outputData;
    }

    /**
     * Indicates whether the date should be included in the output name.
     *
     * @return whether the date should be included in the output name
     */
    public Boolean isOutputDate() {
        return outputDate;
    }
}
