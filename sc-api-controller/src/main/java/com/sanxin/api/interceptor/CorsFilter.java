package com.sanxin.api.interceptor;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;

/**
 * 跨域过滤器
 * @author huangh
 * @since 2019-10-22 22:05:34
 */
public class CorsFilter implements Filter {

	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		response.setHeader("Access-Control-Allow-Origin", "*");

		if ("OPTIONS".equals(request.getMethod())) {
			response.setStatus(HttpStatus.SC_NO_CONTENT);
			response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, DELETE");
			response.setHeader("Access-Control-Allow-Headers", "Content-Type, x-requested-with, Authorization");
			response.addHeader("Access-Control-Max-Age", "1");
		}
		filterChain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {

	}
}
