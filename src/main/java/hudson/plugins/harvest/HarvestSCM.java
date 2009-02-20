/**
 * 
 */
package hudson.plugins.harvest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
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
import hudson.scm.ChangeLogSet;
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
	private boolean useSynchronize=true;
	

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
			String recursiveSearch, Boolean useSynchronize){
		this.broker=broker;
		this.userId=userId;
		this.password=password;
		this.projectName=projectName;
		this.state=state;
		this.viewPath=viewPath;
		this.clientPath=clientPath;
		this.processName=processName;
		this.recursiveSearch=recursiveSearch;
		this.useSynchronize=useSynchronize;
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
			BuildListener listener, File changeLogFile) throws IOException,
			InterruptedException {

		if (!useSynchronize){
	        logger.debug("deleting contents of workspace " + workspace);
	        workspace.deleteContents();			
		}
        
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
		if (!useSynchronize){
			cmd.add("-br");
		} else {
			cmd.add("-sy");
		}
        cmd.add("-r");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        logger.debug("launching command " + cmd.toList());
        
        Proc proc = launcher.launch(cmd.toCommandArray(), new String[0], baos, workspace);
        // ignoring rc as sync might return 3 on success ...
        int rc = proc.join();

        if (!useSynchronize){
            createEmptyChangeLog(changeLogFile, listener, "changelog");         	
        } else {
        	FileInputStream fileInputStream=new FileInputStream(new File(workspace.getRemote()+File.separator+"hco.log"));
        	ChangeLogSet<HarvestChangeLogEntry> history=parse(build, fileInputStream);
            FileOutputStream fileOutputStream = new FileOutputStream(changeLogFile);
            HarvestChangeLogSet.saveToChangeLog(fileOutputStream, history);
            fileOutputStream.close();
            fileInputStream.close();
        }
        
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
        return true;
	}

	protected ChangeLogSet<HarvestChangeLogEntry> parse(AbstractBuild build, InputStream inputStream) throws IOException {
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<HarvestChangeLogEntry> history = new ArrayList<HarvestChangeLogEntry>();
        Pattern pCheckout = Pattern.compile("I00020110: File (.*);([.\\d]+)  checked out to .*");
        Pattern pSummary = Pattern.compile("I00060080: Check out summary: Total: (\\d+) ; Success: (\\d+) ; Failed: (\\d+) ; Not Processed: (\\d+) \\.");
        String line="";
        while ((line=br.readLine())!=null){
        	if (StringUtils.indexOf(line, "E")==0){
        		throw new IllegalArgumentException("error on line "+line);        		
        	} else
        	// I00060040: New connection with Broker broker  established.
        	if (StringUtils.indexOf(line, "I00060040:")==0){
        		continue;
        	} else
        		// I00020052:  No need to update file c:\dir1\file1  from repository version \repository\dir1\file1;0 .
        		if (StringUtils.indexOf(line, "I00020052:")==0){
            		continue;        		
        	} else 
        		// I00020110: File \repository\project\dir3\file5;1  checked out to server\\C:\.hudson\jobs\project\workspace\project\dir3\file5 .
        		if (StringUtils.indexOf(line, "I00020110:")==0) {
        			HarvestChangeLogEntry e=new HarvestChangeLogEntry();
        			Matcher m = pCheckout.matcher(line);
        			if (!m.matches()){
                		throw new IllegalArgumentException("could not parse checkout line "+line);
        			}
        			e.setFullName(m.group(1));
        			e.setVersion(m.group(2));
        			history.add(e);
        	} else 
        		// I00060080: Check out summary: Total: 999 ; Success: 999 ; Failed: 0 ; Not Processed: 0 .
        		if (StringUtils.indexOf(line, "I00060080:")==0){
        			Matcher m = pSummary.matcher(line);
        			if (!m.matches()){
                		throw new IllegalArgumentException("could not parse checkout line "+line);
        			}
        			if (!StringUtils.equals("0", m.group(3))){
                		throw new IllegalArgumentException("failed files in line "+line);        				
        			}
        			if (!StringUtils.equals("0", m.group(4))){
                		throw new IllegalArgumentException("not processed files in line "+line);        				
        			}
        	} else if (StringUtils.indexOf(line, "Checkout has been executed successfully.")==0){
        		// Checkout has been executed successfully.
    			continue;
        	} else {
        		throw new IllegalArgumentException("could not parse line "+line);
        	}
        }
        return new HarvestChangeLogSet(build, history);
	}

	/* (non-Javadoc)
	 * @see hudson.scm.SCM#createChangeLogParser()
	 */
	@Override
	public ChangeLogParser createChangeLogParser() {
		return new HarvestChangeLogParser();
	}

	/* (non-Javadoc)
	 * @see hudson.scm.SCM#getDescriptor()
	 */
	@Override
	public DescriptorImpl getDescriptor() {
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

    /**
	 * @return the useSynchronize
	 */
	public boolean isUseSynchronize() {
		return useSynchronize;
	}

	/**
	 * @param useSynchronize the useSynchronize to set
	 */
	public void setUseSynchronize(boolean useSynchronize) {
		this.useSynchronize = useSynchronize;
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
