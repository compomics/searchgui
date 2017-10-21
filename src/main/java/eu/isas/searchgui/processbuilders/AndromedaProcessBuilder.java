package eu.isas.searchgui.processbuilders;

import com.compomics.util.Util;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.atoms.AtomChain;
import com.compomics.util.experiment.biology.atoms.AtomImpl;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.ions.impl.ReporterIon;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.io.biology.protein.converters.GenericFastaConverter;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.AndromedaParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * ProcessBuilder for the Andromeda search engine.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class AndromedaProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The Andromeda folder.
     */
    private File andromedaFolder;
    /**
     * The temp folder for Andromeda files.
     */
    private static String andromedaTempFolderPath = null;
    /**
     * The name of the temp sub folder for Andromeda files.
     */
    private static String andromedaTempSubFolderName = "temp";
    /**
     * The sub folder containing the apar files.
     */
    private final static String APAR_FOLDER = "apar_files";
    /**
     * The spectrum file.
     */
    private File spectrumFile;
    /**
     * The name of the Andromeda executable.
     */
    public static final String EXECUTABLE_FILE_NAME = "AndromedaCmd.exe";
    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The advanced Andromeda parameters.
     */
    private AndromedaParameters andromedaParameters;

    /**
     * Constructor.
     *
     * @param andromedaFolder the Andromeda folder
     * @param searchParameters the search parameters
     * @param searchParametersFile the file where to save the search parameters
     * @param spectrumFile the spectrum file
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     * @param nThreads the number of threads
     *
     * @throws IOException thrown whenever an error occurred while reading or
     * writing a file.
     */
    public AndromedaProcessBuilder(File andromedaFolder, SearchParameters searchParameters, File searchParametersFile, File spectrumFile, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, int nThreads) throws IOException {

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        this.andromedaFolder = andromedaFolder;
        this.searchParameters = searchParameters;
        andromedaParameters = (AndromedaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex());
        this.spectrumFile = spectrumFile;

        if (andromedaTempFolderPath == null) {

            andromedaTempFolderPath = getTempFolderPath(andromedaFolder);

        }

        File andromedaTempFolder = new File(andromedaTempFolderPath);

        if (!andromedaTempFolder.exists()) {

            andromedaTempFolder.mkdirs();

        }

        // make sure that the andromeda file is executable
        File andromeda = new File(andromedaFolder.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        andromeda.setExecutable(true);

        // create parameters file
        File andromedaParametersFile = createParametersFile(searchParametersFile);

        // full path to executable
        process_name_array.add(andromeda.getAbsolutePath());

        // the input file
        process_name_array.add("-i");
        process_name_array.add(spectrumFile.getAbsolutePath());

        // the parameters file
        process_name_array.add("-p");
        process_name_array.add(andromedaParametersFile.getAbsolutePath());

        // the working folder
        process_name_array.add("-f");
        process_name_array.add(andromedaTempFolder.getAbsolutePath());

        // the number of threads
        process_name_array.add("-n");
        process_name_array.add(nThreads + "");

        // protein summary
        process_name_array.add("-s");
        process_name_array.add("FALSE");

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "andromeda command: ");

        for (Object currentElement : process_name_array) {
            System.out.print(currentElement + " ");
        }

        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);
        pb.directory(andromedaFolder);

        // set error out and std out to same stream
        pb.redirectErrorStream(true);
    }

    /**
     * Returns the temp folder path. Instantiates if null.
     *
     * @param andromedaFolder the Andromeda folder
     *
     * @return the temp folder path
     */
    public static String getTempFolderPath(File andromedaFolder) {

        if (andromedaTempFolderPath == null) {

            andromedaTempFolderPath = andromedaFolder.getAbsolutePath() + File.separator + andromedaTempSubFolderName;

        }

        return andromedaTempFolderPath;

    }

    /**
     * Returns the generic fasta file corresponding to the given fasta file.
     *
     * @param andromedaFolder the andromeda folder
     * @param fastaFileName the fasta file name
     *
     * @return the generic fasta file corresponding to the given fasta file
     */
    public static File getGenericFastaFile(File andromedaFolder, String fastaFileName) {

        return new File(getTempFolderPath(andromedaFolder), fastaFileName);

    }

    /**
     * Create the fasta file.
     *
     * @param andromedaFolder the Andromeda folder
     * @param fastaFile the original fasta file
     * @param waitingHandler the waiting handler
     *
     * @return the parameters file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading or writing a file
     */
    public static File createGenericFastaFile(File andromedaFolder, File fastaFile, WaitingHandler waitingHandler) throws IOException {

        String andromedaTempFolder = getTempFolderPath(andromedaFolder);
        File andromedaFile = new File(andromedaTempFolder, fastaFile.getName());

        if (!andromedaFile.exists()) {

            GenericFastaConverter.convertFile(fastaFile, andromedaFile, waitingHandler);

        }

        return andromedaFile;

    }

    /**
     * Creates the database configuration file.
     *
     * @param andromedaFolder the Andromeda installation folder
     * @param searchParameters the search parameters
     *
     * @throws IOException exception thrown whenever an error occurred while
     * writing the database configuration file.
     */
    public static void createDatabaseFile(File andromedaFolder, SearchParameters searchParameters) throws IOException {

        File databaseFolder = new File(andromedaFolder, "conf");
        File databaseFile = new File(databaseFolder, "databases.xml");
        File genericFastaFile = getGenericFastaFile(andromedaFolder, searchParameters.getFastaFile().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(databaseFile));
        String dbName = genericFastaFile.getName();
        String date = "0001-01-01T00:00:00";

        try {
            bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            bw.newLine();
            bw.write("<databases xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
            bw.newLine();
            bw.write("   <database create_date=\"" + date
                    + "\" last_modified_date=\""
                    + date + "\" user=\"SearchGUI\" filename=\""
                    + dbName + "\" search_expression=\""
                    + ">generic|\\([^|]*\\)|(.*)\" mutation_parse_rule=\"\" "
                    + "species=\"Homo sapiens (Human)\" taxid=\"9606\" "
                    + "source=\"UniprotKB\" />"); //@TODO: add species and source
            bw.newLine();
            bw.write("</databases>");
            bw.newLine();

        } finally {
            bw.close();
        }
    }

    /**
     * Creates the enzyme configuration file.
     *
     * @param andromedaFolder the Andromeda installation folder
     *
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file.
     */
    public static void createEnzymesFile(File andromedaFolder) throws IOException {
        EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
        File file = new File(andromedaFolder, "conf");
        file = new File(file, "enzymes.xml");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        int index = 0;
        String date = "0001-01-01T00:00:00";
        try {
            bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            bw.newLine();
            bw.write("<enzymes xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
            bw.newLine();
            for (Enzyme enzyme : enzymeFactory.getEnzymes()) {

                bw.write("   <enzyme index=\"" + index
                        + "\" title=\"" + enzyme.getName()
                        + "\" description=\"" + enzyme.getDescription()
                        + "\" create_date=\"" + date
                        + "\" last_modified_date=\"" + date
                        + "\" user=\"SearchGUI\">");
                bw.newLine();
                bw.write("      <specificity>");
                bw.newLine();
                ArrayList<Character> aaBefore = new ArrayList<>(enzyme.getAminoAcidBefore());
                if (aaBefore.isEmpty()) {
                    for (char aa : AminoAcid.getUniqueAminoAcids()) {
                        aaBefore.add(aa);
                    }
                }
                ArrayList<Character> aaAfter = new ArrayList<>(enzyme.getAminoAcidAfter());
                if (aaAfter.isEmpty()) {
                    for (char aa : AminoAcid.getUniqueAminoAcids()) {
                        aaAfter.add(aa);
                    }
                }
                for (Character aa1 : aaBefore) {
                    for (Character aa2 : aaAfter) {
                        if (!enzyme.getRestrictionBefore().contains(aa1) && !enzyme.getRestrictionAfter().contains(aa2)) {
                            bw.write("         <string>" + aa1 + aa2 + "</string>");
                            bw.newLine();
                        }
                    }
                }
                bw.write("      </specificity>");
                bw.newLine();
                bw.write("    </enzyme>");
                bw.newLine();
                index++;
            }
            bw.write("</enzymes>");
            bw.newLine();
        } finally {
            bw.close();
        }
    }

    /**
     * Creates the Andromeda PTM file and saves the PTM indexes in the search
     * parameters.
     *
     * @param andromedaFolder the Andromeda installation folder
     * @param identificationParameters the identification parameters
     * @param identificationParametersFile the file where to save the search
     * parameters
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing to the file.
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while saving the search parameters
     */
    public static void createPtmFile(File andromedaFolder, IdentificationParameters identificationParameters, File identificationParametersFile) throws IOException, ClassNotFoundException {

        File file = new File(andromedaFolder, "conf");
        file = new File(file, "modifications.xml");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        int index = 0;
        String date = "0001-01-01T00:00:00";

        ModificationFactory ptmFactory = ModificationFactory.getInstance();
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        AndromedaParameters andromedaParameters = (AndromedaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex());

        try {
            bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            bw.newLine();
            bw.write("<modifications xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
            bw.newLine();

            // add the default ptms
            for (String ptmName : ptmFactory.getDefaultModifications()) {
                Modification ptm = ptmFactory.getModification(ptmName);
                writePtm(bw, date, ptm, index);
                andromedaParameters.setPtmIndex(ptmName, index);
                index++;
            }

            // add the user ptms
            for (String ptmName : ptmFactory.getUserModifications()) {
                Modification ptm = ptmFactory.getModification(ptmName);
                writePtm(bw, date, ptm, index);
                andromedaParameters.setPtmIndex(ptmName, index);
                index++;
            }

            bw.write("</modifications>");
            bw.newLine();
        } finally {
            bw.close();
        }

        IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
    }

    /**
     * Writes the XML bloc corresponding to the modification via the given
     * writer.
     *
     * @param bw a buffered writer
     * @param date the date to use as creation
     * @param modification the modification to write
     * @param index the index of the modification in the list
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing to the file.
     */
    private static void writePtm(BufferedWriter bw, String date, Modification modification, int index) throws IOException {

        bw.write("   <modification index=\"" + index
                + "\" title=\"" + modification.getName()
                + "\" description=\"" + modification.getName()
                + "\" create_date=\"" + date
                + "\" last_modified_date=\"" + date
                + "\" user=\"SearchGUI\" "
                + "reporterCorrectionM2=\"0\" reporterCorrectionM1=\"0\" reporterCorrectionP1=\"0\" reporterCorrectionP2=\"0\" "
                + "composition=\"" + getComposition(modification) + "\" multi_modification=\"false\">");
        bw.newLine();
        if (null == modification.getModificationType()) {
            throw new IllegalArgumentException("Export not implemented for PTM of type " + modification.getModificationType() + ".");
        } else {
            switch (modification.getModificationType()) {
                case modaa:
                    bw.write("      <position>anywhere</position>");
                    break;
                case modn_protein:
                    bw.write("      <position>proteinNterm</position>");
                    break;
                case modnaa_protein:
                    bw.write("      <position>proteinNterm</position>");
                    break;
                case modn_peptide:
                    bw.write("      <position>anyNterm</position>");
                    break;
                case modnaa_peptide:
                    bw.write("      <position>anyNterm</position>");
                    break;
                case modc_protein:
                    bw.write("      <position>proteinCterm</position>");
                    break;
                case modcaa_protein:
                    bw.write("      <position>proteinCterm</position>");
                    break;
                case modc_peptide:
                    bw.write("      <position>anyCterm</position>");
                    break;
                case modcaa_peptide:
                    bw.write("      <position>anyCterm</position>");
                    break;
                default:
                    throw new IllegalArgumentException("Export not implemented for PTM of type " + modification.getModificationType() + ".");
            }
        }
        bw.newLine();
        int siteIndex = 0;
        AminoAcidPattern aminoAcidPattern = modification.getPattern();
        if (aminoAcidPattern != null && !aminoAcidPattern.getAminoAcidsAtTarget().isEmpty()) {
            for (Character aa : aminoAcidPattern.getAminoAcidsAtTarget()) {
                bw.write("      <modification_site index=\"" + siteIndex + "\" site=\"" + aa + "\">");
                bw.newLine();
                siteIndex++;
                ArrayList<NeutralLoss> neutralLosses = modification.getNeutralLosses();
                if (!neutralLosses.isEmpty()) {
                    bw.write("         <neutralloss_collection>");
                    bw.newLine();
                    for (NeutralLoss neutralLoss : neutralLosses) {
                        if (neutralLoss.getComposition() != null) {
                            bw.write("            <neutralloss name=\"" + neutralLoss.name
                                    + "\" shortname=\"" + neutralLoss.name
                                    + "\" composition=\"" + getComposition(neutralLoss.getComposition()) + "\" />");
                            bw.newLine();
                        }
                    }
                    bw.write("         </neutralloss_collection>");
                    bw.newLine();
                } else {
                    bw.write("         <neutralloss_collection />");
                    bw.newLine();
                }
                ArrayList<ReporterIon> reporterIons = modification.getReporterIons();
                if (!reporterIons.isEmpty()) {
                    bw.write("         <diagnostic_collection>");
                    bw.newLine();
                    for (ReporterIon reporterIon : reporterIons) {
                        if (reporterIon.getAtomicComposition() != null) {
                            bw.write("            <diagnostic name=\"" + reporterIon.getName()
                                    + "\" shortname=\"" + reporterIon.getName()
                                    + "\" composition=\"" + getComposition(reporterIon.getAtomicComposition()) + "\" />");
                            bw.newLine();
                        }
                    }
                    bw.write("         </diagnostic_collection>");
                    bw.newLine();
                } else {
                    bw.write("         <neutralloss_collection />");
                    bw.newLine();
                }
                bw.write("      </modification_site>");
                bw.newLine();
            }
        } else {
            bw.write("      <modification_site site=\"-\">");
            bw.newLine();
            bw.write("         <neutralloss_collection />");
            bw.newLine();
            bw.write("         <diagnostic_collection />");
            bw.newLine();
            bw.write("      </modification_site>");
            bw.newLine();
        }
        bw.write("      <type>Standard</type>"); //@TODO: classify PTMs
        bw.newLine();
        bw.write("      <terminus_type>none</terminus_type>"); //@TODO: check with Jürgen what should be here
        bw.newLine();
        bw.write("   </modification>");
        bw.newLine();
    }

    /**
     * Returns the atomic composition of an AtomChain in the Andromeda format.
     *
     * @param atomChain the atom chain of interest
     *
     * @return the atomic composition in the Andromeda format
     */
    private static String getComposition(AtomChain atomChain) {

        HashMap<String, Integer> monoisotopic = new HashMap<>();
        HashMap<String, Integer> isotopic = new HashMap<>();
        ArrayList<String> atoms = new ArrayList<>();

        for (AtomImpl atomImpl : atomChain.getAtomChain()) {
            String atom = atomImpl.getAtomSymbol();
            if (!atoms.contains(atom)) {
                atoms.add(atom);
            }
            if (atomImpl.getIsotope() == 0) {
                Integer occurrence = monoisotopic.get(atom);
                if (occurrence == null) {
                    occurrence = 0;
                }
                monoisotopic.put(atom, occurrence + 1);
            } else {
                Integer occurrence = isotopic.get(atom);
                if (occurrence == null) {
                    occurrence = 0;
                }
                isotopic.put(atom, occurrence + 1);
            }
        }

        Collections.sort(atoms);
        StringBuilder result = new StringBuilder();

        for (String atom : atoms) {
            Integer occurrence = monoisotopic.get(atom);
            if (occurrence != null && occurrence != 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(atom);
                if (occurrence > 1 || occurrence < -1) {
                    result.append("(").append(occurrence).append(")");
                }
            }
            occurrence = isotopic.get(atom);
            if (occurrence != null && occurrence != 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(atom).append("x");
                if (occurrence > 1 || occurrence < -1) {
                    result.append("(").append(occurrence).append(")");
                }
            }
        }

        return result.toString();
    }

    /**
     * Returns the atomic composition of a PTM in the Andromeda format.
     *
     * @param modification the PTM of interest
     *
     * @return the atomic composition in the Andromeda format
     */
    private static String getComposition(Modification modification) {
        AtomChain atomChainAdded = modification.getAtomChainAdded(),
                atomChainRemoved = modification.getAtomChainRemoved();
        HashMap<String, Integer> monoisotopic = new HashMap<>();
        HashMap<String, Integer> isotopic = new HashMap<>();
        ArrayList<String> atoms = new ArrayList<>();

        if (atomChainAdded != null) {
            for (AtomImpl atomImpl : atomChainAdded.getAtomChain()) {
                String atom = atomImpl.getAtomSymbol();
                if (!atoms.contains(atom)) {
                    atoms.add(atom);
                }
                if (atomImpl.getIsotope() == 0) {
                    Integer occurrence = monoisotopic.get(atom);
                    if (occurrence == null) {
                        occurrence = 0;
                    }
                    monoisotopic.put(atom, occurrence + 1);
                } else {
                    Integer occurrence = isotopic.get(atom);
                    if (occurrence == null) {
                        occurrence = 0;
                    }
                    isotopic.put(atom, occurrence + 1);
                }
            }
        }

        if (atomChainRemoved != null) {
            for (AtomImpl atomImpl : atomChainRemoved.getAtomChain()) {
                String atom = atomImpl.getAtomSymbol();
                if (!atoms.contains(atom)) {
                    atoms.add(atom);
                }
                if (atomImpl.getIsotope() == 0) {
                    Integer occurrence = monoisotopic.get(atom);
                    if (occurrence == null) {
                        occurrence = 0;
                    }
                    monoisotopic.put(atom, occurrence - 1);
                } else {
                    Integer occurrence = isotopic.get(atom);
                    if (occurrence == null) {
                        occurrence = 0;
                    }
                    isotopic.put(atom, occurrence - 1);
                }
            }
        }

        Collections.sort(atoms);
        StringBuilder result = new StringBuilder();

        for (String atom : atoms) {
            Integer occurrence = monoisotopic.get(atom);
            if (occurrence != null && occurrence != 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(atom);
                if (occurrence > 1 || occurrence < -1) {
                    result.append("(").append(occurrence).append(")");
                }
            }
            occurrence = isotopic.get(atom);
            if (occurrence != null && occurrence != 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(atom).append("x");
                if (occurrence > 1 || occurrence < -1) {
                    result.append("(").append(occurrence).append(")");
                }
            }
        }

        return result.toString();
    }

    /**
     * Create the parameters file.
     *
     * @param searchParametersFile the file where to save the search parameters
     *
     * @return the parameters file
     *
     * @throws IOException exception thrown whenever an error occurred while
     * writing the configuration file
     */
    private File createParametersFile(File searchParametersFile) throws IOException {

        File andromedaTempFolder = new File(andromedaTempFolderPath);

        String fileName;
        try {
            fileName = Util.removeExtension(searchParametersFile.getName()) + ".apar";
        } catch (Exception e) {
            fileName = "SearchGUI.apar";
        }

        File parameterFile = new File(andromedaTempFolder, fileName);
        BufferedWriter bw = new BufferedWriter(new FileWriter(parameterFile));

        try {
            boolean semiSpecific = false;
            DigestionParameters digestionParameters = searchParameters.getDigestionParameters();

            if (digestionParameters.getCleavagePreference() == DigestionParameters.CleavagePreference.enzyme) {
                Enzyme enzyme = digestionParameters.getEnzymes().get(0);
                String enzymeName = enzyme.getName();
                bw.write("enzymes=" + enzymeName); //@TODO: support multiple enzymes?
                bw.newLine();
                if (digestionParameters.getSpecificity(enzyme.getName()) == DigestionParameters.Specificity.semiSpecific
                        || digestionParameters.getSpecificity(enzyme.getName()) == DigestionParameters.Specificity.specificCTermOnly
                        || digestionParameters.getSpecificity(enzyme.getName()) == DigestionParameters.Specificity.specificNTermOnly) {
                    semiSpecific = true;
                }
            } else if (digestionParameters.getCleavagePreference() == DigestionParameters.CleavagePreference.unSpecific) {
                bw.write("enzyme mode=unspecific");
                bw.newLine();
            } else {
                // whole enzyme
                // @TODO: what to put here..?
            }

            if (semiSpecific) {
                bw.write("enzyme mode=semispecific"); //@TODO: support: Semispecific Free N-terminus and Semispecific Free C-terminus
            } else {
                bw.write("enzyme mode=specific");
            }
            bw.newLine();
            ModificationParameters modificationProfile = searchParameters.getModificationParameters();
            StringBuilder list = new StringBuilder();
            for (String ptmName : modificationProfile.getVariableModifications()) {
                if (list.length() > 0) {
                    list.append(",");
                }
                list.append(ptmName);
            }
            bw.write("variable modifications=" + list);
            bw.newLine();
            list = new StringBuilder();
            for (String ptmName : modificationProfile.getFixedModifications()) {
                if (list.length() > 0) {
                    list.append(",");
                }
                list.append(ptmName);
            }
            bw.write("fixed modifications=" + list);
            bw.newLine();
            bw.write("label modifications="); //@TODO: support labels
            bw.newLine();
            if (!modificationProfile.getRefinementVariableModifications().isEmpty()) {
                bw.write("has additional variable modifications=True");
                bw.newLine();
                list = new StringBuilder();
                for (String ptmName : modificationProfile.getRefinementVariableModifications()) {
                    if (list.length() > 0) {
                        list.append(",");
                    }
                    list.append(ptmName);
                }
                bw.write("additional variable modifications=" + list);
                bw.newLine();
                bw.write("additional variable modification proteins=");
                bw.newLine();
            } else {
                bw.write("has additional variable modifications=False");
                bw.newLine();
                bw.write("additional variable modifications=");
                bw.newLine();
                bw.write("additional variable modification proteins=");
                bw.newLine();
            }
            bw.write("peptide mass tolerance=" + searchParameters.getPrecursorAccuracy());
            bw.newLine();
            bw.write("max peptide mass=" + andromedaParameters.getMaxPeptideMass());
            bw.newLine();
            bw.write("max combinations=" + andromedaParameters.getMaxCombinations());
            bw.newLine();
            if (searchParameters.isPrecursorAccuracyTypePpm()) {
                bw.write("peptide mass tolerance Unit=ppm");
            } else {
                bw.write("peptide mass tolerance Unit=Da");
            }
            bw.newLine();
            bw.write("fragment mass tolerance=" + searchParameters.getFragmentIonAccuracy());
            bw.newLine();
            if (searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.PPM) {
                bw.write("fragment mass tolerance Unit=ppm");
            } else {
                bw.write("fragment mass tolerance Unit=Da");
            }
            bw.newLine();
            bw.write("top peaks=" + andromedaParameters.getTopPeaks());
            bw.newLine();
            bw.write("top peaks window=" + andromedaParameters.getTopPeaksWindow());
            bw.newLine();

            if (digestionParameters.getCleavagePreference() == DigestionParameters.CleavagePreference.enzyme) {
                Integer missedCleavages = null;
                for (Enzyme enzyme : digestionParameters.getEnzymes()) {
                    int enzymeMissedCleavages = digestionParameters.getnMissedCleavages(enzyme.getName());
                    if (missedCleavages == null || enzymeMissedCleavages > missedCleavages) {
                        missedCleavages = enzymeMissedCleavages;
                    }
                }
                bw.write("max missed cleavages=" + missedCleavages);
                bw.newLine();
            }

            bw.write("fasta file=\"" + getGenericFastaFile(andromedaFolder, searchParameters.getFastaFile().getName()).getAbsolutePath() + "\"");
            bw.newLine();
            bw.write("decoy mode=" + andromedaParameters.getDecoyMode());
            bw.newLine();
            bw.write("include contaminants=False");
            bw.newLine();
            if (andromedaParameters.isIncludeWater()) {
                bw.write("include water=True");
            } else {
                bw.write("include water=False");
            }
            bw.newLine();
            if (andromedaParameters.isIncludeAmmonia()) {
                bw.write("include ammonia=True");
            } else {
                bw.write("include ammonia=False");
            }
            bw.newLine();
            if (andromedaParameters.isDependentLosses()) {
                bw.write("dependent losses=True");
            } else {
                bw.write("dependent losses=False");
            }
            bw.newLine();
            bw.write("special aas=");
            bw.newLine();
            if (andromedaParameters.isFragmentAll()) {
                bw.write("fragment all=True");
            } else {
                bw.write("fragment all=False");
            }
            bw.newLine();
            if (andromedaParameters.isEmpiricalCorrection()) {
                bw.write("empirical correction=True");
            } else {
                bw.write("empirical correction=False");
            }
            bw.newLine();
            if (andromedaParameters.isHigherCharge()) {
                bw.write("higher charges=True");
            } else {
                bw.write("higher charges=False");
            }
            bw.newLine();
            bw.write("fragmentation type=" + andromedaParameters.getFragmentationMethod().name);
            bw.newLine();
            bw.write("max number of modifications=" + andromedaParameters.getMaxNumberOfModifications());
            bw.newLine();
            bw.write("min peptide length no enzyme=" + andromedaParameters.getMinPeptideLengthNoEnzyme());
            bw.newLine();
            bw.write("max peptide length no enzyme=" + andromedaParameters.getMaxPeptideLengthNoEnzyme());
            bw.newLine();
            if (andromedaParameters.isEqualIL()) {
                bw.write("equal il=True");
            } else {
                bw.write("equal il=False");
            }
            bw.newLine();
            bw.write("number of candidates=" + andromedaParameters.getNumberOfCandidates());
            bw.newLine();

        } finally {
            bw.close();
        }
        return parameterFile;
    }

    @Override
    public String getType() {
        return "Andromeda";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return spectrumFile.getName();
    }

    /**
     * Returns the temp folder to use for Andromeda files. Null if not set.
     *
     * @return the temp folder to use for Andromeda files
     */
    public static String getTempFolderPath() {
        return andromedaTempFolderPath;
    }

    /**
     * Sets the temp folder to use for Andromeda files. If null the Andromeda
     * folder will be used.
     *
     * @param andromedaTempFolderPath the temp folder to use for Andromeda
     * files.
     */
    public static void setTempFolderPath(String andromedaTempFolderPath) {
        AndromedaProcessBuilder.andromedaTempFolderPath = andromedaTempFolderPath;
    }
}
