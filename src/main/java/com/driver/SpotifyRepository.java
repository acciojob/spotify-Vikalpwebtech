package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap; //
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name,mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        //If the artist does not exist, first create an artist with given name
        //Create an album with given title and artist

        Artist artist = null;
        for (Artist ar:artists){
            if (ar.getName().equals(artistName)){
                artist = ar;
            }
        }
        if (artist == null){
            artist = new Artist(artistName);
        }

        //Now adding the album to list
        Album album = new Album(title);
        albums.add(album);

        if (artistAlbumMap.containsKey(artist)){
            List<Album> list = artistAlbumMap.get(artist);
            list.add(album);
            artistAlbumMap.put(artist,list);
        }
        else {
            List<Album> newlist = new ArrayList<>();
            newlist.add(album);
            artistAlbumMap.put(artist,newlist);
        }
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        //If the album does not exist in database, throw "Album does not exist" exception
        //Create and add the song to respective album

        Album album = null;

        for (Album al:albums){
            if (al.getTitle().equals(albumName)){
                album = al;
            }
        }
        if (album == null){
            throw new Exception("Album does not exist");
        }

        Song song = new Song(title,length);
        List<Song> songlist = albumSongMap.get(album);
        songlist.add(song);
        albumSongMap.put(album,songlist);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        //Create a playlist with given title and add all songs having the given length in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception

        User user = null;
        for (User us: users){
            if (us.getMobile().equals(mobile)){
                user = us;
            }
        }
        if (user == null){
            throw  new Exception("User does not exist");
        }

        Playlist playlist = new Playlist(title);
        List<Song> listofsongs = new ArrayList<>();

        for (Song song : songs){
            if (song.getLength() == length){
                listofsongs.add(song);
            }
        }
        if (playlistSongMap.containsKey(playlist)){
            List<Song> songl = playlistSongMap.get(playlist);
            for (Song s : listofsongs){
                songl.add(s);
            }
            playlistSongMap.put(playlist,songl);
        }
        else {
            playlistSongMap.put(playlist,listofsongs);
        }
        creatorPlaylistMap.put(user,playlist);
        List<User> userplay = playlistListenerMap.get(playlist);
        userplay.add(user);
        playlistListenerMap.put(playlist,userplay);
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        //Create a playlist with given title and add all songs having the given titles in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception

        User user = null;
        for (User us: users){
            if (us.getMobile().equals(mobile)){
                user = us;
            }
        }
        if (user == null) throw  new Exception("User does not exist");

        Playlist playlist1 = new Playlist(title);
        List<Song> listofsongsinpl = new ArrayList<>();
        for (Song song:songs){
            if (songTitles.contains(song.getTitle())){
                listofsongsinpl.add(song);
            }
        }
        playlistSongMap.put(playlist1,listofsongsinpl);
        creatorPlaylistMap.put(user,playlist1);
        List<User> userplay = playlistListenerMap.get(playlist1);
        userplay.add(user);
        playlistListenerMap.put(playlist1,userplay);
        return playlist1;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //Find the playlist with given title and add user as listener of that playlist and update user accordingly
        //If the user is creater or already a listener, do nothing
        //If the user does not exist, throw "User does not exist" exception
        //If the playlist does not exists, throw "Playlist does not exist" exception
        // Return the playlist after updating

        User user = null;
        for (User us: users){
            if (us.getMobile().equals(mobile)){
                user = us;
            }
        }
        if (user == null) throw  new Exception("User does not exist");

        Playlist playlist = null;
        for (Playlist pl :playlists){
            if (pl.getTitle().equals(playlistTitle)){
                playlist = pl;
            }
        }
        if (playlist == null) throw new Exception("Playlist does not exist");

        List<User> listofusers = playlistListenerMap.get(playlist);
        if (!listofusers.contains(user)){
            listofusers.add(user);
        }
        //Now needs to check if users is already creator for the given playlist then do nothing

        playlistListenerMap.put(playlist,listofusers);
        List<Playlist> listofpl = userPlaylistMap.get(user);
        listofpl.add(playlist);
        userPlaylistMap.put(user,listofpl);
        //hold
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        //The user likes the given song. The corresponding artist of the song gets auto-liked
        //A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
        //However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
        //If the user does not exist, throw "User does not exist" exception
        //If the song does not exist, throw "Song does not exist" exception
        //Return the song after updating
        User user = null;
        for (User us: users){
            if (us.getMobile().equals(mobile)){
                user = us;
            }
        }

        if (user == null) throw  new Exception("User does not exist");

        Song song = null;
        for (Song sg : songs){
            if (sg.getTitle().equals(songTitle)){
                song = sg;
            }
        }

        if (song == null) throw  new Exception("Song does not exist");

        List<User> userliked = songLikeMap.get(song);
        if (!userliked.contains(user)){
            userliked.add(user);
        }
        songLikeMap.put(song,userliked);
        int likes = songLikeMap.get(song).size();
        song.setLikes(likes);

        //Now also needs to increase the likes of the artist
        Album albumname = null;
        for (Album album : albumSongMap.keySet()){
            List<Song> songsfromalbum = albumSongMap.get(album);
            for (Song s : songsfromalbum){
                if (s.equals(song)){
                    albumname = album;
                }
            }
        }

        //Now needs to check whoose album was it
        Artist artisttogetlike = null;
        for (Artist ar : artistAlbumMap.keySet()){
            List<Album> allist = artistAlbumMap.get(ar);
            if (allist.contains(albumname)){
                artisttogetlike =ar;
            }
        }

        //Now need to increse the likes of that artist
        for (Artist artist:artists){
            if (artist.getName().equals(artisttogetlike.getName())){
                int currlikes = artist.getLikes();
                artist.setLikes(currlikes+1);
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        //Return the artist name with maximum likes
        int max = 0;
        String artistname = "";
        for (Artist artist:artists){
            if (artist.getLikes() > max){
                max = artist.getLikes();
                artistname = artist.getName();
            }
        }
        return artistname;
    }

    public String mostPopularSong() {
        int max = 0;
        String songname = "";
        for (Song song:songLikeMap.keySet()){
            if (song.getLikes() > max){
                max = song.getLikes();
                songname = song.getTitle();
            }
        }
        return songname;
    }

}
