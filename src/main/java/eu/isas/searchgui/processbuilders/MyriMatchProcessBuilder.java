package eu.isas.searchgui.processbuilders;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.MyriMatchParameters;
import com.compomics.util.waiting.WaitingHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class will set up and start a process to perform a MyriMatch search.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class MyriMatchProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The post translational modifications factory.
     */
    private ModificationFactory modificationFactory = ModificationFactory.getInstance();
    /**
     * The name of the MyriMatch executable.
     */
    public static final String EXECUTABLE_FILE_NAME = "myrimatch";
    /**
     * The spectrum file to search.
     */
    private File spectrumFile;
    /**
     * The MyriMatch parameters.
     */
    private MyriMatchParameters myriMatchParameters;
    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The status update frequency in seconds in the waiting handler.
     */
    private final int statusUpdateFrecuencyInSeconds = 10;

    /**
     * Constructor.
     *
     * @param myriMatchDirectory directory location of MyriMatch executable
     * @param mgfFile the file containing the spectra
     * @param fastaFile the FASTA file
     * @param outputFolder folder where to output the results
     * @param searchParameters the search parameters
     * @param exceptionHandler the handler of exceptions
     * @param waitingHandler the waiting handler
     * @param nThreads the number of threads to use
     */
    public MyriMatchProcessBuilder(File myriMatchDirectory, File mgfFile, File fastaFile, File outputFolder,
            SearchParameters searchParameters, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, int nThreads) {

        this.searchParameters = searchParameters;
        this.exceptionHandler = exceptionHandler;
        myriMatchParameters = (MyriMatchParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex());

        this.waitingHandler = waitingHandler;
        this.spectrumFile = mgfFile;

        // make sure that the myrimatch file is executable
        File myriMatchExecutable = new File(myriMatchDirectory.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        myriMatchExecutable.setExecutable(true);

        // add the myrimatch executable
        process_name_array.add(myriMatchDirectory.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);

        // set the number of threads to use
        process_name_array.add("-cpus");
        process_name_array.add(Integer.toString(nThreads));

        // add the database
        process_name_array.add("-ProteinDatabase");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(fastaFile));

        // add the spectrum file
        process_name_array.add(CommandLineUtils.getCommandLineArgument(spectrumFile));

        // set the output format to mzIdentML
        process_name_array.add("-OutputFormat");
        if (myriMatchParameters.getOutputFormat().equalsIgnoreCase("mzIdentML")) {
            process_name_array.add("mzIdentML");
        } else {
            process_name_array.add("pepXML");
        }

        // set the output folder
        process_name_array.add("-workdir");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(outputFolder));

        // add an suffix to be able to recognize the output files
        process_name_array.add("-OutputSuffix");
        process_name_array.add(".myrimatch");

        // switch of the creation of decoys
        process_name_array.add("-DecoyPrefix");
        process_name_array.add("\"\"");

        // set the min/max peptide lengths
        process_name_array.add("-MinPeptideLength");
        process_name_array.add("" + myriMatchParameters.getMinPeptideLength());
        process_name_array.add("-MaxPeptideLength");
        process_name_array.add("" + myriMatchParameters.getMaxPeptideLength());

        // set the number of matches per spectrum
        process_name_array.add("-MaxResultRank");
        process_name_array.add("" + myriMatchParameters.getNumberOfSpectrumMatches());

        // remove the peak peaking, as mgfs are already peak picked
        process_name_array.add("-SpectrumListFilters");
        process_name_array.add("\"\"");

        // set the fragment ion tolerance
        process_name_array.add("-FragmentMzTolerance");
        String fragemtnTolerance = Double.toString(searchParameters.getFragmentIonAccuracy());
        if (searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.DA) {
            fragemtnTolerance += " daltons";
        } else {
            fragemtnTolerance += " ppm";
        }
        process_name_array.add("\"" + fragemtnTolerance + "\"");

        // set the mono precursor tolerance
        process_name_array.add("-MonoPrecursorMzTolerance");
        String precursorTolerance = Double.toString(searchParameters.getPrecursorAccuracy());
        if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.DA) {
            precursorTolerance += " daltons";
        } else {
            precursorTolerance += " ppm";
        }
        process_name_array.add("\"" + precursorTolerance + "\""); // note: the tolerance is used for +1 spectra

        // set the precusor accuracy to mono
        process_name_array.add("-PrecursorMzToleranceRule");
        process_name_array.add("\"" + "mono" + "\"");

        // filter the modifications and convert fixed to variable if needed
        HashMap<String, ArrayList<String>> filteredModifications = filterFixedModifications();

        // set the fixed modifications
        if (!filteredModifications.get("Fixed").isEmpty()) {
            process_name_array.add("-StaticMods");
            process_name_array.add(getFixedModificationsAsString(filteredModifications.get("Fixed")));
        }

        // set the variable modifications
        if (!filteredModifications.get("Variable").isEmpty()) {
            process_name_array.add("-DynamicMods");
            process_name_array.add(getVariableModificationsAsString(filteredModifications.get("Variable")));
        }

        // set the maximum number of variable modifications
        process_name_array.add("-MaxDynamicMods");
        process_name_array.add("" + myriMatchParameters.getMaxDynamicMods());

        // reduce the status update frequency
        process_name_array.add("-StatusUpdateFrequency");
        process_name_array.add("" + statusUpdateFrecuencyInSeconds);

        // set the maximum number of charges
        process_name_array.add("-NumChargeStates");
        process_name_array.add("" + searchParameters.getMaxChargeSearched()); // note that it is not possible to set the min charge

        // set the TicCutoffPercentage
        process_name_array.add("-TicCutoffPercentage");
        process_name_array.add("" + myriMatchParameters.getTicCutoffPercentage());

        // set the minimum precursor mass
        process_name_array.add("-MinPeptideMass");
        process_name_array.add("" + myriMatchParameters.getMinPrecursorMass());

        // set the maximum precursor mass
        process_name_array.add("-MaxPeptideMass");
        process_name_array.add("" + myriMatchParameters.getMaxPrecursorMass());

        // set the use of the smart plus three model
        process_name_array.add("-UseSmartPlusThreeModel");
        process_name_array.add("" + myriMatchParameters.getUseSmartPlusThreeModel());

        // set if a Sequest-like cross correlation (xcorr) score will be calculated 
        process_name_array.add("-ComputeXCorr");
        process_name_array.add("" + myriMatchParameters.getComputeXCorr());

        // set the number of intensity classes
        process_name_array.add("-NumIntensityClasses");
        process_name_array.add("" + myriMatchParameters.getNumIntensityClasses());

        // set the intensity class size multiplier
        process_name_array.add("-ClassSizeMultiplier");
        process_name_array.add("" + myriMatchParameters.getClassSizeMultiplier());

        // set the nubmer of batches per node
        process_name_array.add("-NumBatches");
        process_name_array.add("" + myriMatchParameters.getNumberOfBatches());

        // set the max peak count
        process_name_array.add("-MaxPeakCount");
        process_name_array.add("" + myriMatchParameters.getMaxPeakCount());

        // set the isotope correction range
        process_name_array.add("-MonoisotopeAdjustmentSet");
        boolean precursorsToleranceWide = false;
        if ((searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.DA
                && searchParameters.getPrecursorAccuracy() >= 0.2)
                || (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.PPM
                && searchParameters.getPrecursorAccuracy() >= 200)) {
            precursorsToleranceWide = true;
        }
        if (precursorsToleranceWide) {
            process_name_array.add("0"); // MonoisotopeAdjustmentSet should be set to 0 when the MonoPrecursorMzTolerance is wide
        } else {
            process_name_array.add("[" + searchParameters.getMinIsotopicCorrection()
                    + "," + searchParameters.getMaxIsotopicCorrection() + "]");
        }

        // set the fragmention rules
        process_name_array.add("-FragmentationAutoRule");
        process_name_array.add("false");
        process_name_array.add("-FragmentationRule"); // options: cid (b, y), etd (c, z*) or manual (a comma-separated list of [abcxyz] or z* (z+1), e.g. manual:b,y,z)
        if (myriMatchParameters.getFragmentationRule().equalsIgnoreCase("HCD")) {
            process_name_array.add("\"" + "manual:b,y" + "\""); // note: same ions as for cid
        } else {
            process_name_array.add("\"" + myriMatchParameters.getFragmentationRule().toLowerCase() + "\"");
        }

        // set the enzyme
        process_name_array.add("-CleavageRules");
        DigestionParameters digestionPreferences = searchParameters.getDigestionParameters();
        if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.unSpecific) {
            process_name_array.add("\"" + "Trypsin" + "\""); // trick to support unspecific cleavage, MinTerminiCleavages is set to 0 below instead
        } else {
            String myriMatchEnzyme = MyriMatchParameters.enzymeMapping(digestionPreferences);
            process_name_array.add("\"" + myriMatchEnzyme + "\"");
        }

        // set the minimum termini cleavages
        process_name_array.add("-MinTerminiCleavages");
        if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.unSpecific) {
            process_name_array.add("0");
        } else if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.wholeProtein) {
            process_name_array.add("0");
        } else {
            boolean semiSpecific = false;
            for (Enzyme enzyme : digestionPreferences.getEnzymes()) {
                if (digestionPreferences.getSpecificity(enzyme.getName()) == DigestionParameters.Specificity.semiSpecific
                        || digestionPreferences.getSpecificity(enzyme.getName()) == DigestionParameters.Specificity.specificCTermOnly
                        || digestionPreferences.getSpecificity(enzyme.getName()) == DigestionParameters.Specificity.specificNTermOnly) {
                    semiSpecific = true;
                    break;
                }
            }
            if (semiSpecific) {
                process_name_array.add("1"); // note that this overrides the MinTerminiCleavages setting from the user 
            } else {
                process_name_array.add("" + myriMatchParameters.getMinTerminiCleavages());
            }
        }

        // set the maximum missed cleavages
        if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.enzyme) {

            Integer missedCleavages = null;
            for (Enzyme enzyme : digestionPreferences.getEnzymes()) {
                int enzymeMissedCleavages = digestionPreferences.getnMissedCleavages(enzyme.getName());
                if (missedCleavages == null || enzymeMissedCleavages > missedCleavages) {
                    missedCleavages = enzymeMissedCleavages;
                }
            }

            if (missedCleavages != null) {
                process_name_array.add("-MaxMissedCleavages");
                process_name_array.add("" + missedCleavages);
            }
        }
        // advanced settings not used:
        //  ProteinSampleSize
        //  FragmentationAutoRule - automatically choose the fragmentation rule based on the activation type of each MSn spectrum
        //  ThreadCountMultiplier - multithreading database search setting
        //  UseMultipleProcessors - if true, each process will use all the processing units available on the system it is running on   
        //  SpectrumListFilters - filters applied to spectra as it is read in
        //      plus some more from the -dump option...
        //
        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "myrimatch command: ");

        for (Object element : process_name_array) {
            System.out.print(element + " ");
        }

        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);

        pb.directory(myriMatchDirectory);
        // set error out and std out to same stream
        pb.redirectErrorStream(true);
    }

    /**
     * Returns the fixed modification as a string in the MyriMatch format.
     *
     * @param fixedModifications the list of fixed modifications
     * @return the fixed modification as a string
     */
    private String getFixedModificationsAsString(ArrayList<String> fixedModifications) {

        String fixedModificationsAsString = "";

        // @TODO: is the code generic enough?
        for (String modName : fixedModifications) {

            Modification tempModification = modificationFactory.getModification(modName);

            if (tempModification.getModificationType() == ModificationType.modaa) {

                for (Character aa : tempModification.getPattern().getAminoAcidsAtTarget()) {

                    fixedModificationsAsString += aa + " " + tempModification.getRoundedMass() + " ";

                }
            } else {

                if (tempModification.getModificationType() == ModificationType.modn_peptide) { // peptide n term

                    fixedModificationsAsString += "( " + tempModification.getRoundedMass() + " ";

                } else if (tempModification.getModificationType() == ModificationType.modc_peptide) { // peptide c term 

                    fixedModificationsAsString += ") " + tempModification.getRoundedMass() + " ";

                }

                // note: the ptms below should have been converted to variable via filterFixedPtms()
//                else if (modificationType == PTM.MODN) { // protein n term
//                    // @TODO: treated as a peptide ptm, should be variable instead?
//                } else if (modificationType == PTM.MODNAA) { // protein n term specific amino acid
//                    // @TODO: not supported? should be variable instead?
//                } else if (modificationType == PTM.MODNPAA) { // peptide n term specifc amino acid
//                    // @TODO: not suported? should be variable instead?
//                } else if (supported == PTM.MODC) { // protein c term
//                    // @TODO: treated as a peptide ptm, should be variable instead?
//                } else if (modificationType == PTM.MODCAA) { // protein c term specific amino acid
//                    // @TODO: not supported? should be variable instead?
//                } else if (modificationType == PTM.MODCPAA) { // peptide c term specific amino acid
//                    // @TODO: not supported? should be variable instead?
//                }
            }
        }

        fixedModificationsAsString = fixedModificationsAsString.trim();
        fixedModificationsAsString = "\"" + fixedModificationsAsString + "\"";

        return fixedModificationsAsString;
    }

    /**
     * Returns the variable modification as a string in the MyriMatch format.
     *
     * @param variableModifications the list of variable modifications
     *
     * @throws IndexOutOfBoundsException if there are more variable
     * modifications than there are symbols to represent them
     *
     * @return the variable modification as a string
     */
    private String getVariableModificationsAsString(ArrayList<String> variableModifications) throws IndexOutOfBoundsException {

        String variableModificationsAsString = "";
        char[] symbols = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '^', '@', '|', '§', '+', '-', '&', '%', '='}; // @TODO: add more symbols?
        int symbolsCounter = 0;

        // more variable modifications than symbols to represent them?
        if (variableModifications.size() > symbols.length) {
            throw new IndexOutOfBoundsException("There are more variable modifications ("
                    + variableModifications.size() + ") than symbols to represent them in MyriMatch ("
                    + symbols.length + "). Search canceled.");
        }

        // @TODO: is the code generic enough?
        for (String modName : variableModifications) {

            Modification modification = modificationFactory.getModification(modName);

            String nTerm = "";
            String cTerm = "";

            // note: protein terminal ptms are handled as peptide terminal ptms and have to be removed in the post-processing
            switch (modification.getModificationType()) {
                case modn_peptide:
                case modn_protein:
                    nTerm = "(";
                    break;
                case modnaa_peptide:
                case modnaa_protein:
                    nTerm = "(!";
                    break;
                case modc_peptide:
                case modc_protein:
                case modcaa_peptide:
                case modcaa_protein:
                    cTerm = ")";
                    break;
                default:
                    break;
            }

            // get the targeted amino acids
            String aminoAcidsAtTarget = "";
            if (modification.getPattern() != null) {
                for (Character aa : modification.getPattern().getAminoAcidsAtTarget()) {
                    aminoAcidsAtTarget += aa;
                }
            }
            if (aminoAcidsAtTarget.length() > 1) {
                aminoAcidsAtTarget = "[" + aminoAcidsAtTarget + "]";
            }

            variableModificationsAsString += nTerm + aminoAcidsAtTarget + cTerm + " " + symbols[symbolsCounter++] + " " + modification.getRoundedMass() + " ";
        }

        variableModificationsAsString = variableModificationsAsString.trim();
        variableModificationsAsString = "\"" + variableModificationsAsString + "\"";

        return variableModificationsAsString;
    }

    @Override
    public String getType() {
        return "MyriMatch";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return spectrumFile.getName();
    }

    /**
     * Filters the fixed modifications to convert unsupported modification types
     * (i.e. fixed protein terminal modifications) into variable modifications.
     *
     * @return a map of the filtered modifications, keys: "Fixed" and "Variable"
     */
    private HashMap<String, ArrayList<String>> filterFixedModifications() {

        HashMap<String, ArrayList<String>> newModifications = new HashMap<>();
        ArrayList<String> filteredFixedModifications = new ArrayList<>();
        ArrayList<String> variableModifications = new ArrayList<>();
        variableModifications.addAll(searchParameters.getModificationParameters().getVariableModifications());

        for (String modName : searchParameters.getModificationParameters().getFixedModifications()) {

            Modification modification = modificationFactory.getModification(modName);

            ModificationType modificationType = modification.getModificationType();

            if (null != modificationType) {
                switch (modificationType) {
                    case modaa: // particular amino acid
                    case modn_peptide: // peptide n term
                    case modc_peptide: // peptide c term
                        filteredFixedModifications.add(modName);
                        break;
                    case modn_protein: // protein n term
                    case modc_protein: // protein c term
                    case modnaa_peptide: // peptide n term specifc amino acid
                    case modcaa_peptide: // peptide c term specific amino acid
                    case modnaa_protein: // protein n term specific amino acid
                    case modcaa_protein: // protein c term specific amino acid
                        variableModifications.add(modName);
                        break;
                    default:
                        break;
                }
            }
        }

        newModifications.put("Fixed", filteredFixedModifications);
        newModifications.put("Variable", variableModifications);

        return newModifications;
    }
}
