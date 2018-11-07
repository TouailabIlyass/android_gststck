package com.mysql.jdbc;

import com.mysql.jdbc.SocketMetadata.Helper;
import com.mysql.jdbc.util.Base64Decoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.crypto.Cipher;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class ExportControlled {
    private static final String SQL_STATE_BAD_SSL_PARAMS = "08000";
    private static final String[] TLS_PROTOCOLS = new String[]{TLSv1_2, TLSv1_1, TLSv1};
    private static final String TLSv1 = "TLSv1";
    private static final String TLSv1_1 = "TLSv1.1";
    private static final String TLSv1_2 = "TLSv1.2";

    public static class X509TrustManagerWrapper implements X509TrustManager {
        private CertificateFactory certFactory = null;
        private X509TrustManager origTm = null;
        private CertPathValidator validator = null;
        private PKIXParameters validatorParams = null;
        private boolean verifyServerCert = false;

        public X509TrustManagerWrapper(X509TrustManager tm, boolean verifyServerCertificate) throws CertificateException {
            this.origTm = tm;
            this.verifyServerCert = verifyServerCertificate;
            if (verifyServerCertificate) {
                try {
                    Set<TrustAnchor> anch = new HashSet();
                    for (X509Certificate cert : tm.getAcceptedIssuers()) {
                        anch.add(new TrustAnchor(cert, null));
                    }
                    this.validatorParams = new PKIXParameters(anch);
                    this.validatorParams.setRevocationEnabled(false);
                    this.validator = CertPathValidator.getInstance("PKIX");
                    this.certFactory = CertificateFactory.getInstance("X.509");
                } catch (Exception e) {
                    throw new CertificateException(e);
                }
            }
        }

        public X509Certificate[] getAcceptedIssuers() {
            return this.origTm != null ? this.origTm.getAcceptedIssuers() : new X509Certificate[0];
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            for (X509Certificate checkValidity : chain) {
                checkValidity.checkValidity();
            }
            if (this.validatorParams != null) {
                new X509CertSelector().setSerialNumber(chain[0].getSerialNumber());
                try {
                    ((PKIXCertPathValidatorResult) this.validator.validate(this.certFactory.generateCertPath(Arrays.asList(chain)), this.validatorParams)).getTrustAnchor().getTrustedCert().checkValidity();
                } catch (InvalidAlgorithmParameterException e) {
                    throw new CertificateException(e);
                } catch (CertPathValidatorException e2) {
                    throw new CertificateException(e2);
                }
            }
            if (this.verifyServerCert) {
                this.origTm.checkServerTrusted(chain, authType);
            }
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            this.origTm.checkClientTrusted(chain, authType);
        }
    }

    public static class StandardSSLSocketFactory implements SocketFactory, SocketMetadata {
        private final Socket existingSocket;
        private final SocketFactory existingSocketFactory;
        private SSLSocket rawSocket = null;
        private final SSLSocketFactory sslFact;

        public StandardSSLSocketFactory(SSLSocketFactory sslFact, SocketFactory existingSocketFactory, Socket existingSocket) {
            this.sslFact = sslFact;
            this.existingSocketFactory = existingSocketFactory;
            this.existingSocket = existingSocket;
        }

        public Socket afterHandshake() throws SocketException, IOException {
            this.existingSocketFactory.afterHandshake();
            return this.rawSocket;
        }

        public Socket beforeHandshake() throws SocketException, IOException {
            return this.rawSocket;
        }

        public Socket connect(String host, int portNumber, Properties props) throws SocketException, IOException {
            this.rawSocket = (SSLSocket) this.sslFact.createSocket(this.existingSocket, host, portNumber, true);
            return this.rawSocket;
        }

        public boolean isLocallyConnected(ConnectionImpl conn) throws SQLException {
            return Helper.isLocallyConnected(conn);
        }
    }

    protected static boolean enabled() {
        return true;
    }

    protected static void transformSocketToSSLSocket(MysqlIO mysqlIO) throws SQLException {
        MysqlIO mysqlIO2 = mysqlIO;
        SocketFactory sslFact = new StandardSSLSocketFactory(getSSLSocketFactoryDefaultOrConfigured(mysqlIO), mysqlIO2.socketFactory, mysqlIO2.mysqlConnection);
        try {
            int i$;
            String protocol;
            mysqlIO2.mysqlConnection = sslFact.connect(mysqlIO2.host, mysqlIO2.port, null);
            String[] tryProtocols = null;
            String enabledTLSProtocols = mysqlIO2.connection.getEnabledTLSProtocols();
            boolean overrideCiphers = true;
            if (enabledTLSProtocols != null && enabledTLSProtocols.length() > 0) {
                tryProtocols = enabledTLSProtocols.split("\\s*,\\s*");
            } else if (mysqlIO2.versionMeetsMinimum(5, 6, 0) && Util.isEnterpriseEdition(mysqlIO.getServerVersion())) {
                tryProtocols = new String[]{TLSv1_2, TLSv1_1, TLSv1};
            }
            if (tryProtocols == null) {
                tryProtocols = new String[]{TLSv1_1, TLSv1};
            }
            List<String> configuredProtocols = new ArrayList(Arrays.asList(tryProtocols));
            List<String> jvmSupportedProtocols = Arrays.asList(((SSLSocket) mysqlIO2.mysqlConnection).getSupportedProtocols());
            List<String> allowedProtocols = new ArrayList();
            for (String protocol2 : TLS_PROTOCOLS) {
                if (jvmSupportedProtocols.contains(protocol2) && configuredProtocols.contains(protocol2)) {
                    allowedProtocols.add(protocol2);
                }
            }
            ((SSLSocket) mysqlIO2.mysqlConnection).setEnabledProtocols((String[]) allowedProtocols.toArray(new String[0]));
            String enabledSSLCipherSuites = mysqlIO2.connection.getEnabledSSLCipherSuites();
            if (enabledSSLCipherSuites == null || enabledSSLCipherSuites.length() <= 0) {
                overrideCiphers = false;
            }
            List<String> allowedCiphers = null;
            String[] strArr;
            String str;
            if (overrideCiphers) {
                allowedCiphers = new ArrayList();
                List<String> availableCiphers = Arrays.asList(((SSLSocket) mysqlIO2.mysqlConnection).getEnabledCipherSuites());
                for (String cipher : enabledSSLCipherSuites.split("\\s*,\\s*")) {
                    if (availableCiphers.contains(cipher)) {
                        allowedCiphers.add(cipher);
                    }
                }
                strArr = tryProtocols;
                str = enabledTLSProtocols;
            } else {
                boolean disableDHAlgorithm = false;
                if ((!mysqlIO2.versionMeetsMinimum(5, 5, 45) || mysqlIO2.versionMeetsMinimum(5, 6, 0)) && ((!mysqlIO2.versionMeetsMinimum(5, 6, 26) || mysqlIO2.versionMeetsMinimum(5, 7, 0)) && !mysqlIO2.versionMeetsMinimum(5, 7, 6))) {
                    if (Util.getJVMVersion() >= 8) {
                        disableDHAlgorithm = true;
                    }
                } else if (Util.getJVMVersion() < 8) {
                    disableDHAlgorithm = true;
                }
                if (disableDHAlgorithm) {
                    allowedCiphers = new ArrayList();
                    String[] arr$ = ((SSLSocket) mysqlIO2.mysqlConnection).getEnabledCipherSuites();
                    int len$ = arr$.length;
                    i$ = 0;
                    while (i$ < len$) {
                        protocol2 = arr$[i$];
                        if (disableDHAlgorithm) {
                            strArr = tryProtocols;
                            str = enabledTLSProtocols;
                            if (protocol2.indexOf("_DHE_") <= -1 && protocol2.indexOf("_DH_") <= -1) {
                            }
                            i$++;
                            tryProtocols = strArr;
                            enabledTLSProtocols = str;
                        } else {
                            strArr = tryProtocols;
                            str = enabledTLSProtocols;
                        }
                        allowedCiphers.add(protocol2);
                        i$++;
                        tryProtocols = strArr;
                        enabledTLSProtocols = str;
                    }
                }
                str = enabledTLSProtocols;
            }
            if (allowedCiphers != null) {
                ((SSLSocket) mysqlIO2.mysqlConnection).setEnabledCipherSuites((String[]) allowedCiphers.toArray(new String[0]));
            }
            ((SSLSocket) mysqlIO2.mysqlConnection).startHandshake();
            if (mysqlIO2.connection.getUseUnbufferedInput()) {
                mysqlIO2.mysqlInput = mysqlIO2.mysqlConnection.getInputStream();
            } else {
                mysqlIO2.mysqlInput = new BufferedInputStream(mysqlIO2.mysqlConnection.getInputStream(), 16384);
            }
            mysqlIO2.mysqlOutput = new BufferedOutputStream(mysqlIO2.mysqlConnection.getOutputStream(), 16384);
            mysqlIO2.mysqlOutput.flush();
            mysqlIO2.socketFactory = sslFact;
        } catch (IOException e) {
            throw SQLError.createCommunicationsException(mysqlIO2.connection, mysqlIO.getLastPacketSentTimeMs(), mysqlIO.getLastPacketReceivedTimeMs(), e, mysqlIO.getExceptionInterceptor());
        }
    }

    private ExportControlled() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static javax.net.ssl.SSLSocketFactory getSSLSocketFactoryDefaultOrConfigured(com.mysql.jdbc.MysqlIO r41) throws java.sql.SQLException {
        /*
        r1 = r41;
        r2 = r1.connection;
        r2 = r2.getClientCertificateKeyStoreUrl();
        r3 = r1.connection;
        r3 = r3.getClientCertificateKeyStorePassword();
        r4 = r1.connection;
        r4 = r4.getClientCertificateKeyStoreType();
        r5 = r1.connection;
        r5 = r5.getTrustCertificateKeyStoreUrl();
        r6 = r1.connection;
        r6 = r6.getTrustCertificateKeyStorePassword();
        r7 = r1.connection;
        r7 = r7.getTrustCertificateKeyStoreType();
        r8 = com.mysql.jdbc.StringUtils.isNullOrEmpty(r2);
        if (r8 == 0) goto L_0x0065;
    L_0x002c:
        r8 = "javax.net.ssl.keyStore";
        r2 = java.lang.System.getProperty(r8);
        r8 = "javax.net.ssl.keyStorePassword";
        r3 = java.lang.System.getProperty(r8);
        r8 = "javax.net.ssl.keyStoreType";
        r4 = java.lang.System.getProperty(r8);
        r8 = com.mysql.jdbc.StringUtils.isNullOrEmpty(r4);
        if (r8 == 0) goto L_0x0046;
    L_0x0044:
        r4 = "JKS";
    L_0x0046:
        r8 = com.mysql.jdbc.StringUtils.isNullOrEmpty(r2);
        if (r8 != 0) goto L_0x0065;
    L_0x004c:
        r8 = new java.net.URL;	 Catch:{ MalformedURLException -> 0x0052 }
        r8.<init>(r2);	 Catch:{ MalformedURLException -> 0x0052 }
        goto L_0x0065;
    L_0x0052:
        r0 = move-exception;
        r8 = r0;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "file:";
        r9.append(r10);
        r9.append(r2);
        r2 = r9.toString();
    L_0x0065:
        r8 = com.mysql.jdbc.StringUtils.isNullOrEmpty(r5);
        if (r8 == 0) goto L_0x00a4;
    L_0x006b:
        r8 = "javax.net.ssl.trustStore";
        r5 = java.lang.System.getProperty(r8);
        r8 = "javax.net.ssl.trustStorePassword";
        r6 = java.lang.System.getProperty(r8);
        r8 = "javax.net.ssl.trustStoreType";
        r7 = java.lang.System.getProperty(r8);
        r8 = com.mysql.jdbc.StringUtils.isNullOrEmpty(r7);
        if (r8 == 0) goto L_0x0085;
    L_0x0083:
        r7 = "JKS";
    L_0x0085:
        r8 = com.mysql.jdbc.StringUtils.isNullOrEmpty(r5);
        if (r8 != 0) goto L_0x00a4;
    L_0x008b:
        r8 = new java.net.URL;	 Catch:{ MalformedURLException -> 0x0091 }
        r8.<init>(r5);	 Catch:{ MalformedURLException -> 0x0091 }
        goto L_0x00a4;
    L_0x0091:
        r0 = move-exception;
        r8 = r0;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "file:";
        r9.append(r10);
        r9.append(r5);
        r5 = r9.toString();
    L_0x00a4:
        r8 = 0;
        r9 = 0;
        r10 = 0;
        r11 = new java.util.ArrayList;
        r11.<init>();
        r12 = 0;
        r13 = javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm();	 Catch:{ NoSuchAlgorithmException -> 0x0545 }
        r13 = javax.net.ssl.TrustManagerFactory.getInstance(r13);	 Catch:{ NoSuchAlgorithmException -> 0x0545 }
        r8 = r13;
        r13 = javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm();	 Catch:{ NoSuchAlgorithmException -> 0x0545 }
        r13 = javax.net.ssl.KeyManagerFactory.getInstance(r13);	 Catch:{ NoSuchAlgorithmException -> 0x0545 }
        r9 = r13;
        r13 = com.mysql.jdbc.StringUtils.isNullOrEmpty(r2);
        r14 = 0;
        if (r13 != 0) goto L_0x02af;
    L_0x00c7:
        r13 = r14;
        r15 = com.mysql.jdbc.StringUtils.isNullOrEmpty(r4);	 Catch:{ UnrecoverableKeyException -> 0x028b, NoSuchAlgorithmException -> 0x025d, KeyStoreException -> 0x022f, CertificateException -> 0x0207, MalformedURLException -> 0x01e2, IOException -> 0x0193, all -> 0x018b }
        if (r15 != 0) goto L_0x017d;
    L_0x00ce:
        r15 = java.security.KeyStore.getInstance(r4);	 Catch:{ UnrecoverableKeyException -> 0x0175, NoSuchAlgorithmException -> 0x016d, KeyStoreException -> 0x0165, CertificateException -> 0x015d, MalformedURLException -> 0x0155, IOException -> 0x0193, all -> 0x018b }
        r14 = new java.net.URL;	 Catch:{ UnrecoverableKeyException -> 0x0175, NoSuchAlgorithmException -> 0x016d, KeyStoreException -> 0x0165, CertificateException -> 0x015d, MalformedURLException -> 0x0155, IOException -> 0x0193, all -> 0x018b }
        r14.<init>(r2);	 Catch:{ UnrecoverableKeyException -> 0x0175, NoSuchAlgorithmException -> 0x016d, KeyStoreException -> 0x0165, CertificateException -> 0x015d, MalformedURLException -> 0x0155, IOException -> 0x0193, all -> 0x018b }
        if (r3 != 0) goto L_0x0113;
    L_0x00d9:
        r16 = r13;
        r13 = new char[r12];	 Catch:{ UnrecoverableKeyException -> 0x010b, NoSuchAlgorithmException -> 0x0103, KeyStoreException -> 0x00fb, CertificateException -> 0x00f3, MalformedURLException -> 0x00eb, IOException -> 0x00e5, all -> 0x00de }
        goto L_0x0119;
    L_0x00de:
        r0 = move-exception;
        r20 = r11;
        r13 = r16;
        goto L_0x02a2;
    L_0x00e5:
        r0 = move-exception;
        r12 = r0;
        r13 = r16;
        goto L_0x0197;
    L_0x00eb:
        r0 = move-exception;
        r20 = r11;
        r13 = r16;
    L_0x00f0:
        r11 = r0;
        goto L_0x01e8;
    L_0x00f3:
        r0 = move-exception;
        r20 = r11;
        r13 = r16;
    L_0x00f8:
        r11 = r0;
        goto L_0x020d;
    L_0x00fb:
        r0 = move-exception;
        r20 = r11;
        r13 = r16;
    L_0x0100:
        r11 = r0;
        goto L_0x0235;
    L_0x0103:
        r0 = move-exception;
        r20 = r11;
        r13 = r16;
    L_0x0108:
        r11 = r0;
        goto L_0x0263;
    L_0x010b:
        r0 = move-exception;
        r20 = r11;
        r13 = r16;
    L_0x0110:
        r11 = r0;
        goto L_0x0291;
    L_0x0113:
        r16 = r13;
        r13 = r3.toCharArray();	 Catch:{ UnrecoverableKeyException -> 0x010b, NoSuchAlgorithmException -> 0x0103, KeyStoreException -> 0x00fb, CertificateException -> 0x00f3, MalformedURLException -> 0x00eb, IOException -> 0x00e5, all -> 0x00de }
    L_0x0119:
        r17 = r14.openStream();	 Catch:{ UnrecoverableKeyException -> 0x010b, NoSuchAlgorithmException -> 0x0103, KeyStoreException -> 0x00fb, CertificateException -> 0x00f3, MalformedURLException -> 0x00eb, IOException -> 0x00e5, all -> 0x00de }
        r18 = r17;
        r12 = r18;
        r15.load(r12, r13);	 Catch:{ UnrecoverableKeyException -> 0x0150, NoSuchAlgorithmException -> 0x014b, KeyStoreException -> 0x0146, CertificateException -> 0x0141, MalformedURLException -> 0x013c, IOException -> 0x0137, all -> 0x0131 }
        r9.init(r15, r13);	 Catch:{ UnrecoverableKeyException -> 0x0150, NoSuchAlgorithmException -> 0x014b, KeyStoreException -> 0x0146, CertificateException -> 0x0141, MalformedURLException -> 0x013c, IOException -> 0x0137, all -> 0x0131 }
        r16 = r9.getKeyManagers();	 Catch:{ UnrecoverableKeyException -> 0x0150, NoSuchAlgorithmException -> 0x014b, KeyStoreException -> 0x0146, CertificateException -> 0x0141, MalformedURLException -> 0x013c, IOException -> 0x0137, all -> 0x0131 }
        r10 = r16;
        r16 = r12;
        goto L_0x017f;
    L_0x0131:
        r0 = move-exception;
        r20 = r11;
        r13 = r12;
        goto L_0x02a2;
    L_0x0137:
        r0 = move-exception;
        r13 = r12;
        r12 = r0;
        goto L_0x0197;
    L_0x013c:
        r0 = move-exception;
        r20 = r11;
        r13 = r12;
        goto L_0x00f0;
    L_0x0141:
        r0 = move-exception;
        r20 = r11;
        r13 = r12;
        goto L_0x00f8;
    L_0x0146:
        r0 = move-exception;
        r20 = r11;
        r13 = r12;
        goto L_0x0100;
    L_0x014b:
        r0 = move-exception;
        r20 = r11;
        r13 = r12;
        goto L_0x0108;
    L_0x0150:
        r0 = move-exception;
        r20 = r11;
        r13 = r12;
        goto L_0x0110;
    L_0x0155:
        r0 = move-exception;
        r16 = r13;
        r20 = r11;
        r11 = r0;
        goto L_0x01e8;
    L_0x015d:
        r0 = move-exception;
        r16 = r13;
        r20 = r11;
        r11 = r0;
        goto L_0x020d;
    L_0x0165:
        r0 = move-exception;
        r16 = r13;
        r20 = r11;
        r11 = r0;
        goto L_0x0235;
    L_0x016d:
        r0 = move-exception;
        r16 = r13;
        r20 = r11;
        r11 = r0;
        goto L_0x0263;
    L_0x0175:
        r0 = move-exception;
        r16 = r13;
        r20 = r11;
        r11 = r0;
        goto L_0x0291;
    L_0x017d:
        r16 = r13;
    L_0x017f:
        r12 = r16;
        if (r12 == 0) goto L_0x0188;
    L_0x0183:
        r12.close();	 Catch:{ IOException -> 0x0187 }
        goto L_0x0188;
    L_0x0187:
        r0 = move-exception;
        goto L_0x02b1;
    L_0x018b:
        r0 = move-exception;
        r16 = r13;
        r20 = r11;
        r11 = r0;
        goto L_0x02a3;
    L_0x0193:
        r0 = move-exception;
        r16 = r13;
        r12 = r0;
    L_0x0197:
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01da }
        r14.<init>();	 Catch:{ all -> 0x01da }
        r15 = "Cannot open ";
        r14.append(r15);	 Catch:{ all -> 0x01da }
        r14.append(r2);	 Catch:{ all -> 0x01da }
        r15 = " [";
        r14.append(r15);	 Catch:{ all -> 0x01da }
        r15 = r12.getMessage();	 Catch:{ all -> 0x01da }
        r14.append(r15);	 Catch:{ all -> 0x01da }
        r15 = "]";
        r14.append(r15);	 Catch:{ all -> 0x01da }
        r14 = r14.toString();	 Catch:{ all -> 0x01da }
        r15 = "08000";
        r19 = r13;
        r13 = r41.getExceptionInterceptor();	 Catch:{ all -> 0x01d2 }
        r20 = r11;
        r11 = 0;
        r11 = com.mysql.jdbc.SQLError.createSQLException(r14, r15, r11, r11, r13);	 Catch:{ all -> 0x01cc }
        r11.initCause(r12);	 Catch:{ all -> 0x01cc }
        throw r11;	 Catch:{ all -> 0x01cc }
    L_0x01cc:
        r0 = move-exception;
        r11 = r0;
        r13 = r19;
        goto L_0x02a3;
    L_0x01d2:
        r0 = move-exception;
        r20 = r11;
        r11 = r0;
        r13 = r19;
        goto L_0x02a3;
    L_0x01da:
        r0 = move-exception;
        r20 = r11;
        r19 = r13;
        r11 = r0;
        goto L_0x02a3;
    L_0x01e2:
        r0 = move-exception;
        r20 = r11;
        r16 = r13;
        r11 = r0;
    L_0x01e8:
        r12 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02a1 }
        r12.<init>();	 Catch:{ all -> 0x02a1 }
        r12.append(r2);	 Catch:{ all -> 0x02a1 }
        r14 = " does not appear to be a valid URL.";
        r12.append(r14);	 Catch:{ all -> 0x02a1 }
        r12 = r12.toString();	 Catch:{ all -> 0x02a1 }
        r14 = "08000";
        r15 = r41.getExceptionInterceptor();	 Catch:{ all -> 0x02a1 }
        r21 = r11;
        r11 = 0;
        r11 = com.mysql.jdbc.SQLError.createSQLException(r12, r14, r11, r11, r15);	 Catch:{ all -> 0x02a1 }
        throw r11;	 Catch:{ all -> 0x02a1 }
    L_0x0207:
        r0 = move-exception;
        r20 = r11;
        r16 = r13;
        r11 = r0;
    L_0x020d:
        r12 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02a1 }
        r12.<init>();	 Catch:{ all -> 0x02a1 }
        r14 = "Could not load client";
        r12.append(r14);	 Catch:{ all -> 0x02a1 }
        r12.append(r4);	 Catch:{ all -> 0x02a1 }
        r14 = " keystore from ";
        r12.append(r14);	 Catch:{ all -> 0x02a1 }
        r12.append(r2);	 Catch:{ all -> 0x02a1 }
        r12 = r12.toString();	 Catch:{ all -> 0x02a1 }
        r14 = r41.getExceptionInterceptor();	 Catch:{ all -> 0x02a1 }
        r12 = com.mysql.jdbc.SQLError.createSQLException(r12, r14);	 Catch:{ all -> 0x02a1 }
        throw r12;	 Catch:{ all -> 0x02a1 }
    L_0x022f:
        r0 = move-exception;
        r20 = r11;
        r16 = r13;
        r11 = r0;
    L_0x0235:
        r12 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02a1 }
        r12.<init>();	 Catch:{ all -> 0x02a1 }
        r14 = "Could not create KeyStore instance [";
        r12.append(r14);	 Catch:{ all -> 0x02a1 }
        r14 = r11.getMessage();	 Catch:{ all -> 0x02a1 }
        r12.append(r14);	 Catch:{ all -> 0x02a1 }
        r14 = "]";
        r12.append(r14);	 Catch:{ all -> 0x02a1 }
        r12 = r12.toString();	 Catch:{ all -> 0x02a1 }
        r14 = "08000";
        r15 = r41.getExceptionInterceptor();	 Catch:{ all -> 0x02a1 }
        r22 = r11;
        r11 = 0;
        r11 = com.mysql.jdbc.SQLError.createSQLException(r12, r14, r11, r11, r15);	 Catch:{ all -> 0x02a1 }
        throw r11;	 Catch:{ all -> 0x02a1 }
    L_0x025d:
        r0 = move-exception;
        r20 = r11;
        r16 = r13;
        r11 = r0;
    L_0x0263:
        r12 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02a1 }
        r12.<init>();	 Catch:{ all -> 0x02a1 }
        r14 = "Unsupported keystore algorithm [";
        r12.append(r14);	 Catch:{ all -> 0x02a1 }
        r14 = r11.getMessage();	 Catch:{ all -> 0x02a1 }
        r12.append(r14);	 Catch:{ all -> 0x02a1 }
        r14 = "]";
        r12.append(r14);	 Catch:{ all -> 0x02a1 }
        r12 = r12.toString();	 Catch:{ all -> 0x02a1 }
        r14 = "08000";
        r15 = r41.getExceptionInterceptor();	 Catch:{ all -> 0x02a1 }
        r23 = r11;
        r11 = 0;
        r11 = com.mysql.jdbc.SQLError.createSQLException(r12, r14, r11, r11, r15);	 Catch:{ all -> 0x02a1 }
        throw r11;	 Catch:{ all -> 0x02a1 }
    L_0x028b:
        r0 = move-exception;
        r20 = r11;
        r16 = r13;
        r11 = r0;
    L_0x0291:
        r12 = "Could not recover keys from client keystore.  Check password?";
        r14 = "08000";
        r15 = r41.getExceptionInterceptor();	 Catch:{ all -> 0x02a1 }
        r24 = r11;
        r11 = 0;
        r11 = com.mysql.jdbc.SQLError.createSQLException(r12, r14, r11, r11, r15);	 Catch:{ all -> 0x02a1 }
        throw r11;	 Catch:{ all -> 0x02a1 }
    L_0x02a1:
        r0 = move-exception;
    L_0x02a2:
        r11 = r0;
    L_0x02a3:
        r12 = r13;
        r13 = r20;
        if (r12 == 0) goto L_0x02ad;
    L_0x02a8:
        r12.close();	 Catch:{ IOException -> 0x02ac }
        goto L_0x02ad;
    L_0x02ac:
        r0 = move-exception;
        throw r11;
    L_0x02af:
        r20 = r11;
    L_0x02b1:
        r12 = 0;
        r13 = r12;
        r12 = 0;
        r14 = com.mysql.jdbc.StringUtils.isNullOrEmpty(r5);	 Catch:{ MalformedURLException -> 0x0515, KeyStoreException -> 0x04e9, NoSuchAlgorithmException -> 0x04bd, CertificateException -> 0x0492, IOException -> 0x044f, all -> 0x0449 }
        if (r14 != 0) goto L_0x0301;
    L_0x02ba:
        r14 = com.mysql.jdbc.StringUtils.isNullOrEmpty(r7);	 Catch:{ MalformedURLException -> 0x02fb, KeyStoreException -> 0x02f5, NoSuchAlgorithmException -> 0x02ef, CertificateException -> 0x02e9, IOException -> 0x02e3, all -> 0x02de }
        if (r14 != 0) goto L_0x0301;
    L_0x02c0:
        r14 = new java.net.URL;	 Catch:{ MalformedURLException -> 0x02fb, KeyStoreException -> 0x02f5, NoSuchAlgorithmException -> 0x02ef, CertificateException -> 0x02e9, IOException -> 0x02e3, all -> 0x02de }
        r14.<init>(r5);	 Catch:{ MalformedURLException -> 0x02fb, KeyStoreException -> 0x02f5, NoSuchAlgorithmException -> 0x02ef, CertificateException -> 0x02e9, IOException -> 0x02e3, all -> 0x02de }
        r14 = r14.openStream();	 Catch:{ MalformedURLException -> 0x02fb, KeyStoreException -> 0x02f5, NoSuchAlgorithmException -> 0x02ef, CertificateException -> 0x02e9, IOException -> 0x02e3, all -> 0x02de }
        r13 = r14;
        if (r6 != 0) goto L_0x02d0;
    L_0x02cc:
        r14 = 0;
        r15 = new char[r14];	 Catch:{ MalformedURLException -> 0x02fb, KeyStoreException -> 0x02f5, NoSuchAlgorithmException -> 0x02ef, CertificateException -> 0x02e9, IOException -> 0x02e3, all -> 0x02de }
        goto L_0x02d4;
    L_0x02d0:
        r15 = r6.toCharArray();	 Catch:{ MalformedURLException -> 0x02fb, KeyStoreException -> 0x02f5, NoSuchAlgorithmException -> 0x02ef, CertificateException -> 0x02e9, IOException -> 0x02e3, all -> 0x02de }
    L_0x02d4:
        r14 = r15;
        r15 = java.security.KeyStore.getInstance(r7);	 Catch:{ MalformedURLException -> 0x02fb, KeyStoreException -> 0x02f5, NoSuchAlgorithmException -> 0x02ef, CertificateException -> 0x02e9, IOException -> 0x02e3, all -> 0x02de }
        r12 = r15;
        r12.load(r13, r14);	 Catch:{ MalformedURLException -> 0x02fb, KeyStoreException -> 0x02f5, NoSuchAlgorithmException -> 0x02ef, CertificateException -> 0x02e9, IOException -> 0x02e3, all -> 0x02de }
        goto L_0x0301;
    L_0x02de:
        r0 = move-exception;
        r33 = r10;
        goto L_0x0539;
    L_0x02e3:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
        goto L_0x0453;
    L_0x02e9:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
        goto L_0x0496;
    L_0x02ef:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
        goto L_0x04c1;
    L_0x02f5:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
        goto L_0x04ed;
    L_0x02fb:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
        goto L_0x0519;
    L_0x0301:
        r8.init(r12);	 Catch:{ MalformedURLException -> 0x0441, KeyStoreException -> 0x0439, NoSuchAlgorithmException -> 0x0431, CertificateException -> 0x0429, IOException -> 0x0422, all -> 0x041a }
        r14 = r8.getTrustManagers();	 Catch:{ MalformedURLException -> 0x0441, KeyStoreException -> 0x0439, NoSuchAlgorithmException -> 0x0431, CertificateException -> 0x0429, IOException -> 0x0422, all -> 0x041a }
        r15 = r1.connection;	 Catch:{ MalformedURLException -> 0x0441, KeyStoreException -> 0x0439, NoSuchAlgorithmException -> 0x0431, CertificateException -> 0x0429, IOException -> 0x0422, all -> 0x041a }
        r15 = r15.getVerifyServerCertificate();	 Catch:{ MalformedURLException -> 0x0441, KeyStoreException -> 0x0439, NoSuchAlgorithmException -> 0x0431, CertificateException -> 0x0429, IOException -> 0x0422, all -> 0x041a }
        r25 = r14;
        r26 = r12;
        r27 = r14;
        r12 = r25;
        r14 = r12.length;	 Catch:{ MalformedURLException -> 0x0441, KeyStoreException -> 0x0439, NoSuchAlgorithmException -> 0x0431, CertificateException -> 0x0429, IOException -> 0x0422, all -> 0x041a }
        r16 = 0;
    L_0x0319:
        r28 = r16;
        r29 = r13;
        r13 = r28;
        if (r13 >= r14) goto L_0x039f;
    L_0x0321:
        r16 = r12[r13];	 Catch:{ MalformedURLException -> 0x0397, KeyStoreException -> 0x038f, NoSuchAlgorithmException -> 0x0387, CertificateException -> 0x037f, IOException -> 0x0377, all -> 0x036f }
        r30 = r16;
        r31 = r12;
        r32 = r14;
        r12 = r30;
        r14 = r12 instanceof javax.net.ssl.X509TrustManager;	 Catch:{ MalformedURLException -> 0x0397, KeyStoreException -> 0x038f, NoSuchAlgorithmException -> 0x0387, CertificateException -> 0x037f, IOException -> 0x0377, all -> 0x036f }
        if (r14 == 0) goto L_0x035e;
    L_0x032f:
        r14 = new com.mysql.jdbc.ExportControlled$X509TrustManagerWrapper;	 Catch:{ MalformedURLException -> 0x0397, KeyStoreException -> 0x038f, NoSuchAlgorithmException -> 0x0387, CertificateException -> 0x037f, IOException -> 0x0377, all -> 0x036f }
        r33 = r10;
        r10 = r12;
        r10 = (javax.net.ssl.X509TrustManager) r10;	 Catch:{ MalformedURLException -> 0x0358, KeyStoreException -> 0x0352, NoSuchAlgorithmException -> 0x034c, CertificateException -> 0x0346, IOException -> 0x0340, all -> 0x033a }
        r14.<init>(r10, r15);	 Catch:{ MalformedURLException -> 0x0358, KeyStoreException -> 0x0352, NoSuchAlgorithmException -> 0x034c, CertificateException -> 0x0346, IOException -> 0x0340, all -> 0x033a }
        goto L_0x0361;
    L_0x033a:
        r0 = move-exception;
        r10 = r0;
        r13 = r29;
        goto L_0x053a;
    L_0x0340:
        r0 = move-exception;
        r10 = r0;
        r13 = r29;
        goto L_0x0453;
    L_0x0346:
        r0 = move-exception;
        r10 = r0;
        r13 = r29;
        goto L_0x0496;
    L_0x034c:
        r0 = move-exception;
        r10 = r0;
        r13 = r29;
        goto L_0x04c1;
    L_0x0352:
        r0 = move-exception;
        r10 = r0;
        r13 = r29;
        goto L_0x04ed;
    L_0x0358:
        r0 = move-exception;
        r10 = r0;
        r13 = r29;
        goto L_0x0519;
    L_0x035e:
        r33 = r10;
        r14 = r12;
    L_0x0361:
        r11.add(r14);	 Catch:{ MalformedURLException -> 0x0358, KeyStoreException -> 0x0352, NoSuchAlgorithmException -> 0x034c, CertificateException -> 0x0346, IOException -> 0x0340, all -> 0x033a }
        r16 = r13 + 1;
        r13 = r29;
        r12 = r31;
        r14 = r32;
        r10 = r33;
        goto L_0x0319;
    L_0x036f:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
        r13 = r29;
        goto L_0x044d;
    L_0x0377:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
        r13 = r29;
        goto L_0x0453;
    L_0x037f:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
        r13 = r29;
        goto L_0x0496;
    L_0x0387:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
        r13 = r29;
        goto L_0x04c1;
    L_0x038f:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
        r13 = r29;
        goto L_0x04ed;
    L_0x0397:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
        r13 = r29;
        goto L_0x0519;
    L_0x039f:
        r33 = r10;
        r10 = r33;
        r12 = r29;
        if (r12 == 0) goto L_0x03ac;
    L_0x03a7:
        r12.close();	 Catch:{ IOException -> 0x03ab }
        goto L_0x03ac;
    L_0x03ab:
        r0 = move-exception;
        r13 = r11.size();
        if (r13 != 0) goto L_0x03bc;
    L_0x03b4:
        r13 = new com.mysql.jdbc.ExportControlled$X509TrustManagerWrapper;
        r13.<init>();
        r11.add(r13);
    L_0x03bc:
        r13 = "TLS";
        r13 = javax.net.ssl.SSLContext.getInstance(r13);	 Catch:{ NoSuchAlgorithmException -> 0x03d7, KeyManagementException -> 0x03df }
        r14 = r11.size();	 Catch:{ NoSuchAlgorithmException -> 0x03d7, KeyManagementException -> 0x03df }
        r14 = new javax.net.ssl.TrustManager[r14];	 Catch:{ NoSuchAlgorithmException -> 0x03d7, KeyManagementException -> 0x03df }
        r14 = r11.toArray(r14);	 Catch:{ NoSuchAlgorithmException -> 0x03d7, KeyManagementException -> 0x03df }
        r14 = (javax.net.ssl.TrustManager[]) r14;	 Catch:{ NoSuchAlgorithmException -> 0x03d7, KeyManagementException -> 0x03df }
        r15 = 0;
        r13.init(r10, r14, r15);	 Catch:{ NoSuchAlgorithmException -> 0x03d7, KeyManagementException -> 0x03df }
        r14 = r13.getSocketFactory();	 Catch:{ NoSuchAlgorithmException -> 0x03d7, KeyManagementException -> 0x03df }
        return r14;
    L_0x03d7:
        r0 = move-exception;
        r34 = r2;
        r35 = r3;
        r3 = 0;
        r2 = r0;
        goto L_0x040d;
    L_0x03df:
        r0 = move-exception;
        r13 = r0;
        r14 = new java.lang.StringBuilder;
        r14.<init>();
        r15 = "KeyManagementException: ";
        r14.append(r15);
        r15 = r13.getMessage();
        r14.append(r15);
        r14 = r14.toString();
        r15 = "08000";
        r34 = r2;
        r2 = r1.getExceptionInterceptor();
        r35 = r3;
        r3 = 0;
        r2 = com.mysql.jdbc.SQLError.createSQLException(r14, r15, r3, r3, r2);
        throw r2;
    L_0x0406:
        r0 = move-exception;
        r34 = r2;
        r35 = r3;
        r3 = 0;
        r2 = r0;
    L_0x040d:
        r13 = "TLS is not a valid SSL protocol.";
        r14 = "08000";
        r15 = r1.getExceptionInterceptor();
        r3 = com.mysql.jdbc.SQLError.createSQLException(r13, r14, r3, r3, r15);
        throw r3;
    L_0x041a:
        r0 = move-exception;
        r33 = r10;
        r29 = r13;
        r10 = r0;
        goto L_0x053a;
    L_0x0422:
        r0 = move-exception;
        r33 = r10;
        r29 = r13;
        r10 = r0;
        goto L_0x0453;
    L_0x0429:
        r0 = move-exception;
        r33 = r10;
        r29 = r13;
        r10 = r0;
        goto L_0x0496;
    L_0x0431:
        r0 = move-exception;
        r33 = r10;
        r29 = r13;
        r10 = r0;
        goto L_0x04c1;
    L_0x0439:
        r0 = move-exception;
        r33 = r10;
        r29 = r13;
        r10 = r0;
        goto L_0x04ed;
    L_0x0441:
        r0 = move-exception;
        r33 = r10;
        r29 = r13;
        r10 = r0;
        goto L_0x0519;
    L_0x0449:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
    L_0x044d:
        goto L_0x053a;
    L_0x044f:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
    L_0x0453:
        r12 = new java.lang.StringBuilder;	 Catch:{ all -> 0x048c }
        r12.<init>();	 Catch:{ all -> 0x048c }
        r14 = "Cannot open ";
        r12.append(r14);	 Catch:{ all -> 0x048c }
        r12.append(r7);	 Catch:{ all -> 0x048c }
        r14 = " [";
        r12.append(r14);	 Catch:{ all -> 0x048c }
        r14 = r10.getMessage();	 Catch:{ all -> 0x048c }
        r12.append(r14);	 Catch:{ all -> 0x048c }
        r14 = "]";
        r12.append(r14);	 Catch:{ all -> 0x048c }
        r12 = r12.toString();	 Catch:{ all -> 0x048c }
        r14 = "08000";
        r15 = r1.getExceptionInterceptor();	 Catch:{ all -> 0x048c }
        r36 = r13;
        r13 = 0;
        r12 = com.mysql.jdbc.SQLError.createSQLException(r12, r14, r13, r13, r15);	 Catch:{ all -> 0x0486 }
        r12.initCause(r10);	 Catch:{ all -> 0x0486 }
        throw r12;	 Catch:{ all -> 0x0486 }
    L_0x0486:
        r0 = move-exception;
        r10 = r0;
        r13 = r36;
        goto L_0x053a;
    L_0x048c:
        r0 = move-exception;
        r36 = r13;
        r10 = r0;
        goto L_0x053a;
    L_0x0492:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
    L_0x0496:
        r12 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0538 }
        r12.<init>();	 Catch:{ all -> 0x0538 }
        r14 = "Could not load trust";
        r12.append(r14);	 Catch:{ all -> 0x0538 }
        r12.append(r7);	 Catch:{ all -> 0x0538 }
        r14 = " keystore from ";
        r12.append(r14);	 Catch:{ all -> 0x0538 }
        r12.append(r5);	 Catch:{ all -> 0x0538 }
        r12 = r12.toString();	 Catch:{ all -> 0x0538 }
        r14 = "08000";
        r15 = r1.getExceptionInterceptor();	 Catch:{ all -> 0x0538 }
        r37 = r10;
        r10 = 0;
        r10 = com.mysql.jdbc.SQLError.createSQLException(r12, r14, r10, r10, r15);	 Catch:{ all -> 0x0538 }
        throw r10;	 Catch:{ all -> 0x0538 }
    L_0x04bd:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
    L_0x04c1:
        r12 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0538 }
        r12.<init>();	 Catch:{ all -> 0x0538 }
        r14 = "Unsupported keystore algorithm [";
        r12.append(r14);	 Catch:{ all -> 0x0538 }
        r14 = r10.getMessage();	 Catch:{ all -> 0x0538 }
        r12.append(r14);	 Catch:{ all -> 0x0538 }
        r14 = "]";
        r12.append(r14);	 Catch:{ all -> 0x0538 }
        r12 = r12.toString();	 Catch:{ all -> 0x0538 }
        r14 = "08000";
        r15 = r1.getExceptionInterceptor();	 Catch:{ all -> 0x0538 }
        r38 = r10;
        r10 = 0;
        r10 = com.mysql.jdbc.SQLError.createSQLException(r12, r14, r10, r10, r15);	 Catch:{ all -> 0x0538 }
        throw r10;	 Catch:{ all -> 0x0538 }
    L_0x04e9:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
    L_0x04ed:
        r12 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0538 }
        r12.<init>();	 Catch:{ all -> 0x0538 }
        r14 = "Could not create KeyStore instance [";
        r12.append(r14);	 Catch:{ all -> 0x0538 }
        r14 = r10.getMessage();	 Catch:{ all -> 0x0538 }
        r12.append(r14);	 Catch:{ all -> 0x0538 }
        r14 = "]";
        r12.append(r14);	 Catch:{ all -> 0x0538 }
        r12 = r12.toString();	 Catch:{ all -> 0x0538 }
        r14 = "08000";
        r15 = r1.getExceptionInterceptor();	 Catch:{ all -> 0x0538 }
        r39 = r10;
        r10 = 0;
        r10 = com.mysql.jdbc.SQLError.createSQLException(r12, r14, r10, r10, r15);	 Catch:{ all -> 0x0538 }
        throw r10;	 Catch:{ all -> 0x0538 }
    L_0x0515:
        r0 = move-exception;
        r33 = r10;
        r10 = r0;
    L_0x0519:
        r12 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0538 }
        r12.<init>();	 Catch:{ all -> 0x0538 }
        r12.append(r5);	 Catch:{ all -> 0x0538 }
        r14 = " does not appear to be a valid URL.";
        r12.append(r14);	 Catch:{ all -> 0x0538 }
        r12 = r12.toString();	 Catch:{ all -> 0x0538 }
        r14 = "08000";
        r15 = r1.getExceptionInterceptor();	 Catch:{ all -> 0x0538 }
        r40 = r10;
        r10 = 0;
        r10 = com.mysql.jdbc.SQLError.createSQLException(r12, r14, r10, r10, r15);	 Catch:{ all -> 0x0538 }
        throw r10;	 Catch:{ all -> 0x0538 }
    L_0x0538:
        r0 = move-exception;
    L_0x0539:
        r10 = r0;
    L_0x053a:
        r12 = r33;
        if (r13 == 0) goto L_0x0543;
    L_0x053e:
        r13.close();	 Catch:{ IOException -> 0x0542 }
        goto L_0x0543;
    L_0x0542:
        r0 = move-exception;
        throw r10;
    L_0x0545:
        r0 = move-exception;
        r20 = r11;
        r11 = r8;
        r8 = r0;
        r12 = "Default algorithm definitions for TrustManager and/or KeyManager are invalid.  Check java security properties file.";
        r13 = "08000";
        r14 = r41.getExceptionInterceptor();
        r15 = 0;
        r12 = com.mysql.jdbc.SQLError.createSQLException(r12, r13, r15, r15, r14);
        throw r12;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mysql.jdbc.ExportControlled.getSSLSocketFactoryDefaultOrConfigured(com.mysql.jdbc.MysqlIO):javax.net.ssl.SSLSocketFactory");
    }

    public static boolean isSSLEstablished(MysqlIO mysqlIO) {
        return SSLSocket.class.isAssignableFrom(mysqlIO.mysqlConnection.getClass());
    }

    public static RSAPublicKey decodeRSAPublicKey(String key, ExceptionInterceptor interceptor) throws SQLException {
        if (key == null) {
            try {
                throw new SQLException("key parameter is null");
            } catch (Throwable ex) {
                throw SQLError.createSQLException("Unable to decode public key", SQLError.SQL_STATE_ILLEGAL_ARGUMENT, ex, interceptor);
            }
        }
        Exception ex2 = key.indexOf("\n") + 1;
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64Decoder.decode(key.getBytes(), ex2, key.indexOf("-----END PUBLIC KEY-----") - ex2)));
    }

    public static byte[] encryptWithRSAPublicKey(byte[] source, RSAPublicKey key, ExceptionInterceptor interceptor) throws SQLException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(1, key);
            return cipher.doFinal(source);
        } catch (Throwable ex) {
            throw SQLError.createSQLException(ex.getMessage(), SQLError.SQL_STATE_ILLEGAL_ARGUMENT, ex, interceptor);
        }
    }
}
