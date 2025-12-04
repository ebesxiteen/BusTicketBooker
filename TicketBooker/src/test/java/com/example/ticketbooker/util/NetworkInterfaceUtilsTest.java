package com.example.ticketbooker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.example.ticketbooker.Util.Utils.NetworkInterfaceUtils;

class NetworkInterfaceUtilsTest {

    @Test
    void returnsFirstIpv4AddressWhenInterfaceMatches() throws Exception {
        NetworkInterface wifiInterface = mock(NetworkInterface.class);
        when(wifiInterface.getName()).thenReturn("wlan0");
        when(wifiInterface.isUp()).thenReturn(true);
        when(wifiInterface.isLoopback()).thenReturn(false);
        InetAddress inetAddress = Inet4Address.getByName("192.168.1.10");
        when(wifiInterface.getInetAddresses()).thenReturn(Collections.enumeration(Collections.singleton(inetAddress)));

        try (MockedStatic<NetworkInterface> networkStatic = Mockito.mockStatic(NetworkInterface.class)) {
            Enumeration<NetworkInterface> interfaces = Collections.enumeration(Collections.singleton(wifiInterface));
            networkStatic.when(NetworkInterface::getNetworkInterfaces).thenReturn(interfaces);

            assertEquals("192.168.1.10", NetworkInterfaceUtils.getIPv4Address());
        }
    }

    @Test
    void returnsNullWhenNoInterfacesAvailable() throws SocketException {
        try (MockedStatic<NetworkInterface> networkStatic = Mockito.mockStatic(NetworkInterface.class)) {
            networkStatic.when(NetworkInterface::getNetworkInterfaces)
                    .thenReturn(Collections.emptyEnumeration());

            assertNull(NetworkInterfaceUtils.getIPv4Address());
        }
    }
}
