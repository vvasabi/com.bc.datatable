package com.bc.datatable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.SortOrder;

@ManagedBean
@SessionScoped
public class CityDataModel extends PersistStateDataModel<City> {

	private static final long serialVersionUID = -1602304519712970957L;

	private static final Map<String, String> PROPERTY_FIELD_MAP;

	static {
		Map<String, String> map = new ConcurrentHashMap<String, String>();
		map.put("id", "id");
		map.put("name", "name");
		map.put("asciiName", "ascii_name");
		map.put("latitude", "latitude");
		map.put("longitude", "longitude");
		map.put("timeZone", "time_zone");
		map.put("population", "population");
		map.put("province", "province");
		PROPERTY_FIELD_MAP = Collections.unmodifiableMap(map);
	}

	private int first;
	private String sortField;
	private String sortOrder;
	private Map<String, String> filters;
	private List<City> cities;

	public CityDataModel() {
		// default values
		first = 0;
		sortField = "name";
		sortOrder = "ascending";
		setPageSize(24);
	}

	@Override
	public int getFirst() {
		return first;
	}

	@Override
	public String getSortField() {
		return sortField;
	}

	@Override
	public String getSortOrder() {
		return sortOrder;
	}

	@Override
	public Map<String, String> getFilters() {
		return filters;
	}

	public List<City> getData() {
		return cities;
	}

	public void setData(List<City> cities) {
		this.cities = cities;
	}

	@Override
	public City getRowData(String rowKey) {
		for (City entry : cities) {
			if (rowKey.equals(entry.getId().toString())) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(City city) {
		if (city == null) {
			return null;
		}

		return city.getId();
	}

	@Override
	public List<City> load(int first, int pageSize, String sortField,
			SortOrder order, Map<String, String> filters) {
		// cache parameters

		this.first = first;
		this.sortField = sortField;
		this.sortOrder = (order == SortOrder.ASCENDING) ? "ascending" :
			"descending";
		this.filters = filters;

		// builder for SELECT query
		StringBuilder selectQuery = new StringBuilder();
		selectQuery.append("SELECT * FROM cities ");

		// builder for COUNT(*) query
		StringBuilder countQuery = new StringBuilder();
		countQuery.append("SELECT COUNT(*) FROM cities ");

		// WHERE clause
		List<Object> args = new ArrayList<Object>();
		Set<Entry<String, String>> entries = filters.entrySet();
		List<String> whereQueries = new ArrayList<String>();
		for (Entry<String, String> entry : entries) {
			String field = getDbFieldName(entry.getKey());
			String operator = "LIKE";
			String rawValue = entry.getValue();
			Object value = rawValue + "%";

			whereQueries.add("LOWER(" + field + ") " + operator + " LOWER(?) ");
			args.add(value);
		}
		if (whereQueries.size() > 0) {
			String where = StringUtils.join(whereQueries, " AND ");
			selectQuery.append("WHERE " + where);
			countQuery.append("WHERE " + where);
		}

		// ORDER BY clause
		if (sortField != null) {
			String field = getDbFieldName(sortField);
			selectQuery.append("ORDER BY " + field + " ");
			if (order == SortOrder.DESCENDING) {
				selectQuery.append("DESC ");
			}
		}

		// LIMIT clause
		selectQuery.append("LIMIT " + first + ", " + pageSize);

		// query for data
		String sql = selectQuery.toString();
		List<Map<String, Object>> rows = getRows(sql, args);
		cities = new ArrayList<City>(pageSize);
		for (Map<String, Object> row : rows) {
			City city = new City();
			city.setId((Integer)row.get("id"));
			city.setName((String)row.get("name"));
			city.setAsciiName((String)row.get("ascii_name"));
			city.setLatitude((Integer)row.get("latitude"));
			city.setLongitude((Integer)row.get("longitude"));
			city.setTimeZone((String)row.get("time_zone"));
			city.setPopulation((Integer)row.get("population"));
			city.setProvince((String)row.get("province"));
			cities.add(city);
		}

		// query for data set size
		sql = countQuery.toString();
		setRowCount(getIntResult(sql, args));
		return cities;
	}

	private List<Map<String, Object>> getRows(String sql, List<Object> args) {
		try {
			List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
			PreparedStatement stmt = getConnection().prepareStatement(sql);
			for (int i = 0, size = args.size(); i < size; i++) {
				stmt.setObject(i + 1, args.get(i));
			}

			ResultSet resultSet = stmt.executeQuery();
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			while (resultSet.next()) {
				Map<String, Object> row = new HashMap<String, Object>();
				for (int i = 1; i <= columnCount; i++) {
					String name = metaData.getColumnName(i).toLowerCase();
					row.put(name, resultSet.getObject(i));
				}
				rows.add(row);
			}
			return rows;
		} catch (SQLException exception) {
			throw new RuntimeException(exception);
		}
	}

	private int getIntResult(String sql, List<Object> args) {
		try {
			PreparedStatement stmt = getConnection().prepareStatement(sql);
			for (int i = 0, size = args.size(); i < size; i++) {
				stmt.setObject(i + 1, args.get(i));
			}

			ResultSet resultSet = stmt.executeQuery();
			resultSet.next();
			return resultSet.getInt(1);
		} catch (SQLException exception) {
			throw new RuntimeException(exception);
		}
	}

	private Connection getConnection() {
		ServletContext servletContext = (ServletContext)FacesContext
			.getCurrentInstance().getExternalContext().getContext();
		return (Connection)servletContext.getAttribute("conn");
	}

	private String getDbFieldName(String propertyName) {
		String field = PROPERTY_FIELD_MAP.get(propertyName);
		if (field == null) {
			throw new RuntimeException("Unsupported property: " + propertyName);
		}
		return field;
	}

}
