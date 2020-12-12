package com.example.music;

public class Song {
    /**
     * 歌手
     */
    public String singer;
    /**
     * 歌曲名
     */
    public String song;
    /**
     * 歌曲的地址
     */
    public String path;
    /**
     * 歌曲长度
     */
    public int duration;
    /**
     * 歌曲的大小
     */
    public long size;

    @Override
    public String toString() {
        return "Song{" +
                "singer='" + singer + '\'' +
                ", song='" + song + '\'' +
                ", path='" + path + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }
}
