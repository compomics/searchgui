package eu.isas.searchgui.processbuilders;

import com.compomics.util.Util;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AtomChain;
import com.compomics.util.experiment.biology.AtomImpl;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.protein_sequences.FastaIndex;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.AndromedaParameters;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.protein.Header;
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
            andromedaTempFolderPath = andromedaFolder.getAbsolutePath() + File.separator + andromedaTempSubFolderName;
        }
        File andromedaTempFolder = new File(andromedaTempFolderPath);
        if (!andromedaTempFolder.exists()) {
            andromedaTempFolder.mkdirs();
        }

        // make sure that the andromeda file is executable
        File andromeda = new File(andromedaFolder.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        andromeda.setExecutable(true);

        // Create parameters file
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
        BufferedWriter bw = new BufferedWriter(new FileWriter(databaseFile));
        String dbName = searchParameters.getFastaFile().getName();
        FastaIndex fastaIndex = SequenceFactory.getFastaIndex(searchParameters.getFastaFile(), false, null);
        String parsingRule = getDatabaseTypeAndromedaAccessionParsingRule(fastaIndex.getMainDatabaseType());
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
                    + parsingRule + "\" mutation_parse_rule=\"\" "
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
                ArrayList<Character> aaBefore = enzyme.getAminoAcidBefore();
                if (aaBefore.isEmpty()) {
                    for (char aa : AminoAcid.getUniqueAminoAcids()) {
                        aaBefore.add(aa);
                    }
                }
                ArrayList<Character> aaAfter = enzyme.getAminoAcidAfter();
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
     * @param identificationParametersFile the file where to save the search parameters
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

        PTMFactory ptmFactory = PTMFactory.getInstance();
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        AndromedaParameters andromedaParameters = (AndromedaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex());

        try {
            bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            bw.newLine();
            bw.write("<modifications xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
            bw.newLine();
            for (String ptmName : ptmFactory.getDefaultModifications()) {
                PTM ptm = ptmFactory.getPTM(ptmName);
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
     * Writes the XML bloc corresponding to the PTM via the given writer.
     *
     * @param bw a buffered writer
     * @param date the date to use as creation
     * @param ptm the PTM to write
     * @param index the index of the PTM in the list
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing to the file.
     */
    private static void writePtm(BufferedWriter bw, String date, PTM ptm, int index) throws IOException {

        bw.write("   <modification index=\"" + index
                + "\" title=\"" + ptm.getName()
                + "\" description=\"" + ptm.getName()
                + "\" create_date=\"" + date
                + "\" last_modified_date=\"" + date
                + "\" user=\"SearchGUI\" "
                + "reporterCorrectionM2=\"0\" reporterCorrectionM1=\"0\" reporterCorrectionP1=\"0\" reporterCorrectionP2=\"0\" "
                + "composition=\"" + getComposition(ptm) + "\" multi_modification=\"false\">");
        bw.newLine();
        if (ptm.getType() == PTM.MODAA) {
            bw.write("      <position>anywhere</position>");
        } else if (ptm.getType() == PTM.MODN) {
            bw.write("      <position>proteinNterm</position>");
        } else if (ptm.getType() == PTM.MODNAA) {
            bw.write("      <position>proteinNterm</position>");
        } else if (ptm.getType() == PTM.MODNP) {
            bw.write("      <position>anyNterm</position>");
        } else if (ptm.getType() == PTM.MODNPAA) {
            bw.write("      <position>anyNterm</position>");
        } else if (ptm.getType() == PTM.MODC) {
            bw.write("      <position>proteinCterm</position>");
        } else if (ptm.getType() == PTM.MODCAA) {
            bw.write("      <position>proteinCterm</position>");
        } else if (ptm.getType() == PTM.MODCP) {
            bw.write("      <position>anyCterm</position>");
        } else if (ptm.getType() == PTM.MODCPAA) {
            bw.write("      <position>anyCterm</position>");
        } else {
            throw new IllegalArgumentException("Export not implemented for PTM of type " + ptm.getType() + ".");
        }
        bw.newLine();
        int siteIndex = 0;
        AminoAcidPattern aminoAcidPattern = ptm.getPattern();
        if (aminoAcidPattern != null && !aminoAcidPattern.getAminoAcidsAtTarget().isEmpty()) {
            for (Character aa : aminoAcidPattern.getAminoAcidsAtTarget()) {
                bw.write("      <modification_site index=\"" + siteIndex + "\" site=\"" + aa + "\">");
                bw.newLine();
                siteIndex++;
                ArrayList<NeutralLoss> neutralLosses = ptm.getNeutralLosses();
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
                ArrayList<ReporterIon> reporterIons = ptm.getReporterIons();
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

        HashMap<String, Integer> monoisotopic = new HashMap<String, Integer>();
        HashMap<String, Integer> isotopic = new HashMap<String, Integer>();
        ArrayList<String> atoms = new ArrayList<String>();

        for (AtomImpl atomImpl : atomChain.getAtomChain()) {
            String atom = atomImpl.getAtom().getLetter();
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
     * @param ptm the PTM of interest
     *
     * @return the atomic composition in the Andromeda format
     */
    private static String getComposition(PTM ptm) {
        AtomChain atomChainAdded = ptm.getAtomChainAdded(),
                atomChainRemoved = ptm.getAtomChainRemoved();
        HashMap<String, Integer> monoisotopic = new HashMap<String, Integer>();
        HashMap<String, Integer> isotopic = new HashMap<String, Integer>();
        ArrayList<String> atoms = new ArrayList<String>();

        if (atomChainAdded != null) {
            for (AtomImpl atomImpl : atomChainAdded.getAtomChain()) {
                String atom = atomImpl.getAtom().getLetter();
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
                String atom = atomImpl.getAtom().getLetter();
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
            Enzyme enzyme = searchParameters.getEnzyme();
            String enzymeName = enzyme.getName();
            bw.write("enzymes=" + enzymeName); //@TODO: support multiple enzymes?
            bw.newLine();
            if (enzyme.isUnspecific()) {
                bw.write("enzyme mode=unspecific");
            } else {
                if (enzyme.isSemiSpecific()) {
                    bw.write("enzyme mode=semispecific"); //@TODO: support: Semispecific Free N-terminus and Semispecific Free C-terminus
                } else {
                    bw.write("enzyme mode=specific");
                }
            }
            bw.newLine();
            PtmSettings modificationProfile = searchParameters.getPtmSettings();
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
            bw.write("max missed cleavages=" + searchParameters.getnMissedCleavages());
            bw.newLine();
            bw.write("fasta file=\"" + searchParameters.getFastaFile().getAbsolutePath() + "\"");
            bw.newLine();
            bw.write("decoy mode=reverse");
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

    /**
     * Returns the regular expression for the parsing of the accession in a
     * FASTA header.
     *
     * @param databaseType the database type
     *
     * @return the name
     */
    public static String getDatabaseTypeAndromedaAccessionParsingRule(Header.DatabaseType databaseType) {

        switch (databaseType) {
            case UniProt:
                return "&gt;.*\\|(.*)\\|";
            case NCBI:
                return "&gt;(gi\\|[0-9]*)";
            case IPI:
                return "&gt;IPI:([^\\| .]*)";
            case H_Invitation:
                return "&gt;([^\\|]*)";
            case Halobacterium:
                return "&gt;([^ ]*)";
            case H_Influenza:
                return "&gt;([^ ]*)";
            case C_Trachomatis:
                return "&gt;([^ ]*)";
            case M_Tuberculosis:
                return "&gt;([^\\|]*)";
            case Drosophile:
                return "&gt;(^ pep:*)"; //@TODO: not sure about this one
            case SGD:
                return "&gt;([^ ]*)";
            case Flybase:
                return "&gt;(.*)"; //@TODO: be more specific
            case GenomeTranslation:
                return "&gt;(.*)"; //@TODO: be more specific
            case Arabidopsis_thaliana_TAIR:
                return "&gt;([^\\|]*)";
            case PSB_Arabidopsis_thaliana:
                return "&gt;(.*)"; //@TODO: be more specific
            case Listeria:
                return "&gt;.*\\|(.*)\\|";
            case Generic_Header:
                return "&gt;([^ ]*)";
            case Generic_Split_Header:
                return "&gt;.*\\|(.*)\\|";
            case GAFFA:
                return "&gt;.*\\|(.*)\\|";
            case UPS:
                return "&gt;([^ ]*)";
            case NextProt:
                return "&gt;.*\\|(.*)\\|";
            case UniRef:
                return "&gt;([^ ]*)";
            default:
                throw new UnsupportedOperationException("Database type not implemented: " + databaseType + ".");
        }
    }
}
