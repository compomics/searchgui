package eu.isas.searchgui.util;

import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Helper methods for installing and locating InstaNovo.
 *
 * @author CompOmics
 */
public class InstaNovoSetup {

    /**
     * The supported InstaNovo version.
     */
    public static final String INSTANOVO_VERSION = "1.2.2";
    /**
     * The CPU package specification.
     */
    private static final String INSTANOVO_CPU_PACKAGE = "instanovo[cpu]==" + INSTANOVO_VERSION;
    /**
     * The CUDA package specification.
     */
    private static final String INSTANOVO_CUDA_PACKAGE = "instanovo[cu126]==" + INSTANOVO_VERSION;
    /**
     * The PyTorch CPU wheel index.
     */
    private static final String PYTORCH_CPU_INDEX = "https://download.pytorch.org/whl/cpu";
    /**
     * The PyTorch CUDA 12.6 wheel index.
     */
    private static final String PYTORCH_CUDA_INDEX = "https://download.pytorch.org/whl/cu126";

    /**
     * Returns the default shared CompOmics InstaNovo installation folder.
     *
     * @return the default installation folder
     */
    public static File getDefaultInstallationFolder() {

        return new File(getCompOmicsDataFolder(), "tools" + File.separator + "instanovo" + File.separator + INSTANOVO_VERSION);
    }

    /**
     * Returns the platform-specific CompOmics data folder.
     *
     * @return the CompOmics data folder
     */
    private static File getCompOmicsDataFolder() {

        String userHome = System.getProperty("user.home");

        if (isWindows()) {

            String localAppData = System.getenv("LOCALAPPDATA");

            if (localAppData != null && !localAppData.trim().isEmpty()) {
                return new File(localAppData, "CompOmics");
            }

            return new File(userHome, "AppData" + File.separator + "Local" + File.separator + "CompOmics");
        }

        if (isMacOs()) {
            return new File(userHome, "Library" + File.separator + "Application Support" + File.separator + "CompOmics");
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");

        if (xdgDataHome != null && !xdgDataHome.trim().isEmpty() && new File(xdgDataHome).isAbsolute()) {
            return new File(xdgDataHome, "compomics");
        }

        return new File(userHome, ".local" + File.separator + "share" + File.separator + "compomics");
    }

    /**
     * Installs uv if needed and creates an InstaNovo virtual environment.
     *
     * @param installationFolder the installation folder
     * @param waitingHandler the waiting handler
     *
     * @throws IOException thrown if a command fails
     * @throws InterruptedException thrown if interrupted
     */
    public static void install(File installationFolder, WaitingHandler waitingHandler) throws IOException, InterruptedException {

        if (!installationFolder.exists() && !installationFolder.mkdirs()) {
            throw new IOException("Could not create " + installationFolder + ".");
        }

        File uvExecutable = ensureUv(waitingHandler);
        boolean cudaInstall = isCudaGpuDetected();
        String instaNovoPackage = cudaInstall ? INSTANOVO_CUDA_PACKAGE : INSTANOVO_CPU_PACKAGE;
        String pytorchIndex = cudaInstall ? PYTORCH_CUDA_INDEX : PYTORCH_CPU_INDEX;

        append(waitingHandler, "Creating the InstaNovo Python environment in " + installationFolder + ".");
        runCommand(
                Arrays.asList(uvExecutable.getAbsolutePath(), "venv", "--python", "3.12", "--clear"),
                installationFolder,
                waitingHandler
        );

        append(waitingHandler, "Installing " + instaNovoPackage + (cudaInstall ? " with CUDA PyTorch wheels." : " with CPU PyTorch wheels."));
        runCommand(
                Arrays.asList(
                        uvExecutable.getAbsolutePath(),
                        "pip",
                        "install",
                        instaNovoPackage,
                        "--extra-index-url",
                        pytorchIndex,
                        "--index-strategy",
                        "unsafe-best-match"
                ),
                installationFolder,
                waitingHandler
        );
    }

    /**
     * Returns a description of the install variant selected for this machine.
     *
     * @return the install variant description
     */
    public static String getInstallVariantDescription() {

        if (isCudaGpuDetected()) {
            return "GPU installation: InstaNovo " + INSTANOVO_VERSION + " with CUDA 12.6 PyTorch wheels.";
        }

        if (isAppleSiliconMac()) {
            return "macOS installation: InstaNovo " + INSTANOVO_VERSION + " with PyTorch wheels that can use Apple Silicon MPS when available.";
        }

        if (isMacOs()) {
            return "CPU fallback installation: CUDA is not available on macOS and no Apple Silicon GPU was detected. Prediction will be very slow.";
        }

        return "CPU fallback installation: no NVIDIA/CUDA GPU was detected. Prediction will be very slow.";
    }

    /**
     * Returns true if an NVIDIA/CUDA-capable GPU is detected.
     *
     * @return true if a CUDA GPU is detected
     */
    public static boolean isCudaGpuDetected() {

        if (isMacOs()) {
            return false;
        }

        String cudaPath = System.getenv("CUDA_PATH");
        String cudaHome = System.getenv("CUDA_HOME");

        return commandSucceeds("nvidia-smi")
                || new File("/proc/driver/nvidia/version").exists()
                || findOnPath("nvcc") != null
                || new File("/usr/local/cuda/bin/nvcc").exists()
                || new File("/usr/local/cuda-12.6/bin/nvcc").exists()
                || cudaPath != null && new File(cudaPath, "bin" + File.separator + "nvcc.exe").exists()
                || cudaHome != null && new File(cudaHome, "bin" + File.separator + "nvcc.exe").exists()
                || new File("C:\\Program Files\\NVIDIA GPU Computing Toolkit\\CUDA").exists()
                || new File("C:\\Windows\\System32\\nvidia-smi.exe").exists();
    }

    /**
     * Returns the InstaNovo models file from either a checkout root or a virtual
     * environment installation.
     *
     * @param instaNovoLocation the InstaNovo location
     *
     * @return the models file, or null if not found
     */
    public static File getModelsFile(File instaNovoLocation) {

        if (instaNovoLocation == null) {
            return null;
        }

        ArrayList<File> candidates = new ArrayList<File>();
        candidates.add(new File(instaNovoLocation, "instanovo" + File.separator + "models.json"));
        candidates.add(new File(instaNovoLocation, ".venv" + File.separator + "Lib" + File.separator + "site-packages" + File.separator + "instanovo" + File.separator + "models.json"));

        File unixSitePackages = new File(instaNovoLocation, ".venv" + File.separator + "lib");

        if (unixSitePackages.exists()) {

            File[] pythonFolders = unixSitePackages.listFiles();

            if (pythonFolders != null) {
                for (File pythonFolder : pythonFolders) {
                    candidates.add(new File(pythonFolder, "site-packages" + File.separator + "instanovo" + File.separator + "models.json"));
                }
            }
        }

        for (File candidate : candidates) {
            if (candidate.exists()) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Ensures that uv is available.
     *
     * @param waitingHandler the waiting handler
     *
     * @return the uv executable
     *
     * @throws IOException thrown if uv cannot be installed
     * @throws InterruptedException thrown if interrupted
     */
    private static File ensureUv(WaitingHandler waitingHandler) throws IOException, InterruptedException {

        File uvExecutable = findUvExecutable();

        if (uvExecutable != null) {
            append(waitingHandler, "Using uv at " + uvExecutable + ".");
            return uvExecutable;
        }

        append(waitingHandler, "uv was not found. Installing uv with the official standalone installer.");
        runCommand(getUvInstallCommand(), null, waitingHandler);

        uvExecutable = findUvExecutable();

        if (uvExecutable == null) {
            throw new IOException("uv was installed, but the uv executable could not be found. Restart the application or install uv manually.");
        }

        return uvExecutable;
    }

    /**
     * Finds uv on PATH or in the default standalone installer location.
     *
     * @return the uv executable, or null if not found
     */
    private static File findUvExecutable() {

        String executableName = isWindows() ? "uv.exe" : "uv";
        File pathExecutable = findOnPath(executableName);

        if (pathExecutable != null) {
            return pathExecutable;
        }

        String userHome = System.getProperty("user.home");
        String localAppData = System.getenv("LOCALAPPDATA");

        ArrayList<File> candidates = new ArrayList<File>();
        candidates.add(new File(userHome, ".local" + File.separator + "bin" + File.separator + executableName));

        if (localAppData != null) {
            candidates.add(new File(localAppData, "uv" + File.separator + "uv.exe"));
            candidates.add(new File(localAppData, "Programs" + File.separator + "uv" + File.separator + "uv.exe"));
        }

        for (File candidate : candidates) {
            if (candidate.exists()) {
                candidate.setExecutable(true);
                return candidate;
            }
        }

        return null;
    }

    /**
     * Finds an executable on PATH.
     *
     * @param executableName the executable name
     *
     * @return the executable, or null if not found
     */
    private static File findOnPath(String executableName) {

        String path = System.getenv("PATH");

        if (path == null) {
            return null;
        }

        String[] pathEntries = path.split(File.pathSeparator);

        for (String pathEntry : pathEntries) {
            File candidate = new File(pathEntry, executableName);
            if (candidate.exists()) {
                candidate.setExecutable(true);
                return candidate;
            }
        }

        return null;
    }

    /**
     * Returns the uv standalone installer command.
     *
     * @return the command
     */
    private static List<String> getUvInstallCommand() {

        if (isWindows()) {
            return Arrays.asList(
                    "powershell",
                    "-ExecutionPolicy",
                    "ByPass",
                    "-c",
                    "$env:UV_NO_MODIFY_PATH='1'; irm https://astral.sh/uv/install.ps1 | iex"
            );
        }

        return Arrays.asList(
                "sh",
                "-c",
                "if command -v curl >/dev/null 2>&1; then curl -LsSf https://astral.sh/uv/install.sh | env UV_NO_MODIFY_PATH=1 sh; "
                + "elif command -v wget >/dev/null 2>&1; then wget -qO- https://astral.sh/uv/install.sh | env UV_NO_MODIFY_PATH=1 sh; "
                + "else echo 'Installing uv requires curl or wget.' >&2; exit 1; fi"
        );
    }

    /**
     * Runs a command.
     *
     * @param command the command
     * @param workingDirectory the working directory
     * @param waitingHandler the waiting handler
     *
     * @throws IOException thrown if the command fails
     * @throws InterruptedException thrown if interrupted
     */
    private static void runCommand(List<String> command, File workingDirectory, WaitingHandler waitingHandler) throws IOException, InterruptedException {

        append(waitingHandler, "Running: " + join(command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        if (workingDirectory != null) {
            processBuilder.directory(workingDirectory);
        }

        Process process = processBuilder.start();

        Scanner scanner = new Scanner(process.getInputStream());
        LinkedList<String> recentOutput = new LinkedList<String>();

        while (scanner.hasNextLine()) {

            if (waitingHandler != null && waitingHandler.isRunCanceled()) {
                process.destroy();
                throw new InterruptedException("InstaNovo installation canceled.");
            }

            String line = scanner.nextLine();
            recentOutput.add(line);

            while (recentOutput.size() > 20) {
                recentOutput.removeFirst();
            }

            append(waitingHandler, line);
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Command exited with status " + exitCode + ": " + join(command) + getRecentOutput(recentOutput));
        }
    }

    /**
     * Returns the recent command output for error reporting.
     *
     * @param recentOutput the recent output lines
     *
     * @return the recent command output
     */
    private static String getRecentOutput(LinkedList<String> recentOutput) {

        if (recentOutput.isEmpty()) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\nLast command output:");

        for (String line : recentOutput) {
            stringBuilder.append("\n").append(line);
        }

        return stringBuilder.toString();
    }

    /**
     * Appends text to the waiting handler.
     *
     * @param waitingHandler the waiting handler
     * @param text the text
     */
    private static void append(WaitingHandler waitingHandler, String text) {

        if (waitingHandler != null) {
            if (waitingHandler.isReport()) {
                waitingHandler.appendReport(text, true, true);
            } else {
                waitingHandler.setWaitingText(text);
            }
        }
    }

    /**
     * Joins a command line for display.
     *
     * @param command the command
     *
     * @return the display string
     */
    private static String join(List<String> command) {

        StringBuilder stringBuilder = new StringBuilder();

        for (String part : command) {

            if (stringBuilder.length() > 0) {
                stringBuilder.append(' ');
            }

            stringBuilder.append(part);
        }

        return stringBuilder.toString();
    }

    /**
     * Returns true on Windows.
     *
     * @return true on Windows
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Returns true on macOS.
     *
     * @return true on macOS
     */
    private static boolean isMacOs() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    /**
     * Returns true on Apple Silicon macOS.
     *
     * @return true on Apple Silicon macOS
     */
    private static boolean isAppleSiliconMac() {

        String architecture = System.getProperty("os.arch").toLowerCase();

        return isMacOs() && (architecture.contains("aarch64") || architecture.contains("arm64"));
    }

    /**
     * Returns true if running a command succeeds.
     *
     * @param command the command
     *
     * @return true if the command succeeds
     */
    private static boolean commandSucceeds(String command) {

        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
