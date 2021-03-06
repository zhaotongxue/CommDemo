package com.zhaotongxue;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * ClientMain
 * @author zhao
 * 客户端主线程
 * @version 1.0
 */
public class ClientMain {
    Thread thread = null;
    //   private String host = "10.122.195.204";
    private String host = "127.0.0.1";
    private int port = 1919;
    private BufferedReader cmdReader = null;
    private User user = null;
    private Socket socket = null;
    private Commands status = Commands.NONE;
    private String strCmd = null;
    private Date date = null;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat slf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ArrayList<Msg> msgArray = new ArrayList<>();
    private boolean logined = false;

    /**
     * 初始化并且执行
     * @param args
     */
    public static void main(String[] args) {
        ClientMain clientMain = new ClientMain();
        clientMain.InitConnection();
        clientMain.MianProcess();
    }

    /**
     * 初始化主线程连接，接收线程连接
     */
    private void InitConnection() {
        while (true) {
            try {
                cmdReader = new BufferedReader(new InputStreamReader(System.in, "GBK"));
                socket = new Socket(host, port);
                user = new User(socket);
                System.out.println(user.recvMsg());
                Thread msgThread = new Thread(new Messages());
                msgThread.start();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Not find host,you can type them yourself:\n1:host");
                try {
                    host = cmdReader.readLine();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.out.println("Not find host,you can type them yourself:\n1:host");
                try {
                    port = Integer.parseInt(cmdReader.readLine());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    /**
     * 处理客户命令
     * @param inCmd
     * 已经转化过的命令
     * @param strCmd
     * 用户输入的原始命令
     */
    private void inCmdProcess(Commands inCmd, String strCmd) {
        switch (inCmd) {

            // 发送文件，那么直接发过去就行
            case FILETRANSFER:
                try {
                    sendFile(user, strCmd);
                } catch (IOException e) {
                }
                status = Commands.NONE;
                break;

            //登录
            case LOGIN:
                try {
                    if (userLogin(user, strCmd)) {
                        logined = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                status = Commands.NONE;
                break;

            //注册用户
            case REGISTER:
                try {
                    reg(user, strCmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                status = Commands.NONE;
                break;

            //获得用户列表
            case GETLIST:
                try {
                    getList(user);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                status = Commands.NONE;
                break;

            //获得历史消息
            case HISTORY:
                try {
                    getHistory(user, strCmd);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                status = Commands.NONE;
                break;

            //退出程序
            case EXIT:
                // new ExitClient(user);
                try {
                    exitClient();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                status = Commands.NONE;
                break;

            //退出端到端聊天
            case EXITPAIR:
                // new ExitPair(user);
                try {
                    exitPair(user);
                    thread.interrupt();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                status = Commands.NONE;
                break;

            //退出群组聊天
            case EXITGROUP:
                // new ExitGroup(user);
                try {
                    exitGroup(user);
                    thread.interrupt();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                status = Commands.NONE;
                break;

            //加入端到端聊天
            case PAIRCOMM:
                try {
                    if (pairComm(user, strCmd)) {
                        status = inCmd;
                        thread = new Thread(new RecvListener());
                        thread.start();
                    } else {
                        status = Commands.NONE;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;

            //加入群组聊天
            case GROUPCOMM:
                try {
                    if (groupComm(user)) {
                        status = inCmd;
                        thread = new Thread(new RecvListener());
                        thread.start();
                    } else {
                        status = Commands.NONE;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;

            case MSG:
                showMsgs();
                break;
            //否则什么都不变
            default:
                break;
        }
    }

    /**
     * 展示消息
     */
    private void showMsgs() {
        for (Msg m : msgArray) {
            System.out.println(m.getUserId() + ":\t" + m.getDate().toString() + "\n" + m.getContext());
        }
        msgArray.clear();
    }

    /**
     * 监听用户输入并且进行相应转化
     */
    private void MianProcess() {
        // 处理本地用户输入操作
        status = Commands.NONE;
        while (true) {
            try {

                // 用户输入，并且转换为命令
                strCmd = cmdReader.readLine();

                //对于未登录的情况，只能是首先登录或者注册
                if (!logined && CommandsConverter.getConverter().toCmds(strCmd.split(" ")[0]) != Commands.LOGIN && CommandsConverter.getConverter().toCmds(strCmd.split(" ")[0]) != Commands.REGISTER) {
                    System.out.println("Please Login first");
                    continue;
                }

                // 在群组通信和端到端通信的时候，直接把输入传进去，至于如果这时候是退出通信的命令，那么就是在群组通信和端到端通信中再检测一次就行了，如果是的话改变Status，不是的话保持。
                if (status != Commands.GROUPCOMM && status != Commands.PAIRCOMM) {
                    Commands inCmd = CommandsConverter.getConverter().toCmds(strCmd.split(" ")[0]);
                    inCmdProcess(inCmd, strCmd);
                } else {
                    if (strCmd.equals(CommandsConverter.getConverter().getStrCmd(Commands.EXITPAIR))) {
                        sendCmd(CommandsConverter.getConverter().getStrCmd(Commands.EXITPAIR));
                    } else if (strCmd.equals(CommandsConverter.getConverter().getStrCmd(Commands.EXITGROUP))) {
                        sendCmd(CommandsConverter.getConverter().getStrCmd(Commands.EXITGROUP));
                    } else {
                        //通信过程中就直接发送
                        sendMsg(strCmd);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    new ExitClient(user);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.out.println("Exit Program");
            }
        }
    }

    /**
     *
     *
     * @param cmd
     * 发送的命令
     * @throws IOException
     */
    private void sendCmd(String cmd) throws IOException {
        user.send(cmd);
        status = Commands.NONE;
    }

    /**
     * 发送消息
     */
    private void sendMsg(String strCmd) throws IOException {
        date = calendar.getTime();
        user.send(String.format("%s//DATE:%s", strCmd, slf.format(date)));
    }

    /**
     * 退出端到端同行
     * @param user
     * @throws IOException
     */
    private void exitPair(User user) throws IOException {
        user.send(CommandsConverter.getConverter().getStrCmd(Commands.EXITPAIR));
        String s = user.recvMsg();
        if (s.equals("//ExitedLogin")) {
            System.out.println("Pair exit login");
        } else {
            System.out.println(s);
        }
    }

    /**
     * 退出群组同行
     * @param user
     * @throws IOException
     */

    private void exitGroup(User user) throws IOException {
        user.send(CommandsConverter.getConverter().getStrCmd(Commands.EXITGROUP));
        String s = user.recvMsg();
        if (!s.equals("1")) {
            System.out.println(s);
        } else {
            System.out.println("Exit Group Comm failed");
        }
    }

    /**
     * 退出程序
     * @throws IOException
     */
    private void exitClient() throws IOException {
        user.disconnect();
        System.exit(1);
    }

    /**
     * 显示服务器发送过来的消息
     * @param recvdMsg
     */
    private void showInfo(String recvdMsg) {
        System.out.println(recvdMsg);
    }

    /**
     * 接收消息
     * @param user
     * @param fileName
     * @throws IOException
     */
    private void fileRecv(User user, String fileName) throws IOException {
        int port = 9009;
        fileRecv(user, port, fileName);
    }

    /**
     * 用特定端口接收消息，拓展功能
     * @param user
     * @param port
     * @param fileName
     * @throws IOException
     */
    private void fileRecv(User user, int port,String fileName) throws IOException {
        FileTransfer fileTransfer = new FileTransfer(user);
        fileTransfer.recvFile(port,fileName);
    }

    /**
     *请求获得历史消息
     * @param user
     * @param strCmd
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void getHistory(User user, String strCmd) throws IOException, ClassNotFoundException {
        GetHistory history = new GetHistory(user, strCmd);
        HistoryMsg historyMsg = history.getHistory();
        if (historyMsg != null) {
            for (int i = 0; i < historyMsg.getMsgSize(); i++) {
                showMsg(historyMsg.getMsg(i));
            }
        }
    }

    /**
     * 展示消息
     * @param msg
     */
    private void showMsg(String msg) {
        System.out.println(msg);
    }

    /**
     * 展示消息，不过基本作废了
     * @param msg
     */
    private void showMsg(Msg msg) {
        System.out.println(String.format("%s\t%s:\n%s", msg.getUserId(), msg.getDate().toString(), msg.getContext()));
    }

    /**
     * 获得当前用户列表
     * @param user
     * @throws IOException
     */
    private void getList(User user) throws IOException {
        GetList getUserList = new GetList(user);
        ArrayList<UserInfo> userList = getUserList.getList();
        if (userList == null)
            return;
        for (int i = 0; i < userList.size(); i++) {
            System.out.println(
                    String.format("%d\t:%s\t(ip is:%s)", i + 1, userList.get(i).getName(), userList.get(i).getIpAddr()));
        }
    }

    /**
     * 获得用户列表后进行相应处理
     * @param str
     * @return
     */
    private ArrayList<UserInfo> handleList(String str) {
        String[] strs = str.split("//MSG:");
        ArrayList<UserInfo> userList = new ArrayList<>();
        for (int i = 1; i < strs.length; i++) {
            UserInfo userInfo = new UserInfo(strs[i].split("//")[0], strs[i].split("//")[1]);
            userList.add(userInfo);
        }
        return userList;
    }

    /**
     * 展示在线用户
     * @param str
     */
    private void showUserList(String str) {
        ArrayList<UserInfo> userList = handleList(str);
        if (userList == null)
            return;
        for (int i = 0; i < userList.size(); i++) {
            System.out.println(
                    String.format("%d\t:%s\t(ip is:%s)", i + 1, userList.get(i).getName(), userList.get(i).getIpAddr()));
        }

    }

    /**
     * 发送文件
     * @param user
     * @param strCmd
     * @return
     * @throws IOException
     */
    private boolean sendFile(User user, String strCmd) throws IOException {
        String[] fileTransferCmds = strCmd.split(" ");
        String filename = "";
        for (int i = 2; i < fileTransferCmds.length; i++) {
            filename += fileTransferCmds[i];
        }
        String recvUserName = fileTransferCmds[1];
        FileTransfer file = new FileTransfer(user, recvUserName, filename);
        if (file.sendFile()) {
            System.out.println("Send File Successfully");
            return true;
        } else {
            System.out.println("Send File failed");
            return false;
        }
    }

    /**
     * 端到端通信管理
     * @param user
     * @param strCmd
     * @return
     * @throws IOException
     */
    private boolean pairComm(User user, String strCmd) throws IOException {
        PairComm pair = new PairComm(user, strCmd);
        return pair.joinPairComm();
    }

    /**
     * 群组通信管理
     * @param user
     * @return
     * @throws IOException
     */
    private boolean groupComm(User user) throws IOException {
        GroupComm group = new GroupComm(user);
        return group.joinGroup(user);
    }

    /**
     * 处理登录事务
     * @param user
     * @param strCmd
     * @return
     * @throws IOException
     */
    private boolean userLogin(User user, String strCmd) throws IOException {
        Login login = new Login(user, strCmd);
        if (login.login()) {
            System.out.println("Login successful");
            System.out.println(String.format("Login time:%s,Last Login IP:%s", login.getLastLoginTime(), login.getLastLoginTime()));
            return true;
        } else {
            System.out.println("Login filed");
            return false;
        }
    }

    /**
     * 负责注册用户
     * @param user
     * @param strCmd
     * @throws IOException
     */
    private void reg(User user, String strCmd) throws IOException {
        RegisterUser registerUser = new RegisterUser(user, strCmd);
        int regRes = registerUser.reg();
        if (regRes == 1) {
            System.out.println("Register successful!");
        } else {
            System.out.println("Register fail");
        }
    }

    /**
     * @author zhao
     * 内部类，实现背景消息监听
     * 主要是用来接收别的用户发过来的、但是当前用户并未与之正在通信的消息
     * @date 2019年6月1日
     * @version 1.0
     */
    class Messages implements Runnable {

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(2019);
                Socket messagesQueue = serverSocket.accept();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(messagesQueue.getInputStream()));
                String msg = null;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                while (true) {
                    msg = bufferedReader.readLine();
                    if (msg.equals("//RECVFILE")) {
                        String fileName = bufferedReader.readLine();
//                        String senderAddr = bufferedReader.readLine();
                        fileRecv(user, fileName);
                    } else {
                        //format:username//CONTENT:content//DATE:date
                        String otherUserName = msg.split("//DATE:")[0].split("//CONTENT:")[0];
                        String otherUserMsgDate = msg.split("//DATE:")[1];
                        String otherUserMsgContent = msg.split("//CONTENT:")[1].split("//DATE:")[0];
                        msgArray.add(new Msg(otherUserMsgContent, otherUserName, simpleDateFormat.parse(otherUserMsgDate)));
                        notificationUser();
                    }
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

        private void notificationUser() {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * @author zhao
     * 监听主线程接收到的消息
     * @date  2019年6月1日
     * @version 1.0
     */
    class RecvListener implements Runnable {

        @Override
        public void run() {
            String recvStrMsg = null;
            while (true) {
                try {
                    recvStrMsg = user.recvMsg();
                    Commands recvCmd = CommandsConverter.getConverter().toCmds(recvStrMsg);
                    if (recvCmd == Commands.EXITGROUP || recvCmd == Commands.EXITPAIR) {
                        status = Commands.NONE;
                        return;
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                switch (status) {

                    // 啥都没有
                    case NONE:
                        showInfo(recvStrMsg);
                        break;
                    case PAIRCOMM:
                        showMsg(recvStrMsg);
                        break;
                    case GROUPCOMM:
                        showMsg(recvStrMsg);
                        break;
                    default:
                        break;
                }
            }
        }

    }
}

