package eu.isas.searchgui.cmd;

import com.compomics.software.CompomicsWrapper;
import com.compomics.util.Util;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.io.mass_spectrometry.MsFileHandler;
import com.compomics.util.experiment.io.temp.TempFilesManager;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.tools.ProcessingParameters;
import com.compomics.util.parameters.UtilitiesUserParameters;
import eu.isas.searchgui.SearchHandler;
import eu.isas.searchgui.utilities.Properties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
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
     * The mass spectrometry file handler.
     */
    private final MsFileHandler msFileHandler = new MsFileHandler();
    /**
     * The log folder given on the command line. Null if not set.
     */
    private static File logFolder = null;
    /**
     * The waiting handler.
     */
    private WaitingHandler waitingHandler;

    /**
     * Construct a new SearchCLI runnable from a list of arguments. When
     * initialization is successful, calling "run" will start SearchGUI and
     * write the output files when finished.
     *
     * @param args the command line arguments
     */
    public SearchCLI(String[] args) {

        try {
            // turn off illegal access log messages
            try {
                Class loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
                Field loggerField = loggerClass.getDeclaredField("logger");
                Class unsafeClass = Class.forName("sun.misc.Unsafe");
                Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                Object unsafe = unsafeField.get(null);
                Long offset = (Long) unsafeClass.getMethod("staticFieldOffset", Field.class).invoke(unsafe, loggerField);
                unsafeClass.getMethod("putObjectVolatile", Object.class, long.class, Object.class) //
                        .invoke(unsafe, loggerClass, offset, null);
            } catch (Throwable ex) {
                // ignore, i.e. simply show the warnings...
                //ex.printStackTrace();
            }

            // check if there are updates to the paths
            String[] nonPathSettingArgsAsList = PathSettingsCLI.extractAndUpdatePathOptions(args);

            waitingHandler = new WaitingHandlerCLIImpl();

            try {
            
                SpeciesFactory speciesFactory = SpeciesFactory.getInstance();
                speciesFactory.initiate(getJarFilePath());
            
            } catch (Exception e) {
                
                waitingHandler.appendReport(
                        "An error occurred while loading the species.", 
                        true, 
                        true
                );
                e.printStackTrace();
            
            }

            // parse the rest of the options   
            Options nonPathOptions = new Options();
            SearchCLIParams.createOptionsCLI(nonPathOptions);
            DefaultParser parser = new DefaultParser();
            CommandLine line = parser.parse(nonPathOptions, nonPathSettingArgsAsList);

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
            
            waitingHandler.appendReport(
                    "An error occurred while running the command line. " + getLogFileMessage(), 
                    true, 
                    true
            );
            e.printStackTrace();
        
        }
    }

    /**
     * Calling this method will run the configured SearchCLI process.
     */
    public Object call() {

        // load enzymes
        enzymeFactory = EnzymeFactory.getInstance();

        try {
            
            WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();

            // get the spectrum files
            ArrayList<File> spectrumFiles = searchCLIInputBean.getSpectrumFiles();

            // Processing
            ProcessingParameters processingParameters = new ProcessingParameters();
            processingParameters.setnThreads(searchCLIInputBean.getNThreads());

            // Identification parameters
            IdentificationParameters identificationParameters = searchCLIInputBean.getIdentificationParameters();
            identificationParameters.getFastaParameters().setTargetDecoyFileNameSuffix(searchCLIInputBean.getTargetDecoyFileNameTag());
            File parametersFile = searchCLIInputBean.getIdentificationParametersFile();

            if (parametersFile == null) {

                String name = identificationParameters.getName();

                if (name == null) {

                    name = "SearchCLI.par";

                } else {

                    name += ".par";

                }
                
                parametersFile = new File(searchCLIInputBean.getOutputFolder(), name);
                IdentificationParameters.saveIdentificationParameters(identificationParameters, parametersFile);
            
            }

            SearchParameters searchParameters = identificationParameters.getSearchParameters();
            String error = SearchHandler.loadModifications(searchParameters);
            if (error != null) {
                System.out.println(error);
            }
           
            UtilitiesUserParameters userParameters = UtilitiesUserParameters.loadUserParameters();
            userParameters.setRefMass(searchCLIInputBean.getRefMass());
            userParameters.setRenameXTandemFile(searchCLIInputBean.renameXTandemFile());
            userParameters.setGzip(searchCLIInputBean.isGzip());
            userParameters.setSearchGuiOutputParameters(searchCLIInputBean.getOutputOption());
            userParameters.setOutputData(searchCLIInputBean.isOutputData());
            userParameters.setIncludeDateInOutputName(searchCLIInputBean.isOutputDate());
            UtilitiesUserParameters.saveUserParameters(userParameters);

            // @TODO: validate the mgf files: see SearchGUI.validateMgfFile
            SearchHandler searchHandler = new SearchHandler(
                    identificationParameters,
                    searchCLIInputBean.getOutputFolder(), 
                    searchCLIInputBean.getDefaultOutputFileName(),
                    spectrumFiles, 
                    searchCLIInputBean.getFastaFile(), 
                    new ArrayList<File>(), 
                    parametersFile,
                    searchCLIInputBean.isOmssaEnabled(), 
                    searchCLIInputBean.isXTandemEnabled(),
                    searchCLIInputBean.isMsgfEnabled(), 
                    searchCLIInputBean.isMsAmandaEnabled(),
                    searchCLIInputBean.isMyriMatchEnabled(), 
                    searchCLIInputBean.isCometEnabled(),
                    searchCLIInputBean.isTideEnabled(), 
                    searchCLIInputBean.isAndromedaEnabled(),
                    searchCLIInputBean.isMetaMorpheusEnabled(),
                    searchCLIInputBean.isNovorEnabled(), 
                    searchCLIInputBean.isDirecTagEnabled(),
                    searchCLIInputBean.getOmssaLocation(), 
                    searchCLIInputBean.getXtandemLocation(),
                    searchCLIInputBean.getMsgfLocation(), 
                    searchCLIInputBean.getMsAmandaLocation(),
                    searchCLIInputBean.getMyriMatchLocation(), 
                    searchCLIInputBean.getCometLocation(),
                    searchCLIInputBean.getTideLocation(), 
                    searchCLIInputBean.getAndromedaLocation(),
                    searchCLIInputBean.getMetaMorpheusLocation(),
                    searchCLIInputBean.getNovorLocation(), 
                    searchCLIInputBean.getDirecTagLocation(),
                    searchCLIInputBean.getMakeblastdbLocation(),
                    processingParameters
            );

            searchHandler.setLogFolder(logFolder);

            // incrementing the counter for a new SearchGUI start
            if (userParameters.isAutoUpdate()) {
                
                Util.sendGAUpdate(
                        "UA-36198780-2", 
                        "startrun-cl", 
                        "searchgui-" + (new Properties().getVersion())
                );

            }

            searchHandler.startSearch(waitingHandlerCLIImpl);
            
        } catch (Exception e) {
            
            waitingHandler.appendReport(
                    "An error occurred while running the command line. " + getLogFileMessage(), 
                    true, 
                    true
            );
            e.printStackTrace();
        
        }

        try {
            
            TempFilesManager.deleteTempFolders();
        
        } catch (Exception e) {
        
            waitingHandler.appendReport(
                    "An error occurred while deleting the temp folder. " + getLogFileMessage(), 
                    true, 
                    true
            );
            e.printStackTrace();
        
        }

        return null;
        
    }

    /**
     * SearchCLI header message when printing the usage.
     */
    private static String getHeader() {
        return System.getProperty("line.separator")
                + "SearchCLI searches spectrum files according to search parameters using multiple search engines." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Spectra must be provided either as mgf or mzML." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "The identification parameters can be provided as a file as saved from the GUI or generated using the IdentificationParametersCLI." + System.getProperty("line.separator")
                + "See https://compomics.github.io/projects/compomics-utilities/wiki/IdentificationParametersCLI.html for more details." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "For further help see https://compomics.github.io/projects/searchgui.html and https://compomics.github.io/projects/searchgui/wiki/SearchCLI.html." + System.getProperty("line.separator")
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
     * Redirects the error stream to the SearchGUI.log of a given folder.
     *
     * @param aLogFolder the folder where to save the log
     */
    public static void redirectErrorStream(File aLogFolder) {

        logFolder = aLogFolder;

        try {
            aLogFolder.mkdirs();
            File file = new File(aLogFolder, "SearchGUI.log");
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
     * Returns the "see the log file" message. With the path if available.
     *
     * @return the "see the log file" message
     */
    public static String getLogFileMessage() {
        if (logFolder == null) {
            return "Please see the SearchGUI log file.";
        } else {
            return "Please see the SearchGUI log file: " + logFolder.getAbsolutePath() + File.separator + "SearchGUI.log";
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
