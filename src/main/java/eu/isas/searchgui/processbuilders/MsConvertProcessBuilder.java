package eu.isas.searchgui.processbuilders;

import com.compomics.software.CommandLineUtils;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.massspectrometry.proteowizard.MsConvertParameters;
import com.compomics.util.experiment.massspectrometry.proteowizard.MsFormat;
import com.compomics.util.experiment.massspectrometry.proteowizard.ProteoWizardFilter;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Process builder to run msconvert.
 *
 * @author Marc Vaudel
 */
public class MsConvertProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The raw file to convert.
     */
    private File rawFile;
    /**
     * The destination folder where to save the output files.
     */
    private File destinationFolder;
    /**
     * The conversion parameters.
     */
    private MsConvertParameters msConvertParameters;
    /**
     * Boolean indicating whether the progress should be displayed. "Verbose"
     * mode of msconvert.
     */
    private static boolean displayProgress = true;

    /**
     * Constructor for the process builder.
     *
     * @param waitingHandler the waiting handler
     * @param rawFile the raw file to convert
     * @param destinationFolder the destination folder
     * @param msConvertParameters the msconvert parameters
     * @param exceptionHandler the handler of exceptions
     *
     * @throws FileNotFoundException thrown if files cannot be found
     * @throws IOException thrown if there are problems accessing the files
     * @throws ClassNotFoundException thrown if a class cannot be found
     */
    public MsConvertProcessBuilder(WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, File rawFile, File destinationFolder, MsConvertParameters msConvertParameters)
            throws FileNotFoundException, IOException, ClassNotFoundException {

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        this.rawFile = rawFile;
        this.destinationFolder = destinationFolder;
        this.msConvertParameters = msConvertParameters;

        setUpProcessBuilder();
    }

    /**
     * This method sets the process builder in the parent class.
     *
     * @throws FileNotFoundException thrown if files cannot be found
     * @throws IOException thrown if there are problems accessing the files
     * @throws ClassNotFoundException thrown if a class cannot be found
     */
    private void setUpProcessBuilder() throws FileNotFoundException, IOException, ClassNotFoundException {

        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        String proteoWizardPath = utilitiesUserPreferences.getProteoWizardPath();

        if (proteoWizardPath == null) {
            throw new IllegalArgumentException("ProteoWizard path not set.");
        }

        File proteoWizardFolder = new File(proteoWizardPath, "msconvert");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(proteoWizardFolder));
        process_name_array.add(CommandLineUtils.getCommandLineArgument(rawFile));

        MsFormat msFormat = msConvertParameters.getMsFormat();
        if (msFormat == null) {
            msFormat = MsFormat.mgf;
        }
        process_name_array.add("--" + msFormat.commandLineOption);
        
        process_name_array.add("-o");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(destinationFolder));
        
        // set the name of the output file
        if (rawFile.getName().lastIndexOf(".") != -1) {
            process_name_array.add("--outfile");
            process_name_array.add(rawFile.getName().substring(0, rawFile.getName().lastIndexOf(".")) + msFormat.fileNameEnding);
        }

        if (displayProgress) {
            process_name_array.add("-v");
        }
        for (Integer filterId : msConvertParameters.getFilters()) {
            ProteoWizardFilter proteoWizardFilter = ProteoWizardFilter.getFilter(filterId);
            if (proteoWizardFilter == null) {
                throw new IllegalArgumentException("Filter of index " + filterId + " not recognized.");
            }
            String commandLine = "\"" + proteoWizardFilter.name;
            String value = msConvertParameters.getValue(filterId);
            if (value != null) {
                commandLine += " " + value;
            }
            commandLine += "\"";
            process_name_array.add("--filter");
            process_name_array.add(commandLine);
        }

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "msconvert command: ");

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
            waitingHandler.appendReport("Processing " + rawFile.getName() + " with msconvert.", true, true);
            waitingHandler.appendReportEndLine();
            super.startProcess();
        }
    }

    @Override
    public String getType() {
        return "msconvert";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return rawFile.getName();
    }
}
