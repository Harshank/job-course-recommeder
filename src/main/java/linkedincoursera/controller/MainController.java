package linkedincoursera.controller;


import linkedincoursera.model.careerbuilder.JobSearchResult;
import linkedincoursera.model.coursera.Categories;
import linkedincoursera.model.coursera.Course;
import linkedincoursera.model.stackoverflow.QuestionCountSOF;
import linkedincoursera.model.udacity.UdacityCourse;
import linkedincoursera.repository.CourseraRepo;
import linkedincoursera.repository.UdacityRepo;
import linkedincoursera.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.social.linkedin.api.Education;
import org.springframework.social.linkedin.api.LinkedInProfile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@PropertySource(value = {"classpath:/properties/application.properties"},ignoreResourceNotFound = false)
public class MainController {
    static boolean toBeInserted= true;
    @Value("${api.key}")
    private String apikey;
    @Value("${api.secret}")
    private String apisecret;
    private String redirect_uri = "http%3A%2F%2Flocalhost%3A8080%2Fauth%2Flinkedin";
    @Autowired
    AuthorizationService authService;
    @Autowired
    LinkedinService linkedinService;
    @Autowired
    CourseraService courseraService;
    @Autowired
    UdacityService udacityService;
    @Autowired
    public StackoverflowService stackoverflowService;
    @Autowired
    public CareerBuilderService careerBuilderService;
    @Autowired
    public CourseraRepo courseraRepo;
    @Autowired
    public UdacityRepo udacityRepo;

    static String access_token="";

    @RequestMapping("/")
    public String index() {
        String url = "https://www.linkedin.com/uas/oauth2/authorization?response_type=code&client_id="+apikey+"&redirect_uri="+redirect_uri+"&state=987654321&scope=r_fullprofile";
        return "redirect:"+url;
    }

    @RequestMapping("/login")
    public String login() {
        return "greeting";
    }
    @RequestMapping("/main")
    public String homepage(Model model) {
        getDetails(model);
        return "main";
    }

    @RequestMapping("/auth/linkedin")
    public String authenticate(Model model, @RequestParam String code, @RequestParam String state) {
        access_token = authService.authorizeLinkedinByPost(code, redirect_uri, apikey, apisecret);
        getDetails(model);
        return "main";
    }
    @RequestMapping("/recommendation")
    public String recommendScreen(Model model) {
        LinkedInProfile basicProf = linkedinService.getLinkedInProfile();
        List<String> skillSet = linkedinService.getSkillSet();
        model.addAttribute("skills",skillSet);
        model.addAttribute("userName", basicProf.getFirstName() + " " + basicProf.getLastName());
        return "recommendations";
    }

    public List<Course> recommendCoursera(List<String> skillSet) {
        List<Course> courses = new ArrayList<Course>();

        try {
            if(skillSet.size() > 3) {
                skillSet = skillSet.subList(0, 3);
            }

            ArrayList<Course> initialCourses = new ArrayList<Course>();
            for(String skill : skillSet) {
                List<Course> courseraCourses = courseraService.fetchCourses(skill);
                initialCourses.addAll(courseraCourses);
            }

            for (Course course : initialCourses) {
                if(course.getLanguage().equals("en")) {
                    courses.add(course);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

        if(courses.size() > 5) {
            courses = courses.subList(0, 5);
        }

        return courses;
    }

    public List<UdacityCourse> recommendUdacity(List<String> skillSet) {
        List<UdacityCourse> courses = new ArrayList<UdacityCourse>();

        try {
            if(skillSet.size() > 3) {
                skillSet = skillSet.subList(0, 3);
            }

            for(String skill : skillSet) {
                List<UdacityCourse> udacityCourses = udacityService.fetchCourses();
                List<UdacityCourse> filteredUdacityCourses = UdacityService.searchCourses(udacityCourses, skill);
                courses.addAll(udacityCourses);
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
        if(courses.size() > 5) {
            courses = courses.subList(0, 5);
        }

        return courses;
    }

    public List<JobSearchResult> recommendJobs(List<String> skillSet) {
        List<JobSearchResult> jobs = new ArrayList<JobSearchResult>();

        try {
            if(skillSet.size() > 3) {
                skillSet = skillSet.subList(0, 3);
            }

            for(String skill : skillSet) {
                List<JobSearchResult> jobSearchResults = careerBuilderService.fetchJobs(skill);
                jobs.addAll(jobSearchResults);
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
        if(jobs.size() > 10) {
            jobs = jobs.subList(0, 10);
        }

        return jobs;
    }

    public ArrayList<String> listSkillsByPopularity(List<String> skillSet) {
        ArrayList<String> orderedSkillSet = new ArrayList<String>();

        ArrayList<String> skills = new ArrayList<String>();
        for(String skill : skillSet) {
            skills.add(skill.toLowerCase());
        }

        try {
            List<QuestionCountSOF> tags = stackoverflowService.fetchMostAskedQuestionsStackoverflow();
            for(QuestionCountSOF tag : tags) {
                if(skills.contains(tag.getName().toLowerCase())) {
                    orderedSkillSet.add(tag.getName());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
        return orderedSkillSet;
    }

    public void getDetails(Model model) {
        try {
            linkedinService.setApi(access_token);
            LinkedInProfile basicProf = linkedinService.getLinkedInProfile();
            String profilePhotoUrl = linkedinService.getLinkedInProfile().getProfilePictureUrl();
            List<String> skillSet = linkedinService.getSkillSet();
            List<Education> educationsList = linkedinService.getEducations();
            List<Course> courses = courseraService.fetchCourses();
            List<Categories> categoryList = courseraService.getCategoriesList();

            model.addAttribute("userName", basicProf.getFirstName() + " " + basicProf.getLastName());
            model.addAttribute("profilePhotoUrl", profilePhotoUrl);
            model.addAttribute("education", educationsList);
            model.addAttribute("skills", skillSet);
            model.addAttribute("summary", linkedinService.getLinkedInProfile().getSummary());
            model.addAttribute("courses", courses);
            model.addAttribute("positions", linkedinService.getLinkedInProfileFull().getPositions());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value ="/recommendations/courses", method=RequestMethod.GET)
    @ResponseBody
    public List recommendCourses(Model model) {
        return null;
    }
    @RequestMapping("/jobs/{skill}")
    @ResponseBody
    public List recommendJobs(@PathVariable String skill) {
        try {
            List<JobSearchResult> jobs = careerBuilderService.fetchJobs(skill);
            System.out.println("CAREERBUILDER:");
            for(JobSearchResult job : jobs) {
                System.out.println(job.getJobTitle());
            }
            ArrayList al = new ArrayList();
            al.add(jobs);
            return al;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    @RequestMapping(value ="/recommendations/{skill}", method=RequestMethod.GET)
//    @ResponseBody
    public String recommendCourses(Model model, @PathVariable String skill) {
        System.out.println("**********************************");
        ArrayList al = new ArrayList();
        try {
            List<String> skillsByPopularity = listSkillsByPopularity(linkedinService.getSkillSet());

            ArrayList allCourses = new ArrayList();
            List<Course> recommendedCoursera = recommendCoursera(skillsByPopularity);
            List<UdacityCourse> recommendedUdacity = recommendUdacity(skillsByPopularity);
            allCourses.addAll(recommendedCoursera);
            allCourses.addAll(recommendedUdacity);

            System.out.println("COURSERA:");
            for (Course course : recommendedCoursera) {
                System.out.println(course.getName());
            }

            System.out.println("UDACITY:");
            for(UdacityCourse course : recommendedUdacity) {
                System.out.println(course.getTitle());
            }

            model.addAttribute("courses", allCourses);
            return "main";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value ="/recommendations/jobs", method=RequestMethod.GET)
    @ResponseBody
    public List recommendJobs(Model model) {
        try {
            List<String> skillsByPopularity = listSkillsByPopularity(linkedinService.getSkillSet());
            List<JobSearchResult> recommendedJobs = recommendJobs(skillsByPopularity);

            System.out.println("CAREERBUILDER:");
            for(JobSearchResult job : recommendedJobs) {
                System.out.println(job.getJobTitle());
            }

            model.addAttribute("jobs", recommendedJobs);
            return recommendedJobs;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<JobSearchResult>();
        }
    }
}