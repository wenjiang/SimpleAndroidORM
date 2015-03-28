package com.example.pc.model;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by pc on 2015/2/27.
 */
public class Status implements Serializable {

    private static final long serialVersionUID = -4256780641070514045L;

    @Expose
    private String statusId = "";
    @Expose
    private String spaceId = "";
    @Expose
    private String userId = "";
    @Expose
    private String medias = "";
    @Expose
    private String forwardStatusId = "";
    @Expose
    private String source = "";
    @Expose
    private String subSource = "";
    @Expose
    private String text = "";
    @Expose
    private String inReplyToStatusId = "";
    @Expose
    private String inReplyToUserId = "";
    @Expose
    private String inReplyScreenName = "";
    @Expose
    private String mention = "";
    @Expose
    private String extendsJson = "";
    @Expose
    private int replyCount = 0;
    @Expose
    private int forwardCount = 0;
    @Expose
    private int favoritedCount = 0;
    @Expose
    private int likedCount = 0;
    @Expose
    private boolean liked = false;
    @Expose
    private int truncated = -1;
    @Expose
    private boolean favorited = false;
    @Expose
    private int sendState = -1;
    @Expose
    private long createTime = 0;
    @Expose
    private long updateTime = 0;

    public static void commitIndexed(SQLiteDatabase db) {
        db.execSQL("create index if not exists index_status on status(statusid)");
        db.execSQL("create index if not exists index_status_spaceid on status(spaceid)");
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMedias() {
        return medias;
    }

    public void setMedias(String medias) {
        this.medias = medias;
    }

    public String getForwardStatusId() {
        return forwardStatusId;
    }

    public void setForwardStatusId(String forwardStatusId) {
        this.forwardStatusId = forwardStatusId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSubSource() {
        return subSource;
    }

    public void setSubSource(String subSource) {
        this.subSource = subSource;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public void setInReplyToStatusId(String inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
    }

    public String getInReplyToUserId() {
        return inReplyToUserId;
    }

    public void setInReplyToUserId(String inReplyToUserId) {
        this.inReplyToUserId = inReplyToUserId;
    }

    public String getInReplyScreenName() {
        return inReplyScreenName;
    }

    public void setInReplyScreenName(String inReplyScreenName) {
        this.inReplyScreenName = inReplyScreenName;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public String getExtendsJson() {
        return extendsJson;
    }

    public void setExtendsJson(String extendsJson) {
        this.extendsJson = extendsJson;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public int getForwardCount() {
        return forwardCount;
    }

    public void setForwardCount(int forwardCount) {
        this.forwardCount = forwardCount;
    }

    public int getFavoritedCount() {
        return favoritedCount;
    }

    public void setFavoritedCount(int favoritedCount) {
        this.favoritedCount = favoritedCount;
    }

    public int getLikedCount() {
        return likedCount;
    }

    public void setLikedCount(int likedCount) {
        this.likedCount = likedCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public int getTruncated() {
        return truncated;
    }

    public void setTruncated(int truncated) {
        this.truncated = truncated;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public int getSendState() {
        return sendState;
    }

    public void setSendState(int sendState) {
        this.sendState = sendState;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
