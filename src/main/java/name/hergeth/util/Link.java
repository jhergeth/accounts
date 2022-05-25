package name.hergeth.util;

public class Link {
    String source;
    String target;
    String description;

    public Link(String source, String target, String description) {
        this.source = source;
        this.target = target;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Link{" +
                "source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
