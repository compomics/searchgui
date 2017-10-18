package eu.isas.searchgui.cmd;

import com.compomics.cli.fasta.FastaParametersCLIParams;
import com.compomics.cli.fasta.FastaParametersInputBean;
import com.compomics.software.CompomicsWrapper;
import com.compomics.software.settings.PathKey;
import com.compomics.software.settings.UtilitiesPathParameters;
import com.compomics.util.Util;
import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import com.compomics.util.experiment.io.biology.protein.FastaSummary;
import com.compomics.util.experiment.io.biology.protein.converters.DecoyConverter;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.parameters.tools.UtilitiesUserParameters;
import static eu.isas.searchgui.cmd.SearchCLI.redirectErrorStream;
import eu.isas.searchgui.parameters.SearchGUIPathParameters;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * This class allows the user to work on the database in command line.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class FastaCLI {

    /**
     * The input from the command line.
     */
    private FastaCLIInputBean fastaCLIInputBean;
    /**
     * The fasta parameters input bean.
     */
    private FastaParametersInputBean fastaParametersInputBean;

    /**
     * Construct a new FastaCLI runnable from a list of arguments.
     *
     * @param args the command line arguments
     */
    public FastaCLI(String[] args) {

        try {

            Options lOptions = new Options();
            FastaCLIParams.createOptionsCLI(lOptions);
            BasicParser parser = new BasicParser();
            CommandLine line = parser.parse(lOptions, args);

            if (!FastaCLIInputBean.isValidStartup(line)) {

                PrintWriter lPrintWriter = new PrintWriter(System.out);
                lPrintWriter.print(System.getProperty("line.separator") + "======================" + System.getProperty("line.separator"));
                lPrintWriter.print("FastaCLI" + System.getProperty("line.separator"));
                lPrintWriter.print("======================" + System.getProperty("line.separator"));
                lPrintWriter.print(getHeader());
                lPrintWriter.print(FastaCLIParams.getOptionsAsString());
                lPrintWriter.flush();
                lPrintWriter.close();

                System.exit(0);

            } else if (!FastaParametersInputBean.isValidStartup(line)) {

                PrintWriter lPrintWriter = new PrintWriter(System.out);
                lPrintWriter.print(System.getProperty("line.separator") + "======================" + System.getProperty("line.separator"));
                lPrintWriter.print("FastaCLI" + System.getProperty("line.separator"));
                lPrintWriter.print("======================" + System.getProperty("line.separator"));
                lPrintWriter.print(getHeader());
                lPrintWriter.print(FastaParametersCLIParams.getOptionsAsString());
                lPrintWriter.flush();
                lPrintWriter.close();

                System.exit(0);

            } else {

                fastaCLIInputBean = new FastaCLIInputBean(line);

                fastaParametersInputBean = new FastaParametersInputBean(line, fastaCLIInputBean.getInputFile());

                call();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calling this method will run the configured FastaCLI process.
     */
    public void call() {

        PathSettingsCLIInputBean pathSettingsCLIInputBean = fastaCLIInputBean.getPathSettingsCLIInputBean();
        FastaParameters fastaParameters = fastaParametersInputBean.getFastaParameters();

        if (pathSettingsCLIInputBean.getLogFolder() != null) {

            redirectErrorStream(pathSettingsCLIInputBean.getLogFolder());

        }

        if (pathSettingsCLIInputBean.hasInput()) {

            PathSettingsCLI pathSettingsCLI = new PathSettingsCLI(pathSettingsCLIInputBean);
            pathSettingsCLI.setPathSettings();

        } else {

            try {

                File pathConfigurationFile = new File(getJarFilePath(), UtilitiesPathParameters.configurationFileName);

                if (pathConfigurationFile.exists()) {

                    SearchGUIPathParameters.loadPathParametersFromFile(pathConfigurationFile);

                }

            } catch (Exception e) {

                System.out.println("An error occurred when setting path configuration. Default paths will be used.");
                e.printStackTrace();

            }

            try {

                ArrayList<PathKey> errorKeys = SearchGUIPathParameters.getErrorKeys(getJarFilePath());

                if (!errorKeys.isEmpty()) {

                    System.out.println("Unable to write in the following configuration folders. Please use a temporary folder, "
                            + "the path configuration command line, or edit the configuration paths from the graphical interface.");

                    for (PathKey pathKey : errorKeys) {

                        System.out.println(pathKey.getId() + ": " + pathKey.getDescription());

                    }
                }

            } catch (Exception e) {

                System.out.println("Unable to load the path configurations. Default pathswill be used.");

            }
        }

        try {

            WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();

            File fastaFile = fastaCLIInputBean.getInputFile();
            System.out.println("Input: " + fastaFile.getAbsolutePath() + System.getProperty("line.separator"));

            FastaSummary fastaSummary = FastaSummary.getSummary(fastaFile, fastaParameters, waitingHandlerCLIImpl);
            writeDbProperties(fastaSummary, fastaParameters);

            if (fastaCLIInputBean.isDecoy()) {

                String decoySuffix = fastaCLIInputBean.getDecoySuffix();

                if (decoySuffix != null) {

                    UtilitiesUserParameters userPreferences = UtilitiesUserParameters.loadUserParameters();
                    userPreferences.setTargetDecoyFileNameSuffix(decoySuffix + ".fasta");

                }

                File newFile = generateTargetDecoyDatabase(waitingHandlerCLIImpl);

                System.out.println("Decoy file successfully created: " + System.getProperty("line.separator"));
                System.out.println("Output: " + newFile.getAbsolutePath() + System.getProperty("line.separator"));

                FastaSummary decoySummary = DecoyConverter.getDecoySummary(newFile, fastaSummary);
                writeDbProperties(decoySummary, fastaParameters);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the database properties to System.out.
     *
     * @param fastaSummary the summary information on the fasta file
     * @param fastaParameters the fasta parsing parameters
     */
    public void writeDbProperties(FastaSummary fastaSummary, FastaParameters fastaParameters) {

        System.out.println("Name: " + fastaParameters.getName());
        System.out.println("Description: " + fastaParameters.getDescription());
        System.out.println("Version: " + fastaParameters.getVersion());

        if (fastaParameters.isTargetDecoy()) {

            if (fastaParameters.isDecoySuffix()) {

                System.out.println("Decoy Flag: " + fastaParameters.getDecoyFlag() + " (Suffix)");

            } else {

                System.out.println("Decoy Flag: " + fastaParameters.getDecoyFlag() + " (Prefix)");

            }

        } else {

            System.out.println("No decoy");

        }

        System.out.println("Type: " + fastaSummary.getTypeAsString());
        System.out.println("Last modified: " + new Date(fastaSummary.lastModified).toString());

        String nSequences = fastaSummary.nSequences + " sequences";

        if (fastaParameters.isTargetDecoy()) {

            nSequences += " (" + fastaSummary.nTarget + " target)";

        }

        System.out.println("Size: " + nSequences + System.getProperty("line.separator"));

    }

    /**
     * Appends decoy sequences to the given target database file.
     *
     * @param waitingHandlerCLIImpl the waiting handler
     *
     * @return the file created
     */
    public File generateTargetDecoyDatabase(WaitingHandlerCLIImpl waitingHandlerCLIImpl) throws IOException {

        // Get file in
        File fileIn = fastaCLIInputBean.getInputFile();

        // Get file out
        UtilitiesUserParameters userParameters = UtilitiesUserParameters.loadUserParameters();
        File fileOut = new File(fileIn.getParent(), Util.removeExtension(fileIn.getName()) + userParameters.getTargetDecoyFileNameSuffix() + ".fasta");

        // Write file
        waitingHandlerCLIImpl.setWaitingText("Appending Decoy Sequences. Please Wait...");
        DecoyConverter.appendDecoySequences(fileIn, fileOut, waitingHandlerCLIImpl);

        return fileOut;
    }

    /**
     * FastaCLI header message when printing the usage.
     */
    private static String getHeader() {
        return System.getProperty("line.separator")
                + "FastaCLI takes a fasta file as input, generates general information about the database and allows operations on the fasta file." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "For further help see http://compomics.github.io/projects/searchgui.html and http://compomics.github.io/projects/searchgui/wiki/searchcli.html." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Or contact the developers at https://groups.google.com/group/peptide-shaker." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "----------------------"
                + System.getProperty("line.separator")
                + "OPTIONS"
                + System.getProperty("line.separator")
                + "----------------------" + System.getProperty("line.separator")
                + "\n";
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new FastaCLI(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        return CompomicsWrapper.getJarFilePath(this.getClass().getResource("FastaCLI.class").getPath(), "SearchGUI");
    }
}
