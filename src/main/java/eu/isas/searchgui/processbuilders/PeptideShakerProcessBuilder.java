package eu.isas.searchgui.processbuilders;

import com.compomics.software.CommandLineUtils;
import com.compomics.software.CompomicsWrapper;
import com.compomics.util.Util;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.preferences.GenePreferences;
import com.compomics.util.experiment.identification.filtering.PeptideAssumptionFilter;
import com.compomics.util.preferences.IdMatchValidationPreferences;
import com.compomics.util.preferences.PTMScoringPreferences;
import com.compomics.util.preferences.PSProcessingPreferences;
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
     * The search parameters.
     */
    private SearchParameters searchParameters;
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
     * The ID filters.
     */
    private PeptideAssumptionFilter idFilter;
    /**
     * The processing preferences.
     */
    private PSProcessingPreferences processingPreferences;
    /**
     * The PTM scoring preferences.
     */
    private PTMScoringPreferences ptmScoringPreferences;
    /**
     * The id match validation preferences.
     */
    private IdMatchValidationPreferences idMatchValidationPreferences = new IdMatchValidationPreferences();
    /**
     * The gene preferences.
     */
    private GenePreferences genePreferences;
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
     * @param searchParameters the search parameters
     * @param cpsFile the cpsx file
     * @param identificationFiles the search engines result files
     * @param showGuiProgress a boolean indicating whether the progress shall be
     * displayed in a GUI
     * @param idFilter the id filters
     * @param processingPreferences the processing preferences
     * @param ptmScoringPreferences the PTM scoring preferences
     * @param idMatchValidationPreferences the id match validation preferences
     * @param genePreferences the gene preferences
     * @param includeData Indicates whether the mgf and FASTA file should be
     * included in the output
     *
     * @throws FileNotFoundException thrown if files cannot be found
     * @throws IOException thrown if there are problems accessing the files
     * @throws ClassNotFoundException thrown if a class cannot be found
     */
    public PeptideShakerProcessBuilder(WaitingHandler waitingHandler, String experiment, String sample, Integer replicate,
            ArrayList<File> spectrumFiles, ArrayList<File> identificationFiles, SearchParameters searchParameters,
            File cpsFile, boolean showGuiProgress, PeptideAssumptionFilter idFilter,
            PSProcessingPreferences processingPreferences, PTMScoringPreferences ptmScoringPreferences, IdMatchValidationPreferences idMatchValidationPreferences, GenePreferences genePreferences, boolean includeData)
            throws FileNotFoundException, IOException, ClassNotFoundException {

        this.waitingHandler = waitingHandler;
        this.experiment = experiment;
        this.sample = sample;
        this.replicate = replicate;
        this.spectrumFiles = spectrumFiles;
        this.searchParameters = searchParameters;
        this.identificationFiles = identificationFiles;
        this.cpsFile = cpsFile;
        this.showGuiProgress = showGuiProgress;
        this.idFilter = idFilter;
        this.processingPreferences = processingPreferences;
        this.ptmScoringPreferences = ptmScoringPreferences;
        this.idMatchValidationPreferences = idMatchValidationPreferences;
        this.genePreferences = genePreferences;
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

            File parametersFile;
            if (searchParameters.getParametersFile() != null && searchParameters.getParametersFile().exists()) {
                parametersFile = searchParameters.getParametersFile();
            } else {
                parametersFile = new File(cpsFile.getParent(), "PS_CLI.par");
                SearchParameters.saveIdentificationParameters(searchParameters, parametersFile);
            }

            process_name_array.add("-id_params");
            process_name_array.add(CommandLineUtils.getCommandLineArgument(parametersFile));
            process_name_array.add("-out");
            process_name_array.add(CommandLineUtils.getCommandLineArgument(cpsFile));
            if (includeData) {
                File zipFile = new File(cpsFile.getParentFile(), Util.removeExtension(cpsFile.getName()) + ".zip");
                process_name_array.add("-zip");
                process_name_array.add(CommandLineUtils.getCommandLineArgument(zipFile));
            }

            // @TODO: add more test for if default parameter values are used and exclude the parameters
            // add the processing preferences
            process_name_array.add("-protein_FDR");
            process_name_array.add("" + idMatchValidationPreferences.getDefaultProteinFDR());
            process_name_array.add("-peptide_FDR");
            process_name_array.add("" + idMatchValidationPreferences.getDefaultPeptideFDR());
            process_name_array.add("-psm_FDR");
            process_name_array.add("" + idMatchValidationPreferences.getDefaultPsmFDR());

            // ptm scoring options
            if (!ptmScoringPreferences.isEstimateFlr()) {
                process_name_array.add("-ptm_threshold");
                process_name_array.add("" + ptmScoringPreferences.getFlrThreshold());
            }
            if (ptmScoringPreferences.isProbabilitsticScoreCalculation()) {
                process_name_array.add("-ptm_score");
                process_name_array.add(ptmScoringPreferences.getSelectedProbabilisticScore().getId() + "");
            }
            if (ptmScoringPreferences.isProbabilisticScoreNeutralLosses()) {
                process_name_array.add("-score_neutral_losses");
                process_name_array.add("1");
            }

            // protein fraction mw confidence
            process_name_array.add("-protein_fraction_mw_confidence");
            process_name_array.add("" + processingPreferences.getProteinConfidenceMwPlots());

            // add the gene preferences
            if (genePreferences.getCurrentSpecies() != null) {
                process_name_array.add("-species");
                process_name_array.add(CommandLineUtils.getQuoteType() + genePreferences.getCurrentSpecies() + CommandLineUtils.getQuoteType());
                process_name_array.add("-species_type");
                process_name_array.add(CommandLineUtils.getQuoteType() + genePreferences.getCurrentSpeciesType() + CommandLineUtils.getQuoteType());
            }

            // add the filters
            process_name_array.add("-min_peptide_length");
            process_name_array.add("" + idFilter.getMinPepLength());
            process_name_array.add("-max_peptide_length");
            process_name_array.add("" + idFilter.getMaxPepLength());
            process_name_array.add("-max_precursor_error");
            process_name_array.add("" + idFilter.getMaxMzDeviation());
            if (!idFilter.isIsPpm()) {
                process_name_array.add("-max_precursor_error_type");
                process_name_array.add("1");
            }
            if (!idFilter.removeUnknownPTMs()) {
                process_name_array.add("-exclude_unknown_ptms");
                process_name_array.add("0");
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
        return "PeptideShaker Process";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return experiment + " (sample: " + sample + ", replicate: " + replicate + ")";
    }
}
