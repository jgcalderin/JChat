package com.bossae.chat.gui.worker;

import com.bossae.chat.gui.ChatSwing;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class ListenTask extends SwingWorker<Void,String> {
    ChatSwing mCaller;
    Socket mSocket;
    private String strReceived;

    public ListenTask(ChatSwing caller, Socket socket) {
        mCaller = caller;
        mSocket = socket;
    }

    @Override
    protected Void doInBackground() throws Exception {
        BufferedReader in = new
                BufferedReader(
                    new InputStreamReader(
                        mSocket.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) //blocks thread; can throw javax.net.ssl.SSLHandshakeException: Received fatal alert: bad_certificate
            publish(inputLine);

        return null;
    }

    protected void process(List<String> strs) {
        for (String s: strs) {
            this.strReceived = s;
            mCaller.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "received"));
        }
    }

    @Override
    protected void done() {
        firePropertyChange("connected","true","false");
    }

    public String getReceived()
    {
        return strReceived;
    }
}
