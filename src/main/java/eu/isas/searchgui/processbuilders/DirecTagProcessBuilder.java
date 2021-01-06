package eu.isas.searchgui.processbuilders;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.DirecTagParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.util.ArrayList;

/**
 * This class will set up and start a process to run DirecTag.
 *
 * @author Thilo Muth
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class DirecTagProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * Title of the DirecTag executable.
     */
    public static final String EXECUTABLE_FILE_NAME = "directag";
    /**
     * The spectrumFile file.
     */
    private File spectrumFile;
    /**
     * The post-translational modifications factory.
     */
    private ModificationFactory modificationFactory = ModificationFactory.getInstance();
    /**
     * The DirecTag modification index.
     */
    private int modIndex = 0;

    /**
     * Constructor.
     *
     * @param exeFolder the path to the executable
     * @param spectrumFile the spectrum file
     * @param nThreads the number of threads
     * @param outputFolder the output folder
     * @param searchParameters the search parameters
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the exception handler
     */
    public DirecTagProcessBuilder(
            File exeFolder,
            File spectrumFile,
            int nThreads,
            File outputFolder,
            SearchParameters searchParameters,
            WaitingHandler waitingHandler,
            ExceptionHandler exceptionHandler
    ) {

        this.spectrumFile = spectrumFile;
        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;

        // get the DirecTag specific parameters
        DirecTagParameters direcTagParameters = (DirecTagParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex());

        // full path to executable
        process_name_array.add(exeFolder.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        process_name_array.trimToSize();

        // link to the spectrum file
        process_name_array.add(spectrumFile.getAbsolutePath());

        // number of cores
        process_name_array.add("-cpus");
        process_name_array.add(Integer.toString(nThreads));

        // add fixed mods
        String fixedModsAsString = "";
        ArrayList<String> fixedModifications = searchParameters.getModificationParameters().getFixedModifications();
        for (String modName : fixedModifications) {
            Modification modification = modificationFactory.getModification(modName);
            if (modification.getModificationType() == ModificationType.modaa) {
                fixedModsAsString += getFixedModificationFormattedForDirecTag(modName);
            }
        }
        fixedModsAsString = fixedModsAsString.trim();
        if (!fixedModsAsString.isEmpty()) {
            process_name_array.add("-StaticMods");
            process_name_array.add(CommandLineUtils.getQuoteType() + fixedModsAsString + CommandLineUtils.getQuoteType());
        }

        // add variable mods
        ArrayList<String> utilitiesModifications = new ArrayList<>();
        String variableModsAsString = "";
        ArrayList<String> variableModifications = searchParameters.getModificationParameters().getVariableModifications();
        for (String modName : variableModifications) {
            Modification modification = modificationFactory.getModification(modName);
            if (modification.getModificationType() == ModificationType.modaa) {
                variableModsAsString += getVariableModificationFormattedForDirecTag(modName, utilitiesModifications);
            }
        }
        variableModsAsString = variableModsAsString.trim();
        if (!variableModsAsString.isEmpty()) {
            process_name_array.add("-DynamicMods");
            process_name_array.add(CommandLineUtils.getQuoteType() + variableModsAsString + CommandLineUtils.getQuoteType());
            direcTagParameters.setModifications(utilitiesModifications);
        }

        // fragment tolerance
        double tolerance = searchParameters.getFragmentIonAccuracy();
        if (searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.PPM) {
            tolerance = IdentificationParameters.getDaTolerance(tolerance, 1000); //@TODO: make the reference mass a user parameter?
        }
        process_name_array.add("-FragmentMzTolerance");
        process_name_array.add(String.valueOf(tolerance));

        // precursor tolerance
        tolerance = searchParameters.getPrecursorAccuracy();
        if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.PPM) {
            tolerance = IdentificationParameters.getDaTolerance(tolerance, 1000); //@TODO: make the reference mass a user parameter?
        }
        process_name_array.add("-PrecursorMzTolerance");
        process_name_array.add(String.valueOf(tolerance));

        // tag length
        process_name_array.add("-TagLength");
        process_name_array.add(String.valueOf(direcTagParameters.getTagLength()));

        // maximum tag count
        process_name_array.add("-MaxTagCount");
        process_name_array.add(String.valueOf(direcTagParameters.getMaxTagCount()));

        // maximum peak count
        process_name_array.add("-MaxPeakCount");
        process_name_array.add("100");
        // process_name_array.add(String.valueOf(direcTagParameters.getMaxPeakCount())); // @TODO: figure out why adding this parameter seems to make DirecTag very slow, even when the default value is used
        // number of intensity classes
        process_name_array.add("-NumIntensityClasses");
        process_name_array.add(String.valueOf(direcTagParameters.getNumIntensityClasses()));

        // adjust precursor mass
        process_name_array.add("-TicCutoffPercentage");
        process_name_array.add(String.valueOf(direcTagParameters.getTicCutoffPercentage() / 100));

        // adjust precursor mass
        process_name_array.add("-AdjustPrecursorMass");
        process_name_array.add(String.valueOf(direcTagParameters.isAdjustPrecursorMass()));

        // min precursor adjustment
        process_name_array.add("-MinPrecursorAdjustment");
        process_name_array.add(String.valueOf(direcTagParameters.getMinPrecursorAdjustment()));

        // max precursor adjustment
        process_name_array.add("-MaxPrecursorAdjustment");
        process_name_array.add(String.valueOf(direcTagParameters.getMaxPrecursorAdjustment()));

        // precursor adjustment step
        process_name_array.add("-PrecursorAdjustmentStep");
        process_name_array.add(String.valueOf(direcTagParameters.getPrecursorAdjustmentStep()));

        // number of charge states
        process_name_array.add("-NumChargeStates");
        process_name_array.add(String.valueOf(direcTagParameters.getNumChargeStates()));

        // the output suffix
        if (!direcTagParameters.getOutputSuffix().trim().isEmpty()) {
            process_name_array.add("-OutputSuffix");
            process_name_array.add(direcTagParameters.getOutputSuffix());
        }

        // use charge state from spectrum
        process_name_array.add("-UseChargeStateFromMS");
        process_name_array.add(String.valueOf(direcTagParameters.isUseChargeStateFromMS()));

        // duplicate spectra per charge
        process_name_array.add("-DuplicateSpectra");
        process_name_array.add(String.valueOf(direcTagParameters.isDuplicateSpectra()));

        // deisotoping mode
        process_name_array.add("-DeisotopingMode");
        process_name_array.add(String.valueOf(direcTagParameters.getDeisotopingMode()));

        // isotope mz tolerance
        process_name_array.add("-IsotopeMzTolerance");
        process_name_array.add(String.valueOf(direcTagParameters.getIsotopeMzTolerance()));

        // complement mz tolerance
        process_name_array.add("-IsotopeMzTolerance");
        process_name_array.add(String.valueOf(direcTagParameters.getComplementMzTolerance()));

        // max number of variable modifications
        process_name_array.add("-MaxDynamicMods");
        process_name_array.add(String.valueOf(direcTagParameters.getMaxDynamicMods()));

        // intensity score weight
        process_name_array.add("-IntensityScoreWeight");
        process_name_array.add(String.valueOf(direcTagParameters.getIntensityScoreWeight()));

        // mz fidelity score weight
        process_name_array.add("-MzFidelityScoreWeight");
        process_name_array.add(String.valueOf(direcTagParameters.getMzFidelityScoreWeight()));

        // complement score weight
        process_name_array.add("-ComplementScoreWeight");
        process_name_array.add(String.valueOf(direcTagParameters.getComplementScoreWeight()));

        // set the output directory
        process_name_array.add("-workdir");
        process_name_array.add(outputFolder.getAbsolutePath());

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "directag command: ");
        for (Object element : process_name_array) {
            System.out.print(element + " ");
        }
        System.out.println(System.getProperty("line.separator"));;

        pb = new ProcessBuilder(process_name_array);
        pb.directory(exeFolder);

        // set error out and std out to same stream
        pb.redirectErrorStream(true);
    }

    /**
     * Get the given modification as a string in the DirecTag format.
     *
     * @param modificationName the utilities name of the modification
     * @param utilitiesModifications the list of utilities modifications, index
     * is the index used in the DirecTag output (note that the same modification
     * may occur more than once in the list as multiple DirecTag modification
     * can map to the same utilities modification)
     * @return the given modification as a string in the DirecTag format
     */
    private String getVariableModificationFormattedForDirecTag(
            String modificationName, 
            ArrayList<String> utilitiesModifications
    ) {

        Modification tempModification = modificationFactory.getModification(modificationName);
        String modificationAsString = "";

        // get the targeted amino acids
        if (tempModification.getPattern() != null && !tempModification.getPattern().getAminoAcidsAtTarget().isEmpty()) {
            for (Character aa : tempModification.getPattern().getAminoAcidsAtTarget()) {
                modificationAsString += " " + aa + " " + modIndex++ + " " + tempModification.getRoundedMass();
                utilitiesModifications.add(modificationName);
            }
        }

        // return the modification
        return modificationAsString;
    }

    /**
     * Get the given modification as a string in the DirecTag format.
     *
     * @param modificationName the utilities name of the modification
     * @return the given modification as a string in the DirecTag format
     */
    private String getFixedModificationFormattedForDirecTag(String modificationName) {

        Modification tempModification = modificationFactory.getModification(modificationName);
        String modificationAsString = "";

        // get the targeted amino acids
        if (tempModification.getPattern() != null && !tempModification.getPattern().getAminoAcidsAtTarget().isEmpty()) {
            for (Character aa : tempModification.getPattern().getAminoAcidsAtTarget()) {
                modificationAsString += " " + aa + " " + tempModification.getRoundedMass();
            }
        }

        // return the modification
        return modificationAsString;
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return spectrumFile.getName();
    }

    @Override
    public String getType() {
        return "DirecTag";
    }
}
