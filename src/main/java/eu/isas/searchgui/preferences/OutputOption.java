package eu.isas.searchgui.preferences;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This enum lists the possible output options.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum OutputOption {

    /**
     * Groups all files in a single compressed zip folder.
     */
    grouped(0, "Single zip file", "Groups all files in a single compressed zip folder"),
    /**
     * Groups files per run (i.e. spectrum file).
     */
    run(1, "One zip file per mgf", "Groups files per run (i.e. spectrum file)"),
    /**
     * Groups files per identification algorithm.
     */
    algorithm(2, "One zip file per algorithm", "Groups files per identification algorithm"),
    /**
     * No file grouping.
     */
    no_zip(3, "No zipping", "No file grouping");

    /**
     * The index of the option.
     */
    public final int id;
    /**
     * Name of the option.
     */
    public final String name;
    /**
     * The description of the option.
     */
    public final String description;

    /**
     * Constructor.
     *
     * @param id the index of the option
     * @param name the name of the option
     * @param description the description of the option
     */
    private OutputOption(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns an array of the indexes of the different output options.
     *
     * @return an array of the indexes of the different output options
     */
    public static int[] getOutputOptions() {
        OutputOption[] options = values();
        int[] result = new int[options.length];
        int i = 0;
        for (OutputOption outputOption : options) {
            result[i] = outputOption.id;
            i++;
        }
        return result;
    }
    
    /**
     * Returns an array of the names of the different options.
     * 
     * @return an array of the names of the different options
     */
    public static String[] getOutputOptionsNames() {
        OutputOption[] options = values();
        String[] result = new String[options.length];
        int i = 0;
        for (OutputOption outputOption : options) {
            result[i] = outputOption.name;
            i++;
        }
        return result;
    }

    /**
     * Returns the output option of the given index.
     *
     * @param id the index of the output option of interest
     *
     * @return the output option of interest
     */
    public static OutputOption getOutputOption(int id) {
        for (OutputOption outputOption : values()) {
            if (outputOption.id == id) {
                return outputOption;
            }
        }
        return null;
    }
    
    /**
     * Convenience method returning all possibilities in a command line option description format.
     * 
     * @return all possibilities in a command line option description format
     */
    public static String getCommandLineOptions() {
        OutputOption[] values = values();
        ArrayList<Integer> options = new ArrayList<Integer>(values.length);
        for (OutputOption option : values) {
            options.add(option.id);
        }
        Collections.sort(options);
        StringBuilder commandLine = new StringBuilder();
        for (int option : options) {
            if (commandLine.length() > 0) {
                commandLine.append(", ");
            }
            commandLine.append(option).append(": ").append(getOutputOption(option).description);
        }
        return commandLine.toString();
    }
}
