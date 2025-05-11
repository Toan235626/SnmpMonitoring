package com.project.snmp.utils;

public class HexStrConvert {

    /**
     * Converts a hexadecimal string to a byte array.
     *
     * @param hexStr the hexadecimal string
     * @return the byte array
     */
    public static byte[] hexStringToByteArray(String hexStr) {
        if (hexStr == null || hexStr.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hexadecimal string");
        }
        int len = hexStr.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4)
                    + Character.digit(hexStr.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param bytes the byte array
     * @return the hexadecimal string
     */
    public static String byteArrayToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder hexStr = new StringBuilder();
        for (byte b : bytes) {
            hexStr.append(String.format("%02X", b));
        }
        return hexStr.toString();
    }
}