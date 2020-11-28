package com.vertxlet.util;

import com.vertxlet.core.VertxletException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class Config {

    private static final String LIST_DELIMITER = ",";
    private static final String DEFAULT_RESOURCE_PATH = "vertxlet.properties";

    private final Properties props = new Properties();

    public Config() {
        this(Config.class.getClassLoader().getResourceAsStream(DEFAULT_RESOURCE_PATH));
    }

    public Config(String path) {
        try {
            // extra properties from file file will have higher priority
            addResource(path);
        } catch (IOException e) {
            throw new VertxletException(e);
        }
    }

    public Config(InputStream is) {
        try {
            addResource(is);
        } catch (IOException e) {
            throw new VertxletException(e);
        }
    }

    public void addResource(InputStream is, boolean closeAfterLoad) throws IOException {
        try {
            props.load(is);
        } finally {
            if (closeAfterLoad) is.close();
        }
    }

    public void addResource(InputStream is) throws IOException {
        this.addResource(is, false);
    }

    public void addResource(String path) throws IOException {
        if (path != null) {
            this.addResource(new FileInputStream(path), true);
        }
    }

    public boolean containsKey(String key) {
        return this.props.containsKey(key);
    }

    public <T> void set(String key, T val) {
        Objects.requireNonNull(val);
        props.setProperty(key, val.toString());
    }

    public void setCollection(String key, String... elements) {
        setCollection(key, Arrays.asList(elements));
    }

    public void setCollection(String key, Collection<String> coll) {
        props.setProperty(key, Strings.join(coll, LIST_DELIMITER));
    }

    public void setClass(String key, Class<?> cls) {
        props.setProperty(key, cls.getName());
    }

    public void setClasses(String key, Class<?>... classes) {
        setClasses(key, Arrays.asList(classes));
    }

    public void setClasses(String key, Collection<Class<?>> classes) {
        List<String> clsNames = new ArrayList<>(classes.size());
        for (Class<?> cls : classes) {
            clsNames.add(cls.getName());
        }
        setCollection(key, clsNames);
    }

    public String getString(String key) {
        String val = getProperty(key);
        if (Strings.isNullOrEmpty(val)) {
            throw new VertxletException(new NoSuchElementException());
        }
        return val;
    }

    public String getString(String key, String defVal) {
        return getProperty(key, defVal);
    }

    public String[] getStringArray(String key) {
        return getString(key).split(",");
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (Exception e) {
            throw new VertxletException(e);
        }
    }

    public int getInt(String key, int defVal) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (Exception ignored) {
            return defVal;
        }
    }

    public long getLong(String key) {
        try {
            return Long.parseLong(getProperty(key));
        } catch (Exception e) {
            throw new VertxletException(e);
        }
    }

    public long getLong(String key, long defVal) {
        try {
            return Long.parseLong(getProperty(key));
        } catch (Exception ignored) {
            return defVal;
        }
    }

    public double getDouble(String key) {
        try {
            return Double.parseDouble(getProperty(key));
        } catch (Exception e) {
            throw  new VertxletException(e);
        }
    }

    public double getDouble(String key, double defVal) {
        try {
            return Double.parseDouble(getProperty(key));
        } catch (Exception ignored) {
            return defVal;
        }
    }

    public float getFloat(String key) {
        try {
            return Float.parseFloat(getProperty(key));
        } catch (Exception e) {
            throw new VertxletException(e);
        }
    }

    public float getFloat(String key, float defVal) {
        try {
            return Float.parseFloat(getProperty(key));
        } catch (Exception ignored) {
            return defVal;
        }
    }

    public boolean getBool(String key) {
        try {
            return Boolean.parseBoolean(getProperty(key));
        } catch (Exception e) {
            throw new VertxletException(e);
        }
    }

    public boolean getBool(String key, boolean defVal) {
        try {
            return Boolean.parseBoolean(getProperty(key));
        } catch (Exception ignored) {
            return defVal;
        }
    }

    public List<String> getList(String key) {
        try {
            return Arrays.asList(getProperty(key).split(","));
        } catch (Exception e) {
            throw new VertxletException(e);
        }
    }

    public Class<?> getClass(String key, Class<?> defVal) {
        try {
            return Class.forName(getProperty(key));
        } catch (Exception ignored) {
            return defVal;
        }
    }

    public Class<?> getClass(String key) {
        return getClass(key, null);
    }

    public Collection<Class<?>> getClasses(String key) throws ClassNotFoundException {
        Collection<String> clsNames = getList(key);
        List<Class<?>> classes = new ArrayList<>(clsNames.size());
        for (String className : getList(key)) {
            classes.add(Class.forName(className));
        }
        return classes;
    }

    public Properties toProperties() {
        return new Properties(props);
    }

    public Set<String> getKeys() {
        Set<String> keys = new HashSet<>();
        for (Object key : this.props.keySet()) {
            keys.add(key.toString());
        }
        return keys;
    }

    @Override
    public String toString() {
        Set<Object> keySet = new TreeSet<>(props.keySet());
        List<Object> keys = new ArrayList<>(keySet);
        keys.sort(Comparator.comparing(Object::toString));

        StringBuilder sb = new StringBuilder("Configuration properties:\n");
        keys.forEach(key -> sb.append(Strings.format("\t%s = %s\n",
                key.toString(), getString(key.toString()))));
        return sb.toString();
    }

    protected String getProperty(String key) {
        return props.getProperty(key);
    }

    protected String getProperty(String key, String defVal) {
        return props.getProperty(key, defVal);
    }
}
