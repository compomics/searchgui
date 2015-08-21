package eu.isas.searchgui.gui;

import com.compomics.util.experiment.identification.Advocate;
import eu.isas.searchgui.processbuilders.AndromedaProcessBuilder;
import eu.isas.searchgui.processbuilders.CometProcessBuilder;
import eu.isas.searchgui.processbuilders.MakeblastdbProcessBuilder;
import eu.isas.searchgui.processbuilders.MsAmandaProcessBuilder;
import eu.isas.searchgui.processbuilders.MsgfProcessBuilder;
import eu.isas.searchgui.processbuilders.MyriMatchProcessBuilder;
import eu.isas.searchgui.processbuilders.OmssaclProcessBuilder;
import eu.isas.searchgui.processbuilders.TandemProcessBuilder;
import eu.isas.searchgui.processbuilders.TideSearchProcessBuilder;
import java.awt.Color;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * The search engines settings dialog.
 *
 * @author Harald Barsnes
 */
public class SearchEnginesSettingsDialog extends javax.swing.JDialog {

    /**
     * Convenience Array for the search engines combo boxes.
     */
    private static final String[] enableDisable = {"Enabled", "Disabled"};
    /**
     * The SearchGUI main frame.
     */
    private SearchGUI searchGUIMainFrame;
    /**
     * True when the GUI has loaded.
     */
    private boolean guiLoaded = false;
    /**
     * True if the OMSSA installation is valid.
     */
    private boolean omssaValid = true;
    /**
     * True if the X!Tandem installation is valid.
     */
    private boolean tandemValid = true;
    /**
     * True if the MS-GF+ installation is valid.
     */
    private boolean msgfValid = true;
    /**
     * True if the MS Amanda installation is valid.
     */
    private boolean msAmandaValid = true;
    /**
     * True if the MyriMatch installation is valid.
     */
    private boolean myriMatchValid = true;
    /**
     * True if the Comet installation is valid.
     */
    private boolean cometValid = true;
    /**
     * True if the Tide installation is valid.
     */
    private boolean tideValid = true;
    /**
     * True if the Andromeda installation is valid.
     */
    private boolean andromedaValid = true;

    /**
     * Creates a new SearchEnginesSettingsDialog.
     *
     * @param searchGUIMainFrame reference to the SearchGUI main frame
     * @param modal if the dialog is to be modal
     */
    public SearchEnginesSettingsDialog(SearchGUI searchGUIMainFrame, boolean modal) {
        super(searchGUIMainFrame, modal);
        this.searchGUIMainFrame = searchGUIMainFrame;
        initComponents();
        loadSearchEngines();
        validateInput(false);
        guiLoaded = true;
        setLocationRelativeTo(searchGUIMainFrame);
        setVisible(true);
    }

    /**
     * Loads the search engines location.
     */
    private void loadSearchEngines() {

        if (searchGUIMainFrame.getSearchHandler().getOmssaLocation() != null) {
            omssaLocationTxt.setText(searchGUIMainFrame.getSearchHandler().getOmssaLocation().getAbsolutePath());
            if (searchGUIMainFrame.getSearchHandler().isOmssaEnabled()) {
                enableOmssaCombo.setSelectedItem("Enabled");
            } else {
                enableOmssaCombo.setSelectedItem("Disabled");
            }
        } else {
            omssaLocationTxt.setText("");
            enableOmssaCombo.setSelectedItem("Disabled");
        }

        if (searchGUIMainFrame.getSearchHandler().getXtandemLocation() != null) {
            xTandemLocationTxt.setText(searchGUIMainFrame.getSearchHandler().getXtandemLocation().getAbsolutePath());
            if (searchGUIMainFrame.getSearchHandler().isXtandemEnabled()) {
                enableXTandemCombo.setSelectedItem("Enabled");
            } else {
                enableXTandemCombo.setSelectedItem("Disabled");
            }
        } else {
            xTandemLocationTxt.setText("");
            enableXTandemCombo.setSelectedItem("Disabled");
        }

        if (searchGUIMainFrame.getSearchHandler().getMsgfLocation() != null) {
            msgfLocationTxt.setText(searchGUIMainFrame.getSearchHandler().getMsgfLocation().getAbsolutePath());
            if (searchGUIMainFrame.getSearchHandler().isMsgfEnabled()) {
                enableMsgfCombo.setSelectedItem("Enabled");
            } else {
                enableMsgfCombo.setSelectedItem("Disabled");
            }
        } else {
            msgfLocationTxt.setText("");
            enableMsgfCombo.setSelectedItem("Disabled");
        }

        if (searchGUIMainFrame.getSearchHandler().getMsAmandaLocation() != null) {
            msAmandaLocationTxt.setText(searchGUIMainFrame.getSearchHandler().getMsAmandaLocation().getAbsolutePath());
            if (searchGUIMainFrame.getSearchHandler().isMsAmandaEnabled()) {
                enableMsAmandaCombo.setSelectedItem("Enabled");
            } else {
                enableMsAmandaCombo.setSelectedItem("Disabled");
            }
        } else {
            msAmandaLocationTxt.setText("");
            enableMsAmandaCombo.setSelectedItem("Disabled");
        }

        if (searchGUIMainFrame.getSearchHandler().getMyriMatchLocation() != null) {
            myriMatchLocationTxt.setText(searchGUIMainFrame.getSearchHandler().getMyriMatchLocation().getAbsolutePath());
            if (searchGUIMainFrame.getSearchHandler().isMyriMatchEnabled()) {
                enableMyriMatchCombo.setSelectedItem("Enabled");
            } else {
                enableMyriMatchCombo.setSelectedItem("Disabled");
            }
        } else {
            myriMatchLocationTxt.setText("");
            enableMyriMatchCombo.setSelectedItem("Disabled");
        }

        if (searchGUIMainFrame.getSearchHandler().getCometLocation() != null) {
            cometLocationTxt.setText(searchGUIMainFrame.getSearchHandler().getCometLocation().getAbsolutePath());
            if (searchGUIMainFrame.getSearchHandler().isCometEnabled()) {
                enableCometCombo.setSelectedItem("Enabled");
            } else {
                enableCometCombo.setSelectedItem("Disabled");
            }
        } else {
            cometLocationTxt.setText("");
            enableCometCombo.setSelectedItem("Disabled");
        }

        if (searchGUIMainFrame.getSearchHandler().getTideLocation() != null) {
            tideLocationTxt.setText(searchGUIMainFrame.getSearchHandler().getTideLocation().getAbsolutePath());
            if (searchGUIMainFrame.getSearchHandler().isTideEnabled()) {
                enableTideCombo.setSelectedItem("Enabled");
            } else {
                enableTideCombo.setSelectedItem("Disabled");
            }
        } else {
            tideLocationTxt.setText("");
            enableTideCombo.setSelectedItem("Disabled");
        }
        
        if (searchGUIMainFrame.getSearchHandler().getAndromedaLocation() != null) {
            andromedaLocationTxt.setText(searchGUIMainFrame.getSearchHandler().getAndromedaLocation().getAbsolutePath());
            if (searchGUIMainFrame.getSearchHandler().isAndromedaEnabled()) {
                enableAndromedaCombo.setSelectedItem("Enabled");
            } else {
                enableAndromedaCombo.setSelectedItem("Disabled");
            }
        } else {
            andromedaLocationTxt.setText("");
            enableAndromedaCombo.setSelectedItem("Disabled");
        }

        String operatingSystem = System.getProperty("os.name").toLowerCase();

        // disable myrimatch, comet and ms amanda for mac
        if (operatingSystem.contains("mac os")) {
            myriMatchLocationTxt.setText("");
            myriMatchBrowse.setEnabled(false);
            enableMyriMatchCombo.setEnabled(false);
            cometLocationTxt.setText("");
            cometBrowse.setEnabled(false);
            enableCometCombo.setEnabled(false);
            msAmandaLocationTxt.setText("");
            msAmandaBrowse.setEnabled(false);
            enableMsAmandaCombo.setEnabled(false);
        }

        if (searchGUIMainFrame.getSearchHandler().getMakeblastdbLocation() != null) {
            makeblastdbLocationTxt.setText(searchGUIMainFrame.getSearchHandler().getMakeblastdbLocation().getAbsolutePath());
        } else {
            File makeBlastDbFolder = new File(searchGUIMainFrame.getJarFilePath(), MakeblastdbProcessBuilder.getMakeblastdbFolder());
            makeblastdbLocationTxt.setText(makeBlastDbFolder.getAbsolutePath());
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

        backgroundPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        searchEnginesLocationPanel = new javax.swing.JPanel();
        xTandemLocationLabel = new javax.swing.JLabel();
        xTandemLocationTxt = new javax.swing.JTextField();
        xTandemBrowse = new javax.swing.JButton();
        enableXTandemCombo = new javax.swing.JComboBox();
        omssaLocationLabel = new javax.swing.JLabel();
        omssaLocationTxt = new javax.swing.JTextField();
        omssaBrowse = new javax.swing.JButton();
        enableOmssaCombo = new javax.swing.JComboBox();
        msgfLocationLabel = new javax.swing.JLabel();
        msgfLocationTxt = new javax.swing.JTextField();
        msgfBrowse = new javax.swing.JButton();
        enableMsgfCombo = new javax.swing.JComboBox();
        msAmandaLocationLabel = new javax.swing.JLabel();
        msAmandaLocationTxt = new javax.swing.JTextField();
        msAmandaBrowse = new javax.swing.JButton();
        enableMsAmandaCombo = new javax.swing.JComboBox();
        makeblastdbLocationLabel = new javax.swing.JLabel();
        makeblastdbLocationTxt = new javax.swing.JTextField();
        makeblastdbBrowse = new javax.swing.JButton();
        myriMatchLocationLabel = new javax.swing.JLabel();
        myriMatchLocationTxt = new javax.swing.JTextField();
        myriMatchBrowse = new javax.swing.JButton();
        enableMyriMatchCombo = new javax.swing.JComboBox();
        cometLocationLabel = new javax.swing.JLabel();
        cometLocationTxt = new javax.swing.JTextField();
        cometBrowse = new javax.swing.JButton();
        enableCometCombo = new javax.swing.JComboBox();
        tideLocationLabel = new javax.swing.JLabel();
        tideLocationTxt = new javax.swing.JTextField();
        tideBrowse = new javax.swing.JButton();
        enableTideCombo = new javax.swing.JComboBox();
        andromedaLocationLabel = new javax.swing.JLabel();
        andromedaLocationTxt = new javax.swing.JTextField();
        andromedaBrowse = new javax.swing.JButton();
        enableAndromedaCombo = new javax.swing.JComboBox();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Search Engine Settings");

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        searchEnginesLocationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Engine Folders"));
        searchEnginesLocationPanel.setOpaque(false);

        xTandemLocationLabel.setText("X!Tandem");

        xTandemLocationTxt.setEditable(false);

        xTandemBrowse.setText("Browse");
        xTandemBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xTandemBrowseActionPerformed(evt);
            }
        });

        enableXTandemCombo.setModel(new DefaultComboBoxModel(enableDisable));
        enableXTandemCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableXTandemComboActionPerformed(evt);
            }
        });

        omssaLocationLabel.setText("OMSSA");

        omssaLocationTxt.setEditable(false);

        omssaBrowse.setText("Browse");
        omssaBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                omssaBrowseActionPerformed(evt);
            }
        });

        enableOmssaCombo.setModel(new DefaultComboBoxModel(enableDisable));
        enableOmssaCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableOmssaComboActionPerformed(evt);
            }
        });

        msgfLocationLabel.setText("MS-GF+");

        msgfLocationTxt.setEditable(false);

        msgfBrowse.setText("Browse");
        msgfBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msgfBrowseActionPerformed(evt);
            }
        });

        enableMsgfCombo.setModel(new DefaultComboBoxModel(enableDisable));
        enableMsgfCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableMsgfComboActionPerformed(evt);
            }
        });

        msAmandaLocationLabel.setText("MS Amanda");

        msAmandaLocationTxt.setEditable(false);

        msAmandaBrowse.setText("Browse");
        msAmandaBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msAmandaBrowseActionPerformed(evt);
            }
        });

        enableMsAmandaCombo.setModel(new DefaultComboBoxModel(enableDisable));
        enableMsAmandaCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableMsAmandaComboActionPerformed(evt);
            }
        });

        makeblastdbLocationLabel.setText("makeblastdb");
        makeblastdbLocationLabel.setToolTipText("makeblastdb is required to use OMSSA");

        makeblastdbLocationTxt.setEditable(false);
        makeblastdbLocationTxt.setToolTipText("makeblastdb is required to use OMSSA");

        makeblastdbBrowse.setText("Browse");
        makeblastdbBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeblastdbBrowseActionPerformed(evt);
            }
        });

        myriMatchLocationLabel.setText("MyriMatch");

        myriMatchLocationTxt.setEditable(false);

        myriMatchBrowse.setText("Browse");
        myriMatchBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myriMatchBrowseActionPerformed(evt);
            }
        });

        enableMyriMatchCombo.setModel(new DefaultComboBoxModel(enableDisable));
        enableMyriMatchCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableMyriMatchComboActionPerformed(evt);
            }
        });

        cometLocationLabel.setText("Comet");

        cometLocationTxt.setEditable(false);

        cometBrowse.setText("Browse");
        cometBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cometBrowseActionPerformed(evt);
            }
        });

        enableCometCombo.setModel(new DefaultComboBoxModel(enableDisable));
        enableCometCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableCometComboActionPerformed(evt);
            }
        });

        tideLocationLabel.setText("Tide");

        tideLocationTxt.setEditable(false);

        tideBrowse.setText("Browse");
        tideBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tideBrowseActionPerformed(evt);
            }
        });

        enableTideCombo.setModel(new DefaultComboBoxModel(enableDisable));
        enableTideCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableTideComboActionPerformed(evt);
            }
        });

        andromedaLocationLabel.setText("Andromeda");

        andromedaLocationTxt.setEditable(false);

        andromedaBrowse.setText("Browse");
        andromedaBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                andromedaBrowseActionPerformed(evt);
            }
        });

        enableAndromedaCombo.setModel(new DefaultComboBoxModel(enableDisable));
        enableAndromedaCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableAndromedaComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout searchEnginesLocationPanelLayout = new javax.swing.GroupLayout(searchEnginesLocationPanel);
        searchEnginesLocationPanel.setLayout(searchEnginesLocationPanelLayout);
        searchEnginesLocationPanelLayout.setHorizontalGroup(
            searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                        .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                                .addComponent(msgfLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(msgfLocationTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(msgfBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                                .addComponent(msAmandaLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(msAmandaLocationTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(msAmandaBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                                .addComponent(xTandemLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(xTandemLocationTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(xTandemBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                                .addComponent(myriMatchLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(myriMatchLocationTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(myriMatchBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchEnginesLocationPanelLayout.createSequentialGroup()
                        .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                                .addComponent(andromedaLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(andromedaLocationTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(andromedaBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                                .addComponent(tideLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tideLocationTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tideBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                                .addComponent(cometLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cometLocationTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cometBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(omssaLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(makeblastdbLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(makeblastdbLocationTxt)
                                    .addComponent(omssaLocationTxt))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(omssaBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(makeblastdbBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(6, 6, 6)))
                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(enableAndromedaCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(enableTideCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(enableCometCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(enableOmssaCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(enableMsgfCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(enableMsAmandaCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(enableMyriMatchCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(enableXTandemCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 80, Short.MAX_VALUE))
                .addContainerGap())
        );
        searchEnginesLocationPanelLayout.setVerticalGroup(
            searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchEnginesLocationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xTandemLocationLabel)
                    .addComponent(xTandemLocationTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xTandemBrowse)
                    .addComponent(enableXTandemCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enableMyriMatchCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(myriMatchBrowse)
                    .addComponent(myriMatchLocationTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(myriMatchLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enableMsAmandaCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msAmandaBrowse)
                    .addComponent(msAmandaLocationTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msAmandaLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(msgfLocationLabel)
                    .addComponent(msgfLocationTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(enableMsgfCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msgfBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enableOmssaCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(omssaBrowse)
                    .addComponent(omssaLocationTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(omssaLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enableCometCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cometBrowse)
                    .addComponent(cometLocationTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cometLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enableTideCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tideBrowse)
                    .addComponent(tideLocationTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tideLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enableAndromedaCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(andromedaBrowse)
                    .addComponent(andromedaLocationTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(andromedaLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchEnginesLocationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(makeblastdbBrowse)
                    .addComponent(makeblastdbLocationTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(makeblastdbLocationLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        searchEnginesLocationPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {enableMsAmandaCombo, enableMsgfCombo, enableOmssaCombo, enableXTandemCombo, makeblastdbBrowse, msAmandaBrowse, msgfBrowse, omssaBrowse, xTandemBrowse});

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchEnginesLocationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchEnginesLocationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * OMSSA location. It auto-validates the OMSSA location.
     *
     * @param evt
     */
    private void omssaBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_omssaBrowseActionPerformed
        browseOMSSALocationPressed();
    }//GEN-LAST:event_omssaBrowseActionPerformed

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * X!Tandem location. It auto-validates the X!Tandem location.
     *
     * @param evt
     */
    private void xTandemBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xTandemBrowseActionPerformed
        browseXTandemLocationPressed();
    }//GEN-LAST:event_xTandemBrowseActionPerformed

    /**
     * Saves the input and closes the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        boolean valid = validateInput(true);
        if (valid) {
            searchGUIMainFrame.getSearchHandler().setOmssaLocation(getOmssaLocation());
            searchGUIMainFrame.getSearchHandler().setXtandemLocation(getXtandemLocation());
            searchGUIMainFrame.getSearchHandler().setMsgfLocation(getMsgfLocation());
            searchGUIMainFrame.getSearchHandler().setMsAmandaLocation(getMsAmandaLocation());
            searchGUIMainFrame.getSearchHandler().setMyriMatchLocation(getMyriMatchLocation());
            searchGUIMainFrame.getSearchHandler().setCometLocation(getCometLocation());
            searchGUIMainFrame.getSearchHandler().setTideLocation(getTideLocation());
            searchGUIMainFrame.getSearchHandler().setAndromedaLocation(getAndromedaLocation());
            searchGUIMainFrame.getSearchHandler().setMakeblastdbLocation(getMakeblastdbLocation());
            searchGUIMainFrame.enableSearchEngines(enableOmssaCombo.getSelectedIndex() == 0,
                    enableXTandemCombo.getSelectedIndex() == 0,
                    enableMsgfCombo.getSelectedIndex() == 0,
                    enableMsAmandaCombo.getSelectedIndex() == 0,
                    enableMyriMatchCombo.getSelectedIndex() == 0,
                    enableCometCombo.getSelectedIndex() == 0,
                    enableTideCombo.getSelectedIndex() == 0,
                    enableAndromedaCombo.getSelectedIndex() == 0);
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Close the dialog without saving.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        searchGUIMainFrame.enableSearchEngines(
                enableOmssaCombo.getSelectedIndex() == 0 && omssaValid,
                enableXTandemCombo.getSelectedIndex() == 0 && tandemValid,
                enableMsgfCombo.getSelectedIndex() == 0 && msgfValid,
                enableMsAmandaCombo.getSelectedIndex() == 0 && msAmandaValid,
                enableMyriMatchCombo.getSelectedIndex() == 0 && myriMatchValid,
                enableCometCombo.getSelectedIndex() == 0 && cometValid,
                enableTideCombo.getSelectedIndex() == 0 && tideValid,
                enableAndromedaCombo.getSelectedIndex() == 0 && andromedaValid);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Validate the search engine input.
     *
     * @param evt
     */
    private void enableOmssaComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableOmssaComboActionPerformed
        if (guiLoaded) {
            validateInput(true);
        }
    }//GEN-LAST:event_enableOmssaComboActionPerformed

    /**
     * Validate the search engine input.
     *
     * @param evt
     */
    private void enableXTandemComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableXTandemComboActionPerformed
        if (guiLoaded) {
            validateInput(true);
        }
    }//GEN-LAST:event_enableXTandemComboActionPerformed

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * MS-GF+ location. It auto-validates the MS-GF+ location.
     *
     * @param evt
     */
    private void msgfBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msgfBrowseActionPerformed
        browseMsgfLocationPressed();
    }//GEN-LAST:event_msgfBrowseActionPerformed

    /**
     * Validate the search engine input.
     *
     * @param evt
     */
    private void enableMsgfComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableMsgfComboActionPerformed
        if (guiLoaded) {
            validateInput(true);
        }
    }//GEN-LAST:event_enableMsgfComboActionPerformed

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * MS Amanda location. It auto-validates the MS Amanda location.
     *
     * @param evt
     */
    private void msAmandaBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msAmandaBrowseActionPerformed
        browseMsAmandaLocationPressed();
    }//GEN-LAST:event_msAmandaBrowseActionPerformed

    /**
     * Validate the search engine input.
     *
     * @param evt
     */
    private void enableMsAmandaComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableMsAmandaComboActionPerformed
        if (guiLoaded) {
            validateInput(true);
        }
    }//GEN-LAST:event_enableMsAmandaComboActionPerformed

    /**
     * Open a file chooser where the folder of makeblastdb can be selected.
     *
     * @param evt
     */
    private void makeblastdbBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeblastdbBrowseActionPerformed
        // First check whether a file has already been selected.
        // If so, start from that file's parent.
        File startLocation = new File(searchGUIMainFrame.getLastSelectedFolder().getLastSelectedFolder());
        if (makeblastdbLocationTxt != null && makeblastdbLocationTxt.getText() != null && !makeblastdbLocationTxt.getText().trim().equals("")) {
            File temp = new File(makeblastdbLocationTxt.getText());
            if (temp.exists() && temp.isDirectory()) {
                startLocation = temp;
            }
        }
        JFileChooser fc = new JFileChooser(startLocation);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            searchGUIMainFrame.getLastSelectedFolder().setLastSelectedFolder(file.getAbsolutePath());
            if (validateSearchEngineFolder(file, "makeblastdb")) {
                makeblastdbLocationTxt.setText(file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(this,
                        new String[]{"Incorrect makeblastdb folder specified.", "Please try again, or press cancel to exit."},
                        "Incorrect makeblastdb Folder", JOptionPane.WARNING_MESSAGE);
                makeblastdbBrowseActionPerformed(null);
            }
            validateInput(true);
        }
    }//GEN-LAST:event_makeblastdbBrowseActionPerformed

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * MyriMatch location. It auto-validates the MyriMatch location.
     *
     * @param evt
     */
    private void myriMatchBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myriMatchBrowseActionPerformed
        browseMyriMatchLocationPressed();
    }//GEN-LAST:event_myriMatchBrowseActionPerformed

    /**
     * Validate the search engine input.
     *
     * @param evt
     */
    private void enableMyriMatchComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableMyriMatchComboActionPerformed
        if (guiLoaded) {
            validateInput(true);
        }
    }//GEN-LAST:event_enableMyriMatchComboActionPerformed

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * Comet location. It auto-validates the Comet location.
     *
     * @param evt
     */
    private void cometBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cometBrowseActionPerformed
        browseCometLocationPressed();
    }//GEN-LAST:event_cometBrowseActionPerformed

    /**
     * Validate the search engine input.
     *
     * @param evt
     */
    private void enableCometComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableCometComboActionPerformed
        if (guiLoaded) {
            validateInput(true);
        }
    }//GEN-LAST:event_enableCometComboActionPerformed

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * Tide location. It auto-validates the Tide location.
     *
     * @param evt
     */
    private void tideBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tideBrowseActionPerformed
        browseTideLocationPressed();
    }//GEN-LAST:event_tideBrowseActionPerformed

    /**
     * Validate the search engine input.
     *
     * @param evt
     */
    private void enableTideComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableTideComboActionPerformed
        if (guiLoaded) {
            validateInput(true);
        }
    }//GEN-LAST:event_enableTideComboActionPerformed

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * Andromeda location. It auto-validates the Andromeda location.
     *
     * @param evt
     */
    private void andromedaBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_andromedaBrowseActionPerformed
        browseAndromedaLocationPressed();
    }//GEN-LAST:event_andromedaBrowseActionPerformed

    /**
     * Validate the search engine input.
     *
     * @param evt
     */
    private void enableAndromedaComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableAndromedaComboActionPerformed
        if (guiLoaded) {
            validateInput(true);
        }
    }//GEN-LAST:event_enableAndromedaComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton andromedaBrowse;
    private javax.swing.JLabel andromedaLocationLabel;
    private javax.swing.JTextField andromedaLocationTxt;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton cometBrowse;
    private javax.swing.JLabel cometLocationLabel;
    private javax.swing.JTextField cometLocationTxt;
    private javax.swing.JComboBox enableAndromedaCombo;
    private javax.swing.JComboBox enableCometCombo;
    private javax.swing.JComboBox enableMsAmandaCombo;
    private javax.swing.JComboBox enableMsgfCombo;
    private javax.swing.JComboBox enableMyriMatchCombo;
    private javax.swing.JComboBox enableOmssaCombo;
    private javax.swing.JComboBox enableTideCombo;
    private javax.swing.JComboBox enableXTandemCombo;
    private javax.swing.JButton makeblastdbBrowse;
    private javax.swing.JLabel makeblastdbLocationLabel;
    private javax.swing.JTextField makeblastdbLocationTxt;
    private javax.swing.JButton msAmandaBrowse;
    private javax.swing.JLabel msAmandaLocationLabel;
    private javax.swing.JTextField msAmandaLocationTxt;
    private javax.swing.JButton msgfBrowse;
    private javax.swing.JLabel msgfLocationLabel;
    private javax.swing.JTextField msgfLocationTxt;
    private javax.swing.JButton myriMatchBrowse;
    private javax.swing.JLabel myriMatchLocationLabel;
    private javax.swing.JTextField myriMatchLocationTxt;
    private javax.swing.JButton okButton;
    private javax.swing.JButton omssaBrowse;
    private javax.swing.JLabel omssaLocationLabel;
    private javax.swing.JTextField omssaLocationTxt;
    private javax.swing.JPanel searchEnginesLocationPanel;
    private javax.swing.JButton tideBrowse;
    private javax.swing.JLabel tideLocationLabel;
    private javax.swing.JTextField tideLocationTxt;
    private javax.swing.JButton xTandemBrowse;
    private javax.swing.JLabel xTandemLocationLabel;
    private javax.swing.JTextField xTandemLocationTxt;
    // End of variables declaration//GEN-END:variables

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * OMSSA location. It auto-validates the OMSSA location.
     */
    public void browseOMSSALocationPressed() {
        browseSearchEngineLocationPressed(Advocate.omssa, OmssaclProcessBuilder.EXECUTABLE_FILE_NAME, omssaLocationTxt);
    }

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * X!Tandem location. It auto-validates the X!Tandem location.
     */
    public void browseXTandemLocationPressed() {
        browseSearchEngineLocationPressed(Advocate.xtandem, TandemProcessBuilder.EXECUTABLE_FILE_NAME, xTandemLocationTxt);
    }

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * MS-GF+ location. It auto-validates the MS-GF+ location.
     */
    public void browseMsgfLocationPressed() {
        browseSearchEngineLocationPressed(Advocate.msgf, MsgfProcessBuilder.EXECUTABLE_FILE_NAME, msgfLocationTxt);
    }

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * MS Amanda location. It auto-validates the MS Amanda location.
     */
    public void browseMsAmandaLocationPressed() {
        browseSearchEngineLocationPressed(Advocate.msAmanda, MsAmandaProcessBuilder.executableFileName, msAmandaLocationTxt);
    }

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * MyriMatch location. It auto-validates the MyriMatch location.
     */
    public void browseMyriMatchLocationPressed() {
        browseSearchEngineLocationPressed(Advocate.myriMatch, MyriMatchProcessBuilder.EXECUTABLE_FILE_NAME, myriMatchLocationTxt);
    }

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * Comet location. It auto-validates the Comet location.
     */
    public void browseCometLocationPressed() {
        browseSearchEngineLocationPressed(Advocate.comet, CometProcessBuilder.EXECUTABLE_FILE_NAME, cometLocationTxt);
    }

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * Tide location. It auto-validates the Tide location.
     */
    public void browseTideLocationPressed() {
        browseSearchEngineLocationPressed(Advocate.tide, TideSearchProcessBuilder.EXECUTABLE_FILE_NAME, tideLocationTxt);
    }

    /**
     * This method should be called whenever 'browse' has been pressed for the
     * Andromeda location. It auto-validates the Andromeda location.
     */
    public void browseAndromedaLocationPressed() {
        browseSearchEngineLocationPressed(Advocate.andromeda, AndromedaProcessBuilder.EXECUTABLE_FILE_NAME, andromedaLocationTxt);
    }

    /**
     * This method should be called whenever 'browse' has been pressed for a
     * search engine location. It auto-validates the location.
     *
     * @param advocate the search engine advocate
     * @param firstTargetName the name of the first target
     * @param searchEngineLocationTxt the text file displaying the search engine
     * location
     */
    public void browseSearchEngineLocationPressed(Advocate advocate, String firstTargetName, JTextField searchEngineLocationTxt) {
        browseSearchEngineLocationPressed(advocate, firstTargetName, null, searchEngineLocationTxt);
    }

    /**
     * This method should be called whenever 'browse' has been pressed for a
     * search engine location. It auto-validates the location.
     *
     * @param advocate the search engine advocate
     * @param firstTargetName the name of the first target
     * @param secondTargetName the name of the second target, can be null
     * @param searchEngineLocationTxt the text file displaying the search engine
     * location
     */
    public void browseSearchEngineLocationPressed(Advocate advocate, String firstTargetName, String secondTargetName, JTextField searchEngineLocationTxt) {
        // first check whether a file has already been selected, if so, start from that file's parent
        File startLocation = new File(searchGUIMainFrame.getLastSelectedFolder().getLastSelectedFolder());
        if (searchEngineLocationTxt != null && searchEngineLocationTxt.getText() != null && !searchEngineLocationTxt.getText().trim().equals("")) {
            File temp = new File(searchEngineLocationTxt.getText());
            if (temp.exists() && temp.isDirectory()) {
                startLocation = temp;
            }
        }
        JFileChooser fc = new JFileChooser(startLocation);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            searchGUIMainFrame.getLastSelectedFolder().setLastSelectedFolder(file.getAbsolutePath());
            if (validateSearchEngineFolder(file, firstTargetName, secondTargetName)) {
                searchEngineLocationTxt.setText(file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(this,
                        new String[]{"Incorrect " + advocate + " home folder specified.", "Please try again, or press cancel to exit."},
                        "Incorrect " + advocate + " Folder", JOptionPane.WARNING_MESSAGE);
                browseSearchEngineLocationPressed(advocate, firstTargetName, secondTargetName, searchEngineLocationTxt);
            }
            validateInput(true);
        }
    }

    /**
     * This method validates whether the specified folder is the root folder if
     * the given search engine.
     *
     * @param aFile file with the search engine root folder
     * @param targetFile the file required in the search engine root folder
     * @return boolean to indicate whether the correct search engine root folder
     * has been selected.
     */
    public boolean validateSearchEngineFolder(File aFile, String targetFile) {
        return validateSearchEngineFolder(aFile, targetFile, null);
    }

    /**
     * This method validates whether the specified folder is the root folder if
     * the given search engine.
     *
     * @param aFile file with the search engine root folder
     * @param targetFile the file required in the search engine root folder
     * @param secondTargetFile the second file required in the search engine
     * root folder, can be null
     * @return boolean to indicate whether the correct search engine root folder
     * has been selected.
     */
    public boolean validateSearchEngineFolder(File aFile, String targetFile, String secondTargetFile) {
        boolean result = false;
        if (aFile.exists() && aFile.isDirectory()) {
            String[] fileNames = aFile.list();
            int count = 0;
            for (String lFileName : fileNames) {
                if (lFileName.startsWith(targetFile)) {
                    count++;
                } else if (secondTargetFile != null && lFileName.startsWith(secondTargetFile)) {
                    count++;
                }
            }

            if (count > 0) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Validates the input.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the input is valid.
     */
    private boolean validateInput(boolean showMessage) {

        boolean valid = true;

        // enable/disable the makeblastdb options
        makeblastdbLocationLabel.setEnabled(enableOmssaCombo.getSelectedIndex() == 0);
        makeblastdbLocationTxt.setEnabled(enableOmssaCombo.getSelectedIndex() == 0);
        makeblastdbBrowse.setEnabled(enableOmssaCombo.getSelectedIndex() == 0);

        // search engines location specified?
        omssaLocationLabel.setForeground(Color.BLACK);
        xTandemLocationLabel.setForeground(Color.BLACK);
        msgfLocationLabel.setForeground(Color.BLACK);
        msAmandaLocationLabel.setForeground(Color.BLACK);
        myriMatchLocationLabel.setForeground(Color.BLACK);
        cometLocationTxt.setForeground(Color.BLACK);
        tideLocationTxt.setForeground(Color.BLACK);
        andromedaLocationTxt.setForeground(Color.BLACK);
        omssaLocationLabel.setToolTipText(null);
        xTandemLocationLabel.setToolTipText(null);
        msgfLocationLabel.setToolTipText(null);
        msAmandaLocationLabel.setToolTipText(null);
        myriMatchLocationLabel.setToolTipText(null);
        cometLocationLabel.setToolTipText(null);
        tideLocationLabel.setToolTipText(null);
        andromedaLocationLabel.setToolTipText(null);
        omssaValid = true;
        tandemValid = true;
        msgfValid = true;
        msAmandaValid = true;
        myriMatchValid = true;
        cometValid = true;
        tideValid = true;
        andromedaValid = true;

        if (isOmssaEnabled()) {
            omssaValid = validateSearchEngineInstallation(Advocate.omssa, getOmssaLocation(), valid, omssaLocationLabel, showMessage);
            valid = omssaValid && valid;
        }
        if (isXtandemEnabled()) {
            tandemValid = validateSearchEngineInstallation(Advocate.xtandem, getXtandemLocation(), valid, xTandemLocationLabel, showMessage);
            valid = tandemValid && valid;
        }
        if (isMsgfEnabled()) {
            msgfValid = validateSearchEngineInstallation(Advocate.msgf, getMsgfLocation(), valid, msgfLocationLabel, showMessage);
            valid = msgfValid && valid;
        }
        if (isMsAmandaEnabled()) {
            msAmandaValid = validateSearchEngineInstallation(Advocate.msAmanda, getMsAmandaLocation(), valid, msAmandaLocationLabel, showMessage);
            valid = msAmandaValid && valid;
        }
        if (isMyriMatchEnabled()) {
            myriMatchValid = validateSearchEngineInstallation(Advocate.myriMatch, getMyriMatchLocation(), valid, myriMatchLocationLabel, showMessage);
            valid = myriMatchValid && valid;
        }
        if (isCometEnabled()) {
            cometValid = validateSearchEngineInstallation(Advocate.comet, getCometLocation(), valid, cometLocationLabel, showMessage);
            valid = cometValid && valid;
        }
        if (isTideEnabled()) {
            tideValid = validateSearchEngineInstallation(Advocate.tide, getTideLocation(), valid, tideLocationLabel, showMessage);
            valid = tideValid && valid;
        }
        if (isAndromedaEnabled()) {
            andromedaValid = validateSearchEngineInstallation(Advocate.andromeda, getAndromedaLocation(), valid, andromedaLocationLabel, showMessage);
            valid = andromedaValid && valid;
        }

        if (!isOmssaEnabled() && !isXtandemEnabled() && !isMsgfEnabled() && !isMsAmandaEnabled() && !isMyriMatchEnabled() && !isCometEnabled() && !isTideEnabled()  && !isAndromedaEnabled()) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to select at least one search engine.", "No Search Engines Selected", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
        }

        okButton.setEnabled(valid);
        return valid;
    }

    /**
     * Returns true of the search engine installation is validated.
     *
     * @param advocate the search engine advocate
     * @param searchEngineLocation the location of the search engine
     * @param allValid the combined validation status of all search engines
     * @param searchEngineLabel the search engine label
     * @param showMessage if an error message is to be shown if an error occurs
     */
    private boolean validateSearchEngineInstallation(Advocate advocate, File searchEngineLocation, boolean allValid, JLabel searchEngineLabel, boolean showMessage) {

        boolean searchEngineValid = true;
        boolean installationOk = false;

        if (searchEngineLocation == null) {
            if (showMessage && allValid) {
                JOptionPane.showMessageDialog(this, "You need to specify the location of " + advocate + ".", advocate + " Not Found", JOptionPane.WARNING_MESSAGE);
            }
            allValid = false;
            searchEngineValid = false;
            searchEngineLabel.setToolTipText("Please specify the location of " + advocate);
            searchEngineLabel.setForeground(Color.RED);
        } else {
            if (searchEngineLocation == null || !searchEngineLocation.exists()) {
                if (showMessage && allValid) {
                    JOptionPane.showMessageDialog(this, "The " + advocate + " folder does not exist. Please specify the location of " + advocate + ".",
                            advocate + " Not Found", JOptionPane.WARNING_MESSAGE);
                }
                allValid = false;
                searchEngineValid = false;
                searchEngineLabel.setToolTipText("Please specify the location of " + advocate);
                searchEngineLabel.setForeground(Color.RED);
            } else {
                // test if search engine is installed correctly
                if (allValid) {

                    if (advocate == Advocate.tide) {
                        installationOk = SearchGUI.validateSearchEngineInstallation(Advocate.tide, getTideLocation(), showMessage);
                    } else if (advocate == Advocate.comet) {
                        installationOk = SearchGUI.validateSearchEngineInstallation(Advocate.comet, getCometLocation(), showMessage);
                    } else if (advocate == Advocate.myriMatch) {
                        installationOk = SearchGUI.validateSearchEngineInstallation(Advocate.myriMatch, getMyriMatchLocation(), showMessage);
                    } else if (advocate == Advocate.msAmanda) {
                        installationOk = SearchGUI.validateSearchEngineInstallation(Advocate.msAmanda, getMsAmandaLocation(), showMessage);
                    } else if (advocate == Advocate.msgf) {
                        installationOk = SearchGUI.validateSearchEngineInstallation(Advocate.msgf, getMsgfLocation(), showMessage);
                    } else if (advocate == Advocate.xtandem) {
                        installationOk = SearchGUI.validateSearchEngineInstallation(Advocate.xtandem, getXtandemLocation(), showMessage);
                    } else if (advocate == Advocate.omssa) {
                        installationOk = SearchGUI.validateSearchEngineInstallation(Advocate.omssa, getOmssaLocation(), showMessage);
                    } else if (advocate == Advocate.andromeda) {
                        installationOk = SearchGUI.validateSearchEngineInstallation(Advocate.andromeda, getAndromedaLocation(), showMessage);
                    }

                    if (!installationOk) {
                        allValid = false;
                        searchEngineValid = false;
                        searchEngineLabel.setToolTipText("Failed to run " + advocate);
                        searchEngineLabel.setForeground(Color.RED);
                    }
                }
            }
        }

        okButton.setEnabled(installationOk);

        return searchEngineValid;
    }

    /**
     * Returns whether OMSSA is enabled.
     *
     * @return a boolean indicating if OMSSA is enabled
     */
    public boolean isOmssaEnabled() {
        return ((String) enableOmssaCombo.getSelectedItem()).equals(enableDisable[0]);
    }

    /**
     * Returns whether X!Tandem is enabled.
     *
     * @return a boolean indicating if X!Tandem is enabled
     */
    public boolean isXtandemEnabled() {
        return ((String) enableXTandemCombo.getSelectedItem()).equals(enableDisable[0]);
    }

    /**
     * Returns whether MS-GF+ is enabled.
     *
     * @return a boolean indicating if MS-GF+ is enabled
     */
    public boolean isMsgfEnabled() {
        return ((String) enableMsgfCombo.getSelectedItem()).equals(enableDisable[0]);
    }

    /**
     * Returns whether MS Amanda is enabled.
     *
     * @return a boolean indicating if MS Amanda is enabled
     */
    public boolean isMsAmandaEnabled() {
        return ((String) enableMsAmandaCombo.getSelectedItem()).equals(enableDisable[0]);
    }

    /**
     * Returns whether MyriMatch is enabled.
     *
     * @return a boolean indicating if MyriMatch is enabled
     */
    public boolean isMyriMatchEnabled() {
        return ((String) enableMyriMatchCombo.getSelectedItem()).equals(enableDisable[0]);
    }

    /**
     * Returns whether Comet is enabled.
     *
     * @return a boolean indicating if Comet is enabled
     */
    public boolean isCometEnabled() {
        return ((String) enableCometCombo.getSelectedItem()).equals(enableDisable[0]);
    }

    /**
     * Returns whether Tide is enabled.
     *
     * @return a boolean indicating if Tide is enabled
     */
    public boolean isTideEnabled() {
        return ((String) enableTideCombo.getSelectedItem()).equals(enableDisable[0]);
    }
    
    /**
     * Returns whether Andromeda is enabled.
     *
     * @return a boolean indicating if Tide is enabled
     */
    public boolean isAndromedaEnabled() {
        return ((String) enableAndromedaCombo.getSelectedItem()).equals(enableDisable[0]);
    }

    /**
     * Returns the OMSSA location.
     *
     * @return the OMSSA location
     */
    public File getOmssaLocation() {
        if (omssaLocationTxt.getText() != null) {
            return new File(omssaLocationTxt.getText());
        } else {
            return null;
        }
    }

    /**
     * Returns the X!Tandem location.
     *
     * @return the X!Tandem location
     */
    public File getXtandemLocation() {
        if (xTandemLocationTxt.getText() != null) {
            return new File(xTandemLocationTxt.getText());
        } else {
            return null;
        }
    }

    /**
     * Returns the MS-GF+ location.
     *
     * @return the MS-GF+ location
     */
    public File getMsgfLocation() {
        if (msgfLocationTxt.getText() != null) {
            return new File(msgfLocationTxt.getText());
        } else {
            return null;
        }
    }

    /**
     * Returns the MS Amanda location.
     *
     * @return the MS Amanda location
     */
    public File getMsAmandaLocation() {
        if (msAmandaLocationTxt.getText() != null) {
            return new File(msAmandaLocationTxt.getText());
        } else {
            return null;
        }
    }

    /**
     * Returns the MyriMatch location.
     *
     * @return the MyriMatch location
     */
    public File getMyriMatchLocation() {
        if (myriMatchLocationTxt.getText() != null) {
            return new File(myriMatchLocationTxt.getText());
        } else {
            return null;
        }
    }

    /**
     * Returns the Comet location.
     *
     * @return the Comet location
     */
    public File getCometLocation() {
        if (cometLocationTxt.getText() != null) {
            return new File(cometLocationTxt.getText());
        } else {
            return null;
        }
    }

    /**
     * Returns the Tide location.
     *
     * @return the Tide location
     */
    public File getTideLocation() {
        if (tideLocationTxt.getText() != null) {
            return new File(tideLocationTxt.getText());
        } else {
            return null;
        }
    }

    /**
     * Returns the Andromeda location.
     *
     * @return the Andromeda location
     */
    public File getAndromedaLocation() {
        if (andromedaLocationTxt.getText() != null) {
            return new File(andromedaLocationTxt.getText());
        } else {
            return null;
        }
    }

    /**
     * Returns the makeblastdb location.
     *
     * @return the makeblastdb location
     */
    public File getMakeblastdbLocation() {
        if (makeblastdbLocationTxt.getText() != null) {
            return new File(makeblastdbLocationTxt.getText());
        } else {
            return null;
        }
    }
}
