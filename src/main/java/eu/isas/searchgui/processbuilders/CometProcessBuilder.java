package eu.isas.searchgui.processbuilders;

import com.compomics.util.Util;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.CometParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.CometParameters.CometOutputFormat;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * ProcessBuilder for the Comet search engine.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class CometProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The Comet folder.
     */
    private File cometFolder;
    /**
     * The temp folder for Comet files.
     */
    private static String cometTempFolderPath;
    /**
     * The name of the temp sub folder for Comet files.
     */
    private static String cometTempSubFolderName = "temp";
    /**
     * The name of the Comet executable.
     */
    public static final String EXECUTABLE_FILE_NAME = "comet.exe";
    /**
     * The Comet version number as a string.
     */
    private final String COMET_VERSION = "2015.02 rev. 1"; // @TODO: extract from the comet usage details?
    /**
     * The spectrum file.
     */
    private File spectrumFile;
    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The advanced Comet parameters.
     */
    private CometParameters cometParameters;
    /**
     * The number of threads to use.
     */
    private int nThreads;
    /**
     * The compomics PTM factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * A reference mass to convert fragment ion tolerance from ppm to Dalton.
     */
    private Double refMass;

    /**
     * Constructor.
     *
     * @param cometFolder the Comet folder
     * @param searchParameters the search parameters
     * @param spectrumFile the spectrum file
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     * @param nThreads the number of threads
     * @param refMass a reference mass to convert fragment ion tolerance from
     * ppm to Dalton
     *
     * @throws IOException thrown if there are problems creating the Comet
     * parameter file
     */
    public CometProcessBuilder(File cometFolder, SearchParameters searchParameters, File spectrumFile,
            WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, int nThreads, Double refMass) throws IOException {

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        this.cometFolder = cometFolder;
        this.searchParameters = searchParameters;
        cometParameters = (CometParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.comet.getIndex());
        this.spectrumFile = spectrumFile;
        this.nThreads = nThreads;
        this.refMass = refMass;

        createParametersFile();

        // make sure that the comet file is executable
        File comet = new File(cometFolder.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        comet.setExecutable(true);

        // full path to executable
        process_name_array.add(comet.getAbsolutePath());

        // link to the parameter file
        if (cometTempFolderPath != null && !cometFolder.getAbsolutePath().equalsIgnoreCase(cometTempFolderPath)) {
            String path = new File(cometTempFolderPath, "comet.params").getPath();
            process_name_array.add("-P" + path);
        }

        // link to the input file
        process_name_array.add(spectrumFile.getAbsolutePath());

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "comet command: ");

        for (Object currentElement : process_name_array) {
            System.out.print(currentElement + " ");
        }

        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);
        pb.directory(cometFolder);

        // set error out and std out to same stream
        pb.redirectErrorStream(true);
    }

    /**
     * Create the parameters file.
     *
     * @throws IOException
     */
    private void createParametersFile() throws IOException {

        File cometTempFolder;
        if (cometTempFolderPath == null) {
            cometTempFolder = cometFolder;
        } else {
            cometTempFolder = new File(cometTempFolderPath);
        }

        // create the temp folder if it doesn't exist
        if (!cometTempFolder.exists()) {
            cometTempFolder.mkdirs();
        }

        BufferedWriter br = new BufferedWriter(new FileWriter(new File(cometTempFolder, "comet.params")));

        String precursorToleranceType; // @TODO: what about mmu?
        if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.DA) {
            precursorToleranceType = "0";
        } else {
            precursorToleranceType = "2";
        }

        String theoretical_Fragment_ions;
        if (cometParameters.getTheoreticalFragmentIonsSumOnly()) {
            theoretical_Fragment_ions = "1";
        } else {
            theoretical_Fragment_ions = "0";
        }

        String clip_nterm_methionine;
        if (cometParameters.getRemoveMethionine()) {
            clip_nterm_methionine = "1";
        } else {
            clip_nterm_methionine = "0";
        }

        Integer enzymeType = cometParameters.getEnzymeType();
        if (searchParameters.getEnzyme().isSemiSpecific()) {
            enzymeType = 1;
        }

        try {
            br.write(
                    /////////////////////////
                    // comet header
                    /////////////////////////
                    "# comet_version " + COMET_VERSION + System.getProperty("line.separator")
                    + "# Comet MS/MS search engine parameters file." + System.getProperty("line.separator")
                    + "# Everything following the '#' symbol is treated as a comment." + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    /////////////////////////
                    // database details
                    /////////////////////////
                    + "database_name = " + searchParameters.getFastaFile().getAbsolutePath() + System.getProperty("line.separator")
                    + "decoy_search = 0 # 0=no (default), 1=concatenated search, 2=separate search" + System.getProperty("line.separator")
                    /////////////////////////
                    // number of threads
                    /////////////////////////
                    + "num_threads = " + nThreads + " # 0=poll CPU to set num threads; else specify num threads directly (max 64)" + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    /////////////////////////
                    // precursor details
                    /////////////////////////
                    + "#" + System.getProperty("line.separator")
                    + "# masses" + System.getProperty("line.separator")
                    + "#" + System.getProperty("line.separator")
                    + "peptide_mass_tolerance = " + searchParameters.getPrecursorAccuracy() + System.getProperty("line.separator")
                    + "peptide_mass_units = " + precursorToleranceType + "           # 0=amu, 1=mmu, 2=ppm" + System.getProperty("line.separator")
                    + "mass_type_parent = 1                   # 0=average masses, 1=monoisotopic masses" + System.getProperty("line.separator")
                    + "mass_type_fragment = 1                 # 0=average masses, 1=monoisotopic masses" + System.getProperty("line.separator")
                    + "precursor_tolerance_type = 0           # 0=MH+ (default), 1=precursor m/z; only valid for amu/mmu tolerances" + System.getProperty("line.separator")
                    + "isotope_error = " + cometParameters.getIsotopeCorrection() + "           # 0=off, 1=on -1/0/1/2/3 (standard C13 error), 2= -8/-4/0/4/8 (for +4/+8 labeling)" + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    /////////////////////////
                    // enzyme
                    /////////////////////////
                    + "#" + System.getProperty("line.separator")
                    + "# search enzyme" + System.getProperty("line.separator")
                    + "#" + System.getProperty("line.separator")
                    + "search_enzyme_number = " + searchParameters.getEnzyme().getId() + "           # choose from list at end of this params file" + System.getProperty("line.separator")
                    + "num_enzyme_termini = " + enzymeType + "           # valid values are 1 (semi-digested), 2 (fully digested, default), 8 N-term, 9 C-term" + System.getProperty("line.separator")
                    + "allowed_missed_cleavage = " + searchParameters.getnMissedCleavages() + "           # maximum value is 5; for enzyme search" + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    /////////////////////////
                    // variable modifications
                    /////////////////////////
                    + getVariableModifications()
                    + System.getProperty("line.separator")
                    /////////////////////////
                    // fragment ions
                    /////////////////////////
                    + "#" + System.getProperty("line.separator")
                    + "# fragment ions" + System.getProperty("line.separator")
                    + "#" + System.getProperty("line.separator")
                    + "# ion trap ms/ms:  1.0005 tolerance, 0.4 offset (mono masses), theoretical_fragment_ions = 1" + System.getProperty("line.separator")
                    + "# high res ms/ms:    0.02 tolerance, 0.0 offset (mono masses), theoretical_fragment_ions = 0" + System.getProperty("line.separator")
                    + "#" + System.getProperty("line.separator")
                    // @TODO: is fragment_bin_tol really fragment ion accuracy? (and set the offset and theoretical_fragment_ions automatically?)
                    + "fragment_bin_tol = " + searchParameters.getFragmentIonAccuracyInDaltons(refMass) + " # binning to use on fragment ions" + System.getProperty("line.separator")
                    + "fragment_bin_offset = " + cometParameters.getFragmentBinOffset() + " # offset position to start the binning (0.0 to 1.0)" + System.getProperty("line.separator")
                    + "theoretical_fragment_ions = " + theoretical_Fragment_ions + " # 0=use flanking peaks, 1=M peak only" + System.getProperty("line.separator")
                    + getIonsSearched()
                    + "use_NL_ions = 1 # 0=no, 1=yes to consider NH3/H2O neutral loss peaks" + System.getProperty("line.separator") // @TODO: set the neutral losses
                    + System.getProperty("line.separator")
                    /////////////////////////
                    // output settings
                    /////////////////////////
                    + "#" + System.getProperty("line.separator")
                    + "# output" + System.getProperty("line.separator")
                    + "#" + System.getProperty("line.separator")
                    + "output_sqtstream = 0                   # 0=no, 1=yes  write sqt to standard output" + System.getProperty("line.separator")
                    + "output_sqtfile = " + outputFormat(CometOutputFormat.SQT) + "                 # 0=no, 1=yes  write sqt file" + System.getProperty("line.separator")
                    + "output_txtfile = " + outputFormat(CometOutputFormat.TXT) + "                 # 0=no, 1=yes  write tab-delimited txt file" + System.getProperty("line.separator")
                    + "output_pepxmlfile = " + outputFormat(CometOutputFormat.PepXML) + "                 # 0=no, 1=yes  write pep.xml file" + System.getProperty("line.separator")
                    + "output_percolatorfile = " + outputFormat(CometOutputFormat.Percolator) + "                 # 0=no, 1=yes  write Percolator tab-delimited input file" + System.getProperty("line.separator")
                    + "output_outfiles = 0                 # 0=no, 1=yes  write .out files" + System.getProperty("line.separator")
                    + "print_expect_score = " + Util.convertBooleanToInteger(cometParameters.getPrintExpectScore()) + "                 # 0=no, 1=yes to replace Sp with expect in out & sqt" + System.getProperty("line.separator")
                    + "num_output_lines = " + cometParameters.getNumberOfSpectrumMatches() + "                 # num peptide results to show" + System.getProperty("line.separator")
                    + "show_fragment_ions = 0                 # 0=no, 1=yes for out files only" + System.getProperty("line.separator")
                    + "sample_enzyme_number = " + searchParameters.getEnzyme().getId() + "               # Sample enzyme which is possibly different than the one applied to the search." + System.getProperty("line.separator") // @TODO: set enzyme!
                    + "                                       # Used to calculate NTT & NMC in pepXML output (default=1 for trypsin)." + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    /////////////////////////
                    // mzXML parameters
                    /////////////////////////
                    + "#" + System.getProperty("line.separator")
                    + "# mzXML parameters" + System.getProperty("line.separator")
                    + "#" + System.getProperty("line.separator")
                    + "scan_range = 0 0                       # start and scan scan range to search; 0 as 1st entry ignores parameter" + System.getProperty("line.separator")
                    + "precursor_charge = 0 0                 # precursor charge range to analyze; does not override any existing charge; 0 as 1st entry ignores parameter" + System.getProperty("line.separator")
                    + "override_charge = 0                    # whether to override existing precursor charge state information when present in the files with the charge range specified by the \"precursor_charge\" parameter - valid values are 0, 1, 2 or 3" + System.getProperty("line.separator")
                    + "ms_level = 2                           # MS level to analyze, valid are levels 2 (default) or 3" + System.getProperty("line.separator")
                    + "activation_method = ALL                # activation method; used if activation method set; allowed ALL, CID, ECD, ETD, PQD, HCD, IRMPD" + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    /////////////////////////
                    // misc parameters
                    /////////////////////////
                    + "#" + System.getProperty("line.separator")
                    + "# misc parameters" + System.getProperty("line.separator")
                    + "#" + System.getProperty("line.separator")
                    + "digest_mass_range = " + cometParameters.getMinPrecursorMass() + " " + cometParameters.getMaxPrecursorMass() + "                 # MH+ peptide mass range to analyze" + System.getProperty("line.separator")
                    + "num_results = " + cometParameters.getNumberOfSpectrumMatches() + "                 # number of search hits to store internally" + System.getProperty("line.separator")
                    + "skip_researching = 1                   # for '.out' file output only, 0=search everything again (default), 1=don't search if .out exists" + System.getProperty("line.separator")
                    + "max_fragment_charge = " + cometParameters.getMaxFragmentCharge() + "                 # set maximum fragment charge state to analyze (allowed max 5)" + System.getProperty("line.separator")
                    + "max_precursor_charge = " + searchParameters.getMaxChargeSearched() + "                 # set maximum precursor charge state to analyze (allowed max 9)" + System.getProperty("line.separator")
                    + "nucleotide_reading_frame = 0           # 0=proteinDB, 1-6, 7=forward three, 8=reverse three, 9=all six" + System.getProperty("line.separator")
                    + "clip_nterm_methionine = " + clip_nterm_methionine + "                 # 0=leave sequences as-is; 1=also consider sequence w/o N-term methionine" + System.getProperty("line.separator")
                    + "spectrum_batch_size = " + cometParameters.getBatchSize() + "                 # max. # of spectra to search at a time; 0 to search the entire scan range in one loop" + System.getProperty("line.separator")
                    + "decoy_prefix = DECOY_                  # decoy entries are denoted by this string which is pre-pended to each protein accession" + System.getProperty("line.separator")
                    + "output_suffix = .comet                 # add a suffix to output base names i.e. suffix \"-C\" generates base-C.pep.xml from base.mzXML input" + System.getProperty("line.separator")
                    + "mass_offsets =                         # one or more mass offsets to search (values substracted from deconvoluted precursor mass)" + System.getProperty("line.separator") // @TODO: implement?
                    + System.getProperty("line.separator")
                    /////////////////////////
                    // spectral processing
                    /////////////////////////
                    + "#" + System.getProperty("line.separator")
                    + "# spectral processing" + System.getProperty("line.separator")
                    + "#" + System.getProperty("line.separator")
                    + "minimum_peaks = " + cometParameters.getMinPeaks() + "                 # required minimum number of peaks in spectrum to search (default 10)" + System.getProperty("line.separator")
                    + "minimum_intensity = " + cometParameters.getMinPeakIntensity() + "                 # minimum intensity value to read in" + System.getProperty("line.separator")
                    + "remove_precursor_peak = " + cometParameters.getRemovePrecursor() + "                 # 0=no, 1=yes, 2=all charge reduced precursor peaks (for ETD)" + System.getProperty("line.separator")
                    + "remove_precursor_tolerance = " + cometParameters.getRemovePrecursorTolerance() + "                 # +- Da tolerance for precursor removal" + System.getProperty("line.separator")
                    + "clear_mz_range = " + cometParameters.getLowerClearMzRange() + " " + cometParameters.getUpperClearMzRange() + "                 # for iTRAQ/TMT type data; will clear out all peaks in the specified m/z range" + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    /////////////////////////
                    // fixed modifications
                    /////////////////////////
                    + getFixedModifications()
                    /////////////////////////
                    // enzyme properties
                    /////////////////////////
                    + getEnzymeListing()
            );
        } finally {
            br.close();
        }
    }

    /**
     * Returns the ion types used as a string.
     *
     * @return the ion types
     */
    private String getIonsSearched() {

        String ions = "";

        if (null != searchParameters.getIonSearched1()) {
            switch (searchParameters.getIonSearched1()) {
                case PeptideFragmentIon.A_ION:
                    ions += "use_A_ions = 1" + System.getProperty("line.separator");
                    ions += "use_B_ions = 0" + System.getProperty("line.separator");
                    ions += "use_C_ions = 0" + System.getProperty("line.separator");
                    break;
                case PeptideFragmentIon.B_ION:
                    ions += "use_A_ions = 0" + System.getProperty("line.separator");
                    ions += "use_B_ions = 1" + System.getProperty("line.separator");
                    ions += "use_C_ions = 0" + System.getProperty("line.separator");
                    break;
                case PeptideFragmentIon.C_ION:
                    ions += "use_A_ions = 0" + System.getProperty("line.separator");
                    ions += "use_B_ions = 0" + System.getProperty("line.separator");
                    ions += "use_C_ions = 1" + System.getProperty("line.separator");
                    break;
                default:
                    break;
            }
        }

        if (null != searchParameters.getIonSearched2()) {
            switch (searchParameters.getIonSearched2()) {
                case PeptideFragmentIon.X_ION:
                    ions += "use_X_ions = 1" + System.getProperty("line.separator");
                    ions += "use_Y_ions = 0" + System.getProperty("line.separator");
                    ions += "use_Z_ions = 0" + System.getProperty("line.separator");
                    break;
                case PeptideFragmentIon.Y_ION:
                    ions += "use_X_ions = 0" + System.getProperty("line.separator");
                    ions += "use_Y_ions = 1" + System.getProperty("line.separator");
                    ions += "use_Z_ions = 0" + System.getProperty("line.separator");
                    break;
                case PeptideFragmentIon.Z_ION:
                    ions += "use_X_ions = 0" + System.getProperty("line.separator");
                    ions += "use_Y_ions = 0" + System.getProperty("line.separator");
                    ions += "use_Z_ions = 1" + System.getProperty("line.separator");
                    break;
                default:
                    break;
            }
        }

        return ions;
    }

    /**
     * Returns a string with the variable modifications.
     *
     * @return the variable modifications
     */
    private String getVariableModifications() {

        StringBuilder result = new StringBuilder("#" + System.getProperty("line.separator")
                + "# Up to 9 variable modifications are supported" + System.getProperty("line.separator")
                + "# format:  <mass> <residues> <0=variable/1=binary set> <max_mods_per_peptide> <term_distance> <n/c-term> <required>" + System.getProperty("line.separator")
                + "#     e.g. 79.966331 STY 0 3 -1 0 0" + System.getProperty("line.separator")
                + "#" + System.getProperty("line.separator"));

        int cpt = 0;

        for (String ptmName : searchParameters.getPtmSettings().getVariableModifications()) {

            // get the ptm
            PTM ptm = ptmFactory.getPTM(ptmName);
            result.append("variable_mod");
            if (++cpt < 10) {
                result.append("0");
            }
            result.append(cpt);
            result.append(" = ");

            // add ptm mass
            result.append(ptm.getRoundedMass());
            result.append(" ");

            // find targeted residues
            StringBuilder ptmCometPattern = new StringBuilder();
            AminoAcidPattern ptmPattern = ptm.getPattern();
            if (ptmPattern != null && ptmPattern.length() > 0) {
                for (Character aminoAcid : ptmPattern.getAminoAcidsAtTarget()) {
                    ptmCometPattern.append(aminoAcid);
                }
            }

            // add targeted residues
            if (ptmCometPattern.length() == 0) {
                if (ptm.isCTerm()) {
                    result.append("c");
                } else if (ptm.isNTerm()) {
                    result.append("n");
                } else {
                    result.append("X");
                }
            } else {
                result.append(ptmCometPattern);
            }

            // add variable ptm tag:
            //      0 = variable modification analyzes all permutations of modified and unmodified residues
            //      non-zero value = a binary modification analyzes peptides where all residues are either modified or all residues are not modified
            result.append(" 0 "); // @TODO: add support for binary modification sets?

            // add max copies of this ptm per peptide
            if (ptm.isNTerm() || ptm.isCTerm()) {
                result.append("1 ");
            } else {
                result.append("3 "); // @TODO: make this a user parameter?
            }

            // add distance to the terminus constraint // @TODO: possible to make this a user param?
            //      -1 = no distance contraint, 
            //      0 = only applies to terminal residue, 
            //      1 = only applies to terminal residue and next residue, 
            //      2 = only applies to terminal residue through next 2 residues, 
            //      N = only applies to terminal residue through next N residues where N is a positive integer)
            if (ptm.isNTerm() || ptm.isCTerm()) {
                result.append("0 ");
            } else {
                result.append("-1 ");
            }

            // add which terminus the terminus constraint applies to (protein or peptide, c or n term)
            if (ptm.isNTerm()) {
                if (ptm.getType() == PTM.MODN || ptm.getType() == PTM.MODNAA) {
                    result.append("0 ");
                } else if (ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODNPAA) {
                    result.append("2 ");
                }
            } else if (ptm.isCTerm()) {
                if (ptm.getType() == PTM.MODC || ptm.getType() == PTM.MODCAA) {
                    result.append("1 ");
                } else if (ptm.getType() == PTM.MODCP || ptm.getType() == PTM.MODCPAA) {
                    result.append("3 ");
                }
            } else {
                result.append("0 "); // cause we have to add something...
            }

            // add whether peptides must contain this modification
            //      0 = not forced to be present
            //      1 = modification is required 
            result.append("0"); // @TODO: make this a user parameter?

            result.append(System.getProperty("line.separator"));
        }

        // add empty lines for the remaining ptm parameter lines
        while (++cpt < 10) {
            result.append("variable_mod");
            if (cpt < 10) {
                result.append("0");
            }
            result.append(cpt);
            result.append(" = 0.0 X 0 3 -1 0 0");
            result.append(System.getProperty("line.separator"));
        }

        // set the max variable ptms per peptide
        result.append("max_variable_mods_in_peptide = ").append(cometParameters.getMaxVariableMods()).append(System.getProperty("line.separator"));

        // require at least one variable ptm per peptide
        if (cometParameters.getRequireVariableMods()) {
            result.append("require_variable_mod = 1").append(System.getProperty("line.separator"));
        } else {
            result.append("require_variable_mod = 0").append(System.getProperty("line.separator"));
        }

        return result.toString();
    }

    /**
     * Returns a string with the fixed modifications.
     *
     * @return the fixed modifications
     */
    private String getFixedModifications() {

        HashMap<Character, Double> residueToModificationMap = new HashMap<Character, Double>();
        double proteinCtermModification = 0,
                proteinNtermModification = 0,
                peptideCtermModification = 0,
                peptideNTermModification = 0;

        for (String ptmName : searchParameters.getPtmSettings().getFixedModifications()) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            switch (ptm.getType()) {
                case PTM.MODAA:
                    for (Character aminoAcid : ptm.getPattern().getAminoAcidsAtTarget()) {
                        Double modification = residueToModificationMap.get(aminoAcid);
                        if (modification == null) {
                            residueToModificationMap.put(aminoAcid, ptm.getRoundedMass());
                        } else {
                            residueToModificationMap.put(aminoAcid, modification + ptm.getRoundedMass());
                        }
                    }
                    break;
                case PTM.MODC:
                    proteinCtermModification += ptm.getRoundedMass();
                    break;
                case PTM.MODN:
                    proteinNtermModification += ptm.getRoundedMass();
                    break;
                case PTM.MODCP:
                    peptideCtermModification += ptm.getRoundedMass();
                    break;
                case PTM.MODNP:
                    peptideNTermModification += ptm.getRoundedMass();
                    break;
                default:
                    break;
            }
        }

        StringBuilder result = new StringBuilder();
        result.append("#").append(System.getProperty("line.separator")).append("# additional modifications").append(System.getProperty("line.separator"));
        result.append("#").append(System.getProperty("line.separator"));
        result.append(System.getProperty("line.separator"));
        result.append("add_Cterm_peptide = ").append(peptideCtermModification).append(System.getProperty("line.separator"));
        result.append("add_Nterm_peptide = ").append(peptideNTermModification).append(System.getProperty("line.separator"));
        result.append("add_Cterm_protein = ").append(proteinCtermModification).append(System.getProperty("line.separator"));
        result.append("add_Nterm_protein = ").append(proteinNtermModification).append(System.getProperty("line.separator"));
        result.append(System.getProperty("line.separator"));

        Double modifiedMass = residueToModificationMap.get('G');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_G_glycine = ").append(modifiedMass).append("                 # added to G - avg.  57.0513, mono.  57.02146").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('A');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_A_alanine = ").append(modifiedMass).append("                 # added to A - avg.  71.0779, mono.  71.03711").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('S');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_S_serine = ").append(modifiedMass).append("                 # added to S - avg.  87.0773, mono.  87.03203").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('P');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_P_proline = ").append(modifiedMass).append("                 # added to P - avg.  97.1152, mono.  97.05276").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('V');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_V_valine = ").append(modifiedMass).append("                 # added to V - avg.  99.1311, mono.  99.06841").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('T');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_T_threonine = ").append(modifiedMass).append("                 # added to T - avg. 101.1038, mono. 101.04768").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('C');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_C_cysteine = ").append(modifiedMass).append("                 # added to C - avg. 103.1429, mono. 103.00918").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('L');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_L_leucine = ").append(modifiedMass).append("                 # added to L - avg. 113.1576, mono. 113.08406").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('I');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_I_isoleucine = ").append(modifiedMass).append("                 # added to I - avg. 113.1576, mono. 113.08406").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('N');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_N_asparagine = ").append(modifiedMass).append("                 # added to N - avg. 114.1026, mono. 114.04293").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('D');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_D_aspartic_acid = ").append(modifiedMass).append("                 # added to D - avg. 115.0874, mono. 115.02694").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('Q');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_Q_glutamine = ").append(modifiedMass).append("                 # added to Q - avg. 128.1292, mono. 128.05858").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('K');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_K_lysine = ").append(modifiedMass).append("                 # added to K - avg. 128.1723, mono. 128.09496").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('E');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_E_glutamic_acid = ").append(modifiedMass).append("                 # added to E - avg. 129.1140, mono. 129.04259").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('M');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_M_methionine = ").append(modifiedMass).append("                 # added to M - avg. 131.1961, mono. 131.04048").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('O');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_O_ornithine = ").append(modifiedMass).append("                 # added to O - avg. 132.1610, mono  132.08988").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('H');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_H_histidine = ").append(modifiedMass).append("                 # added to H - avg. 137.1393, mono. 137.05891").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('F');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_F_phenylalanine = ").append(modifiedMass).append("                 # added to F - avg. 147.1739, mono. 147.06841").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('R');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_R_arginine = ").append(modifiedMass).append("                 # added to R - avg. 156.1857, mono. 156.10111").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('Y');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_Y_tyrosine = ").append(modifiedMass).append("                 # added to Y - avg. 163.0633, mono. 163.06333").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('W');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_W_tryptophan = ").append(modifiedMass).append("                 # added to W - avg. 186.0793, mono. 186.07931").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('B');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_B_user_amino_acid = ").append(modifiedMass).append("                 # added to B - avg.   0.0000, mono.   0.00000").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('J');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_J_user_amino_acid = ").append(modifiedMass).append("                 # added to J - avg. 113.1576, mono. 113.08406").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('U');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_U_user_amino_acid = ").append(modifiedMass).append("                 # added to U - avg.   0.0000, mono.   0.00000").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('X');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_X_user_amino_acid = ").append(modifiedMass).append("                 # added to X - avg.   0.0000, mono.   0.00000").append(System.getProperty("line.separator"));
        modifiedMass = residueToModificationMap.get('Z');
        if (modifiedMass == null) {
            modifiedMass = 0.0;
        }
        result.append("add_Z_user_amino_acid = ").append(modifiedMass).append("                 # added to Z - avg.   0.0000, mono.   0.00000").append(System.getProperty("line.separator"));
        result.append(System.getProperty("line.separator"));

        return result.toString();
    }

    /**
     * Returns the list of enzymes to choose from.
     *
     * @return the list of enzymes
     */
    private String getEnzymeListing() {

        String enzymesAsString = "#" + System.getProperty("line.separator")
                + "# COMET_ENZYME_INFO _must_ be at the end of this parameters file" + System.getProperty("line.separator")
                + "#" + System.getProperty("line.separator")
                + "[COMET_ENZYME_INFO]" + System.getProperty("line.separator");

        EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
        TreeMap<Integer, String> enzymeMap = new TreeMap<Integer, String>();

        for (Enzyme enzyme : enzymeFactory.getEnzymes()) {

            if (enzyme.isWholeProtein()) { // whole protein and top-down
                String enzymeAsString = enzyme.getId() + ". "
                        + enzyme.getName().replaceAll(" ", "_") + " "
                        + "0 "
                        + "$ "
                        + "- "
                        + System.getProperty("line.separator");
                enzymeMap.put(enzyme.getId(), enzymeAsString);
            } else if (enzyme.isUnspecific()) { // unspecific cleavage
                String enzymeAsString = enzyme.getId() + ". "
                        + enzyme.getName().replaceAll(" ", "_") + " "
                        + "0 "
                        + "- "
                        + "- "
                        + System.getProperty("line.separator");
                enzymeMap.put(enzyme.getId(), enzymeAsString);
            } else {

                String cleavageType;
                String cleavageSite = "";
                String restriction = "-";

                if (enzyme.getAminoAcidBefore().isEmpty()) {
                    cleavageType = "0";
                    for (Character character : enzyme.getAminoAcidAfter()) {
                        cleavageSite += character;
                    }
                    if (!enzyme.getRestrictionBefore().isEmpty()) {
                        restriction = "";
                        for (Character character : enzyme.getRestrictionBefore()) {
                            restriction += character;
                        }
                    }
                } else {
                    cleavageType = "1";
                    for (Character character : enzyme.getAminoAcidBefore()) {
                        cleavageSite += character;
                    }
                    if (!enzyme.getRestrictionAfter().isEmpty()) {
                        restriction = "";
                        for (Character character : enzyme.getRestrictionAfter()) {
                            restriction += character;
                        }
                    }
                }

                String enzymeAsString = enzyme.getId() + ". "
                        + enzyme.getName().replaceAll(" ", "_") + " "
                        + cleavageType + " "
                        + cleavageSite + " "
                        + restriction
                        + System.getProperty("line.separator");

                enzymeMap.put(enzyme.getId(), enzymeAsString);
            }
        }

        Iterator<Integer> keys = enzymeMap.keySet().iterator();
        while (keys.hasNext()) {
            enzymesAsString += enzymeMap.get(keys.next());
        }

        return enzymesAsString;
    }

    @Override
    public String getType() {
        return "Comet";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return spectrumFile.getName();
    }

    /**
     * Returns the temp folder to use for Comet files. Null if not set.
     *
     * @return the temp folder to use for Comet files
     */
    public static String getTempFolder() {
        return cometTempFolderPath;
    }

    /**
     * Sets the temp folder to use for Comet files. If null the Comet folder
     * will be used. NB: Cannot contain spaces.
     *
     * @param cometTempFolder the temp folder to use for Comet files.
     */
    public static void setTempFolder(String cometTempFolder) {
        CometProcessBuilder.cometTempFolderPath = cometTempFolder;
    }

    /**
     * Returns the correct integer value to indicate whether the given output
     * format is to be used or not.
     *
     * @return 1 if the given output is to be generated, 0 otherwise
     */
    private int outputFormat(CometOutputFormat cometOutputFormat) {
        if (cometParameters.getSelectedOutputFormat() == cometOutputFormat) {
            return 1;
        } else {
            return 0;
        }
    }
}
