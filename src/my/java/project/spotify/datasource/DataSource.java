package my.java.project.spotify.datasource;

import my.java.project.spotify.exceptionlogger.ErrorLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataSource {

    public static final String DB_NAME = "music.db";

    public static final String CONNECTION_STRING = "jdbc:sqlite:C:\\Users\\HOME\\IdeaProjects\\JavaProjectSpotify\\"
            + DB_NAME;

    private static final String TABLE_ALBUMS = "albums";
    private static final String COLUMN_ALBUM_ID = "_id";
    private static final String COLUMN_ALBUM_ARTIST = "artist";
    private static final String COLUMN_ALBUM_NAME = "name";


    private static final String TABLE_ARTISTS = "artists";
    private static final String COLUMN_ARTIST_ID = "_id";
    private static final String COLUMN_ARTIST_NAME = "name";


    private static final String TABLE_SONGS = "songs";
    private static final String COLUMN_SONG_TITLE = "title";
    private static final String COLUMN_SONG_ALBUM = "album";

    private static final String QUERY_GET_SONGS_BY_SONG_TITLE = "SELECT " + TABLE_SONGS + "." + COLUMN_SONG_TITLE + ", " + TABLE_ARTISTS +
            "." + COLUMN_ARTIST_NAME + " FROM " + TABLE_SONGS + " INNER JOIN " + TABLE_ALBUMS + " ON " +
            TABLE_ALBUMS + "." + COLUMN_ALBUM_ID + "=" + TABLE_SONGS + "." + COLUMN_SONG_ALBUM + " INNER JOIN " +
            TABLE_ARTISTS + " ON " + TABLE_ARTISTS + "." + COLUMN_ARTIST_ID + "=" + TABLE_ALBUMS + "." +
            COLUMN_ALBUM_ARTIST + " WHERE " + TABLE_SONGS + "." + COLUMN_SONG_TITLE + " = ?";


    public static final String QUERY_GET_SONGS_BY_ALBUM = "SELECT " + TABLE_SONGS + "." + COLUMN_SONG_TITLE + ", " + TABLE_ARTISTS +
            "." + COLUMN_ARTIST_NAME + " FROM " + TABLE_SONGS + " INNER JOIN " + TABLE_ALBUMS + " ON " +
            TABLE_ALBUMS + "." + COLUMN_ALBUM_ID + "=" + TABLE_SONGS + "." + COLUMN_SONG_ALBUM + " INNER JOIN " +
            TABLE_ARTISTS + " ON " + TABLE_ARTISTS + "." + COLUMN_ARTIST_ID + "=" + TABLE_ALBUMS + "." +
            COLUMN_ALBUM_ARTIST + " WHERE " + TABLE_ALBUMS + "." + COLUMN_ALBUM_NAME + " = ?";

    public static final String QUERY_SONGS_BY_ARTIST = "SELECT " + TABLE_SONGS + "." + COLUMN_SONG_TITLE + ","
            + TABLE_ARTISTS + "." + COLUMN_ARTIST_NAME + " FROM " + TABLE_SONGS +
            " INNER JOIN " + TABLE_ALBUMS + " ON " + TABLE_SONGS + "." + COLUMN_SONG_ALBUM + "=" + TABLE_ALBUMS +
            "." + COLUMN_ALBUM_ID + " INNER JOIN " + TABLE_ARTISTS + " ON " + TABLE_ALBUMS + "." + COLUMN_ALBUM_ARTIST
            + "=" + TABLE_ARTISTS + "." + COLUMN_ARTIST_ID + " WHERE " + TABLE_ARTISTS + "." + COLUMN_ARTIST_NAME + " = ?";

    private Connection conn;

    private PreparedStatement querySongsBySongTitle;
    private PreparedStatement querySongsByArtistName;
    private PreparedStatement querySongsByAlbumTitle;

    public void open() {
        try {
            conn = DriverManager.getConnection(CONNECTION_STRING);
            querySongsBySongTitle = conn.prepareStatement(QUERY_GET_SONGS_BY_SONG_TITLE);
            querySongsByArtistName = conn.prepareStatement(QUERY_SONGS_BY_ARTIST);
            querySongsByAlbumTitle = conn.prepareStatement(QUERY_GET_SONGS_BY_ALBUM);


        } catch (SQLException e) {
            ErrorLogger.logClientError(e);
            throw new RuntimeException("There is a problem with the database", e);
        }
    }

    public void close() {
        try {
            if (querySongsBySongTitle != null) {
                querySongsBySongTitle.close();
            }
            if (querySongsByArtistName != null) {
                querySongsByArtistName.close();
            }
            if (querySongsByAlbumTitle != null) {
                querySongsByAlbumTitle.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            ErrorLogger.logClientError(e);
            throw new RuntimeException("There is a problem with the database", e);
        }
    }


    public List<Song> querySongByArtist(String name) {
        try {
            querySongsByArtistName.setString(1, name);
            ResultSet results = querySongsByArtistName.executeQuery();

            return ListData(results);

        } catch (SQLException e) {
            ErrorLogger.logClientError(e);
            throw new RuntimeException("There is a problem with the database", e);
        }
    }

    public List<Song> querySongBySongTitle(String title) {
        try {
            querySongsBySongTitle.setString(1, title);
            ResultSet results = querySongsBySongTitle.executeQuery();

            return ListData(results);

        } catch (SQLException e) {
            ErrorLogger.logClientError(e);
            throw new RuntimeException("There is a problem with the database", e);
        }
    }

    public List<Song> querySongByAlbumTitle(String title) {
        try {
            querySongsByAlbumTitle.setString(1, title);
            ResultSet results = querySongsByAlbumTitle.executeQuery();

            return ListData(results);

        } catch (SQLException e) {
            ErrorLogger.logClientError(e);
            throw new RuntimeException("There is a problem with the database", e);
        }
    }

    private List<Song> ListData(ResultSet results) throws SQLException {
        List<Song> songs = new ArrayList<>();
        while (results.next()) {
            String songTitle = results.getString(1);
            String artistName = results.getString(2);
            songs.add(new Song(artistName, songTitle));

        }
        return songs;
    }

}


