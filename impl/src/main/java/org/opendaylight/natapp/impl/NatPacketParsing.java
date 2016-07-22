/*
 * Copyright © 2016 Tata Consultancy Services and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.natapp.impl;

import java.util.Arrays;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

public class NatPacketParsing {

/**
 * Packet Parser for the Packet_In messages received at the Controller.
*/
	/**
    * length of IP address in array
    */
	
	private static final int IP_LENGTH = 4;

    /**
     * start position of source IP address in array
     */
    private static final int SRC_IP_START_POSITION = 26;

    /**
     * end position of source IP address in array
     */
    private static final int SRC_IP_END_POSITION = 30;

    /**
     * start position of Destination IP address in array
     */
    private static final int DST_IP_START_POSITION = 30;

    /**
     * end position of Destination IP address in array
     */
    private static final int DST_IP_END_POSITION = 34;
    
    /**
     * length of source TCP/UDP Port in array
     */
    
    private static final int PORT_LENGTH = 2;

    /**
     * start position of source TCP/UDP Port in array
     */
    private static final int SRC_PORT_START_POSITION = 34;

    /**
     * end position of source TCP/UDP Port  in array
     */
    private static final int SRC_PORT_END_POSITION = 36;

    /**
     * start position of Destination TCP/UDP Port  in array
     */
    private static final int DST_PORT_START_POSITION = 36;

    /**
     * end position of DestinationTCP/UDP Port in array
     */
    private static final int DST_PORT_END_POSITION = 38;
   
    private static final int POWER = 256;

    /**
     * @param payload
     * @return destination IP address
     */
    public static byte[] extractDstIP(final byte[] payload) {
        return Arrays.copyOfRange(payload, DST_IP_START_POSITION, DST_IP_END_POSITION);
    }

    /**
     * @param payload
     * @return source IP address
     */
    public static byte[] extractSrcIP(final byte[] payload) {
        return Arrays.copyOfRange(payload, SRC_IP_START_POSITION, SRC_IP_END_POSITION);
    }

    /**
     * @param payload
     * @return Source TCP Port
     */
    public static byte[] extractSrcPort(final byte[] payload) {
        return Arrays.copyOfRange(payload, SRC_PORT_START_POSITION, SRC_PORT_END_POSITION);
    }

    /**
     * @param payload
     * @return Destination TCP Port
     */
    public static byte[] extractDstPort(final byte[] payload) {
        return Arrays.copyOfRange(payload, DST_PORT_START_POSITION, DST_PORT_END_POSITION);
    }

    /**
     * @param rawIP
     * @return String IPAddress
     */
    public static String rawIPToString(byte[] rawIP) {
        if (rawIP != null && rawIP.length == IP_LENGTH) {
            StringBuilder sb = new StringBuilder();
            for (byte octet : rawIP) {
                sb.append(String.format(".%d", octet));
            }
            return sb.substring(1);
        }
        return null;
    }

    /**
     * @param rawPort
     * @return int TCPPort
     */
    public static int rawPortToInteger(byte[] rawPort) {
        int intOctet =0;
        int intOctetSum = 0;
        int iter = 1;
        if (rawPort != null && rawPort.length == PORT_LENGTH) {
            for (byte octet : rawPort) {
                intOctet = octet & 0xff;
                intOctetSum = (int) (intOctetSum + intOctet *  Math.pow(POWER,iter));
                iter--;
            }
            return intOctetSum;
        }
        return 0;
    }

}
