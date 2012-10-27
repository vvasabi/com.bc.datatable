package com.bc.datatable;

import java.util.Map;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.LazyDataModel;

/**
 * Provide a means to persist the state of a PrimeFaces DataTable across page
 * refresh and a way to programmatically set the state of a PrimeFaces
 * DataTable.
 *
 * @author Brad Chen
 */
public abstract class PersistStateDataModel<T> extends LazyDataModel<T> {

	private static final long serialVersionUID = -2083928021645116757L;

	/**
	 * DataTable is not serializable, hence the transient modifier.
	 */
	private transient DataTable dataTable;

	private transient boolean dataTableInitialized;

	/**
	 * Get the first row to display.
	 *
	 * @return first row to display
	 */
	public abstract int getFirst();

	/**
	 * Current sort field.
	 *
	 * @return current sort field
	 */
	public abstract String getSortField();

	/**
	 * Current sort order.
	 *
	 * @return current sort order
	 */
	public abstract String getSortOrder();

	/**
	 * Current filters applied to the data table.
	 *
	 * @return current filters applied to the data table
	 */
	public abstract Map<String, String> getFilters();

	/**
	 * Data table instance should not be reused across requests.
	 *
	 * @return null
	 */
	public DataTable getDataTable() {
		return null;
	}

	public void setDataTable(DataTable dataTable) {
		this.dataTable = dataTable;

		dataTableInitialized = false;
		if (dataTable.getValue() != null) {
			initializeDataTable();
		}
	}

	/**
	 * Must be called at the PreRenderComponentEvent.
	 */
	public void preRenderComponent() {
		initializeDataTable();
	}

	private void initializeDataTable() {
		if (dataTableInitialized) {
			return;
		}
		dataTableInitialized = true;

		// initialize data table
		setDataTableState();
		setFilterParams();

		// do not reuse the DataTable instance across requests
		dataTable = null;
	}

	/**
	 * Set the state of the data table.
	 */
	private void setDataTableState() {
		String sortField = getSortField();
		if (sortField != null) {
			String var = dataTable.getVar();
			String expression = "#{" + var + "." + getSortField() + "}";
			ValueExpression valueExpresison = createValueExpression(expression);
			dataTable.setValueExpression("sortBy", valueExpresison);
			dataTable.setSortOrder(getSortOrder());
		}

		dataTable.setFilters(getFilters());
		dataTable.setFirst(getFirst());
		dataTable.setRows(getPageSize());
	}

	/**
	 * Restore the filters applied.
	 */
	private void setFilterParams() {
		Map<String, String> filters = getFilters();
		if (filters == null) {
			return;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		char separator = UINamingContainer.getSeparatorChar(context);
		CustomRequestWrapper request = getCustomRequestWrapper();
		for (UIColumn column : dataTable.getColumns()) {
			ValueExpression filterBy = column.getValueExpression("filterBy");
			if (filterBy == null) {
				continue;
			}

			String property = getProperty(filterBy.getExpressionString());
			if (!filters.containsKey(property)) {
				continue;
			}

			String key = column.getContainerClientId(context) + separator
				+ "filter";
			request.addParameter(key, filters.get(property));
		}
	}

	/**
	 * Get the property part from an EL value expression.
	 *
	 * @param expression EL value expression, such as #{var.property}
	 * @return property part of the expression
	 */
	private String getProperty(String expression) {
		String content = expression.substring(2, expression.length() - 1);
		String[] tokens = content.split("\\.");
		if (tokens.length == 2) {
			return tokens[1];
		}
		throw new IllegalArgumentException("Invalid expression: " + expression);
	}

	private CustomRequestWrapper getCustomRequestWrapper() {
		FacesContext context = FacesContext.getCurrentInstance();
		return findCustomRequestWrapper((ServletRequest)context
				.getExternalContext().getRequest());
	}

	private CustomRequestWrapper findCustomRequestWrapper(ServletRequest
			request) {
		if (request instanceof CustomRequestWrapper) {
			return (CustomRequestWrapper)request;
		}
		if (request instanceof HttpServletRequestWrapper) {
			ServletRequest wrapped = ((HttpServletRequestWrapper)request)
				.getRequest();
			return findCustomRequestWrapper(wrapped);
		}
		throw new IllegalStateException("Please install WrapRequestFilter.");
	}

	private ValueExpression createValueExpression(String expression) {
		ELContext elContext = FacesContext.getCurrentInstance()
				.getELContext();
		ExpressionFactory elFactory = FacesContext.getCurrentInstance()
				.getApplication().getExpressionFactory();
		return elFactory.createValueExpression(elContext, expression,
				Object.class);
	}

}
