package my.java.project.spotify.datasource;


import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DataSourceTest {
    DataSource dataSource;

    @Before
    public void makeInstance(){
        dataSource=new DataSource();
        dataSource.open();
    }

    @After
    public void close(){
        dataSource.close();
    }

    @Test
    public void querySongByArtistTest(){
        String message="Grateful Dead";
        List <Song> response = dataSource.querySongByArtist(message);

       List<Song> expectedResponse=new ArrayList<>();
       expectedResponse.add(new Song("Grateful Dead","Touch of Grey"));

        assertEquals(expectedResponse,response);
    }

    @Test
    public void querySongBySongTitleTest(){
        String message="Harry";
        List <Song> response = dataSource.querySongBySongTitle(message);

        List<Song> expectedResponse=new ArrayList<>();
        expectedResponse.add(new Song("Big Brother & The Holding Company","Harry"));

        assertEquals(expectedResponse,response);
    }

    @Test
    public void querySongByAlbumTitleTest(){
        String message="In The Dark";
        List <Song> response = dataSource.querySongByAlbumTitle(message);

        List<Song> expectedResponse=new ArrayList<>();
        expectedResponse.add(new Song("Grateful Dead","Touch of Grey"));

        assertEquals(expectedResponse,response);
    }



}
