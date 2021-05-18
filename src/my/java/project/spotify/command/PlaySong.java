package my.java.project.spotify.command;

public class PlaySong implements Runnable{
    private String songName;
    private boolean isPlaying;


    public PlaySong(String songName){

        this.songName = songName;
        isPlaying=true;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (isPlaying && (System.currentTimeMillis() - startTime) < 60000) {
            //simulating that song is playing
        }
        System.out.println("\n"+songName+" finished playing");
        songName="@Playing"+songName+"finished playing.";

    }

   public void stopSong(){
      isPlaying=false;

   }

    public String getSongName() {
        return songName;
    }
}
