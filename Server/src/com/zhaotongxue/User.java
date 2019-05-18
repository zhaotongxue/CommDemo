package com.zhaotongxue;

import com.zhaotongxue.SubProcess.ListMaster;
import com.zhaotongxue.SubProcess.PairMaster;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class User{
    //User 包括socket，用来通信，Writer，Reader用来实现具体通信
    private Socket socket=null;
    private BufferedWriter bufferedWriter=null;
    private BufferedReader bufferedReader=null;
    private InetAddress addr=null;
    private String name=null;
    private BufferedWriter msgWriter=null;

    public PairMaster getPairMaster() {
        return pairMaster;
    }

    public void setPairMaster(PairMaster pairMaster) {
        this.pairMaster = pairMaster;
    }

    private PairMaster pairMaster=null;
    public void setMsgSocket(Socket msgSocket) throws IOException {
        this.msgSocket = msgSocket;
        msgWriter=new BufferedWriter(new OutputStreamWriter(msgSocket.getOutputStream()));
    }
    public void msgSend(String str) throws IOException {
        msgWriter.write(str+"\n");
        msgWriter.flush();
    }

    private Socket msgSocket=null;

    public User(Socket socket) throws IOException {
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.socket=socket;
        addr=this.socket.getInetAddress();
    }

    @Override
    public boolean equals(Object obj) {
        try{
            User userObj=(User)obj;
            if(userObj.getName().equals(this.getName())) return true;
            return false;
        }catch (Exception e){
            return false;
        }
    }

    //发送消息方法,加锁，不能让信息扰乱了不是
    public void send(String msg) throws IOException {
        //发送消息并刷新
        bufferedWriter.write(msg+"\n");
        bufferedWriter.flush();
    }
    String recvMsg;
    //接收消息
    public String recv() throws IOException {
        //获得消息并且返回
        recvMsg=bufferedReader.readLine();
        return recvMsg;
    }

    public String getName(){return getAddr().toString().substring(1);}
    //获得ip地址
    public InetAddress getAddr() {
        return addr;
    }

    //获得全部用户列表
    public void getList() throws IOException {
        //获得用户列表并且发送
        ArrayList<UserInfo> userList= ListMaster.getListMaster().getUserInfoList();
//        sendObj(userList);
        String s="";
        for(int i=0;i<userList.size();i++){
           s+="//MSG:"+userList.get(i).getIpAddr().toString().substring(1)+"//"+userList.get(i).getName();
        }
        send(s);
    }

    //断开连接
    public void disconnect() throws IOException {
        this.socket.close();
        throw new IOException();
    }

    //发送对象，主要是用来传输历史记录
    public void sendObj(Serializable obj) throws IOException {
        OutputStream outputStream=socket.getOutputStream();
        ObjectOutputStream oos=new ObjectOutputStream(outputStream);
        oos.writeObject(obj);
        oos.flush();
        bufferedWriter.flush();
        oos=null;
        return;
    }
}
