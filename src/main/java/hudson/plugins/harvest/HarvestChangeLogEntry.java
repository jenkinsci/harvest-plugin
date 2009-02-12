package hudson.plugins.harvest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.kohsuke.stapler.export.Exported;

/**
 * @author G&aacute;bor Lipt&aacute;k
 *
 */
public class HarvestChangeLogEntry extends Entry {

	/**
	 * @return the fullName
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @param fullName the fullName to set
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	protected String user=null;
    protected String msg="";
    protected String fullName = "";
    protected String version;

	@Override
	@Exported
    public User getAuthor() {
        return User.getUnknown();        	
    }

	@Override
	@Exported
    public Collection<String> getAffectedPaths() {
		List<String> l=new ArrayList<String>();
		l.add(fullName);
        return l;
    }
    
    /**
     * Overrides the setParent() method so the ClearCaseChangeLogSet can access it.
     */
	@Override
	@Exported
    public void setParent(ChangeLogSet parent) {
        super.setParent(parent);
    }

	@Override
	@Exported
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

    /**
	 * @return the version
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param version the version to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ReflectionToStringBuilder.toString(this);
	}
}
