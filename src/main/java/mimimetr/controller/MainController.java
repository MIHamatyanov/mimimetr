package mimimetr.controller;

import mimimetr.domain.Cat;
import mimimetr.domain.User;
import mimimetr.repo.CatRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Controller
public class MainController {
    private CatRepository catRepository;
    private static final Random random = new Random();
    private static final int MIN_CATS_COUNT = 10;

    public MainController(CatRepository catRepository) {
        this.catRepository = catRepository;
    }

    @GetMapping("/")
    public String getCatsToVote(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            user = new User();
        }
        session.setAttribute("user", user);

        List<Cat> availableCats = catRepository.findAll();
        if (availableCats.size() < MIN_CATS_COUNT) {
            model.addAttribute("notEnoughCatsMessage", "Для голосования должно быть минимум 10 котиков");
            return "ErrorPage";
        }

        user.getVotedCats().forEach(availableCats::remove);

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

        model.addAttribute("firstCat", firstCat);
        model.addAttribute("secondCat", secondCat);

        return "VotePage";
    }

    @PostMapping("/vote")
    public String vote(@RequestParam Long votedId, @RequestParam Long secondId, HttpServletRequest request) {
        Optional<Cat> votedCat = catRepository.findById(votedId);
        Optional<Cat> secondCat = catRepository.findById(secondId);
        if (votedCat.isPresent()) {
            Cat cat = votedCat.get();
            cat.setRating(cat.getRating() + 1);
            catRepository.save(cat);
        }

        User user = (User) request.getSession().getAttribute("user");
        if (votedCat.isPresent() && secondCat.isPresent()) {
            user.addVotedCat(votedCat.get());
            user.addVotedCat(secondCat.get());
        }
        request.getSession().setAttribute("user", user);

        return "redirect:/";
    }

    @GetMapping("/top")
    public String topCats(Model model) {
        List<Cat> cats = catRepository.findAll();
        cats.sort((o1, o2) -> o2.getRating() - o1.getRating());

        int topCount = Math.min(cats.size(), MIN_CATS_COUNT);
        List<Cat> topCatsList = new ArrayList<>();
        for (int i = 0; i < topCount; i++) {
            topCatsList.add(cats.get(i));
        }

        model.addAttribute("cats", topCatsList);

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
        catRepository.save(cat);

        model.addAttribute("message", "Котик добавлен");

        return "AddCat";
    }
}
