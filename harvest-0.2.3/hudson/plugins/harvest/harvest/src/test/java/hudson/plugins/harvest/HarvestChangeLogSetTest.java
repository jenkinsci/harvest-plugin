package hudson.plugins.harvest;

import static org.junit.Assert.assertEquals;
import hudson.scm.ChangeLogSet;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class HarvestChangeLogSetTest {

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
	public void testSaveToChangeLog() {		
	}

	@Test
	public void testParseCheckout() throws IOException, SAXException {		
		InputStream syncIs=getClass().getResourceAsStream("/hco.sync.txt");
		HarvestSCM scm=new HarvestSCM("", "", "", "", "", "", "", "", "", false);
		ChangeLogSet<HarvestChangeLogEntry> syncChanges=scm.parse(null, syncIs);
		InputStream xmlIs=getClass().getResourceAsStream("/changelog.xml");
		ChangeLogSet<HarvestChangeLogEntry> xmlChanges=HarvestChangeLogSet.parse(null, xmlIs);
		assertEquals(syncChanges.getItems().length, xmlChanges.getItems().length);
	}

	@Test
	public void testParseSync() throws IOException, SAXException {		
		InputStream syncIs=getClass().getResourceAsStream("/hco.sync.txt");
		HarvestSCM scm=new HarvestSCM("", "", "", "", "", "", "", "", "", true);
		ChangeLogSet<HarvestChangeLogEntry> syncChanges=scm.parse(null, syncIs);
		InputStream xmlIs=getClass().getResourceAsStream("/changelog.xml");
		ChangeLogSet<HarvestChangeLogEntry> xmlChanges=HarvestChangeLogSet.parse(null, xmlIs);
		assertEquals(syncChanges.getItems().length, xmlChanges.getItems().length);
	}
}
