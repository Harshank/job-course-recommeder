package linkedincoursera.services;

import linkedincoursera.model.udacity.UdacityCourse;
import linkedincoursera.model.udacity.UdacityCourseList;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by melissa on 5/10/15.
 */
@Component
public class UdacityService {

    private static final String UDACITY_URL = "https://www.udacity.com/public-api/v0/courses";

    public RestTemplate restTemplate = new RestTemplate();

    public List<UdacityCourse> fetchCourses() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> stringHttpEntity = new HttpEntity<String>(headers);
        try{
            ResponseEntity<UdacityCourseList> response = restTemplate.exchange(
                    UDACITY_URL, HttpMethod.GET, stringHttpEntity, UdacityCourseList.class);
            UdacityCourseList courseList = response.getBody();
            return courseList.getCourses();
        }catch(HttpClientErrorException e){
            throw new Exception(e);
        }
    }

    public static List<UdacityCourse> searchCourses(List<UdacityCourse> courses, String queryValue) {
        ArrayList<UdacityCourse> filteredCourses = new ArrayList<UdacityCourse>();
        for(UdacityCourse course : courses) {
            String title = course.getTitle().toLowerCase();
            String short_summary = course.getShort_summary();
            String summary = course.getTitle().toLowerCase();
            String skill = queryValue.toLowerCase();
            if(title.contains(skill) || summary.contains(queryValue) || short_summary.contains(queryValue)) {
                if (!(title.contains("intro") && title.contains("computer science")) && !(title.contains("intro") && title.contains(skill))) {
                    filteredCourses.add(course);
                }
            }
        }
        return filteredCourses;
    }
}
