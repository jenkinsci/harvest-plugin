package hudson.plugins.harvest;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * @author G&aacute;bor Lipt&aacute;k
 */
public class HarvestChangeLogParser extends ChangeLogParser {

    @Override
    public ChangeLogSet<HarvestChangeLogEntry> parse(AbstractBuild build, File changelogFile)
            throws IOException, SAXException {
        return HarvestChangeLogSet.parse(build, changelogFile);
    }

}
