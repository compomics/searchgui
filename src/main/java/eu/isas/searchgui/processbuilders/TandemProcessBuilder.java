package eu.isas.searchgui.processbuilders;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.XtandemParameters;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.waiting.WaitingHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class will build files and start a process to perform an X!Tandem
 * search.
 *
 * @author Marc Vaudel
 */
public class TandemProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The input XML file for X!Tandem.
     */
    private final String INPUT_FILE = "input_searchGUI.xml";
    /**
     * The parameters XML file for X!Tandem.
     */
    private final String PARAMETER_FILE = "parameters_searchGUI.xml";
    /**
     * The taxonomy XML file for X!Tandem.
     */
    private final String TAXONOMY_FILE = "taxonomy_searchGUI.xml";
    /**
     * The name of the X!Tandem executable.
     */
    public final static String EXECUTABLE_FILE_NAME = "tandem";
    /**
     * The number of processors available.
     */
    private int nProcessors;
    /**
     * The xTandem file.
     */
    private File xTandemFile;
    /**
     * The input file.
     */
    private File inputFile;
    /**
     * The parameter file.
     */
    private File parameterFile;
    /**
     * The taxonomy file.
     */
    private File taxonomyFile;
    /**
     * The database file.
     */
    private File dataBase;
    /**
     * The spectrum file to search.
     */
    private String spectrumFile;
    /**
     * The output path.
     */
    private String outputPath;
    /**
     * The fragment mass tolerance.
     */
    private Double fragmentMassError;
    /**
     * The precursor mass tolerance.
     */
    private Double precursorMassError;
    /**
     * The precursor mass tolerance unit.
     */
    private String precursorUnit;
    /**
     * The fragment mass tolerance unit.
     */
    private String fragmentUnit;
    /**
     * The upper charge.
     */
    private int maxCharge;
    /**
     * The fixed modifications.
     */
    private ArrayList<String> fixedMod;
    /**
     * The variable modifications.
     */
    private ArrayList<String> variableMod;
    /**
     * The variable modifications targeting a motif.
     */
    private ArrayList<String> variableModMotifs;
    /**
     * The refinement fixed modifications.
     */
    private ArrayList<String> refinementFixedMod = null;
    /**
     * The refinement variable modifications.
     */
    private ArrayList<String> refinementVariableMod;
    /**
     * The refinement variable modifications targeting a motif.
     */
    private ArrayList<String> refinementVariableModMotif;
    /**
     * The refinement variable modifications of peptide N-termini.
     */
    private ArrayList<String> refinementVariableNTermMod;
    /**
     * The refinement variable modifications of peptide C-termini.
     */
    private ArrayList<String> refinementVariableCTermMod;
    /**
     * The mass to be added as fixed modification to protein C-termini.
     */
    private double fixedCtermProteinMod = 0.0;
    /**
     * The mass to be added as fixed modification to protein N-termini.
     */
    private double fixedNtermProteinMod = 0.0;
    /**
     * The enzyme.
     */
    private String enzymeCleaveSiteAsText;
    /**
     * Sets whether the enzyme is semi-specific.
     */
    private String enzymeIsSemiSpecific;
    /**
     * The missed cleavages allowed.
     */
    private int missedCleavages;
    /**
     * The ion types.
     */
    private String ion1, ion2;
    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The X!Tandem parameters.
     */
    private XtandemParameters xtandemParameters;

    /**
     * Constructor.
     *
     * @param xTandem_directory directory location of tandem.exe
     * @param mgfFile name of the file containing the spectra
     * @param outputPath path where to output the results
     * @param searchParameters the search parameters
     * @param waitingHandler the waiting handler
     * @param nThreads the number of threads to use
     * @throws IllegalArgumentException thrown if more than one fixed PTM has
     * the same target
     */
    public TandemProcessBuilder(File xTandem_directory, String mgfFile, String outputPath,
            SearchParameters searchParameters, WaitingHandler waitingHandler, int nThreads) throws IllegalArgumentException {

        try {
            xtandemParameters = (XtandemParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.xtandem.getIndex());

            this.waitingHandler = waitingHandler;
            xTandemFile = xTandem_directory;
            nProcessors = nThreads;
            spectrumFile = mgfFile;
            dataBase = searchParameters.getFastaFile().getAbsoluteFile();
            this.outputPath = outputPath;
            fragmentMassError = searchParameters.getFragmentIonAccuracy();
            precursorMassError = searchParameters.getPrecursorAccuracy();
            if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.PPM) {
                precursorUnit = "ppm";
            } else if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.DA) {
                precursorUnit = "Daltons";
            }
            if (searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.PPM) {
                fragmentUnit = "ppm";
            } else if (searchParameters.getFragmentAccuracyType() == SearchParameters.MassAccuracyType.DA) {
                fragmentUnit = "Daltons";
            }
            maxCharge = searchParameters.getMaxChargeSearched().value;
            fixedMod = new ArrayList<String>();
            PtmSettings modificationProfile = searchParameters.getPtmSettings();
            boolean sameFixed = modificationProfile.getFixedModifications().size() == modificationProfile.getRefinementFixedModifications().size();
            for (String ptmName : modificationProfile.getFixedModifications()) {
                PTM ptm = ptmFactory.getPTM(ptmName);
                if (ptm.getType() == PTM.MODN) {
                    fixedNtermProteinMod += ptm.getRoundedMass();
                } else if (ptm.getType() == PTM.MODC) {
                    fixedCtermProteinMod += ptm.getRoundedMass();
                } else {
                    fixedMod.add(ptmName);
                }
                if (sameFixed && !modificationProfile.getRefinementFixedModifications().contains(ptmName)) {
                    sameFixed = false;
                }
            }
            if (!sameFixed) {
                refinementFixedMod = modificationProfile.getRefinementFixedModifications();
            }
            variableMod = new ArrayList<String>();
            variableModMotifs = new ArrayList<String>();
            for (String ptmName : searchParameters.getPtmSettings().getVariableModifications()) {
                // Exclude PTMs triggered by the quick options
                boolean newModification = true;
                if (xtandemParameters.isProteinQuickAcetyl() && ptmName.equals("Acetylation of protein N-term")) {
                    newModification = false;
                }
                if (newModification && xtandemParameters.isQuickPyrolidone()
                        && (ptmName.equals("Pyrolidone from E") || ptmName.equals("Pyrolidone from Q") || ptmName.equals("Pyrolidone from carbamidomethylated C"))) {
                    newModification = false;
                }
                if (newModification) {
                    PTM ptm = ptmFactory.getPTM(ptmName);
                    if (ptm.getPattern().length() == 1 && ptm.getPattern().getAminoAcidsAtTarget().size() == 1) {
                        variableMod.add(ptmName);
                    } else {
                        variableModMotifs.add(ptmName);
                    }
                }
            }
            refinementVariableMod = new ArrayList<String>();
            refinementVariableModMotif = new ArrayList<String>();
            refinementVariableCTermMod = new ArrayList<String>();
            refinementVariableNTermMod = new ArrayList<String>();
            for (String ptmName : modificationProfile.getRefinementVariableModifications()) {
                // Exclude PTMs triggered by the quick options
                boolean newModification = true;
                if (xtandemParameters.isProteinQuickAcetyl() && ptmName.equals("Acetylation of protein N-term")) {
                    newModification = false;
                }
                if (newModification && xtandemParameters.isQuickPyrolidone()
                        && (ptmName.equals("Pyrolidone from E") || ptmName.equals("Pyrolidone from Q") || ptmName.equals("Pyrolidone from carbamidomethylated C"))) {
                    newModification = false;
                }
                if (newModification) {
                    PTM ptm = ptmFactory.getPTM(ptmName);
                    if (ptm.getType() == PTM.MODC
                            || ptm.getType() == PTM.MODCAA
                            || ptm.getType() == PTM.MODCP
                            || ptm.getType() == PTM.MODCPAA) {
                        refinementVariableCTermMod.add(ptmName);
                    } else if (ptm.getType() == PTM.MODN
                            || ptm.getType() == PTM.MODNAA
                            || ptm.getType() == PTM.MODNP
                            || ptm.getType() == PTM.MODNPAA) {
                        refinementVariableNTermMod.add(ptmName);
                    } else if (ptm.getPattern().length() == 1 && ptm.getPattern().getAminoAcidsAtTarget().size() == 1) {
                        refinementVariableMod.add(ptmName);
                    } else {
                        refinementVariableModMotif.add(ptmName);
                    }
                }
            }
            enzymeCleaveSiteAsText = searchParameters.getEnzyme().getXTandemFormat();
            if (searchParameters.getEnzyme().isSemiSpecific()) {
                enzymeIsSemiSpecific = "yes";
            } else {
                enzymeIsSemiSpecific = "no";
            }
            missedCleavages = searchParameters.getnMissedCleavages();
            if (searchParameters.getEnzyme().isUnspecific()) { // unspecific cleavage
                missedCleavages = 50;
            }
            ion1 = PeptideFragmentIon.getSubTypeAsString(searchParameters.getIonSearched1());
            ion2 = PeptideFragmentIon.getSubTypeAsString(searchParameters.getIonSearched2());

            createInputFile();
            createTaxonomyFile();
            createParameterFile();

            // make sure that the tandem file is executable
            File xTandem = new File(xTandemFile.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
            xTandem.setExecutable(true);

            // full path to executable
            process_name_array.add(xTandemFile.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);

            // Link to the input file
            process_name_array.add(inputFile.getAbsolutePath());

            process_name_array.trimToSize();

            // print the command to the log file
            System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "xtandem command: ");

            for (Object element : process_name_array) {
                System.out.print(element + " ");
            }

            System.out.println(System.getProperty("line.separator"));

            pb = new ProcessBuilder(process_name_array);

            pb.directory(xTandem_directory);
            // set error out and std out to same stream
            pb.redirectErrorStream(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the X!Tandem input file.
     */
    private void createInputFile() {
        inputFile = new File(xTandemFile, INPUT_FILE);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile));
            bw.write("<?xml version=\"1.0\"?>" + System.getProperty("line.separator")
                    + "<bioml>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"list path, default parameters\">" + PARAMETER_FILE + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"list path, taxonomy information\">" + TAXONOMY_FILE + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, taxon\">all</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, path\">" + spectrumFile + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, path\">" + outputPath + "</note>" + System.getProperty("line.separator")
                    + "</bioml>" + System.getProperty("line.separator"));
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Could not create X!Tandem input file. Unable to write file: '" + ioe.getMessage() + "'!");
        }
    }

    /**
     * Creates the taxonomy file.
     */
    private void createTaxonomyFile() throws IllegalArgumentException {
        taxonomyFile = new File(xTandemFile, TAXONOMY_FILE);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(taxonomyFile));
            bw.write(
                    "<?xml version=\"1.0\"?>" + System.getProperty("line.separator")
                    + "<bioml label=\"x! taxon-to-file matching list\">" + System.getProperty("line.separator")
                    + "\t<taxon label=\"all\">" + System.getProperty("line.separator")
                    + "\t\t<file format=\"peptide\" URL=\"" + dataBase + "\" />" + System.getProperty("line.separator")
                    + "\t</taxon>" + System.getProperty("line.separator")
                    + "</bioml>");
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Could not create X!Tandem taxonomy file. Unable to write file: '" + ioe.getMessage() + "'!");
        }
    }

    /**
     * Creates the parameters file.
     *
     * @throws IllegalArgumentException thrown if more than one fixed PTM has
     * the same target
     */
    private void createParameterFile() throws IllegalArgumentException {

        // get the modification tags
        String modDescription = getSearchedModList(variableMod, fixedMod);
        String noiseSuppression = "no";
        if (xtandemParameters.isUseNoiseSuppression()) {
            noiseSuppression = "yes";
        }
        String quickAcetyl = "yes";
        if (!xtandemParameters.isProteinQuickAcetyl()) {
            quickAcetyl = "no";
        }
        String quickPyrolidone = "yes";
        if (!xtandemParameters.isQuickPyrolidone()) {
            quickPyrolidone = "no";
        }
        String stpBias = "no";
        if (xtandemParameters.isStpBias()) {
            stpBias = "yes";
        }
        String outputProtein = "no";
        if (xtandemParameters.isOutputProteins()) {
            outputProtein = "yes";
        }
        String outputSequences = "no";
        if (xtandemParameters.isOutputSequences()) {
            outputSequences = "yes";
        }
        String outputSpectra = "no";
        if (xtandemParameters.isOutputSpectra()) {
            outputSpectra = "yes";
        }
        String outputHistograms = "no";
        if (xtandemParameters.isOutputHistograms()) {
            outputHistograms = "yes";
        }

        String motifs = "";
        for (String ptmName : variableModMotifs) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            motifs += ptm.getRoundedMass() + "@" + ptm.getPattern().getPrositeFormat(); //@TODO: check how multiple modifications at the same amino acid are supported in the refinement search
        }

        parameterFile = new File(xTandemFile, PARAMETER_FILE);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(parameterFile));
            bw.write(
                    "<?xml version=\"1.0\"?>" + System.getProperty("line.separator")
                    + "<?xml-stylesheet type=\"text/xsl\" href=\"tandem-input-style.xsl\"?>" + System.getProperty("line.separator")
                    + "<bioml>" + System.getProperty("line.separator")
                    + "<note>list path parameters</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"list path, default parameters\">default_input.xml</note>" + System.getProperty("line.separator")
                    + "\t\t<note>This value is ignored when it is present in the default parameter" + System.getProperty("line.separator")
                    + "\t\tlist path.</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"list path, taxonomy information\">" + TAXONOMY_FILE + "</note>" + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    + "<note>spectrum parameters</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, fragment monoisotopic mass error\">" + fragmentMassError + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass error plus\">" + precursorMassError + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass error minus\">" + precursorMassError + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass isotope error\">yes</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, fragment monoisotopic mass error units\">" + fragmentUnit + "</note>" + System.getProperty("line.separator")
                    + "\t<note>The value for this parameter may be 'Daltons' or 'ppm': all other values are ignored</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass error units\">" + precursorUnit + "</note>" + System.getProperty("line.separator")
                    + "\t\t<note>The value for this parameter may be 'Daltons' or 'ppm': all other values are ignored</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, fragment mass type\">monoisotopic</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values are monoisotopic|average </note>" + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    + "<note>spectrum conditioning parameters</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, dynamic range\">" + xtandemParameters.getDynamicRange() + "</note>" + System.getProperty("line.separator")
                    + "\t\t<note>The peaks read in are normalized so that the most intense peak" + System.getProperty("line.separator")
                    + "\t\tis set to the dynamic range value. All peaks with values of less that" + System.getProperty("line.separator")
                    + "\t\t1, using this normalization, are not used. This normalization has the" + System.getProperty("line.separator")
                    + "\t\toverall effect of setting a threshold value for peak intensities.</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, total peaks\">" + xtandemParameters.getnPeaks() + "</note> " + System.getProperty("line.separator")
                    + "\t\t<note>If this value is 0, it is ignored. If it is greater than zero (lets say 50)," + System.getProperty("line.separator")
                    + "\t\tthen the number of peaks in the spectrum with be limited to the 50 most intense" + System.getProperty("line.separator")
                    + "\t\tpeaks in the spectrum. X! tandem does not do any peak finding: it only" + System.getProperty("line.separator")
                    + "\t\tlimits the peaks used by this parameter, and the dynamic range parameter.</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, maximum parent charge\">" + maxCharge + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, use noise suppression\">" + noiseSuppression + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, minimum parent m+h\">" + xtandemParameters.getMinPrecursorMass() + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, minimum fragment mz\">" + xtandemParameters.getMinFragmentMz() + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, minimum peaks\">" + xtandemParameters.getMinPeaksPerSpectrum() + "</note> " + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, threads\">" + nProcessors + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"spectrum, sequence batch size\">1000</note>" + System.getProperty("line.separator")
                    + "\t" + System.getProperty("line.separator")
                    + "<note>residue modification parameters</note>" + System.getProperty("line.separator")
                    + modDescription
                    + "\t<note type=\"input\" label=\"residue, potential modification motif\">" + motifs + "</note>" + System.getProperty("line.separator")
                    + "\t\t<note>The format of this parameter is similar to residue, modification mass," + System.getProperty("line.separator")
                    + "\t\twith the addition of a modified PROSITE notation sequence motif specification." + System.getProperty("line.separator")
                    + "\t\tFor example, a value of 80@[ST!]PX[KR] indicates a modification" + System.getProperty("line.separator")
                    + "\t\tof either S or T when followed by P, and residue and the a K or an R." + System.getProperty("line.separator")
                    + "\t\tA value of 204@N!{P}[ST]{P} indicates a modification of N by 204, if it" + System.getProperty("line.separator")
                    + "\t\tis NOT followed by a P, then either an S or a T, NOT followed by a P." + System.getProperty("line.separator")
                    + "\t\tPositive and negative values are allowed." + System.getProperty("line.separator")
                    + "\t\t</note>" + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    + "<note>protein parameters</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, taxon\">all</note>" + System.getProperty("line.separator")
                    + "\t\t<note>This value is interpreted using the information in taxonomy.xml.</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, cleavage site\">" + enzymeCleaveSiteAsText + "</note>" + System.getProperty("line.separator")
                    + "\t\t<note>this setting corresponds to the enzyme trypsin. The first characters" + System.getProperty("line.separator")
                    + "\t\tin brackets represent residues N-terminal to the bond - the '|' pipe -" + System.getProperty("line.separator")
                    + "\t\tand the second set of characters represent residues C-terminal to the" + System.getProperty("line.separator")
                    + "\t\tbond. The characters must be in square brackets (denoting that only" + System.getProperty("line.separator")
                    + "\t\tthese residues are allowed for a cleavage) or french brackets (denoting" + System.getProperty("line.separator")
                    + "\t\tthat these residues cannot be in that position). Use UPPERCASE characters." + System.getProperty("line.separator")
                    + "\t\tTo denote cleavage at any residue, use [X]|[X] and reset the " + System.getProperty("line.separator")
                    + "\t\tscoring, maximum missed cleavage site parameter (see below) to something like 50." + System.getProperty("line.separator")
                    + "\t\t</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, cleavage semi\">" + enzymeIsSemiSpecific + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, modified residue mass file\"></note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, cleavage C-terminal mass change\">17.002735</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, cleavage N-terminal mass change\">+1.007825</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, quick acetyl\">" + quickAcetyl + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, quick pyrolidone\">" + quickPyrolidone + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, stP bias\">" + stpBias + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, N-terminal residue modification mass\">" + fixedNtermProteinMod + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, C-terminal residue modification mass\">" + fixedCtermProteinMod + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"protein, homolog management\">no</note>" + System.getProperty("line.separator")
                    + "\t\t<note>if yes, an upper limit is set on the number of homologues kept for a particular spectrum</note>" + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    + getRefinementParametersSection()
                    + System.getProperty("line.separator")
                    + "<note>scoring parameters</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"scoring, minimum ion count\">4</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"scoring, maximum missed cleavage sites\">" + missedCleavages + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"scoring, x ions\">" + getXSelected() + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"scoring, y ions\">" + getYSelected() + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"scoring, z ions\">" + getZSelected() + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"scoring, a ions\">" + getASelected() + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"scoring, b ions\">" + getBSelected() + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"scoring, c ions\">" + getCSelected() + "</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"scoring, cyclic permutation\">no</note>" + System.getProperty("line.separator")
                    + "\t\t<note>if yes, cyclic peptide sequence permutation is used to pad the scoring histograms</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"scoring, include reverse\">no</note>" + System.getProperty("line.separator")
                    + "\t\t<note>if yes, then reversed sequences are searched at the same time as forward sequences</note>" + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    + "<note>output parameters</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, log path\"></note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, message\"></note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, one sequence copy\">no</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, sequence path\"></note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, path\">output.xml</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, sort results by\">spectrum</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values = protein|spectrum (spectrum is the default)</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, path hashing\">yes</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values = yes|no</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, xsl path\">tandem-style.xsl</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, parameters\">yes</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values = yes|no</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, performance\">yes</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values = yes|no</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, spectra\">" + outputSpectra + "</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values = yes|no</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, histograms\">" + outputHistograms + "</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values = yes|no</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, proteins\">" + outputProtein + "</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values = yes|no</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, sequences\">" + outputSequences + "</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values = yes|no</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, one sequence copy\">no</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values = yes|no, set to yes to produce only one copy of each protein sequence in the output xml</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, results\">all</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values = all|valid|stochastic</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, maximum valid expectation value\">" + xtandemParameters.getMaxEValue() + "</note>" + System.getProperty("line.separator")
                    + "\t\t<note>value is used in the valid|stochastic setting of output, results</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"input\" label=\"output, histogram column width\">50</note>" + System.getProperty("line.separator")
                    + "\t\t<note>values any integer greater than 0. Setting this to '1' makes cutting and pasting histograms" + System.getProperty("line.separator")
                    + "\t\tinto spread sheet programs easier.</note>" + System.getProperty("line.separator")
                    + "<note type=\"description\">ADDITIONAL EXPLANATIONS</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"description\">Each one of the parameters for X! tandem is entered as a labeled note" + System.getProperty("line.separator")
                    + "\t\t\tnode. In the current version of X!, keep those note nodes" + System.getProperty("line.separator")
                    + "\t\t\ton a single line." + System.getProperty("line.separator")
                    + "\t</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"description\">The presence of the type 'input' is necessary if a note is to be considered" + System.getProperty("line.separator")
                    + "\t\t\tan input parameter." + System.getProperty("line.separator")
                    + "\t</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"description\">Any of the parameters that are paths to files may require alteration for a " + System.getProperty("line.separator")
                    + "\t\t\tparticular installation. Full path names usually cause the least trouble," + System.getProperty("line.separator")
                    + "\t\t\tbut there is no reason not to use relative path names, if that is the" + System.getProperty("line.separator")
                    + "\t\t\tmost convenient." + System.getProperty("line.separator")
                    + "\t</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"description\">Any parameter values set in the 'list path, default parameters' file are" + System.getProperty("line.separator")
                    + "\t\t\treset by entries in the normal input file, if they are present. Otherwise," + System.getProperty("line.separator")
                    + "\t\t\tthe default set is used." + System.getProperty("line.separator")
                    + "\t</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"description\">The 'list path, taxonomy information' file must exist." + System.getProperty("line.separator")
                    + "\t\t</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"description\">The directory containing the 'output, path' file must exist: it will not be created." + System.getProperty("line.separator")
                    + "\t\t</note>" + System.getProperty("line.separator")
                    + "\t<note type=\"description\">The 'output, xsl path' is optional: it is only of use if a good XSLT style sheet exists." + System.getProperty("line.separator")
                    + "\t\t</note>" + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    + "</bioml>" + System.getProperty("line.separator"));
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Could not create X!Tandem parameter file. Unable to write file: '" + ioe.getMessage() + "'!");
        }
    }

    /**
     * Returns the refinement parameters section.
     *
     * @return the refinement parameters section
     */
    private String getRefinementParametersSection() {
        StringBuilder parametersSection = new StringBuilder();
        parametersSection.append("<note>model refinement parameters</note>").append(System.getProperty("line.separator"));
        if (xtandemParameters.isRefine()) {
            parametersSection.append("\t<note type=\"input\" label=\"refine\">yes</note>");
        } else {
            parametersSection.append("\t<note type=\"input\" label=\"refine\">no</note>");
        }
        parametersSection.append(System.getProperty("line.separator"));
        String fixedModsString = "";
        if (refinementFixedMod != null) {
            HashMap<Character, ArrayList<PTM>> sortedPtms = sortModifications(refinementFixedMod);
            for (Character target : sortedPtms.keySet()) {
                for (PTM ptm : sortedPtms.get(target)) {
                    if (!fixedModsString.equals("")) {
                        fixedModsString += ",";
                    }
                    fixedModsString += ptm.getRoundedMass() + "@" + ptm.getRoundedMass();
                }
            }
        }
        parametersSection.append("\t<note type=\"input\" label=\"refine, modification mass\">").append(fixedModsString).append("</note>").append(System.getProperty("line.separator")); // @TODO: add refinement modifications!
        parametersSection.append("\t<note type=\"input\" label=\"refine, sequence path\"></note>").append(System.getProperty("line.separator"));
        parametersSection.append("\t<note type=\"input\" label=\"refine, tic percent\">20</note>").append(System.getProperty("line.separator"));
        if (xtandemParameters.isRefineSpectrumSynthesis()) {
            parametersSection.append("\t<note type=\"input\" label=\"refine, spectrum synthesis\">yes</note>").append(System.getProperty("line.separator"));
        } else {
            parametersSection.append("\t<note type=\"input\" label=\"refine, spectrum synthesis\">no</note>").append(System.getProperty("line.separator"));
        }
        parametersSection.append("\t<note type=\"input\" label=\"refine, maximum valid expectation value\">").append(xtandemParameters.getMaximumExpectationValueRefinement()).append("</note>").append(System.getProperty("line.separator"));
        if (xtandemParameters.isRefineUnanticipatedCleavages()) {
            parametersSection.append("\t<note type=\"input\" label=\"refine, unanticipated cleavage\">yes</note>").append(System.getProperty("line.separator"));
        } else {
            parametersSection.append("\t<note type=\"input\" label=\"refine, unanticipated cleavage\">no</note>").append(System.getProperty("line.separator"));
        }
        if (xtandemParameters.isRefineSemi()) {
            parametersSection.append("\t<note type=\"input\" label=\"refine, cleavage semi\">yes</note>").append(System.getProperty("line.separator"));
        } else {
            parametersSection.append("\t<note type=\"input\" label=\"refine, cleavage semi\">no</note>").append(System.getProperty("line.separator"));
        }
        if (xtandemParameters.isRefinePointMutations()) {
            parametersSection.append("\t<note type=\"input\" label=\"refine, point mutations\">yes</note>").append(System.getProperty("line.separator"));
        } else {
            parametersSection.append("\t<note type=\"input\" label=\"refine, point mutations\">no</note>").append(System.getProperty("line.separator"));
        }
        if (xtandemParameters.isRefinePointMutations()) {
            parametersSection.append("\t<note type=\"input\" label=\"refine, saps\">yes</note>").append(System.getProperty("line.separator"));
        } else {
            parametersSection.append("\t<note type=\"input\" label=\"refine, saps\">no</note>").append(System.getProperty("line.separator"));
        }
        if (xtandemParameters.isPotentialModificationsForFullRefinment()) {
            parametersSection.append("\t<note type=\"input\" label=\"refine, use potential modifications for full refinement\">yes</note>").append(System.getProperty("line.separator"));
        } else {
            parametersSection.append("\t<note type=\"input\" label=\"refine, use potential modifications for full refinement\">no</note>").append(System.getProperty("line.separator"));
        }
        String nTerm = "";
        for (String ptmName : refinementVariableNTermMod) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            if (!nTerm.equals("")) {
                nTerm += ",";
            }
            nTerm += ptm.getRoundedMass() + "@[";
        }
        parametersSection.append("\t<note type=\"input\" label=\"refine, potential N-terminus modifications\">").append(nTerm).append("</note>").append(System.getProperty("line.separator"));

        String cTerm = "";
        for (String ptmName : refinementVariableCTermMod) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            if (!cTerm.equals("")) {
                cTerm += ",";
            }
            cTerm += ptm.getRoundedMass() + "@]";
        }
        parametersSection.append("\t<note type=\"input\" label=\"refine, potential C-terminus modifications\">").append(cTerm).append("</note>").append(System.getProperty("line.separator"));

        String modString = "";
        for (String ptmName : refinementVariableMod) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            modString += ptm.getRoundedMass() + "@" + ptm.getPattern().getAminoAcidsAtTarget().get(0); //@TODO: check how multiple modifications at the same amino acid are supported in the refinement search
        }
        parametersSection.append("\t<note type=\"input\" label=\"refine, potential modification mass\">").append(modString).append("</note>").append(System.getProperty("line.separator"));

        String motifs = "";
        for (String ptmName : refinementVariableModMotif) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            motifs += ptm.getRoundedMass() + "@" + ptm.getPattern().getPrositeFormat(); //@TODO: check how multiple modifications at the same amino acid are supported in the refinement search
        }
        parametersSection.append("\t<note type=\"input\" label=\"refine, potential modification motif\">").append(motifs).append("</note>").append(System.getProperty("line.separator"));
        parametersSection.append("\t<note>The format of this parameter is similar to residue, modification mass,").append(System.getProperty("line.separator"));
        parametersSection.append("\t\twith the addition of a modified PROSITE notation sequence motif specification.").append(System.getProperty("line.separator"));
        parametersSection.append("\t\tFor example, a value of 80@[ST!]PX[KR] indicates a modification").append(System.getProperty("line.separator"));
        parametersSection.append("\t\tof either S or T when followed by P, and residue and the a K or an R.").append(System.getProperty("line.separator"));
        parametersSection.append("\t\tA value of 204@N!{P}[ST]{P} indicates a modification of N by 204, if it").append(System.getProperty("line.separator"));
        parametersSection.append("\t\tis NOT followed by a P, then either an S or a T, NOT followed by a P.").append(System.getProperty("line.separator"));
        parametersSection.append("\t\tPositive and negative values are allowed.").append(System.getProperty("line.separator"));
        parametersSection.append("\t\t</note>").append(System.getProperty("line.separator"));
        return parametersSection.toString();
    }

    /**
     * Returns the list of modifications as a String input to X!Tandem.
     *
     * @param variableModifications the map of modifications to parse
     * @return the list of modifications as a String
     * @throws IllegalArgumentException thrown if more than one fixed PTM has
     * the same target
     */
    private String getSearchedModList(ArrayList<String> variableModifications, ArrayList<String> fixedModifications) throws IllegalArgumentException {

        String completeModificationString = "";

        String variableModsString = "\t<note type=\"input\" label=\"residue, potential modification mass\">";
        String variableModsDescriptionString = "\t\t<note>";

        // get the sorted list of ptms, the keys in the maps are the target, and the values the ptms with that target
        HashMap<Character, ArrayList<PTM>> allVariableMods = sortModifications(variableModifications);
        HashMap<Character, ArrayList<PTM>> allFixedMods = sortModifications(fixedModifications);

        // list of ptms that were set as variable, but have to be set as "variable fixed"
        HashMap<Character, ArrayList<PTM>> variableFixedPtms = new HashMap<Character, ArrayList<PTM>>();

        for (Character target : allVariableMods.keySet()) {

            if (allVariableMods.get(target).size() == 1) {
                // unique target across all the variable ptms
                variableModsString += allVariableMods.get(target).get(0).getRoundedMass() + "@" + target + ",";
                variableModsDescriptionString += allVariableMods.get(target).get(0).getName() + ",";
            } else {
                // none-unique target, add to "variable fixed" ptms
                variableFixedPtms.put(target, allVariableMods.get(target));
            }
        }

        // remove the ending commas
        if (variableModsString.endsWith(",")) {
            variableModsString = variableModsString.substring(0, variableModsString.length() - 1);
            variableModsDescriptionString = variableModsDescriptionString.substring(0, variableModsDescriptionString.length() - 1);
        }

        // set the variable ptms
        variableModsString += "</note>" + System.getProperty("line.separator");
        variableModsDescriptionString += "</note>" + System.getProperty("line.separator");

        // fixed mods strings
        String fixedModsStringTemplate = "\t<note type=\"input\" label=\"residue, modification mass";
        String fixedModsStringDescriptionTemplate = "\t\t<note>";

        String fixedModsString = fixedModsStringTemplate + "\">";
        String fixedModsDescriptionString = fixedModsStringDescriptionTemplate;
        String defaultFixedModsString = "";
        String defaultFixedModsDescription = "";

        for (Character target : allFixedMods.keySet()) {

            if (allFixedMods.get(target).size() == 1) {
                // unique target across all the fixed ptms
                fixedModsString += allFixedMods.get(target).get(0).getRoundedMass() + "@" + target + ",";
                fixedModsDescriptionString += allFixedMods.get(target).get(0).getName() + ",";

                defaultFixedModsString += allFixedMods.get(target).get(0).getRoundedMass() + "@" + target + ",";
                defaultFixedModsDescription += allFixedMods.get(target).get(0).getName() + ",";
            } else {
                // non-unique targets for fixed ptms detected, this is not supported!!
                throw new IllegalArgumentException("More than one fixed modification with the same target was detected! Target: " + target + ". "
                        + "X!Tandem does not support this. Please replace by a single modification and try again.");
            }
        }

        // remove the ending commas
        if (fixedModsString.endsWith(",")) {
            fixedModsString = fixedModsString.substring(0, fixedModsString.length() - 1);
            fixedModsDescriptionString = fixedModsDescriptionString.substring(0, fixedModsDescriptionString.length() - 1);
        }

        // close the default fixed mods tag
        fixedModsString += "</note>" + System.getProperty("line.separator");
        fixedModsDescriptionString += "</note>" + System.getProperty("line.separator");

        // add the default fixed mods
        completeModificationString += fixedModsString + fixedModsDescriptionString;

        ArrayList<String> fixedSecondaryLines = new ArrayList<String>();
        ArrayList<String> fixedSecondaryLinesDescription = new ArrayList<String>();

        for (Character target : variableFixedPtms.keySet()) {

            ArrayList<String> newLines = new ArrayList<String>();
            ArrayList<String> newDescriptions = new ArrayList<String>();

            for (PTM tempPtm : variableFixedPtms.get(target)) {

                PTM currentPtm = tempPtm;
                String tempModsString = currentPtm.getRoundedMass() + "@" + target;
                String tempModsStringModsDescriptionString = currentPtm.getName();

                newLines.add(defaultFixedModsString + tempModsString);
                newDescriptions.add(defaultFixedModsDescription + tempModsStringModsDescriptionString);

                for (String previousLines : fixedSecondaryLines) {
                    newLines.add(previousLines + "," + tempModsString);
                }

                for (String previousLines : fixedSecondaryLinesDescription) {
                    newDescriptions.add(previousLines + "," + tempModsStringModsDescriptionString);
                }
            }

            fixedSecondaryLines.addAll(newLines);
            fixedSecondaryLinesDescription.addAll(newDescriptions);
        }

        // add the fixed variable mods
        for (int i = 0; i < fixedSecondaryLines.size(); i++) {

            // add the mods
            fixedModsString = fixedModsStringTemplate + " " + (i + 1) + "\">" + fixedSecondaryLines.get(i);
            fixedModsDescriptionString = fixedModsStringDescriptionTemplate + fixedSecondaryLinesDescription.get(i);

            // close the tags
            fixedModsString += "</note>" + System.getProperty("line.separator");
            fixedModsDescriptionString += "</note>" + System.getProperty("line.separator");

            // add to mods string
            completeModificationString += fixedModsString + fixedModsDescriptionString;
        }

        // add the variable mods to the mods string
        completeModificationString += variableModsString + variableModsDescriptionString;

        return completeModificationString;
    }

    /**
     * Sort the modifications according to their target.
     *
     * @param modifications the modifications to sort
     * @return the modifications sorted according to their target
     */
    private HashMap<Character, ArrayList<PTM>> sortModifications(ArrayList<String> modifications) {

        HashMap<Character, ArrayList<PTM>> sortedMods = new HashMap<Character, ArrayList<PTM>>();

        for (String name : modifications) {

            PTM ptm = ptmFactory.getSingleAAPTM(name);

            if (ptm.getType() == PTM.MODN
                    || ptm.getType() == PTM.MODNAA
                    || ptm.getType() == PTM.MODNP
                    || ptm.getType() == PTM.MODNPAA) {

                ArrayList<PTM> ptms;

                if (sortedMods.containsKey('[')) {
                    ptms = sortedMods.get('[');
                } else {
                    ptms = new ArrayList<PTM>();
                }

                ptms.add(ptm);
                sortedMods.put('[', ptms);
            }

            if (ptm.getPattern() != null) {
                for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {

                    ArrayList<PTM> ptms;

                    if (sortedMods.containsKey(aa)) {
                        ptms = sortedMods.get(aa);
                    } else {
                        ptms = new ArrayList<PTM>();
                    }

                    ptms.add(ptm);
                    sortedMods.put(aa, ptms);
                }
            }

            if (ptm.getType() == PTM.MODC
                    || ptm.getType() == PTM.MODCAA
                    || ptm.getType() == PTM.MODCP
                    || ptm.getType() == PTM.MODCPAA) {

                ArrayList<PTM> ptms;

                if (sortedMods.containsKey(']')) {
                    ptms = sortedMods.get(']');
                } else {
                    ptms = new ArrayList<PTM>();
                }

                ptms.add(ptm);
                sortedMods.put(']', ptms);
            }
        }

        return sortedMods;
    }

    /**
     * Returns the type of the process.
     *
     * @return the type of the process
     */
    public String getType() {
        return "X!Tandem Process";
    }

    /**
     * Returns the file name of the currently processed file.
     *
     * @return the file name of the currently processed file
     */
    public String getCurrentlyProcessedFileName() {
        return spectrumFile;
    }

    /**
     * Returns yes if the x ions are selected, no otherwise.
     *
     * @return whether the x ions are to be searched for.
     */
    public String getXSelected() {
        if (ion1.equals("x") || ion2.equals("x")) {
            return "yes";
        } else {
            return "no";
        }
    }

    /**
     * Returns yes if the y ions are selected, no otherwise.
     *
     * @return whether the y ions are to be searched for.
     */
    public String getYSelected() {
        if (ion1.equals("y") || ion2.equals("y")) {
            return "yes";
        } else {
            return "no";
        }
    }

    /**
     * Returns yes if the z ions are selected, no otherwise.
     *
     * @return whether the z ions are to be searched for.
     */
    public String getZSelected() {
        if (ion1.equals("z") || ion2.equals("z")) {
            return "yes";
        } else {
            return "no";
        }
    }

    /**
     * Returns yes if the a ions are selected, no otherwise.
     *
     * @return whether the a ions are to be searched for.
     */
    public String getASelected() {
        if (ion1.equals("a") || ion2.equals("a")) {
            return "yes";
        } else {
            return "no";
        }
    }

    /**
     * Returns yes if the b ions are selected, no otherwise.
     *
     * @return whether the b ions are to be searched for.
     */
    public String getBSelected() {
        if (ion1.equals("b") || ion2.equals("b")) {
            return "yes";
        } else {
            return "no";
        }
    }

    /**
     * Returns yes if the c ions are selected, no otherwise.
     *
     * @return whether the c ions are to be searched for.
     */
    public String getCSelected() {
        if (ion1.equals("c") || ion2.equals("c")) {
            return "yes";
        } else {
            return "no";
        }
    }
}
