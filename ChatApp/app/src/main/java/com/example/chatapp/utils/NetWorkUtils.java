package com.example.chatapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author wm
 * @Classname NetWorkUtils
 * @Description 获取网络Ip等
 * @Version 1.0.0
 * @Date 2023/3/1 19:57
 * @Created by wm
 */
public class NetWorkUtils {

    /**
     * @param
     * @return
     * @version V1.0
     * @Title getLocalIPAddress
     * @author wm
     * @createTime 2023/3/1 19:39
     * @description wifi下获取本地网络IP地址（局域网地址）,不开wifi，为0:0:0:0;连接WiFi后由局域网分配ip
     */
    public static String getLocalIPAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            @SuppressLint("MissingPermission")
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
            return ipAddress;
        }
        return "";
    }

    /**
     *  @version V1.0
     *  @Title intIP2StringIP
     *  @author wm
     *  @createTime 2023/3/1 20:03
     *  @description 格式化ip地址
     *  @param
     *  @return 
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title getHostIp
     * @author wm
     * @createTime 2023/3/1 19:42
     * @description 获取有限网IP，不开热点时为10.138.11.251；开热点之后本机变为局域网，分配ip：192.168.xx.xx
     */
    public static String getHostIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            ChatAppLog.error(ex.getMessage());
        }
        return "0.0.0.0";

    }

    public String getDeviceUuid(){
        byte[] uuid = (Build.PRODUCT + Build.ID).getBytes();
        return new String(uuid);
    }
}
