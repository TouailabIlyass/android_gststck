package com.mysql.fabric;

import java.util.Set;

public class ServerGroup {
    private String name;
    private Set<Server> servers;

    public ServerGroup(String name, Set<Server> servers) {
        this.name = name;
        this.servers = servers;
    }

    public String getName() {
        return this.name;
    }

    public Set<Server> getServers() {
        return this.servers;
    }

    public Server getMaster() {
        for (Server s : this.servers) {
            if (s.getRole() == ServerRole.PRIMARY) {
                return s;
            }
        }
        return null;
    }

    public Server getServer(String hostPortString) {
        for (Server s : this.servers) {
            if (s.getHostPortString().equals(hostPortString)) {
                return s;
            }
        }
        return null;
    }

    public String toString() {
        return String.format("Group[name=%s, servers=%s]", new Object[]{this.name, this.servers});
    }
}
