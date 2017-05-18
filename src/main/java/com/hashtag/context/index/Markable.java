package com.hashtag.context.index;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Markable implements Serializable{

    private Set<String> entities;
    private Set<String> nouns;
    private Set<String> verbs;

    public Markable() {
        this.entities = new HashSet<>();
        this.verbs = new HashSet<>();
        this.nouns = new HashSet<>();
    }

    public Set<String> getEntities() {
        return entities;
    }

    public Set<String> getNouns() {
        return nouns;
    }

    public Set<String> getVerbs() {
        return verbs;
    }

    public void appendEntity(String entity) {
        this.entities.add(entity);
    }

    public void appendNoun(String noun) {
        this.nouns.add(noun);
    }

    public void appendVerb(String verb) {
        this.verbs.add(verb);
    }

}
