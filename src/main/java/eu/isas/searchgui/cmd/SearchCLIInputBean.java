package eu.isas.searchgui.cmd;

import com.compomics.software.CommandLineUtils;
import com.compomics.util.experiment.identification.parameters_cli.IdentificationParametersCLIParams;
import com.compomics.util.experiment.identification.parameters_cli.IdentificationParametersInputBean;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.SearchGuiOutputOption;
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
     * The identification parameters input.
     */
    private IdentificationParametersInputBean identificationParametersInputBean;
    /**
     * The path settings.
     */
    private PathSettingsCLIInputBean pathSettingsCLIInputBean;
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
     * If true, the mgf files will be checked for size.
     */
    private Boolean checkMgfSize = false;
    /**
     * Number of spectra allowed in the split file.
     */
    private int mgfNSpectra = 25000;
    /**
     * Reference mass for the conversion of the fragment ion tolerance from ppm
     * to Dalton.
     */
    private Double refMass = 2000.0;
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
     * If true, the protein tree will be created.
     */
    private boolean generateProteinTree = false;
    /**
     * The way the output should be organized.
     */
    private SearchGuiOutputOption outputOption = SearchGuiOutputOption.grouped;
    /**
     * Indicates whether the mgf and FASTA files should be included in the
     * output.
     */
    private Boolean outputData = false;
    /**
     * Indicates whether the date should be included in the output file name.
     */
    private Boolean outputDate = false;
    /**
     * If true the X!Tandem file will be renamed.
     */
    private Boolean renameXTandemFile = true;
    /**
     * The tag added after adding decoy sequences to a FASTA file.
     */
    private String targetDecoyFileNameTag = "_concatenated_target_decoy";

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

        // get the mgf size check settings
        if (aLine.hasOption(SearchCLIParams.MGF_CHECK_SIZE.id)) {
            String mgfSizeCheckOption = aLine.getOptionValue(SearchCLIParams.MGF_CHECK_SIZE.id);
            if (mgfSizeCheckOption.trim().equals("1")) {
                checkMgfSize = true;
            }
        }
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
        if (aLine.hasOption(SearchCLIParams.TARGET_DECOY_TAG.id)) {
            arg = aLine.getOptionValue(SearchCLIParams.TARGET_DECOY_TAG.id);
            targetDecoyFileNameTag = arg;
        }

        // set the reference mass
        if (aLine.hasOption(SearchCLIParams.REFERENCE_MASS.id)) {
            arg = aLine.getOptionValue(SearchCLIParams.REFERENCE_MASS.id);
            Double option = new Double(arg);
            refMass = option;
        }

        // load the output preference
        if (aLine.hasOption(SearchCLIParams.OUTPUT_OPTION.id)) {
            int option = new Integer(aLine.getOptionValue(SearchCLIParams.OUTPUT_OPTION.id));
            outputOption = SearchGuiOutputOption.getOutputOption(option);
        }
        if (aLine.hasOption(SearchCLIParams.OUTPUT_DATA.id)) {
            int input = new Integer(aLine.getOptionValue(SearchCLIParams.OUTPUT_DATA.id));
            outputData = input == 1;
        }
        if (aLine.hasOption(SearchCLIParams.OUTPUT_DATE.id)) {
            int input = new Integer(aLine.getOptionValue(SearchCLIParams.OUTPUT_DATE.id));
            outputDate = input == 1;
        }
        if (aLine.hasOption(SearchCLIParams.RENAME_XTANDEM_OUTPUT.id)) {
            int input = new Integer(aLine.getOptionValue(SearchCLIParams.RENAME_XTANDEM_OUTPUT.id));
            renameXTandemFile = input == 1;
        }

        // identification parameters
        identificationParametersInputBean = new IdentificationParametersInputBean(aLine);

        // Path settings
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
        return identificationParametersInputBean.getIdentificationParameters();
    }

    /**
     * Returns the identification parameters file.
     *
     * @return the identification parameters file
     */
    public File getIdentificationParametersFile() {
        if (identificationParametersInputBean.getDestinationFile() != null) {
            return identificationParametersInputBean.getDestinationFile();
        } else {
            return identificationParametersInputBean.getInputFile();
        }
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

        // check the output option
        if (aLine.hasOption(SearchCLIParams.OUTPUT_OPTION.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.OUTPUT_OPTION.id);
            try {
                int option = new Integer(input);
                if (SearchGuiOutputOption.getOutputOption(option) == null) {
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
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.OUTPUT_DATA.id, input)) {
                return false;
            }
        }

        // check the output date option
        if (aLine.hasOption(SearchCLIParams.OUTPUT_DATE.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.OUTPUT_DATE.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.OUTPUT_DATE.id, input)) {
                return false;
            }
        }
        
        // check the rename xtandem output option
        if (aLine.hasOption(SearchCLIParams.RENAME_XTANDEM_OUTPUT.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.RENAME_XTANDEM_OUTPUT.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.RENAME_XTANDEM_OUTPUT.id, input)) {
                return false;
            }
        }
        
        // check the protein index option
        if (aLine.hasOption(SearchCLIParams.PROTEIN_INDEX.id)) {
            String arg = aLine.getOptionValue(SearchCLIParams.PROTEIN_INDEX.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.PROTEIN_INDEX.id, arg)) {
                return false;
            }
        }
        
        // check the target-decoy tag option
        if (aLine.hasOption(SearchCLIParams.TARGET_DECOY_TAG.id)) {
            String arg = aLine.getOptionValue(SearchCLIParams.TARGET_DECOY_TAG.id);
            if (arg.isEmpty()) {
                System.out.println(System.getProperty("line.separator") + "The target-decoy tag cannot be empty!" + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the number of threads
        if (aLine.hasOption(SearchCLIParams.THREADS.id)) {
            String arg = aLine.getOptionValue(SearchCLIParams.THREADS.id);
            if (!IdentificationParametersInputBean.isPositiveInteger(SearchCLIParams.THREADS.id, arg, false)) {
                return false;
            }
        }

        // check the search engine on/off status
        if (aLine.hasOption(SearchCLIParams.OMSSA.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.OMSSA.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.OMSSA.id, input)) {
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.XTANDEM.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.XTANDEM.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.XTANDEM.id, input)) {
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MSGF.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.MSGF.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.MSGF.id, input)) {
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MS_AMANDA.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.MS_AMANDA.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.MS_AMANDA.id, input)) {
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MYRIMATCH.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.MYRIMATCH.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.MYRIMATCH.id, input)) {
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.COMET.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.COMET.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.COMET.id, input)) {
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.TIDE.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.TIDE.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.TIDE.id, input)) {
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.ANDROMEDA.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.ANDROMEDA.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.ANDROMEDA.id, input)) {
                return false;
            }
        }

        // check the search engine folders
        if (aLine.hasOption(SearchCLIParams.OMSSA_LOCATION.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.OMSSA_LOCATION.id);
            File file = new File(input);
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "The " + SearchCLIParams.OMSSA_LOCATION.id + " \'" + input + "\' does not exist." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MAKEBLASTDB_LOCATION.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.MAKEBLASTDB_LOCATION.id);
            File file = new File(input);
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "The " + SearchCLIParams.MAKEBLASTDB_LOCATION.id + " \'" + input + "\' does not exist." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.XTANDEM_LOCATION.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.XTANDEM_LOCATION.id);
            File file = new File(input);
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "The " + SearchCLIParams.XTANDEM_LOCATION.id + " \'" + input + "\' does not exist." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MSGF_LOCATION.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.MSGF_LOCATION.id);
            File file = new File(input);
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "The " + SearchCLIParams.MSGF_LOCATION.id + " \'" + input + "\' does not exist." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MS_AMANDA_LOCATION.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.MS_AMANDA_LOCATION.id);
            File file = new File(input);
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "The " + SearchCLIParams.MS_AMANDA_LOCATION.id + " \'" + input + "\' does not exist." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MYRIMATCH_LOCATION.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.MYRIMATCH_LOCATION.id);
            File file = new File(input);
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "The " + SearchCLIParams.MYRIMATCH_LOCATION.id + " \'" + input + "\' does not exist." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.COMET_LOCATION.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.COMET_LOCATION.id);
            File file = new File(input);
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "The " + SearchCLIParams.COMET_LOCATION.id + " \'" + input + "\' does not exist." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.TIDE_LOCATION.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.TIDE_LOCATION.id);
            File file = new File(input);
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "The " + SearchCLIParams.TIDE_LOCATION.id + " \'" + input + "\' does not exist." + System.getProperty("line.separator"));
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.ANDROMEDA_LOCATION.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.ANDROMEDA_LOCATION.id);
            File file = new File(input);
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "The " + SearchCLIParams.ANDROMEDA_LOCATION.id + " \'" + input + "\' does not exist." + System.getProperty("line.separator"));
                return false;
            }
        }
        
        // check the mgf size filters
        if (aLine.hasOption(SearchCLIParams.MGF_CHECK_SIZE.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.MGF_CHECK_SIZE.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.MGF_CHECK_SIZE.id, input)) {
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MGF_SPLITTING_LIMIT.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.MGF_SPLITTING_LIMIT.id);
            if (!IdentificationParametersInputBean.isPositiveDouble(SearchCLIParams.MGF_CHECK_SIZE.id, input, false)) {
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MGF_MAX_SPECTRA.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.MGF_MAX_SPECTRA.id);
            if (!IdentificationParametersInputBean.isPositiveInteger(SearchCLIParams.MGF_MAX_SPECTRA.id, input, false)) {
                return false;
            }
        }
        
        // check the spectrum title options
        if (aLine.hasOption(SearchCLIParams.DUPLICATE_TITLE_HANDLING.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.DUPLICATE_TITLE_HANDLING.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.DUPLICATE_TITLE_HANDLING.id, input)) {
                return false;
            }
        }
        if (aLine.hasOption(SearchCLIParams.MISSING_TITLE_HANDLING.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.MISSING_TITLE_HANDLING.id);
            if (!IdentificationParametersInputBean.isBooleanInput(SearchCLIParams.MISSING_TITLE_HANDLING.id, input)) {
                return false;
            }
        }
        
        // check the reference mass
        if (aLine.hasOption(SearchCLIParams.REFERENCE_MASS.id)) {
            String input = aLine.getOptionValue(SearchCLIParams.REFERENCE_MASS.id);
            if (!IdentificationParametersInputBean.isPositiveDouble(SearchCLIParams.REFERENCE_MASS.id, input, false)) {
                return false;
            }
        }

        // check the identification parameters
        if (!IdentificationParametersInputBean.isValidStartup(aLine, false)) {
            return false;
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
    public SearchGuiOutputOption getOutputOption() {
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

    /**
     * Returns true if the X! Tandem file should be renamed.
     *
     * @return true if the X! Tandem file should be renamed
     */
    public Boolean renameXTandemFile() {
        if (renameXTandemFile == null) {
            renameXTandemFile = true;
        }
        return renameXTandemFile;
    }

    /**
     * Set if the X! Tandem file should be renamed.
     *
     * @param renameXTandemFile rename file?
     */
    public void setRenameXTandemFile(Boolean renameXTandemFile) {
        this.renameXTandemFile = renameXTandemFile;
    }

    /**
     * Returns the target-decoy file name tag.
     *
     * @return the targetDecoyFileNameTag
     */
    public String getTargetDecoyFileNameTag() {
        if (targetDecoyFileNameTag == null) {
            targetDecoyFileNameTag = "_concatenated_target_decoy";
        }
        return targetDecoyFileNameTag;
    }

    /**
     * Set the target-decoy file name tag.
     *
     * @param targetDecoyFileNameTag the targetDecoyFileNameTag to set
     */
    public void setTargetDecoyFileNameTag(String targetDecoyFileNameTag) {
        this.targetDecoyFileNameTag = targetDecoyFileNameTag;
    }

    /**
     * Returns if the mgf should be checked for size.
     *
     * @return true if the mgf should be checked for size
     */
    public Boolean checkMgfSize() {
        if (checkMgfSize == null) {
            checkMgfSize = false;
        }
        return checkMgfSize;
    }

    /**
     * Set if the mgf should be checked for size.
     *
     * @param checkMgfSize the mgf should be checked for size
     */
    public void setCheckMgfSize(boolean checkMgfSize) {
        this.checkMgfSize = checkMgfSize;
    }

    /**
     * Returns the reference mass for the conversion of the fragment ion
     * tolerance from ppm to Dalton.
     *
     * @return the reference mass for the conversion of the fragment ion
     * tolerance from ppm to Dalton
     */
    public Double getRefMass() {
        if (refMass == null) {
            refMass = 2000.0;
        }
        return refMass;
    }

    /**
     * Sets the reference mass for the conversion of the fragment ion tolerance
     * from ppm to Dalton.
     *
     * @param refMass the reference mass for the conversion of the fragment ion
     * tolerance from ppm to Dalton
     */
    public void setRefMass(Double refMass) {
        this.refMass = refMass;
    }
}
