package eu.isas.searchgui.processbuilders;

import com.compomics.software.CommandLineUtils;
import com.compomics.software.CompomicsWrapper;
import com.compomics.util.Util;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.identification.parameters_cli.IdentificationParametersCLIParams;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.ProcessingPreferences;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import java.io.File;
import java.io.FileNotFoundException;
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
     * The name of the experiment.
     */
    private String experiment;
    /**
     * The name of the sample.
     */
    private String sample;
    /**
     * The number of the replicate.
     */
    private Integer replicate;
    /**
     * The spectrum files.
     */
    private ArrayList<File> spectrumFiles;
    /**
     * The identification files.
     */
    private ArrayList<File> identificationFiles;
    /**
     * The identification parameters.
     */
    private IdentificationParameters identificationParameters;
    /**
     * The file where to store the search parameters.
     */
    private File identificationParametersFile;
    /**
     * The cpsx file.
     */
    private File cpsFile;
    /**
     * Boolean indicating whether the results shall be displayed in
     * PeptideShaker.
     */
    private boolean showGuiProgress;
    /**
     * Indicates whether the mgf and FASTA file should be included in the
     * output.
     */
    private boolean includeData;

    /**
     * Constructor for the process builder.
     *
     * @param waitingHandler the waiting handler
     * @param experiment the name of the experiment
     * @param sample the name of the sample
     * @param replicate the replicate number
     * @param spectrumFiles the spectrum files
     * @param identificationParameters the identification parameters
     * @param cpsFile the cpsx file
     * @param identificationFiles the search engines result files
     * @param showGuiProgress a boolean indicating whether the progress shall be
     * displayed in a GUI
     * @param processingPreferences the processing preferences
     * @param includeData Indicates whether the mgf and FASTA file should be
     * included in the output
     * @param exceptionHandler the handler of exceptions
     * @param identificationParametersFile the file where to save the search parameters
     *
     * @throws FileNotFoundException thrown if files cannot be found
     * @throws IOException thrown if there are problems accessing the files
     * @throws ClassNotFoundException thrown if a class cannot be found
     */
    public PeptideShakerProcessBuilder(WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, String experiment, String sample, Integer replicate,
            ArrayList<File> spectrumFiles, ArrayList<File> identificationFiles, IdentificationParameters identificationParameters, File identificationParametersFile, 
            File cpsFile, boolean showGuiProgress,
            ProcessingPreferences processingPreferences, boolean includeData)
            throws FileNotFoundException, IOException, ClassNotFoundException {

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        this.experiment = experiment;
        this.sample = sample;
        this.replicate = replicate;
        this.spectrumFiles = spectrumFiles;
        this.identificationParameters = identificationParameters;
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
     * @throws FileNotFoundException thrown if files cannot be found
     * @throws IOException thrown if there are problems accessing the files
     * @throws ClassNotFoundException thrown if a class cannot be found
     */
    private void setUpProcessBuilder() throws FileNotFoundException, IOException, ClassNotFoundException {

        try {
            UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
            CompomicsWrapper wrapper = new CompomicsWrapper();

            ArrayList<String> javaHomeAndOptions = wrapper.getJavaHomeAndOptions(utilitiesUserPreferences.getPeptideShakerPath());

            // set java home
            process_name_array.add(javaHomeAndOptions.get(0));

            // set java options
            for (int i = 1; i < javaHomeAndOptions.size(); i++) {
                process_name_array.add(javaHomeAndOptions.get(i));
            }

            process_name_array.add("-cp");
            process_name_array.add(new File(utilitiesUserPreferences.getPeptideShakerPath()).getName());
            process_name_array.add("eu.isas.peptideshaker.cmd.PeptideShakerCLI");
            process_name_array.add("-experiment");
            process_name_array.add(experiment);
            process_name_array.add("-sample");
            process_name_array.add(sample);
            process_name_array.add("-replicate");
            process_name_array.add(replicate + "");
            process_name_array.add("-identification_files");
            process_name_array.add(CommandLineUtils.getCommandLineArgument(identificationFiles));
            process_name_array.add("-spectrum_files");
            process_name_array.add(CommandLineUtils.getCommandLineArgument(spectrumFiles));
            process_name_array.add(IdentificationParametersCLIParams.IDENTIFICATION_PARAMETERS.id);
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

            File psFolder = new File(utilitiesUserPreferences.getPeptideShakerPath()).getParentFile();
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
        return experiment + " (sample: " + sample + ", replicate: " + replicate + ")";
    }
}
