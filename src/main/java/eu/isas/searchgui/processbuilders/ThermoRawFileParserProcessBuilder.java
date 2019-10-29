package eu.isas.searchgui.processbuilders;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.mass_spectrometry.thermo_raw_file_parser.ThermoRawFileParserParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Process builder to run ThermoRawFileParser.
 *
 * @author Harald Barsnes
 */
public class ThermoRawFileParserProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The raw file to convert.
     */
    private File rawFile;
    /**
     * The destination folder where to save the output files.
     */
    private File destinationFolder;
    /**
     * The ThermoRawFileParser folder.
     */
    private File thermoRawFileParserFolder;
    /**
     * The ThermoRawFileParser parameters.
     */
    private ThermoRawFileParserParameters thermoRawFileParserParameters;

    /**
     * Constructor for the process builder.
     *
     * @param thermoRawFileParserFolder the ThermoRawFileParser folder
     * @param rawFile the raw file to convert
     * @param destinationFolder the destination folder
     * @param thermoRawFileParserParameters the ThermoRawFileParser parameters
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     *
     * @throws IOException thrown if there are problems accessing the files
     * @throws ClassNotFoundException thrown if a class cannot be found
     */
    public ThermoRawFileParserProcessBuilder(File thermoRawFileParserFolder, File rawFile, File destinationFolder, ThermoRawFileParserParameters thermoRawFileParserParameters, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler)
            throws IOException, ClassNotFoundException {

        this.rawFile = rawFile;
        this.destinationFolder = destinationFolder;
        this.thermoRawFileParserFolder = thermoRawFileParserFolder;
        this.thermoRawFileParserParameters = thermoRawFileParserParameters;
        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;

        setUpProcessBuilder();
    }

    /**
     * This method sets the process builder in the parent class.
     *
     * @throws IOException thrown if there are problems accessing the files
     * @throws ClassNotFoundException thrown if a class cannot be found
     */
    private void setUpProcessBuilder() throws IOException, ClassNotFoundException {

        // clear the previous process
        process_name_array.clear();

        // use mono if not on windows
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        if (!operatingSystem.contains("windows")) {
            String monoPath = "mono";

            // modern mac os x versions need a specific mono path
            if (operatingSystem.contains("mac os x")) {
                StringTokenizer versionTokens = new StringTokenizer(System.getProperty("os.version"), ".");
                if (versionTokens.countTokens() > 1) {
                    int mainVersion = new Integer(versionTokens.nextToken());
                    int subversion = new Integer(versionTokens.nextToken());
                    if (mainVersion >= 10 && subversion >= 11) {
                        monoPath = "/Library/Frameworks/Mono.framework/Versions/Current/bin/mono";
                    }
                }
            }
            
            process_name_array.add(monoPath);
        }

        // full path to executable
        File thermoRawFileParserExecutable = new File(thermoRawFileParserFolder, "ThermoRawFileParser.exe");
        thermoRawFileParserExecutable.setExecutable(true);

        // add the executable
        process_name_array.add(thermoRawFileParserExecutable.getAbsolutePath());

        // add the conversion parameters
        process_name_array.add("-i=" + rawFile.getAbsolutePath());
        process_name_array.add("-o=" + destinationFolder.getAbsolutePath());
        process_name_array.add("-f=" + thermoRawFileParserParameters.getOutputFormat().index);
        if (!thermoRawFileParserParameters.isPeackPicking()) {
            process_name_array.add("-p");
        }
        process_name_array.add("-e");

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "ThermoRawFileParser command: ");

        for (Object current_entry : process_name_array) {
            System.out.print(current_entry + " ");
        }

        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);

        // set error out and std out to same stream
        pb.redirectErrorStream(true);
    }

    @Override
    public void startProcess() throws IOException {
        if (!waitingHandler.isRunCanceled()) {
            waitingHandler.appendReport("Processing " + rawFile.getName() + " with ThermoRawFileParser.", true, true);
            waitingHandler.appendReportEndLine();
            super.startProcess();
        }
    }

    @Override
    public String getType() {
        return "ThermoRawFileParser";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return rawFile.getName();
    }
}
