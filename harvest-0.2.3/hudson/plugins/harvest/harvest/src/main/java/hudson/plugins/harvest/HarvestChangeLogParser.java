package hudson.plugins.harvest;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;

/**
 * @author G&aacute;bor Lipt&aacute;k
 *
 */
public class HarvestChangeLogParser extends ChangeLogParser {

	@Override
	public ChangeLogSet<HarvestChangeLogEntry> parse(AbstractBuild build, File changelogFile)
			throws IOException, SAXException {
        return HarvestChangeLogSet.parse(build, changelogFile);
	}

}
