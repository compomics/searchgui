package eu.isas.searchgui.parameters;

import com.compomics.software.CompomicsWrapper;
import com.compomics.software.settings.PathKey;
import com.compomics.software.settings.UtilitiesPathParameters;
import eu.isas.searchgui.SearchHandler;
import eu.isas.searchgui.processbuilders.AndromedaProcessBuilder;
import eu.isas.searchgui.processbuilders.CometProcessBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class sets the path preferences for the files to read/write
 *
 * @author Marc Vaudel
 */
public class SearchGUIPathParameters {

    /**
     * Enum of the paths which can be set in SearchGUI.
     */
    public enum SearchGUIPathKey implements PathKey {

        /**
         * Directory where SearchGUI temporary files should be stored.
         */
        tempDirectory("searchgui_temp", "Folder where SearchGUI temporary files are stored.", "", true),
        /**
         * Directory where Comet temporary files should be stored.
         */
        cometDirectory("comet_temp", "Folder where Comet temporary files are stored.", "", true),
        /**
         * Directory where Andromeda temporary files should be stored.
         */
        andromedaDirectory("andromeda_temp", "Folder where Andromeda temporary files are stored.", "", true);
        /**
         * The key used to refer to this path.
         */
        private String id;
        /**
         * The description of the path usage.
         */
        private String description;
        /**
         * The default sub directory or file to use in case all paths should be
         * included in a single directory.
         */
        private String defaultSubDirectory;
        /**
         * Indicates whether the path should be a folder.
         */
        private boolean isDirectory;

        /**
         * Constructor.
         *
         * @param id the id used to refer to this path key
         * @param description the description of the path usage
         * @param defaultSubDirectory the sub directory to use in case all paths
         * should be included in a single directory
         * @param isDirectory boolean indicating whether a folder is expected
         */
        private SearchGUIPathKey(String id, String description, String defaultSubDirectory, boolean isDirectory) {
            this.id = id;
            this.description = description;
            this.defaultSubDirectory = defaultSubDirectory;
            this.isDirectory = isDirectory;
        }

        /**
         * Returns the key from its id. Null if not found.
         *
         * @param id the id of the key of interest
         *
         * @return the key of interest
         */
        public static SearchGUIPathKey getKeyFromId(String id) {
            for (SearchGUIPathKey pathKey : values()) {
                if (pathKey.id.equals(id)) {
                    return pathKey;
                }
            }
            return null;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    /**
     * Loads the path preferences from a text file.
     *
     * @param inputFile the file to load the path preferences from
     *
     * @throws FileNotFoundException thrown if the input file is not found
     * @throws IOException thrown if there are problems reading the input file
     */
    public static void loadPathParametersFromFile(File inputFile) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.equals("") && !line.startsWith("#")) {
                    loadPathParameterFromLine(line);
                }
            }
        } finally {
            br.close();
        }
    }

    /**
     * Loads a path to be set from a line.
     *
     * @param line the line where to read the path from
     * 
     * @throws FileNotFoundException thrown if the file the path refers to
     * cannot be found
     */
    public static void loadPathParameterFromLine(String line) throws FileNotFoundException {
        String id = UtilitiesPathParameters.getPathID(line);
        if (id.equals("")) {
            throw new IllegalArgumentException("Impossible to parse path in " + line + ".");
        }
        SearchGUIPathKey searchGUIPathKey = SearchGUIPathKey.getKeyFromId(id);
        if (searchGUIPathKey == null) {
            UtilitiesPathParameters.loadPathParameterFromLine(line);
        } else {
            String path = UtilitiesPathParameters.getPath(line);
            if (!path.equals(UtilitiesPathParameters.defaultPath)) {
                File file = new File(path);
                if (!file.exists()) {
                    throw new FileNotFoundException("File " + path + " not found.");
                }
                if (searchGUIPathKey.isDirectory && !file.isDirectory()) {
                    throw new FileNotFoundException("Found a file when expecting a directory for " + searchGUIPathKey.id + ".");
                }
                setPathParameter(searchGUIPathKey, path);
            }
        }
    }

    /**
     * Sets the path according to the given key and path.
     *
     * @param searchGUIPathKey the key of the path
     * @param path the path to be set
     */
    public static void setPathParameter(SearchGUIPathKey searchGUIPathKey, String path) {
        switch (searchGUIPathKey) {
            case tempDirectory:
                SearchHandler.setTempFolderPath(path);
                break;
            case cometDirectory:
                CometProcessBuilder.setTempFolder(path);
                break;
            case andromedaDirectory:
                AndromedaProcessBuilder.setTempFolderPath(path);
                break;
            default:
                throw new UnsupportedOperationException("Path " + searchGUIPathKey.id + " not implemented.");
        }
    }

    /**
     * Sets the path according to the given key and path.
     *
     * @param pathKey the key of the path
     * @param path the path to be set
     */
    public static void setPathParameter(PathKey pathKey, String path) {
        if (pathKey instanceof SearchGUIPathKey) {
            setPathParameter((SearchGUIPathKey) pathKey, path);
        } else if (pathKey instanceof UtilitiesPathParameters.UtilitiesPathKey) {
            UtilitiesPathParameters.UtilitiesPathKey utilitiesPathKey = (UtilitiesPathParameters.UtilitiesPathKey) pathKey;
            UtilitiesPathParameters.setPathParameter(utilitiesPathKey, path);
        } else {
            throw new UnsupportedOperationException("Path " + pathKey.getId() + " not implemented.");
        }
    }

    /**
     * Returns the path according to the given key and path.
     *
     * @param searchGUIPathKey the key of the path
     * @param jarFilePath path to the jar file
     *
     * @return the path 
     */
    public static String getPathParameter(SearchGUIPathKey searchGUIPathKey, String jarFilePath) {
        switch (searchGUIPathKey) {
            case tempDirectory:
                return SearchHandler.getTempFolderPath(jarFilePath);
            case cometDirectory:
                return CometProcessBuilder.getTempFolder();
            case andromedaDirectory:
                return AndromedaProcessBuilder.getTempFolderPath();
            default:
                throw new UnsupportedOperationException("Path " + searchGUIPathKey.id + " not implemented.");
        }
    }

    /**
     * Sets all the paths inside a given folder.
     *
     * @param path the path of the folder where to redirect all paths.
     *
     * @throws FileNotFoundException thrown if on of the files the paths refer
     * to cannot be found
     */
    public static void setAllPathsIn(String path) throws FileNotFoundException {
        for (SearchGUIPathKey searchGUIPathKey : SearchGUIPathKey.values()) {
            String subDirectory = searchGUIPathKey.defaultSubDirectory;
            File newFile = new File(path, subDirectory);
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            if (!newFile.exists()) {
                throw new FileNotFoundException(newFile.getAbsolutePath() + " could not be created.");
            }
            setPathParameter(searchGUIPathKey, newFile.getAbsolutePath());
        }
        UtilitiesPathParameters.setAllPathsIn(path);
    }

    /**
     * Writes all path configurations to the given file.
     *
     * @param file the destination file
     * @param jarFilePath path to the jar file
     *
     * @throws IOException thrown of the file cannot be found
     */
    public static void writeConfigurationToFile(File file, String jarFilePath) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        try {
            writeConfigurationToFile(bw, jarFilePath);
        } finally {
            bw.close();
        }
    }

    /**
     * Writes all path configurations to the given file.
     *
     * @param bw the writer to use for writing
     * @param jarFilePath path to the jar file
     *
     * @throws IOException thrown of the file cannot be found
     */
    public static void writeConfigurationToFile(BufferedWriter bw, String jarFilePath) throws IOException {
        for (SearchGUIPathKey pathKey : SearchGUIPathKey.values()) {
            writePathToFile(bw, pathKey, jarFilePath);
        }
        UtilitiesPathParameters.writeConfigurationToFile(bw);
    }

    /**
     * Writes the path of interest using the provided buffered writer.
     *
     * @param bw the writer to use for writing
     * @param pathKey the key of the path of interest
     * @param jarFilePath path to the jar file
     *
     * @throws IOException thrown of the file cannot be found
     */
    public static void writePathToFile(BufferedWriter bw, SearchGUIPathKey pathKey, String jarFilePath) throws IOException {
        bw.write(pathKey.id + UtilitiesPathParameters.separator);
        switch (pathKey) {
            case tempDirectory:
                String toWrite = SearchHandler.getTempFolderPath(jarFilePath);
                if (toWrite == null) {
                    toWrite = UtilitiesPathParameters.defaultPath;
                }
                bw.write(toWrite);
                break;
            case cometDirectory:
                toWrite = CometProcessBuilder.getTempFolder();
                if (toWrite == null) {
                    toWrite = UtilitiesPathParameters.defaultPath;
                }
                bw.write(toWrite);
                break;
            case andromedaDirectory:
                toWrite = AndromedaProcessBuilder.getTempFolderPath();
                if (toWrite == null) {
                    toWrite = UtilitiesPathParameters.defaultPath;
                }
                bw.write(toWrite);
                break;
            default:
                throw new UnsupportedOperationException("Path " + pathKey.id + " not implemented.");
        }
        bw.newLine();
    }

    /**
     * Returns a list containing the keys of the paths where the tool is not
     * able to write.
     *
     * @param jarFilePath the path to the jar file
     * 
     * @return a list containing the keys of the paths where the tool is not
     * able to write
     *
     * @throws IOException exception thrown whenever an error occurred while
     * loading the path configuration
     */
    public static ArrayList<PathKey> getErrorKeys(String jarFilePath) throws IOException {
        ArrayList<PathKey> result = new ArrayList<>();
        for (SearchGUIPathKey pathKey : SearchGUIPathKey.values()) {
            String folder = SearchGUIPathParameters.getPathParameter(pathKey, jarFilePath);
            if (folder != null && !UtilitiesPathParameters.testPath(folder)) {
                result.add(pathKey);
            }
        }
        result.addAll(UtilitiesPathParameters.getErrorKeys());
        return result;
    }
    
    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        return CompomicsWrapper.getJarFilePath(this.getClass().getResource("SearchGUIPathPreferences.class").getPath(), "SearchGUI");
    }
}
