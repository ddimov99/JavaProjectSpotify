package my.java.project.spotify.command;

        import my.java.project.spotify.exceptionlogger.ErrorLogger;
        import my.java.project.spotify.user.User;
        import org.junit.After;
        import org.junit.Before;
        import org.junit.Test;

        import java.io.*;
        import java.nio.file.Files;
        import java.nio.file.Path;
        import java.util.ArrayList;
        import java.util.List;

        import static org.junit.Assert.assertEquals;

public class CommandTest {
    private Command commandExecutor;
    private int dummyScHashCode;

    @Before
    public void makeInstance(){
        commandExecutor=new Command();
        dummyScHashCode=777;
    }

    @After
    public void deleteTestData() {
        String message="logout";
        commandExecutor.execute(message,dummyScHashCode);
        List<User> filteredUsers=new ArrayList<>();
        List<String> filteredPlaylists=new ArrayList<>();
        Path usersFilePath = Path.of("users.txt");
        if (Files.exists(usersFilePath)) {
            try (BufferedReader br = Files.newBufferedReader(usersFilePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if(line.contains("test")){
                        continue;
                    }
                    int whitespaceIndex = line.indexOf(" ");
                    String email = line.substring(0, whitespaceIndex);
                    String password = line.substring(whitespaceIndex + 1);
                    User user = new User(email, password);
                    filteredUsers.add(user);
                }
            } catch (IOException e) {
                throw new RuntimeException("There is a problem with the users file", e);
            }
        }


        try (FileWriter fileWriter = new FileWriter("users.txt" , false);
             PrintWriter writer = new PrintWriter(fileWriter, true)) {
            for(User user :filteredUsers) {
                writer.println(user.email() + " " + user.password());
            }
        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new IllegalStateException("A problem occurred while writing to the users file", e);
        }


        Path playlistsFilePath = Path.of("playlists.txt");
        if (Files.exists(playlistsFilePath)) {
            try (BufferedReader br = Files.newBufferedReader(playlistsFilePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if(line.contains("test")){
                        continue;
                    }
                   filteredPlaylists.add(line);
                }
            } catch (IOException e) {
                throw new RuntimeException("There is a problem with the users file", e);
            }
        }

        try (FileWriter fileWriter = new FileWriter("playlists.txt" , false);
             PrintWriter writer = new PrintWriter(fileWriter, true)) {
            for(String s : filteredPlaylists){
                writer.println(s);
            }
        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new IllegalStateException("A problem occurred while writing to the users file", e);
        }

    }
    @Test
    public void executeHelpTest(){
        String message = "help";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse = """
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
                """+System.lineSeparator();

        assertEquals("'help' must return right result. ", expectedResponse,response);

    }

    @Test
    public void executeRegisterInvalidCommand(){
        String message="register testmisho@abv.bg";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Invalid command"+System.lineSeparator();

        assertEquals("'register' must return 'Invalid command' ", expectedResponse,response);
    }

   @Test
    public void executeRegisterAlreadyLoggedIn(){
        String message= "register testuser4@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testuser4@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="register testuser2@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="You have to first logout before making new account."+System.lineSeparator();

        assertEquals("'register' must return logout first ", expectedResponse,response);
    }

    @Test
    public void executeRegisterInvalidEmail(){
        String message="register test@g 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Email test@g is invalid, select a valid one."+System.lineSeparator();

        assertEquals("'register' must return 'Invalid email' ", expectedResponse,response);
    }

    @Test
    public void executeRegisterTakenEmail(){
        String message= "register testuser@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="register testuser@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse =" Email testuser@abv.bg is already taken, select another one."+System.lineSeparator();

        assertEquals("'register' must return 'Email already taken' ", expectedResponse,response);
    }

    @Test
    public void executeRegisterSuccessfulRegister(){
        String message="register testmisho@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse =" User with email testmisho@abv.bg successfully registered."+System.lineSeparator();

        assertEquals("'register' must be successful ", expectedResponse,response);
    }

    @Test
    public void executeLoginAlreadyLoggedIn(){
        String message= "register testuser@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testuser@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testuser@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="You are already logged"+System.lineSeparator();

        assertEquals("'login' must fail ", expectedResponse,response);
   }

    @Test
    public void executeLoginInvalidCommand(){
        String message="login test@abv.bg";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Invalid command"+System.lineSeparator();

        assertEquals("'login' must fail ", expectedResponse,response);
    }

    @Test
    public void executeLoginOtherUserAlreadyLogged(){
        int otherUserHash=666;
        String message= "register testuser@abv.bg 1";
        commandExecutor.execute(message,otherUserHash);
        message="login testuser@abv.bg 1";
        commandExecutor.execute(message,otherUserHash);
        message="login testuser@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Other user is logged in ,in the account"+System.lineSeparator();

        assertEquals("'login' must fail ", expectedResponse,response);
    }

    @Test
    public void executeLoginEmailNotFound(){
        String message="login testNotExisting@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Invalid email"+System.lineSeparator();

        assertEquals("'login' must fail ", expectedResponse,response);
    }

    @Test
    public void executeLoginWrongPassword(){
        String message= "register testuser5@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testuser5@abv.bg wrong";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Invalid password"+System.lineSeparator();

        assertEquals("'login' must fail ", expectedResponse,response);
    }

    @Test
    public void executeSearchSongByTitleFound(){
        String message="searchBySongTitle Harry";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="[Song[artist=Big Brother & The Holding Company, songName=Harry]]"+System.lineSeparator();

        assertEquals("'searchBySongTitle' must return song ", expectedResponse,response);
    }

    @Test
    public void executeSearchSongByTitleNotFound(){
        String message="searchBySongTitle HarryPotter";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="No songs were found."+System.lineSeparator();

        assertEquals("'searchBySongTitle' must not find any songs with this name ", expectedResponse,response);
    }


    @Test
    public void executeSearchSongByArtistNotFound(){
        String message="searchByArtistName HarryPotter";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="No songs were found."+System.lineSeparator();

        assertEquals("'searchByArtistName' must not find any songs with this name ", expectedResponse,response);
    }


    @Test
    public void executeSearchSongByAlbumNotFound(){
        String message="searchByAlbumTitle HarryPotter";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="No songs were found."+System.lineSeparator();

        assertEquals("'searchByAlbumTitle' must not find any songs with this title ", expectedResponse,response);
    }

    @Test
    public void executePlaySongNotFound(){
        String message="play DUMMYSONG";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="No songs with this name were found."+System.lineSeparator();

        assertEquals("'play' must fail ", expectedResponse,response);
    }

    @Test
    public void executePlayAnotherSongPlaying(){
        String message="play Harry";
        commandExecutor.execute(message,dummyScHashCode);
        message="play Harry";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Another song is currently playing"+System.lineSeparator();

        assertEquals("'play' must fail ", expectedResponse,response);
    }

    @Test
    public void executeStopSuccessful(){
        String message="searchByAlbumTitle HarryPotter";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="No songs were found."+System.lineSeparator();

        assertEquals("'searchByAlbumTitle' must not find any songs with this title ", expectedResponse,response);
    }

    @Test
    public void executeStopNothingToStop(){
        String message="stop";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="There isn't a song that is playing"+System.lineSeparator();

        assertEquals("'stop' must not find any songs to stop ", expectedResponse,response);
    }

    @Test
    public void executeStopSongSuccessful(){
        String message="play Harry";
        commandExecutor.execute(message,dummyScHashCode);
        message="stop";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Song stopped successfully"+System.lineSeparator();

        assertEquals("'stop' must succeed ", expectedResponse,response);
    }

    @Test
    public void executeListTopSongs(){
        String message="play Harry";
        commandExecutor.execute(message,dummyScHashCode);
        message="stop";
        commandExecutor.execute(message,dummyScHashCode);
        message="play Never Gave Up";
        commandExecutor.execute(message,dummyScHashCode);
        message="stop";
        commandExecutor.execute(message,dummyScHashCode);
        message="play Never Gave Up";
        commandExecutor.execute(message,dummyScHashCode);
        message="stop";
        commandExecutor.execute(message,dummyScHashCode);
        message="top";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Never Gave Up || Times played:2\n" +
                "Harry || Times played:1\n"+System.lineSeparator();

        assertEquals("'top' must succeed ", expectedResponse,response);
    }

    @Test
    public void executeCreatePlayListNotLoggedIn(){
        String message="create-playlist testPlaylist";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="You are not logged in"+System.lineSeparator();

        assertEquals("'create-playlist' must fail ", expectedResponse,response);
    }

    @Test
    public void executeCreatePlayListAlreadyExisting(){
        String message="register testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="create-playlist testPlaylist";
        commandExecutor.execute(message,dummyScHashCode);
        message="create-playlist testPlaylist";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Playlist name testPlaylist is already taken, select another one "+System.lineSeparator();

        assertEquals("'create-playlist' must fail ", expectedResponse,response);
    }


    @Test
    public void executeCreatePlayListSuccessful(){
        String message="register testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="create-playlist testPlaylist";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Playlist testPlaylist successfully created "+System.lineSeparator();

        assertEquals("'create-playlist' must succeed ", expectedResponse,response);
    }

    @Test
    public void executeAddSongToNotLoggedIn(){
        String message="add-song-to testplaylist@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="You are not logged in"+System.lineSeparator();

        assertEquals("'add-song-to' must fail ", expectedResponse,response);
    }



    @Test
    public void executeAddSongToNoPlayListFound(){
        String message="register testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="add-song-to testPlaylist 1";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Playlist with name testPlaylist doesn't exist "+System.lineSeparator();

        assertEquals("'add-song-to' must fail ", expectedResponse,response);
    }

    @Test
    public void executeAddSongToSongAlreadyAdded(){
        String message="register testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="create-playlist testPlaylist";
        commandExecutor.execute(message,dummyScHashCode);
        message="add-song-to testPlaylist Harry";
        commandExecutor.execute(message,dummyScHashCode);
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse =" Song Harry is already in playlist testPlaylist "+System.lineSeparator();

        assertEquals("'add-song-to' must fail ", expectedResponse,response);
    }

    @Test
    public void executeAddSongToSongNotFound(){
        String message="register testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="create-playlist testPlaylist";
        commandExecutor.execute(message,dummyScHashCode);
        message="add-song-to testPlaylist 1";
        commandExecutor.execute(message,dummyScHashCode);
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Song with this name not found"+System.lineSeparator();

        assertEquals("'add-song-to' must fail ", expectedResponse,response);
    }

    @Test
    public void executeAddSongToSuccessful(){
        String message="register testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testplaylist@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="create-playlist testPlaylist";
        commandExecutor.execute(message,dummyScHashCode);
        message="add-song-to testPlaylist Harry";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Song added to playlist"+System.lineSeparator();

        assertEquals("'add-song-to' must succeed ", expectedResponse,response);
    }

    @Test
    public void executeDisconnect(){
        String message="disconnect";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Disconnected successfully"+System.lineSeparator();

        assertEquals("'disconnect' must succeed ", expectedResponse,response);
    }

}
