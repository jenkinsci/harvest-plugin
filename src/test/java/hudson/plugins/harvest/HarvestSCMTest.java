/**
 *
 */
package hudson.plugins.harvest;

import hudson.scm.ChangeLogSet;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang.SystemUtils;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author G&aacute;bor Lipt&aacute;k
 */
public class HarvestSCMTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testParse() throws IOException {
        InputStream is = getClass().getResourceAsStream("/hco.sync.txt");
        HarvestSCM scm = new HarvestSCM("", "", "", "", "", "", "", "", "", "", true, "");
        List<HarvestChangeLogEntry> listOfChanges = new ArrayList<HarvestChangeLogEntry>();
        scm.parse(is, listOfChanges);
        ChangeLogSet<HarvestChangeLogEntry> history = new HarvestChangeLogSet(null, listOfChanges);
        assertEquals(2, history.getItems().length);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testParseError() throws IOException {
        InputStream is = getClass().getResourceAsStream("/hco.syncerror.txt");
        HarvestSCM scm = new HarvestSCM("", "", "", "", "", "", "", "", "", "", true, "");
        List<HarvestChangeLogEntry> listOfChanges = new ArrayList<HarvestChangeLogEntry>();
        scm.parse(is, listOfChanges);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testParseFail() throws IOException {
        InputStream is = getClass().getResourceAsStream("/hco.syncfail.txt");
        HarvestSCM scm = new HarvestSCM("", "", "", "", "", "", "", "", "", "", true, "");
        List<HarvestChangeLogEntry> listOfChanges = new ArrayList<HarvestChangeLogEntry>();
        scm.parse(is, listOfChanges);
    }

    @Test
    public final void testPrepareCommandSynch() throws Exception {
        HarvestSCM scm = new HarvestSCM("broker", null, "user", "password",
                "project", "DEV", "/Project", "bar", "Checkout", "*", true, "");
        ArgumentListBuilder cmd = scm.prepareCommand("hco.exe", null, null, null, null, "c:\\foo");
        List<String> parts = cmd.toList();
        StringBuffer sb = new StringBuffer();
        for (String s : parts) {
            sb.append(s);
            sb.append(" ");
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertEquals("hco.exe -b broker -usr user -pw password -en project -st DEV -vp /Project -cp c:\\foo" + File.separator + "bar -pn Checkout -s * -sy -nt -r "
                    , sb.toString());
        } else {
            assertEquals("hco.exe -b broker -usr user -pw password -en project -st DEV -vp /Project -cp \"c:\\foo" + File.separator + "bar\" -pn Checkout -s \"*\" -sy -nt -r "
                    , sb.toString());
        }

    }

    @Test
    public final void testPrepareCommandNoSynch() throws Exception {
        HarvestSCM scm = new HarvestSCM("broker", null, "user", "password",
                "project", "DEV", "/Project", "bar", "Checkout", "*", false, "");
        ArgumentListBuilder cmd = scm.prepareCommand("hco.exe", null, null, null, null, "c:\\foo");
        List<String> parts = cmd.toList();
        StringBuffer sb = new StringBuffer();
        for (String s : parts) {
            sb.append(s);
            sb.append(" ");
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertEquals("hco.exe -b broker -usr user -pw password -en project -st DEV -vp /Project -cp c:\\foo" + File.separator + "bar -pn Checkout -s * -br -r "
                    , sb.toString());
        } else {
            assertEquals("hco.exe -b broker -usr user -pw password -en project -st DEV -vp /Project -cp \"c:\\foo" + File.separator + "bar\" -pn Checkout -s \"*\" -br -r "
                    , sb.toString());
        }
    }
}
