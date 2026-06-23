package eu.isas.searchgui.gui;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Tests loading InstaNovo model ids.
 *
 * @author CompOmics
 */
public class SearchGUIInstaNovoModelsTest extends TestCase {

    /**
     * Tests loading transformer and diffusion model ids from models.json.
     *
     * @throws Exception if an exception occurs
     */
    public void testGetInstaNovoModels() throws Exception {

        File folder = createFolder("instanovo-models");
        File instaNovoFolder = new File(folder, "instanovo");
        Assert.assertTrue(instaNovoFolder.mkdirs());

        File modelsFile = new File(instaNovoFolder, "models.json");

        try (FileWriter writer = new FileWriter(modelsFile)) {
            writer.write("{\n"
                    + "  \"transformer\": {\n"
                    + "    \"instanovo-v1.2.0\": {\"remote\": \"transformer-1\"},\n"
                    + "    \"instanovo-v1.1.0\": {\"remote\": \"transformer-2\"}\n"
                    + "  },\n"
                    + "  \"diffusion\": {\n"
                    + "    \"instanovoplus-v1.1.0\": {\"remote\": \"diffusion-1\"}\n"
                    + "  }\n"
                    + "}\n");
        }

        ArrayList<String> transformerModels = SearchGUI.getInstaNovoModels(folder, "transformer");
        Assert.assertEquals(2, transformerModels.size());
        Assert.assertEquals("instanovo-v1.2.0", transformerModels.get(0));
        Assert.assertEquals("instanovo-v1.1.0", transformerModels.get(1));

        ArrayList<String> diffusionModels = SearchGUI.getInstaNovoModels(folder, "diffusion");
        Assert.assertEquals(1, diffusionModels.size());
        Assert.assertEquals("instanovoplus-v1.1.0", diffusionModels.get(0));

        Assert.assertTrue(SearchGUI.getInstaNovoModels(folder, "missing").isEmpty());
        Assert.assertTrue(SearchGUI.getInstaNovoModels(null, "transformer").isEmpty());
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

        File file = File.createTempFile(prefix, "");
        Assert.assertTrue(file.delete());
        Assert.assertTrue(file.mkdirs());
        file.deleteOnExit();

        return file;
    }
}
