package io.keepcoding.pickandgol.model;

import java.io.Serializable;
import java.util.List;


/**
 * This class contains the info about an Event managed by the client app
 */
public class Event implements Collectible, Serializable {

    private String id;
    private String name;
    private String date;
    private String description;
    private String photoUrl;
    private String category;
    private List<String> pubs;


    public Event(String id, String name, String date, String description,
                 String photoUrl, String category, List<String> pubs) {

        this.id = id;
        this.name = name;
        this.date = date;
        this.description = description;
        this.photoUrl = photoUrl;
        this.category = category;
        this.pubs = pubs;
    }

    public Event() {
    }

    // Getters:

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getPubs() {
        return pubs;
    }

    // Setters
    public Event setId(final String id) {
        this.id = id;
        return this;
    }

    public Event setName(final String name) {
        this.name = name;
        return this;
    }

    public Event setDate(final String date) {
        this.date = date;
        return this;
    }

    public Event setDescription(final String description) {
        this.description = description;
        return this;
    }

    public Event setPhotoUrl(final String photoUrl) {
        this.photoUrl = photoUrl;
        return this;
    }

    public Event setCategory(final String category) {
        this.category = category;
        return this;
    }

    public Event setPubs(final List<String> pubs) {
        this.pubs = pubs;
        return this;
    }
}
