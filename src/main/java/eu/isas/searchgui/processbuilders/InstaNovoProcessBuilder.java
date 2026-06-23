package eu.isas.searchgui.processbuilders;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.parameters.identification.tool_specific.InstaNovoParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Process builder for InstaNovo and InstaNovo+.
 *
 * @author CompOmics
 */
public class InstaNovoProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The InstaNovo executable name.
     */
    public static final String EXECUTABLE_FILE_NAME = "instanovo";
    /**
     * The number of primary progress units allocated to one InstaNovo
     * prediction run.
     */
    public static final int PRIMARY_PROGRESS_UNITS = 200;

    /**
     * InstaNovo execution modes.
     */
    public enum Mode {
        transformer,
        diffusion,
        refined
    }

    /**
     * The input spectrum file.
     */
    private final File spectrumFile;
    /**
     * The output file.
     */
    private final File outputFile;
    /**
     * The mode.
     */
    private final Mode mode;

    /**
     * Constructor.
     *
     * @param instaNovoFolder the InstaNovo folder
     * @param spectrumFile the spectrum file
     * @param outputFile the output file
     * @param mode the mode
     * @param instaNovoParameters the InstaNovo parameters
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the exception handler
     */
    public InstaNovoProcessBuilder(
            File instaNovoFolder,
            File spectrumFile,
            File outputFile,
            Mode mode,
            InstaNovoParameters instaNovoParameters,
            WaitingHandler waitingHandler,
            ExceptionHandler exceptionHandler
    ) {

        this.spectrumFile = spectrumFile;
        this.outputFile = outputFile;
        this.mode = mode;
        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        this.primaryProgressUnits = PRIMARY_PROGRESS_UNITS;

        if (instaNovoParameters == null) {
            instaNovoParameters = new InstaNovoParameters();
        }

        File executableFile = getExecutable(instaNovoFolder);
        process_name_array.add(executableFile.getPath());

        if (mode == Mode.transformer) {
            process_name_array.add("transformer");
            process_name_array.add("predict");
        } else if (mode == Mode.diffusion) {
            process_name_array.add("diffusion");
            process_name_array.add("predict");
        } else {
            process_name_array.add("predict");
        }

        process_name_array.add("--data-path");
        process_name_array.add(spectrumFile.getAbsolutePath());
        process_name_array.add("--output-path");
        process_name_array.add(outputFile.getAbsolutePath());
        process_name_array.add("--denovo");

        if (mode == Mode.transformer || mode == Mode.refined) {
            process_name_array.add("--instanovo-model");
            process_name_array.add(instaNovoParameters.getInstaNovoModel());
        }

        if (mode == Mode.diffusion || mode == Mode.refined) {
            process_name_array.add("--instanovo-plus-model");
            process_name_array.add(instaNovoParameters.getInstaNovoPlusModel());
        }

        if (mode == Mode.diffusion) {
            process_name_array.add("--no-refinement");
        } else if (mode == Mode.refined) {
            process_name_array.add("--with-refinement");
        }

        if (instaNovoParameters.getConfigFile() != null && !instaNovoParameters.getConfigFile().trim().isEmpty()) {
            process_name_array.add("--config-path");
            process_name_array.add(instaNovoParameters.getConfigFile());
        }

        process_name_array.add("num_beams=" + instaNovoParameters.getNumberOfBeams());
        process_name_array.add("use_knapsack=" + Boolean.toString(instaNovoParameters.isUseKnapsack()));
        process_name_array.add("save_all_predictions=" + Boolean.toString(instaNovoParameters.isSaveAllPredictions()));

        if (instaNovoParameters.getBatchSize() > 0) {
            process_name_array.add("batch_size=" + instaNovoParameters.getBatchSize());
        }

        process_name_array.add("force_cpu=" + Boolean.toString(instaNovoParameters.isForceCpu()));
        process_name_array.add("log_interval=1");

        process_name_array.trimToSize();

        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "instanovo command: ");
        for (Object element : process_name_array) {
            System.out.print(element + " ");
        }
        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);
        if (instaNovoFolder != null && instaNovoFolder.exists()) {
            pb.directory(instaNovoFolder);
        }
        pb.redirectErrorStream(true);
    }

    /**
     * Returns the executable.
     *
     * @param instaNovoFolder the InstaNovo folder
     *
     * @return the executable
     */
    public static File getExecutable(File instaNovoFolder) {

        if (instaNovoFolder != null) {

            String[] relativeExecutablePaths = {
                ".venv" + File.separator + "bin" + File.separator + EXECUTABLE_FILE_NAME,
                ".venv" + File.separator + "Scripts" + File.separator + EXECUTABLE_FILE_NAME,
                ".venv" + File.separator + "Scripts" + File.separator + EXECUTABLE_FILE_NAME + ".exe",
                EXECUTABLE_FILE_NAME,
                EXECUTABLE_FILE_NAME + ".exe"
            };

            for (String relativeExecutablePath : relativeExecutablePaths) {

                File executable = new File(instaNovoFolder, relativeExecutablePath);

                if (executable.exists()) {
                    executable.setExecutable(true);
                    return executable;
                }
            }
        }

        return new File(EXECUTABLE_FILE_NAME);
    }

    /**
     * Returns the command line used by the process builder.
     *
     * @return the command line used by the process builder
     */
    public List<String> getCommand() {

        ArrayList<String> result = new ArrayList<>();

        for (Object argument : process_name_array) {
            result.add(argument.toString());
        }

        return result;
    }

    @Override
    public String getType() {
        if (mode == Mode.diffusion) {
            return "InstaNovo+";
        } else if (mode == Mode.refined) {
            return "InstaNovo with InstaNovo+ refinement";
        }
        return "InstaNovo";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return spectrumFile.getName();
    }
}
