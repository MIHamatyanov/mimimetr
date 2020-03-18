package mimimetr.controller;

import mimimetr.domain.Cat;
import mimimetr.repo.CatRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Controller
public class MainController {
    private CatRepository catRepository;
    private List<Cat> availableCats;
    private static final Random random = new Random();

    public MainController(CatRepository catRepository) {
        this.catRepository = catRepository;
    }

    @PostConstruct
    public void initialize() {
        availableCats = catRepository.findAll();
    }

    @GetMapping("/")
    public String getCatsToVote(Model model) {
        int size = availableCats.size();

        if (size < 2) {
            return "redirect:/top";
        }

        int firstCatIndex = random.nextInt(size);
        int secondCatIndex = random.nextInt(size);
        while (firstCatIndex == secondCatIndex) {
            secondCatIndex = random.nextInt(size);
        }

        Cat firstCat = availableCats.get(firstCatIndex);
        Cat secondCat = availableCats.get(secondCatIndex);
        availableCats.remove(firstCat);
        availableCats.remove(secondCat);

        model.addAttribute("firstCat", firstCat);
        model.addAttribute("secondCat", secondCat);

        return "VotePage";
    }

    @PostMapping("/vote")
    public String vote(@RequestParam Long id) {
        Optional<Cat> votedCat = catRepository.findById(id);
        if (votedCat.isPresent()) {
            Cat cat = votedCat.get();
            cat.setRating(cat.getRating() + 1);
            catRepository.save(cat);
        }

        return "redirect:/";
    }

    @GetMapping("/top")
    public String topCats(Model model) {
        List<Cat> cats = catRepository.findAll();
        cats.sort((o1, o2) -> o2.getRating() - o1.getRating());

        List<Cat> top10Cats = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            top10Cats.add(cats.get(i));
        }

        model.addAttribute("cats", top10Cats);

        return "TopCats";
    }

    @GetMapping("/addCat")
    public String addCatPage() {
        return "AddCat";
    }

    @PostMapping("/addCat")
    public String addCat(@RequestParam String name, @RequestParam String photoUrl, Model model) {
        Cat cat = new Cat();
        cat.setName(name);
        cat.setPhotoUrl(photoUrl);
        cat.setRating(0);

        cat = catRepository.save(cat);

        availableCats.add(cat);
        model.addAttribute("message", "Котик добавлен");

        return "AddCat";
    }
}
