/**
 * Copyright 2010 Lennart Koopmann <lennart@socketfeed.com>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.graylog2.messagehandlers.gelf;

import org.graylog2.Log;

import java.net.*;
import java.io.*;
import java.util.zip.Inflater;

/**
 * GELFThread.java: Jun 23, 2010 6:58:07 PM
 *
 * Server that can listen for GELF messages.
 *
 * @author: Lennart Koopmann <lennart@socketfeed.com>
 */
public class GELFServer {
    private static final int MAX_PACKET_SIZE = 8192;

    private DatagramSocket serverSocket = null;

    /**
     * Server that can listen for GELF messages.
     */
    public GELFServer() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override public void run() {
                Log.info("Closing server socket.");
                tearDown();
            }
        }));
    }

    /**
     * Create the UDP socket.
     *
     * @param port The port to listen on.
     * @return boolean
     */
    public boolean create(int port) {
        try {
            this.serverSocket = new DatagramSocket(port);
        } catch(IOException e) {
            Log.emerg("Could not create ServerSocket in GELFServer::create(): " + e.toString());
            return false;
        }

        return true;
    }

    /**
     * Listens on the formerly created (create()) socket and returns
     * unzipped (GZIP) raw message that can be parsed to a GELFMessage.
     *
     * @return Received message
     * @throws Exception
     */
    public String listen() throws Exception {

        // Reveive and fill buffer.
        byte[] receiveData = new byte[MAX_PACKET_SIZE];
        DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            serverSocket.receive(receivedPacket);
        } catch (SocketException e) {
            return new String();
        }

        // Uncompress.
        Inflater decompresser = new Inflater();
        decompresser.setInput(receiveData, 0, receiveData.length);
        int finalLength = decompresser.inflate(receiveData);

        // Convert to String and return.
        return new String(receiveData, 0, finalLength, "UTF-8");
    }

    /**
     * Tear down the server. Closes the socket.
     */
    public void tearDown() {
        this.serverSocket.close();
    }
}
