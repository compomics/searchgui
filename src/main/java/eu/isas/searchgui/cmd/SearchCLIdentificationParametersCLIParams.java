package eu.isas.searchgui.cmd;

import com.compomics.util.experiment.identification.parameters_cli.IdentificationParametersCLIParams;
import org.apache.commons.cli.Options;

/**
 * This class provides the parameters which can be used for the identification
 * parameters cli in SearchGUI.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SearchCLIdentificationParametersCLIParams {

    /**
     * Creates the options for the command line interface based on the possible
     * values.
     *
     * @param aOptions the options object where the options will be added
     */
    public static void createOptionsCLI(Options aOptions) {

        aOptions.addOption(IdentificationParametersCLIParams.OUT.id, true, IdentificationParametersCLIParams.OUT.description);
        aOptions.addOption(IdentificationParametersCLIParams.MODS.id, false, IdentificationParametersCLIParams.MODS.description);
        aOptions.addOption(IdentificationParametersCLIParams.USAGE.id, false, IdentificationParametersCLIParams.USAGE.description);
        aOptions.addOption(IdentificationParametersCLIParams.USAGE_2.id, false, IdentificationParametersCLIParams.USAGE_2.description);
        aOptions.addOption(IdentificationParametersCLIParams.USAGE_3.id, false, IdentificationParametersCLIParams.USAGE_3.description);

        aOptions.addOption(IdentificationParametersCLIParams.PREC_PPM.id, true, IdentificationParametersCLIParams.PREC_PPM.description);
        aOptions.addOption(IdentificationParametersCLIParams.PREC_TOL.id, true, IdentificationParametersCLIParams.PREC_TOL.description);
        aOptions.addOption(IdentificationParametersCLIParams.FRAG_TOL.id, true, IdentificationParametersCLIParams.FRAG_TOL.description);
        aOptions.addOption(IdentificationParametersCLIParams.ENZYME.id, true, IdentificationParametersCLIParams.ENZYME.description);
        aOptions.addOption(IdentificationParametersCLIParams.FIXED_MODS.id, true, IdentificationParametersCLIParams.FIXED_MODS.description);
        aOptions.addOption(IdentificationParametersCLIParams.VARIABLE_MODS.id, true, IdentificationParametersCLIParams.VARIABLE_MODS.description);
        aOptions.addOption(IdentificationParametersCLIParams.MIN_CHARGE.id, true, IdentificationParametersCLIParams.MIN_CHARGE.description);
        aOptions.addOption(IdentificationParametersCLIParams.MAX_CHARGE.id, true, IdentificationParametersCLIParams.MAX_CHARGE.description);
        aOptions.addOption(IdentificationParametersCLIParams.MC.id, true, IdentificationParametersCLIParams.MC.description);
        aOptions.addOption(IdentificationParametersCLIParams.FI.id, true, IdentificationParametersCLIParams.FI.description);
        aOptions.addOption(IdentificationParametersCLIParams.RI.id, true, IdentificationParametersCLIParams.RI.description);
        aOptions.addOption(IdentificationParametersCLIParams.DB.id, true, IdentificationParametersCLIParams.DB.description);

        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id, true, IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_NPEAKS.id, true, IdentificationParametersCLIParams.XTANDEM_NPEAKS.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id, true, IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id, true, IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id, true, IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id, true, IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id, true, IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id, true, IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id, true, IdentificationParametersCLIParams.XTANDEM_STP_BIAS.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_REFINE.id, true, IdentificationParametersCLIParams.XTANDEM_REFINE.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id, true, IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id, true, IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id, true, IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id, true, IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id, true, IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id, true, IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id, true, IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_EVALUE.id, true, IdentificationParametersCLIParams.XTANDEM_EVALUE.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id, true, IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id, true, IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id, true, IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.description);
        aOptions.addOption(IdentificationParametersCLIParams.XTANDEM_SKYLINE.id, true, IdentificationParametersCLIParams.XTANDEM_SKYLINE.description);

        aOptions.addOption(IdentificationParametersCLIParams.MSGF_DECOY.id, true, IdentificationParametersCLIParams.MSGF_DECOY.description);
        aOptions.addOption(IdentificationParametersCLIParams.MSGF_INSTRUMENT.id, true, IdentificationParametersCLIParams.MSGF_INSTRUMENT.description);
        aOptions.addOption(IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id, true, IdentificationParametersCLIParams.MSGF_FRAGMENTATION.description);
        aOptions.addOption(IdentificationParametersCLIParams.MSGF_PROTOCOL.id, true, IdentificationParametersCLIParams.MSGF_PROTOCOL.description);
        aOptions.addOption(IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id, true, IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id, true, IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id, true, IdentificationParametersCLIParams.MSGF_NUM_MATCHES.description);
        aOptions.addOption(IdentificationParametersCLIParams.MSGF_ADDITIONAL.id, true, IdentificationParametersCLIParams.MSGF_ADDITIONAL.description);
        aOptions.addOption(IdentificationParametersCLIParams.MSGF_ISOTOPE_LOW.id, true, IdentificationParametersCLIParams.MSGF_ISOTOPE_LOW.description);
        aOptions.addOption(IdentificationParametersCLIParams.MSGF_ISOTOPE_HIGH.id, true, IdentificationParametersCLIParams.MSGF_ISOTOPE_HIGH.description);
        aOptions.addOption(IdentificationParametersCLIParams.MSGF_TERMINI.id, true, IdentificationParametersCLIParams.MSGF_TERMINI.description);
        aOptions.addOption(IdentificationParametersCLIParams.MSGF_PTMS.id, true, IdentificationParametersCLIParams.MSGF_PTMS.description);

        aOptions.addOption(IdentificationParametersCLIParams.MS_AMANDA_DECOY.id, true, IdentificationParametersCLIParams.MS_AMANDA_DECOY.description);
        aOptions.addOption(IdentificationParametersCLIParams.MS_AMANDA_INSTRUMENT.id, true, IdentificationParametersCLIParams.MS_AMANDA_INSTRUMENT.description);
        aOptions.addOption(IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id, true, IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.description);
        aOptions.addOption(IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id, true, IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.description);
        aOptions.addOption(IdentificationParametersCLIParams.MS_AMANDA_LOW_MEM_MODE.id, true, IdentificationParametersCLIParams.MS_AMANDA_LOW_MEM_MODE.description);

        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id, true, IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id, true, IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id, true, IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id, true, IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_LOW.id, true, IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_LOW.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_HIGH.id, true, IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_HIGH.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_NUM_MATCHES.id, true, IdentificationParametersCLIParams.MYRIMATCH_NUM_MATCHES.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_PTMS.id, true, IdentificationParametersCLIParams.MYRIMATCH_PTMS.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_FRAGMENTATION.id, true, IdentificationParametersCLIParams.MYRIMATCH_FRAGMENTATION.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id, true, IdentificationParametersCLIParams.MYRIMATCH_TERMINI.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id, true, IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_XCORR.id, true, IdentificationParametersCLIParams.MYRIMATCH_XCORR.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id, true, IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id, true, IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id, true, IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id, true, IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.description);
        aOptions.addOption(IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id, true, IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.description);

        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id, true, IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id, true, IdentificationParametersCLIParams.OMSSA_SCALE_PREC.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id, true, IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id, true, IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id, true, IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id, true, IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id, true, IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id, true, IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_FORMAT.id, true, IdentificationParametersCLIParams.OMSSA_FORMAT.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id, true, IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_ISOTOPES.id, true, IdentificationParametersCLIParams.OMSSA_ISOTOPES.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_NEUTRON.id, true, IdentificationParametersCLIParams.OMSSA_NEUTRON.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id, true, IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id, true, IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id, true, IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id, true, IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id, true, IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id, true, IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id, true, IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id, true, IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.id, true, IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_PEAKS.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id, true, IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_METHIONINE.id, true, IdentificationParametersCLIParams.OMSSA_METHIONINE.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id, true, IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id, true, IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id, true, IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id, true, IdentificationParametersCLIParams.OMSSA_PLUS_ONE.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id, true, IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id, true, IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id, true, IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id, true, IdentificationParametersCLIParams.OMSSA_REWIND_IONS.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id, true, IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id, true, IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id, true, IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id, true, IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id, true, IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.description);
        aOptions.addOption(IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id, true, IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.description);

        aOptions.addOption(IdentificationParametersCLIParams.COMET_NUM_MATCHES.id, true, IdentificationParametersCLIParams.COMET_NUM_MATCHES.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_PTMS.id, true, IdentificationParametersCLIParams.COMET_PTMS.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_REQ_PTMS.id, true, IdentificationParametersCLIParams.COMET_REQ_PTMS.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_MIN_PEAKS.id, true, IdentificationParametersCLIParams.COMET_MIN_PEAKS.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id, true, IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id, true, IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id, true, IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id, true, IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id, true, IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id, true, IdentificationParametersCLIParams.COMET_ENZYME_TYPE.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id, true, IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id, true, IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id, true, IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id, true, IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_REMOVE_METH.id, true, IdentificationParametersCLIParams.COMET_REMOVE_METH.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_BATCH_SIZE.id, true, IdentificationParametersCLIParams.COMET_BATCH_SIZE.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id, true, IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.description);
        aOptions.addOption(IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id, true, IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.description);

        aOptions.addOption(IdentificationParametersCLIParams.TIDE_PTMS.id, true, IdentificationParametersCLIParams.TIDE_PTMS.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id, true, IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id, true, IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id, true, IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id, true, IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id, true, IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id, true, IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id, true, IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_DECOY_SEED.id, true, IdentificationParametersCLIParams.TIDE_DECOY_SEED.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_OUTPUT_FOLDER.id, true, IdentificationParametersCLIParams.TIDE_OUTPUT_FOLDER.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id, true, IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_VERBOSITY.id, true, IdentificationParametersCLIParams.TIDE_VERBOSITY.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id, true, IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id, true, IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id, true, IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id, true, IdentificationParametersCLIParams.TIDE_COMPUTE_SP.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_MAX_PSMS.id, true, IdentificationParametersCLIParams.TIDE_MAX_PSMS.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_COMPUTE_P.id, true, IdentificationParametersCLIParams.TIDE_COMPUTE_P.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id, true, IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id, true, IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id, true, IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id, true, IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id, true, IdentificationParametersCLIParams.TIDE_REMOVE_PREC.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id, true, IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id, true, IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_USE_FLANKING.id, true, IdentificationParametersCLIParams.TIDE_USE_FLANKING.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id, true, IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id, true, IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id, true, IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_CONCAT.id, true, IdentificationParametersCLIParams.TIDE_CONCAT.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_STORE_SPECTRA.id, true, IdentificationParametersCLIParams.TIDE_STORE_SPECTRA.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id, true, IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id, true, IdentificationParametersCLIParams.TIDE_EXPORT_SQT.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id, true, IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id, true, IdentificationParametersCLIParams.TIDE_EXPORT_MZID.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id, true, IdentificationParametersCLIParams.TIDE_EXPORT_PIN.description);
        aOptions.addOption(IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id, true, IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.description);

        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.id, true, IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.id, true, IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS.id, true, IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.id, true, IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.id, true, IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.id, true, IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.id, true, IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.id, true, IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.id, true, IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.id, true, IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.id, true, IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.id, true, IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.id, true, IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.id, true, IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.id, true, IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.description);
        aOptions.addOption(IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.id, true, IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.description);
    }

    /**
     * Returns the options as a string.
     *
     * @return the options as a string
     */
    public static String getOptionsAsString() {

        String output = "";
        String formatter = "%-25s";

        output += "Parameters Files:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OUT.id) + IdentificationParametersCLIParams.OUT.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.IDENTIFICATION_PARAMETERS.id) + IdentificationParametersCLIParams.IDENTIFICATION_PARAMETERS.description + "\n";
        output += getParametersOptionsAsString();
        return output;
    }

    /**
     * Returns the options as a string.
     *
     * @return the options as a string
     */
    public static String getParametersOptionsAsString() {

        String output = "";
        String formatter = "%-25s";

        output += "Search Parameters:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.DB.id) + IdentificationParametersCLIParams.DB.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.PREC_TOL.id) + IdentificationParametersCLIParams.PREC_TOL.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.PREC_PPM.id) + IdentificationParametersCLIParams.PREC_PPM.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.FRAG_TOL.id) + IdentificationParametersCLIParams.FRAG_TOL.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ENZYME.id) + IdentificationParametersCLIParams.ENZYME.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.FIXED_MODS.id) + IdentificationParametersCLIParams.FIXED_MODS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.VARIABLE_MODS.id) + IdentificationParametersCLIParams.VARIABLE_MODS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MIN_CHARGE.id) + IdentificationParametersCLIParams.MIN_CHARGE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MAX_CHARGE.id) + IdentificationParametersCLIParams.MAX_CHARGE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MC.id) + IdentificationParametersCLIParams.MC.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.FI.id) + IdentificationParametersCLIParams.FI.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.RI.id) + IdentificationParametersCLIParams.RI.description + "\n";

        output += "\n\nX!Tandem advanced parameters:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.id) + IdentificationParametersCLIParams.XTANDEM_DYNAMIC_RANGE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_NPEAKS.id) + IdentificationParametersCLIParams.XTANDEM_NPEAKS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.id) + IdentificationParametersCLIParams.XTANDEM_MIN_FRAG_MZ.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.id) + IdentificationParametersCLIParams.XTANDEM_MIN_PEAKS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.id) + IdentificationParametersCLIParams.XTANDEM_NOISE_SUPPRESSION.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.id) + IdentificationParametersCLIParams.XTANDEM_MIN_PREC_MASS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.id) + IdentificationParametersCLIParams.XTANDEM_QUICK_ACETYL.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.id) + IdentificationParametersCLIParams.XTANDEM_QUICK_PYRO.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_STP_BIAS.id) + IdentificationParametersCLIParams.XTANDEM_STP_BIAS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_REFINE.id) + IdentificationParametersCLIParams.XTANDEM_REFINE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.id) + IdentificationParametersCLIParams.XTANDEM_REFINE_EVALUE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.id) + IdentificationParametersCLIParams.XTANDEM_REFINE_UNANTICIPATED_CLEAVAGE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.id) + IdentificationParametersCLIParams.XTANDEM_REFINE_SEMI.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.id) + IdentificationParametersCLIParams.XTANDEM_REFINE_POTENTIAL_MOD_FULL_REFINEMENT.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.id) + IdentificationParametersCLIParams.XTANDEM_REFINE_POINT_MUTATIONS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.id) + IdentificationParametersCLIParams.XTANDEM_REFINE_SNAPS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.id) + IdentificationParametersCLIParams.XTANDEM_REFINE_SPECTRUM_SYNTHESIS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_EVALUE.id) + IdentificationParametersCLIParams.XTANDEM_EVALUE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.id) + IdentificationParametersCLIParams.XTANDEM_OUTPUT_PROTEINS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.id) + IdentificationParametersCLIParams.XTANDEM_OUTPUT_SEQUENCES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.id) + IdentificationParametersCLIParams.XTANDEM_OUTPUT_SPECTRA.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.XTANDEM_SKYLINE.id) + IdentificationParametersCLIParams.XTANDEM_SKYLINE.description + "\n";

        output += "\n\nMyriMatch advanced parameters:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.id) + IdentificationParametersCLIParams.MYRIMATCH_MIN_PEP_LENGTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.id) + IdentificationParametersCLIParams.MYRIMATCH_MAX_PEP_LENGTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.id) + IdentificationParametersCLIParams.MYRIMATCH_MIN_PREC_MASS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.id) + IdentificationParametersCLIParams.MYRIMATCH_MAX_PREC_MASS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_LOW.id) + IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_LOW.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_HIGH.id) + IdentificationParametersCLIParams.MYRIMATCH_ISOTOPE_HIGH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_NUM_MATCHES.id) + IdentificationParametersCLIParams.MYRIMATCH_NUM_MATCHES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_PTMS.id) + IdentificationParametersCLIParams.MYRIMATCH_PTMS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_FRAGMENTATION.id) + IdentificationParametersCLIParams.MYRIMATCH_FRAGMENTATION.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_TERMINI.id) + IdentificationParametersCLIParams.MYRIMATCH_TERMINI.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.id) + IdentificationParametersCLIParams.MYRIMATCH_SMART_PLUS_THREE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_XCORR.id) + IdentificationParametersCLIParams.MYRIMATCH_XCORR.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.id) + IdentificationParametersCLIParams.MYRIMATCH_TIC_CUTOFF.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.id) + IdentificationParametersCLIParams.MYRIMATCH_INTENSTITY_CLASSES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.id) + IdentificationParametersCLIParams.MYRIMATCH_CLASS_MULTIPLIER.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.id) + IdentificationParametersCLIParams.MYRIMATCH_NUM_BATCHES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.id) + IdentificationParametersCLIParams.MYRIMATCH_MAX_PEAK_COUNT.description + "\n";

        output += "\n\nMS Amanda advanced parameters:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MS_AMANDA_DECOY.id) + IdentificationParametersCLIParams.MS_AMANDA_DECOY.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MS_AMANDA_INSTRUMENT.id) + IdentificationParametersCLIParams.MS_AMANDA_INSTRUMENT.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.id) + IdentificationParametersCLIParams.MS_AMANDA_MAX_RANK.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.id) + IdentificationParametersCLIParams.MS_AMANDA_MONOISOTOPIC.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MS_AMANDA_LOW_MEM_MODE.id) + IdentificationParametersCLIParams.MS_AMANDA_LOW_MEM_MODE.description + "\n";

        output += "\n\nMS-GF+ advanced parameters:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_DECOY.id) + IdentificationParametersCLIParams.MSGF_DECOY.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_INSTRUMENT.id) + IdentificationParametersCLIParams.MSGF_INSTRUMENT.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_FRAGMENTATION.id) + IdentificationParametersCLIParams.MSGF_FRAGMENTATION.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_PROTOCOL.id) + IdentificationParametersCLIParams.MSGF_PROTOCOL.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.id) + IdentificationParametersCLIParams.MSGF_MIN_PEP_LENGTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.id) + IdentificationParametersCLIParams.MSGF_MAX_PEP_LENGTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_NUM_MATCHES.id) + IdentificationParametersCLIParams.MSGF_NUM_MATCHES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_ADDITIONAL.id) + IdentificationParametersCLIParams.MSGF_ADDITIONAL.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_ISOTOPE_LOW.id) + IdentificationParametersCLIParams.MSGF_ISOTOPE_LOW.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_ISOTOPE_HIGH.id) + IdentificationParametersCLIParams.MSGF_ISOTOPE_HIGH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_TERMINI.id) + IdentificationParametersCLIParams.MSGF_TERMINI.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MSGF_PTMS.id) + IdentificationParametersCLIParams.MSGF_PTMS.description + "\n";

        output += "\n\nOMSSA advanced parameters:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.id) + IdentificationParametersCLIParams.OMSSA_REMOVE_PREC.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_SCALE_PREC.id) + IdentificationParametersCLIParams.OMSSA_SCALE_PREC.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.id) + IdentificationParametersCLIParams.OMSSA_ESTIMATE_CHARGE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.id) + IdentificationParametersCLIParams.OMSSA_MAX_EVALUE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.id) + IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.id) + IdentificationParametersCLIParams.OMSSA_HITLIST_LENGTH_CHARGE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.id) + IdentificationParametersCLIParams.OMSSA_MIN_PEP_LENGTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.id) + IdentificationParametersCLIParams.OMSSA_MAX_PEP_LENGTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_FORMAT.id) + IdentificationParametersCLIParams.OMSSA_FORMAT.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.id) + IdentificationParametersCLIParams.OMSSA_SEQUENCES_IN_MEMORY.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_ISOTOPES.id) + IdentificationParametersCLIParams.OMSSA_ISOTOPES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_NEUTRON.id) + IdentificationParametersCLIParams.OMSSA_NEUTRON.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.id) + IdentificationParametersCLIParams.OMSSA_LOW_INTENSITY.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.id) + IdentificationParametersCLIParams.OMSSA_HIGH_INTENSITY.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.id) + IdentificationParametersCLIParams.OMSSA_INTENSITY_INCREMENT.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.id) + IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_WIDTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.id) + IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_WIDTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.id) + IdentificationParametersCLIParams.OMSSA_SINGLE_WINDOW_PEAKS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.id) + IdentificationParametersCLIParams.OMSSA_DOUBLE_WINDOW_PEAKS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.id) + IdentificationParametersCLIParams.OMSSA_MIN_ANNOTATED_INTENSE_PEAKS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.id) + IdentificationParametersCLIParams.OMSSA_MIN_PEAKS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_METHIONINE.id) + IdentificationParametersCLIParams.OMSSA_METHIONINE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.id) + IdentificationParametersCLIParams.OMSSA_MAX_LADDERS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.id) + IdentificationParametersCLIParams.OMSSA_MAX_FRAG_CHARGE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.id) + IdentificationParametersCLIParams.OMSSA_MAX_FRACTION.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_PLUS_ONE.id) + IdentificationParametersCLIParams.OMSSA_PLUS_ONE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.id) + IdentificationParametersCLIParams.OMSSA_POSITIVE_IONS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.id) + IdentificationParametersCLIParams.OMSSA_PREC_PER_SPECTRUM.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.id) + IdentificationParametersCLIParams.OMSSA_FORWARD_IONS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_REWIND_IONS.id) + IdentificationParametersCLIParams.OMSSA_REWIND_IONS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.id) + IdentificationParametersCLIParams.OMSSA_MAX_FRAG_SERIES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.id) + IdentificationParametersCLIParams.OMSSA_CORRELATION_CORRECTION.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.id) + IdentificationParametersCLIParams.OMSSA_CONSECUTIVE_ION_PROBABILITY.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.id) + IdentificationParametersCLIParams.OMSSA_ITERATIVE_SEQUENCE_EVALUE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.id) + IdentificationParametersCLIParams.OMSSA_ITERATIVE_SPECTRUM_EVALUE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.id) + IdentificationParametersCLIParams.OMSSA_ITERATIVE_REPLACE_EVALUE.description + "\n";

        output += "\n\nComet advanced parameters:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_NUM_MATCHES.id) + IdentificationParametersCLIParams.COMET_NUM_MATCHES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_PTMS.id) + IdentificationParametersCLIParams.COMET_PTMS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_REQ_PTMS.id) + IdentificationParametersCLIParams.COMET_REQ_PTMS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_MIN_PEAKS.id) + IdentificationParametersCLIParams.COMET_MIN_PEAKS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.id) + IdentificationParametersCLIParams.COMET_MIN_PEAK_INTENSITY.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.id) + IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.id) + IdentificationParametersCLIParams.COMET_REMOVE_PRECURSOR_TOLERANCE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.id) + IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_LOWER.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.id) + IdentificationParametersCLIParams.COMET_CLEAR_MZ_RANGE_UPPER.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_ENZYME_TYPE.id) + IdentificationParametersCLIParams.COMET_ENZYME_TYPE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.id) + IdentificationParametersCLIParams.COMET_ISOTOPE_CORRECTION.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.id) + IdentificationParametersCLIParams.COMET_MIN_PREC_MASS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.id) + IdentificationParametersCLIParams.COMET_MAX_PREC_MASS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.id) + IdentificationParametersCLIParams.COMET_MAX_FRAGMENT_CHARGE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_REMOVE_METH.id) + IdentificationParametersCLIParams.COMET_REMOVE_METH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_BATCH_SIZE.id) + IdentificationParametersCLIParams.COMET_BATCH_SIZE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.id) + IdentificationParametersCLIParams.COMET_THEORETICAL_FRAGMENT_IONS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.id) + IdentificationParametersCLIParams.COMET_FRAGMENT_BIN_OFFSET.description + "\n";

        output += "\n\nTide advanced parameters:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_PTMS.id) + IdentificationParametersCLIParams.TIDE_PTMS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.id) + IdentificationParametersCLIParams.TIDE_PTMS_PER_TYPE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.id) + IdentificationParametersCLIParams.TIDE_MIN_PEP_LENGTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.id) + IdentificationParametersCLIParams.TIDE_MAX_PEP_LENGTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.id) + IdentificationParametersCLIParams.TIDE_MIN_PREC_MASS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.id) + IdentificationParametersCLIParams.TIDE_MAX_PREC_MASS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.id) + IdentificationParametersCLIParams.TIDE_DECOY_FORMAT.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.id) + IdentificationParametersCLIParams.TIDE_KEEP_TERM_AA.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_DECOY_SEED.id) + IdentificationParametersCLIParams.TIDE_DECOY_SEED.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_OUTPUT_FOLDER.id) + IdentificationParametersCLIParams.TIDE_OUTPUT_FOLDER.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.id) + IdentificationParametersCLIParams.TIDE_PRINT_PEPTIDES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_VERBOSITY.id) + IdentificationParametersCLIParams.TIDE_VERBOSITY.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.id) + IdentificationParametersCLIParams.TIDE_MONOISOTOPIC.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.id) + IdentificationParametersCLIParams.TIDE_CLIP_N_TERM.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.id) + IdentificationParametersCLIParams.TIDE_DIGESTION_TYPE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_COMPUTE_SP.id) + IdentificationParametersCLIParams.TIDE_COMPUTE_SP.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_MAX_PSMS.id) + IdentificationParametersCLIParams.TIDE_MAX_PSMS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_COMPUTE_P.id) + IdentificationParametersCLIParams.TIDE_COMPUTE_P.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.id) + IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_MZ.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.id) + IdentificationParametersCLIParams.TIDE_MAX_SPECTRUM_MZ.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.id) + IdentificationParametersCLIParams.TIDE_MIN_SPECTRUM_PEAKS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.id) + IdentificationParametersCLIParams.TIDE_SPECTRUM_CHARGES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_REMOVE_PREC.id) + IdentificationParametersCLIParams.TIDE_REMOVE_PREC.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.id) + IdentificationParametersCLIParams.TIDE_REMOVE_PREC_TOL.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.id) + IdentificationParametersCLIParams.TIDE_PROGRESS_INDICATOR.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_USE_FLANKING.id) + IdentificationParametersCLIParams.TIDE_USE_FLANKING.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.id) + IdentificationParametersCLIParams.TIDE_USE_NEUTRAL_LOSSES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.id) + IdentificationParametersCLIParams.TIDE_MZ_BIN_WIDTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.id) + IdentificationParametersCLIParams.TIDE_MZ_BIN_OFFSET.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_CONCAT.id) + IdentificationParametersCLIParams.TIDE_CONCAT.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_STORE_SPECTRA.id) + IdentificationParametersCLIParams.TIDE_STORE_SPECTRA.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.id) + IdentificationParametersCLIParams.TIDE_EXPORT_TEXT.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_EXPORT_SQT.id) + IdentificationParametersCLIParams.TIDE_EXPORT_SQT.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.id) + IdentificationParametersCLIParams.TIDE_EXPORT_PEPXML.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_EXPORT_MZID.id) + IdentificationParametersCLIParams.TIDE_EXPORT_MZID.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_EXPORT_PIN.id) + IdentificationParametersCLIParams.TIDE_EXPORT_PIN.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.id) + IdentificationParametersCLIParams.TIDE_REMOVE_TEMP.description + "\n";

        output += "\n\nAndromeda advanced parameters:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.id) + IdentificationParametersCLIParams.ANDROMEDA_MAX_PEPTIDE_MASS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.id) + IdentificationParametersCLIParams.ANDROMEDA_MAX_COMBINATIONS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS.id) + IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.id) + IdentificationParametersCLIParams.ANDROMEDA_TOP_PEAKS_WINDOW.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.id) + IdentificationParametersCLIParams.ANDROMEDA_INCL_WATER.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.id) + IdentificationParametersCLIParams.ANDROMEDA_INCL_AMMONIA.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.id) + IdentificationParametersCLIParams.ANDROMEDA_NEUTRAL_LOSSES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.id) + IdentificationParametersCLIParams.ANDROMEDA_FRAGMENT_ALL.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.id) + IdentificationParametersCLIParams.ANDROMEDA_EMP_CORRECTION.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.id) + IdentificationParametersCLIParams.ANDROMEDA_HIGHER_CHARGE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.id) + IdentificationParametersCLIParams.ANDROMEDA_FRAG_METHOD.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.id) + IdentificationParametersCLIParams.ANDROMEDA_MAX_MODS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.id) + IdentificationParametersCLIParams.ANDROMEDA_MIN_PEP_LENGTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.id) + IdentificationParametersCLIParams.ANDROMEDA_MAX_PEP_LENGTH.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.id) + IdentificationParametersCLIParams.ANDROMEDA_EQUAL_IL.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.id) + IdentificationParametersCLIParams.ANDROMEDA_MAX_PSMS.description + "\n";

        output += "\n\nPeptideShaker advanced parameters:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.SPECIES_TYPE.id) + IdentificationParametersCLIParams.SPECIES_TYPE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.SPECIES.id) + IdentificationParametersCLIParams.SPECIES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANNOTATION_LEVEL.id) + IdentificationParametersCLIParams.ANNOTATION_LEVEL.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANNOTATION_MZ_TOLERANCE.id) + IdentificationParametersCLIParams.ANNOTATION_MZ_TOLERANCE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.id) + IdentificationParametersCLIParams.ANNOTATION_HIGH_RESOLUTION.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.SEQUENCE_MATCHING_TYPE.id) + IdentificationParametersCLIParams.SEQUENCE_MATCHING_TYPE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.SEQUENCE_MATCHING_X.id) + IdentificationParametersCLIParams.SEQUENCE_MATCHING_X.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MIN.id) + IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MIN.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MAX.id) + IdentificationParametersCLIParams.IMPORT_PEPTIDE_LENGTH_MAX.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ.id) + IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ_PPM.id) + IdentificationParametersCLIParams.IMPORT_PRECURSOR_MZ_PPM.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.EXCLUDE_UNKNOWN_PTMs.id) + IdentificationParametersCLIParams.EXCLUDE_UNKNOWN_PTMs.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.PTM_SCORE.id) + IdentificationParametersCLIParams.PTM_SCORE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.PTM_THRESHOLD.id) + IdentificationParametersCLIParams.PTM_THRESHOLD.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.SCORE_NEUTRAL_LOSSES.id) + IdentificationParametersCLIParams.SCORE_NEUTRAL_LOSSES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.PTM_SEQUENCE_MATCHING_TYPE.id) + IdentificationParametersCLIParams.PTM_SEQUENCE_MATCHING_TYPE.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.DB_PI.id) + IdentificationParametersCLIParams.DB_PI.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.PSM_FDR.id) + IdentificationParametersCLIParams.PSM_FDR.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.PEPTIDE_FDR.id) + IdentificationParametersCLIParams.PEPTIDE_FDR.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.PROTEIN_FDR.id) + IdentificationParametersCLIParams.PROTEIN_FDR.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.SEPARATE_PSMs.id) + IdentificationParametersCLIParams.SEPARATE_PSMs.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.SEPARATE_PEPTIDES.id) + IdentificationParametersCLIParams.SEPARATE_PEPTIDES.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MERGE_SUBGROUPS.id) + IdentificationParametersCLIParams.MERGE_SUBGROUPS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.PROTEIN_FRACTION_MW_CONFIDENCE.id) + IdentificationParametersCLIParams.PROTEIN_FRACTION_MW_CONFIDENCE.description + "\n";
        
        output += "\n\nHelp:\n\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.MODS.id) + IdentificationParametersCLIParams.MODS.description + "\n";
        output += "-" + String.format(formatter, IdentificationParametersCLIParams.USAGE.id) + IdentificationParametersCLIParams.USAGE.description + "\n";

        return output;
    }
}
