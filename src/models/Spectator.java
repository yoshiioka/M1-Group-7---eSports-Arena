package models;

public class Spectator {
    private int spectatorId;
    private String fullName;
    private String email;
    private String password;
    private String createdAt;

    public Spectator() {}

    public Spectator(int spectatorId, String fullName, String email, String password, String createdAt) {
        this.spectatorId = spectatorId;
        this.fullName    = fullName;
        this.email       = email;
        this.password    = password;
        this.createdAt   = createdAt;
    }

    public int    getSpectatorId() { return spectatorId; }
    public String getFullName()    { return fullName;    }
    public String getEmail()       { return email;       }
    public String getPassword()    { return password;    }
    public String getCreatedAt()   { return createdAt;   }

    public void setSpectatorId(int spectatorId) { this.spectatorId = spectatorId; }
    public void setFullName(String fullName)     { this.fullName    = fullName;    }
    public void setEmail(String email)           { this.email       = email;       }
    public void setPassword(String password)     { this.password    = password;    }
    public void setCreatedAt(String createdAt)   { this.createdAt   = createdAt;   }

    @Override
    public String toString() {
        return "Spectator{id=" + spectatorId + ", name=" + fullName + ", email=" + email + "}";
    }
}
