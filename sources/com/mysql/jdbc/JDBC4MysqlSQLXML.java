package com.mysql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLXML;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class JDBC4MysqlSQLXML implements SQLXML {
    private ByteArrayOutputStream asByteArrayOutputStream;
    private DOMResult asDOMResult;
    private SAXResult asSAXResult;
    private StringWriter asStringWriter;
    private int columnIndexOfXml;
    private ExceptionInterceptor exceptionInterceptor;
    private boolean fromResultSet;
    private XMLInputFactory inputFactory;
    private boolean isClosed;
    private XMLOutputFactory outputFactory;
    private ResultSetInternalMethods owningResultSet;
    private SimpleSaxToReader saxToReaderConverter;
    private String stringRep;
    private boolean workingWithResult;

    class SimpleSaxToReader extends DefaultHandler {
        StringBuilder buf = new StringBuilder();
        private boolean inCDATA = false;

        SimpleSaxToReader() {
        }

        public void startDocument() throws SAXException {
            this.buf.append("<?xml version='1.0' encoding='UTF-8'?>");
        }

        public void endDocument() throws SAXException {
        }

        public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
            this.buf.append("<");
            this.buf.append(qName);
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    this.buf.append(" ");
                    StringBuilder stringBuilder = this.buf;
                    stringBuilder.append(attrs.getQName(i));
                    stringBuilder.append("=\"");
                    escapeCharsForXml(attrs.getValue(i), true);
                    this.buf.append("\"");
                }
            }
            this.buf.append(">");
        }

        public void characters(char[] buf, int offset, int len) throws SAXException {
            if (this.inCDATA) {
                this.buf.append(buf, offset, len);
            } else {
                escapeCharsForXml(buf, offset, len, false);
            }
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            characters(ch, start, length);
        }

        public void startCDATA() throws SAXException {
            this.buf.append("<![CDATA[");
            this.inCDATA = true;
        }

        public void endCDATA() throws SAXException {
            this.inCDATA = false;
            this.buf.append("]]>");
        }

        public void comment(char[] ch, int start, int length) throws SAXException {
            this.buf.append("<!--");
            for (int i = 0; i < length; i++) {
                this.buf.append(ch[start + i]);
            }
            this.buf.append("-->");
        }

        Reader toReader() {
            return new StringReader(this.buf.toString());
        }

        private void escapeCharsForXml(String str, boolean isAttributeData) {
            if (str != null) {
                int strLen = str.length();
                for (int i = 0; i < strLen; i++) {
                    escapeCharsForXml(str.charAt(i), isAttributeData);
                }
            }
        }

        private void escapeCharsForXml(char[] buf, int offset, int len, boolean isAttributeData) {
            if (buf != null) {
                for (int i = 0; i < len; i++) {
                    escapeCharsForXml(buf[offset + i], isAttributeData);
                }
            }
        }

        private void escapeCharsForXml(char c, boolean isAttributeData) {
            if (c == '\r') {
                this.buf.append("&#xD;");
            } else if (c != '\"') {
                if (c == '&') {
                    this.buf.append("&amp;");
                } else if (c == '<') {
                    this.buf.append("&lt;");
                } else if (c != '>') {
                    if ((c < '\u0001' || c > '\u001f' || c == '\t' || c == '\n') && ((c < '' || c > '') && c != ' ')) {
                        if (isAttributeData) {
                            if (c != '\t') {
                                if (c == '\n') {
                                }
                            }
                        }
                        this.buf.append(c);
                        return;
                    }
                    this.buf.append("&#x");
                    this.buf.append(Integer.toHexString(c).toUpperCase());
                    this.buf.append(";");
                } else {
                    this.buf.append("&gt;");
                }
            } else if (isAttributeData) {
                this.buf.append("&quot;");
            } else {
                this.buf.append("\"");
            }
        }
    }

    protected JDBC4MysqlSQLXML(ResultSetInternalMethods owner, int index, ExceptionInterceptor exceptionInterceptor) {
        this.isClosed = false;
        this.owningResultSet = owner;
        this.columnIndexOfXml = index;
        this.fromResultSet = true;
        this.exceptionInterceptor = exceptionInterceptor;
    }

    protected JDBC4MysqlSQLXML(ExceptionInterceptor exceptionInterceptor) {
        this.isClosed = false;
        this.fromResultSet = false;
        this.exceptionInterceptor = exceptionInterceptor;
    }

    public synchronized void free() throws SQLException {
        this.stringRep = null;
        this.asDOMResult = null;
        this.asSAXResult = null;
        this.inputFactory = null;
        this.outputFactory = null;
        this.owningResultSet = null;
        this.workingWithResult = false;
        this.isClosed = true;
    }

    public synchronized String getString() throws SQLException {
        checkClosed();
        checkWorkingWithResult();
        if (this.fromResultSet) {
            return this.owningResultSet.getString(this.columnIndexOfXml);
        }
        return this.stringRep;
    }

    private synchronized void checkClosed() throws SQLException {
        if (this.isClosed) {
            throw SQLError.createSQLException("SQLXMLInstance has been free()d", this.exceptionInterceptor);
        }
    }

    private synchronized void checkWorkingWithResult() throws SQLException {
        if (this.workingWithResult) {
            throw SQLError.createSQLException("Can't perform requested operation after getResult() has been called to write XML data", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
        }
    }

    public synchronized void setString(String str) throws SQLException {
        checkClosed();
        checkWorkingWithResult();
        this.stringRep = str;
        this.fromResultSet = false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean isEmpty() throws java.sql.SQLException {
        /*
        r2 = this;
        monitor-enter(r2);
        r2.checkClosed();	 Catch:{ all -> 0x001f }
        r2.checkWorkingWithResult();	 Catch:{ all -> 0x001f }
        r0 = r2.fromResultSet;	 Catch:{ all -> 0x001f }
        r1 = 0;
        if (r0 != 0) goto L_0x001d;
    L_0x000c:
        r0 = r2.stringRep;	 Catch:{ all -> 0x001f }
        if (r0 == 0) goto L_0x001a;
    L_0x0010:
        r0 = r2.stringRep;	 Catch:{ all -> 0x001f }
        r0 = r0.length();	 Catch:{ all -> 0x001f }
        if (r0 != 0) goto L_0x0019;
    L_0x0018:
        goto L_0x001a;
    L_0x0019:
        goto L_0x001b;
    L_0x001a:
        r1 = 1;
    L_0x001b:
        monitor-exit(r2);
        return r1;
    L_0x001d:
        monitor-exit(r2);
        return r1;
    L_0x001f:
        r0 = move-exception;
        monitor-exit(r2);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.JDBC4MysqlSQLXML.isEmpty():boolean");
    }

    public synchronized InputStream getBinaryStream() throws SQLException {
        checkClosed();
        checkWorkingWithResult();
        return this.owningResultSet.getBinaryStream(this.columnIndexOfXml);
    }

    public synchronized Reader getCharacterStream() throws SQLException {
        checkClosed();
        checkWorkingWithResult();
        return this.owningResultSet.getCharacterStream(this.columnIndexOfXml);
    }

    public synchronized <T extends javax.xml.transform.Source> T getSource(java.lang.Class<T> r7) throws java.sql.SQLException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.mysql.jdbc.JDBC4MysqlSQLXML.getSource(java.lang.Class):T. bs: [B:9:0x001b, B:34:0x0093]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/1205406622.run(Unknown Source)
*/
        /*
        r6 = this;
        monitor-enter(r6);
        r6.checkClosed();	 Catch:{ all -> 0x0112 }
        r6.checkWorkingWithResult();	 Catch:{ all -> 0x0112 }
        if (r7 == 0) goto L_0x00ea;	 Catch:{ all -> 0x0112 }
    L_0x0009:
        r0 = javax.xml.transform.sax.SAXSource.class;	 Catch:{ all -> 0x0112 }
        r0 = r7.equals(r0);	 Catch:{ all -> 0x0112 }
        if (r0 == 0) goto L_0x0013;	 Catch:{ all -> 0x0112 }
    L_0x0011:
        goto L_0x00ea;	 Catch:{ all -> 0x0112 }
    L_0x0013:
        r0 = javax.xml.transform.dom.DOMSource.class;	 Catch:{ all -> 0x0112 }
        r0 = r7.equals(r0);	 Catch:{ all -> 0x0112 }
        if (r0 == 0) goto L_0x0064;
    L_0x001b:
        r0 = javax.xml.parsers.DocumentBuilderFactory.newInstance();	 Catch:{ Throwable -> 0x0053 }
        r1 = 1;	 Catch:{ Throwable -> 0x0053 }
        r0.setNamespaceAware(r1);	 Catch:{ Throwable -> 0x0053 }
        r1 = r0.newDocumentBuilder();	 Catch:{ Throwable -> 0x0053 }
        r2 = 0;	 Catch:{ Throwable -> 0x0053 }
        r3 = r6.fromResultSet;	 Catch:{ Throwable -> 0x0053 }
        if (r3 == 0) goto L_0x003b;	 Catch:{ Throwable -> 0x0053 }
    L_0x002c:
        r3 = new org.xml.sax.InputSource;	 Catch:{ Throwable -> 0x0053 }
        r4 = r6.owningResultSet;	 Catch:{ Throwable -> 0x0053 }
        r5 = r6.columnIndexOfXml;	 Catch:{ Throwable -> 0x0053 }
        r4 = r4.getCharacterStream(r5);	 Catch:{ Throwable -> 0x0053 }
        r3.<init>(r4);	 Catch:{ Throwable -> 0x0053 }
        r2 = r3;	 Catch:{ Throwable -> 0x0053 }
        goto L_0x0048;	 Catch:{ Throwable -> 0x0053 }
    L_0x003b:
        r3 = new org.xml.sax.InputSource;	 Catch:{ Throwable -> 0x0053 }
        r4 = new java.io.StringReader;	 Catch:{ Throwable -> 0x0053 }
        r5 = r6.stringRep;	 Catch:{ Throwable -> 0x0053 }
        r4.<init>(r5);	 Catch:{ Throwable -> 0x0053 }
        r3.<init>(r4);	 Catch:{ Throwable -> 0x0053 }
        r2 = r3;	 Catch:{ Throwable -> 0x0053 }
    L_0x0048:
        r3 = new javax.xml.transform.dom.DOMSource;	 Catch:{ Throwable -> 0x0053 }
        r4 = r1.parse(r2);	 Catch:{ Throwable -> 0x0053 }
        r3.<init>(r4);	 Catch:{ Throwable -> 0x0053 }
        monitor-exit(r6);
        return r3;
    L_0x0053:
        r0 = move-exception;
        r1 = r0.getMessage();	 Catch:{ all -> 0x0112 }
        r2 = "S1009";	 Catch:{ all -> 0x0112 }
        r3 = r6.exceptionInterceptor;	 Catch:{ all -> 0x0112 }
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r3);	 Catch:{ all -> 0x0112 }
        r1.initCause(r0);	 Catch:{ all -> 0x0112 }
        throw r1;	 Catch:{ all -> 0x0112 }
    L_0x0064:
        r0 = javax.xml.transform.stream.StreamSource.class;	 Catch:{ all -> 0x0112 }
        r0 = r7.equals(r0);	 Catch:{ all -> 0x0112 }
        if (r0 == 0) goto L_0x008a;	 Catch:{ all -> 0x0112 }
    L_0x006c:
        r0 = 0;	 Catch:{ all -> 0x0112 }
        r1 = r6.fromResultSet;	 Catch:{ all -> 0x0112 }
        if (r1 == 0) goto L_0x007b;	 Catch:{ all -> 0x0112 }
    L_0x0071:
        r1 = r6.owningResultSet;	 Catch:{ all -> 0x0112 }
        r2 = r6.columnIndexOfXml;	 Catch:{ all -> 0x0112 }
        r1 = r1.getCharacterStream(r2);	 Catch:{ all -> 0x0112 }
        r0 = r1;	 Catch:{ all -> 0x0112 }
        goto L_0x0083;	 Catch:{ all -> 0x0112 }
    L_0x007b:
        r1 = new java.io.StringReader;	 Catch:{ all -> 0x0112 }
        r2 = r6.stringRep;	 Catch:{ all -> 0x0112 }
        r1.<init>(r2);	 Catch:{ all -> 0x0112 }
        r0 = r1;	 Catch:{ all -> 0x0112 }
    L_0x0083:
        r1 = new javax.xml.transform.stream.StreamSource;	 Catch:{ all -> 0x0112 }
        r1.<init>(r0);	 Catch:{ all -> 0x0112 }
        monitor-exit(r6);
        return r1;
    L_0x008a:
        r0 = javax.xml.transform.stax.StAXSource.class;	 Catch:{ all -> 0x0112 }
        r0 = r7.equals(r0);	 Catch:{ all -> 0x0112 }
        if (r0 == 0) goto L_0x00c7;
    L_0x0092:
        r0 = 0;
        r1 = r6.fromResultSet;	 Catch:{ XMLStreamException -> 0x00b6 }
        if (r1 == 0) goto L_0x00a1;	 Catch:{ XMLStreamException -> 0x00b6 }
    L_0x0097:
        r1 = r6.owningResultSet;	 Catch:{ XMLStreamException -> 0x00b6 }
        r2 = r6.columnIndexOfXml;	 Catch:{ XMLStreamException -> 0x00b6 }
        r1 = r1.getCharacterStream(r2);	 Catch:{ XMLStreamException -> 0x00b6 }
        r0 = r1;	 Catch:{ XMLStreamException -> 0x00b6 }
        goto L_0x00a9;	 Catch:{ XMLStreamException -> 0x00b6 }
    L_0x00a1:
        r1 = new java.io.StringReader;	 Catch:{ XMLStreamException -> 0x00b6 }
        r2 = r6.stringRep;	 Catch:{ XMLStreamException -> 0x00b6 }
        r1.<init>(r2);	 Catch:{ XMLStreamException -> 0x00b6 }
        r0 = r1;	 Catch:{ XMLStreamException -> 0x00b6 }
    L_0x00a9:
        r1 = new javax.xml.transform.stax.StAXSource;	 Catch:{ XMLStreamException -> 0x00b6 }
        r2 = r6.inputFactory;	 Catch:{ XMLStreamException -> 0x00b6 }
        r2 = r2.createXMLStreamReader(r0);	 Catch:{ XMLStreamException -> 0x00b6 }
        r1.<init>(r2);	 Catch:{ XMLStreamException -> 0x00b6 }
        monitor-exit(r6);
        return r1;
    L_0x00b6:
        r0 = move-exception;
        r1 = r0.getMessage();	 Catch:{ all -> 0x0112 }
        r2 = "S1009";	 Catch:{ all -> 0x0112 }
        r3 = r6.exceptionInterceptor;	 Catch:{ all -> 0x0112 }
        r1 = com.mysql.jdbc.SQLError.createSQLException(r1, r2, r3);	 Catch:{ all -> 0x0112 }
        r1.initCause(r0);	 Catch:{ all -> 0x0112 }
        throw r1;	 Catch:{ all -> 0x0112 }
    L_0x00c7:
        r0 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0112 }
        r0.<init>();	 Catch:{ all -> 0x0112 }
        r1 = "XML Source of type \"";	 Catch:{ all -> 0x0112 }
        r0.append(r1);	 Catch:{ all -> 0x0112 }
        r1 = r7.toString();	 Catch:{ all -> 0x0112 }
        r0.append(r1);	 Catch:{ all -> 0x0112 }
        r1 = "\" Not supported.";	 Catch:{ all -> 0x0112 }
        r0.append(r1);	 Catch:{ all -> 0x0112 }
        r0 = r0.toString();	 Catch:{ all -> 0x0112 }
        r1 = "S1009";	 Catch:{ all -> 0x0112 }
        r2 = r6.exceptionInterceptor;	 Catch:{ all -> 0x0112 }
        r0 = com.mysql.jdbc.SQLError.createSQLException(r0, r1, r2);	 Catch:{ all -> 0x0112 }
        throw r0;	 Catch:{ all -> 0x0112 }
    L_0x00ea:
        r0 = 0;	 Catch:{ all -> 0x0112 }
        r1 = r6.fromResultSet;	 Catch:{ all -> 0x0112 }
        if (r1 == 0) goto L_0x00fe;	 Catch:{ all -> 0x0112 }
    L_0x00ef:
        r1 = new org.xml.sax.InputSource;	 Catch:{ all -> 0x0112 }
        r2 = r6.owningResultSet;	 Catch:{ all -> 0x0112 }
        r3 = r6.columnIndexOfXml;	 Catch:{ all -> 0x0112 }
        r2 = r2.getCharacterStream(r3);	 Catch:{ all -> 0x0112 }
        r1.<init>(r2);	 Catch:{ all -> 0x0112 }
        r0 = r1;	 Catch:{ all -> 0x0112 }
        goto L_0x010b;	 Catch:{ all -> 0x0112 }
    L_0x00fe:
        r1 = new org.xml.sax.InputSource;	 Catch:{ all -> 0x0112 }
        r2 = new java.io.StringReader;	 Catch:{ all -> 0x0112 }
        r3 = r6.stringRep;	 Catch:{ all -> 0x0112 }
        r2.<init>(r3);	 Catch:{ all -> 0x0112 }
        r1.<init>(r2);	 Catch:{ all -> 0x0112 }
        r0 = r1;	 Catch:{ all -> 0x0112 }
    L_0x010b:
        r1 = new javax.xml.transform.sax.SAXSource;	 Catch:{ all -> 0x0112 }
        r1.<init>(r0);	 Catch:{ all -> 0x0112 }
        monitor-exit(r6);
        return r1;
    L_0x0112:
        r7 = move-exception;
        monitor-exit(r6);
        throw r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.JDBC4MysqlSQLXML.getSource(java.lang.Class):T");
    }

    public synchronized OutputStream setBinaryStream() throws SQLException {
        checkClosed();
        checkWorkingWithResult();
        this.workingWithResult = true;
        return setBinaryStreamInternal();
    }

    private synchronized OutputStream setBinaryStreamInternal() throws SQLException {
        this.asByteArrayOutputStream = new ByteArrayOutputStream();
        return this.asByteArrayOutputStream;
    }

    public synchronized Writer setCharacterStream() throws SQLException {
        checkClosed();
        checkWorkingWithResult();
        this.workingWithResult = true;
        return setCharacterStreamInternal();
    }

    private synchronized Writer setCharacterStreamInternal() throws SQLException {
        this.asStringWriter = new StringWriter();
        return this.asStringWriter;
    }

    public synchronized <T extends Result> T setResult(Class<T> clazz) throws SQLException {
        checkClosed();
        checkWorkingWithResult();
        this.workingWithResult = true;
        this.asDOMResult = null;
        this.asSAXResult = null;
        this.saxToReaderConverter = null;
        this.stringRep = null;
        this.asStringWriter = null;
        this.asByteArrayOutputStream = null;
        if (clazz != null) {
            if (!clazz.equals(SAXResult.class)) {
                if (clazz.equals(DOMResult.class)) {
                    this.asDOMResult = new DOMResult();
                    return this.asDOMResult;
                } else if (clazz.equals(StreamResult.class)) {
                    return new StreamResult(setCharacterStreamInternal());
                } else if (clazz.equals(StAXResult.class)) {
                    try {
                        if (this.outputFactory == null) {
                            this.outputFactory = XMLOutputFactory.newInstance();
                        }
                        return new StAXResult(this.outputFactory.createXMLEventWriter(setCharacterStreamInternal()));
                    } catch (XMLStreamException ex) {
                        SQLException sqlEx = SQLError.createSQLException(ex.getMessage(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
                        sqlEx.initCause(ex);
                        throw sqlEx;
                    }
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("XML Result of type \"");
                    stringBuilder.append(clazz.toString());
                    stringBuilder.append("\" Not supported.");
                    throw SQLError.createSQLException(stringBuilder.toString(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
                }
            }
        }
        this.saxToReaderConverter = new SimpleSaxToReader();
        this.asSAXResult = new SAXResult(this.saxToReaderConverter);
        return this.asSAXResult;
    }

    private Reader binaryInputStreamStreamToReader(ByteArrayOutputStream out) {
        try {
            String encoding = "UTF-8";
            try {
                XMLStreamReader reader = this.inputFactory.createXMLStreamReader(new ByteArrayInputStream(out.toByteArray()));
                int eventType;
                do {
                    int next = reader.next();
                    eventType = next;
                    if (next == 8) {
                        break;
                    }
                } while (eventType != 7);
                String possibleEncoding = reader.getEncoding();
                if (possibleEncoding != null) {
                    encoding = possibleEncoding;
                }
            } catch (Throwable th) {
            }
            return new StringReader(new String(out.toByteArray(), encoding));
        } catch (UnsupportedEncodingException badEnc) {
            throw new RuntimeException(badEnc);
        }
    }

    protected String readerToString(Reader reader) throws SQLException {
        StringBuilder buf = new StringBuilder();
        char[] charBuf = new char[512];
        while (true) {
            try {
                int read = reader.read(charBuf);
                int charsRead = read;
                if (read == -1) {
                    return buf.toString();
                }
                buf.append(charBuf, 0, charsRead);
            } catch (IOException ioEx) {
                SQLException sqlEx = SQLError.createSQLException(ioEx.getMessage(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
                sqlEx.initCause(ioEx);
                throw sqlEx;
            }
        }
    }

    protected synchronized Reader serializeAsCharacterStream() throws SQLException {
        checkClosed();
        if (this.workingWithResult) {
            if (this.stringRep != null) {
                return new StringReader(this.stringRep);
            } else if (this.asDOMResult != null) {
                return new StringReader(domSourceToString());
            } else if (this.asStringWriter != null) {
                return new StringReader(this.asStringWriter.toString());
            } else if (this.asSAXResult != null) {
                return this.saxToReaderConverter.toReader();
            } else if (this.asByteArrayOutputStream != null) {
                return binaryInputStreamStreamToReader(this.asByteArrayOutputStream);
            }
        }
        return this.owningResultSet.getCharacterStream(this.columnIndexOfXml);
    }

    protected String domSourceToString() throws SQLException {
        try {
            DOMSource source = new DOMSource(this.asDOMResult.getNode());
            Transformer identity = TransformerFactory.newInstance().newTransformer();
            StringWriter stringOut = new StringWriter();
            identity.transform(source, new StreamResult(stringOut));
            return stringOut.toString();
        } catch (Throwable t) {
            SQLError.createSQLException(t.getMessage(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor).initCause(t);
        }
    }

    protected synchronized String serializeAsString() throws SQLException {
        checkClosed();
        if (this.workingWithResult) {
            if (this.stringRep != null) {
                return this.stringRep;
            } else if (this.asDOMResult != null) {
                return domSourceToString();
            } else if (this.asStringWriter != null) {
                return this.asStringWriter.toString();
            } else if (this.asSAXResult != null) {
                return readerToString(this.saxToReaderConverter.toReader());
            } else if (this.asByteArrayOutputStream != null) {
                return readerToString(binaryInputStreamStreamToReader(this.asByteArrayOutputStream));
            }
        }
        return this.owningResultSet.getString(this.columnIndexOfXml);
    }
}
