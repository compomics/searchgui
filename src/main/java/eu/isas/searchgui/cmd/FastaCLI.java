package eu.isas.searchgui.cmd;

import com.compomics.util.experiment.identification.protein_sequences.FastaIndex;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import java.io.File;
import java.io.PrintWriter;
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
            } else {
                fastaCLIInputBean = new FastaCLIInputBean(line);
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

        try {
            WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
            SequenceFactory sequenceFactory = SequenceFactory.getInstance(1000);
            File fastaFile = fastaCLIInputBean.getInputFile();
            sequenceFactory.loadFastaFile(fastaFile, waitingHandlerCLIImpl);
            System.out.println("Input: " + fastaFile.getAbsolutePath() + System.getProperty("line.separator"));
            writeDbProperties();

            if (fastaCLIInputBean.isDecoy()) {
                UtilitiesUserPreferences userPreferences = UtilitiesUserPreferences.loadUserPreferences();
                userPreferences.setTargetDecoyFileNameTag(fastaCLIInputBean.getDecoySuffix() + ".fasta");
                boolean success = generateTargetDecoyDatabase(waitingHandlerCLIImpl);
                if (success) {
                    System.out.println("Decoy file successfully created: " + System.getProperty("line.separator"));
                    System.out.println("Output: " + sequenceFactory.getCurrentFastaFile().getAbsolutePath() + System.getProperty("line.separator"));
                    writeDbProperties();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Outputs the properties of the file currently loaded in the sequence
     * factory.
     */
    public void writeDbProperties() {
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        FastaIndex fastaIndex = sequenceFactory.getCurrentFastaIndex();
        System.out.println("Name: " + fastaIndex.getName());
        System.out.println("Version: " + fastaIndex.getVersion());
        System.out.println("Decoy Tag: " + fastaIndex.getDecoyTag());
        System.out.println("Type: " + fastaIndex.getMainDatabaseType().toString());
        System.out.println("Last modified: " + new Date(fastaIndex.getLastModified()).toString());
        String nSequences = fastaIndex.getNSequences() + " sequences";
        if (fastaIndex.isConcatenatedTargetDecoy()) {
            nSequences += " (" + fastaIndex.getNTarget() + " target)";
        }
        System.out.println("Size: " + nSequences + System.getProperty("line.separator"));
    }

    /**
     * Appends decoy sequences to the given target database file.
     *
     * @param waitingHandlerCLIImpl the waiting handler
     * @return true if the process was successfully completed
     */
    public boolean generateTargetDecoyDatabase(WaitingHandlerCLIImpl waitingHandlerCLIImpl) {

        SequenceFactory sequenceFactory = SequenceFactory.getInstance();

        // set up the new fasta file name
        String newFasta = fastaCLIInputBean.getInputFile().getAbsolutePath();

        // remove the ending .fasta (if there)
        if (newFasta.lastIndexOf(".") != -1) {
            newFasta = newFasta.substring(0, newFasta.lastIndexOf("."));
        }

        // add the target decoy tag
        UtilitiesUserPreferences userPreferences = UtilitiesUserPreferences.loadUserPreferences();
        newFasta += userPreferences.getTargetDecoyFileNameTag() + ".fasta";
        File newFile = new File(newFasta);

        try {
            waitingHandlerCLIImpl.setWaitingText("Appending Decoy Sequences. Please Wait...");
            sequenceFactory.appendDecoySequences(newFile, waitingHandlerCLIImpl);
            sequenceFactory.clearFactory();
            sequenceFactory.loadFastaFile(newFile, waitingHandlerCLIImpl);
        } catch (OutOfMemoryError error) {
            System.out.println("Ran out of memory!");
            error.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("An error occurred while appending decoy sequences");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * FastaCLI header message when printing the usage.
     */
    private static String getHeader() {
        return System.getProperty("line.separator")
                + "FastaCLI takes a fasta file as input, generates general information about the database and allows operations on the fasta file." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "For further help see http://compomics.github.io/projects/searchgui.html and http://compomics.github.io/searchgui/wiki/searchcli.html." + System.getProperty("line.separator")
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
}
