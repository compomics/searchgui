package eu.isas.searchgui.gui;

import com.compomics.software.CompomicsWrapper;
import static com.compomics.software.autoupdater.DownloadLatestZipFromRepo.downloadLatestZipFromRepo;
import com.compomics.software.autoupdater.GUIFileDAO;
import com.compomics.software.autoupdater.MavenJarFile;
import com.compomics.software.autoupdater.WebDAO;
import com.compomics.software.dialogs.JavaSettingsDialog;
import com.compomics.software.dialogs.PeptideShakerSetupDialog;
import com.compomics.util.Util;
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationParametersFactory;
import com.compomics.util.gui.parameters.IdentificationParametersSelectionDialog;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import java.awt.Color;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.stream.XMLStreamException;

/**
 * A dialog for editing the PeptideShaker settings required when starting
 * PeptideShaker from SearchGUI.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class PeptideShakerSettingsDialog extends javax.swing.JDialog {

    /**
     * The SearchGUI parent.
     */
    private SearchGUI searchGUI;
    /**
     * The list of Mascot dat files.
     */
    private ArrayList<File> mascotDatFiles = new ArrayList<File>();
    /**
     * Boolean indicating whether the user canceled the action.
     */
    private boolean canceled = false;
    /**
     * The progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * The identification parameters.
     */
    private IdentificationParameters identificationParameters;
    /**
     * The identification parameters file.
     */
    private File identificationParametersFile;

    /**
     * Creates a new PeptideShakerSettingsDialog.
     *
     * @param searchGUI the SearchGUI parent
     * @param identificationParameters the identification preferences
     * @param identificationParametersFile the identification parameters file
     * @param modal if the dialog is to be modal
     * @param mascotFiles the mascot dat files
     */
    public PeptideShakerSettingsDialog(SearchGUI searchGUI, IdentificationParameters identificationParameters, File identificationParametersFile, boolean modal, ArrayList<File> mascotFiles) {
        super(searchGUI, modal);
        this.searchGUI = searchGUI;

        initComponents();

        // check for 64 bit java and for at least 4 gb memory 
        boolean java64bit = CompomicsWrapper.is64BitJava();
        boolean memoryOk = (searchGUI.getUtilitiesUserPreferences().getMemoryPreference() >= 4000);
        String javaVersion = System.getProperty("java.version");
        boolean javaVersionWarning = javaVersion.startsWith("1.5") || javaVersion.startsWith("1.6");
        if (java64bit && memoryOk && !javaVersionWarning) {
            lowMemoryWarningLabel.setVisible(false);
        }
        if (javaVersionWarning) {
            lowMemoryWarningLabel.setText("<html><u>Java Version Warning!</u>");
        }

        projectNameIdTxt.setText(searchGUI.getSearchHandler().getExperimentLabel());
        sampleNameIdtxt.setText(searchGUI.getSearchHandler().getSampleLabel());
        replicateNumberIdtxt.setText(searchGUI.getSearchHandler().getReplicateNumber().toString());

        // make sure that long strings are handled correctly
        peptideShakerInstallationJTextField.setColumns(1);
        outputFileTextField.setColumns(1);
        projectNameIdTxt.setColumns(1);
        sampleNameIdtxt.setColumns(1);

        mascotDatFiles.addAll(mascotFiles);

        if (mascotDatFiles.isEmpty()) {
            mascotFilesTextField.setText(null);
        } else if (mascotDatFiles.size() == 1) {
            mascotFilesTextField.setText(mascotDatFiles.get(0).getAbsolutePath());
        } else {
            mascotFilesTextField.setText(mascotDatFiles.size() + " file(s) selected");
        }

        if (searchGUI.getSearchHandler().getPeptideShakerFile() != null) {
            outputFileTextField.setText(searchGUI.getSearchHandler().getPeptideShakerFile().getAbsolutePath());
        }

        UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();

        // display the current peptide shaker path
        if (utilitiesUserPreferences != null) {
            peptideShakerInstallationJTextField.setText(utilitiesUserPreferences.getPeptideShakerPath());
            String peptideShakerJarPath = utilitiesUserPreferences.getPeptideShakerPath();
            if (peptideShakerJarPath != null && peptideShakerJarPath.lastIndexOf("-beta") == -1 && new File(peptideShakerJarPath).exists()) {
                // check the peptide shaker version
                boolean newVersion = checkForNewVersion(peptideShakerJarPath);

                if (newVersion) {
                    int option = JOptionPane.showConfirmDialog(null,
                            "A newer version of PeptideShaker is available.\n"
                            + "Do you want to update?",
                            "Update Available",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        boolean success = downloadPeptideShaker();

                        if (success) {
                            utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
                            peptideShakerInstallationJTextField.setText(utilitiesUserPreferences.getPeptideShakerPath());
                        }
                    } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                        dispose();
                        return;
                    }
                }
            }
        }

        this.identificationParameters = identificationParameters;
        this.identificationParametersFile = identificationParametersFile;

        validateInput();

        setLocationRelativeTo(searchGUI);
        setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundPanel = new javax.swing.JPanel();
        projectDetailsPanel = new javax.swing.JPanel();
        replicateNumberIdtxt = new javax.swing.JTextField();
        projectNameIdTxt = new javax.swing.JTextField();
        replicateLabel = new javax.swing.JLabel();
        sampleNameLabel = new javax.swing.JLabel();
        projectReferenceLabel = new javax.swing.JLabel();
        sampleNameIdtxt = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        fileNamePanel = new javax.swing.JPanel();
        outputFileLabel = new javax.swing.JLabel();
        outputFileTextField = new javax.swing.JTextField();
        editOutputButton = new javax.swing.JButton();
        peptideShakerInstallationPanel = new javax.swing.JPanel();
        peptideShakerInstallationJTextField = new javax.swing.JTextField();
        editPeptideShakerLocationButton = new javax.swing.JButton();
        peptideShakerLocationLabel = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        advancedPanel = new javax.swing.JPanel();
        mascotFilesLabel = new javax.swing.JLabel();
        mascotFilesTextField = new javax.swing.JTextField();
        browseMascotFilesButton = new javax.swing.JButton();
        clearMascotFilesButton = new javax.swing.JButton();
        identificationSettingsLbl = new javax.swing.JLabel();
        identificationSettingsTxt = new javax.swing.JTextField();
        editIdentificationSettingsButton = new javax.swing.JButton();
        projectSettingsLbl = new javax.swing.JLabel();
        projectSettingsTxt = new javax.swing.JTextField();
        editProjectSettingsButton = new javax.swing.JButton();
        openDialogHelpJButton = new javax.swing.JButton();
        lowMemoryWarningLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PeptideShaker Settings");
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        projectDetailsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Project Details"));
        projectDetailsPanel.setOpaque(false);

        replicateNumberIdtxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        replicateNumberIdtxt.setText("0");
        replicateNumberIdtxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                replicateNumberIdtxtKeyReleased(evt);
            }
        });

        projectNameIdTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        projectNameIdTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                projectNameIdTxtKeyReleased(evt);
            }
        });

        replicateLabel.setText("Replicate");

        sampleNameLabel.setText("Sample Name");

        projectReferenceLabel.setText("Project Name");

        sampleNameIdtxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        sampleNameIdtxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sampleNameIdtxtKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout projectDetailsPanelLayout = new javax.swing.GroupLayout(projectDetailsPanel);
        projectDetailsPanel.setLayout(projectDetailsPanelLayout);
        projectDetailsPanelLayout.setHorizontalGroup(
            projectDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(projectDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(projectDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(projectReferenceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sampleNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(projectDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectNameIdTxt)
                    .addComponent(sampleNameIdtxt))
                .addGap(18, 18, 18)
                .addComponent(replicateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(replicateNumberIdtxt, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        projectDetailsPanelLayout.setVerticalGroup(
            projectDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(projectDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(projectDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectNameIdTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectReferenceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(projectDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sampleNameIdtxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(replicateNumberIdtxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(replicateLabel)
                    .addComponent(sampleNameLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        okButton.setText("OK");
        okButton.setEnabled(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        fileNamePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));
        fileNamePanel.setOpaque(false);

        outputFileLabel.setText("Output File");

        outputFileTextField.setEditable(false);

        editOutputButton.setText("Browse");
        editOutputButton.setToolTipText("The file where the output will be stored.");
        editOutputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editOutputButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fileNamePanelLayout = new javax.swing.GroupLayout(fileNamePanel);
        fileNamePanel.setLayout(fileNamePanelLayout);
        fileNamePanelLayout.setHorizontalGroup(
            fileNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fileNamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(outputFileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputFileTextField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editOutputButton, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        fileNamePanelLayout.setVerticalGroup(
            fileNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileNamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fileNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputFileLabel)
                    .addComponent(outputFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editOutputButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        peptideShakerInstallationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PeptideShaker"));
        peptideShakerInstallationPanel.setOpaque(false);

        peptideShakerInstallationJTextField.setEditable(false);
        peptideShakerInstallationJTextField.setToolTipText("The folder containing the PeptideShaker jar file.");

        editPeptideShakerLocationButton.setText("Edit");
        editPeptideShakerLocationButton.setToolTipText("The folder containing the PeptideShaker jar file.");
        editPeptideShakerLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editPeptideShakerLocationButtonActionPerformed(evt);
            }
        });

        peptideShakerLocationLabel.setText("Location");

        javax.swing.GroupLayout peptideShakerInstallationPanelLayout = new javax.swing.GroupLayout(peptideShakerInstallationPanel);
        peptideShakerInstallationPanel.setLayout(peptideShakerInstallationPanelLayout);
        peptideShakerInstallationPanelLayout.setHorizontalGroup(
            peptideShakerInstallationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, peptideShakerInstallationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(peptideShakerLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(peptideShakerInstallationJTextField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editPeptideShakerLocationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        peptideShakerInstallationPanelLayout.setVerticalGroup(
            peptideShakerInstallationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peptideShakerInstallationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(peptideShakerInstallationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(peptideShakerInstallationJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editPeptideShakerLocationButton)
                    .addComponent(peptideShakerLocationLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        advancedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Project Settings (see help for details)"));
        advancedPanel.setOpaque(false);

        mascotFilesLabel.setText("Mascot Files");

        mascotFilesTextField.setEditable(false);

        browseMascotFilesButton.setText("Browse");
        browseMascotFilesButton.setToolTipText("The file where the output will be stored.");
        browseMascotFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseMascotFilesButtonActionPerformed(evt);
            }
        });

        clearMascotFilesButton.setText("Clear");
        clearMascotFilesButton.setToolTipText("The file where the output will be stored.");
        clearMascotFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearMascotFilesButtonActionPerformed(evt);
            }
        });

        identificationSettingsLbl.setText("Identification");

        identificationSettingsTxt.setEditable(false);
        identificationSettingsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        identificationSettingsTxt.setText("Default");
        identificationSettingsTxt.setToolTipText("Minimum Peptide Length");

        editIdentificationSettingsButton.setText("Edit");
        editIdentificationSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editIdentificationSettingsButtonActionPerformed(evt);
            }
        });

        projectSettingsLbl.setText("Project");

        projectSettingsTxt.setEditable(false);
        projectSettingsTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        projectSettingsTxt.setText("Default");
        projectSettingsTxt.setToolTipText("Minimum Peptide Length");

        editProjectSettingsButton.setText("Edit");
        editProjectSettingsButton.setEnabled(false);
        editProjectSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProjectSettingsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout advancedPanelLayout = new javax.swing.GroupLayout(advancedPanel);
        advancedPanel.setLayout(advancedPanelLayout);
        advancedPanelLayout.setHorizontalGroup(
            advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(projectSettingsLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(identificationSettingsLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mascotFilesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(identificationSettingsTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                    .addComponent(mascotFilesTextField)
                    .addComponent(projectSettingsTxt, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(advancedPanelLayout.createSequentialGroup()
                        .addComponent(browseMascotFilesButton, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearMascotFilesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(editProjectSettingsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editIdentificationSettingsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        advancedPanelLayout.setVerticalGroup(
            advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(identificationSettingsLbl)
                    .addComponent(identificationSettingsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editIdentificationSettingsButton))
                .addGap(7, 7, 7)
                .addGroup(advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectSettingsLbl)
                    .addComponent(projectSettingsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editProjectSettingsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mascotFilesLabel)
                    .addComponent(mascotFilesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseMascotFilesButton)
                    .addComponent(clearMascotFilesButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        openDialogHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        openDialogHelpJButton.setToolTipText("Help");
        openDialogHelpJButton.setBorder(null);
        openDialogHelpJButton.setBorderPainted(false);
        openDialogHelpJButton.setContentAreaFilled(false);
        openDialogHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                openDialogHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                openDialogHelpJButtonMouseExited(evt);
            }
        });
        openDialogHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDialogHelpJButtonActionPerformed(evt);
            }
        });

        lowMemoryWarningLabel.setFont(lowMemoryWarningLabel.getFont().deriveFont(lowMemoryWarningLabel.getFont().getStyle() | java.awt.Font.BOLD));
        lowMemoryWarningLabel.setForeground(new java.awt.Color(255, 0, 0));
        lowMemoryWarningLabel.setText("<html><u>Low Memory Warning!</u>");
        lowMemoryWarningLabel.setToolTipText("Click to see details");
        lowMemoryWarningLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        lowMemoryWarningLabel.setIconTextGap(-4);
        lowMemoryWarningLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lowMemoryWarningLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lowMemoryWarningLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                lowMemoryWarningLabelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(openDialogHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lowMemoryWarningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(projectDetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(peptideShakerInstallationPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fileNamePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(advancedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(peptideShakerInstallationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectDetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileNamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(advancedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(okButton)
                    .addComponent(cancelButton)
                    .addComponent(openDialogHelpJButton)
                    .addComponent(lowMemoryWarningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Open the dialog to edit the PeptideShaker location.
     *
     * @param evt
     */
    private void editPeptideShakerLocationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editPeptideShakerLocationButtonActionPerformed
        try {
            new PeptideShakerSetupDialog(searchGUI, true);

            UtilitiesUserPreferences utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();

            // display the current peptide shaker path
            if (utilitiesUserPreferences != null) {
                peptideShakerInstallationJTextField.setText(utilitiesUserPreferences.getPeptideShakerPath());
                //lastSelectedFolder = utilitiesUserPreferences.getPeptideShakerPath();
            }

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "File not found.", "File Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "File error.", "File Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "File not found.", "File Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        validateInput();
    }//GEN-LAST:event_editPeptideShakerLocationButtonActionPerformed

    /**
     * Lets the user select the file to use for saving the PeptideShaker
     * project.
     *
     * @param evt
     */
    private void editOutputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editOutputButtonActionPerformed

        if (new File(outputFileTextField.getText()).getParentFile() != null) {
            searchGUI.getLastSelectedFolder().setLastSelectedFolder(new File(outputFileTextField.getText()).getParentFile().getAbsolutePath());
        }

        File selectedFile = Util.getUserSelectedFile(this, ".cpsx", "Compomics Peptide Shaker format (*.cpsx)", "Select PeptideShaker Output",
                searchGUI.getLastSelectedFolder().getLastSelectedFolder(), "PeptideShaker_output.cpsx", false);

        if (selectedFile != null) {
            if (!selectedFile.getName().endsWith(".cpsx")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".cpsx");
            }

            outputFileTextField.setText(selectedFile.getAbsolutePath());
        }

        validateInput();
    }//GEN-LAST:event_editOutputButtonActionPerformed

    /**
     * Close the dialog without saving.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Saves the settings and closes the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Open a file chooser where the user can select the Mascot dat files to
     * merge with the SearchGUI results in PeptideShaker.
     *
     * @param evt
     */
    private void browseMascotFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseMascotFilesButtonActionPerformed

        JFileChooser fileChooser = new JFileChooser(searchGUI.getLastSelectedFolder().getLastSelectedFolder());
        fileChooser.setDialogTitle("Select Mascot Result File(s)");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File myFile) {

                return myFile.getName().toLowerCase().endsWith("dat") || myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Supported formats: Mascot (.dat)";
            }
        };

        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showDialog(this.getParent(), "Select");

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            for (File newFile : fileChooser.getSelectedFiles()) {
                if (newFile.isDirectory()) {
                    File[] tempFiles = newFile.listFiles();
                    for (File file : tempFiles) {
                        if (file.getName().toLowerCase().endsWith("dat")) {
                            mascotDatFiles.add(file);
                        }
                    }
                } else {
                    mascotDatFiles.add(newFile);
                }
                searchGUI.getLastSelectedFolder().setLastSelectedFolder(newFile.getAbsolutePath());
            }
        }

        if (mascotDatFiles.isEmpty()) {
            mascotFilesTextField.setText(null);
        } else if (mascotDatFiles.size() == 1) {
            mascotFilesTextField.setText(mascotDatFiles.get(0).getAbsolutePath());
        } else {
            mascotFilesTextField.setText(mascotDatFiles.size() + " file(s) selected");
        }
    }//GEN-LAST:event_browseMascotFilesButtonActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void openDialogHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void openDialogHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonMouseExited

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void openDialogHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDialogHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(searchGUI, getClass().getResource("/helpFiles/PeptideShakerSettingsDialog.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")),
                "PeptideShaker - Help", 500, 50);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openDialogHelpJButtonActionPerformed

    /**
     * Clear the list of Mascot dat files.
     *
     * @param evt
     */
    private void clearMascotFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearMascotFilesButtonActionPerformed
        mascotDatFiles = new ArrayList<File>();
        mascotFilesTextField.setText(null);
    }//GEN-LAST:event_clearMascotFilesButtonActionPerformed

    /**
     * Open the ImportSettingsDialog.
     *
     * @param evt
     */
    private void editIdentificationSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editIdentificationSettingsButtonActionPerformed
        IdentificationParametersSelectionDialog identificationParametersSelectionDialog = new IdentificationParametersSelectionDialog(searchGUI, this, identificationParameters, IdentificationParametersSelectionDialog.StartupMode.advanced, searchGUI.getSearchHandler().getConfigurationFile(),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")), searchGUI.getLastSelectedFolder(), null, true);
        if (!identificationParametersSelectionDialog.isCanceled()) {
            IdentificationParameters tempIdentificationParameters = identificationParametersSelectionDialog.getIdentificationParameters();
            setIdentificationParameters(tempIdentificationParameters);
            identificationParametersFile = IdentificationParametersFactory.getIdentificationParametersFile(tempIdentificationParameters.getName());
        }
    }//GEN-LAST:event_editIdentificationSettingsButtonActionPerformed

    /**
     * Open the ProcessingPreferencesDialog.
     *
     * @param evt
     */
    private void editProjectSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProjectSettingsButtonActionPerformed


    }//GEN-LAST:event_editProjectSettingsButtonActionPerformed

    /**
     * Validate if the user input.
     *
     * @param evt
     */
    private void sampleNameIdtxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sampleNameIdtxtKeyReleased
        validateInput();
    }//GEN-LAST:event_sampleNameIdtxtKeyReleased

    /**
     * Validate if the user input.
     *
     * @param evt
     */
    private void projectNameIdTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_projectNameIdTxtKeyReleased
        validateInput();
    }//GEN-LAST:event_projectNameIdTxtKeyReleased

    /**
     * Validate if the user input.
     *
     * @param evt
     */
    private void replicateNumberIdtxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_replicateNumberIdtxtKeyReleased
        validateInput();
    }//GEN-LAST:event_replicateNumberIdtxtKeyReleased

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void lowMemoryWarningLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lowMemoryWarningLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_lowMemoryWarningLabelMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void lowMemoryWarningLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lowMemoryWarningLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_lowMemoryWarningLabelMouseExited

    /**
     * Open the memory warning help dialog.
     *
     * @param evt
     */
    private void lowMemoryWarningLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lowMemoryWarningLabelMouseReleased
        new JavaSettingsDialog(searchGUI, searchGUI, null, "SearchGUI", true);
    }//GEN-LAST:event_lowMemoryWarningLabelMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedPanel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton browseMascotFilesButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton clearMascotFilesButton;
    private javax.swing.JButton editIdentificationSettingsButton;
    private javax.swing.JButton editOutputButton;
    private javax.swing.JButton editPeptideShakerLocationButton;
    private javax.swing.JButton editProjectSettingsButton;
    private javax.swing.JPanel fileNamePanel;
    private javax.swing.JLabel identificationSettingsLbl;
    private javax.swing.JTextField identificationSettingsTxt;
    private javax.swing.JLabel lowMemoryWarningLabel;
    private javax.swing.JLabel mascotFilesLabel;
    private javax.swing.JTextField mascotFilesTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openDialogHelpJButton;
    private javax.swing.JLabel outputFileLabel;
    private javax.swing.JTextField outputFileTextField;
    private javax.swing.JTextField peptideShakerInstallationJTextField;
    private javax.swing.JPanel peptideShakerInstallationPanel;
    private javax.swing.JLabel peptideShakerLocationLabel;
    private javax.swing.JPanel projectDetailsPanel;
    private javax.swing.JTextField projectNameIdTxt;
    private javax.swing.JLabel projectReferenceLabel;
    private javax.swing.JLabel projectSettingsLbl;
    private javax.swing.JTextField projectSettingsTxt;
    private javax.swing.JLabel replicateLabel;
    private javax.swing.JTextField replicateNumberIdtxt;
    private javax.swing.JTextField sampleNameIdtxt;
    private javax.swing.JLabel sampleNameLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Validated if the user input is valid.
     *
     * @return true of valid
     */
    private boolean validateInput() {

        peptideShakerLocationLabel.setForeground(Color.black);
        projectReferenceLabel.setForeground(Color.black);
        sampleNameLabel.setForeground(Color.black);
        replicateLabel.setForeground(Color.black);
        outputFileLabel.setForeground(Color.black);

        peptideShakerLocationLabel.setToolTipText(null);
        projectReferenceLabel.setToolTipText(null);
        sampleNameLabel.setToolTipText(null);
        replicateLabel.setToolTipText(null);
        outputFileLabel.setToolTipText(null);

        boolean valid = true;

        if (peptideShakerInstallationJTextField.getText().trim().length() == 0) {
            valid = false;
            peptideShakerLocationLabel.setForeground(Color.red);
            peptideShakerLocationLabel.setToolTipText("Please locate the PeptideShaker jar file.");
        }

        if (!new File(peptideShakerInstallationJTextField.getText().trim()).exists()) {
            valid = false;
            peptideShakerLocationLabel.setForeground(Color.red);
            peptideShakerLocationLabel.setToolTipText("PeptideShaker jar file not found.");
        }
        if (projectNameIdTxt.getText().trim().length() == 0) {
            valid = false;
            projectReferenceLabel.setForeground(Color.red);
            projectReferenceLabel.setToolTipText("Please provide a project name.");
        }
        if (sampleNameIdtxt.getText().trim().length() == 0) {
            valid = false;
            sampleNameLabel.setForeground(Color.red);
            sampleNameLabel.setToolTipText("Please provide a sample name.");
        }
        if (replicateNumberIdtxt.getText().trim().length() == 0) {
            valid = false;
            sampleNameLabel.setForeground(Color.red);
            sampleNameLabel.setToolTipText("Please provide a replicate number.");
        }
        try {
            new Integer(replicateNumberIdtxt.getText());
        } catch (NumberFormatException e) {
            valid = false;
            replicateLabel.setForeground(Color.red);
            replicateLabel.setToolTipText("Replicate has to be a number!");
        }
        if (outputFileTextField.getText().trim().length() == 0) {
            valid = false;
            outputFileLabel.setForeground(Color.red);
            outputFileLabel.setToolTipText("Please provide an output file.");
        }

        okButton.setEnabled(valid);

        return valid;
    }

    /**
     * Sets the search parameters in the identification parameters and updates
     * the GUI.
     *
     * @param searchParameters new search parameters
     */
    private void setIdentificationParameters(IdentificationParameters identificationParameters) {
        this.identificationParameters = identificationParameters;
        identificationSettingsTxt.setText(identificationParameters.getName());
    }

    /**
     * Returns the Mascot files.
     *
     * @return the mascot files
     */
    public ArrayList<File> getMascotFiles() {
        return mascotDatFiles;
    }

    /**
     * Indicates whether the user pushed on cancel.
     *
     * @return a boolean indicating whether the user pushed on cancel
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Returns the project name as set by the user.
     *
     * @return the project name as set by the user
     */
    public String getProjectName() {
        return projectNameIdTxt.getText();
    }

    /**
     * Returns the sample name as set by the user.
     *
     * @return the sample name as set by the user
     */
    public String getSampleName() {
        return sampleNameIdtxt.getText();
    }

    /**
     * Returns the replicate number as set by the user.
     *
     * @return the replicate number as set by the user
     */
    public Integer getReplicateNumber() {
        return new Integer(replicateNumberIdtxt.getText());
    }

    /**
     * Returns the PeptideShaker output file as set by the user.
     *
     * @return the PeptideShaker output file as set by the user
     */
    public File getPeptideShakerOutputFile() {
        return new File(outputFileTextField.getText());
    }

    /**
     * Returns the identification parameters as set by the user.
     *
     * @return the identification parameters as set by the user
     */
    public IdentificationParameters getIdentificationParameters() {
        return identificationParameters;
    }

    /**
     * Returns the identification parameters file selected by the user.
     *
     * @return the identification parameters file selected by the user
     */
    public File getIdentificationParametersFile() {
        return identificationParametersFile;
    }

    /**
     * Check for new version.
     *
     * @param peptideShakerJarPath the path to the PeptideShaker jar file
     * @return true if a new version is available
     */
    public boolean checkForNewVersion(String peptideShakerJarPath) {
        try {
            File jarFile = new File(peptideShakerJarPath);
            MavenJarFile oldMavenJarFile = new MavenJarFile(jarFile.toURI());
            URL jarRepository = new URL("http", "genesis.ugent.be", new StringBuilder().append("/maven2/").toString());
            return WebDAO.newVersionReleased(oldMavenJarFile, jarRepository);
        } catch (UnknownHostException ex) {
            // no internet connection
            System.out.println("Checking for new version failed. No internet connection.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Download PeptideShaker.
     *
     * @return true if not canceled
     */
    public boolean downloadPeptideShaker() {

        boolean firstTimeInstall = true;
        String installPath = null;

        if (searchGUI.getUtilitiesUserPreferences().getPeptideShakerPath() != null) {
            if (new File(searchGUI.getUtilitiesUserPreferences().getPeptideShakerPath()).getParentFile() != null
                    && new File(searchGUI.getUtilitiesUserPreferences().getPeptideShakerPath()).getParentFile().getParentFile() != null) {
                installPath = new File(searchGUI.getUtilitiesUserPreferences().getPeptideShakerPath()).getParentFile().getParent();
            }
        }

        final File downloadFolder;

        if (installPath == null) {
            installPath = "user.home";
            downloadFolder = Util.getUserSelectedFolder(this, "Select PeptideShaker Folder", installPath, "PeptideShaker Folder", "Select", false);
        } else {
            firstTimeInstall = false;
            downloadFolder = new File(installPath);
        }

        final boolean finalFirstTimeInstall = firstTimeInstall;

        if (downloadFolder != null) {

            progressDialog = new ProgressDialogX(searchGUI,
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                    Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")),
                    true);

            progressDialog.setPrimaryProgressCounterIndeterminate(true);
            progressDialog.setTitle("Downloading PeptideShaker. Please Wait...");

            new Thread(new Runnable() {
                public void run() {
                    try {
                        progressDialog.setVisible(true);
                    } catch (IndexOutOfBoundsException e) {
                        // ignore
                    }
                }
            }, "ProgressDialog").start();

            Thread thread = new Thread("DownloadThread") {
                @Override
                public void run() {
                    try {
                        URL jarRepository = new URL("http", "genesis.ugent.be", new StringBuilder().append("/maven2/").toString());
                        if (finalFirstTimeInstall) {
                            downloadLatestZipFromRepo(downloadFolder, "PeptideShaker", "eu.isas.peptideshaker", "PeptideShaker", "peptide-shaker.ico",
                                    null, jarRepository, false, true, new GUIFileDAO(), progressDialog);
                        } else {
                            downloadLatestZipFromRepo(new File(searchGUI.getUtilitiesUserPreferences().getPeptideShakerPath()).toURI().toURL(), "PeptideShaker", false,
                                    "peptide-shaker.ico", null, jarRepository, false, true, new GUIFileDAO(), progressDialog);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (progressDialog.isRunCanceled()) {
                progressDialog.setRunFinished();
                return false;
            } else {
                if (!progressDialog.isRunFinished()) {
                    progressDialog.setRunFinished();
                }
            }

            return true;
        }

        return false;
    }
}
