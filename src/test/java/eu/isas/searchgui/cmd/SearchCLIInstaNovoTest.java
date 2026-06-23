package eu.isas.searchgui.cmd;

import eu.isas.searchgui.SearchHandler;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.Assert;

/**
 * Tests the InstaNovo SearchCLI integration.
 *
 * @author CompOmics
 */
public class SearchCLIInstaNovoTest extends TestCase {

    /**
     * Tests parsing of the InstaNovo command line options.
     *
     * @throws Exception if an exception occurs
     */
    public void testInstaNovoCliParsing() throws Exception {

        File instaNovoFolder = createFolder("instanovo-cli");
        CommandLine commandLine = parse(
                "-spectrum_files", createFile("input", ".mgf").getAbsolutePath(),
                "-fasta_file", createFile("database", ".fasta").getAbsolutePath(),
                "-output_folder", createFolder("searchgui-output").getAbsolutePath(),
                "-instanovo", "1",
                "-instanovo_plus", "1",
                "-instanovo_refine", "1",
                "-instanovo_folder", instaNovoFolder.getAbsolutePath()
        );

        SearchCLIInputBean inputBean = new SearchCLIInputBean(commandLine);

        Assert.assertTrue(inputBean.isInstaNovoEnabled());
        Assert.assertTrue(inputBean.isInstaNovoPlusEnabled());
        Assert.assertTrue(inputBean.isInstaNovoRefineEnabled());
        Assert.assertEquals(instaNovoFolder.getAbsoluteFile(), inputBean.getInstaNovoLocation().getAbsoluteFile());

        CommandLine deNovoOnlyCommandLine = parse(
                "-spectrum_files", createFile("input", ".mgf").getAbsolutePath(),
                "-output_folder", createFolder("searchgui-output").getAbsolutePath(),
                "-instanovo", "1",
                "-instanovo_folder", instaNovoFolder.getAbsolutePath()
        );

        SearchCLIInputBean deNovoOnlyInputBean = new SearchCLIInputBean(deNovoOnlyCommandLine);

        Assert.assertTrue(deNovoOnlyInputBean.isInstaNovoEnabled());
        Assert.assertNull(deNovoOnlyInputBean.getFastaFile());
    }

    /**
     * Tests validation and help listing of the InstaNovo command line options.
     *
     * @throws Exception if an exception occurs
     */
    public void testInstaNovoCliValidationAndHelp() throws Exception {

        CommandLine invalidBoolean = parse(
                "-spectrum_files", createFile("input", ".mgf").getAbsolutePath(),
                "-fasta_file", createFile("database", ".fasta").getAbsolutePath(),
                "-output_folder", createFolder("searchgui-output").getAbsolutePath(),
                "-instanovo", "2"
        );
        Assert.assertFalse(SearchCLIInputBean.isValidStartup(invalidBoolean));

        CommandLine missingFolder = parse(
                "-spectrum_files", createFile("input", ".mgf").getAbsolutePath(),
                "-fasta_file", createFile("database", ".fasta").getAbsolutePath(),
                "-output_folder", createFolder("searchgui-output").getAbsolutePath(),
                "-instanovo_folder", new File(createFolder("missing-parent"), "missing").getAbsolutePath()
        );
        Assert.assertFalse(SearchCLIInputBean.isValidStartup(missingFolder));

        CommandLine databaseSearchWithoutFasta = parse(
                "-spectrum_files", createFile("input", ".mgf").getAbsolutePath(),
                "-output_folder", createFolder("searchgui-output").getAbsolutePath(),
                "-msgf", "1"
        );
        Assert.assertFalse(SearchCLIInputBean.isValidStartup(databaseSearchWithoutFasta));

        CommandLine noEngineSelected = parse(
                "-spectrum_files", createFile("input", ".mgf").getAbsolutePath(),
                "-fasta_file", createFile("database", ".fasta").getAbsolutePath(),
                "-output_folder", createFolder("searchgui-output").getAbsolutePath()
        );
        Assert.assertFalse(SearchCLIInputBean.isValidStartup(noEngineSelected));

        String help = SearchCLIParams.getOptionsAsString();
        Assert.assertTrue(help.contains("-instanovo"));
        Assert.assertTrue(help.contains("-instanovo_plus"));
        Assert.assertTrue(help.contains("-instanovo_refine"));
        Assert.assertTrue(help.contains("-instanovo_folder"));
        Assert.assertTrue(help.contains("Conditional Parameters"));
        Assert.assertTrue(help.contains("optional for de novo-only searches"));
    }

    /**
     * Tests that de novo-only input manifests do not contain blank FASTA lines.
     *
     * @throws Exception if an exception occurs
     */
    public void testDeNovoOnlyInputManifestHasNoBlankFastaLine() throws Exception {

        File outputFolder = createFolder("searchgui-input-manifest");
        File spectrumFile = createFile("input", ".mgf");

        SearchHandler searchHandler = new SearchHandler();
        searchHandler.setFastaFile(null);

        ArrayList<File> spectrumFiles = new ArrayList<>();
        spectrumFiles.add(spectrumFile);
        searchHandler.setSpectrumFiles(spectrumFiles);
        searchHandler.saveInputFile(outputFolder);

        List<String> lines = Files.readAllLines(SearchHandler.getInputFile(outputFolder).toPath());

        Assert.assertEquals(1, lines.size());
        Assert.assertEquals(spectrumFile.getAbsolutePath(), lines.get(0));
        Assert.assertFalse(lines.get(0).isEmpty());
    }

    /**
     * Parses command line arguments.
     *
     * @param args the command line arguments
     *
     * @return the parsed command line
     *
     * @throws Exception if an exception occurs
     */
    private CommandLine parse(String... args) throws Exception {

        Options options = new Options();
        SearchCLIParams.createOptionsCLI(options);

        return new DefaultParser().parse(options, args);
    }

    /**
     * Creates a temporary file.
     *
     * @param prefix the prefix
     * @param suffix the suffix
     *
     * @return the file
     *
     * @throws Exception if an exception occurs
     */
    private File createFile(String prefix, String suffix) throws Exception {

        File file = File.createTempFile(prefix, suffix);
        file.deleteOnExit();

        return file;
    }

    /**
     * Creates a temporary folder.
     *
     * @param prefix the prefix
     *
     * @return the folder
     *
     * @throws Exception if an exception occurs
     */
    private File createFolder(String prefix) throws Exception {

        File folder = File.createTempFile(prefix, "");
        folder.delete();
        folder.mkdirs();
        folder.deleteOnExit();

        return folder;
    }
}
