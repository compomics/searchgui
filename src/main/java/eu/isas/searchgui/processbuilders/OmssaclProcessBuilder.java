package eu.isas.searchgui.processbuilders;

import com.compomics.software.CommandLineUtils;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.OmssaParameters;
import com.compomics.util.waiting.WaitingHandler;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import java.io.BufferedWriter;

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
     * The spectra file as a string.
     */
    private String spectraFile;
    /**
     * The modification profile of this search.
     */
    private PtmSettings modificationProfile;
    /**
     * The name of the OMSSA executable.
     */
    public final static String EXECUTABLE_FILE_NAME = "omssacl";

    /**
     * Constructor.
     *
     * @param omssacl_directory directory location of omssacl.exe
     * @param spectraFile string location of spectra file to search
     * @param outputFile string location where to send omx/csv/pepxml formatted
     * results file
     * @param searchParameters the search parameters
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the handler of exceptions
     * @param nThreads the number of threads to use
     *
     * @throws java.io.IOException exception thrown whenever an error occurred
     * while reading or writing a file.
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while saving the search parameters
     */
    public OmssaclProcessBuilder(File omssacl_directory, String spectraFile, File outputFile, SearchParameters searchParameters, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler, int nThreads) throws IOException, ClassNotFoundException, SecurityException {

            this.spectraFile = spectraFile;
            this.waitingHandler = waitingHandler;
            this.modificationProfile = searchParameters.getPtmSettings();

            OmssaParameters omssaParameters = (OmssaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex());

            // The database file path and name.
            File seqDBFile = searchParameters.getFastaFile();
            File dbFilePath = seqDBFile.getParentFile();

            // make sure that the omssacl file is executable
            File omssaFile = new File(omssacl_directory.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);
            omssaFile.setExecutable(true);

            // full path to executable
            process_name_array.add(omssacl_directory.getAbsolutePath() + File.separator + EXECUTABLE_FILE_NAME);

            // always ask for spectra and search settings to be included in results file
            process_name_array.add("-w"); // @TODO: find a way of being able to turn this off. would require a new parser...

            // check if set by user (that is, if not defaults) and then add to array
            process_name_array.add("-to");
            process_name_array.add(searchParameters.getFragmentIonAccuracy() + "");
            process_name_array.add("-te");
            process_name_array.add(searchParameters.getPrecursorAccuracy() + "");
            if (searchParameters.getPrecursorAccuracyType() == SearchParameters.MassAccuracyType.PPM) {
                process_name_array.add("-teppm");
            }
            if (searchParameters.getEnzyme().getId() != -1) {
                process_name_array.add("-e");
                process_name_array.add(Integer.toString(searchParameters.getEnzyme().getId()));
            }
            if (searchParameters.getMinChargeSearched() != null) {
                process_name_array.add("-zl");
                process_name_array.add(Integer.toString(searchParameters.getMinChargeSearched().value));
            }
            if (searchParameters.getMaxChargeSearched() != null) {
                process_name_array.add("-zh");
                process_name_array.add(Integer.toString(searchParameters.getMaxChargeSearched().value));
            }
            if (omssaParameters.getMinimalChargeForMultipleChargedFragments() != null) {
                process_name_array.add("-zt");
                process_name_array.add(Integer.toString(omssaParameters.getMinimalChargeForMultipleChargedFragments().value));
            }
            if (omssaParameters.isMemoryMappedSequenceLibraries() != null && omssaParameters.isMemoryMappedSequenceLibraries()) {
                process_name_array.add("-umm");
            }
            if (omssaParameters.getNumberOfItotopicPeaks() != null && omssaParameters.getNumberOfItotopicPeaks() > 0) {
                process_name_array.add("-ti");
                process_name_array.add(Integer.toString(omssaParameters.getNumberOfItotopicPeaks()));
            }
            if (omssaParameters.getNeutronThreshold() != null) {
                process_name_array.add("-tex");
                process_name_array.add(Double.toString(omssaParameters.getNeutronThreshold()));
            }
            if (omssaParameters.getLowIntensityCutOff() != null) {
                process_name_array.add("-cl");
                process_name_array.add(Double.toString(omssaParameters.getLowIntensityCutOff()));
            }
            if (omssaParameters.getHighIntensityCutOff() != null) {
                process_name_array.add("-ch");
                process_name_array.add(Double.toString(omssaParameters.getHighIntensityCutOff()));
            }
            if (omssaParameters.getIntensityCutOffIncrement() != null) {
                process_name_array.add("-ci");
                process_name_array.add(Double.toString(omssaParameters.getIntensityCutOffIncrement()));
            }
            if (omssaParameters.getSingleChargeWindow() != null) {
                process_name_array.add("-w1");
                process_name_array.add(Integer.toString(omssaParameters.getSingleChargeWindow()));
            }
            if (omssaParameters.getDoubleChargeWindow() != null) {
                process_name_array.add("-w2");
                process_name_array.add(Integer.toString(omssaParameters.getDoubleChargeWindow()));
            }
            if (omssaParameters.getnPeaksInSingleChargeWindow() != null) {
                process_name_array.add("-h1");
                process_name_array.add(Integer.toString(omssaParameters.getnPeaksInSingleChargeWindow()));
            }
            if (omssaParameters.getnPeaksInDoubleChargeWindow() != null) {
                process_name_array.add("-h2");
                process_name_array.add(Integer.toString(omssaParameters.getnPeaksInDoubleChargeWindow()));
            }
            if (omssaParameters.getMaxHitsPerSpectrumPerCharge() != null) {
                process_name_array.add("-hl");
                process_name_array.add(Integer.toString(omssaParameters.getMaxHitsPerSpectrumPerCharge()));
            }
            if (omssaParameters.getHitListLength() != null && omssaParameters.getHitListLength() > 0) {
                process_name_array.add("-hc");
                process_name_array.add(Integer.toString(omssaParameters.getMaxHitsPerSpectrumPerCharge()));
            }
            if (omssaParameters.getMinAnnotatedPeaks() != null) {
                process_name_array.add("-hm");
                process_name_array.add(Integer.toString(omssaParameters.getMinAnnotatedPeaks()));
            }
            if (omssaParameters.getMinPeaks() != null) {
                process_name_array.add("-hs");
                process_name_array.add(Integer.toString(omssaParameters.getMinPeaks()));
            }
            if (omssaParameters.getnAnnotatedMostIntensePeaks() != null) {
                process_name_array.add("-ht");
                process_name_array.add(Integer.toString(omssaParameters.getnAnnotatedMostIntensePeaks()));
            }
            if (omssaParameters.isCleaveNterMethionine() != null && !omssaParameters.isCleaveNterMethionine()) {
                process_name_array.add("-mnm");
            }
            if (omssaParameters.getMaxMzLadders() != null) {
                process_name_array.add("-mm");
                process_name_array.add(Integer.toString(omssaParameters.getMaxMzLadders()));
            }
            if (omssaParameters.getMaxFragmentCharge() != null) {
                process_name_array.add("-zoh");
                process_name_array.add(Integer.toString(omssaParameters.getMaxFragmentCharge()));
            }
            if (omssaParameters.getFractionOfPeaksForChargeEstimation() != null) {
                process_name_array.add("-z1");
                process_name_array.add(Double.toString(omssaParameters.getFractionOfPeaksForChargeEstimation()));
            }
            if (omssaParameters.isDetermineChargePlusOneAlgorithmically() != null && !omssaParameters.isDetermineChargePlusOneAlgorithmically()) {
                process_name_array.add("-zc");
                process_name_array.add(Integer.toString(0));
            }
            if (omssaParameters.isSearchPositiveIons() != null && !omssaParameters.isSearchPositiveIons()) {
                process_name_array.add("-zn");
                process_name_array.add(Integer.toString(-1));
            }
            if (omssaParameters.getMinPrecPerSpectrum() != null) {
                process_name_array.add("-pc");
                process_name_array.add(Integer.toString(omssaParameters.getMinPrecPerSpectrum()));
            }
            if (omssaParameters.isSearchForwardFragmentFirst() != null && !omssaParameters.isSearchForwardFragmentFirst()) {
                process_name_array.add("-sb1");
                process_name_array.add(Integer.toString(0));
            }
            if (omssaParameters.isSearchRewindFragments() != null && !omssaParameters.isSearchRewindFragments()) {
                process_name_array.add("-sct");
                process_name_array.add(Integer.toString(1));
            }
            if (omssaParameters.getMaxFragmentPerSeries() != null) {
                process_name_array.add("-sp");
                process_name_array.add(Integer.toString(omssaParameters.getMaxFragmentPerSeries()));
            }
            if (omssaParameters.isUseCorrelationCorrectionScore() != null && !omssaParameters.isUseCorrelationCorrectionScore()) {
                process_name_array.add("-scorr");
                process_name_array.add(Integer.toString(1));
            }
            if (omssaParameters.getConsecutiveIonProbability() != null) {
                process_name_array.add("-scorp");
                process_name_array.add(Double.toString(omssaParameters.getConsecutiveIonProbability()));
            }
            if (omssaParameters.getIterativeSequenceEvalue() != null) {
                process_name_array.add("-is");
                process_name_array.add(Double.toString(omssaParameters.getIterativeSequenceEvalue()));
            }
            if (omssaParameters.getIterativeReplaceEvalue() != null) {
                process_name_array.add("-ir");
                process_name_array.add(Double.toString(omssaParameters.getIterativeReplaceEvalue()));
            }
            if (omssaParameters.getIterativeSpectrumEvalue() != null) {
                process_name_array.add("-ii");
                process_name_array.add(Double.toString(omssaParameters.getIterativeSpectrumEvalue()));
            }
            process_name_array.add("-v");
            process_name_array.add(Integer.toString(searchParameters.getnMissedCleavages()));
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
            if (omssaParameters.getMinPeptideLength() != null) { // @TODO: these have to to be set if using no-enzyme or semi-enyzyme searches!!
                process_name_array.add("-no");
                process_name_array.add(omssaParameters.getMinPeptideLength().toString());
            }
            if (omssaParameters.getMaxPeptideLength() != null) {
                process_name_array.add("-nox");
                process_name_array.add(omssaParameters.getMaxPeptideLength().toString());
            }

            // look for monoisotopic peaks
            process_name_array.add("-tom");
            process_name_array.add("0");
            process_name_array.add("-tem");
            process_name_array.add("0");
            // look for b and y ions
            process_name_array.add("-i");
            process_name_array.add(getIonId(PeptideFragmentIon.getSubTypeAsString(searchParameters.getIonSearched1())) + ","
                    + getIonId(PeptideFragmentIon.getSubTypeAsString(searchParameters.getIonSearched2()))); // @TODO: add support for more ion types...

            String modificationIndexes = "";
            for (Integer index : getSearchedModificationsIds(modificationProfile.getFixedModifications(), omssaParameters)) {
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
            for (Integer index : getSearchedModificationsIds(modificationProfile.getVariableModifications(), omssaParameters)) {
                if (!modificationIndexes.equals("")) {
                    modificationIndexes += ",";
                }
                modificationIndexes += index;
            }
            if (!modificationIndexes.equals("")) {
                process_name_array.add("-mv");
                process_name_array.add(modificationIndexes);
            }
            process_name_array.add("-d");
            process_name_array.add(seqDBFile.getName());
            if (spectraFile != null) {
                if (spectraFile.toLowerCase().endsWith(".mgf")) {
                    process_name_array.add("-fm");
                } else if (spectraFile.toLowerCase().endsWith(".pkl")) {
                    process_name_array.add("-fp");
                } else if (spectraFile.toLowerCase().endsWith(".dta")) {
                    process_name_array.add("-f");
                }
                process_name_array.add(spectraFile);
            }
            if (outputFile != null) {
                if (omssaParameters.getSelectedOutput().equals("OMX")) {
                    process_name_array.add("-ox");
                } else if (omssaParameters.getSelectedOutput().equals("CSV")) {
                    process_name_array.add("-oc");
                } else {
                    process_name_array.add("-op");
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
        return "OMSSA Process";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return spectraFile;
    }

    /**
     * Returns the ion type. zdot is considered as z. adot, x-CO2 and adot-CO2
     * are ignored as long as nobody complains.
     *
     * @param letter ion letter
     * @return ion number
     */
    public int getIonId(String letter) {
        if (letter.equals("a")) {
            return 0;
        } else if (letter.equals("b")) {
            return 1;
        } else if (letter.equals("c")) {
            return 2;
        } else if (letter.equals("x")) {
            return 3;
        } else if (letter.equals("y")) {
            return 4;
        } else if (letter.equals("z")) {
            return 5;
        }
        return -1;
    }

    /**
     * Returns an array of OMSSA indexes corresponding to the list of given
     * modification names.
     *
     * @param modificationsNames the modification names
     * @param omssaParameters the omssa parameters
     *
     * @return the corresponding list of OMSSA modification indexes
     */
    public ArrayList<Integer> getSearchedModificationsIds(ArrayList<String> modificationsNames, OmssaParameters omssaParameters) {
        ArrayList<Integer> result = new ArrayList<Integer>();
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
     * @param file the file
     * @param searchParameters the search parameters
     *
     * @throws IOException exception thrown whenever an error occurred while
     * writing the file
     * @throws java.lang.ClassNotFoundException exception thrown whenever an
     * error occurred while saving the search parameters
     */
    public static void writeOmssaUserModificationsFile(File file, SearchParameters searchParameters) throws IOException, ClassNotFoundException {

        PTMFactory ptmFactory = PTMFactory.getInstance();

        PtmSettings modificationProfile = searchParameters.getPtmSettings();
        OmssaParameters omssaParameters = (OmssaParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex());
        omssaParameters.setPtmIndexes(modificationProfile);
        SearchParameters.saveIdentificationParameters(searchParameters, searchParameters.getParametersFile());

        HashMap<Integer, String> ptmIndexes = omssaParameters.getPtmIndexes();
        ArrayList<Integer> indexes = new ArrayList<Integer>(ptmIndexes.keySet());
        Collections.sort(indexes);

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        try {
            String toWrite = "<?xml version=\"1.0\"?>\n<MSModSpecSet\n"
                    + "xmlns=\"http://www.ncbi.nlm.nih.gov\"\n"
                    + "xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    + "xs:schemaLocation=\"http://www.ncbi.nlm.nih.gov OMSSA.xsd\"\n>\n\n";
            bw.write(toWrite);

            int cpt = 1;
            for (Integer index : indexes) {
                String ptmName = ptmIndexes.get(index);
                PTM ptm = ptmFactory.getPTM(ptmName);
                toWrite = getOmssaUserModBloc(ptm, cpt, index);
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
     * for a given PTM. Only the amino acids targeted by the pattern of the PTM
     * will be considered.
     *
     * @param ptm the PTM
     * @param cpt the index of this ptm in the list
     * @param omssaIndex the OMSSA index of this PTM
     *
     * @return a string containing the XML bloc
     */
    public static String getOmssaUserModBloc(PTM ptm, int cpt, int omssaIndex) {

        String result = "\t<MSModSpec>\n";
        result += "\t\t<MSModSpec_mod>\n";
        result += "\t\t\t<MSMod value=\"usermod" + cpt + "\">" + omssaIndex + "</MSMod>\n";
        result += "\t\t</MSModSpec_mod>\n"
                + "\t\t<MSModSpec_type>\n";
        if (ptm.getType() == PTM.MODAA) {
            result += "\t\t\t<MSModType value=\"modaa\">" + PTM.MODAA + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODN) {
            result += "\t\t\t<MSModType value=\"modn\">" + PTM.MODN + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODNAA) {
            result += "\t\t\t<MSModType value=\"modnaa\">" + PTM.MODNAA + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODNP) {
            result += "\t\t\t<MSModType value=\"modnp\">" + PTM.MODNP + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODNPAA) {
            result += "\t\t\t<MSModType value=\"modnpaa\">" + PTM.MODNPAA + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODC) {
            result += "\t\t\t<MSModType value=\"modc\">" + PTM.MODC + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODCAA) {
            result += "\t\t\t<MSModType value=\"modcaa\">" + PTM.MODCAA + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODCP) {
            result += "\t\t\t<MSModType value=\"modcp\">" + PTM.MODCP + "</MSModType>\n";
        } else if (ptm.getType() == PTM.MODCPAA) {
            result += "\t\t\t<MSModType value=\"modcpaa\">" + PTM.MODCPAA + "</MSModType>\n";
        } else {
            throw new IllegalArgumentException("Export not implemented for PTM of type " + ptm.getType() + ".");
        }
        result += "\t\t</MSModSpec_type>\n";
        result += "\t\t<MSModSpec_name>" + ptm.getName() + "</MSModSpec_name>\n";
        result += "\t\t<MSModSpec_monomass>" + ptm.getRoundedMass() + "</MSModSpec_monomass>\n"
                + "\t\t<MSModSpec_averagemass>0</MSModSpec_averagemass>\n"
                + "\t\t<MSModSpec_n15mass>0</MSModSpec_n15mass>\n";
        if (ptm.getType() == PTM.MODAA
                || ptm.getType() == PTM.MODNAA
                || ptm.getType() == PTM.MODNPAA
                || ptm.getType() == PTM.MODCAA
                || ptm.getType() == PTM.MODCPAA) {
            result += "\t\t<MSModSpec_residues>\n";
            for (Character aa : ptm.getPattern().getAminoAcidsAtTarget()) {
                result += "\t\t\t<MSModSpec_residues_E>" + aa + "</MSModSpec_residues_E>\n";
            }
            result += "\t\t</MSModSpec_residues>\n";
        }
        boolean first = true;
        for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
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
}
