package eu.isas.searchgui;

import com.compomics.software.CommandLineUtils;
import com.compomics.software.CompomicsWrapper;
import com.compomics.util.Util;
import com.compomics.util.db.DerbyUtil;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.AndromedaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MyriMatchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.OmssaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.TideParameters;
import com.compomics.util.experiment.io.massspectrometry.export.AplExporter;
import com.compomics.util.experiment.io.massspectrometry.export.Ms2Exporter;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.experiment.massspectrometry.proteowizard.MsConvertParameters;
import com.compomics.util.gui.filehandling.TempFilesManager;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingDialog;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.io.ConfigurationFile;
import com.compomics.util.io.compression.ZipUtils;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.ProcessingPreferences;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.waiting.Duration;
import eu.isas.searchgui.preferences.OutputOption;
import eu.isas.searchgui.processbuilders.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;

/**
 * This class represents the Search command line interface.
 *
 * @author Marc Vaudel
 * @author Lennart Martens
 * @author Harald Barsnes
 */
public class SearchHandler {

    /**
     * A file where the input files used will be stored.
     */
    private final static String SEARCHGUI_INPUT = "searchGUI_input.txt";
    /**
     * The waiting handler used during the search.
     */
    private WaitingHandler waitingHandler;
    /**
     * Default search engines location.
     */
    public static final String SEARCH_GUI_CONFIGURATION_FILE = "searchGUI_configuration.txt";
    /**
     * Enzymes file.
     */
    private static String enzymeFile = "resources/conf/searchGUI_enzymes.xml";
    /**
     * Folder where the output is stored before packaging.
     */
    public final static String OUTPUT_TEMP_FOLDER_NAME = ".SearchGUI_temp";
    /**
     * The sub folder to use to store peak lists.
     */
    private final static String PEAK_LIST_SUBFOLDER = "peak_lists";
    /**
     * If set to true SearchGUI is ran from the command line only, i.e., no GUI
     * will appear.
     */
    private static boolean useCommandLine = false;
    /**
     * The worker which will coordinate the searches.
     */
    private SearchWorker searchWorker;
    /**
     * Worker which indexes files.
     */
    private IndexingWorker indexingWorker;
    /**
     * Worker which builds the protein tree.
     */
    private ProteinTreeWorker proteinTreeWorker;
    /**
     * If true the X!Tandem file will be renamed.
     */
    private boolean renameXTandemFile = true;
    /**
     * If true the protein tree will be created parallel to the searches.
     */
    private boolean generateProteinTree = false;
    /**
     * The results folder.
     */
    private File resultsFolder;
    /**
     * If true, OMSSA will be used.
     */
    private boolean enableOmssa = true;
    /**
     * If true, X!Tandem will be used.
     */
    private boolean enableXtandem = true;
    /**
     * If true, MS-GF+ will be used.
     */
    private boolean enableMsgf = true;
    /**
     * If true, MS Amanda will be used.
     */
    private boolean enableMsAmanda = true;
    /**
     * If true, MyriMatch will be used.
     */
    private boolean enableMyriMatch = true;
    /**
     * If true, Comet will be used.
     */
    private boolean enableComet = true;
    /**
     * If true, Tide will be used.
     */
    private boolean enableTide = true;
    /**
     * If true, Andromeda will be used.
     */
    private boolean enableAndromeda = false;
    /**
     * If true, PeptideShaker will be used.
     */
    private boolean enablePeptideShaker = false;
    /**
     * If true, Reporter will be used.
     */
    private boolean enableReporter = true;
    /**
     * The identification parameters.
     */
    private IdentificationParameters identificationParameters;
    /**
     * The file where to store the identification parameters.
     */
    private File identificationParametersFile;
    /**
     * The raw files.
     */
    private ArrayList<File> rawFiles;
    /**
     * The mgf files.
     */
    private ArrayList<File> mgfFiles;
    /**
     * The OMSSA location.
     */
    private File omssaLocation = null;
    /**
     * The X!Tandem location.
     */
    private File xtandemLocation = null;
    /**
     * The MS-GF+ location.
     */
    private File msgfLocation = null;
    /**
     * The MS Amanda location.
     */
    private File msAmandaLocation = null;
    /**
     * The MyriMatch location.
     */
    private File myriMatchLocation = null;
    /**
     * The Comet location.
     */
    private File cometLocation = null;
    /**
     * The Tide location.
     */
    private File tideLocation = null;
    /**
     * The Andromeda location.
     */
    private File andromedaLocation = null;
    /**
     * The makeblastdb location.
     */
    private File makeblastdbLocation;
    /**
     * The PeptideShaker experiment label.
     */
    private String experimentLabel;
    /**
     * The PeptideShaker sample label.
     */
    private String sampleLabel;
    /**
     * The PeptideShaker replicate number.
     */
    private Integer replicateNumber = 0;
    /**
     * The PeptideShaker CPS file.
     */
    private File peptideShakerFile = null;
    /**
     * The mascot dat files.
     */
    private ArrayList<File> mascotFiles = new ArrayList<File>();
    /**
     * The msconvert process.
     */
    private ArrayList<MsConvertProcessBuilder> msConvertProcessBuilders = null;
    /**
     * The makeblastdb process.
     */
    private MakeblastdbProcessBuilder makeblastdbProcessBuilder = null;
    /**
     * The OMSSA process.
     */
    private OmssaclProcessBuilder omssaProcessBuilder = null;
    /**
     * The X!Tandem process.
     */
    private TandemProcessBuilder xTandemProcessBuilder = null;
    /**
     * The MS-GF+ process.
     */
    private MsgfProcessBuilder msgfProcessBuilder = null;
    /**
     * The MS Amanda process.
     */
    private MsAmandaProcessBuilder msAmandaProcessBuilder = null;
    /**
     * The MyriMatch process.
     */
    private MyriMatchProcessBuilder myriMatchProcessBuilder = null;
    /**
     * The Comet process.
     */
    private CometProcessBuilder cometProcessBuilder = null;
    /**
     * The Tide index process.
     */
    private TideIndexProcessBuilder tideIndexProcessBuilder = null;
    /**
     * The Tide search process.
     */
    private TideSearchProcessBuilder tideSearchProcessBuilder = null;
    /**
     * The Andromeda process.
     */
    private AndromedaProcessBuilder andromedaProcessBuilder = null;
    /**
     * The PeptideShaker process.
     */
    private PeptideShakerProcessBuilder peptideShakerProcessBuilder = null;
    /**
     * The processing preferences.
     */
    private ProcessingPreferences processingPreferences = new ProcessingPreferences();
    /**
     * The msconvert parameters.
     */
    private MsConvertParameters msConvertParameters;
    /**
     * The way output files should be exported.
     */
    private OutputOption outputOption = OutputOption.grouped;
    /**
     * Indicates whether data files (mgf and FASTA) should be copied in the
     * output.
     */
    private boolean outputData = false;
    /**
     * Indicates whether the date should be included in the output.
     */
    private boolean includeDateInOutputName = false;
    /**
     * Default name for a SearchGUI output.
     */
    public final static String defaultOutput = "searchgui_out.zip";
    /**
     * The name of the folder where to save the mgf and FASTA file.
     */
    public final static String defaultDataFolder = "data";
    /**
     * Default SearchGUI configurations.
     */
    public static final String SEARCHGUI_CONFIGURATION_FILE = "searchGUI_configuration.txt";
    /**
     * Handler for exceptions.
     */
    private ExceptionHandler exceptionHandler;
    /**
     * A folder to use to store temporary files.
     */
    private static String tempFolderPath = null;
    /**
     * The duration of the search.
     */
    private Duration searchDuration;

    /**
     * Constructor for the SearchGUI command line interface. Uses the
     * configuration file searchGUI_configuration.txt to get the default search
     * engine locations and which search engines that are enabled. Mainly for
     * use via the graphical UI.
     *
     * @param identificationParameters the identification parameters
     * @param resultsFolder the results folder
     * @param mgfFiles list of peak list files in the mgf format
     * @param rawFiles list of raw files
     * @param identificationParametersFile the identification parameters file
     * @param processingPreferences the processing preferences
     * @param generateProteinTree if true, the protein tree will be generated
     * @param exceptionHandler a handler for exceptions
     */
    public SearchHandler(IdentificationParameters identificationParameters, File resultsFolder, ArrayList<File> mgfFiles, ArrayList<File> rawFiles, File identificationParametersFile, ProcessingPreferences processingPreferences, boolean generateProteinTree, ExceptionHandler exceptionHandler) {

        this.resultsFolder = resultsFolder;
        this.mgfFiles = mgfFiles;
        this.rawFiles = rawFiles;
        this.exceptionHandler = exceptionHandler;
        enableOmssa = loadSearchEngineLocation(Advocate.omssa, false, true, true, true, false, false, false);
        enableXtandem = loadSearchEngineLocation(Advocate.xtandem, false, true, true, true, true, false, true);
        enableMsgf = loadSearchEngineLocation(Advocate.msgf, true, true, true, true, false, false, false);
        enableMsAmanda = loadSearchEngineLocation(Advocate.msAmanda, false, true, true, true, false, false, false);
        enableMyriMatch = loadSearchEngineLocation(Advocate.myriMatch, false, true, false, true, true, false, true);
        enableComet = loadSearchEngineLocation(Advocate.comet, false, true, false, true, true, false, false);
        enableTide = loadSearchEngineLocation(Advocate.tide, false, true, true, true, false, false, true);
        enableAndromeda = loadSearchEngineLocation(Advocate.andromeda, false, true, false, false, false, false, false);
        loadSearchEngineLocation(null, false, true, true, true, false, false, true);
        this.identificationParametersFile = identificationParametersFile;
        this.processingPreferences = processingPreferences;
        this.generateProteinTree = generateProteinTree;
        this.identificationParameters = identificationParameters;
        searchDuration = new Duration();
    }

    /**
     * Constructor for the SearchGUI command line interface. If the search
     * engines folders are set to null the default search engine locations are
     * used.
     *
     * @param identificationParameters the identification parameters
     * @param resultsFolder the results folder
     * @param mgfFiles list of peak list files in the mgf format
     * @param rawFiles list of raw files
     * @param identificationParametersFile the search parameters file
     * @param searchOmssa if true the OMSSA search is enabled
     * @param searchXTandem if true the XTandem search is enabled
     * @param searchMsgf if true the MS-GF+ search is enabled
     * @param searchMsAmanda if true the MS Amanda search is enabled
     * @param searchMyriMatch if true the MyriMatch search is enabled
     * @param searchComet if true the Comet search is enabled
     * @param searchTide if true the Tide search is enabled
     * @param searchAndromeda if true the Andromeda search is enabled
     * @param omssaFolder the folder where OMSSA is installed, if null the
     * default location is used
     * @param xTandemFolder the folder where X!Tandem is installed, if null the
     * default location is used
     * @param msgfFolder the folder where MS-GF+ is installed, if null the
     * default location is used
     * @param msAmandaFolder the folder where MS Amanda is installed, if null
     * the default location is used
     * @param myriMatchFolder the folder where MyriMatch is installed, if null
     * the default location is used
     * @param cometFolder the folder where Comet is installed, if null the
     * default location is used
     * @param tideFolder the folder where Tide is installed, if null the default
     * location is used
     * @param andromedaFolder the folder where Andromeda is installed, if null
     * the default location is used
     * @param makeblastdbFolder the folder where makeblastdb is installed, if
     * null the default location is used
     * @param processingPreferences the processing preferences
     * @param generateProteinTree if true, the protein tree will be generated
     */
    public SearchHandler(IdentificationParameters identificationParameters, File resultsFolder, ArrayList<File> mgfFiles, ArrayList<File> rawFiles, File identificationParametersFile,
            boolean searchOmssa, boolean searchXTandem, boolean searchMsgf, boolean searchMsAmanda, boolean searchMyriMatch, boolean searchComet, boolean searchTide, boolean searchAndromeda,
            File omssaFolder, File xTandemFolder, File msgfFolder, File msAmandaFolder, File myriMatchFolder, File cometFolder, File tideFolder, File andromedaFolder, File makeblastdbFolder,
            ProcessingPreferences processingPreferences, boolean generateProteinTree) {

        this.resultsFolder = resultsFolder;
        this.mgfFiles = mgfFiles;
        this.rawFiles = rawFiles;
        this.enableOmssa = searchOmssa;
        this.enableXtandem = searchXTandem;
        this.enableMsgf = searchMsgf;
        this.enableMsAmanda = searchMsAmanda;
        this.enableMyriMatch = searchMyriMatch;
        this.enableComet = searchComet;
        this.enableTide = searchTide;
        this.enableAndromeda = searchAndromeda;

        this.identificationParameters = identificationParameters;
        this.processingPreferences = processingPreferences;
        this.generateProteinTree = generateProteinTree;

        if (omssaFolder != null) {
            this.omssaLocation = omssaFolder;
        } else {
            loadSearchEngineLocation(Advocate.omssa, false, true, true, true, false, false, false); // try to use the default
        }

        if (xTandemFolder != null) {
            this.xtandemLocation = xTandemFolder;
        } else {
            loadSearchEngineLocation(Advocate.xtandem, false, true, true, true, true, false, true); // try to use the default
        }

        if (msgfFolder != null) {
            this.msgfLocation = msgfFolder;
        } else {
            loadSearchEngineLocation(Advocate.msgf, true, true, true, true, false, false, false); // try to use the default
        }

        if (msAmandaFolder != null) {
            this.msAmandaLocation = msAmandaFolder;
        } else {
            loadSearchEngineLocation(Advocate.msAmanda, false, true, true, true, false, false, false); // try to use the default
        }

        if (myriMatchFolder != null) {
            this.myriMatchLocation = myriMatchFolder;
        } else {
            loadSearchEngineLocation(Advocate.myriMatch, false, true, false, true, true, false, true); // try to use the default
        }

        if (cometFolder != null) {
            this.cometLocation = cometFolder;
        } else {
            loadSearchEngineLocation(Advocate.comet, false, true, false, true, true, false, false); // try to use the default
        }

        if (tideFolder != null) {
            this.tideLocation = tideFolder;
        } else {
            loadSearchEngineLocation(Advocate.tide, false, true, true, true, false, false, true); // try to use the default
        }

        if (andromedaFolder != null) {
            this.andromedaLocation = andromedaFolder;
        } else {
            loadSearchEngineLocation(Advocate.andromeda, false, true, false, false, false, false, false); // try to use the default
        }

        if (makeblastdbFolder != null) {
            this.makeblastdbLocation = makeblastdbFolder;
        } else {
            loadSearchEngineLocation(null, false, true, true, true, false, false, true); // try to use the default
        }

        // set this version as the default SearchGUI version
        if (!getJarFilePath().equalsIgnoreCase(".")) {
            String versionNumber = new eu.isas.searchgui.utilities.Properties().getVersion();
            UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
            utilitiesUserPreferences.setSearchGuiPath(new File(getJarFilePath(), "SearchGUI-" + versionNumber + ".jar").getAbsolutePath());
            UtilitiesUserPreferences.saveUserPreferences(utilitiesUserPreferences);
        }

        searchDuration = new Duration();
    }

    /**
     * Start the search.
     *
     * @param waitingHandler the waiting handler
     *
     * @throws InterruptedException thrown if the process is interrupted
     */
    public synchronized void startSearch(WaitingHandler waitingHandler) throws InterruptedException {

        this.waitingHandler = waitingHandler;
        searchDuration.start();

        searchWorker = new SearchWorker(waitingHandler);
        searchWorker.execute();

        indexingWorker = new IndexingWorker(waitingHandler);
        indexingWorker.execute();

        // display the waiting dialog
        if (waitingHandler != null && waitingHandler instanceof WaitingDialog) {
            try {
                ((WaitingDialog) waitingHandler).setVisible(true);
            } catch (IndexOutOfBoundsException e) {
                // ignore
            }
            ((WaitingDialog) waitingHandler).setModal(true);
        } else {
            useCommandLine = true;
        }

        if (useCommandLine && !searchWorker.isFinished()) {
            wait();
        }
    }

    /**
     * Notifies the handler that the process is finished.
     */
    private synchronized void notifySearchFinished() {
        notify();
    }

    /**
     * Cancel the search.
     */
    public void cancelSearch() {
        searchWorker.cancelRun();

        if (proteinTreeWorker != null && !proteinTreeWorker.isFinished()) {
            proteinTreeWorker.cancelBuild();
        }

        if (waitingHandler != null) {
            waitingHandler.setRunCanceled();
        }
    }

    /**
     * Returns the user defined enzymes file.
     *
     * @param jarFilePath the path to the jar file
     * @return the user defined enzymes file
     */
    public static File getEnzymesFile(String jarFilePath) {
        File result = new File(jarFilePath, enzymeFile);
        if (!result.exists()) {
            JOptionPane.showMessageDialog(null, enzymeFile + " not found.", "Enzymes File Error", JOptionPane.ERROR_MESSAGE);
        }
        return result;
    }

    /**
     * Called when the search has been completed.
     */
    private void searchCompleted() {

        if (searchWorker.isFinished() && indexingWorker.isFinished()) {

            // stop the building of the tree
            if (proteinTreeWorker != null && !proteinTreeWorker.isFinished()) {
                proteinTreeWorker.cancelBuild();
                while (!proteinTreeWorker.isFinished()) {
                    // wait for the db to shut down
                }
            }

            if (waitingHandler != null) {
                if (waitingHandler instanceof WaitingDialog) {
                    // change the icon back to the default version
                    ((JFrame) ((WaitingDialog) waitingHandler).getParent()).setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")));
                }
                searchDuration.end();
                waitingHandler.appendReport("Search Completed (" + searchDuration.toString() + ").", true, true);
                waitingHandler.appendReportEndLine();
            }

            saveReport();

            if (waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.setRunFinished();
            }

            // open project in PeptideShaker?
            if (enablePeptideShaker) {
                if (peptideShakerFile.exists()) {
                    try {
                        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();

                        CompomicsWrapper wrapper = new CompomicsWrapper();
                        ArrayList<String> javaHomeAndOptions = wrapper.getJavaHomeAndOptions(utilitiesUserPreferences.getPeptideShakerPath());

                        ArrayList process_name_array = new ArrayList();
                        process_name_array.add(javaHomeAndOptions.get(0)); // set java home

                        // set java options
                        for (int i = 1; i < javaHomeAndOptions.size(); i++) {
                            process_name_array.add(javaHomeAndOptions.get(i));
                        }

                        process_name_array.add("-jar");
                        process_name_array.add(new File(utilitiesUserPreferences.getPeptideShakerPath()).getName());
                        process_name_array.add("-cps");
                        process_name_array.add(CommandLineUtils.getCommandLineArgument(peptideShakerFile));

                        ProcessBuilder openPeptideShakerProcess = new ProcessBuilder(process_name_array);

                        // print the command to the log file
                        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "PeptideShaker command: ");

                        for (Object currentElement : process_name_array) {
                            System.out.print(currentElement + " ");
                        }

                        System.out.println(System.getProperty("line.separator"));

                        File psFolder = new File(utilitiesUserPreferences.getPeptideShakerPath()).getParentFile();
                        openPeptideShakerProcess.directory(psFolder);

                        // set error out and std out to same stream
                        openPeptideShakerProcess.redirectErrorStream(true);

                        openPeptideShakerProcess.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (waitingHandler != null) {
                        waitingHandler.appendReport("PeptideShaker file (" + peptideShakerFile.getAbsolutePath() + ") not found!", true, true);
                    }
                }
            }

            if (useCommandLine) {
                System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "Search Completed." + System.getProperty("line.separator"));
                System.exit(0);
            }
        } else if (!indexingWorker.isFinished()) {
            waitingHandler.appendReport("Search completed. Waiting for the file indexing to finish.", true, true);
            waitingHandler.appendReportEndLine();
            waitingHandler.appendReport("Please do not close SearchGUI.", true, true);
            waitingHandler.appendReportEndLine();
        }
    }

    /**
     * Save the SearchGUI report to the results folder.
     */
    private void saveReport() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss");
        String fileName = "SearchGUI Report " + df.format(new Date()) + ".html";
        String report = "";

        if (waitingHandler instanceof WaitingDialog) {
            report = "<pre>" + ((WaitingDialog) waitingHandler).getReport(new File(resultsFolder, fileName)) + "</pre>";
        }

        // append the search parameters
        report += identificationParameters.getSearchParameters().toString(true);
        report = "<html>" + report + "</html>";

        try {
            FileWriter fw = new FileWriter(new File(resultsFolder, fileName));
            fw.write(report);
            fw.close();
        } catch (IOException e) {
            if (waitingHandler != null) {
                waitingHandler.appendReport("Failed to write to the report file!", true, true);
            }
            e.printStackTrace();
        }
    }

    /**
     * Called if the search does not finish properly.
     */
    private void searchCrashed() {
        if (waitingHandler != null) {
            if (waitingHandler instanceof WaitingDialog) {
                // change the icon back to the default version
                ((JFrame) ((WaitingDialog) waitingHandler).getParent()).setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")));
            }
            waitingHandler.appendReport("The search or processing did not finish properly!", true, true);
            waitingHandler.setRunCanceled();

            saveReport();

            if (waitingHandler instanceof WaitingHandlerCLIImpl) {
                System.exit(0);
            }
        } else {
            System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator")
                    + "The search did not finish properly:" + System.getProperty("line.separator") + JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    /**
     * This method loads the location for the search engine from the conf
     * folder. If the file is not found, or it is empty, the location will be
     * set to null.
     *
     * @param searchEngineAdvocate the search engine advocate (if null
     * makeblastdb is assumed)
     * @param searchEngineLoation the location of the search engine will be
     * stored in this file)
     * @param sameVersionForAll if true, the same version is used for all search
     * engines
     * @param windowsSupported true if Windows is supported for the given search
     * engine
     * @param osxSupported true if OSX is supported for the given search engine
     * @param linuxSupported true if Linux is supported for the given search
     * engine
     * @param windowsBitVersions if true, different versions will be used for 32
     * and 64 bit for Windows
     * @param osxBitVersions if true, different versions will be used for 32 and
     * 64 bit for OSX
     * @param linuxBitVersions if true, different versions will be used for 32
     * and 64 bit for Linux
     *
     * @return boolean if the search engine can be selected or not
     */
    private boolean loadSearchEngineLocation(Advocate searchEngineAdvocate,
            boolean sameVersionForAll, boolean windowsSupported, boolean osxSupported, boolean linuxSupported,
            boolean windowsBitVersions, boolean osxBitVersions, boolean linuxBitVersions) {

        boolean enableSearchEngine = false;
        String advocateName;
        if (searchEngineAdvocate == null) {
            advocateName = "makeblastdb";
        } else {
            advocateName = searchEngineAdvocate.getName();
        }

        // remove '!' from the advocate name, e.g., X!Tandem > XTandem
        String correctedAdvocateName = advocateName.replaceAll("!", "");

        File folder = new File(getJarFilePath() + File.separator + "resources" + File.separator + "conf" + File.separator);
        File searchEngineLoation = null;

        if (folder.exists()) {
            File input = new File(folder, SEARCH_GUI_CONFIGURATION_FILE);
            try {
                BufferedReader br = new BufferedReader(new FileReader(input));
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("") || line.startsWith("#")) {
                        // skip empty lines and comment ('#') lines.
                    } else if (line.equals(advocateName + " Location:")) {
                        String result = br.readLine().trim();
                        searchEngineLoation = new File(result);

                        if (result.equalsIgnoreCase("Not Selected") || !searchEngineLoation.exists()) { // provided location not set or not found

                            String basePath = getJarFilePath() + File.separator + "resources" + File.separator + correctedAdvocateName;

                            if (sameVersionForAll) {
                                searchEngineLoation = new File(basePath);
                            } else {

                                String operatingSystem = System.getProperty("os.name").toLowerCase();
                                String arch = System.getProperty("os.arch").toLowerCase();
                                boolean is64Bit = arch.lastIndexOf("64") != -1; // @TODO: note that this tests the version of the Java VM and not the OS...

                                // default to the correct version for the given os
                                if (operatingSystem.contains("windows") && windowsSupported) {
                                    if (!windowsBitVersions) {
                                        searchEngineLoation = new File(basePath + File.separator + "windows");
                                    } else {
                                        if (is64Bit) {
                                            searchEngineLoation = new File(basePath + File.separator + "windows" + File.separator + "windows_64bit");
                                        } else {
                                            searchEngineLoation = new File(basePath + File.separator + "windows" + File.separator + "windows_32bit");
                                        }
                                    }
                                } else if (operatingSystem.contains("mac os") && osxSupported) {
                                    if (!osxBitVersions) {
                                        searchEngineLoation = new File(basePath + File.separator + "osx");
                                    } else {
                                        if (is64Bit) {
                                            searchEngineLoation = new File(basePath + File.separator + "osx" + File.separator + "osx_64bit");
                                        } else {
                                            searchEngineLoation = new File(basePath + File.separator + "osx" + File.separator + "osx_32bit");
                                        }
                                    }
                                } else if ((operatingSystem.contains("nix") || operatingSystem.contains("nux")) && linuxSupported) {
                                    if (!linuxBitVersions) {
                                        searchEngineLoation = new File(basePath + File.separator + "linux");
                                    } else {
                                        if (is64Bit) {
                                            searchEngineLoation = new File(basePath + File.separator + "linux" + File.separator + "linux_64bit");
                                        } else {
                                            searchEngineLoation = new File(basePath + File.separator + "linux" + File.separator + "linux_32bit");
                                        }
                                    }
                                } else {
                                    // unsupported OS version
                                    searchEngineLoation = null;
                                }
                            }
                        } else {
                            searchEngineLoation = new File(result); // use given location
                        }

                        if (searchEngineLoation == null) {
                            enableSearchEngine = false;
                        } else {
                            String selected = br.readLine().trim();
                            if (selected.length() > 0) {
                                enableSearchEngine = Boolean.parseBoolean(selected);
                            } else {
                                enableSearchEngine = true;
                            }
                        }
                    }
                }
                br.close();
            } catch (IOException ioe) {
                enableSearchEngine = false;
                ioe.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred when trying to load the " + advocateName + " location.",
                        "Configuration Import Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            enableSearchEngine = false;
        }

        if (searchEngineAdvocate != null) {
            if (searchEngineAdvocate == Advocate.omssa) {
                omssaLocation = searchEngineLoation;
            } else if (searchEngineAdvocate == Advocate.xtandem) {
                xtandemLocation = searchEngineLoation;
            } else if (searchEngineAdvocate == Advocate.myriMatch) {
                myriMatchLocation = searchEngineLoation;
            } else if (searchEngineAdvocate == Advocate.msAmanda) {
                msAmandaLocation = searchEngineLoation;
            } else if (searchEngineAdvocate == Advocate.msgf) {
                msgfLocation = searchEngineLoation;
            } else if (searchEngineAdvocate == Advocate.comet) {
                cometLocation = searchEngineLoation;
            } else if (searchEngineAdvocate == Advocate.tide) {
                tideLocation = searchEngineLoation;
            } else if (searchEngineAdvocate == Advocate.andromeda) {
                andromedaLocation = searchEngineLoation;
            }
        } else {
            makeblastdbLocation = searchEngineLoation;
        }

        return enableSearchEngine;
    }

    /**
     * Returns a string with the modifications used.
     *
     * @return a string with the modifications used.
     */
    public String loadModificationsUse() {
        String result = "";

        File folder = new File(getJarFilePath() + File.separator + "resources" + File.separator + "conf" + File.separator);
        if (folder.exists()) {
            File input = new File(folder, SEARCH_GUI_CONFIGURATION_FILE);
            try {
                BufferedReader br = new BufferedReader(new FileReader(input));
                String line;
                while ((line = br.readLine()) != null) {
                    // Skip empty lines and comment ('#') lines.
                    line = line.trim();
                    if (line.equals("") || line.startsWith("#")) {
                    } else if (line.equals("Modification use:")) {
                        result = br.readLine().trim();
                    }
                }
                br.close();
            } catch (IOException ioe) {
                ioe.printStackTrace(); // @TODO: this exception should be thrown to the GUI!
                JOptionPane.showMessageDialog(null, "An error occurred when trying to load the modifications preferences.",
                        "Configuration Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return result;
    }

    /**
     * Returns the name of the X!Tandem result file if renamed.
     *
     * @param spectrumFileName the name of the spectrum file searched
     *
     * @return the name of the X!Tandem result file
     */
    public static String getXTandemFileName(String spectrumFileName) {
        return Util.removeExtension(spectrumFileName) + ".t.xml";
    }

    /**
     * Returns the name of the Comet result file.
     *
     * @param spectrumFileName the name of the spectrum file searched
     *
     * @return the name of the Comet result file
     */
    public static String getCometFileName(String spectrumFileName) {
        return Util.removeExtension(spectrumFileName) + ".comet.pep.xml";
    }

    /**
     * Returns the name of the Tide result file.
     *
     * @param spectrumFileName the spectrum file name
     * @return the name of the Tide result file
     */
    public String getTideFileName(String spectrumFileName) {
        TideParameters tideParameters = (TideParameters) identificationParameters.getSearchParameters().getIdentificationAlgorithmParameter(Advocate.tide.getIndex());
        return getTideFileName(spectrumFileName, tideParameters);
    }

    /**
     * Returns the name of the Tide result file.
     *
     * @param spectrumFileName the mgf file name
     * @param tideParameters the Tide parameters
     *
     * @return the name of the Tide result file
     */
    public static String getTideFileName(String spectrumFileName, TideParameters tideParameters) {
        if (tideParameters.getTextOutput()) {
            return Util.removeExtension(spectrumFileName) + ".tide-search.target.txt";
        } else if (tideParameters.getMzidOutput()) {
            return Util.removeExtension(spectrumFileName) + ".tide-search.mzid";
        } else if (tideParameters.getPepXmlOutput()) {
            return Util.removeExtension(spectrumFileName) + ".tide-search.target.pep.xml";
        } else if (tideParameters.getSqtOutput()) {
            return Util.removeExtension(spectrumFileName) + ".tide-search.target.sqt";
        } else {
            return Util.removeExtension(spectrumFileName) + ".tide-search.pin";
        }
    }

    /**
     * Returns the name of the Andromeda result file.
     *
     * @param spectrumFileName the name of the spectrum file searched
     *
     * @return the name of the Andromeda result file
     */
    public static String getAndromedaFileName(String spectrumFileName) {
        return Util.removeExtension(spectrumFileName) + ".res";
    }

    /**
     * Returns the name of the OMSSA result file.
     *
     * @param spectrumFileName the spectrum file name
     * @return the name of the OMSSA result file
     */
    public String getOMSSAFileName(String spectrumFileName) {
        OmssaParameters omssaParameters = (OmssaParameters) identificationParameters.getSearchParameters().getIdentificationAlgorithmParameter(Advocate.omssa.getIndex());
        return getOMSSAFileName(spectrumFileName, omssaParameters);
    }

    /**
     * Returns the name of the OMSSA result file.
     *
     * @param spectrumFileName the mgf file name
     * @param omssaParameters the OMSSA parameters
     *
     * @return the name of the OMSSA result file
     */
    public static String getOMSSAFileName(String spectrumFileName, OmssaParameters omssaParameters) {
        return Util.removeExtension(spectrumFileName) + "." + omssaParameters.getSelectedOutput().toLowerCase();
    }

    /**
     * Returns the name of the MS-GF+ result file.
     *
     * @param spectrumFileName the mgf file name
     *
     * @return the name of the MS-GF+ result file
     */
    public static String getMsgfFileName(String spectrumFileName) {
        return Util.removeExtension(spectrumFileName) + ".msgf.mzid";
    }

    /**
     * Returns the name of the MS Amanda result file.
     *
     * @param spectrumFileName the mgf file name
     *
     * @return the name of the MS Amanda result file
     */
    public static String getMsAmandaFileName(String spectrumFileName) {
        return Util.removeExtension(spectrumFileName) + ".ms-amanda.csv";
    }

    /**
     * Returns the name of the MyriMatch result file.
     *
     * @param spectrumFileName the spectrum file name
     * @return the name of the MyriMatch result file
     */
    public String getMyriMatchFileName(String spectrumFileName) {
        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) identificationParameters.getSearchParameters().getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex());
        return getMyriMatchFileName(spectrumFileName, myriMatchParameters);
    }

    /**
     * Returns the name of the MyriMatch result file.
     *
     * @param spectrumFileName the mgf file name
     * @param myriMatchParameters the MyriMatch parameters
     *
     * @return the name of the MyriMatch result file
     */
    public static String getMyriMatchFileName(String spectrumFileName, MyriMatchParameters myriMatchParameters) {
        if (myriMatchParameters.getOutputFormat().equalsIgnoreCase("mzIdentML")) {
            return Util.removeExtension(spectrumFileName) + ".myrimatch.mzid";
        } else {
            return Util.removeExtension(spectrumFileName) + ".myrimatch.pepXML";
        }
    }

    /**
     * Lists all the files which can be X!Tandem output for this spectrum file
     * in the given folder.
     *
     * @param folder the folder to screen
     * @param spectrumFileName the name of the spectrum file
     * @return the list of candidate identification result files
     */
    public ArrayList<File> getXTandemFiles(File folder, String spectrumFileName) {
        String regex = ".*\\d{4}_\\d{2}[_]\\d{2}[_]\\d{2}[_]\\d{2}[_]\\d{2}[.]t[.]xml";
        Pattern pattern = Pattern.compile(regex);
        ArrayList<File> result = new ArrayList<File>();
        for (File file : folder.listFiles()) {
            String fileName = file.getName();
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.matches() || fileName.equals(getXTandemFileName(spectrumFileName))) {
                result.add(file);
            }
        }
        return result;
    }

    /**
     * Returns true if the X!Tandem file should be renamed.
     *
     * @return true if the X!Tandem file should be renamed
     */
    public boolean renameXTandemFile() {
        return renameXTandemFile;
    }

    /**
     * Set if the X!Tandem file should be renamed.
     *
     * @param renameXTandemFile rename file?
     */
    public void setRenameXTandemFile(boolean renameXTandemFile) {
        this.renameXTandemFile = renameXTandemFile;
    }

    /**
     * Returns true if the protein tree will be created parallel to the
     * searches.
     *
     * @return true if the protein tree will be created parallel to the searches
     */
    public boolean generateProteinTree() {
        return generateProteinTree;
    }

    /**
     * Set if the the protein tree will be created parallel to the searches.
     *
     * @param generateProteinTree create protein tree?
     */
    public void setGenerateProteinTree(boolean generateProteinTree) {
        this.generateProteinTree = generateProteinTree;
    }

    /**
     * Returns the OMSSA location.
     *
     * @return the omssaLocation
     */
    public File getOmssaLocation() {
        return omssaLocation;
    }

    /**
     * Set the OMSSA location.
     *
     * @param omssaLocation the omssaLocation to set
     */
    public void setOmssaLocation(File omssaLocation) {
        this.omssaLocation = omssaLocation;
    }

    /**
     * Returns the X!Tandem location.
     *
     * @return the xtandemLocation
     */
    public File getXtandemLocation() {
        return xtandemLocation;
    }

    /**
     * Set the X!Tandem location.
     *
     * @param xtandemLocation the xtandemLocation to set
     */
    public void setXtandemLocation(File xtandemLocation) {
        this.xtandemLocation = xtandemLocation;
    }

    /**
     * Returns the MS-GF+ location.
     *
     * @return the msgfLocation
     */
    public File getMsgfLocation() {
        return msgfLocation;
    }

    /**
     * Set the MS-GF+ location.
     *
     * @param msgfLocation the msgfLocation to set
     */
    public void setMsgfLocation(File msgfLocation) {
        this.msgfLocation = msgfLocation;
    }

    /**
     * Returns the MS Amanda location.
     *
     * @return the msAmandaLocation
     */
    public File getMsAmandaLocation() {
        return msAmandaLocation;
    }

    /**
     * Set the MS Amanda location.
     *
     * @param msAmandaLocation the msAmandaLocation to set
     */
    public void setMsAmandaLocation(File msAmandaLocation) {
        this.msAmandaLocation = msAmandaLocation;
    }

    /**
     * Returns the MyriMatch location.
     *
     * @return the myriMatchLocation
     */
    public File getMyriMatchLocation() {
        return myriMatchLocation;
    }

    /**
     * Set the MyriMatch location.
     *
     * @param myriMatchLocation the myriMatchLocation to set
     */
    public void setMyriMatchLocation(File myriMatchLocation) {
        this.myriMatchLocation = myriMatchLocation;
    }

    /**
     * Returns the Comet location.
     *
     * @return the comet location
     */
    public File getCometLocation() {
        return cometLocation;
    }

    /**
     * Set the Comet location.
     *
     * @param cometLocation the cometLocation to set
     */
    public void setCometLocation(File cometLocation) {
        this.cometLocation = cometLocation;
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
     * Set the Tide location.
     *
     * @param tideLocation the Tide location to set
     */
    public void setTideLocation(File tideLocation) {
        this.tideLocation = tideLocation;
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
     * Set the Andromeda location.
     *
     * @param andromedaLocation the Andromeda location to set
     */
    public void setAndromedaLocation(File andromedaLocation) {
        this.andromedaLocation = andromedaLocation;
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
     * Set the makeblastdb location.
     *
     * @param makeblastdbLocation the makeblastdbLocation to set
     */
    public void setMakeblastdbLocation(File makeblastdbLocation) {
        this.makeblastdbLocation = makeblastdbLocation;
    }

    /**
     * Returns true if OMSSA is to be used.
     *
     * @return true if OMSSA is to be used
     */
    public boolean isOmssaEnabled() {
        return enableOmssa;
    }

    /**
     * Set if PeptideShaker is to be run or not.
     *
     * @param runPeptideShaker if PeptideShaker is to be run or not
     */
    public void setPeptideShakerEnabled(boolean runPeptideShaker) {
        enablePeptideShaker = runPeptideShaker;
    }

    /**
     * Returns a boolean indicating whether PeptideShaker was enabled.
     *
     * @return a boolean indicating whether PeptideShaker was enabled
     */
    public boolean isPeptideShakerEnabled() {
        return enablePeptideShaker;
    }

    /**
     * Returns a boolean indicating whether Reporter was enabled.
     *
     * @return a boolean indicating whether Reporter was enabled
     */
    public boolean isReporterEnabled() {
        return enableReporter;
    }

    /**
     * Set if OMSSA is to be used.
     *
     * @param runOmssa run OMSSA?
     */
    public void setOmssaEnabled(boolean runOmssa) {
        this.enableOmssa = runOmssa;
    }

    /**
     * Returns true if X!Tandem is to be used.
     *
     * @return if X!Tandem is to be used
     */
    public boolean isXtandemEnabled() {
        return enableXtandem;
    }

    /**
     * Returns true if MS-GF+ is to be used.
     *
     * @return if MS-GF+ is to be used
     */
    public boolean isMsgfEnabled() {
        return enableMsgf;
    }

    /**
     * Returns true if MS Amanda is to be used.
     *
     * @return if MS Amanda is to be used
     */
    public boolean isMsAmandaEnabled() {
        return enableMsAmanda;
    }

    /**
     * Returns true if MyriMatch is to be used.
     *
     * @return if MyriMatch is to be used
     */
    public boolean isMyriMatchEnabled() {
        return enableMyriMatch;
    }

    /**
     * Returns true if Comet is to be used.
     *
     * @return if Comet is to be used
     */
    public boolean isCometEnabled() {
        return enableComet;
    }

    /**
     * Returns true if Tide is to be used.
     *
     * @return if Tide is to be used
     */
    public boolean isTideEnabled() {
        return enableTide;
    }

    /**
     * Returns true if Andromeda is to be used.
     *
     * @return if Andromeda is to be used
     */
    public boolean isAndromedaEnabled() {
        return enableAndromeda;
    }

    /**
     * Set if X!Tandem is to be used.
     *
     * @param runXtandem run X!Tandem?
     */
    public void setXtandemEnabled(boolean runXtandem) {
        this.enableXtandem = runXtandem;
    }

    /**
     * Set if MS-GF+ is to be used.
     *
     * @param runMsgf run MS-GF+?
     */
    public void setMsgfEnabled(boolean runMsgf) {
        this.enableMsgf = runMsgf;
    }

    /**
     * Set if MS Amanda is to be used.
     *
     * @param runMsAmanda run MS Amanda?
     */
    public void setMsAmandaEnabled(boolean runMsAmanda) {
        this.enableMsAmanda = runMsAmanda;
    }

    /**
     * Set if MyriMatch is to be used.
     *
     * @param runMyriMatch run MyriMatch?
     */
    public void setMyriMatchEnabled(boolean runMyriMatch) {
        this.enableMyriMatch = runMyriMatch;
    }

    /**
     * Set if Comet is to be used.
     *
     * @param runComet run Comet?
     */
    public void setCometEnabled(boolean runComet) {
        this.enableComet = runComet;
    }

    /**
     * Set if Tide is to be used.
     *
     * @param runTide run Tide?
     */
    public void setTideEnabled(boolean runTide) {
        this.enableTide = runTide;
    }

    /**
     * Set if Andromeda is to be used.
     *
     * @param runAndromeda run Andromeda?
     */
    public void setAndromedaEnabled(boolean runAndromeda) {
        this.enableAndromeda = runAndromeda;
    }

    /**
     * Returns the results folder.
     *
     * @return the resultsFolder
     */
    public File getResultsFolder() {
        return resultsFolder;
    }

    /**
     * Set the results folder.
     *
     * @param resultsFolder the resultsFolder to set
     */
    public void setResultsFolder(File resultsFolder) {
        this.resultsFolder = resultsFolder;
    }

    /**
     * Returns the list of mgf files.
     *
     * @return the mgf files
     */
    public ArrayList<File> getMgfFiles() {
        return mgfFiles;
    }

    /**
     * Sets the list of mgf files.
     *
     * @param mgfFiles the mgf files
     */
    public void setMgfFiles(ArrayList<File> mgfFiles) {
        this.mgfFiles = mgfFiles;
    }

    /**
     * Returns the list of raw files.
     *
     * @return the raw files
     */
    public ArrayList<File> getRawFiles() {
        return rawFiles;
    }

    /**
     * Sets the list of raw files.
     *
     * @param rawFiles the raw files
     */
    public void setRawFiles(ArrayList<File> rawFiles) {
        this.rawFiles = rawFiles;
    }

    /**
     * Returns the experiment label.
     *
     * @return the experiment label
     */
    public String getExperimentLabel() {
        return experimentLabel;
    }

    /**
     * Sets the the experiment label.
     *
     * @param experimentLabel the experimentLabel to set
     */
    public void setExperimentLabel(String experimentLabel) {
        this.experimentLabel = experimentLabel;
    }

    /**
     * Returns the sample label.
     *
     * @return the sample label
     */
    public String getSampleLabel() {
        return sampleLabel;
    }

    /**
     * Sets the sample label.
     *
     * @param sampleLabel the sampleLabel to set
     */
    public void setSampleLabel(String sampleLabel) {
        this.sampleLabel = sampleLabel;
    }

    /**
     * Returns the replicate number.
     *
     * @return the replicate number
     */
    public Integer getReplicateNumber() {
        return replicateNumber;
    }

    /**
     * Sets the replicate number.
     *
     * @param replicateNumber the replicateNumber to set
     */
    public void setReplicateNumber(Integer replicateNumber) {
        this.replicateNumber = replicateNumber;
    }

    /**
     * Returns the PeptideShaker file.
     *
     * @return the PeptideShaker file
     */
    public File getPeptideShakerFile() {
        return peptideShakerFile;
    }

    /**
     * Sets the PeptideShaker file.
     *
     * @param peptideShakerFile the peptideShakerFile to set
     */
    public void setPeptideShakerFile(File peptideShakerFile) {
        this.peptideShakerFile = peptideShakerFile;
    }

    /**
     * Sets how output files should be organized.
     *
     * @param outputOption the file output option
     */
    public void setOutputOption(OutputOption outputOption) {
        this.outputOption = outputOption;
    }

    /**
     * Returns the selected output option.
     *
     * @return the selected output option
     */
    public OutputOption getOutputOption() {
        return outputOption;
    }

    /**
     * Indicates whether data should be copied along with the identification
     * files.
     *
     * @return a boolean indicating whether data should be copied along with the
     * identification files
     */
    public boolean outputData() {
        return outputData;
    }

    /**
     * Sets whether data should be copied along with the identification files.
     *
     * @param outputData whether data should be copied along with the
     * identification files
     */
    public void setOutputData(boolean outputData) {
        this.outputData = outputData;
    }

    /**
     * Indicates whether the date should be included in the file output name.
     *
     * @return a boolean indicating whether the date should be included in the
     * file output name
     */
    public boolean isIncludeDateInOutputName() {
        return includeDateInOutputName;
    }

    /**
     * Sets whether the date should be included in the file output name.
     *
     * @param includeDateInOutputName whether the date should be included in the
     * file output name
     */
    public void setIncludeDateInOutputName(boolean includeDateInOutputName) {
        this.includeDateInOutputName = includeDateInOutputName;
    }

    /**
     * SearchWorker extends SwingWorker and is a helper class for performing the
     * searches.
     */
    private class SearchWorker extends SwingWorker {

        /**
         * The waiting dialog.
         */
        private WaitingHandler waitingHandler;
        /**
         * True if the process has finished.
         */
        private boolean finished = false;

        /**
         * Creates a new SearchWorker object.
         *
         * @param waitingHandler
         */
        public SearchWorker(WaitingHandler waitingHandler) {
            this.waitingHandler = waitingHandler;

            if (waitingHandler instanceof WaitingDialog) {
                // make sure the icon is set to the waiting icon
                ((JFrame) ((WaitingDialog) waitingHandler).getParent()).setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
            }
        }

        /*
         * Cancel the search.
         */
        public void cancelRun() {

            // start the cancelling of the process
            if (waitingHandler != null) {
                waitingHandler.setWaitingText("Canceling");
            }

            // stop makeblastdb and ms convert
            if (makeblastdbProcessBuilder != null) {
                makeblastdbProcessBuilder.endProcess();
            }
            if (msConvertProcessBuilders != null) {
                for (MsConvertProcessBuilder msConvertProcessBuilder : msConvertProcessBuilders) {
                    msConvertProcessBuilder.endProcess();
                }
            }

            // stop the search engines and peptide shaker
            if (omssaProcessBuilder != null) {
                omssaProcessBuilder.endProcess();
            }
            if (xTandemProcessBuilder != null) {
                xTandemProcessBuilder.endProcess();
            }
            if (msgfProcessBuilder != null) {
                msgfProcessBuilder.endProcess();
            }
            if (msAmandaProcessBuilder != null) {
                msAmandaProcessBuilder.endProcess();
            }
            if (myriMatchProcessBuilder != null) {
                myriMatchProcessBuilder.endProcess();
            }
            if (cometProcessBuilder != null) {
                cometProcessBuilder.endProcess();
            }
            if (tideIndexProcessBuilder != null) {
                tideIndexProcessBuilder.endProcess();
            }
            if (tideSearchProcessBuilder != null) {
                tideSearchProcessBuilder.endProcess();
            }
            if (andromedaProcessBuilder != null) {
                andromedaProcessBuilder.endProcess();
            }
            if (peptideShakerProcessBuilder != null) {
                peptideShakerProcessBuilder.endProcess();
            }

            // stop the building of the tree
            if (proteinTreeWorker != null && !proteinTreeWorker.isFinished()) {
                proteinTreeWorker.cancelBuild();
            }
        }

        @Override
        protected Object doInBackground() {

            try {
                File outputFolder = getResultsFolder();
                File outputTempFolder;

                if (outputOption == OutputOption.no_zip) {
                    outputTempFolder = outputFolder;
                } else {
                    try {
                        outputTempFolder = new File(outputFolder, OUTPUT_TEMP_FOLDER_NAME);
                        if (outputTempFolder.exists()) {
                            Util.deleteDir(outputTempFolder);
                        }
                        outputTempFolder.mkdirs();
                        TempFilesManager.registerTempFolder(outputTempFolder);
                    } catch (Exception e) {
                        e.printStackTrace();
                        outputTempFolder = outputFolder;
                    }
                }

                SearchParameters searchParameters = identificationParameters.getSearchParameters();

                File fastaFile = searchParameters.getFastaFile();

                if (enableOmssa) {
                    // call Makeblastdb class, check if run before and then start process
                    makeblastdbProcessBuilder = new MakeblastdbProcessBuilder(getJarFilePath(), fastaFile, makeblastdbLocation, waitingHandler, exceptionHandler);

                    if (makeblastdbProcessBuilder.needsFormatting()) {

                        // @TODO: should the MS-GF+ database formatting be done here as well..?
                        if (waitingHandler != null) {
                            if (!useCommandLine) {
                                waitingHandler.setWaitingText("Formatting " + makeblastdbProcessBuilder.getCurrentlyProcessedFileName() + " for OMSSA.");
                            }
                            waitingHandler.appendReport("Formatting " + makeblastdbProcessBuilder.getCurrentlyProcessedFileName() + " for OMSSA.", true, true);
                            waitingHandler.appendReportEndLine();
                        }
                        makeblastdbProcessBuilder.startProcess();

                        if (waitingHandler != null) {
                            waitingHandler.appendReport(makeblastdbProcessBuilder.getCurrentlyProcessedFileName() + " formatted for OMSSA.", true, true);
                            waitingHandler.appendReportEndLine();
                        }
                    }

                    // Write modification files to the OMSSA directory and save PTM indexes in the search parameters
                    File modsXmlFile = new File(omssaLocation, "mods.xml");
                    if (!modsXmlFile.exists()) {
                        throw new IllegalArgumentException("OMSSA mods.xml file not found.");
                    }
                    File userModsXmlFile = new File(omssaLocation, "usermods.xml");
                    omssaProcessBuilder.writeOmssaUserModificationsFile(userModsXmlFile, identificationParameters, identificationParametersFile);

                    // Copy the files to the results folder
                    File destinationFile = new File(outputTempFolder, "omssa_mods.xml");
                    Util.copyFile(modsXmlFile, destinationFile);
                    destinationFile = new File(outputTempFolder, "omssa_usermods.xml");
                    Util.copyFile(userModsXmlFile, destinationFile);
                }

                if (enableAndromeda) {
                    if (!useCommandLine) {
                        waitingHandler.setWaitingText("Andromeda configuration.");
                    }
                    waitingHandler.appendReport("Andromeda configuration.", true, true);
                    waitingHandler.appendReportEndLine();
                    // write Andromeda database configuration file
                    AndromedaProcessBuilder.createDatabaseFile(andromedaLocation, searchParameters);
                    // write Andromeda enzyme configuration file
                    AndromedaProcessBuilder.createEnzymesFile(andromedaLocation);
                    // write Andromeda PTM configuration file and save PTM indexes in the search parameters
                    AndromedaProcessBuilder.createPtmFile(andromedaLocation, identificationParameters, identificationParametersFile);
                }

                int nRawFiles = getRawFiles().size();
                int nFilesToSearch = nRawFiles + getMgfFiles().size();
                int nProgress = 2 + nRawFiles;
                if (isOmssaEnabled()) {
                    nProgress += nFilesToSearch;
                }
                if (isXtandemEnabled()) {
                    nProgress += nFilesToSearch;
                }
                if (enableMsgf) {
                    nProgress += nFilesToSearch;
                }
                if (enableMsAmanda) {
                    nProgress += nFilesToSearch;
                }
                if (enableMyriMatch) {
                    nProgress += nFilesToSearch;
                }
                if (enableComet) {
                    nProgress += nFilesToSearch;
                }
                if (enableTide) {
                    nProgress += nFilesToSearch;
                    nProgress++; // the tide indexing
                }
                if (enableAndromeda) {
                    nProgress += nFilesToSearch;
                }
                if (enablePeptideShaker) {
                    nProgress++;
                }
                if (isReporterEnabled()) {
                    nProgress++;
                }

                waitingHandler.setMaxPrimaryProgressCounter(nProgress);
                waitingHandler.increasePrimaryProgressCounter(); // just to not be stuck at 0% for the whole first search

                if (enableTide && !waitingHandler.isRunCanceled()) {
                    // create the tide index
                    tideIndexProcessBuilder = new TideIndexProcessBuilder(tideLocation, searchParameters, waitingHandler, exceptionHandler);
                    waitingHandler.appendReport("Indexing " + fastaFile.getName() + " for Tide.", true, true);
                    waitingHandler.appendReportEndLine();
                    tideIndexProcessBuilder.startProcess();
                }

                // convert raw files
                ExecutorService pool = Executors.newFixedThreadPool(processingPreferences.getnThreads());

                ArrayList<File> rawFiles = getRawFiles();

                if (!rawFiles.isEmpty() && !waitingHandler.isRunCanceled()) {

                    waitingHandler.resetSecondaryProgressCounter();
                    waitingHandler.setMaxSecondaryProgressCounter(rawFiles.size() * 100);

                    msConvertProcessBuilders = new ArrayList<MsConvertProcessBuilder>();

                    Duration conversionDuration = new Duration();
                    if (rawFiles.size() > 1) {
                        conversionDuration.start();
                        waitingHandler.appendReport("Converting raw files.", true, true);
                    }

                    for (int i = 0; i < rawFiles.size() && !waitingHandler.isRunCanceled(); i++) {

                        File rawFile = rawFiles.get(i);
                        String rawFileName = rawFile.getName();
                        File folder = rawFile.getParentFile();
                        String mgfFileName = Util.removeExtension(rawFileName) + ".mgf";
                        File mgfFile = new File(folder, mgfFileName);
                        if (!mgfFile.exists()) {
                            MsConvertProcessBuilder msConvertProcessBuilder = new MsConvertProcessBuilder(waitingHandler, exceptionHandler, rawFile, folder, getMsConvertParameters());
                            msConvertProcessBuilders.add(msConvertProcessBuilder);
                            pool.submit(msConvertProcessBuilder);
                            // @TODO: validate the mgf file!
                        } else {
                            waitingHandler.appendReport(mgfFileName + " already exists. Conversion canceled.", true, true);
                            waitingHandler.appendReportEndLine();
                        }
                        mgfFiles.add(mgfFile);
                    }

                    if (waitingHandler.isRunCanceled()) {
                        pool.shutdownNow();
                    } else {
                        pool.shutdown();
                        if (!pool.awaitTermination(1 * rawFiles.size(), TimeUnit.DAYS)) {
                            throw new InterruptedException("Conversion timed out. Please contact the developers.");
                        }

                        if (!waitingHandler.isRunCanceled() && rawFiles.size() > 1) {
                            conversionDuration.end();
                            waitingHandler.appendReport("Raw files conversion completed (" + conversionDuration.toString() + ").", true, true);
                        }
                    }

                    waitingHandler.setSecondaryProgressCounterIndeterminate(true);
                }

                if (!waitingHandler.isRunCanceled()) {
                    // indexing the spectrum files
                    waitingHandler.appendReportEndLine();
                    waitingHandler.appendReport("Indexing spectrum files.", true, true);
                    SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();
                    for (File mgfFile : mgfFiles) {
                        spectrumFactory.addSpectra(mgfFile);
                    }

                    // indexing the spectrum files
                    waitingHandler.appendReport("Extracting search settings.", true, true);
                    waitingHandler.appendReportEndLine();
                }

                if (!waitingHandler.isRunCanceled()) {

                    saveInputFile(outputTempFolder);

                    // load database in parallel of the search (Note: apparently needs to be done after completion of makeblastdb)
                    if (generateProteinTree && UtilitiesUserPreferences.loadUserPreferences().getMemoryPreference() >= 4000) { // only build the tree if enough memory is available
                        proteinTreeWorker = new ProteinTreeWorker(waitingHandler);
                        proteinTreeWorker.execute();
                    }

                    waitingHandler.increasePrimaryProgressCounter();
                }

                // Keep track of the identification files created in a map: spectrum file name -> algorithm index -> identification file
                HashMap<String, HashMap<Integer, File>> identificationFiles = new HashMap<String, HashMap<Integer, File>>(mgfFiles.size());

                for (int i = 0; i < getMgfFiles().size() && !waitingHandler.isRunCanceled(); i++) {

                    File spectrumFile = getMgfFiles().get(i);

                    String spectrumFileName = spectrumFile.getName();
                    if (useCommandLine) {
                        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator")
                                + "Processing: " + spectrumFileName + " (" + (i + 1) + "/" + getMgfFiles().size() + ")");
                    } else {
                        waitingHandler.setWaitingText("Processing: " + spectrumFileName + " (" + (i + 1) + "/" + getMgfFiles().size() + ")");
                    }

                    if (enableXtandem && !waitingHandler.isRunCanceled()) {
                        File xTandemOutputFile = new File(outputTempFolder, Util.removeExtension(spectrumFileName) + ".t.xml");
                        xTandemProcessBuilder = new TandemProcessBuilder(xtandemLocation,
                                spectrumFile.getAbsolutePath(), xTandemOutputFile.getAbsolutePath(),
                                searchParameters, waitingHandler, exceptionHandler, processingPreferences.getnThreads());

                        waitingHandler.appendReport("Processing " + spectrumFileName + " with " + Advocate.xtandem.getName() + ".", true, true);
                        waitingHandler.appendReportEndLine();
                        xTandemProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {
                            if (renameXTandemFile) {
                                ArrayList<File> result = getXTandemFiles(outputTempFolder, spectrumFileName);
                                if (result.size() == 1) {
                                    File xTandemFile = result.get(0);
                                    File destinationFile = new File(outputTempFolder, getXTandemFileName(spectrumFileName));
                                    try {
                                        xTandemFile.renameTo(destinationFile);
                                        xTandemFile = destinationFile;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        waitingHandler.appendReport("Could not rename " + Advocate.xtandem.getName() + " result for " + spectrumFileName + ".", true, true);
                                    }
                                } else {
                                    waitingHandler.appendReport("Could not rename " + Advocate.xtandem.getName() + " result for " + spectrumFileName + ".", true, true);
                                }
                            }
                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);
                            if (runIdentificationFiles == null) {
                                runIdentificationFiles = new HashMap<Integer, File>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);
                            }
                            if (xTandemOutputFile.exists()) {
                                runIdentificationFiles.put(Advocate.xtandem.getIndex(), xTandemOutputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.xtandem.getName() + " result file for " + spectrumFileName + ".", true, true);
                            }
                            waitingHandler.increasePrimaryProgressCounter();
                        }
                    }

                    if (enableMyriMatch && !waitingHandler.isRunCanceled()) {
                        File myriMatchOutputFile = new File(outputTempFolder, getMyriMatchFileName(spectrumFileName));
                        myriMatchProcessBuilder = new MyriMatchProcessBuilder(myriMatchLocation,
                                spectrumFile.getAbsolutePath(), outputTempFolder, searchParameters, waitingHandler, exceptionHandler, processingPreferences.getnThreads());
                        waitingHandler.appendReport("Processing " + spectrumFileName + " with " + Advocate.myriMatch.getName() + ".", true, true);
                        waitingHandler.appendReportEndLine();
                        myriMatchProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {
                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);
                            if (runIdentificationFiles == null) {
                                runIdentificationFiles = new HashMap<Integer, File>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);
                            }

                            if (myriMatchOutputFile.exists()) {
                                runIdentificationFiles.put(Advocate.myriMatch.getIndex(), myriMatchOutputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.myriMatch.getName() + " result file for " + spectrumFileName + ".", true, true);
                            }
                            waitingHandler.increasePrimaryProgressCounter();
                        }
                    }

                    if (enableMsAmanda && !waitingHandler.isRunCanceled()) {
                        File msAmandaOutputFile = new File(outputTempFolder, Util.removeExtension(spectrumFileName) + ".ms-amanda.csv");
                        String filePath = msAmandaOutputFile.getAbsolutePath();
                        msAmandaProcessBuilder = new MsAmandaProcessBuilder(msAmandaLocation,
                                spectrumFile.getAbsolutePath(), filePath, searchParameters, waitingHandler, exceptionHandler, processingPreferences.getnThreads());
                        waitingHandler.appendReport("Processing " + spectrumFileName + " with " + Advocate.msAmanda.getName() + ".", true, true);
                        waitingHandler.appendReportEndLine();
                        msAmandaProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {
                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);
                            if (runIdentificationFiles == null) {
                                runIdentificationFiles = new HashMap<Integer, File>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);
                            }
                            if (msAmandaOutputFile.exists()) {
                                runIdentificationFiles.put(Advocate.msAmanda.getIndex(), msAmandaOutputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.msAmanda.getName() + " result file for " + spectrumFileName + ".", true, true);
                            }
                            waitingHandler.increasePrimaryProgressCounter();
                        }
                    }

                    if (enableMsgf && !waitingHandler.isRunCanceled()) {
                        File msgfOutputFile = new File(outputTempFolder, Util.removeExtension(spectrumFileName) + ".msgf.mzid");
                        msgfProcessBuilder = new MsgfProcessBuilder(msgfLocation,
                                spectrumFile.getAbsolutePath(), msgfOutputFile, searchParameters, waitingHandler, exceptionHandler, processingPreferences.getnThreads(), useCommandLine);
                        waitingHandler.appendReport("Processing " + spectrumFileName + " with " + Advocate.msgf.getName() + ".", true, true);
                        waitingHandler.appendReportEndLine();
                        msgfProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {
                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);
                            if (runIdentificationFiles == null) {
                                runIdentificationFiles = new HashMap<Integer, File>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);
                            }
                            if (msgfOutputFile.exists()) {
                                runIdentificationFiles.put(Advocate.msgf.getIndex(), msgfOutputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.msgf.getName() + " result file for " + spectrumFileName + ".", true, true);
                            }
                            waitingHandler.increasePrimaryProgressCounter();
                        }
                    }

                    if (enableOmssa && !waitingHandler.isRunCanceled()) {
                        File omssaOutputFile = new File(outputTempFolder, getOMSSAFileName(spectrumFileName));
                        omssaProcessBuilder = new OmssaclProcessBuilder(omssaLocation,
                                spectrumFile.getAbsolutePath(), omssaOutputFile, searchParameters, waitingHandler, exceptionHandler, processingPreferences.getnThreads());
                        waitingHandler.appendReport("Processing " + spectrumFileName + " with " + Advocate.omssa.getName() + ".", true, true);
                        waitingHandler.appendReportEndLine();
                        omssaProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {
                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);
                            if (runIdentificationFiles == null) {
                                runIdentificationFiles = new HashMap<Integer, File>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);
                            }
                            if (omssaOutputFile.exists()) {
                                runIdentificationFiles.put(Advocate.omssa.getIndex(), omssaOutputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.omssa.getName() + " result file for " + spectrumFileName + ".", true, true);
                            }
                            waitingHandler.increasePrimaryProgressCounter();
                        }
                    }

                    File ms2File = null;
                    if ((enableComet || enableTide) && !waitingHandler.isRunCanceled()) {
                        waitingHandler.appendReport("Converting spectrum file " + spectrumFileName + " for Comet/Tide.", true, true); // @TODO: from "Comet 2015.02 rev. 0" mgf is supported directly
                        ms2File = new File(getPeakListFolder(getJarFilePath()), Util.removeExtension(spectrumFileName) + ".ms2");
                        Ms2Exporter.mgfToMs2(spectrumFile, ms2File, true);
                    }

                    if (enableComet && !waitingHandler.isRunCanceled()) {

                        File cometOutputFile = new File(outputTempFolder, getCometFileName(spectrumFileName));
                        // Comet does not overwrite files but crashes
                        if (cometOutputFile.exists()) {
                            cometOutputFile.delete();
                        }
                        cometProcessBuilder = new CometProcessBuilder(cometLocation, searchParameters, ms2File, waitingHandler, exceptionHandler, processingPreferences.getnThreads());
                        waitingHandler.appendReport("Processing " + spectrumFileName + " with " + Advocate.comet.getName() + ".", true, true);
                        waitingHandler.appendReportEndLine();
                        cometProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            // move the comet result file to the results folder
                            File tempCometOutputFile = new File(getPeakListFolder(getJarFilePath()), getCometFileName(spectrumFileName));
                            FileUtils.moveFile(tempCometOutputFile, cometOutputFile);

                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);
                            if (runIdentificationFiles == null) {
                                runIdentificationFiles = new HashMap<Integer, File>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);
                            }
                            if (cometOutputFile.exists()) {
                                runIdentificationFiles.put(Advocate.comet.getIndex(), cometOutputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.comet.getName() + " result file for " + spectrumFileName + ".", true, true);
                            }
                            waitingHandler.increasePrimaryProgressCounter();
                        }
                    }

                    if (enableTide && !waitingHandler.isRunCanceled()) {

                        File tideOutputFile = new File(outputTempFolder, getTideFileName(spectrumFileName));

                        // perform the tide search
                        if (!waitingHandler.isRunCanceled()) {
                            tideSearchProcessBuilder = new TideSearchProcessBuilder(tideLocation, searchParameters, ms2File, waitingHandler, exceptionHandler);
                            waitingHandler.appendReport("Processing " + spectrumFileName + " with " + Advocate.tide.getName() + ".", true, true);
                            waitingHandler.appendReportEndLine();
                            tideSearchProcessBuilder.startProcess();
                        }

                        if (!waitingHandler.isRunCanceled()) {

                            String tideResultsFolderName = ((TideParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.tide.getIndex())).getOutputFolderName();

                            // move the tide result file to the results folder
                            File tempTideOutputFile = new File(new File(tideLocation, tideResultsFolderName), getTideFileName(spectrumFileName));
                            FileUtils.moveFile(tempTideOutputFile, tideOutputFile);

                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);
                            if (runIdentificationFiles == null) {
                                runIdentificationFiles = new HashMap<Integer, File>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);
                            }
                            if (tideOutputFile.exists()) {
                                runIdentificationFiles.put(Advocate.tide.getIndex(), tideOutputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.tide.getName() + " result file for " + spectrumFileName + ".", true, true);
                            }
                            waitingHandler.increasePrimaryProgressCounter();
                        }
                    }

                    File aplFile = null;
                    if (enableAndromeda && !waitingHandler.isRunCanceled()) {
                        waitingHandler.appendReport("Converting spectrum file " + spectrumFileName + " for Andromeda.", true, true);
                        aplFile = new File(getPeakListFolder(getJarFilePath()), Util.removeExtension(spectrumFileName) + ".apl");
                        AndromedaParameters andromedaParameters = (AndromedaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex());
                        AplExporter.mgfToApl(spectrumFile, aplFile, andromedaParameters.getFragmentationMethod(), searchParameters.getMinChargeSearched().value, searchParameters.getMaxChargeSearched().value);
                    }

                    if (enableAndromeda && !waitingHandler.isRunCanceled()) {

                        File andromedaOutputFile = new File(outputTempFolder, getAndromedaFileName(spectrumFileName));
                        andromedaProcessBuilder = new AndromedaProcessBuilder(andromedaLocation, searchParameters, identificationParametersFile, aplFile, waitingHandler, exceptionHandler, processingPreferences.getnThreads());
                        waitingHandler.appendReport("Processing " + spectrumFileName + " with " + Advocate.andromeda.getName() + ".", true, true);
                        waitingHandler.appendReportEndLine();
                        andromedaProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            File tempResultFile = new File(aplFile.getParent(), getAndromedaFileName(spectrumFileName));
                            if (tempResultFile.exists()) {
                                Util.copyFile(tempResultFile, andromedaOutputFile);
                                try {
                                    tempResultFile.delete();
                                } catch (Exception e) {
                                    waitingHandler.appendReport("An error occurred when attempting to delete " + tempResultFile.getName() + ".", true, true);
                                }
                                HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);
                                if (runIdentificationFiles == null) {
                                    runIdentificationFiles = new HashMap<Integer, File>();
                                    identificationFiles.put(spectrumFileName, runIdentificationFiles);
                                }
                                if (andromedaOutputFile.exists()) {
                                    runIdentificationFiles.put(Advocate.andromeda.getIndex(), andromedaOutputFile);
                                } else {
                                    waitingHandler.appendReport("Could not find " + Advocate.andromeda.getName() + " result file for " + spectrumFileName + ".", true, true);
                                }
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.andromeda.getName() + " .res file for " + spectrumFileName + ".", true, true);
                            }
                            waitingHandler.increasePrimaryProgressCounter();
                        }
                    }

                    if (aplFile != null) {
                        aplFile.delete();
                    }
                    if (ms2File != null) {
                        ms2File.delete();
                    }
                }

                // delete the tide index and the crux-output folder?
                if (enableTide) {

                    TideParameters tideParameters = ((TideParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.tide.getIndex()));

                    if (tideParameters.getRemoveTempFolders()) {
                        String tideResultsFolderName = tideParameters.getOutputFolderName();
                        File tideResultsFolder = new File(tideLocation, tideResultsFolderName);
                        if (tideResultsFolder.exists()) {
                            FileUtils.deleteDirectory(tideResultsFolder);
                        }

                        File tideIndexFolder = new File(tideLocation, "fasta-index");
                        if (tideIndexFolder.exists()) {
                            FileUtils.deleteDirectory(tideIndexFolder);
                        }
                    }
                }

                if (!waitingHandler.isRunCanceled()) {
                    // organize the output files
                    waitingHandler.appendReport("Zipping output files.", true, true);
                    waitingHandler.appendReportEndLine();
                    organizeOutput(outputFolder, outputTempFolder, identificationFiles, identificationParametersFile, includeDateInOutputName);
                    waitingHandler.increasePrimaryProgressCounter();
                }

                if (enablePeptideShaker && !waitingHandler.isRunCanceled()) {

                    ArrayList<File> identificationFilesList = new ArrayList<File>();
                    identificationFilesList.addAll(mascotFiles);

                    if (outputOption == OutputOption.grouped) {
                        File outputFile = getDefaultOutputFile(outputFolder, includeDateInOutputName);
                        if (outputFile.exists()) {
                            identificationFilesList.add(outputFile);
                        } else {
                            waitingHandler.appendReport("Could not find SearchGUI results.", true, true);
                        }
                    } else if (outputOption == OutputOption.algorithm) {
                        if (enableMsAmanda) {
                            File outputFile = getDefaultOutputFile(outputFolder, Advocate.msAmanda.getName(), includeDateInOutputName);
                            if (outputFile.exists()) {
                                identificationFilesList.add(outputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.msAmanda.getName() + " results.", true, true);
                            }
                        }
                        if (enableMsgf) {
                            File outputFile = getDefaultOutputFile(outputFolder, Advocate.msgf.getName(), includeDateInOutputName);
                            if (outputFile.exists()) {
                                identificationFilesList.add(outputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.msgf.getName() + " results.", true, true);
                            }
                        }
                        if (enableMyriMatch) {
                            File outputFile = getDefaultOutputFile(outputFolder, Advocate.myriMatch.getName(), includeDateInOutputName);
                            if (outputFile.exists()) {
                                identificationFilesList.add(outputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.myriMatch.getName() + " results.", true, true);
                            }
                        }
                        if (enableOmssa) {
                            File outputFile = getDefaultOutputFile(outputFolder, Advocate.omssa.getName(), includeDateInOutputName);
                            if (outputFile.exists()) {
                                identificationFilesList.add(outputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.omssa.getName() + " results.", true, true);
                            }
                        }
                        if (enableXtandem) {
                            File outputFile = getDefaultOutputFile(outputFolder, Advocate.xtandem.getName(), includeDateInOutputName);
                            if (outputFile.exists()) {
                                identificationFilesList.add(outputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.xtandem.getName() + " results.", true, true);
                            }
                        }
                        if (enableComet) {
                            File outputFile = getDefaultOutputFile(outputFolder, Advocate.comet.getName(), includeDateInOutputName);
                            if (outputFile.exists()) {
                                identificationFilesList.add(outputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.comet.getName() + " results.", true, true);
                            }
                        }
                        if (enableTide) {
                            File outputFile = getDefaultOutputFile(outputFolder, Advocate.tide.getName(), includeDateInOutputName);
                            if (outputFile.exists()) {
                                identificationFilesList.add(outputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.tide.getName() + " results.", true, true);
                            }
                        }
                        if (enableAndromeda) {
                            File outputFile = getDefaultOutputFile(outputFolder, Advocate.andromeda.getName(), includeDateInOutputName);
                            if (outputFile.exists()) {
                                identificationFilesList.add(outputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.andromeda.getName() + " results.", true, true);
                            }
                        }
                    } else if (outputOption == OutputOption.run) {
                        for (String run : identificationFiles.keySet()) {
                            String runName = Util.removeExtension(run);
                            File outputFile = getDefaultOutputFile(outputFolder, runName, includeDateInOutputName);
                            if (outputFile.exists()) {
                                identificationFilesList.add(outputFile);
                            } else {
                                waitingHandler.appendReport("SearchGUI results not found for run " + runName + ".", true, true);
                            }
                        }
                    } else {
                        for (HashMap<Integer, File> fileMap : identificationFiles.values()) {
                            for (File identificationFile : fileMap.values()) {
                                identificationFilesList.add(identificationFile);
                            }
                        }
                    }

                    UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();

                    if (utilitiesUserPreferences.getPeptideShakerPath() == null || !new File(utilitiesUserPreferences.getPeptideShakerPath()).exists()) {
                        waitingHandler.appendReport("PeptideShaker not found! Please check the PeptideShaker path.", true, true);
                        waitingHandler.appendReportEndLine();
                        waitingHandler.setRunCanceled();
                    } else {
                        if (!identificationFiles.isEmpty()) {
                            peptideShakerProcessBuilder = new PeptideShakerProcessBuilder(
                                    waitingHandler, exceptionHandler, experimentLabel, sampleLabel, replicateNumber, mgfFiles, identificationFilesList,
                                    identificationParameters, identificationParametersFile, peptideShakerFile, true, processingPreferences, outputData);
                            waitingHandler.appendReport("Processing identification files with PeptideShaker.", true, true);

                            // cancel the protein tree if not done
                            if (proteinTreeWorker != null && !proteinTreeWorker.isFinished()) {
                                proteinTreeWorker.cancelBuild();
                                while (!proteinTreeWorker.isFinished()) {
                                    // wait until the tree is closed
                                    Thread.sleep(100);
                                }
                            }

                            peptideShakerProcessBuilder.startProcess();
                        } else {
                            enablePeptideShaker = false;
                            waitingHandler.appendReportEndLine();
                            waitingHandler.appendReport("No identification files to process with PeptideShaker!", true, true);
                            waitingHandler.appendReportEndLine();
                        }
                    }
                } else {
                    // cancel the protein tree if not done
                    if (proteinTreeWorker != null && !proteinTreeWorker.isFinished()) {
                        proteinTreeWorker.cancelBuild();
                        while (!proteinTreeWorker.isFinished()) {
                            // wait until the tree is closed
                            Thread.sleep(10);
                        }
                    }
                }

                if (!outputFolder.getAbsolutePath().equals(outputTempFolder.getAbsolutePath())) {
                    Util.deleteDir(outputTempFolder);
                }

                if (enableAndromeda && AndromedaProcessBuilder.getTempFolderPath() != null) {
                    File andromedaTempFolder = new File(AndromedaProcessBuilder.getTempFolderPath());
                    if (andromedaTempFolder.exists()) {
                        Util.emptyDir(andromedaTempFolder);
                    }
                }

                finished = true;
                if (!waitingHandler.isRunCanceled()) {
                    searchCompleted();
                } else {
                    searchCrashed();
                }

                notifySearchFinished();

                return 0;
            } catch (Exception e) {
                waitingHandler.appendReport("Error: " + e.getMessage(), true, true);
                waitingHandler.appendReport("An error occurred while running SearchGUI. Please contact the developers.", true, true);
                e.printStackTrace();
                searchCrashed();
                return 1;
            }
        }

        /**
         * Returns a boolean indicating whether the searches have finished.
         *
         * @return a boolean indicating whether the searches have finished
         */
        public boolean isFinished() {
            return finished;
        }
    }

    /**
     * Save the input file.
     *
     * @param folder the folder where to save the input file
     */
    public void saveInputFile(File folder) {

        File outputFile = getInputFile(folder);
        ArrayList<File> mgfFiles = new ArrayList<File>(this.mgfFiles);
        ArrayList<String> names = new ArrayList<String>();
        for (File file : mgfFiles) {
            names.add(file.getName());
        }
        if (outputFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(outputFile));
                String line;
                while ((line = br.readLine()) != null) {
                    // Skip empty lines.
                    line = line.trim();
                    if (!line.equals("")) {
                        try {
                            File newFile = new File(line);
                            if (!names.contains(newFile.getName())) {
                                names.add(newFile.getName());
                                mgfFiles.add(newFile);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                br.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                // ignore error
                mgfFiles = new ArrayList<File>(this.mgfFiles);
            }
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            for (File mgfFile : mgfFiles) {
                bw.write(mgfFile.getAbsolutePath() + System.getProperty("line.separator"));
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            // ignore error
        }
    }

    /**
     * Worker which indexes files when necessary.
     */
    private class IndexingWorker extends SwingWorker {

        /**
         * The waiting handler displaying feedback to the user.
         */
        private WaitingHandler waitingHandler;
        /**
         * Boolean indicating that the processing is finished.
         */
        private boolean finished = false;

        /**
         * Constructor of the worker.
         *
         * @param waitingHandler
         */
        public IndexingWorker(WaitingHandler waitingHandler) {
            this.waitingHandler = waitingHandler;

            if (waitingHandler instanceof WaitingDialog) {
                // make sure the icon is set to the waiting icon
                ((JFrame) ((WaitingDialog) waitingHandler).getParent()).setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
            }
        }

        @Override
        protected Object doInBackground() throws Exception {

            File fastaFile = identificationParameters.getSearchParameters().getFastaFile();
            File indexFile = new File(fastaFile.getParent(), fastaFile.getName() + ".cui");

            if (!indexFile.exists()) {
                SequenceFactory.getInstance().loadFastaFile(fastaFile, waitingHandler);
            }

            finished = true;

            if (!waitingHandler.isRunCanceled()) {
                searchCompleted();
            }

            return 0;
        }

        /**
         * Returns a boolean indicating whether the indexing is finished.
         *
         * @return a boolean indicating whether the indexing is finished
         */
        public boolean isFinished() {
            return finished;
        }
    }

    /**
     * Worker which builds the protein tree.
     */
    private class ProteinTreeWorker extends SwingWorker {

        /**
         * The waiting handler displaying feedback to the user.
         */
        private WaitingHandler proteinTreeWaitingHandler;
        /**
         * Boolean indicating that the processing is finished.
         */
        private boolean finished = false;
        /**
         * Boolean indicating if the waiting handler should be command line or
         * GUI.
         */
        private boolean commandlineWaitingHandler = true; // note: only set to false for testing purposes
        /**
         * The GUI waiting dialog.
         */
        private WaitingDialog guiWaitingDialog;

        /**
         * Constructor of the worker.
         *
         * @param waitingHandler
         */
        public ProteinTreeWorker(WaitingHandler waitingHandler) {
            if (waitingHandler instanceof WaitingDialog) {
                guiWaitingDialog = (WaitingDialog) waitingHandler;
                ((JFrame) guiWaitingDialog.getParent()).setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
            }
            if (commandlineWaitingHandler) {
                this.proteinTreeWaitingHandler = new WaitingHandlerCLIImpl(); // @TODO: find a way of displaying this process in the waiting dialog?
            } else {
                this.proteinTreeWaitingHandler = waitingHandler;
            }
        }

        @Override
        protected synchronized Object doInBackground() throws Exception {

            SequenceFactory sequenceFactory = SequenceFactory.getInstance();

            try {
                File fastaFile = sequenceFactory.getCurrentFastaFile();
                Duration indexingTime = new Duration();
                indexingTime.start();
                proteinTreeWaitingHandler.appendReport("Importing " + fastaFile.getName(), true, true);
                UtilitiesUserPreferences userPreferences = UtilitiesUserPreferences.loadUserPreferences();
                int memoryPreference = userPreferences.getMemoryPreference();
                long fileSize = fastaFile.length();
                long nSequences = sequenceFactory.getNTargetSequences();
                if (!sequenceFactory.isDefaultReversed()) {
                    nSequences = sequenceFactory.getNSequences();
                }
                long sequencesPerMb = 1048576 * nSequences / fileSize;
                long availableCachSize = 3 * memoryPreference * sequencesPerMb / 4;
                if (availableCachSize > nSequences) {
                    availableCachSize = nSequences;
                } else {
                    proteinTreeWaitingHandler.appendReport("Warning: SearchGUI cannot load your FASTA file entirely into memory. This will slow down the processing. "
                            + "Note that using large large databases also induces random hits efficiency. "
                            + "Try to either (i) use a smaller database, (ii) increase the memory provided to DeNovoGUI, or (iii) improve the reading speed by using an SSD disc. "
                            + "(See also http://compomics.github.io/compomics-utilities/wiki/proteininference.html.)", true, true);
                }
                int cacheSize = (int) availableCachSize;
                sequenceFactory.setnCache(cacheSize);
                sequenceFactory.getDefaultProteinTree(processingPreferences.getnThreads(), proteinTreeWaitingHandler, exceptionHandler, true);
                if (!proteinTreeWaitingHandler.isRunCanceled()) {
                    indexingTime.end();
                    proteinTreeWaitingHandler.appendReport("Importing " + sequenceFactory.getFileName() + " finished (" + indexingTime.toString() + ").", true, true);
                } else {
                    proteinTreeWaitingHandler.appendReport("Importing " + sequenceFactory.getFileName() + " canceled.", true, true);
                }
                sequenceFactory.emptyCache();
            } catch (Exception e) {
                e.printStackTrace();
                proteinTreeWaitingHandler.appendReport("Importing " + sequenceFactory.getFileName() + " failed: " + e.getMessage(), true, true);
            } catch (OutOfMemoryError error) {
                System.out.println("Ran out of memory building the protein tree!");
                cancelBuild();
            }

            sequenceFactory.clearFactory();

            if (proteinTreeWaitingHandler.isRunCanceled()) {
                sequenceFactory.deleteProteinTree(exceptionHandler);
            }

            DerbyUtil.closeConnection();

            if (guiWaitingDialog != null) {
                // change the icon to the waiting icon
                ((JFrame) guiWaitingDialog.getParent()).setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
            }

            finished = true;
            return 0;
        }

        /**
         * Cancel the building of the tree.
         */
        private void cancelBuild() {
            proteinTreeWaitingHandler.setRunCanceled();
        }

        /**
         * Returns a boolean indicating whether the loading of the protein tree
         * is finished.
         *
         * @return a boolean indicating whether the loading of the protein tree
         * is finished
         */
        public boolean isFinished() {
            return finished;
        }
    }

    /**
     * Set the identification parameters.
     *
     * @param identificationParameters the identification parameters
     */
    public void setIdentificationParameters(IdentificationParameters identificationParameters) {
        this.identificationParameters = identificationParameters;
    }

    /**
     * Set the identification parameters file.
     *
     * @param identificationParametersFile the identification parameters file
     */
    public void setIdentificationParametersFile(File identificationParametersFile) {
        this.identificationParametersFile = identificationParametersFile;
    }

    /**
     * Sets the mascot files.
     *
     * @param mascotFiles the mascot files
     */
    public void setMascotFiles(ArrayList<File> mascotFiles) {
        this.mascotFiles = mascotFiles;
    }

    /**
     * Returns the Mascot files.
     *
     * @return the mascot files
     */
    public ArrayList<File> getMascotFiles() {
        return mascotFiles;
    }

    /**
     * Returns the processing preferences.
     *
     * @return the processingPreferences
     */
    public ProcessingPreferences getProcessingPreferences() {
        return processingPreferences;
    }

    /**
     * Set the processing preferences.
     *
     * @param processingPreferences the processingPreferences to set
     */
    public void setProcessingPreferences(ProcessingPreferences processingPreferences) {
        this.processingPreferences = processingPreferences;
    }

    /**
     * Returns the msconvert parameters.
     *
     * @return the msconvert parameters
     */
    public MsConvertParameters getMsConvertParameters() {
        return msConvertParameters;
    }

    /**
     * Sets the msconvert parameters.
     *
     * @param msConvertParameters the msconvert parameters
     */
    public void setMsConvertParameters(MsConvertParameters msConvertParameters) {
        this.msConvertParameters = msConvertParameters;
    }

    /**
     * Returns the file containing the enzymes.
     *
     * @return the file containing the enzymes
     */
    public static String getEnzymeFile() {
        return enzymeFile;
    }

    /**
     * Sets the file containing the enzymes.
     *
     * @param enzymeFile the file containing the enzymes
     */
    public static void setEnzymeFile(String enzymeFile) {
        SearchHandler.enzymeFile = enzymeFile;
    }

    /**
     * Returns the default output file produced by SearchGUI.
     *
     * @param outputFolder the folder where to put the file
     * @param includeDate if true the date will be included in the output file
     * name
     *
     * @return the default output file produced by SearchGUI
     */
    public static File getDefaultOutputFile(File outputFolder, boolean includeDate) {
        String fileName = "";
        if (includeDate) {
            fileName += getOutputDate() + "_";
        }
        fileName += defaultOutput;
        return new File(outputFolder, fileName);
    }

    /**
     * Returns the default output file produced by SearchGUI classified
     * according to a classifier. For example "OMSSA_searchgui.zip".
     *
     * @param outputFolder the folder where to put the file
     * @param classifier the first part of the name used to classify the output
     * @param includeDate if true the date will be included in the output file
     * name
     *
     * @return the default output file produced by SearchGUI
     */
    public static File getDefaultOutputFile(File outputFolder, String classifier, boolean includeDate) {
        String fileName = classifier;
        if (includeDate) {
            fileName += "_" + getOutputDate();
        }
        fileName += "_" + defaultOutput;
        return new File(outputFolder, fileName);
    }

    /**
     * Returns the date as a string to be included in the output.
     * YYYY-MM-DD_HH.MM.SS.
     *
     * @return the date as a string to be included in the output
     */
    public static String getOutputDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH)
                + "_" + calendar.get(Calendar.HOUR_OF_DAY) + "." + calendar.get(Calendar.MINUTE) + "." + calendar.get(Calendar.SECOND);
    }

    /**
     * Returns the file where the spectrum file names are saved.
     *
     * @param outputFolder the folder where this file shall be saved.
     *
     * @return the file where the spectrum file names are saved
     */
    public static File getInputFile(File outputFolder) {
        return new File(outputFolder, SEARCHGUI_INPUT);
    }

    /**
     * Organizes the identification files in zip files according to the output
     * option of the search handler. Existing zip files will be overwritten and
     * result files will be deleted.
     *
     * @param outputFolder the output folder
     * @param tempOutputFolder the folder where the raw searchgui output is
     * stored
     * @param identificationFiles the identification files
     * @param parametersFile the parameters file
     * @param includeDate if true the date will be included in the output file
     * name
     *
     * @throws IOException thrown if there is a problem with the files
     */
    public void organizeOutput(File outputFolder, File tempOutputFolder, HashMap<String, HashMap<Integer, File>> identificationFiles, File parametersFile, boolean includeDate) throws IOException {

        switch (outputOption) {

            case grouped:

                // put everything in a single zip file and delete old zip files
                File zipFile = getDefaultOutputFile(outputFolder, includeDate);

                if (zipFile.exists()) {
                    zipFile.delete();
                }

                FileOutputStream fos = new FileOutputStream(zipFile);

                try {
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    try {
                        ZipOutputStream out = new ZipOutputStream(bos);

                        // find the uncompressed size of all the files to add to the zip
                        long totalUncompressedSize = getTotalUncompressedSize(tempOutputFolder, parametersFile, identificationFiles);
                        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                        waitingHandler.setSecondaryProgressCounter(0);
                        waitingHandler.setMaxSecondaryProgressCounter(100);

                        try {
                            // add input file
                            File inputFile = getInputFile(tempOutputFolder);
                            ZipUtils.addFileToZip(inputFile, out, waitingHandler, totalUncompressedSize);

                            // add search parameters files
                            ZipUtils.addFileToZip(parametersFile, out, waitingHandler, totalUncompressedSize);

                            if (enableOmssa) {
                                // add OMSSA modification files
                                File modificationsFile = new File(tempOutputFolder, "omssa_mods.xml");
                                ZipUtils.addFileToZip(modificationsFile, out, waitingHandler, totalUncompressedSize);

                                File userModificationsFile = new File(tempOutputFolder, "omssa_usermods.xml");
                                ZipUtils.addFileToZip(userModificationsFile, out, waitingHandler, totalUncompressedSize);
                            }

                            if (enableMsAmanda) {
                                // add MS Amanda settings file
                                for (File spectrumFile : mgfFiles) {
                                    String newName = Util.removeExtension(spectrumFile.getName()) + "_settings.xml";
                                    File settingsFile = new File(tempOutputFolder, newName);
                                    if (settingsFile.exists()) {
                                        ZipUtils.addFileToZip(settingsFile, out, waitingHandler, totalUncompressedSize);
                                    }
                                }
                            }

                            for (HashMap<Integer, File> fileMap : identificationFiles.values()) {
                                for (File identificationFile : fileMap.values()) {
                                    ZipUtils.addFileToZip(identificationFile, out, waitingHandler, totalUncompressedSize);
                                }
                            }

                            if (outputData) {
                                addDataToZip(out, totalUncompressedSize);
                            }
                        } finally {
                            out.close();
                        }
                    } finally {
                        bos.close();
                    }
                } finally {
                    fos.close();
                }

                break;

            case algorithm:

                // group files according to the search engine used
                HashMap<Integer, ArrayList<File>> algorithmToFileMap = new HashMap<Integer, ArrayList<File>>();
                for (HashMap<Integer, File> fileMap : identificationFiles.values()) {
                    for (Integer algorithm : fileMap.keySet()) {
                        ArrayList<File> files = algorithmToFileMap.get(algorithm);
                        if (files == null) {
                            files = new ArrayList<File>();
                            algorithmToFileMap.put(algorithm, files);
                        }
                        files.add(fileMap.get(algorithm));
                    }
                }

                File inputFile = getInputFile(tempOutputFolder);

                // find the uncompressed size of all the files to add to the zip
                long totalUncompressedSize = 0;
                for (Integer algorithm : algorithmToFileMap.keySet()) {
                    totalUncompressedSize += getTotalUncompressedSizeAlgorithm(inputFile, tempOutputFolder, algorithm, parametersFile, algorithmToFileMap.get(algorithm));
                }
                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                waitingHandler.setSecondaryProgressCounter(0);
                waitingHandler.setMaxSecondaryProgressCounter(100);

                for (Integer algorithm : algorithmToFileMap.keySet()) {

                    String advocateName = Advocate.getAdvocate(algorithm).getName();
                    zipFile = getDefaultOutputFile(outputFolder, advocateName, includeDate);

                    if (zipFile.exists()) {
                        zipFile.delete();
                    }
                    fos = new FileOutputStream(zipFile);
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        try {
                            ZipOutputStream out = new ZipOutputStream(bos);
                            try {
                                // add input file
                                ZipUtils.addFileToZip(inputFile, out, waitingHandler, totalUncompressedSize);

                                // add search parameters files
                                ZipUtils.addFileToZip(parametersFile, out, waitingHandler, totalUncompressedSize);

                                if (algorithm == Advocate.omssa.getIndex()) {
                                    File modificationsFile = new File(tempOutputFolder, "omssa_mods.xml");
                                    ZipUtils.addFileToZip(modificationsFile, out, waitingHandler, totalUncompressedSize);

                                    File userModificationsFile = new File(tempOutputFolder, "omssa_usermods.xml");
                                    ZipUtils.addFileToZip(userModificationsFile, out, waitingHandler, totalUncompressedSize);
                                }
                                if (algorithm == Advocate.msAmanda.getIndex()) {
                                    // add MS Amanda settings file
                                    for (File spectrumFile : mgfFiles) {
                                        String newName = Util.removeExtension(spectrumFile.getName()) + "_settings.xml";
                                        File settingsFile = new File(tempOutputFolder, newName);
                                        if (settingsFile.exists()) {
                                            ZipUtils.addFileToZip(settingsFile, out, waitingHandler, totalUncompressedSize);
                                        }
                                    }
                                }

                                for (File identificationFile : algorithmToFileMap.get(algorithm)) {
                                    ZipUtils.addFileToZip(identificationFile, out, waitingHandler, totalUncompressedSize);
                                }

                                if (outputData) {
                                    addDataToZip(out, totalUncompressedSize);
                                }

                            } finally {
                                out.close();
                            }
                        } finally {
                            bos.close();
                        }
                    } finally {
                        fos.close();
                    }
                }

                break;

            case run:

                // group files according to the run name
                inputFile = getInputFile(tempOutputFolder);

                // find the uncompressed size of all the files to add to the zip
                totalUncompressedSize = 0;
                for (String run : identificationFiles.keySet()) {
                    String runName = Util.removeExtension(run);
                    totalUncompressedSize += getTotalUncompressedSizeRun(inputFile, tempOutputFolder, runName, run, parametersFile, identificationFiles);
                }
                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                waitingHandler.setSecondaryProgressCounter(0);
                waitingHandler.setMaxSecondaryProgressCounter(100);

                for (String run : identificationFiles.keySet()) {

                    String runName = Util.removeExtension(run);
                    zipFile = getDefaultOutputFile(outputFolder, runName, includeDate);

                    if (zipFile.exists()) {
                        zipFile.delete();
                    }

                    fos = new FileOutputStream(zipFile);

                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(fos);

                        try {
                            ZipOutputStream out = new ZipOutputStream(bos);

                            try {
                                // add input file
                                ZipUtils.addFileToZip(inputFile, out, waitingHandler, totalUncompressedSize);

                                // add search parameters files
                                ZipUtils.addFileToZip(parametersFile, out, waitingHandler, totalUncompressedSize);

                                if (enableOmssa) {
                                    // add OMSSA modification files
                                    File modificationsFile = new File(tempOutputFolder, "omssa_mods.xml");
                                    ZipUtils.addFileToZip(modificationsFile, out, waitingHandler, totalUncompressedSize);

                                    File userModificationsFile = new File(tempOutputFolder, "omssa_usermods.xml");
                                    ZipUtils.addFileToZip(userModificationsFile, out, waitingHandler, totalUncompressedSize);
                                }

                                if (enableMsAmanda) {
                                    // add MS Amanda settings file
                                    String newName = runName + "_settings.xml";
                                    File settingsFile = new File(tempOutputFolder, newName);
                                    if (settingsFile.exists()) {
                                        ZipUtils.addFileToZip(settingsFile, out, waitingHandler, totalUncompressedSize);
                                    }
                                }

                                HashMap<Integer, File> fileMap = identificationFiles.get(run);
                                for (File identificationFile : fileMap.values()) {
                                    ZipUtils.addFileToZip(identificationFile, out, waitingHandler, totalUncompressedSize);
                                }

                                if (outputData) {
                                    addDataToZip(out, totalUncompressedSize);
                                }

                            } finally {
                                out.close();
                            }
                        } finally {
                            bos.close();
                        }
                    } finally {
                        fos.close();
                    }
                }

                break;

            default: // no zipping

                File copiedParametersFile = new File(outputFolder, identificationParametersFile.getName());
                if (!copiedParametersFile.exists()) {
                    Util.copyFile(identificationParametersFile, copiedParametersFile);
                }

                // add data files if needed
                if (outputData) {
                    // create the data folder
                    File dataFolder = new File(outputFolder, defaultDataFolder);
                    dataFolder.mkdir();

                    File dbFile = identificationParameters.getSearchParameters().getFastaFile();
                    Util.copyFile(dbFile, new File(dataFolder, dbFile.getName()));

                    for (File spectrumFile : getMgfFiles()) {
                        Util.copyFile(spectrumFile, new File(dataFolder, spectrumFile.getName()));
                    }
                }
        }

        if (!outputFolder.getAbsolutePath().equals(tempOutputFolder.getAbsolutePath())) {
            Util.deleteDir(tempOutputFolder);
        }
    }

    /**
     * Adds the mgf and FASTA files to the zip file.
     *
     * @param out the zip stream
     *
     * @throws IOException
     */
    private void addDataToZip(ZipOutputStream out, long totalUncompressedSize) throws IOException {

        // create the data folder in the zip file
        ZipUtils.addFolderToZip(defaultDataFolder, out);

        File dbFile = identificationParameters.getSearchParameters().getFastaFile();
        ZipUtils.addFileToZip(defaultDataFolder, dbFile, out, waitingHandler, totalUncompressedSize);

        for (File spectrumFile : getMgfFiles()) {
            ZipUtils.addFileToZip(defaultDataFolder, spectrumFile, out, waitingHandler, totalUncompressedSize);
        }
    }

    /**
     * Get the total uncompressed size of the files to compress.
     *
     * @param outputFolder the output folder
     * @param parametersFile the parameters file
     * @param identificationFiles the identification files
     *
     * @return the total uncompressed size
     */
    private long getTotalUncompressedSize(File outputFolder, File parametersFile, HashMap<String, HashMap<Integer, File>> identificationFiles) {

        long totalUncompressedSize = 0;

        totalUncompressedSize += getInputFile(outputFolder).length(); // input file
        totalUncompressedSize += parametersFile.length(); // parameters file

        if (enableOmssa) {
            // OMSSA modification files
            File modificationsFile = new File(outputFolder, "omssa_mods.xml");
            totalUncompressedSize += modificationsFile.length();
            File userModificationsFile = new File(outputFolder, "omssa_usermods.xml");
            totalUncompressedSize += userModificationsFile.length();
        }

        if (enableMsAmanda) { // MS Amanda settings file
            for (File spectrumFile : mgfFiles) {
                String newName = Util.removeExtension(spectrumFile.getName()) + "_settings.xml";
                File settingsFile = new File(outputFolder, newName);
                if (settingsFile.exists()) {
                    totalUncompressedSize += settingsFile.length();
                }
            }
        }

        for (HashMap<Integer, File> fileMap : identificationFiles.values()) { // identification files
            for (File identificationFile : fileMap.values()) {
                totalUncompressedSize += identificationFile.length();
            }
        }

        // output data
        if (outputData) {
            totalUncompressedSize += getTotalUncompressedSizeOfData();
        }

        return totalUncompressedSize;
    }

    /**
     * Get the total uncompressed size of the files to compress when grouping
     * per algorithm.
     *
     * @param inputFile the input file
     * @param outputFolder the output folder
     * @param algorithm the algorithm of interest
     * @param parametersFile the parameters file
     * @param identificationFiles the identification files
     *
     * @return the total uncompressed size per algorithm
     */
    private long getTotalUncompressedSizeAlgorithm(File inputFile, File outputFolder, Integer algorithm, File parametersFile, ArrayList<File> identificationFiles) {

        long totalUncompressedSize = 0;

        totalUncompressedSize += inputFile.length();
        totalUncompressedSize += parametersFile.length();

        if (algorithm == Advocate.omssa.getIndex()) {
            // OMSSA modification files
            File modificationsFile = new File(outputFolder, "omssa_mods.xml");
            totalUncompressedSize += modificationsFile.length();
            File userModificationsFile = new File(outputFolder, "omssa_usermods.xml");
            totalUncompressedSize += userModificationsFile.length();
        }

        if (algorithm == Advocate.msAmanda.getIndex()) {
            for (File spectrumFile : mgfFiles) {
                String newName = Util.removeExtension(spectrumFile.getName()) + "_settings.xml";
                File settingsFile = new File(outputFolder, newName);
                if (settingsFile.exists()) {
                    totalUncompressedSize += settingsFile.length();
                }
            }
        }

        for (File identificationFile : identificationFiles) {
            totalUncompressedSize += identificationFile.length();
        }

        // output data
        if (outputData) {
            totalUncompressedSize += getTotalUncompressedSizeOfData();
        }

        return totalUncompressedSize;
    }

    /**
     * Get the total uncompressed size of the files to compress for the given
     * run.
     *
     * @param inputFile the input file
     * @param outputFolder the output folder
     * @param runName the name of the run of interest
     * @param run the mgf file of interest
     * @param parametersFile the parameters file
     * @param identificationFiles the identification files
     *
     * @return the total uncompressed size of the files for the given run
     */
    private long getTotalUncompressedSizeRun(File inputFile, File outputFolder, String runName, String run, File parametersFile, HashMap<String, HashMap<Integer, File>> identificationFiles) {

        long totalUncompressedSize = 0;

        totalUncompressedSize += inputFile.length();
        totalUncompressedSize += parametersFile.length();
        if (enableOmssa) {
            // OMSSA modification files
            File modificationsFile = new File(outputFolder, "omssa_mods.xml");
            totalUncompressedSize += modificationsFile.length();
            File userModificationsFile = new File(outputFolder, "omssa_usermods.xml");
            totalUncompressedSize += userModificationsFile.length();
        }
        if (enableMsAmanda) {
            String newName = runName + "_settings.xml";
            File settingsFile = new File(outputFolder, newName);
            if (settingsFile.exists()) {
                totalUncompressedSize += settingsFile.length();
            }
        }

        HashMap<Integer, File> fileMap = identificationFiles.get(run);
        for (File identificationFile : fileMap.values()) {
            totalUncompressedSize += identificationFile.length();
        }

        // output data
        if (outputData) {
            totalUncompressedSize += getTotalUncompressedSizeOfData();
        }

        return totalUncompressedSize;
    }

    /**
     * Get the total uncompressed size of the FASTA and spectrum files.
     *
     * @return the total uncompressed size of the FASTA and spectrum files
     */
    private long getTotalUncompressedSizeOfData() {

        long totalUncompressedSize = identificationParameters.getSearchParameters().getFastaFile().length();
        for (File spectrumFile : getMgfFiles()) {
            totalUncompressedSize += spectrumFile.length();
        }

        return totalUncompressedSize;
    }

    /**
     * Returns the configuration file.
     *
     * @return the configuration file
     */
    public ConfigurationFile getConfigurationFile() {
        File folder = new File(getJarFilePath() + File.separator + "resources" + File.separator + "conf" + File.separator); // @TODO: make this more generic?
        File file = new File(folder, SEARCHGUI_CONFIGURATION_FILE);
        return new ConfigurationFile(file);
    }

    /**
     * Returns the folder to use to store peak lists.
     *
     * @param jarFilePath the path to the jar file
     * @return the folder to use to store peak lists
     */
    public static File getPeakListFolder(String jarFilePath) {
        File peakListFolder = new File(getTempFolderPath(jarFilePath), PEAK_LIST_SUBFOLDER);
        if (!peakListFolder.exists()) {
            peakListFolder.mkdirs();
        }
        return peakListFolder;
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        return CompomicsWrapper.getJarFilePath(this.getClass().getResource("SearchHandler.class").getPath(), "SearchGUI");
    }

    /**
     * Returns the folder to use for temporary files. By default the resources
     * folder is used.
     *
     * @param jarFilePath the path to the jar file
     * @return the folder to use for temporary files
     */
    public static String getTempFolderPath(String jarFilePath) {
        if (tempFolderPath == null) {
            if (jarFilePath.equals(".")) {
                tempFolderPath = "resources" + File.separator + "temp";
            } else {
                tempFolderPath = jarFilePath + File.separator + "resources" + File.separator + "temp";
            }
            File tempFolder = new File(tempFolderPath);
            if (!tempFolder.exists()) {
                tempFolder.mkdirs();
            }
        }
        return tempFolderPath;
    }

    /**
     * Sets the folder to use for temporary files.
     *
     * @param aTempFolderPath the folder to use for temporary files
     */
    public static void setTempFolderPath(String aTempFolderPath) {
        tempFolderPath = aTempFolderPath;
    }
}
