package eu.isas.searchgui;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.software.CompomicsWrapper;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationParametersFactory;
import com.compomics.util.experiment.io.mass_spectrometry.MsFileExporter;
import com.compomics.util.experiment.io.mass_spectrometry.MsFileHandler;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFolder;
import com.compomics.util.experiment.mass_spectrometry.proteowizard.MsConvertParameters;
import com.compomics.util.experiment.mass_spectrometry.proteowizard.ProteoWizardMsFormat;
import com.compomics.util.experiment.mass_spectrometry.thermo_raw_file_parser.ThermoRawFileParserParameters;
import com.compomics.util.experiment.io.temp.TempFilesManager;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingDialog;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.io.IoUtil;
import com.compomics.util.io.compression.GzUtils;
import com.compomics.util.io.compression.ZipUtils;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.CometParameters;
import com.compomics.util.parameters.identification.tool_specific.MsAmandaParameters;
import com.compomics.util.parameters.identification.tool_specific.MyriMatchParameters;
import com.compomics.util.parameters.identification.tool_specific.OmssaParameters;
import com.compomics.util.parameters.identification.tool_specific.TideParameters;
import com.compomics.util.parameters.tools.ProcessingParameters;
import com.compomics.util.parameters.searchgui.OutputParameters;
import com.compomics.util.parameters.UtilitiesUserParameters;
import com.compomics.util.parameters.identification.tool_specific.MetaMorpheusParameters;
import com.compomics.util.waiting.Duration;
import eu.isas.searchgui.processbuilders.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;

/**
 * This class represents the search command line interface.
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
     * Enzymes file.
     */
    private static String enzymeFile = "resources/conf/searchGUI_enzymes.xml";
    /**
     * Default SearchGUI configurations.
     */
    public static final String SEARCHGUI_CONFIGURATION_FILE = "searchGUI_configuration.txt";
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
     * The worker running the searches.
     */
    private SearchWorker searchWorker;
    /**
     * The results folder.
     */
    private File resultsFolder;
    /**
     * If true, OMSSA will be used.
     */
    private boolean enableOmssa = false;
    /**
     * If true, X! Tandem will be used.
     */
    private boolean enableXtandem = false;
    /**
     * If true, MS-GF+ will be used.
     */
    private boolean enableMsgf = false;
    /**
     * If true, MS Amanda will be used.
     */
    private boolean enableMsAmanda = false;
    /**
     * If true, MyriMatch will be used.
     */
    private boolean enableMyriMatch = false;
    /**
     * If true, Comet will be used.
     */
    private boolean enableComet = false;
    /**
     * If true, Tide will be used.
     */
    private boolean enableTide = false;
    /**
     * If true, Andromeda will be used.
     */
    private boolean enableAndromeda = false;
    /**
     * If true, MetaMorpheus will be used.
     */
    private boolean enableMetaMorpheus = false;
    /**
     * If true, Novor will be used.
     */
    private boolean enableNovor = false;
    /**
     * If true, DirecTag will be used.
     */
    private boolean enableDirecTag = false;
    /**
     * If true, PeptideShaker will be used.
     */
    private boolean enablePeptideShaker = false;
    /**
     * If true, Reporter will be used.
     */
    private boolean enableReporter = false;
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
     * The mass spectrometry files in supported format.
     */
    private ArrayList<File> msFiles;
    /**
     * The cms files.
     */
    private ArrayList<File> cmsFiles;
    /**
     * The FASTA file.
     */
    private File fastaFile;
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
     * The MetaMorpheus location.
     */
    private File metaMorpheusLocation = null;
    /**
     * The Novor location.
     */
    private File novorLocation = null;
    /**
     * The DirecTag location.
     */
    private File direcTagLocation = null;
    /**
     * The makeblastdb location.
     */
    private File makeblastdbLocation;
    /**
     * The PeptideShaker experiment label.
     */
    private String experimentLabel;
    /**
     * The PeptideShaker psdb file.
     */
    private File peptideShakerFile = null;
    /**
     * The mascot dat files.
     */
    private ArrayList<File> mascotFiles = new ArrayList<>();
    /**
     * The msconvert process.
     */
    private ArrayList<MsConvertProcessBuilder> msConvertProcessBuilders = null;
    /**
     * The ThermoRawFileParser process.
     */
    private ArrayList<ThermoRawFileParserProcessBuilder> thermoRawFileParserProcessBuilders = null;
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
     * The MetaMorpheus process.
     */
    private MetaMorpheusProcessBuilder metaMorpheusProcessBuilder = null;
    /**
     * The Novor process.
     */
    private NovorProcessBuilder novorProcessBuilder = null;
    /**
     * The DirecTag process.
     */
    private DirecTagProcessBuilder direcTagProcessBuilder = null;
    /**
     * The PeptideShaker process.
     */
    private PeptideShakerProcessBuilder peptideShakerProcessBuilder = null;
    /**
     * The processing parameters.
     */
    private ProcessingParameters processingParameters = new ProcessingParameters();
    /**
     * The msconvert parameters.
     */
    private MsConvertParameters msConvertParameters;
    /**
     * The ThermoRawFileParser parameters.
     */
    private ThermoRawFileParserParameters thermoRawFileParserParameters;
    /**
     * The name for the SearchGUI output file.
     */
    private static String defaultOutputFileName = "searchgui_out";
    /**
     * Default file name ending for a SearchGUI output.
     */
    public final static String DEFAULT_OUTPUT_FILE_NAME_ENDING = ".zip";
    /**
     * The name of the folder where to save the spectrum and FASTA files.
     */
    public final static String DEFAULT_DATA_FOLDER = "data";
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
     * The folder where to save the logs.
     */
    private File logFolder = null;
    /**
     * The output time stamp.
     */
    private static String outputTimeStamp = null;
    /**
     * A map from the name of the ID files to the spectrum file used.
     */
    private HashMap<String, File> idFileToSpectrumFileMap;
    /**
     * The identification parameters factory.
     */
    private IdentificationParametersFactory identificationParametersFactory = IdentificationParametersFactory.getInstance();
    /**
     * The mass spectrometry file handler.
     */
    private final MsFileHandler msFileHandler;

    /**
     * Constructor for the SearchGUI command line interface.Uses the
     * configuration file searchGUI_configuration.txt to get the default search
     * engine locations and which search engines that are enabled. Mainly for
     * use via the graphical UI.
     *
     * @param identificationParameters the identification parameters
     * @param resultsFolder the results folder
     * @param msFiles list of mass spectrometry files
     * @param fastaFile the FASTA file
     * @param rawFiles list of raw files
     * @param identificationParametersFile the identification parameters file
     * @param processingParameters the processing parameters
     * @param msFileHandler The mass spectrometry file handler.
     * @param exceptionHandler a handler for exceptions
     */
    public SearchHandler(
            IdentificationParameters identificationParameters,
            File resultsFolder,
            ArrayList<File> msFiles,
            File fastaFile,
            ArrayList<File> rawFiles,
            File identificationParametersFile,
            ProcessingParameters processingParameters,
            MsFileHandler msFileHandler,
            ExceptionHandler exceptionHandler
    ) {

        this.resultsFolder = resultsFolder;
        this.msFiles = msFiles;
        this.cmsFiles = new ArrayList<File>();
        this.fastaFile = fastaFile;
        this.rawFiles = rawFiles;
        this.exceptionHandler = exceptionHandler;

        enableOmssa = loadSearchEngineLocation(
                Advocate.omssa,
                false,
                true,
                true,
                true,
                false,
                false,
                false
        );
        enableXtandem = loadSearchEngineLocation(
                Advocate.xtandem,
                false,
                true,
                true,
                true,
                true,
                false,
                true
        );
        enableMsgf = loadSearchEngineLocation(
                Advocate.msgf,
                true,
                true,
                true,
                true,
                false,
                false,
                false
        );
        enableMsAmanda = loadSearchEngineLocation(
                Advocate.msAmanda,
                true,
                true,
                true,
                true,
                false,
                false,
                false
        );
        enableMyriMatch = loadSearchEngineLocation(
                Advocate.myriMatch,
                false,
                true,
                false,
                true,
                true,
                false,
                true
        );
        enableComet = loadSearchEngineLocation(
                Advocate.comet,
                false,
                true,
                false,
                true,
                true,
                false,
                false
        );
        enableTide = loadSearchEngineLocation(
                Advocate.tide,
                false,
                true,
                true,
                true,
                true,
                false,
                true
        );
        enableAndromeda = loadSearchEngineLocation(
                Advocate.andromeda,
                false,
                true,
                false,
                false,
                false,
                false,
                false
        );
        enableMetaMorpheus = loadSearchEngineLocation(
                Advocate.metaMorpheus,
                true,
                true,
                true,
                true,
                false,
                false,
                false
        );
        enableNovor = loadSearchEngineLocation(
                Advocate.novor,
                true,
                true,
                true,
                true,
                false,
                false,
                false
        );
        enableDirecTag = loadSearchEngineLocation(
                Advocate.direcTag,
                false,
                true,
                false,
                true,
                true,
                false,
                true
        );
        this.identificationParametersFile = identificationParametersFile;
        this.processingParameters = processingParameters;
        this.identificationParameters = identificationParameters;
        this.msFileHandler = msFileHandler;

        searchDuration = new Duration();

    }

    /**
     * Constructor for the SearchGUI command line interface. If the search
     * engines folders are set to null the default search engine locations are
     * used.
     *
     * @param identificationParameters the identification parameters
     * @param resultsFolder the results folder
     * @param msFiles list of mass spectrometry files
     * @param defaultOutputFileName the default output file name
     * @param fastaFile the FASTA file
     * @param rawFiles list of raw files
     * @param identificationParametersFile the search parameters file
     * @param runOmssa if true, the OMSSA search is enabled
     * @param runXTandem if true, the XTandem search is enabled
     * @param runMsgf if true, the MS-GF+ search is enabled
     * @param runMsAmanda if true, the MS Amanda search is enabled
     * @param runMyriMatch if true, the MyriMatch search is enabled
     * @param runComet if true, the Comet search is enabled
     * @param runTide if true, the Tide search is enabled
     * @param runAndromeda if true, the Andromeda search is enabled
     * @param runMetaMorpheus if true, the MetaMorpheus search is enabled
     * @param runNovor if true, the Novor search is enabled
     * @param runDirecTag if true, the DirecTag search is enabled
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
     * @param metaMorpheusFolder the folder where MetaMorpheus is installed, if
     * null the default location is used
     * @param novorFolder the folder where Novor is installed, if null the
     * default location is used
     * @param direcTagFolder the folder where DirecTag is installed, if null the
     * default location is used
     * @param makeblastdbFolder the folder where makeblastdb is installed, if
     * null the default location is used
     * @param processingParameters the processing preferences
     */
    public SearchHandler(
            IdentificationParameters identificationParameters,
            File resultsFolder,
            String defaultOutputFileName,
            ArrayList<File> msFiles,
            File fastaFile,
            ArrayList<File> rawFiles,
            File identificationParametersFile,
            boolean runOmssa,
            boolean runXTandem,
            boolean runMsgf,
            boolean runMsAmanda,
            boolean runMyriMatch,
            boolean runComet,
            boolean runTide,
            boolean runAndromeda,
            boolean runMetaMorpheus,
            boolean runNovor,
            boolean runDirecTag,
            File omssaFolder,
            File xTandemFolder,
            File msgfFolder,
            File msAmandaFolder,
            File myriMatchFolder,
            File cometFolder,
            File tideFolder,
            File andromedaFolder,
            File metaMorpheusFolder,
            File novorFolder,
            File direcTagFolder,
            File makeblastdbFolder,
            ProcessingParameters processingParameters
    ) {

        this.resultsFolder = resultsFolder;
        if (defaultOutputFileName != null) {
            this.defaultOutputFileName = defaultOutputFileName;
        }
        this.msFiles = msFiles;
        this.cmsFiles = new ArrayList<File>();
        this.fastaFile = fastaFile;
        this.rawFiles = rawFiles;
        this.enableOmssa = runOmssa;
        this.enableXtandem = runXTandem;
        this.enableMsgf = runMsgf;
        this.enableMsAmanda = runMsAmanda;
        this.enableMyriMatch = runMyriMatch;
        this.enableComet = runComet;
        this.enableTide = runTide;
        this.enableAndromeda = runAndromeda;
        this.enableMetaMorpheus = runMetaMorpheus;
        this.enableNovor = runNovor;
        this.enableDirecTag = runDirecTag;

        this.identificationParameters = identificationParameters;
        this.processingParameters = processingParameters;
        this.identificationParametersFile = identificationParametersFile;

        if (omssaFolder != null) {
            this.omssaLocation = omssaFolder;
        } else {
            loadSearchEngineLocation(
                    Advocate.omssa,
                    false,
                    true,
                    true,
                    true,
                    false,
                    false,
                    false
            ); // try to use the default
        }

        if (xTandemFolder != null) {
            this.xtandemLocation = xTandemFolder;
        } else {
            loadSearchEngineLocation(
                    Advocate.xtandem,
                    false,
                    true,
                    true,
                    true,
                    true,
                    false,
                    true
            ); // try to use the default
        }

        if (msgfFolder != null) {
            this.msgfLocation = msgfFolder;
        } else {
            loadSearchEngineLocation(
                    Advocate.msgf,
                    true,
                    true,
                    true,
                    true,
                    false,
                    false,
                    false
            ); // try to use the default
        }

        if (msAmandaFolder != null) {
            this.msAmandaLocation = msAmandaFolder;
        } else {
            loadSearchEngineLocation(
                    Advocate.msAmanda,
                    true,
                    true,
                    true,
                    true,
                    false,
                    false,
                    false
            ); // try to use the default
        }

        if (myriMatchFolder != null) {
            this.myriMatchLocation = myriMatchFolder;
        } else {
            loadSearchEngineLocation(
                    Advocate.myriMatch,
                    false,
                    true,
                    false,
                    true,
                    true,
                    false,
                    true
            ); // try to use the default
        }

        if (cometFolder != null) {
            this.cometLocation = cometFolder;
        } else {
            loadSearchEngineLocation(
                    Advocate.comet,
                    false,
                    true,
                    false,
                    true,
                    true,
                    false,
                    false
            ); // try to use the default
        }

        if (tideFolder != null) {
            this.tideLocation = tideFolder;
        } else {
            loadSearchEngineLocation(
                    Advocate.tide,
                    false,
                    true,
                    true,
                    true,
                    true,
                    false,
                    true
            ); // try to use the default
        }

        if (andromedaFolder != null) {
            this.andromedaLocation = andromedaFolder;
        } else {
            loadSearchEngineLocation(
                    Advocate.andromeda,
                    false,
                    true,
                    false,
                    false,
                    false,
                    false,
                    false
            ); // try to use the default
        }

        if (metaMorpheusFolder != null) {
            this.metaMorpheusLocation = metaMorpheusFolder;
        } else {
            loadSearchEngineLocation(
                    Advocate.metaMorpheus,
                    true,
                    true,
                    true,
                    true,
                    false,
                    false,
                    false
            ); // try to use the default
        }

        if (novorFolder != null) {
            this.novorLocation = novorFolder;
        } else {
            loadSearchEngineLocation(
                    Advocate.novor,
                    true,
                    true,
                    true,
                    true,
                    false,
                    false,
                    false
            ); // try to use the default
        }

        if (direcTagFolder != null) {
            this.direcTagLocation = direcTagFolder;
        } else {
            loadSearchEngineLocation(
                    Advocate.direcTag,
                    false,
                    true,
                    false,
                    true,
                    true,
                    false,
                    true
            ); // try to use the default
        }

        if (makeblastdbFolder != null) {
            this.makeblastdbLocation = makeblastdbFolder;
        } else {
            loadSearchEngineLocation(
                    null,
                    false,
                    true,
                    true,
                    true,
                    false,
                    false,
                    true
            ); // try to use the default
        }

        // set this version as the default SearchGUI version
        if (!getJarFilePath().equalsIgnoreCase(".")) {

            UtilitiesUserParameters utilitiesUserParameters = UtilitiesUserParameters.loadUserParameters();
            String versionNumber = new eu.isas.searchgui.utilities.Properties().getVersion();
            utilitiesUserParameters.setSearchGuiPath(new File(getJarFilePath(), "SearchGUI-" + versionNumber + ".jar").getAbsolutePath());
            UtilitiesUserParameters.saveUserParameters(utilitiesUserParameters);

        }

        this.msFileHandler = new MsFileHandler();

        searchDuration = new Duration();
    }

    /**
     * Start the search.
     *
     * @param waitingHandler the waiting handler
     *
     * @throws InterruptedException thrown if the process is interrupted
     */
    public synchronized void startSearch(
            WaitingHandler waitingHandler
    ) throws InterruptedException {

        this.waitingHandler = waitingHandler;
        searchDuration.start();

        searchWorker = new SearchWorker(waitingHandler);
        searchWorker.execute();

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
            throw new IllegalArgumentException(enzymeFile + " not found.");
        }
        return result;
    }

    /**
     * Called when the search has been completed.
     */
    private void searchCompleted() {

        if (searchWorker.isFinished()) {

            if (waitingHandler != null) {
                if (waitingHandler instanceof WaitingDialog) {
                    // change the icon back to the default version
                    ((JFrame) ((WaitingDialog) waitingHandler).getParent())
                            .setIconImage(
                                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif"))
                            );
                }
                searchDuration.end();
                waitingHandler.appendReport(
                        "Search Completed (" + searchDuration.toString() + ").",
                        true,
                        true
                );
                waitingHandler.appendReportEndLine();
            }

            saveReport();

            if (waitingHandler != null && !waitingHandler.isRunCanceled()) {
                waitingHandler.setRunFinished();
            }

            // open project in PeptideShaker?
            if (enablePeptideShaker) {

                UtilitiesUserParameters utilitiesUserParameters = UtilitiesUserParameters.loadUserParameters();

                File tempPeptideShakerFile = peptideShakerFile;

                if (utilitiesUserParameters.outputData()) {
                    tempPeptideShakerFile = new File(peptideShakerFile.getParentFile(),
                            IoUtil.removeExtension(peptideShakerFile.getName()) + ".zip");
                }

                if (tempPeptideShakerFile.exists()) {
                    try {
                        CompomicsWrapper wrapper = new CompomicsWrapper();
                        ArrayList<String> javaHomeAndOptions
                                = wrapper.getJavaHomeAndOptions(utilitiesUserParameters.getPeptideShakerPath());

                        ArrayList process_name_array = new ArrayList();
                        process_name_array.add(javaHomeAndOptions.get(0)); // set java home

                        // set java options
                        for (int i = 1; i < javaHomeAndOptions.size(); i++) {
                            process_name_array.add(javaHomeAndOptions.get(i));
                        }

                        process_name_array.add("-jar");
                        process_name_array.add(new File(utilitiesUserParameters.getPeptideShakerPath()).getName());
                        process_name_array.add("-psdb");
                        process_name_array.add(CommandLineUtils.getCommandLineArgument(tempPeptideShakerFile));

                        ProcessBuilder openPeptideShakerProcess = new ProcessBuilder(process_name_array);

                        // print the command to the log file
                        System.out.println(System.getProperty("line.separator")
                                + System.getProperty("line.separator") + "PeptideShaker command: ");

                        for (Object currentElement : process_name_array) {
                            System.out.print(currentElement + " ");
                        }

                        System.out.println(System.getProperty("line.separator"));

                        File psFolder = new File(utilitiesUserParameters.getPeptideShakerPath()).getParentFile();
                        openPeptideShakerProcess.directory(psFolder);

                        // set error out and std out to same stream
                        openPeptideShakerProcess.redirectErrorStream(true);

                        openPeptideShakerProcess.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (waitingHandler != null) {

                    waitingHandler.appendReport(
                            "PeptideShaker file (" + tempPeptideShakerFile.getAbsolutePath() + ") not found!",
                            true,
                            true
                    );
                }
            }

            if (useCommandLine) {
                System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "Search Completed." + System.getProperty("line.separator"));
                System.exit(0);
            }
        }
    }

    /**
     * Save the SearchGUI report to the results folder.
     */
    private void saveReport() {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
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
            try {
                fw.write(report);
            } finally {
                fw.close();
            }
        } catch (IOException e) {
            if (waitingHandler != null) {
                waitingHandler.appendReport(
                        "Failed to write to the report file!",
                        true,
                        true
                );
            }
            e.printStackTrace();
        }

        try {
            if (logFolder != null) {
                FileWriter fw = new FileWriter(new File(logFolder, fileName));
                try {
                    fw.write(report);
                } finally {
                    fw.close();
                }
            }
        } catch (IOException e) {
            if (waitingHandler != null) {
                waitingHandler.appendReport("Failed to write to the log report file!", true, true);
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
    private boolean loadSearchEngineLocation(
            Advocate searchEngineAdvocate,
            boolean sameVersionForAll,
            boolean windowsSupported,
            boolean osxSupported,
            boolean linuxSupported,
            boolean windowsBitVersions,
            boolean osxBitVersions,
            boolean linuxBitVersions
    ) {

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
            File input = new File(folder, SEARCHGUI_CONFIGURATION_FILE);
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
                                    } else if (is64Bit) {
                                        searchEngineLoation = new File(basePath + File.separator + "windows" + File.separator + "windows_64bit");
                                    } else {
                                        searchEngineLoation = new File(basePath + File.separator + "windows" + File.separator + "windows_32bit");
                                    }
                                } else if (operatingSystem.contains("mac os") && osxSupported) {
                                    if (!osxBitVersions) {
                                        searchEngineLoation = new File(basePath + File.separator + "osx");
                                    } else if (is64Bit) {
                                        searchEngineLoation = new File(basePath + File.separator + "osx" + File.separator + "osx_64bit");
                                    } else {
                                        searchEngineLoation = new File(basePath + File.separator + "osx" + File.separator + "osx_32bit");
                                    }
                                } else if ((operatingSystem.contains("nix") || operatingSystem.contains("nux")) && linuxSupported) {
                                    if (!linuxBitVersions) {
                                        searchEngineLoation = new File(basePath + File.separator + "linux");
                                    } else if (is64Bit) {
                                        searchEngineLoation = new File(basePath + File.separator + "linux" + File.separator + "linux_64bit");
                                    } else {
                                        searchEngineLoation = new File(basePath + File.separator + "linux" + File.separator + "linux_32bit");
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
            } else if (searchEngineAdvocate == Advocate.metaMorpheus) {
                metaMorpheusLocation = searchEngineLoation;
            } else if (searchEngineAdvocate == Advocate.novor) {
                novorLocation = searchEngineLoation;
            } else if (searchEngineAdvocate == Advocate.direcTag) {
                direcTagLocation = searchEngineLoation;
            }
        } else {
            makeblastdbLocation = searchEngineLoation;
        }

        return enableSearchEngine;
    }

    /**
     * Returns the name of the X!Tandem result file if renamed.
     *
     * @param spectrumFileName the name of the spectrum file searched
     *
     * @return the name of the X!Tandem result file
     */
    public static String getXTandemFileName(String spectrumFileName) {
        return IoUtil.removeExtension(spectrumFileName) + ".t.xml";
    }

    /**
     * Returns the name of the Comet result file.
     *
     * @param spectrumFileName the name of the spectrum file searched
     *
     * @return the name of the Comet result file
     */
    public String getCometFileName(String spectrumFileName) {
        CometParameters cometParameters = (CometParameters) identificationParameters.getSearchParameters().getIdentificationAlgorithmParameter(Advocate.comet.getIndex());
        return getCometFileName(spectrumFileName, cometParameters);
    }

    /**
     * Returns the name of the Comet result file.
     *
     * @param spectrumFileName the spectrum file name
     * @param cometParameters the Comet parameters
     *
     * @return the name of the Comet result file
     */
    public static String getCometFileName(String spectrumFileName, CometParameters cometParameters) {

        if (cometParameters.getSelectedOutputFormat() != null) {
            switch (cometParameters.getSelectedOutputFormat()) {
                case PepXML:
                    return IoUtil.removeExtension(spectrumFileName) + ".comet.pep.xml";
                case Percolator:
                    return IoUtil.removeExtension(spectrumFileName) + ".comet.pin";
                case SQT:
                    return IoUtil.removeExtension(spectrumFileName) + ".comet.sqt";
                case TXT:
                    return IoUtil.removeExtension(spectrumFileName) + ".comet.txt";
                default:
                    break;
            }
        }

        return IoUtil.removeExtension(spectrumFileName) + ".comet.pep.xml";
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
     * @param spectrumFileName the spectrum file name
     * @param tideParameters the Tide parameters
     *
     * @return the name of the Tide result file
     */
    public static String getTideFileName(String spectrumFileName, TideParameters tideParameters) {
        if (tideParameters.getTextOutput()) {
            return IoUtil.removeExtension(spectrumFileName) + ".tide-search.target.txt";
        } else if (tideParameters.getMzidOutput()) {
            return IoUtil.removeExtension(spectrumFileName) + ".tide-search.target.mzid";
        } else if (tideParameters.getPepXmlOutput()) {
            return IoUtil.removeExtension(spectrumFileName) + ".tide-search.target.pep.xml";
        } else if (tideParameters.getSqtOutput()) {
            return IoUtil.removeExtension(spectrumFileName) + ".tide-search.target.sqt";
        } else {
            return IoUtil.removeExtension(spectrumFileName) + ".tide-search.target.pin";
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
        return IoUtil.removeExtension(spectrumFileName) + ".res";
    }

    /**
     * Returns the name of the MetaMorpheus result file.
     *
     * @param spectrumFileName the name of the spectrum file searched
     *
     * @return the name of the MetaMorpheus result file
     */
    public static String getMetaMorpheusFileName(String spectrumFileName) {
        return IoUtil.removeExtension(spectrumFileName) + ".mzID";
    }

    /**
     * Returns the name of the Novor result file.
     *
     * @param spectrumFileName the name of the spectrum file searched
     *
     * @return the name of the Novor result file
     */
    public static String getNovorFileName(String spectrumFileName) {
        return IoUtil.removeExtension(spectrumFileName) + ".novor.csv";
    }

    /**
     * Returns the name of the DirecTag result file.
     *
     * @param spectrumFileName the name of the spectrum file searched
     *
     * @return the name of the DirecTag result file
     */
    public static String getDirecTagFileName(String spectrumFileName) {
        return IoUtil.removeExtension(spectrumFileName) + ".tags";
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
     * @param spectrumFileName the spectrum file name
     * @param omssaParameters the OMSSA parameters
     *
     * @return the name of the OMSSA result file
     */
    public static String getOMSSAFileName(String spectrumFileName, OmssaParameters omssaParameters) {
        return IoUtil.removeExtension(spectrumFileName) + "." + omssaParameters.getSelectedOutput().toLowerCase();
    }

    /**
     * Returns the name of the MS-GF+ result file.
     *
     * @param spectrumFileName the spectrum file name
     *
     * @return the name of the MS-GF+ result file
     */
    public static String getMsgfFileName(String spectrumFileName) {
        return IoUtil.removeExtension(spectrumFileName) + ".msgf.mzid";
    }

    /**
     * Returns the name of the MS Amanda result file.
     *
     * @param spectrumFileName the spectrum file name
     *
     * @return the name of the MS Amanda result file
     */
    public String getMsAmandaFileName(String spectrumFileName) {
        MsAmandaParameters msAmandaParameters = (MsAmandaParameters) identificationParameters.getSearchParameters().getIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex());
        return getMsAmandaFileName(spectrumFileName, msAmandaParameters);
    }

    /**
     * Returns the name of the MS Amanda result file.
     *
     * @param spectrumFileName the spectrum file name
     * @param msAmandaParameters the MS Amanda parameters
     *
     * @return the name of the MS Amanda result file
     */
    public static String getMsAmandaFileName(String spectrumFileName, MsAmandaParameters msAmandaParameters) {
        if (msAmandaParameters.getOutputFormat().equalsIgnoreCase("mzIdentML")) {
            return IoUtil.removeExtension(spectrumFileName) + ".ms-amanda.mzid.gz";
        } else {
            return IoUtil.removeExtension(spectrumFileName) + ".ms-amanda.csv";
        }
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
     * @param spectrumFileName the spectrum file name
     * @param myriMatchParameters the MyriMatch parameters
     *
     * @return the name of the MyriMatch result file
     */
    public static String getMyriMatchFileName(String spectrumFileName, MyriMatchParameters myriMatchParameters) {
        if (myriMatchParameters.getOutputFormat().equalsIgnoreCase("mzIdentML")) {
            return IoUtil.removeExtension(spectrumFileName) + ".myrimatch.mzid";
        } else {
            return IoUtil.removeExtension(spectrumFileName) + ".myrimatch.pepXML";
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
        ArrayList<File> result = new ArrayList<>();
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
     * Returns the MetaMorpheus location.
     *
     * @return the MetaMorpheus location
     */
    public File getMetaMorpheusLocation() {
        return metaMorpheusLocation;
    }

    /**
     * Set the MetaMorpheus location.
     *
     * @param metaMorpheusLocation the MetaMorpheus location to set
     */
    public void setMetaMorpheusLocation(File metaMorpheusLocation) {
        this.metaMorpheusLocation = metaMorpheusLocation;
    }

    /**
     * Returns the Novor location.
     *
     * @return the Novor location
     */
    public File getNovorLocation() {
        return novorLocation;
    }

    /**
     * Set the Novor location.
     *
     * @param novorLocation the Novor location to set
     */
    public void setNovorLocation(File novorLocation) {
        this.novorLocation = novorLocation;
    }

    /**
     * Returns the DirecTag location.
     *
     * @return the DirecTag location
     */
    public File getDirecTagLocation() {
        return direcTagLocation;
    }

    /**
     * Set the DirecTag location.
     *
     * @param direcTagLocation the DirecTag location to set
     */
    public void setDirecTagLocation(File direcTagLocation) {
        this.direcTagLocation = direcTagLocation;
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
     * Returns true if MetaMorpheus is to be used.
     *
     * @return if MetaMorpheus is to be used
     */
    public boolean isMetaMorpheusEnabled() {
        return enableMetaMorpheus;
    }

    /**
     * Returns true if Novor is to be used.
     *
     * @return if Novor is to be used
     */
    public boolean isNovorEnabled() {
        return enableNovor;
    }

    /**
     * Returns true if DirecTag is to be used.
     *
     * @return if DirecTag is to be used
     */
    public boolean isDirecTagEnabled() {
        return enableDirecTag;
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
     * Set if MetaMorpheus is to be used.
     *
     * @param runMetaMorpheus run MetaMorpheus?
     */
    public void setMetaMorpheusEnabled(boolean runMetaMorpheus) {
        this.enableMetaMorpheus = runMetaMorpheus;
    }

    /**
     * Set if Novor is to be used.
     *
     * @param runNovor run Novor?
     */
    public void setNovorEnabled(boolean runNovor) {
        this.enableNovor = runNovor;
    }

    /**
     * Set if DirecTag is to be used.
     *
     * @param runDirecTag run DirecTag?
     */
    public void setDirecTagEnabled(boolean runDirecTag) {
        this.enableDirecTag = runDirecTag;
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
     * Returns the list of mass spectrometry files.
     *
     * @return the mass spectrometry files
     */
    public ArrayList<File> getSpectrumFiles() {
        return msFiles;
    }

    /**
     * Sets the list of mass spectrometry files.
     *
     * @param msFiles the mass spectrometry files
     */
    public void setSpectrumFiles(ArrayList<File> msFiles) {
        this.msFiles = new ArrayList<>(msFiles);
    }

    /**
     * Returns the spectrum file with the given name. Null if not found.
     *
     * @param fileName the file name of the spectrum file
     * @return the spectrum file with the given name
     */
    public File getSpectrumFile(String fileName) {

        for (File tempFile : msFiles) {
            if (tempFile.getName().equalsIgnoreCase(fileName)) {
                return tempFile;
            }
        }

        return null;
    }

    /**
     * Returns the list of cms files.
     *
     * @return the cms files
     */
    public ArrayList<File> getCmsFiles() {
        return cmsFiles;
    }

    /**
     * Sets the list of cms files.
     *
     * @param cmsFiles the cms files
     */
    public void setCmsFiles(ArrayList<File> cmsFiles) {
        this.cmsFiles = cmsFiles;
    }

    /**
     * Returns the cms file with the given name. Null if not found.
     *
     * @param fileName the file name of the cms file
     * @return the spectrum file with the given name
     */
    public File getCmsFile(String fileName) {

        for (File tempFile : cmsFiles) {
            if (tempFile.getName().equalsIgnoreCase(fileName)) {
                return tempFile;
            }
        }

        return null;
    }

    /**
     * Returns the FASTA file.
     *
     * @return the FASTA file
     */
    public File getFastaFile() {
        return fastaFile;
    }

    /**
     * Set the FASTA file.
     *
     * @param fastaFile the FASTA file
     */
    public void setFastaFile(File fastaFile) {
        this.fastaFile = fastaFile;
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
                ((JFrame) ((WaitingDialog) waitingHandler).getParent())
                        .setIconImage(
                                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif"))
                        );
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
            if (thermoRawFileParserProcessBuilders != null) {
                for (ThermoRawFileParserProcessBuilder thermoRawFileParserProcessBuilder : thermoRawFileParserProcessBuilders) {
                    thermoRawFileParserProcessBuilder.endProcess();
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
            if (metaMorpheusProcessBuilder != null) {
                metaMorpheusProcessBuilder.endProcess();
            }
            if (novorProcessBuilder != null) {
                novorProcessBuilder.endProcess();
            }
            if (direcTagProcessBuilder != null) {
                direcTagProcessBuilder.endProcess();
            }
            if (peptideShakerProcessBuilder != null) {
                peptideShakerProcessBuilder.endProcess();
            }
        }

        @Override
        protected Object doInBackground() {

            try {
                UtilitiesUserParameters utilitiesUserParameters = UtilitiesUserParameters.loadUserParameters();

                File outputFolder = getResultsFolder();
                File outputTempFolder;

                if (utilitiesUserParameters.getSearchGuiOutputParameters() == OutputParameters.no_zip) {
                    outputTempFolder = outputFolder;
                } else {
                    try {
                        outputTempFolder = new File(outputFolder, OUTPUT_TEMP_FOLDER_NAME);
                        if (outputTempFolder.exists()) {
                            IoUtil.deleteDir(outputTempFolder);
                        }
                        outputTempFolder.mkdirs();
                        TempFilesManager.registerTempFolder(outputTempFolder);
                    } catch (Exception e) {
                        e.printStackTrace();
                        outputTempFolder = outputFolder;
                    }
                }

                int nRawFiles = getRawFiles().size();
                int nFilesToSearch = nRawFiles + getSpectrumFiles().size();
                int nProgress = 3 + nRawFiles;
                if (enableOmssa) {
                    nProgress += nFilesToSearch;
                    nProgress++; // the omssa indexing
                }
                if (enableXtandem) {
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
                    nProgress++; // the adromeda indexing
                }
                if (enableMetaMorpheus) {
                    nProgress += nFilesToSearch;
                }
                if (enableNovor) {
                    nProgress += nFilesToSearch;
                }
                if (enableDirecTag) {
                    nProgress += nFilesToSearch;
                }
                if (enablePeptideShaker) {
                    nProgress++;
                }
                if (enableReporter) {
                    nProgress++;
                }

                waitingHandler.setPrimaryProgressCounterIndeterminate(false);
                waitingHandler.setMaxPrimaryProgressCounter(nProgress);
                waitingHandler.increasePrimaryProgressCounter(); // just to not be stuck at 0% for the whole first search

                SearchParameters searchParameters = identificationParameters.getSearchParameters();

                if (enableOmssa) {
                    // call Makeblastdb class, check if run before and then start process
                    makeblastdbProcessBuilder = new MakeblastdbProcessBuilder(getJarFilePath(), fastaFile, makeblastdbLocation, waitingHandler, exceptionHandler);

                    if (makeblastdbProcessBuilder.needsFormatting()) {

                        if (waitingHandler != null) {
                            if (!useCommandLine) {
                                waitingHandler.setWaitingText(
                                        "Formatting " + makeblastdbProcessBuilder.getCurrentlyProcessedFileName() + " for OMSSA."
                                );
                            }
                            waitingHandler.appendReport(
                                    "Formatting " + makeblastdbProcessBuilder.getCurrentlyProcessedFileName() + " for OMSSA.",
                                    true,
                                    true
                            );
                            waitingHandler.appendReportEndLine();
                        }
                        makeblastdbProcessBuilder.startProcess();

                        if (waitingHandler != null) {
                            waitingHandler.appendReport(
                                    makeblastdbProcessBuilder.getCurrentlyProcessedFileName() + " formatted for OMSSA.",
                                    true,
                                    true
                            );
                            waitingHandler.appendReportEndLine();
                        }
                    }

                    // Write modification files to the OMSSA directory and save PTM indexes in the search parameters
                    File modsXmlFile = new File(omssaLocation, "mods.xml");
                    if (!modsXmlFile.exists()) {
                        throw new IllegalArgumentException("OMSSA mods.xml file not found.");
                    }
                    File userModsXmlFile = new File(omssaLocation, "usermods.xml");
                    OmssaclProcessBuilder.writeOmssaUserModificationsFile(
                            userModsXmlFile,
                            identificationParameters,
                            identificationParametersFile
                    );

                    // Copy the files to the results folder
                    File destinationFile = new File(outputTempFolder, "omssa_mods.xml");
                    IoUtil.copyFile(modsXmlFile, destinationFile);
                    destinationFile = new File(outputTempFolder, "omssa_usermods.xml");
                    IoUtil.copyFile(userModsXmlFile, destinationFile);

                    waitingHandler.increasePrimaryProgressCounter();
                }

                if (enableAndromeda) {

                    if (!useCommandLine) {

                        waitingHandler.setWaitingText("Andromeda configuration.");

                    }

                    waitingHandler.appendReport(
                            "Andromeda configuration.",
                            true,
                            true
                    );
                    waitingHandler.appendReportEndLine();

                    // create generic database
                    AndromedaProcessBuilder.createGenericFastaFile(
                            andromedaLocation,
                            fastaFile,
                            waitingHandler
                    );

                    // write Andromeda database configuration file
                    AndromedaProcessBuilder.createDatabaseFile(
                            andromedaLocation,
                            fastaFile
                    );

                    // write Andromeda enzyme configuration file
                    AndromedaProcessBuilder.createEnzymesFile(
                            andromedaLocation
                    );

                    // write Andromeda PTM configuration file and save PTM indexes in the search parameters
                    AndromedaProcessBuilder.createPtmFile(
                            andromedaLocation,
                            identificationParameters,
                            identificationParametersFile
                    );

                    waitingHandler.increasePrimaryProgressCounter();
                }

                if (enableTide && !waitingHandler.isRunCanceled()) {

                    // create the tide index
                    tideIndexProcessBuilder = new TideIndexProcessBuilder(
                            tideLocation,
                            fastaFile,
                            searchParameters,
                            waitingHandler,
                            exceptionHandler
                    );
                    waitingHandler.appendReport(
                            "Indexing " + fastaFile.getName() + " for Tide.",
                            true,
                            true
                    );
                    waitingHandler.appendReportEndLine();
                    tideIndexProcessBuilder.startProcess();

                    waitingHandler.increasePrimaryProgressCounter();
                }

                // convert raw files
                ExecutorService pool = Executors.newFixedThreadPool(processingParameters.getnThreads());

                ArrayList<File> rawFiles = getRawFiles();

                if (!rawFiles.isEmpty() && !waitingHandler.isRunCanceled()) {

                    waitingHandler.resetSecondaryProgressCounter();
                    waitingHandler.setMaxSecondaryProgressCounter(rawFiles.size() * 100);

                    Duration conversionDuration = new Duration();
                    conversionDuration.start();
                    waitingHandler.appendReport(
                            "Converting raw files.",
                            true,
                            true
                    );

                    boolean useThermoRawFileParser = true;

                    for (File tempRawFile : rawFiles) {
                        if (!tempRawFile.getName().toLowerCase().endsWith(ProteoWizardMsFormat.raw.fileNameEnding)) { // @TODO: could allow the user to still use msconvert for thermo raw files?
                            useThermoRawFileParser = false;
                        }
                    }

                    if (useThermoRawFileParser) {

                        thermoRawFileParserProcessBuilders = new ArrayList<>();
                        File thermoRawFileParserFolder = new File(getJarFilePath() + File.separator + "resources" + File.separator + "ThermoRawFileParser");

                        ThermoRawFileParserParameters thermoRawFileParserParameters = getThermoRawFileParserParameters();

                        for (int i = 0; i < rawFiles.size() && !waitingHandler.isRunCanceled(); i++) {

                            File rawFile = rawFiles.get(i);
                            String rawFileName = rawFile.getName();
                            File folder = rawFile.getParentFile();
                            String msFileName = IoUtil.removeExtension(rawFileName) + thermoRawFileParserParameters.getOutputFormat().fileNameEnding;
                            File msFile = new File(folder, msFileName);

                            if (!msFile.exists()) {

                                ThermoRawFileParserProcessBuilder thermoRawFileParserProcessBuilder = new ThermoRawFileParserProcessBuilder(
                                        thermoRawFileParserFolder,
                                        rawFile,
                                        folder,
                                        thermoRawFileParserParameters,
                                        waitingHandler,
                                        exceptionHandler
                                );

                                thermoRawFileParserProcessBuilders.add(thermoRawFileParserProcessBuilder);
                                pool.submit(thermoRawFileParserProcessBuilder);
                                // @TODO: validate the output file!

                            } else {

                                waitingHandler.appendReport(msFileName + " already exists. Conversion canceled.",
                                        true,
                                        true
                                );
                                waitingHandler.appendReportEndLine();

                            }

                            msFiles.add(msFile);

                        }

                    } else {

                        msConvertProcessBuilders = new ArrayList<>();

                        for (int i = 0; i < rawFiles.size() && !waitingHandler.isRunCanceled(); i++) {

                            MsConvertParameters msConvertParameters = getMsConvertParameters();
                            File rawFile = rawFiles.get(i);
                            String rawFileName = rawFile.getName();
                            File folder = rawFile.getParentFile();
                            String msFileName = IoUtil.removeExtension(rawFileName) + msConvertParameters.getMsFormat().fileNameEnding;
                            File msFile = new File(folder, msFileName);

                            if (!msFile.exists()) {

                                MsConvertProcessBuilder msConvertProcessBuilder = new MsConvertProcessBuilder(
                                        rawFile,
                                        folder,
                                        msConvertParameters,
                                        waitingHandler,
                                        exceptionHandler
                                );
                                msConvertProcessBuilders.add(msConvertProcessBuilder);
                                pool.submit(msConvertProcessBuilder);
                                // @TODO: validate the output file!

                            } else {

                                waitingHandler.appendReport(msFileName + " already exists. Conversion canceled.",
                                        true,
                                        true
                                );

                                waitingHandler.appendReportEndLine();

                            }

                            msFiles.add(msFile);

                        }
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
                            waitingHandler.appendReport(
                                    "Raw files conversion completed (" + conversionDuration.toString() + ").",
                                    true,
                                    true
                            );

                        }
                    }

                    waitingHandler.setSecondaryProgressCounterIndeterminate(true);

                }

                if (!waitingHandler.isRunCanceled()) {

                    // indexing the spectrum files
                    waitingHandler.appendReportEndLine();
                    waitingHandler.appendReport(
                            "Importing spectrum files.",
                            true,
                            true
                    );

                    Duration spectrumFilesImportDuration = new Duration();
                    spectrumFilesImportDuration.start();

                    for (File spectrumFile : msFiles) {

                        File folder = CmsFolder.getParentFolder() == null
                                ? spectrumFile.getParentFile() : new File(CmsFolder.getParentFolder());

                        msFileHandler.register(
                                spectrumFile,
                                folder,
                                waitingHandler
                        );

                        File cmsFile = new File(MsFileHandler.getCmsFilePath(spectrumFile, folder));

                        // add the cms file
                        cmsFiles.add(cmsFile);
                    }

                    spectrumFilesImportDuration.end();
                    waitingHandler.appendReport(
                            "Importing spectrum files completed (" + spectrumFilesImportDuration.toString() + ").",
                            true,
                            true
                    );

                }

                if (!waitingHandler.isRunCanceled()) {

                    saveInputFile(outputTempFolder);
                    waitingHandler.increasePrimaryProgressCounter();

                }

                // keep track of the identification files created in a map: spectrum file name -> software index -> identification file
                HashMap<String, HashMap<Integer, File>> identificationFiles = new HashMap<>(msFiles.size());

                // keep track of the spectrum files used to generate the id files
                idFileToSpectrumFileMap = new HashMap<>();

                for (int i = 0; i < getSpectrumFiles().size() && !waitingHandler.isRunCanceled(); i++) {

                    File spectrumFile = getSpectrumFiles().get(i);
                    String spectrumFileName = spectrumFile.getName();

                    if (useCommandLine) {

                        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator")
                                + "Processing: " + spectrumFileName + " (" + (i + 1) + "/" + getSpectrumFiles().size() + ")"
                        );

                    } else {

                        waitingHandler.setWaitingText("Processing: " + spectrumFileName + " (" + (i + 1) + "/" + getSpectrumFiles().size() + ")");

                    }

                    // Write mgf file
                    File mgfFile = null;
                    if (enableXtandem || enableMyriMatch || enableMsAmanda
                            || enableOmssa || enableNovor || enableDirecTag) {

                        // Make mgf file
                        waitingHandler.appendReport(
                                "Converting spectrum file " + spectrumFileName + " to peak list.",
                                true,
                                true
                        );

                        mgfFile = new File(getPeakListFolder(getJarFilePath()),
                                IoUtil.removeExtension(spectrumFileName) + ".mgf");

                        MsFileExporter.writeMgfFile(
                                msFileHandler,
                                spectrumFileName,
                                mgfFile,
                                waitingHandler
                        );
                    }

                    waitingHandler.appendReportEndLine();

                    // Run X!Tandem
                    if (enableXtandem && !waitingHandler.isRunCanceled()) {

                        File xTandemOutputFile = new File(outputTempFolder, getXTandemFileName(spectrumFileName));

                        xTandemProcessBuilder = new TandemProcessBuilder(
                                xtandemLocation,
                                mgfFile,
                                fastaFile,
                                xTandemOutputFile.getAbsolutePath(),
                                searchParameters,
                                waitingHandler,
                                exceptionHandler,
                                processingParameters.getnThreads()
                        );

                        waitingHandler.appendReport(
                                "Processing " + mgfFile.getName() + " with " + Advocate.xtandem.getName() + ".",
                                true,
                                true
                        );
                        waitingHandler.appendReportEndLine();
                        xTandemProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            if (utilitiesUserParameters.renameXTandemFile()) {

                                ArrayList<File> result = getXTandemFiles(outputTempFolder, spectrumFileName);

                                if (result.size() == 1) {

                                    File xTandemFile = result.get(0);
                                    File destinationFile = new File(outputTempFolder, getXTandemFileName(spectrumFileName));

                                    try {

                                        xTandemFile.renameTo(destinationFile);

                                    } catch (Exception e) {

                                        e.printStackTrace();
                                        waitingHandler.appendReport(
                                                "Could not rename " + Advocate.xtandem.getName() + " result for " + spectrumFileName + ".",
                                                true,
                                                true
                                        );
                                    }
                                } else {

                                    waitingHandler.appendReport(
                                            "Could not rename " + Advocate.xtandem.getName() + " result for " + spectrumFileName + ".",
                                            true,
                                            true
                                    );

                                }
                            }

                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);

                            if (runIdentificationFiles == null) {

                                runIdentificationFiles = new HashMap<>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);

                            }

                            if (xTandemOutputFile.exists()) {

                                runIdentificationFiles.put(Advocate.xtandem.getIndex(), xTandemOutputFile);
                                idFileToSpectrumFileMap.put(xTandemOutputFile.getName(), spectrumFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.xtandem.getName() + " result file for " + spectrumFileName + ".",
                                        true,
                                        true
                                );

                            }

                            waitingHandler.increasePrimaryProgressCounter();

                        }
                    }

                    // Run Myrimatch
                    if (enableMyriMatch && !waitingHandler.isRunCanceled()) {

                        File myriMatchOutputFile = new File(outputTempFolder, getMyriMatchFileName(spectrumFileName));

                        myriMatchProcessBuilder = new MyriMatchProcessBuilder(
                                myriMatchLocation,
                                mgfFile,
                                fastaFile,
                                outputTempFolder,
                                searchParameters,
                                waitingHandler,
                                exceptionHandler,
                                processingParameters.getnThreads()
                        );

                        waitingHandler.appendReport(
                                "Processing " + mgfFile.getName() + " with " + Advocate.myriMatch.getName() + ".",
                                true,
                                true
                        );
                        waitingHandler.appendReportEndLine();
                        myriMatchProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);

                            if (runIdentificationFiles == null) {

                                runIdentificationFiles = new HashMap<>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);

                            }

                            if (myriMatchOutputFile.exists()) {

                                runIdentificationFiles.put(Advocate.myriMatch.getIndex(), myriMatchOutputFile);
                                idFileToSpectrumFileMap.put(myriMatchOutputFile.getName(), spectrumFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.myriMatch.getName() + " result file for " + spectrumFileName + ".",
                                        true,
                                        true
                                );

                            }

                            waitingHandler.increasePrimaryProgressCounter();

                        }
                    }

                    // Run MS Amanda
                    if (enableMsAmanda && !waitingHandler.isRunCanceled()) {

                        File msAmandaOutputFile = new File(outputTempFolder, getMsAmandaFileName(spectrumFileName));
                        String filePath = msAmandaOutputFile.getAbsolutePath();

                        msAmandaProcessBuilder = new MsAmandaProcessBuilder(
                                msAmandaLocation,
                                mgfFile,
                                fastaFile,
                                filePath,
                                searchParameters,
                                waitingHandler,
                                exceptionHandler,
                                processingParameters.getnThreads()
                        );

                        waitingHandler.appendReport(
                                "Processing " + mgfFile.getName() + " with " + Advocate.msAmanda.getName() + ".",
                                true,
                                true
                        );
                        waitingHandler.appendReportEndLine();
                        msAmandaProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);

                            if (runIdentificationFiles == null) {

                                runIdentificationFiles = new HashMap<>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);

                            }

                            if (msAmandaOutputFile.exists()) {

                                runIdentificationFiles.put(Advocate.msAmanda.getIndex(), msAmandaOutputFile);
                                idFileToSpectrumFileMap.put(msAmandaOutputFile.getName(), spectrumFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.msAmanda.getName() + " result file for " + spectrumFileName + ".",
                                        true,
                                        true
                                );
                            }
                            waitingHandler.increasePrimaryProgressCounter();
                        }
                    }

                    // Run ms-gf+
                    if (enableMsgf && !waitingHandler.isRunCanceled()) {

                        File msgfOutputFile = new File(outputTempFolder, getMsgfFileName(spectrumFileName));

                        msgfProcessBuilder = new MsgfProcessBuilder(
                                msgfLocation,
                                spectrumFile,
                                fastaFile,
                                msgfOutputFile,
                                searchParameters,
                                waitingHandler,
                                exceptionHandler,
                                processingParameters.getnThreads(),
                                useCommandLine
                        );

                        waitingHandler.appendReport(
                                "Processing " + spectrumFile.getName() + " with " + Advocate.msgf.getName() + ".",
                                true,
                                true
                        );
                        waitingHandler.appendReportEndLine();
                        msgfProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);

                            if (runIdentificationFiles == null) {

                                runIdentificationFiles = new HashMap<>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);

                            }

                            if (msgfOutputFile.exists()) {

                                runIdentificationFiles.put(Advocate.msgf.getIndex(), msgfOutputFile);
                                idFileToSpectrumFileMap.put(msgfOutputFile.getName(), spectrumFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.msgf.getName() + " result file for " + spectrumFileName + ".",
                                        true,
                                        true
                                );
                            }

                            waitingHandler.increasePrimaryProgressCounter();

                        }
                    }

                    // Run OMSSA
                    if (enableOmssa && !waitingHandler.isRunCanceled()) {

                        File omssaOutputFile = new File(outputTempFolder, getOMSSAFileName(spectrumFileName));

                        omssaProcessBuilder = new OmssaclProcessBuilder(
                                omssaLocation,
                                mgfFile,
                                fastaFile,
                                omssaOutputFile,
                                searchParameters,
                                waitingHandler,
                                exceptionHandler,
                                utilitiesUserParameters.getRefMass(),
                                processingParameters.getnThreads()
                        );

                        waitingHandler.appendReport(
                                "Processing " + mgfFile.getName() + " with " + Advocate.omssa.getName() + ".",
                                true,
                                true
                        );
                        waitingHandler.appendReportEndLine();
                        omssaProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);

                            if (runIdentificationFiles == null) {

                                runIdentificationFiles = new HashMap<>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);

                            }

                            if (omssaOutputFile.exists()) {

                                runIdentificationFiles.put(Advocate.omssa.getIndex(), omssaOutputFile);
                                idFileToSpectrumFileMap.put(omssaOutputFile.getName(), spectrumFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.omssa.getName() + " result file for " + spectrumFileName + ".",
                                        true,
                                        true
                                );
                            }

                            waitingHandler.increasePrimaryProgressCounter();

                        }
                    }

                    // Run Comet
                    if (enableComet && !waitingHandler.isRunCanceled()) {

                        File cometOutputFile = new File(outputTempFolder, getCometFileName(spectrumFileName));
                        // Comet does not overwrite files but crashes
                        if (cometOutputFile.exists()) {
                            cometOutputFile.delete();
                        }

                        cometProcessBuilder = new CometProcessBuilder(
                                cometLocation,
                                searchParameters,
                                spectrumFile,
                                fastaFile,
                                waitingHandler,
                                exceptionHandler,
                                processingParameters.getnThreads(),
                                utilitiesUserParameters.getRefMass()
                        );

                        waitingHandler.appendReport(
                                "Processing " + spectrumFile.getName() + " with " + Advocate.comet.getName() + ".",
                                true,
                                true
                        );

                        waitingHandler.appendReportEndLine();
                        cometProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            // move the comet result file to the results folder
                            File tempCometOutputFile = new File(spectrumFile.getParent(), getCometFileName(spectrumFileName));
                            FileUtils.moveFile(tempCometOutputFile, cometOutputFile);

                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);

                            if (runIdentificationFiles == null) {

                                runIdentificationFiles = new HashMap<>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);

                            }

                            if (cometOutputFile.exists()) {

                                runIdentificationFiles.put(Advocate.comet.getIndex(), cometOutputFile);
                                idFileToSpectrumFileMap.put(cometOutputFile.getName(), spectrumFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.comet.getName() + " result file for " + spectrumFileName + ".",
                                        true,
                                        true
                                );
                            }

                            waitingHandler.increasePrimaryProgressCounter();

                        }
                    }

                    // run Tide
                    File ms2File = null;

                    if (enableTide && !waitingHandler.isRunCanceled()) {

                        // Make ms2 file
                        waitingHandler.appendReport(
                                "Converting spectrum file " + spectrumFileName + " for Tide.",
                                true,
                                true
                        );

                        ms2File = new File(getPeakListFolder(getJarFilePath()), IoUtil.removeExtension(spectrumFileName) + ".ms2");

                        MsFileExporter.writeMs2File(
                                msFileHandler,
                                spectrumFileName,
                                ms2File,
                                waitingHandler
                        );

                        File tideOutputFile = new File(outputTempFolder, getTideFileName(spectrumFileName));

                        // perform the tide search
                        if (!waitingHandler.isRunCanceled()) {

                            tideSearchProcessBuilder = new TideSearchProcessBuilder(
                                    tideLocation,
                                    searchParameters,
                                    ms2File,
                                    waitingHandler,
                                    exceptionHandler,
                                    processingParameters.getnThreads()
                            );

                            waitingHandler.appendReport(
                                    "Processing " + ms2File.getName() + " with " + Advocate.tide.getName() + ".",
                                    true,
                                    true
                            );
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

                                runIdentificationFiles = new HashMap<>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);

                            }

                            if (tideOutputFile.exists()) {

                                runIdentificationFiles.put(Advocate.tide.getIndex(), tideOutputFile);
                                idFileToSpectrumFileMap.put(tideOutputFile.getName(), spectrumFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.tide.getName() + " result file for " + spectrumFileName + ".",
                                        true,
                                        true
                                );

                            }

                            waitingHandler.increasePrimaryProgressCounter();

                        }
                    }

                    // Run Andromeda
                    File aplFile = null;

                    if (enableAndromeda && !waitingHandler.isRunCanceled()) {

                        // Make apl file
                        waitingHandler.appendReport(
                                "Converting spectrum file " + spectrumFileName + " for Andromeda.",
                                true,
                                true
                        );

                        aplFile = new File(getPeakListFolder(getJarFilePath()), IoUtil.removeExtension(spectrumFileName) + ".apl");

                        MsFileExporter.writeAplFile(
                                msFileHandler,
                                spectrumFileName,
                                aplFile,
                                searchParameters,
                                waitingHandler
                        );

                        File andromedaOutputFile = new File(outputTempFolder, getAndromedaFileName(spectrumFileName));

                        andromedaProcessBuilder = new AndromedaProcessBuilder(
                                andromedaLocation,
                                searchParameters,
                                identificationParametersFile,
                                aplFile,
                                fastaFile,
                                waitingHandler,
                                exceptionHandler,
                                processingParameters.getnThreads()
                        );
                        waitingHandler.appendReport(
                                "Processing " + aplFile.getName() + " with " + Advocate.andromeda.getName() + ".",
                                true,
                                true
                        );
                        waitingHandler.appendReportEndLine();
                        andromedaProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            File tempResultFile = new File(aplFile.getParent(), getAndromedaFileName(spectrumFileName));

                            if (tempResultFile.exists()) {

                                IoUtil.copyFile(tempResultFile, andromedaOutputFile);

                                try {
                                    tempResultFile.delete();
                                } catch (Exception e) {
                                    waitingHandler.appendReport(
                                            "An error occurred when attempting to delete " + tempResultFile.getName() + ".",
                                            true,
                                            true
                                    );
                                }

                                HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);

                                if (runIdentificationFiles == null) {

                                    runIdentificationFiles = new HashMap<>();
                                    identificationFiles.put(spectrumFileName, runIdentificationFiles);

                                }

                                if (andromedaOutputFile.exists()) {

                                    runIdentificationFiles.put(Advocate.andromeda.getIndex(), andromedaOutputFile);
                                    idFileToSpectrumFileMap.put(andromedaOutputFile.getName(), spectrumFile);

                                } else {

                                    waitingHandler.appendReport(
                                            "Could not find " + Advocate.andromeda.getName() + " result file for " + spectrumFileName + ".",
                                            true,
                                            true
                                    );

                                }
                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.andromeda.getName() + " .res file for " + spectrumFileName + ".",
                                        true,
                                        true
                                );
                            }

                            waitingHandler.increasePrimaryProgressCounter();

                        }
                    }

                    // Run MetaMorpheus
                    if (enableMetaMorpheus && !waitingHandler.isRunCanceled()) {

                        File metaMorpheusOutputFile = new File(outputTempFolder, getMetaMorpheusFileName(spectrumFileName));

                        metaMorpheusProcessBuilder = new MetaMorpheusProcessBuilder(
                                metaMorpheusLocation,
                                searchParameters,
                                spectrumFile, // @TODO: should complain if not mzml!
                                processingParameters.getnThreads(),
                                fastaFile,
                                metaMorpheusOutputFile,
                                waitingHandler,
                                exceptionHandler
                        );
                        waitingHandler.appendReport(
                                "Processing " + spectrumFileName + " with " + Advocate.metaMorpheus.getName() + ".",
                                true,
                                true
                        );
                        waitingHandler.appendReportEndLine();
                        metaMorpheusProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            String taskFileName
                                    = ((MetaMorpheusParameters) searchParameters.getIdentificationAlgorithmParameter(
                                            Advocate.metaMorpheus.getIndex())).runGptm()
                                    ? "Task2SearchTask" : "Task1SearchTask";

                            File tempResultFile = new File(
                                    MetaMorpheusProcessBuilder.getTempFolderPath(metaMorpheusLocation)
                                    + File.separator + taskFileName,
                                    getMetaMorpheusFileName(spectrumFileName));

                            if (tempResultFile.exists()) {

                                IoUtil.copyFile(tempResultFile, metaMorpheusOutputFile);

                                try {
                                    tempResultFile.delete();
                                } catch (Exception e) {
                                    waitingHandler.appendReport(
                                            "An error occurred when attempting to delete " + tempResultFile.getName() + ".",
                                            true,
                                            true
                                    );
                                }

                                HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);

                                if (runIdentificationFiles == null) {

                                    runIdentificationFiles = new HashMap<>();
                                    identificationFiles.put(spectrumFileName, runIdentificationFiles);

                                }

                                if (metaMorpheusOutputFile.exists()) {

                                    runIdentificationFiles.put(Advocate.metaMorpheus.getIndex(), metaMorpheusOutputFile);
                                    idFileToSpectrumFileMap.put(metaMorpheusOutputFile.getName(), spectrumFile);

                                } else {

                                    waitingHandler.appendReport(
                                            "Could not find " + Advocate.metaMorpheus.getName() + " result file for " + spectrumFileName + ".",
                                            true,
                                            true
                                    );

                                }
                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.metaMorpheus.getName() + " .mzID file for " + spectrumFileName + ".",
                                        true,
                                        true
                                );
                            }

                        }

                    }

                    // Run Novor
                    if (enableNovor && !waitingHandler.isRunCanceled()) {

                        File novorOutputFile = new File(outputTempFolder, getNovorFileName(spectrumFileName));

                        novorProcessBuilder = new NovorProcessBuilder(
                                novorLocation,
                                mgfFile,
                                novorOutputFile,
                                searchParameters,
                                useCommandLine,
                                waitingHandler,
                                exceptionHandler
                        );
                        waitingHandler.appendReport(
                                "Processing " + mgfFile.getName() + " with " + Advocate.novor.getName() + ".",
                                true,
                                true
                        );
                        waitingHandler.appendReportEndLine();
                        novorProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);

                            if (runIdentificationFiles == null) {

                                runIdentificationFiles = new HashMap<>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);

                            }

                            if (novorOutputFile.exists()) {

                                runIdentificationFiles.put(Advocate.novor.getIndex(), novorOutputFile);
                                idFileToSpectrumFileMap.put(novorOutputFile.getName(), spectrumFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.novor.getName() + " result file for " + spectrumFileName + ".",
                                        true,
                                        true
                                );
                            }

                            waitingHandler.increasePrimaryProgressCounter();

                        }
                    }

                    // Run DirecTag
                    if (enableDirecTag && !waitingHandler.isRunCanceled()) {

                        File direcTagOutputFile = new File(outputTempFolder, getDirecTagFileName(spectrumFileName));

                        direcTagProcessBuilder = new DirecTagProcessBuilder(
                                direcTagLocation,
                                mgfFile,
                                processingParameters.getnThreads(),
                                outputTempFolder,
                                searchParameters,
                                waitingHandler,
                                exceptionHandler
                        );
                        waitingHandler.appendReport(
                                "Processing " + mgfFile.getName() + " with " + Advocate.direcTag.getName() + ".",
                                true,
                                true
                        );
                        waitingHandler.appendReportEndLine();
                        direcTagProcessBuilder.startProcess();

                        if (!waitingHandler.isRunCanceled()) {

                            HashMap<Integer, File> runIdentificationFiles = identificationFiles.get(spectrumFileName);

                            if (runIdentificationFiles == null) {

                                runIdentificationFiles = new HashMap<>();
                                identificationFiles.put(spectrumFileName, runIdentificationFiles);

                            }
                            if (direcTagOutputFile.exists()) {

                                runIdentificationFiles.put(Advocate.direcTag.getIndex(), direcTagOutputFile);
                                idFileToSpectrumFileMap.put(direcTagOutputFile.getName(), spectrumFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.direcTag.getName() + " result file for " + spectrumFileName + ".",
                                        true,
                                        true
                                );
                            }

                            waitingHandler.increasePrimaryProgressCounter();

                        }
                    }

                    // Delete the temp spectrum files
                    if (mgfFile != null) {
                        mgfFile.delete();
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

                // save the ptm mappings for novor and directag
                identificationParametersFactory.addIdentificationParameters(identificationParameters);

                outputTimeStamp = getOutputDate();

                if (!waitingHandler.isRunCanceled()) {

                    // organize the output files
                    if (utilitiesUserParameters.getSearchGuiOutputParameters() != OutputParameters.no_zip) {

                        waitingHandler.appendReport(
                                "Zipping output files.",
                                true,
                                true
                        );

                    } else {

                        waitingHandler.appendReport(
                                "Preparing output files.",
                                true,
                                true
                        );
                    }

                    waitingHandler.appendReportEndLine();

                    organizeOutput(
                            outputFolder,
                            outputTempFolder,
                            identificationFiles,
                            identificationParametersFile,
                            utilitiesUserParameters.isIncludeDateInOutputName()
                    );

                    waitingHandler.increasePrimaryProgressCounter();

                }

                if (enablePeptideShaker && !waitingHandler.isRunCanceled()) { // @TODO: the output file checks below don't work when the date is added to the file name... 

                    ArrayList<File> identificationFilesList = new ArrayList<>();
                    identificationFilesList.addAll(mascotFiles);

                    if (utilitiesUserParameters.getSearchGuiOutputParameters() == OutputParameters.grouped) {

                        File outputFile = getDefaultOutputFile(outputFolder, utilitiesUserParameters.isIncludeDateInOutputName());

                        if (outputFile.exists()) {

                            identificationFilesList.add(outputFile);

                        } else {

                            waitingHandler.appendReport(
                                    "Could not find SearchGUI results.",
                                    true,
                                    true
                            );
                        }
                    } else if (utilitiesUserParameters.getSearchGuiOutputParameters() == OutputParameters.algorithm) {

                        if (enableMsAmanda) {

                            File outputFile = getDefaultOutputFile(
                                    outputFolder,
                                    Advocate.msAmanda.getName(),
                                    utilitiesUserParameters.isIncludeDateInOutputName()
                            );

                            if (outputFile.exists()) {

                                identificationFilesList.add(outputFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.msAmanda.getName() + " results.",
                                        true,
                                        true
                                );
                            }
                        }
                        if (enableMsgf) {

                            File outputFile = getDefaultOutputFile(
                                    outputFolder,
                                    Advocate.msgf.getName(),
                                    utilitiesUserParameters.isIncludeDateInOutputName()
                            );
                            if (outputFile.exists()) {

                                identificationFilesList.add(outputFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.msgf.getName() + " results.",
                                        true,
                                        true
                                );
                            }
                        }
                        if (enableMyriMatch) {

                            File outputFile = getDefaultOutputFile(
                                    outputFolder,
                                    Advocate.myriMatch.getName(),
                                    utilitiesUserParameters.isIncludeDateInOutputName()
                            );

                            if (outputFile.exists()) {

                                identificationFilesList.add(outputFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.myriMatch.getName() + " results.",
                                        true,
                                        true
                                );
                            }
                        }
                        if (enableOmssa) {

                            File outputFile = getDefaultOutputFile(
                                    outputFolder,
                                    Advocate.omssa.getName(),
                                    utilitiesUserParameters.isIncludeDateInOutputName()
                            );

                            if (outputFile.exists()) {

                                identificationFilesList.add(outputFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.omssa.getName() + " results.",
                                        true,
                                        true
                                );
                            }
                        }
                        if (enableXtandem) {

                            File outputFile = getDefaultOutputFile(
                                    outputFolder,
                                    Advocate.xtandem.getName(),
                                    utilitiesUserParameters.isIncludeDateInOutputName()
                            );

                            if (outputFile.exists()) {

                                identificationFilesList.add(outputFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.xtandem.getName() + " results.",
                                        true,
                                        true
                                );
                            }
                        }
                        if (enableComet) {

                            File outputFile = getDefaultOutputFile(
                                    outputFolder,
                                    Advocate.comet.getName(),
                                    utilitiesUserParameters.isIncludeDateInOutputName()
                            );

                            if (outputFile.exists()) {

                                identificationFilesList.add(outputFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.comet.getName() + " results.",
                                        true,
                                        true
                                );
                            }
                        }
                        if (enableTide) {
                            File outputFile = getDefaultOutputFile(outputFolder, Advocate.tide.getName(), utilitiesUserParameters.isIncludeDateInOutputName());
                            if (outputFile.exists()) {
                                identificationFilesList.add(outputFile);
                            } else {
                                waitingHandler.appendReport("Could not find " + Advocate.tide.getName() + " results.", true, true);
                            }
                        }
                        if (enableAndromeda) {

                            File outputFile = getDefaultOutputFile(
                                    outputFolder,
                                    Advocate.andromeda.getName(),
                                    utilitiesUserParameters.isIncludeDateInOutputName()
                            );

                            if (outputFile.exists()) {

                                identificationFilesList.add(outputFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.andromeda.getName() + " results.",
                                        true,
                                        true
                                );
                            }
                        }
                        if (enableAndromeda) {

                            File outputFile = getDefaultOutputFile(
                                    outputFolder,
                                    Advocate.andromeda.getName(),
                                    utilitiesUserParameters.isIncludeDateInOutputName()
                            );

                            if (outputFile.exists()) {

                                identificationFilesList.add(outputFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.andromeda.getName() + " results.",
                                        true,
                                        true
                                );
                            }
                        }
                        if (enableMetaMorpheus) {

                            File outputFile = getDefaultOutputFile(
                                    outputFolder,
                                    Advocate.metaMorpheus.getName(),
                                    utilitiesUserParameters.isIncludeDateInOutputName()
                            );

                            if (outputFile.exists()) {

                                identificationFilesList.add(outputFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.metaMorpheus.getName() + " results.",
                                        true,
                                        true
                                );
                            }
                        }
                        if (enableDirecTag) {

                            File outputFile = getDefaultOutputFile(
                                    outputFolder,
                                    Advocate.direcTag.getName(),
                                    utilitiesUserParameters.isIncludeDateInOutputName()
                            );

                            if (outputFile.exists()) {

                                identificationFilesList.add(outputFile);

                            } else {

                                waitingHandler.appendReport(
                                        "Could not find " + Advocate.direcTag.getName() + " results.",
                                        true,
                                        true
                                );
                            }
                        }
                    } else if (utilitiesUserParameters.getSearchGuiOutputParameters() == OutputParameters.run) {

                        for (String run : identificationFiles.keySet()) {

                            String runName = IoUtil.removeExtension(run);
                            File outputFile = getDefaultOutputFile(outputFolder, runName, utilitiesUserParameters.isIncludeDateInOutputName());

                            if (outputFile.exists()) {

                                identificationFilesList.add(outputFile);

                            } else {

                                waitingHandler.appendReport(
                                        "SearchGUI results not found for run " + runName + ".",
                                        true,
                                        true
                                );
                            }
                        }
                    } else {

                        for (HashMap<Integer, File> fileMap : identificationFiles.values()) {

                            for (File identificationFile : fileMap.values()) {

                                identificationFilesList.add(identificationFile);

                            }
                        }
                    }

                    if (utilitiesUserParameters.getPeptideShakerPath() == null || !new File(utilitiesUserParameters.getPeptideShakerPath()).exists()) {

                        waitingHandler.appendReport(
                                "PeptideShaker not found! Please check the PeptideShaker path.",
                                true,
                                true
                        );
                        waitingHandler.appendReportEndLine();
                        waitingHandler.setRunCanceled();

                    } else if (!identificationFiles.isEmpty()) {

                        ArrayList<File> cmsAndMsFiles = new ArrayList<>(cmsFiles.size() + msFiles.size());
                        cmsAndMsFiles.addAll(cmsFiles);
                        cmsAndMsFiles.addAll(msFiles);

                        // add date to the PeptideShaker file name
                        if (utilitiesUserParameters.isIncludeDateInOutputName()) {
                            peptideShakerFile = new File(
                                    peptideShakerFile.getParentFile(),
                                    IoUtil.removeExtension(
                                            peptideShakerFile.getName()) + "_" + outputTimeStamp + ".psdb");
                        }

                        peptideShakerProcessBuilder = new PeptideShakerProcessBuilder(
                                waitingHandler,
                                exceptionHandler,
                                experimentLabel,
                                cmsAndMsFiles,
                                fastaFile,
                                identificationFilesList,
                                identificationParametersFile,
                                peptideShakerFile,
                                true,
                                processingParameters,
                                utilitiesUserParameters.outputData()
                        );

                        waitingHandler.appendReport(
                                "Processing identification files with PeptideShaker.",
                                true,
                                true
                        );

                        peptideShakerProcessBuilder.startProcess();

                    } else {

                        enablePeptideShaker = false;
                        waitingHandler.appendReportEndLine();

                        waitingHandler.appendReport(
                                "No identification files to process with PeptideShaker.",
                                true,
                                true
                        );
                        waitingHandler.appendReportEndLine();
                    }
                }

                if (!outputFolder.getAbsolutePath().equals(outputTempFolder.getAbsolutePath())) {

                    boolean deleteTempFolder = true;

                    if (!identificationFiles.isEmpty() && waitingHandler.isRunCanceled() && waitingHandler instanceof WaitingDialog) {

                        WaitingDialog guiWaitingDialog = (WaitingDialog) waitingHandler;

                        // change the icon to the normal icon
                        ((JFrame) guiWaitingDialog.getParent()).setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")));

                        int option = JOptionPane.showConfirmDialog(
                                guiWaitingDialog,
                                "Keep the partial search results?",
                                "Keep Partial Results?",
                                JOptionPane.YES_NO_OPTION
                        );

                        deleteTempFolder = (option == JOptionPane.NO_OPTION);

                        // change the icon to the waiting icon
                        ((JFrame) guiWaitingDialog.getParent()).setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
                    }
                    if (deleteTempFolder) {

                        IoUtil.deleteDir(outputTempFolder);

                    }
                }

                if (enableAndromeda && AndromedaProcessBuilder.getTempFolderPath() != null) {

                    File andromedaTempFolder = new File(AndromedaProcessBuilder.getTempFolderPath());

                    if (andromedaTempFolder.exists()) {

                        IoUtil.emptyDir(andromedaTempFolder);

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

                waitingHandler.appendReport(
                        "Error: " + e.getMessage(),
                        true,
                        true
                );
                waitingHandler.appendReport(
                        "An error occurred while running SearchGUI. Please contact the developers.",
                        true,
                        true
                );
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

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

            // add the fasta file
            bw.write(fastaFile.getAbsolutePath() + System.getProperty("line.separator"));

            // add the ms files
            for (File spectrumFile : msFiles) {

                bw.write(spectrumFile.getAbsolutePath() + System.getProperty("line.separator"));

            }

        } catch (Exception e) {
            e.printStackTrace();
            // ignore error
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
     * @return the processingParameters
     */
    public ProcessingParameters getProcessingParameters() {
        return processingParameters;
    }

    /**
     * Set the processing preferences.
     *
     * @param processingParameters the processingParameters to set
     */
    public void setProcessingParameters(ProcessingParameters processingParameters) {
        this.processingParameters = processingParameters;
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
     * Returns the ThermoRawFileParser parameters.
     *
     * @return the ThermoRawFileParser parameters
     */
    public ThermoRawFileParserParameters getThermoRawFileParserParameters() {
        return thermoRawFileParserParameters;
    }

    /**
     * Sets the ThermoRawFileParser parameters.
     *
     * @param thermoRawFileParserParameters the ThermoRawFileParser parameters
     */
    public void setThermoRawFileParserParameters(ThermoRawFileParserParameters thermoRawFileParserParameters) {
        this.thermoRawFileParserParameters = thermoRawFileParserParameters;
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
        fileName += defaultOutputFileName;
        if (includeDate) {
            fileName += "_" + outputTimeStamp;
        }
        fileName += DEFAULT_OUTPUT_FILE_NAME_ENDING;
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
        fileName += defaultOutputFileName;
        if (includeDate) {
            fileName += "_" + outputTimeStamp;
        }
        fileName += DEFAULT_OUTPUT_FILE_NAME_ENDING;
        return new File(outputFolder, fileName);
    }

    /**
     * Returns the date as a string to be included in the output.
     * yyyy-MM-dd_HH.mm.ss.
     *
     * @return the date as a string to be included in the output
     */
    public static String getOutputDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        return df.format(new Date());
    }

    /**
     * Set the current output time stamp.
     *
     * @param outputTimeStamp the current output time stamp
     */
    public void setOutputTimeStamp(String outputTimeStamp) {
        SearchHandler.outputTimeStamp = outputTimeStamp;
    }

    /**
     * Returns the file where the paths to the spectrum and FASTA files are
     * saved.
     *
     * @param outputFolder the folder where this file shall be saved
     *
     * @return the file where the paths to the spectrum and FASTA file paths are
     * saved
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
     * @param tempOutputFolder the folder where the raw SearchGUI output is
     * stored
     * @param identificationFilesMap the identification files map
     * @param parametersFile the parameters file
     * @param includeDate if true the date will be included in the output file
     * name
     *
     * @throws IOException thrown if there is a problem with the files
     */
    public void organizeOutput(
            File outputFolder,
            File tempOutputFolder,
            HashMap<String, HashMap<Integer, File>> identificationFilesMap,
            File parametersFile,
            boolean includeDate
    ) throws IOException {

        UtilitiesUserParameters utilitiesUserParameters = UtilitiesUserParameters.loadUserParameters();

        if (utilitiesUserParameters.isGzip()) {

            identificationFilesMap.values().stream()
                    .flatMap(
                            map -> map.values().stream()
                    )
                    .parallel()
                    .forEach(
                            file -> GzUtils.gzFile(
                                    file,
                                    true
                            )
                    );

            HashMap<String, HashMap<Integer, File>> compressedIdentificationFiles = new HashMap<>(identificationFilesMap.size());

            for (Entry<String, HashMap<Integer, File>> entry1 : identificationFilesMap.entrySet()) {

                HashMap<Integer, File> map = entry1.getValue();
                HashMap<Integer, File> newMap = new HashMap<>(map.size());
                compressedIdentificationFiles.put(entry1.getKey(), newMap);

                for (Entry<Integer, File> entry2 : map.entrySet()) {

                    File file = entry2.getValue();
                    File gzFile = new File(file.getAbsolutePath() + ".gz");

                    newMap.put(entry2.getKey(), gzFile);

                }
            }

            identificationFilesMap = compressedIdentificationFiles;

        }

        switch (utilitiesUserParameters.getSearchGuiOutputParameters()) {

            case grouped:

                // put everything in a single zip file
                File zipFile = getDefaultOutputFile(outputFolder, includeDate);

                // delete old zip file
                if (zipFile.exists()) {
                    zipFile.delete();
                }

                try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {

                    // find the uncompressed size of all the files to add to the zip
                    long totalUncompressedSize = getTotalUncompressedSize(tempOutputFolder, parametersFile, identificationFilesMap);
                    waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                    waitingHandler.setSecondaryProgressCounter(0);
                    waitingHandler.setMaxSecondaryProgressCounter(100);

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
                        for (File spectrumFile : msFiles) {
                            String newName = IoUtil.removeExtension(spectrumFile.getName()) + "_settings.xml";
                            File settingsFile = new File(tempOutputFolder, newName);
                            if (settingsFile.exists()) {
                                ZipUtils.addFileToZip(settingsFile, out, waitingHandler, totalUncompressedSize);
                            }
                        }
                    }

                    // add the identification files
                    for (HashMap<Integer, File> fileMap : identificationFilesMap.values()) {
                        for (File identificationFile : fileMap.values()) {
                            ZipUtils.addFileToZip(identificationFile, out, waitingHandler, totalUncompressedSize);
                        }
                    }

                    // add the data files
                    if (utilitiesUserParameters.outputData()) {
                        addDataToZip(out, totalUncompressedSize);
                    }
                }

                break;

            case algorithm:

                // group files according to the search engine used
                HashMap<Integer, ArrayList<File>> algorithmToFileMap = new HashMap<>();

                for (HashMap<Integer, File> fileMap : identificationFilesMap.values()) {

                    for (Integer algorithm : fileMap.keySet()) {

                        ArrayList<File> files = algorithmToFileMap.get(algorithm);

                        if (files == null) {
                            files = new ArrayList<>();
                            algorithmToFileMap.put(algorithm, files);
                        }

                        files.add(fileMap.get(algorithm));
                    }
                }

                File inputFile = getInputFile(tempOutputFolder);

                // find the uncompressed size of all the files to add to the zip
                long totalUncompressedSize = 0;

                for (Integer algorithm : algorithmToFileMap.keySet()) {
                    totalUncompressedSize += getTotalUncompressedSizeAlgorithm(
                            inputFile, tempOutputFolder, algorithm,
                            parametersFile, algorithmToFileMap.get(algorithm));
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

                    try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {

                        // add input file
                        ZipUtils.addFileToZip(inputFile, out, waitingHandler, totalUncompressedSize);

                        // add search parameters files
                        ZipUtils.addFileToZip(parametersFile, out, waitingHandler, totalUncompressedSize);

                        if (algorithm == Advocate.omssa.getIndex()) {
                            // add OMSSA settings files
                            File modificationsFile = new File(tempOutputFolder, "omssa_mods.xml");
                            ZipUtils.addFileToZip(modificationsFile, out, waitingHandler, totalUncompressedSize);

                            File userModificationsFile = new File(tempOutputFolder, "omssa_usermods.xml");
                            ZipUtils.addFileToZip(userModificationsFile, out, waitingHandler, totalUncompressedSize);
                        }

                        if (algorithm == Advocate.msAmanda.getIndex()) {
                            // add MS Amanda settings file
                            for (File spectrumFile : msFiles) {
                                String newName = IoUtil.removeExtension(spectrumFile.getName()) + "_settings.xml";
                                File settingsFile = new File(tempOutputFolder, newName);
                                if (settingsFile.exists()) {
                                    ZipUtils.addFileToZip(settingsFile, out, waitingHandler, totalUncompressedSize);
                                }
                            }
                        }

                        for (File identificationFile : algorithmToFileMap.get(algorithm)) {
                            ZipUtils.addFileToZip(identificationFile, out, waitingHandler, totalUncompressedSize);
                        }

                        if (utilitiesUserParameters.outputData()) {
                            addDataToZip(out, totalUncompressedSize);
                        }

                    }
                }

                break;

            case run:

                // group files according to the spectrum files
                inputFile = getInputFile(tempOutputFolder);

                // find the uncompressed size of all the files to add to the zip
                totalUncompressedSize = 0;

                for (String spectrumFileName : identificationFilesMap.keySet()) {

                    String spectrumFileNameWithoutExtension = IoUtil.removeExtension(spectrumFileName);

                    totalUncompressedSize += getTotalUncompressedSizeRun(
                            inputFile,
                            tempOutputFolder,
                            spectrumFileNameWithoutExtension,
                            spectrumFileName,
                            parametersFile,
                            identificationFilesMap,
                            getSpectrumFile(spectrumFileName),
                            getCmsFile(IoUtil.removeExtension(spectrumFileName) + ".cms")
                    );
                }

                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                waitingHandler.setSecondaryProgressCounter(0);
                waitingHandler.setMaxSecondaryProgressCounter(100);

                for (String spectrumFileName : identificationFilesMap.keySet()) {

                    String spectrumFileNameWithoutExtension = IoUtil.removeExtension(spectrumFileName);
                    zipFile = getDefaultOutputFile(outputFolder, spectrumFileNameWithoutExtension, includeDate);

                    if (zipFile.exists()) {
                        zipFile.delete();
                    }

                    try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {

                        // add input file
                        ZipUtils.addFileToZip(inputFile, out, waitingHandler, totalUncompressedSize);

                        // add search parameters files
                        ZipUtils.addFileToZip(parametersFile, out, waitingHandler, totalUncompressedSize);

                        if (enableOmssa) {
                            // add omssa modification files
                            File modificationsFile = new File(tempOutputFolder, "omssa_mods.xml");
                            ZipUtils.addFileToZip(modificationsFile, out, waitingHandler, totalUncompressedSize);

                            File userModificationsFile = new File(tempOutputFolder, "omssa_usermods.xml");
                            ZipUtils.addFileToZip(userModificationsFile, out, waitingHandler, totalUncompressedSize);
                        }

                        if (enableMsAmanda) {
                            // add ms amanda settings file
                            String newName = spectrumFileNameWithoutExtension + "_settings.xml";
                            File settingsFile = new File(tempOutputFolder, newName);
                            if (settingsFile.exists()) {
                                ZipUtils.addFileToZip(settingsFile, out, waitingHandler, totalUncompressedSize);
                            }
                        }

                        // add the identification files
                        HashMap<Integer, File> fileMap = identificationFilesMap.get(spectrumFileName);
                        for (File identificationFile : fileMap.values()) {
                            ZipUtils.addFileToZip(identificationFile, out, waitingHandler, totalUncompressedSize);
                        }

                        // add the data files
                        if (utilitiesUserParameters.outputData()) {
                            String cmsFileName = IoUtil.removeExtension(spectrumFileName) + ".cms";
                            addDataToZip(out, totalUncompressedSize, spectrumFileName, cmsFileName);
                        }

                    }
                }

                break;

            default: // no zipping

                // add data files if needed
                if (utilitiesUserParameters.outputData()) {

                    // create the data folder
                    File dataFolder = new File(outputFolder, DEFAULT_DATA_FOLDER);
                    dataFolder.mkdir();

                    // copy fasta file
                    IoUtil.copyFile(fastaFile, new File(dataFolder, fastaFile.getName()));

                    // copy the spectrum files
                    for (File spectrumFile : getSpectrumFiles()) {
                        IoUtil.copyFile(spectrumFile, new File(dataFolder, spectrumFile.getName()));
                    }

                    // copy the cms files
                    for (File cmsFile : getCmsFiles()) {
                        IoUtil.copyFile(cmsFile, new File(dataFolder, cmsFile.getName()));
                    }
                }
        }

        if (!outputFolder.getAbsolutePath().equals(tempOutputFolder.getAbsolutePath())) {
            IoUtil.deleteDir(tempOutputFolder);
        }
    }

    /**
     * Adds the spectrum and FASTA files to the zip file.
     *
     * @param out the zip stream
     *
     * @throws IOException
     */
    private void addDataToZip(
            ZipOutputStream out,
            long totalUncompressedSize
    ) throws IOException {

        addDataToZip(
                out,
                totalUncompressedSize,
                null,
                null
        );
    }

    /**
     * Adds the spectrum and FASTA files to the zip file.
     *
     * @param out the zip stream
     * @param spectrumFileName only add the given spectrum file, null means add
     * all spectrum files
     * @param cmsFileName only add the given cms file, null means add all cms
     * files
     *
     * @throws IOException
     */
    private void addDataToZip(
            ZipOutputStream out,
            long totalUncompressedSize,
            String spectrumFileName,
            String cmsFileName
    ) throws IOException {

        // create the data folder in the zip file
        ZipUtils.addFolderToZip(
                DEFAULT_DATA_FOLDER,
                out
        );

        // add the fasta file
        ZipUtils.addFileToZip(
                DEFAULT_DATA_FOLDER,
                fastaFile,
                out,
                waitingHandler,
                totalUncompressedSize
        );

        // add the spectrum files
        for (File spectrumFile : getSpectrumFiles()) {

            boolean addFile = true;

            if (spectrumFileName != null) {

                addFile = spectrumFile.getName().equals(spectrumFileName);

            }

            if (addFile) {

                ZipUtils.addFileToZip(
                        DEFAULT_DATA_FOLDER,
                        spectrumFile,
                        out,
                        waitingHandler,
                        totalUncompressedSize
                );
            }
        }

        // add the cms files
        for (File cmsFile : getCmsFiles()) {

            boolean addFile = true;

            if (cmsFileName != null) {

                addFile = cmsFile.getName().equals(cmsFileName);

            }

            if (addFile) {
                ZipUtils.addFileToZip(
                        DEFAULT_DATA_FOLDER,
                        cmsFile,
                        out,
                        waitingHandler,
                        totalUncompressedSize
                );
            }
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
    private long getTotalUncompressedSize(
            File outputFolder,
            File parametersFile,
            HashMap<String, HashMap<Integer, File>> identificationFiles
    ) {

        long totalUncompressedSize = 0;

        totalUncompressedSize += getInputFile(outputFolder).length(); // input file
        totalUncompressedSize += parametersFile.length(); // parameters file

        if (enableOmssa) {
            // omssa modification files
            File modificationsFile = new File(outputFolder, "omssa_mods.xml");
            totalUncompressedSize += modificationsFile.length();
            File userModificationsFile = new File(outputFolder, "omssa_usermods.xml");
            totalUncompressedSize += userModificationsFile.length();
        }

        if (enableMsAmanda) {

            // ms amanda settings file
            for (File spectrumFile : msFiles) {

                String newName = IoUtil.removeExtension(spectrumFile.getName()) + "_settings.xml";
                File settingsFile = new File(outputFolder, newName);

                if (settingsFile.exists()) {

                    totalUncompressedSize += settingsFile.length();

                }
            }
        }

        // identification files
        for (HashMap<Integer, File> fileMap : identificationFiles.values()) {
            for (File identificationFile : fileMap.values()) {
                totalUncompressedSize += identificationFile.length();
            }
        }

        UtilitiesUserParameters utilitiesUserParameters = UtilitiesUserParameters.loadUserParameters();

        // output data
        if (utilitiesUserParameters.outputData()) {
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
    private long getTotalUncompressedSizeAlgorithm(
            File inputFile,
            File outputFolder,
            Integer algorithm,
            File parametersFile,
            ArrayList<File> identificationFiles
    ) {

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
            for (File spectrumFile : msFiles) {
                String newName = IoUtil.removeExtension(spectrumFile.getName()) + "_settings.xml";
                File settingsFile = new File(outputFolder, newName);
                if (settingsFile.exists()) {
                    totalUncompressedSize += settingsFile.length();
                }
            }
        }

        for (File identificationFile : identificationFiles) {
            totalUncompressedSize += identificationFile.length();
        }

        UtilitiesUserParameters utilitiesUserParameters = UtilitiesUserParameters.loadUserParameters();

        // output data
        if (utilitiesUserParameters.outputData()) {
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
     * @param run the spectrum file of interest
     * @param parametersFile the parameters file
     * @param identificationFiles the identification files
     * @param spectrumFile only add given spectrum file, null means add all
     * @param cmsFile the cms file
     *
     * @return the total uncompressed size of the files for the given run
     */
    private long getTotalUncompressedSizeRun(
            File inputFile,
            File outputFolder,
            String runName,
            String run,
            File parametersFile,
            HashMap<String, HashMap<Integer, File>> identificationFiles,
            File spectrumFile,
            File cmsFile
    ) {

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

        UtilitiesUserParameters utilitiesUserParameters = UtilitiesUserParameters.loadUserParameters();

        // output data
        if (utilitiesUserParameters.outputData()) {
            totalUncompressedSize += getTotalUncompressedSizeOfData(spectrumFile, cmsFile);
        }

        return totalUncompressedSize;
    }

    /**
     * Get the total uncompressed size of the FASTA and spectrum files.
     *
     * @return the total uncompressed size of the FASTA and spectrum files
     */
    private long getTotalUncompressedSizeOfData() {
        return getTotalUncompressedSizeOfData(null, null);
    }

    /**
     * Get the total uncompressed size of the FASTA and spectrum files.
     *
     * @param spectrumFile only add given spectrumFile, null means add all
     * @param cmsFile only add the given cms file, null means add all
     *
     * @return the total uncompressed size of the FASTA and spectrum files
     */
    private long getTotalUncompressedSizeOfData(File spectrumFile, File cmsFile) {

        long totalUncompressedSize = fastaFile.length();

        if (spectrumFile != null) {
            totalUncompressedSize += spectrumFile.length();
        } else {
            for (File tempSpectrumFile : getSpectrumFiles()) {
                totalUncompressedSize += tempSpectrumFile.length();
            }
        }

        if (cmsFile != null) {
            totalUncompressedSize += cmsFile.length();
        } else {
            for (File tempCmsFile : getCmsFiles()) {
                totalUncompressedSize += tempCmsFile.length();
            }
        }

        return totalUncompressedSize;
    }

    /**
     * Returns the folder to use to store peak lists.
     *
     * @param jarFilePath the path to the jar file
     * @return the folder to use to store peak lists
     */
    public static File getPeakListFolder(
            String jarFilePath
    ) {
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
    public static String getTempFolderPath(
            String jarFilePath
    ) {
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
     * @param tempFolderPath the folder to use for temporary files
     */
    public static void setTempFolderPath(
            String tempFolderPath
    ) {
        tempFolderPath = tempFolderPath;
    }

    /**
     * Sets the log folder.
     *
     * @param logFolder the log folder
     */
    public void setLogFolder(
            File logFolder
    ) {
        this.logFolder = logFolder;
    }

    /**
     * Verifies that the modifications backed-up in the search parameters are
     * loaded and returns an error message if one was already loaded, null
     * otherwise.
     *
     * @param searchParameters the search parameters to load
     * @return an error message if one was already loaded, null otherwise
     */
    public static String loadModifications(
            SearchParameters searchParameters
    ) {
        String error = null;
        ArrayList<String> toCheck = ModificationFactory.getInstance().loadBackedUpModifications(searchParameters, true);
        if (!toCheck.isEmpty()) {
            error = "The definition of the following PTM(s) seems to have changed and were overwritten:\n";
            for (int i = 0; i < toCheck.size(); i++) {
                if (i > 0) {
                    if (i < toCheck.size() - 1) {
                        error += ", ";
                    } else {
                        error += " and ";
                    }
                }
                error += toCheck.get(i);
            }
            error += ".\nPlease verify the definition of the PTM(s) in the modifications editor.";
        }
        return error;
    }

    /**
     * Returns the default output file name.
     *
     * @return the defaultOutputFileName
     */
    public static String getDefaultOutputFileName() {
        return defaultOutputFileName;
    }

    /**
     * Sets the default output file name.
     *
     * @param newOutputFileName the defaultOutputFileName to set
     */
    public static void setDefaultOutputFileName(
            String newOutputFileName
    ) {
        defaultOutputFileName = newOutputFileName;
    }
}
