package eu.isas.searchgui.processbuilders;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MyriMatchParameters;
import com.compomics.util.waiting.WaitingHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class will set up and start a process to perform a MyriMatch search.
 *
 * @author Harald Barsnes
 */
public class MyriMatchProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The name of the MyriMatch executable.
     */
    public static final String EXECUTABLE_FILE_NAME = "myrimatch";
    /**
     * The spectrum file to search.
     */
    private String spectrumFile;
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
     * @param mgfFile name of the file containing the spectra
     * @param outputFolder folder where to output the results
     * @param searchParameters the search parameters
     * @param exceptionHandler the handler of exceptions
     * @param waitingHandler the waiting handler
     * @param nThreads the number of threads to use
     */
    public MyriMatchProcessBuilder(File myriMatchDirectory, String mgfFile, File outputFolder,
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
        process_name_array.add(CommandLineUtils.getCommandLineArgument(searchParameters.getFastaFile()));

        // add the spectrum file
        process_name_array.add(CommandLineUtils.getCommandLineArgument(new File(mgfFile)));

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
        String fragemtnTolerance = searchParameters.getFragmentIonAccuracy().toString();
        if (searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.DA) {
            fragemtnTolerance += " daltons";
        } else {
            fragemtnTolerance += " ppm";
        }
        process_name_array.add("\"" + fragemtnTolerance + "\"");

        // set the mono precursor tolerance
        process_name_array.add("-MonoPrecursorMzTolerance");
        String precursorTolerance = searchParameters.getPrecursorAccuracy().toString();
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
        HashMap<String, ArrayList<String>> filteredPtms = filterFixedPtms();

        // set the fixed modifications
        if (!filteredPtms.get("Fixed").isEmpty()) {
            process_name_array.add("-StaticMods");
            process_name_array.add(getFixedModificationsAsString(filteredPtms.get("Fixed")));
        }

        // set the variable modifications
        if (!filteredPtms.get("Variable").isEmpty()) {
            process_name_array.add("-DynamicMods");
            process_name_array.add(getVariableModificationsAsString(filteredPtms.get("Variable")));
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
        String myriMatchEnzyme = MyriMatchParameters.enzymeMapping(searchParameters.getEnzyme());
        if (myriMatchEnzyme != null) {
            process_name_array.add("-CleavageRules");
            if (myriMatchEnzyme.equalsIgnoreCase("unspecific cleavage")) {
                process_name_array.add("\"" + "Trypsin" + "\""); // trick to support unspecific cleavage, MinTerminiCleavages is set to 0 below instead
            } else {
                process_name_array.add("\"" + myriMatchEnzyme + "\"");
            }
        }

        // set the minimum termini cleavages
        process_name_array.add("-MinTerminiCleavages");
        if (myriMatchEnzyme != null && myriMatchEnzyme.equalsIgnoreCase("unspecific cleavage")) {
            process_name_array.add("0");
        } else {
            if (searchParameters.getEnzyme().isSemiSpecific()) {
                process_name_array.add("1"); // note that this overrides the MinTerminiCleavages setting from the user 
            } else {
                process_name_array.add("" + myriMatchParameters.getMinTerminiCleavages());
            }
        }

        // set the maximum missed cleavages
        process_name_array.add("-MaxMissedCleavages");
        process_name_array.add("" + searchParameters.getnMissedCleavages());
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
     * @param fixedPtms the list of fixed PTMs
     * @return the fixed modification as a string
     */
    private String getFixedModificationsAsString(ArrayList<String> fixedPtms) {

        String fixedModifications = "";

        // @TODO: is the code generic enough?
        for (String ptmName : fixedPtms) {
            PTM tempPtm = ptmFactory.getPTM(ptmName);

            if (tempPtm.getType() == PTM.MODAA) {
                for (Character aa : tempPtm.getPattern().getAminoAcidsAtTarget()) {
                    fixedModifications += aa + " " + tempPtm.getRoundedMass() + " ";
                }
            } else {

                if (tempPtm.getType() == PTM.MODNP) { // peptide n term
                    fixedModifications += "( " + tempPtm.getRoundedMass() + " ";
                } else if (tempPtm.getType() == PTM.MODCP) { // peptide c term 
                    fixedModifications += ") " + tempPtm.getRoundedMass() + " ";
                }

                // note: the ptms below should have been converted to variable via filterFixedPtms()
//                else if (tempPtm.getType() == PTM.MODN) { // protein n term
//                    // @TODO: treated as a peptide ptm, should be variable instead?
//                } else if (tempPtm.getType() == PTM.MODNAA) { // protein n term specific amino acid
//                    // @TODO: note suported? should be variable instead?
//                } else if (tempPtm.getType() == PTM.MODNPAA) { // peptide n term specifc amino acid
//                    // @TODO: note suported? should be variable instead?
//                } else if (tempPtm.getType() == PTM.MODC) { // protein c term
//                    // @TODO: treated as a peptide ptm, should be variable instead?
//                } else if (tempPtm.getType() == PTM.MODCAA) { // protein c term specific amino acid
//                    // @TODO: note suported? should be variable instead?
//                } else if (tempPtm.getType() == PTM.MODCPAA) { // peptide c term specific amino acid
//                    // @TODO: note suported? should be variable instead?
//                }
            }
        }

        fixedModifications = fixedModifications.trim();
        fixedModifications = "\"" + fixedModifications + "\"";

        return fixedModifications;
    }

    /**
     * Returns the variable modification as a string in the MyriMatch format.
     *
     * @param variablePtms the list of variable PTMs
     * @return the variable modification as a string
     */
    private String getVariableModificationsAsString(ArrayList<String> variablePtms) {

        String variableModifications = "";
        char[] symbols = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '^', '@', '|', '§', '+', '-', '&', '%', '='}; // @TODO: add more symbols?
        int symbolsCounter = 0;

        // @TODO: is the code generic enough?
        for (String ptmName : variablePtms) {
            PTM tempPtm = ptmFactory.getPTM(ptmName);

            String nTerm = "";
            String cTerm = "";

            if (tempPtm.getType() == PTM.MODNAA
                    || tempPtm.getType() == PTM.MODNPAA
                    || tempPtm.getType() == PTM.MODN
                    || tempPtm.getType() == PTM.MODNP) { // note: does not separate peptide and protein n term
                nTerm = "(";
            } else if (tempPtm.getType() == PTM.MODCPAA
                    || tempPtm.getType() == PTM.MODCAA
                    || tempPtm.getType() == PTM.MODC
                    || tempPtm.getType() == PTM.MODCP) { // note: does not separate peptide and protein c term
                cTerm = ")";
            }

            // get the targeted amino acids
            String aminoAcidsAtTarget = "";
            if (tempPtm.getPattern() != null) {
                for (Character aa : tempPtm.getPattern().getAminoAcidsAtTarget()) {
                    aminoAcidsAtTarget += aa;
                }
            }
            if (aminoAcidsAtTarget.length() > 1) {
                aminoAcidsAtTarget = "[" + aminoAcidsAtTarget + "]";
            }

            variableModifications += nTerm + aminoAcidsAtTarget + cTerm + " " + symbols[symbolsCounter++] + " " + tempPtm.getRoundedMass() + " ";
        }

        variableModifications = variableModifications.trim();
        variableModifications = "\"" + variableModifications + "\"";

        return variableModifications;
    }

    /**
     * Returns the type of the process.
     *
     * @return the type of the process
     */
    public String getType() {
        return "MyriMatch";
    }

    /**
     * Returns the file name of the currently processed file.
     *
     * @return the file name of the currently processed file
     */
    public String getCurrentlyProcessedFileName() {
        return spectrumFile;
    }

    /**
     * Filters the fixed modifications to convert unsupported modification types
     * (fixed protein terminal and fixed terminal at specific amino acids) into
     * variable modifications.
     *
     * @return a map of the filtered modifications, keys: "Fixed" and "Variable"
     */
    private HashMap<String, ArrayList<String>> filterFixedPtms() {

        HashMap<String, ArrayList<String>> newPtms = new HashMap<String, ArrayList<String>>();
        ArrayList<String> filteredFixedPtms = new ArrayList<String>();
        ArrayList<String> variablePtms = new ArrayList<String>();
        variablePtms.addAll(searchParameters.getPtmSettings().getVariableModifications());

        for (String ptmName : searchParameters.getPtmSettings().getFixedModifications()) {

            PTM tempPtm = ptmFactory.getPTM(ptmName);

            if (tempPtm.getType() == PTM.MODAA) { // particular amino acid
                filteredFixedPtms.add(ptmName);
            } else if (tempPtm.getType() == PTM.MODN) { // protein n term
                variablePtms.add(ptmName);
            } else if (tempPtm.getType() == PTM.MODNAA) { // protein n term specific amino acid
                variablePtms.add(ptmName);
            } else if (tempPtm.getType() == PTM.MODNP) { // peptide n term
                filteredFixedPtms.add(ptmName);
            } else if (tempPtm.getType() == PTM.MODNPAA) { // peptide n term specifc amino acid
                variablePtms.add(ptmName);
            } else if (tempPtm.getType() == PTM.MODC) { // protein c term
                variablePtms.add(ptmName);
            } else if (tempPtm.getType() == PTM.MODCAA) { // protein c term specific amino acid
                variablePtms.add(ptmName);
            } else if (tempPtm.getType() == PTM.MODCP) { // peptide c term 
                filteredFixedPtms.add(ptmName);
            } else if (tempPtm.getType() == PTM.MODCPAA) { // peptide c term specific amino acid
                variablePtms.add(ptmName);
            }
        }

        newPtms.put("Fixed", filteredFixedPtms);
        newPtms.put("Variable", variablePtms);

        return newPtms;
    }
}
