package com.ivo;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


class Transmitter {
    private Webcam webcam;

    // initializes UI and gets device's default web camera
    private void initUI(String address, int port){
        System.out.println("Starting transmission to " + address + ":" + port);

        webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());

        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);

        JFrame window = new JFrame("Stream Transmitter");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }

    public void writeMessage(DataOutputStream dout, byte[] msg, int msgLen) throws IOException {
        dout.writeInt(msgLen);
        dout.write(msg, 0, msgLen);
        dout.flush();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    void transmit(String address, int port) throws IOException, InterruptedException {
        initUI(address, port);

        // Setup Socket and get it's output stream
        InetAddress ipAddress = InetAddress.getByName(address);
        Socket socket = new Socket(ipAddress, port);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        BufferedImage image;

        while(true)
        {
            // Get web cam image
            image = webcam.getImage();

            // Convert image and it's size to byte arrays
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image,"jpg", baos);
            int imageByteCount = baos.size();
            byte[] imageBytes = baos.toByteArray();

            // Send image's byte array size(used for reading) and it's data bytes
            writeMessage(out, imageBytes, imageByteCount);
            baos.flush();

            System.out.println("Transmitted: " + System.currentTimeMillis());
            //Thread.sleep(25);

            /* TODO: close socket when done
               System.out.println("Closing: " + System.currentTimeMillis());
               socket.close();
            */
        }
    }
}