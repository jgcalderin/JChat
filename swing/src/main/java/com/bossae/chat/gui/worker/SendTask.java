package com.bossae.chat.gui.worker;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class SendTask extends SwingWorker<Void,Void> {
    Socket mSocket;
    String mMessage;

    public SendTask(Socket socket, String message) {
        mSocket = socket;
        mMessage = message;
    }

    @Override
    protected Void doInBackground() throws Exception {

        PrintWriter out = null;
        out = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                mSocket.getOutputStream())));


        if (mMessage.isEmpty()) return null;

        out.write(mMessage + "\r\n");
        out.flush();

        return null;
    }
}
