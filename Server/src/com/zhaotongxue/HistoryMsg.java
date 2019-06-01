package com.zhaotongxue;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author zhao
 * ����������ʷ��Ϣ
 * @version 1.0
 * ���������ô���������ʽ��������ʷ��Ϣ��������Ϊ��Ϣ��ʽ��һ�¿ͻ��˲��ô�������תΪ�ַ�������
 */
class SeriableSimpDateFormat extends SimpleDateFormat implements Serializable{
    public SeriableSimpDateFormat(String s){
        super(s);
    }
}
/**
 * @author zhao
 * ������ѯ��������ʷ��Ϣ
 * @version 1.0
 */
public class HistoryMsg implements Serializable {
    //��Ϣ�б�
    private ArrayList<Msg> msgs=new ArrayList<Msg>();
    //���ڸ�ʽ
    private SeriableSimpDateFormat simpleDateFormat=new SeriableSimpDateFormat("yyyy-MM-dd hh:mm:ss");

    /**
     *
     * @param userId
     * ������Ϣ�û�id
     * @param content
     * ����
     * @param msgDate
     * ����
     * @return �Ƿ���ӳɹ�
     */
    public boolean addMsg(String userId,String content,Date msgDate){
        return msgs.add(new Msg(userId,content,msgDate));
    }
    /**
     *
     * @param userId
     * ������Ϣ�û�id
     * @param content
     * ����
     * @param strDateTime
     * ����
     * @return �Ƿ���ӳɹ�
     */
    public boolean addMsg(String userId,String content,String strDateTime){
        try{
            Date msgDate=simpleDateFormat.parse(strDateTime);
            addMsg(userId,content,msgDate);
        }catch (ParseException e){
            e.printStackTrace();
            return false;
        }
        finally {
            return true;
        }

    }

    /**
     *
     * @param i
     *  ��Ϣ�±�
     * @return ��i����Ϣ
     */
    public Msg getMsg(int i){
        return msgs.get(i);
    }
    /**
     *
     * @param i
     *  ��Ϣ�±�
     * @return ��i����Ϣ����
     */
    public String getMsgContent(int i){
        return msgs.get(i).getContext();
    }
    /**
     *
     * @param i
     *  ��Ϣ�±�
     * @return ��i����Ϣ����
     */
    public Date getMsgDate(int i){
        return msgs.get(i).getDate();
    }
    /**
     *
     * @param i
     *  ��Ϣ�±�
     * @return ��i����Ϣ���ͷ�id
     */
    public String getMsgUserid(int i){
        return msgs.get(i).getUserId();
    }
    /**
     *
     * @param i
     *  ��Ϣ�±�
     * @return ��i����Ϣ��С
     */
    public int getMsgSize(){
        return msgs.size();
    }
}

/**
 * @author zhao
 * Msg����������Ϣ���䣬��������
 * @version 1.0
 */
class Msg implements Serializable{
    private static final long serialVersionUID = 1L;
    private String content = null;
    private String userId=null;
    private Date date=null;

    public Msg(String context, String userId, Date date) {
        this.content = context;
        this.userId = userId;
        this.date = date;
    }

    public String getContext() {
        return content;
    }

    public void setContext(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
