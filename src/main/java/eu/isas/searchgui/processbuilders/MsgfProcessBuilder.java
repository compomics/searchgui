package eu.isas.searchgui.processbuilders;

import com.compomics.software.CommandLineUtils;
import com.compomics.software.CompomicsWrapper;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsgfParameters;
import com.compomics.util.preferences.UtilitiesUserPreferences;
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
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The modification file for MS-GF+.
     */
    private final String MOD_FILE = "Mods.txt";
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
     * while getting the java home
     * @throws java.io.FileNotFoundException exception thrown whenever an error
     * occurred while getting the java home
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while getting the SearchGUI path
     */
    public MsgfProcessBuilder(File msgfDirectory, String mgfFile, File outputFile,
            SearchParameters searchParameters, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, int nThreads, boolean isCommandLine) throws IOException, FileNotFoundException, ClassNotFoundException {

        this.searchParameters = searchParameters;
        msgfParameters = (MsgfParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.msgf.getIndex());

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        this.spectrumFile = mgfFile;

        // make sure that the msgf+ jar file is executable
        File msgfExecutable = new File(msgfDirectory.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        msgfExecutable.setExecutable(true);

        // create the ms-gf+ modification file
        msgfModFile = new File(msgfDirectory, MOD_FILE);
        createModificationsFile();

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
        String msgfEnzyme = MsgfParameters.enzymeMapping(searchParameters.getEnzyme());
        if (msgfEnzyme != null) {
            process_name_array.add("-e");
            process_name_array.add(msgfEnzyme);
        }

        // set the protocol
        process_name_array.add("-protocol");
        process_name_array.add("" + msgfParameters.getProtocol());

        // set the number of tolerable termini
        process_name_array.add("-ntt");
        process_name_array.add("" + msgfParameters.getNumberTolerableTermini());

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
                + msgfParameters.getLowerIsotopeErrorRange()
                + "," + msgfParameters.getUpperIsotopeErrorRange()
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
     * @throws IllegalArgumentException thrown if more than one fixed PTM has
     * the same target
     */
    private void createModificationsFile() throws IllegalArgumentException {

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(msgfModFile));

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
            bw.close();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Could not create MS-GF+ modifications file. Unable to write file: '" + ioe.getMessage() + "'!");
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
}
