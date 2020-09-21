package eu.isas.searchgui.processbuilders;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.identification.Advocate;
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
import java.util.StringTokenizer;

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
    private final String SETTINGS_FILE = "settings_SearchGUI.xml";
    /**
     * The enzymes XML file.
     */
    private final String ENZYMES_FILE = "enzymes_SearchGUI.xml";
    /**
     * The taxonomy XML file for X!Tandem.
     */
    private final String INSTRUMENTS_FILE = "instruments_SearchGUI.xml";
    /**
     * The Unimod XML file.
     */
    private final String UNIMOD_FILE = "unimod.xml";
    /**
     * The Unimod OBO file.
     */
    private final String UNIMOD_OBO_FILE = "unimod.obo";
    /**
     * The PSI-MS OBO file.
     */
    private final String PSI_MS_OBO_FILE = "psi-ms.obo";
    /**
     * The MS Amanda folder.
     */
    private File msAmandaFolder;
    /**
     * The name of the MS Amanda executable.
     */
    public static final String EXECUTABLE_FILE_NAME = "MSAmanda.exe";
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
     * The path to the folder where the MS Amanda temp files are stored. Set to
     * DEFAULT to use the default location for MS Amanda.
     */
    private String msAmandaTempFolder = "DEFAULT";
    /**
     * The MS Amanda parameters.
     */
    private MsAmandaParameters msAmandaParameters;

    /**
     * Constructor.
     *
     * @param msAmandaDirectory directory location of MSAmanda.exe
     * @param mgfFile the spectrum file
     * @param fastaFile the FASTA file
     * @param outputPath path where to output the results
     * @param searchParameters the search parameters
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     * @param nThreads the number of threads to use (note: cannot be used)
     */
    public MsAmandaProcessBuilder(File msAmandaDirectory, File mgfFile, File fastaFile, String outputPath,
            SearchParameters searchParameters, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, int nThreads) {

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        msAmandaParameters = (MsAmandaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex());

        // set the paths
        msAmandaFolder = msAmandaDirectory;
        spectrumFile = mgfFile;
        database = fastaFile.getAbsoluteFile();
        //msAmandTempFolder = ""; @TODO: allow the user to set the temp folder

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
            Enzyme enzyme = digestionPreferences.getEnzymes().get(0);
            enzymeName = enzyme.getName();
            Specificity specificity = digestionPreferences.getSpecificity(enzymeName);
            if (specificity == Specificity.specific) {
                enzymeSpecificity = "FULL";
            } else if (specificity == Specificity.semiSpecific) {
                enzymeSpecificity = "SEMI";
            } else if (specificity == Specificity.specificCTermOnly) {
                enzymeSpecificity = "SEMI(C)";
            } else if (specificity == Specificity.specificNTermOnly) {
                enzymeSpecificity = "SEMI(N)";
            }
            missedCleavages = digestionPreferences.getnMissedCleavages(enzymeName);
        } else if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.unSpecific) {
            enzymeName = digestionPreferences.getCleavageParameter().toString();
            enzymeSpecificity = "FULL";
            missedCleavages = 50; // @TODO: is this correct?
        } else { // whole protein
            enzymeName = digestionPreferences.getCleavageParameter().toString();
            enzymeSpecificity = "FULL";
            missedCleavages = 0;
        }

        // set the modifications
        modificationsAsString = getModificationsAsString(searchParameters.getModificationParameters());
        instrument = msAmandaParameters.getInstrumentID();

        // create the enzyme file
        createEnzymeFile();

        // create the settings xml file
        createSettingsFile();

        // make sure that the ms amanda exe file is executable
        File msAmanda = new File(msAmandaFolder.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        msAmanda.setExecutable(true);

        // add mono if not on windows
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        if (!operatingSystem.contains("windows")) {

            String monoPath = "mono";

            // modern mac os x versions need a specific mono path
            if (operatingSystem.contains("mac os x")) {
                StringTokenizer versionTokens = new StringTokenizer(System.getProperty("os.version"), ".");
                if (versionTokens.countTokens() > 1) {
                    int mainVersion = Integer.valueOf(versionTokens.nextToken());
                    int subversion = Integer.valueOf(versionTokens.nextToken());
                    if (mainVersion >= 10 && subversion >= 11) {
                        monoPath = "/Library/Frameworks/Mono.framework/Versions/Current/bin/mono";
                    }
                }
            }

            process_name_array.add(monoPath);
        }

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
        process_name_array.add(CommandLineUtils.getCommandLineArgument(new File(msAmandaFolder, SETTINGS_FILE)));

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
    private void createEnzymeFile() {

        File enzymeFile = new File(msAmandaFolder, ENZYMES_FILE);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(enzymeFile));
            bw.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + System.getProperty("line.separator"));
            bw.write("<enzymes>" + System.getProperty("line.separator"));

            EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();

            for (Enzyme enzyme : enzymeFactory.getEnzymes()) {

                bw.write("  <enzyme>" + System.getProperty("line.separator"));
                bw.write("    <name>" + enzyme.getName() + "</name>" + System.getProperty("line.separator"));

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

                bw.write("    <cleavage_sites>" + cleavageSite + "</cleavage_sites>" + System.getProperty("line.separator"));
                if (!restriction.isEmpty()) {
                    bw.write("    <inhibitors>" + restriction + "</inhibitors>" + System.getProperty("line.separator"));
                }
                bw.write("    <position>" + cleavageType + "</position>" + System.getProperty("line.separator"));

                bw.write("  </enzyme>" + System.getProperty("line.separator"));
            }

            bw.write("  <enzyme>" + System.getProperty("line.separator"));
            bw.write("    <name>" + DigestionParameters.CleavageParameter.wholeProtein + "</name>" + System.getProperty("line.separator"));
            bw.write("    <cleavage_sites></cleavage_sites>" + System.getProperty("line.separator"));
            bw.write("  </enzyme>" + System.getProperty("line.separator"));

            bw.write("  <enzyme>" + System.getProperty("line.separator"));
            bw.write("    <name>" + DigestionParameters.CleavageParameter.unSpecific + "</name>" + System.getProperty("line.separator"));
            bw.write("    <cleavage_sites>X</cleavage_sites>" + System.getProperty("line.separator"));
            bw.write("  </enzyme>" + System.getProperty("line.separator"));

            bw.write("</enzymes>" + System.getProperty("line.separator"));

            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Could not create MS Amanda enzyme file. Unable to write file: '" + ioe.getMessage() + "'!");
        }
    }

    /**
     * Creates the settings XML file.
     */
    private void createSettingsFile() throws IllegalArgumentException {

        File settingsFile = new File(msAmandaFolder, SETTINGS_FILE);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(settingsFile));
            bw.write(
                    "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + System.getProperty("line.separator")
                    + "<settings>" + System.getProperty("line.separator")
                    + "\t<search_settings>" + System.getProperty("line.separator")
                    + "\t\t<enzyme specificity=\"" + enzymeSpecificity + "\">" + enzymeName + "</enzyme>" + System.getProperty("line.separator")
                    + "\t\t<missed_cleavages>" + missedCleavages + "</missed_cleavages>" + System.getProperty("line.separator")
                    + modificationsAsString
                    + "\t\t<instrument>" + instrument + "</instrument>" + System.getProperty("line.separator")
                    + "\t\t<ms1_tol unit=\"" + precursorUnit + "\">" + precursorMassError + "</ms1_tol> " + System.getProperty("line.separator")
                    + "\t\t<ms2_tol unit=\"" + fragmentUnit + "\">" + fragmentMassError + "</ms2_tol> " + System.getProperty("line.separator")
                    + "\t\t<max_rank>" + maxRank + "</max_rank> " + System.getProperty("line.separator")
                    + "\t\t<generate_decoy>" + generateDecoys + "</generate_decoy> " + System.getProperty("line.separator")
                    + "\t\t<PerformDeisotoping>" + performDeisotoping + "</PerformDeisotoping> " + System.getProperty("line.separator")
                    + "\t\t<MaxNoModifs>" + maxModifications + "</MaxNoModifs> " + System.getProperty("line.separator")
                    + "\t\t<MaxNoDynModifs>" + maxVariableModifications + "</MaxNoDynModifs> " + System.getProperty("line.separator")
                    + "\t\t<MaxNumberModSites>" + maxModificationSites + "</MaxNumberModSites> " + System.getProperty("line.separator")
                    + "\t\t<MaxNumberNeutralLoss>" + maxNeutralLosses + "</MaxNumberNeutralLoss> " + System.getProperty("line.separator")
                    + "\t\t<MaxNumberNeutralLossModifications>" + maxNeutralLosses + "</MaxNumberNeutralLossModifications> " + System.getProperty("line.separator")
                    + "\t\t<MinimumPepLength>" + minPeptideLength + "</MinimumPepLength> " + System.getProperty("line.separator")
                    + "\t\t<MaximumPepLength>" + maxPeptideLength + "</MaximumPepLength> " + System.getProperty("line.separator")
                    + "\t\t<ReportBothBestHitsForTD>" + reportBothBestHitsForTD + "</ReportBothBestHitsForTD> " + System.getProperty("line.separator")
                    + "\t</search_settings> " + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    + "\t<basic_settings> " + System.getProperty("line.separator")
                    + "\t\t<instruments_file>" + new File(msAmandaFolder, INSTRUMENTS_FILE).getAbsolutePath() + "</instruments_file> " + System.getProperty("line.separator")
                    + "\t\t<unimod_file>" + new File(msAmandaFolder, UNIMOD_FILE).getAbsolutePath() + "</unimod_file> " + System.getProperty("line.separator")
                    + "\t\t<enzyme_file>" + new File(msAmandaFolder, ENZYMES_FILE).getAbsolutePath() + "</enzyme_file> " + System.getProperty("line.separator")
                    + "\t\t<unimod_obo_file>" + new File(msAmandaFolder, UNIMOD_OBO_FILE).getAbsolutePath() + "</unimod_obo_file> " + System.getProperty("line.separator")
                    + "\t\t<psims_obo_file>" + new File(msAmandaFolder, PSI_MS_OBO_FILE).getAbsolutePath() + "</psims_obo_file> " + System.getProperty("line.separator")
                    + "\t\t<monoisotopic>" + monoisotopic + "</monoisotopic> " + System.getProperty("line.separator")
                    + "\t\t<considered_charges>" + getChargeRangeAsString() + "</considered_charges> " + System.getProperty("line.separator")
                    + "\t\t<LoadedProteinsAtOnce>" + maxLoadedProteins + "</LoadedProteinsAtOnce> " + System.getProperty("line.separator")
                    + "\t\t<LoadedSpectraAtOnce>" + maxLoadedSpectra + "</LoadedSpectraAtOnce> " + System.getProperty("line.separator")
                    + "\t\t<data_folder>" + msAmandaTempFolder + "</data_folder> " + System.getProperty("line.separator")
                    + "\t</basic_settings> " + System.getProperty("line.separator")
                    + "</settings>"
                    + System.getProperty("line.separator")
            );
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Could not create MS Amanda settings file. Unable to write file: '" + ioe.getMessage() + "'!");
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

        String temp = "\t\t<modifications>" + System.getProperty("line.separator");

        for (String modificationName : modificationProfile.getFixedModifications()) {
            Modification modification = modificationFactory.getModification(modificationName);
            temp += getModificationAsString(modification, true) + System.getProperty("line.separator");
        }

        for (String modificationName : modificationProfile.getVariableModifications()) {
            Modification modification = modificationFactory.getModification(modificationName);
            temp += getModificationAsString(modification, false) + System.getProperty("line.separator");
        }

        temp += "\t\t</modifications>" + System.getProperty("line.separator");

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
                cTermTag = " cterm=\"true\"";
                break;
            case modc_peptide:
            case modcaa_peptide:
                cTermTag = " cterm=\"true\"";
                break;
            case modn_protein:
            case modnaa_protein:
                proteinTag = " protein=\"true\"";
                nTermTag = " nterm=\"true\"";
                break;
            case modn_peptide:
            case modnaa_peptide:
                nTermTag = " nterm=\"true\"";
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
            return "\t\t\t<modification" + fixedTag
                    + nTermTag + cTermTag + proteinTag + ">" + cvTerm.getName() + aminoAcidsAtTarget + "</modification>";
        } else {
            return "\t\t\t<modification delta_mass=\"" + modification.getRoundedMass() + "\"" + fixedTag
                    + nTermTag + cTermTag + proteinTag + ">" + modification.getName() + aminoAcidsAtTarget + "</modification>";
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
