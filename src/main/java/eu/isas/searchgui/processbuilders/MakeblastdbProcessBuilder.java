package eu.isas.searchgui.processbuilders;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;

/**
 * This class takes care of starting the Makeblastdb Process. Mandatory when
 * using OMSSA.
 *
 * @author Ravi Tharakan
 * @author Lennart Martens
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class MakeblastdbProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * Default Mac folder.
     */
    private static final String DEFALUT_MAC_FOLDER = "resources" + File.separator + "makeblastdb" + File.separator + "osx";
    /**
     * Default Windows folder.
     */
    private static final String DEFALUT_WINDOWS_FOLDER = "resources" + File.separator + "makeblastdb" + File.separator + "windows";
    /**
     * Default Linux 32 bit folder.
     */
    private static final String DEFALUT_LINUX_32_BIT_FOLDER = "resources" + File.separator + "makeblastdb" + File.separator + "linux" + File.separator + "linux_32bit";
    /**
     * Default Linux 64 bit folder.
     */
    private static final String DEFALUT_LINUX_64_BIT_FOLDER = "resources" + File.separator + "makeblastdb" + File.separator + "linux" + File.separator + "linux_64bit";
    /**
     * The name of the makeblastdb executable.
     */
    public final static String EXECUTABLE_FILE_NAME = "makeblastdb";
    /**
     * The FASTA sequence database file to process.
     */
    private File iDatabaseFile = null;
    /**
     * Boolean indicating that the process is canceled
     */
    private boolean isCanceled = false;

    /**
     * Constructor.
     *
     * @param pathToJarFile the path to the jar file
     * @param aDatabaseFile File with the DB file to be formatted
     * @param makeblastdbLocation the location of makeblastdb
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     */
    public MakeblastdbProcessBuilder(String pathToJarFile, File aDatabaseFile, File makeblastdbLocation, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) {

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;

        File makeBlastDb;

        if (makeblastdbLocation != null) {
            makeBlastDb = new File(makeblastdbLocation, EXECUTABLE_FILE_NAME);
        } else {
            makeBlastDb = new File(pathToJarFile + File.separator + getMakeblastdbFolder() + File.separator + EXECUTABLE_FILE_NAME);
        }

        // make sure that the makeblastdb file is executable
        try {
            makeBlastDb.setExecutable(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // full path to executable
        process_name_array.add(makeBlastDb.getAbsolutePath());

        // add arguments for database file
        process_name_array.add("-in");
        process_name_array.add(aDatabaseFile.getName());

        // mandatory option in newer makeblastdb versions
        process_name_array.add("-dbtype");
        process_name_array.add("prot");

        // parse the sequence ids
//        process_name_array.add("-parse_seqids"); // @TODO: make this into an option in the gui?
//        process_name_array.trimToSize();
        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "makeblastdb command: ");

        for (Object element : process_name_array) {
            System.out.print(element + " ");
        }

        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);

        pb.directory(aDatabaseFile.getAbsoluteFile().getParentFile());

        // set error out and std out to same stream
        pb.redirectErrorStream(true);
        iDatabaseFile = aDatabaseFile;
    }

    /**
     * Returns the folder containing the makeblastdb script. Depends on the
     * operation system.
     *
     * @return the folder containing the makeblastdb script
     */
    public static String getMakeblastdbFolder() {
        String operating_system = System.getProperty("os.name").toLowerCase();
        if (operating_system.contains("mac os")) {
            return DEFALUT_MAC_FOLDER;
        } else if (operating_system.contains("windows")) {
            return DEFALUT_WINDOWS_FOLDER;
        } else {
            String arch = System.getProperty("os.arch").toLowerCase();
            if (arch.lastIndexOf("64") != -1) {
                return DEFALUT_LINUX_64_BIT_FOLDER;
            } else {
                return DEFALUT_LINUX_32_BIT_FOLDER;
            }
        }
    }

    /**
     * Checks if makeblastdb has already been run on this file.
     *
     * @return boolean returns true if makeblastdb has been run
     */
    public boolean needsFormatting() {
        boolean result = true;
        String[] list = iDatabaseFile.getAbsoluteFile().getParentFile().list();
        // Get the filename.
        String name = iDatabaseFile.getName();

        // Find all three processed files.
        boolean phr = false;
        boolean pin = false;
        boolean psq = false;
        for (String s : list) {
            if (s.equals(name + ".phr")) {
                phr = true;
            }
            if (s.equals(name + ".pin")) {
                pin = true;
            }
            if (s.equals(name + ".psq")) {
                psq = true;
            }
        }

        if (phr && pin && psq) {
            result = false;
        }

        return result;
    }

    /**
     * Starts the process of a process builder, gets the input stream from the
     * process and shows it in a JTextArea. Does not close until the process is
     * completed.
     *
     * @throws java.io.IOException Exception thrown whenever an error occurred
     * while reading the progress stream
     */
    public void startProcess() throws IOException {
        super.startProcess();
        if (isCanceled) {
            // remove incomplete db index files.
            File tempFile = new File(iDatabaseFile.getAbsolutePath() + ".phr");
            deleteFile(tempFile);
            tempFile = new File(iDatabaseFile.getAbsolutePath() + ".pin");
            deleteFile(tempFile);
            tempFile = new File(iDatabaseFile.getAbsolutePath() + ".psq");
            deleteFile(tempFile);
        }
    }

    /**
     * Cancels the process.
     */
    public void endProcess() {
        isCanceled = true;
        super.endProcess();
    }

    /**
     * Tries to the delete the given file.
     *
     * @param aFile the file to delete
     */
    private void deleteFile(File aFile) {
        int count = 0;
        boolean deleteOK = true;
        while (!aFile.delete()) {
            count++;
            if (count > 5) {
                deleteOK = false;
                break;
            }
        }
        if (!deleteOK) {
            System.err.println(" *** Failed to deleted file " + aFile.getAbsolutePath() + "! ***");
        }
    }

    /**
     * Returns the type of the process.
     *
     * @return the type of the process
     */
    public String getType() {
        return "Database Formating Process";
    }

    /**
     * Returns the file name of the currently processed file.
     *
     * @return the file name of the currently processed file
     */
    public String getCurrentlyProcessedFileName() {
        return iDatabaseFile.getName();
    }
}
