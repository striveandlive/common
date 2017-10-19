package ua.common;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class WritableHttpServletRequest extends HttpServletRequestWrapper {
    private final Set<String> names = new HashSet<String>();
    private final Map<String, String> headers = new HashMap<String, String>();
    public WritableHttpServletRequest(HttpServletRequest request) {
        super(request);
        Enumeration<String> e = super.getHeaderNames();
        while (e.hasMoreElements()) {
            names.add(e.nextElement());
        }
    }
    public void addHeader(String name, String value) {
        headers.put(name, value);
        names.add(name);
    }
    @Override
    public String getHeader(String name) {
        if (headers.containsKey(name)) {
            return headers.get(name);
        } else {
            return super.getHeader(name);
        }
    }
    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(names);
    }
}