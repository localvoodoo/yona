/**
 * @author Ahn Hyeok Jun
 */

package models;

import java.util.*;

import javax.persistence.*;

import models.enumeration.*;
import models.support.*;

import org.joda.time.Duration;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import utils.JodaDateUtil;

import com.avaje.ebean.Page;

@Entity
public class Post extends Model {
    private static final long serialVersionUID = 1L;
    private static Finder<Long, Post> find = new Finder<Long, Post>(Long.class, Post.class);

    public final static String ORDER_ASCENDING = "asc";
    public final static String ORDER_DESCENDING = "desc";

    public final static String ORDERING_KEY_ID = "id";
    public final static String ORDERING_KEY_TITLE = "title";
    public final static String ORDERING_KEY_AGE = "date";

    @Id
    public Long id;

    @Constraints.Required
    public String title;

    @Constraints.Required
    public String contents;

    @Constraints.Required
    @Formats.DateTime(pattern = "YYYY/MM/DD/hh/mm/ss")
    public Date date;

    public int commentCount;
    public String filePath;

    public Long authorId;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    public List<Comment> comments;

    @ManyToOne
    public Project project;

    public Post() {
        this.date = JodaDateUtil.now();
    }

    public static Post findById(Long id) {
        return find.byId(id);
    }

    /**
     * @param projectName
     *            프로젝트이름
     * @param pageNum
     *            페이지 번호
     * @param direction
     *            오름차순(asc), 내림차순(decs)
     * @param key
     *            오름차순과 내림차수를 결정하는 기준
     * @return
     */
    public static Page<Post> findOnePage(String ownerName, String projectName, int pageNum,
            Direction direction, String key) {
        SearchParams searchParam = new SearchParams()
            .add("project.owner", ownerName, Matching.EQUALS)
            .add("project.name", projectName, Matching.EQUALS);
        OrderParams orderParams = new OrderParams().add(key, direction);
        return FinderTemplate.getPage(orderParams, searchParam, find, 10, pageNum - 1);
    }

    public static Long write(Post post) {
        post.save();
        return post.id;
    }

    public static void delete(Long id) {
        find.byId(id).delete();
    }

    public static void countUpCommentCounter(Long id) {
        Post post = find.byId(id);
        post.commentCount++;
        post.update();
    }

    public Duration ago() {
        return JodaDateUtil.ago(this.date);
    }

    public static void edit(Post post) {
        Post beforePost = findById(post.id);
        post.commentCount = beforePost.commentCount;
        if (post.filePath == null) {
            post.filePath = beforePost.filePath;
        }
        post.update();
    }

    @Deprecated
    public static class Param {
        public Param() {
            this.order = ORDER_DESCENDING;
            this.key = ORDERING_KEY_ID;
            this.filter = "";
            this.pageNum = 1;
        }

        public String order;
        public String key;
        public String filter;
        public int pageNum;
    }

    public String authorName() {
        return User.findNameById(this.authorId);
    }
}
