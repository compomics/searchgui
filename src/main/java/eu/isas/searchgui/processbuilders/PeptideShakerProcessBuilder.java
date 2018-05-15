package eu.isas.searchgui.processbuilders;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.software.CompomicsWrapper;
import com.compomics.util.Util;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.tools.ProcessingParameters;
import com.compomics.util.parameters.UtilitiesUserParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This process builder runs PeptideShaker.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PeptideShakerProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The reference of the project.
     */
    private final String reference;
    /**
     * The spectrum files.
     */
    private final ArrayList<File> spectrumFiles;
    /**
     * The identification files.
     */
    private final ArrayList<File> identificationFiles;
    /**
     * The file where to store the search parameters.
     */
    private final File identificationParametersFile;
    /**
     * The cpsx file.
     */
    private final File cpsFile;
    /**
     * Boolean indicating whether the results shall be displayed in
     * PeptideShaker.
     */
    private final boolean showGuiProgress;
    /**
     * Indicates whether the mgf and FASTA file should be included in the
     * output.
     */
    private final boolean includeData;

    /**
     * Constructor for the process builder.
     *
     * @param waitingHandler the waiting handler
     * @param reference the name of the experiment
     * @param spectrumFiles the spectrum files
     * @param cpsFile the cpsx file
     * @param identificationFiles the search engines result files
     * @param showGuiProgress a boolean indicating whether the progress shall be
     * displayed in a GUI
     * @param processingParameters the processing parameters
     * @param includeData Indicates whether the mgf and FASTA file should be
     * included in the output
     * @param exceptionHandler the handler of exceptions
     * @param identificationParametersFile the file where to save the search parameters
     *
     * @throws IOException thrown if there are problems accessing the files
     * @throws ClassNotFoundException thrown if a class cannot be found
     */
    public PeptideShakerProcessBuilder(WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, String reference,
            ArrayList<File> spectrumFiles, ArrayList<File> identificationFiles, File identificationParametersFile, 
            File cpsFile, boolean showGuiProgress,
            ProcessingParameters processingParameters, boolean includeData)
            throws IOException, ClassNotFoundException {

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        this.reference = reference;
        this.spectrumFiles = spectrumFiles;
        this.identificationParametersFile = identificationParametersFile;
        this.identificationFiles = identificationFiles;
        this.cpsFile = cpsFile;
        this.showGuiProgress = showGuiProgress;
        this.includeData = includeData;

        setUpProcessBuilder();
    }

    /**
     * This method sets the process builder in the parent class.
     *
     * @throws IOException thrown if there are problems accessing the files
     * @throws ClassNotFoundException thrown if a class cannot be found
     */
    private void setUpProcessBuilder() throws IOException, ClassNotFoundException {

        try {
            
            UtilitiesUserParameters utilitiesUserParameters = UtilitiesUserParameters.loadUserParameters();
            CompomicsWrapper wrapper = new CompomicsWrapper();

            ArrayList<String> javaHomeAndOptions = wrapper.getJavaHomeAndOptions(utilitiesUserParameters.getPeptideShakerPath());

            // set java home
            process_name_array.add(javaHomeAndOptions.get(0));

            // set java options
            for (int i = 1; i < javaHomeAndOptions.size(); i++) {
                process_name_array.add(javaHomeAndOptions.get(i));
            }

            process_name_array.add("-cp");
            process_name_array.add(new File(utilitiesUserParameters.getPeptideShakerPath()).getName());
            process_name_array.add("eu.isas.peptideshaker.cmd.PeptideShakerCLI");
            process_name_array.add("-reference");
            process_name_array.add(reference);
            process_name_array.add("-identification_files");
            process_name_array.add(CommandLineUtils.getCommandLineArgument(identificationFiles));
            process_name_array.add("-spectrum_files");
            process_name_array.add(CommandLineUtils.getCommandLineArgument(spectrumFiles));
            process_name_array.add("-id_params");
            process_name_array.add(CommandLineUtils.getCommandLineArgument(identificationParametersFile));
            process_name_array.add("-out");
            process_name_array.add(CommandLineUtils.getCommandLineArgument(cpsFile));
            if (includeData) {
                File zipFile = new File(cpsFile.getParentFile(), Util.removeExtension(cpsFile.getName()) + ".zip");
                process_name_array.add("-zip");
                process_name_array.add(CommandLineUtils.getCommandLineArgument(zipFile));
            }

            // show gui progress
            if (showGuiProgress) {
                process_name_array.add("-gui");
                process_name_array.add("1");
            }

            process_name_array.trimToSize();

            // print the command to the log file
            System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "PeptideShaker command: ");

            for (Object current_entry : process_name_array) {
                System.out.print(current_entry + " ");
            }

            System.out.println(System.getProperty("line.separator"));

            pb = new ProcessBuilder(process_name_array);

            File psFolder = new File(utilitiesUserParameters.getPeptideShakerPath()).getParentFile();
            pb.directory(psFolder);

            // set error out and std out to same stream
            pb.redirectErrorStream(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getType() {
        return "PeptideShaker";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return reference;
    }
}
