package eu.isas.searchgui.processbuilders;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.ions.impl.ReporterIon;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.search.SearchParameters.MassAccuracyType;
import com.compomics.util.parameters.identification.tool_specific.MetaMorpheusParameters;
import com.compomics.util.pride.CvTerm;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ProcessBuilder for the MetaMorpheus search engine.
 *
 * @author Harald Barsnes
 */
public class MetaMorpheusProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The available MetaMorpheus task types.
     */
    public enum MetaMorpheusTaskType {
        Search, Gptmd;
    }

    /**
     * The MetaMorpheus folder.
     */
    private File metaMorpheusFolder;
    /**
     * The temp folder for MetaMorpheus files.
     */
    private static String metaMorpheusTempFolderPath = null;
    /**
     * The name of the temp sub folder for MetaMorpheus files.
     */
    private static String metaMorpheusTempSubFolderName = "temp";
    /**
     * The spectrum file.
     */
    private File spectrumFile;
    /**
     * The FASTA file.
     */
    private File fastaFile;
    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The advanced MetaMorpheus parameters.
     */
    private MetaMorpheusParameters metaMorpheusParameters;
    /**
     * The post translational modifications factory.
     */
    private ModificationFactory modificationFactory = ModificationFactory.getInstance();
    /**
     * The number of threads to use.
     */
    private int numberOfThreads;

    /**
     * Constructor.
     *
     * @param metaMorpheusFolder the MetaMorpheus folder
     * @param searchParameters the search parameters
     * @param spectrumFile the spectrum file
     * @param threads the number of threads to use
     * @param outputFile the output file
     * @param fastaFile the FASTA file
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     *
     * @throws IOException thrown whenever an error occurred while reading or
     * writing a file.
     */
    public MetaMorpheusProcessBuilder(
            File metaMorpheusFolder,
            SearchParameters searchParameters,
            File spectrumFile,
            int threads,
            File fastaFile,
            File outputFile,
            WaitingHandler waitingHandler,
            ExceptionHandler exceptionHandler
    ) throws IOException {

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        this.metaMorpheusFolder = metaMorpheusFolder;
        this.searchParameters = searchParameters;
        metaMorpheusParameters = (MetaMorpheusParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.metaMorpheus.getIndex());
        this.spectrumFile = spectrumFile;
        this.numberOfThreads = threads;
        this.fastaFile = fastaFile;

        metaMorpheusTempFolderPath = getTempFolderPath(metaMorpheusFolder);

        File metaMorpheusTempFolder = new File(metaMorpheusTempFolderPath);

        if (!metaMorpheusTempFolder.exists()) {
            metaMorpheusTempFolder.mkdirs();
        }

        // make sure that the MetaMorpheus file is executable
        File metaMorpheus;
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        if (operatingSystem.contains("windows")) {
            metaMorpheus = new File(metaMorpheusFolder.getAbsolutePath() + File.separator + getExecutableFileName());
        } else {
            metaMorpheus = new File(metaMorpheusFolder.getAbsolutePath() + File.separator + getExecutableFileName());
        }
        metaMorpheus.setExecutable(true);

        // create the modification lists
        File metaMorpheusModFile = new File(metaMorpheusFolder, "Mods" + File.separator + "CustomModifications.txt");
        createModificationsFile(metaMorpheusModFile);

        // create enzyme
        File metaMorpheusEnzymesFile = new File(metaMorpheusFolder, "ProteolyticDigestion" + File.separator + "proteases.tsv");
        createEnzymesFile(metaMorpheusEnzymesFile, searchParameters.getDigestionParameters());

        // create the parameter files
        File metaMorpheusGptmdParameterFile = null;
        if (metaMorpheusParameters.runGptm()) {
            metaMorpheusGptmdParameterFile = createParameterFile(searchParameters, MetaMorpheusTaskType.Gptmd);
        }
        File metaMorpheusSearchParametersFile = createParameterFile(searchParameters, MetaMorpheusTaskType.Search);

        // add dotnet if not on windows
        if (!operatingSystem.contains("windows")) {
            String dotNetPath = "dotnet";
            if (operatingSystem.contains("mac os x")) {
                dotNetPath = "/usr/local/share/dotnet/dotnet";
            }
            process_name_array.add(dotNetPath);
        }

        // full path to executable
        process_name_array.add(metaMorpheus.getAbsolutePath());

        // the protein sequence file
        process_name_array.add("-d");
        process_name_array.add(fastaFile.getAbsolutePath()); // @TODO: also support uniprot xml?

        // the spectrum file
        process_name_array.add("-s");
        process_name_array.add(spectrumFile.getAbsolutePath());

        // the parameters file
        process_name_array.add("-t");
        if (metaMorpheusParameters.runGptm() && metaMorpheusGptmdParameterFile != null) {
            process_name_array.add(metaMorpheusGptmdParameterFile.getAbsolutePath());
        }
        process_name_array.add(metaMorpheusSearchParametersFile.getAbsolutePath());

        // the output folder
        process_name_array.add("-o");
        process_name_array.add(metaMorpheusTempFolderPath);

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator")
                + System.getProperty("line.separator") + "MetaMorpheus command: ");

        for (Object currentElement : process_name_array) {
            System.out.print(currentElement + " ");
        }

        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);
        pb.directory(metaMorpheusFolder);

        // set error out and std out to same stream
        pb.redirectErrorStream(true);
    }

    /**
     * Returns the name of the MetaMorpheus executable.
     *
     * @return the name of the MetaMorpheus executable
     */
    public static String getExecutableFileName() {

        String operatingSystem = System.getProperty("os.name").toLowerCase();

        if (operatingSystem.contains("windows")) {
            return "CMD.exe";
        } else {
            return "CMD.dll";
        }
    }

    /**
     * Creates a MetaMorpheus parameter file.
     *
     * @param searchParameters the file where to save the search parameters
     * @param taskType the task type
     *
     * @return the parameter file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * writing the configuration file
     */
    private File createParameterFile(SearchParameters searchParameters, MetaMorpheusTaskType taskType) throws IOException {

        File parameterFile = new File(metaMorpheusTempFolderPath, taskType.toString() + ".toml");
        BufferedWriter bw = new BufferedWriter(new FileWriter(parameterFile));

        try {
            String enzymeName = "";
            Integer missedCleavages = null;

            DigestionParameters digestionParameters = searchParameters.getDigestionParameters();

            if (digestionParameters.getCleavageParameter() == DigestionParameters.CleavageParameter.wholeProtein) {
                enzymeName = "Whole Protein";
                missedCleavages = 0;
            } else if (digestionParameters.getCleavageParameter() == DigestionParameters.CleavageParameter.unSpecific) {
                enzymeName = "Unspecific";
                missedCleavages = 24;
            } else if (digestionParameters.getEnzymes().size() > 1) {
                throw new IOException("Multiple enzymes not supported!");
            } else {
                Enzyme enzyme = digestionParameters.getEnzymes().get(0);
                enzymeName = enzyme.getName();
                missedCleavages = digestionParameters.getnMissedCleavages(enzymeName);
            }

            // task type
            bw.write("TaskType = \"" + taskType.toString() + "\"" + System.getProperty("line.separator"));
            bw.newLine();

            //////////////////////////
            // gptmd parameters
            //////////////////////////
            if (taskType == MetaMorpheusTaskType.Gptmd) {
                bw.write("[GptmdParameters]" + System.getProperty("line.separator"));
                bw.write("ListOfModsGptmd = \"");
                writeModifications(modificationFactory.getModifications(ModificationCategory.Common_Biological), bw);
                writeModifications(modificationFactory.getModifications(ModificationCategory.Common_Artifact), bw);
                writeModifications(modificationFactory.getModifications(ModificationCategory.Metal), bw);
                // @TODO: re-add the substitutions
                //writeModifications(modificationFactory.getModifications(ModificationCategory.Nucleotide_Substitution_One), bw);
                bw.write("\"" + System.getProperty("line.separator") + System.getProperty("line.separator"));
            }

            //////////////////////////
            // search parameters
            //////////////////////////
            if (taskType == MetaMorpheusTaskType.Search) {
                bw.write("[SearchParameters]" + System.getProperty("line.separator"));
                bw.write("DisposeOfFileWhenDone = false" + System.getProperty("line.separator"));
                bw.write("DoParsimony = true" + System.getProperty("line.separator")); // NOTE: if false, the mzid file is not created!
                bw.write("ModPeptidesAreDifferent = " + metaMorpheusParameters.getModPeptidesAreDifferent() + System.getProperty("line.separator"));
                bw.write("NoOneHitWonders = " + metaMorpheusParameters.getNoOneHitWonders() + System.getProperty("line.separator"));
                bw.write("MatchBetweenRuns = false" + System.getProperty("line.separator"));
                bw.write("Normalize = false" + System.getProperty("line.separator"));
                bw.write("QuantifyPpmTol = 5.0" + System.getProperty("line.separator"));
                bw.write("DoHistogramAnalysis = false" + System.getProperty("line.separator"));
                bw.write("SearchTarget = " + metaMorpheusParameters.getSearchTarget() + System.getProperty("line.separator"));
                bw.write("DecoyType = \"" + metaMorpheusParameters.getDecoyType() + "\"" + System.getProperty("line.separator"));
                bw.write("MassDiffAcceptorType = \"" + metaMorpheusParameters.getMassDiffAcceptorType() + "\"" + System.getProperty("line.separator"));
                bw.write("WritePrunedDatabase = false" + System.getProperty("line.separator"));
                bw.write("KeepAllUniprotMods = true" + System.getProperty("line.separator"));
                bw.write("DoLocalizationAnalysis = true" + System.getProperty("line.separator"));
                bw.write("DoQuantification = false" + System.getProperty("line.separator"));
                bw.write("SearchType = \"" + metaMorpheusParameters.getSearchType() + "\"" + System.getProperty("line.separator"));
                bw.write("LocalFdrCategories = [\"FullySpecific\"]" + System.getProperty("line.separator"));
                bw.write("MaxFragmentSize = " + metaMorpheusParameters.getMaxFragmentSize() + System.getProperty("line.separator"));
                bw.write("HistogramBinTolInDaltons = 0.003" + System.getProperty("line.separator"));
                bw.write("MaximumMassThatFragmentIonScoreIsDoubled = 0.0" + System.getProperty("line.separator"));
                bw.write("WriteMzId = " + metaMorpheusParameters.getWriteMzId() + System.getProperty("line.separator"));
                bw.write("WritePepXml = " + metaMorpheusParameters.getWritePepXml() + System.getProperty("line.separator"));
                bw.write("WriteDecoys = true" + System.getProperty("line.separator"));
                bw.write("WriteContaminants = true" + System.getProperty("line.separator"));
                bw.newLine();

                //////////////////////////////////
                // modification output parameters
                //////////////////////////////////
                bw.write("[SearchParameters.ModsToWriteSelection]" + System.getProperty("line.separator"));
                bw.write("'N-linked glycosylation' = 3" + System.getProperty("line.separator"));
                bw.write("'O-linked glycosylation' = 3" + System.getProperty("line.separator"));
                bw.write("'Other glycosylation' = 3" + System.getProperty("line.separator"));
                bw.write("'Common Biological' = 3" + System.getProperty("line.separator"));
                bw.write("'Less Common' = 3" + System.getProperty("line.separator"));
                bw.write("Metal = 3" + System.getProperty("line.separator"));
                bw.write("'2+ nucleotide substitution' = 3" + System.getProperty("line.separator"));
                bw.write("'1 nucleotide substitution' = 3" + System.getProperty("line.separator"));
                bw.write("UniProt = 2" + System.getProperty("line.separator"));
                bw.newLine();
            }

            //////////////////////////
            // common parameters
            //////////////////////////
            bw.write("[CommonParameters]" + System.getProperty("line.separator"));
            bw.write("MaxThreadsToUsePerFile = " + numberOfThreads + System.getProperty("line.separator"));

            // fixed modifications
            bw.write("ListOfModsFixed = \"");
            writeModifications(searchParameters.getModificationParameters().getFixedModifications(), bw);
            bw.write("\"" + System.getProperty("line.separator"));

            // variable modifications
            bw.write("ListOfModsVariable = \"");
            writeModifications(searchParameters.getModificationParameters().getVariableModifications(), bw);
            bw.write("\"" + System.getProperty("line.separator"));

            bw.write("DoPrecursorDeconvolution = " + metaMorpheusParameters.getDoPrecursorDeconvolution() + System.getProperty("line.separator"));
            bw.write("UseProvidedPrecursorInfo = " + metaMorpheusParameters.getUseProvidedPrecursorInfo() + System.getProperty("line.separator"));
            bw.write("DeconvolutionIntensityRatio = " + metaMorpheusParameters.getDeconvolutionIntensityRatio() + System.getProperty("line.separator"));
            bw.write("DeconvolutionMaxAssumedChargeState = " + searchParameters.getMaxChargeSearched() + System.getProperty("line.separator"));
            bw.write("DeconvolutionMassTolerance = \"Â±" + metaMorpheusParameters.getDeconvolutionMassTolerance()
                    + metaMorpheusParameters.getDeconvolutionMassToleranceType() + " \"" + System.getProperty("line.separator"));
            bw.write("TotalPartitions = 1" + System.getProperty("line.separator"));

            // fragment and precursor tolerances
            bw.write("ProductMassTolerance = \"Â±" + searchParameters.getFragmentIonAccuracy());
            if (searchParameters.getFragmentAccuracyType() == MassAccuracyType.PPM) {
                bw.write(" PPM\"" + System.getProperty("line.separator"));
            } else {
                bw.write(" Absolute\"" + System.getProperty("line.separator"));
            }
            bw.write("PrecursorMassTolerance = \"Â±" + searchParameters.getPrecursorAccuracy());
            if (searchParameters.getPrecursorAccuracyType() == MassAccuracyType.PPM) {
                bw.write(" PPM\"" + System.getProperty("line.separator"));
            } else {
                bw.write(" Absolute\"" + System.getProperty("line.separator"));
            }

            bw.write("AddCompIons = false" + System.getProperty("line.separator"));
            bw.write("ScoreCutoff = " + metaMorpheusParameters.getScoreCutoff() + System.getProperty("line.separator"));
            bw.write("ReportAllAmbiguity = true" + System.getProperty("line.separator"));
            bw.write("NumberOfPeaksToKeepPerWindow = " + metaMorpheusParameters.getNumberOfPeaksToKeepPerWindow() + System.getProperty("line.separator"));
            bw.write("MinimumAllowedIntensityRatioToBasePeak = " + metaMorpheusParameters.getMinAllowedIntensityRatioToBasePeak() + System.getProperty("line.separator"));

            if (metaMorpheusParameters.getWindowWidthThomsons() != null) {
                bw.write("WindowWidthThomsons = " + metaMorpheusParameters.getWindowWidthThomsons() + System.getProperty("line.separator"));
            }
            if (metaMorpheusParameters.getNumberOfWindows() != null) {
                bw.write("NumberOfWindows = " + metaMorpheusParameters.getNumberOfWindows() + System.getProperty("line.separator"));
            }

            bw.write("NormalizePeaksAccrossAllWindows = " + metaMorpheusParameters.getNormalizePeaksAcrossAllWindows() + System.getProperty("line.separator"));
            bw.write("TrimMs1Peaks = " + metaMorpheusParameters.getTrimMs1Peaks() + System.getProperty("line.separator"));
            bw.write("TrimMsMsPeaks = " + metaMorpheusParameters.getTrimMsMsPeaks() + System.getProperty("line.separator"));
            bw.write("UseDeltaScore = " + metaMorpheusParameters.getUseDeltaScore() + System.getProperty("line.separator"));
            bw.write("QValueOutputFilter = 0.0" + System.getProperty("line.separator"));
            bw.write("CustomIons = []" + System.getProperty("line.separator"));
            bw.write("AssumeOrphanPeaksAreZ1Fragments = true" + System.getProperty("line.separator"));
            bw.write("MaxHeterozygousVariants = " + metaMorpheusParameters.getMaxHeterozygousVariants() + System.getProperty("line.separator"));
            bw.write("MinVariantDepth = " + metaMorpheusParameters.getMinVariantDepth() + System.getProperty("line.separator"));
            bw.write("DissociationType = \"" + metaMorpheusParameters.getDissociationType() + "\"" + System.getProperty("line.separator"));
            bw.write("ChildScanDissociationType = \"Unknown\"" + System.getProperty("line.separator"));
            bw.newLine();

            //////////////////////////
            // digestion parameters
            //////////////////////////
            bw.write("[CommonParameters.DigestionParams]" + System.getProperty("line.separator"));
            bw.write("MaxMissedCleavages = " + missedCleavages + System.getProperty("line.separator"));
            bw.write("InitiatorMethionineBehavior = \"Variable\"" + System.getProperty("line.separator"));
            bw.write("MinPeptideLength = " + metaMorpheusParameters.getMinPeptideLength() + System.getProperty("line.separator"));
            bw.write("MaxPeptideLength = " + metaMorpheusParameters.getMaxPeptideLength() + System.getProperty("line.separator"));
            bw.write("MaxModificationIsoforms = " + metaMorpheusParameters.getMaxModificationIsoforms() + System.getProperty("line.separator"));
            bw.write("MaxModsForPeptide = " + metaMorpheusParameters.getMaxModsForPeptide() + System.getProperty("line.separator"));
            bw.write("Protease = \"" + enzymeName + "\"" + System.getProperty("line.separator"));
            bw.write("SearchModeType = \"Full\"" + System.getProperty("line.separator"));
            bw.write("FragmentationTerminus = \"" + metaMorpheusParameters.getFragmentationTerminus() + "\"" + System.getProperty("line.separator"));
            bw.write("SpecificProtease = \"" + enzymeName + "\"" + System.getProperty("line.separator"));
            bw.write("GeneratehUnlabeledProteinsForSilac = false" + System.getProperty("line.separator"));

        } finally {
            bw.close();
        }

        return parameterFile;
    }

    @Override
    public String getType() {
        return "MetaMorpheus";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return spectrumFile.getName();
    }

    /**
     * Returns the temp folder path. Instantiates if null.
     *
     * @param metaMorpheusFolder the MetaMorpheus folder
     *
     * @return the temp folder path
     */
    public static String getTempFolderPath(File metaMorpheusFolder) {

        if (metaMorpheusTempFolderPath == null) {

            metaMorpheusTempFolderPath = metaMorpheusFolder.getAbsolutePath()
                    + File.separator + metaMorpheusTempSubFolderName;

        }

        return metaMorpheusTempFolderPath;

    }

    /**
     * Creates the MetaMorpheus enzymes file.
     *
     * @param metaMorpheusEnzymesFile the MetaMorpheus enzyme file
     * @param digestionPreferences the digestion preferences
     *
     * @throws IOException if the enzymes file could not be written
     */
    private void createEnzymesFile(File metaMorpheusEnzymesFile, DigestionParameters digestionPreferences) throws IOException {

        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(metaMorpheusEnzymesFile));

            try {

                // write the header
                bw.write("Name\t"
                        + "Sequences Inducing Cleavage\t"
                        + "Sequences Preventing Cleavage\t"
                        + "Cleavage Terminus\t"
                        + "Cleavage Specificity\t"
                        + "PSI-MS Accession Number\t"
                        + "PSI-MS Name\t"
                        + "Site Regular Expression\t"
                        + "Notes");
                bw.newLine();

                // dummy trypsin, as otherwise metamorpheus refuses to start... // @TODO: figure out why!
                bw.write("trypsin\tK|,R|\t\t\tfull\tMS:1001313\tTrypsin/P\t(?<=[KR])");
                bw.newLine();

                if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.wholeProtein) {
                    bw.write("Whole Protein\t\t\t\tnone\tMS:1001955\tno cleavage");
                    bw.newLine();
                } else if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.unSpecific) {
                    bw.write("Unspecific\tX|\t\t\tfull\tMS:1001956\tunspecific cleavage");
                    bw.newLine();
                } else if (digestionPreferences.getEnzymes().size() > 1) {
                    throw new IOException("Multiple enzymes not supported!");
                } else {

                    Enzyme enzyme = digestionPreferences.getEnzymes().get(0);

                    String enzymeName = enzyme.getName();

                    // name
                    bw.write(enzymeName + "\t");

                    // sequence inducing cleavage 
                    String cleavageSite = "";

                    if (!enzyme.getAminoAcidBefore().isEmpty()) {
                        for (Character cleaveCharacter : enzyme.getAminoAcidBefore()) {
                            if (!enzyme.getRestrictionAfter().isEmpty()) {
                                for (Character restrictCharacter : enzyme.getRestrictionAfter()) {
                                    if (!cleavageSite.isEmpty()) {
                                        cleavageSite += ",";
                                    }
                                    cleavageSite += cleaveCharacter + "|[" + restrictCharacter + "]";
                                }
                            } else {
                                if (!cleavageSite.isEmpty()) {
                                    cleavageSite += ",";
                                }
                                cleavageSite += cleaveCharacter + "|";
                            }
                        }
                    } else {
                        for (Character cleaveCharacter : enzyme.getAminoAcidAfter()) {
                            if (!enzyme.getRestrictionBefore().isEmpty()) {
                                for (Character restrictCharacter : enzyme.getRestrictionBefore()) {
                                    if (!cleavageSite.isEmpty()) {
                                        cleavageSite += ",";
                                    }
                                    cleavageSite += "[" + restrictCharacter + "]|" + cleaveCharacter;
                                }
                            } else {
                                if (!cleavageSite.isEmpty()) {
                                    cleavageSite += ",";
                                }
                                cleavageSite += "|" + cleaveCharacter;
                            }
                        }
                    }

                    bw.write(cleavageSite + "\t\t\t");

                    // cleavage specificity
                    DigestionParameters.Specificity specificity = digestionPreferences.getSpecificity(enzymeName);
                    if (null != specificity) {
                        if (specificity == DigestionParameters.Specificity.specific) {
                            bw.write("full\t");
                        } else {
                            bw.write("semi\t");
                        }
                    }

                    // psi-ms accesion number and name
                    if (enzyme.getCvTerm() != null) {
                        bw.write(enzyme.getCvTerm().getAccession() + "\t");
                        bw.write(enzyme.getCvTerm().getName() + "\t");
                    } else {
                        bw.write("\t\t");
                    }

                    // site regular expresssion, e.g. for chymotrypsin (?<=[FYWL])(?!P)
                    // bw.write("(?<=[FYWL])(?!P)"); // @TODO: add regular expressions?
                    bw.write("\t");

                    // notes
                    bw.write("\t");

                    bw.newLine();
                }

            } finally {
                bw.close();
            }
        } catch (IOException ioe) {
            throw new IOException("Could not create MetaMorpheus enzymes file. Unable to write file: '" + ioe.getMessage() + "'.");
        }
    }

    /**
     * Creates the MetaMorpheus modifications file.
     *
     * @param metaMorpheusModFile the MetaMorpheus modification file
     *
     * @throws IOException if the modification file could not be created
     */
    private void createModificationsFile(File metaMorpheusModFile) throws IOException {

        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(metaMorpheusModFile));

            try {
                bw.write("Custom Modifications\n");

                // add the fixed modifications
                ArrayList<String> fixedModifications = searchParameters.getModificationParameters().getFixedModifications();
                for (String modName : fixedModifications) {
                    bw.write(getModificationFormattedForMetaMorpheus(modName));
                }

                // add the variable modifications
                ArrayList<String> variableModifications = searchParameters.getModificationParameters().getVariableModifications();
                for (String modName : variableModifications) {
                    bw.write(getModificationFormattedForMetaMorpheus(modName));
                }

                // add the open search modifications
                if (metaMorpheusParameters.runGptm()) {
                    ArrayList<String> openSearchModifications = modificationFactory.getModifications(
                            ModificationCategory.Common_Biological,
                            ModificationCategory.Common_Artifact,
                            ModificationCategory.Metal,
                            ModificationCategory.Nucleotide_Substitution_One // @TODO: make the ptm selection user-selectable
                    );

                    // make sure that no ptm is added more than once
                    for (String fixedMod : fixedModifications) {
                        openSearchModifications.remove(fixedMod);
                    }
                    for (String variableMod : variableModifications) {
                        openSearchModifications.remove(variableMod);
                    }

                    // write the gptm modifications to the file
                    for (String modName : openSearchModifications) {
                        bw.write(getModificationFormattedForMetaMorpheus(modName));
                    }
                }

            } finally {
                bw.close();
            }
        } catch (IOException ioe) {

            throw new IllegalArgumentException(
                    "Could not create MetaMorpheus modifications file. "
                    + "Unable to write file: '" + ioe.getMessage() + "'.");

        }
    }

    /**
     * Get the given modification as a string in the MetaMorpheus format.
     *
     * @param modName the utilities name of the modification
     * @param fixed if the modification is fixed or not
     * @return the given modification as a string in the MetaMorpheus format
     */
    private String getModificationFormattedForMetaMorpheus(String modName) {

        Modification modification = modificationFactory.getModification(modName);

        // Example:
        //  ID   Phosphorylation
        //  TG   Y
        //  PP   Anywhere.
        //  NL   HCD:H0 or HCD:H3 O4 P1
        //  MT   Common Biological
        //  CF   H1 O3 P1
        //  DI   HCD:C8 H10 N1 O4 P1
        //  DR   Unimod; 21.
        //  //
        // the id
        String tempModName = modification.getName().replaceAll(" of ", " off "); // temporary fix given that MetaMorpheus kicks out ptms with " of " in the name...
        String modificationAsString = "ID   " + tempModName + "\n";

        // the targeted amino acids
        modificationAsString += "TG   ";

        String aminoAcidsAtTarget = "";
        AminoAcidPattern aminoAcidPattern = modification.getPattern();

        if (aminoAcidPattern != null) {
            for (Character aa : modification.getPattern().getAminoAcidsAtTarget()) {
                if (!aminoAcidsAtTarget.isEmpty()) {
                    aminoAcidsAtTarget += " or ";
                }
                aminoAcidsAtTarget += aa;
            }
        }

        if (aminoAcidsAtTarget.length() == 0) {
            aminoAcidsAtTarget = "X";
        }

        modificationAsString += aminoAcidsAtTarget + "\n";

        // the type of the modification
        modificationAsString += "PP   ";

        String position = "";
        switch (modification.getModificationType()) {
            case modaa:
                position = "Anywhere.";
                break;
            case modc_protein:
            case modcaa_protein:
                position = "C-terminal.";
                break;
            case modc_peptide:
            case modcaa_peptide:
                position = "Peptide C-terminal.";
                break;
            case modn_protein:
            case modnaa_protein:
                position = "N-terminal.";
                break;
            case modn_peptide:
            case modnaa_peptide:
                position = "Peptide N-terminal.";
                break;
            default:
                throw new UnsupportedOperationException("Modification type " + modification.getModificationType() + " not supported.");
        }

        modificationAsString += position + "\n";

        // the neutral losses
        for (NeutralLoss tempNeutralLoss : modification.getNeutralLosses()) {
            modificationAsString += "NL   "
                    + tempNeutralLoss.getComposition().getStringValue(
                            true, false, true, true, false, true) + "\n";
        }

        // the modification type
        modificationAsString += "MT   " + modification.getCategory() + "\n";

        // chemical formula
        modificationAsString += "CF   ";
        if (modification.getAtomChainAdded().size() > 0) {
            modificationAsString
                    += modification.getAtomChainAdded().getStringValue(true, false, true, true, false, true)
                    + " ";
        }
        if (modification.getAtomChainRemoved().size() > 0) {
            modificationAsString
                    += modification.getAtomChainRemoved().getStringValue(true, false, true, true, true, true);
        }
        modificationAsString += "\n";

        // diagnostic ions
        for (ReporterIon tempReporterIon : modification.getReporterIons()) {
            modificationAsString += "DI   "
                    + tempReporterIon.getAtomicComposition().getStringValue(
                            true, false, true, true, false, true) + "\n";
        }

        // add unimod name
        CvTerm cvTerm = modification.getUnimodCvTerm();
        if (cvTerm != null) {
            String completeAccession = cvTerm.getAccession();
            modificationAsString += "DR   Unimod; " + completeAccession.substring(7) + ".\n";
        }

        modificationAsString += "//\n";

        return modificationAsString;
    }

    /**
     * Write the list of modifications to the toml file.
     *
     * @param modifications the list of modification names
     * @param bw the buffered writer
     * @throws IOException thrown if an IO exception occurs
     */
    private void writeModifications(ArrayList<String> modifications, BufferedWriter bw) throws IOException {

        for (int i = 0; i < modifications.size(); i++) {

            if (i > 0) {
                bw.write("\\t\\t");
            }

            String modName = modifications.get(i);
            String tempModName = modName.replaceAll(" of ", " off "); // temporary fix given that MetaMorpheus kicks out ptms with " of " in the name...

            Modification tempModification = modificationFactory.getModification(modName);

            AminoAcidPattern aminoAcidPattern = tempModification.getPattern();

            if (aminoAcidPattern != null) {

                if (!aminoAcidPattern.getAminoAcidsAtTarget().isEmpty()) {
                    for (Character residue : aminoAcidPattern.getAminoAcidsAtTarget()) {
                        bw.write(tempModification.getCategory() + "\\t" + tempModName + " on " + residue);
                    }
                } else {
                    bw.write(tempModification.getCategory() + "\\t" + tempModName + " on X");
                }

            }
        }
    }
}
