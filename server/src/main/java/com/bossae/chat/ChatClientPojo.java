package com.bossae.chat;

import com.sun.jdi.IntegerType;

import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import javax.net.ssl.*;

public class ChatClientPojo {

    Socket mSocket = null;
    PrintWriter mIn;
    public ChatClientPojo(Socket socket, PrintWriter in)
    {
        mSocket = socket;
        mIn = in;
    }
    public PrintWriter getWriter()
    {
        return mIn;
    }
    public int getPort()
    {
        return mSocket.getPort();
    }

    public String getClientId()
    {
        //TODO: return client certificate alias when SSLSocket
        if (mSocket instanceof SSLSocket)
            return ((SSLSocket)mSocket).getSession().toString();
        else
            return Integer.toString(this.getPort());
    }
}