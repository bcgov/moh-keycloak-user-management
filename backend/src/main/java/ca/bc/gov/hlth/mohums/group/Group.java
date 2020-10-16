package ca.bc.gov.hlth.mohums.group;

public class Group {
    private final String id;
    private final String name;
    private final String path;

    public Group(String id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
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
}
