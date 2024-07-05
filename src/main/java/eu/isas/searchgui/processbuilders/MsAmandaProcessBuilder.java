package eu.isas.searchgui.processbuilders;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.DigestionParameters.Specificity;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.MsAmandaParameters;
import com.compomics.util.pride.CvTerm;
import com.compomics.util.waiting.WaitingHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class will build files and start a process to perform an MS Amanda
 * search.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class MsAmandaProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The settings XML file for MS Amanda.
     */
    private final String SETTINGS_FILE = "settings.xml";
    /**
     * The enzymes XML file.
     */
    private final String ENZYMES_FILE = "enzymes.xml";
    /**
     * The modifications XML file.
     */
    private final String MODIFICATIONS_FILE = "modifications.xml";
    /**
     * The MS Amanda folder.
     */
    private File msAmandaFolder;
    /**
     * The name of the MS Amanda executable.
     */
    public static final String EXECUTABLE_FILE_NAME = "MSAmanda";
    /**
     * The database file.
     */
    private File database;
    /**
     * The spectrum file.
     */
    private File spectrumFile;
    /**
     * The fragment mass tolerance.
     */
    private Double fragmentMassError;
    /**
     * The precursor mass tolerance.
     */
    private Double precursorMassError;
    /**
     * The precursor mass tolerance unit.
     */
    private String precursorUnit;
    /**
     * The fragment mass tolerance unit.
     */
    private String fragmentUnit;
    /**
     * The lower charge.
     */
    private int minCharge;
    /**
     * The upper charge.
     */
    private int maxCharge;
    /**
     * The missed cleavages allowed.
     */
    private int missedCleavages;
    /**
     * The instrument label.
     */
    private String instrument;
    /**
     * The post translational modifications factory.
     */
    private ModificationFactory modificationFactory = ModificationFactory.getInstance();
    /**
     * The modifications as an XML string.
     */
    private String modificationsAsString;
    /**
     * The name of the enzyme.
     */
    private String enzymeName;
    /**
     * The enzyme specificity: FULL, SEMI, SEMI(N) or SEMI(C).
     */
    private String enzymeSpecificity;
    /**
     * The enzyme cleavage as text.
     */
    private String enzymeCleavage;
    /**
     * The maximum number of matches to provide per spectrum.
     */
    private int maxRank;
    /**
     * Defines whether monoisotopic mass values shall be used (in contrast to
     * average mass values).
     */
    private boolean monoisotopic = true;
    /**
     * Defines whether a decoy database shall be created and searched against.
     */
    private boolean generateDecoys;
    /**
     * False = combine ranks for target and decoy, true = own rankings for
     * target and decoy.
     */
    private boolean reportBothBestHitsForTD;
    /**
     * The flag used to indicate decoys.
     */
    private String decoyFlag;
    /**
     * Defines whether a combined target decoy database is provided.
     */
    private boolean combinedTargetDecoyDBProvided;
    /**
     * Defines whether the low memory mode is used.
     *
     * @deprecated since MS Amanda 2.0
     */
    private Boolean lowMemoryMode = true;
    /**
     * Defines whether deisotoping is to be performed.
     */
    private Boolean performDeisotoping = true;
    /**
     * Maximum number of occurrences of a specific modification on a peptide
     * (0-10).
     */
    private Integer maxModifications = 3;
    /**
     * Maximum number of variable modifications per peptide (0-10).
     */
    private Integer maxVariableModifications = 4;
    /**
     * Maximum number of potential modification sites per modification per
     * peptide (0-20).
     */
    private Integer maxModificationSites = 6;
    /**
     * Maximum number of water and ammonia losses per peptide (0-5).
     */
    private Integer maxNeutralLosses = 1;
    /**
     * Maximum number identical modification specific losses per peptide (0-5).
     */
    private Integer maxNeutralLossesPerModification = 2;
    /**
     * Minimum peptide length (0-20).
     */
    private Integer minPeptideLength = 6;
    /**
     * Maximum peptide length (0-60).
     */
    private Integer maxPeptideLength = 30;
    /**
     * Maximum number of proteins loaded into memory (1000-500000).
     */
    private Integer maxLoadedProteins = 100000;
    /**
     * Maximum number of spectra loaded into memory (1000-500000).
     */
    private Integer maxLoadedSpectra = 2000;
    /**
     * Maximum charge state of calculated fragment ions (+2, +3, +4, Precursor -
     * 1).
     */
    private String maxAllowedChargeState = "+2";
    /**
     * Minimum number of selected peaks within peak picking window (1-30).
     */
    private Integer minPeakDepth = 1;
    /**
     * Maximum number of selected peaks within peak picking window (1-30).
     */
    private Integer maxPeakDepth = 10;
    /**
     * Perform second search to identify mixed spectra.
     */
    private Boolean performSecondSearch = false;
    /**
     * Whether y1 ion shall be kept for second search.
     */
    private Boolean keepY1Ion = true;
    /**
     * Whether water losses shall be removed for second search.
     */
    private Boolean removeWaterLosses = true;
    /**
     * Whether ammonia losses shall be removed for second search.
     */
    private Boolean removeAmmoniaLosses = true;
    /**
     * Exclude original precursor in second search.
     */
    private Boolean excludeFirstPrecursor = true;
    /**
     * Maximum number of different precursors for second search (1-10).
     */
    private Integer maxMultiplePrecursors = 5;
    /**
     * Considered charges are combined in one result.
     */
    private Boolean combineConsideredCharges = true;
    /**
     * Automatically run percolator and add q-values to output file.
     */
    private Boolean runPercolator = false;
    /**
     * Generate file for percolator; filename is the same as stated in output
     * filename with suffix _pin.tsv.
     */
    private Boolean generatePInFile = false;
    /**
     * The folder where the MS Amanda temp files are stored.
     */
    private File msAmandaTempFolder;
    /**
     * The MS Amanda parameters.
     */
    private MsAmandaParameters msAmandaParameters;

    /**
     * Constructor.
     *
     * @param msAmandaDirectory directory location of MSAmanda.exe
     * @param msAmandaTempFolder the folder for temp MS Amanda files
     * @param mgfFile the spectrum file
     * @param fastaFile the FASTA file
     * @param outputPath path where to output the results
     * @param searchParameters the search parameters
     * @param fastaParameters the FASTA parameters
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     * @param nThreads the number of threads to use (not supported by ms amanda)
     * @throws java.io.IOException thrown whenever an IO error occurs
     */
    public MsAmandaProcessBuilder(
            File msAmandaDirectory,
            File msAmandaTempFolder,
            File mgfFile,
            File fastaFile,
            String outputPath,
            SearchParameters searchParameters,
            FastaParameters fastaParameters,
            WaitingHandler waitingHandler,
            ExceptionHandler exceptionHandler,
            int nThreads
    ) throws IOException {

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        msAmandaParameters = (MsAmandaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex());
        
        decoyFlag = fastaParameters.getDecoyFlag();
        combinedTargetDecoyDBProvided = fastaParameters.isTargetDecoy();

        // set the paths
        msAmandaFolder = msAmandaDirectory;
        this.msAmandaTempFolder = msAmandaTempFolder;
        spectrumFile = mgfFile;
        database = fastaFile.getAbsoluteFile();

        // create the temp folder if it does not exist
        if (!msAmandaTempFolder.exists()) {
            msAmandaTempFolder.mkdirs();
        }

        // set the various search settings
        maxRank = msAmandaParameters.getMaxRank();
        generateDecoys = msAmandaParameters.generateDecoy();
        reportBothBestHitsForTD = msAmandaParameters.reportBothBestHitsForTD();
        monoisotopic = msAmandaParameters.isMonoIsotopic();
        performDeisotoping = msAmandaParameters.isPerformDeisotoping();
        maxModifications = msAmandaParameters.getMaxModifications();
        maxVariableModifications = msAmandaParameters.getMaxVariableModifications();
        maxModificationSites = msAmandaParameters.getMaxModificationSites();
        maxNeutralLosses = msAmandaParameters.getMaxNeutralLosses();
        maxNeutralLossesPerModification = msAmandaParameters.getMaxNeutralLossesPerModification();
        minPeptideLength = msAmandaParameters.getMinPeptideLength();
        maxPeptideLength = msAmandaParameters.getMaxPeptideLength();
        maxLoadedProteins = msAmandaParameters.getMaxLoadedProteins();
        maxLoadedSpectra = msAmandaParameters.getMaxLoadedSpectra();
        maxAllowedChargeState = msAmandaParameters.getMaxAllowedChargeState();
        minPeakDepth = msAmandaParameters.getMinPeakDepth();
        maxPeakDepth = msAmandaParameters.getMaxPeakDepth();
        performSecondSearch = msAmandaParameters.getPerformSecondSearch();
        keepY1Ion = msAmandaParameters.getKeepY1Ion();
        removeWaterLosses = msAmandaParameters.getRemoveWaterLosses();
        removeAmmoniaLosses = msAmandaParameters.getRemoveAmmoniaLosses();
        excludeFirstPrecursor = msAmandaParameters.getExcludeFirstPrecursor();
        maxMultiplePrecursors = msAmandaParameters.getMaxMultiplePrecursors();
        combineConsideredCharges = msAmandaParameters.getCombineConsideredCharges();
        runPercolator = msAmandaParameters.getRunPercolator();
        generatePInFile = msAmandaParameters.getGeneratePInFile();

        // set the mass accuracies
        fragmentMassError = searchParameters.getFragmentIonAccuracy();
        precursorMassError = searchParameters.getPrecursorAccuracy();

        if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.PPM) {
            precursorUnit = "ppm";
        } else if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.DA) {
            precursorUnit = "Da";
        }

        if (searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.PPM) {
            fragmentUnit = "ppm";
        } else if (searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.DA) {
            fragmentUnit = "Da";
        }

        // set the charge range
        minCharge = searchParameters.getMinChargeSearched();
        maxCharge = searchParameters.getMaxChargeSearched();

        // set the digestion preferences
        DigestionParameters digestionPreferences = searchParameters.getDigestionParameters();

        if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.enzyme) {

            if (digestionPreferences.getEnzymes().size() > 1) {
                throw new IOException("Multiple enzymes not supported by MS Amanda!");
            }

            Enzyme enzyme = digestionPreferences.getEnzymes().get(0); // @TODO: support more than one enzyme?
            enzymeName = enzyme.getName();
            Specificity specificity = digestionPreferences.getSpecificity(enzymeName);

            switch (specificity) {
                case specific:
                    enzymeSpecificity = "FULL";
                    break;
                case semiSpecific:
                    enzymeSpecificity = "SEMI";
                    break;
                case specificCTermOnly:
                    enzymeSpecificity = "SEMI(C)";
                    break;
                case specificNTermOnly:
                    enzymeSpecificity = "SEMI(N)";
                    break;
                default:
                    break;
            }

            missedCleavages = digestionPreferences.getnMissedCleavages(enzymeName);
            enzymeCleavage = getEnzymeCleavageAsText(enzyme);

        } else if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.unSpecific) {

            enzymeName = digestionPreferences.getCleavageParameter().toString();
            enzymeSpecificity = "FULL";
            missedCleavages = 2; // note: this settings is ignored anyway (but has to be between 0 and 5)
            enzymeCleavage = "CleavageSites=\"\" PrefixInhibitors=\"\" PostfixInhibitors=\"\" Offset=\"\"";

        } else { // whole protein

            enzymeName = digestionPreferences.getCleavageParameter().toString();
            enzymeSpecificity = "FULL";
            missedCleavages = 0;
            enzymeCleavage = "CleavageSites=\"X\" PrefixInhibitors=\"\" PostfixInhibitors=\"\" Offset=\"\"";

        }

        // set the modifications
        modificationsAsString = getModificationsAsString(searchParameters.getModificationParameters());
        instrument = msAmandaParameters.getInstrumentID();

        // create the settings xml file
        createSettingsFile();

        // make sure that the ms amanda exe file is executable
        File msAmanda = new File(msAmandaFolder.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        msAmanda.setExecutable(true);

        // full path to executable
        process_name_array.add(msAmanda.getAbsolutePath());

        // add the spectrum file
        process_name_array.add("-s");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(spectrumFile));

        // add database file
        process_name_array.add("-d");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(database));

        // add the settings file
        process_name_array.add("-e");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(new File(msAmandaTempFolder, SETTINGS_FILE)));

        // add the file format
        process_name_array.add("-f");

        if (msAmandaParameters.getOutputFormat().equalsIgnoreCase("csv")) {
            process_name_array.add("1"); // csv
        } else {
            process_name_array.add("2"); // mzid
        }

        // add the output file
        process_name_array.add("-o");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(new File(outputPath)));

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "ms amanda command: ");

        for (Object processElement : process_name_array) {
            System.out.print(processElement + " ");
        }

        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);
        pb.directory(msAmandaDirectory);

        // set error out and std out to same stream
        pb.redirectErrorStream(true);

    }

    /**
     * Create the enzyme file.
     */
    private String getEnzymeCleavageAsText(Enzyme enzyme) {

        String enzymeCleavageAsText = "";

        String cleavageType;
        String cleavageSite = "";
        String restriction = "";

        if (enzyme.getAminoAcidBefore().isEmpty()) {

            cleavageType = "before";

            for (Character character : enzyme.getAminoAcidAfter()) {
                cleavageSite += character;
            }

            if (!enzyme.getRestrictionBefore().isEmpty()) {

                restriction = "";

                for (Character character : enzyme.getRestrictionBefore()) {
                    restriction += character;
                }

            }

        } else {

            cleavageType = "after";

            for (Character character : enzyme.getAminoAcidBefore()) {
                cleavageSite += character;
            }

            if (!enzyme.getRestrictionAfter().isEmpty()) {

                restriction = "";

                for (Character character : enzyme.getRestrictionAfter()) {
                    restriction += character;
                }

            }

        }

        enzymeCleavageAsText += "CleavageSites=\"" + cleavageSite + "\"";

        if (!restriction.isEmpty()) {

            if (cleavageType.equalsIgnoreCase("before")) {
                enzymeCleavageAsText += " PrefixInhibitors=\"" + restriction + "\"";
            } else {
                enzymeCleavageAsText += " PostfixInhibitors=\"" + restriction + "\"";
            }

        }

        enzymeCleavageAsText += " Offset=\"" + cleavageType + "\"";

        return enzymeCleavageAsText;
    }

    /**
     * Creates the settings XML file.
     */
    private void createSettingsFile() throws IllegalArgumentException {

        File settingsFile = new File(msAmandaTempFolder, SETTINGS_FILE);

        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(settingsFile));

            bw.write(
                    "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + System.getProperty("line.separator")
                    + "<Settings>" + System.getProperty("line.separator")
                    ////////////////////////////      
                    // Search settings
                    ////////////////////////////
                    + "\t<SearchSettings>" + System.getProperty("line.separator")
                    + "\t\t<Enzyme Name=\"" + enzymeName + "\" Specificity=\"" + enzymeSpecificity + "\">" + System.getProperty("line.separator")
                    + "\t\t\t<Cleavage " + enzymeCleavage + "/>" + System.getProperty("line.separator")
                    + "\t\t</Enzyme>" + System.getProperty("line.separator")        
                    + "\t\t<MissedCleavages>" + missedCleavages + "</MissedCleavages>" + System.getProperty("line.separator")
                    + modificationsAsString       
                    + "\t\t<Instrument>" + instrument + "</Instrument>" + System.getProperty("line.separator")
                    + "\t\t<MS1Tol Unit=\"" + precursorUnit + "\">" + precursorMassError + "</MS1Tol> " + System.getProperty("line.separator")
                    + "\t\t<MS2Tol Unit=\"" + fragmentUnit + "\">" + fragmentMassError + "</MS2Tol> " + System.getProperty("line.separator")
                    + "\t\t<MaxRank>" + maxRank + "</MaxRank> " + System.getProperty("line.separator")
                    + "\t\t<GenerateDecoy>" + generateDecoys + "</GenerateDecoy> " + System.getProperty("line.separator")
                    + "\t\t<CombinedTargetDecoyDBProvided>" + combinedTargetDecoyDBProvided + "</CombinedTargetDecoyDBProvided> " + System.getProperty("line.separator")
                    + "\t\t<DecoyFlag>" + decoyFlag + "</DecoyFlag> " + System.getProperty("line.separator")  // @TODO: suffix vs prefix?
                    + "\t\t<PerformDeisotoping>" + performDeisotoping + "</PerformDeisotoping> " + System.getProperty("line.separator")
                    + "\t\t<MaxNoModifs>" + maxModifications + "</MaxNoModifs> " + System.getProperty("line.separator")
                    + "\t\t<MaxNoDynModifs>" + maxVariableModifications + "</MaxNoDynModifs> " + System.getProperty("line.separator")
                    + "\t\t<MaxNumberModSites>" + maxModificationSites + "</MaxNumberModSites> " + System.getProperty("line.separator")
                    + "\t\t<MaxNumberNeutralLoss>" + maxNeutralLosses + "</MaxNumberNeutralLoss> " + System.getProperty("line.separator")
                    + "\t\t<MaxNumberNeutralLossModifications>" + maxNeutralLossesPerModification + "</MaxNumberNeutralLossModifications> " + System.getProperty("line.separator")
                    + "\t\t<MinimumPepLength>" + minPeptideLength + "</MinimumPepLength> " + System.getProperty("line.separator")
                    + "\t\t<MaximumPepLength>" + maxPeptideLength + "</MaximumPepLength> " + System.getProperty("line.separator")
                    + "\t\t<ReportBothBestHitsForTD>" + reportBothBestHitsForTD + "</ReportBothBestHitsForTD> " + System.getProperty("line.separator")
                    + "\t\t<MaxAllowedChargeState>" + maxAllowedChargeState + "</MaxAllowedChargeState> " + System.getProperty("line.separator")
                    + "\t\t<MinimumPeakDepth>" + minPeakDepth + "</MinimumPeakDepth> " + System.getProperty("line.separator")
                    + "\t\t<MaximumPeakDepth>" + maxPeakDepth + "</MaximumPeakDepth> " + System.getProperty("line.separator")
                    + "\t</SearchSettings> " + System.getProperty("line.separator") + System.getProperty("line.separator")
                    ////////////////////////////     
                    // Second search settings
                    ////////////////////////////
                    + "\t<SecondSearchSettings> " + System.getProperty("line.separator")
                    + "\t\t<PerformSecondSearch>" + performSecondSearch + "</PerformSecondSearch> " + System.getProperty("line.separator")
                    + "\t\t<KeepY1Ion>" + keepY1Ion + "</KeepY1Ion> " + System.getProperty("line.separator")
                    + "\t\t<RemoveWaterLosses>" + removeWaterLosses + "</RemoveWaterLosses> " + System.getProperty("line.separator")
                    + "\t\t<RemoveAmmoniaLosses>" + removeAmmoniaLosses + "</RemoveAmmoniaLosses> " + System.getProperty("line.separator")
                    + "\t\t<ExcludeFirstPrecursor>" + excludeFirstPrecursor + "</ExcludeFirstPrecursor> " + System.getProperty("line.separator")
                    + "\t\t<MaxMultiplePrecursors>" + maxMultiplePrecursors + "</MaxMultiplePrecursors> " + System.getProperty("line.separator")
                    + "\t</SecondSearchSettings> " + System.getProperty("line.separator") + System.getProperty("line.separator")
                    ////////////////////////////
                    // Basic settings
                    ////////////////////////////
                    + "\t<BasicSettings> " + System.getProperty("line.separator")
                    + "\t\t<Monoisotopic>" + monoisotopic + "</Monoisotopic> " + System.getProperty("line.separator")
                    + "\t\t<ConsideredCharges>" + getChargeRangeAsString() + "</ConsideredCharges> " + System.getProperty("line.separator")
                    + "\t\t<CombineConsideredCharges>" + combineConsideredCharges + "</CombineConsideredCharges> " + System.getProperty("line.separator")
                    + "\t\t<LoadedProteinsAtOnce>" + maxLoadedProteins + "</LoadedProteinsAtOnce> " + System.getProperty("line.separator")
                    + "\t\t<LoadedSpectraAtOnce>" + maxLoadedSpectra + "</LoadedSpectraAtOnce> " + System.getProperty("line.separator")
                    + "\t\t<DataFolder>" + msAmandaTempFolder + "</DataFolder> " + System.getProperty("line.separator")     
                    + "\t\t<EnzymesFile>" + new File(msAmandaFolder, ENZYMES_FILE).getAbsolutePath() + "</EnzymesFile> " + System.getProperty("line.separator")
                    + "\t\t<ModificationsFile>" + new File(msAmandaFolder, MODIFICATIONS_FILE).getAbsolutePath() + "</ModificationsFile> " + System.getProperty("line.separator")
                    + "\t</BasicSettings> " + System.getProperty("line.separator")
                    ////////////////////////////        
                    // Percolator settings
                    ////////////////////////////
                    + "\t<PercolatorSettings> " + System.getProperty("line.separator")
                    + "\t\t<GeneratePInFile>" + generatePInFile + "</GeneratePInFile> " + System.getProperty("line.separator")
                    + "\t\t<RunPercolator>" + runPercolator + "</RunPercolator> " + System.getProperty("line.separator")
                    + "\t</PercolatorSettings> " + System.getProperty("line.separator")
                    + "</Settings>"
                    + System.getProperty("line.separator")
            );

            bw.flush();
            bw.close();

        } catch (IOException ioe) {

            throw new IllegalArgumentException(
                    "Could not create MS Amanda settings file. Unable to write file: '"
                    + ioe.getMessage()
                    + "'!"
            );

        }

    }

    @Override
    public String getType() {
        return "MS Amanda";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return spectrumFile.getName();
    }

    /**
     * Get the modifications as an XML tag.
     *
     * @param modificationProfile
     * @return the modifications as an XML tag
     */
    private String getModificationsAsString(ModificationParameters modificationProfile) {

        String temp = "\t\t<Modifications>" + System.getProperty("line.separator");

        for (String modificationName : modificationProfile.getFixedModifications()) {
            Modification modification = modificationFactory.getModification(modificationName);
            temp += getModificationAsString(modification, true) + System.getProperty("line.separator");
        }

        for (String modificationName : modificationProfile.getVariableModifications()) {
            Modification modification = modificationFactory.getModification(modificationName);
            temp += getModificationAsString(modification, false) + System.getProperty("line.separator");
        }

        temp += "\t\t</Modifications>" + System.getProperty("line.separator");

        return temp;

    }

    /**
     * Returns a single modification as a string.
     *
     * @param modification the modification to convert
     * @return the modification as a string
     */
    private String getModificationAsString(Modification modification, boolean fixed) {

        String nTermTag = "";
        String cTermTag = "";
        String proteinTag = "";
        String fixedTag = "";

        // get the fixed tag
        if (fixed) {
            fixedTag = " fix=\"true\"";
        }

        // get the terminal tags
        switch (modification.getModificationType()) {

            case modaa:
                // not terminal
                break;

            case modc_protein:
            case modcaa_protein:
                //proteinTag = " protein=\"true\""; // note: MS Amanda Manual: "Protein level modifications are only valid in combination with n‐terminal modifications"
                cTermTag = " Cterm=\"true\"";
                break;

            case modc_peptide:
            case modcaa_peptide:
                cTermTag = " Cterm=\"true\"";
                break;

            case modn_protein:
            case modnaa_protein:
                proteinTag = " protein=\"true\"";
                nTermTag = " Nterm=\"true\"";
                break;

            case modn_peptide:
            case modnaa_peptide:
                nTermTag = " Nterm=\"true\"";
                break;

        }

        String aminoAcidsAtTarget = "";

        // get the targeted amino acids
        if (modification.getModificationType() == ModificationType.modaa
                || modification.getModificationType() == ModificationType.modcaa_peptide
                || modification.getModificationType() == ModificationType.modcaa_protein
                || modification.getModificationType() == ModificationType.modnaa_peptide
                || modification.getModificationType() == ModificationType.modnaa_protein) {

            for (Character aa : modification.getPattern().getAminoAcidsAtTarget()) {

                if (!aminoAcidsAtTarget.isEmpty()) {
                    aminoAcidsAtTarget += ",";
                }

                aminoAcidsAtTarget += aa;
            }

        }

        if (!aminoAcidsAtTarget.isEmpty()) {
            aminoAcidsAtTarget = "(" + aminoAcidsAtTarget + ")";
        }

        // use unimod name if possible
        CvTerm cvTerm = modification.getUnimodCvTerm();

        if (cvTerm != null) {
            return "\t\t\t<Modification" + fixedTag
                    + nTermTag + cTermTag + proteinTag + ">" + cvTerm.getName() + aminoAcidsAtTarget + "</Modification>";
        } else {
            return "\t\t\t<Modification DeltaMass=\"" + modification.getRoundedMass() + "\"" + fixedTag
                    + nTermTag + cTermTag + proteinTag + ">" + modification.getName() + aminoAcidsAtTarget + "</Modification>";
        }

    }

    /**
     * Returns the charges as a string.
     *
     * @return the charges as a string
     */
    private String getChargeRangeAsString() {

        String charges = "";

        for (int i = minCharge; i <= maxCharge; i++) {

            if (!charges.isEmpty()) {
                charges += ",";
            }

            charges += i + "+";

        }

        return charges;
    }

    /**
     * Returns true if target and decoy are ranked separately, false if shared
     * rank.
     *
     * @return true if target and decoy are ranked separately, false if shared
     * rank
     */
    public boolean reportBothBestHitsForTD() {
        return reportBothBestHitsForTD;
    }

    /**
     * Set if target and decoy are ranked separately or shared.
     *
     * @param reportBothBestHitsForTD the reportBothBestHitsForTD to set
     */
    public void setReportBothBestHitsForTD(boolean reportBothBestHitsForTD) {
        this.reportBothBestHitsForTD = reportBothBestHitsForTD;
    }

}
