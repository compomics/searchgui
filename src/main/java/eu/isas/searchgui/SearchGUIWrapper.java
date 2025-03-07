package eu.isas.searchgui;

import com.compomics.software.CompomicsWrapper;
import com.compomics.software.settings.PathKey;
import com.compomics.software.settings.UtilitiesPathParameters;
import eu.isas.searchgui.parameters.SearchGUIPathParameters;
import eu.isas.searchgui.utilities.Properties;
import java.io.*;
import java.util.ArrayList;

/**
 * A wrapper class used to start the jar file with parameters. The parameters
 * are read from the JavaOptions file.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class SearchGUIWrapper extends CompomicsWrapper {

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     */
    public SearchGUIWrapper() {
        this(null);
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args the arguments to pass to the tool
     */
    public SearchGUIWrapper(String[] args) {

        // get the version number set in the pom file
        String jarFileName = "SearchGUI-" + new Properties().getVersion() + ".jar";
        String path = getJarFilePath();
        File jarFile = new File(path, jarFileName);

        // get the splash 
        String splash = "searchgui-splash.png";
        String mainClass = "eu.isas.searchgui.gui.SearchGUI";

        // set path for utilities preferences
        try {
            setPathConfiguration();
        } catch (Exception e) {
            System.out.println("Unable to load the path configurations. Default paths will be used.");
        }
        try {
            ArrayList<PathKey> errorKeys = SearchGUIPathParameters.getErrorKeys(new File(getJarFilePath()));
            if (!errorKeys.isEmpty()) {
                System.out.println("Unable to write in the following configuration folders. Please edit the configuration paths.");
                for (PathKey pathKey : errorKeys) {
                    System.out.println(pathKey.getId() + ": " + pathKey.getDescription());
                }
            }
        } catch (Exception e) {
            System.out.println("Unable to load the path configurations. Default paths will be used.");
        }

        launchTool("SearchGUI", jarFile, splash, mainClass, args);
    }

    /**
     * Sets the path configuration.
     */
    private void setPathConfiguration() throws IOException {
        File pathConfigurationFile = new File(getJarFilePath(), UtilitiesPathParameters.configurationFileName);
        if (pathConfigurationFile.exists()) {
            SearchGUIPathParameters.loadPathParametersFromFile(pathConfigurationFile);
        }
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        return CompomicsWrapper.getJarFilePath(this.getClass().getResource("SearchGUIWrapper.class").getPath(), "SearchGUI");
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new SearchGUIWrapper(args);
    }
}
