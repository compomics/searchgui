package eu.isas.searchgui.processbuilders;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.software.CompomicsWrapper;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.pride.CvTerm;
import com.compomics.util.waiting.WaitingHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class will set up and start a process to perform an MS-GF+ search.
 *
 * @author Harald Barsnes
 */
public class MsgfProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The MS-GF+ modifications file.
     */
    private File msgfModFile;
    /**
     * The MS-GF+ enzymes file.
     */
    private File msgfEnzymesFile;
    /**
     * The MS-GF+ enzyme map. Key: utilities enzyme name, element: ms-gf+ index.
     */
    private HashMap<String, Integer> enzymeMap = new HashMap<String, Integer>();
    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The modification file for MS-GF+.
     */
    private final String MOD_FILE = "Mods.txt";
    /**
     * The enzymes file for MS-GF+.
     */
    private final String ENZYMES_FILE = "enzymes.txt";
    /**
     * The name of the folder where the parameters are located. Assumed to be in
     * the MS-GF+ installation folder.
     */
    private final String PARAMS_FOLDER_NAME = "params";
    /**
     * The name of the MS-GF+ executable.
     */
    public final static String EXECUTABLE_FILE_NAME = "MSGFPlus.jar";
    /**
     * The spectrum file to search.
     */
    private String spectrumFile;
    /**
     * The MS-GF+ parameters.
     */
    private MsgfParameters msgfParameters;
    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;

    /**
     * Constructor.
     *
     * @param msgfDirectory directory location of MSGFPlus.jar
     * @param mgfFile name of the file containing the spectra
     * @param outputFile the output file
     * @param searchParameters the search parameters
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     * @param nThreads the number of threads to use
     * @param isCommandLine true if run from the command line, false if GUI
     *
     * @throws java.io.IOException exception thrown whenever an error occurred
     * while getting the Java home
     * @throws java.io.FileNotFoundException exception thrown whenever an error
     * occurred while getting the java home
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while getting the SearchGUI path
     */
    public MsgfProcessBuilder(File msgfDirectory, String mgfFile, File outputFile, SearchParameters searchParameters,
            WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, int nThreads, boolean isCommandLine)
            throws IOException, FileNotFoundException, ClassNotFoundException {

        this.searchParameters = searchParameters;
        msgfParameters = (MsgfParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.msgf.getIndex());

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        this.spectrumFile = mgfFile;

        // make sure that the msgf+ jar file is executable
        File msgfExecutable = new File(msgfDirectory.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        msgfExecutable.setExecutable(true);

        // create the parameters folder if it does not exist
        File parametersFolder = new File(msgfDirectory, PARAMS_FOLDER_NAME);
        if (!parametersFolder.exists()) {
            parametersFolder.mkdir();
        }

        // create the ms-gf+ modification file
        msgfModFile = new File(parametersFolder, MOD_FILE);
        createModificationsFile();

        // create ms-gf+ enzyme file
        msgfEnzymesFile = new File(parametersFolder, ENZYMES_FILE);
        createEnzymesFile();

        // set java home
        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        CompomicsWrapper wrapper = new CompomicsWrapper();
        ArrayList<String> javaHomeAndOptions = wrapper.getJavaHomeAndOptions(utilitiesUserPreferences.getSearchGuiPath());
        process_name_array.add(javaHomeAndOptions.get(0)); // set java home

        // set java options
        if (!isCommandLine) {
            for (int i = 1; i < javaHomeAndOptions.size(); i++) {
                process_name_array.add(javaHomeAndOptions.get(i));
            }
        } else {
            // add the jvm arguments for searchgui to ms-gf+
            RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
            List<String> aList = bean.getInputArguments();
            for (String element : aList) {
                process_name_array.add(element);
            }
        }

        // add the MSGFPlus.jar
        process_name_array.add("-jar");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(new File(msgfDirectory, EXECUTABLE_FILE_NAME)));

        // add the spectrum file
        process_name_array.add("-s");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(new File(mgfFile)));

        // add the database
        process_name_array.add("-d");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(searchParameters.getFastaFile()));

        // set the output file
        process_name_array.add("-o");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(outputFile));

        // set the precursor mass tolerance
        Double precursorMassError = searchParameters.getPrecursorAccuracy();
        String precursorMassErrorUnit = "ppm";
        if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.DA) {
            precursorMassErrorUnit = "Da";
        }
        process_name_array.add("-t");
        process_name_array.add(precursorMassError + precursorMassErrorUnit);

        // enable/disable the msgf+ decoy search
        process_name_array.add("-tda");
        if (msgfParameters.searchDecoyDatabase()) {
            process_name_array.add("1");
        } else {
            process_name_array.add("0");
        }

        // link to the msgf+ modifications file
        process_name_array.add("-mod");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(msgfModFile));

        // add min/max precursor charge
        process_name_array.add("-minCharge");
        process_name_array.add("" + searchParameters.getMinChargeSearched().value);
        process_name_array.add("-maxCharge");
        process_name_array.add("" + searchParameters.getMaxChargeSearched().value);

        // set the instrument type
        process_name_array.add("-inst");
        process_name_array.add("" + msgfParameters.getInstrumentID());

        // set the number of threads to use
        process_name_array.add("-thread");
        process_name_array.add("" + nThreads);

        // set the fragmentation method
        process_name_array.add("-m");
        process_name_array.add("" + msgfParameters.getFragmentationType());

        // set the enzyme
        Integer msgfEnzyme = getEnzymeMapping(searchParameters.getDigestionPreferences());
        if (msgfEnzyme != null) {
            process_name_array.add("-e");
            process_name_array.add(msgfEnzyme.toString());
        }

        // set the number of tolerable termini
        process_name_array.add("-ntt");
        process_name_array.add("" + msgfParameters.getNumberTolerableTermini());

        // set the protocol
        process_name_array.add("-protocol");
        process_name_array.add("" + msgfParameters.getProtocol());

        // set the min/max peptide lengths
        process_name_array.add("-minLength");
        process_name_array.add("" + msgfParameters.getMinPeptideLength());
        process_name_array.add("-maxLength");
        process_name_array.add("" + msgfParameters.getMaxPeptideLength());

        // set the number of matches per spectrum
        process_name_array.add("-n");
        process_name_array.add("" + msgfParameters.getNumberOfSpectrumMatches());

        // provide additional output
        process_name_array.add("-addFeatures");
        if (msgfParameters.isAdditionalOutput()) {
            process_name_array.add("1");
        } else {
            process_name_array.add("0");
        }

        // set the range of allowed isotope peak errors
        process_name_array.add("-ti");
        process_name_array.add(CommandLineUtils.getQuoteType()
                + searchParameters.getMinIsotopicCorrection()
                + "," + searchParameters.getMaxIsotopicCorrection()
                + CommandLineUtils.getQuoteType());

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "ms-gf+ command: ");

        for (Object element : process_name_array) {
            System.out.print(element + " ");
        }

        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);

        pb.directory(msgfDirectory);
        // set error out and std out to same stream
        pb.redirectErrorStream(true);
    }

    /**
     * Creates the MS-GF+ modifications file.
     *
     * @throws IOException if the modification file could not be created
     */
    private void createModificationsFile() throws IOException {

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(msgfModFile));
            try {
                // add the number of modifications per peptide
                bw.write("#max number of modifications per peptide\n");
                bw.write("NumMods=" + msgfParameters.getNumberOfPtmsPerPeptide() + "\n\n");

                // add the fixed modifications
                bw.write("#fixed modifications\n");
                ArrayList<String> fixedPtms = searchParameters.getPtmSettings().getFixedModifications();
                for (String ptmName : fixedPtms) {
                    bw.write(getPtmFormattedForMsgf(ptmName, true) + "\n");
                }
                bw.write("\n");

                // add the variable modifications
                bw.write("#variable modifications\n");
                ArrayList<String> variablePtms = searchParameters.getPtmSettings().getVariableModifications();
                for (String ptmName : variablePtms) {
                    bw.write(getPtmFormattedForMsgf(ptmName, false) + "\n");
                }

                bw.flush();
            } finally {
                bw.close();
            }
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Could not create MS-GF+ modifications file. Unable to write file: '" + ioe.getMessage() + "'!");
        }
    }

    /**
     * Creates the MS-GF+ enzymes file.
     *
     * @throws IOException if the enzymes file could not be written
     */
    private void createEnzymesFile() throws IOException {

        // Format: ShortName,CleaveAt,Terminus (https://bix-lab.ucsd.edu/display/CCMStools/enzymes.txt)
        // - ShortName: an unique short name of the enzyme (e.g. Tryp). No space is allowed.
        // - CleaveAt: the residues cleaved by the enzyme (e.g. KR). Put "null" in case of no specificy.
        // - Terminus: Whether the enzyme cleaves C-term (C) or N-term (N)
        // - Description: description of the enzyme
        // Example: Tryp,KR,C,Trypsin
        enzymeMap = new HashMap<String, Integer>();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(msgfEnzymesFile));
            try {

                EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
                int enzymeCounter = 10; // as there are ten default ms-gf+ enzymes

                for (Enzyme enzyme : enzymeFactory.getEnzymes()) {

                    String enzymeName = enzyme.getName();

                    Integer enzymeIndex = getEnzymeMapping(enzyme);

                    if (enzymeIndex == null) {

                        String cleavageType;
                        String cleavageSite = "";

                        if (!enzyme.getAminoAcidBefore().isEmpty()) {
                            cleavageType = "C";
                            for (Character character : enzyme.getAminoAcidBefore()) {
                                cleavageSite += character;
                            }
                        } else {
                            cleavageType = "N";
                            for (Character character : enzyme.getAminoAcidAfter()) {
                                cleavageSite += character;
                            }
                        }

                        String nameWithoutComma = enzymeName;
                        nameWithoutComma = nameWithoutComma.replaceAll(",", "");
                        String nameWithoutCommaAndSpaces = nameWithoutComma.replaceAll(" ", "_");

                        bw.write(nameWithoutCommaAndSpaces + ",");
                        bw.write(cleavageSite + ",");
                        bw.write(cleavageType + ",");
                        bw.write(nameWithoutComma + System.getProperty("line.separator"));

                        enzymeMap.put(enzymeName, enzymeCounter++);
                    }
                }
            } finally {
                bw.close();
            }
        } catch (IOException ioe) {
            throw new IOException("Could not create MS-GF+ enzymes file. Unable to write file: '" + ioe.getMessage() + "'.");
        }
    }

    /**
     * Get the given modification as a string in the MS-GF+ format.
     *
     * @param ptmName the utilities name of the PTM
     * @param fixed if the PTM is fixed or not
     * @return the given modification as a string in the MS-GF+ format
     */
    private String getPtmFormattedForMsgf(String ptmName, boolean fixed) {

        PTM tempPtm = ptmFactory.getPTM(ptmName);

        // get the targeted amino acids
        String aminoAcidsAtTarget = "";
        if (tempPtm.getPattern() != null) {
            for (Character aa : tempPtm.getPattern().getAminoAcidsAtTarget()) {
                aminoAcidsAtTarget += aa;
            }
        }
        if (aminoAcidsAtTarget.length() == 0) {
            aminoAcidsAtTarget = "*";
        }

        // get the type of the modification
        String position = "";
        switch (tempPtm.getType()) {
            case PTM.MODAA:
                position = "any";
                break;
            case PTM.MODC:
            case PTM.MODCAA:
                position = "Prot-C-term";
                break;
            case PTM.MODCP:
            case PTM.MODCPAA:
                position = "C-term";
                break;
            case PTM.MODN:
            case PTM.MODNAA:
                position = "Prot-N-term";
                break;
            case PTM.MODNP:
            case PTM.MODNPAA:
                position = "N-term";
                break;
        }

        // use unimod name if possible
        String ptmCvTermName = ptmName;
        CvTerm cvTerm = tempPtm.getCvTerm();
        if (cvTerm != null) {
            ptmCvTermName = cvTerm.getName();
        }

        // set the ptm type as fixed or variable
        String ptmType = "fix";
        if (!fixed) {
            ptmType = "opt";
        }

        // return the ptm
        return tempPtm.getRoundedMass() + "," + aminoAcidsAtTarget + "," + ptmType + "," + position + "," + ptmCvTermName;
    }

    /**
     * Returns the type of the process.
     *
     * @return the type of the process
     */
    public String getType() {
        return "MS-GF+";
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
     * Tries to map the utilities digestion preferences to the default enzymes
     * supported by MS-GF+. Null if not found.
     *
     * @param digestionPreferences the utilities digestion preferences
     *
     * @return the index of the MS-GF+ enzyme
     */
    private Integer getEnzymeMapping(DigestionPreferences digestionPreferences) {

        if (digestionPreferences.getCleavagePreference() == DigestionPreferences.CleavagePreference.wholeProtein) {
            return 9;
        }
        if (digestionPreferences.getCleavagePreference() == DigestionPreferences.CleavagePreference.unSpecific) {
            return 0;
        }
        if (digestionPreferences.getEnzymes().size() > 1) {
            return 0;
        }

        Enzyme enzyme = digestionPreferences.getEnzymes().get(0);
        return getEnzymeMapping(enzyme);
    }

    /**
     * Tries to map the utilities enzyme to the default enzymes supported by
     * MS-GF+. Null if not found.
     *
     * @param enzyme the utilities enzyme
     *
     * @return the index of the MS-GF+ enzyme
     */
    private Integer getEnzymeMapping(Enzyme enzyme) {

        String enzymeName = enzyme.getName();
        if (enzymeName.equalsIgnoreCase("Trypsin")) {
            return 1;
        }
        if (enzymeName.equalsIgnoreCase("Chymotrypsin")) {
            return 2;
        }
        if (enzymeName.equalsIgnoreCase("Lys-C")) {
            return 3;
        }
        if (enzymeName.equalsIgnoreCase("Lys-N")) {
            return 4;
        }
        if (enzymeName.equalsIgnoreCase("Glu-C")) {
            return 5;
        }
        if (enzymeName.equalsIgnoreCase("Arg-C")) {
            return 6;
        }
        if (enzymeName.equalsIgnoreCase("Asp-N")) {
            return 7;
        } // else if (enzymeName.equalsIgnoreCase("alphaLP")) { // alphaLP: Alpha-lytic protease (aLP) is an alternative specificity protease for proteomics applications.
        //      msgfEnzymeIndex = 8;                        //          cleaves after T, A, S, and V residues. It generates peptides of similar average length as trypsin.
        // };

        return enzymeMap.get(enzyme.getName());
    }
}
