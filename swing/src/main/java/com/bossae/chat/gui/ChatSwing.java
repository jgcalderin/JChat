package com.bossae.chat.gui;
import com.bossae.chat.gui.worker.ListenTask;
import com.bossae.chat.gui.worker.SendTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ChatSwing extends JPanel implements ActionListener {

    private static int port = 2001;
    private static String remoteHost = "192.168.1.42";


    JTextField outboundTxt;
    JTextArea chatArea;
    JButton connectBtn;
    JButton sendBtn;
    Socket mSocket;
    public ChatSwing(JFrame frame)
    {
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Welcome to my chat"),BorderLayout.NORTH);
        connectBtn = new JButton("Connect");
        connectBtn.addActionListener(this);
        connectBtn.setActionCommand("connect");
        topPanel.add(connectBtn, BorderLayout.NORTH);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollChatPane = new JScrollPane(chatArea);
        scrollChatPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollChatPane.getVerticalScrollBar().setAutoscrolls(true); //doesn't work after enlarging window
        scrollChatPane.setPreferredSize(new Dimension(250, 145));
        scrollChatPane.setMinimumSize(new Dimension(10, 10));
        frame.getContentPane().add(scrollChatPane);


        JPanel outPanel = new JPanel();
        outboundTxt = new JTextField(60);
        outboundTxt.setActionCommand("Enter Text");
        outboundTxt.addActionListener(this);
        outPanel.add(outboundTxt);
        sendBtn = new JButton("Send");
        sendBtn.setActionCommand("send");
        sendBtn.addActionListener(this);
        sendBtn.setEnabled(false);
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setActionCommand("delete");
        deleteBtn.addActionListener(this);
        outPanel.add(sendBtn);
        outPanel.add(deleteBtn);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(outPanel);

        frame.getContentPane().add(bottomPanel,BorderLayout.SOUTH);

    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("ChatSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        new ChatSwing(frame);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("delete"))
            outboundTxt.setText("");
        else if (e.getActionCommand().equalsIgnoreCase("connect")) {
            try {
                mSocket = new Socket(remoteHost, port);
                connectBtn.setEnabled(false);
                sendBtn.setEnabled(true);
                ListenTask listentask = new ListenTask(this,mSocket);
                listentask.addPropertyChangeListener(
                        new PropertyChangeListener() {
                            public  void propertyChange(PropertyChangeEvent evt) {
                                if ("connected".equals(evt.getPropertyName())) {
                                    if ("false".equalsIgnoreCase((String)evt.getNewValue()))
                                    {
                                        connectBtn.setEnabled(true);
                                        sendBtn.setEnabled(false);
                                        chatArea.append("----Server disconnected----\n");
                                }
                            }
                        }
                    });
                listentask.execute();
            }
            catch(Exception ex)
            {
                System.out.println("Error on connection to server" + ex.getMessage());
                chatArea.append("Error:" + ex.getMessage() + "\n");
            }
        }
        else if (e.getActionCommand().equalsIgnoreCase("send")) {
            if (!outboundTxt.getText().isEmpty())
            {
                (new SendTask(mSocket, outboundTxt.getText())).execute();
                chatArea.append("Me> " + outboundTxt.getText() + "\n");
                outboundTxt.setText("");
            }
        }
        else if (e.getActionCommand().equalsIgnoreCase("received")) {
            chatArea.append(
                    ((ListenTask)e.getSource()).getReceived() + "\n");
        }
    }

    public static void main(String[] args) {

        if (args.length >= 1) remoteHost = args[0];
        if (args.length >= 2) port = Integer.parseInt(args[1]);

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

        //TODO: create here a Thread.sleep() and examine threads list (main + even-thread expected)
    }
}