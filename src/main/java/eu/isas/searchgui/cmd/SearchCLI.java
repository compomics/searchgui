package eu.isas.searchgui.cmd;

import com.compomics.software.CompomicsWrapper;
import com.compomics.software.settings.PathKey;
import com.compomics.software.settings.UtilitiesPathPreferences;
import com.compomics.util.Util;
import com.compomics.util.experiment.biology.*;
import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.io.massspectrometry.MgfIndex;
import com.compomics.util.experiment.io.massspectrometry.MgfReader;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.gui.filehandling.TempFilesManager;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.ProcessingPreferences;
import eu.isas.searchgui.SearchHandler;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import eu.isas.searchgui.preferences.SearchGUIPathPreferences;
import eu.isas.searchgui.utilities.Properties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;
import org.apache.commons.cli.*;

/**
 * This class can be used to control SearchGUI in command line.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SearchCLI implements Callable {

    /**
     * The command line parameters.
     */
    private SearchCLIInputBean searchCLIInputBean;
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory;
    /**
     * The spectrum factory.
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();

    /**
     * Construct a new SearchCLI runnable from a list of arguments. When
     * initialization is successful, calling "run" will start SearchGUI and
     * write the output files when finished.
     *
     * @param args the command line arguments
     */
    public SearchCLI(String[] args) {

        try {

            try {
                SpeciesFactory speciesFactory = SpeciesFactory.getInstance();
                speciesFactory.initiate(getJarFilePath());
            } catch (Exception e) {
                System.out.println("An error occurred while loading the species.");
                e.printStackTrace();
            }

            Options lOptions = new Options();
            SearchCLIParams.createOptionsCLI(lOptions);
            BasicParser parser = new BasicParser();
            CommandLine line = parser.parse(lOptions, args);

            if (!SearchCLIInputBean.isValidStartup(line)) {
                PrintWriter lPrintWriter = new PrintWriter(System.out);
                lPrintWriter.print(System.getProperty("line.separator") + "======================" + System.getProperty("line.separator"));
                lPrintWriter.print("SearchCLI" + System.getProperty("line.separator"));
                lPrintWriter.print("======================" + System.getProperty("line.separator"));
                lPrintWriter.print(getHeader());
                lPrintWriter.print(SearchCLIParams.getOptionsAsString());
                lPrintWriter.flush();
                lPrintWriter.close();

                System.exit(0);
            } else {
                searchCLIInputBean = new SearchCLIInputBean(line);
                call();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calling this method will run the configured SearchCLI process.
     */
    public Object call() {

        PathSettingsCLIInputBean pathSettingsCLIInputBean = searchCLIInputBean.getPathSettingsCLIInputBean();

        if (pathSettingsCLIInputBean.getLogFolder() != null) {
            redirectErrorStream(pathSettingsCLIInputBean.getLogFolder());
        }

        if (pathSettingsCLIInputBean.hasInput()) {
            PathSettingsCLI pathSettingsCLI = new PathSettingsCLI(pathSettingsCLIInputBean);
            pathSettingsCLI.setPathSettings();
        } else {
            try {
                File pathConfigurationFile = new File(getJarFilePath(), UtilitiesPathPreferences.configurationFileName);
                if (pathConfigurationFile.exists()) {
                    SearchGUIPathPreferences.loadPathPreferencesFromFile(pathConfigurationFile);
                }
            } catch (Exception e) {
                System.out.println("An error occurred when setting path configuration. Default paths will be used.");
                e.printStackTrace();
            }
            try {
                ArrayList<PathKey> errorKeys = SearchGUIPathPreferences.getErrorKeys(getJarFilePath());
                if (!errorKeys.isEmpty()) {
                    System.out.println("Unable to write in the following configuration folders. Please use a temporary folder, "
                            + "the path configuration command line, or edit the configuration paths from the graphical interface.");
                    for (PathKey pathKey : errorKeys) {
                        System.out.println(pathKey.getId() + ": " + pathKey.getDescription());
                    }
                }
            } catch (Exception e) {
                System.out.println("Unable to load the path configurations. Default pathswill be used.");
            }
        }

        // load enzymes
        enzymeFactory = EnzymeFactory.getInstance();

        try {
            // @TODO: not sure if this is the best place to perform the mgf validation and splitting??
            WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();

            // @TODO: merge with code from the gui (and make it gui independent!)
            // validate that all the spectra has unique spectrum titles
            for (File tempMgfFile : searchCLIInputBean.getSpectrumFiles()) {
                waitingHandlerCLIImpl.appendReport("Validating MGF file: " + tempMgfFile.getAbsolutePath(), true, true);

                // index the spectrum file
                spectrumFactory.addSpectra(tempMgfFile, waitingHandlerCLIImpl);
                File indexFile = new File(tempMgfFile.getParent(), tempMgfFile.getName() + ".cui");

                // check for missing spectrum titles
                if (spectrumFactory.getIndex(indexFile).getSpectrumTitles().size() < spectrumFactory.getIndex(indexFile).getNSpectra()) {
                    if (searchCLIInputBean.getMissingSpectrumTitleHandling() == 0) {
                        if (spectrumFactory.getIndex(indexFile).getSpectrumTitles().isEmpty()) {
                            waitingHandlerCLIImpl.appendReport("Warning: No spectrum titles found in file: " + tempMgfFile.getAbsolutePath() + "! "
                                    + "Titles are mandatory. See the missing_titles option. File will be ignored.", true, true);
                        } else {
                            waitingHandlerCLIImpl.appendReport("Warning: Spectrum titles missing in file: " + tempMgfFile.getAbsolutePath() + "! "
                                    + "Titles are mandatory. See the missing_titles option. File will be ignored.", true, true);
                        }
                        return false;
                    } else {
                        // add missing spectrum titles
                        waitingHandlerCLIImpl.appendReport("Adding missing spectrum titles in file: " + tempMgfFile.getAbsolutePath(), true, true);
                        spectrumFactory.closeFiles();
                        MgfReader.addMissingSpectrumTitles(tempMgfFile, waitingHandlerCLIImpl);
                        spectrumFactory.addSpectra(tempMgfFile, waitingHandlerCLIImpl);
                    }
                }

                // check for lack of peak picking
                if (!spectrumFactory.getIndex(indexFile).isPeakPicked()) {
                    waitingHandlerCLIImpl.appendReport("Warning: The file \'" + tempMgfFile.getName() + "\' contains zero intensity peaks. "
                            + "It is highly recommended to apply peak picking before starting a search!", true, true);
                }

                // check for ms2 spectra
                if (spectrumFactory.getIndex(indexFile).getMaxPeakCount() == 0) {
                    waitingHandlerCLIImpl.appendReport("Warning: No MS2 spectra found in file: " + tempMgfFile.getName() + "! File will be ignored.", true, true);
                    return false;
                }

                // check for duplicate headers
                HashMap<String, Integer> duplicatedSpectrumTitles = spectrumFactory.getIndex(indexFile).getDuplicatedSpectrumTitles();

                if (duplicatedSpectrumTitles != null && duplicatedSpectrumTitles.size() > 0) {
                    waitingHandlerCLIImpl.appendReport("Warning: The spectrum file contains non-unique spectrum titles!", true, true);

                    // rename or delete spectra with duplicated spectrum titles
                    if (searchCLIInputBean.getDuplicateSpectrumTitleHandling() == 1) {
                        waitingHandlerCLIImpl.appendReport("Renaming duplicated spectrum titles in file: " + tempMgfFile.getAbsolutePath(), true, true);
                        spectrumFactory.closeFiles();
                        MgfReader.renameDuplicateSpectrumTitles(tempMgfFile, null);
                        spectrumFactory.addSpectra(tempMgfFile, waitingHandlerCLIImpl);
                    } else if (searchCLIInputBean.getDuplicateSpectrumTitleHandling() == 2) {
                        waitingHandlerCLIImpl.appendReport("Removing spectra with duplicated titles in file: " + tempMgfFile.getAbsolutePath(), true, true);
                        spectrumFactory.closeFiles();
                        MgfReader.removeDuplicateSpectrumTitles(tempMgfFile, null);
                        spectrumFactory.addSpectra(tempMgfFile, waitingHandlerCLIImpl);
                    }
                }
            }

            // get the spectrum files
            ArrayList<File> spectrumFiles = new ArrayList<File>();

            // see if we need to split any of the mgf files
            ArrayList<File> fatMgfFiles = new ArrayList<File>();
            for (File tempMgfFile : searchCLIInputBean.getSpectrumFiles()) {
                if (searchCLIInputBean.checkMgfSize() && tempMgfFile.length() > (((long) searchCLIInputBean.getMgfMaxSize()) * 1048576)) {
                    fatMgfFiles.add(tempMgfFile);
                } else {
                    spectrumFiles.add(tempMgfFile);
                }
            }

            // did we find any mgf files that are too big?
            if (!fatMgfFiles.isEmpty()) {
                waitingHandlerCLIImpl.appendReportEndLine();
                waitingHandlerCLIImpl.appendReport("MGF files requires splitting. (See options: mgf_splitting and mgf_spectrum_count.)", true, true);
                ArrayList<File> splitMgfs = splitFiles(fatMgfFiles, waitingHandlerCLIImpl);
                if (splitMgfs != null) {
                    for (File tempMgfFile : splitMgfs) {
                        spectrumFiles.add(tempMgfFile);
                    }
                }

                waitingHandlerCLIImpl.appendReport("Current MGF input (listed in \"output_folder\"\\searchGUI_input.txt): ", true, true);
                for (File tempMgfFile : spectrumFiles) {
                    waitingHandlerCLIImpl.appendReport(tempMgfFile.getAbsolutePath(), false, true);
                }
            }

            // Processing
            ProcessingPreferences processingPreferences = new ProcessingPreferences();
            processingPreferences.setnThreads(searchCLIInputBean.getNThreads());

            // Identification parameters
            IdentificationParameters identificationParameters = searchCLIInputBean.getIdentificationParameters();
            File parametersFile = searchCLIInputBean.getIdentificationParametersFile();
            if (parametersFile == null) {
                String name = identificationParameters.getName();
                if (name == null) {
                    name = "SearchCLI.par";
                } else {
                    name += ".par";
                }
                parametersFile = new File(searchCLIInputBean.getOutputFile(), name);
                IdentificationParameters.saveIdentificationParameters(identificationParameters, parametersFile);
            }

            // Load the fasta file in the factory
            SearchParameters searchParameters = identificationParameters.getSearchParameters();
            String error = SearchHandler.loadModifications(searchParameters);
            if (error != null) {
                System.out.println(error);
            }

            UtilitiesUserPreferences userPreferences = UtilitiesUserPreferences.loadUserPreferences();
            userPreferences.setTargetDecoyFileNameSuffix(searchCLIInputBean.getTargetDecoyFileNameTag());
            userPreferences.setRefMass(searchCLIInputBean.getRefMass());
            userPreferences.setRenameXTandemFile(searchCLIInputBean.renameXTandemFile());
            userPreferences.setOutputOption(searchCLIInputBean.getOutputOption());
            userPreferences.setOutputData(searchCLIInputBean.isOutputData());
            userPreferences.setIncludeDateInOutputName(searchCLIInputBean.isOutputDate());
            UtilitiesUserPreferences.saveUserPreferences(userPreferences);

            File fastaFile = searchParameters.getFastaFile();
            SequenceFactory.getInstance().loadFastaFile(fastaFile);

            // @TODO: validate the mgf files: see SearchGUI.validateMgfFile
            SearchHandler searchHandler = new SearchHandler(identificationParameters,
                    searchCLIInputBean.getOutputFile(), spectrumFiles,
                    new ArrayList<File>(), parametersFile,
                    searchCLIInputBean.isOmssaEnabled(), searchCLIInputBean.isXTandemEnabled(),
                    searchCLIInputBean.isMsgfEnabled(), searchCLIInputBean.isMsAmandaEnabled(),
                    searchCLIInputBean.isMyriMatchEnabled(), searchCLIInputBean.isCometEnabled(),
                    searchCLIInputBean.isTideEnabled(), searchCLIInputBean.isAndromedaEnabled(),
                    searchCLIInputBean.isNovorEnabled(), searchCLIInputBean.isDirecTagEnabled(),
                    searchCLIInputBean.getOmssaLocation(), searchCLIInputBean.getXtandemLocation(),
                    searchCLIInputBean.getMsgfLocation(), searchCLIInputBean.getMsAmandaLocation(),
                    searchCLIInputBean.getMyriMatchLocation(), searchCLIInputBean.getCometLocation(),
                    searchCLIInputBean.getTideLocation(), searchCLIInputBean.getAndromedaLocation(),
                    searchCLIInputBean.getNovorLocation(), searchCLIInputBean.getDirecTagLocation(),
                    searchCLIInputBean.getMakeblastdbLocation(),
                    processingPreferences);

            File logFolder = pathSettingsCLIInputBean.getLogFolder();
            if (logFolder != null) {
                searchHandler.setLogFolder(logFolder);
            }

            // incrementing the counter for a new SearchGUI start
            if (userPreferences.isAutoUpdate()) {
                Util.sendGAUpdate("UA-36198780-2", "startrun-cl", "searchgui");
            }

            searchHandler.startSearch(waitingHandlerCLIImpl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            TempFilesManager.deleteTempFolders();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Splits the given MGF files.
     *
     * @param mgfFiles the files to split
     * @param waitingHandler the waiting handler
     * @return the split mgf files
     */
    private ArrayList<File> splitFiles(ArrayList<File> mgfFiles, WaitingHandler waitingHandler) {

        ArrayList<File> splitMgfFiles = new ArrayList<File>();
        MgfReader mgfReader = new MgfReader();

        for (File originalFile : mgfFiles) {

            ArrayList<MgfIndex> indexes;
            waitingHandler.appendReport("Splitting " + originalFile.getName() + ". Please Wait...", true, true);

            try {
                indexes = mgfReader.splitFile(originalFile, searchCLIInputBean.getMgfNSpectra(), waitingHandler);
            } catch (FileNotFoundException e) {
                waitingHandler.appendReport("File " + originalFile.getName() + " not found.", true, true);
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                waitingHandler.appendReport("An error occurred while reading/writing the mgf file.", true, true);
                e.printStackTrace();
                return null;
            } catch (OutOfMemoryError error) {
                waitingHandler.appendReport("SearchGUI used up all the available memory and had to be stopped.\n"
                        + "Memory boundaries are set in the Edit menu (Edit > Java Options).", true, true);
                error.printStackTrace();
                return null;
            }

            try {
                if (indexes != null && !indexes.isEmpty()) {
                    waitingHandler.appendReport("Writing Indexes. Please Wait...", true, true);
                    for (MgfIndex currentIndex : indexes) {
                        spectrumFactory.writeIndex(currentIndex, originalFile.getParentFile());
                    }
                }
            } catch (IOException e) {
                waitingHandler.appendReport("An error occurred while writing an mgf index.", true, true);
                e.printStackTrace();
                return null;
            }

            for (MgfIndex currentIndex : indexes) {
                File newFile = new File(originalFile.getParent(), currentIndex.getFileName());
                splitMgfFiles.add(newFile);
            }
        }

        waitingHandler.appendReport("MGF file(s) split and selected.", true, true);
        return splitMgfFiles;
    }

    /**
     * SearchCLI header message when printing the usage.
     */
    private static String getHeader() {
        return System.getProperty("line.separator")
                + "SearchCLI searches spectrum files according to search parameters using multiple search engines." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Spectra must be provided in the Mascot Generic File (mgf) format." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "The identification parameters can be provided as a file as saved from the GUI or generated using the IdentificationParametersCLI." + System.getProperty("line.separator")
                + "See http://compomics.github.io/compomics-utilities/wiki/identificationparameterscli.html for more details." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "For further help see http://compomics.github.io/projects/searchgui.html and http://compomics.github.io/searchgui/wiki/searchcli.html." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Or contact the developers at https://groups.google.com/group/peptide-shaker." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "----------------------"
                + System.getProperty("line.separator")
                + "OPTIONS"
                + System.getProperty("line.separator")
                + "----------------------" + System.getProperty("line.separator")
                + "\n";
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new SearchCLI(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * redirects the error stream to the PeptideShaker.log of a given folder.
     *
     * @param logFolder the folder where to save the log
     */
    public static void redirectErrorStream(File logFolder) {

        try {
            logFolder.mkdirs();
            File file = new File(logFolder, "SearchGUI.log");
            System.setErr(new java.io.PrintStream(new FileOutputStream(file, true)));

            System.err.println(System.getProperty("line.separator") + System.getProperty("line.separator") + new Date()
                    + ": SearchGUI version " + new Properties().getVersion() + ".");
            System.err.println("Memory given to the Java virtual machine: " + Runtime.getRuntime().maxMemory() + ".");
            System.err.println("Total amount of memory in the Java virtual machine: " + Runtime.getRuntime().totalMemory() + ".");
            System.err.println("Free memory: " + Runtime.getRuntime().freeMemory() + ".");
            System.err.println("Java version: " + System.getProperty("java.version") + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        return CompomicsWrapper.getJarFilePath(this.getClass().getResource("SearchCLI.class").getPath(), "SearchGUI");
    }
}
