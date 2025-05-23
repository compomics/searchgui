package eu.isas.searchgui.processbuilders;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.SageParameters;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * ProcessBuilder for the Sage search engine.
 *
 * @author Harald Barsnes
 */
public class SageProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The Sage folder.
     */
    private File sageFolder;
    /**
     * The temp folder for Sage files.
     */
    private File sageTempFolder;
    /**
     * The Sage version number as a string.
     */
    private final String SAGE_VERSION = "0.14.7";
    /**
     * The spectrum file.
     */
    private File spectrumFile;
    /**
     * The FASTA file.
     */
    private File fastaFile;
    /**
     * The FASTA file decoy tag.
     */
    private String decoyTag;
    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The advanced Sage parameters.
     */
    private SageParameters sageParameters;
    /**
     * The number of threads to use.
     */
    private int nThreads;
    /**
     * The compomics modification factory.
     */
    private ModificationFactory modificationFactory = ModificationFactory.getInstance();
    /**
     * A reference mass to convert fragment ion tolerance from ppm to Dalton.
     */
    private Double refMass;

    /**
     * Constructor.
     *
     * @param sageFolder the Sage folder
     * @param sageTempFolder the temp folder for Sage files
     * @param searchParameters the search parameters
     * @param spectrumFile the spectrum file
     * @param fastaFile the FASTA file
     * @param decoyTag the FASTA file decoy tag
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     * @param nThreads the number of threads
     *
     * @throws IOException thrown if there are problems creating the Sage
     * parameter file
     */
    public SageProcessBuilder(
            File sageFolder,
            File sageTempFolder,
            SearchParameters searchParameters,
            File spectrumFile,
            File fastaFile,
            String decoyTag,
            WaitingHandler waitingHandler,
            ExceptionHandler exceptionHandler,
            int nThreads
    ) throws IOException {

        this.sageFolder = sageFolder;
        this.sageTempFolder = sageTempFolder;
        this.searchParameters = searchParameters;
        sageParameters = (SageParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.sage.getIndex());
        this.spectrumFile = spectrumFile;
        this.fastaFile = fastaFile;
        this.decoyTag = decoyTag;
        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        this.nThreads = nThreads;

        // create the temp folder if it does not exist
        if (!sageTempFolder.exists()) {
            sageTempFolder.mkdirs();
        }

        createParametersFile();

        // make sure that the sage file is executable
        File sage = new File(sageFolder.getAbsolutePath() + File.separator + getExecutableFileName());
        sage.setExecutable(true);

        // full path to executable
        process_name_array.add(sage.getAbsolutePath());

        // link to the output directory
        process_name_array.add("-o");
        process_name_array.add(sageTempFolder.getAbsolutePath());

        // set the batch size
        if (sageParameters.getBatchSize() != null) {
            process_name_array.add("--batch-size");
            process_name_array.add(sageParameters.getBatchSize().toString());
        }

        // turn off the sendig of basic telemetry data
        process_name_array.add("--disable-telemetry-i-dont-want-to-improve-sage");

        // @TODO: implement?
//        // write pin file
//        if (sageParameters.getWritePin() != null) {
//            process_name_array.add("-write-pin");
//        }
        // link to the parameter file
        String path = new File(sageTempFolder, "sage.json").getAbsolutePath();
        process_name_array.add(path);

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "sage command: ");

        for (Object currentElement : process_name_array) {
            System.out.print(currentElement + " ");
        }

        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);
        pb.directory(sageFolder);

        // set error out and std out to same stream
        pb.redirectErrorStream(true);

    }

    /**
     * Create the parameters file.
     *
     * @throws IOException
     */
    private void createParametersFile() throws IOException {

        Enzyme tempEnzyme;
        String enzymeDetailsAsString = "";
        String enzymeMissedCleavagesAsString = "";
        String enzymeSemiEnzymaticAsString = "";

        DigestionParameters digestionPreferences = searchParameters.getDigestionParameters();

        if (null == digestionPreferences.getCleavageParameter()) {

            throw new IOException("Enzyme type not supported by Sage!");

        } else {

            switch (digestionPreferences.getCleavageParameter()) {

                case enzyme:

                    if (searchParameters.getDigestionParameters().getEnzymes().size() > 1) {
                        throw new IOException("Multiple enzymes not supported by Sage!");
                    } else {
                        tempEnzyme = searchParameters.getDigestionParameters().getEnzymes().get(0);
                    }

                    String aminoAcidsBefore = "";
                    String restrictionAfter = "";
                    String aminoAcidsAfter = "";
                    String restrictionBefore = "";

                    for (Character character : tempEnzyme.getAminoAcidBefore()) {
                        aminoAcidsBefore += character;
                    }

                    for (Character character : tempEnzyme.getRestrictionAfter()) {
                        restrictionAfter += character;
                    }

                    for (Character character : tempEnzyme.getAminoAcidAfter()) {
                        aminoAcidsAfter += character;
                    }

                    for (Character character : tempEnzyme.getRestrictionBefore()) {
                        restrictionBefore += character;
                    }

                    if (!aminoAcidsBefore.isEmpty() && !aminoAcidsAfter.isEmpty()) {
                        throw new IOException("Enzymes cleavage site not supported by Sage!");
                    }

                    String cleaveAt;
                    String restrict = null;
                    Boolean cTerminalEnzyme = null;

                    if (!aminoAcidsBefore.isEmpty()) {

                        cTerminalEnzyme = true;
                        cleaveAt = aminoAcidsBefore;
                        restrict = restrictionAfter;

                    } else if (!aminoAcidsAfter.isEmpty()) {

                        cTerminalEnzyme = false;
                        cleaveAt = aminoAcidsAfter;
                        restrict = restrictionBefore;

                    } else {
                        throw new IOException("Enzyme type not supported by Sage!");
                    }

                    enzymeDetailsAsString
                            = "\t\t\t\"cleave_at\": \""
                            + cleaveAt
                            + "\","
                            + System.getProperty("line.separator");

                    if (!restrict.isEmpty()) {
                        enzymeDetailsAsString
                                += "\t\t\t\"restrict\": \""
                                + restrict
                                + "\","
                                + System.getProperty("line.separator");
                    }

                    enzymeDetailsAsString
                            += "\t\t\t\"c_terminal\": "
                            + cTerminalEnzyme
                            + ","
                            + System.getProperty("line.separator");

                    enzymeMissedCleavagesAsString
                            = "\t\t\t\"missed_cleavages\": "
                            + digestionPreferences.getnMissedCleavages(tempEnzyme.getName())
                            + ","
                            + System.getProperty("line.separator");

                    switch (searchParameters.getDigestionParameters().getSpecificity(tempEnzyme.getName())) {

                        case specific:

                            enzymeSemiEnzymaticAsString
                                    = "\t\t\t\"semi_enzymatic\": null"
                                    + System.getProperty("line.separator");

                            break;

                        case semiSpecific:

                            enzymeSemiEnzymaticAsString
                                    = "\t\t\t\"semi_enzymatic\": true"
                                    + System.getProperty("line.separator");

                            break;

                        default:
                            // N-term Specific or C-term Specific
                            throw new IOException("Enzyme specificity type not supported by Sage!");

                    }

                    break;

                case unSpecific:

                    enzymeDetailsAsString
                            = "\t\t\t\"cleave_at\": \"\""
                            + System.getProperty("line.separator");
                    break;

                case wholeProtein:

                    enzymeDetailsAsString
                            = "\t\t\t\"cleave_at\": \"$\""
                            + System.getProperty("line.separator");
                    break;

                default:

                    throw new IOException("Enzyme type not supported by Sage!");

            }
        }

        String fixedModificationsAsString = getModifications(searchParameters.getModificationParameters().getFixedModifications(), true);
        String variableModificationsAsString = getModifications(searchParameters.getModificationParameters().getVariableModifications(), false);

        String tmtType = sageParameters.getTmtType() != null ? "\"" + sageParameters.getTmtType() + "\"" : "null";
        String tmtLevel = sageParameters.getTmtLevel() != null ? sageParameters.getTmtLevel().toString() : "3";
        String tmtSn = sageParameters.getTmtSn() ? "true" : "null";
        String performLfq = sageParameters.getPerformLfq() ? "true" : "null";

        BufferedWriter br = new BufferedWriter(new FileWriter(new File(sageTempFolder, "sage.json")));

        try {

            br.write(
                    "{" + System.getProperty("line.separator")
                    + "\t\"database\": {" + System.getProperty("line.separator")
                    + "\t\t\"bucket_size\": " + sageParameters.getBucketSize() + "," + System.getProperty("line.separator")
                    //////////////////////
                    // emzyme settings
                    //////////////////////
                    + "\t\t\"enzyme\": {" + System.getProperty("line.separator")
                    + enzymeMissedCleavagesAsString
                    + "\t\t\t\"min_len\": " + sageParameters.getMinPeptideLength() + "," + System.getProperty("line.separator")
                    + "\t\t\t\"max_len\": " + sageParameters.getMaxPeptideLength() + "," + System.getProperty("line.separator")
                    + enzymeDetailsAsString
                    + enzymeSemiEnzymaticAsString
                    + "\t\t}," + System.getProperty("line.separator")
                    ///////////////////////////////////
                    // fragment and peptide settings
                    ///////////////////////////////////
                    + "\t\t\"fragment_min_mz\": " + sageParameters.getMinFragmentMz() + "," + System.getProperty("line.separator")
                    + "\t\t\"fragment_max_mz\": " + sageParameters.getMaxFragmentMz() + "," + System.getProperty("line.separator")
                    + "\t\t\"peptide_min_mass\": " + sageParameters.getMinPeptideMass() + "," + System.getProperty("line.separator")
                    + "\t\t\"peptide_max_mass\": " + sageParameters.getMaxPeptideMass() + "," + System.getProperty("line.separator")
                    + "\t\t\"ion_kinds\": " + getFragmentIonTypesAsList() + "," + System.getProperty("line.separator")
                    + "\t\t\"min_ion_index\": " + sageParameters.getMinIonIndex() + "," + System.getProperty("line.separator")
                    //////////////////////////////////////
                    // fixed and variable modifications
                    //////////////////////////////////////
                    + fixedModificationsAsString
                    + variableModificationsAsString
                    ///////////////////////////////////
                    // database settings
                    ///////////////////////////////////
                    + "\t\t\"max_variable_mods\": " + sageParameters.getMaxVariableMods() + "," + System.getProperty("line.separator")
                    + "\t\t\"decoy_tag\": \"" + decoyTag + "\"," + System.getProperty("line.separator")
                    + "\t\t\"generate_decoys\": " + sageParameters.getGenerateDecoys().toString() + "," + System.getProperty("line.separator")
                    + "\t\t\"fasta\": \"" + fastaFile.getAbsolutePath().replace("\\", "\\\\") + "\"" + System.getProperty("line.separator")
                    + "\t\t}," + System.getProperty("line.separator")
                    ///////////////////////////////////
                    // quant settings
                    ///////////////////////////////////    
                    + "\t\"quant\": {" + System.getProperty("line.separator")
                    + "\t\t\"tmt\": " + tmtType + "," + System.getProperty("line.separator")
                    + "\t\t\"tmt_settings\": {" + System.getProperty("line.separator")
                    + "\t\t\t\"tmt_level\": " + tmtLevel + "," + System.getProperty("line.separator")
                    + "\t\t\t\"tmt_sn\": " + tmtSn + System.getProperty("line.separator")
                    + "\t\t}," + System.getProperty("line.separator")
                    + "\t\t\"lfq\": " + performLfq + "," + System.getProperty("line.separator")
                    + "\t\t\"lfq_settings\": {" + System.getProperty("line.separator")
                    + "\t\t\t\"peak_scoring\" : \"" + sageParameters.getLfqPeakScoring() + "\"," + System.getProperty("line.separator")
                    + "\t\t\t\"integration\": \"" + sageParameters.getLfqIntergration() + "\"," + System.getProperty("line.separator")
                    + "\t\t\t\"spectral_angle\": " + sageParameters.getLfqSpectralAngle() + "," + System.getProperty("line.separator")
                    + "\t\t\t\"ppm_tolerance\": " + sageParameters.getLfqPpmTolerance() + "," + System.getProperty("line.separator")
                    + "\t\t\t\"combine_charge_state\": " + sageParameters.getCombineChargeStates().toString() + System.getProperty("line.separator")
                    + "\t\t}" + System.getProperty("line.separator")
                    + "\t}," + System.getProperty("line.separator")
                    ////////////////////////////////////////
                    // precursor and fragment tolerance
                    ////////////////////////////////////////
                    + "\t\"precursor_tol\": {" + System.getProperty("line.separator")
                    + "\t\t\"" + searchParameters.getPrecursorAccuracyType().toString().toLowerCase() + "\": [" + System.getProperty("line.separator")
                    + "\t\t\t" + -searchParameters.getPrecursorAccuracy() + "," + System.getProperty("line.separator")
                    + "\t\t\t" + searchParameters.getPrecursorAccuracy() + System.getProperty("line.separator")
                    + "\t\t]" + System.getProperty("line.separator")
                    + "\t}," + System.getProperty("line.separator")
                    + "\t\"fragment_tol\": {" + System.getProperty("line.separator")
                    + "\t\t\"" + searchParameters.getFragmentAccuracyType().toString().toLowerCase() + "\": [" + System.getProperty("line.separator")
                    + "\t\t\t" + -searchParameters.getFragmentIonAccuracy() + "," + System.getProperty("line.separator")
                    + "\t\t\t" + searchParameters.getFragmentIonAccuracy() + System.getProperty("line.separator")
                    + "\t\t]" + System.getProperty("line.separator")
                    + "\t}," + System.getProperty("line.separator")
                    ////////////////////////////////////////
                    // precursor charge
                    ////////////////////////////////////////
                    + "\t\"precursor_charge\": ["
                    + searchParameters.getMinChargeSearched()
                    + ", "
                    + searchParameters.getMaxChargeSearched()
                    + "],"
                    + System.getProperty("line.separator")
                    ////////////////////////////////////////
                    // isotope settings
                    ////////////////////////////////////////
                    + "\t\"isotope_errors\": [" + System.getProperty("line.separator")
                    + "\t\t" + searchParameters.getMinIsotopicCorrection() + "," + System.getProperty("line.separator")
                    + "\t\t" + searchParameters.getMaxIsotopicCorrection() + System.getProperty("line.separator")
                    + "\t]," + System.getProperty("line.separator")
                    + "\t\"deisotope\": " + sageParameters.getDeisotope().toString() + "," + System.getProperty("line.separator")
                    ////////////////////////////////////////
                    // other
                    ////////////////////////////////////////
                    + "\t\"chimera\": " + sageParameters.getChimera().toString() + "," + System.getProperty("line.separator")
                    + "\t\"wide_window\": " + sageParameters.getWideWindow().toString() + "," + System.getProperty("line.separator")
                    + "\t\"predict_rt\": " + sageParameters.getPredictRt().toString() + "," + System.getProperty("line.separator")
                    + "\t\"min_peaks\": " + sageParameters.getMinPeaks() + "," + System.getProperty("line.separator")
                    + "\t\"max_peaks\": " + sageParameters.getMaxPeaks() + "," + System.getProperty("line.separator")
                    + "\t\"min_matched_peaks\": " + sageParameters.getMinMatchedPeaks() + "," + System.getProperty("line.separator")
                    + "\t\"max_fragment_charge\": " + sageParameters.getMaxFragmentCharge() + "," + System.getProperty("line.separator")
                    + "\t\"report_psms\": " + sageParameters.getNumPsmsPerSpectrum() + "," + System.getProperty("line.separator")
                    ////////////////////////////////////////
                    // spectrum file paths
                    ////////////////////////////////////////
                    + "\t\"mzml_paths\": [\"" + spectrumFile.getAbsolutePath().replace("\\", "\\\\") + "\"]" + System.getProperty("line.separator")
                    + "}" + System.getProperty("line.separator")
            );

        } finally {
            br.close();
        }

    }

    /**
     * Returns the list of fragment ions types in the Sage format.
     *
     * @return the list of fragment ions type
     */
    private String getFragmentIonTypesAsList() { // @TODO: support more then two fragment ion types at the same time in the GUI?

        String listOfFragmentIonTypes = "[";

        for (Integer tempFragmentIonNumber : searchParameters.getForwardIons()) {

            if (listOfFragmentIonTypes.length() > 1) {
                listOfFragmentIonTypes += ", ";
            }

            listOfFragmentIonTypes += "\"" + PeptideFragmentIon.getSubTypeAsString(tempFragmentIonNumber) + "\"";

        }

        for (Integer tempFragmentIonNumber : searchParameters.getRewindIons()) {

            if (listOfFragmentIonTypes.length() > 1) {
                listOfFragmentIonTypes += ", ";
            }

            listOfFragmentIonTypes += "\"" + PeptideFragmentIon.getSubTypeAsString(tempFragmentIonNumber) + "\"";

        }

        listOfFragmentIonTypes += "]";

        return listOfFragmentIonTypes;

    }

    /**
     * Returns the modifications as a formatted string.
     *
     * @param modifications the list of variable or fixed modifications
     * @param fixed true of the modifications are fixed, false if variable
     *
     * @return the modifications as a formatted string
     *
     * @throws IOException if an unsupported modification is detected
     */
    private String getModifications(
            ArrayList<String> modifications,
            boolean fixed
    ) throws IOException {

        HashMap<String, ArrayList<Double>> modificationMap = new HashMap<>();

        for (String modName : modifications) {

            Modification modification = modificationFactory.getModification(modName);

            switch (modification.getModificationType()) {

                case modaa:

                    for (Character aminoAcid : modification.getPattern().getAminoAcidsAtTarget()) {

                        if (!modificationMap.containsKey(aminoAcid.toString())) {
                            ArrayList modMassList = new ArrayList<>();
                            modificationMap.put(aminoAcid.toString(), modMassList);
                        }

                        modificationMap.get(aminoAcid.toString()).add(modification.getMass());

                    }

                    break;

                case modn_protein:

                    if (!modificationMap.containsKey("[")) {
                        ArrayList modMassList = new ArrayList<>();
                        modificationMap.put("[", modMassList);
                    }

                    modificationMap.get("[").add(modification.getMass());

                    break;

                case modn_peptide:

                    if (!modificationMap.containsKey("^")) {
                        ArrayList modMassList = new ArrayList<>();
                        modificationMap.put("^", modMassList);
                    }

                    modificationMap.get("^").add(modification.getMass());

                    break;

                case modnaa_protein:

                    for (Character aminoAcid : modification.getPattern().getAminoAcidsAtTarget()) {

                        if (!modificationMap.containsKey("[" + aminoAcid)) {
                            ArrayList modMassList = new ArrayList<>();
                            modificationMap.put("[" + aminoAcid, modMassList);
                        }

                        modificationMap.get("[" + aminoAcid).add(modification.getMass());

                    }

                    break;

                case modnaa_peptide:

                    for (Character aminoAcid : modification.getPattern().getAminoAcidsAtTarget()) {

                        if (!modificationMap.containsKey("^" + aminoAcid)) {
                            ArrayList modMassList = new ArrayList<>();
                            modificationMap.put("^" + aminoAcid, modMassList);
                        }

                        modificationMap.get("^" + aminoAcid).add(modification.getMass());

                    }

                    break;

                case modc_protein:

                    if (!modificationMap.containsKey("]")) {
                        ArrayList modMassList = new ArrayList<>();
                        modificationMap.put("]", modMassList);
                    }

                    modificationMap.get("]").add(modification.getMass());

                    break;

                case modc_peptide:

                    if (!modificationMap.containsKey("$")) {
                        ArrayList modMassList = new ArrayList<>();
                        modificationMap.put("$", modMassList);
                    }

                    modificationMap.get("$").add(modification.getMass());

                    break;

                case modcaa_protein:

                    for (Character aminoAcid : modification.getPattern().getAminoAcidsAtTarget()) {

                        if (!modificationMap.containsKey("]" + aminoAcid)) {
                            ArrayList modMassList = new ArrayList<>();
                            modificationMap.put("]" + aminoAcid, modMassList);
                        }

                        modificationMap.get("]" + aminoAcid).add(modification.getMass());

                    }

                    break;

                case modcaa_peptide:

                    for (Character aminoAcid : modification.getPattern().getAminoAcidsAtTarget()) {

                        if (!modificationMap.containsKey("$" + aminoAcid)) {
                            ArrayList modMassList = new ArrayList<>();
                            modificationMap.put("$" + aminoAcid, modMassList);
                        }

                        modificationMap.get("$" + aminoAcid).add(modification.getMass());

                    }

                    break;

                default:
                    break;
            }

        }

        String staticOrVariableTag = fixed ? "static_mods" : "variable_mods";

        String modificationsAsString
                = "\t\t\""
                + staticOrVariableTag
                + "\": {"
                + System.getProperty("line.separator");

        String startBracket = fixed ? "" : "[";
        String endBracket = fixed ? "" : "]";

        String tempModString = "";

        for (String modKey : modificationMap.keySet()) {

            if (!tempModString.isEmpty()) {
                tempModString += "," + System.getProperty("line.separator");
            }

            tempModString += "\t\t\t\"" + modKey + "\": " + startBracket;

            ArrayList<Double> modMasses = modificationMap.get(modKey);

            for (int i = 0; i < modMasses.size(); i++) {

                if (i > 0) {
                    tempModString += ", ";
                }

                tempModString += modMasses.get(i);

            }

            tempModString += endBracket;

        }

        modificationsAsString += tempModString + System.getProperty("line.separator");
        modificationsAsString += "\t\t}," + System.getProperty("line.separator");

        return modificationsAsString;

    }

    @Override
    public String getType() {
        return "Sage";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return spectrumFile.getName();
    }

    /**
     * Returns the name of the Sage executable.
     *
     * @return the name of the Sage executable
     */
    public static String getExecutableFileName() {

        String operatingSystem = System.getProperty("os.name").toLowerCase();

        if (operatingSystem.contains("windows")) {
            return "sage.exe";
        } else {
            return "sage"; // @TODO: support both M1 and x86 versions for osx?
        }

    }

}
