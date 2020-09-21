package eu.isas.searchgui.cmd;

import com.compomics.cli.fasta.FastaParametersCLIParams;
import com.compomics.cli.fasta.FastaParametersInputBean;
import com.compomics.software.CompomicsWrapper;
import com.compomics.util.experiment.io.biology.protein.FastaParameters;
import com.compomics.util.experiment.io.biology.protein.FastaSummary;
import com.compomics.util.experiment.io.biology.protein.converters.DecoyConverter;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.io.IoUtil;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
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
     * The FASTA parameters input bean.
     */
    private FastaParametersInputBean fastaParametersInputBean;
    /**
     * The waiting handler.
     */
    private WaitingHandler waitingHandler;

    /**
     * Construct a new FastaCLI runnable from a list of arguments.
     *
     * @param args the command line arguments
     */
    public FastaCLI(String[] args) {

        try {
            waitingHandler = new WaitingHandlerCLIImpl();

             // check if there are updates to the paths
            String[] nonPathSettingArgsAsList = PathSettingsCLI.extractAndUpdatePathOptions(args);

            // parse the rest of the cptions   
            Options nonPathOptions = new Options();
            FastaCLIParams.createOptionsCLI(nonPathOptions);
            FastaParametersCLIParams.createOptionsCLI(nonPathOptions);
            DefaultParser parser = new DefaultParser();
            CommandLine line = parser.parse(nonPathOptions, nonPathSettingArgsAsList);

            if (!FastaCLIInputBean.isValidStartup(line)) {

                PrintWriter lPrintWriter = new PrintWriter(System.out);
                lPrintWriter.print(System.getProperty("line.separator") + "======================" + System.getProperty("line.separator"));
                lPrintWriter.print("FastaCLI" + System.getProperty("line.separator"));
                lPrintWriter.print("======================" + System.getProperty("line.separator"));
                lPrintWriter.print(getHeader());
                lPrintWriter.print(FastaCLIParams.getOptionsAsString());
                lPrintWriter.print(FastaParametersCLIParams.getOptionsAsString());
                lPrintWriter.flush();
                lPrintWriter.close();

                System.exit(0);

            } else if (!FastaParametersInputBean.isValidStartup(line)) {

                PrintWriter lPrintWriter = new PrintWriter(System.out);
                lPrintWriter.print(System.getProperty("line.separator") + "======================" + System.getProperty("line.separator"));
                lPrintWriter.print("FastaCLI" + System.getProperty("line.separator"));
                lPrintWriter.print("======================" + System.getProperty("line.separator"));
                lPrintWriter.print(getHeader());
                lPrintWriter.print(FastaCLIParams.getOptionsAsString());
                lPrintWriter.print(FastaParametersCLIParams.getOptionsAsString());
                lPrintWriter.flush();
                lPrintWriter.close();

                System.exit(0);

            } else {

                fastaCLIInputBean = new FastaCLIInputBean(line);

                fastaParametersInputBean = new FastaParametersInputBean(line, fastaCLIInputBean.getInputFile(), waitingHandler);

                call();

            }
        } catch (Exception e) {
            waitingHandler.appendReport("An error occurred while running the command line. " + getLogFileMessage(), true, true);
            e.printStackTrace();
        }
    }

    /**
     * Calling this method will run the configured FastaCLI process.
     */
    public void call() {

        try {

            FastaParameters fastaParameters = fastaParametersInputBean.getFastaParameters();
            String fastaFilePath = fastaCLIInputBean.getInputFile().getAbsolutePath();
            System.out.println("Input: " + fastaFilePath + System.getProperty("line.separator"));

            FastaSummary fastaSummary = FastaSummary.getSummary(fastaFilePath, fastaParameters, true, waitingHandler);
            writeDbProperties(fastaSummary, fastaParameters);

            if (fastaCLIInputBean.isDecoy()) {

                String decoySuffix = fastaCLIInputBean.getDecoySuffix();

                if (decoySuffix != null) {
                    fastaParameters.setTargetDecoyFileNameSuffix(decoySuffix);
                }

                File newFile = generateTargetDecoyDatabase(fastaParameters, waitingHandler);

                System.out.println("Decoy file successfully created: " + System.getProperty("line.separator"));
                System.out.println("Output: " + newFile.getAbsolutePath() + System.getProperty("line.separator"));

                FastaParameters decoyParameters = DecoyConverter.getDecoyParameters(fastaParameters);
                FastaSummary decoySummary = DecoyConverter.getDecoySummary(newFile, fastaSummary);
                writeDbProperties(decoySummary, decoyParameters);

            }
        } catch (Exception e) {
            waitingHandler.appendReport("An error occurred while running the command line. " + getLogFileMessage(), true, true);
            e.printStackTrace();
        }
    }

    /**
     * Writes the database properties to System.out.
     *
     * @param fastaSummary the summary information on the FASTA file
     * @param fastaParameters the FASTA parsing parameters
     */
    public void writeDbProperties(
            FastaSummary fastaSummary, 
            FastaParameters fastaParameters
    ) {

        System.out.println("Name: " + fastaSummary.getName());
        System.out.println("Description: " + fastaSummary.getDescription());
        System.out.println("Version: " + fastaSummary.getVersion());

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
     * @param fastaParameters the FASTA parsing parameters
     * @param waitingHandler the waiting handler
     * 
     * @return the file created
     * @throws IOException exception thrown whenever an error happened while 
     * reading or writing a FASTA file
     */
    public File generateTargetDecoyDatabase(
            FastaParameters fastaParameters, 
            WaitingHandler waitingHandler
    ) throws IOException {

        // Get file in
        File fileIn = fastaCLIInputBean.getInputFile();

        // Get file out
        File fileOut = new File(fileIn.getParent(), IoUtil.removeExtension(fileIn.getName()) + fastaParameters.getTargetDecoyFileNameSuffix() + ".fasta");

        // Write file
        waitingHandler.setWaitingText("Appending Decoy Sequences. Please Wait...");
        DecoyConverter.appendDecoySequences(fileIn, fileOut, fastaParameters, waitingHandler);

        return fileOut;
    }

    /**
     * FastaCLI header message when printing the usage.
     */
    private static String getHeader() {
        return System.getProperty("line.separator")
                + "FastaCLI takes a FASTA file as input, generates general information about the database and allows operations on the FASTA file." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "For further help see https://compomics.github.io/projects/searchgui.html and https://compomics.github.io/projects/searchgui/wiki/SearchCLI.html." + System.getProperty("line.separator")
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
     * Returns the "see the log file" message. With the path if available. 
     * 
     * @return the "see the log file" message
     */
    public static String getLogFileMessage() {
//        if (logFolder == null) {
            return "Please see the SearchGUI log file.";
//        } else {
//            return "Please see the SearchGUI log file: " + logFolder.getAbsolutePath() + File.separator + "SearchGUI.log";  // @TODO: figure out how to get the location of the log file
//        }
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
