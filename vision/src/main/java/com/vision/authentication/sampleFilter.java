package com.vision.authentication;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.vision.exception.RuntimeCustomException;

import jespa.util.LogStream;


@Component
public class sampleFilter /*implements Filter */{

    /**
     * Default constructor. 
     */
    public sampleFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String username = request.getParameter("username");
		HttpServletRequest req = (HttpServletRequest) request;
		String rpath = getRequestPath(req);
		if(rpath.contains("authenticate")) {
			if(username.equalsIgnoreCase("dd")) {
				chain.doFilter(request, response);
			} else {
				throw new RuntimeCustomException("Wrong credential");
			}
		}else {
			chain.doFilter(request, response);
		}
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}
	
	protected String getRequestPath(HttpServletRequest request) throws ServletException {
		try {
			return new URI(request.getRequestURI()).normalize().getPath();
		} catch (URISyntaxException se) {
			throw new ServletException("Failed to compose request path", se);
		}
	}

}
