package com.bossae.chat;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.util.Scanner;

/**
 * USAGE: arg1 arg2 arg3 [arg4]
 * arg1: remoteHost
 * arg2: remotePort
 * arg3: plain|tls
 * arg4[optional]: true (for client authentication required)
 */
public class SocketClient implements Runnable, HandshakeCompletedListener {

    Socket mSocket = null;
    private static String remoteHost = "192.168.1.42";
    private static int remotePort = 2001;
    private static String socketType = "plain";
    private static boolean clientAuth = false;
    Thread mListenThread = null;
    public static void main(String[] args) throws Exception
    {
        if (args.length>=1) remoteHost = args[0];
        if (args.length>=2) remotePort = Integer.parseInt(args[1]);
        if (args.length>=3) socketType = args[2];
        if (args.length>=4) clientAuth = "true".equalsIgnoreCase(args[3]);

        SocketClient me = new SocketClient();
        me.mSocket = me.connect(remoteHost, remotePort, socketType,clientAuth);

        //new thread listens
        me.mListenThread = new Thread(me);
        me.mListenThread.start();

        //main thread writes:
        PrintWriter out = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                me.mSocket.getOutputStream())));

        Scanner scInput = null;
        String outboundLine = null;
        scInput = new Scanner(System.in);

        while ((outboundLine = scInput.nextLine())!= null) {
            if (!me.mListenThread.isAlive())
            {
                System.out.println("Message not sent: listener thread is dead");
                break;
            }
            out.write(outboundLine + "\r\n");
            out.flush();
        }
    }
    public SocketClient(){};

    int mPort;
    PrintWriter mIn;
    public SocketClient(int port, PrintWriter in)
    {
        mPort = port;
        mIn = in;
    }
    public PrintWriter getWriter()
    {
        return mIn;
    }
    public int getPort()
    {
        return mPort;
    }
    public void run() {

        BufferedReader in = null;
        try {

            in = new BufferedReader(
                    new InputStreamReader(
                            mSocket.getInputStream()));

            String inboundLine;
            while ((inboundLine = in.readLine()) != null) //can throw javax.net.ssl.SSLHandshakeException: Received fatal alert: bad_certificate
                System.out.println(inboundLine);
            System.out.println("Listen Thread: connexion ended");
        }
        catch(javax.net.ssl.SSLHandshakeException sslEx){
            System.out.println("Listen Thread: error: SSL Exception: " + sslEx.getMessage());
        }
        catch (Exception e) {
            System.out.println("Listen Thread: error: " + e.getMessage());
        }
        finally
        {
            try {
                if (in != null) in.close();
            }
            catch(Exception e){throw new RuntimeException(e);}
        }
    }

    private SSLSocketFactory getClientAuthSSLFactory() throws IOException {
        SSLSocketFactory factory = null;
        try {
            SSLContext ctx;
            KeyManagerFactory kmf;
            KeyStore ks;

            ctx = SSLContext.getInstance("TLS");
            kmf = KeyManagerFactory.getInstance("SunX509");
            ks = KeyStore.getInstance("JKS");


            InputStream fi = SocketClient.class.getClassLoader().getResourceAsStream("duke.jks");
            if (fi == null) throw new IOException("Could not find duke.jks");

            ks.load(fi, "passphrase".toCharArray());
            kmf.init(ks, "passphrase".toCharArray());

            ctx.init(kmf.getKeyManagers(), null, null);
            System.out.println("Using client certificate: duke.jks");
            return ctx.getSocketFactory();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private Socket connect(String serverIP, int port, String type, boolean clientAuth) {
        switch (type) {
            case "plain":
                try {
                    return new Socket(serverIP, port);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            case "tls":
                try {
                    System.out.println("Using TLS");
                    URL cacerts = SocketClient.class.getClassLoader().getResource("samplecacerts.jks");
                    if (cacerts == null) throw new IOException("Could not find samplecacerts.jks");
                    System.setProperty("javax.net.ssl.trustStore",cacerts.getFile());
                    System.out.println("-Djavax.net.ssl.trustStore=" + System.getProperty("javax.net.ssl.trustStore"));
                    //TODO: no password needed: is it because it is "changeit" and that's default?

                    SSLSocketFactory ssf = null;
                    if (clientAuth)
                        ssf = getClientAuthSSLFactory();
                    else
                        ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();

                    SSLSocket sslSocket = (SSLSocket) ssf.createSocket(serverIP, port);
                    sslSocket.addHandshakeCompletedListener(this);
                    sslSocket.startHandshake();
                    return sslSocket;
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
        }
        return null;
    }

    @Override
    public void handshakeCompleted(HandshakeCompletedEvent event) {
        System.out.println("Handshake completed: " + event.toString());
    }
}