package my.java.project.spotify.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserCollection {
    private static UserCollection instance=null;
    private static Set<User> registeredUsers;
    private static Map<Integer,User> loggedUsers;

    public static UserCollection getInstance() {
        if(instance==null) {
            instance=new UserCollection();
            registeredUsers=new HashSet<>();
            loggedUsers=new HashMap<>();
        }

        return instance;
    }

    public boolean addRegisteredUser(User user) {
     return registeredUsers.add(user);
    }

    public User getRegisteredUser(String email) {
        for(User u:registeredUsers) {
            if(u.email().equals(email)){
                return u;
            }
        }
        return null;
    }

    public void addLoggedUser(int scHash,User user) {
       if(!loggedUsers.containsValue(user)){
           loggedUsers.put(scHash,user);
       }
    }

    public boolean removeLoggedUser(int scHash){
       if(loggedUsers.containsKey(scHash)){
           loggedUsers.remove(scHash);
           return true;
        }
        return false;
    }

    public boolean checkIfAlreadyLogged(User user){
        for(User users : loggedUsers.values()){
            if(user.email().equals(users.email())){
                return false;
            }
        }
        return true;
    }

    public boolean checkIfLogged(int scHash){
        return loggedUsers.containsKey(scHash);
    }


}
