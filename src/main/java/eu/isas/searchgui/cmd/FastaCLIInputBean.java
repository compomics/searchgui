package eu.isas.searchgui.cmd;

import com.compomics.util.preferences.UtilitiesUserPreferences;
import java.io.File;
import org.apache.commons.cli.CommandLine;

/**
 * This class contains the information necessary for running the FastaCLI.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class FastaCLIInputBean {

    /**
     * The input file.
     */
    private File inputFile = null;
    /**
     * Indicates whether a decoy database shall be created.
     */
    private boolean decoy = false;
    /**
     * The tag added after adding decoy sequences to a FASTA file.
     */
    private String decoySuffix;

    /**
     * Constructor.
     *
     * @param aLine The command line given to the tool
     */
    public FastaCLIInputBean(CommandLine aLine) {
        if (aLine.hasOption(FastaCLIParams.in.id)) {
            String arg = aLine.getOptionValue(FastaCLIParams.in.id);
            inputFile = new File(arg);
        }
        if (aLine.hasOption(FastaCLIParams.decoy.id)) {
            decoy = true;
        }
        if (aLine.hasOption(FastaCLIParams.decoy_suffix.id)) {
            decoySuffix = aLine.getOptionValue(FastaCLIParams.decoy_suffix.id);
        } else {
            UtilitiesUserPreferences userPreferences = UtilitiesUserPreferences.loadUserPreferences();
            decoySuffix = userPreferences.getTargetDecoyFileNameTag();
        }
    }

    /**
     * Returns the input file.
     *
     * @return the input file
     */
    public File getInputFile() {
        return inputFile;
    }

    /**
     * Indicates whether decoy sequences shall be added.
     *
     * @return a boolean indicating whether whether decoy sequences shall be
     * added
     */
    public boolean isDecoy() {
        return decoy;
    }

    /**
     * Returns the tag added after adding decoy sequences to a FASTA file.
     *
     * @return the decoy suffix tag
     */
    public String getDecoySuffix() {
        return decoySuffix;
    }

    /**
     * Verifies the command line start parameters.
     *
     * @param aLine the command line to validate
     * @return true if the startup was valid
     */
    public static boolean isValidStartup(CommandLine aLine) {

        if (aLine.hasOption(FastaCLIParams.in.id)) {
            String arg = aLine.getOptionValue(FastaCLIParams.in.id);
            File test = new File(arg);
            if (!test.exists()) {
                System.out.println(System.getProperty("line.separator") + "FASTA file " + arg + " not found." + System.getProperty("line.separator"));
                return false;
            }
        } else {
            System.out.println(System.getProperty("line.separator") + "FASTA file not specified." + System.getProperty("line.separator"));
            return false;
        }

        return true;
    }
}
