package hudson.plugins.harvest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import hudson.Plugin;
import hudson.scm.SCMS;

/**
 * Entry point of a plugin.
 *
 * <p>
 * There must be one {@link Plugin} class in each plugin.
 * See javadoc of {@link Plugin} for more about what can be done on this class.
 *
 * @author G&aacute;bor Lipt&aacute;k
 */
public class PluginImpl extends Plugin {
    private final Log logger = LogFactory.getLog(getClass());

    /**
     * Registers Harvest SCM.
     */
    @Override
    public void start() throws Exception {
    	HarvestSCM.DescriptorImpl desc = HarvestSCM.DescriptorImpl.DESCRIPTOR;
        
        logger.debug("adding " + desc + " to SCMS");
        
        SCMS.SCMS.add(desc);

        super.start();
    }
}
