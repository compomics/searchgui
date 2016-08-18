package eu.isas.searchgui.processbuilders;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.TideParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ProcessBuilder for the Tide index generation.
 *
 * @author Harald Barsnes
 */
public class TideIndexProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The name of the Tide executable.
     */
    public static final String EXECUTABLE_FILE_NAME = "crux";
    /**
     * The FASTA file.
     */
    private File fastaFile;
    /**
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * The advanced Tide parameters.
     */
    private TideParameters tideParameters;
    /**
     * The compomics PTM factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();

    /**
     * Constructor.
     *
     * @param tideFolder the Tide folder
     * @param searchParameters the search parameters
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     *
     * @throws IOException thrown of there are problems creating the Tide
     * parameter file
     */
    public TideIndexProcessBuilder(File tideFolder, SearchParameters searchParameters, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) throws IOException {

        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;
        this.searchParameters = searchParameters;
        tideParameters = (TideParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.tide.getIndex());
        this.fastaFile = searchParameters.getFastaFile();

        // make sure that the tide file is executable
        File tide = new File(tideFolder.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        tide.setExecutable(true);

        // full path to executable
        process_name_array.add(tide.getAbsolutePath());
        process_name_array.add("tide-index");

        // add the fasta file
        process_name_array.add(searchParameters.getFastaFile().getAbsolutePath());

        // the name of the index file
        process_name_array.add(tideParameters.getFastIndexFolderName()); // @TODO: put in the user temp folder instead?

        // overwrite existing files
        process_name_array.add("--overwrite");
        process_name_array.add("T");

        // add the modifications
        String nonTerminalMods = getNonTerminalModifications();
        if (!nonTerminalMods.isEmpty()) {
            process_name_array.add("--mods-spec");
            process_name_array.add(getNonTerminalModifications());
        }
        String nTermMods = getTerminalModifications(true);
        if (!nTermMods.isEmpty()) {
            process_name_array.add("--nterm-peptide-mods-spec");
            process_name_array.add(nTermMods);
        }
        String cTermMods = getTerminalModifications(false);
        if (!cTermMods.isEmpty()) {
            process_name_array.add("--cterm-peptide-mods-spec");
            process_name_array.add(cTermMods);
        }

        // the max number of modifications per peptide
        if (tideParameters.getMaxVariablePtmsPerPeptide() != null) {
            process_name_array.add("--max-mods");
            process_name_array.add(tideParameters.getMaxVariablePtmsPerPeptide().toString());
        }

        // the decoy format
        process_name_array.add("--decoy-format");
        process_name_array.add(tideParameters.getDecoyFormat());
        if (!tideParameters.getDecoyFormat().equalsIgnoreCase("none")) {
            process_name_array.add("--keep-terminal-aminos");
            process_name_array.add(tideParameters.getKeepTerminalAminoAcids());
            process_name_array.add("--seed");
            process_name_array.add(tideParameters.getDecoySeed().toString());
        }

        // missed cleavages
        process_name_array.add("--missed-cleavages");
        process_name_array.add("" + searchParameters.getnMissedCleavages());

        // set the output directory
        process_name_array.add("--output-dir");
        process_name_array.add(tideParameters.getOutputFolderName());

        // create peptide list
        process_name_array.add("--peptide-list");
        if (tideParameters.getPrintPeptides()) {
            process_name_array.add("T");
        } else {
            process_name_array.add("F");
        }

        // use parameter file
        //process_name_array.add("--parameter-file");
        //process_name_array.add("a file"); // @TODO: implement?
        //
        // output verbosity
        process_name_array.add("--verbosity");
        process_name_array.add(tideParameters.getVerbosity().toString());

        // peptide lengths
        process_name_array.add("--min-length");
        process_name_array.add(tideParameters.getMinPeptideLength().toString());
        process_name_array.add("--max-length");
        process_name_array.add(tideParameters.getMaxPeptideLength().toString());

        // peptide masses
        process_name_array.add("--min-mass");
        process_name_array.add(tideParameters.getMinPrecursorMass().toString());
        process_name_array.add("--max-mass");
        process_name_array.add(tideParameters.getMaxPrecursorMass().toString());

        // isotopic masses type used when calculating the peptide mass
        process_name_array.add("--isotopic-mass");
        if (tideParameters.getMonoisotopicPrecursor()) {
            process_name_array.add("mono");
        } else {
            process_name_array.add("average");
        }

        // include starting peptide both with and without the initial m
        process_name_array.add("--clip-nterm-methionine");
        if (tideParameters.getClipNtermMethionine()) {
            process_name_array.add("T");
        } else {
            process_name_array.add("F");
        }

        // enzyme
        //      note: Tide enzymes not implemented in utilities: 
        //          elastase ([ALIV]|{P}), clostripain ([R]|[]), iodosobenzoate ([W]|[]), proline-endopeptidase ([P]|[]), 
        //          staph-protease ([E]|[]), pepsin-a ([FL]|{P}), elastase-trypsin-chymotrypsin ([ALIVKRWFY]|{P})
        if (searchParameters.getEnzyme().getName().equals("Trypsin")) {
            process_name_array.add("--enzyme");
            process_name_array.add("trypsin");
        } else if (searchParameters.getEnzyme().getName().equals("Trypsin, no P rule")) {
            process_name_array.add("--enzyme");
            process_name_array.add("trypsin/p");
        } else if (searchParameters.getEnzyme().getName().equals("Chymotrypsin (FYWL)")) {
            process_name_array.add("--enzyme");
            process_name_array.add("chymotrypsin");
        } else if (searchParameters.getEnzyme().getName().equals("CNBr")) {
            process_name_array.add("--enzyme");
            process_name_array.add("cyanogen-bromide");
        } else if (searchParameters.getEnzyme().getName().equals("Asp-N")) {
            process_name_array.add("--enzyme");
            process_name_array.add("asp-n");
        } else if (searchParameters.getEnzyme().getName().equals("Lys-C")) {
            process_name_array.add("--enzyme");
            process_name_array.add("lys-c");
        } else if (searchParameters.getEnzyme().getName().equals("Lys-N (K)")) {
            process_name_array.add("--enzyme");
            process_name_array.add("lys-n");
        } else if (searchParameters.getEnzyme().getName().equals("Arg-C")) {
            process_name_array.add("--enzyme");
            process_name_array.add("arg-c");
        } else if (searchParameters.getEnzyme().getName().equals("Glu-C (DE)")) {
            process_name_array.add("--enzyme");
            process_name_array.add("glu-c");
        } else if (searchParameters.getEnzyme().getName().equals("Unspecific")) {
            process_name_array.add("--enzyme");
            process_name_array.add("no-enzyme");
        } else {
            process_name_array.add("--custom-enzyme");
            process_name_array.add(searchParameters.getEnzyme().getXTandemFormat());
        }

        // full or partial enzyme digestion
        process_name_array.add("--digestion");
        if (searchParameters.getEnzyme().isSemiSpecific()) {
            process_name_array.add("partial-digest");
        } else {
            process_name_array.add(tideParameters.getDigestionType());
        }

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "tide index command: ");

        for (Object currentElement : process_name_array) {
            System.out.print(currentElement + " ");
        }

        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);
        pb.directory(tideFolder);

        // set error out and std out to same stream
        pb.redirectErrorStream(true);
    }

    /**
     * Returns a string with the non-terminal modifications as a string in the
     * Tide format.
     *
     * @return the non-terminal modifications as a string in the Tide format.
     */
    private String getNonTerminalModifications() {

        String tempFixedNonTerminalModifications = getNonTerminalModifications(searchParameters.getPtmSettings().getFixedModifications(), true);
        String tempVariableNonTerminalModifications = getNonTerminalModifications(searchParameters.getPtmSettings().getVariableModifications(), false);

        if (!tempFixedNonTerminalModifications.isEmpty() && !tempVariableNonTerminalModifications.isEmpty()) {
            tempFixedNonTerminalModifications += "," + tempVariableNonTerminalModifications;
        } else if (!tempVariableNonTerminalModifications.isEmpty()) {
            return tempVariableNonTerminalModifications;
        }

        return tempFixedNonTerminalModifications;
    }

    /**
     * Get the non-terminal modifications as a string in the Tide format.
     *
     * @param modifications the modifications to check
     * @param fixed if the modifications are to to be added as fixed or variable
     * @return the non-terminal modifications as a string in the Tide forma
     */
    private String getNonTerminalModifications(ArrayList<String> modifications, boolean fixed) {

        // tide ptm pattern: [max_per_peptide]residues[+/-]mass_change
        String nonTerminalModifications = "";

        for (String ptmName : modifications) {

            PTM ptm = ptmFactory.getPTM(ptmName);

            if (!ptm.isNTerm() && !ptm.isCTerm()) {

                if (!nonTerminalModifications.isEmpty()) {
                    nonTerminalModifications += ",";
                }

                // add the number of allowed ptms per peptide
                if (!fixed) {
                    nonTerminalModifications += tideParameters.getMaxVariablePtmsPerTypePerPeptide(); // @TODO: make this modification specific?
                }

                // add the residues affected
                AminoAcidPattern ptmPattern = ptm.getPattern();
                if (ptmPattern != null && ptmPattern.length() > 0) {
                    for (Character aminoAcid : ptmPattern.getAminoAcidsAtTarget()) {
                        nonTerminalModifications += aminoAcid;
                    }
                }

                // add the ptm mass
                if (ptm.getRoundedMass() > 0) {
                    nonTerminalModifications += "+";
                }
                nonTerminalModifications += ptm.getRoundedMass();
            }
        }

        return nonTerminalModifications;
    }

    /**
     * Returns a string with the terminal modifications as a string in the Tide
     * format.
     *
     * @param nTerm true if the modifications are n-terminal, false if
     * c-terminal
     * @return the terminal modifications as a string in the Tide format.
     */
    private String getTerminalModifications(boolean nTerm) {

        String tempNTermModifications = getTerminalModifications(searchParameters.getPtmSettings().getFixedModifications(), true, nTerm);
        String tempCTermModifications = getTerminalModifications(searchParameters.getPtmSettings().getVariableModifications(), false, nTerm);

        if (!tempNTermModifications.isEmpty() && !tempCTermModifications.isEmpty()) {
            tempNTermModifications += "," + tempCTermModifications;
        } else if (!tempCTermModifications.isEmpty()) {
            return tempCTermModifications;
        }

        return tempNTermModifications;
    }

    /**
     * Get the terminal modifications as a string in the Tide format.
     *
     * @param modifications the modifications to check
     * @param fixed if the modifications are to to be added as fixed or variable
     * @param nTerm true if the modifications are n-terminal, false if
     * c-terminal
     * @return the terminal modifications as a string in the Tide format
     */
    private String getTerminalModifications(ArrayList<String> modifications, boolean fixed, boolean nTerm) {

        String terminalModifications = "";

        for (String ptmName : modifications) {

            PTM ptm = ptmFactory.getPTM(ptmName);

            if ((ptm.isNTerm() && nTerm) || (ptm.isCTerm() && !nTerm)) {

                if (!terminalModifications.isEmpty()) {
                    terminalModifications += ",";
                }

                // add the number of allowed ptms per peptide
                if (!fixed) {
                    terminalModifications += "1";
                }

                // add the residues affected 
                AminoAcidPattern ptmPattern = ptm.getPattern();
                String tempPtmPattern = "";
                if (ptmPattern != null && ptmPattern.length() > 0) {
                    for (Character aminoAcid : ptmPattern.getAminoAcidsAtTarget()) {
                        tempPtmPattern += aminoAcid;
                    }
                }

                if (tempPtmPattern.length() == 0) {
                    tempPtmPattern = "X";
                }

                terminalModifications += tempPtmPattern;

                // add the ptm mass
                if (ptm.getRoundedMass() > 0) {
                    terminalModifications += "+";
                }
                terminalModifications += ptm.getRoundedMass();
            }
        }

        return terminalModifications;
    }

    @Override
    public String getType() {
        return "Tide Indexing";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return fastaFile.getName();
    }
}
