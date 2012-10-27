package com.bc.datatable;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogExceptionHandler extends ExceptionHandlerWrapper {

	private static final Logger logger
		= LoggerFactory.getLogger(LogExceptionHandler.class);

	private ExceptionHandler wrapped;

	public LogExceptionHandler(ExceptionHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ExceptionHandler getWrapped() {
		return this.wrapped;
	}

	@Override
	public void handle() throws FacesException {
		for (ExceptionQueuedEvent event : getUnhandledExceptionQueuedEvents()) {
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext)event.getSource();
			logger.error("Error occured.", context.getException());
		}

		// At this point, the queue will not contain any ViewExpiredEvents.
		// Therefore, let the parent handle them.
		getWrapped().handle();
	}

}
