package my.java.project.spotify.command;

import my.java.project.spotify.datasource.DataSource;
import my.java.project.spotify.datasource.Song;
import my.java.project.spotify.exceptionlogger.ErrorLogger;
import my.java.project.spotify.user.User;
import my.java.project.spotify.user.UserCollection;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Command {
    private static final String USERS_FILE = "users.txt";
    private static final String PLAYLISTS_FILE = "playlists.txt";

    private final DataSource dataSource;

    private final Map<Integer, String> nowPlaying;
    private static UserCollection users;
    private static Map<Song, Integer> topSongs;

    public Command() {
        users = UserCollection.getInstance();
        topSongs = new HashMap<>();
        dataSource = new DataSource();
        dataSource.open();
        nowPlaying = new HashMap<>();


        Path usersFilePath = Path.of(USERS_FILE);
        if (Files.exists(usersFilePath)) {
            try (BufferedReader br = Files.newBufferedReader(usersFilePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    int whitespaceIndex = line.indexOf(" ");
                    String email = line.substring(0, whitespaceIndex);
                    String password = line.substring(whitespaceIndex + 1);
                    User user = new User(email, password);
                    users.addRegisteredUser(user);
                }
            } catch (IOException e) {
                ErrorLogger.logClientError(e);
                throw new RuntimeException("There is a problem with the users file", e);
            }
        }
    }


    public String execute(String message, int scHash) {
        String response;
        List<Song> songs;
        if (message.equals("help")) {
            response = listCommands();
        }else if (message.startsWith("register")) {
            response = register(message, scHash);
        } else if (message.startsWith("login")) {
            response = login(message, scHash);
        } else if (message.startsWith("searchBySongTitle ")) {
            songs = searchSongsBySongTitle(message);
            response = ListSongs(songs);
        } else if (message.startsWith("searchByArtistName ")) {
            songs = searchSongsByArtistName(message);
            response = ListSongs(songs);
        } else if (message.startsWith("searchByAlbumTitle ")) {
            songs = searchSongsByAlbumTitle(message);
            response = ListSongs(songs);
        } else if (message.startsWith("play ")) {
            response = playSong(message, scHash);
        } else if (message.equals("stop")) {
            response = stopSong(scHash);
        } else if (message.equals("top")) {
            response = listTopSongs();
        } else if (message.startsWith("create-playlist ")) {
            response = createPlaylist(message, scHash);
        } else if (message.startsWith("add-song-to ")) {
            response = addSong(message, scHash);
        } else if (message.startsWith("show-playlist ")) {
            response = getPlaylist(message);
        } else if (message.equals("logout")) {
            response = logout(scHash);
        } else if (message.equals("disconnect")) {
            response = disconnect(scHash);
        } else {
            response = "Invalid command.";
        }

        return response + System.lineSeparator();
    }

    private String listCommands(){
        return """
                Available commands:
                register <email> <password>
                login <email> <password>
                searchBySongTitle <words>
                searchByArtistName <words>
                searchByAlbumTitle <words>
                play <song>
                stop
                top 
                create-playlist <name_of_the_playlist>
                add-song-to <name_of_the_playlist> <song>
                show-playlist <name_of_the_playlist>
                logout
                disconnect
                """;
    }

    private String register(String message, int scHash) {

        if (checkIfLogged(scHash)) {
            return "You have to first logout before making new account.";
        }

        if (validateCommand(message)!=3) {
            return "Invalid command";
        }

        User user = getNewUser(message);
        String email = user.email();
        String password = user.password();

        if (invalidEmail(email)) {
            return "Email " + email + " is invalid, select a valid one.";
        }

        if (!users.addRegisteredUser(user)) {
           return" Email " + email + " is already taken, select another one.";
        }

            try (FileWriter fileWriter = new FileWriter(USERS_FILE, true);
                 PrintWriter writer = new PrintWriter(fileWriter, true)) {
                 writer.println(email + " " + password);
            } catch (IOException e) {
                ErrorLogger.logClientError(e);
                throw new IllegalStateException("A problem occurred while writing to the users file", e);
            }

            users.addRegisteredUser(user);
          return " User with email " + email + " successfully registered.";
        }

        private String login(String message, int scHash) {
        if (checkIfLogged(scHash)) {
            return "You are already logged";
        }

        if (validateCommand(message)!=3) {
                return "Invalid command";
        }

        User user = getNewUser(message);
        User existingUser;

        if (!users.addRegisteredUser(user)) {
            existingUser = users.getRegisteredUser(user.email());
            if (!users.checkIfAlreadyLogged(existingUser)) {
                return "Other user is logged in ,in the account";
            }
        } else {
            return "Invalid email";
        }
            if (existingUser.password().equals(user.password())) {
                users.addLoggedUser(scHash, user);
                return " User with email " + user.email() + " successfully logged in ";
            } else {
                return "Invalid password";
            }
    }

    private List<Song> searchSongsBySongTitle(String message) {

        String parameter = getParameter(message);

        return dataSource.querySongBySongTitle(parameter);
    }

    private List<Song> searchSongsByArtistName(String message) {
        String parameter = getParameter(message);

        return dataSource.querySongByArtist(parameter);
    }

    private List<Song> searchSongsByAlbumTitle(String message) {
        String parameter = getParameter(message);

        return dataSource.querySongByAlbumTitle(parameter);
    }

    private String playSong(String message, int scHash) {
        if (nowPlaying.containsKey(scHash)) {
            return "Another song is currently playing";
        }
        List<Song> foundSongs = searchSongsBySongTitle(message);

        if (foundSongs.isEmpty()) {
            return "No songs with this name were found.";
        }

        Song onlySongFound = foundSongs.get(0);

        nowPlaying.put(scHash, onlySongFound.songName());
        int cnt = topSongs.getOrDefault(onlySongFound, 0);
        topSongs.put(onlySongFound, cnt + 1);

        return "Script" + onlySongFound.songName();

    }


    private String stopSong(int scHash) {
       if(nowPlaying.containsKey(scHash)) {
           nowPlaying.remove(scHash);
           return "Song stopped successfully";
       }
       return "There isn't a song that is playing";
    }

    private String listTopSongs() {
        Map<Song, Integer> sorted =
                topSongs.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(10)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        StringBuilder sBr=new StringBuilder();

        for (Map.Entry<Song,Integer> entry : sorted.entrySet()) {
            System.out.println("Key = " + entry.getKey() +
                    ", Value = " + entry.getValue());
            sBr.append(entry.getKey().songName()).append(" || Times played:").append(entry.getValue()).append("\n");
        }
        return sBr.toString();
    }

    private String createPlaylist(String message, int scHash) {
        if (!checkIfLogged(scHash)) {
            return "You are not logged in";
        }

        String playlistName = getParameter(message);

        Path playlistsFilePath = Path.of(PLAYLISTS_FILE);

        if (Files.exists(playlistsFilePath)) {
            try (BufferedReader br = Files.newBufferedReader(playlistsFilePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(playlistName)) {
                        return "Playlist name " + playlistName + " is already taken, select another one ";
                    }
                }
            } catch (IOException e) {
                ErrorLogger.logClientError(e);
                throw new RuntimeException("There is a problem with the playlists file", e);
            }
        }

        try (FileWriter fileWriter = new FileWriter(PLAYLISTS_FILE, true);
             PrintWriter writer = new PrintWriter(fileWriter, true)) {
            writer.println(playlistName);
        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }

        return "Playlist " + playlistName + " successfully created ";
    }

    private String addSong(String message, int scHash) {
        if (!checkIfLogged(scHash)) {
            return "You are not logged in";
        }

        int firstWordSplitter = message.indexOf(" ");
        int SecondWordSplitter = message.substring(firstWordSplitter + 1).indexOf(" ")
                + firstWordSplitter + 1;

        String playlistName = message.substring(firstWordSplitter + 1, SecondWordSplitter).strip();
        String songName = message.substring(SecondWordSplitter + 1).strip();

        String playlist = getPlaylist(playlistName);

        if (playlist.equals("wrong name")) {
            return "Playlist with name " + playlistName + " doesn't exist ";
        }

        if (playlist.contains(songName)) {
            return " Song " + songName + " is already in playlist " + playlistName + " ";
        }

        List<Song> songs = dataSource.querySongBySongTitle(songName);

        if (songs.isEmpty()) {
            return "Song with this name not found";
        }

        String updatedPlaylist
                = playlist.contains("::") ? playlist + songName + ";" : playlist + " ::" + songName + ";";

        int playlistLineIndex = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(PLAYLISTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(playlistName)) {
                    break;
                }
                playlistLineIndex++;
            }
        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new RuntimeException("There is a problem with the playlists file", e);
        }

        Path playlistFilePath = Path.of(PLAYLISTS_FILE);
        try {
            List<String> lines = Files.readAllLines(playlistFilePath, StandardCharsets.UTF_8);
            lines.set(playlistLineIndex, updatedPlaylist);
            Files.write(playlistFilePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }


        return "Song added to playlist";
    }


    private String getPlaylist(String message) {
        String playlistName = getParameter(message);

        Path playlistsFilePath = Path.of(PLAYLISTS_FILE);
        if (Files.exists(playlistsFilePath)) {
            try (BufferedReader br = Files.newBufferedReader(playlistsFilePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(playlistName)) {
                        return line;
                    }
                }
                return "wrong name";
            } catch (IOException e) {
                ErrorLogger.logClientError(e);
                throw new RuntimeException("There is a problem with the playlists file", e);
            }
        }

        return "";
    }

    private String logout(int scHash) {
        if (!checkIfLogged(scHash)) {
            return "You are not logged in";
        }
        if (users.removeLoggedUser(scHash)) {
            return "You successfully logged out. ";
        }
        return "There's a problem with logging out.";
    }

    private String disconnect(int scHash) {
        users.removeLoggedUser(scHash);
        dataSource.close();
        return "Disconnected successfully";
    }

    private String ListSongs(List<Song> songs) {
        if (songs.isEmpty()) {
            return "No songs were found.";
        }
        return songs.toString();
    }

    private String getParameter(String s) {
        int firstWordSplitter = s.indexOf(" ");

        return s.substring(firstWordSplitter + 1).strip();
    }

    private boolean invalidEmail(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        return !email.matches(regex);
    }

    private User getNewUser(String s) {
        int firstWordSplitter = s.indexOf(" ");
        int SecondWordSplitter = s.substring(firstWordSplitter + 1).indexOf(" ")
                + firstWordSplitter + 1;

        String email = s.substring(firstWordSplitter + 1, SecondWordSplitter).strip();
        String password = s.substring(SecondWordSplitter + 1).strip();

        return new User(email, password);
    }

    private int validateCommand(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }

        String[] words = message.split(" ");
        return words.length;
    }

    private boolean checkIfLogged(int scHash) {
        return users.checkIfLogged(scHash);
    }

}
