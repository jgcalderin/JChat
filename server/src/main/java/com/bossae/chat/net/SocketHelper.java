package com.bossae.chat.net;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;

public class SocketHelper {

    public static ServerSocketFactory getServerSocketFactory(String socketType)
            throws
            UnrecoverableKeyException,
            CertificateException,
            NoSuchAlgorithmException,
            KeyStoreException,
            IOException, KeyManagementException {
        if (!socketType.equalsIgnoreCase("TLS")) return ServerSocketFactory.getDefault();

        SSLServerSocketFactory ssf = null;
        try {
            // set up key manager to do server authentication
            SSLContext ctx;
            KeyManagerFactory kmf;
            KeyStore ks;

            URL cacerts = SocketHelper.class.getClassLoader().getResource("samplecacerts.jks");
            if (cacerts == null) throw new IOException("Could not find samplecacerts.jks");
            System.setProperty("javax.net.ssl.trustStore",cacerts.getFile());
            System.out.println("-Djavax.net.ssl.trustStore=" + System.getProperty("javax.net.ssl.trustStore"));

            ctx = SSLContext.getInstance("TLS");
            kmf = KeyManagerFactory.getInstance("SunX509");
            ks = KeyStore.getInstance("JKS");

            //InputStream fi = SocketHelper.class.getResourceAsStream("testkeys.jks"); doesn't find shit
            InputStream fi = SocketHelper.class.getClassLoader().getResourceAsStream("testkeys.jks");
            if (fi == null) throw new IOException("Could not find testkeys.jks");
            ks.load(fi, "passphrase".toCharArray());
            kmf.init(ks, "passphrase".toCharArray());
            ctx.init(kmf.getKeyManagers(), null, null);

            ssf = ctx.getServerSocketFactory();
            System.out.println("USING TLS: testkeys.jks");
            return ssf;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
