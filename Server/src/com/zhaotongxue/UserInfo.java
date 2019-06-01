package com.zhaotongxue;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * @author zhao
 * ����������UserInfo�����䣬����������Ϣ��ʽ��һ�²����㴦�����Է���
 * @version 1.0
 */
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private InetAddress ipAddr;
    private String name;

    public UserInfo(InetAddress inetAddress, String name) {
        this.ipAddr = inetAddress;
        this.name = name;
    }

    /**
     *
     * @return ip
     */
    public InetAddress getIpAddr() {
        return ipAddr;
    }

    /**
     *
     * @param ipAddr
     * �û�ip
     */
    public void setIpAddr(InetAddress ipAddr) {
        this.ipAddr = ipAddr;
    }

    /**
     *
     * @return �û���
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * �����û���
     */
    public void setName(String name) {
        this.name = name;
    }
}
