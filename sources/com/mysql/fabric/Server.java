package com.mysql.fabric;

public class Server implements Comparable<Server> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private String groupName;
    private String hostname;
    private ServerMode mode;
    private int port;
    private ServerRole role;
    private String uuid;
    private double weight;

    public Server(String groupName, String uuid, String hostname, int port, ServerMode mode, ServerRole role, double weight) {
        this.groupName = groupName;
        this.uuid = uuid;
        this.hostname = hostname;
        this.port = port;
        this.mode = mode;
        this.role = role;
        this.weight = weight;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getHostname() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    public ServerMode getMode() {
        return this.mode;
    }

    public ServerRole getRole() {
        return this.role;
    }

    public double getWeight() {
        return this.weight;
    }

    public String getHostPortString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.hostname);
        stringBuilder.append(":");
        stringBuilder.append(this.port);
        return stringBuilder.toString();
    }

    public boolean isMaster() {
        return this.role == ServerRole.PRIMARY;
    }

    public boolean isSlave() {
        if (this.role != ServerRole.SECONDARY) {
            if (this.role != ServerRole.SPARE) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return String.format("Server[%s, %s:%d, %s, %s, weight=%s]", new Object[]{this.uuid, this.hostname, Integer.valueOf(this.port), this.mode, this.role, Double.valueOf(this.weight)});
    }

    public boolean equals(Object o) {
        if (o instanceof Server) {
            return ((Server) o).getUuid().equals(getUuid());
        }
        return false;
    }

    public int hashCode() {
        return getUuid().hashCode();
    }

    public int compareTo(Server other) {
        return getUuid().compareTo(other.getUuid());
    }
}
