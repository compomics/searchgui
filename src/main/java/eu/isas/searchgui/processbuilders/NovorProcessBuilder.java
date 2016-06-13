package eu.isas.searchgui.processbuilders;

import com.compomics.software.CommandLineUtils;
import com.compomics.software.CompomicsWrapper;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.NovorParameters;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.UtilitiesUserPreferences;
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
import javax.swing.JOptionPane;

/**
 * This class will set up and start a process to run Novor.
 *
 * @author Harald Barsnes
 */
public class NovorProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The name of the Novor executable.
     */
    public final static String EXECUTABLE_FILE_NAME = "novor.jar";
    /**
     * The spectrumFile file.
     */
    private File spectrumFile;
    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The path to the Novor executable.
     */
    private File novorFolder;
    /**
     * The name of the Novor parameters file.
     */
    private String parameterFileName = "novor_params.txt";
    /**
     * The name of the Novor custom modifications file.
     */
    private String modsFileName = "novor_mods.txt";
    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The Novor to utilities PTM map. Key: Novor PTM short name, element:
     * utilities PTM name.
     */
    private HashMap<String, String> novorPtmMap;

    /**
     * Constructor.
     *
     * @param novorFolder the path to the Novor executable
     * @param mgfFile the spectrum MGF file
     * @param outputFile the output file
     * @param searchParameters the search parameters
     * @param isCommandLine true if run from the command line, false if GUI
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the exception handler
     * 
     * @throws java.io.IOException exception thrown whenever an error occurred
     * while getting the Java home
     * @throws java.io.FileNotFoundException exception thrown whenever an error
     * occurred while getting the java home
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while getting the SearchGUI path
     */
    public NovorProcessBuilder(File novorFolder, File mgfFile, File outputFile, SearchParameters searchParameters, boolean isCommandLine, 
            WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) throws IOException, FileNotFoundException, ClassNotFoundException {

        this.novorFolder = novorFolder;
        this.spectrumFile = mgfFile;
        this.searchParameters = searchParameters;
        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;

        // make sure that the novor jar file is executable
        File novorExecutable = new File(novorFolder.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        novorExecutable.setExecutable(true);

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
            // add the jvm arguments for denovogui to novor
            RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
            List<String> aList = bean.getInputArguments();
            for (String element : aList) {
                process_name_array.add(element);
            }
        }

        // add novor.jar
        process_name_array.add("-jar");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(new File(novorFolder, EXECUTABLE_FILE_NAME)));

        // create the parameters file
        createParameterFile();

        // add the parameters
        process_name_array.add("-p");
        process_name_array.add(novorFolder.getAbsolutePath() + File.separator + parameterFileName);

        // add the custom modifications
        process_name_array.add("-m");
        process_name_array.add(novorFolder.getAbsolutePath() + File.separator + modsFileName);

        // add output folder
        process_name_array.add("-o");
        process_name_array.add(CommandLineUtils.getCommandLineArgument(outputFile));

        // force overwrite of output file
        process_name_array.add("-f");

        // add the spectrum file
        process_name_array.add(CommandLineUtils.getCommandLineArgument(spectrumFile));

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "novor command: ");
        for (Object element : process_name_array) {
            System.out.print(element + " ");
        }
        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);
        pb.directory(novorFolder);

        // set error out and std out to same stream
        pb.redirectErrorStream(true);
    }

    /**
     * Create the Novor parameters file.
     */
    private void createParameterFile() {

        // get the Novoe specific parameters
        NovorParameters novorParameters = (NovorParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.novor.getIndex());
        try {
            FileWriter parameterWriter = new FileWriter(novorFolder.getAbsolutePath() + File.separator + parameterFileName);
            BufferedWriter bufferedParameterWriter = new BufferedWriter(parameterWriter);

            bufferedParameterWriter.write("# Search parameters" + System.getProperty("line.separator"));

            // the enzyme
            bufferedParameterWriter.write("enzyme = Trypsin" + System.getProperty("line.separator"));

            // fragmentation method
            bufferedParameterWriter.write("fragmentation = " + novorParameters.getFragmentationMethod() + System.getProperty("line.separator"));

            // the instrument
            bufferedParameterWriter.write("massAnalyzer = " + novorParameters.getMassAnalyzer() + System.getProperty("line.separator"));

            // the fragment ion tolerance
            bufferedParameterWriter.write("fragmentIonErrorTol = ");
            if (searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.DA) {
                bufferedParameterWriter.write(searchParameters.getFragmentIonAccuracy() + "Da" + System.getProperty("line.separator"));
            } else {
                double convertedTolerance = IdentificationParameters.getDaTolerance(searchParameters.getFragmentIonAccuracy(), 1000);
                bufferedParameterWriter.write(convertedTolerance + "Da" + System.getProperty("line.separator")); // note: only dalton is currently supported
            }

            // the precursor ion tolerance
            bufferedParameterWriter.write("precursorErrorTol = " + searchParameters.getPrecursorAccuracy());
            if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.DA) {
                bufferedParameterWriter.write("Da" + System.getProperty("line.separator"));
            } else {
                bufferedParameterWriter.write("ppm" + System.getProperty("line.separator"));
            }

            // add empty line
            bufferedParameterWriter.write(System.getProperty("line.separator"));

            // modifications
            FileWriter modsWriter = new FileWriter(novorFolder.getAbsolutePath() + File.separator + modsFileName);
            BufferedWriter bufferedModsWriter = new BufferedWriter(modsWriter);

            // create map for mapping back to the utilities ptms used
            novorPtmMap = new HashMap<String, String>();

            // variable modifications
            if (!searchParameters.getPtmSettings().getVariableModifications().isEmpty()) {
                bufferedParameterWriter.write("# Variable modifications" + System.getProperty("line.separator"));
                String variableModsAsString = "";

                for (String variableModification : searchParameters.getPtmSettings().getVariableModifications()) {
                    PTM ptm = ptmFactory.getPTM(variableModification);
                    addModification(bufferedModsWriter, ptm);

                    // update the modifications string
                    if (!variableModsAsString.isEmpty()) {
                        variableModsAsString += ", ";
                    }
                    variableModsAsString += ptm.getName();
                }

                // add the modification to the parameter file
                variableModsAsString = "variableModifications = " + variableModsAsString;
                bufferedParameterWriter.write(variableModsAsString + System.getProperty("line.separator") + System.getProperty("line.separator"));
            }

            // fixed modifications
            if (!searchParameters.getPtmSettings().getFixedModifications().isEmpty()) {
                bufferedParameterWriter.write("# Fixed modifications" + System.getProperty("line.separator"));
                String fixedModsAsString = "";

                for (String fixedModification : searchParameters.getPtmSettings().getFixedModifications()) {
                    PTM ptm = ptmFactory.getPTM(fixedModification);
                    addModification(bufferedModsWriter, ptm);

                    // update the modifications string
                    if (!fixedModsAsString.isEmpty()) {
                        fixedModsAsString += ", ";
                    }
                    fixedModsAsString += ptm.getName();
                }

                // add the modification to the parameter file
                fixedModsAsString = "fixedModifications = " + fixedModsAsString;
                bufferedParameterWriter.write(fixedModsAsString + System.getProperty("line.separator") + System.getProperty("line.separator"));
            }

            novorParameters.setNovorPtmMap(novorPtmMap);

            // close the mods writer
            bufferedModsWriter.close();
            modsWriter.close();

            // forbidden residues
            bufferedParameterWriter.write("# The residue which will not be used in de novo algorithm." + System.getProperty("line.separator"));
            bufferedParameterWriter.write("# I is disabled as default because it is the same as L" + System.getProperty("line.separator"));
            bufferedParameterWriter.write("# U is disabled because it is very rare" + System.getProperty("line.separator"));
            bufferedParameterWriter.write("forbiddenResidues = I,U" + System.getProperty("line.separator")); // @TODO: make this a user parameter?

            // close the parameters writer
            bufferedParameterWriter.close();
            modsWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, new String[]{"Unable to write file: '" + e.getMessage() + "'!",
                "Could not save Novr+ parameter file."}, "Novor Parameter File Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Converts a modification to the Novor format.
     *
     * @param bufferedModsWriter the writer to add the modification to
     * @param ptm the current PTM
     * @param modsAsString the current modifications as a string
     * @throws IOException thrown if an IOException occurs
     */
    private void addModification(BufferedWriter bufferedModsWriter, PTM ptm) throws IOException {

        // modification id
        bufferedModsWriter.write(ptm.getName() + ", ");

        // short name
        bufferedModsWriter.write(novorPtmMap.keySet().size() + ", ");
        novorPtmMap.put("" + novorPtmMap.keySet().size(), ptm.getName());

        // long name
        bufferedModsWriter.write(ptm.getName() + ", ");

        // the groups involved in the modification
        if (ptm.isNTerm()) {
            if (ptm.getType() == PTM.MODNAA || ptm.getType() == PTM.MODNPAA) {
                bufferedModsWriter.write("nr-, ");
            } else {
                bufferedModsWriter.write("n--, ");
            }
        } else if (ptm.isCTerm()) {
            if (ptm.getType() == PTM.MODCAA || ptm.getType() == PTM.MODCPAA) {
                bufferedModsWriter.write("-rc, ");
            } else {
                bufferedModsWriter.write("--c, ");
            }
        } else {
            bufferedModsWriter.write("-r-, ");
        }

        // the affected residues
        if (ptm.getPattern() != null) {
            for (Character target : ptm.getPattern().getAminoAcidsAtTarget()) {
                bufferedModsWriter.write(target);
            }
            bufferedModsWriter.write(", ");
        } else {
            bufferedModsWriter.write("*, ");
        }

        // the change of atoms
        bufferedModsWriter.write(", "); // @TOOD: we use this one instead of the mass?

        // the mass change
        bufferedModsWriter.write("" + ptm.getRoundedMass());

        // add new line
        bufferedModsWriter.write(System.getProperty("line.separator"));
    }

    /**
     * Returns the file name of the currently processed file.
     *
     * @return the file name of the currently processed file
     */
    public String getCurrentlyProcessedFileName() {
        return spectrumFile.getName();
    }

    /**
     * Returns the type of the process.
     *
     * @return the type of the process
     */
    public String getType() {
        return "Novor";
    }
}
