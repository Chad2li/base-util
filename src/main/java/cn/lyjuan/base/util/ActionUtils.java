package cn.lyjuan.base.util;


import cn.lyjuan.base.cst.ProjectCst;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;


public class ActionUtils
{
	private static Logger log = LogManager.getLogger(ActionUtils.class.getName());

	/**
	 * 是否ajax请求
	 */
	public static boolean isAjaxRequest(HttpServletRequest request)
	{
		if ("XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
				|| request.getParameter("ajax") != null || request.getHeader("ajax") == "true") { return true; }

		if (request.getRequestURI().indexOf("/api/") > -1) return true;

		return false;
	}


	/**
	 * 返回 Ajax 文字结果信息
	 * 
	 * @return
	 */
	public static String getApiAjaxResult(int code, String desc)
	{
		return "{\"" + ProjectCst.$_RESULT_CODE + "\":" + code + ", \"" + ProjectCst.$_RESULT_CONTENT + "\":\"" + desc + "\"}";
	}


	/**
	 * 为请求和响应设置默认编码
	 * 
	 * @param req
	 */
	public static void setDefaultEncode(HttpServletRequest req, HttpServletResponse resp)
	{
		try
		{
			req.setCharacterEncoding(ProjectCst.$_CHARSET);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		resp.setCharacterEncoding(ProjectCst.$_CHARSET);
		resp.setContentType("text/html;charset=" + ProjectCst.$_CHARSET);
	}

	/**
	 * 返回 Ajax 请求响应
	 * @param req		HTTP请求
	 * @param resp		HTTP响应
	 * @param jsonObj	转化为JSON字符串
	 * @throws java.io.IOException
	 */
	public static void returnAjax(HttpServletRequest req, HttpServletResponse resp, Object jsonObj)
	{
		String json = JsonUtils.to(jsonObj);

		returnAjax(req, resp, json);
	}

	/**
	 * 返回 Ajax 请求响应
	 * @param req		HTTP请求
	 * @param resp		HTTP响应
	 * @param code		返回状态码
	 * @param desc		返回状态码描述
	 * @throws java.io.IOException
	 */
	public static void returnAjax(HttpServletRequest req, HttpServletResponse resp, int code,
			String desc)
	{
		String json = getApiAjaxResult(code, desc);

		returnAjax(req, resp, json);
	}

	/**
	 * 返回 Ajax 请求响应
	 * @param req		HTTP请求
	 * @param resp		HTTP响应
	 * @param json		返回的JSON数据
	 * @throws java.io.IOException
	 */
	public static void returnAjax(HttpServletRequest req, HttpServletResponse resp, String json)
	{
		// 设置请求响应编码
		setDefaultEncode(req, resp);

		// 获取输出封装流
		PrintWriter out = null;
		try
		{
			out = resp.getWriter();
			
			// 返回信息
			log.debug("Method[returnAjax] response[" + json + "]");
			
			out.write(json);
			out.flush();
		}
		catch (IOException e)
		{
			log.error("Method[returnAjax] 获取输出流异常", e);
		}
	}

	/**
	 * 返回后台信息
	 * @param req
	 * @param resp
	 * @param code
	 * @param desc
	 */
	public static void returnDwzAjax(HttpServletRequest req, HttpServletResponse resp, int code, String desc)
	{
		String json = getDWZajaxReturn(code, desc);

		returnAjax(req, resp, json);
	}
	
	/**
	 * 得到dwz ajax返回的字符串
      "statusCode":"200", 
      "message":"操作成功", 
      "navTabId":"", 
      "rel":"", 
      "callbackType":"closeCurrent",
      "forwardUrl":""
	 * 服务器转回navTabId可以把那个navTab标记为reloadFlag=1, 下次切换到那个navTab时会重新载入内容. 
	 * callbackType如果是closeCurrent就会关闭当前tab
	 * 只有callbackType="forward"时需要forwardUrl值
	 * @return
	 */
	public static String getDWZajaxReturn(int code,String msg,String navTabId,String rel,String callbackType,String forwardUrl){
		StringBuffer sb = new StringBuffer();
		sb.append("{\"statusCode\":\"").append(code).append("\",\"message\":\"").append(msg).append("\"");
		if (!StringUtils.isNull(navTabId)){
			sb.append(",\"navTabId\":\"").append(navTabId).append("\"");
		}
		if (!StringUtils.isNull(rel)){
			sb.append(",\"rel\":\"").append(rel).append("\"");
		}
		if (!StringUtils.isNull(callbackType)){
			sb.append(",\"callbackType\":\"").append(callbackType).append("\"");
		}
		if (!StringUtils.isNull(forwardUrl)){
			sb.append(",\"forwardUrl\":\"").append(forwardUrl).append("\"");
		}
		sb.append("}");
		return sb.toString();
	}
	
	public static String getDWZajaxReturn(int code, String msg)
	{
		return getDWZajaxReturn(code, msg, null, null, null, null);
	}
}