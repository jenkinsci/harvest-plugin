/**
 * 
 */
package hudson.plugins.harvest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormFieldValidator;

/**
 * @author G&aacute;bor Lipt&aacute;k
 *
 */
public class HarvestSCM extends SCM {

	private String broker = null;
    private String userId = null;
    private String password = null;
    private String projectName = null;
    private String state = null;
    private String viewPath = null;
    private String clientPath = null;
    private String processName = null;
    private String recursiveSearch = null;

	/**
	 * Constructor
	 * @param broker
	 * @param userId
	 * @param password
	 * @param projectName
	 * @param state
	 * @param viewPath
	 * @param clientPath
	 * @param processName
	 * @param recursiveSearch
	 */
    @DataBoundConstructor
	public HarvestSCM(String broker, String userId, String password, String projectName,
			String state, String viewPath, String clientPath, String processName,
			String recursiveSearch){
		this.broker=broker;
		this.userId=userId;
		this.password=password;
		this.projectName=projectName;
		this.state=state;
		this.viewPath=viewPath;
		this.clientPath=clientPath;
		this.processName=processName;
		this.recursiveSearch=recursiveSearch;
	}
	
    @Override
	public boolean supportsPolling() {
		return false;
	}

	private final Log logger = LogFactory.getLog(getClass());

    /* (non-Javadoc)
	 * @see hudson.scm.SCM#checkout(hudson.model.AbstractBuild, hudson.Launcher, hudson.FilePath, hudson.model.BuildListener, java.io.File)
	 */
	@Override
	public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspace,
			BuildListener listener, File changeLog) throws IOException,
			InterruptedException {
		boolean checkoutSucceeded=false;
        
        logger.debug("deleting contents of workspace " + workspace);
        workspace.deleteContents();
        
		logger.debug("starting checkout");
        
        ArgumentListBuilder cmd = new ArgumentListBuilder();
        cmd.add(getDescriptor().getExecutable());
        cmd.add("-b", getBroker());
        cmd.add("-usr", getUserId());
        cmd.add("-pw", getPassword());
        cmd.add("-en", getProjectName());
        cmd.add("-st", getState());
        cmd.add("-vp", getViewPath());
        cmd.add("-cp");
        cmd.addQuoted(workspace.getRemote());
        cmd.add("-pn", getProcessName());
        cmd.add("-s");
        cmd.addQuoted(getRecursiveSearch());
        cmd.add("-br");
        cmd.add("-r");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        logger.debug("launching command " + cmd.toList());
        
        Proc proc = launcher.launch(cmd.toCommandArray(), new String[0], baos, workspace);
        int rc = proc.join();

        if (rc != 0) {
            logger.error("command exited with " + rc);
            listener.error("command exited with " + rc);
            
        } else { 
            checkoutSucceeded = true; 
            if (logger.isDebugEnabled()){
                logger.debug("hco output:\n" + new String(baos.toByteArray()));            	
            }
        }
        listener.getLogger().write(baos.toByteArray(), 0, baos.size());
        listener.getLogger().println("reading from "+workspace.getRemote()+File.separator+"hco.log: ");
        
        BufferedReader r=new BufferedReader(new FileReader(workspace.getRemote()+File.separator+"hco.log"));
        try {
        	String line=null;
        	while ((line=r.readLine())!=null){
        		listener.getLogger().println(line);
        	}
        } finally {
        	if (r!=null){
        		r.close();
        	}
        }

        logger.debug("completing checkout");
        return checkoutSucceeded;
	}

	/* (non-Javadoc)
	 * @see hudson.scm.SCM#createChangeLogParser()
	 */
	@Override
	public ChangeLogParser createChangeLogParser() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see hudson.scm.SCM#getDescriptor()
	 */
	@Override
	public DescriptorImpl getDescriptor() {
		// TODO Auto-generated method stub
		return DescriptorImpl.DESCRIPTOR;
	}

	/* (non-Javadoc)
	 * @see hudson.scm.SCM#pollChanges(hudson.model.AbstractProject, hudson.Launcher, hudson.FilePath, hudson.model.TaskListener)
	 */
	@Override
	public boolean pollChanges(AbstractProject arg0, Launcher arg1,
			FilePath arg2, TaskListener arg3) throws IOException,
			InterruptedException {
		return false;
	}

    /**
	 * @return the broker
	 */
	public String getBroker() {
		return broker;
	}

	/**
	 * @param broker the broker to set
	 */
	public void setBroker(String broker) {
		this.broker = broker;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the projectName
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * @param projectName the projectName to set
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the viewPath
	 */
	public String getViewPath() {
		return viewPath;
	}

	/**
	 * @param viewPath the viewPath to set
	 */
	public void setViewPath(String viewPath) {
		this.viewPath = viewPath;
	}

	/**
	 * @return the clientPath
	 */
	public String getClientPath() {
		return clientPath;
	}

	/**
	 * @param clientPath the clientPath to set
	 */
	public void setClientPath(String clientPath) {
		this.clientPath = clientPath;
	}

	/**
	 * @return the processName
	 */
	public String getProcessName() {
		return processName;
	}

	/**
	 * @param processName the processName to set
	 */
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	/**
	 * @return the recursiveSearch
	 */
	public String getRecursiveSearch() {
		return recursiveSearch;
	}

	/**
	 * @param recursiveSearch the recursiveSearch to set
	 */
	public void setRecursiveSearch(String recursiveSearch) {
		this.recursiveSearch = recursiveSearch;
	}

    public static final class DescriptorImpl extends SCMDescriptor<HarvestSCM> {

    	private String executable="hco";
    	
		private static final Log LOGGER = LogFactory.getLog(DescriptorImpl.class);
        
		public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

		private DescriptorImpl() {
            super(HarvestSCM.class, null);
            load();
        }
        
		/* (non-Javadoc)
		 * @see hudson.model.Descriptor#configure(org.kohsuke.stapler.StaplerRequest)
		 */
		@Override
		public boolean configure(StaplerRequest req) throws FormException {
            LOGGER.debug("configuring from " + req);
            
            executable = Util.fixEmpty(req.getParameter("harvest.executable").trim());
            save();
			return true;
		}

        /**
         * 
         */
        public void doExecutableCheck(final StaplerRequest req, final StaplerResponse resp)
            throws IOException, ServletException {
        		new FormFieldValidator.Executable(req, resp).process();
        }
        
		@Override
		public String getDisplayName() {
			return "CA Harvest";
		}

        public String getExecutable() {
            return executable;
        }
}

}
