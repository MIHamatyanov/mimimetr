package mimimetr.controller;

import mimimetr.domain.Cat;
import mimimetr.repo.CatRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Controller
public class MainController {
    private CatRepository catRepository;
    private List<Cat> availableCats;
    private static final Random radom = new Random();

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

        if (size == 0) {
            return "redirect:/top";
        }

        int firstCatIndex = radom.nextInt(size);
        int secondCatIndex = radom.nextInt(size);
        while (firstCatIndex == secondCatIndex) {
            secondCatIndex = radom.nextInt(size);
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

        model.addAttribute("cats", cats);

        return "TopCats";
    }
}
