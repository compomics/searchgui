package eu.isas.searchgui.gui;

import com.compomics.software.CommandLineUtils;
import com.compomics.software.CompomicsWrapper;
import com.compomics.software.ToolFactory;
import com.compomics.software.autoupdater.MavenJarFile;
import com.compomics.software.dialogs.JavaHomeOrMemoryDialogParent;
import com.compomics.software.dialogs.JavaSettingsDialog;
import com.compomics.software.dialogs.PeptideShakerSetupDialog;
import com.compomics.software.dialogs.ProteoWizardSetupDialog;
import com.compomics.software.settings.PathKey;
import com.compomics.software.settings.UtilitiesPathPreferences;
import com.compomics.software.settings.gui.PathSettingsDialog;
import com.compomics.util.Util;
import com.compomics.util.db.DerbyUtil;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.exceptions.exception_handlers.FrameExceptionHandler;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationParametersFactory;
import com.compomics.util.experiment.identification.protein_sequences.FastaIndex;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.AndromedaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.CometParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsAmandaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsgfParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MyriMatchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.OmssaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.TideParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.XtandemParameters;
import com.compomics.util.experiment.io.massspectrometry.MgfIndex;
import com.compomics.util.experiment.io.massspectrometry.MgfReader;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.experiment.massspectrometry.proteowizard.MsConvertParameters;
import com.compomics.util.experiment.massspectrometry.proteowizard.MsFormat;
import com.compomics.util.experiment.massspectrometry.proteowizard.ProteoWizardFilter;
import com.compomics.util.experiment.massspectrometry.proteowizard.gui.MsConvertParametersDialog;
import com.compomics.util.gui.JOptionEditorPane;
import com.compomics.util.gui.PrivacySettingsDialog;
import com.compomics.util.gui.UtilitiesGUIDefaults;
import com.compomics.util.gui.error_handlers.BugReport;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import eu.isas.searchgui.SearchHandler;
import java.awt.Color;
import java.awt.Toolkit;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.filehandling.FileDisplayDialog;
import com.compomics.util.gui.filehandling.TempFilesManager;
import com.compomics.util.gui.parameters.IdentificationParametersEditionDialog;
import com.compomics.util.gui.parameters.ProcessingPreferencesDialog;
import com.compomics.util.gui.ptm.ModificationsDialog;
import com.compomics.util.gui.parameters.identification_parameters.SearchSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.AndromedaSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.CometSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.MsAmandaSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.MsgfSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.MyriMatchSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.OmssaSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.TideSettingsDialog;
import com.compomics.util.gui.parameters.identification_parameters.algorithm_settings.XTandemSettingsDialog;
import com.compomics.util.waiting.WaitingActionListener;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingDialog;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.LastSelectedFolder;
import com.compomics.util.preferences.ProcessingPreferences;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import eu.isas.searchgui.SearchGUIWrapper;
import eu.isas.searchgui.preferences.OutputOption;
import eu.isas.searchgui.preferences.SearchGUIPathPreferences;
import eu.isas.searchgui.processbuilders.AndromedaProcessBuilder;
import eu.isas.searchgui.processbuilders.CometProcessBuilder;
import eu.isas.searchgui.processbuilders.MsAmandaProcessBuilder;
import eu.isas.searchgui.processbuilders.MsgfProcessBuilder;
import eu.isas.searchgui.processbuilders.MyriMatchProcessBuilder;
import eu.isas.searchgui.processbuilders.OmssaclProcessBuilder;
import eu.isas.searchgui.processbuilders.TandemProcessBuilder;
import eu.isas.searchgui.processbuilders.TideSearchProcessBuilder;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import net.jimmc.jshortcut.JShellLink;

/**
 * The main frame of SearchGUI.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class SearchGUI extends javax.swing.JFrame implements JavaHomeOrMemoryDialogParent {

    /**
     * The last folder opened by the user. Defaults to user.home.
     */
    private LastSelectedFolder lastSelectedFolder = new LastSelectedFolder();
    /**
     * A simple progress dialog.
     */
    private static ProgressDialogX progressDialog;
    /**
     * The output folder.
     */
    private File outputFolder;
    /**
     * The mgf files.
     */
    private ArrayList<File> mgfFiles = new ArrayList<File>();
    /**
     * The raw files.
     */
    private ArrayList<File> rawFiles = new ArrayList<File>();
    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory;
    /**
     * The spectrum factory.
     */
    private SpectrumFactory spectrumFactory;
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory;
    /**
     * The SearchCLI instance.
     */
    private SearchHandler searchHandler;
    /**
     * The MGF reader.
     */
    private MgfReader mgfReader;
    /**
     * A boolean indicating if the user has visited the Settings tab. If the
     * user does not visit the settings tab before starting the search a warning
     * is displayed.s
     */
    private boolean settingsTabDisplayed = false;
    /**
     * The text to display when default settings are loaded.
     */
    public static final String defaultSettingsTxt = "[not selected]";
    /**
     * The text to display when user defined settings are loaded.
     */
    public static final String userSettingsTxt = "[user settings]";
    /**
     * If an mgf file exceeds this limit, the user will be asked for a split.
     */
    private double mgfMaxSize = 1000; // @TODO: should be moved to user preferences?
    /**
     * Number of spectra allowed in the split file.
     */
    private int mgfNSpectra = 25000; // @TODO: should be moved to user preferences?
    /**
     * If true, the selected spectra will be checked for duplicate spectrum
     * titles.
     */
    private boolean checkDuplicateTitles = true; // @TODO: should be moved to user preferences?
    /**
     * If true, the selected spectra will be checked for peak picking.
     */
    private boolean checkPeakPicking = true; // @TODO: should be moved to user preferences?
    /**
     * The search parameters file.
     */
    private File identificationParametersFile;
    /**
     * The search parameters.
     */
    private IdentificationParameters identificationParameters;
    /**
     * The processing preferences.
     */
    private ProcessingPreferences processingPreferences;
    /**
     * The msconvert parameters.
     */
    private MsConvertParameters msConvertParameters;
    /**
     * The horizontal padding used before and after the text in the titled
     * borders. (Needed to make it look as good in Java 7 as it did in Java
     * 6...)
     */
    public static String TITLED_BORDER_HORIZONTAL_PADDING = "";
    /**
     * If set to true all messages will be sent to a log file.
     */
    private static boolean useLogFile = true;
    /**
     * The dialog displayed during the search.
     */
    private WaitingDialog waitingDialog;
    /**
     * The utilities user preferences.
     */
    private UtilitiesUserPreferences utilitiesUserPreferences = null;
    /**
     * Reference for the separation of modifications.
     */
    public static final String MODIFICATION_SEPARATOR = "//";
    /**
     * Reference for the separation of modification and its frequency.
     */
    public static final String MODIFICATION_USE_SEPARATOR = "_";
    /**
     * The list of the default modifications.
     */
    private ArrayList<String> modificationUse = new ArrayList<String>();
    /**
     * If true, then one of the currently processed spectra has duplicate
     * titles.
     */
    private boolean validSpectrumTitles;
    /**
     * Handler for exceptions.
     */
    private FrameExceptionHandler exceptionHandler = new FrameExceptionHandler(this, "https://github.com/compomics/searchgui/issues");
    /**
     * The error message shown if there is an issue with the MS Amanda
     * installation.
     */
    public static String msAmandaErrorMessage = "Make sure that MS Amanda is installed correctly and that you have selected<br>"
            + "the correct version of MS Amanda for your system. Note that for Mac and Linux<br>"
            + "<a href=\"http://www.mono-project.com/download/\">Mono</a> has to be installed. "
            + "See the <a href=\"http://compomics.github.io/projects/searchgui.html#troubleshooting\">TroubleShooting</a> section at the SearchGUI<br>"
            + "web page for help, and the SearchGUI log for details about the error.";
    /**
     * The identification parameters factory.
     */
    private IdentificationParametersFactory identificationParametersFactory = IdentificationParametersFactory.getInstance();

    /**
     * Empty constructor for instantiation purposes.
     */
    private SearchGUI() {

    }

    /**
     * Creates a SearchGUI dialog.
     *
     * @param spectrumFiles the spectrum files (can be null)
     * @param rawFiles the raw files (can be null)
     * @param searchParametersFile the search parameters file (can be null)
     * @param outputFolder the output folder (can be null)
     * @param species the species (can be null)
     * @param speciesType the species type (can be null)
     */
    public SearchGUI(ArrayList<File> spectrumFiles, ArrayList<File> rawFiles, File searchParametersFile, File outputFolder, String species, String speciesType) {

        // set path configuration
        try {
            setPathConfiguration();
        } catch (Exception e) {
            // Will be taken care of next 
        }
        try {
            if (!SearchGUIPathPreferences.getErrorKeys(getJarFilePath()).isEmpty()) {
                editPathSettings();
            }
        } catch (Exception e) {
            editPathSettings();
        }

        ptmFactory = PTMFactory.getInstance();
        spectrumFactory = SpectrumFactory.getInstance();
        enzymeFactory = EnzymeFactory.getInstance();

        initComponents();

        // change the icon back to the default version
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")));
        setTitle("SearchGUI " + new eu.isas.searchgui.utilities.Properties().getVersion());

        setUpLogFile();

        // turn off the derby log file
        DerbyUtil.disableDerbyLog();

        // load the utilities user preferences
        try {
            utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error occurred when reading the user preferences.", "File Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // check if a newer version of SearchGUI is available
        boolean newVersion = false;
        if (!getJarFilePath().equalsIgnoreCase(".") && utilitiesUserPreferences.isAutoUpdate()) {
            newVersion = checkForNewVersion();
        }

        if (!newVersion) {

            // load enzymes
            try {
                enzymeFactory.importEnzymes(SearchHandler.getEnzymesFile(getJarFilePath()));
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error while reading " + SearchHandler.getEnzymeFile() + ".", "Enzyme File Error", JOptionPane.ERROR_MESSAGE);
            }

            // set this version as the default SearchGUI version
            if (!getJarFilePath().equalsIgnoreCase(".")) {
                String versionNumber = new eu.isas.searchgui.utilities.Properties().getVersion();
                utilitiesUserPreferences.setSearchGuiPath(new File(getJarFilePath(), "SearchGUI-" + versionNumber + ".jar").getAbsolutePath());
                UtilitiesUserPreferences.saveUserPreferences(utilitiesUserPreferences);
            }

            // Set the processing preferences
            processingPreferences = new ProcessingPreferences();
            processingPreferences.setnThreads(Runtime.getRuntime().availableProcessors());

            searchHandler = new SearchHandler(identificationParameters, outputFolder, spectrumFiles, rawFiles, searchParametersFile, processingPreferences, false, exceptionHandler);

            enableOmssaJCheckBox.setSelected(searchHandler.isOmssaEnabled());
            enableXTandemJCheckBox.setSelected(searchHandler.isXtandemEnabled());
            enableMsgfJCheckBox.setSelected(searchHandler.isMsgfEnabled());
            enableMsAmandaJCheckBox.setSelected(searchHandler.isMsAmandaEnabled());
            enableMyriMatchJCheckBox.setSelected(searchHandler.isMyriMatchEnabled());
            enableCometJCheckBox.setSelected(searchHandler.isCometEnabled());
            enableTideJCheckBox.setSelected(searchHandler.isTideEnabled());
            enableAndromedaJCheckBox.setSelected(searchHandler.isAndromedaEnabled());

            // add desktop shortcut?
            if (!getJarFilePath().equalsIgnoreCase(".")
                    && System.getProperty("os.name").lastIndexOf("Windows") != -1
                    && new File(getJarFilePath() + "/resources/conf/firstRun").exists()) {

                // @TODO: add support for desktop icons on mac and linux??
                // delete the firstRun file such that the user is not asked the next time around
                boolean fileDeleted = new File(getJarFilePath() + "/resources/conf/firstRun").delete();

                if (!fileDeleted) {
                    JOptionPane.showMessageDialog(this, "Failed to delete the file /resources/conf/firstRun.\n"
                            + "Please contact the developers.", "File Error", JOptionPane.OK_OPTION);
                }

                int value = JOptionPane.showConfirmDialog(null,
                        "Create a shortcut to SearchGUI on the desktop?",
                        "Create Desktop Shortcut?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (value == JOptionPane.YES_OPTION) {
                    addShortcutAtDeskTop();
                }
            }

            // Set msconvert parameters
            msConvertParameters = new MsConvertParameters();
            msConvertParameters.setMsFormat(MsFormat.mgf);
            msConvertParameters.addFilter(ProteoWizardFilter.peakPicking.number, "");

            settingsComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

            // set the font color for the titled borders, looks better than the default black
            UIManager.put("TitledBorder.titleColor", new Color(59, 59, 59));

            // update the horizontal padding for the titled borders
            ((TitledBorder) inputFilesPanel.getBorder()).setTitle(SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING + "Input & Output" + SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING);
            ((TitledBorder) preProcessingPanel.getBorder()).setTitle(SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING + "Pre Processing (beta)" + SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING);
            ((TitledBorder) searchEnginesLocationPanel.getBorder()).setTitle(SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING + "Search Engines" + SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING);
            ((TitledBorder) postProcessingPanel.getBorder()).setTitle(SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING + "Post Processing" + SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING);

            searchEnginesLocationPanel.repaint();
            inputFilesPanel.repaint();

            mgfReader = new MgfReader();

            loadModificationUse(searchHandler.loadModificationsUse());

            String operatingSystem = System.getProperty("os.name").toLowerCase();

            // disable myrimatch and comet if mac
            if (operatingSystem.contains("mac os")) {
                enableMyriMatchJCheckBox.setSelected(false);
                enableMyriMatchJCheckBox.setEnabled(false);
                enableMyriMatchJCheckBox.setToolTipText("Not available for Mac");
                myriMatchButton.setEnabled(false);
                myriMatchButton.setToolTipText("Not available for Mac");
                myriMatchSettingsButton.setEnabled(false);
                myriMatchSettingsButton.setToolTipText("Not available for Mac");
                myriMatchLinkLabel.setEnabled(false);
                searchHandler.setMyriMatchEnabled(false);

                enableCometJCheckBox.setSelected(false);
                enableCometJCheckBox.setEnabled(false);
                enableCometJCheckBox.setToolTipText("Not available for Mac");
                cometButton.setEnabled(false);
                cometButton.setToolTipText("Not available for Mac");
                cometSettingsButton.setEnabled(false);
                cometSettingsButton.setToolTipText("Not available for Mac");
                cometLinkLabel.setEnabled(false);
                searchHandler.setCometEnabled(false);
            }

            // disable andromeda on non-windows platforms
            if (!operatingSystem.contains("windows")) {
                enableAndromedaJCheckBox.setSelected(false);
                enableAndromedaJCheckBox.setEnabled(false);
                enableAndromedaJCheckBox.setToolTipText("Only available for Windows");
                andromedaButton.setEnabled(false);
                andromedaButton.setToolTipText("Only available for Windows");
                andromedaSettingsButton.setEnabled(false);
                andromedaSettingsButton.setToolTipText("Only available for Windows");
                andromedaLinkLabel.setEnabled(false);
                searchHandler.setCometEnabled(false);
            }

            validateSearchEngines(true);

            // set the spectra files
            if ((spectrumFiles != null && !spectrumFiles.isEmpty()) || (rawFiles != null && !rawFiles.isEmpty())) {
                setSpectrumFiles(spectrumFiles, rawFiles);

                // set default peptideshaker experiment details
                String experimentLabel = "";
                ArrayList<File> tempFiles;
                if (spectrumFiles != null) {
                    tempFiles = spectrumFiles;
                } else {
                    tempFiles = rawFiles;
                }
                for (File tempFile : tempFiles) {
                    String fileName = tempFile.getName();
                    if (experimentLabel.length() > 0) {
                        experimentLabel += "_";
                    }
                    experimentLabel += fileName.substring(0, fileName.lastIndexOf("."));
                }

                searchHandler.setExperimentLabel(experimentLabel);
                searchHandler.setSampleLabel(experimentLabel);
                searchHandler.setPeptideShakerFile(new File(tempFiles.get(0).getParentFile(), experimentLabel + ".cpsx"));
                peptideShakerCheckBox.setSelected(true);
            }

            // set the search parameters
            Vector parameterList = new Vector();
            parameterList.add("-- Select --");

            if (searchParametersFile != null) {
                this.identificationParametersFile = searchParametersFile;
                try {
                    identificationParameters = IdentificationParameters.getIdentificationParameters(searchParametersFile);
                    SearchParameters searchParameters = identificationParameters.getSearchParameters();
                    loadModifications(searchParameters);

                    identificationParametersFactory.addIdentificationParameters(identificationParameters); // @TODO: have to check if settings are already added...
                    settingsComboBox.setSelectedItem(identificationParameters.getName());

                    // load the gene mappings
                    boolean genesLoaded = identificationParameters.getGenePreferences().loadGeneMappings(getJarFilePath(), progressDialog);
                    if (!genesLoaded) {
                        JOptionPane.showMessageDialog(null, "An error occurred while loading the gene mappings.", "Gene Mapping File Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        // set the gene preferences
                        if (species != null && speciesType != null) {
                            identificationParameters.getGenePreferences().setCurrentSpecies(species);
                            identificationParameters.getGenePreferences().setCurrentSpeciesType(speciesType);
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to import search parameters from: " + searchParametersFile.getAbsolutePath() + ".", "Search Parameters",
                            JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();

                    // set the search settings to default
                    identificationParameters = null;
                    identificationParametersFile = null;
                }
            }

            for (String tempParameters : identificationParametersFactory.getParametersList()) {
                parameterList.add(tempParameters);
            }

            settingsComboBox.setModel(new javax.swing.DefaultComboBoxModel(parameterList));

            // set the results folder
            if (outputFolder != null && outputFolder.exists()) {
                setOutputFolder(outputFolder);
            }

            if (rawFiles != null && !rawFiles.isEmpty()) {
                checkProteoWizard();
                msconvertCheckBox.setSelected(!rawFiles.isEmpty());
            }

            validateInput(false);

            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    /**
     * Sets the path configuration.
     */
    private void setPathConfiguration() throws IOException {
        File pathConfigurationFile = new File(getJarFilePath(), UtilitiesPathPreferences.configurationFileName);
        if (pathConfigurationFile.exists()) {
            SearchGUIPathPreferences.loadPathPreferencesFromFile(pathConfigurationFile);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modificationOptionsPopupMenu = new javax.swing.JPopupMenu();
        editModificationsMenuItem = new javax.swing.JMenuItem();
        reporterPostProcessPanel = new javax.swing.JPanel();
        editReporterSettingsLabel = new javax.swing.JLabel();
        reporterButton = new javax.swing.JButton();
        reporterCheckBox = new javax.swing.JCheckBox();
        reporterLabel = new javax.swing.JLabel();
        jMenuItem1 = new javax.swing.JMenuItem();
        taskEditorPanel = new javax.swing.JPanel();
        searchEnginesLocationPanel = new javax.swing.JPanel();
        searchEnginesScrollPane = new javax.swing.JScrollPane();
        searchEnginesPanel = new javax.swing.JPanel();
        omssaButton = new javax.swing.JButton();
        msgfSettingsButton = new javax.swing.JButton();
        enableTideJCheckBox = new javax.swing.JCheckBox();
        msAmandaSettingsButton = new javax.swing.JButton();
        myrimatchSupportButton = new javax.swing.JButton();
        myriMatchLinkLabel = new javax.swing.JLabel();
        andromedaLinkLabel = new javax.swing.JLabel();
        andromedaButton = new javax.swing.JButton();
        tideLinkLabel = new javax.swing.JLabel();
        xtandemSettingsButton = new javax.swing.JButton();
        tideButton = new javax.swing.JButton();
        msgfSupportButton = new javax.swing.JButton();
        enableCometJCheckBox = new javax.swing.JCheckBox();
        cometLinkLabel = new javax.swing.JLabel();
        enableMyriMatchJCheckBox = new javax.swing.JCheckBox();
        omssaSupportButton = new javax.swing.JButton();
        enableMsAmandaJCheckBox = new javax.swing.JCheckBox();
        xtandemLinkLabel = new javax.swing.JLabel();
        xtandemSupportButton = new javax.swing.JButton();
        myriMatchSettingsButton = new javax.swing.JButton();
        enableMsgfJCheckBox = new javax.swing.JCheckBox();
        xtandemButton = new javax.swing.JButton();
        msAmandaSupportButton = new javax.swing.JButton();
        omssaLinkLabel = new javax.swing.JLabel();
        myriMatchButton = new javax.swing.JButton();
        andromedaSupportButton = new javax.swing.JButton();
        msAmandaLinkLabel = new javax.swing.JLabel();
        enableXTandemJCheckBox = new javax.swing.JCheckBox();
        cometSettingsButton = new javax.swing.JButton();
        msAmandaButton = new javax.swing.JButton();
        andromedaSettingsButton = new javax.swing.JButton();
        omssaSettingsButton = new javax.swing.JButton();
        enableAndromedaJCheckBox = new javax.swing.JCheckBox();
        tideSettingsButton = new javax.swing.JButton();
        msgfLinkLabel = new javax.swing.JLabel();
        tideSupportButton = new javax.swing.JButton();
        cometSupportButton = new javax.swing.JButton();
        msgfButton = new javax.swing.JButton();
        enableOmssaJCheckBox = new javax.swing.JCheckBox();
        cometButton = new javax.swing.JButton();
        inputFilesPanel = new javax.swing.JPanel();
        spectraFilesLabel = new javax.swing.JLabel();
        clearSpectraButton = new javax.swing.JButton();
        addSpectraButton = new javax.swing.JButton();
        spectraFilesTxt = new javax.swing.JTextField();
        searchSettingsLbl = new javax.swing.JLabel();
        editSettingsButton = new javax.swing.JButton();
        addSettingsButton = new javax.swing.JButton();
        resultFolderLbl = new javax.swing.JLabel();
        outputFolderTxt = new javax.swing.JTextField();
        resultFolderBrowseButton = new javax.swing.JButton();
        settingsComboBox = new javax.swing.JComboBox();
        searchButton = new javax.swing.JButton();
        aboutButton = new javax.swing.JButton();
        searchGUIPublicationLabel = new javax.swing.JLabel();
        postProcessingPanel = new javax.swing.JPanel();
        peptideShakerCheckBox = new javax.swing.JCheckBox();
        peptideShakerLabel = new javax.swing.JLabel();
        peptideShakerButton = new javax.swing.JButton();
        peptideShakerSettingsButton = new javax.swing.JButton();
        peptideShakerSupportButton = new javax.swing.JButton();
        preProcessingPanel = new javax.swing.JPanel();
        msconvertCheckBox = new javax.swing.JCheckBox();
        msconvertLabel = new javax.swing.JLabel();
        msconvertButton = new javax.swing.JButton();
        msconvertSettingsButton = new javax.swing.JButton();
        msconvertSupportButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        advancedSettingsMenuItem = new javax.swing.JMenuItem();
        processingMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        editModificationsEditMenuItem = new javax.swing.JMenuItem();
        editSearchEngineLocationsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        javaSettingsJMenuItem = new javax.swing.JMenuItem();
        resourceSettingsMenuItem = new javax.swing.JMenuItem();
        privacyMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpMenuItem = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JPopupMenu.Separator();
        logReportMenu = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        aboutMenuItem = new javax.swing.JMenuItem();

        editModificationsMenuItem.setText("Edit Modifications");
        editModificationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editModificationsMenuItemActionPerformed(evt);
            }
        });
        modificationOptionsPopupMenu.add(editModificationsMenuItem);

        editReporterSettingsLabel.setFont(editReporterSettingsLabel.getFont().deriveFont((editReporterSettingsLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        editReporterSettingsLabel.setText("<html><a href=\"\">Edit Settings</a></html>");
        editReporterSettingsLabel.setToolTipText("Edit Reporter settings");
        editReporterSettingsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editReporterSettingsLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                editReporterSettingsLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                editReporterSettingsLabelMouseExited(evt);
            }
        });

        reporterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/reporter_logo.png"))); // NOI18N
        reporterButton.setToolTipText("<html>\nOpen the Reporter web page<br>\n(under development...)\n</html>");
        reporterButton.setBorder(null);
        reporterButton.setBorderPainted(false);
        reporterButton.setContentAreaFilled(false);
        reporterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                reporterButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                reporterButtonMouseExited(evt);
            }
        });
        reporterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reporterButtonActionPerformed(evt);
            }
        });

        reporterCheckBox.setIconTextGap(15);
        reporterCheckBox.setOpaque(false);

        reporterLabel.setText("<html>Reporter - <a href=\"http://compomics.github.io/projects/reporter.html\">Quantify the Reporter Ions in Reporter</a></html>");
        reporterLabel.setToolTipText("Open the Reporter web page");
        reporterLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                reporterLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                reporterLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                reporterLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout reporterPostProcessPanelLayout = new javax.swing.GroupLayout(reporterPostProcessPanel);
        reporterPostProcessPanel.setLayout(reporterPostProcessPanelLayout);
        reporterPostProcessPanelLayout.setHorizontalGroup(
            reporterPostProcessPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reporterPostProcessPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reporterCheckBox)
                .addGap(73, 73, 73)
                .addComponent(reporterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(reporterLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 109, Short.MAX_VALUE)
                .addComponent(editReporterSettingsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        reporterPostProcessPanelLayout.setVerticalGroup(
            reporterPostProcessPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reporterPostProcessPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reporterPostProcessPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(reporterCheckBox)
                    .addComponent(reporterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reporterLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editReporterSettingsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SearchGUI");
        setMinimumSize(new java.awt.Dimension(775, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        taskEditorPanel.setBackground(new java.awt.Color(230, 230, 230));

        searchEnginesLocationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Engines"));
        searchEnginesLocationPanel.setOpaque(false);

        searchEnginesScrollPane.setBorder(null);
        searchEnginesScrollPane.setOpaque(false);

        searchEnginesPanel.setBackground(new java.awt.Color(230, 230, 230));

        omssaButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/omssa.png"))); // NOI18N
        omssaButton.setToolTipText("Enable OMSSA");
        omssaButton.setBorder(null);
        omssaButton.setBorderPainted(false);
        omssaButton.setContentAreaFilled(false);
        omssaButton.setEnabled(false);
        omssaButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                omssaButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                omssaButtonMouseExited(evt);
            }
        });
        omssaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                omssaButtonActionPerformed(evt);
            }
        });

        msgfSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        msgfSettingsButton.setToolTipText("Edit MS-GF+ Advanced Settings");
        msgfSettingsButton.setBorder(null);
        msgfSettingsButton.setBorderPainted(false);
        msgfSettingsButton.setContentAreaFilled(false);
        msgfSettingsButton.setEnabled(false);
        msgfSettingsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        msgfSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                msgfSettingsButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                msgfSettingsButtonMouseExited(evt);
            }
        });
        msgfSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msgfSettingsButtonActionPerformed(evt);
            }
        });

        enableTideJCheckBox.setSelected(true);
        enableTideJCheckBox.setToolTipText("Enable Tide");
        enableTideJCheckBox.setEnabled(false);
        enableTideJCheckBox.setOpaque(false);
        enableTideJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableTideJCheckBoxActionPerformed(evt);
            }
        });

        msAmandaSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        msAmandaSettingsButton.setToolTipText("Edit MS Amanda Advanced Settings");
        msAmandaSettingsButton.setBorder(null);
        msAmandaSettingsButton.setBorderPainted(false);
        msAmandaSettingsButton.setContentAreaFilled(false);
        msAmandaSettingsButton.setEnabled(false);
        msAmandaSettingsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        msAmandaSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                msAmandaSettingsButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                msAmandaSettingsButtonMouseExited(evt);
            }
        });
        msAmandaSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msAmandaSettingsButtonActionPerformed(evt);
            }
        });

        myrimatchSupportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/windows_and_linux_gray.png"))); // NOI18N
        myrimatchSupportButton.setToolTipText("Supported on Windows and Linux");
        myrimatchSupportButton.setBorderPainted(false);
        myrimatchSupportButton.setContentAreaFilled(false);

        myriMatchLinkLabel.setText("<html>MyriMatch Search Algorithm - <a href=\"http://fenchurch.mc.vanderbilt.edu/bumbershoot/myrimatch/\">MyriMatch web page</a></html> ");
        myriMatchLinkLabel.setToolTipText("Open the MyriMatch web page");
        myriMatchLinkLabel.setEnabled(false);
        myriMatchLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                myriMatchLinkLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                myriMatchLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                myriMatchLinkLabelMouseExited(evt);
            }
        });

        andromedaLinkLabel.setText("<html>Andromeda Search Algorithm - <a href=\"http://www.andromeda-search.org\">Andromeda web page</a></html> ");
        andromedaLinkLabel.setToolTipText("Open the Andromeda web page");
        andromedaLinkLabel.setEnabled(false);
        andromedaLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                andromedaLinkLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                andromedaLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                andromedaLinkLabelMouseExited(evt);
            }
        });

        andromedaButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/andromeda.png"))); // NOI18N
        andromedaButton.setToolTipText("Enable Andromeda");
        andromedaButton.setBorder(null);
        andromedaButton.setBorderPainted(false);
        andromedaButton.setContentAreaFilled(false);
        andromedaButton.setEnabled(false);
        andromedaButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                andromedaButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                andromedaButtonMouseExited(evt);
            }
        });
        andromedaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                andromedaButtonActionPerformed(evt);
            }
        });

        tideLinkLabel.setText("<html>Tide Search Algorithm - <a href=http://cruxtoolkit.sourceforge.net\">Tide web page</a></html> ");
        tideLinkLabel.setToolTipText("Open the Tide web page");
        tideLinkLabel.setEnabled(false);
        tideLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tideLinkLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tideLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tideLinkLabelMouseExited(evt);
            }
        });

        xtandemSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        xtandemSettingsButton.setToolTipText("Edit X!Tandem Advanced Settings");
        xtandemSettingsButton.setBorder(null);
        xtandemSettingsButton.setBorderPainted(false);
        xtandemSettingsButton.setContentAreaFilled(false);
        xtandemSettingsButton.setEnabled(false);
        xtandemSettingsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        xtandemSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                xtandemSettingsButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                xtandemSettingsButtonMouseExited(evt);
            }
        });
        xtandemSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xtandemSettingsButtonActionPerformed(evt);
            }
        });

        tideButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tide.png"))); // NOI18N
        tideButton.setToolTipText("Enable Tide");
        tideButton.setBorder(null);
        tideButton.setBorderPainted(false);
        tideButton.setContentAreaFilled(false);
        tideButton.setEnabled(false);
        tideButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tideButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tideButtonMouseExited(evt);
            }
        });
        tideButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tideButtonActionPerformed(evt);
            }
        });

        msgfSupportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/all_platforms_gray.png"))); // NOI18N
        msgfSupportButton.setToolTipText("Supported on Windows, Mac and Linux");
        msgfSupportButton.setBorderPainted(false);
        msgfSupportButton.setContentAreaFilled(false);

        enableCometJCheckBox.setSelected(true);
        enableCometJCheckBox.setToolTipText("Enable Comet");
        enableCometJCheckBox.setEnabled(false);
        enableCometJCheckBox.setOpaque(false);
        enableCometJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableCometJCheckBoxActionPerformed(evt);
            }
        });

        cometLinkLabel.setText("<html>Comet Search Algorithm - <a href=http://comet-ms.sourceforge.net\">Comet web page</a></html> ");
        cometLinkLabel.setToolTipText("Open the Comet web page");
        cometLinkLabel.setEnabled(false);
        cometLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cometLinkLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cometLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cometLinkLabelMouseExited(evt);
            }
        });

        enableMyriMatchJCheckBox.setSelected(true);
        enableMyriMatchJCheckBox.setToolTipText("Enable MyriMatch");
        enableMyriMatchJCheckBox.setEnabled(false);
        enableMyriMatchJCheckBox.setOpaque(false);
        enableMyriMatchJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableMyriMatchJCheckBoxActionPerformed(evt);
            }
        });

        omssaSupportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/all_platforms_gray.png"))); // NOI18N
        omssaSupportButton.setToolTipText("Supported on Windows, Mac and Linux");
        omssaSupportButton.setBorderPainted(false);
        omssaSupportButton.setContentAreaFilled(false);

        enableMsAmandaJCheckBox.setSelected(true);
        enableMsAmandaJCheckBox.setToolTipText("Enable MS Amanda");
        enableMsAmandaJCheckBox.setEnabled(false);
        enableMsAmandaJCheckBox.setOpaque(false);
        enableMsAmandaJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableMsAmandaJCheckBoxActionPerformed(evt);
            }
        });

        xtandemLinkLabel.setText("<html>X!Tandem Search Algorithm - <a href=\"http://www.thegpm.org/tandem\">X!Tandem web page</a></html>\n");
        xtandemLinkLabel.setToolTipText("Open the X!Tandem web page");
        xtandemLinkLabel.setEnabled(false);
        xtandemLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                xtandemLinkLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                xtandemLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                xtandemLinkLabelMouseExited(evt);
            }
        });

        xtandemSupportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/all_platforms_gray.png"))); // NOI18N
        xtandemSupportButton.setToolTipText("Supported on Windows, Mac and Linux");
        xtandemSupportButton.setBorderPainted(false);
        xtandemSupportButton.setContentAreaFilled(false);

        myriMatchSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        myriMatchSettingsButton.setToolTipText("Edit MyriMatch Advanced Settings");
        myriMatchSettingsButton.setBorder(null);
        myriMatchSettingsButton.setBorderPainted(false);
        myriMatchSettingsButton.setContentAreaFilled(false);
        myriMatchSettingsButton.setEnabled(false);
        myriMatchSettingsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        myriMatchSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                myriMatchSettingsButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                myriMatchSettingsButtonMouseExited(evt);
            }
        });
        myriMatchSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myriMatchSettingsButtonActionPerformed(evt);
            }
        });

        enableMsgfJCheckBox.setSelected(true);
        enableMsgfJCheckBox.setToolTipText("Enable MS-GF+");
        enableMsgfJCheckBox.setEnabled(false);
        enableMsgfJCheckBox.setOpaque(false);
        enableMsgfJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableMsgfJCheckBoxActionPerformed(evt);
            }
        });

        xtandemButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/xtandem.png"))); // NOI18N
        xtandemButton.setToolTipText("Enable X!Tandem");
        xtandemButton.setBorder(null);
        xtandemButton.setBorderPainted(false);
        xtandemButton.setContentAreaFilled(false);
        xtandemButton.setEnabled(false);
        xtandemButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        xtandemButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                xtandemButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                xtandemButtonMouseExited(evt);
            }
        });
        xtandemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xtandemButtonActionPerformed(evt);
            }
        });

        msAmandaSupportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/all_platforms_gray.png"))); // NOI18N
        msAmandaSupportButton.setToolTipText("Supported on Windows, Mac and Linux");
        msAmandaSupportButton.setBorderPainted(false);
        msAmandaSupportButton.setContentAreaFilled(false);

        omssaLinkLabel.setText("<html>OMSSA Search Algorithm - <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/15473683\">OMSSA web page</a></html> ");
        omssaLinkLabel.setToolTipText("Open the OMSSA web page");
        omssaLinkLabel.setEnabled(false);
        omssaLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                omssaLinkLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                omssaLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                omssaLinkLabelMouseExited(evt);
            }
        });

        myriMatchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/myrimatch.png"))); // NOI18N
        myriMatchButton.setToolTipText("Enable MyriMatch");
        myriMatchButton.setBorder(null);
        myriMatchButton.setBorderPainted(false);
        myriMatchButton.setContentAreaFilled(false);
        myriMatchButton.setEnabled(false);
        myriMatchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                myriMatchButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                myriMatchButtonMouseExited(evt);
            }
        });
        myriMatchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myriMatchButtonActionPerformed(evt);
            }
        });

        andromedaSupportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/windows_only_gray.png"))); // NOI18N
        andromedaSupportButton.setToolTipText("Supported on Windows");
        andromedaSupportButton.setBorderPainted(false);
        andromedaSupportButton.setContentAreaFilled(false);

        msAmandaLinkLabel.setText("<html>MS Amanda Search Algorithm - <a href=\"http://ms.imp.ac.at/?goto=msamanda\">MS Amanda web page</a></html> ");
        msAmandaLinkLabel.setToolTipText("Open the MS Amanda web page");
        msAmandaLinkLabel.setEnabled(false);
        msAmandaLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                msAmandaLinkLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                msAmandaLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                msAmandaLinkLabelMouseExited(evt);
            }
        });

        enableXTandemJCheckBox.setSelected(true);
        enableXTandemJCheckBox.setToolTipText("Enable X!Tandem");
        enableXTandemJCheckBox.setEnabled(false);
        enableXTandemJCheckBox.setOpaque(false);
        enableXTandemJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableXTandemJCheckBoxActionPerformed(evt);
            }
        });

        cometSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        cometSettingsButton.setToolTipText("Edit Comet Advanced Settings");
        cometSettingsButton.setBorder(null);
        cometSettingsButton.setBorderPainted(false);
        cometSettingsButton.setContentAreaFilled(false);
        cometSettingsButton.setEnabled(false);
        cometSettingsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        cometSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cometSettingsButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cometSettingsButtonMouseExited(evt);
            }
        });
        cometSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cometSettingsButtonActionPerformed(evt);
            }
        });

        msAmandaButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ms_amanda.png"))); // NOI18N
        msAmandaButton.setToolTipText("Enable MS Amanda");
        msAmandaButton.setBorder(null);
        msAmandaButton.setBorderPainted(false);
        msAmandaButton.setContentAreaFilled(false);
        msAmandaButton.setEnabled(false);
        msAmandaButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                msAmandaButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                msAmandaButtonMouseExited(evt);
            }
        });
        msAmandaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msAmandaButtonActionPerformed(evt);
            }
        });

        andromedaSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        andromedaSettingsButton.setToolTipText("Edit Andromeda Advanced Settings");
        andromedaSettingsButton.setBorder(null);
        andromedaSettingsButton.setBorderPainted(false);
        andromedaSettingsButton.setContentAreaFilled(false);
        andromedaSettingsButton.setEnabled(false);
        andromedaSettingsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        andromedaSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                andromedaSettingsButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                andromedaSettingsButtonMouseExited(evt);
            }
        });
        andromedaSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                andromedaSettingsButtonActionPerformed(evt);
            }
        });

        omssaSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        omssaSettingsButton.setToolTipText("Edit OMSSA Advanced Settings");
        omssaSettingsButton.setBorder(null);
        omssaSettingsButton.setBorderPainted(false);
        omssaSettingsButton.setContentAreaFilled(false);
        omssaSettingsButton.setEnabled(false);
        omssaSettingsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        omssaSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                omssaSettingsButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                omssaSettingsButtonMouseExited(evt);
            }
        });
        omssaSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                omssaSettingsButtonActionPerformed(evt);
            }
        });

        enableAndromedaJCheckBox.setToolTipText("Enable Andromeda");
        enableAndromedaJCheckBox.setEnabled(false);
        enableAndromedaJCheckBox.setOpaque(false);
        enableAndromedaJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableAndromedaJCheckBoxActionPerformed(evt);
            }
        });

        tideSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        tideSettingsButton.setToolTipText("Edit Tide Advanced Settings");
        tideSettingsButton.setBorder(null);
        tideSettingsButton.setBorderPainted(false);
        tideSettingsButton.setContentAreaFilled(false);
        tideSettingsButton.setEnabled(false);
        tideSettingsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        tideSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tideSettingsButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tideSettingsButtonMouseExited(evt);
            }
        });
        tideSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tideSettingsButtonActionPerformed(evt);
            }
        });

        msgfLinkLabel.setText("<html>MS-GF+ Search Algorithm - <a href=\"http://proteomics.ucsd.edu/Software/MSGFPlus\">MS-GF+ web page</a></html> ");
        msgfLinkLabel.setToolTipText("Open the MS-GF+ web page");
        msgfLinkLabel.setEnabled(false);
        msgfLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                msgfLinkLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                msgfLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                msgfLinkLabelMouseExited(evt);
            }
        });

        tideSupportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/all_platforms_gray.png"))); // NOI18N
        tideSupportButton.setToolTipText("Supported on Windows, Mac and Linux");
        tideSupportButton.setBorderPainted(false);
        tideSupportButton.setContentAreaFilled(false);

        cometSupportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/windows_and_linux_gray.png"))); // NOI18N
        cometSupportButton.setToolTipText("Supported on Windows and Linux");
        cometSupportButton.setBorderPainted(false);
        cometSupportButton.setContentAreaFilled(false);

        msgfButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/msgf.png"))); // NOI18N
        msgfButton.setToolTipText("Enable MS-GF+");
        msgfButton.setBorder(null);
        msgfButton.setBorderPainted(false);
        msgfButton.setContentAreaFilled(false);
        msgfButton.setEnabled(false);
        msgfButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                msgfButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                msgfButtonMouseExited(evt);
            }
        });
        msgfButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msgfButtonActionPerformed(evt);
            }
        });

        enableOmssaJCheckBox.setSelected(true);
        enableOmssaJCheckBox.setToolTipText("Enable OMSSA");
        enableOmssaJCheckBox.setEnabled(false);
        enableOmssaJCheckBox.setOpaque(false);
        enableOmssaJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableOmssaJCheckBoxActionPerformed(evt);
            }
        });

        cometButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/comet.png"))); // NOI18N
        cometButton.setToolTipText("Enable Comet");
        cometButton.setBorder(null);
        cometButton.setBorderPainted(false);
        cometButton.setContentAreaFilled(false);
        cometButton.setEnabled(false);
        cometButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cometButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cometButtonMouseExited(evt);
            }
        });
        cometButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cometButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout searchEnginesPanelLayout = new javax.swing.GroupLayout(searchEnginesPanel);
        searchEnginesPanel.setLayout(searchEnginesPanelLayout);
        searchEnginesPanelLayout.setHorizontalGroup(
            searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchEnginesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(searchEnginesPanelLayout.createSequentialGroup()
                            .addGap(81, 81, 81)
                            .addComponent(msAmandaButton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(enableMsAmandaJCheckBox)
                        .addGroup(searchEnginesPanelLayout.createSequentialGroup()
                            .addComponent(enableCometJCheckBox)
                            .addGap(63, 63, 63)
                            .addComponent(cometButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchEnginesPanelLayout.createSequentialGroup()
                            .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(enableTideJCheckBox)
                                .addComponent(enableAndromedaJCheckBox))
                            .addGap(53, 53, 53)
                            .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(andromedaButton)
                                .addComponent(tideButton))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                        .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, searchEnginesPanelLayout.createSequentialGroup()
                                .addComponent(enableXTandemJCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(xtandemButton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, searchEnginesPanelLayout.createSequentialGroup()
                                .addComponent(enableMyriMatchJCheckBox)
                                .addGap(63, 63, 63)
                                .addComponent(myriMatchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(searchEnginesPanelLayout.createSequentialGroup()
                        .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(searchEnginesPanelLayout.createSequentialGroup()
                                .addComponent(enableMsgfJCheckBox)
                                .addGap(45, 45, 45)
                                .addComponent(msgfButton, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(searchEnginesPanelLayout.createSequentialGroup()
                                .addComponent(enableOmssaJCheckBox)
                                .addGap(62, 62, 62)
                                .addComponent(omssaButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(34, 34, 34)))
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tideSupportButton)
                    .addComponent(xtandemSupportButton)
                    .addComponent(myrimatchSupportButton)
                    .addComponent(msAmandaSupportButton)
                    .addComponent(msgfSupportButton)
                    .addComponent(cometSupportButton)
                    .addComponent(omssaSupportButton)
                    .addComponent(andromedaSupportButton))
                .addGap(18, 18, 18)
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(myriMatchLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msgfLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(andromedaLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tideLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cometLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(omssaLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msAmandaLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xtandemLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 125, Short.MAX_VALUE)
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(msAmandaSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(myriMatchSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(omssaSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xtandemSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msgfSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cometSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tideSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(andromedaSettingsButton))
                .addContainerGap())
        );

        searchEnginesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {msgfButton, xtandemButton});

        searchEnginesPanelLayout.setVerticalGroup(
            searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchEnginesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enableXTandemJCheckBox)
                    .addComponent(xtandemButton)
                    .addComponent(xtandemSupportButton)
                    .addComponent(xtandemLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xtandemSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enableMyriMatchJCheckBox)
                    .addComponent(myriMatchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(myrimatchSupportButton)
                    .addComponent(myriMatchLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(myriMatchSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enableMsAmandaJCheckBox)
                    .addComponent(msAmandaButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msAmandaLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msAmandaSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msAmandaSupportButton))
                .addGap(0, 0, 0)
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enableMsgfJCheckBox)
                    .addComponent(msgfButton)
                    .addComponent(msgfLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msgfSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msgfSupportButton))
                .addGap(0, 0, 0)
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enableOmssaJCheckBox)
                    .addComponent(omssaButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(omssaLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(omssaSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(omssaSupportButton))
                .addGap(0, 0, 0)
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enableCometJCheckBox)
                    .addComponent(cometButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cometLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cometSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cometSupportButton))
                .addGap(0, 0, 0)
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enableTideJCheckBox)
                    .addComponent(tideButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tideLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tideSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tideSupportButton))
                .addGap(0, 0, 0)
                .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enableAndromedaJCheckBox)
                    .addComponent(andromedaButton)
                    .addComponent(andromedaSupportButton)
                    .addComponent(andromedaLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(andromedaSettingsButton))
                .addContainerGap())
        );

        searchEnginesScrollPane.setViewportView(searchEnginesPanel);

        javax.swing.GroupLayout searchEnginesLocationPanelLayout = new javax.swing.GroupLayout(searchEnginesLocationPanel);
        searchEnginesLocationPanel.setLayout(searchEnginesLocationPanelLayout);
        searchEnginesLocationPanelLayout.setHorizontalGroup(
            searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(searchEnginesScrollPane)
                .addGap(24, 24, 24))
        );
        searchEnginesLocationPanelLayout.setVerticalGroup(
            searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchEnginesLocationPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(searchEnginesScrollPane))
        );

        inputFilesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Input & Output"));
        inputFilesPanel.setOpaque(false);

        spectraFilesLabel.setForeground(new java.awt.Color(255, 0, 0));
        spectraFilesLabel.setText("Spectrum File(s)");

        clearSpectraButton.setText("Clear");
        clearSpectraButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSpectraButtonActionPerformed(evt);
            }
        });

        addSpectraButton.setText("Add");
        addSpectraButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSpectraButtonActionPerformed(evt);
            }
        });

        spectraFilesTxt.setEditable(false);
        spectraFilesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        spectraFilesTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                spectraFilesTxtMouseClicked(evt);
            }
        });

        searchSettingsLbl.setForeground(new java.awt.Color(255, 0, 0));
        searchSettingsLbl.setText("Search Settings");

        editSettingsButton.setText("Edit");
        editSettingsButton.setEnabled(false);
        editSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSettingsButtonActionPerformed(evt);
            }
        });

        addSettingsButton.setText("Add");
        addSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSettingsButtonActionPerformed(evt);
            }
        });

        resultFolderLbl.setForeground(new java.awt.Color(255, 0, 0));
        resultFolderLbl.setText("Output Folder");

        outputFolderTxt.setEditable(false);
        outputFolderTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        resultFolderBrowseButton.setText("Browse");
        resultFolderBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resultFolderBrowseButtonActionPerformed(evt);
            }
        });

        settingsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-- Select --" }));
        settingsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout inputFilesPanelLayout = new javax.swing.GroupLayout(inputFilesPanel);
        inputFilesPanel.setLayout(inputFilesPanelLayout);
        inputFilesPanelLayout.setHorizontalGroup(
            inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputFilesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputFilesPanelLayout.createSequentialGroup()
                        .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(searchSettingsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resultFolderLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(outputFolderTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 409, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(settingsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(inputFilesPanelLayout.createSequentialGroup()
                        .addComponent(spectraFilesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spectraFilesTxt)))
                .addGap(10, 10, 10)
                .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(inputFilesPanelLayout.createSequentialGroup()
                            .addComponent(addSettingsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(editSettingsButton))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inputFilesPanelLayout.createSequentialGroup()
                            .addComponent(addSpectraButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(clearSpectraButton, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)))
                    .addComponent(resultFolderBrowseButton))
                .addContainerGap())
        );

        inputFilesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addSettingsButton, addSpectraButton, clearSpectraButton, editSettingsButton, resultFolderBrowseButton});

        inputFilesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {resultFolderLbl, searchSettingsLbl, spectraFilesLabel});

        inputFilesPanelLayout.setVerticalGroup(
            inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputFilesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spectraFilesLabel)
                    .addComponent(spectraFilesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearSpectraButton)
                    .addComponent(addSpectraButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchSettingsLbl)
                    .addComponent(addSettingsButton)
                    .addComponent(settingsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editSettingsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inputFilesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resultFolderLbl)
                    .addComponent(resultFolderBrowseButton)
                    .addComponent(outputFolderTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        inputFilesPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {addSettingsButton, addSpectraButton, clearSpectraButton, editSettingsButton, resultFolderBrowseButton});

        searchButton.setBackground(new java.awt.Color(0, 153, 0));
        searchButton.setFont(searchButton.getFont().deriveFont(searchButton.getFont().getStyle() | java.awt.Font.BOLD));
        searchButton.setForeground(new java.awt.Color(255, 255, 255));
        searchButton.setText("Start the Search!");
        searchButton.setToolTipText("Click here to start the search");
        searchButton.setEnabled(false);
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        aboutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/searchgui_shadow.png"))); // NOI18N
        aboutButton.setToolTipText("Open the SearchGUI web page");
        aboutButton.setBorder(null);
        aboutButton.setBorderPainted(false);
        aboutButton.setContentAreaFilled(false);
        aboutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                aboutButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                aboutButtonMouseExited(evt);
            }
        });
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });

        searchGUIPublicationLabel.setText("<html>Please cite SearchGUI as <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/21337703\">Vaudel <i>et al.</i>: Proteomics 2011;11(5):996-9</a>.</html>");
        searchGUIPublicationLabel.setToolTipText("Open the SearchGUI publication");
        searchGUIPublicationLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchGUIPublicationLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchGUIPublicationLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchGUIPublicationLabelMouseExited(evt);
            }
        });

        postProcessingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Post Processing"));
        postProcessingPanel.setOpaque(false);
        postProcessingPanel.setPreferredSize(new java.awt.Dimension(785, 83));

        peptideShakerCheckBox.setToolTipText("Enable PeptideShaker");
        peptideShakerCheckBox.setEnabled(false);
        peptideShakerCheckBox.setIconTextGap(15);
        peptideShakerCheckBox.setOpaque(false);
        peptideShakerCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                peptideShakerCheckBoxActionPerformed(evt);
            }
        });

        peptideShakerLabel.setText("<html>PeptideShaker - <a href=\"http://compomics.github.io/projects/peptide-shaker.html\">Visualize the results in PeptideShaker</a></html>");
        peptideShakerLabel.setToolTipText("Open the PeptideShaker web page");
        peptideShakerLabel.setEnabled(false);
        peptideShakerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                peptideShakerLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                peptideShakerLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                peptideShakerLabelMouseExited(evt);
            }
        });

        peptideShakerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/peptide-shaker-medium-orange-shadow.png"))); // NOI18N
        peptideShakerButton.setToolTipText("Enable PeptideShaker");
        peptideShakerButton.setBorder(null);
        peptideShakerButton.setBorderPainted(false);
        peptideShakerButton.setContentAreaFilled(false);
        peptideShakerButton.setEnabled(false);
        peptideShakerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                peptideShakerButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                peptideShakerButtonMouseExited(evt);
            }
        });
        peptideShakerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                peptideShakerButtonActionPerformed(evt);
            }
        });

        peptideShakerSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        peptideShakerSettingsButton.setToolTipText("Edit PeptideShaker Settings");
        peptideShakerSettingsButton.setBorder(null);
        peptideShakerSettingsButton.setBorderPainted(false);
        peptideShakerSettingsButton.setContentAreaFilled(false);
        peptideShakerSettingsButton.setEnabled(false);
        peptideShakerSettingsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        peptideShakerSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                peptideShakerSettingsButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                peptideShakerSettingsButtonMouseExited(evt);
            }
        });
        peptideShakerSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                peptideShakerSettingsButtonActionPerformed(evt);
            }
        });

        peptideShakerSupportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/all_platforms_gray.png"))); // NOI18N
        peptideShakerSupportButton.setToolTipText("Supported on Windows, Mac and Linux");
        peptideShakerSupportButton.setBorderPainted(false);
        peptideShakerSupportButton.setContentAreaFilled(false);

        javax.swing.GroupLayout postProcessingPanelLayout = new javax.swing.GroupLayout(postProcessingPanel);
        postProcessingPanel.setLayout(postProcessingPanelLayout);
        postProcessingPanelLayout.setHorizontalGroup(
            postProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(postProcessingPanelLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(peptideShakerCheckBox)
                .addGap(57, 57, 57)
                .addComponent(peptideShakerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
                .addComponent(peptideShakerSupportButton)
                .addGap(20, 20, 20)
                .addComponent(peptideShakerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(peptideShakerSettingsButton)
                .addGap(33, 33, 33))
        );
        postProcessingPanelLayout.setVerticalGroup(
            postProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(postProcessingPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(postProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(peptideShakerCheckBox)
                    .addComponent(peptideShakerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideShakerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(peptideShakerSettingsButton)
                    .addComponent(peptideShakerSupportButton))
                .addGap(0, 0, 0))
        );

        preProcessingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Pre Processing (beta)"));
        preProcessingPanel.setOpaque(false);

        msconvertCheckBox.setToolTipText("Enable msconvert");
        msconvertCheckBox.setEnabled(false);
        msconvertCheckBox.setIconTextGap(15);
        msconvertCheckBox.setOpaque(false);
        msconvertCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msconvertCheckBoxActionPerformed(evt);
            }
        });

        msconvertLabel.setText("<html>msconvert File Conversion - <a href=\"http://proteowizard.sourceforge.net/downloads.shtml\">ProteoWizard web page</a></html>");
        msconvertLabel.setToolTipText("Open the ProteoWizard web page");
        msconvertLabel.setEnabled(false);
        msconvertLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                msconvertLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                msconvertLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                msconvertLabelMouseExited(evt);
            }
        });

        msconvertButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/msconvert.png"))); // NOI18N
        msconvertButton.setBorder(null);
        msconvertButton.setBorderPainted(false);
        msconvertButton.setContentAreaFilled(false);
        msconvertButton.setEnabled(false);

        msconvertSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_gray.png"))); // NOI18N
        msconvertSettingsButton.setToolTipText("Edit msconvert Settings");
        msconvertSettingsButton.setBorder(null);
        msconvertSettingsButton.setBorderPainted(false);
        msconvertSettingsButton.setContentAreaFilled(false);
        msconvertSettingsButton.setEnabled(false);
        msconvertSettingsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        msconvertSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                msconvertSettingsButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                msconvertSettingsButtonMouseExited(evt);
            }
        });
        msconvertSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msconvertSettingsButtonActionPerformed(evt);
            }
        });

        msconvertSupportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/windows_only_gray.png"))); // NOI18N
        msconvertSupportButton.setToolTipText("Supported on Windows");
        msconvertSupportButton.setBorderPainted(false);
        msconvertSupportButton.setContentAreaFilled(false);

        javax.swing.GroupLayout preProcessingPanelLayout = new javax.swing.GroupLayout(preProcessingPanel);
        preProcessingPanel.setLayout(preProcessingPanelLayout);
        preProcessingPanelLayout.setHorizontalGroup(
            preProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(preProcessingPanelLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(msconvertCheckBox)
                .addGap(55, 55, 55)
                .addComponent(msconvertButton, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(msconvertSupportButton)
                .addGap(21, 21, 21)
                .addComponent(msconvertLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(msconvertSettingsButton)
                .addGap(33, 33, 33))
        );
        preProcessingPanelLayout.setVerticalGroup(
            preProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(preProcessingPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(preProcessingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(msconvertCheckBox)
                    .addComponent(msconvertButton, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msconvertLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msconvertSettingsButton)
                    .addComponent(msconvertSupportButton)))
        );

        javax.swing.GroupLayout taskEditorPanelLayout = new javax.swing.GroupLayout(taskEditorPanel);
        taskEditorPanel.setLayout(taskEditorPanelLayout);
        taskEditorPanelLayout.setHorizontalGroup(
            taskEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskEditorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(taskEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchEnginesLocationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(postProcessingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 799, Short.MAX_VALUE)
                    .addComponent(inputFilesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, taskEditorPanelLayout.createSequentialGroup()
                        .addComponent(aboutButton)
                        .addGap(50, 50, 50)
                        .addComponent(searchGUIPublicationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12))
                    .addComponent(preProcessingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        taskEditorPanelLayout.setVerticalGroup(
            taskEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskEditorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(inputFilesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(preProcessingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchEnginesLocationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(postProcessingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(taskEditorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(aboutButton)
                    .addComponent(searchGUIPublicationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        fileMenu.setText("File");

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");

        advancedSettingsMenuItem.setMnemonic('A');
        advancedSettingsMenuItem.setText("Advanced Settings");
        advancedSettingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advancedSettingsMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(advancedSettingsMenuItem);

        processingMenuItem.setText("Processing Settings");
        processingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processingMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(processingMenuItem);
        editMenu.add(jSeparator1);

        editModificationsEditMenuItem.setMnemonic('M');
        editModificationsEditMenuItem.setText("Modifications");
        editModificationsEditMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editModificationsEditMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(editModificationsEditMenuItem);

        editSearchEngineLocationsMenuItem.setMnemonic('E');
        editSearchEngineLocationsMenuItem.setText("Search Engines");
        editSearchEngineLocationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSearchEngineLocationsMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(editSearchEngineLocationsMenuItem);
        editMenu.add(jSeparator2);

        javaSettingsJMenuItem.setMnemonic('J');
        javaSettingsJMenuItem.setText("Java Settings");
        javaSettingsJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaSettingsJMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(javaSettingsJMenuItem);

        resourceSettingsMenuItem.setMnemonic('E');
        resourceSettingsMenuItem.setText("Resource Settings");
        resourceSettingsMenuItem.setToolTipText("Set paths to resource folders");
        resourceSettingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resourceSettingsMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(resourceSettingsMenuItem);

        privacyMenuItem.setMnemonic('P');
        privacyMenuItem.setText("Privacy Settings");
        privacyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privacyMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(privacyMenuItem);

        menuBar.add(editMenu);

        helpMenu.setText("Help");

        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        helpMenuItem.setMnemonic('H');
        helpMenuItem.setText("Help");
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuItem);
        helpMenu.add(jSeparator17);

        logReportMenu.setMnemonic('B');
        logReportMenu.setText("Bug Report");
        logReportMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logReportMenuActionPerformed(evt);
            }
        });
        helpMenu.add(logReportMenu);
        helpMenu.add(jSeparator16);

        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(taskEditorPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(taskEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Clear the list of spectra.
     *
     * @param evt the action event
     */
    private void clearSpectraButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSpectraButtonActionPerformed
        mgfFiles.clear();
        rawFiles.clear();

        enableMsConvertPanel();

        spectraFilesTxt.setText("");
        validateInput(false);
    }//GEN-LAST:event_clearSpectraButtonActionPerformed

    /**
     * Opens a file chooser for the user to add spectra.
     *
     * @param evt the action event
     */
    private void addSpectraButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSpectraButtonActionPerformed

        // First check whether a file has already been selected.
        // If so, start from that file's parent.
        File startLocation = new File(lastSelectedFolder.getLastSelectedFolder());
        if (mgfFiles.size() > 0) {
            File temp = mgfFiles.get(0);
            startLocation = temp.getParentFile();
        }

        JFileChooser fc = new JFileChooser(startLocation); // @TODO: implement a getUserSelectedFiles method in the Util class?
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File myFile) {
                String lowercaseName = myFile.getName().toLowerCase();
                for (MsFormat tempFormat : MsFormat.values()) {
                    if (lowercaseName.endsWith(tempFormat.fileNameEnding)) {
                        return true;
                    }
                }
                return myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                String description = "MS Files (";
                for (MsFormat tempFormat : MsFormat.values()) {
                    if (tempFormat.index > 0) {
                        description += ", ";
                    }
                    description += tempFormat.fileNameEnding;
                }
                description += ")";
                return description;
            }
        };
        fc.setFileFilter(filter);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        int result = fc.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            progressDialog = new ProgressDialogX(this,
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")),
                    true);
            progressDialog.setPrimaryProgressCounterIndeterminate(true);
            progressDialog.setTitle("Validating File(s). Please Wait...");

            final SearchGUI finalRef = this;
            final JFileChooser finalJFileChooser = fc;

            new Thread(new Runnable() {
                public void run() {
                    try {
                        progressDialog.setVisible(true);
                    } catch (IndexOutOfBoundsException e) {
                        // ignore
                    }
                }
            }, "ProgressDialog").start();

            new Thread("ValidateMgfThread") {
                @Override
                public void run() {

                    validSpectrumTitles = true;
                    ArrayList<File> tempMgfFiles = new ArrayList<File>();
                    ArrayList<File> tempRawFiles = new ArrayList<File>();

                    // get the mgf files
                    for (File newFile : finalJFileChooser.getSelectedFiles()) {
                        if (newFile.isDirectory()) {
                            File[] tempFiles = newFile.listFiles();
                            for (File file : tempFiles) {
                                String lowercaseName = file.getName().toLowerCase();
                                if (lowercaseName.endsWith(MsFormat.mgf.fileNameEnding)) {
                                    tempMgfFiles.add(file);
                                } else {
                                    for (MsFormat tempFormat : MsFormat.values()) {
                                        if (lowercaseName.endsWith(tempFormat.fileNameEnding)) {
                                            tempRawFiles.add(file);
                                        }
                                    }
                                }
                            }
                            lastSelectedFolder.setLastSelectedFolder(newFile.getAbsolutePath());
                        } else {
                            String lowercaseName = newFile.getName().toLowerCase();
                            if (lowercaseName.endsWith(MsFormat.mgf.fileNameEnding)) {
                                tempMgfFiles.add(newFile);
                            } else {
                                for (MsFormat tempFormat : MsFormat.values()) {
                                    if (lowercaseName.endsWith(tempFormat.fileNameEnding)) {
                                        tempRawFiles.add(newFile);
                                    }
                                }
                            }
                            lastSelectedFolder.setLastSelectedFolder(newFile.getParent());
                        }
                    }

                    rawFiles.addAll(tempRawFiles);

                    // iterate the mgf files and validate them
                    int fileCounter = 0;
                    for (File mgfFile : tempMgfFiles) {

                        progressDialog.setTitle("Validating Spectrum Files. Please Wait... (" + ++fileCounter + "/" + tempMgfFiles.size() + ")");
                        validSpectrumTitles = validateMgfFile(mgfFile, progressDialog);
                        if (validSpectrumTitles) {
                            mgfFiles.add(mgfFile);
                            lastSelectedFolder.setLastSelectedFolder(mgfFile.getAbsolutePath());
                        }

                        if (progressDialog.isRunCanceled()) {
                            mgfFiles.clear();
                            progressDialog.setRunFinished();
                            return;
                        }
                    }

                    if (!validSpectrumTitles) {
                        mgfFiles.clear();
                        progressDialog.setRunFinished();
                        return;
                    }

                    // check for duplicate mgf file names
                    if (!verifyMgfFilesNames()) {
                        mgfFiles.clear();
                        spectraFilesTxt.setText("");
                        validateInput(false);
                        progressDialog.setRunFinished();
                        return;
                    }

                    progressDialog.setRunFinished();

                    // check if we found any valid mgf files
                    if (mgfFiles.isEmpty() && rawFiles.isEmpty()) {
                        JOptionPane.showMessageDialog(finalRef, "The selection contained no valid spectrum files.", "No Spectrum Files", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    // check if proteowizard is installed in case raw files were selected
                    checkProteoWizard();

                    // verify the sizes of the mgf files
                    verifyMgfFilesSize();

                    int nFiles = mgfFiles.size() + rawFiles.size();
                    spectraFilesTxt.setText(nFiles + " file(s) selected");
                    msconvertCheckBox.setSelected(!rawFiles.isEmpty());

                    enableMsConvertPanel();

                    validateInput(false);
                }
            }.start();
        }
    }//GEN-LAST:event_addSpectraButtonActionPerformed

    /**
     * Opens a file chooser where the user can select the output folder.
     *
     * @param evt the action event
     */
    private void resultFolderBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resultFolderBrowseButtonActionPerformed

        // First check whether a file has already been selected.
        // If so, start from that file's parent.
        File startLocation = new File(lastSelectedFolder.getLastSelectedFolder());
        if (outputFolderTxt.getText() != null && new File(outputFolderTxt.getText()).exists()) {
            File temp = new File(outputFolderTxt.getText());
            if (temp.isDirectory()) {
                startLocation = temp;
            } else {
                startLocation = temp.getParentFile();
            }
        }

        JFileChooser fc = new JFileChooser(startLocation);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);

        int result = fc.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            File tempDir = fc.getSelectedFile();

            if (!tempDir.exists()) {
                int value = JOptionPane.showConfirmDialog(this, "The folder \'" + tempDir.getAbsolutePath() + "\' does not exist.\n"
                        + "Do you want to create it?", "Create Folder?", JOptionPane.YES_NO_OPTION);
                if (value == JOptionPane.NO_OPTION) {
                    return;
                } else { // yes option selected
                    boolean success = tempDir.mkdir();

                    if (!success) {
                        JOptionPane.showMessageDialog(this, "Failed to create the folder. Please create it manually and then select it.",
                                "File Error", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                }
            }

            outputFolder = fc.getSelectedFile();
            outputFolderTxt.setText(outputFolder.getAbsolutePath());
            if (outputFolderTxt.getText().length() > 70) {
                outputFolderTxt.setHorizontalAlignment(JTextField.LEADING);
            } else {
                outputFolderTxt.setHorizontalAlignment(JTextField.CENTER);
            }

            // set the peptideshaker output file
            searchHandler.setPeptideShakerFile(new File(outputFolder, "PeptideShaker_output.cpsx"));

            lastSelectedFolder.setLastSelectedFolder(outputFolder.getAbsolutePath());
            validateInput(false);
        }
    }//GEN-LAST:event_resultFolderBrowseButtonActionPerformed

    /**
     * Start the search.
     *
     * @param evt the action event
     */
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed

        if (!validateSearchEngines(true)) {
            return;
        }

        // validate the msconvert output format
        if (!rawFiles.isEmpty() && msConvertParameters.getMsFormat() != MsFormat.mgf) {
            JOptionPane.showMessageDialog(this,
                    "Mgf is the only spectrum format compatible with SearchGUI.\n\n"
                    + "Please change the output format for msconvert.",
                    "Output Format Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        Enzyme enzyme = searchParameters.getEnzyme();

        // check if the choosen enzyme is valid for ms-gf+
        if (enableMsgfJCheckBox.isSelected() && enzyme != null) {
            String msgfEnzyme = MsgfParameters.enzymeMapping(enzyme);
            if (msgfEnzyme == null) {
                JOptionPane.showMessageDialog(this,
                        "The selected enzyme is not supported for MS-GF+.\n\n"
                        + "Supported enzymes are: Trypsin, Chymotrypsin (FYWL),\n"
                        + "Lys-C, Lys-N (K), Glu-C (DE), Arg-C, Asp-N, Unspecific,\n"
                        + "Top-Down and Whole Protein.", "Unsupported Enzyme", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // check if the choosen enzyme is valid for ms amanda
        if (enableMsAmandaJCheckBox.isSelected() && enzyme != null) {

            String enzymeName = enzyme.getName();

            if (enzymeName.equalsIgnoreCase("Asp-N + Glu-C")) {
                JOptionPane.showMessageDialog(this,
                        "The selected enzyme is not yet supported for MS Amanda.\n\n"
                        + "Please choose a different enzyme or disable MS Amanda.",
                        "Unsupported Enzyme", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // check if the choosen enzyme is valid for myrimatch
        if (enableMyriMatchJCheckBox.isSelected() && enzyme != null) {
            if (MyriMatchParameters.enzymeMapping(enzyme) == null) { // enzymes not supported: Trypsin + CNBr, Asp-N + Glu-C, Lys-N (K), Thermolysin, no P rule
                JOptionPane.showMessageDialog(this,
                        "The selected enzyme is not yet supported for MyriMatch.\n\n"
                        + "Please choose a different enzyme or disable MyriMatch.",
                        "Unsupported Enzyme", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // check if the choosen enzyme is valid for tide
        if (enableTideJCheckBox.isSelected() && enzyme != null) {
            String enzymeName = enzyme.getName();

            if (enzymeName.equalsIgnoreCase("Asp-N + Glu-C")) {
                JOptionPane.showMessageDialog(this,
                        "The selected enzyme is not yet supported for Tide.\n\n"
                        + "Please choose a different enzyme or disable Tide.",
                        "Unsupported Enzyme", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // check if the file paths for xtandem are xml compatible
        if (enableXTandemJCheckBox.isSelected()) {
            for (File tempFile : mgfFiles) {
                if (tempFile.getAbsolutePath().contains("&")) {
                    JOptionPane.showMessageDialog(this,
                            "Spectrum files with \'&\' in the file path (" + tempFile.getAbsolutePath() + ")\n"
                            + "are not allowed in X!Tandem. Please rename of remove the file.", "Spectrum File Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            for (File tempFile : rawFiles) {
                if (tempFile.getAbsolutePath().contains("&")) {
                    JOptionPane.showMessageDialog(this,
                            "Spectrum files with \'&\' in the file path (" + tempFile.getAbsolutePath() + ")\n"
                            + "are not allowed in X!Tandem. Please rename of remove the file.", "Spectrum File Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            if (outputFolder.getAbsolutePath().contains("&")) {
                JOptionPane.showMessageDialog(this,
                        "Output folders with \'&\' in the file path (" + outputFolder.getAbsolutePath() + ")\n"
                        + "are not allowed in X!Tandem. Please rename of replace the folder.", "Output Folder Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (searchParameters.getFastaFile().getAbsolutePath().contains("&")) {
                JOptionPane.showMessageDialog(this,
                        "Database files with \'&\' in the file path (" + searchParameters.getFastaFile().getAbsolutePath() + ")\n"
                        + "are not allowed in X!Tandem. Please rename of replace the database.", "Database File Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // check if the fasta file name is not too long for ms amanda
        if (enableMsAmandaJCheckBox.isSelected()) {
            if (Util.removeExtension(searchParameters.getFastaFile().getName()).length() > MsAmandaParameters.MAX_MS_AMANDA_FASTA_FILE_NAME_LENGTH) {
                JOptionPane.showMessageDialog(this,
                        "Database files names longer than " + MsAmandaParameters.MAX_MS_AMANDA_FASTA_FILE_NAME_LENGTH + " characters are not allowed in MS Amanda.\n"
                        + "Please rename of replace the database.", "Database File Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // check if there are not too many PTMs for OMSSA
        if (enableOmssaJCheckBox.isSelected() && searchParameters.getPtmSettings().getAllModifications().size() > 30) {
            JOptionPane.showMessageDialog(this,
                    "OMSSA cannot be operated with >30 modifications.", "Unsupported parameters", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // check if the database can be parsed by Andromdeda
        if (enableAndromedaJCheckBox.isSelected()) {
            try {
                FastaIndex fastaIndex = SequenceFactory.getFastaIndex(searchParameters.getFastaFile(), false, null);
                AndromedaProcessBuilder.getDatabaseTypeAndromedaAccessionParsingRule(fastaIndex.getMainDatabaseType());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Database format not supported by Andromeda.", "Unsupported Fasta", JOptionPane.WARNING_MESSAGE); //@TODO: link to the help page? Make a more generic check for home made databases?
                return;
            }
        }

        // check output formats
        OmssaParameters omssaParameters = (OmssaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex());
        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex());
        TideParameters tideParameters = (TideParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.tide.getIndex());

        if (peptideShakerCheckBox.isSelected() && enableOmssaJCheckBox.isSelected() && !omssaParameters.getSelectedOutput().equals("OMX")) {
            JOptionPane.showMessageDialog(this, JOptionEditorPane.getJOptionEditorPane(
                    "The selected OMSSA output format is not compatible with <a href=\"http://compomics.github.io/projects/peptide-shaker.html\">PeptideShaker</a>. Please change to the<br>"
                    + "OMSSA OMX format in the Advanced Settings, or disable OMSSA or <a href=\"http://compomics.github.io/projects/peptide-shaker.html\">PeptideShaker</a>."),
                    "Format Warning", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (peptideShakerCheckBox.isSelected() && enableMyriMatchJCheckBox.isSelected() && myriMatchParameters.getOutputFormat().equals("pepXML")) {
            JOptionPane.showMessageDialog(this, JOptionEditorPane.getJOptionEditorPane(
                    "The selected MyriMatch output format is not compatible with <a href=\"http://compomics.github.io/projects/peptide-shaker.html\">PeptideShaker</a>. Please change to<br>"
                    + "mzIdentML in the Advanced Settings, or disable MyriMatch or <a href=\"http://compomics.github.io/projects/peptide-shaker.html\">PeptideShaker</a>."),
                    "Format Warning", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (peptideShakerCheckBox.isSelected() && enableTideJCheckBox.isSelected() && !tideParameters.getTextOutput()) {
            JOptionPane.showMessageDialog(this, JOptionEditorPane.getJOptionEditorPane(
                    "The selected Tide output format is not compatible with <a href=\"http://compomics.github.io/projects/peptide-shaker.html\">PeptideShaker</a>. Please change to<br>"
                    + "Tide text output in the Advanced Settings, or disable Tide or <a href=\"http://compomics.github.io/projects/peptide-shaker.html\">PeptideShaker</a>."),
                    "Format Warning", JOptionPane.WARNING_MESSAGE);
            return;
        } else {
            // check if the output files already exist
            boolean fileFound = false;

            ArrayList<File> spectrumFiles = new ArrayList<File>(mgfFiles);
            spectrumFiles.addAll(rawFiles);
            for (File spectrumFile : spectrumFiles) {

                String spectrumFileName = spectrumFile.getName();

                if (searchHandler.isOmssaEnabled()) {
                    File omssaOutputFile = new File(outputFolder, SearchHandler.getOMSSAFileName(spectrumFileName, omssaParameters));
                    if (omssaOutputFile.exists()) {
                        fileFound = true;
                        break;
                    }
                }

                if (searchHandler.isXtandemEnabled() && searchHandler.renameXTandemFile()
                        && !searchHandler.getXTandemFiles(outputFolder, spectrumFileName).isEmpty()) {
                    fileFound = true;
                    break;
                }

                if (searchHandler.isMsgfEnabled()) {
                    File msgfOutputFile = new File(outputFolder, SearchHandler.getMsgfFileName(spectrumFileName));
                    if (msgfOutputFile.exists()) {
                        fileFound = true;
                        break;
                    }
                }

                if (searchHandler.isMsAmandaEnabled()) {
                    File msAmandaOutputFile = new File(outputFolder, SearchHandler.getMsAmandaFileName(spectrumFileName));
                    if (msAmandaOutputFile.exists()) {
                        fileFound = true;
                        break;
                    }
                }

                if (searchHandler.isMyriMatchEnabled()) {
                    File myriMatchOutputFile = new File(outputFolder, SearchHandler.getMyriMatchFileName(spectrumFileName, myriMatchParameters));
                    if (myriMatchOutputFile.exists()) {
                        fileFound = true;
                        break;
                    }
                }

                if (searchHandler.isCometEnabled()) {
                    File cometOutputFile = new File(outputFolder, SearchHandler.getCometFileName(spectrumFileName));
                    if (cometOutputFile.exists()) {
                        fileFound = true;
                        break;
                    }
                }

                if (searchHandler.isTideEnabled()) {
                    File tideOutputFile = new File(outputFolder, SearchHandler.getTideFileName(spectrumFileName, tideParameters));
                    if (tideOutputFile.exists()) {
                        fileFound = true;
                        break;
                    }
                }

                if (searchHandler.isAndromedaEnabled()) {
                    File andromedaOutputFile = new File(outputFolder, SearchHandler.getAndromedaFileName(spectrumFileName));
                    if (andromedaOutputFile.exists()) {
                        fileFound = true;
                        break;
                    }
                }
            }

            OutputOption outputOption = searchHandler.getOutputOption();

            if (outputOption == OutputOption.grouped) {
                File outputFile = SearchHandler.getDefaultOutputFile(outputFolder, searchHandler.isIncludeDateInOutputName());
                if (outputFile.exists()) {
                    fileFound = true;
                }
            } else if (outputOption == OutputOption.algorithm) {

                if (searchHandler.isOmssaEnabled()) {
                    File outputFile = SearchHandler.getDefaultOutputFile(outputFolder, Advocate.omssa.getName(), searchHandler.isIncludeDateInOutputName());
                    if (outputFile.exists()) {
                        fileFound = true;
                    }
                }

                if (searchHandler.isXtandemEnabled()) {
                    File outputFile = SearchHandler.getDefaultOutputFile(outputFolder, Advocate.xtandem.getName(), searchHandler.isIncludeDateInOutputName());
                    if (outputFile.exists()) {
                        fileFound = true;
                    }
                }

                if (searchHandler.isMsgfEnabled()) {
                    File outputFile = SearchHandler.getDefaultOutputFile(outputFolder, Advocate.msgf.getName(), searchHandler.isIncludeDateInOutputName());
                    if (outputFile.exists()) {
                        fileFound = true;
                    }
                }

                if (searchHandler.isMsAmandaEnabled()) {
                    File outputFile = SearchHandler.getDefaultOutputFile(outputFolder, Advocate.msAmanda.getName(), searchHandler.isIncludeDateInOutputName());
                    if (outputFile.exists()) {
                        fileFound = true;
                    }
                }

                if (searchHandler.isMyriMatchEnabled()) {
                    File outputFile = SearchHandler.getDefaultOutputFile(outputFolder, Advocate.myriMatch.getName(), searchHandler.isIncludeDateInOutputName());
                    if (outputFile.exists()) {
                        fileFound = true;
                    }
                }
            } else if (outputOption == OutputOption.run) {
                for (File spectrumFile : spectrumFiles) {
                    String runName = Util.removeExtension(spectrumFile.getName());
                    File outputFile = SearchHandler.getDefaultOutputFile(outputFolder, runName, searchHandler.isIncludeDateInOutputName());
                    if (outputFile.exists()) {
                        fileFound = true;
                        break;
                    }
                }
            }

            if (fileFound) {
                int outcome = JOptionPane.showConfirmDialog(this,
                        "Existing output files found.\nOverwrite?", "Overwrite Files?",
                        JOptionPane.YES_NO_OPTION);
                if (outcome != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // check if the xtandem files can be renamed
            if (searchHandler.isXtandemEnabled() && searchHandler.renameXTandemFile()) {
                for (File spectrumFile : spectrumFiles) {
                    String spectrumFileName = spectrumFile.getName();

                    ArrayList<File> tempFiles = searchHandler.getXTandemFiles(outputFolder, spectrumFileName);

                    for (File tempSpectrumFile : tempFiles) {
                        if (!tempSpectrumFile.delete()) {
                            JOptionPane.showMessageDialog(this,
                                    new String[]{"Impossible to overwrite " + tempSpectrumFile.getName() + ". Please delete the file and retry."},
                                    "X!Tandem File", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                }
            }
        }

        saveConfigurationFile(); // save the search engine locations and ptms used
        searchHandler.setIdentificationParameters(identificationParameters);
        searchHandler.setIdentificationParametersFile(identificationParametersFile);
        searchHandler.setProcessingPreferences(processingPreferences);
        searchHandler.setMgfFiles(mgfFiles);
        searchHandler.setRawFiles(rawFiles);
        searchHandler.setResultsFolder(outputFolder);
        searchHandler.setPeptideShakerEnabled(peptideShakerCheckBox.isSelected());
        searchHandler.setMsConvertParameters(msConvertParameters); //@TODO: check that proteowizard in installed?
        startSearch();
    }//GEN-LAST:event_searchButtonActionPerformed

    /**
     * Changes the cursor into a hand cursor.
     *
     * @param evt the mouse event
     */
    private void aboutButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_aboutButtonMouseEntered

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void aboutButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutButtonMouseExited

    /**
     * Open the SearchGUI web page.
     *
     * @param evt the action event
     */
    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://compomics.github.io/projects/searchgui.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutButtonActionPerformed

    /**
     * Open the SettingsDialog.
     *
     * @param evt the action event
     */
    private void editSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSettingsButtonActionPerformed
        editIdentificationParameters();
    }//GEN-LAST:event_editSettingsButtonActionPerformed

    /**
     * Load search settings from a file.
     *
     * @param evt the action event
     */
    private void addSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSettingsButtonActionPerformed

        IdentificationParametersEditionDialog identificationParametersEditionDialog = new IdentificationParametersEditionDialog(
                this, null, searchHandler.getConfigurationFile(), Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")), lastSelectedFolder, null, true);

        if (!identificationParametersEditionDialog.isCanceled()) {
            IdentificationParameters tempIdentificationParameters = identificationParametersEditionDialog.getIdentificationParameters();
            identificationParametersFile = IdentificationParametersFactory.getIdentificationParametersFile(tempIdentificationParameters.getName());
            setIdentificationParameters(tempIdentificationParameters);
        }
    }//GEN-LAST:event_addSettingsButtonActionPerformed

    /**
     * Open the ModificationsDialog.
     *
     * @param evt the action event
     */
    private void editModificationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editModificationsMenuItemActionPerformed
        new ModificationsDialog(this, true);
    }//GEN-LAST:event_editModificationsMenuItemActionPerformed

    /**
     * Close the tool.
     *
     * @param evt the action event
     */
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        // @TODO: how to close???
        saveConfigurationFile();
        deleteTempFolders();
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /**
     * Open the advanced settings dialog.
     *
     * @param evt the action event
     */
    private void advancedSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advancedSettingsMenuItemActionPerformed
        new AdvancedSettingsDialog(this, true);
    }//GEN-LAST:event_advancedSettingsMenuItemActionPerformed

    /**
     * Open the help dialog.
     *
     * @param evt the action event
     */
    private void helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuItemActionPerformed
        new HelpDialog(this, getClass().getResource("/helpFiles/SearchGUI.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                "SearchGUI - Help", 500, 50);
    }//GEN-LAST:event_helpMenuItemActionPerformed

    /**
     * Open the about dialog.
     *
     * @param evt the action event
     */
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        new HelpDialog(this, getClass().getResource("/helpFiles/AboutSearchGUI.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                "About SearchGUI", 500, 50);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    /**
     * Open the BugReport dialog.
     *
     * @param evt the action event
     */
    private void logReportMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logReportMenuActionPerformed
        new BugReport(this, lastSelectedFolder, "SearchGUI", "searchgui",
                new eu.isas.searchgui.utilities.Properties().getVersion(),
                "peptide-shaker", "PeptideShaker",
                new File(getJarFilePath() + "/resources/SearchGUI.log"));
    }//GEN-LAST:event_logReportMenuActionPerformed

    /**
     * Open the Modifications dialog.
     *
     * @param evt the action event
     */
    private void editModificationsEditMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editModificationsEditMenuItemActionPerformed
        new ModificationsDialog(this, true);
    }//GEN-LAST:event_editModificationsEditMenuItemActionPerformed

    /**
     * Open the dialog for editing the search engines settings.
     *
     * @param evt the action event
     */
    private void editSearchEngineLocationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editSearchEngineLocationsMenuItemActionPerformed
        new SearchEnginesSettingsDialog(this, true);
    }//GEN-LAST:event_editSearchEngineLocationsMenuItemActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void omssaButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_omssaButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_omssaButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void omssaButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_omssaButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_omssaButtonMouseExited

    /**
     * Enable/disable OMSSA.
     *
     * @param evt the action event
     */
    private void omssaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_omssaButtonActionPerformed
        enableOmssaJCheckBox.setSelected(!enableOmssaJCheckBox.isSelected());
        enableOmssaJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_omssaButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void xtandemButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xtandemButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_xtandemButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void xtandemButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xtandemButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_xtandemButtonMouseExited

    /**
     * Enable/disable XTandem.
     *
     * @param evt the action event
     */
    private void xtandemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xtandemButtonActionPerformed
        enableXTandemJCheckBox.setSelected(!enableXTandemJCheckBox.isSelected());
        enableXTandemJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_xtandemButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void xtandemLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xtandemLinkLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_xtandemLinkLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void xtandemLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xtandemLinkLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_xtandemLinkLabelMouseExited

    /**
     * Open the XTandem web page.
     *
     * @param evt the mouse event
     */
    private void xtandemLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xtandemLinkLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.thegpm.org/tandem/");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_xtandemLinkLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void omssaLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_omssaLinkLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_omssaLinkLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void omssaLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_omssaLinkLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_omssaLinkLabelMouseExited

    /**
     * Open the OMSSA web page.
     *
     * @param evt the mouse event
     */
    private void omssaLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_omssaLinkLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.ncbi.nlm.nih.gov/pubmed/15473683");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_omssaLinkLabelMouseClicked

    /**
     * Set OMSSA enabled.
     *
     * @param evt the action event
     */
    private void enableOmssaJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableOmssaJCheckBoxActionPerformed
        searchHandler.setOmssaEnabled(enableOmssaJCheckBox.isSelected());
        if (enableOmssaJCheckBox.isSelected()) {
            boolean valid = validateSearchEngineInstallation(Advocate.omssa, searchHandler.getOmssaLocation(), true);
            if (!valid) {
                new SearchEnginesSettingsDialog(this, true);
            }
        }
        validateInput(false);
    }//GEN-LAST:event_enableOmssaJCheckBoxActionPerformed

    /**
     * Set X!Tandem enabled.
     *
     * @param evt the action event
     */
    private void enableXTandemJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableXTandemJCheckBoxActionPerformed
        searchHandler.setXtandemEnabled(enableXTandemJCheckBox.isSelected());
        if (enableXTandemJCheckBox.isSelected()) {
            boolean valid = validateSearchEngineInstallation(Advocate.xtandem, searchHandler.getXtandemLocation(), true);
            if (!valid) {
                new SearchEnginesSettingsDialog(this, true);
            }
        }
        validateInput(false);
    }//GEN-LAST:event_enableXTandemJCheckBoxActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void peptideShakerButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptideShakerButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_peptideShakerButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void peptideShakerButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptideShakerButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_peptideShakerButtonMouseExited

    /**
     * Enable/disable PeptideShaker.
     *
     * @param evt the action event
     */
    private void peptideShakerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_peptideShakerButtonActionPerformed
        peptideShakerCheckBox.setSelected(!peptideShakerCheckBox.isSelected());
        peptideShakerCheckBoxActionPerformed(null);
    }//GEN-LAST:event_peptideShakerButtonActionPerformed

    /**
     * Open the PeptideShaker web page.
     *
     * @param evt the mouse event
     */
    private void peptideShakerLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptideShakerLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://compomics.github.io/projects/peptide-shaker.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_peptideShakerLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void peptideShakerLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptideShakerLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_peptideShakerLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void peptideShakerLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptideShakerLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_peptideShakerLabelMouseExited

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void searchGUIPublicationLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchGUIPublicationLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_searchGUIPublicationLabelMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void searchGUIPublicationLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchGUIPublicationLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_searchGUIPublicationLabelMouseEntered

    /**
     * Open the SearchGUI publication.
     *
     * @param evt the mouse event
     */
    private void searchGUIPublicationLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchGUIPublicationLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.ncbi.nlm.nih.gov/pubmed/21337703");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_searchGUIPublicationLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void reporterButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reporterButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_reporterButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void reporterButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reporterButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_reporterButtonMouseExited

    /**
     * Open the Reporter web page.
     *
     * @param evt the action event
     */
    private void reporterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reporterButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://compomics.github.io/projects/reporter.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_reporterButtonActionPerformed

    /**
     * Open the Reporter web page.
     *
     * @param evt the mouse event
     */
    private void reporterLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reporterLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://compomics.github.io/projects/reporter.html");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_reporterLabelMouseClicked

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void reporterLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reporterLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_reporterLabelMouseEntered

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void reporterLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reporterLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_reporterLabelMouseExited

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void editReporterSettingsLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editReporterSettingsLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_editReporterSettingsLabelMouseEntered

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void editReporterSettingsLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editReporterSettingsLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_editReporterSettingsLabelMouseExited

    /**
     * Open the Reporter settings dialog.
     *
     * @param evt the mouse event
     */
    private void editReporterSettingsLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editReporterSettingsLabelMouseClicked
        JOptionPane.showMessageDialog(this, "Not yet implemented...", "Not Implemented", JOptionPane.WARNING_MESSAGE);
        // @TODO: implement me!!
    }//GEN-LAST:event_editReporterSettingsLabelMouseClicked

    /**
     * Open the PeptideShaker settings dialog.
     *
     * @param evt the action event
     */
    private void peptideShakerCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_peptideShakerCheckBoxActionPerformed
        openPeptideShakerSettings(false);
    }//GEN-LAST:event_peptideShakerCheckBoxActionPerformed

    /**
     * Open the Java settings dialog.
     *
     * @param evt the action event
     */
    private void javaSettingsJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaSettingsJMenuItemActionPerformed
        new JavaSettingsDialog(this, this, null, "SearchGUI", true);
    }//GEN-LAST:event_javaSettingsJMenuItemActionPerformed

    /**
     * Close the tool.
     *
     * @param evt the window event
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        exitMenuItemActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Display the list of selected spectrum files.
     *
     * @param evt the mouse event
     */
    private void spectraFilesTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spectraFilesTxtMouseClicked
        if (!mgfFiles.isEmpty() || !rawFiles.isEmpty()) {
            ArrayList<File> spectrumFiles = new ArrayList<File>(mgfFiles);
            spectrumFiles.addAll(rawFiles);
            FileDisplayDialog fileDisplayDialog = new FileDisplayDialog(this, spectrumFiles, true);
            if (!fileDisplayDialog.canceled()) {
                spectrumFiles = fileDisplayDialog.getSelectedFiles();
                spectraFilesTxt.setText(spectrumFiles.size() + " file(s) selected");
                mgfFiles.clear();
                rawFiles.clear();
                for (File file : spectrumFiles) {
                    if (file.getName().toLowerCase().endsWith("mgf")) {
                        mgfFiles.add(file);
                    } else {
                        rawFiles.add(file);
                    }
                }
                msconvertCheckBox.setSelected(!rawFiles.isEmpty());
                validateInput(false);
            }
        }
    }//GEN-LAST:event_spectraFilesTxtMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void omssaSettingsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_omssaSettingsButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_omssaSettingsButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void omssaSettingsButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_omssaSettingsButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_omssaSettingsButtonMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void xtandemSettingsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xtandemSettingsButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_xtandemSettingsButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void xtandemSettingsButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xtandemSettingsButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_xtandemSettingsButtonMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void peptideShakerSettingsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptideShakerSettingsButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_peptideShakerSettingsButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void peptideShakerSettingsButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peptideShakerSettingsButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_peptideShakerSettingsButtonMouseExited

    /**
     * Set MS-GF+ enabled.
     *
     * @param evt the action event
     */
    private void enableMsgfJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableMsgfJCheckBoxActionPerformed
        searchHandler.setMsgfEnabled(enableMsgfJCheckBox.isSelected());
        if (enableMsgfJCheckBox.isSelected()) {
            boolean valid = validateSearchEngineInstallation(Advocate.msgf, searchHandler.getMsgfLocation(), true);
            if (!valid) {
                new SearchEnginesSettingsDialog(this, true);
            }
        }
        validateInput(false);
    }//GEN-LAST:event_enableMsgfJCheckBoxActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void msgfButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msgfButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_msgfButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void msgfButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msgfButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_msgfButtonMouseExited

    /**
     * Enable/disable MS-GF+.
     *
     * @param evt the action event
     */
    private void msgfButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msgfButtonActionPerformed
        enableMsgfJCheckBox.setSelected(!enableMsgfJCheckBox.isSelected());
        enableMsgfJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_msgfButtonActionPerformed

    /**
     * Open the MS-GF+ web page.
     *
     * @param evt the mouse event
     */
    private void msgfLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msgfLinkLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://bix-lab.ucsd.edu/pages/viewpage.action?pageId=13533355");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_msgfLinkLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void msgfLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msgfLinkLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_msgfLinkLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void msgfLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msgfLinkLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_msgfLinkLabelMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void msgfSettingsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msgfSettingsButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_msgfSettingsButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void msgfSettingsButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msgfSettingsButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_msgfSettingsButtonMouseExited

    /**
     * Set MS Amanda enabled.
     *
     * @param evt the action event
     */
    private void enableMsAmandaJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableMsAmandaJCheckBoxActionPerformed
        searchHandler.setMsAmandaEnabled(enableMsAmandaJCheckBox.isSelected());
        if (enableMsAmandaJCheckBox.isSelected()) {
            boolean valid = validateSearchEngineInstallation(Advocate.msAmanda, searchHandler.getMsAmandaLocation(), true);
            if (!valid) {
                new SearchEnginesSettingsDialog(this, true);
            }
        }
        validateInput(false);
    }//GEN-LAST:event_enableMsAmandaJCheckBoxActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void msAmandaButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msAmandaButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_msAmandaButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void msAmandaButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msAmandaButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_msAmandaButtonMouseExited

    /**
     * Enable/disable OMSSA.
     *
     * @param evt the action event
     */
    private void msAmandaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msAmandaButtonActionPerformed
        enableMsAmandaJCheckBox.setSelected(!enableMsAmandaJCheckBox.isSelected());
        enableMsAmandaJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_msAmandaButtonActionPerformed

    /**
     * Open the MS Amanda web page.
     *
     * @param evt the mouse event
     */
    private void msAmandaLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msAmandaLinkLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://ms.imp.ac.at/?goto=msamanda");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_msAmandaLinkLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void msAmandaLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msAmandaLinkLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_msAmandaLinkLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void msAmandaLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msAmandaLinkLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_msAmandaLinkLabelMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void msAmandaSettingsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msAmandaSettingsButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_msAmandaSettingsButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void msAmandaSettingsButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msAmandaSettingsButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_msAmandaSettingsButtonMouseExited

    /**
     * Open the PrivacySettingsDialog.
     *
     * @param evt the action event
     */
    private void privacyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privacyMenuItemActionPerformed
        new PrivacySettingsDialog(this, Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/reporter.gif")));
    }//GEN-LAST:event_privacyMenuItemActionPerformed

    /**
     * Set MyriMatch enabled.
     *
     * @param evt the action event
     */
    private void enableMyriMatchJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableMyriMatchJCheckBoxActionPerformed
        searchHandler.setMyriMatchEnabled(enableMyriMatchJCheckBox.isSelected());
        if (enableMyriMatchJCheckBox.isSelected()) {
            boolean valid = validateSearchEngineInstallation(Advocate.myriMatch, searchHandler.getMyriMatchLocation(), true);
            if (!valid) {
                new SearchEnginesSettingsDialog(this, true);
            }
        }
        validateInput(false);
    }//GEN-LAST:event_enableMyriMatchJCheckBoxActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void myriMatchButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_myriMatchButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_myriMatchButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void myriMatchButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_myriMatchButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_myriMatchButtonMouseExited

    /**
     * Enable/disable MyriMatch.
     *
     * @param evt the action event
     */
    private void myriMatchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myriMatchButtonActionPerformed
        enableMyriMatchJCheckBox.setSelected(!enableMyriMatchJCheckBox.isSelected());
        enableMyriMatchJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_myriMatchButtonActionPerformed

    /**
     * Open the MyriMatch web page.
     *
     * @param evt the mouse event
     */
    private void myriMatchLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_myriMatchLinkLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://forge.fenchurch.mc.vanderbilt.edu/scm/viewvc.php/*checkout*/trunk/doc/index.html?root=myrimatch");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_myriMatchLinkLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void myriMatchLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_myriMatchLinkLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_myriMatchLinkLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void myriMatchLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_myriMatchLinkLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_myriMatchLinkLabelMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void myriMatchSettingsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_myriMatchSettingsButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_myriMatchSettingsButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void myriMatchSettingsButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_myriMatchSettingsButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_myriMatchSettingsButtonMouseExited

    /**
     * Set Comet enabled.
     *
     * @param evt the action event
     */
    private void enableCometJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableCometJCheckBoxActionPerformed
        searchHandler.setCometEnabled(enableCometJCheckBox.isSelected());
        if (enableCometJCheckBox.isSelected()) {
            boolean valid = validateSearchEngineInstallation(Advocate.comet, searchHandler.getCometLocation(), true);
            if (!valid) {
                new SearchEnginesSettingsDialog(this, true);
            }
        }
        validateInput(false);
    }//GEN-LAST:event_enableCometJCheckBoxActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void cometButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cometButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_cometButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void cometButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cometButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_cometButtonMouseExited

    /**
     * Enable/disable Comet.
     *
     * @param evt the action event
     */
    private void cometButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cometButtonActionPerformed
        enableCometJCheckBox.setSelected(!enableCometJCheckBox.isSelected());
        enableCometJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_cometButtonActionPerformed

    /**
     * Open the Comet web page.
     *
     * @param evt the mouse event
     */
    private void cometLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cometLinkLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://comet-ms.sourceforge.net");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_cometLinkLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void cometLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cometLinkLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_cometLinkLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void cometLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cometLinkLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_cometLinkLabelMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void cometSettingsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cometSettingsButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_cometSettingsButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void cometSettingsButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cometSettingsButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_cometSettingsButtonMouseExited

    /**
     * Open the Edit Paths dialog.
     *
     * @param evt
     */
    private void resourceSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resourceSettingsMenuItemActionPerformed
        editPathSettings();
    }//GEN-LAST:event_resourceSettingsMenuItemActionPerformed

    /**
     * Set Tide enabled.
     *
     * @param evt the action event
     */
    private void enableTideJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableTideJCheckBoxActionPerformed
        searchHandler.setTideEnabled(enableTideJCheckBox.isSelected());
        if (enableTideJCheckBox.isSelected()) {
            boolean valid = validateSearchEngineInstallation(Advocate.tide, searchHandler.getTideLocation(), true);
            if (!valid) {
                new SearchEnginesSettingsDialog(this, true);
            }
        }
        validateInput(false);
    }//GEN-LAST:event_enableTideJCheckBoxActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void tideButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tideButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_tideButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void tideButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tideButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_tideButtonMouseExited

    /**
     * Enable/disable Tide.
     *
     * @param evt the action event
     */
    private void tideButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tideButtonActionPerformed
        enableTideJCheckBox.setSelected(!enableTideJCheckBox.isSelected());
        enableTideJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_tideButtonActionPerformed

    /**
     * Open the Tide web page.
     *
     * @param evt the mouse event
     */
    private void tideLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tideLinkLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://cruxtoolkit.sourceforge.net/");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_tideLinkLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void tideLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tideLinkLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_tideLinkLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void tideLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tideLinkLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_tideLinkLabelMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void tideSettingsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tideSettingsButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_tideSettingsButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void tideSettingsButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tideSettingsButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_tideSettingsButtonMouseExited

    /**
     * Set Andromeda enabled.
     *
     * @param evt the action event
     */
    private void enableAndromedaJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAndromedaJCheckBoxActionPerformed
        searchHandler.setAndromedaEnabled(enableAndromedaJCheckBox.isSelected());
        if (enableAndromedaJCheckBox.isSelected()) {
            boolean valid = validateSearchEngineInstallation(Advocate.andromeda, searchHandler.getAndromedaLocation(), true);
            if (!valid) {
                new SearchEnginesSettingsDialog(this, true);
            }
        }
        validateInput(false);
    }//GEN-LAST:event_enableAndromedaJCheckBoxActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void andromedaButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_andromedaButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_andromedaButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void andromedaButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_andromedaButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_andromedaButtonMouseExited

    /**
     * Enable/disable Andromeda.
     *
     * @param evt the action event
     */
    private void andromedaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_andromedaButtonActionPerformed
        enableAndromedaJCheckBox.setSelected(!enableAndromedaJCheckBox.isSelected());
        enableAndromedaJCheckBoxActionPerformed(null);
    }//GEN-LAST:event_andromedaButtonActionPerformed

    /**
     * Open the Andromeda web page.
     *
     * @param evt the mouse event
     */
    private void andromedaLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_andromedaLinkLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://www.andromeda-search.org");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_andromedaLinkLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void andromedaLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_andromedaLinkLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_andromedaLinkLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void andromedaLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_andromedaLinkLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_andromedaLinkLabelMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void andromedaSettingsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_andromedaSettingsButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_andromedaSettingsButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void andromedaSettingsButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_andromedaSettingsButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_andromedaSettingsButtonMouseExited

    /**
     * Open the ProteoWizard web page.
     *
     * @param evt
     */
    private void msconvertLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msconvertLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://proteowizard.sourceforge.net");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_msconvertLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void msconvertLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msconvertLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_msconvertLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void msconvertLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msconvertLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_msconvertLabelMouseExited

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt the mouse event
     */
    private void msconvertSettingsButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msconvertSettingsButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_msconvertSettingsButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt the mouse event
     */
    private void msconvertSettingsButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_msconvertSettingsButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_msconvertSettingsButtonMouseExited

    /**
     * Set MSConvert enabled.
     *
     * @param evt the action event
     */
    private void msconvertCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msconvertCheckBoxActionPerformed
        msconvertCheckBox.setSelected(!rawFiles.isEmpty());
    }//GEN-LAST:event_msconvertCheckBoxActionPerformed

    private void processingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processingMenuItemActionPerformed
        ProcessingPreferencesDialog processingPreferencesDialog = new ProcessingPreferencesDialog(this, processingPreferences, true);
        if (!processingPreferencesDialog.isCanceled()) {
            processingPreferences = processingPreferencesDialog.getProcessingPreferences();
        }
    }//GEN-LAST:event_processingMenuItemActionPerformed

    /**
     * Enable/disable the Edit button for the settings.
     *
     * @param evt
     */
    private void settingsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsComboBoxActionPerformed
        editSettingsButton.setEnabled(settingsComboBox.getSelectedIndex() != 0);

        if (settingsComboBox.getSelectedIndex() != 0) {
            identificationParametersFile = IdentificationParametersFactory.getIdentificationParametersFile((String) settingsComboBox.getSelectedItem());
            try {
                identificationParameters = IdentificationParameters.getIdentificationParameters(identificationParametersFile);

                enableSearchEnginePanel(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to import search parameters from: " + identificationParametersFile.getAbsolutePath() + ".", "Search Parameters",
                        JOptionPane.WARNING_MESSAGE);
                e.printStackTrace();
            }
        } else {
            enableSearchEnginePanel(false);
        }
    }//GEN-LAST:event_settingsComboBoxActionPerformed

    /**
     * Open the MS Convert settings dialog.
     *
     * @param evt
     */
    private void msconvertSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msconvertSettingsButtonActionPerformed
        boolean canceled = false;
        if (utilitiesUserPreferences.getProteoWizardPath() == null) {
            canceled = !editProteoWizardInstallation();
        }
        if (!canceled) {
            MsConvertParametersDialog msConvertParametersDialog = new MsConvertParametersDialog(this, msConvertParameters);
            if (!msConvertParametersDialog.isCanceled()) {
                msConvertParameters = msConvertParametersDialog.getMsConvertParameters();
            }
        }
    }//GEN-LAST:event_msconvertSettingsButtonActionPerformed

    /**
     * Edit the X!Tandem settings.
     *
     * @param evt the mouse event
     */
    private void xtandemSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xtandemSettingsButtonActionPerformed
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        XtandemParameters oldXtandemParameters = (XtandemParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.xtandem.getIndex());
        XTandemSettingsDialog xtandemSettingsDialog = new XTandemSettingsDialog(this, oldXtandemParameters, searchParameters.getPtmSettings(), searchParameters.getFragmentIonAccuracy(), true);

        boolean xtandemParametersSet = false;

        while (!xtandemParametersSet) {

            if (!xtandemSettingsDialog.isCancelled()) {
                XtandemParameters newXtandemParameters = xtandemSettingsDialog.getInput();

                // see if there are changes to the parameters and ask the user if these are to be saved
                if (!oldXtandemParameters.equals(newXtandemParameters) || xtandemSettingsDialog.modProfileEdited()) {
                    SearchParameters newSearchParameters = new SearchParameters(searchParameters);
                    newSearchParameters.setIdentificationAlgorithmParameter(Advocate.xtandem.getIndex(), newXtandemParameters);
                    newSearchParameters.setPtmSettings(xtandemSettingsDialog.getModificationProfile());
                    File newSearchParametersFile = SearchSettingsDialog.saveSearchParameters(xtandemSettingsDialog, newSearchParameters, identificationParametersFile, lastSelectedFolder);
                    if (newSearchParametersFile != null) {
                        identificationParameters.setSearchParameters(newSearchParameters);
                        identificationParametersFile = newSearchParametersFile;
                        //searchSettingsTxt.setText(newSearchParametersFile.getName()); // @TODO: ???
                        xtandemParametersSet = true;
                    } else {
                        xtandemSettingsDialog = new XTandemSettingsDialog(this, newXtandemParameters, newSearchParameters.getPtmSettings(), newSearchParameters.getFragmentIonAccuracy(), true);
                    }
                } else {
                    xtandemParametersSet = true;
                }
            } else {
                xtandemParametersSet = true;
            }
        }
    }//GEN-LAST:event_xtandemSettingsButtonActionPerformed

    /**
     * Edit the MyriMatch advanced settings.
     *
     * @param evt the mouse event
     */
    private void myriMatchSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myriMatchSettingsButtonActionPerformed
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        MyriMatchParameters oldMyriMatchParameters = (MyriMatchParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex());
        MyriMatchSettingsDialog myriMatchParametersDialog = new MyriMatchSettingsDialog(this, oldMyriMatchParameters, true);

        boolean myriMatchParametersSet = false;

        while (!myriMatchParametersSet) {

            if (!myriMatchParametersDialog.isCancelled()) {
                MyriMatchParameters newMyriMatchParameters = myriMatchParametersDialog.getInput();

                // see if there are changes to the parameters and ask the user if these are to be saved
                if (!oldMyriMatchParameters.equals(newMyriMatchParameters)) {
                    SearchParameters newSearchParameters = new SearchParameters(searchParameters);
                    newSearchParameters.setIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex(), newMyriMatchParameters);
                    File newSearchParametersFile = SearchSettingsDialog.saveSearchParameters(myriMatchParametersDialog, searchParameters, identificationParametersFile, lastSelectedFolder);
                    if (newSearchParametersFile != null) {
                        identificationParameters.setSearchParameters(newSearchParameters);
                        identificationParametersFile = newSearchParametersFile;
                        //searchSettingsTxt.setText(newSearchParametersFile.getName()); // @TODO: ???
                        myriMatchParametersSet = true;
                    } else {
                        myriMatchParametersDialog = new MyriMatchSettingsDialog(this, newMyriMatchParameters, true);
                    }
                } else {
                    myriMatchParametersSet = true;
                }
            } else {
                myriMatchParametersSet = true;
            }
        }
    }//GEN-LAST:event_myriMatchSettingsButtonActionPerformed

    /**
     * Edit the MS Amanda advanced settings.
     *
     * @param evt the mouse event
     */
    private void msAmandaSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msAmandaSettingsButtonActionPerformed
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        MsAmandaParameters oldMsAmandaParameters = (MsAmandaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex());
        MsAmandaSettingsDialog msAmandaParametersDialog = new MsAmandaSettingsDialog(this, oldMsAmandaParameters, true);

        boolean msAmandaParametersSet = false;

        while (!msAmandaParametersSet) {

            if (!msAmandaParametersDialog.isCancelled()) {
                MsAmandaParameters newMsAmandaParameters = msAmandaParametersDialog.getInput();

                // see if there are changes to the parameters and ask the user if these are to be saved
                if (!oldMsAmandaParameters.equals(newMsAmandaParameters)) {
                    SearchParameters newSearchParameters = new SearchParameters(searchParameters);
                    newSearchParameters.setIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex(), newMsAmandaParameters);
                    File newSearchParametersFile = SearchSettingsDialog.saveSearchParameters(msAmandaParametersDialog, searchParameters, identificationParametersFile, lastSelectedFolder);
                    if (newSearchParametersFile != null) {
                        identificationParameters.setSearchParameters(newSearchParameters);
                        identificationParametersFile = newSearchParametersFile;
                        //searchSettingsTxt.setText(newSearchParametersFile.getName()); // @TODO: ???
                        msAmandaParametersSet = true;
                    } else {
                        msAmandaParametersDialog = new MsAmandaSettingsDialog(this, newMsAmandaParameters, true);
                    }
                } else {
                    msAmandaParametersSet = true;
                }
            } else {
                msAmandaParametersSet = true;
            }
        }
    }//GEN-LAST:event_msAmandaSettingsButtonActionPerformed

    /**
     * Edit the MS-GF+ settings.
     *
     * @param evt the mouse event
     */
    private void msgfSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msgfSettingsButtonActionPerformed
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        MsgfParameters oldMsgfParameters = (MsgfParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.msgf.getIndex());
        if (oldMsgfParameters == null) { //backward compatibility check
            oldMsgfParameters = new MsgfParameters();
        }
        MsgfSettingsDialog msgfParametersDialog = new MsgfSettingsDialog(this, oldMsgfParameters, true);

        boolean msgfParametersSet = false;

        while (!msgfParametersSet) {

            if (!msgfParametersDialog.isCancelled()) {
                MsgfParameters newMsgfParameters = msgfParametersDialog.getInput();

                // see if there are changes to the parameters and ask the user if these are to be saved
                if (!oldMsgfParameters.equals(newMsgfParameters)) {
                    SearchParameters newSearchParameters = new SearchParameters(searchParameters);
                    newSearchParameters.setIdentificationAlgorithmParameter(Advocate.msgf.getIndex(), newMsgfParameters);
                    File newSearchParametersFile = SearchSettingsDialog.saveSearchParameters(msgfParametersDialog, newSearchParameters, identificationParametersFile, lastSelectedFolder);
                    if (newSearchParametersFile != null) {
                        identificationParameters.setSearchParameters(newSearchParameters);
                        identificationParametersFile = newSearchParametersFile;
                        //searchSettingsTxt.setText(newSearchParametersFile.getName()); // @TODO: ???
                        msgfParametersSet = true;
                    } else {
                        msgfParametersDialog = new MsgfSettingsDialog(this, newMsgfParameters, true);
                    }
                } else {
                    msgfParametersSet = true;
                }
            } else {
                msgfParametersSet = true;
            }
        }
    }//GEN-LAST:event_msgfSettingsButtonActionPerformed

    /**
     * Edit the OMSSA settings.
     *
     * @param evt the mouse event
     */
    private void omssaSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_omssaSettingsButtonActionPerformed
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        OmssaParameters oldOmssaParameters = (OmssaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex());
        if (oldOmssaParameters == null) { //backward compatibility check
            oldOmssaParameters = new OmssaParameters();
        }
        OmssaSettingsDialog omssaParametersDialog = new OmssaSettingsDialog(this, oldOmssaParameters, true);

        boolean omssaParametersSet = false;

        while (!omssaParametersSet) {

            if (!omssaParametersDialog.isCancelled()) {
                OmssaParameters newOmssaParameters = omssaParametersDialog.getInput();

                // see if there are changes to the parameters and ask the user if these are to be saved
                if (!oldOmssaParameters.equals(newOmssaParameters)) {
                    SearchParameters newSearchParameters = new SearchParameters(searchParameters);
                    newSearchParameters.setIdentificationAlgorithmParameter(Advocate.omssa.getIndex(), newOmssaParameters);
                    File newSearchParametersFile = SearchSettingsDialog.saveSearchParameters(omssaParametersDialog, newSearchParameters, identificationParametersFile, lastSelectedFolder);
                    if (newSearchParametersFile != null) {
                        identificationParameters.setSearchParameters(newSearchParameters);
                        identificationParametersFile = newSearchParametersFile;
                        //searchSettingsTxt.setText(newSearchParametersFile.getName()); // @TODO: ???
                        omssaParametersSet = true;
                    } else {
                        omssaParametersDialog = new OmssaSettingsDialog(this, newOmssaParameters, true);
                    }
                } else {
                    omssaParametersSet = true;
                }
            } else {
                omssaParametersSet = true;
            }
        }
    }//GEN-LAST:event_omssaSettingsButtonActionPerformed

    /**
     * Edit the Comet settings.
     *
     * @param evt the mouse event
     */
    private void cometSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cometSettingsButtonActionPerformed
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        CometParameters oldCometParameters = (CometParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.comet.getIndex());
        CometSettingsDialog cometSettingsDialog = new CometSettingsDialog(this, oldCometParameters, true);

        boolean cometParametersSet = false;

        while (!cometParametersSet) {

            if (!cometSettingsDialog.isCancelled()) {
                CometParameters newCometParameters = cometSettingsDialog.getInput();

                // see if there are changes to the parameters and ask the user if these are to be saved
                if (!oldCometParameters.equals(newCometParameters)) {
                    SearchParameters newSearchParameters = new SearchParameters(searchParameters);
                    newSearchParameters.setIdentificationAlgorithmParameter(Advocate.comet.getIndex(), newCometParameters);
                    File newSearchParametersFile = SearchSettingsDialog.saveSearchParameters(cometSettingsDialog, searchParameters, identificationParametersFile, lastSelectedFolder);
                    if (newSearchParametersFile != null) {
                        identificationParameters.setSearchParameters(newSearchParameters);
                        identificationParametersFile = newSearchParametersFile;
                        //searchSettingsTxt.setText(newSearchParametersFile.getName()); // @TODO: ???
                        cometParametersSet = true;
                    } else {
                        cometSettingsDialog = new CometSettingsDialog(this, newCometParameters, true);
                    }
                } else {
                    cometParametersSet = true;
                }
            } else {
                cometParametersSet = true;
            }
        }
    }//GEN-LAST:event_cometSettingsButtonActionPerformed

    /**
     * Edit the Tide settings.
     *
     * @param evt the mouse event
     */
    private void tideSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tideSettingsButtonActionPerformed
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        TideParameters oldTideParameters = (TideParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.tide.getIndex());
        if (oldTideParameters == null) { //backward compatibility check
            oldTideParameters = new TideParameters();
        }
        TideSettingsDialog tideParametersDialog = new TideSettingsDialog(this, oldTideParameters, true);

        boolean tideParametersSet = false;

        while (!tideParametersSet) {

            if (!tideParametersDialog.isCancelled()) {
                TideParameters newTideParameters = tideParametersDialog.getInput();

                // see if there are changes to the parameters and ask the user if these are to be saved
                if (!oldTideParameters.equals(newTideParameters)) {
                    SearchParameters newSearchParameters = new SearchParameters(searchParameters);
                    newSearchParameters.setIdentificationAlgorithmParameter(Advocate.tide.getIndex(), newTideParameters);
                    File newSearchParametersFile = SearchSettingsDialog.saveSearchParameters(tideParametersDialog, searchParameters, identificationParametersFile, lastSelectedFolder);
                    if (newSearchParametersFile != null) {
                        identificationParameters.setSearchParameters(newSearchParameters);
                        identificationParametersFile = newSearchParametersFile;
                        //searchSettingsTxt.setText(newSearchParametersFile.getName()); // @TODO: ???
                        tideParametersSet = true;
                    } else {
                        tideParametersDialog = new TideSettingsDialog(this, newTideParameters, true);
                    }
                } else {
                    tideParametersSet = true;
                }
            } else {
                tideParametersSet = true;
            }
        }
    }//GEN-LAST:event_tideSettingsButtonActionPerformed

    /**
     * Edit the Andromeda settings.
     *
     * @param evt the mouse event
     */
    private void andromedaSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_andromedaSettingsButtonActionPerformed
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        AndromedaParameters oldAndromedaParameters = (AndromedaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex());
        if (oldAndromedaParameters == null) { //backward compatibility check
            oldAndromedaParameters = new AndromedaParameters();
        }
        AndromedaSettingsDialog andromedaParametersDialog = new AndromedaSettingsDialog(this, oldAndromedaParameters, true);

        boolean andromedaParametersSet = false;

        while (!andromedaParametersSet) {

            if (!andromedaParametersDialog.isCancelled()) {
                AndromedaParameters newAndromedaParameters = andromedaParametersDialog.getInput();

                // see if there are changes to the parameters and ask the user if these are to be saved
                if (!oldAndromedaParameters.equals(newAndromedaParameters)) {
                    SearchParameters newSearchParameters = new SearchParameters(searchParameters);
                    newSearchParameters.setIdentificationAlgorithmParameter(Advocate.andromeda.getIndex(), newAndromedaParameters);
                    File newSearchParametersFile = SearchSettingsDialog.saveSearchParameters(andromedaParametersDialog, searchParameters, identificationParametersFile, lastSelectedFolder);
                    if (newSearchParametersFile != null) {
                        identificationParameters.setSearchParameters(newSearchParameters);
                        identificationParametersFile = newSearchParametersFile;
                        //searchSettingsTxt.setText(newSearchParametersFile.getName()); // @TODO: ???
                        andromedaParametersSet = true;
                    } else {
                        andromedaParametersDialog = new AndromedaSettingsDialog(this, newAndromedaParameters, true);
                    }
                } else {
                    andromedaParametersSet = true;
                }
            } else {
                andromedaParametersSet = true;
            }
        }
    }//GEN-LAST:event_andromedaSettingsButtonActionPerformed

    /**
     * Edit the PeptideShaker settings.
     *
     * @param evt the mouse event
     */
    private void peptideShakerSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_peptideShakerSettingsButtonActionPerformed
        openPeptideShakerSettings(true);
    }//GEN-LAST:event_peptideShakerSettingsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutButton;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton addSettingsButton;
    private javax.swing.JButton addSpectraButton;
    private javax.swing.JMenuItem advancedSettingsMenuItem;
    private javax.swing.JButton andromedaButton;
    private javax.swing.JLabel andromedaLinkLabel;
    private javax.swing.JButton andromedaSettingsButton;
    private javax.swing.JButton andromedaSupportButton;
    private javax.swing.JButton clearSpectraButton;
    private javax.swing.JButton cometButton;
    private javax.swing.JLabel cometLinkLabel;
    private javax.swing.JButton cometSettingsButton;
    private javax.swing.JButton cometSupportButton;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editModificationsEditMenuItem;
    private javax.swing.JMenuItem editModificationsMenuItem;
    private javax.swing.JLabel editReporterSettingsLabel;
    private javax.swing.JMenuItem editSearchEngineLocationsMenuItem;
    private javax.swing.JButton editSettingsButton;
    private javax.swing.JCheckBox enableAndromedaJCheckBox;
    private javax.swing.JCheckBox enableCometJCheckBox;
    private javax.swing.JCheckBox enableMsAmandaJCheckBox;
    private javax.swing.JCheckBox enableMsgfJCheckBox;
    private javax.swing.JCheckBox enableMyriMatchJCheckBox;
    private javax.swing.JCheckBox enableOmssaJCheckBox;
    private javax.swing.JCheckBox enableTideJCheckBox;
    private javax.swing.JCheckBox enableXTandemJCheckBox;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JPanel inputFilesPanel;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JPopupMenu.Separator jSeparator17;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JMenuItem javaSettingsJMenuItem;
    private javax.swing.JMenuItem logReportMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPopupMenu modificationOptionsPopupMenu;
    private javax.swing.JButton msAmandaButton;
    private javax.swing.JLabel msAmandaLinkLabel;
    private javax.swing.JButton msAmandaSettingsButton;
    private javax.swing.JButton msAmandaSupportButton;
    private javax.swing.JButton msconvertButton;
    private javax.swing.JCheckBox msconvertCheckBox;
    private javax.swing.JLabel msconvertLabel;
    private javax.swing.JButton msconvertSettingsButton;
    private javax.swing.JButton msconvertSupportButton;
    private javax.swing.JButton msgfButton;
    private javax.swing.JLabel msgfLinkLabel;
    private javax.swing.JButton msgfSettingsButton;
    private javax.swing.JButton msgfSupportButton;
    private javax.swing.JButton myriMatchButton;
    private javax.swing.JLabel myriMatchLinkLabel;
    private javax.swing.JButton myriMatchSettingsButton;
    private javax.swing.JButton myrimatchSupportButton;
    private javax.swing.JButton omssaButton;
    private javax.swing.JLabel omssaLinkLabel;
    private javax.swing.JButton omssaSettingsButton;
    private javax.swing.JButton omssaSupportButton;
    private javax.swing.JTextField outputFolderTxt;
    private javax.swing.JButton peptideShakerButton;
    private javax.swing.JCheckBox peptideShakerCheckBox;
    private javax.swing.JLabel peptideShakerLabel;
    private javax.swing.JButton peptideShakerSettingsButton;
    private javax.swing.JButton peptideShakerSupportButton;
    private javax.swing.JPanel postProcessingPanel;
    private javax.swing.JPanel preProcessingPanel;
    private javax.swing.JMenuItem privacyMenuItem;
    private javax.swing.JMenuItem processingMenuItem;
    private javax.swing.JButton reporterButton;
    private javax.swing.JCheckBox reporterCheckBox;
    private javax.swing.JLabel reporterLabel;
    private javax.swing.JPanel reporterPostProcessPanel;
    private javax.swing.JMenuItem resourceSettingsMenuItem;
    private javax.swing.JButton resultFolderBrowseButton;
    private javax.swing.JLabel resultFolderLbl;
    private javax.swing.JButton searchButton;
    private javax.swing.JPanel searchEnginesLocationPanel;
    private javax.swing.JPanel searchEnginesPanel;
    private javax.swing.JScrollPane searchEnginesScrollPane;
    private javax.swing.JLabel searchGUIPublicationLabel;
    private javax.swing.JLabel searchSettingsLbl;
    private javax.swing.JComboBox settingsComboBox;
    private javax.swing.JLabel spectraFilesLabel;
    private javax.swing.JTextField spectraFilesTxt;
    private javax.swing.JPanel taskEditorPanel;
    private javax.swing.JButton tideButton;
    private javax.swing.JLabel tideLinkLabel;
    private javax.swing.JButton tideSettingsButton;
    private javax.swing.JButton tideSupportButton;
    private javax.swing.JButton xtandemButton;
    private javax.swing.JLabel xtandemLinkLabel;
    private javax.swing.JButton xtandemSettingsButton;
    private javax.swing.JButton xtandemSupportButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Edits the identification parameters.
     */
    private void editIdentificationParameters() {

        IdentificationParametersEditionDialog identificationParametersEditionDialog = new IdentificationParametersEditionDialog(
                this, identificationParameters, searchHandler.getConfigurationFile(), Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")), lastSelectedFolder, null, true);

        if (!identificationParametersEditionDialog.isCanceled()) {
            IdentificationParameters tempIdentificationParameters = identificationParametersEditionDialog.getIdentificationParameters();
            identificationParametersFile = IdentificationParametersFactory.getIdentificationParametersFile(tempIdentificationParameters.getName());
            setIdentificationParameters(tempIdentificationParameters);
        }
    }

    /**
     * Opens a dialog allowing the setting of paths.
     */
    public void editPathSettings() {
        try {
            HashMap<PathKey, String> pathSettings = new HashMap<PathKey, String>();
            for (SearchGUIPathPreferences.SearchGUIPathKey searchGUIPathKey : SearchGUIPathPreferences.SearchGUIPathKey.values()) {
                pathSettings.put(searchGUIPathKey, SearchGUIPathPreferences.getPathPreference(searchGUIPathKey, getJarFilePath()));
            }
            for (UtilitiesPathPreferences.UtilitiesPathKey utilitiesPathKey : UtilitiesPathPreferences.UtilitiesPathKey.values()) {
                pathSettings.put(utilitiesPathKey, UtilitiesPathPreferences.getPathPreference(utilitiesPathKey));
            }
            PathSettingsDialog pathSettingsDialog = new PathSettingsDialog(this, "SearchGUI", pathSettings);
            if (!pathSettingsDialog.isCanceled()) {
                HashMap<PathKey, String> newSettings = pathSettingsDialog.getKeyToPathMap();
                for (PathKey pathKey : pathSettings.keySet()) {
                    String oldPath = pathSettings.get(pathKey);
                    String newPath = newSettings.get(pathKey);
                    if (oldPath == null && newPath != null
                            || oldPath != null && newPath == null
                            || oldPath != null && newPath != null && !oldPath.equals(newPath)) {
                        SearchGUIPathPreferences.setPathPreference(pathKey, newPath);
                    }
                }
                // write path file preference
                File destinationFile = new File(getJarFilePath(), UtilitiesPathPreferences.configurationFileName);
                try {
                    SearchGUIPathPreferences.writeConfigurationToFile(destinationFile, getJarFilePath());
                    restart();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, new String[]{"An error occurred while setting the configuration ", e.getMessage()}, "Error Reading File", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, new String[]{"An error occurred while setting the configuration ", e.getMessage()}, "Error Reading File", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Opens a dialog allowing the user to edit the PeptideShaker settings.
     */
    private void editPeptideShakerSettings() {

        PeptideShakerSettingsDialog psSettingsDialog = new PeptideShakerSettingsDialog(this, true, searchHandler.getMascotFiles());
        if (!psSettingsDialog.isCanceled()) {
            searchHandler.setExperimentLabel(psSettingsDialog.getProjectName());
            searchHandler.setSampleLabel(psSettingsDialog.getSampleName());
            searchHandler.setReplicateNumber(psSettingsDialog.getReplicateNumber());
            searchHandler.setPeptideShakerFile(psSettingsDialog.getPeptideShakerOutputFile());
            searchHandler.setMascotFiles(psSettingsDialog.getMascotFiles());
        } else {
            peptideShakerCheckBox.setSelected(false);
        }
    }

    /**
     * Sets a new output folder.
     *
     * @param aFolder the new output folder
     */
    private void setOutputFolder(File aFolder) {
        if (aFolder != null) {
            outputFolder = aFolder;
            outputFolderTxt.setText(outputFolder.getAbsolutePath());
            if (outputFolderTxt.getText().length() > 70) {
                outputFolderTxt.setHorizontalAlignment(JTextField.LEADING);
            } else {
                outputFolderTxt.setHorizontalAlignment(JTextField.CENTER);
            }
        }
    }

    /**
     * Returns the output folder.
     *
     * @return the output folder
     */
    public File getOutputFolder() {
        return outputFolder;
    }

    /**
     * Set the spectrum files.
     *
     * @param files the spectrum files.
     */
    private void setSpectrumFiles(ArrayList<File> mgfFiles, ArrayList<File> rawFiles) {

        if (mgfFiles != null) {
            this.mgfFiles = mgfFiles;
        } else {
            mgfFiles = new ArrayList<File>();
        }

        if (rawFiles != null) {
            this.rawFiles = rawFiles;
        } else {
            rawFiles = new ArrayList<File>();
        }

        // note: already done in the command line
//        // verify that all mgf files have different names
//        if (!verifyMgfFilesNames()) {
//            mgfFiles.clear();
//        } else {
//            // check if some of the mgfs are too big
//            verifyMgfFilesSize();
//        }
        spectraFilesTxt.setText((mgfFiles.size() + rawFiles.size()) + " file(s) selected");
    }

    /**
     * Returns the spectra files selected.
     *
     * @return the spectra file selected
     */
    public ArrayList<File> getMgfFiles() {
        return mgfFiles;
    }

    /**
     * Validate that the search engines work.
     *
     * @param showMessage show massage to the user
     * @return true, if both search engines seem to work
     */
    public boolean validateSearchEngines(boolean showMessage) {

        boolean omssaValid = true;
        boolean xtandemValid = true;
        boolean msgfValid = true;
        boolean msAmandaValid = true;
        boolean myriMatchValid = true;
        boolean cometValid = true;
        boolean tideValid = true;
        boolean andromedaValid = true;

        if (enableOmssaJCheckBox.isSelected()) {
            omssaValid = validateSearchEngineInstallation(Advocate.omssa, searchHandler.getOmssaLocation(), showMessage);
        }
        if (enableXTandemJCheckBox.isSelected()) {
            xtandemValid = validateSearchEngineInstallation(Advocate.xtandem, searchHandler.getXtandemLocation(), showMessage);
        }
        if (enableMsgfJCheckBox.isSelected()) {
            msgfValid = validateSearchEngineInstallation(Advocate.msgf, searchHandler.getMsgfLocation(), showMessage);
        }
        if (enableMsAmandaJCheckBox.isSelected()) {
            msAmandaValid = validateSearchEngineInstallation(Advocate.msAmanda, searchHandler.getMsAmandaLocation(), showMessage);
        }
        if (enableMyriMatchJCheckBox.isSelected()) {
            myriMatchValid = validateSearchEngineInstallation(Advocate.myriMatch, searchHandler.getMyriMatchLocation(), showMessage);
        }
        if (enableCometJCheckBox.isSelected()) {
            cometValid = validateSearchEngineInstallation(Advocate.comet, searchHandler.getCometLocation(), showMessage);
        }
        if (enableTideJCheckBox.isSelected()) {
            tideValid = validateSearchEngineInstallation(Advocate.tide, searchHandler.getTideLocation(), showMessage);
        }
        if (enableAndromedaJCheckBox.isSelected()) {
            andromedaValid = validateSearchEngineInstallation(Advocate.andromeda, searchHandler.getAndromedaLocation(), showMessage);
        }

        if (!omssaValid || !xtandemValid || !msgfValid || !msAmandaValid || !myriMatchValid || !cometValid || !tideValid || !andromedaValid) {
            new SearchEnginesSettingsDialog(this, true);
        }

        return omssaValid && xtandemValid && msgfValid && msAmandaValid && myriMatchValid && cometValid && tideValid && andromedaValid;
    }

    /**
     * Validates the input.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the input is valid.
     */
    private boolean validateInput(boolean showMessage) {

        boolean valid = true;

        if (!enableOmssaJCheckBox.isSelected() && !enableXTandemJCheckBox.isSelected()
                && !enableMsgfJCheckBox.isSelected() && !enableMsAmandaJCheckBox.isSelected()
                && !enableMyriMatchJCheckBox.isSelected()
                && !enableCometJCheckBox.isSelected()
                && !enableTideJCheckBox.isSelected()
                && !enableAndromedaJCheckBox.isSelected()) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to select at least one search engine.", "No Search Engines Selected", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
        }

        if (mgfFiles.isEmpty() && rawFiles.isEmpty()) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to select at least one spectrum file.", "Spectra Files Not Found", JOptionPane.WARNING_MESSAGE);
            }
            spectraFilesLabel.setForeground(Color.RED);
            spectraFilesLabel.setToolTipText("Please select at least one spectrum file");
            spectraFilesTxt.setToolTipText(null);
            valid = false;
            spectraFilesTxt.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        } else {
            spectraFilesLabel.setToolTipText(null);
            spectraFilesTxt.setToolTipText("Click to see the selected files");
            spectraFilesTxt.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            spectraFilesLabel.setForeground(Color.BLACK);
        }

        // validate the search parameters
        if (!validateParametersInput(showMessage)) {
            valid = false;
        }

        // validate the output folder
        if (outputFolderTxt.getText() == null || outputFolderTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify an output folder.", "Output Folder Not Found", JOptionPane.WARNING_MESSAGE);
            }
            resultFolderLbl.setForeground(Color.RED);
            resultFolderLbl.setToolTipText("Please select an output folder");
            valid = false;
        } else if (!new File(outputFolderTxt.getText()).exists()) {
            int value = JOptionPane.showConfirmDialog(this, "The selected output folder does not exist. Do you want to create it?", "Folder Not Found", JOptionPane.YES_NO_OPTION);

            if (value == JOptionPane.YES_OPTION) {
                boolean success = new File(outputFolderTxt.getText()).mkdir();

                if (!success) {
                    JOptionPane.showMessageDialog(this, "Failed to create the output folder. Please create it manually and re-select it.", "File Error", JOptionPane.ERROR_MESSAGE);
                    valid = false;
                } else {
                    resultFolderLbl.setForeground(Color.BLACK);
                    resultFolderLbl.setToolTipText(null);
                }
            }
        } else {
            resultFolderLbl.setForeground(Color.BLACK);
            resultFolderLbl.setToolTipText(null);
        }

        searchButton.setEnabled(valid);
        return valid;
    }

    /**
     * Verifies that the modifications backed-up in the search parameters are
     * loaded and alerts the user in case conflicts are found.
     *
     * @param searchParameters the search parameters to load
     */
    private void loadModifications(SearchParameters searchParameters) {
        ArrayList<String> toCheck = ptmFactory.loadBackedUpModifications(searchParameters, false); // @TODO: have to set the searchparams???
        if (!toCheck.isEmpty()) {
            String message = "The definition of the following PTM(s) seems to have changed and were not loaded:\n";
            for (int i = 0; i < toCheck.size(); i++) {
                if (i > 0) {
                    if (i < toCheck.size() - 1) {
                        message += ", ";
                    } else {
                        message += " and ";
                    }
                }
                message += toCheck.get(i);
            }
            message += ".\nPlease verify the definition of the PTM(s) in the modifications editor.";
            javax.swing.JOptionPane.showMessageDialog(this,
                    message, "PTM Definition Obsolete", JOptionPane.OK_OPTION);
        }
    }

    /**
     * Inspects the parameter validity.
     *
     * @param showMessage if true an error message is shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateParametersInput(boolean showMessage) {

        if (identificationParameters == null || identificationParametersFile == null) {
            return false;
        }

        String parametersName = identificationParameters.getName();
        if (parametersName == null) {
            parametersName = Util.removeExtension(identificationParametersFile.getName());
        }
        SearchSettingsDialog settingsDialog = new SearchSettingsDialog(this, identificationParameters.getSearchParameters(),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")),
                false, true, searchHandler.getConfigurationFile(), lastSelectedFolder, parametersName, true);
        boolean valid = settingsDialog.validateParametersInput(false);

        if (!valid) {
            if (showMessage) {
                settingsDialog.validateParametersInput(true);
                editIdentificationParameters();
            } else {
                searchSettingsLbl.setForeground(Color.RED);
                searchSettingsLbl.setToolTipText("Please check the search settings");
            }
        } else {
            searchSettingsLbl.setToolTipText(null);
            searchSettingsLbl.setForeground(Color.BLACK);
        }

        return valid;
    }

    /**
     * Verifies that all mgf files have different names and displays a warning
     * with the first conflict encountered.
     *
     * @return true if all mgf files have different names, false otherwise
     */
    public boolean verifyMgfFilesNames() {
        for (File file1 : mgfFiles) {
            for (File file2 : mgfFiles) {
                if (file1 != file2 && file1.getName().equals(file2.getName())) {

                    this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")));

                    JOptionPane.showMessageDialog(this,
                            "The following files have the same name: \n"
                            + file1.getAbsolutePath() + "\n"
                            + file2.getAbsolutePath() + "\n\n"
                            + "Please select files with unique file names.",
                            "Identical File Names!",
                            JOptionPane.WARNING_MESSAGE);

                    this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));

                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validates an MGF file.
     *
     * @param currentSpectrumFile the MGF file to validate
     * @param waitingHandler the waiting handler
     * @return true, if the file is validated, false if canceled by the user
     */
    private boolean validateMgfFile(File currentSpectrumFile, WaitingHandler waitingHandler) {

        boolean canceled = false;

        try {
            // index the file, if needed
            spectrumFactory.addSpectra(currentSpectrumFile, waitingHandler);

            if (waitingHandler.isRunCanceled()) {
                return false;
            }

            // @TODO: merge with code from the cli (and make it gui independent!)
            File indexFile = new File(currentSpectrumFile.getParent(), currentSpectrumFile.getName() + ".cui");

            // check for missing spectrum titles
            if (spectrumFactory.getIndex(indexFile).getSpectrumTitles().size() < spectrumFactory.getIndex(indexFile).getNSpectra()) {

                this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")));

                String errorText;

                if (spectrumFactory.getIndex(indexFile).getSpectrumTitles().isEmpty()) {
                    errorText = "No spectrum titles found in file: ";
                } else {
                    errorText = "Spectrum titles missing in file: ";
                }

                Object[] options = {"Yes", "No", "Cancel"};
                int result = JOptionPane.showOptionDialog(this,
                        errorText + " \'" + currentSpectrumFile.getName() + "\'.\n"
                        + "Spectrum titles are mandatory in SearchGUI and PeptideShaker.\n"
                        + "Add the missing spectrum titles?",
                        "Spectrum Titles?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                if (result == JOptionPane.YES_OPTION) {
                    // add missing titles
                    this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
                    progressDialog.setTitle("Adding Missing Spectrum Titles. Please Wait...");
                    spectrumFactory.closeFiles();
                    MgfReader.addMissingSpectrumTitles(currentSpectrumFile, waitingHandler);
                    spectrumFactory.addSpectra(currentSpectrumFile, waitingHandler);
                } else {
                    // don't use the file
                    this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
                    return false;
                }

                this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
            }

            // check for lack of peak picking
            if (checkPeakPicking && !spectrumFactory.getIndex(indexFile).isPeakPicked()) {

                this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")));

                int result = JOptionPane.showConfirmDialog(this,
                        JOptionEditorPane.getJOptionEditorPane(
                                "The file \'" + currentSpectrumFile.getName() + "\' contains zero intensity peaks.<br><br>"
                                + "Please make sure that the file is peak picked.<br>"
                                + "See <a href=\"http://compomics.com/bioinformatics-for-proteomics\">Bioinformatics for Proteomics - Chapter 1.2</a> for more help.<br><br>"
                                + "Do you want to continue with this mgf file anyway?"),
                        "Remove zero intensities?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    // remove zero intensities
                    this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
                    progressDialog.setTitle("Removing Zero Intensities. Please Wait...");
                    spectrumFactory.closeFiles();
                    MgfReader.removeZeroes(currentSpectrumFile, waitingHandler);
                    spectrumFactory.addSpectra(currentSpectrumFile, waitingHandler);
                } else {
                    // don't use the file
                    this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
                    return false;
                }

                this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
            }

            if (canceled) {
                return false;
            }

            // check for ms2 spectra
            if (spectrumFactory.getIndex(indexFile).getMaxPeakCount() == 0) {
                this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")));
                JOptionPane.showMessageDialog(this, "No MS2 spectra found in file: " + currentSpectrumFile.getName() + "!"
                        + "\nFile will be ignored.", "No MS2 Spectra", JOptionPane.WARNING_MESSAGE);
                this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
                return false;
            }

            // check for duplicate titles
            HashMap<String, Integer> duplicatedSpectrumTitles = spectrumFactory.getIndex(indexFile).getDuplicatedSpectrumTitles();

            if (checkDuplicateTitles && duplicatedSpectrumTitles != null && duplicatedSpectrumTitles.size() > 0) {

                this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")));

                Object[] options = {"Rename", "Delete", "Ignore"};
                int result = JOptionPane.showOptionDialog(this,
                        "The file \'" + currentSpectrumFile.getAbsolutePath() + "\' contains duplicate spectrum titles!\n"
                        + "Example: \'" + duplicatedSpectrumTitles.keySet().iterator().next() + "\'.\n"
                        + "For the complete list see the SearchGUI log file.\n\n"
                        + "We strongly recommend having unique spectrum titles. Fix duplicated titles?",
                        "Duplicated Spectrum Titles", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                if (result == JOptionPane.YES_OPTION) {
                    // rename duplicated titles
                    this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
                    progressDialog.setTitle("Renaming Duplicated Spectrum Titles. Please Wait...");
                    spectrumFactory.closeFiles();
                    MgfReader.renameDuplicateSpectrumTitles(currentSpectrumFile, waitingHandler);
                    spectrumFactory.addSpectra(currentSpectrumFile, waitingHandler);
                } else if (result == JOptionPane.NO_OPTION) {
                    // delete duplicated titles
                    this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
                    progressDialog.setTitle("Deleting Duplicated Spectrum Titles. Please Wait...");
                    spectrumFactory.closeFiles();
                    MgfReader.removeDuplicateSpectrumTitles(currentSpectrumFile, waitingHandler);
                    spectrumFactory.addSpectra(currentSpectrumFile, waitingHandler);
                } else {
                    // do nothing with the titles
                }

                this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")));
            }
        } catch (FileNotFoundException e) {
            canceled = true;
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Mgf File Error", JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            canceled = true;
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Mgf File Error", JOptionPane.WARNING_MESSAGE);
        } catch (ClassNotFoundException e) {
            canceled = true;
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while validating the mgf file: " + e.getMessage(), "Mgf Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            canceled = true;
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while validating the mgf file: " + e.getMessage(), "Mgf Validation Error", JOptionPane.WARNING_MESSAGE);
        }

        return !canceled;
    }

    /**
     * Verifies whether MGF files are below the given maximal size and splits
     * the large ones if needed.
     */
    private void verifyMgfFilesSize() {
        ArrayList<File> fatFiles = new ArrayList<File>();
        for (File file : mgfFiles) {
            if (file.length() > (((long) mgfMaxSize) * 1048576)) {
                fatFiles.add(file);
            }
        }
        if (!fatFiles.isEmpty()) {
            String message = "";
            if (fatFiles.size() == 1) {
                message += "The file " + fatFiles.get(0).getName() + "\nis rather large and can thus impair the search and parsing of the result files."
                        + "\nSplit into smaller files?";
            } else if (fatFiles.size() <= 6) {
                message += "The files\n";

                for (File file : fatFiles) {
                    message += file.getName() + "\n";
                }

                message += " are rather large and can thus impair the search and parsing of the result files."
                        + "\nSplit into smaller files?";
            } else {
                message += "Some files are rather large and can thus impair the search and parsing of the result files."
                        + "\nSplit into smaller files?";
            }
            int outcome = JOptionPane.showConfirmDialog(this,
                    message, "Large MGF Files",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (outcome == JOptionPane.YES_OPTION) {
                for (File file : fatFiles) {
                    String splittedName = file.getName().substring(0, file.getName().lastIndexOf("."));
                    String currentName = splittedName + "_" + 1 + ".mgf";
                    File testFile = new File(file.getParent(), currentName);
                    if (testFile.exists()) {
                        outcome = JOptionPane.showConfirmDialog(this,
                                "Split files seem to alredy exist. Overwrite existing mgf files?", "Existing MGF Files",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (outcome != JOptionPane.YES_OPTION) {
                            return;
                        }
                        break;
                    }
                }
                splitFiles(fatFiles);
            }
        }
    }

    /**
     * Splits the given MGF files.
     *
     * @param files the files to split
     */
    public void splitFiles(ArrayList<File> files) {

        progressDialog = new ProgressDialogX(this,
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")),
                true);
        progressDialog.setPrimaryProgressCounterIndeterminate(true);
        progressDialog.setTitle("Splitting MGF File(s). Please Wait...");

        final SearchGUI finalRef = this;

        final ArrayList<File> originalMgfFiles = new ArrayList<File>(files);

        new Thread(new Runnable() {
            public void run() {
                try {
                    progressDialog.setVisible(true);
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                }
            }
        }, "ProgressDialog").start();

        new Thread("SplitThread") {
            @Override
            public void run() {
                for (File originalFile : originalMgfFiles) {

                    if (progressDialog.isRunCanceled()) {
                        break;
                    }

                    ArrayList<MgfIndex> indexes;
                    progressDialog.setTitle("Splitting " + originalFile.getName() + ". Please Wait...");
                    progressDialog.setPrimaryProgressCounterIndeterminate(false);

                    try {
                        indexes = mgfReader.splitFile(originalFile, getMgfNSpectra(), progressDialog);
                    } catch (FileNotFoundException e) {
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(finalRef,
                                new String[]{"Mgf Splitting Error.", "File " + originalFile.getName() + " not found."},
                                "Mgf Splitting Error", JOptionPane.WARNING_MESSAGE);
                        e.printStackTrace();
                        return;
                    } catch (IOException e) {
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(finalRef,
                                new String[]{"Mgf Splitting Error.", "An error occurred while reading/writing the mgf file."},
                                "Mgf Splitting Error", JOptionPane.WARNING_MESSAGE);
                        e.printStackTrace();
                        return;
                    } catch (OutOfMemoryError error) {
                        Runtime.getRuntime().gc();
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(finalRef,
                                "SearchGUI used up all the available memory and had to be stopped.\n"
                                + "Memory boundaries are set in the Edit menu (Edit > Java Options).",
                                "Out Of Memory Error",
                                JOptionPane.ERROR_MESSAGE);
                        System.out.println("Ran out of memory!");
                        error.printStackTrace();
                        return;
                    }

                    if (progressDialog.isRunCanceled()) {
                        progressDialog.setRunFinished();
                        return;
                    }

                    try {
                        if (indexes != null && !indexes.isEmpty()) {
                            progressDialog.setPrimaryProgressCounterIndeterminate(false);
                            progressDialog.setMaxPrimaryProgressCounter(indexes.size());
                            progressDialog.setTitle("Writing Indexes. Please Wait...");
                            for (int i = 0; i < indexes.size() && !progressDialog.isRunCanceled(); i++) {
                                spectrumFactory.writeIndex(indexes.get(i), originalFile.getParentFile());
                                progressDialog.setValue(i);
                            }
                        }
                    } catch (IOException e) {
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(finalRef,
                                new String[]{"MGF Splitting Error.", "An error occurred while writing an mgf index."},
                                "MGF Splitting Error", JOptionPane.WARNING_MESSAGE);
                        e.printStackTrace();
                        return;
                    }

                    if (progressDialog.isRunCanceled()) {
                        progressDialog.setRunFinished();
                        return;
                    }

                    mgfFiles.remove(originalFile);

                    for (int i = 0; i < indexes.size() && !progressDialog.isRunCanceled(); i++) {
                        File newFile = new File(originalFile.getParent(), indexes.get(i).getFileName());
                        mgfFiles.add(newFile);
                    }
                }

                if (progressDialog.isRunCanceled()) {
                    progressDialog.setRunFinished();
                } else {
                    progressDialog.setRunFinished();
                    spectraFilesTxt.setText(mgfFiles.size() + " file(s) selected");
                    JOptionPane.showMessageDialog(finalRef, "MGF file(s) split and selected.", "Files Split", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }.start();
    }

    /**
     * Returns true if the settings have been displayed.
     *
     * @return true if the settings have has been displayed
     */
    public boolean settingsDisplayed() {
        return settingsTabDisplayed;
    }

    /**
     * Set if the settings have been displayed.
     *
     * @param settingsDisplayed boolean indicating whether the settings have
     * been displayed
     */
    public void setSettingsDisplayed(boolean settingsDisplayed) {
        this.settingsTabDisplayed = settingsDisplayed;
    }

    /**
     * The main method used to start SearchGUI.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // set the look and feel
        boolean numbusLookAndFeelSet = false;
        try {
            numbusLookAndFeelSet = UtilitiesGUIDefaults.setLookAndFeel();
        } catch (Exception e) {
        }

        if (!numbusLookAndFeelSet) {
            JOptionPane.showMessageDialog(null,
                    "Failed to set the default look and feel. Using backup look and feel.\n"
                    + "SearchGUI will work but not look as good as it should...", "Look and Feel",
                    JOptionPane.WARNING_MESSAGE);
        }

        // need to add some padding to the text in the titled borders on Java 1.7 
        if (!System.getProperty("java.version").startsWith("1.6")) {
            TITLED_BORDER_HORIZONTAL_PADDING = "   ";
        }

        ArrayList<File> spectrumFiles = null;
        ArrayList<File> rawFiles = null;
        File searchParametersFile = null;
        File outputFolder = null;
        String currentSpecies = null, currentSpeciesType = null;
        boolean spectrum = false, raw = false, parameters = false, output = false, species = false, speciesType = false;

        for (String arg : args) {
            if (spectrum) {
                try {
                    ArrayList<String> extensions = new ArrayList<String>();
                    extensions.add(".mgf");
                    spectrumFiles = CommandLineUtils.getFiles(arg, extensions);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Failed importing spectrum files from command line option " + arg + ".", "Spectrum Files",
                            JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                }
                spectrum = false;
            }
            if (raw) {
                try {
                    ArrayList<String> extensions = new ArrayList<String>();
                    for (MsFormat format : MsFormat.values()) {
                        if (format != MsFormat.mgf) {
                            extensions.add(format.fileNameEnding);
                        }
                    }
                    rawFiles = CommandLineUtils.getFiles(arg, extensions);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Failed importing raw files from command line option " + arg + ".", "Raw Files",
                            JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                }
                raw = false;
            }
            if (parameters) {
                searchParametersFile = new File(arg);
                try {
                    IdentificationParameters.getIdentificationParameters(searchParametersFile);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to import search parameters from: " + searchParametersFile.getAbsolutePath() + ".", "Search Parameters",
                            JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                }
                parameters = false;
            }
            if (output) {
                outputFolder = new File(arg);
                output = false;
            }
            if (species) {
                currentSpecies = arg;
                species = false;
            }
            if (speciesType) {
                currentSpeciesType = arg;
                speciesType = false;
            }
            if (arg.equals(ToolFactory.searchGuiSpectrumFileOption)) {
                spectrum = true;
            }
            if (arg.equals(ToolFactory.searchGuiRawFileOption)) {
                raw = true;
            }
            if (arg.equals(ToolFactory.searchGuiParametersFileOption)) {
                parameters = true;
            }
            if (arg.equals(ToolFactory.outputFolderOption)) {
                output = true;
            }
            if (arg.equals(ToolFactory.speciesOption)) {
                species = true;
            }
            if (arg.equals(ToolFactory.speciesTypeOption)) {
                speciesType = true;
            }
        }

        new SearchGUI(spectrumFiles, rawFiles, searchParametersFile, outputFolder, currentSpecies, currentSpeciesType);
    }

    /**
     * Returns the last selected folder.
     *
     * @return the last selected folder
     */
    public LastSelectedFolder getLastSelectedFolder() {
        if (lastSelectedFolder == null) {
            lastSelectedFolder = new LastSelectedFolder();
            utilitiesUserPreferences.setLastSelectedFolder(lastSelectedFolder);
        }
        return lastSelectedFolder;
    }

    /**
     * Set the last selected folder.
     *
     * @param lastSelectedFolder the folder to set
     */
    public void setLastSelectedFolder(LastSelectedFolder lastSelectedFolder) {
        this.lastSelectedFolder = lastSelectedFolder;
    }

    /**
     * Ask the user if he/she wants to add a shortcut at the desktop.
     */
    private void addShortcutAtDeskTop() {

        String jarFilePath = getJarFilePath();
        String versionNumber = new eu.isas.searchgui.utilities.Properties().getVersion();

        if (!jarFilePath.equalsIgnoreCase(".")) {

            // remove the initial '/' at the start of the line
            if (jarFilePath.startsWith("\\") && !jarFilePath.startsWith("\\\\")) {
                jarFilePath = jarFilePath.substring(1);
            }

            String iconFileLocation = jarFilePath + "\\resources\\searchgui.ico";
            String jarFileLocation = jarFilePath + "\\SearchGUI-" + versionNumber + ".jar";

            try {
                JShellLink link = new JShellLink();
                link.setFolder(JShellLink.getDirectory("desktop"));
                link.setName("SearchGUI " + versionNumber);
                link.setIconLocation(iconFileLocation);
                link.setPath(jarFileLocation);
                link.save();
            } catch (Exception e) {
                System.out.println("An error occurred when trying to create a desktop shortcut...");
                e.printStackTrace();
            }
        }
    }

    /**
     * Set up the log file.
     */
    private void setUpLogFile() {
        if (useLogFile && !getJarFilePath().equalsIgnoreCase(".")) {
            try {
                String path = getJarFilePath() + "/resources/SearchGUI.log";

                File file = new File(path);
                System.setOut(new java.io.PrintStream(new FileOutputStream(file, true)));
                System.setErr(new java.io.PrintStream(new FileOutputStream(file, true)));

                // creates a new log file if it does not exist
                if (!file.exists()) {
                    boolean fileCreated = file.createNewFile();

                    if (fileCreated) {
                        FileWriter w = new FileWriter(file);
                        BufferedWriter bw = new BufferedWriter(w);
                        bw.close();
                        w.close();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to create the file log file.\n"
                                + "Please contact the developers.", "File Error", JOptionPane.OK_OPTION);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null, "An error occurred when trying to create the SearchGUI log file.",
                        "Error Creating Log File", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is called whenever the 'Search' button is pressed. It starts
     * the search.
     */
    public void startSearch() {

        if (validateInput(true)) {

            waitingDialog = new WaitingDialog(this,
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")), false,
                    getTips(),
                    "Search",
                    "SearchGUI",
                    new eu.isas.searchgui.utilities.Properties().getVersion(),
                    true);

            waitingDialog.addWaitingActionListener(new WaitingActionListener() {
                @Override
                public void cancelPressed() {
                    searchHandler.cancelSearch();
                }
            });

            waitingDialog.setLocationRelativeTo(this);

            try {
                searchHandler.startSearch(waitingDialog);
            } catch (InterruptedException e) {
                // should happen only in cli mode
                e.printStackTrace();
            }
        }
    }

    /**
     * Attempts to delete the temporary folders. Prints the stacktrace if an
     * error occurs.
     */
    private static void deleteTempFolders() {
        try {
            TempFilesManager.deleteTempFolders();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method saves the Search engines location file in the conf folder.
     */
    private void saveConfigurationFile() {

        File folder = new File(getJarFilePath() + File.separator + "resources" + File.separator + "conf" + File.separator);

        if (!folder.exists()) {
            JOptionPane.showMessageDialog(this, new String[]{"Unable to find folder: '" + folder.getAbsolutePath() + "'!",
                "Could not save search engine locations."}, "Folder Not Found", JOptionPane.WARNING_MESSAGE);
        } else {
            File output = new File(folder, SearchHandler.SEARCH_GUI_CONFIGURATION_FILE);
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(output));
                bw.write("OMSSA Location:" + System.getProperty("line.separator"));
                bw.write(searchHandler.getOmssaLocation() + System.getProperty("line.separator") + searchHandler.isOmssaEnabled() + System.getProperty("line.separator"));
                bw.write("X!Tandem Location:" + System.getProperty("line.separator"));
                bw.write(searchHandler.getXtandemLocation() + System.getProperty("line.separator") + searchHandler.isXtandemEnabled() + System.getProperty("line.separator"));
                bw.write("MS-GF+ Location:" + System.getProperty("line.separator"));
                bw.write(searchHandler.getMsgfLocation() + System.getProperty("line.separator") + searchHandler.isMsgfEnabled() + System.getProperty("line.separator"));
                bw.write("MS Amanda Location:" + System.getProperty("line.separator"));
                bw.write(searchHandler.getMsAmandaLocation() + System.getProperty("line.separator") + searchHandler.isMsAmandaEnabled() + System.getProperty("line.separator"));
                bw.write("MyriMatch Location:" + System.getProperty("line.separator"));
                bw.write(searchHandler.getMyriMatchLocation() + System.getProperty("line.separator") + searchHandler.isMyriMatchEnabled() + System.getProperty("line.separator"));
                bw.write("Comet Location:" + System.getProperty("line.separator"));
                bw.write(searchHandler.getCometLocation() + System.getProperty("line.separator") + searchHandler.isCometEnabled() + System.getProperty("line.separator"));
                bw.write("Tide Location:" + System.getProperty("line.separator"));
                bw.write(searchHandler.getTideLocation() + System.getProperty("line.separator") + searchHandler.isTideEnabled() + System.getProperty("line.separator"));
                bw.write("Andromeda Location:" + System.getProperty("line.separator"));
                bw.write(searchHandler.getAndromedaLocation() + System.getProperty("line.separator") + searchHandler.isAndromedaEnabled() + System.getProperty("line.separator"));
                bw.write("makeblastdb Location:" + System.getProperty("line.separator"));
                bw.write(searchHandler.getMakeblastdbLocation() + System.getProperty("line.separator") + System.getProperty("line.separator"));
                bw.write("Modification use:" + System.getProperty("line.separator"));
                bw.write(getModificationUseAsString() + System.getProperty("line.separator"));
                bw.flush();
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                JOptionPane.showMessageDialog(this, new String[]{"Unable to write file: '" + ioe.getMessage() + "'!",
                    "Could not save search engine locations."}, "Search Engine Location Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Returns the max mgf file size before splitting.
     *
     * @return the mgfMaxSize
     */
    public double getMgfMaxSize() {
        return mgfMaxSize;
    }

    /**
     * Set the max mgf file size before splitting.
     *
     * @param mgfMaxSize the mgfMaxSize to set
     */
    public void setMgfMaxSize(double mgfMaxSize) {
        this.mgfMaxSize = mgfMaxSize;
    }

    /**
     * Get the max number of spectra in an mgf file.
     *
     * @return the mgfNSpectra
     */
    public int getMgfNSpectra() {
        return mgfNSpectra;
    }

    /**
     * Set the max number of spectra in an mgf file.
     *
     * @param mgfNSpectra the mgfNSpectra to set
     */
    public void setMgfNSpectra(int mgfNSpectra) {
        this.mgfNSpectra = mgfNSpectra;
    }

    /**
     * Returns if the spectra should be checked for duplicate titles or not.
     *
     * @return true if the spectra should be checked for duplicate titles
     */
    public boolean checkDuplicateTitles() {
        return checkDuplicateTitles;
    }

    /**
     * Set if the spectra should be checked for duplicate titles or not.
     *
     * @param checkDuplicateTitles the checkDuplicateTitles to set
     */
    public void setCheckDuplicateTitles(boolean checkDuplicateTitles) {
        this.checkDuplicateTitles = checkDuplicateTitles;
    }

    /**
     * Returns if the spectra should be checked for peak picking or not.
     *
     * @return true if the spectra should be checked for peak picking
     */
    public boolean checkPeakPicking() {
        return checkPeakPicking;
    }

    /**
     * Set if the spectra should be checked for peak picking or not.
     *
     * @param checkPeakPicking the checkPeakPicking to set
     */
    public void setCheckPeakPicking(boolean checkPeakPicking) {
        this.checkPeakPicking = checkPeakPicking;
    }

    /**
     * Returns a reference to the SearchGUI command line interface.
     *
     * @return a reference to the SearchGUI command line interface
     */
    public SearchHandler getSearchHandler() {
        return searchHandler;
    }

    /**
     * Enable or disable the search engines.
     *
     * @param enableOmssa enable OMSSA?
     * @param enbleXTandem enable X!Tandem?
     * @param enableMsgf enable MS-GF+?
     * @param enableMsAmanda enable MS Amanda
     * @param enableMyriMatch enable MyriMatch
     * @param enableComet enable Comet
     * @param enableTide enable Tide
     * @param enableAndromeda enable Tide
     */
    public void enableSearchEngines(boolean enableOmssa, boolean enbleXTandem, boolean enableMsgf, boolean enableMsAmanda,
            boolean enableMyriMatch, boolean enableComet, boolean enableTide, boolean enableAndromeda) {
        enableOmssaJCheckBox.setSelected(enableOmssa);
        enableXTandemJCheckBox.setSelected(enbleXTandem);
        enableMsgfJCheckBox.setSelected(enableMsgf);
        enableMsAmandaJCheckBox.setSelected(enableMsAmanda);
        enableMyriMatchJCheckBox.setSelected(enableMyriMatch);
        enableCometJCheckBox.setSelected(enableComet);
        enableTideJCheckBox.setSelected(enableTide);
        enableAndromedaJCheckBox.setSelected(enableAndromeda);
        searchHandler.setOmssaEnabled(enableOmssa);
        searchHandler.setXtandemEnabled(enbleXTandem);
        searchHandler.setMsgfEnabled(enableMsgf);
        searchHandler.setMsAmandaEnabled(enableMsAmanda);
        searchHandler.setMyriMatchEnabled(enableMyriMatch);
        searchHandler.setCometEnabled(enableComet);
        searchHandler.setTideEnabled(enableTide);
        searchHandler.setAndromedaEnabled(enableAndromeda);
        validateInput(false);
    }

    /**
     * Returns the identification parameters.
     *
     * @return the identification parameters
     */
    public IdentificationParameters getIdentificationParameters() {
        return identificationParameters;
    }

    /**
     * Sets the search parameters.
     *
     * @param identificationParameters the identification parameters
     */
    public void setIdentificationParameters(IdentificationParameters identificationParameters) {
        this.identificationParameters = identificationParameters;

        Vector parameterList = new Vector();
        parameterList.add("-- Select --");

        for (String tempParameters : identificationParametersFactory.getParametersList()) {
            parameterList.add(tempParameters);
        }

        settingsComboBox.setModel(new javax.swing.DefaultComboBoxModel(parameterList));
        settingsComboBox.setSelectedItem(identificationParameters.getName());

        validateInput(false);
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public static String getJarFilePath() {
        return CompomicsWrapper.getJarFilePath((new SearchGUI()).getClass().getResource("SearchGUI.class").getPath(), "SearchGUI");
    }

    @Override
    public void restart() {
        dispose();
        new SearchGUIWrapper();
        System.exit(0); // have to close the current java process (as a new one is started on the line above)
    }

    @Override
    public UtilitiesUserPreferences getUtilitiesUserPreferences() {
        return utilitiesUserPreferences;
    }

    /**
     * Loads the use of modifications from a line.
     *
     * @param aLine modification use line from the configuration file
     */
    private void loadModificationUse(String aLine) {
        ArrayList<String> modificationUses = new ArrayList<String>();

        // Split the different modifications.
        int start;

        while ((start = aLine.indexOf(MODIFICATION_SEPARATOR)) >= 0) {
            String name = aLine.substring(0, start);
            aLine = aLine.substring(start + 2);
            if (!name.trim().equals("")) {
                modificationUses.add(name);
            }
        }

        for (String name : modificationUses) {
            start = name.indexOf("_");
            String modificationName = name;

            if (start != -1) {
                modificationName = name.substring(0, start); // old format, remove usage statistics
            }

            if (ptmFactory.containsPTM(modificationName)) {
                modificationUse.add(modificationName);
            }
        }
    }

    /**
     * Returns a line with the most used modifications.
     *
     * @return a line containing the most used modifications
     */
    public String getModificationUseAsString() {
        String result = "";
        for (String name : modificationUse) {
            result += name + MODIFICATION_SEPARATOR;
        }
        return result;
    }

    /**
     * Returns a list with the most used modifications.
     *
     * @return a list with the most used modifications
     */
    public ArrayList<String> getModificationUse() {
        return modificationUse;
    }

    /**
     * Tries to validate the search engine installation by running the
     * executable.
     *
     * @param advocate the search engine advocate
     * @param searchEngineLocation the folder where the search engine is
     * installed
     * @param feedBackInDialog shows feedback in dialog, otherwise the
     * feedbackDialog will be used
     * @return true if the search engine seems to be correctly installed, false
     * otherwise
     */
    public static boolean validateSearchEngineInstallation(Advocate advocate, File searchEngineLocation, boolean feedBackInDialog) {

        if (advocate == Advocate.omssa) {
            return validateSearchEngineInstallation(Advocate.omssa, OmssaclProcessBuilder.EXECUTABLE_FILE_NAME, "-ml", null, searchEngineLocation, null, false, feedBackInDialog);
        } else if (advocate == Advocate.xtandem) {
            return validateSearchEngineInstallation(Advocate.xtandem, TandemProcessBuilder.EXECUTABLE_FILE_NAME, null, null, searchEngineLocation, null, false, feedBackInDialog);
        } else if (advocate == Advocate.msgf) {
            return validateSearchEngineInstallation(Advocate.msgf, MsgfProcessBuilder.EXECUTABLE_FILE_NAME, null, null, searchEngineLocation, null, true, feedBackInDialog);
        } else if (advocate == Advocate.msAmanda) {
            String operatingSystem = System.getProperty("os.name").toLowerCase();
            String mono = null;
            if (!operatingSystem.contains("windows")) {
                mono = "mono";
            }
            return validateSearchEngineInstallation(Advocate.msAmanda, MsAmandaProcessBuilder.executableFileName, null, mono, searchEngineLocation, null, false, feedBackInDialog, msAmandaErrorMessage);
        } else if (advocate == Advocate.myriMatch) {
            return validateSearchEngineInstallation(Advocate.myriMatch, MyriMatchProcessBuilder.EXECUTABLE_FILE_NAME, null, null, searchEngineLocation, "Usage: \"myrimatch\"", false, feedBackInDialog);
        } else if (advocate == Advocate.comet) {
            return validateSearchEngineInstallation(Advocate.comet, CometProcessBuilder.EXECUTABLE_FILE_NAME, null, null, searchEngineLocation, null, false, feedBackInDialog);
        } else if (advocate == Advocate.tide) {
            return validateSearchEngineInstallation(Advocate.tide, TideSearchProcessBuilder.EXECUTABLE_FILE_NAME, null, null, searchEngineLocation, null, false, feedBackInDialog);
        } else if (advocate == Advocate.andromeda) {
            return validateSearchEngineInstallation(Advocate.andromeda, AndromedaProcessBuilder.EXECUTABLE_FILE_NAME, null, null, searchEngineLocation, null, false, feedBackInDialog);
        }

        return false;
    }

    /**
     * Tries to validate the search engine installation by running the
     * executable.
     *
     * @param advocate the search engine advocate
     * @param executable the file name of the executable
     * @param executableOption option given to the executable, null if no
     * options are to be added
     * @param executableWrapper wrapper needed to run the command, like mono for
     * MS Amanda on Windows and OSX
     * @param searchEngineLocation the folder where the search engine is
     * installed
     * @param ignorableOutput output from the tool that can be ignored, i.e.,
     * represents normal usage of the executable, can be null
     * @param isJava set to true for Java jar files, adds Java home and Java
     * options as for SearchGUI
     * @param feedBackInDialog shows feedback in dialog, otherwise the
     * feedbackDialog will be used
     * @return true if the search engine seems to be correctly installed, false
     * otherwise
     */
    private static boolean validateSearchEngineInstallation(Advocate advocate, String executable, String executableOption,
            String executableWrapper, File searchEngineLocation, String ignorableOutput, boolean isJava, boolean feedBackInDialog) {
        return validateSearchEngineInstallation(advocate, executable, executableOption, executableWrapper, searchEngineLocation, ignorableOutput, isJava, feedBackInDialog, null);
    }

    /**
     * Tries to validate the search engine installation by running the
     * executable.
     *
     * @param advocate the search engine advocate
     * @param executable the file name of the executable
     * @param executableOption option given to the executable, null if no
     * options are to be added
     * @param executableWrapper wrapper needed to run the command, like mono for
     * MS Amanda on Windows and OSX
     * @param searchEngineLocation the folder where the search engine is
     * installed
     * @param ignorableOutput output from the tool that can be ignored, i.e.,
     * represents normal usage of the executable, can be null
     * @param isJava set to true for Java jar files, adds Java home and Java
     * options as for SearchGUI
     * @param feedBackInDialog shows feedback in dialog, otherwise the
     * feedbackDialog will be used
     * @param customErrorMessage a custom error message, like tip on installing
     * Mono for MS Amanda, null uses the default message
     * @return true if the search engine seems to be correctly installed, false
     * otherwise
     */
    private static boolean validateSearchEngineInstallation(Advocate advocate, String executable, String executableOption,
            String executableWrapper, File searchEngineLocation, String ignorableOutput, boolean isJava, boolean feedBackInDialog, String customErrorMessage) {

        boolean error = false;

        if (searchEngineLocation != null) {

            try {
                ArrayList process_name_array = new ArrayList();

                // add java home
                if (isJava) {
                    UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
                    CompomicsWrapper wrapper = new CompomicsWrapper();
                    if (utilitiesUserPreferences.getSearchGuiPath() != null) {
                        ArrayList<String> javaHomeAndOptions = wrapper.getJavaHomeAndOptions(utilitiesUserPreferences.getSearchGuiPath());
                        process_name_array.add(javaHomeAndOptions.get(0)); // set java home
                    } else {
                        process_name_array.add("java");
                    }
                    process_name_array.add("-jar");
                }

                // add executable wrapper, like mono
                if (executableWrapper != null) {
                    process_name_array.add(executableWrapper);
                }

                // add the path to the executable
                process_name_array.add(searchEngineLocation + File.separator + executable);
                if (executableOption != null) {
                    process_name_array.add(executableOption);
                }

                // set up and run the process
                ProcessBuilder pb = new ProcessBuilder(process_name_array);
                pb.directory(searchEngineLocation);

                Process p = pb.start();

                InputStream stderr = p.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String errorMessage = "";

                if (br.ready()) {

                    String line = br.readLine();

                    while (line != null) {
                        System.out.println(line);
                        errorMessage += line + "\n";
                        line = br.readLine();
                        error = true;
                    }
                }

                br.close();
                isr.close();
                stderr.close();

                if (ignorableOutput != null && errorMessage.contains(ignorableOutput)) {
                    error = false;
                }

                if (error) {
                    System.out.println(advocate + " startup error: " + errorMessage);

                    if (feedBackInDialog) {
                        if (customErrorMessage != null) {
                            JOptionPane.showMessageDialog(null, JOptionEditorPane.getJOptionEditorPane(customErrorMessage),
                                    advocate + " - Startup Failed", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, JOptionEditorPane.getJOptionEditorPane(
                                    getDefaultSearchEngineStartupErrorMessage(advocate.getName())),
                                    advocate + " - Startup Failed", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                } else {
                    p.destroy();
                }

            } catch (IOException e) {

                e.printStackTrace();
                error = true;

                if (feedBackInDialog) {
                    if (customErrorMessage != null) {
                        JOptionPane.showMessageDialog(null, JOptionEditorPane.getJOptionEditorPane(customErrorMessage),
                                advocate + " - Startup Failed", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, JOptionEditorPane.getJOptionEditorPane(
                                getDefaultSearchEngineStartupErrorMessage(advocate.getName())),
                                advocate + " - Startup Failed", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(0);
            }
        }

        return !error;
    }

    /**
     * Returns the default search engine startup error message.
     *
     * @param searchEngineName the name of the search engine
     * @return the default search engine startup error message
     */
    private static String getDefaultSearchEngineStartupErrorMessage(String searchEngineName) {
        return "Make sure that " + searchEngineName + " is installed correctly and that you have selected<br>"
                + "the correct version of " + searchEngineName + " for your system. See the <a href=\"http://compomics.github.io/projects/searchgui.html#troubleshooting\">TroubleShooting</a><br>"
                + "section at the SearchGUI web page for help, and the SearchGUI<br>"
                + "log for details about the error.";
    }

    /**
     * Returns the tips of the day.
     *
     * @return the tips of the day in an ArrayList
     */
    public ArrayList<String> getTips() {

        ArrayList<String> tips;

        try {
            InputStream stream = getClass().getResource("/tips.txt").openStream();
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader b = new BufferedReader(streamReader);
            tips = new ArrayList<String>();
            String line;

            while ((line = b.readLine()) != null) {
                tips.add(line);
            }

            b.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error occurred when reading the tip of the day.", "File Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            tips = new ArrayList<String>();
        }

        return tips;
    }

    /**
     * Check for new version.
     *
     * @return true if a new version is to be downloaded
     */
    public boolean checkForNewVersion() {
        try {
            File jarFile = new File(SearchGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            MavenJarFile oldMavenJarFile = new MavenJarFile(jarFile.toURI());
            URL jarRepository = new URL("http", "genesis.ugent.be", new StringBuilder().append("/maven2/").toString());
            return CompomicsWrapper.checkForNewDeployedVersion("SearchGUI", oldMavenJarFile, jarRepository, "searchgui.ico",
                    false, true, true, Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")), true);
        } catch (UnknownHostException ex) {
            // no internet connection
            System.out.println("Checking for new version failed. No internet connection.");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Open the PeptideShaker settings dialog.
     *
     * @param openAlways if true the dialog will be opened even if PeptideShaker
     * is not enabled
     */
    private void openPeptideShakerSettings(boolean openAlways) {

        boolean checkPeptideShaker = true;

        if (peptideShakerCheckBox.isSelected()) {
            OmssaParameters omssaParameters = (OmssaParameters) identificationParameters.getSearchParameters().getIdentificationAlgorithmParameter(Advocate.omssa.getIndex());
            if (enableOmssaJCheckBox.isSelected() && !omssaParameters.getSelectedOutput().equals("OMX")) {
                JOptionPane.showMessageDialog(this, JOptionEditorPane.getJOptionEditorPane(
                        "The selected OMSSA output format is not compatible with <a href=\"http://compomics.github.io/projects/peptide-shaker.html\">PeptideShaker</a>. Please change to the<br>"
                        + "OMSSA OMX format in the Advanced Settings, or disable OMSSA or <a href=\"http://compomics.github.io/projects/peptide-shaker.html\">PeptideShaker</a>."),
                        "Format Warning", JOptionPane.ERROR_MESSAGE);
                peptideShakerCheckBox.setSelected(false);
                checkPeptideShaker = false;
            }
        }

        if (peptideShakerCheckBox.isSelected() && checkPeptideShaker || openAlways) {
            new Thread(new Runnable() {
                public void run() {
                    // check if peptideshaker is installed
                    if (utilitiesUserPreferences.getPeptideShakerPath() == null
                            || !(new File(utilitiesUserPreferences.getPeptideShakerPath()).exists())) {
                        try {
                            PeptideShakerSetupDialog peptideShakerSetupDialog = new PeptideShakerSetupDialog(SearchGUI.this, true);
                            boolean canceled = peptideShakerSetupDialog.isDialogCanceled();

                            if (!canceled) {

                                // reload the user preferences as these may have been changed by other tools
                                try {
                                    utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
                                } catch (Exception e) {
                                    JOptionPane.showMessageDialog(null, "An error occurred when reading the user preferences.", "File Error", JOptionPane.ERROR_MESSAGE);
                                    e.printStackTrace();
                                }

                                editPeptideShakerSettings();
                            } else {
                                peptideShakerCheckBox.setSelected(false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        editPeptideShakerSettings();
                    }
                }
            }, "PeptideShakerDownload").start();
        }
    }

    /**
     * Opens a dialog allowing the edition of the ProteoWizard installation
     * folder.
     *
     * @return true of the installation is now set
     */
    public boolean editProteoWizardInstallation() {

        boolean canceled = false;

        try {
            ProteoWizardSetupDialog proteoWizardSetupDialog = new ProteoWizardSetupDialog(this, true);
            canceled = proteoWizardSetupDialog.isDialogCanceled();

            if (!canceled) {

                // reload the user preferences as these may have been changed by other tools
                try {
                    utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "An error occurred when reading the user preferences.", "File Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            } else {
                msconvertCheckBox.setSelected(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return !canceled;
    }

    /**
     * Check that the ProteoWizard folder is set.
     */
    private void checkProteoWizard() {
        if (!rawFiles.isEmpty() && utilitiesUserPreferences.getProteoWizardPath() == null) {
            boolean folderSet = editProteoWizardInstallation();
            if (!folderSet) {
                JOptionPane.showMessageDialog(this, "ProteoWizard folder not set. Raw file(s) not selected.", "Raw File Error", JOptionPane.WARNING_MESSAGE);
                rawFiles.clear();
            }
        }
    }

    /**
     * Enable/disable the msconvert options.
     */
    private void enableMsConvertPanel() {
        msconvertSettingsButton.setEnabled(!rawFiles.isEmpty());
        msconvertCheckBox.setEnabled(!rawFiles.isEmpty());
        msconvertButton.setEnabled(!rawFiles.isEmpty());
        msconvertLabel.setEnabled(!rawFiles.isEmpty());
        msconvertCheckBox.setSelected(!rawFiles.isEmpty());
    }

    /**
     * Enable/disable the search engine panel.
     *
     * @param enable if true, the panel is enabled
     */
    private void enableSearchEnginePanel(boolean enable) {
        xtandemSettingsButton.setEnabled(enable);
        msAmandaSettingsButton.setEnabled(enable);
        msgfSettingsButton.setEnabled(enable);
        omssaSettingsButton.setEnabled(enable);
        tideSettingsButton.setEnabled(enable);
        peptideShakerSettingsButton.setEnabled(enable);
        enableXTandemJCheckBox.setEnabled(enable);

        enableMsAmandaJCheckBox.setEnabled(enable);
        enableMsgfJCheckBox.setEnabled(enable);
        enableOmssaJCheckBox.setEnabled(enable);
        enableTideJCheckBox.setEnabled(enable);
        peptideShakerCheckBox.setEnabled(enable);

        xtandemButton.setEnabled(enable);
        msAmandaButton.setEnabled(enable);
        msgfButton.setEnabled(enable);
        omssaButton.setEnabled(enable);
        tideButton.setEnabled(enable);
        xtandemButton.setEnabled(enable);
        peptideShakerButton.setEnabled(enable);

        xtandemLinkLabel.setEnabled(enable);
        msAmandaLinkLabel.setEnabled(enable);
        msgfLinkLabel.setEnabled(enable);
        omssaLinkLabel.setEnabled(enable);
        tideLinkLabel.setEnabled(enable);
        peptideShakerLabel.setEnabled(enable);

        String operatingSystem = System.getProperty("os.name").toLowerCase();

        // disable myrimatch and comet if mac
        if (!operatingSystem.contains("mac os")) {
            myriMatchSettingsButton.setEnabled(enable);
            cometSettingsButton.setEnabled(enable);
            enableMyriMatchJCheckBox.setEnabled(enable);
            enableCometJCheckBox.setEnabled(enable);
            myriMatchButton.setEnabled(enable);
            cometButton.setEnabled(enable);
            myriMatchLinkLabel.setEnabled(enable);
            cometLinkLabel.setEnabled(enable);
        }

        // disable andromeda on non-windows platforms
        if (operatingSystem.contains("windows")) {
            andromedaSettingsButton.setEnabled(enable);
            enableAndromedaJCheckBox.setEnabled(enable);
            andromedaButton.setEnabled(enable);
            andromedaLinkLabel.setEnabled(enable);
        }
    }
}
