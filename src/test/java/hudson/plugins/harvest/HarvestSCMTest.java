/**
 * 
 */
package hudson.plugins.harvest;

import static org.junit.Assert.*;

import hudson.scm.ChangeLogSet;
import hudson.util.ArgumentListBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
		HarvestSCM scm=new HarvestSCM("", "", "", "", "", "", "", "", "", true);
		ChangeLogSet<HarvestChangeLogEntry> changes=scm.parse(null, is);
		assertEquals(2, changes.getItems().length);
	}

	@Test (expected=IllegalArgumentException.class)
	public final void testParseError() throws IOException {
		InputStream is=getClass().getResourceAsStream("/hco.syncerror.txt");
		HarvestSCM scm=new HarvestSCM("", "", "", "", "", "", "", "", "", true);
		scm.parse(null, is);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public final void testParseFail() throws IOException {
		InputStream is=getClass().getResourceAsStream("/hco.syncfail.txt");
		HarvestSCM scm=new HarvestSCM("", "", "", "", "", "", "", "", "", true);
		scm.parse(null, is);
	}
	
	@Test
	public final void testPrepareCommandSynch(){
		HarvestSCM scm=new HarvestSCM("broker", "user", "password",
				"project", "DEV", "/Project", "bar", "Checkout", "", true);
		ArgumentListBuilder cmd=scm.prepareCommand("hco.exe", "c:\\foo");
		List<String> parts=cmd.toList();
		StringBuffer sb=new StringBuffer();
		for (String s: parts){
			sb.append(s);
			sb.append(" ");
		}
		assertEquals("hco.exe -b broker -usr user -pw password -en project -st DEV -vp /Project -cp \"c:\\foo"+File.separator+"bar\" -pn Checkout -s \"\" -sy -nt -r "
				, sb.toString());
	}

	@Test
	public final void testPrepareCommandNoSynch(){
		HarvestSCM scm=new HarvestSCM("broker", "user", "password",
				"project", "DEV", "/Project", "bar", "Checkout", "", false);
		ArgumentListBuilder cmd=scm.prepareCommand("hco.exe", "c:\\foo");
		List<String> parts=cmd.toList();
		StringBuffer sb=new StringBuffer();
		for (String s: parts){
			sb.append(s);
			sb.append(" ");
		}
		assertEquals("hco.exe -b broker -usr user -pw password -en project -st DEV -vp /Project -cp \"c:\\foo"+File.separator+"bar\" -pn Checkout -s \"\" -br -r "
				, sb.toString());
	}
}
