package linkedincoursera.repository;

import linkedincoursera.model.coursera.Categories;
import linkedincoursera.model.coursera.Course;
import linkedincoursera.model.linkedin.LinkedinUser;
import linkedincoursera.util.DBConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by harsh on 5/12/15.
 */
@Component
public class LinkedinRepo {
    static MongoTemplate mongoTemplate = DBConnection.getConnection();
    public void insertLinkedUser(LinkedinUser user) {
        mongoTemplate.insert(user,"linkedindb");
    }
    public  List<LinkedinUser> findUserByEmail(String email) {
        List<LinkedinUser> list = mongoTemplate.find(new Query(Criteria.where("email").is(email)),LinkedinUser.class,"linkedindb");
        if(list.size()>0) return list;
        else return null;
    }
}
