package ca.bc.gov.hlth.mohums.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class Group {

    private final String id;
    private final String name;
    private final String path;
    @JsonIgnore
    private final List<String> umsRoles;
    private final String description;

    public Group(String id, String name, String path, List<String> umsRoles, String description) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.umsRoles = umsRoles;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public List<String> getUmsRoles() {
        return umsRoles;
    }

    public String getDescription() {
        return description;
    }
}
