package eu.isas.searchgui.cmd;

import org.apache.commons.cli.Options;

/**
 * This class contains the parameters which can be used in the Fasta command
 * line.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum FastaCLIParams {

    // When adding an option don't forget to update the getOptions and getOptionsAsString methods
    in("in", "FASTA file", true, true),
    decoy("decoy", "Create a concatenated target/decoy database.", false, false),
    decoy_suffix("decoy_suffix", "Target decoy suffix, defaults to _concatenated_target_decoy.fasta.", false, true);
    /**
     * Short Id for the CLI parameter.
     */
    public String id;
    /**
     * Explanation for the CLI parameter.
     */
    public String description;
    /**
     * Boolean indicating whether the parameter is mandatory.
     */
    public boolean mandatory;
    /**
     * Boolean indicating whether the command option has argument
     */
    public boolean hasArgument;

    /**
     * Private constructor managing the various variables for the enum
     * instances.
     *
     * @param id the id
     * @param description the description
     * @param mandatory is the parameter mandatory
     * @param hasArgument do we expect an argument for this option
     */
    private FastaCLIParams(String id, String description, boolean mandatory, boolean hasArgument) {
        this.id = id;
        this.description = description;
        this.mandatory = mandatory;
        this.hasArgument = hasArgument;
    }

    /**
     * Returns the options as a string.
     *
     * @return the options as a string
     */
    public static String getOptionsAsString() {

        String output = "";
        String formatter = "%-25s";

        output += "Mandatory parameters:\n\n";
        output += "-" + String.format(formatter, in.id) + in.description + "\n";

        output += "\n\nOptional parameters:\n\n";
        output += "-" + String.format(formatter, decoy.id) + decoy.description + "\n";
        output += "-" + String.format(formatter, decoy_suffix.id) + decoy_suffix.description + "\n";

        return output;
    }

    /**
     * Creates the options for the command line interface based on the possible
     * values.
     *
     * @param aOptions the options object where the options will be added
     */
    public static void createOptionsCLI(Options aOptions) {
        for (FastaCLIParams param : values()) {
            aOptions.addOption(param.id, param.hasArgument, param.description);
        }
    }
}
