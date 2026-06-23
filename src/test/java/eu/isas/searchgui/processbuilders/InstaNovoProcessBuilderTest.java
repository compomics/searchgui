package eu.isas.searchgui.processbuilders;

import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerDummy;
import com.compomics.util.parameters.identification.tool_specific.InstaNovoParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Tests the InstaNovo process builder command lines.
 *
 * @author CompOmics
 */
public class InstaNovoProcessBuilderTest extends TestCase {

    /**
     * Tests the three supported InstaNovo execution modes.
     *
     * @throws Exception if an exception occurs
     */
    public void testInstaNovoCommandLines() throws Exception {

        File instaNovoFolder = createInstaNovoFolder();
        File spectrumFile = createFile("input", ".mgf");
        File outputFile = createFile("output", ".csv");

        InstaNovoParameters defaultParameters = new InstaNovoParameters();

        assertCommand(
                new InstaNovoProcessBuilder(instaNovoFolder, spectrumFile, outputFile, InstaNovoProcessBuilder.Mode.transformer, defaultParameters, null, null).getCommand(),
                instaNovoFolder,
                spectrumFile,
                outputFile,
                "InstaNovo",
                "transformer",
                "predict",
                "--instanovo-model",
                InstaNovoParameters.DEFAULT_INSTANOVO_MODEL,
                null,
                null
        );

        assertCommand(
                new InstaNovoProcessBuilder(instaNovoFolder, spectrumFile, outputFile, InstaNovoProcessBuilder.Mode.diffusion, defaultParameters, null, null).getCommand(),
                instaNovoFolder,
                spectrumFile,
                outputFile,
                "InstaNovo+",
                "diffusion",
                "predict",
                "--instanovo-plus-model",
                InstaNovoParameters.DEFAULT_INSTANOVO_PLUS_MODEL,
                "--no-refinement",
                null
        );

        assertCommand(
                new InstaNovoProcessBuilder(instaNovoFolder, spectrumFile, outputFile, InstaNovoProcessBuilder.Mode.refined, defaultParameters, null, null).getCommand(),
                instaNovoFolder,
                spectrumFile,
                outputFile,
                "InstaNovo with InstaNovo+ refinement",
                "predict",
                null,
                "--instanovo-model",
                InstaNovoParameters.DEFAULT_INSTANOVO_MODEL,
                "--with-refinement",
                "--instanovo-plus-model"
        );

        InstaNovoParameters advancedParameters = new InstaNovoParameters();
        advancedParameters.setInstaNovoModel("instanovo-custom");
        advancedParameters.setInstaNovoPlusModel("instanovoplus-custom");
        advancedParameters.setConfigFile("custom-config");
        advancedParameters.setNumberOfBeams(17);
        advancedParameters.setUseKnapsack(true);
        advancedParameters.setSaveAllPredictions(false);
        advancedParameters.setBatchSize(64);
        advancedParameters.setForceCpu(true);

        List<String> advancedCommand = new InstaNovoProcessBuilder(
                instaNovoFolder,
                spectrumFile,
                outputFile,
                InstaNovoProcessBuilder.Mode.refined,
                advancedParameters,
                null,
                null
        ).getCommand();

        Assert.assertTrue(advancedCommand.contains("--config-path"));
        Assert.assertTrue(advancedCommand.contains("custom-config"));
        Assert.assertTrue(advancedCommand.contains("instanovo-custom"));
        Assert.assertTrue(advancedCommand.contains("instanovoplus-custom"));
        Assert.assertTrue(advancedCommand.contains("num_beams=17"));
        Assert.assertTrue(advancedCommand.contains("use_knapsack=true"));
        Assert.assertTrue(advancedCommand.contains("save_all_predictions=false"));
        Assert.assertTrue(advancedCommand.contains("batch_size=64"));
        Assert.assertTrue(advancedCommand.contains("force_cpu=true"));
        Assert.assertTrue(advancedCommand.contains("log_interval=1"));

        InstaNovoParameters legacyParameters = new InstaNovoParameters();
        legacyParameters.setBatchSize(-1);
        Assert.assertTrue(new InstaNovoProcessBuilder(
                instaNovoFolder,
                spectrumFile,
                outputFile,
                InstaNovoProcessBuilder.Mode.transformer,
                legacyParameters,
                null,
                null
        ).getCommand().contains("batch_size=" + InstaNovoParameters.DEFAULT_BATCH_SIZE));

        InstaNovoProcessBuilder processBuilder = new InstaNovoProcessBuilder(
                instaNovoFolder,
                spectrumFile,
                outputFile,
                InstaNovoProcessBuilder.Mode.transformer,
                defaultParameters,
                null,
                null
        );
        Assert.assertEquals(InstaNovoProcessBuilder.PRIMARY_PROGRESS_UNITS, processBuilder.getPrimaryProgressUnits());

        File windowsInstaNovoFolder = createFolder("instanovo-windows-process");
        File windowsExecutable = new File(windowsInstaNovoFolder, ".venv/Scripts/instanovo.exe");
        windowsExecutable.getParentFile().mkdirs();
        windowsExecutable.createNewFile();
        windowsExecutable.deleteOnExit();

        Assert.assertEquals(
                windowsExecutable.getAbsolutePath(),
                InstaNovoProcessBuilder.getExecutable(windowsInstaNovoFolder).getAbsolutePath()
        );
    }

    /**
     * Tests parsing InstaNovo progress output.
     */
    public void testInstaNovoProgressParsing() {

        Assert.assertEquals(
                Integer.valueOf(50),
                SearchGUIProcessBuilder.parseInstaNovoProgressPercentage("[Batch 00050/00100] [00:10/00:20, 5.0it/s]:")
        );
        Assert.assertEquals(
                Integer.valueOf(42),
                SearchGUIProcessBuilder.parseInstaNovoProgressPercentage("Predicting:  42%|####2     | 42/100 [00:04<00:06, 7.5it/s]")
        );
        Assert.assertEquals(
                Integer.valueOf(25),
                SearchGUIProcessBuilder.parseInstaNovoProgressPercentage("25/100 [00:01<00:03]")
        );
        Assert.assertEquals(
                Integer.valueOf(12),
                SearchGUIProcessBuilder.parseInstaNovoProgressPercentage("\u001B[32mINFO\u001B[0m Rows filtered: 12.50%")
        );
        Assert.assertNull(SearchGUIProcessBuilder.parseInstaNovoProgressPercentage("Loading model..."));
    }

    /**
     * Tests that non-zero external process exits cancel the run.
     *
     * @throws Exception if an exception occurs
     */
    public void testFailedProcessCancelsRun() throws Exception {

        TestWaitingHandler waitingHandler = new TestWaitingHandler();
        TestProcessBuilder processBuilder = new TestProcessBuilder(waitingHandler, "echo failing; exit 7");

        processBuilder.startProcess();

        Assert.assertTrue(waitingHandler.isRunCanceled());
        Assert.assertTrue(waitingHandler.report.toString().contains("failed for input.mgf with exit code 7"));
        Assert.assertFalse(waitingHandler.report.toString().contains("finished for input.mgf"));
    }

    /**
     * Asserts a command line.
     *
     * @param command the command
     * @param instaNovoFolder the InstaNovo folder
     * @param spectrumFile the spectrum file
     * @param outputFile the output file
     * @param type the expected type
     * @param firstCommand the first command
     * @param secondCommand the second command
     * @param modelFlag the model flag
     * @param modelName the model name
     * @param modeFlag the mode flag
     * @param extraFlag the extra flag
     */
    private void assertCommand(
            List<String> command,
            File instaNovoFolder,
            File spectrumFile,
            File outputFile,
            String type,
            String firstCommand,
            String secondCommand,
            String modelFlag,
            String modelName,
            String modeFlag,
            String extraFlag
    ) {

        Assert.assertEquals(new File(instaNovoFolder, ".venv/bin/instanovo").getAbsolutePath(), command.get(0));
        Assert.assertTrue(command.contains(firstCommand));

        if (secondCommand != null) {
            Assert.assertTrue(command.contains(secondCommand));
        }

        Assert.assertTrue(command.contains("--data-path"));
        Assert.assertTrue(command.contains(spectrumFile.getAbsolutePath()));
        Assert.assertTrue(command.contains("--output-path"));
        Assert.assertTrue(command.contains(outputFile.getAbsolutePath()));
        Assert.assertTrue(command.contains("--denovo"));
        Assert.assertTrue(command.contains(modelFlag));
        Assert.assertTrue(command.contains(modelName));

        if (modeFlag != null) {
            Assert.assertTrue(command.contains(modeFlag));
        }

        if (extraFlag != null) {
            Assert.assertTrue(command.contains(extraFlag));
        }

        InstaNovoProcessBuilder.Mode mode;
        if (type.equals("InstaNovo+")) {
            mode = InstaNovoProcessBuilder.Mode.diffusion;
        } else if (type.equals("InstaNovo with InstaNovo+ refinement")) {
            mode = InstaNovoProcessBuilder.Mode.refined;
        } else {
            mode = InstaNovoProcessBuilder.Mode.transformer;
        }

        Assert.assertTrue(command.contains("num_beams=5"));
        Assert.assertTrue(command.contains("use_knapsack=false"));
        Assert.assertTrue(command.contains("save_all_predictions=true"));
        Assert.assertTrue(command.contains("batch_size=" + InstaNovoParameters.DEFAULT_BATCH_SIZE));
        Assert.assertTrue(command.contains("force_cpu=false"));
        Assert.assertTrue(command.contains("log_interval=1"));
        Assert.assertFalse(command.contains("batch_size=-1"));

        Assert.assertEquals(type, new InstaNovoProcessBuilder(instaNovoFolder, spectrumFile, outputFile, mode, new InstaNovoParameters(), null, null).getType());
    }

    /**
     * Creates a temporary InstaNovo folder with a virtual environment
     * executable.
     *
     * @return the InstaNovo folder
     *
     * @throws Exception if an exception occurs
     */
    private File createInstaNovoFolder() throws Exception {

        File folder = createFolder("instanovo-process");
        File executable = new File(folder, ".venv/bin/instanovo");
        executable.getParentFile().mkdirs();
        executable.createNewFile();
        executable.deleteOnExit();

        return folder;
    }

    /**
     * Creates a temporary file.
     *
     * @param prefix the prefix
     * @param suffix the suffix
     *
     * @return the file
     *
     * @throws Exception if an exception occurs
     */
    private File createFile(String prefix, String suffix) throws Exception {

        File file = File.createTempFile(prefix, suffix);
        file.deleteOnExit();

        return file;
    }

    /**
     * Creates a temporary folder.
     *
     * @param prefix the prefix
     *
     * @return the folder
     *
     * @throws Exception if an exception occurs
     */
    private File createFolder(String prefix) throws Exception {

        File folder = File.createTempFile(prefix, "");
        folder.delete();
        folder.mkdirs();
        folder.deleteOnExit();

        return folder;
    }

    /**
     * Test process builder.
     */
    private static class TestProcessBuilder extends SearchGUIProcessBuilder {

        /**
         * Constructor.
         *
         * @param waitingHandler the waiting handler
         * @param command the shell command
         */
        private TestProcessBuilder(WaitingHandler waitingHandler, String command) {

            this.waitingHandler = waitingHandler;
            process_name_array.add("sh");
            process_name_array.add("-c");
            process_name_array.add(command);
            pb = new ProcessBuilder(process_name_array);
            pb.redirectErrorStream(true);
        }

        @Override
        public String getType() {
            return "TestTool";
        }

        @Override
        public String getCurrentlyProcessedFileName() {
            return "input.mgf";
        }
    }

    /**
     * Test waiting handler.
     */
    private static class TestWaitingHandler extends WaitingHandlerDummy {

        /**
         * Whether the run was canceled.
         */
        private boolean runCanceled = false;
        /**
         * The report.
         */
        private final StringBuilder report = new StringBuilder();

        @Override
        public void setRunCanceled() {
            runCanceled = true;
        }

        @Override
        public void appendReport(String report, boolean includeDate, boolean addNewLine) {
            this.report.append(report);
            if (addNewLine) {
                this.report.append(System.getProperty("line.separator"));
            }
        }

        @Override
        public void appendReportEndLine() {
            report.append(System.getProperty("line.separator"));
        }

        @Override
        public boolean isRunCanceled() {
            return runCanceled;
        }

    }
}
