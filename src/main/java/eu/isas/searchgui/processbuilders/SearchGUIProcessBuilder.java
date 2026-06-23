package eu.isas.searchgui.processbuilders;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.waiting.Duration;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple ancestor class to reduce code duplication in formatdb, omssacl and
 * tandem process builders.
 *
 * @author Lennart Martens
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public abstract class SearchGUIProcessBuilder implements Runnable {

    /**
     * The process to be executed as array.
     */
    ArrayList process_name_array = new ArrayList();
    /**
     * The process builder.
     */
    ProcessBuilder pb;
    /**
     * The process.
     */
    Process p;
    /**
     * The waiting handler to display the feedback.
     */
    protected WaitingHandler waitingHandler;
    /**
     * The exception handler to manage exception.
     */
    protected ExceptionHandler exceptionHandler;
    /**
     * The number of primary progress units covered by this process.
     */
    protected int primaryProgressUnits = 1;
    /**
     * The number of primary progress units already reported by this process.
     */
    private int primaryProgressUnitsCompleted = 0;
    /**
     * Pattern used to remove ANSI escape codes from external tool output.
     */
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\x1B\\[[;\\d]*[ -/]*[@-~]");
    /**
     * Pattern used to parse InstaNovo batch progress.
     */
    private static final Pattern INSTANOVO_BATCH_PROGRESS_PATTERN = Pattern.compile("\\[Batch\\s+([0-9,]+)\\s*/\\s*([0-9,]+)\\]");
    /**
     * Pattern used to parse generic percentage progress.
     */
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("(?<![0-9.])([0-9]+(?:\\.[0-9]+)?)\\s*%");
    /**
     * Pattern used to parse generic current/total progress.
     */
    private static final Pattern CURRENT_TOTAL_PATTERN = Pattern.compile("(?<![0-9.])([0-9,]+)\\s*/\\s*([0-9,]+)(?![0-9.])");

    /**
     * Empty constructor.
     */
    public SearchGUIProcessBuilder() {
    }

    @Override
    public void run() {
        try {
            if (waitingHandler == null || !waitingHandler.isRunCanceled()) {
                startProcess();
            }
        } catch (Exception e) {
            exceptionHandler.catchException(e);
        }
    }

    /**
     * Starts the process of a process builder, gets the input stream from the
     * process and shows it in a JEditorPane supporting HTML. Does not close
     * until the process is completed.
     *
     * @throws java.io.IOException Exception thrown whenever an error occurred
     * while reading the progress stream
     */
    public void startProcess() throws IOException {

        if (waitingHandler == null || !waitingHandler.isRunCanceled()) {

            Duration processDuration = new Duration();
            processDuration.start();

            p = null;
            try {
                p = pb.start();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
                ioe.printStackTrace();
            }

            // the external process could not be started (e.g. the executable or a
            // required runtime is missing); report the real cause and abort gracefully
            // instead of failing later with a null process NPE
            if (p == null) {

                if (waitingHandler != null) {
                    waitingHandler.appendReport(
                            "Could not start " + getType() + ". Please verify that it is installed and on the path. "
                            + "Note that .NET based tools such as ThermoRawFileParser require mono on Linux and macOS.",
                            true,
                            true
                    );
                    waitingHandler.setRunCanceled();
                }

                return;

            }

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {

                if (isInstaNovoProcess()) {

                    handleInstaNovoOutput(bufferedReader);

                } else if (getType().equalsIgnoreCase("Comet")) {

                    Scanner scanner = new Scanner(bufferedReader);
                    scanner.useDelimiter("\n|\b ");
                    String lastString = "";

                    // get input from scanner, send to std out and text box
                    while (scanner.hasNext() && !waitingHandler.isRunCanceled()) {
                        String temp = scanner.next();
                        if (!lastString.contains(temp)) {
                            waitingHandler.appendReport(temp + " ", false, temp.lastIndexOf("%") == -1 || temp.lastIndexOf("100%") != -1);
                        }
                        lastString = temp;
                    }

                    scanner.close();

                } else if (getType().equalsIgnoreCase("msconvert")) {

                    boolean progressOutputStarted = false;

                    String line;

                    // get input from stream
                    while ((line = bufferedReader.readLine()) != null) {

                        if (line.startsWith("processing file:") || line.startsWith("writing output file:")) {
                            waitingHandler.appendReport(line, false, true);

                            if (line.startsWith("writing output file:")) {
                                progressOutputStarted = true;
                                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                                waitingHandler.resetSecondaryProgressCounter();
                                waitingHandler.setMaxSecondaryProgressCounter(100);
                            }

                        } else {

                            if (progressOutputStarted && line.lastIndexOf("/") != -1) {

                                String[] progress = line.split("/");

                                try {
                                    int currentValue = Integer.parseInt(progress[0].trim());
                                    int maxValue = Integer.parseInt(progress[1].trim());
                                    int msConvertProgressFrequency = 100;

                                    int previousProgressPercentage = (int) Math.floor(((((double) (currentValue - msConvertProgressFrequency)) / maxValue) * 100));
                                    int currentProgressPercentage = (int) Math.floor(((((double) currentValue) / maxValue) * 100));

                                    if (currentValue != 1 && previousProgressPercentage != currentProgressPercentage) {
                                        waitingHandler.increaseSecondaryProgressCounter();
                                    }
                                } catch (NumberFormatException e) {
                                    // ignore
                                }
                            }
                        }
                    }
                } else if (getType().equalsIgnoreCase("ThermoRawFileParser")) {

                    Scanner scanner = new Scanner(bufferedReader);
                    scanner.useDelimiter("\\s|\\n");

                    waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                    waitingHandler.resetSecondaryProgressCounter();
                    waitingHandler.setMaxSecondaryProgressCounter(100);

                    // get input from scanner, send to std out and text box
                    while (scanner.hasNext() && !waitingHandler.isRunCanceled()) {
                        String temp = scanner.next();

                        if (!temp.isEmpty()) {

                            if (temp.endsWith("%")) {
                                waitingHandler.increaseSecondaryProgressCounter(10);
                            } else {
                                waitingHandler.appendReport(temp + " ", false, temp.endsWith("scans"));
                            }

                        } else {
                            waitingHandler.appendReportEndLine();
                        }
                    }

                    scanner.close();

                } else if (getType().equalsIgnoreCase("MetaMorpheus")) {

                    Scanner scanner = new Scanner(bufferedReader);
                    scanner.useDelimiter("\\s|\\n");

                    waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                    waitingHandler.resetSecondaryProgressCounter();
                    waitingHandler.setMaxSecondaryProgressCounter(100);

                    int numberOfEmptyLines = 0;
                    boolean ignoreOutput = false;
                    boolean lastProgressCounter = false;
                    boolean currentlyCountingProgress = false;

                    String currentText = "";

                    // get input from scanner, send to std out and text box
                    while (scanner.hasNext() && !waitingHandler.isRunCanceled()) {

                        String temp = scanner.next();

                        if (!currentlyCountingProgress) {

                            currentText += temp + " ";

                            if (currentText.lastIndexOf("Starting task: Task1GptmdTask") != -1) {
                                currentText = "";
                                waitingHandler.setMaxSecondaryProgressCounter(200);
                            } else if (currentText.lastIndexOf("Finished task: Task1GptmdTask") != -1) {
                                temp = "Finished task: Task1GptmdTask";
                                currentText = "";
                                ignoreOutput = false;
                            } else if (currentText.lastIndexOf("Starting task: Task1SearchTask") != -1
                                    || currentText.lastIndexOf("Starting task: Task2SearchTask") != -1) {
                                currentText = "";
                                lastProgressCounter = true;
                            }
                        }

                        if (!ignoreOutput) {

                            if (!temp.isEmpty()) {

                                if (temp.matches("[1-9]?\\d") || temp.equalsIgnoreCase("100")) {
                                    waitingHandler.increaseSecondaryProgressCounter(1);

                                    currentlyCountingProgress = true;

                                    if (Integer.parseInt(temp) == 99 || Integer.parseInt(temp) == 100) {

                                        currentlyCountingProgress = false;

                                        ignoreOutput = true;

                                        if (lastProgressCounter) {
                                            waitingHandler.setSecondaryProgressCounterIndeterminate(true);
                                            waitingHandler.appendReport("Writing MetaMorpheus output.", false, true);
                                        }
                                    }

                                } else {
                                    waitingHandler.appendReport(temp + " ", false, false);
                                }

                                numberOfEmptyLines = 0;

                            } else {

                                numberOfEmptyLines++;

                                if (numberOfEmptyLines < 3) {
                                    waitingHandler.appendReportEndLine();
                                }
                            }
                        }
                    }

                    scanner.close();

                } else {
                    String line;

                    // get input from stream and check for errors
                    while ((line = bufferedReader.readLine()) != null) {

                        line += System.getProperty("line.separator");

                        if (line.lastIndexOf("<CompomicsError>") != -1) {
                            waitingHandler.appendReportEndLine();
                            line = line.substring("<CompomicsError>".length(), line.length() - ("</CompomicsError>".length() + 2));
                            waitingHandler.appendReport(line, true, true);
                            waitingHandler.setRunCanceled();
                        } else {
                            waitingHandler.appendReport(line, false, false);
                        }
                    }
                }

            } finally {

                // check if the user has cancelled the process or not
                if (waitingHandler.isRunCanceled()) {
                    terminateProcess();
                } else {

                    processDuration.end();

                    // wait for process to terminate before reporting success
                    try {

                        int exitCode = p.waitFor();

                        if (exitCode == 0) {
                            waitingHandler.appendReportEndLine();
                            waitingHandler.appendReportEndLine();
                            waitingHandler.appendReport(getType() + " finished for " + getCurrentlyProcessedFileName() + " (" + processDuration.toString() + ").", true, true);
                            waitingHandler.appendReportEndLine();
                        } else {
                            waitingHandler.appendReportEndLine();
                            waitingHandler.appendReport(getType() + " failed for " + getCurrentlyProcessedFileName() + " with exit code " + exitCode + ".", true, true);
                            waitingHandler.setRunCanceled();
                        }

                    } catch (InterruptedException e) {

                        terminateProcess();

                        waitingHandler.appendReportEndLine();
                        waitingHandler.appendReport(getType() + " was interrupted for " + getCurrentlyProcessedFileName() + ".", true, true);
                        waitingHandler.setRunCanceled();
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /**
     * Ends the process.
     */
    public void endProcess() {
        terminateProcess();
    }

    /**
     * Terminates the external process.
     */
    private void terminateProcess() {

        if (p != null) {

            p.destroy();

            try {

                if (!p.waitFor(5, TimeUnit.SECONDS)) {
                    p.destroyForcibly();
                }

            } catch (InterruptedException e) {

                p.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Handles InstaNovo output and progress reporting.
     *
     * @param bufferedReader the process output reader
     *
     * @throws IOException if reading the output fails
     */
    private void handleInstaNovoOutput(BufferedReader bufferedReader) throws IOException {

        boolean predictionStarted = false;
        boolean secondaryProgressStarted = false;
        int secondaryProgress = 0;
        StringBuilder buffer = new StringBuilder();
        int character;

        while ((character = bufferedReader.read()) != -1 && !waitingHandler.isRunCanceled()) {

            if (character == '\r' || character == '\n') {

                InstaNovoOutputStatus status = processInstaNovoOutputLine(
                        buffer.toString(),
                        predictionStarted,
                        secondaryProgressStarted,
                        secondaryProgress
                );

                predictionStarted = status.predictionStarted;
                secondaryProgressStarted = status.secondaryProgressStarted;
                secondaryProgress = status.secondaryProgress;
                buffer.setLength(0);

            } else {
                buffer.append((char) character);
            }
        }

        if (buffer.length() > 0 && !waitingHandler.isRunCanceled()) {
            processInstaNovoOutputLine(
                    buffer.toString(),
                    predictionStarted,
                    secondaryProgressStarted,
                    secondaryProgress
            );
        }
    }

    /**
     * Processes one InstaNovo output line.
     *
     * @param line the output line
     * @param predictionStarted whether prediction progress has started
     * @param secondaryProgressStarted whether the secondary progress bar is
     * initialized
     * @param secondaryProgress the current secondary progress
     *
     * @return the updated output status
     */
    private InstaNovoOutputStatus processInstaNovoOutputLine(
            String line,
            boolean predictionStarted,
            boolean secondaryProgressStarted,
            int secondaryProgress
    ) {

        String cleanLine = stripAnsi(line).trim();

        if (cleanLine.isEmpty()) {
            return new InstaNovoOutputStatus(predictionStarted, secondaryProgressStarted, secondaryProgress);
        }

        if (cleanLine.lastIndexOf("<CompomicsError>") != -1) {
            waitingHandler.appendReportEndLine();
            cleanLine = cleanLine.substring("<CompomicsError>".length(), cleanLine.length() - "</CompomicsError>".length());
            waitingHandler.appendReport(cleanLine, true, true);
            waitingHandler.setRunCanceled();
            return new InstaNovoOutputStatus(predictionStarted, secondaryProgressStarted, secondaryProgress);
        }

        if (isInstaNovoPredictionStart(cleanLine)) {

            predictionStarted = true;

            if (!secondaryProgressStarted) {
                waitingHandler.setSecondaryProgressCounterIndeterminate(false);
                waitingHandler.resetSecondaryProgressCounter();
                waitingHandler.setMaxSecondaryProgressCounter(100);
                secondaryProgressStarted = true;
            }
        }

        Integer progressPercentage = predictionStarted ? parseInstaNovoProgressPercentage(cleanLine) : null;

        if (progressPercentage != null) {

            int boundedProgress = Math.max(0, Math.min(100, progressPercentage));
            int primaryProgress = (int) Math.floor(((double) boundedProgress * primaryProgressUnits) / 100.0);
            increaseProcessPrimaryProgress(primaryProgress - primaryProgressUnitsCompleted);

            if (secondaryProgressStarted && boundedProgress > secondaryProgress) {
                waitingHandler.increaseSecondaryProgressCounter(boundedProgress - secondaryProgress);
                secondaryProgress = boundedProgress;
            }

            if (boundedProgress > 0) {
                waitingHandler.appendReport(cleanLine, false, true);
            }

        } else {
            waitingHandler.appendReport(cleanLine, false, true);
        }

        return new InstaNovoOutputStatus(predictionStarted, secondaryProgressStarted, secondaryProgress);
    }

    /**
     * Returns true if this process is an InstaNovo process.
     *
     * @return true if this process is an InstaNovo process
     */
    private boolean isInstaNovoProcess() {
        return getType().equalsIgnoreCase("InstaNovo")
                || getType().equalsIgnoreCase("InstaNovo+")
                || getType().equalsIgnoreCase("InstaNovo with InstaNovo+ refinement");
    }

    /**
     * Returns true if the line marks the start of InstaNovo prediction progress.
     *
     * @param line the output line
     *
     * @return true if prediction has started
     */
    private static boolean isInstaNovoPredictionStart(String line) {
        return line.contains("Predicting...")
                || INSTANOVO_BATCH_PROGRESS_PATTERN.matcher(line).find();
    }

    /**
     * Parses an InstaNovo progress percentage from an output line.
     *
     * @param line the output line
     *
     * @return the progress percentage, or null if no progress could be parsed
     */
    static Integer parseInstaNovoProgressPercentage(String line) {

        String cleanLine = stripAnsi(line);
        Matcher batchMatcher = INSTANOVO_BATCH_PROGRESS_PATTERN.matcher(cleanLine);

        if (batchMatcher.find()) {
            return getProgressPercentage(batchMatcher.group(1), batchMatcher.group(2));
        }

        Matcher percentageMatcher = PERCENTAGE_PATTERN.matcher(cleanLine);

        if (percentageMatcher.find()) {

            try {
                return (int) Math.floor(Double.parseDouble(percentageMatcher.group(1)));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        Matcher currentTotalMatcher = CURRENT_TOTAL_PATTERN.matcher(cleanLine);

        if (currentTotalMatcher.find()) {
            return getProgressPercentage(currentTotalMatcher.group(1), currentTotalMatcher.group(2));
        }

        return null;
    }

    /**
     * Converts current/total strings to a progress percentage.
     *
     * @param current the current value
     * @param total the total value
     *
     * @return the progress percentage, or null if parsing fails
     */
    private static Integer getProgressPercentage(String current, String total) {

        try {

            int currentValue = Integer.parseInt(current.replace(",", ""));
            int totalValue = Integer.parseInt(total.replace(",", ""));

            if (totalValue <= 0) {
                return null;
            }

            return (int) Math.floor(((double) currentValue * 100.0) / totalValue);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Removes ANSI escape codes.
     *
     * @param line the line
     *
     * @return the line without ANSI escape codes
     */
    private static String stripAnsi(String line) {
        return ANSI_PATTERN.matcher(line).replaceAll("");
    }

    /**
     * Increases the primary progress counter for this process.
     *
     * @param increment the increment
     */
    protected void increaseProcessPrimaryProgress(int increment) {

        if (increment > 0 && waitingHandler != null && !waitingHandler.isRunCanceled()) {
            int cappedIncrement = Math.min(increment, primaryProgressUnits - primaryProgressUnitsCompleted);
            waitingHandler.increasePrimaryProgressCounter(cappedIncrement);
            primaryProgressUnitsCompleted += cappedIncrement;
        }
    }

    /**
     * Returns the number of primary progress units covered by this process.
     *
     * @return the number of primary progress units
     */
    public int getPrimaryProgressUnits() {
        return primaryProgressUnits;
    }

    /**
     * Returns the number of primary progress units already reported by this
     * process.
     *
     * @return the number of completed primary progress units
     */
    public int getPrimaryProgressUnitsCompleted() {
        return primaryProgressUnitsCompleted;
    }

    /**
     * The InstaNovo output progress status.
     */
    private static class InstaNovoOutputStatus {

        /**
         * Whether prediction progress has started.
         */
        private final boolean predictionStarted;
        /**
         * Whether the secondary progress bar is initialized.
         */
        private final boolean secondaryProgressStarted;
        /**
         * The current secondary progress.
         */
        private final int secondaryProgress;

        /**
         * Constructor.
         *
         * @param predictionStarted whether prediction progress has started
         * @param secondaryProgressStarted whether the secondary progress bar is
         * initialized
         * @param secondaryProgress the current secondary progress
         */
        private InstaNovoOutputStatus(
                boolean predictionStarted,
                boolean secondaryProgressStarted,
                int secondaryProgress
        ) {
            this.predictionStarted = predictionStarted;
            this.secondaryProgressStarted = secondaryProgressStarted;
            this.secondaryProgress = secondaryProgress;
        }
    }

    /**
     * Returns the type of the process.
     *
     * @return the type of the process
     */
    public abstract String getType();

    /**
     * Returns the file name of the currently processed file.
     *
     * @return the file name of the currently processed file
     */
    public abstract String getCurrentlyProcessedFileName();
}
