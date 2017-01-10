package cn.lyjuan.base.util;

import cn.lyjuan.base.cst.ProjectCst;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ly on 2015/4/15.
 */
public class RespMap
{
    private Map<String, Object> respMap = new HashMap<String, Object>();

    public RespMap(Map<String, Object> respMap)
    {
        this.respMap = respMap;
    }

    public RespMap()
    {
    }

    public RespMap(int retcode, String retmsg)
    {
        this.putMsg(ProjectCst.$_RESULT_CODE, retcode).putMsg(ProjectCst.$_RESULT_CONTENT, retmsg);
    }

    public RespMap putMsg(String key, Object value)
    {
        respMap.put(key, value);

        return this;
    }

    public Map<String, Object> getRespMap()
    {
        return respMap;
    }

    public static RespMap init(int retcode, String retmsg)
    {
        return  new RespMap(retcode, retmsg);
    }

    public static RespMap init(){return new RespMap(); }
}
