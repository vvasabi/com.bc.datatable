package com.bc.datatable;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class WrapRequestFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// NOOP
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(new CustomRequestWrapper((HttpServletRequest)request),
				response);
	}

	@Override
	public void destroy() {
		// NOOP
	}

}
