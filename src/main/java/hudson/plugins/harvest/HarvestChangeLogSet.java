package hudson.plugins.harvest;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import org.apache.commons.digester3.Digester;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author G&aacute;bor Lipt&aacute;k
 */
public class HarvestChangeLogSet extends ChangeLogSet<HarvestChangeLogEntry> {

    private List<HarvestChangeLogEntry> history = null;

    public HarvestChangeLogSet(AbstractBuild<?, ?> build, List<HarvestChangeLogEntry> logs) {
        super(build);
        for (HarvestChangeLogEntry entry : logs) {
            entry.setParent(this);
        }
        this.history = Collections.unmodifiableList(logs);
    }

    public static ChangeLogSet<HarvestChangeLogEntry> parse(AbstractBuild<?, ?> build, File changeLogFile) throws IOException, SAXException {
        InputStream fileInputStream = new FileInputStream(changeLogFile);
        HarvestChangeLogSet logSet = parse(build, fileInputStream);
        fileInputStream.close();
        return logSet;
    }

    protected static HarvestChangeLogSet parse(AbstractBuild<?, ?> build, InputStream inputStream) throws IOException, SAXException {
        List<HarvestChangeLogEntry> history = new ArrayList<HarvestChangeLogEntry>();

        // Parse the change log file.
        Digester digester = new Digester();
        if (!Boolean.getBoolean(HarvestChangeLogSet.class.getName() + ".UNSAFE")) {
            digester.setXIncludeAware(false);
            try {
                digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                digester.setFeature("http://xml.org/sax/features/external-general-entities", false);
                digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                digester.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (ParserConfigurationException ex) {
                throw new SAXException("Failed to securely configure xml digester parser", ex);
            }
        }
        digester.setClassLoader(HarvestChangeLogSet.class.getClassLoader());
        digester.push(history);
        digester.addObjectCreate("*/entry", HarvestChangeLogEntry.class);

        digester.addBeanPropertySetter("*/entry/user");
        digester.addBeanPropertySetter("*/entry/msg");
        digester.addBeanPropertySetter("*/entry/fullName");
        digester.addBeanPropertySetter("*/entry/version");

        digester.addSetNext("*/entry", "add");
        digester.parse(inputStream);

        return new HarvestChangeLogSet(build, history);
    }

//    @Override
//    public String getKind() {
//        return "harvest";
//    }

    /**
     * Stores the history objects to the output stream as xml
     *
     * @param outputStream the stream to write to
     * @param history      the history objects to store
     * @throws IOException
     */
    public static void saveToChangeLog(OutputStream outputStream, ChangeLogSet<HarvestChangeLogEntry> history)
            throws IOException {
        PrintStream stream = new PrintStream(outputStream, false, "UTF-8");

        stream.println("<?xml version='1.0' encoding='UTF-8'?>");
        stream.println("<history>");
        for (HarvestChangeLogEntry entry : history) {
            stream.println("<entry>");
            stream.print("<user>");
            stream.print(entry.getAuthor());
            stream.println("</user>");
            stream.print("<msg>");
            stream.print(entry.getMsgEscaped());
            stream.println("</msg>");
            stream.print("<fullName>");
            stream.print(entry.getFullName());
            stream.println("</fullName>");
            stream.print("<version>");
            stream.print(entry.getVersion());
            stream.println("</version>");
            stream.println("</entry>");
        }
        stream.println("</history>");
        stream.close();
    }

    @Override
    public boolean isEmptySet() {
        return history.size() == 0;
    }

    public Iterator<HarvestChangeLogEntry> iterator() {
        return history.iterator();
    }

    public List<HarvestChangeLogEntry> getLogs() {
        return history;
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
