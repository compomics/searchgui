package eu.isas.searchgui.processbuilders;

import com.compomics.software.cli.CommandLineUtils;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.DigestionParameters.Specificity;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.OmssaParameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class takes care of building the omssacl process.
 *
 * @author Lennart Martens
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class OmssaclProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The spectrum file.
     */
    private File spectrumFile;
    /**
     * The modification parameters of this search.
     */
    private ModificationParameters modificationParameters;
    /**
     * The name of the OMSSA executable.
     */
    public final static String EXECUTABLE_FILE_NAME = "omssacl";
    /**
     * The temp folder for OMSSA files.
     */
    private File omssaTempFolder;

    /**
     * Constructor.
     *
     * @param omssacl_directory directory location of omssacl.exe
     * @param aOmssaTempFolder the OMSSA temp folder
     * @param mgfFile the spectrum file
     * @param fastaFile the FASTA file
     * @param outputFile string location where to send omx/csv/pepxml formatted
     * results file
     * @param searchParameters the search parameters
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     * @param refMass the reference mass to convert the fragment ion tolerance
     * from ppm to Dalton
     * @param nThreads the number of threads to use
     *
     * @throws java.io.IOException exception thrown whenever an error occurred
     * while reading or writing a file.
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while saving the search parameters
     */
    public OmssaclProcessBuilder(
            File omssacl_directory,
            File aOmssaTempFolder,
            File mgfFile,
            File fastaFile,
            File outputFile,
            SearchParameters searchParameters,
            WaitingHandler waitingHandler,
            ExceptionHandler exceptionHandler,
            Double refMass,
            int nThreads
    ) throws IOException, ClassNotFoundException {

        this.spectrumFile = mgfFile;
        this.waitingHandler = waitingHandler;
        this.modificationParameters = searchParameters.getModificationParameters();
        this.omssaTempFolder = aOmssaTempFolder;

        // create the temp folder if it does not exist
        if (!omssaTempFolder.exists()) {
            omssaTempFolder.mkdirs();
        }

        OmssaParameters omssaParameters = (OmssaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex());

        // the database file path and name
        File seqDBFile = fastaFile;
        File dbFilePath = fastaFile.getParentFile();

        // make sure that the omssacl file is executable
        File omssaFile = new File(omssacl_directory.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
        omssaFile.setExecutable(true);

        // full path to executable
        process_name_array.add(omssacl_directory.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);

        // always ask for spectra and search settings to be included in results file
        process_name_array.add("-w"); // @TODO: find a way of being able to turn this off. would require a new parser...

        // check if set by user (that is, if not defaults) and then add to array
        process_name_array.add("-to");
        process_name_array.add(Double.toString(searchParameters.getFragmentIonAccuracyInDaltons(refMass)));
        process_name_array.add("-te");
        process_name_array.add(Double.toString(searchParameters.getPrecursorAccuracy()));
        if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.PPM) {
            process_name_array.add("-teppm");
        }
        DigestionParameters digestionPreferences = searchParameters.getDigestionParameters();
        int enzymeIndex = getEnzymeIndex(digestionPreferences);
        process_name_array.add("-e");
        process_name_array.add(Integer.toString(enzymeIndex));
        Integer missedCleavages = null;
        if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.enzyme) {
            for (Enzyme enzyme : digestionPreferences.getEnzymes()) {
                int enzymeMissedCleavages = digestionPreferences.getnMissedCleavages(enzyme.getName());
                if (missedCleavages == null || enzymeMissedCleavages > missedCleavages) {
                    missedCleavages = enzymeMissedCleavages;
                }
            }
        }
        if (missedCleavages != null) {
            process_name_array.add("-v");
            process_name_array.add(Integer.toString(missedCleavages));
        }
        process_name_array.add("-zl");
        process_name_array.add(Integer.toString(searchParameters.getMinChargeSearched()));
        process_name_array.add("-zh");
        process_name_array.add(Integer.toString(searchParameters.getMaxChargeSearched()));
        process_name_array.add("-zt");
        process_name_array.add(Integer.toString(omssaParameters.getMinimalChargeForMultipleChargedFragments()));
        if (omssaParameters.isMemoryMappedSequenceLibraries()) {
            process_name_array.add("-umm");
        }
        if (searchParameters.getMaxIsotopicCorrection() > 0) {
            process_name_array.add("-ti");
            process_name_array.add(Integer.toString(searchParameters.getMaxIsotopicCorrection()));
        }
        process_name_array.add("-tex");
        process_name_array.add(Double.toString(omssaParameters.getNeutronThreshold()));
        process_name_array.add("-cl");
        process_name_array.add(Double.toString(omssaParameters.getLowIntensityCutOff()));
        process_name_array.add("-ch");
        process_name_array.add(Double.toString(omssaParameters.getHighIntensityCutOff()));
        process_name_array.add("-ci");
        process_name_array.add(Double.toString(omssaParameters.getIntensityCutOffIncrement()));
        process_name_array.add("-w1");
        process_name_array.add(Integer.toString(omssaParameters.getSingleChargeWindow()));
        process_name_array.add("-w2");
        process_name_array.add(Integer.toString(omssaParameters.getDoubleChargeWindow()));
        process_name_array.add("-h1");
        process_name_array.add(Integer.toString(omssaParameters.getnPeaksInSingleChargeWindow()));
        process_name_array.add("-h2");
        process_name_array.add(Integer.toString(omssaParameters.getnPeaksInDoubleChargeWindow()));
        process_name_array.add("-hl");
        process_name_array.add(Integer.toString(omssaParameters.getMaxHitsPerSpectrumPerCharge()));
        process_name_array.add("-hc");
        process_name_array.add(Integer.toString(omssaParameters.getMaxHitsPerSpectrumPerCharge()));
        process_name_array.add("-hm");
        process_name_array.add(Integer.toString(omssaParameters.getMinAnnotatedPeaks()));
        process_name_array.add("-hs");
        process_name_array.add(Integer.toString(omssaParameters.getMinPeaks()));
        process_name_array.add("-ht");
        process_name_array.add(Integer.toString(omssaParameters.getnAnnotatedMostIntensePeaks()));
        if (!omssaParameters.isCleaveNterMethionine()) {
            process_name_array.add("-mnm");
        }
        process_name_array.add("-mm");
        process_name_array.add(Integer.toString(omssaParameters.getMaxMzLadders()));
        process_name_array.add("-zoh");
        process_name_array.add(Integer.toString(omssaParameters.getMaxFragmentCharge()));
        process_name_array.add("-z1");
        process_name_array.add(Double.toString(omssaParameters.getFractionOfPeaksForChargeEstimation()));
        if (!omssaParameters.isDetermineChargePlusOneAlgorithmically()) {
            process_name_array.add("-zc");
            process_name_array.add(Integer.toString(0));
        }
        if (!omssaParameters.isSearchPositiveIons()) {
            process_name_array.add("-zn");
            process_name_array.add(Integer.toString(-1));
        }
        process_name_array.add("-pc");
        process_name_array.add(Integer.toString(omssaParameters.getMinPrecPerSpectrum()));
        if (!omssaParameters.isSearchForwardFragmentFirst()) {
            process_name_array.add("-sb1");
            process_name_array.add(Integer.toString(0));
        }
        if (!omssaParameters.isSearchRewindFragments()) {
            process_name_array.add("-sct");
            process_name_array.add(Integer.toString(1));
        }
        process_name_array.add("-sp");
        process_name_array.add(Integer.toString(omssaParameters.getMaxFragmentPerSeries()));
        if (!omssaParameters.isUseCorrelationCorrectionScore()) {
            process_name_array.add("-scorr");
            process_name_array.add(Integer.toString(1));
        }
        process_name_array.add("-scorp");
        process_name_array.add(Double.toString(omssaParameters.getConsecutiveIonProbability()));
        process_name_array.add("-is");
        process_name_array.add(Double.toString(omssaParameters.getIterativeSequenceEvalue()));
        process_name_array.add("-ir");
        process_name_array.add(Double.toString(omssaParameters.getIterativeReplaceEvalue()));
        process_name_array.add("-ii");
        process_name_array.add(Double.toString(omssaParameters.getIterativeSpectrumEvalue()));
        process_name_array.add("-he");
        process_name_array.add(Double.toString(omssaParameters.getMaxEValue()));
        process_name_array.add("-tez");
        if (omssaParameters.isScalePrecursor()) {
            process_name_array.add("1");
        } else {
            process_name_array.add("0");
        }
        process_name_array.add("-zcc");
        if (omssaParameters.isEstimateCharge()) {
            process_name_array.add("2");
        } else {
            process_name_array.add("1");
        }
        process_name_array.add("-cp");
        if (omssaParameters.isRemovePrecursor()) {
            process_name_array.add("1");
        } else {
            process_name_array.add("0");
        }
        process_name_array.add("-no");
        process_name_array.add(Integer.toString(omssaParameters.getMinPeptideLength()));
        process_name_array.add("-nox");
        process_name_array.add(Integer.toString(omssaParameters.getMaxPeptideLength()));

        // look for monoisotopic peaks
        process_name_array.add("-tom");
        process_name_array.add("0");
        process_name_array.add("-tem");
        process_name_array.add("0");
        // look for b and y ions
        process_name_array.add("-i");
        StringBuilder ions = new StringBuilder();
        for (Integer ion : searchParameters.getForwardIons()) {
            if (ions.length() > 0) {
                ions.append(",");
            }
            ions.append(getIonId(PeptideFragmentIon.getSubTypeAsString(ion)));
        }
        for (Integer ion : searchParameters.getRewindIons()) {
            if (ions.length() > 0) {
                ions.append(",");
            }
            ions.append(getIonId(PeptideFragmentIon.getSubTypeAsString(ion)));
        }
        process_name_array.add(ions.toString());

        String modificationIndexes = "";
        for (Integer index : getSearchedModificationsIds(modificationParameters.getFixedModifications(), omssaParameters)) {
            if (!modificationIndexes.equals("")) {
                modificationIndexes += ",";
            }
            modificationIndexes += index;
        }
        if (!modificationIndexes.equals("")) {
            process_name_array.add("-mf");
            process_name_array.add(modificationIndexes);
        }
        modificationIndexes = "";
        for (Integer index : getSearchedModificationsIds(modificationParameters.getVariableModifications(), omssaParameters)) {
            if (!modificationIndexes.equals("")) {
                modificationIndexes += ",";
            }
            modificationIndexes += index;
        }
        if (!modificationIndexes.equals("")) {
            process_name_array.add("-mv");
            process_name_array.add(modificationIndexes);
        }
        process_name_array.add("-mux");
        process_name_array.add(new File(omssaTempFolder, "usermods.xml").getAbsolutePath());
        process_name_array.add("-d");
        process_name_array.add(seqDBFile.getName());
        if (spectrumFile != null) {
            if (spectrumFile.getName().endsWith(".mgf")) {
                process_name_array.add("-fm");
            } else if (spectrumFile.getName().toLowerCase().endsWith(".pkl")) {
                process_name_array.add("-fp");
            } else if (spectrumFile.getName().toLowerCase().endsWith(".dta")) {
                process_name_array.add("-f");
            }
            process_name_array.add(spectrumFile.getAbsolutePath());
        }
        if (outputFile != null) {
            switch (omssaParameters.getSelectedOutput()) {
                case "OMX":
                    process_name_array.add("-ox");
                    break;
                case "CSV":
                    process_name_array.add("-oc");
                    break;
                default:
                    process_name_array.add("-op");
                    break;
            }
            process_name_array.add(CommandLineUtils.getCommandLineArgument(outputFile));
        }

        process_name_array.add("-nt");
        process_name_array.add(Integer.toString(nThreads));

        process_name_array.trimToSize();

        // print the command to the log file
        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "omssa command: ");

        for (int i = 0; i < process_name_array.size(); i++) {
            System.out.print(process_name_array.get(i) + " ");
        }

        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);
        pb.directory(dbFilePath);

        // set error out and std out to same stream
        pb.redirectErrorStream(true);
    }

    @Override
    public String getType() {
        return "OMSSA";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return spectrumFile.getName();
    }

    /**
     * Returns the ion type. zdot is considered as z. adot, x-CO2 and adot-CO2
     * are ignored as long as nobody complains.
     *
     * @param letter ion letter
     * @return ion number
     */
    public int getIonId(String letter) {

        switch (letter) {
            case "a":
                return 0;
            case "b":
                return 1;
            case "c":
                return 2;
            case "x":
                return 3;
            case "y":
                return 4;
            case "z":
                return 5;
            default:
                break;
        }

        return -1;
    }

    /**
     * Returns an array of OMSSA indexes corresponding to the list of given
     * modification names.
     *
     * @param modificationsNames the modification names
     * @param omssaParameters the OMSSA parameters
     *
     * @return the corresponding list of OMSSA modification indexes
     */
    public ArrayList<Integer> getSearchedModificationsIds(
            ArrayList<String> modificationsNames,
            OmssaParameters omssaParameters
    ) {

        ArrayList<Integer> result = new ArrayList<>();

        for (String modName : modificationsNames) {

            Integer index = omssaParameters.getPtmIndex(modName);

            if (index == null) {
                throw new IllegalArgumentException("No OMSSA index found for modification " + modName);
            }

            if (!result.contains(index)) {
                result.add(index);
            }

        }

        Collections.sort(result);
        return result;
    }

    /**
     * Writes the OMSSA modification file corresponding to the PTMs loaded in
     * the factory in the given file. Stores the PTM indexes in the OMSSA
     * specific parameters.
     *
     * @param omssaFile the file where to write the OMSSA modification files
     * @param identificationParameters the identification parameters
     * @param identificationParametersFile the file where to save the
     * identification parameters
     *
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while saving the search parameters
     */
    public static void writeOmssaUserModificationsFile(
            File omssaFile,
            IdentificationParameters identificationParameters,
            File identificationParametersFile
    ) throws IOException, ClassNotFoundException {

        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        ModificationParameters modificationParameters = searchParameters.getModificationParameters();
        OmssaParameters omssaParameters = (OmssaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex());
        omssaParameters.setPtmIndexes(modificationParameters);
        IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);

        HashMap<Integer, String> ptmIndexes = omssaParameters.getPtmIndexes();
        ArrayList<Integer> indexes = new ArrayList<>(ptmIndexes.keySet());
        Collections.sort(indexes);

        BufferedWriter bw = new BufferedWriter(new FileWriter(omssaFile));

        try {
            String toWrite = "<?xml version=\"1.0\"?>\n<MSModSpecSet\n"
                    + "xmlns=\"http://www.ncbi.nlm.nih.gov\"\n"
                    + "xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    + "xs:schemaLocation=\"http://www.ncbi.nlm.nih.gov OMSSA.xsd\"\n>\n\n";
            bw.write(toWrite);

            int cpt = 1;
            for (Integer index : indexes) {
                String ptmName = ptmIndexes.get(index);
                Modification modification = modificationFactory.getModification(ptmName);
                toWrite = getOmssaUserModBloc(modification, cpt, index);
                bw.write(toWrite);
                cpt++;
            }

            toWrite = "</MSModSpecSet>";
            bw.write(toWrite);

        } finally {
            bw.close();
        }
    }

    /**
     * Returns an MSModSpec bloc as present in the OMSSA user modification files
     * for a given PTM. Only the amino acids targeted by the pattern of the
     * modification will be considered.
     *
     * @param modification the modification
     * @param cpt the index of this modification in the list
     * @param omssaIndex the OMSSA index of this modification
     *
     * @return a string containing the XML bloc
     */
    public static String getOmssaUserModBloc(Modification modification, int cpt, int omssaIndex) {

        String result = "\t<MSModSpec>\n";
        result += "\t\t<MSModSpec_mod>\n";
        result += "\t\t\t<MSMod value=\"usermod" + cpt + "\">" + omssaIndex + "</MSMod>\n";
        result += "\t\t</MSModSpec_mod>\n"
                + "\t\t<MSModSpec_type>\n";

        ModificationType modificationType = modification.getModificationType();

        switch (modificationType) {
            case modaa:
                result += "\t\t\t<MSModType value=\"modaa\">" + modificationType.index + "</MSModType>\n";
                break;

            case modn_peptide:
                result += "\t\t\t<MSModType value=\"modnp\">" + modificationType.index + "</MSModType>\n";
                break;

            case modnaa_peptide:
                result += "\t\t\t<MSModType value=\"modnpaa\">" + modificationType.index + "</MSModType>\n";
                break;

            case modn_protein:
                result += "\t\t\t<MSModType value=\"modn\">" + modificationType.index + "</MSModType>\n";
                break;

            case modnaa_protein:
                result += "\t\t\t<MSModType value=\"modnaa\">" + modificationType.index + "</MSModType>\n";
                break;

            case modc_peptide:
                result += "\t\t\t<MSModType value=\"modcp\">" + modificationType.index + "</MSModType>\n";
                break;

            case modcaa_peptide:
                result += "\t\t\t<MSModType value=\"modcpaa\">" + modificationType.index + "</MSModType>\n";
                break;

            case modc_protein:
                result += "\t\t\t<MSModType value=\"modc\">" + modificationType.index + "</MSModType>\n";
                break;

            case modcaa_protein:
                result += "\t\t\t<MSModType value=\"modcaa\">" + modificationType.index + "</MSModType>\n";
                break;

            default:
                throw new UnsupportedOperationException(
                        "Export not implemented for modification of type "
                        + modificationType
                        + "."
                );
        }

        result += "\t\t</MSModSpec_type>\n";
        result += "\t\t<MSModSpec_name>" + modification.getName() + "</MSModSpec_name>\n";
        result += "\t\t<MSModSpec_monomass>" + modification.getRoundedMass() + "</MSModSpec_monomass>\n"
                + "\t\t<MSModSpec_averagemass>0</MSModSpec_averagemass>\n"
                + "\t\t<MSModSpec_n15mass>0</MSModSpec_n15mass>\n";

        if (modificationType == ModificationType.modaa
                || modificationType == ModificationType.modcaa_peptide
                || modificationType == ModificationType.modcaa_protein
                || modificationType == ModificationType.modnaa_peptide
                || modificationType == ModificationType.modnaa_protein) {

            result += "\t\t<MSModSpec_residues>\n";

            for (Character aa : modification.getPattern().getAminoAcidsAtTarget()) {
                result += "\t\t\t<MSModSpec_residues_E>" + aa + "</MSModSpec_residues_E>\n";
            }

            result += "\t\t</MSModSpec_residues>\n";
        }

        boolean first = true;

        for (NeutralLoss neutralLoss : modification.getNeutralLosses()) {

            if (neutralLoss.isFixed()) {

                if (first) {
                    result += "\t\t<MSModSpec_neutralloss>\n";
                    first = false;
                }

                result += "\t\t\t<MSMassSet>\n";
                result += "\t\t\t\t<MSMassSet_monomass>" + neutralLoss.getMass() + "</MSMassSet_monomass>\n";
                result += "\t\t\t\t<MSMassSet_averagemass>0</MSMassSet_averagemass>";
                result += "\t\t\t\t<MSMassSet_n15mass>0</MSMassSet_n15mass>";
                result += "\t\t\t</MSMassSet>\n";

            }
        }

        if (!first) {
            result += "\t\t</MSModSpec_neutralloss>\n";
        }

        result += "\t</MSModSpec>\n";

        return result;
    }

    /**
     * Returns the OMSSA enzyme index corresponding to the digestion
     * preferences. Unspecific if not found.
     *
     * @param digestionPreferences the digestion preferences
     *
     * @return the OMSSA enzyme index corresponding to the digestion preferences
     */
    private int getEnzymeIndex(DigestionParameters digestionPreferences) {

        if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.wholeProtein) {
            return 11;
        } else if (digestionPreferences.getCleavageParameter() == DigestionParameters.CleavageParameter.unSpecific) {
            return 17;
        } else if (digestionPreferences.getEnzymes().size() > 1) {
            return 17;
        }

        Enzyme enzyme = digestionPreferences.getEnzymes().get(0);
        String enzymeName = enzyme.getName();
        Specificity specificity = digestionPreferences.getSpecificity(enzymeName);

        switch (enzymeName) {

            case "Trypsin":
                if (specificity == Specificity.specific) {
                    return 0;
                } else {
                    return 16;
                }

            case "Trypsin (no P rule)":
                return 10;

            case "Arg-C":
            case "Arg-C (no P rule)":
                return 1;

            case "Glu-C":
                if (specificity == Specificity.specific) {
                    return 13;
                } else {
                    return 24;
                }

            case "Lys-C":
                return 5;

            case "Lys-C (no P rule)":
                return 6;

            case "Lys-N":
                return 21;

            case "Asp-N":
                return 12;

            case "Asp-N Ammonium Bicarbonate":
                return 19;

            case "Chymotrypsin":
                if (specificity == Specificity.specific) {
                    return 3;
                } else {
                    return 23;
                }

            case "Chymotrypsin (no P rule)":
                if (specificity == Specificity.specific) {
                    return 18;
                } else {
                    return 23;
                }

            case "Pepsin A":
                return 7;

            case "CNBr":
                return 2;

            case "Thermolysin":
                return 22;

            default:
                break;
        }

        return 17;
    }
}
