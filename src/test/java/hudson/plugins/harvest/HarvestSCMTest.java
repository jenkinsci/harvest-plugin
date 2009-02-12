/**
 * 
 */
package hudson.plugins.harvest;

import static org.junit.Assert.*;

import hudson.scm.ChangeLogSet;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author G&aacute;bor Lipt&aacute;k
 *
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
		InputStream is=getClass().getResourceAsStream("/hco.sync.txt");
		HarvestSCM scm=new HarvestSCM("", "", "", "", "", "", "", "", "");
		ChangeLogSet<HarvestChangeLogEntry> changes=scm.parse(null, is);
		assertEquals(2, changes.getItems().length);
	}

	@Test (expected=IllegalArgumentException.class)
	public final void testParseError() throws IOException {
		InputStream is=getClass().getResourceAsStream("/hco.syncerror.txt");
		HarvestSCM scm=new HarvestSCM("", "", "", "", "", "", "", "", "");
		scm.parse(null, is);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public final void testParseFail() throws IOException {
		InputStream is=getClass().getResourceAsStream("/hco.syncfail.txt");
		HarvestSCM scm=new HarvestSCM("", "", "", "", "", "", "", "", "");
		scm.parse(null, is);
	}
}
