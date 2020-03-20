package mimimetr.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private String id;
    private List<Cat> votedCats;

    public User() {
        this.id = UUID.randomUUID().toString();
        votedCats = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public List<Cat> getVotedCats() {
        return votedCats;
    }

    public void addVotedCat(Cat cat) {
        votedCats.add(cat);
    }
}
