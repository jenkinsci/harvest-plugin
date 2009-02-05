/**
 * 
 */
package hudson.plugins.harvest;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author G&aacute;bor Lipt&aacute;k
 *
 */
public class HarvestSCMTest extends TestCase {

	/**
	 * @param name
	 */
	public HarvestSCMTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link hudson.plugins.harvest.HarvestSCM#checkout(hudson.model.AbstractBuild, hudson.Launcher, hudson.FilePath, hudson.model.BuildListener, java.io.File)}.
	 * @throws IOException 
	 */
	public final void testCheckoutAbstractBuildLauncherFilePathBuildListenerFile() throws IOException {
	}

	public final void testCheckoutNotFound() {
		String harvestHome="C:\\harvestnotinstalledhere";
		try {
			Process p=Runtime.getRuntime().exec(harvestHome+File.separator+"hco");
			fail("expected IOException");
		} catch (IOException e) {
			// SUCCESS;
		}
	}
}
