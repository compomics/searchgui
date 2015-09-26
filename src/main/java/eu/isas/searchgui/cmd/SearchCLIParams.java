package eu.isas.searchgui.cmd;

import eu.isas.searchgui.preferences.OutputOption;
import org.apache.commons.cli.Options;

/**
 * Command line option parameters for SearchCLI.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum SearchCLIParams {

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMPORTANT: Any change here must be reported in the wiki: 
    // https://github.com/compomics/searchgui/wiki/SearchCLI.
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    SPECTRUM_FILES("spectrum_files", "Spectrum files (mgf format), comma separated list or an entire folder.", true),
    OUTPUT_FOLDER("output_folder", "The output folder.", true),
    IDENTIFICATION_PARAMETERS("id_params", "The search parameters file. Generated from the GUI or using IdentificationParametersCLI.", true),
    THREADS("threads", "Number of threads to use for the processing, default: the number of cores.", false),
    OMSSA("omssa", "Turn the OMSSA search on or off (1: on, 0: off, default is '1').", false),
    XTANDEM("xtandem", "Turn the X!Tandem search on or off (1: on, 0: off, default is '1').", false),
    MSGF("msgf", "Turn the MS-GF+ search on or off (1: on, 0: off, default is '1').", false),
    MS_AMANDA("ms_amanda", "Turn the MS Amanda search on or off (1: on, 0: off, default is '1').", false),
    MYRIMATCH("myrimatch", "Turn the MyriMatch search on or off (1: on, 0: off, default is '1').", false),
    COMET("comet", "Turn the Comet search on or off (1: on, 0: off, default is '1').", false),
    TIDE("tide", "Turn the Tide search on or off (1: on, 0: off, default is '1').", false),
    ANDROMEDA("andromeda", "Turn the Andromeda search on or off (1: on, 0: off, default is '1').", false),
    OMSSA_LOCATION("omssa_folder", "The folder where OMSSA is installed, defaults to the provided version for the given OS.", false),
    XTANDEM_LOCATION("xtandem_folder", "The folder where X!Tandem is installed, defaults to the provided version for the given OS.", false),
    MSGF_LOCATION("msgf_folder", "The folder where MS-GF+ is installed, defaults to the included version.", false),
    MS_AMANDA_LOCATION("ms_amanda_folder", "The folder where MS Amanda is installed, defaults to the included version.", false),
    MYRIMATCH_LOCATION("myrimatch_folder", "The folder where MyriMatch is installed, defaults to the included version.", false),
    COMET_LOCATION("comet_folder", "The folder where Comet is installed, defaults to the included version.", false),
    TIDE_LOCATION("tide_folder", "The folder where Tide is installed, defaults to the included version.", false),
    ANDROMEDA_LOCATION("andromeda_folder", "The folder where Andromeda is installed, defaults to the included version.", false),
    MGF_SPLITTING_LIMIT("mgf_splitting", "The maximum mgf file size in MB before splitting the mgf. Default is '1000'.", false),
    MGF_MAX_SPECTRA("mgf_spectrum_count", "The maximum number of spectra per mgf file when splitting. Default is '25000'.", false),
    DUPLICATE_TITLE_HANDLING("correct_titles", "Correct for duplicate spectrum titles. (0: no correction, 1: rename spectra, 2: delete spectra, default is '1').", false),
    MISSING_TITLE_HANDLING("missing_titles", "Add missing spectrum titles. (0: no correction, 1: add missing spectrum titles, default is '0').", false),
    SPECIES("species", "The species to use for the gene annotation. Supported species are listed in the GUI.", false),
    SPECIES_TYPE("species_type", "The species type to use for the gene annotation. Supported species types are listed in the GUI.", false),
    OUTPUT_OPTION("output_option", "Optional result file compression (" + OutputOption.getCommandLineOptions() + "), default is '0'.", false),
    OUTPUT_DATA("output_data", "Include mgf and FASTA file in zipped output (0: no, 1: yes, default is '0').", false),
    OUTPUT_DATE("output_date", "Include date in output name (0: no, 1: yes, default is '0').", false),
    MAKEBLASTDB_LOCATION("makeblastdb_folder", "The folder where makeblastdb is installed, defaults to the provided version for the given OS.", false),
    PROTEIN_INDEX("protein_index", "Turn the FASTA file indexing on/off. (1: on, 0: off, default is '0').", false),
    FASTA_FILE("fasta", "Optional path to the database to search. The value in the search parameters will be overridden.", false);

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
     * Private constructor managing the various variables for the enum
     * instances.
     *
     * @param id the id
     * @param description the description
     * @param mandatory is the parameter mandatory
     */
    private SearchCLIParams(String id, String description, boolean mandatory) {
        this.id = id;
        this.description = description;
        this.mandatory = mandatory;
    }

    /**
     * Creates the options for the command line interface based on the possible
     * values.
     *
     * @param aOptions the options object where the options will be added
     */
    public static void createOptionsCLI(Options aOptions) {
        for (SearchCLIParams identificationParametersCLIParams : values()) {
            aOptions.addOption(identificationParametersCLIParams.id, true, identificationParametersCLIParams.description);
        }
        
        // Path setup
        aOptions.addOption(PathSettingsCLIParams.ALL.id, true, PathSettingsCLIParams.ALL.description);
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
        output += "-" + String.format(formatter, SPECTRUM_FILES.id) + SPECTRUM_FILES.description + "\n";
        output += "-" + String.format(formatter, OUTPUT_FOLDER.id) + OUTPUT_FOLDER.description + "\n";
        output += "-" + String.format(formatter, IDENTIFICATION_PARAMETERS.id) + IDENTIFICATION_PARAMETERS.description + "\n";

        output += "\n\nOptional parameters:\n\n";
        output += "-" + String.format(formatter, OMSSA.id) + OMSSA.description + "\n";
        output += "-" + String.format(formatter, XTANDEM.id) + XTANDEM.description + "\n";
        output += "-" + String.format(formatter, MSGF.id) + MSGF.description + "\n";
        output += "-" + String.format(formatter, MS_AMANDA.id) + MS_AMANDA.description + "\n";
        output += "-" + String.format(formatter, MYRIMATCH.id) + MYRIMATCH.description + "\n";
        output += "-" + String.format(formatter, COMET.id) + COMET.description + "\n";
        output += "-" + String.format(formatter, TIDE.id) + TIDE.description + "\n";
        output += "-" + String.format(formatter, ANDROMEDA.id) + ANDROMEDA.description + "\n";
        output += "-" + String.format(formatter, SPECIES.id) + SPECIES.description + "\n";
        output += "-" + String.format(formatter, SPECIES_TYPE.id) + SPECIES_TYPE.description + "\n";

        output += "\n\nOptional advanced parameters:\n\n";
        output += "-" + String.format(formatter, OMSSA_LOCATION.id) + OMSSA_LOCATION.description + "\n";
        output += "-" + String.format(formatter, XTANDEM_LOCATION.id) + XTANDEM_LOCATION.description + "\n";
        output += "-" + String.format(formatter, MSGF_LOCATION.id) + MSGF_LOCATION.description + "\n";
        output += "-" + String.format(formatter, MS_AMANDA_LOCATION.id) + MS_AMANDA_LOCATION.description + "\n";
        output += "-" + String.format(formatter, MYRIMATCH_LOCATION.id) + MYRIMATCH_LOCATION.description + "\n";
        output += "-" + String.format(formatter, COMET_LOCATION.id) + COMET_LOCATION.description + "\n";
        output += "-" + String.format(formatter, TIDE_LOCATION.id) + TIDE_LOCATION.description + "\n";
        output += "-" + String.format(formatter, ANDROMEDA_LOCATION.id) + ANDROMEDA_LOCATION.description + "\n";
        output += "-" + String.format(formatter, MAKEBLASTDB_LOCATION.id) + MAKEBLASTDB_LOCATION.description + "\n";
        output += "-" + String.format(formatter, MGF_SPLITTING_LIMIT.id) + MGF_SPLITTING_LIMIT.description + "\n";
        output += "-" + String.format(formatter, MGF_MAX_SPECTRA.id) + MGF_MAX_SPECTRA.description + "\n";
        output += "-" + String.format(formatter, DUPLICATE_TITLE_HANDLING.id) + DUPLICATE_TITLE_HANDLING.description + "\n";
        output += "-" + String.format(formatter, MISSING_TITLE_HANDLING.id) + MISSING_TITLE_HANDLING.description + "\n";
        output += "-" + String.format(formatter, THREADS.id) + THREADS.description + "\n";
        output += "-" + String.format(formatter, OUTPUT_OPTION.id) + OUTPUT_OPTION.description + "\n";
        output += "-" + String.format(formatter, OUTPUT_DATA.id) + OUTPUT_DATA.description + "\n";
        output += "-" + String.format(formatter, OUTPUT_DATE.id) + OUTPUT_DATE.description + "\n";
        output += "-" + String.format(formatter, PROTEIN_INDEX.id) + PROTEIN_INDEX.description + "\n";
        output += "-" + String.format(formatter, FASTA_FILE.id) + FASTA_FILE.description + "\n";

        output += "\n\nOptional temporary folder:\n\n";
        output += "-" + String.format(formatter, PathSettingsCLIParams.ALL.id) + PathSettingsCLIParams.ALL.description + "\n";

        return output;
    }
}
